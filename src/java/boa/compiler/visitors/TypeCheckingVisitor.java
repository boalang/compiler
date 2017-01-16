/*
 * Copyright 2014, Anthony Urso, Hridesh Rajan, Robert Dyer, 
 *                 and Iowa State University of Science and Technology
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import boa.aggregators.AggregatorSpec;
import boa.compiler.SymbolTable;
import boa.compiler.TypeCheckException;
import boa.compiler.ast.Call;
import boa.compiler.ast.Comparison;
import boa.compiler.ast.Component;
import boa.compiler.ast.Composite;
import boa.compiler.ast.Conjunction;
import boa.compiler.ast.EnumBodyDeclaration;
import boa.compiler.ast.Factor;
import boa.compiler.ast.Identifier;
import boa.compiler.ast.Index;
import boa.compiler.ast.Node;
import boa.compiler.ast.Pair;
import boa.compiler.ast.Program;
import boa.compiler.ast.Selector;
import boa.compiler.ast.Start;
import boa.compiler.ast.Term;
import boa.compiler.ast.UnaryFactor;
import boa.compiler.ast.expressions.Expression;
import boa.compiler.ast.expressions.FunctionExpression;
import boa.compiler.ast.expressions.ParenExpression;
import boa.compiler.ast.expressions.SimpleExpr;
import boa.compiler.ast.expressions.VisitorExpression;
import boa.compiler.ast.literals.CharLiteral;
import boa.compiler.ast.literals.FloatLiteral;
import boa.compiler.ast.literals.ILiteral;
import boa.compiler.ast.literals.IntegerLiteral;
import boa.compiler.ast.literals.StringLiteral;
import boa.compiler.ast.literals.TimeLiteral;
import boa.compiler.ast.statements.AssignmentStatement;
import boa.compiler.ast.statements.Block;
import boa.compiler.ast.statements.BreakStatement;
import boa.compiler.ast.statements.ContinueStatement;
import boa.compiler.ast.statements.DoStatement;
import boa.compiler.ast.statements.EmitStatement;
import boa.compiler.ast.statements.ExistsStatement;
import boa.compiler.ast.statements.ExprStatement;
import boa.compiler.ast.statements.ForStatement;
import boa.compiler.ast.statements.ForeachStatement;
import boa.compiler.ast.statements.IfAllStatement;
import boa.compiler.ast.statements.IfStatement;
import boa.compiler.ast.statements.PostfixStatement;
import boa.compiler.ast.statements.ReturnStatement;
import boa.compiler.ast.statements.Statement;
import boa.compiler.ast.statements.StopStatement;
import boa.compiler.ast.statements.SwitchCase;
import boa.compiler.ast.statements.SwitchStatement;
import boa.compiler.ast.statements.TypeDecl;
import boa.compiler.ast.statements.VarDeclStatement;
import boa.compiler.ast.statements.VisitStatement;
import boa.compiler.ast.statements.WhileStatement;
import boa.compiler.ast.types.ArrayType;
import boa.compiler.ast.types.EnumType;
import boa.compiler.ast.types.FunctionType;
import boa.compiler.ast.types.MapType;
import boa.compiler.ast.types.ModelType;
import boa.compiler.ast.types.OutputType;
import boa.compiler.ast.types.SetType;
import boa.compiler.ast.types.StackType;
import boa.compiler.ast.types.TupleType;
import boa.compiler.ast.types.VisitorType;
import boa.compiler.transforms.VisitorDesugar;
import boa.types.BoaAny;
import boa.types.BoaArray;
import boa.types.BoaBool;
import boa.types.BoaEnum;
import boa.types.BoaFloat;
import boa.types.BoaFunction;
import boa.types.BoaInt;
import boa.types.BoaMap;
import boa.types.BoaName;
import boa.types.BoaProtoList;
import boa.types.BoaProtoMap;
import boa.types.BoaProtoTuple;
import boa.types.BoaScalar;
import boa.types.BoaSet;
import boa.types.BoaStack;
import boa.types.BoaString;
import boa.types.BoaTable;
import boa.types.BoaTime;
import boa.types.BoaTuple;
import boa.types.BoaType;
import boa.types.BoaVisitor;
import boa.types.ml.BoaAdaBoostM1;
import boa.types.ml.BoaAdditiveRegression;
import boa.types.ml.BoaApriori;
import boa.types.ml.BoaAttributeSelectedClassifier;
import boa.types.ml.BoaBagging;
import boa.types.ml.BoaBayesNet;
import boa.types.ml.BoaBayesNetGenerator;
import boa.types.ml.BoaCVParameterSelection;
import boa.types.ml.BoaClassificationViaRegression;
import boa.types.ml.BoaDecisionStump;
import boa.types.ml.BoaDecisionTable;
import boa.types.ml.BoaFilteredClassifier;
import boa.types.ml.BoaGaussianProcesses;
import boa.types.ml.BoaHoeffdingTree;
import boa.types.ml.BoaIBk;
import boa.types.ml.BoaInputMappedClassifier;
import boa.types.ml.BoaIterativeClassifierOptimizer;
import boa.types.ml.BoaJ48;
import boa.types.ml.BoaJRip;
import boa.types.ml.BoaKStar;
import boa.types.ml.BoaLMT;
import boa.types.ml.BoaLWL;
import boa.types.ml.BoaLinearRegression;
import boa.types.ml.BoaLogisticRegression;
import boa.types.ml.BoaLogitBoost;
import boa.types.ml.BoaLsa;
import boa.types.ml.BoaModel;
import boa.types.ml.BoaMultiClassClassifier;
import boa.types.ml.BoaMultiClassClassifierUpdateable;
import boa.types.ml.BoaMultiScheme;
import boa.types.ml.BoaMultilayerPerceptron;
import boa.types.ml.BoaNaiveBayes;
import boa.types.ml.BoaNaiveBayesMultinomial;
import boa.types.ml.BoaNaiveBayesMultinomialUpdateable;
import boa.types.ml.BoaOneR;
import boa.types.ml.BoaPART;
import boa.types.ml.BoaPrincipalComponents;
import boa.types.ml.BoaRandomForest;
import boa.types.ml.BoaSMO;
import boa.types.ml.BoaSimpleKMeans;
import boa.types.ml.BoaVote;
import boa.types.ml.BoaZeroR;

/**
 * Prescan the program and check that all variables are consistently typed.
 *
 * @author anthonyu
 * @author rdyer
 * @author ankuraga
 */
