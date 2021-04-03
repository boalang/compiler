/*
 * Copyright 2017, Anthony Urso, Hridesh Rajan, Robert Dyer, Ramanathan Ramu, Che Shian Hung
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.stringtemplate.v4.ST;

import boa.aggregators.AggregatorSpec;
import boa.compiler.SymbolTable;
import boa.compiler.TypeCheckException;
import boa.compiler.ast.*;
import boa.compiler.ast.expressions.*;
import boa.compiler.ast.literals.*;
import boa.compiler.ast.statements.*;
import boa.compiler.ast.types.*;
import boa.compiler.visitors.analysis.*;
import boa.types.*;
import boa.types.ml.BoaModel;

/**
 *
 * @author anthonyu
 * @author rdyer
 * @author ankuraga
 * @author rramu
 * @author hungc
 */
public class CodeGeneratingVisitor extends AbstractCodeGeneratingVisitor {
	String identifier = "";
	boolean flowSensitive = false;
	boolean loopSensitive = false;
	HashMap<String, Boolean> traversalMap = new HashMap<String, Boolean>();
	String lastVarDecl;

	/**
	 *
	 * @author anthonyu
	 */
	protected class AggregatorDescription {
		protected String aggregator;
		protected BoaType type;
		protected List<String> parameters;

		public AggregatorDescription(final String aggregator, final BoaType type) {
			this(aggregator, type, null);
		}

		public AggregatorDescription(final String aggregator, final BoaType type, final List<String> parameters) {
			this.aggregator = aggregator;
			this.type = type;
			this.parameters = parameters;
		}

		/**
		 * @return the name
		 */
		public String getAggregator() {
			return this.aggregator;
		}

		/**
		 * @return the parameters
		 */
		public List<String> getParameters() {
			return this.parameters;
		}

		/**
		 * @param name
		 *            the name to set
		 */
		public void setAggregator(final String aggregator) {
			this.aggregator = aggregator;
		}

		/**
		 * @param parameters
		 *            the parameters to set
		 */
		public void setParameters(final List<String> parameters) {
			this.parameters = parameters;
		}

		/**
		 * @return the types
		 */
		public BoaType getType() {
			return this.type;
		}

		/**
		 * @param types
		 *            the types to set
		 */
		public void setTypes(final BoaType type) {
			this.type = type;
		}
	}

	/**
	 * Scan the program and generate code for any variable declarations.
	 *
	 * @author rdyer
	 */
	protected class VarDeclCodeGeneratingVisitor extends AbstractCodeGeneratingVisitor {
		private boolean nest;

		/** {@inheritDoc} */
		@Override
		protected void initialize() {
			super.initialize();
			nest = true;
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final Program n) {
			if (!nest) return;

			nest = false;
			super.visit(n);
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final FunctionExpression n) {
			if (!nest) return;

			nest = false;
			super.visit(n);
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final VisitorExpression n) {
			if (!nest) return;

			nest = false;
			super.visit(n);
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final TraversalExpression n) {
			if (!nest) return;

			nest = false;
			super.visit(n);
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final FixPExpression n) {
			if (!nest) return;

			nest = false;
			super.visit(n);
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final VarDeclStatement n) {
			if (n.type instanceof BoaTable)
				return;

			final ST st = stg.getInstanceOf("VarDecl");

			st.add("id", n.getId().getToken());
			st.add("type", n.type.toJavaType());

			if (n.isStatic())
				st.add("isstatic", true);

			code.add(st.render());
		}
	}

	/**
	 * Finds the set of all function types and generates classes for each unique type.
	 *
	 * @author rdyer
	 */
	protected class FunctionDeclaratorCodeGeneratingVisitor extends AbstractCodeGeneratingVisitor {
		protected final Set<String> funcs = new HashSet<String>();

		/** {@inheritDoc} */
		@Override
		public void initialize() {
			super.initialize();
			funcs.clear();
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final FunctionType n) {
			final String name = ((BoaFunction)n.type).toJavaType();
			if (funcs.contains(name))
				return;

			super.visit(n);

			funcs.add(name);

			final ST st = stg.getInstanceOf("FunctionType");

			final BoaFunction funcType = ((BoaFunction) n.type);

			final List<Component> params = n.getArgs();
			final List<String> args = new ArrayList<String>();
			final List<String> types = new ArrayList<String>();

			for (final Component c : params) {
				args.add(c.getIdentifier().getToken());
				types.add(c.getType().type.toJavaType());
			}

			st.add("name", funcType.toJavaType());
			if (funcType.getType() instanceof BoaAny)
				st.add("ret", "void");
			else
				st.add("ret", funcType.getType().toBoxedJavaType());
			st.add("args", args);
			st.add("types", types);

			code.add(st.render());
		}
	}

	/**
	 * Finds the set of all tuple types and generates classes for each unique tuple type.
	 *
	 * @author ankuraga
	 */
	protected class TupleDeclaratorCodeGeneratingVisitor extends AbstractCodeGeneratingVisitor {
		protected final Set<String> tuples = new HashSet<String>();

		/** {@inheritDoc} */
		@Override
		public void initialize() {
			super.initialize();
			tuples.clear();
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final Composite n) {
			super.visit(n);

			if (n.type instanceof BoaTuple) {
				final String name = ((BoaTuple)n.type).toJavaType();
				if (tuples.contains(name))
					return;
				tuples.add(name);

				final ST st = stg.getInstanceOf("TupleType");

				final List<String> fields = new ArrayList<String>();
				final List<String> types = new ArrayList<String>();
				final List<Boolean> protos = new ArrayList<Boolean>();
				final List<Boolean> enums = new ArrayList<Boolean>();

				int counter = 0;
				for (final Expression e : n.getExprs()) {
					fields.add("f" + counter);
					BoaType type = e.type;
					counter++;
					types.add(type.toBoxedJavaType());
					protos.add(type instanceof BoaProtoTuple);
					enums.add(type instanceof BoaEnum);
				}

				st.add("name", name);
				st.add("fields", fields);
				st.add("types", types);
				st.add("protos", protos);
				st.add("enums", enums);

				code.add(st.render());
			}
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final TupleType n) {
			final String name = ((BoaTuple)n.type).toJavaType();
			if (tuples.contains(name))
				return;

			super.visit(n);

			tuples.add(name);

			final ST st = stg.getInstanceOf("TupleType");

			final BoaTuple tupType = ((BoaTuple) n.type);

			final List<Component> members = n.getMembers();
			final List<String> fields = new ArrayList<String>();
			final List<String> types = new ArrayList<String>();
			final List<Boolean> protos = new ArrayList<Boolean>();
			final List<Boolean> enums = new ArrayList<Boolean>();

			int fieldCount = 0;
			for (final Component c : members) {
				if (c.hasIdentifier()) {
					fields.add(c.getIdentifier().getToken());
				} else {
					fields.add("f" + fieldCount);
				}
				fieldCount++;
				BoaType type = c.getType().type;
				protos.add(type instanceof BoaProtoTuple);
				enums.add(type instanceof BoaEnum);
				types.add(type.toBoxedJavaType());
			}

			st.add("name", tupType.toJavaType());
			st.add("fields", fields);
			st.add("types", types);
			st.add("protos", protos);
			st.add("enums", enums);

			code.add(st.render());
		}
	}

