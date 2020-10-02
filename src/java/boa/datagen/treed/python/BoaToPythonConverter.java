package boa.datagen.treed.python;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.dltk.ast.ASTNode;
import org.eclipse.dltk.ast.ASTVisitor;
import org.eclipse.dltk.ast.DLTKToken;
import org.eclipse.dltk.ast.declarations.Argument;
import org.eclipse.dltk.ast.declarations.MethodDeclaration;
import org.eclipse.dltk.ast.declarations.TypeDeclaration;
import org.eclipse.dltk.ast.expressions.ExpressionConstants;
import org.eclipse.dltk.ast.expressions.ExpressionList;
import org.eclipse.dltk.ast.expressions.StringLiteral;
import org.eclipse.dltk.ast.references.SimpleReference;
import org.eclipse.dltk.ast.references.VariableReference;
import org.eclipse.dltk.ast.statements.Block;
import org.eclipse.dltk.python.parser.ast.PythonArgument;
import org.eclipse.dltk.python.parser.ast.PythonAssertStatement;
import org.eclipse.dltk.python.parser.ast.PythonConstants;
import org.eclipse.dltk.python.parser.ast.PythonDelStatement;
import org.eclipse.dltk.python.parser.ast.PythonExceptStatement;
import org.eclipse.dltk.python.parser.ast.PythonForStatement;
import org.eclipse.dltk.python.parser.ast.PythonImportFromStatement;
import org.eclipse.dltk.python.parser.ast.PythonImportStatement;
import org.eclipse.dltk.python.parser.ast.PythonModuleDeclaration;
import org.eclipse.dltk.python.parser.ast.PythonRaiseStatement;
import org.eclipse.dltk.python.parser.ast.PythonTryStatement;
import org.eclipse.dltk.python.parser.ast.PythonWhileStatement;
import org.eclipse.dltk.python.parser.ast.PythonWithStatement;
import org.eclipse.dltk.python.parser.ast.PythonYieldStatement;
import org.eclipse.dltk.python.parser.ast.expressions.Assignment;
import org.eclipse.dltk.python.parser.ast.expressions.BinaryExpression;
import org.eclipse.dltk.python.parser.ast.expressions.CallHolder;
import org.eclipse.dltk.python.parser.ast.expressions.EmptyExpression;
import org.eclipse.dltk.python.parser.ast.expressions.ExtendedVariableReference;
import org.eclipse.dltk.python.parser.ast.expressions.IndexHolder;
import org.eclipse.dltk.python.parser.ast.expressions.NotStrictAssignment;
import org.eclipse.dltk.python.parser.ast.expressions.PythonAllImportExpression;
import org.eclipse.dltk.python.parser.ast.expressions.PythonDictExpression;
import org.eclipse.dltk.python.parser.ast.expressions.PythonForListExpression;
import org.eclipse.dltk.python.parser.ast.expressions.PythonFunctionDecorator;
import org.eclipse.dltk.python.parser.ast.expressions.PythonImportAsExpression;
import org.eclipse.dltk.python.parser.ast.expressions.PythonImportExpression;
import org.eclipse.dltk.python.parser.ast.expressions.PythonLambdaExpression;
import org.eclipse.dltk.python.parser.ast.expressions.PythonListExpression;
import org.eclipse.dltk.python.parser.ast.expressions.PythonListForExpression;
import org.eclipse.dltk.python.parser.ast.expressions.PythonSetExpression;
import org.eclipse.dltk.python.parser.ast.expressions.PythonSubscriptExpression;
import org.eclipse.dltk.python.parser.ast.expressions.PythonTestListExpression;
import org.eclipse.dltk.python.parser.ast.expressions.PythonTupleExpression;
import org.eclipse.dltk.python.parser.ast.expressions.ShortHandIfExpression;
import org.eclipse.dltk.python.parser.ast.expressions.UnaryExpression;
import org.eclipse.dltk.python.parser.ast.statements.BreakStatement;
import org.eclipse.dltk.python.parser.ast.statements.ContinueStatement;
import org.eclipse.dltk.python.parser.ast.statements.EmptyStatement;
import org.eclipse.dltk.python.parser.ast.statements.ExecStatement;
import org.eclipse.dltk.python.parser.ast.statements.GlobalStatement;
import org.eclipse.dltk.python.parser.ast.statements.IfStatement;
import org.eclipse.dltk.python.parser.ast.statements.ReturnStatement;
import org.eclipse.dltk.python.parser.ast.statements.TryFinallyStatement;

import boa.functions.BoaStringIntrinsics;
import boa.graphs.slicers.python.Status;
import boa.types.Ast.ASTRoot;
import boa.types.Ast.Cell;
import boa.types.Ast.Declaration;
import boa.types.Ast.Expression;
import boa.types.Ast.Expression.ExpressionKind;
import boa.types.Ast.Method;
import boa.types.Ast.Modifier;
import boa.types.Ast.Namespace;
import boa.types.Ast.Statement;
import boa.types.Ast.Type;
import boa.types.Ast.Variable;
import boa.types.Ast.Statement.StatementKind;

public class BoaToPythonConverter {

