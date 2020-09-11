package boa.datagen.generic.treed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.dltk.ast.ASTNode;
import org.eclipse.dltk.ast.ASTVisitor;
import org.eclipse.dltk.ast.declarations.MethodDeclaration;
import org.eclipse.dltk.ast.declarations.TypeDeclaration;
import org.eclipse.dltk.ast.expressions.ExpressionList;
import org.eclipse.dltk.ast.expressions.StringLiteral;
import org.eclipse.dltk.ast.references.SimpleReference;
import org.eclipse.dltk.ast.references.VariableReference;
import org.eclipse.dltk.ast.statements.Block;
import org.eclipse.dltk.python.parser.ast.PythonArgument;
import org.eclipse.dltk.python.parser.ast.PythonAssertStatement;
import org.eclipse.dltk.python.parser.ast.PythonDelStatement;
import org.eclipse.dltk.python.parser.ast.PythonExceptStatement;
import org.eclipse.dltk.python.parser.ast.PythonForStatement;
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

import boa.types.Ast.ASTRoot;
import boa.types.Ast.Cell;
import boa.types.Ast.Declaration;
import boa.types.Ast.Expression;
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
			ast.addStatement(visit(st));
		}
		
		for (boa.types.Ast.Statement st : node.getStatementsList())
		{
			ast.addStatement(visit(st));
		}
		
		for (boa.types.Ast.Declaration st : node.getDeclarationsList())
		{
			ast.addStatement(visit(st));
		}
		
		for (boa.types.Ast.Method st : node.getMethodsList())
		{
			ast.addStatement(visit(st));
		}

		return ast;
	}

	public final ASTNode visit(final Declaration node) throws Exception {
		TypeDeclaration ast=new TypeDeclaration(node.getName(), 0, 0, 0, 0);
		
		for (boa.types.Ast.Type st : node.getParentsList())
		{
			ast.addSuperClass(visit(st));
		}
		
		List<ASTNode> sts=new ArrayList<ASTNode>();
		for (boa.types.Ast.Statement st : node.getStatementsList())
		{
			sts.add(visit(st));
		}
		
		for (boa.types.Ast.Declaration st : node.getNestedDeclarationsList())
		{
			sts.add(visit(st));
		}
		
		for (boa.types.Ast.Method st : node.getMethodsList())
		{
			sts.add(visit(st));
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
			ast.addArgument(visitArgument(st));
		}
		
		List<ASTNode> sts=new ArrayList<ASTNode>();
		for (boa.types.Ast.Statement st : node.getStatementsList())
		{
			ast.acceptBody((Block) visit(st));
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
		if(node.getKind()==StatementKind.GLOBAL)
			return visitGlobalStatement(node);
		if(node.getKind()==StatementKind.EMPTY)
			return visitEmptyStatement(node);
		
		return null;
	}
	
	public final ASTNode visitBlock(final Statement node) throws Exception {
		
		List<ASTNode> sts=new ArrayList<ASTNode>();
		
		for (boa.types.Ast.Variable st : node.getVariableDeclarationsList())
		{
			sts.add(visit(st));
		}
		
		for (boa.types.Ast.Statement st : node.getStatementsList())
		{
			sts.add(visit(st));
		}
		
		for (boa.types.Ast.Declaration st : node.getTypeDeclarationsList())
		{
			sts.add(visit(st));
		}
		
		for (boa.types.Ast.Method st : node.getMethodsList())
		{
			sts.add(visit(st));
		}
		
		return wrapInBlock(sts);
	}

	public final ASTNode visit(final Expression node) throws Exception {
		final List<Expression> expressionsList = node.getExpressionsList();
		final int expressionsSize = expressionsList.size();
		for (int i = 0; i < expressionsSize; i++)
			visit(expressionsList.get(i));

		final List<Variable> varDeclsList = node.getVariableDeclsList();
		final int varDeclsSize = varDeclsList.size();
		for (int i = 0; i < varDeclsSize; i++)
			visit(varDeclsList.get(i));

		if (node.hasNewType())
			visit(node.getNewType());

		final List<Type> genericParametersList = node.getGenericParametersList();
		final int genericParametersSize = genericParametersList.size();
		for (int i = 0; i < genericParametersSize; i++)
			visit(genericParametersList.get(i));

		final List<Expression> methodArgsList = node.getMethodArgsList();
		final int methodArgsSize = methodArgsList.size();
		for (int i = 0; i < methodArgsSize; i++)
			visit(methodArgsList.get(i));

		if (node.hasAnonDeclaration())
			visit(node.getAnonDeclaration());
		
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
		PythonWithStatement ast=new PythonWithStatement(null, null, null, null, 0, 0);
		return ast;
	}
	public final ASTNode visitYieldStatement(final Statement node) throws Exception {
		PythonYieldStatement ast=new PythonYieldStatement(null, null);
		return ast;
	}
	public final ASTNode visitAssertStatement(final Statement node) throws Exception {
		PythonAssertStatement ast=new PythonAssertStatement(null,null,null);
		return ast;
	}
	public final ASTNode visitDelStatement(final Statement node) throws Exception {
		PythonDelStatement ast=new PythonDelStatement(null,null);
		return ast;
	}
	public final ASTNode visitRaiseStatement(final Statement node) throws Exception {
		PythonRaiseStatement ast=new PythonRaiseStatement(null);
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
		return ast;
	}
	public final ASTNode visitWhileStatement(final Statement node) throws Exception {
		PythonWhileStatement ast=null;
		return ast;
	}
	public final ASTNode visitForStatement(final Statement node) throws Exception {
		PythonForStatement ast=null;
		return ast;
	}
	public final ASTNode visitTryFinallyStatement(final Statement node) throws Exception {
		TryFinallyStatement ast=null;
		return ast;
	}
	public final ASTNode visitExceptStatement(final Statement node) throws Exception {
		PythonExceptStatement ast=null;
		return ast;
	}
	public final ASTNode visitTryStatement(final Statement node) throws Exception {
		PythonTryStatement ast=null;
		return ast;
	}
	public final ASTNode visitExpressionStatement(final Statement node) throws Exception {
		return null;
	}
	public final ASTNode visitReturnStatement(final Statement node) throws Exception {
		ReturnStatement ast=null;
		return ast;
	}
	
	
	public final ASTNode visitOtherExpression(final Expression node) throws Exception {
		ExpressionList ast=null;
		return ast;
	}
	public final ASTNode visitCallHolderExpression(final Expression node) throws Exception {
		CallHolder ast=null;
		return ast;
	}
	public final ASTNode visitExtendedExpression(final Expression node) throws Exception {
		ExtendedVariableReference ast=null;
		return ast;
	}
	public final ASTNode visitLambdaExpression(final Expression node) throws Exception {
		PythonLambdaExpression ast=null;
		return ast;
	}
	public final ASTNode visitDecoratorExpression(final Expression node) throws Exception {
		PythonFunctionDecorator ast=null;
		return ast;
	}
	public final ASTNode visitIndexExpression(final Expression node) throws Exception {
		IndexHolder ast=null;
		PythonSubscriptExpression ast2=null;
		return ast;
	}
	public final ASTNode visitListExpression(final Expression node) throws Exception {
		PythonListExpression ast=null;
		return ast;
	}
	public final ASTNode visitSetExpression(final Expression node) throws Exception {
		PythonSetExpression ast=null;
		return ast;
	}
	public final ASTNode visitTupleExpression(final Expression node) throws Exception {
		PythonTupleExpression ast=null;
		return ast;
	}
	public final PythonArgument visitArgument(final Variable node) throws Exception {
		PythonArgument ast=new PythonArgument();
		ast.setArgumentName(node.getName());
		ast.setInitializationExpression(visit(node.getInitializer()));
		return ast;
	}
	public final ASTNode visitForListExpression(final Expression node) throws Exception {
		PythonForListExpression ast=null;
		return ast;
	}
	public final ASTNode visitListForExpression(final Expression node) throws Exception {
		PythonListForExpression ast=null;
		return ast;
	}
	public final ASTNode visitDictExpression(final Expression node) throws Exception {
		PythonDictExpression ast=null;
		return ast;
	}
	public final ASTNode visitConditionalExpression(final Expression node) throws Exception {
		ShortHandIfExpression ast=null;
		return ast;
	}
	public final ASTNode visitAssignExpression(final Expression node) throws Exception {
		Assignment ast=null;
		return ast;
	}
	public final ASTNode visitNotStrictAssignExpression(final Expression node) throws Exception {
		NotStrictAssignment ast=null;
		return ast;
	}
	public final ASTNode visitReferenceExpression(final Expression node) throws Exception {
		VariableReference ast=null;
		return ast;
	}
	public final ASTNode visitLiteralExpression(final Expression node) throws Exception {
		StringLiteral ast=null;
		return ast;
	}
	public final ASTNode visitEmptyExpression(final Expression node) throws Exception {
		EmptyExpression ast=null;
		return ast;
	}
	public final ASTNode visitUnaryExpression(final Expression node) throws Exception {
		UnaryExpression ast=null;
		return ast;
	}
	public final ASTNode visitBinaryExpression(final Expression node) throws Exception {
		BinaryExpression ast=null;
		return ast;
	}
	
	public final Block wrapInBlock(List<ASTNode> l)
	{
		Block ast=new Block();
		ast.acceptStatements(l);
		return ast;
	}
	
	public final ExpressionList wrapInExpressionList(List<Expression> l) throws Exception
	{
		ExpressionList ast=new ExpressionList();
		for (Expression e : l) {
			org.eclipse.dltk.ast.expressions.Expression ex=
					(org.eclipse.dltk.ast.expressions.Expression) visit(e);
			
			ast.addExpression(ex);
		}
		return ast;
	}
}
