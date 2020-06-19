package boa.datagen.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
//import org.eclipse.dltk.compiler.IElementRequestor;
//import org.eclipse.dltk.compiler.SourceElementRequestVisitor;
//
//import com.puppycrawl.tools.checkstyle.checks.coding.SuperCloneCheck;
//
//import org.eclipse.dltk.ast.expressions.Expression;
//import org.eclipse.dltk.ast.ASTVisitor;
//import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
import org.eclipse.dltk.ast.ASTNode;
import org.eclipse.dltk.ast.ASTVisitor;
import org.eclipse.dltk.ast.declarations.Decorator;
import org.eclipse.dltk.ast.declarations.FieldDeclaration;
import org.eclipse.dltk.ast.declarations.MethodDeclaration;
import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
import org.eclipse.dltk.ast.declarations.ModuleDeclarationWrapper;
import org.eclipse.dltk.ast.declarations.TypeDeclaration;
import org.eclipse.dltk.ast.expressions.ExpressionConstants;
import org.eclipse.dltk.ast.expressions.ExpressionList;
import org.eclipse.dltk.ast.expressions.Literal;
import org.eclipse.dltk.python.parser.ast.PythonArgument;
import org.eclipse.dltk.python.parser.ast.PythonAssertStatement;
import org.eclipse.dltk.python.parser.ast.PythonClassDeclaration;
import org.eclipse.dltk.python.parser.ast.PythonConstants;
import org.eclipse.dltk.python.parser.ast.PythonDelStatement;
import org.eclipse.dltk.python.parser.ast.PythonExceptStatement;
import org.eclipse.dltk.python.parser.ast.PythonForStatement;
import org.eclipse.dltk.python.parser.ast.PythonImportFromStatement;
import org.eclipse.dltk.python.parser.ast.PythonModuleDeclaration;
import org.eclipse.dltk.python.parser.ast.PythonRaiseStatement;
import org.eclipse.dltk.python.parser.ast.PythonTryStatement;
import org.eclipse.dltk.python.parser.ast.PythonWhileStatement;
import org.eclipse.dltk.python.parser.ast.PythonWithStatement;
import org.eclipse.dltk.python.parser.ast.PythonYieldStatement;
import org.eclipse.dltk.python.parser.ast.expressions.PythonImportAsExpression;
import org.eclipse.dltk.python.parser.ast.expressions.PythonImportExpression;
import org.eclipse.dltk.python.parser.ast.expressions.PythonLambdaExpression;
import org.eclipse.dltk.python.parser.ast.expressions.PythonListExpression;
import org.eclipse.dltk.python.parser.ast.expressions.PythonListForExpression;
import org.eclipse.dltk.python.parser.ast.expressions.PythonSubscriptExpression;
import org.eclipse.dltk.python.parser.ast.expressions.PythonTestListExpression;
import org.eclipse.dltk.python.parser.ast.expressions.PythonTupleExpression;
import org.eclipse.dltk.python.parser.ast.expressions.UnaryExpression;
import org.eclipse.dltk.python.parser.ast.statements.BreakStatement;
import org.eclipse.dltk.python.parser.ast.statements.ContinueStatement;
import org.eclipse.dltk.python.parser.ast.statements.EmptyStatement;
import org.eclipse.dltk.python.parser.ast.statements.ExecStatement;
import org.eclipse.dltk.python.parser.ast.statements.IfStatement;
import org.eclipse.dltk.python.parser.ast.statements.ReturnStatement;
import org.eclipse.dltk.python.parser.ast.statements.SimpleStatement;
import org.eclipse.dltk.python.parser.ast.statements.TryFinallyStatement;
import org.eclipse.php.internal.core.ast.nodes.YieldExpression;
import org.eclipse.dltk.python.parser.ast.expressions.Assignment;
import org.eclipse.dltk.python.parser.ast.expressions.BinaryExpression;
import org.eclipse.dltk.python.parser.ast.expressions.CallHolder;
import org.eclipse.dltk.python.parser.ast.expressions.EmptyExpression;
import org.eclipse.dltk.python.parser.ast.expressions.ExtendedVariableReference;
import org.eclipse.dltk.python.parser.ast.expressions.IndexHolder;
import org.eclipse.dltk.python.parser.ast.expressions.NotStrictAssignment;
import org.eclipse.dltk.python.parser.ast.expressions.PrintExpression;
import org.eclipse.dltk.python.parser.ast.expressions.PythonDictExpression;
import org.eclipse.dltk.python.parser.ast.expressions.PythonDictExpression.DictNode;
import org.eclipse.dltk.python.parser.ast.expressions.PythonForListExpression;
import org.eclipse.dltk.ast.references.SimpleReference;
import org.eclipse.dltk.ast.references.VariableReference;
import org.eclipse.dltk.ast.statements.Block;
import org.eclipse.dltk.ast.expressions.NumericLiteral;
import org.eclipse.dltk.ast.expressions.StringLiteral;

import boa.types.Ast.Declaration;
import boa.types.Ast.Expression;
import boa.types.Ast.Method;
import boa.types.Ast.Modifier;
import boa.types.Ast.Namespace;
import boa.types.Ast.PositionInfo;
import boa.types.Ast.Statement;
import boa.types.Ast.Statement.StatementKind;
import boa.types.Ast.Type;
import boa.types.Ast.TypeKind;
import boa.types.Ast.Variable;
import boa.types.Ast.Expression.ExpressionKind;

import boa.types.Ast.Namespace;
import boa.types.Ast.Statement;
import boa.types.Ast.Statement.StatementKind;

public class NewPythonVisitor extends ASTVisitor {