	public final ASTNode visit(final Namespace node) throws Exception {
		
		PythonModuleDeclaration ast=new PythonModuleDeclaration(0);
		
		for (boa.types.Ast.Variable st : node.getVariablesList())
		{
			ASTNode chast=visit(st);
			if(chast!=null)
				ast.addStatement(chast);
		}
		
		for (boa.types.Ast.Statement st : node.getStatementsList())
		{
			ASTNode chast=visit(st);
			if(chast!=null)
				ast.addStatement(chast);
		}
		
		for (boa.types.Ast.Declaration st : node.getDeclarationsList())
		{
			ASTNode chast=visit(st);
			if(chast!=null)
				ast.addStatement(chast);
		}
		
		for (boa.types.Ast.Method st : node.getMethodsList())
		{
			ASTNode chast=visit(st);
			if(chast!=null)
				ast.addStatement(chast);
		}
		
		for(String imp: node.getImportsList())
		{
			if(imp.startsWith("from"))
			{
				String[] p1 = BoaStringIntrinsics.splitall(imp, "from");
				if (p1.length > 1) {
					imp = BoaStringIntrinsics.trim(p1[1]);
					
					long v = BoaStringIntrinsics.indexOf(" as ", imp);
					if (v == -1) {
						String[] p2 = BoaStringIntrinsics.splitall(imp, " ");
						PythonTestListExpression ptl=new PythonTestListExpression();
						ptl.addExpression((org.eclipse.dltk.ast.expressions.Expression) this.makeImportExpression(p2[1]));
						ast.addStatement(new PythonImportFromStatement(new DLTKToken(), 
								new VariableReference(0, 0, p2[0]), ptl));
						
						}
					} else {
						String[] p2 = BoaStringIntrinsics.splitall(imp, " ");
						PythonTestListExpression ptl=new PythonTestListExpression();
						ptl.addExpression((org.eclipse.dltk.ast.expressions.Expression) this.makeImportASExpression(p2[1],p2[3]));
						ast.addStatement(new PythonImportFromStatement(new DLTKToken(), 
								new VariableReference(0, 0, p2[0]), ptl));
					}
			}
			else
			{
				long v = BoaStringIntrinsics.indexOf(" as ", imp);
				if(v==-1)
				{
					ast.addStatement(new PythonImportStatement(new DLTKToken(), 
							(org.eclipse.dltk.ast.expressions.Expression) this.makeImportExpression(imp)));
				}
				else
					ast.addStatement(new PythonImportStatement(new DLTKToken(), 
							this.makeImportASExpression(
									BoaStringIntrinsics.substring(imp, 0, v),
									BoaStringIntrinsics.substring(imp, v + 4))));
			}
		}

		return ast;
	}
	
	public final ASTNode visit(final Declaration node) throws Exception {
		TypeDeclaration ast=new TypeDeclaration(node.getName(), 0, 0, 0, 0);
		
		for (boa.types.Ast.Type st : node.getParentsList())
		{
			ASTNode chast=visit(st);
			if(chast!=null)
				ast.addSuperClass(chast);
		}
		
		List<ASTNode> sts=new ArrayList<ASTNode>();
		for (boa.types.Ast.Statement st : node.getStatementsList())
		{
			ASTNode chast=visit(st);
			if(chast!=null)
				sts.add(chast);
		}
		
		for (boa.types.Ast.Declaration st : node.getNestedDeclarationsList())
		{
			ASTNode chast=visit(st);
			if(chast!=null)
				sts.add(chast);
		}
		
		for (boa.types.Ast.Method st : node.getMethodsList())
		{
			ASTNode chast=visit(st);
			if(chast!=null)
				sts.add(chast);
		}
		
		ast.setBody(wrapInBlock(sts));
		
		
		return ast;
	}

	public final ASTNode visit(final Type node) throws Exception {
		SimpleReference ast=new SimpleReference(0, 0, node.getName());
		return ast;
	}

	public final ASTNode visit(final Method node) throws Exception {
		MethodDeclaration ast=new MethodDeclaration(node.getName(), 0, 0, 0, 0);

		for (boa.types.Ast.Variable st : node.getArgumentsList())
		{
			ASTNode chast=visitArgument(st);
			if(chast!=null)
				ast.addArgument((Argument) chast);
		}
		
		List<ASTNode> sts=new ArrayList<ASTNode>();
		for (boa.types.Ast.Statement st : node.getStatementsList())
		{
			Block chast=(Block) visit(st);
			if(chast!=null)
				ast.acceptBody(chast);
		}
		
		return ast;
	}

	public final ASTNode visit(final Variable node) throws Exception {
		SimpleReference ast=new SimpleReference(0, 0, node.getName());
		
		return ast;
	}

	public final ASTNode visit(final Statement node) throws Exception {
		
		if(node.getKind()==StatementKind.BLOCK)
			return visitBlock(node);
		
		else if(node.getKind()==StatementKind.GLOBAL)
			return visitGlobalStatement(node);
		
		else if(node.getKind()==StatementKind.EMPTY)
			return visitEmptyStatement(node);
		
		else if(node.getKind()==StatementKind.TRY)
			return visitTryStatement(node);
		
		else if(node.getKind()==StatementKind.FINALLY)
			return visitTryFinallyStatement(node);
		
		else if(node.getKind()==StatementKind.CATCH)
			return visitExceptStatement(node);
		
		else if(node.getKind()==StatementKind.WHILE)
			return visitWhileStatement(node);
		
		else if(node.getKind()==StatementKind.IF)
			return visitIfStatement(node);
		
		else if(node.getKind()==StatementKind.BREAK)
			return visitBreakStatement(node);
		
		else if(node.getKind()==StatementKind.CONTINUE)
			return visitContinueStatement(node);
		
		else if(node.getKind()==StatementKind.RAISE)
			return visitRaiseStatement(node);
		
		else if(node.getKind()==StatementKind.DEL)
			return visitDelStatement(node);
		
		else if(node.getKind()==StatementKind.ASSERT)
			return visitAssertStatement(node);
		
		else if(node.getKind()==StatementKind.WITH)
			return visitWithStatement(node);
		
		else if(node.getKind()==StatementKind.FOREACH)
			return visitForStatement(node);
		
		else if(node.getKind()==StatementKind.RETURN)
			return visitReturnStatement(node);
		
		else if(node.getKind()==StatementKind.EXPRESSION)
			return visitExpressionStatement(node);
		
		return null;
	}
	