	/**
	 * Finds the set of all enum types and generates classes for each enum type.
	 *
	 * @author ankuraga
	 */
	protected class EnumDeclaratorCodeGeneratingVisitor extends AbstractCodeGeneratingVisitor {
		/** {@inheritDoc} */
		@Override
		public void visit(final EnumType n) {
			final ST st = stg.getInstanceOf("EnumType");

			final BoaEnum enumType = ((BoaEnum) n.type);
			final BoaType fieldType = enumType.getType();
			final List<String> fields = new ArrayList<String>();
			final List<String> values = new ArrayList<String>();

			for (final EnumBodyDeclaration c : n.getMembers()) {
				final Factor f = c.getExp().getLhs().getLhs().getLhs().getLhs().getLhs();

				if (f.getOperand() instanceof ILiteral) {
					code.add(((ILiteral)(f.getOperand())).getLiteral());
					fields.add(c.getIdentifier().getToken());
					values.add(code.removeLast());
				}
			}

			st.add("ename", enumType.toJavaType());
			st.add("fields", fields);
			st.add("values", values);
			st.add("fname", fieldType.toJavaType());

			code.add(st.render());
		}
	}


	/**
	 *
	 * @author rdyer
	 */
	protected class StaticInitializationCodeGeneratingVisitor extends AbstractCodeGeneratingVisitor {
		/** {@inheritDoc} */
		@Override
		public void visit(final VarDeclStatement n) {
			if (!n.isStatic() || !n.hasInitializer())
				return;

			n.env.setId("___" + n.getId().getToken());
			n.getInitializer().accept(this);
			n.env.setId(null);
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final Pair n) {
			super.visit(n);

			final ST st = stg.getInstanceOf("Pair");

			st.add("map", n.env.getId());
			st.add("value", code.removeLast());
			st.add("key", code.removeLast());

			code.add(st.render());
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final Composite n) {
			if (n.isEmpty())
				return;

			String s = "";

			for (final Pair p : n.getPairs()) {
				if (s.length() > 0)
					s += "\n";
				p.accept(this);
				s += code.removeLast();
			}

			code.add(s);
		}
	}

	/**
	 *
	 * @author rdyer
	 */
	protected class IndexeeFindingVisitor extends AbstractVisitorNoReturn<String> {
		protected Factor firstFactor;
		protected Node lastFactor;

		protected Map<Node, Node> lastFactors = new HashMap<Node, Node>();
		protected final Set<Node> indexees = new HashSet<Node>();

		/** {@inheritDoc} */
		@Override
		protected void initialize(String arg) {
			lastFactors.clear();
			indexees.clear();
			firstFactor = null;
			lastFactor = null;
		}

		public Map<Node, Node> getFactors() {
			return lastFactors;
		}

		public Set<Node> getIndexees() {
			return indexees;
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final Factor n, final String arg) {
			firstFactor = n;
			n.getOperand().accept(this, arg);
			lastFactor = n.getOperand();

			for (final Node f : n.getOps()) {
				f.accept(this, arg);
				lastFactor = f;
			}
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final Index n, final String arg) {
			CodeGeneratingVisitor.this.idFinder.start(n);

			if (CodeGeneratingVisitor.this.idFinder.getNames().contains(arg)) {
				indexees.add(firstFactor);
				lastFactors.put(firstFactor, lastFactor);
			}
		}
	}

	protected final IdentifierFindingVisitor idFinder = new IdentifierFindingVisitor();
	protected final IndexeeFindingVisitor indexeeFinder = new IndexeeFindingVisitor();
	protected final CallFindingVisitor callFinder = new CallFindingVisitor();
	protected final VarDeclCodeGeneratingVisitor varDecl;
	protected final StaticInitializationCodeGeneratingVisitor staticInitialization;
	protected final FunctionDeclaratorCodeGeneratingVisitor functionDeclarator;
	protected final TupleDeclaratorCodeGeneratingVisitor tupleDeclarator;
	protected final EnumDeclaratorCodeGeneratingVisitor enumDeclarator;

	protected final HashMap<String, AggregatorDescription> aggregators = new HashMap<String, AggregatorDescription>();

	protected String skipIndex = "";
	protected boolean abortGeneration = false;

	protected String className;
	protected int splitSize;
	protected int seed;
	protected boolean isLocal;

