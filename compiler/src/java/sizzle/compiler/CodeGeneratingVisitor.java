package sizzle.compiler;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

import sizzle.parser.syntaxtree.*;
import sizzle.types.*;

class TableDescription {
	private String aggregator;
	private SizzleType type;
	private List<String> parameters;

	public TableDescription(final String aggregator, final SizzleType type) {
		this(aggregator, type, null);
	}

	public TableDescription(final String aggregator, final SizzleType type, final List<String> parameters) {
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
	public SizzleType getType() {
		return this.type;
	}

	/**
	 * @param types
	 *            the types to set
	 */
	public void setTypes(final SizzleType type) {
		this.type = type;
	}
}

public class CodeGeneratingVisitor extends DefaultVisitorNoArgu<String> {
	public final TypeCheckingVisitor typechecker;
	private final NameFindingVisitor namefinder;
	private final IndexeeFindingVisitor indexeefinder;
	private final StaticDeclarationCodeGeneratingVisitor staticdeclarator;
	private final StaticInitializationCodeGeneratingVisitor staticinitializer;

	private final HashMap<String, TableDescription> tables;

	private final String name;
	public final StringTemplateGroup stg;

	private String skipIndex = "";
	private boolean abortGeneration = false;

	public CodeGeneratingVisitor(final TypeCheckingVisitor typechecker, final String name, final StringTemplateGroup stg) throws IOException {
		this.typechecker = typechecker;
		this.namefinder = new NameFindingVisitor();
		this.indexeefinder = new IndexeeFindingVisitor(this, namefinder);
		this.staticdeclarator = new StaticDeclarationCodeGeneratingVisitor(this);
		this.staticinitializer = new StaticInitializationCodeGeneratingVisitor(this);

		this.tables = new HashMap<String, TableDescription>();
		this.tables.put("stdout", new TableDescription("stdout", new SizzleString()));
		this.tables.put("stderr", new TableDescription("stderr", new SizzleString()));

		this.name = name;
		this.stg = stg;
	}