public class TypeCheckingVisitor extends AbstractVisitorNoReturn<SymbolTable> {
	/**
	 * This verifies visitors have at most 1 before/after for a type.
	 *
	 * @author rdyer
	 */
	protected class VisitorCheckingVisitor extends AbstractVisitorNoArg {
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

	/**
	 * This does type checking of function bodies to ensure the
	 * returns are the correct type.
	 *
	 * @author rdyer
	 */
	protected class ReturnCheckingVisitor extends AbstractVisitorNoArg {
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

	/**
	 * Finds if the expression is a Call.
	 *
	 * @author rdyer
	 */
	protected class CallFindingVisitor extends AbstractVisitorNoArg {
		protected boolean isCall;

		public boolean isCall() {
			return isCall;
		}

		/** {@inheritDoc} */
		@Override
		public void initialize() {
			super.initialize();
			isCall = false;
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final Factor n) {
			for (final Node node : n.getOps()) {
				isCall = false;
				node.accept(this);
			}
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final Call n) {
			isCall = true;
		}
	}

	protected final VisitorCheckingVisitor visitorChecker = new VisitorCheckingVisitor();
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

		List<BoaType> expr = this.check(n.getArgs(), env);
		if(expr.size() > 1) {
			if(expr.get(0) instanceof BoaModel && expr.get(1) instanceof BoaTuple) {
				final BoaType t = ((BoaModel)expr.get(0)).getType();
				if(t instanceof BoaTuple) {
					final List<BoaType> mtypes = ((BoaTuple)t).getTypes();
					final List<BoaType> vtypes = ((BoaTuple)expr.get(1)).getTypes();
					if(mtypes.size() - 1 == vtypes.size()) {
						for(int i=0; i < mtypes.size() - 1; i++)
							if(!(mtypes.get(i).toString().equals(vtypes.get(i).toString())))
								throw new TypeCheckException(n, "incompatible types for model:" + " required '" + mtypes.get(i) + "', found '" + vtypes.get(i) + "'");
					}
					else
						throw new TypeCheckException(n, "'incorrect number of types for model classification '" + "': required " + (mtypes.size()-1) + ", found " + vtypes.size());
				}
			}
		}

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