	private ModuleDeclaration root;
	protected Namespace.Builder b = Namespace.newBuilder();
	protected Stack<boa.types.Ast.Expression> expressions = new Stack<boa.types.Ast.Expression>();

	protected Stack<List<boa.types.Ast.Variable>> fields = new Stack<List<boa.types.Ast.Variable>>();
	protected Stack<List<boa.types.Ast.Method>> methods = new Stack<List<boa.types.Ast.Method>>();
	protected Stack<List<boa.types.Ast.Statement>> statements = new Stack<List<boa.types.Ast.Statement>>();
	protected Stack<List<boa.types.Ast.Declaration>> declarations = new Stack<List<boa.types.Ast.Declaration>>();

	protected Stack<boa.types.Ast.Comment> comments = new Stack<boa.types.Ast.Comment>();

	public Namespace getNamespace(ModuleDeclaration node, String name) throws Exception {
		root = node;
		node.traverse(this);
		b.setName(name);
		return b.build();
	}

	@Override
	public boolean visitGeneral(ASTNode md) throws Exception {
		System.out.println("Enter General:  " + md.toString());

//		if(md.toString().startsWith("class org.eclipse.dltk.python.parser.ast.statements.ExecStatement"))
//		{
//			System.out.println("REACHED.");
//		}
		
		boolean opFound = false;
		
		if (md instanceof ExecStatement) {
			visit((ExecStatement) md);
			opFound = true;
		}

		else if (md instanceof PythonImportAsExpression) {
			visit((PythonImportAsExpression) md);
			opFound = true;
		} else if (md instanceof PythonImportExpression) {
			visit((PythonImportExpression) md);
			opFound = true;
		} else if (md instanceof PythonImportFromStatement) {
			visit((PythonImportFromStatement) md);
			opFound = true;
		} else if (md instanceof NotStrictAssignment) {
			visit((NotStrictAssignment) md);
			opFound = true;
		} else if (md instanceof Assignment) {
			visit((Assignment) md);
			opFound = true;
		} else if (md instanceof PythonLambdaExpression) {
			visit((PythonLambdaExpression) md);
			opFound = true;
		} else if (md instanceof TypeDeclaration) {
			visitTypeDeclaration((TypeDeclaration) md);
			opFound = true;
		} else if (md instanceof PythonDictExpression) {
			visit((PythonDictExpression) md);
			opFound = true;
		} else if (md instanceof VariableReference) {
			visit((VariableReference) md);
			opFound = true;
		} else if (md instanceof PythonWhileStatement) {
			visit((PythonWhileStatement) md);
			opFound = true;
		} else if (md instanceof BinaryExpression) {
			visit((BinaryExpression) md);
			opFound = true;
		} else if (md instanceof PrintExpression) {
			visit((PrintExpression) md);
			opFound = true;
		} else if (md instanceof ExtendedVariableReference) {
			visit((ExtendedVariableReference) md);
			opFound = true;
		} else if (md instanceof PythonTupleExpression) {
			visit((PythonTupleExpression) md);
			opFound = true;
		} else if (md instanceof PythonArgument) {
			visit((PythonArgument) md);
			opFound = true;
		} else if (md instanceof PythonTryStatement) {
			visit((PythonTryStatement) md);
			opFound = true;
		} else if (md instanceof PythonExceptStatement) {
			visit((PythonExceptStatement) md);
			opFound = true;
		} else if (md instanceof PythonAssertStatement) {
			visit((PythonAssertStatement) md);
			opFound = true;
		} else if (md instanceof PythonForStatement) {
			visit((PythonForStatement) md);
			opFound = true;
		} else if (md instanceof PythonWhileStatement) {
			visit((PythonWhileStatement) md);
			opFound = true;
		} else if (md instanceof IfStatement) {
			visit((IfStatement) md);
			opFound = true;
		} else if (md instanceof BreakStatement) {
			visit((BreakStatement) md);
			opFound = true;
		} else if (md instanceof ContinueStatement) {
			visit((ContinueStatement) md);
			opFound = true;
		} else if (md instanceof PythonRaiseStatement) {
			visit((PythonRaiseStatement) md);
			opFound = true;
		} else if (md instanceof PythonDelStatement) {
			visit((PythonDelStatement) md);
			opFound = true;
		} else if (md instanceof PythonSubscriptExpression) {
			visit((PythonSubscriptExpression) md);
			opFound = true;
		} else if (md instanceof PythonListExpression) {
			visit((PythonListExpression) md);
			opFound = true;
		} else if (md instanceof SimpleReference) {
			visit((SimpleReference) md);
			opFound = true;
		} else if (md instanceof ReturnStatement) {
			visit((ReturnStatement) md);
			opFound = true;
		} else if (md instanceof TryFinallyStatement) {
			visit((TryFinallyStatement) md);
			opFound = true;
		} else if (md instanceof PythonYieldStatement) {
			visit((PythonYieldStatement) md);
			opFound = true;
		} else if (md instanceof Block) {
			visit((Block) md);
			opFound = true;
		} else if (md instanceof org.eclipse.dltk.ast.expressions.StringLiteral) {
			visit((org.eclipse.dltk.ast.expressions.StringLiteral) md);
			opFound = true;
		} else if (md instanceof PythonWithStatement) {
			visit((PythonWithStatement) md);
			opFound = true;
		} else if (md instanceof EmptyStatement) {
			visit((EmptyStatement) md);
			opFound = true;
		} else if (md instanceof PythonListForExpression) {
			visit((PythonListForExpression) md);
			opFound = true;
		} else if (md instanceof PythonForListExpression) {
			visit((PythonForListExpression) md);
			opFound = true;
		} else if (md instanceof EmptyExpression) {
			visit((EmptyExpression) md);
			opFound = true;
		} else if (md instanceof UnaryExpression) {
			visit((UnaryExpression) md);
			opFound = true;
		}

		return !opFound;

	}

