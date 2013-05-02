package boa.compiler.visitors;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

import boa.compiler.SymbolTable;
import boa.compiler.TypeException;
import boa.compiler.ast.*;
import boa.compiler.ast.expressions.*;
import boa.compiler.ast.literals.*;
import boa.compiler.ast.statements.*;
import boa.compiler.ast.types.*;
import boa.types.*;

/**
 * 
 * @author anthonyu
 */
class TableDescription {
	private String aggregator;
	private BoaType type;
	private List<String> parameters;

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

/***
 * 
 * @author anthonyu
 * @author rdyer
 */
public class CodeGeneratingVisitor extends AbstractCodeGeneratingVisitor {
	private final IdentifierFindingVisitor idFinder;
	private final IndexeeFindingVisitor indexeeFinder;
	private final VarDeclCodeGeneratingVisitor varDecl;
	private final StaticInitializationCodeGeneratingVisitor staticInitialization;
	private final FunctionDeclaratorCodeGeneratingVisitor functionDeclarator;

	private final HashMap<String, TableDescription> tables;

	private boolean hasEmit = false;

	private final String name;

	private String skipIndex = "";
	private boolean abortGeneration = false;

	final public static List<String> tableStrings = new ArrayList<String>();

	public CodeGeneratingVisitor(final String name, final StringTemplateGroup stg) throws IOException {
		this.idFinder = new IdentifierFindingVisitor();
		this.indexeeFinder = new IndexeeFindingVisitor(idFinder);
		this.varDecl = new VarDeclCodeGeneratingVisitor(stg);
		this.staticInitialization = new StaticInitializationCodeGeneratingVisitor(stg);
		this.functionDeclarator = new FunctionDeclaratorCodeGeneratingVisitor(stg);

		this.tables = new HashMap<String, TableDescription>();

		this.name = name;
		this.stg = stg;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Program n) {
		final SymbolTable argu = n.env;
		final StringTemplate st = this.stg.getInstanceOf("Job");

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
			statements.add(code.removeLast());
		}
		st.setAttribute("statements", statements);

		if (this.tables.size() == 0)
			throw new TypeException(n, "No output variables were declared - must declare at least one output variable");

		if (!hasEmit)
			throw new TypeException(n, "No emit statements detected - there will be no output generated");

		for (final Entry<String, TableDescription> entry : this.tables.entrySet()) {
			final String id = entry.getKey();
			final TableDescription description = entry.getValue();
			final String parameters = description.getParameters() == null ? "" : description.getParameters().get(0);
			final BoaType type = description.getType();

			final StringBuilder src = new StringBuilder();
			for (final Class<?> c : argu.getAggregators(description.getAggregator(), type))
				src.append(", new " + c.getCanonicalName() + "(" + parameters + ")");

			tableStrings.add("this.tables.put(\"" + name + "::" + id + "\", new boa.aggregators.Table(" + src.toString().substring(2) + "));");
		}