	public void setSkipIndex(final String skipIndex)
	{
		this.skipIndex = skipIndex;
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Program n) {
		final SymbolTable argu = this.typechecker.getSyms(n);
		final StringTemplate st = this.stg.getInstanceOf("Program");

		st.setAttribute("name", this.name);

		st.setAttribute("staticDeclarations", this.staticdeclarator.visit(n));
		st.setAttribute("staticStatements", this.staticinitializer.visit(n));

		final List<String> statements = new ArrayList<String>();
		for (final Node node : n.f0.nodes) {
			final String statement = node.accept(this);
			if (statement != null)
				statements.add(statement);
		}
		st.setAttribute("statements", statements);

		final List<String> tables = new ArrayList<String>();
		for (final Entry<String, TableDescription> entry : this.tables.entrySet()) {
			final String id = entry.getKey();
			final TableDescription description = entry.getValue();
			final String parameters = description.getParameters() == null ? "" : description.getParameters().get(0);
			final SizzleType type = description.getType();

			final StringBuilder src = new StringBuilder();
			for (final Class<?> c : argu.getAggregators(description.getAggregator(), type))
				src.append(", new " + c.getCanonicalName() + "(" + parameters + ")");

			tables.add("this.tables.put(\"" + id + "\", new sizzle.aggregators.Table(" + src.toString().substring(2) + "));");
		}

		st.setAttribute("tables", tables);

		return st.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Declaration n) {
		return n.f0.choice.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final StaticVarDecl n) {
		// this is handled by the static code generator
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final VarDecl n) {
		final SymbolTable argu = this.typechecker.getSyms(n);
		final SizzleType type = argu.get(n.f0.f0.tokenImage);

		final SizzleType lhsType;
		if (n.f2.present()) {
			argu.setId(n.f0.f0.tokenImage);
			lhsType = this.typechecker.getBinding(n.f2.node);
			n.f2.node.accept(this);
			argu.setId(null);
		} else {
			lhsType = null;
		}

		if (type instanceof SizzleTable)
			return null;

		final StringTemplate idSt = this.stg.getInstanceOf("Identifier");

		idSt.setAttribute("id", n.f0.f0.tokenImage);

		final StringTemplate st = this.stg.getInstanceOf("Assignment");
		st.setAttribute("lhs", idSt.toString());

		if (!n.f3.present()) {
			if (!(lhsType instanceof SizzleMap))
				return null;
			
			st.setAttribute("rhs", n.f2.node.accept(this));
			return st.toString();
		}

		final NodeChoice nodeChoice = (NodeChoice) n.f3.node;

		switch (nodeChoice.which) {
		case 0: // initializer
			Node elem = (Expression) ((NodeSequence) nodeChoice.choice).elementAt(1);
			argu.setOperandType(type);
			SizzleType t = this.typechecker.getBinding(elem);
			if (t instanceof SizzleFunction && !elem.accept(new IsFunctionVisitor(), this.typechecker.getSyms(elem)))
				t = ((SizzleFunction)t).getType();

			String src = ((NodeSequence) nodeChoice.choice).elementAt(1).accept(this);

			if (!type.assigns(t)) {
				final SizzleFunction f = argu.getCast(t, type);

				if (f.hasName()) {
					src = f.getName() + "(" + src + ")";
				} else if (f.hasMacro()) {
					src = CodeGeneratingVisitor.expand(f.getMacro(), src.split(","));
				}
			}

			st.setAttribute("rhs", src);
			break;
		default:
			throw new RuntimeException("unexpected choice " + nodeChoice.which + " is a " + nodeChoice.choice.getClass().getSimpleName().toString());
		}

		return st.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Type n) {
		return n.f0.choice.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Component n) {
		// intentionally ignoring the identifier
		return n.f1.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final ArrayType n) {
		final StringTemplate st = this.stg.getInstanceOf("ArrayType");

		st.setAttribute("type", n.f2.accept(this));

		return st.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final TupleType n) {
		return n.f0.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final SimpleTupleType n) {
		return "";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final MapType n) {
		final SymbolTable argu = this.typechecker.getSyms(n);
		final StringTemplate st = this.stg.getInstanceOf("MapType");

		argu.setNeedsBoxing(true);
		st.setAttribute("key", n.f2.accept(this));
		st.setAttribute("value", n.f5.accept(this));
		argu.setNeedsBoxing(false);

		return st.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final OutputType n) {
		final SymbolTable argu = this.typechecker.getSyms(n);
		final String id = argu.getId();

		final String aggregator = n.f1.f0.tokenImage;

		final SizzleTable t = (SizzleTable) argu.get(id);

		if (n.f2.present()) {
			final String parameter = ((NodeSequence) n.f2.node).nodes.get(1).accept(this);
			this.tables.put(id, new TableDescription(aggregator, t.getType(), Arrays.asList(parameter)));
		} else {
			this.tables.put(id, new TableDescription(aggregator, t.getType()));
		}

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final ExprList n) {
		final StringTemplate st = this.stg.getInstanceOf("ExprList");

		final List<String> expressions = new ArrayList<String>();

		expressions.add(n.f0.accept(this));

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				expressions.add(((NodeSequence) node).elementAt(1).accept(this));

		st.setAttribute("expressions", expressions);

		return st.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final FunctionType n) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Statement n) {
		return n.f0.choice.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Assignment n) {
		final StringTemplate st = this.stg.getInstanceOf("Assignment");

		final String lhs = n.f0.accept(this);
		final String rhs = n.f2.accept(this);

		// FIXME rdyer hack to fix assigning to maps
		if (lhs.contains(".get(")) {
			final String s = lhs.replaceFirst(Pattern.quote(".get("), ".put(");
			return s.substring(0, s.lastIndexOf(')')) + ", " + rhs + s.substring(s.lastIndexOf(')')) + ";";
		}

		st.setAttribute("lhs", lhs);
		st.setAttribute("rhs", rhs);

		return st.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Block n) {
		final StringTemplate st = this.stg.getInstanceOf("Block");

		final List<String> statements = new ArrayList<String>();

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				statements.add(node.accept(this));

		st.setAttribute("statements", statements);

		return st.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final BreakStatement n) {
		return this.stg.getInstanceOf("Break").toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final ContinueStatement n) {
		return this.stg.getInstanceOf("Continue").toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final DoStatement n) {
		final StringTemplate st = this.stg.getInstanceOf("DoWhile");

		st.setAttribute("condition", n.f4.accept(this));
		st.setAttribute("stmt", n.f2.accept(this));

		return st.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final EmitStatement n) {
		final StringTemplate st = this.stg.getInstanceOf("EmitStatement");

		if (n.f1.present()) {
			final List<String> indices = new ArrayList<String>();

			for (final Node node : n.f1.nodes)
				indices.add(((NodeSequence) node).elementAt(1).accept(this));

			st.setAttribute("indices", indices);
		}

		st.setAttribute("id", Character.toString('"') + n.f0.f0.tokenImage + '"');

		st.setAttribute("expression", n.f3.f0.accept(this));

		if (n.f4.present())
			st.setAttribute("weight", ((NodeSequence) n.f4.node).elementAt(1).accept(this));

		return st.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final ExprStatement n) {
		final StringTemplate st = this.stg.getInstanceOf("ExprStatement");

		st.setAttribute("expression", n.f0.accept(this));

		if (n.f1.present())
			st.setAttribute("operator", ((NodeToken)((NodeChoice) n.f1.node).choice).tokenImage);

		return st.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final ForStatement n) {
		final StringTemplate st = this.stg.getInstanceOf("ForStatement");

		st.setAttribute("declaration", n.f2.accept(this));
		st.setAttribute("expression", n.f4.accept(this));
		st.setAttribute("exprstmt", n.f6.accept(this));
		st.setAttribute("statement", n.f8.accept(this));

		return st.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final ForVarDecl n) {
		final VarDecl varDecl = new VarDecl(n.f0, n.f1, n.f2, n.f3, new NodeToken(";"));

		return varDecl.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final ForExprStatement n) {
		if (n.f1.present())
			return n.f0.accept(this) + ((NodeToken)((NodeChoice)n.f1.node).choice).tokenImage;
		return n.f0.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final IfStatement n) {
		final StringTemplate st = this.stg.getInstanceOf("IfStatement");

		st.setAttribute("expression", n.f2.f0.accept(this));
		st.setAttribute("statement", n.f4.f0.accept(this));

		if (n.f5.present())
			st.setAttribute("elseStatement", ((NodeSequence) n.f5.node).nodes.elementAt(1).accept(this));

		return st.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final ReturnStatement n) {
		final StringTemplate st = this.stg.getInstanceOf("Return");

		if (n.f1.present())
			st.setAttribute("expr", n.f1.node.accept(this));

		return st.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final WhenStatement n) {
		final SymbolTable argu = this.typechecker.getSyms(n);
		final StringTemplate st = this.stg.getInstanceOf("WhenStatement");

		switch (n.f0.f0.which) {
		case 0: // each
			break;
		case 1: // all
			st.setAttribute("all", "true");
			break;
		case 2: // some
			st.setAttribute("some", "true");
			break;
		default:
			throw new RuntimeException("unexpected choice " + n.f0.f0.which + " is " + n.f0.f0.choice.getClass());
		}

		final SizzleType type = this.typechecker.getBinding(n.f4);

		final String id = n.f2.f0.tokenImage;

		argu.set(id, type);
		st.setAttribute("type", type.toJavaType());
		st.setAttribute("index", id);

		final Set<String> indexees = this.indexeefinder.visit(n.f6, id);
	
		if (indexees.size() > 0) {
			final List<String> array = new ArrayList<String>(indexees);
			String src = "";
			for (int i = 0; i < array.size(); i++) {
				String indexee = array.get(i);
				// FIXME rdyer
//				SizzleType indexeeType = this.typechecker.visit(indexee.cloneNonLocals());
//				String func = indexeeType instanceof SizzleArray) ? ".length()" : ".size()";
				String func = ".size()";
				if (src.length() > 0)
					src = "java.lang.Math.min(" + indexee + func + ", " + src + ")";
				else
					src = indexee + func;
			}

			st.setAttribute("len", src);
		}

		st.setAttribute("expression", n.f6.accept(this));
		st.setAttribute("statement", n.f8.accept(this));

		return st.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Expression n) {
		final StringTemplate st = this.stg.getInstanceOf("Expression");

		st.setAttribute("lhs", n.f0.accept(this));

		if (n.f1.present()) {
			final List<String> operators = new ArrayList<String>();
			final List<String> operands = new ArrayList<String>();

			for (final Node node : n.f1.nodes) {
				operators.add("||");
				operands.add(((NodeSequence) node).elementAt(1).accept(this));
			}

			st.setAttribute("operators", operators);
			st.setAttribute("operands", operands);
		}

		return st.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Conjunction n) {
		final StringTemplate st = this.stg.getInstanceOf("Expression");

		st.setAttribute("lhs", n.f0.accept(this));

		if (n.f1.present()) {
			final List<String> operators = new ArrayList<String>();
			final List<String> operands = new ArrayList<String>();

			for (final Node node : n.f1.nodes) {
				operators.add("&&");
				operands.add(((NodeSequence) node).elementAt(1).accept(this));
			}

			st.setAttribute("operators", operators);
			st.setAttribute("operands", operands);
		}

		return st.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Comparison n) {
		final StringTemplate st = this.stg.getInstanceOf("Expression");

		st.setAttribute("lhs", n.f0.accept(this));

		if (n.f1.present()) {
			final List<String> operators = new ArrayList<String>();
			final List<String> operands = new ArrayList<String>();

			final Vector<Node> nodes = ((NodeSequence) n.f1.node).nodes;
			final NodeChoice nodeChoice = (NodeChoice) nodes.elementAt(0);
			switch (nodeChoice.which) {
			case 0:
			case 1:
				// special case string (in)equality
				// FIXME rdyer string != is doing == right now
				if (n.f0.accept(this.typechecker, this.typechecker.getSyms(n.f0)) instanceof SizzleString) {
					operators.add(".equals(" + nodes.elementAt(1).accept(this) + ")");
					operands.add("");
					break;
				}
				// fall through
			case 2:
			case 3:
			case 4:
			case 5:
				operators.add(" " + ((NodeToken)nodeChoice.choice).tokenImage + " ");
				operands.add(nodes.elementAt(1).accept(this));
				break;
			default:
				throw new RuntimeException("unexpected choice " + nodeChoice.which + " is " + nodeChoice.choice.getClass());
			}

			st.setAttribute("operators", operators);
			st.setAttribute("operands", operands);
		}

		return st.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final SimpleExpr n) {
		final StringTemplate st = this.stg.getInstanceOf("Expression");

		st.setAttribute("lhs", n.f0.accept(this));

		if (n.f1.present()) {
			final List<String> operators = new ArrayList<String>();
			final List<String> operands = new ArrayList<String>();

			for (final Node node : n.f1.nodes) {
				final NodeSequence nodeSequence = (NodeSequence) node;
				operators.add(((NodeToken) ((NodeChoice) nodeSequence.elementAt(0)).choice).tokenImage);
				operands.add(nodeSequence.elementAt(1).accept(this));
			}

			st.setAttribute("operators", operators);
			st.setAttribute("operands", operands);
		}

		return st.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Term n) {
		final StringTemplate st = this.stg.getInstanceOf("Expression");

		st.setAttribute("lhs", n.f0.accept(this));

		if (n.f1.present()) {
			final List<String> operators = new ArrayList<String>();
			final List<String> operands = new ArrayList<String>();

			for (final Node node : n.f1.nodes) {
				final NodeSequence nodeSequence = (NodeSequence) node;
				operators.add(((NodeToken) ((NodeChoice) nodeSequence.elementAt(0)).choice).tokenImage);
				operands.add(nodeSequence.elementAt(1).accept(this));
			}

			st.setAttribute("operators", operators);
			st.setAttribute("operands", operands);
		}

		return st.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Factor n) {
		if (n.f1.present()) {
			final SymbolTable argu = this.typechecker.getSyms(n);
			final NodeChoice nodeChoice = (NodeChoice) n.f1.nodes.get(0);

			switch (nodeChoice.which) {
			case 0: // selector
			case 1: // index
				argu.setOperand(n.f0);
				argu.setOperandType(this.typechecker.getBinding(n.f0));
				String accept = n.f0.accept(this);
				abortGeneration = false;
				for (int i = 0; !abortGeneration && i < n.f1.nodes.size(); i++)
					accept += n.f1.nodes.elementAt(i).accept(this);
				argu.setOperand(null);
				return accept;
			case 2: // call
				argu.setOperand(n.f0);
				accept = n.f1.nodes.elementAt(0).accept(this);
				argu.setOperand(null);
				return accept;
			default:
				throw new RuntimeException("unexpected choice " + nodeChoice.which + " is " + nodeChoice.choice.getClass());
			}
		} else {
			return n.f0.accept(this);
		}
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Selector n) {
		try {
			final SymbolTable argu = this.typechecker.getSyms(n);
			SizzleType opType = argu.getOperandType();
			if (opType instanceof SizzleName)
				opType = ((SizzleName) opType).getType();

			// operand is a proto map (aka enum)
			if (opType instanceof SizzleProtoMap)
				return "." + n.f1.f0.tokenImage;

			final String member = n.f1.f0.tokenImage;

			// operand is a proto tuple
			if (opType instanceof SizzleProtoTuple) {
				final SizzleType memberType = ((SizzleProtoTuple) opType).getMember(member);
				argu.setOperandType(memberType);
				if (memberType instanceof SizzleProtoList)
					return ".get" + camelCase(n.f1.f0.tokenImage) + "List()";
				return ".get" + camelCase(n.f1.f0.tokenImage) + "()";
			}

			// operand is a tuple
			if (opType instanceof SizzleTuple)
				return "[" + ((SizzleTuple) opType).getMemberIndex(member) + "]";

			throw new RuntimeException("unimplemented");
		} catch (final TypeException e) {
			throw new RuntimeException("unimplemented");
		}
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Index n) {
		if (this.namefinder.visit(n.f1).contains(this.skipIndex)) {
			abortGeneration = true;
			return "";
		}

		final SymbolTable argu = this.typechecker.getSyms(n);
		final StringTemplate st = this.stg.getInstanceOf("Index");

		final SizzleType t = argu.getOperandType();
		if (t instanceof SizzleMap) {
			argu.setOperandType(((SizzleMap) t).getType());
			st.setAttribute("map", true);
		} else if (t instanceof SizzleProtoList) {
			argu.setOperandType(((SizzleProtoList) t).getType());
			st.setAttribute("map", true);
		} else if (t instanceof SizzleArray) {
			argu.setOperandType(((SizzleArray) t).getType());
		}

		st.setAttribute("operand", "");

		SizzleType indexType = this.typechecker.getBinding(n.f1);
		if (indexType instanceof SizzleInt)
			st.setAttribute("index", "(int)" + n.f1.accept(this));
		else
			st.setAttribute("index", n.f1.accept(this));

		if (n.f2.present())
			st.setAttribute("slice", ((NodeSequence) n.f2.node).elementAt(1).accept(this));

		return st.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Call n) {
		final SymbolTable argu = this.typechecker.getSyms(n);
		final StringTemplate st = this.stg.getInstanceOf("Call");

		final SizzleFunction f = argu.getFunction(this.namefinder.visit(argu.getOperand()).toArray()[0].toString(), this.typechecker.check(n));

		if (f.hasMacro()) {
			final List<String> parts = new ArrayList<String>();
			final ExprList list = (ExprList) n.f1.node;
			parts.add(list.f0.accept(this));
			if (list.f1.present())
				for (final Node node : list.f1.nodes)
					parts.add(((Expression)((NodeSequence)node).elementAt(1)).accept(this));
			st.setAttribute("call", CodeGeneratingVisitor.expand(f.getMacro(), parts.toArray(new String[]{})));
		} else if (f.hasName()) {
			st.setAttribute("operand", f.getName());

			if (n.f1.present())
				st.setAttribute("parameters", ((ExprList) n.f1.node).accept(this));
		} else {
			st.setAttribute("operand", argu.getOperand().accept(this) + ".invoke");

			if (n.f1.present())
				st.setAttribute("parameters", "new Object[] {" + ((ExprList) n.f1.node).accept(this) + "}");
		}

		return st.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visitOperandFactor(final NodeToken op, final Factor n) {
		if (op.tokenImage.equals("not"))
			return "!" + n.accept(this);
		return op.tokenImage + n.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public String visitOperandParen(final Expression n) {
		return "(" + n.accept(this) + ")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Composite n) {
		final SymbolTable argu = this.typechecker.getSyms(n);
		final StringTemplate st = this.stg.getInstanceOf("Composite");

		if (n.f1.present()) {
			final NodeChoice nodeChoice = (NodeChoice) n.f1.node;
			switch (nodeChoice.which) {
			case 0: // pair list
				st.setAttribute("pairlist", nodeChoice.choice.accept(this));
				break;
			case 1: // expression list
				SizzleType t = this.typechecker.getBinding(((ExprList) nodeChoice.choice));

				if (argu.getOperandType() instanceof SizzleArray && t instanceof SizzleTuple)
					t = new SizzleArray(((SizzleTuple)t).getMember(0));

				st.setAttribute("type", t.toJavaType());
				st.setAttribute("exprlist", nodeChoice.choice.accept(this));
				break;
			default:
				throw new RuntimeException("unexpected choice " + nodeChoice.which + " is " + nodeChoice.choice.getClass());
			}
		}

		return st.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final PairList n) {
		final StringTemplate st = this.stg.getInstanceOf("PairList");

		final List<String> pairs = new ArrayList<String>();

		pairs.add(n.f0.accept(this));

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				pairs.add(((NodeSequence) node).elementAt(1).accept(this));

		st.setAttribute("pairs", pairs);

		return st.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Pair n) {
		final SymbolTable argu = this.typechecker.getSyms(n);
		final StringTemplate st = this.stg.getInstanceOf("Pair");

		st.setAttribute("map", argu.getId());
		st.setAttribute("key", n.f0.accept(this));
		st.setAttribute("value", n.f2.accept(this));

		return st.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Function n) {
		final StringTemplate st = this.stg.getInstanceOf("Function");

		final SizzleType t = this.typechecker.getBinding(n.f0);

		if (!(t instanceof SizzleFunction))
			throw new TypeException(n ,"type " + t + " is not a function type");
		final SizzleFunction funcType = ((SizzleFunction) t);

		SizzleType[] paramTypes = funcType.getFormalParameters();
		List<String> params = new ArrayList<String>();

		for (int i = 0; i < paramTypes.length; i++)
			params.add(paramTypes[i].toBoxedJavaType() + " ___" + ((SizzleName) paramTypes[i]).getId() + " = (" + paramTypes[i].toBoxedJavaType() + ")args[" + i + "];");

		if (!(funcType.getType() instanceof SizzleAny))
			st.setAttribute("ret", funcType.getType().toBoxedJavaType());
		st.setAttribute("staticDeclarations", this.staticdeclarator.visit(n.f1));
		st.setAttribute("parameters", params);
		st.setAttribute("body", n.f1.accept(this));

		return st.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Identifier n) {
		final SymbolTable argu = this.typechecker.getSyms(n);
		final String id = n.f0.tokenImage;

		if (argu.hasType(id)) {
			if (argu.getNeedsBoxing())
				return argu.getType(id).toBoxedJavaType();
			return argu.getType(id).toJavaType();
		}

		// otherwise return the identifier template
		final StringTemplate st = this.stg.getInstanceOf("Identifier");

		st.setAttribute("id", id);

		return st.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final IntegerLiteral n) {
		return n.f0.tokenImage + 'l';
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final FloatingPointLiteral n) {
		return n.f0.tokenImage + "d";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final CharLiteral n) {
		return n.f0.tokenImage;
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final StringLiteral n) {
		switch (n.f0.which) {
		case 0: // STRING
			return ((NodeToken) n.f0.choice).tokenImage;
		case 1: // REGEX
			String s = ((NodeToken) n.f0.choice).tokenImage;
			s = "\"" + s.substring(1, s.length() - 1).replace("\\", "\\\\") + "\"";
			return s;
		default:
			throw new RuntimeException("unimplemented");
		}
	}

	private static String expand(final String template, final String... parameters) {
		String replaced = template;

		for (int i = 0; i < parameters.length; i++)
			replaced = replaced.replace("${" + i + "}", parameters[i]);

		return replaced;
	}

	private static String camelCase(final String string) {
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

	/** {@inheritDoc} */
	@Override
	public String visit(final EmptyStatement n) {
		return this.stg.getInstanceOf("Empty").toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final StopStatement n) {
		return this.stg.getInstanceOf("Stop").toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final VisitorExpr n) {
		final StringTemplate st = this.stg.getInstanceOf("Visitor");

		st.setAttribute("staticDeclarations", this.staticdeclarator.visit(n.f1));
		final List<String> body = new ArrayList<String>();
		for (final Node node : n.f1.f1.nodes)
			body.add(node.accept(this));
		st.setAttribute("body", body);

		return st.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final VisitStatement n) {
		final SymbolTable argu = this.typechecker.getSyms(n);
		argu.setIsBeforeVisitor(n.f0.which == 0 && n.f1.which != 2);

		final boolean isBefore = n.f0.which == 0;
		String s = "";
		switch (n.f1.which) {
		case 0: // single type
			final String typeName = ((Identifier)((NodeSequence)n.f1.choice).nodes.get(2)).f0.tokenImage;
			final String ident = ((Identifier)((NodeSequence)n.f1.choice).nodes.get(0)).f0.tokenImage;
			final SizzleType type = argu.get(typeName);
			if (type != null)
				argu.set(ident, type);
			else
				throw new TypeException(n, "Invalid type '" + typeName + "'");

			s = "@Override\nprotected ";
			s += isBefore ? "boolean preVisit" : "void postVisit";
			s += "(final " + argu.get(typeName).toJavaType() + " ___" + ident + ") throws Exception {\n";
			if (n.f3.f0.which == 1)
				for (final Node b : ((Block)n.f3.f0.choice).f1.nodes)
					s += b.accept(this);
			else
				s += n.f3.accept(this);
			if (isBefore && !lastStatementIsStop(n.f3))
				s += "return true;\n";
			s += "}\n";
			break;
		case 1: // list of types
			final IdentifierList idlist = (IdentifierList)n.f1.choice;
			final List<String> ids = new ArrayList<String>();
			ids.add(idlist.f0.f0.tokenImage);
			if (idlist.f1.present())
				for (final Node ns : idlist.f1.nodes)
					ids.add(((Identifier)((NodeSequence)ns).nodes.get(1)).f0.tokenImage);
			for (final String t : ids) {
				s += "@Override\nprotected ";
				s += isBefore ? "boolean preVisit" : "void postVisit";
				s += "(final " + argu.get(t).toJavaType() + " __UNUSED) throws Exception {\n";
				if (n.f3.f0.which == 1)
					for (final Node b : ((Block)n.f3.f0.choice).f1.nodes)
						s += b.accept(this);
				else
					s += n.f3.accept(this);
				if (isBefore && !lastStatementIsStop(n.f3))
					s += "return true;\n";
				s += "}\n";
			}
			break;
		case 2: // wildcard
			s = "@Override\nprotected void ";
			s += isBefore ? "defaultPreVisit" : "defaultPostVisit";
			s += "() throws Exception {\n";
			if (n.f3.f0.which == 1)
				for (final Node b : ((Block)n.f3.f0.choice).f1.nodes)
					s += b.accept(this);
			else
				s += n.f3.accept(this);
			s += "}\n";
			break;
		default:
			throw new RuntimeException("unexpected choice " + n.f0.which + " is " + n.f0.choice.getClass());
		}
		return s;
	}

	private boolean lastStatementIsStop(Statement s) {
		if (s.f0.choice instanceof StopStatement)
			return true;

		if (s.f0.choice instanceof Block)
			for (final Node n : ((Block)s.f0.choice).f1.nodes)
				if (((NodeChoice)n).which == 1 && ((Statement)((NodeChoice)n).choice).f0.choice instanceof StopStatement)
					return true;

		return false;
	}
}