	public final ASTNode visitBlock(final Statement node) throws Exception {
		
		List<ASTNode> sts=new ArrayList<ASTNode>();
		
		for (boa.types.Ast.Variable st : node.getVariableDeclarationsList())
		{
			ASTNode chast=visit(st);
			if(chast!=null)
				sts.add(chast);
		}
		
		for (boa.types.Ast.Statement st : node.getStatementsList())
		{
			ASTNode chast=visit(st);
			if(chast!=null)
				sts.add(chast);
		}
		
		for (boa.types.Ast.Declaration st : node.getTypeDeclarationsList())
		{
			ASTNode chast=visit(st);
			if(chast!=null)
				sts.add(chast);
		}
		
		for (boa.types.Ast.Method st : node.getMethodsList())
		{
			ASTNode chast=visit(st);
			if(chast!=null)
				sts.add(chast);
		}
		
		return wrapInBlock(sts);
	}

	public final ASTNode visit(final Expression node) throws Exception {
		
		if(node.getKind()==ExpressionKind.METHODCALL ||
				node.getKind()==ExpressionKind.ARRAYACCESS ||
				(node.getKind()==ExpressionKind.VARACCESS && node.getExpressionsCount()>0))
			return visitExtendedExpression(node);
		
		else if(node.getKind()==ExpressionKind.VARACCESS)
			return visitReferenceExpression(node);
		
		else if(node.getKind().toString().startsWith("ASSIGN"))
			return visitAssignExpression(node);
		
		else if(node.getKind()==ExpressionKind.LITERAL)
			return visitLiteralExpression(node);
		
		else if(node.getKind()==ExpressionKind.UNARY)
			return visitUnaryExpression(node);
		
		else if(node.getKind()==ExpressionKind.LAMBDA)
			return visitLambdaExpression(node);
		
		else if(node.getKind()==ExpressionKind.EMPTY)
			return visitEmptyExpression(node);
		
		else if(node.getKind()==ExpressionKind.NEWARRAY)
			return visitListExpression(node);
		
		else if(node.getKind()==ExpressionKind.SET)
			return visitSetExpression(node);
		
		else if(node.getKind()==ExpressionKind.TUPLE)
			return visitTupleExpression(node);
		
		else if(node.getKind()==ExpressionKind.FOR_LIST)
			return visitForListExpression(node);
		
		else if(node.getKind()==ExpressionKind.ARRAY_COMPREHENSION)
			return visitListForExpression(node);
		
		else if(node.getKind()==ExpressionKind.DICT)
			return visitDictExpression(node);
		
		else if(node.getKind()==ExpressionKind.CONDITIONAL)
			return visitConditionalExpression(node);
		
		else if(node.getKind()==ExpressionKind.YIELD)
			return visitYieldExpression(node);
		
		else if(isBinaryExpressionKind(node))
			return visitBinaryExpression(node);
		
		else if(node.getKind()==ExpressionKind.OTHER)
			return visitOtherExpression(node);
		return null;

	}
	
	public final ASTNode visitGlobalStatement(final Statement node) throws Exception {
		if(node.getExpressionsCount()>0)
			return new GlobalStatement(0, 0, 
					wrapInExpressionList(node.getExpressionsList()));
		return null;
	}
	public final ASTNode visitEmptyStatement(final Statement node) throws Exception {
		EmptyStatement ast=new EmptyStatement(0, 0);
		return ast;
	}
	public final ASTNode visitWithStatement(final Statement node) throws Exception {
		PythonWithStatement ast=null;
		org.eclipse.dltk.ast.expressions.Expression asExp=null;
		org.eclipse.dltk.ast.expressions.Expression whatExp=null;
		if(node.getVariableDeclarationsCount()!=0)
		{
			if(node.getVariableDeclarations(0).getComputedName()!=null)
			{
				asExp=(org.eclipse.dltk.ast.expressions.Expression) visit(node.getVariableDeclarations(0).getComputedName());
			}
		}
		if(node.getExpressionsCount()>=1)
		{
			ASTNode chast=visit(node.getExpressions(0));
			if(chast!=null)
				whatExp=(org.eclipse.dltk.ast.expressions.Expression) chast;
		}
		org.eclipse.dltk.ast.expressions.Expression body=null;
		if(node.getStatementsCount()>=1)
		{
			ASTNode chast=visit(node.getStatements(0));
			if(chast!=null)
				body=(org.eclipse.dltk.ast.expressions.Expression) chast;
		}
		ast=new PythonWithStatement(new DLTKToken(), whatExp, asExp, (Block) body, 0, 0);
		return ast;
	}
	