		code.add(st.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Call n) {
		final SymbolTable argu = n.env;
		final StringTemplate st = this.stg.getInstanceOf("Call");

		this.idFinder.start(argu.getOperand());
		final BoaFunction f = argu.getFunction(this.idFinder.getNames().toArray()[0].toString(), check(n));

		if (f.hasMacro()) {
			final List<String> parts = new ArrayList<String>();
			for (final Expression e : n.getArgs()) {
				e.accept(this);
				parts.add(code.removeLast());
			}

			st.setAttribute("call", expand(f.getMacro(), parts.toArray(new String[]{})));
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
		final StringTemplate st = this.stg.getInstanceOf("Expression");

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
		final StringTemplate st = this.stg.getInstanceOf("Composite");

		if (n.getPairsSize() > 0) {
			visit(n.getPairs());
			st.setAttribute("pairlist", code.removeLast());
		}
		if (n.getExprsSize() > 0) {
			// FIXME
			BoaType t = n.type;
//			BoaType t = ((ExprList) nodeChoice.choice).type;

			if (n.env.getOperandType() instanceof BoaArray && t instanceof BoaTuple)
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
		final StringTemplate st = this.stg.getInstanceOf("Expression");

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
				code.add(argu.getType(id).toBoxedJavaType());
			else
				code.add(argu.getType(id).toJavaType());
			return;
		}

		// otherwise return the identifier template
		final StringTemplate st = this.stg.getInstanceOf("Identifier");

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
		final StringTemplate st = this.stg.getInstanceOf("Index");

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
		final StringTemplate st = this.stg.getInstanceOf("Pair");

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
		} catch (final TypeException e) {
			throw new RuntimeException("unimplemented");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Term n) {
		final StringTemplate st = this.stg.getInstanceOf("Expression");

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
		final StringTemplate st = this.stg.getInstanceOf("Assignment");

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
		final StringTemplate st = this.stg.getInstanceOf("Block");

		final List<String> statements = new ArrayList<String>();

		for (final Node node : n.getStatements()) {
			node.accept(this);
			statements.add(code.removeLast());
		}

		st.setAttribute("statements", statements);

		code.add(st.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final BreakStatement n) {
		code.add(this.stg.getInstanceOf("Break").toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ContinueStatement n) {
		code.add(this.stg.getInstanceOf("Continue").toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final DoStatement n) {
		final StringTemplate st = this.stg.getInstanceOf("DoWhile");

		n.getCondition().accept(this);
		st.setAttribute("condition", code.removeLast());

		n.getBody().accept(this);
		st.setAttribute("stmt", code.removeLast());

		code.add(st.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final EmitStatement n) {
		final StringTemplate st = this.stg.getInstanceOf("EmitStatement");

		hasEmit = true;

		if (n.getIndicesSize() > 0) {
			final List<String> indices = new ArrayList<String>();

			for (final Expression e : n.getIndices()) {
				e.accept(this);
				indices.add(code.removeLast());
			}

			st.setAttribute("indices", indices);
		}

		st.setAttribute("id", "\"" + n.getId().getToken() + "\"");
		st.setAttribute("job", name);

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
	public void visit(final ExistsStatement n) {
		final SymbolTable argu = n.env;
		final StringTemplate st = this.stg.getInstanceOf("WhenStatement");

		st.setAttribute("some", "true");

		final BoaType type = n.getVar().getType().type;

		final String id = n.getVar().getIdentifier().getToken();

		argu.set(id, type);
		st.setAttribute("type", type.toJavaType());
		st.setAttribute("index", id);

		this.indexeeFinder.start(n.getCondition(), id);
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
		}

		n.getCondition().accept(this);
		st.setAttribute("expression", code.removeLast());

		n.getBody().accept(this);
		st.setAttribute("statement", code.removeLast());

		code.add(st.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ExprStatement n) {
		final StringTemplate st = this.stg.getInstanceOf("ExprStatement");

		n.getExpr().accept(this);
		st.setAttribute("expression", code.removeLast());

		code.add(st.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ForeachStatement n) {
		final SymbolTable argu = n.env;
		final StringTemplate st = this.stg.getInstanceOf("WhenStatement");

		final BoaType type = n.getVar().getType().type;

		final String id = n.getVar().getIdentifier().getToken();

		argu.set(id, type);
		st.setAttribute("type", type.toJavaType());
		st.setAttribute("index", id);

		this.indexeeFinder.start(n.getCondition(), id);
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
		}

		n.getCondition().accept(this);
		st.setAttribute("expression", code.removeLast());

		n.getBody().accept(this);
		st.setAttribute("statement", code.removeLast());

		code.add(st.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final ForStatement n) {
		final StringTemplate st = this.stg.getInstanceOf("ForStatement");

		if (n.hasInit()) {
			n.getInit().accept(this);
			st.setAttribute("declaration", code.removeLast());
		}

		if (n.hasCondition()) {
			n.getCondition().accept(this);
			st.setAttribute("expression", code.removeLast());
		}

		if (n.hasUpdate()) {
			n.getUpdate().accept(this);
			st.setAttribute("exprstmt", code.removeLast());
		}

		n.getBody().accept(this);
		st.setAttribute("statement", code.removeLast());

		code.add(st.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final IfAllStatement n) {
		final SymbolTable argu = n.env;
		final StringTemplate st = this.stg.getInstanceOf("WhenStatement");

		st.setAttribute("all", "true");

		final BoaType type = n.getVar().getType().type;

		final String id = n.getVar().getIdentifier().getToken();

		argu.set(id, type);
		st.setAttribute("type", type.toJavaType());
		st.setAttribute("index", id);

		this.indexeeFinder.start(n.getCondition(), id);
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
		}

		n.getCondition().accept(this);
		st.setAttribute("expression", code.removeLast());

		n.getBody().accept(this);
		st.setAttribute("statement", code.removeLast());

		code.add(st.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final IfStatement n) {
		final StringTemplate st = this.stg.getInstanceOf("IfStatement");

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
		final StringTemplate st = this.stg.getInstanceOf("ExprStatement");

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
		final StringTemplate st = this.stg.getInstanceOf("Return");

		if (n.hasExpr()) {
			n.getExpr().accept(this);
			st.setAttribute("expr", code.removeLast());
		}

		code.add(st.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final StopStatement n) {
		code.add(this.stg.getInstanceOf("Stop").toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SwitchCase n) {
		final StringTemplate caseSt = this.stg.getInstanceOf("Case");

		final List<String> cases = new ArrayList<String>();
		for (final Expression expr : n.getCases()) {
			expr.accept(this);
			String s = code.removeLast();
			if (expr.type instanceof BoaProtoMap)
				s = s.substring(s.lastIndexOf(".") + 1);
			cases.add(s);
		}

		final List<String> caseBody = new ArrayList<String>();
		for (final Statement stmt : n.getStmts()) {
			stmt.accept(this);
			caseBody.add(code.removeLast());
		}

		caseSt.setAttribute("cases", cases);
		caseSt.setAttribute("body", caseBody);
		code.add(caseSt.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SwitchStatement n) {
		final StringTemplate st = this.stg.getInstanceOf("Switch");

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

		final StringTemplate st = this.stg.getInstanceOf("Assignment");
		st.setAttribute("lhs", "___" + n.getId().getToken());

		if (!n.hasInitializer()) {
			if (lhsType instanceof BoaProtoMap ||
					!(lhsType instanceof BoaMap || lhsType instanceof BoaStack)) {
				code.add("");
				return;
			}

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
				src = CodeGeneratingVisitor.expand(f.getMacro(), src.split(","));
		}

		st.setAttribute("rhs", src);

		code.add(st.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VisitStatement n) {
		final StringTemplate st = this.stg.getInstanceOf("VisitClause");
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
			ids.add(" ___" + id);

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
		final StringTemplate st = this.stg.getInstanceOf("While");

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
		final StringTemplate st = this.stg.getInstanceOf("Expression");

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
		final StringTemplate st = this.stg.getInstanceOf("Function");

		if (!(n.getType().type instanceof BoaFunction))
			throw new TypeException(n ,"type " + n.getType().type + " is not a function type");

		final BoaFunction funcType = ((BoaFunction) n.getType().type);

		final BoaType[] paramTypes = funcType.getFormalParameters();
		final List<String> args = new ArrayList<String>();
		final List<String> types = new ArrayList<String>();

		for (int i = 0; i < paramTypes.length; i++) {
			args.add(((BoaName) paramTypes[i]).getId());
			types.add(paramTypes[i].toJavaType());
		}

		this.varDecl.start(n.getBody());
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
		final StringTemplate st = this.stg.getInstanceOf("Expression");

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
		final StringTemplate st = this.stg.getInstanceOf("Visitor");

		this.varDecl.start(n.getBody());
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
	public void visit(final BytesLiteral n) {
		throw new RuntimeException("unimplemented");
	}

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
		throw new RuntimeException("unimplemented");
	}

	//
	// types
	//
	/** {@inheritDoc} */
	@Override
	public void visit(final ArrayType n) {
		final StringTemplate st = this.stg.getInstanceOf("ArrayType");

		n.getValue().accept(this);
		st.setAttribute("type", code.removeLast());

		code.add(st.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final FunctionType n) {
		final StringTemplate st = this.stg.getInstanceOf("FunctionType");

		if (!(n.type instanceof BoaFunction))
			throw new TypeException(n ,"type " + n.type + " is not a function type");

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
		final StringTemplate st = this.stg.getInstanceOf("MapType");

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
		final StringTemplate st = this.stg.getInstanceOf("StackType");

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

	private static String expand(final String template, final String... parameters) {
		String replaced = template;

		// FIXME rdyer
		replaced = replaced.replace("${K}", "String");

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

	private boolean lastStatementIsStop(Statement s) {
		if (s instanceof StopStatement)
			return true;

		if (s instanceof Block)
			for (final Node n : ((Block)s).getStatements())
				if (n instanceof StopStatement)
					return true;

		return false;
	}

	private List<BoaType> check(final Call c) {
		if (c.getArgsSize() > 0)
			return this.check(c.getArgs());

		return new ArrayList<BoaType>();
	}

	private List<BoaType> check(final List<Expression> el) {
		final List<BoaType> types = new ArrayList<BoaType>();

		for (final Expression e : el)
			types.add(assignableType(e.type));

		return types;
	}

	private BoaType assignableType(BoaType t) {
		if (t instanceof BoaFunction)
			return ((BoaFunction) t).getType();
		return t;
	}
}
