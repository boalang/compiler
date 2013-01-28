package sizzle.compiler;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

import sizzle.parser.syntaxtree.*;
import sizzle.parser.visitor.GJDepthFirst;
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

public class CodeGeneratingVisitor extends GJDepthFirst<String, SymbolTable> {
	private final TypeCheckingVisitor typechecker;
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

	@Override
	public String visit(final Start n, final SymbolTable argu) {
		return n.f0.accept(this, argu);
	}

	@Override
	public String visit(final Program n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("Program");

		st.setAttribute("name", this.name);

		st.setAttribute("staticDeclarations", this.staticdeclarator.visit(n, argu));
		st.setAttribute("staticStatements", this.staticinitializer.visit(n, argu));

		final List<String> statements = new ArrayList<String>();
		for (final Node node : n.f0.nodes) {
			final String statement = node.accept(this, argu);
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

	@Override
	public String visit(final Declaration n, final SymbolTable argu) {
		return n.f0.choice.accept(this, argu);
	}

	@Override
	public String visit(final TypeDecl n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final StaticVarDecl n, final SymbolTable argu) {
		// this is handled by the static code generator
		return null;
	}

	@Override
	public String visit(final VarDecl n, final SymbolTable argu) {
		final SizzleType type = argu.get(n.f0.f0.tokenImage);

		if (n.f2.present()) {
			argu.setId(n.f0.f0.tokenImage);
			n.f2.node.accept(this, argu);
			argu.setId(null);
		}

		if (type instanceof SizzleTable)
			return null;

		final StringTemplate idSt = this.stg.getInstanceOf("Identifier");

		idSt.setAttribute("id", n.f0.f0.tokenImage);

		final StringTemplate st = this.stg.getInstanceOf("Assignment");
		st.setAttribute("lhs", idSt.toString());

		if (!n.f3.present())
			return null;

		final NodeChoice nodeChoice = (NodeChoice) n.f3.node;

		switch (nodeChoice.which) {
		case 0: // initializer
			SizzleType t;
			Node elem = (Expression) ((NodeSequence) nodeChoice.choice).elementAt(1);
			try {
				argu.setOperandType(type);
				t = elem.accept(this.typechecker, argu.cloneNonLocals());
			} catch (final IOException e) {
				throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
			}
			if (t instanceof SizzleFunction && !elem.accept(new IsFunctionVisitor(), argu))
				t = ((SizzleFunction)t).getType();

			String src = ((NodeSequence) nodeChoice.choice).elementAt(1).accept(this, argu);

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

	@Override
	public String visit(final Type n, final SymbolTable argu) {
		return n.f0.choice.accept(this, argu);
	}

	@Override
	public String visit(final Component n, final SymbolTable argu) {
		// intentionally ignoring the identifier
		return n.f1.accept(this, argu);
	}

	@Override
	public String visit(final ArrayType n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("ArrayType");

		st.setAttribute("type", n.f2.accept(this, argu));

		return st.toString();
	}

	@Override
	public String visit(final TupleType n, final SymbolTable argu) {
		return n.f0.accept(this, argu);
	}

	@Override
	public String visit(final SimpleTupleType n, final SymbolTable argu) {
		return "";
	}

	@Override
	public String visit(final SimpleMemberList n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final SimpleMember n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final ProtoTupleType n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final ProtoMemberList n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final ProtoMember n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final ProtoFieldDecl n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final MapType n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("MapType");

		argu.setNeedsBoxing(true);
		st.setAttribute("key", n.f2.accept(this, argu));
		st.setAttribute("value", n.f5.accept(this, argu));
		argu.setNeedsBoxing(false);

		return st.toString();
	}

	@Override
	public String visit(final OutputType n, final SymbolTable argu) {
		final String id = argu.getId();

		final String aggregator = n.f1.f0.tokenImage;

		final SizzleTable t = (SizzleTable) argu.get(id);

		if (n.f2.present()) {
			final String parameter = ((NodeSequence) n.f2.node).nodes.get(1).accept(this, argu);
			this.tables.put(id, new TableDescription(aggregator, t.getType(), Arrays.asList(parameter)));
		} else {
			this.tables.put(id, new TableDescription(aggregator, t.getType()));
		}

		return null;
	}

	@Override
	public String visit(final ExprList n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("ExprList");

		final List<String> expressions = new ArrayList<String>();

		expressions.add(n.f0.accept(this, argu));

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				expressions.add(((NodeSequence) node).elementAt(1).accept(this, argu));

		st.setAttribute("expressions", expressions);

		return st.toString();
	}

	@Override
	public String visit(final FunctionType n, final SymbolTable argu) {
		return null;
	}

	@Override
	public String visit(final Statement n, final SymbolTable argu) {
		return n.f0.choice.accept(this, argu);
	}

	@Override
	public String visit(final Assignment n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("Assignment");

		final String lhs = n.f0.accept(this, argu);
		final String rhs = n.f2.accept(this, argu);

		// FIXME rdyer hack to fix assigning to maps
		if (lhs.contains(".get(")) {
			final String s = lhs.replaceFirst(Pattern.quote(".get("), ".put(");
			return s.substring(0, s.lastIndexOf(')')) + ", " + rhs + s.substring(s.lastIndexOf(')')) + ";";
		}

		st.setAttribute("lhs", lhs);
		st.setAttribute("rhs", rhs);

		return st.toString();
	}

	@Override
	public String visit(final Block n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("Block");

		SymbolTable symtab;
		try {
			symtab = argu.cloneNonLocals();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}
		
		final List<String> statements = new ArrayList<String>();

		if (n.f1.present())
			for (final Node node : n.f1.nodes) {
				node.accept(typechecker, symtab);
				statements.add(node.accept(this, symtab));
			}

		st.setAttribute("statements", statements);

		return st.toString();
	}

	@Override
	public String visit(final BreakStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final ContinueStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final DoStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final EmitStatement n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("EmitStatement");

		if (n.f1.present()) {
			final List<String> indices = new ArrayList<String>();

			for (final Node node : n.f1.nodes)
				indices.add(((NodeSequence) node).elementAt(1).accept(this, argu));

			st.setAttribute("indices", indices);
		}

		st.setAttribute("id", Character.toString('"') + n.f0.f0.tokenImage + '"');

		st.setAttribute("expression", n.f3.f0.accept(this, argu));

		if (n.f4.present())
			st.setAttribute("weight", ((NodeSequence) n.f4.node).elementAt(1).accept(this, argu));

		return st.toString();
	}

	@Override
	public String visit(final ExprStatement n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("ExprStatement");

		st.setAttribute("expression", n.f0.accept(this, argu));

		if (n.f1.present()) {
			final NodeChoice nodeChoice = (NodeChoice) n.f1.node;
			switch (nodeChoice.which) {
			case 0:
				st.setAttribute("operator", "++");
				break;
			case 1:
				st.setAttribute("operator", "--");
				break;
			default:
				throw new RuntimeException("unexpected choice " + nodeChoice.which + " is a " + nodeChoice.choice.getClass().getSimpleName().toString());
			}
		}

		return st.toString();
	}

	@Override
	public String visit(final ForStatement n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("ForStatement");

		SymbolTable symtab;
		try {
			symtab = argu.cloneNonLocals();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		this.typechecker.visit(n.f2, symtab);

		st.setAttribute("declaration", n.f2.accept(this, symtab));
		st.setAttribute("expression", n.f4.accept(this, symtab));
		st.setAttribute("exprstmt", n.f6.accept(this, symtab));
		st.setAttribute("statement", n.f8.accept(this, symtab));

		return st.toString();
	}

	@Override
	public String visit(final ForVarDecl n, final SymbolTable argu) {
		final VarDecl varDecl = new VarDecl(n.f0, n.f1, n.f2, n.f3, new NodeToken(";"));

		return varDecl.accept(this, argu);
	}

	@Override
	public String visit(final ForExprStatement n, final SymbolTable argu) {
		if (n.f1.present())
			return n.f0.accept(this, argu) + ((NodeToken)((NodeChoice)n.f1.node).choice).tokenImage;
		return n.f0.accept(this, argu);
	}

	@Override
	public String visit(final IfStatement n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("IfStatement");

		st.setAttribute("expression", n.f2.f0.accept(this, argu));
		st.setAttribute("statement", n.f4.f0.accept(this, argu));

		if (n.f5.present())
			st.setAttribute("elseStatement", ((NodeSequence) n.f5.node).nodes.elementAt(1).accept(this, argu));

		return st.toString();
	}

	@Override
	public String visit(final ResultStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final ReturnStatement n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("Return");

		if (n.f1.present())
			st.setAttribute("expr", n.f1.node.accept(this, argu));

		return st.toString();
	}

	@Override
	public String visit(final SwitchStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final WhenStatement n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("WhenStatement");

		SymbolTable localArgu;
		try {
			localArgu = argu.cloneNonLocals();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

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

		final SizzleType type = this.typechecker.visit(n.f4, argu);

		final String id = n.f2.f0.tokenImage;

		localArgu.set(id, type);
		st.setAttribute("type", type.toJavaType());
		st.setAttribute("index", id);

		this.indexeefinder.setSymbolTable(argu);
		final Set<String> indexees = this.indexeefinder.visit(n.f6, id);
	
		if (indexees.size() > 0) {
			final List<String> array = new ArrayList<String>(indexees);
			String src = "";
			for (int i = 0; i < array.size(); i++) {
				String indexee = array.get(i);
				// FIXME rdyer
//				SizzleType indexeeType = this.typechecker.visit(indexee, argu.cloneNonLocals());
//				String func = indexeeType instanceof SizzleArray) ? ".length()" : ".size()";
				String func = ".size()";
				if (src.length() > 0)
					src = "java.lang.Math.min(" + indexee + func + ", " + src + ")";
				else
					src = indexee + func;
			}

			st.setAttribute("len", src);
		}

		st.setAttribute("expression", n.f6.accept(this, localArgu));
		st.setAttribute("statement", n.f8.accept(this, localArgu));

		return st.toString();
	}

	@Override
	public String visit(final IdentifierList n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final WhileStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final Expression n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("Expression");

		st.setAttribute("lhs", n.f0.accept(this, argu));

		if (n.f1.present()) {
			final List<String> operators = new ArrayList<String>();
			final List<String> operands = new ArrayList<String>();

			for (final Node node : n.f1.nodes) {
				operators.add(" || ");
				operands.add(((NodeSequence) node).elementAt(1).accept(this, argu));
			}

			st.setAttribute("operators", operators);
			st.setAttribute("operands", operands);
		}

		return st.toString();
	}

	@Override
	public String visit(final Conjunction n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("Expression");

		st.setAttribute("lhs", n.f0.accept(this, argu));

		if (n.f1.present()) {
			final List<String> operators = new ArrayList<String>();
			final List<String> operands = new ArrayList<String>();

			for (final Node node : n.f1.nodes) {
				operators.add(" && ");
				operands.add(((NodeSequence) node).elementAt(1).accept(this, argu));
			}

			st.setAttribute("operators", operators);
			st.setAttribute("operands", operands);
		}

		return st.toString();
	}

	@Override
	public String visit(final Comparison n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("Expression");

		st.setAttribute("lhs", n.f0.accept(this, argu));

		if (n.f1.present()) {
			final List<String> operators = new ArrayList<String>();
			final List<String> operands = new ArrayList<String>();

			final Vector<Node> nodes = ((NodeSequence) n.f1.node).nodes;
			final NodeChoice nodeChoice = (NodeChoice) nodes.elementAt(0);
			switch (nodeChoice.which) {
			case 0:
				if (n.f0.accept(this.typechecker, argu) instanceof SizzleString) {
					operators.add(".equals(" + nodes.elementAt(1).accept(this, argu) + ")");
					operands.add("");
					break;
				}
				// fall through
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
				operators.add(" " + ((NodeToken)nodeChoice.choice).tokenImage + " ");
				break;
			default:
				throw new RuntimeException("unexpected choice " + nodeChoice.which + " is " + nodeChoice.choice.getClass());
			}
			if (operands.size() == 0)
				operands.add(nodes.elementAt(1).accept(this, argu));

			st.setAttribute("operators", operators);
			st.setAttribute("operands", operands);
		}

		return st.toString();
	}

	@Override
	public String visit(final SimpleExpr n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("Expression");

		st.setAttribute("lhs", n.f0.accept(this, argu));

		if (n.f1.present()) {
			final List<String> operators = new ArrayList<String>();
			final List<String> operands = new ArrayList<String>();

			for (final Node node : n.f1.nodes) {
				final NodeSequence nodeSequence = (NodeSequence) node;
				operators.add(((NodeToken) ((NodeChoice) nodeSequence.elementAt(0)).choice).tokenImage);
				operands.add(nodeSequence.elementAt(1).accept(this, argu));
			}

			st.setAttribute("operators", operators);
			st.setAttribute("operands", operands);
		}

		return st.toString();
	}

	@Override
	public String visit(final Term n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("Expression");

		st.setAttribute("lhs", n.f0.accept(this, argu));

		if (n.f1.present()) {
			final List<String> operators = new ArrayList<String>();
			final List<String> operands = new ArrayList<String>();

			for (final Node node : n.f1.nodes) {
				final NodeSequence nodeSequence = (NodeSequence) node;
				operators.add(((NodeToken) ((NodeChoice) nodeSequence.elementAt(0)).choice).tokenImage);
				operands.add(nodeSequence.elementAt(1).accept(this, argu));
			}

			st.setAttribute("operators", operators);
			st.setAttribute("operands", operands);
		}

		return st.toString();
	}

	@Override
	public String visit(final Factor n, final SymbolTable argu) {
		if (n.f1.present()) {
			final NodeChoice nodeChoice = (NodeChoice) n.f1.nodes.get(0);

			switch (nodeChoice.which) {
			case 0: // selector
			case 1: // index
				argu.setOperand(n.f0);
				try {
					argu.setOperandType(this.typechecker.visit(argu.getOperand(), argu.cloneNonLocals()));
				} catch (final IOException e) { }
				String accept = n.f0.accept(this, argu);
				abortGeneration = false;
				for (int i = 0; !abortGeneration && i < n.f1.nodes.size(); i++)
					accept += n.f1.nodes.elementAt(i).accept(this, argu);
				argu.setOperand(null);
				return accept;
			case 2: // call
				argu.setOperand(n.f0);
				accept = n.f1.nodes.elementAt(0).accept(this, argu);
				argu.setOperand(null);
				return accept;
			default:
				throw new RuntimeException("unexpected choice " + nodeChoice.which + " is " + nodeChoice.choice.getClass());
			}
		} else {
			return n.f0.accept(this, argu);
		}
	}

	@Override
	public String visit(final Selector n, final SymbolTable argu) {
		try {
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

	@Override
	public String visit(final Index n, final SymbolTable argu) {
		if (this.namefinder.visit(n.f1).contains(this.skipIndex)) {
			abortGeneration = true;
			return "";
		}

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

		SizzleType indexType = n.f1.accept(typechecker, argu);
		if (indexType instanceof SizzleInt)
			st.setAttribute("index", "(int)" + n.f1.accept(this, argu));
		else
			st.setAttribute("index", n.f1.accept(this, argu));

		if (n.f2.present())
			st.setAttribute("slice", ((NodeSequence) n.f2.node).elementAt(1).accept(this, argu));

		return st.toString();
	}

	@Override
	public String visit(final Call n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("Call");

		final SizzleFunction f = argu.getFunction(this.namefinder.visit(argu.getOperand()).toArray()[0].toString(), this.typechecker.check(n, argu));

		if (f.hasMacro()) {
			final List<String> parts = new ArrayList<String>();
			final ExprList list = (ExprList) n.f1.node;
			parts.add(list.f0.accept(this, argu));
			if (list.f1.present())
				for (final Node node : list.f1.nodes)
					parts.add(((Expression)((NodeSequence)node).elementAt(1)).accept(this, argu));
			st.setAttribute("call", CodeGeneratingVisitor.expand(f.getMacro(), parts.toArray(new String[]{})));
		} else if (f.hasName()) {
			st.setAttribute("operand", f.getName());

			if (n.f1.present())
				st.setAttribute("parameters", ((ExprList) n.f1.node).accept(this, argu));
		} else {
			st.setAttribute("operand", argu.getOperand().accept(this, argu) + ".invoke");

			if (n.f1.present())
				st.setAttribute("parameters", "new Object[] {" + ((ExprList) n.f1.node).accept(this, argu) + "}");
		}

		return st.toString();
	}

	@Override
	public String visit(final RegexpList n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final Regexp n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final Operand n, final SymbolTable argu) {
		switch (n.f0.which) {
		case 0: // identifier
		case 1: // string literal
		case 2: // integer literal
		case 3: // floating point literal
		case 4: // composite
		case 5: // visitor
		case 6: // function
		case 9: // statement expression
			return n.f0.choice.accept(this, argu);
		case 7: // unary operator
			NodeChoice c = (NodeChoice)((NodeSequence) n.f0.choice).nodes.elementAt(0);
			switch (c.which) {
			case 0:
			case 1:
			case 2:
			case 3:
				return ((NodeToken)c.choice).tokenImage + ((NodeSequence) n.f0.choice).nodes.elementAt(1).accept(this, argu);
			case 4:
				return "!" + ((NodeSequence) n.f0.choice).nodes.elementAt(1).accept(this, argu);
			default:
				throw new RuntimeException("unexpected choice " + c.which + " is " + c.choice.getClass());
			}
		case 10: // parenthetical
			return "(" + ((NodeSequence) n.f0.choice).nodes.elementAt(1).accept(this, argu) + ")";
		default:
			throw new RuntimeException("unexpected choice " + n.f0.which + " is " + n.f0.choice.getClass());
		}
	}

	@Override
	public String visit(final Composite n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("Composite");

		if (n.f1.present()) {
			final NodeChoice nodeChoice = (NodeChoice) n.f1.node;
			switch (nodeChoice.which) {
			case 0: // pair list
				st.setAttribute("pairlist", nodeChoice.choice.accept(this, argu));
				break;
			case 1: // expression list
				SizzleType t;
				try {
					t = this.typechecker.visit((ExprList) nodeChoice.choice, argu.cloneNonLocals());
				} catch (final IOException e) {
					throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
				}

				if (argu.getOperandType() instanceof SizzleArray && t instanceof SizzleTuple)
					t = new SizzleArray(((SizzleTuple)t).getMember(0));

				st.setAttribute("type", t.toJavaType());
				st.setAttribute("exprlist", nodeChoice.choice.accept(this, argu));
				break;
			default:
				throw new RuntimeException("unexpected choice " + nodeChoice.which + " is " + nodeChoice.choice.getClass());
			}
		}

		return st.toString();
	}

	@Override
	public String visit(final PairList n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("PairList");

		final List<String> pairs = new ArrayList<String>();

		pairs.add(n.f0.accept(this, argu));

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				pairs.add(((NodeSequence) node).elementAt(1).accept(this, argu));

		st.setAttribute("pairs", pairs);

		return st.toString();
	}

	@Override
	public String visit(final Pair n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("Pair");

		st.setAttribute("map", argu.getId());
		st.setAttribute("key", n.f0.accept(this, argu));
		st.setAttribute("value", n.f2.accept(this, argu));

		return st.toString();
	}

	@Override
	public String visit(final Function n, final SymbolTable argu) {
		final StringTemplate st = this.stg.getInstanceOf("Function");

		SymbolTable funcArgu = null;
		try {
			funcArgu = argu.cloneNonLocals();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		final SizzleType t = this.typechecker.visit(n.f0, funcArgu);
		this.typechecker.visit(n.f1, funcArgu);

		if (!(t instanceof SizzleFunction))
			throw new TypeException(n ,"type " + t + " no a function type");
		final SizzleFunction funcType = ((SizzleFunction) t);

		SizzleType[] paramTypes = funcType.getFormalParameters();
		List<String> params = new ArrayList<String>();

		for (int i = 0; i < paramTypes.length; i++)
			params.add(paramTypes[i].toBoxedJavaType() + " ___" + ((SizzleName) paramTypes[i]).getId() + " = (" + paramTypes[i].toBoxedJavaType() + ")args[" + i + "];");

		if (!(funcType.getType() instanceof SizzleAny))
			st.setAttribute("ret", funcType.getType().toBoxedJavaType());
		st.setAttribute("parameters", params);
		st.setAttribute("body", n.f1.accept(this, funcArgu));

		return st.toString();
	}

	@Override
	public String visit(final StatementExpr n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final Identifier n, final SymbolTable argu) {
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

	@Override
	public String visit(final IntegerLiteral n, final SymbolTable argu) {
		return n.f0.tokenImage + 'l';
	}

	@Override
	public String visit(final FingerprintLiteral n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final FloatingPointLiteral n, final SymbolTable argu) {
		return n.f0.tokenImage + "d";
	}

	@Override
	public String visit(final CharLiteral n, final SymbolTable argu) {
		return n.f0.tokenImage;
	}

	@Override
	public String visit(final StringLiteral n, final SymbolTable argu) {
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

	@Override
	public String visit(final BytesLiteral n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final TimeLiteral n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
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

	@Override
	public String visit(final EmptyStatement n, final SymbolTable argu) {
		return ";\n";
	}

	@Override
	public String visit(final StopStatement n, final SymbolTable argu) {
		return "return false;\n";
	}

	@Override
	public String visit(final VisitorExpr n, final SymbolTable argu) {
		return "new sizzle.runtime.BoaAbstractVisitor()" + n.f1.accept(this, argu);
	}

	@Override
	public String visit(final VisitorType n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final VisitStatement n, final SymbolTable argu) {
		SymbolTable st;
		try {
			st = argu.cloneNonLocals();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		st.setIsBeforeVisitor(n.f0.which == 0 && n.f1.which != 2);

		final boolean isBefore = n.f0.which == 0;
		String s = "";
		switch (n.f1.which) {
		case 0: // single type
			final String typeName = ((Identifier)((NodeSequence)n.f1.choice).nodes.get(2)).f0.tokenImage;
			final String ident = ((Identifier)((NodeSequence)n.f1.choice).nodes.get(0)).f0.tokenImage;
			final SizzleType type = st.get(typeName);
			if (type != null)
				st.set(ident, type);
			else
				throw new TypeException(n, "Invalid type '" + typeName + "'");

			s = "@Override\nprotected ";
			s += isBefore ? "boolean preVisit" : "void postVisit";
			s += "(final " + argu.get(typeName).toJavaType() + " ___" + ident + ") throws Exception {\n";
			s += n.f3.accept(this, st);
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
				s += n.f3.accept(this, st);
				if (isBefore && !lastStatementIsStop(n.f3))
					s += "return true;\n";
				s += "}\n";
			}
			break;
		case 2: // wildcard
			s = "@Override\nprotected void ";
			s += isBefore ? "defaultPreVisit" : "defaultPostVisit";
			s += "() throws Exception {\n";
			s += n.f3.accept(this, st);
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
