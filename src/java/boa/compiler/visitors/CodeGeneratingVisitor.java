package boa.compiler.visitors;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.stringtemplate.StringTemplate;

import boa.aggregators.AggregatorSpec;
import boa.compiler.SymbolTable;
import boa.compiler.TypeCheckException;
import boa.compiler.ast.*;
import boa.compiler.ast.expressions.*;
import boa.compiler.ast.literals.*;
import boa.compiler.ast.statements.*;
import boa.compiler.ast.types.*;
import boa.types.*;

/***
 * 
 * @author anthonyu
 * @author rdyer
 */
public class CodeGeneratingVisitor extends AbstractCodeGeneratingVisitor {
	/**
	 * 
	 * @author anthonyu
	 */
	protected class TableDescription {
		protected String aggregator;
		protected BoaType type;
		protected List<String> parameters;

		public TableDescription(final String aggregator, final BoaType type) {
			this(aggregator, type, null);
		}

		public TableDescription(final String aggregator, final BoaType type, final List<String> parameters) {
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
		public void visit(final VarDeclStatement n) {
			if (n.type instanceof BoaTable)
				return;

			final StringTemplate st = stg.getInstanceOf("VarDecl");

			st.setAttribute("id", n.getId().getToken());
			st.setAttribute("type", n.type.toJavaType());

			if (n.isStatic())
				st.setAttribute("isstatic", true);

			code.add(st.toString());
		}
	}

	/***
	 * Finds the set of all function types and generates classes for each unique type.
	 * 
	 * @author rdyer
	 */
	protected class FunctionDeclaratorCodeGeneratingVisitor extends AbstractCodeGeneratingVisitor {
		protected final Set<String> funcs = new HashSet<String>();

		/** {@inheritDoc} */
		@Override
		public void visit(final FunctionType n) {
			super.visit(n);

			final String name = ((BoaFunction)n.type).toJavaType();
			if (funcs.contains(name))
				return;

			funcs.add(name);

			final StringTemplate st = stg.getInstanceOf("FunctionType");

			if (!(n.type instanceof BoaFunction))
				throw new TypeCheckException(n ,"type " + n.type + " is not a function type");

			final BoaFunction funcType = ((BoaFunction) n.type);

			final List<Component> params = n.getArgs();
			final List<String> args = new ArrayList<String>();
			final List<String> types = new ArrayList<String>();

			for (final Component c : params) {
				args.add(c.getIdentifier().getToken());
				types.add(c.getType().type.toJavaType());
			}

			st.setAttribute("name", funcType.toJavaType());
			if (funcType.getType() instanceof BoaAny)
				st.setAttribute("ret", "void");
			else
				st.setAttribute("ret", funcType.getType().toBoxedJavaType());
			st.setAttribute("args", args);
			st.setAttribute("types", types);

			code.add(st.toString());
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
		public void visit(final Composite n) {
			if (n.isEmpty())
				return;

			String s = "";

			for (final Pair p : n.getPairs()) {
				if (s.length() > 0)
					s += "\n";
				p.accept(this);
				s += code.pop();
			}

			code.add(s);
		}
	}

	/**
	 * 
	 * @author rdyer
	 */
	protected class IdentifierFindingVisitor extends AbstractVisitorNoArg {
		protected final Set<String> names = new HashSet<String>();

		public Set<String> getNames() {
			return names;
		}

		/** {@inheritDoc} */
		@Override
		protected void initialize() {
			names.clear();
		}

		/** {@inheritDoc} */
		@Override
		public void visit(final Identifier n) {
			names.add(n.getToken());
		}
	}

	/**
	 * 
	 * @author rdyer
	 */
	protected class IndexeeFindingVisitor extends AbstractVisitor<String> {
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


	protected final IdentifierFindingVisitor idFinder = new IdentifierFindingVisitor();
	protected final IndexeeFindingVisitor indexeeFinder = new IndexeeFindingVisitor();
	protected final CallFindingVisitor callFinder = new CallFindingVisitor();
	protected final VarDeclCodeGeneratingVisitor varDecl;
	protected final StaticInitializationCodeGeneratingVisitor staticInitialization;
	protected final FunctionDeclaratorCodeGeneratingVisitor functionDeclarator;