	public final ASTNode visitAssertStatement(final Statement node) throws Exception {
		PythonAssertStatement ast=null;
		org.eclipse.dltk.ast.expressions.Expression exp1=null;
		org.eclipse.dltk.ast.expressions.Expression exp2=null;
		if(node.getConditionsCount()>=1)
		{
			ASTNode chast=visit(node.getConditions(0));
			if(chast!=null) {
				exp1=(org.eclipse.dltk.ast.expressions.Expression) chast;
			}
		}
		if(node.getExpressionsCount()>=1)
		{
			ASTNode chast=visit(node.getExpressions(1));
			if(chast!=null) {
				exp2=(org.eclipse.dltk.ast.expressions.Expression) chast;
			}
		}
		ast=new PythonAssertStatement(new DLTKToken(), exp1, exp2);
		return ast;
	}
	public final ASTNode visitDelStatement(final Statement node) throws Exception {
		PythonDelStatement ast=null;
		org.eclipse.dltk.ast.expressions.Expression exp1=null;
		if(node.getExpressionsCount()>=1)
		{
			ASTNode chast=visit(node.getExpressions(0));
			if(chast!=null) {
				exp1=(org.eclipse.dltk.ast.expressions.Expression) chast;
			}
		}
		ast=new PythonDelStatement(new DLTKToken(), exp1);
		return ast;
	}
	public final ASTNode visitRaiseStatement(final Statement node) throws Exception {
		PythonRaiseStatement ast=new PythonRaiseStatement(new DLTKToken());
		org.eclipse.dltk.ast.expressions.Expression exp1=null;
		org.eclipse.dltk.ast.expressions.Expression exp2=null;
		org.eclipse.dltk.ast.expressions.Expression exp3=null;
		if(node.getExpressionsCount()>=1)
		{
			ASTNode chast=visit(node.getExpressions(0));
			if(chast!=null) {
				exp1=(org.eclipse.dltk.ast.expressions.Expression) chast;
				ast.acceptExpression1(exp1);
			}
		}
		if(node.getExpressionsCount()>=2)
		{
			ASTNode chast=visit(node.getExpressions(1));
			if(chast!=null) {
				exp2=(org.eclipse.dltk.ast.expressions.Expression) chast;
				ast.acceptExpression2(exp2);
			}
		}
		if(node.getExpressionsCount()>=3)
		{
			ASTNode chast=visit(node.getExpressions(2));
			if(chast!=null) {
				exp3=(org.eclipse.dltk.ast.expressions.Expression) chast;
				ast.acceptExpression3(exp3);
			}
		}
		
		return ast;
	}
	public final ASTNode visitContinueStatement(final Statement node) throws Exception {
		ContinueStatement ast=new ContinueStatement(null, null, 0);
		return ast;
	}
	public final ASTNode visitBreakStatement(final Statement node) throws Exception {
		BreakStatement ast=new BreakStatement(null, null, 0);
		return ast;
	}
	public final ASTNode visitIfStatement(final Statement node) throws Exception {
		IfStatement ast=null;
		org.eclipse.dltk.ast.expressions.Expression cond=null;
		if(node.getConditionsCount()>=1)
		{
			ASTNode chast=visit(node.getConditions(0));
			if(chast!=null)
				cond=(org.eclipse.dltk.ast.expressions.Expression) chast;
		}
		org.eclipse.dltk.ast.expressions.Expression body=null;
		if(node.getStatementsCount()>=1)
		{
			ASTNode chast=visit(node.getStatements(0));
			if(chast!=null)
				body=(org.eclipse.dltk.ast.expressions.Expression) chast;
		}
		org.eclipse.dltk.ast.expressions.Expression elseBlock=null;
		if(node.getStatementsCount()>=2)
		{
			ASTNode chast=visit(node.getStatements(1));
			if(chast!=null)
				elseBlock=(org.eclipse.dltk.ast.expressions.Expression) chast;
		}
		ast=new IfStatement(new DLTKToken(), cond, body);
		if(elseBlock!=null)
			ast.setElse(elseBlock);
		return ast;
	}
	public final ASTNode visitWhileStatement(final Statement node) throws Exception {
		PythonWhileStatement ast=null;
		org.eclipse.dltk.ast.expressions.Expression cond=null;
		if(node.getConditionsCount()>=1)
		{
			ASTNode chast=visit(node.getConditions(0));
			if(chast!=null)
				cond=(org.eclipse.dltk.ast.expressions.Expression) chast;
		}
		org.eclipse.dltk.ast.expressions.Expression body=null;
		if(node.getStatementsCount()>=1)
		{
			ASTNode chast=visit(node.getStatements(0));
			if(chast!=null)
				body=(org.eclipse.dltk.ast.expressions.Expression) chast;
		}
		org.eclipse.dltk.ast.expressions.Expression elseBlock=null;
		if(node.getStatementsCount()>=2)
		{
			ASTNode chast=visit(node.getStatements(1));
			if(chast!=null)
				elseBlock=(org.eclipse.dltk.ast.expressions.Expression) chast;
		}
		ast=new PythonWhileStatement(new DLTKToken(), cond, body);
		if(elseBlock!=null)
			ast.setElseStatement(elseBlock);
		return ast;
	}
	public final ASTNode visitForStatement(final Statement node) throws Exception {
		PythonForStatement ast=null;
		ExpressionList mains=new ExpressionList();
		for(Variable v: node.getVariableDeclarationsList())
		{
			if(v.getComputedName()!=null)
			{
				ASTNode chast=visit(v.getComputedName());
				if(chast!=null)
				{
					org.eclipse.dltk.ast.expressions.Expression tex=
							(org.eclipse.dltk.ast.expressions.Expression) chast;
					mains.addExpression(tex);
				}
			}
		}
		org.eclipse.dltk.ast.expressions.Expression cond=null;
		if(node.getConditionsCount()>=1)
		{
			ASTNode chast=visit(node.getConditions(0));
			if(chast!=null)
				cond=(org.eclipse.dltk.ast.expressions.Expression) chast;
		}
		org.eclipse.dltk.ast.expressions.Expression body=null;
		if(node.getStatementsCount()>=1)
		{
			ASTNode chast=visit(node.getStatements(0));
			if(chast!=null)
				body=(org.eclipse.dltk.ast.expressions.Expression) chast;
		}
		org.eclipse.dltk.ast.expressions.Expression elseBlock=null;
		if(node.getStatementsCount()>=2)
		{
			ASTNode chast=visit(node.getStatements(1));
			if(chast!=null)
				elseBlock=(org.eclipse.dltk.ast.expressions.Expression) chast;
		}
		ast=new PythonForStatement(new DLTKToken(), mains, 
				cond, (Block) body);
		return ast;
	}
	public final ASTNode visitTryFinallyStatement(final Statement node) throws Exception {
		TryFinallyStatement ast=null;
		org.eclipse.dltk.ast.expressions.Expression body=null;
		if(node.getStatementsCount()>=1)
		{
			ASTNode chast=visit(node.getStatements(0));
			if(chast!=null)
				body=(org.eclipse.dltk.ast.expressions.Expression) chast;
		}
		ast=new TryFinallyStatement(new DLTKToken(), (Block) body);

		return ast;
	}
	public final ASTNode visitExceptStatement(final Statement node) throws Exception {
		PythonExceptStatement ast=null;
		org.eclipse.dltk.ast.expressions.Expression exp=null;
		org.eclipse.dltk.ast.expressions.Expression msg=null;
		if(node.getVariableDeclaration()!=null)
		{
			if(node.getVariableDeclaration().getComputedName()!=null)
			{
				exp=(org.eclipse.dltk.ast.expressions.Expression) visit(node.getVariableDeclaration().getComputedName());
			}
		}
		if(node.getExpressionsCount()>=1)
		{
			ASTNode chast=visit(node.getExpressions(0));
			if(chast!=null)
				msg=(org.eclipse.dltk.ast.expressions.Expression) chast;
		}
		org.eclipse.dltk.ast.expressions.Expression body=null;
		if(node.getStatementsCount()>=1)
		{
			ASTNode chast=visit(node.getStatements(0));
			if(chast!=null)
				body=(org.eclipse.dltk.ast.expressions.Expression) chast;
		}
		ast=new PythonExceptStatement(new DLTKToken(), exp, msg, (Block) body);
		return ast;
	}
	