	public CodeGeneratingVisitor(final String className, final int splitSize, final int seed, final boolean isLocal) throws IOException {
		this.className = className;
		this.splitSize = splitSize;
		this.seed = seed;
		this.isLocal = isLocal;

		varDecl = new VarDeclCodeGeneratingVisitor();
		staticInitialization = new StaticInitializationCodeGeneratingVisitor();
		functionDeclarator = new FunctionDeclaratorCodeGeneratingVisitor();
		tupleDeclarator = new TupleDeclaratorCodeGeneratingVisitor();
		enumDeclarator = new EnumDeclaratorCodeGeneratingVisitor();
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Program n) {
		final ST st = stg.getInstanceOf("Program");

		this.varDecl.start(n);
		this.functionDeclarator.start(n);
		this.tupleDeclarator.start(n);
		this.enumDeclarator.start(n);

		if (this.functionDeclarator.hasCode())
			st.add("staticDeclarations", this.varDecl.getCode() + "\n" + this.functionDeclarator.getCode());
		else
			st.add("staticDeclarations", this.varDecl.getCode());

		if (this.tupleDeclarator.hasCode())
			st.add("staticDeclarations", "\n" + this.tupleDeclarator.getCode());
		if (this.enumDeclarator.hasCode())
			st.add("staticDeclarations", "\n" + this.enumDeclarator.getCode());

		this.staticInitialization.start(n);
		if (this.staticInitialization.hasCode())
			st.add("staticStatements", this.staticInitialization.getCode());

		final List<String> statements = new ArrayList<String>();
		for (final Statement s : n.getStatements()) {
			s.accept(this);
			final String statement = code.removeLast();
			if (!statement.isEmpty())
				statements.add(statement);
		}
		st.add("statements", statements);

		if (this.aggregators.size() == 0)
			throw new TypeCheckException(n, "No output variables were declared - must declare at least one output variable");

		final List<String> combineAggregatorStrings = new ArrayList<String>();
		final List<String> reduceAggregatorStrings = new ArrayList<String>();

		for (final Entry<String, AggregatorDescription> entry : this.aggregators.entrySet()) {
			final String id = entry.getKey();

			final AggregatorDescription description = entry.getValue();
			final String parameters = description.getParameters() == null ? "" : description.getParameters().get(0);
			final BoaType type = description.getType();

			boolean combines = false;
			final Class<?> c = n.env.getAggregator(description.getAggregator(), type);
			try {
				final AggregatorSpec annotation = c.getAnnotation(AggregatorSpec.class);
				if (annotation.canCombine())
					combines = true;
			} catch (final RuntimeException e) {
				throw new TypeCheckException(n, e.getMessage(), e);
			}
			reduceAggregatorStrings.add("this.aggregators.put(\"" + id + "\", new " + c.getCanonicalName() + "(" + parameters + "));");
			if (combines)
				combineAggregatorStrings.add(reduceAggregatorStrings.get(reduceAggregatorStrings.size() - 1));
		}

		st.add("combineTables", combineAggregatorStrings);
		st.add("reduceTables", reduceAggregatorStrings);

		final List<String> variableNames = new ArrayList<String>();
		for (final String s : reduceAggregatorStrings)
			variableNames.add(s.substring(s.indexOf('"'), s.indexOf(", new")));
		Collections.sort(variableNames);

		st.add("name", className);
		st.add("splitsize", splitSize);
		st.add("seed", seed);
		st.add("outputVariableNames", variableNames);
		if (isLocal) st.add("isLocal", true);

		code.add(st.render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Call n) {
		final ST st = stg.getInstanceOf("Call");

		this.idFinder.start(n.env.getOperand());
		final String funcName = this.idFinder.getNames().toArray()[0].toString();
		final BoaFunction f = n.env.getFunction(funcName, check(n));
		n.env.setOperandType(n.type);

		if (f.hasMacro()) {
			final List<String> parts = new ArrayList<String>();
			for (final Expression e : n.getArgs()) {
				e.accept(this);
				parts.add(code.removeLast());
			}

			if (funcName.equals("traverse") && parts.size() > 3) {
				if (parts.get(2).equals("boa.types.Graph.Traversal.TraversalKind.HYBRID") && !traversalMap.get(parts.get(3).trim()))
					parts.set(2, "boa.types.Graph.Traversal.TraversalKind.RANDOM");
			}

			final String s = expand(f.getMacro(), n.getArgs(), parts.toArray(new String[]{}));

			// FIXME rdyer a hack, so that "def(pbuf.attr)" generates "pbuf.hasAttr()"
			if (funcName.equals("def") && n.getArgsSize() == 1) {
				final Matcher m = Pattern.compile("\\((\\w+).get(\\w+)\\(\\) != null\\)").matcher(s);
				if (m.matches() && !m.group(2).endsWith("List"))
					st.add("call", m.group(1) + ".has" + m.group(2) + "()");
				// #68 - def(a[i]) was generating a[i] != null which fails for arrays of ints (or any nullable type)
				// so instead, since they are always defined, replace with 'true'
				else if (n.getArg(0).type instanceof BoaScalar && !(n.getArg(0).type instanceof BoaString) && !(n.getArg(0).type instanceof BoaTuple))
					st.add("call", "true");
				else
					st.add("call", s);
			} else {
				st.add("call", s);
			}
		} else {
			if (f.hasName()) {
				st.add("operand", f.getName());
			} else {
				n.env.getOperand().accept(this);
				st.add("operand", code.removeLast() + ".invoke");
			}

			if (n.getArgsSize() > 0) {
				visit(n.getArgs());
				st.add("parameters", code.removeLast());
			}
		}

		code.add(st.render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Comparison n) {
		final ST st = stg.getInstanceOf("Expression");

		if (n.hasRhs()) {
			final List<String> operators = new ArrayList<String>();
			final List<String> operands = new ArrayList<String>();

			n.getLhs().accept(this);
			n.getRhs().accept(this);
			operators.add(n.getOp());

			if (n.getOp().equals("==") || n.getOp().equals("!=")) {
				// special case string/stack/set/map (in)equality
				if (n.getLhs().type instanceof BoaString || n.getLhs().type instanceof BoaStack || n.getLhs().type instanceof BoaQueue || n.getLhs().type instanceof BoaSet || n.getLhs().type instanceof BoaMap) {
					final String expr = code.removeLast() + ".equals(" + code.removeLast() + ")";

					if (n.getOp().equals("!="))
						st.add("lhs", "!" + expr);
					else
						st.add("lhs", expr);

					operators.clear();
				}
				// special case arrays
				else if (n.getLhs().type instanceof BoaArray) {
					final String expr = "boa.functions.BoaIntrinsics.deepEquals(" + code.removeLast() + ", " + code.removeLast() + ")";

					if (n.getOp().equals("!="))
						st.add("lhs", "!" + expr);
					else
						st.add("lhs", expr);

					operators.clear();
				}
				// special case AST (in)equality
				else if (n.getLhs().type instanceof BoaProtoTuple) {
					operands.add(code.removeLast() + ".hashCode()");
					st.add("lhs", code.removeLast() + ".hashCode()");
				} else {
					operands.add(code.removeLast());
					st.add("lhs", code.removeLast());
				}
			} else {
				operands.add(code.removeLast());
				st.add("lhs", code.removeLast());
			}

			st.add("operators", operators);
			st.add("operands", operands);
		} else {
			n.getLhs().accept(this);
			st.add("lhs", code.removeLast());
		}

		code.add(st.render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Component n) {
		// intentionally ignoring the identifier
		n.getType().accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Composite n) {
		final ST st = stg.getInstanceOf("Composite");

		if (n.getPairsSize() > 0) {
			String s = "\t{\n";
			for (final Pair p : n.getPairs()) {
				visit(p);
				s += "\t\t" + code.removeLast() + "\n";
			}
			s += "\t}";

			st.add("exprlist", s);
			st.add("type", n.type.toBoxedJavaType() + "()");
		} else if (n.getExprsSize() > 0) {
			final BoaType t = n.type;

			if (t instanceof BoaTuple) {
				final ST stup = stg.getInstanceOf("Tuple");
				stup.add("name", t.toJavaType());
				visit(n.getExprs());
				stup.add("exprlist", code.removeLast());
				code.add(stup.render());
				return;
			}

			visit(n.getExprs());

			if (t instanceof BoaArray && ((BoaArray)t).getType() instanceof BoaEnum) {
				st.add("type", "Object[] ");
			} else {
				st.add("type", t.toJavaType().replaceAll("<(.*)>", ""));
			}

			st.add("exprlist", code.removeLast());
		}

		code.add(st.render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Pair n) {
		super.visit(n);

		final ST st = stg.getInstanceOf("Pair");

		st.add("map", n.env.getId());
		st.add("value", code.removeLast());
		st.add("key", code.removeLast());

		code.add(st.render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Conjunction n) {
		final ST st = stg.getInstanceOf("Expression");

		n.getLhs().accept(this);
		st.add("lhs", code.removeLast());

		if (n.getRhsSize() > 0) {
			final List<String> operators = new ArrayList<String>();
			final List<String> operands = new ArrayList<String>();

			for (final Comparison c : n.getRhs()) {
				operators.add("&&");
				c.accept(this);
				operands.add(code.removeLast());
			}

			st.add("operators", operators);
			st.add("operands", operands);
		}

		code.add(st.render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Factor n) {
		if (n.getOpsSize() > 0) {
			n.env.setOperand(n.getOperand());

			String accept = "";
			abortGeneration = false;

			if (!(n.getOp(0) instanceof Call)) {
				n.getOperand().accept(this);
				n.env.setOperandType(n.getOperand().type);
				accept = code.removeLast();
			}

			for (int i = 0; !abortGeneration && i < n.getOpsSize(); i++) {
				final Node o = n.getOp(i);

				o.accept(this);
				accept += code.removeLast();
			}

			n.env.getOperandType();

			code.add(accept);
		} else {
			n.getOperand().accept(this);
			code.add(code.removeLast());
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Identifier n) {
		final String id = n.getToken();
		if (n.env.hasType(id)) {
			if (n.env.getNeedsBoxing())
				code.add(SymbolTable.getType(id).toBoxedJavaType());
			else
				code.add(SymbolTable.getType(id).toJavaType());
			return;
		}

		// otherwise return the identifier template
		final ST st = stg.getInstanceOf("Identifier");

		st.add("id", id);

		code.add(st.render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Index n) {
		this.idFinder.start(n.getStart());
		if (idFinder.getNames().contains(this.skipIndex)) {
			abortGeneration = true;
			code.add("");
			return;
		}

		final ST st = stg.getInstanceOf("Index");

		final BoaType t = n.env.getOperandType();
		if (t instanceof BoaMap) {
			n.env.setOperandType(((BoaMap) t).getType());
			st.add("map", true);
		} else if (t instanceof BoaProtoList) {
			n.env.setOperandType(((BoaProtoList) t).getType());
			st.add("map", true);
		} else if (t instanceof BoaArray) {
			n.env.setOperandType(((BoaArray) t).getType());
		}

		st.add("operand", "");

		final BoaType indexType = n.getStart().type;
		n.getStart().accept(this);
		if (indexType instanceof BoaInt && !(t instanceof BoaMap))
			st.add("index", "(int)(" + code.removeLast() + ")");
		else
			st.add("index", code.removeLast());

		if (n.hasEnd()) {
			n.getEnd().accept(this);
			st.add("slice", code.removeLast());
		}

		code.add(st.render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Selector n) {
		try {
			BoaType opType = n.env.getOperandType();
			if (opType instanceof BoaName)
				opType = ((BoaName) opType).getType();

			if (opType == null)
				throw new RuntimeException("operand type is null");

			final String member = n.getId().getToken();

			// operand is a proto map (aka enum)
			if (opType instanceof BoaProtoMap) {
				n.env.setOperandType(new BoaInt());
				code.add("." + member);
				return;
			}

			// operand is a proto tuple
			if (opType instanceof BoaProtoTuple) {
				final BoaType memberType = ((BoaProtoTuple) opType).getMember(member);
				n.env.setOperandType(memberType);
				if (memberType instanceof BoaProtoList)
					code.add(".get" + camelCase(member) + "List()");
				else
					code.add(".get" + camelCase(member) + "()");
				return;
			}

			// operand is a tuple
			if (opType instanceof BoaTuple) {
				final BoaTuple tuple = (BoaTuple) opType;
				n.env.setOperandType(tuple.getMember(member));
				code.add(".___" + tuple.getMemberName(member));
				return;
			}

			// operand is a enum
			if (opType instanceof BoaEnum) {
				final BoaEnum tenum = (BoaEnum) opType;
				n.env.setOperandType(tenum.getMember(member));
				code.add("." + member);
				return;
			}

			throw new RuntimeException("unimplemented operand type: " + opType.getClass());
		} catch (final TypeCheckException e) {
			throw new RuntimeException("unimplemented", e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Term n) {
		final ST st = stg.getInstanceOf("Expression");

		n.getLhs().accept(this);
		st.add("lhs", code.removeLast());

		if (n.getRhsSize() > 0) {
			final List<String> operands = new ArrayList<String>();

			for (final Factor f : n.getRhs()) {
				f.accept(this);
				operands.add(code.removeLast());
			}

			st.add("operators", n.getOps());
			st.add("operands", operands);
		}

		code.add(st.render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final UnaryFactor n) {
		n.getFactor().accept(this);
		if (n.getOp().equals("not"))
			code.add("!" + code.removeLast());
		else
			code.add(n.getOp() + code.removeLast());
	}

	//
	// statements
	//
	/** {@inheritDoc} */
	@Override
	public void visit(final AssignmentStatement n) {
		final ST st = stg.getInstanceOf("Assignment");

		n.getLhs().accept(this);
		final String lhs = code.removeLast();

		n.getRhs().accept(this);
		String rhs = code.removeLast();

		if (n.getLhs().type instanceof BoaTuple && n.getRhs().type instanceof BoaArray) {
			final Operand op = n.getRhs().getLhs().getLhs().getLhs().getLhs().getLhs().getOperand();
			if (op instanceof Composite) {
				final List<Expression> exps = ((Composite)op).getExprs();
				if (checkTupleArray(this.check(exps)) == false) {
					final ST stup = stg.getInstanceOf("Tuple");
					stup.add("name", n.getLhs().type.toJavaType());
					visit(exps);
					stup.add("exprlist", code.removeLast());
					rhs = stup.render();
				}
			}
		}

		// FIXME rdyer hack to fix assigning to maps
		if (lhs.contains(".get(")) {
			int idx = lhs.lastIndexOf(')') - 1;
			int parens = 1;
			for (; idx >= 0; idx--) {
				if (lhs.charAt(idx) == '(')
					parens--;
				else if (lhs.charAt(idx) == ')')
					parens++;
				if (parens == 0) break;
			}
			idx += 1;
			code.add(lhs.substring(0, idx - ".get(".length()) + ".put(" + lhs.substring(idx, lhs.lastIndexOf(')')) + ", " + rhs + lhs.substring(lhs.lastIndexOf(')')) + ";");
			return;
		}

		if (rhs.contains(".load(")) {
			Operand o = n.getLhs().getOperand();
			if (o instanceof Identifier) {
				String token = ((Identifier) o).getToken();
				token = token.substring(0, token.lastIndexOf('_'));
				rhs = rhs.substring(0,rhs.length()-1) + ", \"" + token + "\"" + ", \"" + n.getLhs().type.toJavaType() + "\"" +  ", new " + ((BoaModel)n.getLhs().type).getType().toJavaType() + "())";
			}
			rhs = "(" + (n.getLhs().type + "").split("\\/")[0] + ")" + rhs;
		}

		st.add("lhs", lhs);
		st.add("operator", n.getOp());
		st.add("rhs", rhs);

		code.add(st.render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Block n) {
		final ST st = stg.getInstanceOf("Block");

		final List<String> statements = new ArrayList<String>();

		for (final Node node : n.getStatements()) {
			node.accept(this);
			final String statement = code.removeLast();
			if (!statement.isEmpty())
				statements.add(statement);
		}

		st.add("statements", statements);

		code.add(st.render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final BreakStatement n) {
		code.add(stg.getInstanceOf("Break").render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ContinueStatement n) {
		code.add(stg.getInstanceOf("Continue").render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final DoStatement n) {
		final ST st = stg.getInstanceOf("DoWhile");

		n.getCondition().accept(this);
		st.add("condition", code.removeLast());

		n.getBody().accept(this);
		st.add("stmt", code.removeLast());

		code.add(st.render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final EmitStatement n) {
		final ST st = stg.getInstanceOf("EmitStatement");

		if (n.getIndicesSize() > 0) {
			final List<String> indices = new ArrayList<String>();

			for (final Expression e : n.getIndices()) {
				e.accept(this);
				indices.add(code.removeLast());
			}

			st.add("indices", indices);
		}

		st.add("id", "\"" + n.getId().getToken() + "\"");

		n.getValue().accept(this);
		st.add("expression", code.removeLast());

		if (n.hasWeight()) {
			n.getWeight().accept(this);
			st.add("weight", code.removeLast());
		}

		code.add(st.render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ExprStatement n) {
		final ST st = stg.getInstanceOf("ExprStatement");

		n.getExpr().accept(this);
		st.add("expression", code.removeLast());

		code.add(st.render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ExistsStatement n) {
		final ST st = stg.getInstanceOf("WhenStatement");
		st.add("some", "true");
		generateQuantifier(n, n.getVar(), n.getCondition(), n.getBody(), "exists", st);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ForeachStatement n) {
		final ST st = stg.getInstanceOf("WhenStatement");
		generateQuantifier(n, n.getVar(), n.getCondition(), n.getBody(), "foreach", st);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final IfAllStatement n) {
		final ST st = stg.getInstanceOf("WhenStatement");
		st.add("all", "true");
		generateQuantifier(n, n.getVar(), n.getCondition(), n.getBody(), "ifall", st);
	}

	protected void generateQuantifier(final Node n, final Component c, final Expression e, final Block b, final String kind, final ST st) {
		final BoaType type = c.getType().type;

		final String id = c.getIdentifier().getToken();

		n.env.set(id, type);
		st.add("type", type.toJavaType());
		st.add("index", id);

		this.indexeeFinder.start(e, id);
		final Set<Node> indexees = this.indexeeFinder.getIndexees();

		if (indexees.size() > 0) {
			final List<Node> array = new ArrayList<Node>(indexees);
			final Set<String> seen = new LinkedHashSet<String>();
			String src = "";
			for (int i = 0; i < array.size(); i++) {
				final Factor indexee = (Factor)array.get(i);

				this.skipIndex = id;
				indexee.accept(this);
				final String src2 = code.removeLast();
				this.skipIndex = "";

				if (seen.contains(src2)) continue;
				seen.add(src2);

				final BoaType indexeeType = this.indexeeFinder.getFactors().get(indexee).type;
				final String func = (indexeeType instanceof BoaArray) ? ".length" : ".size()";

				if (src.length() > 0)
					src = "java.lang.Math.min(" + src2 + func + ", " + src + ")";
				else
					src = src2 + func;
			}

			st.add("len", src);
		} else {
			throw new TypeCheckException(e, "quantifier variable '" + id + "' must be used in the " + kind + " condition expression");
		}

		e.accept(this);
		st.add("expression", code.removeLast());

		b.accept(this);
		st.add("statement", code.removeLast());

		code.add(st.render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ForStatement n) {
		final ST st = stg.getInstanceOf("ForStatement");

		if (n.hasInit()) {
			n.getInit().accept(this);
			// FIXME rdyer this is a bit of a hack, to remove the newline
			final String s = code.removeLast();
			st.add("declaration", s.substring(0, s.length() - 1));
		} else
			st.add("declaration", ";");

		if (n.hasCondition()) {
			n.getCondition().accept(this);
			st.add("expression", code.removeLast());
		} else
			st.add("declaration", ";");

		if (n.hasUpdate()) {
			n.getUpdate().accept(this);
			// FIXME rdyer this is a bit of a hack, to remove the semicolon+newline
			final String s = code.removeLast();
			st.add("exprstmt", s.substring(0, s.length() - 2));
		}

		n.getBody().accept(this);
		st.add("statement", code.removeLast());

		code.add(st.render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final IfStatement n) {
		final ST st = stg.getInstanceOf("IfStatement");

		n.getCondition().accept(this);
		st.add("expression", code.removeLast());

		n.getBody().accept(this);
		st.add("statement", code.removeLast());

		if (n.hasElse()) {
			n.getElse().accept(this);
			st.add("elseStatement", code.removeLast());
		}

		code.add(st.render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final PostfixStatement n) {
		final ST st = stg.getInstanceOf("ExprStatement");

		n.getExpr().accept(this);
		st.add("expression", code.removeLast());
		st.add("operator", n.getOp());

		code.add(st.render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ReturnStatement n) {
		final ST st = stg.getInstanceOf("Return");

		if (n.hasExpr()) {
			n.getExpr().accept(this);
			st.add("expr", code.removeLast());
		}

		code.add(st.render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final StopStatement n) {
		code.add(stg.getInstanceOf("Stop").render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SwitchCase n) {
		final ST st = stg.getInstanceOf("SwitchCase");

		final List<String> cases = new ArrayList<String>();
		for (final Expression expr : n.getCases()) {
			expr.accept(this);
			String s = code.removeLast();
			if (expr.type instanceof BoaProtoMap || expr.type instanceof BoaEnum)
				s = s.substring(s.lastIndexOf(".") + 1);
			cases.add(s);
		}

		st.add("cases", cases);
		n.getBody().accept(this);
		st.add("body", code.removeLast());
		code.add(st.render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SwitchStatement n) {
		final ST st = stg.getInstanceOf("Switch");

		final List<String> caseStmts = new ArrayList<String>();

		for (final SwitchCase sc : n.getCases()) {
			sc.accept(this);
			caseStmts.add(code.removeLast());
		}

		final List<String> defBody = new ArrayList<String>();
		n.getDefault().accept(this);
		defBody.add(code.removeLast());

		n.getCondition().accept(this);
		st.add("expr", code.removeLast());
		st.add("cases", caseStmts);
		st.add("body", defBody);

		code.add(st.render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VarDeclStatement n) {
		if (n.isStatic()) {
			code.add("");
			return;
		}

		final BoaType type = n.env.get(n.getId().getToken());

		final BoaType lhsType;
		if (n.hasType()) {
			n.env.setId(n.getId().getToken());
			lhsType = n.getType().type;
			n.getType().accept(this);
			code.removeLast();
			n.env.setId(null);
		} else {
			lhsType = null;
		}

		if (type instanceof BoaTable) {
			code.add("");
			return;
		}

		final ST st = stg.getInstanceOf("Assignment");
		st.add("operator", "=");
		st.add("lhs", "___" + n.getId().getToken());
		lastVarDecl = "___" + n.getId().getToken();

		if (!n.hasInitializer()) {
			if (lhsType instanceof BoaProtoMap ||
					!(lhsType instanceof BoaMap || lhsType instanceof BoaStack || lhsType instanceof BoaQueue || lhsType instanceof BoaSet)) {
				st.add("rhs", n.type.defaultValue());
				code.add(st.render());
				return;
			}

			n.getType().accept(this);
			st.add("rhs", "new " + code.removeLast() + "()");
			code.add(st.render());
			return;
		}

		n.env.setOperandType(type);

		final BoaType t = n.getInitializer().type;

		n.getInitializer().accept(this);
		String src = code.removeLast();

		if (lhsType instanceof BoaTuple && t instanceof BoaArray) {
			final Operand op = n.getInitializer().getLhs().getLhs().getLhs().getLhs().getLhs().getOperand();
			if (op instanceof Composite) {
				final List<Expression> exps = ((Composite)op).getExprs();
				if (checkTupleArray(this.check(exps)) == false) {
					final ST stup = stg.getInstanceOf("Tuple");
					stup.add("name", lhsType.toJavaType());
					visit(exps);
					stup.add("exprlist", code.removeLast());
					src = stup.render();
				}
			}
		}

		if (!type.assigns(t)) {
			final BoaFunction f = n.env.getCast(t, type);

			if (f.hasName())
				src = f.getName() + "(" + src + ")";
			else if (f.hasMacro())
				src = expand(f.getMacro(), src.split(","));
		}

		st.add("rhs", src);

		code.add(st.render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VisitStatement n) {
		final ST st = stg.getInstanceOf("VisitClause");

		final boolean isBefore = n.isBefore();

		final List<String> body = new ArrayList<String>();

		if (n.hasWildcard()) {
			st.add("name", isBefore ? "defaultPreVisit" : "defaultPostVisit");
		} else if (n.hasComponent()) {
			final Component c = n.getComponent();
			final String id = c.getIdentifier().getToken();

			n.env.set(id, c.getType().type);
			st.add("arg", "___" + id);
			st.add("type", c.getType().type.toJavaType());

			st.add("name", isBefore ? "preVisit" : "postVisit");
		}

		st.add("ret", isBefore ? "boolean" : "void");

		if (n.getBody() instanceof Block) {
			for (final Node b : ((Block)n.getBody()).getStatements()) {
				b.accept(this);
				body.add(code.removeLast());
			}
		} else {
			n.getBody().accept(this);
			body.add(code.removeLast());
		}
		if (isBefore && !lastStatementIsStop(n.getBody()))
			body.add("return true;\n");
		st.add("body", body);

		code.add(st.render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FixPStatement n) {
		final ST st = stg.getInstanceOf("FixPClause");

		final List<String> body = new ArrayList<String>();
		String types = "";
		Component c = n.getParam1();

		n.env.set("___"+c.getIdentifier().getToken(), c.getType().type);
		types = c.getType().type.toJavaType();
		st.add("arg1", "___"+c.getIdentifier().getToken());
		c = n.getParam2();

		n.env.set("___"+c.getIdentifier().getToken(), c.getType().type);
		types = c.getType().type.toJavaType();
		st.add("arg2", "___"+c.getIdentifier().getToken());

		if (n.hasBody()) {
			if (n.getBody() instanceof Block) {

				for (final Node b : ((Block)n.getBody()).getStatements()) {
					b.accept(this);
					body.add(code.removeLast());
				}
			} else {
				n.getBody().accept(this);
				body.add(code.removeLast());
			}
		}

		st.add("body", body);
		st.add("type", types);

		code.add(st.render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final TraverseStatement n) {
		String traverseVar = lastVarDecl;

		final ST st = stg.getInstanceOf("TraverseClause");

		final BoaFunction funcType = ((BoaFunction) n.type);
		final List<String> body = new ArrayList<String>();
		String types = "";
		String traversalNodeIdentifier = "";
		Identifier traversalId = null;

		if (n.hasWildcard()) {
			st.add("name", "defaultPreTraverse");
		} else if (n.hasComponent()) {
			final Component c = n.getComponent();
			traversalNodeIdentifier = "___" + c.getIdentifier().getToken();
			traversalId = c.getIdentifier();
			n.env.set(traversalNodeIdentifier, c.getType().type);
			types = c.getType().type.toJavaType();

			st.add("name", "preTraverse");
		}

		if (!(funcType.getType() instanceof BoaAny))
			st.add("ret", funcType.getType().toBoxedJavaType());

		if (n.hasBody()) {
			if (n.getBody() instanceof Block) {
				for (final Node b : ((Block)n.getBody()).getStatements()) {
					b.accept(this);
					body.add(code.removeLast());
				}
			} else {
				n.getBody().accept(this);
				body.add(code.removeLast());
			}
		}

		final CFGBuildingVisitor cfgBuilder = new CFGBuildingVisitor();
		n.accept(cfgBuilder);
		new CreateNodeId().start(cfgBuilder);

		final HashSet<Identifier> aliastSet = new LocalMayAliasAnalysis().start(cfgBuilder, traversalId);

		final DataFlowSensitivityAnalysis dataFlowSensitivityAnalysis = new DataFlowSensitivityAnalysis();
		dataFlowSensitivityAnalysis.start(cfgBuilder, aliastSet);
		flowSensitive = dataFlowSensitivityAnalysis.isFlowSensitive();

		if (flowSensitive) {
			final LoopSensitivityAnalysis loopSensitivityAnalysis = new LoopSensitivityAnalysis();
			loopSensitivityAnalysis.start(cfgBuilder, aliastSet);
			loopSensitive = loopSensitivityAnalysis.isLoopSensitive();
		}

		traversalMap.put(traverseVar, flowSensitive);

		st.add("body", body);

		st.add("args", traversalNodeIdentifier);
		st.add("types", types);

		code.add(st.render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final WhileStatement n) {
		final ST st = stg.getInstanceOf("While");

		n.getCondition().accept(this);
		st.add("condition", code.removeLast());

		n.getBody().accept(this);
		st.add("stmt", code.removeLast());

		code.add(st.render());
	}

	//
	// expressions
	//
	/** {@inheritDoc} */
	@Override
	public void visit(final Expression n) {
		final ST st = stg.getInstanceOf("Expression");

		n.getLhs().accept(this);
		st.add("lhs", code.removeLast());

		if (n.getRhsSize() > 0) {
			final List<String> operators = new ArrayList<String>();
			final List<String> operands = new ArrayList<String>();

			for (final Conjunction c : n.getRhs()) {
				operators.add("||");
				c.accept(this);
				operands.add(code.removeLast());
			}

			st.add("operators", operators);
			st.add("operands", operands);
		}

		code.add(st.render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FunctionExpression n) {
		final ST st = stg.getInstanceOf("Function");

		final BoaFunction funcType = ((BoaFunction) n.getType().type);

		final BoaType[] params = funcType.getFormalParameters();
		final List<String> args = new ArrayList<String>();
		final List<String> types = new ArrayList<String>();

		for (final BoaType c : params) {
			if (!(c instanceof BoaName))
				continue;
			args.add(((BoaName)c).getId());
			types.add(((BoaName)c).getType().toJavaType());
		}

		this.varDecl.start(n);
		st.add("staticDeclarations", this.varDecl.getCode());

		st.add("type", funcType.toJavaType());
		if (funcType.getType() instanceof BoaAny)
			st.add("ret", "void");
		else
			st.add("ret", funcType.getType().toBoxedJavaType());
		st.add("args", args);
		st.add("types", types);

		n.getBody().accept(this);
		st.add("body", code.removeLast());

		code.add(st.render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ParenExpression n) {
		n.getExpression().accept(this);
		code.add("(" + code.removeLast() + ")");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SimpleExpr n) {
		final ST st = stg.getInstanceOf("Expression");

		// support '+' (concat) on arrays
		if (n.getLhs().type instanceof BoaArray) {
			n.getLhs().accept(this);
			String str = code.removeLast();

			if (n.getRhsSize() > 0) {
				str = "boa.functions.BoaIntrinsics.concat(" + str;
				for (final Term t : n.getRhs()) {
					t.accept(this);
					str = str + ", " + code.removeLast();
				}
				str = str + ")";
			}

			st.add("lhs", str);
		} else {
			n.getLhs().accept(this);
			st.add("lhs", code.removeLast());

			if (n.getRhsSize() > 0) {
				final List<String> operands = new ArrayList<String>();

				for (final Term t : n.getRhs()) {
					t.accept(this);
					operands.add(code.removeLast());
				}

				st.add("operators", n.getOps());
				st.add("operands", operands);
			}
		}

		code.add(st.render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VisitorExpression n) {
		final ST st = stg.getInstanceOf("Visitor");

		this.varDecl.start(n);
		st.add("staticDeclarations", this.varDecl.getCode());

		final List<String> body = new ArrayList<String>();
		for (final Node node : n.getBody().getStatements()) {
			node.accept(this);
			body.add(code.removeLast());
		}
		st.add("body", body);

		code.add(st.render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final TraversalExpression n) {
		final ST st = stg.getInstanceOf("Traversal");

		this.varDecl.start(n);
		st.add("staticDeclarations", this.varDecl.getCode());

		final List<String> body = new ArrayList<String>();
		for (final Node node : n.getBody().getStatements()) {
			if (node instanceof TraverseStatement) {
				if (!(((BoaFunction) node.type).getType() instanceof BoaAny)) {
					st.add("T", ((BoaFunction) node.type).getType().toBoxedJavaType());
				} else {
					st.add("T", "Object");
				}
			}
			node.accept(this);
			body.add(code.removeLast());
		}
		st.add("body", body);
		st.add("flowSensitive", flowSensitive);
		st.add("loopSensitive", loopSensitive);

		code.add(st.render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FixPExpression n) {
		final ST st = stg.getInstanceOf("FixP");

		this.varDecl.start(n);
		st.add("staticDeclarations", this.varDecl.getCode());

		final List<String> body = new ArrayList<String>();
		for (final Node node : n.getBody().getStatements()) {
			node.accept(this);
			body.add(code.removeLast());
		}
		st.add("body", body);

		code.add(st.render());
	}

	//
	// literals
	//
	/** {@inheritDoc} */
	@Override
	public void visit(final CharLiteral n) {
		code.add(n.getLiteral());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FloatLiteral n) {
		code.add(n.getLiteral() + "d");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final IntegerLiteral n) {
		code.add(n.getLiteral() + 'l');
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final StringLiteral n) {
		code.add(n.getLiteral());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final TimeLiteral n) {
		final String lit = n.getLiteral();

		// time lit is a string, convert to an int
		if (lit.startsWith("T")) {
			final String s = lit.substring(2, lit.length() - 1);

			// first try a standard format
			try {
				final DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss zzz yyyy");
				code.add(formatDate(df.parse(s)));
				return;
			} catch (final Exception e) { }

			// then try every possible combination of built in formats
			final int [] formats = new int[] {DateFormat.DEFAULT, DateFormat.FULL, DateFormat.SHORT, DateFormat.LONG, DateFormat.MEDIUM};
			for (final int f : formats)
				for (final int f2 : formats)
					try {
						final DateFormat df = DateFormat.getDateTimeInstance(f, f2);
						code.add(formatDate(df.parse(s)));
						return;
					} catch (final Exception e) { }

			throw new TypeCheckException(n, "Invalid time literal '" + s + "'");
		}

		code.add(lit.substring(0, lit.length() - 1));
	}

	private String formatDate(final Date date) {
		return (date.getTime() * 1000) + "L";
	}

	//
	// types
	//
	/** {@inheritDoc} */
	@Override
	public void visit(final TypeDecl n) {
		code.add("");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VisitorType n) {
		code.add("");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final TraversalType n) {
		code.add("");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ArrayType n) {
		final ST st = stg.getInstanceOf("ArrayType");

		// arrays dont need their component type boxed
		boolean boxing = n.env.getNeedsBoxing();
		n.env.setNeedsBoxing(false);
		n.getValue().accept(this);
		n.env.setNeedsBoxing(boxing);

		st.add("type", code.removeLast().replaceAll("<(.*)>", ""));

		code.add(st.render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FunctionType n) {
		final ST st = stg.getInstanceOf("FunctionType");

		final BoaFunction funcType = ((BoaFunction) n.type);

		final BoaType[] paramTypes = funcType.getFormalParameters();
		final List<String> args = new ArrayList<String>();
		final List<String> types = new ArrayList<String>();

		for (int i = 0; i < paramTypes.length; i++) {
			args.add(((BoaName) paramTypes[i]).getId());
			types.add(paramTypes[i].toJavaType());
		}

		st.add("name", funcType.toJavaType());
		if (funcType.getType() instanceof BoaAny)
			st.add("ret", "void");
		else
			st.add("ret", funcType.getType().toBoxedJavaType());
		st.add("args", args);
		st.add("types", types);

		code.add(st.render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FixPType n) {
		code.add("");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final MapType n) {
		final ST st = stg.getInstanceOf("MapType");

		n.env.setNeedsBoxing(true);

		n.getIndex().accept(this);
		st.add("key", code.removeLast());

		n.getValue().accept(this);
		st.add("value", code.removeLast());

		n.env.setNeedsBoxing(false);

		code.add(st.render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final OutputType n) {
		final String id = n.env.getId();

		final String aggregator = n.getId().getToken();

		final BoaTable t = (BoaTable) n.env.get(id);

		if (n.getArgsSize() > 0) {
			n.getArg(0).accept(this);
			this.aggregators.put(id, new AggregatorDescription(aggregator, t.getType(), Arrays.asList(code.removeLast())));
		} else {
			this.aggregators.put(id, new AggregatorDescription(aggregator, t.getType()));
		}

		code.add("");
	}
	
	/** {@inheritDoc} */
	@Override
	public void visit(final ModelType n) {
		visit(n.getType());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final StackType n) {
		final ST st = stg.getInstanceOf("StackType");

		n.env.setNeedsBoxing(true);

		n.getValue().accept(this);
		st.add("value", code.removeLast());

		n.env.setNeedsBoxing(false);

		code.add(st.render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final QueueType n) {
		final ST st = stg.getInstanceOf("QueueType");

		n.env.setNeedsBoxing(true);

		n.getValue().accept(this);
		st.add("value", code.removeLast());

		n.env.setNeedsBoxing(false);

		code.add(st.render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SetType n) {
		final ST st = stg.getInstanceOf("SetType");

		n.env.setNeedsBoxing(true);

		n.getValue().accept(this);
		st.add("value", code.removeLast());

		n.env.setNeedsBoxing(false);

		code.add(st.render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final TupleType n) {
		final ST st = stg.getInstanceOf("TupleType");

		if (!(n.type instanceof BoaTuple))
			throw new TypeCheckException(n ,"type " + n.type + " is not a tuple type");

		final BoaTuple tupType = ((BoaTuple) n.type);

		final List<Component> members = n.getMembers();
		final List<String> fields = new ArrayList<String>();
		final List<String> types = new ArrayList<String>();

		int fieldCount = 0;
		for (final Component c : members) {
			if(c.hasIdentifier()){
				fields.add(c.getIdentifier().getToken());
			} else {
				fields.add("id" + fieldCount++);
			}
			types.add(c.getType().type.toJavaType());
		}

		st.add("name", tupType.toJavaType());
		st.add("fields", fields);
		st.add("types", types);

		code.add(st.render());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final EnumType n) {
		final ST st = stg.getInstanceOf("EnumType");

		final BoaEnum enumType = ((BoaEnum) n.type);
		final BoaType fieldType = enumType.getType();
		final List<String> fields = new ArrayList<String>();
		final List<String> values = new ArrayList<String>();

		for (final EnumBodyDeclaration c : n.getMembers()) {
			final Factor f = c.getExp().getLhs().getLhs().getLhs().getLhs().getLhs();

			if (f.getOperand() instanceof ILiteral) {
				code.add(((ILiteral)(f.getOperand())).getLiteral());
				fields.add(c.getIdentifier().getToken());
				values.add(code.removeLast());
			}
		}

		st.add("ename", enumType.toJavaType());
		st.add("fields", fields);
		st.add("values", values);
		st.add("fname", fieldType.toJavaType());

		code.add(st.render());
	}

	protected static String expand(final String template, final String... parameters) {
		return expand(template, new ArrayList<Expression>(), parameters);
	}

	protected static String expand(final String template, final List<Expression> args, final String... parameters) {
		String replaced = template;

		// FIXME rdyer probably we should check the type, find the typevar, and then pull accordingly...
		if (replaced.contains("${K}") || replaced.contains("${V}")) {
			if (args.size() == 1 && args.get(0).type instanceof BoaMap) {
				final BoaMap m = (BoaMap)args.get(0).type;
				replaced = replaced.replace("${K}", nonScalarTypeTransform(m.getIndexType(), m.getIndexType().toBoxedJavaType()));
				replaced = replaced.replace("${V}", nonScalarTypeTransform(m.getType(), m.getType().toBoxedJavaType()));
			} else if (args.size() == 1 && args.get(0).type instanceof BoaStack) {
				final BoaStack s = (BoaStack)args.get(0).type;
				replaced = replaced.replace("${V}", nonScalarTypeTransform(s.getType(), s.getType().toBoxedJavaType()));
			} else if (args.size() == 1 && args.get(0).type instanceof BoaQueue) {
				final BoaQueue q = (BoaQueue)args.get(0).type;
				replaced = replaced.replace("${V}", nonScalarTypeTransform(q.getType(), q.getType().toBoxedJavaType()));
			} else if (args.size() == 1 && args.get(0).type instanceof BoaSet) {
				final BoaSet s = (BoaSet)args.get(0).type;
				replaced = replaced.replace("${V}", nonScalarTypeTransform(s.getType(), s.getType().toBoxedJavaType()));
			}
		}

		for (int i = 0; i < parameters.length; i++)
			replaced = replaced.replace("${" + i + "}", parameters[i]);

		return replaced;
	}

	private static String nonScalarTypeTransform(final BoaType type, String typeStr) {
		if (type instanceof BoaArray)
			return typeStr.replace("[]", "[0]");
		if (type instanceof BoaSet || type instanceof BoaStack || type instanceof BoaQueue || type instanceof BoaMap)
			return typeStr.replaceAll("<(.*)>", "");
		return typeStr;
	}

	protected static String camelCase(final String string) {
		final StringBuilder camelized = new StringBuilder();

		boolean lower = false;
		for (final char c : string.toCharArray())
			if (c == '_')
				lower = false;
			else if (Character.isDigit(c)) {
				camelized.append(c);
				lower = false;
			} else if (Character.isLetter(c)) {
				if (lower)
					camelized.append(c);
				else
					camelized.append(Character.toUpperCase(c));

				lower = true;
			}

		return camelized.toString();
	}

	protected boolean lastStatementIsStop(Statement s) {
		if (s instanceof StopStatement)
			return true;

		if (s instanceof IfStatement) {
			final IfStatement ifs = (IfStatement)s;
			if (ifs.hasElse())
				return lastStatementIsStop(ifs.getBody()) && lastStatementIsStop(ifs.getElse());
			return false;
		}

		if (s instanceof Block) {
			final List<Statement> stmts = ((Block)s).getStatements();
			if (stmts.size() > 0)
				return lastStatementIsStop(stmts.get(stmts.size() - 1));
		}

		return false;
	}

	protected List<BoaType> check(final Call c) {
		if (c.getArgsSize() > 0)
			return this.check(c.getArgs());

		return new ArrayList<BoaType>();
	}

	protected List<BoaType> check(final List<Expression> el) {
		final List<BoaType> types = new ArrayList<BoaType>();

		for (final Expression e : el) {
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
}