	public boolean visit(ExtendedVariableReference md) throws Exception {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();

		if (md.getExpressionCount() > 1 && md.getExpression(md.getExpressionCount() - 1) instanceof CallHolder) {
			b.setKind(boa.types.Ast.Expression.ExpressionKind.METHODCALL);
			if (md.getExpression(md.getExpressionCount() - 2) instanceof VariableReference) {
				VariableReference vr = (VariableReference) md.getExpression(md.getExpressionCount() - 2);

				b.setMethod(vr.getName());
			}
			for (int i = 0; i < md.getExpressionCount() - 2; i++) {
				md.getExpression(i).traverse(this);
				b.addExpressions(expressions.pop());
			}
			CallHolder ch = (CallHolder) md.getExpression(md.getExpressionCount() - 1);
			if (ch.getArguments() instanceof ExpressionList) {
				ExpressionList el = (ExpressionList) ch.getArguments();
				for (Object ob : el.getExpressions()) {
					org.eclipse.dltk.ast.expressions.Expression ex = (org.eclipse.dltk.ast.expressions.Expression) ob;
					ex.traverse(this);
					b.addMethodArgs(expressions.pop());
				}
			}

		} else if (md.getExpressionCount() > 1
				&& md.getExpression(md.getExpressionCount() - 1) instanceof IndexHolder) {
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ARRAYACCESS);

			for (int i = 0; i < md.getExpressionCount() - 1; i++) {
				md.getExpression(i).traverse(this);
				b.addExpressions(expressions.pop());
			}
			IndexHolder ch = (IndexHolder) md.getExpression(md.getExpressionCount() - 1);
			ch.traverse(this);
			b.addExpressions(expressions.pop());

		} else
			b.setKind(boa.types.Ast.Expression.ExpressionKind.EMPTY);
		expressions.push(b.build());
		return true;
	}

	public boolean visit(PythonLambdaExpression node) throws Exception {
		System.out.println(node.toString());
		boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.LAMBDA);

		for (Object ob : node.getArguments()) {
			fields.push(new ArrayList<Variable>());
			PythonArgument ex = (PythonArgument) ob;
			ex.traverse(this);
			List<boa.types.Ast.Variable> fs = fields.pop();

			for (boa.types.Ast.Variable v : fs)
				eb.addVariableDecls(v);
		}

		if (node.getBodyExpression() != null) {
			node.getBodyExpression().traverse(this);
			boa.types.Ast.Expression e = expressions.pop();
			eb.addExpressions(e);
		}
		expressions.push(eb.build());

		return false;
	}

	public boolean visit(PythonSubscriptExpression md) throws Exception {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();

		// need to handle unary expressions
		b.setKind(boa.types.Ast.Expression.ExpressionKind.ARRAYINDEX);
		if (md.getTest() != null) {
			md.getTest().traverse(this);
			b.addExpressions(expressions.pop());
		} else {
			System.out.println("");
		}
		if (md.getCondition() != null) {
			md.getCondition().traverse(this);
			b.addExpressions(expressions.pop());
		}
		expressions.push(b.build());
		return true;
	}

	public boolean visit(PythonListExpression md) throws Exception {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();

		b.setKind(boa.types.Ast.Expression.ExpressionKind.NEWARRAY);

		for (Object ob : md.getExpressions()) {
			org.eclipse.dltk.ast.expressions.Expression ex = (org.eclipse.dltk.ast.expressions.Expression) ob;
			ex.traverse(this);
			b.addExpressions(expressions.pop());
		}
		expressions.push(b.build());
		return true;
	}

	public boolean visit(PythonTupleExpression md) throws Exception {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();

		b.setKind(boa.types.Ast.Expression.ExpressionKind.TUPLE);
		for (Object ob : md.getExpressions()) {
			org.eclipse.dltk.ast.expressions.Expression ex = (org.eclipse.dltk.ast.expressions.Expression) ob;
			ex.traverse(this);
			b.addExpressions(expressions.pop());
		}
		expressions.push(b.build());
		return true;
	}

	public boolean visit(PythonImportAsExpression md) {
		String imp = "";
		imp += md.getName() + " as " + md.getAsName();

		b.addImports(imp);
		return true;
	}

	public boolean visit(PythonImportExpression md) {
		String imp = "";
		imp += md.getName();
		b.addImports(imp);
		return true;
	}

	public boolean visit(PythonImportFromStatement md) {

		Map<String, String> imas = md.getImportedAsNames();

		String moduleName = md.getImportModuleName();

		if (md.isAllImport()) {
			b.addImports(moduleName + ".*");
		} else {
			for (Map.Entry<String, String> entry : imas.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				if (key.equals(value))
					b.addImports(moduleName + "." + key);
				else
					b.addImports(moduleName + "." + key + " as " + value);
			}
		}

		return true;
	}

	public boolean visit(PythonArgument node) throws Exception {

		Variable.Builder b = Variable.newBuilder();
		List<Variable> list = fields.peek();

		b.setName(node.getName());

		// to handle argument initialization

//		if (node.getDefaultValue() != null) {
//			node.getDefaultValue().accept(this);
//			b.setInitializer(expressions.pop());
//		}
		list.add(b.build());
		return false;

	}

	public boolean visit(PrintExpression md) throws Exception {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();

		b.setKind(boa.types.Ast.Expression.ExpressionKind.METHODCALL);
		b.setMethod("print");
		md.getExpression().traverse(this);
		b.addMethodArgs(expressions.pop());

		expressions.push(b.build());

		return true;

	}

	public boolean visit(PythonForListExpression md) throws Exception {

		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();

		b.setKind(boa.types.Ast.Expression.ExpressionKind.FOR_LIST);

		if (md.getVars() != null) {
			md.getVars().traverse(this);
			b.addExpressions(expressions.pop());
		}

		if (md.getFrom() != null) {
			md.getFrom().traverse(this);
			b.addExpressions(expressions.pop());
		}
		if (md.getIfList() != null) {
			md.getIfList().traverse(this);
			b.addExpressions(expressions.pop());
		}
		expressions.push(b.build());

		return true;

	}

	public boolean visit(PythonListForExpression md) throws Exception {

		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();

		b.setKind(boa.types.Ast.Expression.ExpressionKind.ARRAY_COMPREHENSION);

		if (md.getMaker() != null) {
			md.getMaker().traverse(this);
			b.addExpressions(expressions.pop());
		}
		if (md.getExpressions() != null) {

			for (Object ob : md.getExpressions()) {
				org.eclipse.dltk.ast.expressions.Expression ex = (org.eclipse.dltk.ast.expressions.Expression) ob;
				ex.traverse(this);
				b.addExpressions(expressions.pop());
			}
		}
		expressions.push(b.build());

		return true;

	}

	public boolean visit(PythonDictExpression md) throws Exception {

		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();

		b.setKind(boa.types.Ast.Expression.ExpressionKind.DICT);

		Iterator i = md.getfDictionary().iterator();
		while (i.hasNext()) {

			boa.types.Ast.Expression.Builder ditem = boa.types.Ast.Expression.newBuilder();

			ditem.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);

			DictNode node = (DictNode) i.next();
			org.eclipse.dltk.ast.expressions.Expression key = node.getKey();
			if (key != null) {
				key.traverse(this);
				ditem.addExpressions(expressions.pop());
			}
			org.eclipse.dltk.ast.expressions.Expression value = node.getValue();
			if (value != null) {
				value.traverse(this);
				ditem.addExpressions(expressions.pop());
			}
			b.addExpressions(ditem);
		}
		expressions.push(b.build());

		return true;

	}

	public boolean visit(Assignment md) throws Exception {
		System.out.println("Enter Assigning: " + md.toString());
		System.out.println(md.getLeft().getKind());
		System.out.println(md.getRight().getKind());
		System.out.println(md.getLeft().getClass().getName());
		System.out.println(md.getRight().getClass().getName());

		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();

		b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN);

		md.getLeft().traverse(this);
		b.addExpressions(expressions.pop());

		md.getRight().traverse(this);
		b.addExpressions(expressions.pop());

		expressions.push(b.build());

		return true;

	}

	public boolean visit(NotStrictAssignment md) throws Exception {

		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();

		if (md.getKind() == ExpressionConstants.E_PLUS_ASSIGN)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_ADD);
		else if (md.getKind() == ExpressionConstants.E_MINUS_ASSIGN)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_SUB);
		else if (md.getKind() == ExpressionConstants.E_MULT_ASSIGN)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_MULT);
		else if (md.getKind() == ExpressionConstants.E_DIV_ASSIGN)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_DIV);
		else if (md.getKind() == ExpressionConstants.E_MOD_ASSIGN)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_MOD);
		else if (md.getKind() == ExpressionConstants.E_POWER_ASSIGN)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_POW);
		
		else if (md.getKind() == ExpressionConstants.E_RSHIFT_ASSIGN)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_RSHIFT);
		else if (md.getKind() == ExpressionConstants.E_LSHIFT_ASSIGN)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_LSHIFT);
		else if (md.getKind() == ExpressionConstants.E_BAND_ASSIGN)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_BITAND);
		else if (md.getKind() == ExpressionConstants.E_BOR_ASSIGN)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_BITOR);
		else if (md.getKind() == ExpressionConstants.E_BXOR_ASSIGN)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_BITXOR);
		else
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);
		

		md.getLeft().traverse(this);
		b.addExpressions(expressions.pop());

		md.getRight().traverse(this);
		b.addExpressions(expressions.pop());

		expressions.push(b.build());

		return true;

	}

	public boolean visit(SimpleReference md) {

		Variable.Builder vb = Variable.newBuilder();
		vb.setName(md.getName());

		System.out.println("VAr access: " + md.toString());
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.VARACCESS);

		b.setVariable(md.getName());
		// fields.push(b.build());

		return true;

	}

	public boolean visit(VariableReference md) {

		System.out.println("VAr access: " + md.toString());
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.VARACCESS);

		b.setVariable(md.getName());
		expressions.push(b.build());

		return true;

	}

	public boolean visit(org.eclipse.dltk.ast.expressions.StringLiteral md) {

		System.out.println("Literal: " + md.toString());

		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);

		b.setLiteral(md.getValue());
		expressions.push(b.build());

		return true;

	}

	public boolean visit(ReturnStatement node) throws Exception {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.RETURN);
		if (node.getExpression() != null) {
			node.getExpression().traverse(this);
			b.addExpressions(expressions.pop());
		}
		list.add(b.build());
		return false;
	}

	public boolean visit(EmptyExpression md) throws Exception {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.EMPTY);

		expressions.push(b.build());

		return true;

	}

	public boolean visit(UnaryExpression md) throws Exception {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.UNARY);

		boa.types.Ast.Expression.Builder left = boa.types.Ast.Expression.newBuilder();
		left.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);

		left.setLiteral(md.getOperator());

		b.addExpressions(left.build());

		md.getExpression().traverse(this);

		b.addExpressions(expressions.pop());

		expressions.push(b.build());

		return true;

	}

	public boolean visit(BinaryExpression md) throws Exception {

		System.out.println("Binary Exp :  " + md.toString());

		System.out.println(md.getKind());
		System.out.println(md.getOperator());
		System.out.println(md.getLeft().getKind());
		System.out.println(md.getRight().getKind());
		System.out.println(md.getLeft().getClass().getName());
		System.out.println(md.getRight().getClass().getName());

		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();

		if (md.getKind() == ExpressionConstants.E_PLUS) {
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_ADD);

		} else if (md.getKind() == ExpressionConstants.E_MULT) {
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_MULT);
		} else if (md.getKind() == ExpressionConstants.E_MINUS) {
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_SUB);
		} else if (md.getKind() == ExpressionConstants.E_DIV) {
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_DIV);
		} else if (md.getKind() == ExpressionConstants.E_MOD) {
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_MOD);
		} else if (md.getKind() == ExpressionConstants.E_POWER) {
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_POW);
		}
		else if (md.getKind() == PythonConstants.E_INTEGER_DIV) {
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_INT_DIV);
		}
		
		else if (md.getKind() == ExpressionConstants.E_NOT_EQUAL) {
			b.setKind(boa.types.Ast.Expression.ExpressionKind.NEQ);
		} else if (md.getKind() == ExpressionConstants.E_GE) {
			b.setKind(boa.types.Ast.Expression.ExpressionKind.GTEQ);
		} else if (md.getKind() == ExpressionConstants.E_LT) {
			b.setKind(boa.types.Ast.Expression.ExpressionKind.LT);
		} else if (md.getKind() == ExpressionConstants.E_GT) {
			b.setKind(boa.types.Ast.Expression.ExpressionKind.GT);
		} else if (md.getKind() == ExpressionConstants.E_EQUAL) {
			b.setKind(boa.types.Ast.Expression.ExpressionKind.EQ);
		} else if (md.getKind() == ExpressionConstants.E_LE) {
			b.setKind(boa.types.Ast.Expression.ExpressionKind.LTEQ);
		} else if (md.getKind() == ExpressionConstants.E_IN) {
			b.setKind(boa.types.Ast.Expression.ExpressionKind.IN);
		} else if (md.getKind() == ExpressionConstants.E_NOTIN) {
			b.setKind(boa.types.Ast.Expression.ExpressionKind.NOT_IN);
		}

		else if (md.getKind() == ExpressionConstants.E_IS) {
			b.setKind(boa.types.Ast.Expression.ExpressionKind.IS);
		}
		else if (md.getKind() == ExpressionConstants.E_ISNOT) {
			b.setKind(boa.types.Ast.Expression.ExpressionKind.IS_NOT);
		}

		
		else if (md.getKind() == ExpressionConstants.E_LAND) {
			b.setKind(boa.types.Ast.Expression.ExpressionKind.LOGICAL_AND);
		} else if (md.getKind() == ExpressionConstants.E_LOR) {
			b.setKind(boa.types.Ast.Expression.ExpressionKind.LOGICAL_OR);
		} else if (md.getKind() == ExpressionConstants.E_LNOT) {
			b.setKind(boa.types.Ast.Expression.ExpressionKind.LOGICAL_NOT);
		}
		
		else if (md.getKind() == ExpressionConstants.E_BAND) {
			b.setKind(boa.types.Ast.Expression.ExpressionKind.BIT_AND);
		} else if (md.getKind() == ExpressionConstants.E_BOR) {
			b.setKind(boa.types.Ast.Expression.ExpressionKind.BIT_OR);
		} else if (md.getKind() == ExpressionConstants.E_RSHIFT) {
			b.setKind(boa.types.Ast.Expression.ExpressionKind.BIT_RSHIFT);
		} else if (md.getKind() == ExpressionConstants.E_LSHIFT) {
			b.setKind(boa.types.Ast.Expression.ExpressionKind.BIT_LSHIFT);
		} else if (md.getKind() == ExpressionConstants.E_XOR) {
			b.setKind(boa.types.Ast.Expression.ExpressionKind.BIT_XOR);
		} else if (md.getKind() == ExpressionConstants.E_BNOT) {
			b.setKind(boa.types.Ast.Expression.ExpressionKind.BIT_NOT);
		} 
		else
			
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);



		md.getLeft().traverse(this);
		b.addExpressions(expressions.pop());

		md.getRight().traverse(this);
		b.addExpressions(expressions.pop());

		expressions.push(b.build());

		return true;

	}

	public boolean visitComment(org.eclipse.dltk.ast.expressions.StringLiteral node) {
		boa.types.Ast.Comment.Builder b = boa.types.Ast.Comment.newBuilder();
		String comment = node.getValue();
		if (comment.startsWith("#")) {
			b.setKind(boa.types.Ast.Comment.CommentKind.LINE);
		} else {
			b.setKind(boa.types.Ast.Comment.CommentKind.DOC);

		}

		b.setValue(comment);

		comments.push(b.build());
		return false;
	}

	public boolean visitTypeDeclaration(TypeDeclaration node) throws Exception {
		System.out.println("Enter type declaration");

		boa.types.Ast.Declaration.Builder b = boa.types.Ast.Declaration.newBuilder();

		if (node.getRef() instanceof SimpleReference) {
			b.setName(((SimpleReference) node.getRef()).getName());
		}
		b.setKind(boa.types.Ast.TypeKind.CLASS);
//		if (node.getSuperClass() != null) {
//			Type.Builder tb = Type.newBuilder();
//			tb.setKind(TypeKind.CLASS);
//			if (node.getSuperClass() instanceof Identifier) {
//				tb.setName(((Identifier) node.getSuperClass()).getName());
//			} else {
//				node.getSuperClass().accept(this);
//				tb.setComputedName(expressions.pop());
//			}
//			b.addParents(tb.build());
//		}

		statements.push(new ArrayList<boa.types.Ast.Statement>());

		for (Object d : node.getStatements()) {

//			if (d instanceof org.eclipse.dltk.ast.expressions.StringLiteral) {
//				this.visitComment((org.eclipse.dltk.ast.expressions.StringLiteral) d);
//				b.addComments(comments.pop());
//			}
			if (d instanceof MethodDeclaration) {
				methods.push(new ArrayList<boa.types.Ast.Method>());
				((MethodDeclaration) d).traverse(this);
				for (boa.types.Ast.Method m : methods.pop()) {
					b.addMethods(m);
				}
			} else if (isDeclarationKind((ASTNode) d)) {
				declarations.push(new ArrayList<boa.types.Ast.Declaration>());
				((ASTNode) d).traverse(this);
				for (boa.types.Ast.Declaration nd : declarations.pop())
					b.addNestedDeclarations(nd);
			} else {
				((ASTNode) d).traverse(this);
				if (isExpressionStatement((ASTNode) d) == true) {
					addStatementExpression();
				}
			}
		}

		List<boa.types.Ast.Statement> ss = statements.pop();
		for (boa.types.Ast.Statement st : ss)
			b.addStatements(st);

		declarations.peek().add(b.build());

		return false;

	}

	public boolean visit(MethodDeclaration s) throws Exception {

		System.out.println("Enter Method Declaration: " + s.toString());

		List<boa.types.Ast.Method> list = methods.peek();
		Method.Builder b = Method.newBuilder();
		if (s.getRef() instanceof SimpleReference) {
			b.setName(((SimpleReference) s.getRef()).getName());
		}

		for (Object ob : s.getArguments()) {
			fields.push(new ArrayList<Variable>());
			PythonArgument ex = (PythonArgument) ob;
			ex.traverse(this);
			List<boa.types.Ast.Variable> fs = fields.pop();

			for (boa.types.Ast.Variable v : fs)
				b.addArguments(v);

		}

		if (s.getStatements() != null) {
			statements.push(new ArrayList<boa.types.Ast.Statement>());
			s.getBody().traverse(this);
			List<boa.types.Ast.Statement> ss = statements.pop();
			for (boa.types.Ast.Statement st : ss)
				b.addStatements(st);
		}

		Type.Builder tb = Type.newBuilder();
		String name = "";
		tb.setKind(boa.types.Ast.TypeKind.OTHER);
		tb.setName(name);
		b.setReturnType(tb.build());
		list.add(b.build());

		return false;
	}

	public boolean visit(Block node) throws Exception {
		Statement.Builder b = Statement.newBuilder();
		b.setKind(boa.types.Ast.Statement.StatementKind.BLOCK);
		fields.push(new ArrayList<boa.types.Ast.Variable>());
		methods.push(new ArrayList<boa.types.Ast.Method>());
		declarations.push(new ArrayList<boa.types.Ast.Declaration>());
		statements.push(new ArrayList<boa.types.Ast.Statement>());

		for (Object o : node.getStatements()) {
			((org.eclipse.dltk.ast.ASTNode) o).traverse(this);

			if (isExpressionStatement((ASTNode) o) == true) {
				addStatementExpression();
			}
		}
		List<boa.types.Ast.Statement> ss = statements.pop();
		List<boa.types.Ast.Declaration> ds = declarations.pop();
		List<boa.types.Ast.Method> ms = methods.pop();
		List<boa.types.Ast.Variable> fs = fields.pop();

		for (boa.types.Ast.Statement st : ss)
			b.addStatements(st);
		for (boa.types.Ast.Declaration d : ds)
			b.addTypeDeclarations(d);
		for (boa.types.Ast.Method m : ms)
			b.addMethods(m);
		for (boa.types.Ast.Variable v : fs)
			b.addVariableDeclarations(v);

		statements.peek().add(b.build());

		return false;
	}

	public boolean isDeclarationKind(org.eclipse.dltk.ast.ASTNode o) {
		if (o instanceof MethodDeclaration)
			return true;
		if (o instanceof TypeDeclaration)
			return true;
		if (o instanceof FieldDeclaration)
			return true;
		if (o instanceof ModuleDeclaration)
			return true;
		if (o instanceof ModuleDeclarationWrapper)
			return true;
		if (o instanceof PythonClassDeclaration)
			return true;
		if (o instanceof org.eclipse.dltk.ast.declarations.Declaration)
			return true;
		return false;
	}

	public boolean isExpressionStatement(org.eclipse.dltk.ast.ASTNode o) {
		if (isDeclarationKind(o))
			return false;
		if (o instanceof Decorator)
			return false;
		if (o instanceof SimpleStatement)
			return false;
		if (o instanceof PythonTryStatement)
			return false;
		if (o instanceof PythonExceptStatement)
			return false;
		if (o instanceof TryFinallyStatement)
			return false;
		if (o instanceof PythonForStatement)
			return false;
		if (o instanceof PythonWhileStatement)
			return false;
		if (o instanceof IfStatement)
			return false;
		if (o instanceof PythonImportFromStatement)
			return false;
//		if (o instanceof BreakStatement)
//			return false;
//		if (o instanceof ContinueStatement)
//			return false;
		if (o instanceof PythonRaiseStatement)
			return false;
//		if (o instanceof PythonDelStatement)
//			return false;
		if (o instanceof PythonAssertStatement)
			return false;
		if (o instanceof PythonWithStatement)
			return false;
		if (o instanceof EmptyStatement)
			return false;
		return true;
	}

	public void addStatementExpression() {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.EXPRESSION);
		b.addExpressions(expressions.pop());
		list.add(b.build());
	}

	public boolean visit(ModuleDeclaration s) throws Exception {
		// System.out.println("Enter Module Declaration: "+s.toString());

		statements.push(new ArrayList<boa.types.Ast.Statement>());
		declarations.push(new ArrayList<boa.types.Ast.Declaration>());
		fields.push(new ArrayList<boa.types.Ast.Variable>());
		methods.push(new ArrayList<boa.types.Ast.Method>());

		for (Object o : s.getStatements()) {
			((org.eclipse.dltk.ast.ASTNode) o).traverse(this);

			if (isExpressionStatement((ASTNode) o) == true) {
				addStatementExpression();
			}
		}
		List<boa.types.Ast.Statement> ss = statements.pop();
		List<boa.types.Ast.Declaration> ds = declarations.pop();
		List<boa.types.Ast.Method> ms = methods.pop();
		List<boa.types.Ast.Variable> fs = fields.pop();
		for (boa.types.Ast.Statement st : ss)
			b.addStatements(st);
		for (boa.types.Ast.Declaration d : ds)
			b.addDeclarations(d);
		for (boa.types.Ast.Method m : ms)
			b.addMethods(m);
		for (boa.types.Ast.Variable v : fs)
			b.addVariables(v);

		return false;
	}

	public boolean visit(PythonTryStatement s) throws Exception {
		// have to handle following
		// try-finally
		// try-except-else
		System.out.println("Enter Try: " + s.toString());

		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.TRY);
		statements.push(new ArrayList<boa.types.Ast.Statement>());

		s.getBody().traverse(this);

		for (Object c : s.getCatchFinallyStatements())
			((ASTNode) c).traverse(this);

		if (s.getfElseStatement() != null) {
			s.getfElseStatement().traverse(this);
		}

		List<boa.types.Ast.Statement> ss = statements.pop();
		for (boa.types.Ast.Statement st : ss)
			b.addStatements(st);

		list.add(b.build());

		return false;
	}

	public boolean visit(TryFinallyStatement s) throws Exception {
		System.out.println("Enter Finally : " + s.toString());

		Statement.Builder b = Statement.newBuilder();
		List<Statement> list = statements.peek();
		b.setKind(Statement.StatementKind.FINALLY);

		statements.push(new ArrayList<boa.types.Ast.Statement>());

		if (s.getfBody() != null)
			s.getfBody().traverse(this);

		List<boa.types.Ast.Statement> ss = statements.pop();
		for (boa.types.Ast.Statement st : ss)
			b.addStatements(st);

		list.add(b.build());

		return false;
	}

	public boolean visit(PythonExceptStatement s) throws Exception {
		System.out.println("Enter Except: " + s.toString());

		Statement.Builder b = Statement.newBuilder();
		List<Statement> list = statements.peek();
		b.setKind(Statement.StatementKind.CATCH);

		if (s.getExpression() != null) {
			boa.types.Ast.Variable.Builder vb = boa.types.Ast.Variable.newBuilder();

			s.getExpression().traverse(this);
			boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();

			tb.setComputedName(expressions.pop());
			tb.setKind(boa.types.Ast.TypeKind.CLASS);
			vb.setVariableType(tb.build());
			b.setVariableDeclaration(vb.build());
		}

		statements.push(new ArrayList<boa.types.Ast.Statement>());

		s.getBody().traverse(this);

		List<boa.types.Ast.Statement> ss = statements.pop();
		for (boa.types.Ast.Statement st : ss)
			b.addStatements(st);

		list.add(b.build());

		return false;
	}

	@SuppressWarnings("deprecation")
	public boolean visit(PythonForStatement s) throws Exception {
		System.out.println("Enter For: " + s.toString());

		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();

		b.setKind(boa.types.Ast.Statement.StatementKind.FOREACH);

		if (s.getfMainArguments() instanceof PythonTestListExpression) {
			for (Object ob : ((ExpressionList) s.getfMainArguments()).getExpressions()) {
				((ASTNode) ob).traverse(this);

				boa.types.Ast.Variable.Builder vb = boa.types.Ast.Variable.newBuilder();
				vb.setComputedName(expressions.pop());
				b.addVariableDeclarations(vb.build());
			}
		}

		if (s.getCondition() != null) {
			s.getCondition().traverse(this);
			boa.types.Ast.Expression ex = expressions.pop();
			b.addExpressions(ex);
		}

		statements.push(new ArrayList<boa.types.Ast.Statement>());
		s.getAction().traverse(this);
		for (boa.types.Ast.Statement ss : statements.pop())
			b.addStatements(ss);

		// Python enables else statement for the loops. The else statement is added as
		// the second block statement of FOR (similar to else in IF statement).
		if (s.getfElseStatement() != null) {
			statements.push(new ArrayList<boa.types.Ast.Statement>());
			s.getfElseStatement().traverse(this);
			for (boa.types.Ast.Statement ss : statements.pop())
				b.addStatements(ss);
		}

		list.add(b.build());

		return false;
	}

	public boolean visit(PythonWhileStatement s) throws Exception {
		System.out.println("Enter While: " + s.toString());

		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();

		b.setKind(boa.types.Ast.Statement.StatementKind.WHILE);

		if (s.getCondition() != null) {
			s.getCondition().traverse(this);
			boa.types.Ast.Expression ex = expressions.pop();
			b.addConditions(ex);
		}

		statements.push(new ArrayList<boa.types.Ast.Statement>());
		s.getAction().traverse(this);
		for (boa.types.Ast.Statement ss : statements.pop())
			b.addStatements(ss);

		// Python enables else statement for the loops. The else statement is added as
		// the second block statement of WHILE (similar to else in IF statement).
		if (s.getElseStatement() != null) {
			statements.push(new ArrayList<boa.types.Ast.Statement>());
			s.getElseStatement().traverse(this);
			for (boa.types.Ast.Statement ss : statements.pop())
				b.addStatements(ss);
		}

		list.add(b.build());

		return false;
	}

	public boolean visit(IfStatement s) throws Exception {
		System.out.println("Enter IF: " + s.toString());

		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.IF);

		if (s.getCondition() != null) {
			s.getCondition().traverse(this);
			boa.types.Ast.Expression ex = expressions.pop();
			b.addConditions(ex);
		}

		statements.push(new ArrayList<boa.types.Ast.Statement>());
		s.getThen().traverse(this);
		for (boa.types.Ast.Statement ss : statements.pop())
			b.addStatements(ss);

		if (s.getElse() != null) {
			statements.push(new ArrayList<boa.types.Ast.Statement>());
			s.getElse().traverse(this);
			for (boa.types.Ast.Statement ss : statements.pop())
				b.addStatements(ss);
		}

		list.add(b.build());

		return false;
	}

	public boolean visit(BreakStatement s) throws Exception {
		System.out.println("Enter BREAK: " + s.toString());

		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.BREAK);
		if (s.getExpression() != null) {
			s.getExpression().traverse(this);
			b.addExpressions(expressions.pop());
		}

		list.add(b.build());

		return false;
	}

	public boolean visit(ContinueStatement s) throws Exception {
		System.out.println("Enter CONTINUE: " + s.toString());

		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.CONTINUE);
		if (s.getExpression() != null) {
			s.getExpression().traverse(this);
			b.addExpressions(expressions.pop());
		}

		list.add(b.build());

		return false;
	}

	public boolean visit(PythonRaiseStatement s) throws Exception {
		System.out.println("Enter RAISE: " + s.toString());

		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.RAISE);
		if (s.getExpression1() != null) {
			s.getExpression1().traverse(this);
			b.addExpressions(expressions.pop());
		}
		if (s.getExpression2() != null) {
			s.getExpression1().traverse(this);
			b.addExpressions(expressions.pop());
		}
		if (s.getExpression3() != null) {
			s.getExpression1().traverse(this);
			b.addExpressions(expressions.pop());
		}

		list.add(b.build());

		return false;
	}

	public boolean visit(PythonDelStatement s) throws Exception {
		System.out.println("Enter DEL: " + s.toString());

		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.DEL);
		if (s.getExpression() != null) {
			s.getExpression().traverse(this);
			b.addExpressions(expressions.pop());
		}

		list.add(b.build());

		return false;
	}

	public boolean visit(PythonAssertStatement s) throws Exception {
		System.out.println("Enter Assert: " + s.toString());

		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.ASSERT);

		if (s.getfExpression1() != null) {
			s.getfExpression1().traverse(this);
			boa.types.Ast.Expression ex = expressions.pop();
			b.addConditions(ex);
		}

		if (s.getfExpression2() != null) {
			s.getfExpression2().traverse(this);
			boa.types.Ast.Expression ex = expressions.pop();
			b.addExpressions(ex);
		}

		list.add(b.build());

		return false;
	}

	public boolean visit(PythonYieldStatement s) throws Exception {
		System.out.println("Enter Yield: " + s.toString());

		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.EXPRESSION);

		boa.types.Ast.Expression.Builder ex = boa.types.Ast.Expression.newBuilder();
		ex.setKind(boa.types.Ast.Expression.ExpressionKind.YIELD);

		if (s.getExpression() != null) {
			s.getExpression().traverse(this);
			ex.addExpressions(expressions.pop());
		}

		b.addExpressions(ex);

		list.add(b.build());
		return false;
	}

	public boolean visit(PythonWithStatement s) throws Exception {
		System.out.println("Enter With: " + s.toString());

		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();

		b.setKind(boa.types.Ast.Statement.StatementKind.WITH);

		if (s.getWhat() != null) {
			s.getWhat().traverse(this);
			boa.types.Ast.Expression ex = expressions.pop();
			b.addExpressions(ex);
		}

		// Check if adding the variable name as ComputedName is okay
		if (s.getAs() != null) {
			s.getAs().traverse(this);
			boa.types.Ast.Variable.Builder vb = boa.types.Ast.Variable.newBuilder();
			vb.setComputedName(expressions.pop());
			b.addVariableDeclarations(vb.build());
		}

		statements.push(new ArrayList<boa.types.Ast.Statement>());
		s.getBlock().traverse(this);
		for (boa.types.Ast.Statement ss : statements.pop())
			b.addStatements(ss);

		list.add(b.build());
		return false;
	}

	public boolean visit(EmptyStatement s) throws Exception {
		System.out.println("Enter pass: " + s.toString());

		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();

		b.setKind(boa.types.Ast.Statement.StatementKind.EMPTY);
		list.add(b.build());

		return false;
	}
	
	public boolean visit(ExecStatement md) throws Exception {
		System.out.println("Enter exec: " + md.toString());
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();

		b.setKind(boa.types.Ast.Expression.ExpressionKind.METHODCALL);
		b.setMethod("exec");
		md.getExpression().traverse(this);
		b.addMethodArgs(expressions.pop());

		expressions.push(b.build());

		return true;

	}

}