	protected final HashMap<String, TableDescription> tables = new HashMap<String, TableDescription>();

	protected boolean hasEmit = false;

	protected final String name;

	protected String skipIndex = "";
	protected boolean abortGeneration = false;

	final public static List<String> combineTableStrings = new ArrayList<String>();
	final public static List<String> reduceTableStrings = new ArrayList<String>();

	public CodeGeneratingVisitor(final String name) throws IOException {
		this.name = name;

		varDecl = new VarDeclCodeGeneratingVisitor();
		staticInitialization = new StaticInitializationCodeGeneratingVisitor();
		functionDeclarator = new FunctionDeclaratorCodeGeneratingVisitor();
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Program n) {
		final SymbolTable argu = n.env;
		final StringTemplate st = stg.getInstanceOf("Job");

		st.setAttribute("name", this.name);

		this.varDecl.start(n);
		this.functionDeclarator.start(n);
		if (this.functionDeclarator.hasCode())
			st.setAttribute("staticDeclarations", this.varDecl.getCode() + "\n" + this.functionDeclarator.getCode());
		else
			st.setAttribute("staticDeclarations", this.varDecl.getCode());

		this.staticInitialization.start(n);
		if (this.staticInitialization.hasCode())
			st.setAttribute("staticStatements", this.staticInitialization.getCode());

		final List<String> statements = new ArrayList<String>();
		for (final Statement s : n.getStatements()) {
			s.accept(this);
			final String statement = code.removeLast();
			if (!statement.isEmpty())
				statements.add(statement);
		}
		st.setAttribute("statements", statements);

		if (this.tables.size() == 0)
			throw new TypeCheckException(n, "No output variables were declared - must declare at least one output variable");

		if (!hasEmit)
			throw new TypeCheckException(n, "No emit statements detected - there will be no output generated");

		for (final Entry<String, TableDescription> entry : this.tables.entrySet()) {
			String id = entry.getKey();
			String prefix = name;

			if (id.matches("\\d+_.*")) {
				prefix = id.substring(0, id.indexOf('_'));
				id = id.substring(id.indexOf('_') + 1);
			}

			final TableDescription description = entry.getValue();
			final String parameters = description.getParameters() == null ? "" : description.getParameters().get(0);
			final BoaType type = description.getType();

			final StringBuilder src = new StringBuilder();
			boolean combines = false;
			for (final Class<?> c : argu.getAggregators(description.getAggregator(), type)) {
				src.append(", new " + c.getCanonicalName() + "(" + parameters + ")");
				try {
					final AggregatorSpec annotation = c.getAnnotation(AggregatorSpec.class);
					if (annotation.canCombine())
						combines = true;
				} catch (final RuntimeException e) {
					throw new TypeCheckException(n, e.getMessage(), e);
				}
			}

			if (combines)
				combineTableStrings.add("this.tables.put(\"" + prefix + "::" + id + "\", new boa.aggregators.Table(" + src.toString().substring(2) + "));");
			reduceTableStrings.add("this.tables.put(\"" + prefix + "::" + id + "\", new boa.aggregators.Table(" + src.toString().substring(2) + "));");
		}

		code.add(st.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Call n) {
		final SymbolTable argu = n.env;
		final StringTemplate st = stg.getInstanceOf("Call");

		this.idFinder.start(argu.getOperand());
		final String funcName = this.idFinder.getNames().toArray()[0].toString();
		final BoaFunction f = argu.getFunction(funcName, check(n));

		if (f.hasMacro()) {
			final List<String> parts = new ArrayList<String>();
			for (final Expression e : n.getArgs()) {
				e.accept(this);
				parts.add(code.removeLast());
			}

			final String s = expand(f.getMacro(), n.getArgs(), parts.toArray(new String[]{}));

			// FIXME rdyer a hack, so that "def(pbuf.attr)" generates "pbuf.hasAttr()"
			if (funcName.equals("def")) {
				final Matcher m = Pattern.compile("\\((\\w+).get(\\w+)\\(\\) != null\\)").matcher(s);
				if (m.matches() && !m.group(2).endsWith("List"))
					st.setAttribute("call", m.group(1) + ".has" + m.group(2) + "()");
				else
					st.setAttribute("call", s);
			} else {
				st.setAttribute("call", s);
			}
		} else {
			if (f.hasName()) {
				st.setAttribute("operand", f.getName());
			} else {
				argu.getOperand().accept(this);
				st.setAttribute("operand", code.removeLast() + ".invoke");
			}

			if (n.getArgsSize() > 0) {
				visit(n.getArgs());
				st.setAttribute("parameters", code.removeLast());
			}
		}

		code.add(st.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Comparison n) {
		final StringTemplate st = stg.getInstanceOf("Expression");

		if (n.hasRhs()) {
			final List<String> operators = new ArrayList<String>();
			final List<String> operands = new ArrayList<String>();

			if (n.getOp().equals("==") || n.getOp().equals("!=")) {
				// special case string (in)equality
				if (n.getLhs().type instanceof BoaString) {
					n.getRhs().accept(this);
					n.getLhs().accept(this);
					final String expr = code.removeLast() + ".equals(" + code.removeLast() + ")";

					if (n.getOp().equals("!="))
						st.setAttribute("lhs", "!" + expr);
					else
						st.setAttribute("lhs", expr);
				}
				// special case AST (in)equality
				else if (n.getLhs().type instanceof BoaProtoTuple) {
					n.getLhs().accept(this);
					st.setAttribute("lhs", code.removeLast() + ".hashCode()");

					n.getRhs().accept(this);
					operands.add(code.removeLast() + ".hashCode()");

					operators.add(n.getOp());
				} else {
					n.getLhs().accept(this);
					st.setAttribute("lhs", code.removeLast());

					n.getRhs().accept(this);
					operands.add(code.removeLast());

					operators.add(n.getOp());
				}
			} else {
				n.getLhs().accept(this);
				st.setAttribute("lhs", code.removeLast());

				n.getRhs().accept(this);
				operands.add(code.removeLast());

				operators.add(n.getOp());
			}

			st.setAttribute("operators", operators);
			st.setAttribute("operands", operands);
		} else {
			n.getLhs().accept(this);
			st.setAttribute("lhs", code.removeLast());
		}

		code.add(st.toString());
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
		final StringTemplate st = stg.getInstanceOf("Composite");

		if (n.getPairsSize() > 0) {
			visit(n.getPairs());
			st.setAttribute("pairlist", code.removeLast());
		}
		if (n.getExprsSize() > 0) {
			// FIXME rdyer
			BoaType t = n.type;
//			BoaType t = ((ExprList) nodeChoice.choice).type;

			if (n.env.hasOperandType() && n.env.getOperandType() instanceof BoaArray && t instanceof BoaTuple)
				t = new BoaArray(((BoaTuple)t).getMember(0));

			visit(n.getExprs());
			st.setAttribute("exprlist", code.removeLast());
			st.setAttribute("type", t.toJavaType());
		}

		code.add(st.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Conjunction n) {
		final StringTemplate st = stg.getInstanceOf("Expression");

		n.getLhs().accept(this);
		st.setAttribute("lhs", code.removeLast());

		if (n.getRhsSize() > 0) {
			final List<String> operators = new ArrayList<String>();
			final List<String> operands = new ArrayList<String>();

			for (final Comparison c : n.getRhs()) {
				operators.add("&&");
				c.accept(this);
				operands.add(code.removeLast());
			}

			st.setAttribute("operators", operators);
			st.setAttribute("operands", operands);
		}

		code.add(st.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Factor n) {
		if (n.getOpsSize() > 0) {
			n.env.setOperand(n.getOperand());

			final Node o = n.getOp(0);

			if (o instanceof Selector || o instanceof Index) {
				n.env.setOperandType(n.getOperand().type);
				n.getOperand().accept(this);
				String accept = code.removeLast();
				abortGeneration = false;
				for (int i = 0; !abortGeneration && i < n.getOpsSize(); i++) {
					n.getOp(i).accept(this);
					accept += code.removeLast();
				}
				n.env.getOperandType();
				code.add(accept);
			} else if (o instanceof Call) {
				o.accept(this);
				code.add(code.removeLast());
			}
		} else {
			n.getOperand().accept(this);
			code.add(code.removeLast());
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Identifier n) {
		final SymbolTable argu = n.env;
		final String id = n.getToken();

		if (argu.hasType(id)) {
			if (argu.getNeedsBoxing())
				code.add(SymbolTable.getType(id).toBoxedJavaType());
			else
				code.add(SymbolTable.getType(id).toJavaType());
			return;
		}

		// otherwise return the identifier template
		final StringTemplate st = stg.getInstanceOf("Identifier");

		st.setAttribute("id", id);

		code.add(st.toString());
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

		final SymbolTable argu = n.env;
		final StringTemplate st = stg.getInstanceOf("Index");

		final BoaType t = argu.getOperandType();
		if (t instanceof BoaMap) {
			argu.setOperandType(((BoaMap) t).getType());
			st.setAttribute("map", true);
		} else if (t instanceof BoaProtoList) {
			argu.setOperandType(((BoaProtoList) t).getType());
			st.setAttribute("map", true);
		} else if (t instanceof BoaArray) {
			argu.setOperandType(((BoaArray) t).getType());
		}

		st.setAttribute("operand", "");

		BoaType indexType = n.getStart().type;
		n.getStart().accept(this);
		if (indexType instanceof BoaInt)
			st.setAttribute("index", "(int)(" + code.removeLast() + ")");
		else
			st.setAttribute("index", code.removeLast());

		if (n.hasEnd()) {
			n.getEnd().accept(this);
			st.setAttribute("slice", code.removeLast());
		}

		code.add(st.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Pair n) {
		final StringTemplate st = stg.getInstanceOf("Pair");

		st.setAttribute("map", n.env.getId());

		n.getExpr1().accept(this);
		st.setAttribute("key", code.removeLast());

		n.getExpr2().accept(this);
		st.setAttribute("value", code.removeLast());

		code.add(st.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Selector n) {
		try {
			BoaType opType = n.env.getOperandType();
			if (opType instanceof BoaName)
				opType = ((BoaName) opType).getType();

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
				code.add("[" + tuple.getMemberIndex(member) + "]");
				return;
			}

			throw new RuntimeException("unimplemented");
		} catch (final TypeCheckException e) {
			throw new RuntimeException("unimplemented");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Term n) {
		final StringTemplate st = stg.getInstanceOf("Expression");

		n.getLhs().accept(this);
		st.setAttribute("lhs", code.removeLast());

		if (n.getRhsSize() > 0) {
			final List<String> operands = new ArrayList<String>();

			for (final Factor f : n.getRhs()) {
				f.accept(this);
				operands.add(code.removeLast());
			}

			st.setAttribute("operators", n.getOps());
			st.setAttribute("operands", operands);
		}

		code.add(st.toString());
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
		final StringTemplate st = stg.getInstanceOf("Assignment");

		n.getLhs().accept(this);
		final String lhs = code.removeLast();
		n.getRhs().accept(this);
		final String rhs = code.removeLast();

		// FIXME rdyer hack to fix assigning to maps
		if (lhs.contains(".get(")) {
			final String s = lhs.replaceFirst(Pattern.quote(".get("), ".put(");
			code.add(s.substring(0, s.lastIndexOf(')')) + ", " + rhs + s.substring(s.lastIndexOf(')')) + ";");
			return;
		}

		st.setAttribute("lhs", lhs);
		st.setAttribute("rhs", rhs);

		code.add(st.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Block n) {
		final StringTemplate st = stg.getInstanceOf("Block");

		final List<String> statements = new ArrayList<String>();

		for (final Node node : n.getStatements()) {
			node.accept(this);
			final String statement = code.removeLast();
			if (!statement.isEmpty())
				statements.add(statement);
		}

		st.setAttribute("statements", statements);

		code.add(st.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final BreakStatement n) {
		code.add(stg.getInstanceOf("Break").toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ContinueStatement n) {
		code.add(stg.getInstanceOf("Continue").toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final DoStatement n) {
		final StringTemplate st = stg.getInstanceOf("DoWhile");

		n.getCondition().accept(this);
		st.setAttribute("condition", code.removeLast());

		n.getBody().accept(this);
		st.setAttribute("stmt", code.removeLast());

		code.add(st.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final EmitStatement n) {
		final StringTemplate st = stg.getInstanceOf("EmitStatement");

		hasEmit = true;

		if (n.getIndicesSize() > 0) {
			final List<String> indices = new ArrayList<String>();

			for (final Expression e : n.getIndices()) {
				e.accept(this);
				indices.add(code.removeLast());
			}

			st.setAttribute("indices", indices);
		}

		String id = n.getId().getToken();
		String prefix = name;

		if (id.matches("\\d+_.*")) {
			prefix = id.substring(0, id.indexOf('_'));
			id = id.substring(id.indexOf('_') + 1);
		}

		st.setAttribute("id", "\"" + id + "\"");
		st.setAttribute("job", prefix);

		n.getValue().accept(this);
		st.setAttribute("expression", code.removeLast());

		if (n.hasWeight()) {
			n.getWeight().accept(this);
			st.setAttribute("weight", code.removeLast());
		}

		code.add(st.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ExprStatement n) {
		final StringTemplate st = stg.getInstanceOf("ExprStatement");

		n.getExpr().accept(this);
		st.setAttribute("expression", code.removeLast());

		code.add(st.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ExistsStatement n) {
		final StringTemplate st = stg.getInstanceOf("WhenStatement");
		st.setAttribute("some", "true");
		generateQuantifier(n, n.getVar(), n.getCondition(), n.getBody(), "exists", st);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ForeachStatement n) {
		final StringTemplate st = stg.getInstanceOf("WhenStatement");
		generateQuantifier(n, n.getVar(), n.getCondition(), n.getBody(), "foreach", st);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final IfAllStatement n) {
		final StringTemplate st = stg.getInstanceOf("WhenStatement");
		st.setAttribute("all", "true");
		generateQuantifier(n, n.getVar(), n.getCondition(), n.getBody(), "ifall", st);
	}

	protected void generateQuantifier(final Node n, final Component c, final Expression e, final Block b, final String kind, final StringTemplate st) {
		final BoaType type = c.getType().type;

		final String id = c.getIdentifier().getToken();

		n.env.set(id, type);
		st.setAttribute("type", type.toJavaType());
		st.setAttribute("index", id);

		this.indexeeFinder.start(e, id);
		final Set<Node> indexees = this.indexeeFinder.getIndexees();
	
		if (indexees.size() > 0) {
			final List<Node> array = new ArrayList<Node>(indexees);
			String src = "";
			for (int i = 0; i < array.size(); i++) {
				final Factor indexee = (Factor)array.get(i);

				this.skipIndex = id;
				indexee.accept(this);
				final String src2 = code.removeLast();
				this.skipIndex = "";

				final BoaType indexeeType = this.indexeeFinder.getFactors().get(indexee).type;
				final String func = (indexeeType instanceof BoaArray) ? ".length" : ".size()";

				if (src.length() > 0)
					src = "java.lang.Math.min(" + src2 + func + ", " + src + ")";
				else
					src = src2 + func;
			}

			st.setAttribute("len", src);
		} else {
			throw new TypeCheckException(e, "quantifier variable '" + id + "' must be used in the " + kind + " condition expression");
		}

		e.accept(this);
		st.setAttribute("expression", code.removeLast());

		b.accept(this);
		st.setAttribute("statement", code.removeLast());

		code.add(st.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ForStatement n) {
		final StringTemplate st = stg.getInstanceOf("ForStatement");

		if (n.hasInit()) {
			n.getInit().accept(this);
			// FIXME rdyer this is a bit of a hack, to remove the newline
			final String s = code.removeLast();
			st.setAttribute("declaration", s.substring(0, s.length() - 1));
		} else
			st.setAttribute("declaration", ";");

		if (n.hasCondition()) {
			n.getCondition().accept(this);
			st.setAttribute("expression", code.removeLast());
		} else
			st.setAttribute("declaration", ";");

		if (n.hasUpdate()) {
			n.getUpdate().accept(this);
			// FIXME rdyer this is a bit of a hack, to remove the semicolon+newline
			final String s = code.removeLast();
			st.setAttribute("exprstmt", s.substring(0, s.length() - 2));
		}

		n.getBody().accept(this);
		st.setAttribute("statement", code.removeLast());

		code.add(st.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final IfStatement n) {
		final StringTemplate st = stg.getInstanceOf("IfStatement");

		n.getCondition().accept(this);
		st.setAttribute("expression", code.removeLast());

		n.getBody().accept(this);
		st.setAttribute("statement", code.removeLast());

		if (n.hasElse()) {
			n.getElse().accept(this);
			st.setAttribute("elseStatement", code.removeLast());
		}

		code.add(st.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final PostfixStatement n) {
		final StringTemplate st = stg.getInstanceOf("ExprStatement");

		n.getExpr().accept(this);
		st.setAttribute("expression", code.removeLast());
		st.setAttribute("operator", n.getOp());

		code.add(st.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ResultStatement n) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ReturnStatement n) {
		final StringTemplate st = stg.getInstanceOf("Return");

		if (n.hasExpr()) {
			n.getExpr().accept(this);
			st.setAttribute("expr", code.removeLast());
		}

		code.add(st.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final StopStatement n) {
		code.add(stg.getInstanceOf("Stop").toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SwitchCase n) {
		final StringTemplate caseSt = stg.getInstanceOf("SwitchCase");

		final List<String> cases = new ArrayList<String>();
		for (final Expression expr : n.getCases()) {
			expr.accept(this);
			String s = code.removeLast();
			if (expr.type instanceof BoaProtoMap)
				s = s.substring(s.lastIndexOf(".") + 1);
			cases.add(s);
		}

		caseSt.setAttribute("cases", cases);
		n.getBody().accept(this);
		caseSt.setAttribute("body", code.removeLast());
		code.add(caseSt.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SwitchStatement n) {
		final StringTemplate st = stg.getInstanceOf("Switch");

		final List<String> caseStmts = new ArrayList<String>();

		for (final SwitchCase sc : n.getCases()) {
			sc.accept(this);
			caseStmts.add(code.removeLast());
		}

		final List<String> defBody = new ArrayList<String>();
		n.getDefault().accept(this);
		defBody.add(code.removeLast());

		n.getCondition().accept(this);
		st.setAttribute("expr", code.removeLast());
		st.setAttribute("cases", caseStmts);
		st.setAttribute("body", defBody);

		code.add(st.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VarDeclStatement n) {
		if (n.isStatic()) {
			code.add("");
			return;
		}

		final SymbolTable argu = n.env;
		final BoaType type = argu.get(n.getId().getToken());

		final BoaType lhsType;
		if (n.hasType()) {
			argu.setId(n.getId().getToken());
			lhsType = n.getType().type;
			n.getType().accept(this);
			code.removeLast();
			argu.setId(null);
		} else {
			lhsType = null;
		}

		if (type instanceof BoaTable) {
			code.add("");
			return;
		}

		final StringTemplate st = stg.getInstanceOf("Assignment");
		st.setAttribute("lhs", "___" + n.getId().getToken());

		if (!n.hasInitializer()) {
			if (lhsType instanceof BoaProtoMap ||
					!(lhsType instanceof BoaMap || lhsType instanceof BoaStack || lhsType instanceof BoaSet)) {
				code.add("");
				return;
			}

			// FIXME rdyer if the type is a type identifier, n.getType() returns Identifier
			// and maps/stacks/sets wind up not having the proper constructors here
			n.getType().accept(this);
			st.setAttribute("rhs", code.removeLast());
			code.add(st.toString());
			return;
		}

		argu.setOperandType(type);
		BoaType t = n.getInitializer().type;

		if (t instanceof BoaFunction) {
			final IsFunctionVisitor isFuncV = new IsFunctionVisitor();
			isFuncV.start(n.getInitializer());
			if (!isFuncV.isFunction())
				t = ((BoaFunction)t).getType();
		}

		n.getInitializer().accept(this);
		String src = code.removeLast();

		if (!type.assigns(t)) {
			final BoaFunction f = argu.getCast(t, type);

			if (f.hasName())
				src = f.getName() + "(" + src + ")";
			else if (f.hasMacro())
				src = expand(f.getMacro(), src.split(","));
		}

		st.setAttribute("rhs", src);

		code.add(st.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VisitStatement n) {
		final StringTemplate st = stg.getInstanceOf("VisitClause");
		final SymbolTable argu = n.env;

		final boolean isBefore = n.isBefore();

		final List<String> body = new ArrayList<String>();
		final List<String> types = new ArrayList<String>();
		final List<String> ids = new ArrayList<String>();

		if (n.hasWildcard()) {
			st.setAttribute("name", isBefore ? "defaultPreVisit" : "defaultPostVisit");
		} else if (n.hasComponent()) {
			final Component c = n.getComponent();
			final String id = c.getIdentifier().getToken();

			argu.set(id, c.getType().type);
			types.add(c.getType().type.toJavaType());
			ids.add("___" + id);

			st.setAttribute("name", isBefore ? "preVisit" : "postVisit");
		} else {
			for (final Identifier id : n.getIdList()) {
				types.add(argu.get(id.getToken()).toJavaType());
				ids.add("__UNUSED");
			}

			st.setAttribute("name", isBefore ? "preVisit" : "postVisit");
		}

		st.setAttribute("ret", isBefore ? "boolean" : "void");

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
		st.setAttribute("body", body);

		if (ids.size() > 0) {
			st.setAttribute("args", ids);
			st.setAttribute("types", types);
		}

		code.add(st.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final WhileStatement n) {
		final StringTemplate st = stg.getInstanceOf("While");

		n.getCondition().accept(this);
		st.setAttribute("condition", code.removeLast());

		n.getBody().accept(this);
		st.setAttribute("stmt", code.removeLast());

		code.add(st.toString());
	}

	//
	// expressions
	//
	/** {@inheritDoc} */
	@Override
	public void visit(final Expression n) {
		final StringTemplate st = stg.getInstanceOf("Expression");

		n.getLhs().accept(this);
		st.setAttribute("lhs", code.removeLast());

		if (n.getRhsSize() > 0) {
			final List<String> operators = new ArrayList<String>();
			final List<String> operands = new ArrayList<String>();

			for (final Conjunction c : n.getRhs()) {
				operators.add("||");
				c.accept(this);
				operands.add(code.removeLast());
			}

			st.setAttribute("operators", operators);
			st.setAttribute("operands", operands);
		}

		code.add(st.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FunctionExpression n) {
		final StringTemplate st = stg.getInstanceOf("Function");

		if (!(n.getType().type instanceof BoaFunction))
			throw new TypeCheckException(n ,"type " + n.getType().type + " is not a function type");

		final BoaFunction funcType = ((BoaFunction) n.getType().type);

		final List<Component> params = n.getType().getArgs();
		final List<String> args = new ArrayList<String>();
		final List<String> types = new ArrayList<String>();

		for (final Component c : params) {
			args.add(c.getIdentifier().getToken());
			types.add(c.getType().type.toJavaType());
		}

		this.varDecl.start(n);
		st.setAttribute("staticDeclarations", this.varDecl.getCode());

		st.setAttribute("type", funcType.toJavaType());
		if (funcType.getType() instanceof BoaAny)
			st.setAttribute("ret", "void");
		else
			st.setAttribute("ret", funcType.getType().toBoxedJavaType());
		st.setAttribute("args", args);
		st.setAttribute("types", types);

		n.getBody().accept(this);
		st.setAttribute("body", code.removeLast());

		code.add(st.toString());
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
		final StringTemplate st = stg.getInstanceOf("Expression");

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

			st.setAttribute("lhs", str);
		} else {
			n.getLhs().accept(this);
			st.setAttribute("lhs", code.removeLast());
	
			if (n.getRhsSize() > 0) {
				final List<String> operands = new ArrayList<String>();
	
				for (final Term t : n.getRhs()) {
					t.accept(this);
					operands.add(code.removeLast());
				}
	
				st.setAttribute("operators", n.getOps());
				st.setAttribute("operands", operands);
			}
		}

		code.add(st.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final StatementExpr n) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VisitorExpression n) {
		final StringTemplate st = stg.getInstanceOf("Visitor");

		this.varDecl.start(n);
		st.setAttribute("staticDeclarations", this.varDecl.getCode());

		final List<String> body = new ArrayList<String>();
		for (final Node node : n.getBody().getStatements()) {
			node.accept(this);
			body.add(code.removeLast());
		}
		st.setAttribute("body", body);

		code.add(st.toString());
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
			} catch (Exception e) { }

			// then try every possible combination of built in formats
			final int [] formats = new int[] {DateFormat.DEFAULT, DateFormat.FULL, DateFormat.SHORT, DateFormat.LONG, DateFormat.MEDIUM};
			for (final int f : formats)
				for (final int f2 : formats)
					try {
						final DateFormat df = DateFormat.getDateTimeInstance(f, f2);
						code.add(formatDate(df.parse(s)));
						return;
					} catch (Exception e) { }

			throw new TypeCheckException(n, "Invalid time literal '" + s + "'");
		}

		code.add(lit.substring(0, lit.length() - 1));
	}
	private String formatDate(final Date date) {
		return "" + (date.getTime() * 1000) + "L";
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
	public void visit(final ArrayType n) {
		final StringTemplate st = stg.getInstanceOf("ArrayType");

		n.getValue().accept(this);
		st.setAttribute("type", code.removeLast());

		code.add(st.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FunctionType n) {
		final StringTemplate st = stg.getInstanceOf("FunctionType");

		if (!(n.type instanceof BoaFunction))
			throw new TypeCheckException(n ,"type " + n.type + " is not a function type");

		final BoaFunction funcType = ((BoaFunction) n.type);

		final BoaType[] paramTypes = funcType.getFormalParameters();
		final List<String> args = new ArrayList<String>();
		final List<String> types = new ArrayList<String>();

		for (int i = 0; i < paramTypes.length; i++) {
			args.add(((BoaName) paramTypes[i]).getId());
			types.add(paramTypes[i].toJavaType());
		}

		st.setAttribute("name", funcType.toJavaType());
		if (funcType.getType() instanceof BoaAny)
			st.setAttribute("ret", "void");
		else
			st.setAttribute("ret", funcType.getType().toBoxedJavaType());
		st.setAttribute("args", args);
		st.setAttribute("types", types);

		code.add(st.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final MapType n) {
		final StringTemplate st = stg.getInstanceOf("MapType");

		n.env.setNeedsBoxing(true);

		n.getIndex().accept(this);
		st.setAttribute("key", code.removeLast());

		n.getValue().accept(this);
		st.setAttribute("value", code.removeLast());

		n.env.setNeedsBoxing(false);

		code.add(st.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final OutputType n) {
		final String id = n.env.getId();

		final String aggregator = n.getId().getToken();

		final BoaTable t = (BoaTable) n.env.get(id);

		if (n.getArgsSize() > 0) {
			n.getArg(0).accept(this);
			this.tables.put(id, new TableDescription(aggregator, t.getType(), Arrays.asList(code.removeLast())));
		} else {
			this.tables.put(id, new TableDescription(aggregator, t.getType()));
		}

		code.add("");
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final StackType n) {
		final StringTemplate st = stg.getInstanceOf("StackType");

		n.env.setNeedsBoxing(true);

		n.getValue().accept(this);
		st.setAttribute("value", code.removeLast());

		n.env.setNeedsBoxing(false);

		code.add(st.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SetType n) {
		final StringTemplate st = stg.getInstanceOf("SetType");

		n.env.setNeedsBoxing(true);

		n.getValue().accept(this);
		st.setAttribute("value", code.removeLast());

		n.env.setNeedsBoxing(false);

		code.add(st.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final TupleType n) {
//		return n.f0.accept(this);
		throw new RuntimeException("unimplemented");
	}

	protected static String expand(final String template, final String... parameters) {
		return expand(template, new ArrayList<Expression>(), parameters);
	}

	protected static String expand(final String template, final List<Expression> args, final String... parameters) {
		String replaced = template;

		if (args.size() == 1 && args.get(0).type instanceof BoaMap) {
			BoaMap m = (BoaMap)args.get(0).type;
			replaced = replaced.replace("${K}", m.getIndexType().toBoxedJavaType());
			replaced = replaced.replace("${V}", m.getType().toBoxedJavaType());
		}

		for (int i = 0; i < parameters.length; i++)
			replaced = replaced.replace("${" + i + "}", parameters[i]);

		return replaced;
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

		if (s instanceof Block)
			for (final Node n : ((Block)s).getStatements())
				if (n instanceof StopStatement)
					return true;

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
}