			if (n.type instanceof BoaString || n.type instanceof BoaProtoTuple)
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
			n.type = new BoaName(n.getType().type, n.getIdentifier().getToken());
			env.set(n.getIdentifier().getToken(), n.getType().type);
			n.getIdentifier().accept(this, env);
		} else {
			n.type = n.getType().type;
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Composite n, final SymbolTable env) {
		n.env = env;

		if (n.getPairsSize() > 0)
			n.type = checkPairs(n.getPairs(), env);
		else if (n.getExprsSize() > 0) {
			List<BoaType> types = check(n.getExprs(), env);

			if(!(checkTupleArray(types) == true))
				n.type = new BoaArray(types.get(0));
			else
				n.type = new BoaTuple(types);
		}
		else
			n.type = new BoaMap(new BoaAny(), new BoaAny());
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

					final FunctionFindingVisitor v = new FunctionFindingVisitor(formalParameters);
					try {
						v.start((Identifier)n.getOperand(), env);
					} catch (final ClassCastException e) {
						throw new TypeCheckException(n.getOperand(), "Function declarations must be assigned to a variable and can not be used anonymously", e);
					} catch (final RuntimeException e) {
						throw new TypeCheckException(n.getOperand(), e.getMessage(), e);
					}
					type = v.getFunction().erase(formalParameters);
				}
				node.type = type;
			}
		} else {
			n.getOperand().accept(this, env);
			type = n.getOperand().type;

			if (type instanceof BoaFunction && n.getOperand() instanceof Identifier)
				throw new TypeCheckException(n, "expected a call to function '" + n.getOperand() + "'");
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
				throw new TypeCheckException(n, "invalid identifier '" + n.getToken() + "'", e);
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
			BoaScalar type;

			if (accepts instanceof BoaFunction)
				type = (BoaScalar) ((BoaFunction) accepts).getType();
			else
				type = (BoaScalar) accepts;

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

		n.getLhs().accept(this, env);
		n.getRhs().accept(this, env);

		if (!(n.getLhs().type instanceof BoaArray && n.getRhs().type instanceof BoaTuple))
			if (!n.getLhs().type.assigns(n.getRhs().type))
				throw new TypeCheckException(n.getRhs(), "incompatible types for assignment: required '" + n.getLhs().type + "', found '" + n.getRhs().type + "'");

		if(n.getLhs().type instanceof BoaTuple && n.getRhs().type instanceof BoaTuple) {
			if (((BoaTuple)n.getLhs().type).getSize() != ((BoaTuple)n.getRhs().type).getSize())
				throw new TypeCheckException(n.getRhs(), "'incorrect number of types for assignment '" + "': required " + ((BoaTuple)n.getLhs().type).getSize() + ", found " + ((BoaTuple)n.getRhs().type).getSize());

			for (int i = 0; i < ((BoaTuple)n.getLhs().type).getSize(); i++) {
				if (!( ((BoaTuple)n.getLhs().type).getMember(i).assigns(((BoaTuple)n.getRhs().type).getMember(i))))
					throw new TypeCheckException(n.getRhs(), "incompatible types for assignment: required '" + n.getLhs().type + "', found '" + n.getRhs().type + "'");

				if(((BoaTuple)n.getLhs().type).getMember(i) instanceof BoaEnum && ((BoaTuple)n.getRhs().type).getMember(i) instanceof BoaEnum)
					if(!checkSameEnum(((BoaTuple)n.getLhs().type).getMember(i).toString(), ((BoaTuple)n.getRhs().type).getMember(i).toString()) )
						throw new TypeCheckException(n.getRhs(), "incompatible type '" + ((BoaTuple)n.getRhs().type).getMember(i) + "' for assignment to '" + ((BoaTuple)n.getLhs().type).getMember(i) + "'");

				if(((BoaTuple)n.getLhs().type).getMember(i) instanceof BoaName)
					if(((BoaName)(((BoaTuple)n.getLhs().type).getMember(i))).getType() instanceof BoaEnum && ((BoaTuple)n.getRhs().type).getMember(i) instanceof BoaEnum)
						if(!checkSameEnum(((BoaName)(((BoaTuple)n.getLhs().type).getMember(i))).getType().toString(), ((BoaTuple)n.getRhs().type).getMember(i).toString()) )
							throw new TypeCheckException(n.getRhs(), "incompatible type '" + ((BoaTuple)n.getRhs().type).getMember(i) + "' for assignment to '" + ((BoaName)(((BoaTuple)n.getLhs().type).getMember(i))).getType() + "'");

			}
		}

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

		if(n.getValue().type instanceof BoaTuple) {
			if (((BoaTuple)n.getValue().type).getSize() != ((BoaTuple)t.getType()).getSize())
				throw new TypeCheckException(n.getValue(), "output variable '" + id + "': incorrect number of types for '" + id + "': required " + ((BoaTuple)t.getType()).getSize() + ", found " + ((BoaTuple)n.getValue().type).getSize() );

			for (int i = 0; i < ((BoaTuple)n.getValue().type).getSize(); i++) {
				if (!( ((BoaTuple)t.getType()).getMember(i).assigns(((BoaTuple)n.getValue().type).getMember(i))))
					throw new TypeCheckException(n.getValue(), "output variable '" + id + "': incompatible emit value types: '" + i + "': required '" + ((BoaTuple)t.getType()).getMember(i) + "', found '" + ((BoaTuple)n.getValue().type).getMember(i) + "'");

				if(((BoaTuple)t.getType()).getMember(i) instanceof BoaEnum && ((BoaTuple)n.getValue().type).getMember(i) instanceof BoaEnum)
					if(!checkSameEnum(((BoaTuple)t.getType()).getMember(i).toString(), ((BoaTuple)n.getValue().type).getMember(i).toString()) )
						throw new TypeCheckException(n.getValue(), "output variable '" + id + "': incompatible emit value types: required '" + ((BoaTuple)t.getType()).getMember(i) + "', found '" + ((BoaTuple)n.getValue().type).getMember(i) + "'");

				if(((BoaTuple)t.getType()).getMember(i) instanceof BoaName)
					if(((BoaName)(((BoaTuple)t.getType()).getMember(i))).getType() instanceof BoaEnum && ((BoaTuple)n.getValue().type).getMember(i) instanceof BoaEnum)
						if(!checkSameEnum(((BoaName)(((BoaTuple)t.getType()).getMember(i))).getType().toString(), ((BoaTuple)n.getValue().type).getMember(i).toString()) )
							throw new TypeCheckException(n.getValue(), "output variable '" + id + "': incompatible emit value types: required '" + ((BoaName)(((BoaTuple)t.getType()).getMember(i))).getType() + "', found '" + ((BoaTuple)n.getValue().type).getMember(i) + "'");
			}
		}

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
			if (!(n.getCondition().type instanceof BoaFunction && ((BoaFunction) n.getCondition().type).getType() instanceof BoaBool))
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
		if (env.getIsBeforeVisitor())
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

		if (!env.getIsBeforeVisitor())
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
			n.getInitializer().accept(this, env);
			rhs = n.getInitializer().type;

			final Factor f = n.getInitializer().getLhs().getLhs().getLhs().getLhs().getLhs();
			if (f.getOperand() instanceof Identifier && f.getOpsSize() == 0 && env.hasType(((Identifier)f.getOperand()).getToken()))
				throw new TypeCheckException(n.getInitializer(), "type '" + f.getOperand().type + "' is not a value and can not be assigned");

			// if type is a function but rhs isnt a function decl,
			// then its a call so the lhs type is the return type
			if (rhs instanceof BoaFunction) {
				final IsFunctionVisitor v = new IsFunctionVisitor();
				v.start(n.getInitializer());
				if (!v.isFunction())
					rhs = ((BoaFunction)rhs).getType();
			}
		}

		BoaType lhs;
		if (n.hasType()) {
			if (n.getType() instanceof Identifier && !env.hasType(((Identifier)n.getType()).getToken()))
				throw new TypeCheckException(n.getType(), "type '" + ((Identifier)n.getType()).getToken() + "' undefined");

			n.getType().accept(this, env);
			lhs = n.getType().type;

			if (lhs instanceof BoaArray && rhs instanceof BoaTuple)
				rhs = new BoaArray(((BoaTuple)rhs).getMember(0));

			if (lhs instanceof BoaTuple && rhs instanceof BoaArray) {
				List<BoaType> types = ((BoaTuple)lhs).getTypes();
				if(!(checkTupleArray(types) == true))
					if(types.get(0).assigns(((BoaArray)rhs).getType())) {
						rhs = new BoaTuple(types);
					}
			}

			if(lhs instanceof BoaModel) {
				final BoaType t = ((BoaModel)lhs).getType();
				List<BoaType> types = new ArrayList<BoaType>();

				if(t instanceof BoaTuple)
					types = ((BoaTuple)t).getTypes();
				else if(t instanceof BoaArray)
					types.add(((BoaArray)t).getType());

				if(lhs instanceof BoaAdaBoostM1) {
					if(!(types.get(types.size() - 1) instanceof BoaEnum))
						throw new TypeCheckException(n, "AdaBoostM1 required class to be nominal");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaEnum || types.get(i) instanceof BoaFloat ||
								types.get(i) instanceof BoaInt || types.get(i) instanceof BoaTime || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "AdaBoostM1 required attributes to be numeric, nominal or date");
					}
				} else if(lhs instanceof BoaAdditiveRegression) {
					if(!(types.get(types.size() - 1) instanceof BoaInt || types.get(types.size() - 1) instanceof BoaFloat
							|| types.get(types.size() - 1) instanceof BoaTime))
						throw new TypeCheckException(n, "AdditiveRegression required class to be numeric or date");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaEnum || types.get(i) instanceof BoaFloat ||
								types.get(i) instanceof BoaInt || types.get(i) instanceof BoaTime || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "AdditiveRegression required attributes to be numeric, nominal or date");
					}
				} else if(lhs instanceof BoaApriori) {
					if(!(types.get(types.size() - 1) instanceof BoaEnum))
						throw new TypeCheckException(n, "Apriori required class to be nominal");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaEnum || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "Apriori required attributes to be nominal");
					}
				} else if(lhs instanceof BoaAttributeSelectedClassifier) {
					if(!(types.get(types.size() - 1) instanceof BoaEnum || types.get(types.size() - 1) instanceof BoaInt
							|| types.get(types.size() - 1) instanceof BoaFloat || types.get(types.size() - 1) instanceof BoaTime))
						throw new TypeCheckException(n, "AttributeSelectedClassifier required class to be numeric, nominal or date");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaEnum || types.get(i) instanceof BoaFloat ||
								types.get(i) instanceof BoaInt || types.get(i) instanceof BoaTime || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "AttributeSelectedClassifier required attributes to be numeric, nominal or date");
					}
				} else if(lhs instanceof BoaBagging) {
					if(!(types.get(types.size() - 1) instanceof BoaEnum || types.get(types.size() - 1) instanceof BoaInt
							|| types.get(types.size() - 1) instanceof BoaFloat || types.get(types.size() - 1) instanceof BoaTime))
						throw new TypeCheckException(n, "Bagging required class to be numeric, nominal or date");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaEnum || types.get(i) instanceof BoaFloat ||
								types.get(i) instanceof BoaInt || types.get(i) instanceof BoaTime || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "Bagging required attributes to be numeric, nominal or date");
					}
				} else if(lhs instanceof BoaBayesNetGenerator) {
					if(!(types.get(types.size() - 1) instanceof BoaEnum))
						throw new TypeCheckException(n, "BayesNetGenerator required class to be nominal");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaEnum || types.get(i) instanceof BoaFloat ||
								types.get(i) instanceof BoaInt || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "BayesNetGenerator required attributes to be numeric or nominal");
					}
				} else if(lhs instanceof BoaBayesNet) {
					if(!(types.get(types.size() - 1) instanceof BoaEnum))
						throw new TypeCheckException(n, "BayesNet required class to be nominal");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaEnum || types.get(i) instanceof BoaFloat ||
								types.get(i) instanceof BoaInt || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "BayesNet required attributes to be numeric or nominal");
					}
				} else if(lhs instanceof BoaClassificationViaRegression) {
					if(!(types.get(types.size() - 1) instanceof BoaEnum))
						throw new TypeCheckException(n, "ClassificationViaRegression required class to be nominal");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaEnum || types.get(i) instanceof BoaFloat ||
								types.get(i) instanceof BoaInt || types.get(i) instanceof BoaTime || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "ClassificationViaRegression required attributes to be numeric, nominal or date");
					}
				} else if(lhs instanceof BoaCVParameterSelection) {
					if(!(types.get(types.size() - 1) instanceof BoaEnum || types.get(types.size() - 1) instanceof BoaInt
							|| types.get(types.size() - 1) instanceof BoaFloat || types.get(types.size() - 1) instanceof BoaTime))
						throw new TypeCheckException(n, "CVParameterSelection required class to be numeric, nominal or date");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaEnum || types.get(i) instanceof BoaFloat ||
								types.get(i) instanceof BoaInt || types.get(i) instanceof BoaTime || types.get(i) instanceof BoaString || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "CVParameterSelection required attributes to be numeric, nominal, date or string");
					}
				} else if(lhs instanceof BoaDecisionStump) {
					if(!(types.get(types.size() - 1) instanceof BoaEnum || types.get(types.size() - 1) instanceof BoaInt
							|| types.get(types.size() - 1) instanceof BoaFloat || types.get(types.size() - 1) instanceof BoaTime))
						throw new TypeCheckException(n, "DecisionStump required class to be numeric, nominal or date");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaEnum || types.get(i) instanceof BoaFloat ||
								types.get(i) instanceof BoaInt || types.get(i) instanceof BoaTime || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "DecisionStump required attributes to be numeric, nominal or date");
					}
				} else if(lhs instanceof BoaDecisionTable) {
					if(!(types.get(types.size() - 1) instanceof BoaEnum || types.get(types.size() - 1) instanceof BoaInt
							|| types.get(types.size() - 1) instanceof BoaFloat || types.get(types.size() - 1) instanceof BoaTime))
						throw new TypeCheckException(n, "DecisionTable required class to be numeric, nominal or date");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaEnum || types.get(i) instanceof BoaFloat ||
								types.get(i) instanceof BoaInt || types.get(i) instanceof BoaTime || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "DecisionTable required attributes to be numeric, nominal or date");
					}
				} else if(lhs instanceof BoaFilteredClassifier) {
					if(!(types.get(types.size() - 1) instanceof BoaEnum))
						throw new TypeCheckException(n, "FilteredClassifier required class to be nominal");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaEnum || types.get(i) instanceof BoaFloat ||
								types.get(i) instanceof BoaInt || types.get(i) instanceof BoaTime || types.get(i) instanceof BoaString || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "FilteredClassifier required attributes to be numeric, nominal, date or string");
					}
				} else if(lhs instanceof BoaGaussianProcesses) {
					if(!(types.get(types.size() - 1) instanceof BoaInt || types.get(types.size() - 1) instanceof BoaTime
							|| types.get(types.size() - 1) instanceof BoaFloat))
						throw new TypeCheckException(n, "GaussianProcesses required class to be numeric or date");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaFloat || types.get(i) instanceof BoaInt || types.get(i) instanceof BoaEnum || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "GaussianProcesses required attributes to be nominal or numeric");
					}
				} else if(lhs instanceof BoaHoeffdingTree) {
					if(!(types.get(types.size() - 1) instanceof BoaEnum))
						throw new TypeCheckException(n, "HoeffdingTree required class to be nominal");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaEnum || types.get(i) instanceof BoaFloat ||
								types.get(i) instanceof BoaInt || types.get(i) instanceof BoaTime || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "HoeffdingTree required attributes to be numeric, nominal or date");
					}
				} else if(lhs instanceof BoaIBk) {
					if(!(types.get(types.size() - 1) instanceof BoaEnum || types.get(types.size() - 1) instanceof BoaInt
							|| types.get(types.size() - 1) instanceof BoaFloat || types.get(types.size() - 1) instanceof BoaTime))
						throw new TypeCheckException(n, "IBk required class to be numeric, nominal or date");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaEnum || types.get(i) instanceof BoaFloat ||
								types.get(i) instanceof BoaInt || types.get(i) instanceof BoaTime || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "IBk required attributes to be numeric, nominal or date");
					}
				} else if(lhs instanceof BoaInputMappedClassifier) {
					if(!(types.get(types.size() - 1) instanceof BoaEnum || types.get(types.size() - 1) instanceof BoaInt
							|| types.get(types.size() - 1) instanceof BoaFloat || types.get(types.size() - 1) instanceof BoaTime))
						throw new TypeCheckException(n, "InputMappedClassifier required class to be numeric, nominal or date");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaEnum || types.get(i) instanceof BoaFloat ||
								types.get(i) instanceof BoaInt || types.get(i) instanceof BoaTime || types.get(i) instanceof BoaString || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "InputMappedClassifier required attributes to be numeric, nominal, date or string");
					}
				} else if(lhs instanceof BoaIterativeClassifierOptimizer) {
					if(!(types.get(types.size() - 1) instanceof BoaEnum))
						throw new TypeCheckException(n, "IterativeClassifierOptimizer required class to be nominal");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaEnum || types.get(i) instanceof BoaFloat ||
								types.get(i) instanceof BoaInt || types.get(i) instanceof BoaTime || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "IterativeClassifierOptimizer required attributes to be numeric, nominal or date");
					}
				} else if(lhs instanceof BoaJ48) {
					if(!(types.get(types.size() - 1) instanceof BoaEnum))
						throw new TypeCheckException(n, "J48 required class to be nominal");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaEnum || types.get(i) instanceof BoaFloat ||
								types.get(i) instanceof BoaInt || types.get(i) instanceof BoaTime || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "J48 required attributes to be numeric, nominal or date");
					}
				} else if(lhs instanceof BoaJRip) {
					if(!(types.get(types.size() - 1) instanceof BoaEnum))
						throw new TypeCheckException(n, "JRip required class to be nominal");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaEnum || types.get(i) instanceof BoaFloat ||
								types.get(i) instanceof BoaInt || types.get(i) instanceof BoaTime || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "JRip required attributes to be numeric, nominal or date");
					}
				} else if(lhs instanceof BoaKStar) {
					if(!(types.get(types.size() - 1) instanceof BoaEnum || types.get(types.size() - 1) instanceof BoaInt
							|| types.get(types.size() - 1) instanceof BoaFloat || types.get(types.size() - 1) instanceof BoaTime))
						throw new TypeCheckException(n, "KStar required class to be numeric, nominal or date");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaEnum || types.get(i) instanceof BoaFloat ||
								types.get(i) instanceof BoaInt || types.get(i) instanceof BoaTime || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "KStar required attributes to be numeric, nominal or date");
					}
				} else if(lhs instanceof BoaLinearRegression) {
					if(!(types.get(types.size() - 1) instanceof BoaInt
							|| types.get(types.size() - 1) instanceof BoaFloat || types.get(types.size() - 1) instanceof BoaTime))
						throw new TypeCheckException(n, "LinearRegression required class to be numeric or date");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaEnum || types.get(i) instanceof BoaFloat ||
								types.get(i) instanceof BoaInt || types.get(i) instanceof BoaTime || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "LinearRegression required attributes to be numeric, nominal or date");
					}
				} else if(lhs instanceof BoaLMT) {
					if(!(types.get(types.size() - 1) instanceof BoaEnum))
						throw new TypeCheckException(n, "LMT required class to be nominal");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaEnum || types.get(i) instanceof BoaFloat ||
								types.get(i) instanceof BoaInt || types.get(i) instanceof BoaTime || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "LMT required attributes to be numeric, nominal or date");
					}
				} else if(lhs instanceof BoaLogisticRegression) {
					if(!(types.get(types.size() - 1) instanceof BoaEnum))
						throw new TypeCheckException(n, "LogisticRegression required class to be nominal");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaEnum || types.get(i) instanceof BoaFloat ||
								types.get(i) instanceof BoaInt || types.get(i) instanceof BoaTime || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "LogisticRegression required attributes to be numeric, nominal or date");
					}
				} else if(lhs instanceof BoaLogitBoost) {
					if(!(types.get(types.size() - 1) instanceof BoaEnum))
						throw new TypeCheckException(n, "LogitBoost required class to be nominal");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaEnum || types.get(i) instanceof BoaFloat ||
								types.get(i) instanceof BoaInt || types.get(i) instanceof BoaTime || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "LogitBoost required attributes to be numeric, nominal or date");
					}
				} else if(lhs instanceof BoaLsa) {
					if(!(types.get(types.size() - 1) instanceof BoaEnum || types.get(types.size() - 1) instanceof BoaInt
							|| types.get(types.size() - 1) instanceof BoaFloat || types.get(types.size() - 1) instanceof BoaTime))
						throw new TypeCheckException(n, "Lsa required class to be numeric, nominal or date");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaEnum || types.get(i) instanceof BoaFloat ||
								types.get(i) instanceof BoaInt || types.get(i) instanceof BoaTime || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "Lsa required attributes to be numeric, nominal or date");
					}
				} else if(lhs instanceof BoaLWL) {
					if(!(types.get(types.size() - 1) instanceof BoaEnum || types.get(types.size() - 1) instanceof BoaInt
							|| types.get(types.size() - 1) instanceof BoaFloat || types.get(types.size() - 1) instanceof BoaTime))
						throw new TypeCheckException(n, "LWL required class to be numeric, nominal or date");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaEnum || types.get(i) instanceof BoaFloat ||
								types.get(i) instanceof BoaInt || types.get(i) instanceof BoaTime || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "LWL required attributes to be numeric, nominal or date");
					}
				} else if(lhs instanceof BoaMultiClassClassifier) {
					if(!(types.get(types.size() - 1) instanceof BoaEnum))
						throw new TypeCheckException(n, "MultiClassClassifier required class to be nominal");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaEnum || types.get(i) instanceof BoaFloat ||
								types.get(i) instanceof BoaInt || types.get(i) instanceof BoaTime || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "MultiClassClassifier required attributes to be numeric, nominal or date");
					}
				} else if(lhs instanceof BoaMultiClassClassifierUpdateable) {
					if(!(types.get(types.size() - 1) instanceof BoaEnum))
						throw new TypeCheckException(n, "MultiClassClassifierUpdateable required class to be nominal");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaEnum || types.get(i) instanceof BoaFloat ||
								types.get(i) instanceof BoaInt || types.get(i) instanceof BoaTime || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "MultiClassClassifierUpdateable required attributes to be numeric, nominal or date");
					}
				} else if(lhs instanceof BoaMultilayerPerceptron) {
					if(!(types.get(types.size() - 1) instanceof BoaEnum || types.get(types.size() - 1) instanceof BoaInt
							|| types.get(types.size() - 1) instanceof BoaFloat || types.get(types.size() - 1) instanceof BoaTime))
						throw new TypeCheckException(n, "MultilayerPerceptron required class to be numeric, nominal or date");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaEnum || types.get(i) instanceof BoaFloat ||
								types.get(i) instanceof BoaInt || types.get(i) instanceof BoaTime || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "MultilayerPerceptron required attributes to be numeric, nominal or date");
					}
				} else if(lhs instanceof BoaMultiScheme) {
					if(!(types.get(types.size() - 1) instanceof BoaEnum || types.get(types.size() - 1) instanceof BoaInt
							|| types.get(types.size() - 1) instanceof BoaFloat || types.get(types.size() - 1) instanceof BoaTime))
						throw new TypeCheckException(n, "MultiScheme required class to be numeric, nominal or date");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaEnum || types.get(i) instanceof BoaFloat ||
								types.get(i) instanceof BoaInt || types.get(i) instanceof BoaTime || types.get(i) instanceof BoaString || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "MultiScheme required attributes to be numeric, nominal, date or string");
					}
				} else if(lhs instanceof BoaNaiveBayes) {
					if(!(types.get(types.size() - 1) instanceof BoaEnum))
						throw new TypeCheckException(n, "NaiveBayes required class to be nominal");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaEnum || types.get(i) instanceof BoaFloat ||
								types.get(i) instanceof BoaInt || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "NaiveBayes required attributes to be numeric or nominal");
					}
				} else if(lhs instanceof BoaNaiveBayesMultinomial) {
					if(!(types.get(types.size() - 1) instanceof BoaEnum))
						throw new TypeCheckException(n, "NaiveBayesMultinomial required class to be nominal");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaFloat ||
								types.get(i) instanceof BoaInt || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "NaiveBayesMultinomial required attributes to be numeric");
					}
				} else if(lhs instanceof BoaNaiveBayesMultinomialUpdateable) {
					if(!(types.get(types.size() - 1) instanceof BoaEnum))
						throw new TypeCheckException(n, "NaiveBayesMultinomialUpdateable required class to be nominal");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaFloat ||
								types.get(i) instanceof BoaInt || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "NaiveBayesMultinomialUpdateable required attributes to be numeric");
					}
				} else if(lhs instanceof BoaOneR) {
					if(!(types.get(types.size() - 1) instanceof BoaEnum))
						throw new TypeCheckException(n, "OneR required class to be nominal");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaEnum || types.get(i) instanceof BoaFloat ||
								types.get(i) instanceof BoaInt || types.get(i) instanceof BoaTime || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "OneR required attributes to be numeric, nominal or date");
					}
				} else if(lhs instanceof BoaPART) {
					if(!(types.get(types.size() - 1) instanceof BoaEnum))
						throw new TypeCheckException(n, "PART required class to be nominal");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaEnum || types.get(i) instanceof BoaFloat ||
								types.get(i) instanceof BoaInt || types.get(i) instanceof BoaTime || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "PART required attributes to be numeric, nominal or date");
					}
				} else if(lhs instanceof BoaPrincipalComponents) {
					if(!(types.get(types.size() - 1) instanceof BoaEnum || types.get(types.size() - 1) instanceof BoaInt
							|| types.get(types.size() - 1) instanceof BoaFloat || types.get(types.size() - 1) instanceof BoaTime))
						throw new TypeCheckException(n, "PrincipalComponents required class to be numeric, nominal or date");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaEnum || types.get(i) instanceof BoaFloat ||
								types.get(i) instanceof BoaInt || types.get(i) instanceof BoaTime || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "PrincipalComponents required attributes to be numeric, nominal or date");
					}
				} else if(lhs instanceof BoaRandomForest) {
					if(!(types.get(types.size() - 1) instanceof BoaEnum || types.get(types.size() - 1) instanceof BoaInt
							|| types.get(types.size() - 1) instanceof BoaFloat))
						throw new TypeCheckException(n, "RandomForest required class to be numeric or nominal");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaEnum || types.get(i) instanceof BoaFloat ||
								types.get(i) instanceof BoaInt || types.get(i) instanceof BoaTime || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "RandomForest required attributes to be numeric, nominal or date");
					}
				} else if(lhs instanceof BoaSimpleKMeans) {
					for(int i=0; i<types.size(); i++) {
						if(!(types.get(i) instanceof BoaEnum || types.get(i) instanceof BoaFloat ||
								types.get(i) instanceof BoaInt || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "SimpleKMeans required attributes to be numeric or nominal");
					}
				} else if(lhs instanceof BoaSMO) {
					if(!(types.get(types.size() - 1) instanceof BoaEnum))
						throw new TypeCheckException(n, "SMO required class to be nominal");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaEnum || types.get(i) instanceof BoaFloat ||
								types.get(i) instanceof BoaInt || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "SMO required attributes to be numeric or nominal");
					}
				} else if(lhs instanceof BoaVote) {
					if(!(types.get(types.size() - 1) instanceof BoaEnum || types.get(types.size() - 1) instanceof BoaInt
							|| types.get(types.size() - 1) instanceof BoaFloat || types.get(types.size() - 1) instanceof BoaTime))
						throw new TypeCheckException(n, "Vote required class to be numeric, nominal or date");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaEnum || types.get(i) instanceof BoaFloat ||
								types.get(i) instanceof BoaInt || types.get(i) instanceof BoaTime || types.get(i) instanceof BoaString || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "Vote required attributes to be numeric, nominal, date or string");
					}
				} else if(lhs instanceof BoaZeroR) {
					if(!(types.get(types.size() - 1) instanceof BoaEnum || types.get(types.size() - 1) instanceof BoaInt
							|| types.get(types.size() - 1) instanceof BoaFloat || types.get(types.size() - 1) instanceof BoaTime))
						throw new TypeCheckException(n, "ZeroR required class to be numeric, nominal or date");
					for(int i=0; i<types.size()-1; i++) {
						if(!(types.get(i) instanceof BoaEnum || types.get(i) instanceof BoaFloat ||
								types.get(i) instanceof BoaInt || types.get(i) instanceof BoaTime || types.get(i) instanceof BoaString || types.get(i) instanceof BoaArray))
							throw new TypeCheckException(n, "ZeroR required attributes to be numeric, nominal, date or string");
					}
				}
			}

			if(n.getType().type instanceof BoaTuple && rhs != null && rhs instanceof BoaTuple) {
				if (((BoaTuple)n.getType().type).getSize() != ((BoaTuple)rhs).getSize())
					throw new TypeCheckException(n.getInitializer(), "' incorrect number of types for '" + id + "': required " + ((BoaTuple)n.getType().type).getSize() + ", found " + ((BoaTuple)rhs).getSize());
				for (int i = 0; i < ((BoaTuple)n.getType().type).getSize(); i++) {
					BoaType t1 = ((BoaTuple)n.getType().type).getMember(i);
					BoaType t2 = ((BoaTuple)rhs).getMember(i);
					if (!( ((BoaTuple)n.getType().type).getMember(i).assigns(((BoaTuple)rhs).getMember(i))))
						throw new TypeCheckException(n.getInitializer(), "incompatible type '" + rhs + "' for assignment to '" + id + ": " + lhs + "'");
					if(((BoaTuple)n.getType().type).getMember(i) instanceof BoaEnum && ((BoaTuple)rhs).getMember(i) instanceof BoaEnum)
						if(!checkSameEnum(((BoaTuple)n.getType().type).getMember(i).toString(), ((BoaTuple)rhs).getMember(i).toString()) )
							throw new TypeCheckException(n.getInitializer(), "incompatible type '" + ((BoaTuple)rhs).getMember(i) + "' for assignment to '" + id + ": " + ((BoaTuple)n.getType().type).getMember(i) + "'");
					if(((BoaTuple)n.getType().type).getMember(i) instanceof BoaName)
						if( ((BoaName)(((BoaTuple)n.getType().type).getMember(i))).getType() instanceof BoaEnum && ((BoaTuple)rhs).getMember(i) instanceof BoaEnum)
							if(!checkSameEnum(((BoaName)(((BoaTuple)n.getType().type).getMember(i))).getType().toString(), ((BoaTuple)rhs).getMember(i).toString()) )
								throw new TypeCheckException(n.getInitializer(), "incompatible type '" + ((BoaTuple)rhs).getMember(i) + "' for assignment to '" + id + ": " + ((BoaName)(((BoaTuple)n.getType().type).getMember(i))).getType() + "'");
				}
			}

			if (rhs != null && !lhs.assigns(rhs) && !env.hasCast(rhs, lhs))
				throw new TypeCheckException(n.getInitializer(), "incorrect type '" + rhs + "' for assignment to '" + id + ": " + lhs + "'");
		} else {
			if (rhs == null)
				throw new TypeCheckException(n, "variable declaration requires an explicit type or an initializer");

			lhs = rhs;
		}

		if (lhs instanceof BoaFunction && (env.hasGlobalFunction(id) || env.hasLocalFunction(id)))
			throw new TypeCheckException(n.getId(), "name conflict: a function '" + id + "' already exists");

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

		st.setIsBeforeVisitor(n.isBefore());
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

		n.getBody().accept(this, st);
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

		n.getBody().accept(this, st);
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
		n.env = env;
		n.getValue().accept(this, env);
		n.type = new BoaArray(n.getValue().type);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FunctionType n, final SymbolTable env) {
		n.env = env;

		final BoaType[] params = new BoaType[n.getArgsSize()];
		if (n.getArgsSize() > 0) {
			int i = 0;
			for (final Component c : n.getArgs()) {
				c.getType().accept(this, env);
				params[i++] = new BoaName(c.getType().type, c.getIdentifier().getToken());
				env.set(c.getIdentifier().getToken(), c.getType().type);
				c.getIdentifier().accept(this, env);
			}
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
		n.env = env;
		n.getValue().accept(this, env);
		n.getIndex().accept(this, env);
		n.type = new BoaMap(n.getValue().type, n.getIndex().type);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final OutputType n, final SymbolTable env) {
		n.env = env;

		List<BoaScalar> indexTypes = null;
		if (n.getIndicesSize() > 0) {
			indexTypes = new ArrayList<BoaScalar>();

			for (final Component c : n.getIndices()) {
				c.accept(this, env);

				if (!(c.type instanceof BoaScalar))
					throw new TypeCheckException(c, "incorrect type '" + c.type + "' for index");

				indexTypes.add((BoaScalar) c.type);
			}
		}

		n.getType().accept(this, env);
		final BoaType type = n.getType().type;

		final AggregatorSpec annotation;
		try {
			annotation = env.getAggregators(n.getId().getToken(), type).get(0).getAnnotation(AggregatorSpec.class);
		} catch (final RuntimeException e) {
			throw new TypeCheckException(n, e.getMessage(), e);
		}

		BoaScalar tweight = null;
		if (n.hasWeight()) {
			if (annotation.weightType().equals("none"))
				throw new TypeCheckException(n.getWeight(), "output aggregator '" + n.getId().getToken() + "' does not expect a weight");

			final BoaType aweight = SymbolTable.getType(annotation.weightType());
			n.getWeight().accept(this, env);
			tweight = (BoaScalar) n.getWeight().type;

			if (!aweight.assigns(tweight))
				throw new TypeCheckException(n.getWeight(), "invalid weight type, found: " + tweight + " expected: " + aweight);
		} else if (!annotation.weightType().equals("none") && !annotation.weightType().equals("any"))
			throw new TypeCheckException(n, "output aggregator expects a weight type");

		if (n.getArgsSize() > 0 && annotation.formalParameters().length == 0)
			throw new TypeCheckException(n.getArgs(), "output aggregator '" + n.getId().getToken() + "' takes no arguments");

		n.type = new BoaTable(type, indexTypes, tweight, annotation.canOmitWeight());
		env.set(n.getId().getToken(), n.type);
		n.getId().accept(this, env);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ModelType n, final SymbolTable env) {
		n.env = env;
		n.getType().accept(this, env);

		if (env.hasType(n.getId().getToken()))
			n.type = SymbolTable.getType(n.getId().getToken());
		else
			try {
				n.type = env.get(n.getId().getToken());
			} catch (final RuntimeException e) {
				throw new TypeCheckException(n, "invalid identifier '" + n.getId().getToken() + "'", e);
			}

		if(n.type instanceof BoaAdaBoostM1)
			n.type = new BoaAdaBoostM1(n.getType().type);
		else if(n.type instanceof BoaAdditiveRegression)
			n.type = new BoaAdditiveRegression(n.getType().type);
		else if(n.type instanceof BoaApriori)
			n.type = new BoaApriori(n.getType().type);
		else if(n.type instanceof BoaAttributeSelectedClassifier)
			n.type = new BoaAttributeSelectedClassifier(n.getType().type);
		else if(n.type instanceof BoaBagging)
			n.type = new BoaBagging(n.getType().type);
		else if(n.type instanceof BoaBayesNetGenerator)
			n.type = new BoaBayesNetGenerator(n.getType().type);
		else if(n.type instanceof BoaBayesNet)
			n.type = new BoaBayesNet(n.getType().type);
		else if(n.type instanceof BoaClassificationViaRegression)
			n.type = new BoaClassificationViaRegression(n.getType().type);
		else if(n.type instanceof BoaCVParameterSelection)
			n.type = new BoaCVParameterSelection(n.getType().type);
		else if(n.type instanceof BoaDecisionStump)
			n.type = new BoaDecisionStump(n.getType().type);
		else if(n.type instanceof BoaDecisionTable)
			n.type = new BoaDecisionTable(n.getType().type);
		else if(n.type instanceof BoaFilteredClassifier)
			n.type = new BoaFilteredClassifier(n.getType().type);
		else if(n.type instanceof BoaGaussianProcesses)
			n.type = new BoaGaussianProcesses(n.getType().type);
		else if(n.type instanceof BoaHoeffdingTree)
			n.type = new BoaHoeffdingTree(n.getType().type);
		else if(n.type instanceof BoaIBk)
			n.type = new BoaIBk(n.getType().type);
		else if(n.type instanceof BoaInputMappedClassifier)
			n.type = new BoaInputMappedClassifier(n.getType().type);
		else if(n.type instanceof BoaIterativeClassifierOptimizer)
			n.type = new BoaIterativeClassifierOptimizer(n.getType().type);
		else if(n.type instanceof BoaJ48)
			n.type = new BoaJ48(n.getType().type);
		else if(n.type instanceof BoaJRip)
			n.type = new BoaJRip(n.getType().type);
		else if(n.type instanceof BoaKStar)
			n.type = new BoaKStar(n.getType().type);
		else if(n.type instanceof BoaLinearRegression)
			n.type = new BoaLinearRegression(n.getType().type);
		else if(n.type instanceof BoaLMT)
			n.type = new BoaLMT(n.getType().type);
		else if(n.type instanceof BoaLogisticRegression)
			n.type = new BoaLogisticRegression(n.getType().type);
		else if(n.type instanceof BoaLogitBoost)
			n.type = new BoaLogitBoost(n.getType().type);
		else if(n.type instanceof BoaLsa)
			n.type = new BoaLsa(n.getType().type);
		else if(n.type instanceof BoaLWL)
			n.type = new BoaLWL(n.getType().type);
		else if(n.type instanceof BoaMultiClassClassifier)
			n.type = new BoaMultiClassClassifier(n.getType().type);
		else if(n.type instanceof BoaMultiClassClassifierUpdateable)
			n.type = new BoaMultiClassClassifierUpdateable(n.getType().type);
		else if(n.type instanceof BoaMultilayerPerceptron)
			n.type = new BoaMultilayerPerceptron(n.getType().type);
		else if(n.type instanceof BoaMultiScheme)
			n.type = new BoaMultiScheme(n.getType().type);
		else if(n.type instanceof BoaNaiveBayes)
			n.type = new BoaNaiveBayes(n.getType().type);
		else if(n.type instanceof BoaNaiveBayesMultinomial)
			n.type = new BoaNaiveBayesMultinomial(n.getType().type);
		else if(n.type instanceof BoaNaiveBayesMultinomialUpdateable)
			n.type = new BoaNaiveBayesMultinomialUpdateable(n.getType().type);
		else if(n.type instanceof BoaOneR)
			n.type = new BoaOneR(n.getType().type);
		else if(n.type instanceof BoaPART)
			n.type = new BoaPART(n.getType().type);
		else if(n.type instanceof BoaPrincipalComponents)
			n.type = new BoaPrincipalComponents(n.getType().type);
		else if(n.type instanceof BoaRandomForest)
			n.type = new BoaRandomForest(n.getType().type);
		else if(n.type instanceof BoaSimpleKMeans)
			n.type = new BoaSimpleKMeans(n.getType().type);
		else if(n.type instanceof BoaSMO)
			n.type = new BoaSMO(n.getType().type);
		else if(n.type instanceof BoaVote)
			n.type = new BoaVote(n.getType().type);
		else if(n.type instanceof BoaZeroR)
			n.type = new BoaZeroR(n.getType().type);

	}

	/** {@inheritDoc} */
	@Override
	public void visit(final StackType n, final SymbolTable env) {
		n.env = env;
		n.getValue().accept(this, env);
		n.type = new BoaStack(n.getValue().type);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SetType n, final SymbolTable env) {
		n.env = env;
		n.getValue().accept(this, env);
		n.type = new BoaSet(n.getValue().type);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final TupleType n, final SymbolTable env) {
		n.env = env;

		final List<BoaType> types = new ArrayList<BoaType>();

		for (final Component c : n.getMembers()) {
			c.accept(this, env);
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

			Factor f = c.getExp().getLhs().getLhs().getLhs().getLhs().getLhs();
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

	protected List<BoaType> check(final Call c, final SymbolTable env) {
		if (c.getArgsSize() > 0)
			return this.check(c.getArgs(), env);

		return new ArrayList<BoaType>();
	}

	protected List<BoaType> check(final List<Expression> el, final SymbolTable env) {
		final List<BoaType> types = new ArrayList<BoaType>();

		for (final Expression e : el) {
			e.accept(this, env);

			// special case of a function call, use its return type instead of function type
			if (e.type instanceof BoaFunction) {
				callFinder.start(e);
				if (callFinder.isCall()) {
					types.add(((BoaFunction) e.type).getType());
					continue;
				}
			}

			types.add(e.type);
		}

		return types;
	}

	protected boolean checkTupleArray(final List<BoaType> types) {
		if (types == null)
			return false;

		final String type = types.get(0).toBoxedJavaType();
		final boolean isEnum = types.get(0) instanceof BoaEnum;

		for (int i = 1; i < types.size(); i++)
			if (!type.equals(types.get(i).toBoxedJavaType()))
				if (!(types.get(i) instanceof BoaEnum && isEnum))
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

	protected boolean checkSameEnum(String s1, String s2) {
		Pattern p = Pattern.compile("\"(.*?)\"");
		Matcher m1 = p.matcher(s1);
		Matcher m2 = p.matcher(s2);
		String s3 = "";
		String s4 = "";
		if(m2.find()) {
			s3 = m2.group(0);
		}

		while (m1.find()) {
			s4 = s4 + m1.group(0);
			if(s4.contains(s3)) {
				return true;
			}
		}
		return false;
	}
}
