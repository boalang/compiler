/*
 * Copyright 2017, Anthony Urso, Hridesh Rajan, Robert Dyer,
 *                 Iowa State University of Science and Technology
 *                 and Bowling Green State University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package boa.compiler.visitors;

import java.io.IOException;
import java.util.*;

import boa.aggregators.AggregatorSpec;
import boa.compiler.SymbolTable;
import boa.compiler.TypeCheckException;
import boa.compiler.ast.*;
import boa.compiler.ast.expressions.*;
import boa.compiler.ast.literals.*;
import boa.compiler.ast.statements.*;
import boa.compiler.ast.types.*;
import boa.compiler.transforms.VisitorDesugar;
import boa.types.*;
import boa.types.proto.CodeRepositoryProtoTuple;

/**
 * Prescan the program and check that all variables are consistently typed.
 *
 * @author anthonyu
 * @author rdyer
 * @author ankuraga
 * @author rramu
 */
public class TypeCheckingVisitor extends AbstractVisitorNoReturn<SymbolTable> {
	BoaType lastRetType;

	/**
	 * This verifies visitors have at most 1 before/after for a type.
	 *
	 * @author rdyer
	 */
	protected class VisitorCheckingVisitor extends AbstractVisitorNoArgNoRet {
		protected Set<String> befores = new HashSet<String>();
		protected Set<String> afters = new HashSet<String>();
		protected boolean nested = false;

		/** {@inheritDoc} */
		@Override
		public void initialize() {
			befores.clear();
			afters.clear();
			nested = false;
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final VisitorExpression n) {
			// dont nest
			if (nested)
				return;
			nested = true;
			n.getBody().accept(this);
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final VisitStatement n) {
			final Set<String> s = n.isBefore() ? befores : afters;

			if (n.hasComponent()) {
				final Identifier id = (Identifier)n.getComponent().getType();
				final String token = id.getToken();
				if (s.contains(token))
					throw new TypeCheckException(id, "The type '" + token + "' already has a '" + (n.isBefore() ? "before" : "after") + "' visit statement");
				s.add(token);
			} else if (n.getIdListSize() > 0) {
				for (final Identifier id : n.getIdList()) {
					final String token = id.getToken();
					if (s.contains(token))
						throw new TypeCheckException(id, "The type '" + token + "' already has a '" + (n.isBefore() ? "before" : "after") + "' visit statement");
					s.add(token);
				}
			}
		}
	}

	protected class TraversalCheckingVisitor extends AbstractVisitorNoArgNoRet {
		protected Set<String> befores = new HashSet<String>();
		protected boolean nested = false;

		/** {@inheritDoc} */
		@Override
		public void initialize() {
			befores.clear();
			nested = false;
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final TraversalExpression n) {
			// dont nest
			if (nested)
				return;
			nested = true;
			n.getBody().accept(this);
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final TraverseStatement n) {
			if (n.hasComponent()) {
				final Identifier id = (Identifier)n.getComponent().getType();
				final String token = id.getToken();
				if (befores.contains(token))
					throw new TypeCheckException(id, "The type '" + token + "' already has a traverse statement");
				befores.add(token);
			} else if (n.getIdListSize() > 0) {
				for (final Identifier id : n.getIdList()) {
					final String token = id.getToken();
					if (befores.contains(token))
						throw new TypeCheckException(id, "The type '" + token + "' already has a traverse statement");
					befores.add(token);
				}
			}
		}

	}

	protected class FixPCheckingVisitor extends AbstractVisitorNoArgNoRet {
		protected Set<String> befores = new HashSet<String>();
		protected boolean nested = false;

		/** {@inheritDoc} */
		@Override
		public void initialize() {
			befores.clear();
			nested = false;
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final FixPExpression n) {
			// dont nest
			if (nested)
				return;
			nested = true;
			n.getBody().accept(this);
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final FixPStatement n) {
			final Identifier id = (Identifier)n.getParam1().getType();
			final String token = id.getToken();
			befores.add(token);

			final Identifier id1 = (Identifier)n.getParam2().getType();
			final String token1 = id1.getToken();
			befores.add(token1);
		}

	}

	/**
	 * This does type checking of function bodies to ensure the
	 * returns are the correct type.
	 *
	 * @author rdyer
	 */
	protected class ReturnCheckingVisitor extends AbstractVisitorNoArgNoRet {
		protected BoaType retType;

		/**
		 * Initialize the visitor with the function's return type.
		 *
		 * @param retType the function's return type
		 */
		public void initialize(final BoaType retType) {
			initialize();
			this.retType = retType;
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final Block n) {
			super.visit(n);

			// look for unreachable code (any statement after a return or stop)
			for (int i = 0; i < n.getStatementsSize() - 1; i++) {
				final Statement s = n.getStatement(i);
				if (s instanceof ReturnStatement || s instanceof StopStatement)
					throw new TypeCheckException(n.getStatement(i + 1), "unreachable code");
			}
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final FunctionExpression n) {
			// dont nest
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final ReturnStatement n) {
			if (n.hasExpr() && retType == null)
				throw new TypeCheckException(n.getExpr(), "returning values not allowed by function's type");
			if (!(retType instanceof BoaAny) && !n.hasExpr())
				throw new TypeCheckException(n, "must return a value of type '" + retType + "'");
			if (!(retType instanceof BoaAny) && !retType.assigns(n.getExpr().type))
				throw new TypeCheckException(n.getExpr(), "incompatible types: required '" + retType + "', found '" + n.getExpr().type + "'");
		}
	}

