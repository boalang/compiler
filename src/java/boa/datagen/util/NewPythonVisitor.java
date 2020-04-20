package boa.datagen.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
//
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
import org.eclipse.dltk.ast.declarations.MethodDeclaration;
import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
import org.eclipse.dltk.ast.declarations.TypeDeclaration;
import org.eclipse.dltk.ast.expressions.ExpressionConstants;
import org.eclipse.dltk.python.parser.ast.PythonModuleDeclaration;
import org.eclipse.dltk.python.parser.ast.expressions.PythonImportAsExpression;
import org.eclipse.dltk.python.parser.ast.expressions.Assignment;
import org.eclipse.dltk.python.parser.ast.expressions.BinaryExpression;
import 	org.eclipse.dltk.ast.references.VariableReference;
import org.eclipse.dltk.ast.expressions.NumericLiteral;
//import boa.types.Ast.Declaration;
//import boa.types.Ast.Expression;
//import boa.types.Ast.Method;
//import boa.types.Ast.Modifier;
//import boa.types.Ast.Namespace;
//import boa.types.Ast.PositionInfo;
//import boa.types.Ast.Statement;
//import boa.types.Ast.Statement.StatementKind;
//import boa.types.Ast.Type;
//import boa.types.Ast.TypeKind;
//import boa.types.Ast.Variable;
//import boa.types.Ast.Expression.ExpressionKind;


import boa.types.Ast.Namespace;
import boa.types.Ast.Statement;
import boa.types.Ast.Statement.StatementKind;

public class NewPythonVisitor extends ASTVisitor {
	
	private ModuleDeclaration root;
	protected Namespace.Builder b = Namespace.newBuilder();
	protected Stack<boa.types.Ast.Expression> expressions = new Stack<boa.types.Ast.Expression>();
	protected Stack<List<boa.types.Ast.Statement>> statements = new Stack<List<boa.types.Ast.Statement>>();


	
	public Namespace getNamespace(ModuleDeclaration node) throws Exception {
		root = node;
		node.traverse(this);
		return b.build();
	}
	
	
	
	@Override
	public boolean visitGeneral(ASTNode md) throws Exception {
		System.out.println("Enter General:  "+md.toString());
		
		boolean opFound=false;
		
		if (md instanceof PythonImportAsExpression) {
		  visit((PythonImportAsExpression) md);
		  //opFound=true;
		}
		else if(md instanceof Assignment)
		{
			visit((Assignment) md);
			opFound=true;
		}
		else if(md instanceof VariableReference)
		{
			visit((VariableReference) md);
			opFound=true;
		}
		else if(md instanceof BinaryExpression)
		{
			visit((BinaryExpression) md);
			opFound=true;
		}
		else if(md instanceof NumericLiteral)
		{
			visit((NumericLiteral) md);
			opFound=true;
		}

		return !opFound;
	
	}
//	
//	@Override
//    public void endvisitGeneral(ASTNode node) throws Exception {
//    	System.out.println("Exit 2:  "+node.toString());
//    }
	
	
	public boolean visit(PythonImportAsExpression md)  {
		b.setName(md.getName());
		String imp = "";
		imp += md.getName()+" as "+md.getAsName();
		
		b.addImports(imp);
		return true;
	}
	
	public boolean visit(Assignment md) throws Exception  {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();

		b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN);
		
		md.getLeft().traverse(this);
		b.addExpressions(expressions.pop());
		
		md.getRight().traverse(this);
		b.addExpressions(expressions.pop());

		expressions.push(b.build());
		
		return true;
	
	}
	public boolean visit(VariableReference md)  {

		System.out.println("VAr access: "+md.toString());
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.VARACCESS);
		
	    b.setVariable(md.getName());
		expressions.push(b.build());
		

		return true;
	
	}
	public boolean visit(NumericLiteral md)  {
		
		System.out.println("Literal: "+md.toString());

		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		
	    b.setLiteral(md.getValue());
		expressions.push(b.build());
		

		return true;
	
	}
	public boolean visit(BinaryExpression md) throws Exception  {
		
	System.out.println("Binary Exp :  "+md.toString());
		
		System.out.println(md.getOperator());
		System.out.println(md.getLeft().getKind());
		System.out.println(md.getRight().getKind());
		System.out.println(md.getLeft().getClass().getName());
		System.out.println(md.getRight().getClass().getName());
		
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		
		
		if(md.getKind()==ExpressionConstants.E_PLUS)
		{
			System.out.println("OP ADD found");
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_ADD);	
			
			md.getLeft().traverse(this);
			b.addExpressions(expressions.pop());
//			System.out.println("Back to binary exp: "+b.getExpressions(b.getExpressionsCount()-1).toString());

			md.getRight().traverse(this);
			b.addExpressions(expressions.pop());
			
//			System.out.println("Back to binary exp: "+b.getExpressions(b.getExpressionsCount()-1).toString());

			
			expressions.push(b.build());
		}
		
		

		return true;
	
	}
	

//	public boolean visit(org.eclipse.dltk.ast.statements.Statement s) throws Exception {
//		System.out.println("Enter Statement: "+s.toString());
//		return visitGeneral(s);
//	}
//
//	public boolean visit(org.eclipse.dltk.ast.expressions.Expression s) throws Exception {
//		System.out.println("Enter Expression: "+s.toString());
//		return visitGeneral(s);
//	}
//
//	public boolean visit(TypeDeclaration s) throws Exception {
//		System.out.println("Enter type declaration: "+s.toString());
//		return visitGeneral(s);
//	}
//
//	public boolean visit(MethodDeclaration s) throws Exception {
//		System.out.println("Enter Method Declaration: "+s.toString());
//		return visitGeneral(s);
//	}

	public boolean visit(ModuleDeclaration s) throws Exception {
		System.out.println("Enter Module Declaration: "+s.toString());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		return visitGeneral(s);
	}

//	public boolean endvisit(org.eclipse.dltk.ast.statements.Statement s) throws Exception {
//		System.out.println("Exit Statement: "+s.toString());
//		endvisitGeneral(s);
//		return false;
//	}
//
//	public boolean endvisit(org.eclipse.dltk.ast.expressions.Expression s) throws Exception {
//		System.out.println("Exit Expression: "+s.toString());
//		endvisitGeneral(s);
//		return false;
//	}
//
//	public boolean endvisit(TypeDeclaration s) throws Exception {
//		System.out.println("Exit Type Declaration: "+s.toString());
//		endvisitGeneral(s);
//		return false;
//	}
//
//	public boolean endvisit(MethodDeclaration s) throws Exception {
//		System.out.println("Exit Method Declaration: "+s.toString());
//		endvisitGeneral(s);
//		return false;
//	}

	public boolean endvisit(ModuleDeclaration s) throws Exception {
		System.out.println("Exit Module Declaration: "+s.toString());
		Statement.Builder sb = Statement.newBuilder();
		sb.setKind(StatementKind.EXPRESSION);

		while(!expressions.isEmpty())
		{
			sb.addExpressions(expressions.pop());
		}
		b.addStatements(sb.build());
		return false;
	}
	
	
}