	public final ASTNode visitTryStatement(final Statement node) throws Exception {
		PythonTryStatement ast=null ;
		org.eclipse.dltk.ast.expressions.Expression body=null;
		if(node.getStatementsCount()>=1)
		{
			ASTNode chast=visit(node.getStatements(0));
			if(chast!=null)
				body=(org.eclipse.dltk.ast.expressions.Expression) chast;
		}
		org.eclipse.dltk.ast.expressions.Expression elseBlock=null;
		List catches = new ArrayList();
		for(int i=1;i<node.getStatementsCount();i++)
		{
			ASTNode chast=visit(node.getStatements(i));
			if(chast!=null)
			{
				if(chast instanceof PythonExceptStatement ||
						chast instanceof TryFinallyStatement)
					catches.add(chast);
				else
					elseBlock=(org.eclipse.dltk.ast.expressions.Expression) chast;
			}
		}
		ast=new PythonTryStatement(new DLTKToken(), (Block) body, catches);
		if(elseBlock!=null)
			ast.setElseStatement(elseBlock);
		return ast;
	}
	
	public final ASTNode visitExpressionStatement(final Statement node) throws Exception {
		if(node.getExpressionsCount()==1)
			return visit(node.getExpressions(0));
		
		return null;
	}
	public final ASTNode visitReturnStatement(final Statement node) throws Exception {
		ReturnStatement ast=null;
		org.eclipse.dltk.ast.expressions.Expression exp1=null;
		org.eclipse.dltk.ast.expressions.Expression exp2=null;
		if(node.getExpressionsCount()>=1)
		{
			ASTNode chast=visit(node.getExpressions(0));
			if(chast!=null) {
				exp1=(org.eclipse.dltk.ast.expressions.Expression) chast;
			}
		}
		ast=new ReturnStatement(new DLTKToken(), exp1, 0);
		return ast;
	}
	