	protected final VisitorCheckingVisitor visitorChecker = new VisitorCheckingVisitor();
	protected final TraversalCheckingVisitor traversalChecker = new TraversalCheckingVisitor();
	protected final FixPCheckingVisitor fixPChecker = new FixPCheckingVisitor();
	protected final ReturnCheckingVisitor returnFinder = new ReturnCheckingVisitor();
	protected final CallFindingVisitor callFinder = new CallFindingVisitor();

	protected boolean hasEmit = false;

	/** {@inheritDoc} */
	@Override
	public void visit(Start n, SymbolTable env) {
		n.env = env;
		super.visit(n, env);

		if (!hasEmit)
			throw new TypeCheckException(n, "No emit statements detected - there will be no output generated");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Program n, final SymbolTable env) {
		SymbolTable st;

		try {
			st = env.cloneNonLocals();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		n.env = st;

		for (final Statement s : n.getStatements())
			s.accept(this, env);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Call n, final SymbolTable env) {
		n.env = env;

		if (n.getArgsSize() > 0)
			n.type = new BoaTuple(this.check(n.getArgs(), env));
		else
			n.type = new BoaArray();
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Comparison n, final SymbolTable env) {
		n.env = env;

		n.getLhs().accept(this, env);
		n.type = n.getLhs().type;

		if (n.hasRhs()) {
			n.getRhs().accept(this, env);

			if (!n.getRhs().type.compares(n.type))
				throw new TypeCheckException(n.getRhs(), "incompatible types for comparison: required '" + n.type + "', found '" + n.getRhs().type + "'");

			if (n.type instanceof BoaString || n.type instanceof BoaMap || n.type instanceof BoaSet || n.type instanceof BoaStack || !(n.type instanceof BoaScalar))
				if (!n.getOp().equals("==") && !n.getOp().equals("!="))
					throw new TypeCheckException(n.getLhs(), "invalid comparison operator '" + n.getOp() + "' for type '" + n.type + "'");

			n.type = new BoaBool();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Component n, final SymbolTable env) {
		n.env = env;

		n.getType().accept(this, env);

		if (n.hasIdentifier()) {
			final String id = n.getIdentifier().getToken();

			if (!env.getShadowing()) {
				if (env.hasGlobal(id))
					throw new TypeCheckException(n.getIdentifier(), "name conflict: constant '" + id + "' already exists");
				if (env.hasLocal(id))
					throw new TypeCheckException(n.getIdentifier(), "variable '" + id + "' already declared as '" + env.get(id) + "'");
			}

			n.type = new BoaName(n.getType().type, n.getIdentifier().getToken());
			try {
				env.set(n.getIdentifier().getToken(), n.getType().type);
			} catch (final Exception e) {
				throw new TypeCheckException(n, e.getMessage(), e);
			}
			n.getIdentifier().accept(this, env);
		} else {
			n.type = n.getType().type;
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Composite n, final SymbolTable env) {
		n.env = env;

		if (n.getPairsSize() > 0) {
			n.type = checkPairs(n.getPairs(), env);
		} else if (n.getExprsSize() > 0) {
			final List<BoaType> types = check(n.getExprs(), env);

			if (!(checkTupleArray(types) == true)) {
				n.type = new BoaArray(types.get(0));
			} else {
				n.type = new BoaTuple(types);
			}
		} else {
			n.type = new BoaMap(new BoaAny(), new BoaAny());
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Conjunction n, final SymbolTable env) {
		n.env = env;

		n.getLhs().accept(this, env);
		final BoaType ltype = n.getLhs().type;
		n.type = ltype;

		if (n.getRhsSize() > 0) {
			if (!(ltype instanceof BoaBool))
				throw new TypeCheckException(n.getLhs(), "incompatible types for conjunction: required 'bool', found '" + ltype + "'");

			for (final Comparison c : n.getRhs()) {
				c.accept(this, env);
				if (!(c.type instanceof BoaBool))
					throw new TypeCheckException(c, "incompatible types for conjunction: required 'bool', found '" + c.type + "'");
			}

			n.type = new BoaBool();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Factor n, final SymbolTable env) {
		n.env = env;

		BoaType type = null;

		if (n.getOpsSize() > 0) {
			for (final Node node : n.getOps()) {
				if (node instanceof Selector) {
					if (type == null) {
						n.getOperand().accept(this, env);
						type = n.getOperand().type;
					}

					if (type instanceof BoaName)
						type = ((BoaName) type).getType();

					env.setOperandType(type);
					node.accept(this, env);
					type = node.type;
				} else if (node instanceof Index) {
					if (type == null) {
						n.getOperand().accept(this, env);
						type = n.getOperand().type;
					}

					node.accept(this, env);
					final BoaType index = node.type;

					if (type instanceof BoaArray) {
						if (!(index instanceof BoaInt))
							throw new TypeCheckException(node, "invalid index type '" + index + "' for indexing into '" + type + "'");

						type = ((BoaArray) type).getType();
					} else if (type instanceof BoaProtoList) {
						if (!(index instanceof BoaInt))
							throw new TypeCheckException(node, "invalid index type '" + index + "' for indexing into '" + type + "'");

						type = ((BoaProtoList) type).getType();
					} else if (type instanceof BoaMap) {
						if (!((BoaMap) type).getIndexType().assigns(index))
							throw new TypeCheckException(node, "invalid index type '" + index + "' for indexing into '" + type + "'");

						type = ((BoaMap) type).getType();
					} else {
						throw new TypeCheckException(node, "type '" + type + "' does not allow index operations");
					}
				} else {
					node.accept(this, env);
					n.getOperand().env = env;

					final List<BoaType> formalParameters = this.check((Call) node, env);
					try {
						type = env.getFunction(((Identifier)n.getOperand()).getToken(), formalParameters).erase(formalParameters);
					} catch (final ClassCastException e) {
						throw new TypeCheckException(n.getOperand(), "Function declarations must be assigned to a variable and can not be used anonymously", e);
					} catch (final RuntimeException e) {
						throw new TypeCheckException(n.getOperand(), e.getMessage(), e);
					}

					if (formalParameters.size() == 1 && ((Identifier)n.getOperand()).getToken().equals("getvalue")) {
						type = lastRetType;
					}
				}
				node.type = type;
			}
		} else {
			n.getOperand().accept(this, env);
			type = n.getOperand().type;
		}

		n.type = type;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Identifier n, final SymbolTable env) {
		n.env = env;

		if (env.hasType(n.getToken()))
			n.type = SymbolTable.getType(n.getToken());
		else
			try {
				n.type = env.get(n.getToken());
			} catch (final RuntimeException e) {
				if (!env.hasGlobalFunction(n.getToken()) && !env.hasLocalFunction(n.getToken()))
					throw new TypeCheckException(n, "invalid identifier '" + n.getToken() + "'", e);
				n.type = env.getFunction(n.getToken());
			}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Index n, final SymbolTable env) {
		n.env = env;

		n.getStart().accept(this, env);
		n.type = n.getStart().type;

		if (n.getStart().type == null)
			throw new RuntimeException();

		if (n.hasEnd()) {
			if (!(n.getStart().type instanceof BoaInt))
				throw new TypeCheckException(n.getStart(), "invalid type '" + n.getStart().type + "' for slice expression");

			n.getEnd().accept(this, env);
			if (!(n.getEnd().type instanceof BoaInt))
				throw new TypeCheckException(n.getEnd(), "invalid type '" + n.getEnd().type + "' for slice expression");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Pair n, final SymbolTable env) {
		n.env = env;
		n.getExpr1().accept(this, env);
		n.getExpr2().accept(this, env);
		n.type = new BoaMap(n.getExpr2().type, n.getExpr1().type);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Selector n, final SymbolTable env) {
		n.env = env;

		final String selector = n.getId().getToken();
		BoaType type = env.getOperandType();

		if (type instanceof BoaProtoMap) {
			if (!((BoaProtoMap) type).hasAttribute(selector))
				throw new TypeCheckException(n.getId(), type + " has no member named '" + selector + "'");
		} else if (type instanceof BoaTuple) {
			if (!((BoaTuple) type).hasMember(selector))
				throw new TypeCheckException(n.getId(), "'" + type + "' has no member named '" + selector + "'");

			if (type instanceof CodeRepositoryProtoTuple && selector.equals("revisions")) {
				throw new TypeCheckException(n.getId(), "Accessing " + "'" + selector + "' of '" + type + "' is prohibited! "
						+ "Use functions 'getrevisionscount(CodeRepository)' and 'getrevision(CodeRepository, int)' instead! "
						+ "E.g., revision := getrevision(cr, 0); or for (i := 0; i < getrevisionscount(cr); i++) revision := getrevision(cr, i);");
			}

			type = ((BoaTuple) type).getMember(selector);
			if (type instanceof BoaName)
				type = ((BoaName) type).getType();
		} else if (type instanceof BoaEnum) {
			if (!((BoaEnum) type).hasMember(selector))
				throw new TypeCheckException(n.getId(), "'" + type + "' has no member named '" + selector + "'");
			type = ((BoaEnum) type).getMember(selector);
		}
		else {
			throw new TypeCheckException(n, "invalid operand type '" + type + "' for member selection");
		}

		n.type = type;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Term n, final SymbolTable env) {
		n.env = env;

		n.getLhs().accept(this, env);
		final BoaType accepts = n.getLhs().type;
		n.type = accepts;

		if (n.getRhsSize() > 0) {
			BoaScalar type = (BoaScalar) accepts;

			for (int i = 0; i < n.getRhsSize(); i++) {
				final Factor f = n.getRhs(i);
				f.accept(this, env);
				try {
					type = type.arithmetics(f.type);
				} catch (final Exception e) {
					throw new TypeCheckException(f, "type '" + f.type + "' does not support the '" + n.getOp(i) + "' operator", e);
				}
			}

			n.type = type;
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final UnaryFactor n, final SymbolTable env) {
		n.env = env;
		n.getFactor().accept(this, env);
		n.type = n.getFactor().type;
	}

	//
	// statements
	//
	/** {@inheritDoc} */
	@Override
	public void visit(final AssignmentStatement n, final SymbolTable env) {
		n.env = env;

		try {
			n.getLhs().accept(this, env);
		} catch (final TypeCheckException e) {
			if (!e.getMessage().startsWith("expected a call to function"))
				throw e;
		}

		n.getRhs().accept(this, env);

		if (!(n.getLhs().type instanceof BoaArray && n.getRhs().type instanceof BoaTuple))
			if (!n.getLhs().type.assigns(n.getRhs().type))
				throw new TypeCheckException(n.getRhs(), "incompatible types for assignment: required '" + n.getLhs().type + "', found '" + n.getRhs().type + "'");

		if (n.getLhs().getOperand().type instanceof BoaProtoTuple && n.getLhs().getOpsSize() > 0)
			throw new TypeCheckException(n.getLhs(), "assignment not allowed to input-derived type '" + n.getLhs().getOperand().type + "'");

		final Factor f = n.getRhs().getLhs().getLhs().getLhs().getLhs().getLhs();
		if (f.getOperand() instanceof Identifier && f.getOpsSize() == 0 && env.hasType(((Identifier)f.getOperand()).getToken()))
			throw new TypeCheckException(n.getRhs(), "type '" + f.getOperand().type + "' is not a value and can not be assigned");

		n.type = n.getLhs().type;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Block n, final SymbolTable env) {
		SymbolTable st;

		try {
			st = env.cloneNonLocals();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		n.env = st;

		for (final Node s : n.getStatements())
			s.accept(this, st);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final BreakStatement n, final SymbolTable env) {
		n.env = env;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ContinueStatement n, final SymbolTable env) {
		n.env = env;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final DoStatement n, final SymbolTable env) {
		SymbolTable st;

		try {
			st = env.cloneNonLocals();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		n.env = st;

		n.getCondition().accept(this, st);
		n.getBody().accept(this, st);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final EmitStatement n, final SymbolTable env) {
		hasEmit = true;

		n.env = env;

		n.getId().accept(this, env);
		final String id = n.getId().getToken();
		final BoaType type = n.getId().type;

		if (type == null)
			throw new TypeCheckException(n.getId(), "emitting to undeclared output variable '" + id + "'");
		if (!(type instanceof BoaTable))
			throw new TypeCheckException(n.getId(), "emitting to non-output variable '" + id + "'");

		final BoaTable t = (BoaTable) type;

		if (n.getIndicesSize() != t.countIndices())
			throw new TypeCheckException(n.getId(), "output variable '" + id + "': incorrect number of indices for '" + id + "': required " + t.countIndices() + ", found " + n.getIndicesSize());

		if (n.getIndicesSize() > 0)
			for (int i = 0; i < n.getIndicesSize() && i < t.countIndices(); i++) {
				n.getIndice(i).accept(this, env);
				if (!t.getIndex(i).assigns(n.getIndice(i).type))
					throw new TypeCheckException(n.getIndice(i), "output variable '" + id + "': incompatible types for index '" + i + "': required '" + t.getIndex(i) + "', found '" + n.getIndice(i).type + "'");
			}

		n.getValue().accept(this, env);
		if (!t.accepts(n.getValue().type))
			throw new TypeCheckException(n.getValue(), "output variable '" + id + "': incompatible emit value types: required '" + t.getType() + "', found '" + n.getValue().type + "'");

		if (n.hasWeight()) {
			if (t.getWeightType() == null)
				throw new TypeCheckException(n.getWeight(), "output variable '" + id + "': emit contains a weight, but variable not declared with a weight");

			n.getWeight().accept(this, env);

			if (!t.acceptsWeight(n.getWeight().type))
				throw new TypeCheckException(n.getWeight(), "output variable '" + id + "': incompatible types for weight: required '" + t.getWeightType() + "', found '" + n.getWeight().type + "'");
		} else if (t.getWeightType() != null && !t.canOmitWeight())
			throw new TypeCheckException(n, "output variable '" + id + "': emit must specify a weight");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ExprStatement n, final SymbolTable env) {
		n.env = env;

		n.getExpr().accept(this, env);
		n.type = n.getExpr().type;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ExistsStatement n, final SymbolTable env) {
		final Expression e = checkQuantifier(n, n.getVar(), n.getCondition(), n.getBody(), "exists", env);
		if (e != n.getCondition())
			n.setCondition(e);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ForeachStatement n, final SymbolTable env) {
		final Expression e = checkQuantifier(n, n.getVar(), n.getCondition(), n.getBody(), "foreach", env);
		if (e != n.getCondition())
			n.setCondition(e);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final IfAllStatement n, final SymbolTable env) {
		final Expression e = checkQuantifier(n, n.getVar(), n.getCondition(), n.getBody(), "ifall", env);
		if (e != n.getCondition())
			n.setCondition(e);
	}

	protected Expression checkQuantifier(final Node n, final Component c, Expression e, final Block b, final String kind, final SymbolTable env) {
		SymbolTable st;
		try {
			st = env.cloneNonLocals();
		} catch (final IOException ex) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", ex);
		}

		n.env = st;

		c.accept(this, st);

		e.accept(this, st);

		if (!(e.type instanceof BoaBool)) {
			e = new Expression(
					new Conjunction(
						new Comparison(
							new SimpleExpr(
								new Term(
									new Factor(
										new Identifier("def")
									).addOp(new Call().addArg(e.clone()))
								)
							)
						)
					)
				);
			e.accept(this, st);
		}

		if (n instanceof IfAllStatement)
			b.accept(this, env);
		else
			b.accept(this, st);

		return e;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ForStatement n, final SymbolTable env) {
		SymbolTable st;

		try {
			st = env.cloneNonLocals();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		n.env = st;

		if (n.hasInit())
			n.getInit().accept(this, st);

		if (n.hasCondition())
			n.getCondition().accept(this, st);

		if (n.hasUpdate())
			n.getUpdate().accept(this, st);

		n.getBody().accept(this, st);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final IfStatement n, final SymbolTable env) {
		n.env = env;

		n.getCondition().accept(this, env);

		if (!(n.getCondition().type instanceof BoaBool))
			throw new TypeCheckException(n.getCondition(), "incompatible types for if condition: required 'boolean', found '" + n.getCondition().type + "'");

		n.getBody().accept(this, env);

		if (n.hasElse())
			n.getElse().accept(this, env);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final PostfixStatement n, final SymbolTable env) {
		n.env = env;

		n.getExpr().accept(this, env);
		if (!(n.getExpr().type instanceof BoaInt))
			throw new TypeCheckException(n.getExpr(), "incompatible types for operator '" + n.getOp() + "': required 'int', found '" + n.getExpr().type + "'");

		n.type = n.getExpr().type;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ReturnStatement n, final SymbolTable env) {
		if (env.getIsVisitor())
			throw new TypeCheckException(n, "return statement not allowed inside visitors");

		n.env = env;

		if (n.hasExpr()) {
			n.getExpr().accept(this, env);
			n.type = n.getExpr().type;
		} else {
			n.type = new BoaAny();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final StopStatement n, final SymbolTable env) {
		n.env = env;

		if (!env.getIsVisitor())
			throw new TypeCheckException(n, "Stop statement only allowed inside 'before' visits");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SwitchCase n, final SymbolTable env) {
		n.env = env;

		for (final Expression e : n.getCases())
			e.accept(this, env);

		n.getBody().accept(this, env);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SwitchStatement n, final SymbolTable env) {
		SymbolTable st;
		try {
			st = env.cloneNonLocals();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		n.env = st;

		n.getCondition().accept(this, st);
		final BoaType expr = n.getCondition().type;
		if (!(expr instanceof BoaInt) && !(expr instanceof BoaProtoMap) && !(expr instanceof BoaEnum))
			throw new TypeCheckException(n.getCondition(), "incompatible types for switch expression: required 'int' or 'enum', found: " + expr);

		for (final SwitchCase sc : n.getCases()) {
			sc.accept(this, st);
			for (final Expression e : sc.getCases())
				if (!expr.assigns(e.type))
					throw new TypeCheckException(e, "incompatible types for case expression: required '" + expr + "', found '" + e.type + "'");
		}

		n.getDefault().accept(this, st);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VarDeclStatement n, final SymbolTable env) {
		n.env = env;

		final String id = n.getId().getToken();

		if (env.hasGlobal(id))
			throw new TypeCheckException(n.getId(), "name conflict: constant '" + id + "' already exists");
		if (env.hasLocal(id))
			throw new TypeCheckException(n.getId(), "variable '" + id + "' already declared as '" + env.get(id) + "'");

		BoaType rhs = null;
		if (n.hasInitializer()) {
			final Factor f = n.getInitializer().getLhs().getLhs().getLhs().getLhs().getLhs();

			if (f.getOperand() instanceof FunctionExpression) {
				SymbolTable st;
				try {
					st = env.cloneNonLocals();
				} catch (final IOException e) {
					throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
				}

				((FunctionExpression)f.getOperand()).getType().accept(this, st);

				if (env.hasGlobalFunction(id) || env.hasLocalFunction(id))
					throw new TypeCheckException(n.getId(), "name conflict: a function '" + id + "' already exists");

				env.set(id, ((FunctionExpression)f.getOperand()).getType().type);
			}

			n.getInitializer().accept(this, env);
			rhs = n.getInitializer().type;
			if (!(f.getOperand() instanceof FunctionExpression)) {
				if (env.hasGlobalFunction(id) || env.hasLocalFunction(id))
					throw new TypeCheckException(n.getId(), "name conflict: a function '" + id + "' already exists");
				env.set(id, rhs);
			}

			if (rhs instanceof BoaAny)
				throw new TypeCheckException(n.getInitializer(), "functions without a return type can not be used as initializers");

			if (f.getOperand() instanceof Identifier && f.getOpsSize() == 0 && env.hasType(((Identifier)f.getOperand()).getToken()))
				throw new TypeCheckException(n.getInitializer(), "type '" + f.getOperand().type + "' is not a value and can not be assigned");
		}

		BoaType lhs;
		if (n.hasType()) {
			if (n.getType() instanceof Identifier && !env.hasType(((Identifier)n.getType()).getToken()))
				throw new TypeCheckException(n.getType(), "type '" + ((Identifier)n.getType()).getToken() + "' undefined");

			n.getType().accept(this, env);
			lhs = n.getType().type;

			if (lhs instanceof BoaArray && rhs instanceof BoaTuple)
				rhs = new BoaArray(((BoaTuple)rhs).getMember(0));

			if (rhs != null && !lhs.assigns(rhs) && !env.hasCast(rhs, lhs))
				throw new TypeCheckException(n.getInitializer(), "incorrect type '" + rhs + "' for assignment to '" + id + ": " + lhs + "'");
		} else {
			if (rhs == null)
				throw new TypeCheckException(n, "variable declaration requires an explicit type or an initializer");

			lhs = rhs;
		}

		if (!(rhs instanceof BoaFunction))
			env.set(id, lhs);
		n.type = lhs;
		n.getId().accept(this, env);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VisitStatement n, final SymbolTable env) {
		SymbolTable st;
		try {
			st = env.cloneNonLocals();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		n.env = st;

		if (n.hasComponent()) {
			st.setShadowing(true);
			n.getComponent().accept(this, st);
			if (n.getComponent().type instanceof BoaName)
				n.getComponent().type = n.getComponent().getType().type;
			st.setShadowing(false);
		}
		else if (!n.hasWildcard())
			for (final Identifier id : n.getIdList()) {
				if (SymbolTable.getType(id.getToken()) == null)
					throw new TypeCheckException(id, "Invalid type '" + id.getToken() + "'");
				id.accept(this, st);
			}

		st.setIsVisitor(true);
		n.getBody().accept(this, st);
		st.unsetIsVisitor();
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final TraverseStatement n, final SymbolTable env) {
		SymbolTable st;
		try {
			st = env.cloneNonLocals();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		st.setIsTraverse(true);

		BoaType ret = new BoaAny();
		if (n.getReturnType()!=null) {
			n.getReturnType().accept(this, env);
			ret = n.getReturnType().type;
		}
		n.type = new BoaFunction(ret, new BoaType[]{});
		lastRetType = ret;
		n.env = st;

		if (n.hasComponent()) {
			n.getComponent().accept(this, st);
			if (n.getComponent().type instanceof BoaName)
				n.getComponent().type = n.getComponent().getType().type;
		}
		else if (!n.hasWildcard())
			for (final Identifier id : n.getIdList()) {
				if (SymbolTable.getType(id.getToken()) == null)
					throw new TypeCheckException(id, "Invalid type '" + id.getToken() + "'");
				id.accept(this, st);
			}

		for (final IfStatement ifStatement : n.getIfStatements()) {
			ifStatement.accept(this, st);
		}
		if (n.hasCondition()) {
			n.getCondition().accept(this, st);
		}
		if (n.hasBody()) {
			st.setIsVisitor(false);
			n.getBody().accept(this, st);
			st.unsetIsVisitor();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FixPStatement n, final SymbolTable env) {
		SymbolTable st;
		try {
			st = env.cloneNonLocals();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		BoaType ret = new BoaAny();
		if (n.getReturnType()!=null) {
			n.getReturnType().accept(this, env);
			ret = n.getReturnType().type;
		}
		n.type = new BoaFunction(ret, new BoaType[]{});

		n.env = st;

		n.getParam1().accept(this, st);
		if (n.getParam1().type instanceof BoaName)
			n.getParam1().type = n.getParam1().getType().type;

		n.getParam2().accept(this, st);
		if (n.getParam2().type instanceof BoaName)
			n.getParam2().type = n.getParam2().getType().type;

		if (n.hasCondition()) {
			n.getCondition().accept(this, st);
		}
		if (n.hasBody()) {
			st.setIsVisitor(false);
			n.getBody().accept(this, st);
			st.unsetIsVisitor();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final WhileStatement n, final SymbolTable env) {
		SymbolTable st;

		try {
			st = env.cloneNonLocals();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		n.env = st;

		n.getCondition().accept(this, st);
		n.getBody().accept(this, st);
	}

	//
	// expressions
	//
	/** {@inheritDoc} */
	@Override
	public void visit(final Expression n, final SymbolTable env) {
		n.env = env;

		n.getLhs().accept(this, env);
		final BoaType ltype = n.getLhs().type;
		n.type = ltype;

		if (n.getRhsSize() > 0) {
			if (!(ltype instanceof BoaBool))
				throw new TypeCheckException(n.getLhs(), "incompatible types for disjunction: required 'bool', found '" + ltype + "'");

			for (final Conjunction c : n.getRhs()) {
				c.accept(this, env);
				if (!(c.type instanceof BoaBool))
					throw new TypeCheckException(c, "incompatible types for disjunction: required 'bool', found '" + c.type + "'");
			}

			n.type = new BoaBool();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FunctionExpression n, final SymbolTable env) {
		SymbolTable st;
		try {
			st = env.cloneNonLocals();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		n.env = st;

		n.getType().accept(this, st);
		if (!(n.getType().type instanceof BoaFunction))
			throw new TypeCheckException(n.getType(), "the identifier '" + n.getType() + "' must be a function type");
		final BoaFunction t = (BoaFunction)n.getType().type;
		n.type = t;

		st.setIsVisitor(false);
		n.getBody().accept(this, st);
		st.unsetIsVisitor();

		returnFinder.initialize(t.getType());
		returnFinder.start(n.getBody());
		if (!(t.getType() instanceof BoaAny)
				&& (n.getBody().getStatementsSize() == 0 || !(n.getBody().getStatement(n.getBody().getStatementsSize() - 1) instanceof ReturnStatement)))
			throw new TypeCheckException(n.getBody(), "missing return statement");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ParenExpression n, final SymbolTable env) {
		n.env = env;
		n.getExpression().accept(this, env);
		n.type = n.getExpression().type;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SimpleExpr n, final SymbolTable env) {
		n.env = env;

		n.getLhs().accept(this, env);
		BoaType type = n.getLhs().type;

		// only allow '+' (concat) on arrays
		if (type instanceof BoaArray) {
			for (final String s : n.getOps())
				if (!s.equals("+"))
					throw new TypeCheckException(n, "arrays do not support the '" + s + "' arithmetic operator, perhaps you meant '+'?");

			final BoaType valType = ((BoaArray)type).getType();
			for (final Term t : n.getRhs()) {
				t.accept(this, env);
				if (!(t.type instanceof BoaArray) || !valType.assigns(((BoaArray)t.type).getType()))
					throw new TypeCheckException(t, "invalid array concatenation, found: " + t.type + " expected: " + type);
			}
		// only allow '+' (concat) on strings
		} else if (type instanceof BoaString) {
			for (final String s : n.getOps())
				if (!s.equals("+"))
					throw new TypeCheckException(n, "strings do not support the '" + s + "' arithmetic operator, perhaps you meant '+'?");

			for (final Term t : n.getRhs()) {
				t.accept(this, env);
				if (!(t.type instanceof BoaString))
					throw new TypeCheckException(t, "invalid string concatenation, found: " + t.type + " expected: string");
			}
		} else
			for (int i = 0; i < n.getRhsSize(); i++) {
				final Term t = n.getRhs(i);
				t.accept(this, env);
				try {
					type = type.arithmetics(t.type);
				} catch (final Exception e) {
					throw new TypeCheckException(t, "type '" + t.type + "' does not support the '" + n.getOp(i) + "' operator", e);
				}
			}

		n.type = type;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VisitorExpression n, final SymbolTable env) {
		n.env = env;
		n.getType().accept(this, env);
		for (final Statement s : n.getBody().getStatements())
			if (!(s instanceof VisitStatement))
				throw new TypeCheckException(s, "only 'before' or 'after' visit statements are allowed inside visitor bodies");
		visitorChecker.start(n);
		n.getBody().accept(this, env);
		n.type = n.getType().type;
		final VisitorDesugar desugar = new VisitorDesugar();
		desugar.start(n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final TraversalExpression n, final SymbolTable env) {
		n.env = env;
		n.getType().accept(this, env);
		//n.getBody.addStatement(,0);
		for (final Statement s : n.getBody().getStatements())
			if (!(s instanceof TraverseStatement))
				throw new TypeCheckException(s, "only traverse statements are allowed inside traversal bodies");
		traversalChecker.start(n);
		n.getBody().accept(this, env);
		n.type = n.getType().type;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FixPExpression n, final SymbolTable env) {
		n.env = env;
		n.getType().accept(this, env);
		fixPChecker.start(n);
		n.getBody().accept(this, env);
		n.type = n.getType().type;
	}

	//
	// literals
	//
	/** {@inheritDoc} */
	@Override
	public void visit(final CharLiteral n, final SymbolTable env) {
		n.env = env;
		n.type = new BoaInt();
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FloatLiteral n, final SymbolTable env) {
		n.env = env;
		n.type = new BoaFloat();
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final IntegerLiteral n, final SymbolTable env) {
		n.env = env;
		n.type = new BoaInt();
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final StringLiteral n, final SymbolTable env) {
		n.env = env;
		n.type = new BoaString();
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final TimeLiteral n, final SymbolTable env) {
		n.env = env;
		n.type = new BoaTime();
	}

	//
	// types
	//
	/** {@inheritDoc} */
	@Override
	public void visit(final TypeDecl n, final SymbolTable env) {
		n.env = env;
		n.getType().accept(this, env);
		n.type = n.getType().type;
		n.env.setType(n.getId().getToken(), n.type);
		n.getId().accept(this, env);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ArrayType n, final SymbolTable env) {
		SymbolTable st;

		try {
			st = env.cloneNonLocals();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		n.env = st;
		n.getValue().accept(this, st);
		n.type = new BoaArray(n.getValue().type);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FunctionType n, final SymbolTable env) {
		n.env = env;

		final BoaType[] params = new BoaType[n.getArgsSize()];
		if (n.getArgsSize() > 0) {
			int i = 0;
			env.setShadowing(true);
			for (final Component c : n.getArgs()) {
				c.accept(this, env);
				params[i++] = new BoaName(c.getType().type, c.getIdentifier().getToken());
			}
			env.setShadowing(false);
		}

		BoaType ret = new BoaAny();
		if (n.hasType()) {
			n.getType().accept(this, env);
			ret = n.getType().type;
		}

		n.type = new BoaFunction(ret, params);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final MapType n, final SymbolTable env) {
		SymbolTable st;

		try {
			st = env.cloneNonLocals();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		n.env = st;
		n.getValue().accept(this, st);
		n.getIndex().accept(this, st);
		n.type = new BoaMap(n.getValue().type, n.getIndex().type);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final OutputType n, final SymbolTable env) {
		SymbolTable st;

		try {
			st = env.cloneNonLocals();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		n.env = st;

		List<BoaScalar> indexTypes = null;
		if (n.getIndicesSize() > 0) {
			indexTypes = new ArrayList<BoaScalar>();

			for (final Component c : n.getIndices()) {
				c.accept(this, st);

				if (!(c.type instanceof BoaScalar))
					throw new TypeCheckException(c, "incorrect type '" + c.type + "' for index");

				indexTypes.add((BoaScalar) c.type);
			}
		}

		n.getType().accept(this, st);
		final BoaType type = n.getType().type;

		final AggregatorSpec annotation;
		try {
			annotation = st.getAggregators(n.getId().getToken(), type).get(0).getAnnotation(AggregatorSpec.class);
		} catch (final RuntimeException e) {
			throw new TypeCheckException(n, e.getMessage(), e);
		}

		BoaScalar tweight = null;
		if (n.hasWeight()) {
			if (annotation.weightType().equals("none"))
				throw new TypeCheckException(n.getWeight(), "output aggregator '" + n.getId().getToken() + "' does not expect a weight");

			final BoaType aweight = SymbolTable.getType(annotation.weightType());
			n.getWeight().accept(this, st);
			tweight = (BoaScalar) n.getWeight().type;

			if (!aweight.assigns(tweight))
				throw new TypeCheckException(n.getWeight(), "invalid weight type, found: " + tweight + " expected: " + aweight);
		} else if (!annotation.weightType().equals("none") && !annotation.weightType().equals("any"))
			throw new TypeCheckException(n, "output aggregator expects a weight type");

		if (n.getArgsSize() > 0 && annotation.formalParameters().length == 0)
			throw new TypeCheckException(n.getArgs(), "output aggregator '" + n.getId().getToken() + "' takes no arguments");

		n.type = new BoaTable(type, indexTypes, tweight, annotation.canOmitWeight());

		n.env = env;
		env.set(n.getId().getToken(), n.type);
		n.getId().accept(this, env);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final StackType n, final SymbolTable env) {
		SymbolTable st;

		try {
			st = env.cloneNonLocals();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		n.env = st;
		n.getValue().accept(this, st);
		n.type = new BoaStack(n.getValue().type);
	}
	
	/** {@inheritDoc} */
	@Override
	public void visit(final QueueType n, final SymbolTable env) {
		SymbolTable st;

		try {
			st = env.cloneNonLocals();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		n.env = st;
		n.getValue().accept(this, st);
		n.type = new BoaQueue(n.getValue().type);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SetType n, final SymbolTable env) {
		SymbolTable st;

		try {
			st = env.cloneNonLocals();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		n.env = st;
		n.getValue().accept(this, st);
		n.type = new BoaSet(n.getValue().type);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final TupleType n, final SymbolTable env) {
		n.env = env;
		final SymbolTable e = new SymbolTable();

		final List<BoaType> types = new ArrayList<BoaType>();

		for (final Component c : n.getMembers()) {
			c.accept(this, e);
			types.add(c.type);
		}

		n.type = new BoaTuple(types);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final EnumType n, final SymbolTable env) {
		n.env = env;

		final List<BoaEnum> types = new ArrayList<BoaEnum>();
		final List<String> names = new ArrayList<String>();
		final List<String> values = new ArrayList<String>();
		BoaType fieldType = null;

		for (final EnumBodyDeclaration c : n.getMembers()) {
			names.add(c.getIdentifier().getToken());

			final Factor f = c.getExp().getLhs().getLhs().getLhs().getLhs().getLhs();
			if(f.getOperand() instanceof ILiteral) {
				if(f.getOperand() instanceof StringLiteral)
					fieldType = new BoaString();
				else if(f.getOperand() instanceof IntegerLiteral)
					fieldType = new BoaInt();
				else if(f.getOperand() instanceof FloatLiteral)
					fieldType = new BoaFloat();
				else if(f.getOperand() instanceof TimeLiteral)
					fieldType = new BoaTime();
				values.add(((ILiteral)(f.getOperand())).getLiteral());
				types.add(new BoaEnum(c.getIdentifier().getToken(),((ILiteral)(f.getOperand())).getLiteral(),fieldType));
			}
		}

		n.type = new BoaEnum(types, names, values, fieldType);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VisitorType n, final SymbolTable env) {
		n.env = env;
		n.type = new BoaVisitor();
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final TraversalType n, final SymbolTable env) {
		n.env = env;
		BoaTraversal tr = new BoaTraversal();
		if(n.getIndex()!=null) {
			n.getIndex().accept(this, env);
			tr.setIndex(n.getIndex().type);
		}
		n.type = tr;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FixPType n, final SymbolTable env) {
		n.env = env;
		n.type = new BoaFixP();
	}

	protected List<BoaType> check(final Call c, final SymbolTable env) {
		if (c.getArgsSize() > 0)
			return this.check(c.getArgs(), env);

		return new ArrayList<BoaType>();
	}

	protected List<BoaType> check(final List<Expression> el, final SymbolTable env) {
		final List<BoaType> types = new ArrayList<BoaType>();

		for (final Expression e : el) {
			e.accept(this, env);
			types.add(e.type);
		}
		
		return types;
	}

	protected boolean checkTupleArray(final List<BoaType> types) {
		if (types == null)
			return false;

		final String type = types.get(0).toBoxedJavaType();

		for (int i = 1; i < types.size(); i++)
			if (!type.equals(types.get(i).toBoxedJavaType()))
				return true;

		return false;
	}

	protected BoaType checkPairs(final List<Pair> pl, final SymbolTable env) {
		pl.get(0).accept(this, env);
		final BoaMap boaMap = (BoaMap) pl.get(0).type;

		for (final Pair p : pl) {
			p.accept(this, env);
			if (!boaMap.assigns(p.type))
				throw new TypeCheckException(p, "incompatible types: required '" + boaMap + "', found '" + p.type + "'");
		}

		return boaMap;
	}
}