	public final ASTNode visitOtherExpression(final Expression node) throws Exception {
		return wrapInExpressionList(node.getExpressionsList());
	}
	public final ASTNode visitCallHolderExpression(final Expression node) throws Exception {
		if(node.getMethodArgsCount()==0)
			return new CallHolder(0, 0, new EmptyExpression());
		else
		{
			Expression l=node.getMethodArgs(0);
			if(l.getExpressionsCount()==0)
				return new CallHolder(0, 0, new EmptyExpression());
			else
				return new CallHolder(0, 0, wrapInExpressionList(l.getExpressionsList()));
		}
	}
	public final ASTNode visitYieldExpression(final Expression node) throws Exception {
		PythonYieldStatement ast=null;
		org.eclipse.dltk.ast.expressions.Expression exp1=null;
		if(node.getExpressionsCount()>=1)
		{
			ASTNode chast=visit(node.getExpressions(0));
			if(chast!=null) {
				exp1=(org.eclipse.dltk.ast.expressions.Expression) chast;
			}
		}
		ast=new PythonYieldStatement(new DLTKToken(), exp1);
		return ast;
	}
	public ASTNode makeImportExpression(String name)
	{
		if(name.equals("*")) return new PythonAllImportExpression();
		return new PythonImportExpression(new DLTKToken(0, name));
	}
	public PythonImportAsExpression makeImportASExpression(String name, String asName)
	{
		return new PythonImportAsExpression(new DLTKToken(0, name),
				new DLTKToken(0, asName));
	}
	public final ASTNode visitExtendedExpression(final Expression node) throws Exception {
		ExtendedVariableReference ast;
		boolean methodInserted=false;
		
		if(node.getExpressionsCount()==0)
		{
			 ast=new ExtendedVariableReference(new VariableReference(0, 0, node.getMethod()));
			 methodInserted=true;
		}
		else 
			ast=new ExtendedVariableReference(
				(org.eclipse.dltk.ast.expressions.Expression) visit(node.getExpressions(0)));
		
		final List<Expression> expressionsList = node.getExpressionsList();
		final int expressionsSize = expressionsList.size();
		for (int i = 1; i < expressionsSize; i++)
		{
			Expression ex=expressionsList.get(i);
			if(ex.getKind()==ExpressionKind.VARACCESS)
				ast.addExpression((org.eclipse.dltk.ast.expressions.Expression) 
						visit(ex));
			else if(ex.getKind()==ExpressionKind.METHODCALL)
			{
				ast.addExpression(new VariableReference(0, 0, ex.getMethod()));
				
				ast.addExpression((org.eclipse.dltk.ast.expressions.Expression) 
						visitCallHolderExpression(ex));
			}
			else if(ex.getKind()==ExpressionKind.ARRAYINDEX)
			{
				ast.addExpression((org.eclipse.dltk.ast.expressions.Expression) 
						visitIndexExpression(ex));
			}
		}
		
		if(node.getKind()==ExpressionKind.METHODCALL)
		{
			if(methodInserted==false)
				ast.addExpression(new VariableReference(0, 0, node.getMethod()));
			
			ast.addExpression((org.eclipse.dltk.ast.expressions.Expression) 
					visitCallHolderExpression(node));
		}
		else if(node.getKind()==ExpressionKind.VARACCESS)
		{
			ast.addExpression(new VariableReference(0, 0, node.getVariable()));
		}
		
		return ast;
	}
	public final ASTNode visitLambdaExpression(final Expression node) throws Exception {
		PythonLambdaExpression ast=null ;
		
		List<PythonArgument> arg=new ArrayList<PythonArgument>();
		for (boa.types.Ast.Variable st : node.getVariableDeclsList())
		{
			ASTNode chast=visit(st);
			if(chast!=null)
				arg.add((PythonArgument) chast);
		}
		ASTNode exp=null;
		if(node.getExpressionsCount()==1)
		{
			ASTNode chast=visit(node.getExpressions(0));
			if(chast!=null)
				exp=chast;
		}
		return new PythonLambdaExpression(new DLTKToken(), arg, (org.eclipse.dltk.ast.expressions.Expression) exp);
	}
	public final ASTNode visitDecoratorExpression(final Expression node) throws Exception {
		PythonFunctionDecorator ast=null;
		return ast;
	}
	public final ASTNode visitIndexExpression(final Expression node) throws Exception {
		if(node.getExpressionsCount()==0)
			return new IndexHolder(0, 0, new EmptyExpression());
		else
		{
			PythonSubscriptExpression ast=new PythonSubscriptExpression();
			if(node.getExpressionsCount()>0)
				ast.setTest((org.eclipse.dltk.ast.expressions.Expression) 
					visit(node.getExpressions(0)));
			
			if(node.getExpressionsCount()>1)
				ast.setCondition((org.eclipse.dltk.ast.expressions.Expression) 
					visit(node.getExpressions(1)));
			
			if(node.getExpressionsCount()>2)
				ast.setSlice((org.eclipse.dltk.ast.expressions.Expression) 
					visit(node.getExpressions(2)));
		
			return ast;
		}
	}
	public final ASTNode visitListExpression(final Expression node) throws Exception {
		PythonListExpression ast=new PythonListExpression();
		for (boa.types.Ast.Expression st : node.getExpressionsList())
		{
			ASTNode chast=visit(st);
			if(chast!=null)
				ast.addExpression((org.eclipse.dltk.ast.expressions.Expression) chast);
		}
		return ast;
	}
	public final ASTNode visitSetExpression(final Expression node) throws Exception {
		PythonSetExpression ast=new PythonSetExpression();
		for (boa.types.Ast.Expression st : node.getExpressionsList())
		{
			ASTNode chast=visit(st);
			if(chast!=null)
				ast.addExpression((org.eclipse.dltk.ast.expressions.Expression) chast);
		}
		return ast;
	}
	public final ASTNode visitTupleExpression(final Expression node) throws Exception {
		PythonTupleExpression ast=new PythonTupleExpression();
		for (boa.types.Ast.Expression st : node.getExpressionsList())
		{
			ASTNode chast=visit(st);
			if(chast!=null)
				ast.addExpression((org.eclipse.dltk.ast.expressions.Expression) chast);
		}
		return ast;
	}
	public final PythonArgument visitArgument(final Variable node) throws Exception {
		PythonArgument ast=new PythonArgument();
		ast.setArgumentName(node.getName());
		
		if(node.getInitializer()!=null)
			ast.setInitializationExpression(visit(node.getInitializer()));
		return ast;
	}
	public final ASTNode visitForListExpression(final Expression node) throws Exception {
		PythonForListExpression ast=null;
		org.eclipse.dltk.ast.expressions.Expression exp1=null;
		org.eclipse.dltk.ast.expressions.Expression exp2=null;
		if(node.getExpressionsCount()>=1)
		{
			ASTNode chast=visit(node.getExpressions(0));
			if(chast!=null)
				exp1=(org.eclipse.dltk.ast.expressions.Expression) chast;
		}
		if(node.getExpressionsCount()>=2)
		{
			ASTNode chast=visit(node.getExpressions(1));
			if(chast!=null)
				exp2=(org.eclipse.dltk.ast.expressions.Expression) chast;
		}
		ast=new PythonForListExpression(exp1,exp2);
		if(node.getExpressionsCount()>=3)
		{
			ASTNode chast=visit(node.getExpressions(2));
			if(chast!=null)
				ast.setIfListExpression((ExpressionList) chast);
		}
		return ast;
	}
	public final ASTNode visitListForExpression(final Expression node) throws Exception {
		PythonListForExpression ast=null;
		org.eclipse.dltk.ast.expressions.Expression exp=null;
		if(node.getExpressionsCount()>=1)
		{
			ASTNode chast=visit(node.getExpressions(0));
			if(chast!=null)
				exp=(org.eclipse.dltk.ast.expressions.Expression) chast;
		}
		ast=new PythonListForExpression(exp);
		for (int i=1;i<node.getExpressionsCount();i++)
		{
			ASTNode chast=visit(node.getExpressions(i));
			if(chast!=null)
				ast.addExpression((org.eclipse.dltk.ast.expressions.Expression) chast);
		}
		return ast;
	}
	public final ASTNode visitDictExpression(final Expression node) throws Exception {
		PythonDictExpression ast=new PythonDictExpression();
		for (boa.types.Ast.Expression st : node.getExpressionsList())
		{
			if(st.getExpressionsCount()==2)
			{
				ASTNode key=visit(st.getExpressions(0));
				ASTNode value=visit(st.getExpressions(1));
				ast.putExpression((org.eclipse.dltk.ast.expressions.Expression)key, (org.eclipse.dltk.ast.expressions.Expression)value);
			}
		}
		return ast;
	}
	public final ASTNode visitConditionalExpression(final Expression node) throws Exception {
		ShortHandIfExpression ast=null ;
		org.eclipse.dltk.ast.expressions.Expression exp1=null;
		org.eclipse.dltk.ast.expressions.Expression exp2=null;
		org.eclipse.dltk.ast.expressions.Expression exp3=null;
		if(node.getExpressionsCount()>=1)
		{
			ASTNode chast=visit(node.getExpressions(0));
			if(chast!=null)
				exp1=(org.eclipse.dltk.ast.expressions.Expression) chast;
		}
		if(node.getExpressionsCount()>=2)
		{
			ASTNode chast=visit(node.getExpressions(1));
			if(chast!=null)
				exp2=(org.eclipse.dltk.ast.expressions.Expression) chast;
		}
		if(node.getExpressionsCount()>=3)
		{
			ASTNode chast=visit(node.getExpressions(2));
			if(chast!=null)
				exp3=(org.eclipse.dltk.ast.expressions.Expression) chast;
		}
		ast=new ShortHandIfExpression(exp1,exp2,exp3);
		return ast;
	}
	public final ASTNode visitAssignExpression(final Expression node) throws Exception {
		if(node.getExpressionsCount()<2)
			return null;
		
		org.eclipse.dltk.ast.statements.Statement left=(org.eclipse.dltk.ast.statements.Statement) visit(node.getExpressions(0));
		org.eclipse.dltk.ast.statements.Statement right=(org.eclipse.dltk.ast.statements.Statement) visit(node.getExpressions(1));
		
		if(node.getKind()==ExpressionKind.ASSIGN)
			return new Assignment(left, right);
		else
			return new NotStrictAssignment(left, getAssignKindMapping(node), right);
		
	}
	public final ASTNode visitReferenceExpression(final Expression node) throws Exception {
		VariableReference ast=new VariableReference(0, 0, node.getVariable());
		return ast;
	}
	public final ASTNode visitLiteralExpression(final Expression node) throws Exception {
		StringLiteral ast=new StringLiteral(0, 0, node.getLiteral());
		return ast;
	}
	public final ASTNode visitEmptyExpression(final Expression node) throws Exception {
		return new EmptyExpression();
	}
	public final ASTNode visitUnaryExpression(final Expression node) throws Exception {
		if(node.getExpressionsCount()<2)
			return null;
		
		return new UnaryExpression(0, 0, getUnaryKindMapping(node.getExpressions(0)), 
				(org.eclipse.dltk.ast.statements.Statement) visit(node.getExpressions(1)));
	}
	public final ASTNode visitBinaryExpression(final Expression node) throws Exception {
		if(node.getExpressionsCount()<2)
			return null;
		
		org.eclipse.dltk.ast.statements.Statement left=(org.eclipse.dltk.ast.statements.Statement) visit(node.getExpressions(0));
		org.eclipse.dltk.ast.statements.Statement right=(org.eclipse.dltk.ast.statements.Statement) visit(node.getExpressions(1));
		
		return new BinaryExpression(left, getBinaryExpressionKindMapping(node), right);
	}
	
	public final Block wrapInBlock(List<ASTNode> l)
	{
		Block ast=new Block();
		if(l!=null)
			ast.acceptStatements(l);
		return ast;
	}
	
	public final ExpressionList wrapInExpressionList(List<Expression> l) throws Exception
	{
		ExpressionList ast=new ExpressionList();
		for (Expression e : l) {
			org.eclipse.dltk.ast.expressions.Expression ex=
					(org.eclipse.dltk.ast.expressions.Expression) visit(e);
			
			if(ex!=null)
				ast.addExpression(ex);
		}
		return ast;
	}
	
	public boolean isBinaryExpressionKind(final Expression node)
	{
		if(node.getExpressionsCount()!=2) return false;
		
		return node.getKind().toString().startsWith("OP")||
				node.getKind().toString().endsWith("EQ") ||
				node.getKind().toString().endsWith("IN") ||
				node.getKind().toString().startsWith("IS")||
				node.getKind().toString().startsWith("LOGICAL")||
				node.getKind().toString().startsWith("BIT")||
				node.getKind()==ExpressionKind.GT ||
				node.getKind()==ExpressionKind.LT ||
				node.getKind()==ExpressionKind.OTHER
				;
	}
	public int getBinaryExpressionKindMapping(final Expression md)
	{
		if (md.getKind() == ExpressionKind.OP_ADD)
			return ExpressionConstants.E_PLUS;
		
		if (md.getKind() == ExpressionKind.OP_MULT)
			return ExpressionConstants.E_MULT;
		
		if (md.getKind() == ExpressionKind.OP_SUB)
			return ExpressionConstants.E_MINUS;
		
		if (md.getKind() == ExpressionKind.OP_DIV)
			return ExpressionConstants.E_DIV;
		
		if (md.getKind() == ExpressionKind.OP_MOD)
			return ExpressionConstants.E_MOD;
		
		if (md.getKind() == ExpressionKind.OP_POW)
			return ExpressionConstants.E_POWER;
		
		if (md.getKind() == ExpressionKind.OP_INT_DIV)
			return PythonConstants.E_INTEGER_DIV;
		
		if (md.getKind() == ExpressionKind.NEQ)
			return ExpressionConstants.E_NOT_EQUAL;
		
		if (md.getKind() == ExpressionKind.GTEQ)
			return ExpressionConstants.E_GE;
		
		if (md.getKind() == ExpressionKind.LT)
			return ExpressionConstants.E_LT;
		
		if (md.getKind() == ExpressionKind.GT)
			return ExpressionConstants.E_GT;
		
		if (md.getKind() == ExpressionKind.EQ)
			return ExpressionConstants.E_EQUAL;
		
		if (md.getKind() == ExpressionKind.LTEQ)
			return ExpressionConstants.E_LE;
		
		if (md.getKind() == ExpressionKind.IN)
			return ExpressionConstants.E_IN;
		
		if (md.getKind() == ExpressionKind.NOT_IN)
			return ExpressionConstants.E_NOTIN;
		
		if (md.getKind() == ExpressionKind.IS)
			return ExpressionConstants.E_IS;
		
		if (md.getKind() == ExpressionKind.IS_NOT)
			return ExpressionConstants.E_ISNOT;
		
		if (md.getKind() == ExpressionKind.LOGICAL_AND)
			return ExpressionConstants.E_LAND;
		
		if (md.getKind() == ExpressionKind.LOGICAL_OR)
			return ExpressionConstants.E_LOR;
		
		if (md.getKind() == ExpressionKind.LOGICAL_NOT)
			return ExpressionConstants.E_LNOT;
		
		if (md.getKind() == ExpressionKind.BIT_AND)
			return ExpressionConstants.E_BAND;
		
		if (md.getKind() == ExpressionKind.BIT_OR)
			return ExpressionConstants.E_BOR;
		
		if (md.getKind() == ExpressionKind.BIT_RSHIFT)
			return ExpressionConstants.E_RSHIFT;
		
		if (md.getKind() == ExpressionKind.BIT_LSHIFT)
			return ExpressionConstants.E_LSHIFT;
		
		if (md.getKind() == ExpressionKind.BIT_XOR)
			return ExpressionConstants.E_XOR;
		
		if (md.getKind() == ExpressionKind.BIT_NOT)
			return ExpressionConstants.E_BNOT;
		
		return ExpressionConstants.E_SINGLE_ARROW;
	}
	public int getAssignKindMapping(final Expression md)
	{
		if (md.getKind() == ExpressionKind.ASSIGN_ADD)
			return ExpressionConstants.E_PLUS_ASSIGN;
		
		if (md.getKind() == ExpressionKind.ASSIGN_SUB)
			return ExpressionConstants.E_MINUS_ASSIGN;
		
		if (md.getKind() == ExpressionKind.ASSIGN_MULT)
			return ExpressionConstants.E_MULT_ASSIGN;
		
		if (md.getKind() == ExpressionKind.ASSIGN_DIV)
			return ExpressionConstants.E_DIV_ASSIGN;
		
		if (md.getKind() == ExpressionKind.ASSIGN_MOD)
			return ExpressionConstants.E_MOD_ASSIGN;
		
		if (md.getKind() == ExpressionKind.ASSIGN_POW)
			return ExpressionConstants.E_POWER_ASSIGN;
		
		if (md.getKind() == ExpressionKind.ASSIGN_INT_DIV)
			return ExpressionConstants.E_DOUBLEDIV_ASSIGN;
		
		if (md.getKind() == ExpressionKind.ASSIGN_RSHIFT)
			return ExpressionConstants.E_RSHIFT_ASSIGN;
		
		if (md.getKind() == ExpressionKind.ASSIGN_LSHIFT)
			return ExpressionConstants.E_LSHIFT_ASSIGN;
		
		if (md.getKind() == ExpressionKind.ASSIGN_BITAND)
			return ExpressionConstants.E_BAND_ASSIGN;
		
		if (md.getKind() == ExpressionKind.ASSIGN_BITOR)
			return ExpressionConstants.E_BOR_ASSIGN;
		
		if (md.getKind() == ExpressionKind.ASSIGN_BITXOR)
			return ExpressionConstants.E_BXOR_ASSIGN;
		
		return ExpressionConstants.E_ASSIGN;
	}
	public int getUnaryKindMapping(final Expression md)
	{
		if(md.getLiteral()=="+")
			return ExpressionConstants.E_PLUS;
		if(md.getLiteral()=="-")
			return ExpressionConstants.E_MINUS;
		if(md.getLiteral()=="~")
			return ExpressionConstants.E_BNOT;
		if(md.getLiteral()=="!")
			return ExpressionConstants.E_LNOT;
	
		return -1;
	}
}
