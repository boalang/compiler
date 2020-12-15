package boa.graphs.slicers.python;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.dltk.ast.ASTNode;
import org.eclipse.dltk.ast.ASTVisitor;

import boa.runtime.BoaAbstractVisitor;
import boa.types.Ast.Declaration;
import boa.types.Ast.Expression;
import boa.types.Ast.Expression.ExpressionKind;
import boa.types.Ast.Method;
import boa.types.Ast.Namespace;
import boa.types.Ast.Statement;
import boa.types.Ast.Type;
import boa.types.Ast.Variable;
import boa.types.Shared.ChangeKind;

public class TreeChangeSetter {

	protected Namespace visit(Namespace node) {

		Namespace.Builder b=node.toBuilder();
				
		b.clearVariables();
		for(Variable v: node.getVariablesList())
		{
			b.addVariables(visit(v));
		}
		
		b.clearStatements();
		for(Statement v: node.getStatementsList())
		{
			b.addStatements(visit(v));
		}
		
		b.clearDeclarations();
		for(Declaration v: node.getDeclarationsList())
		{
			b.addDeclarations(visit(v));
		}
		
		b.clearMethods();
		for(Method v: node.getMethodsList())
		{
			b.addMethods(visit(v));
		}
		
		return b.build();

	}

	Variable visit(Variable node) {
		Variable.Builder b=node.toBuilder();
		
		if(!node.hasChange() && Status.slicedMap.containsKey(node.getId()))
			b.setChange(ChangeKind.IMPACTED);
				
		if(node.getComputedName()!=null)
			b.setComputedName(visit(node.getComputedName()));
		
		if(node.getInitializer()!=null)
			b.setInitializer(visit(node.getInitializer()));
		
		return b.build();

	}

	Statement visit(Statement node) {

		Statement.Builder b=node.toBuilder(); 
		
		if(!node.hasChange() && Status.slicedMap.containsKey(node.getId()))
			b.setChange(ChangeKind.IMPACTED);
		
		b.clearVariableDeclarations();
		for(Variable v: node.getVariableDeclarationsList())
		{
			b.addVariableDeclarations(visit(v));
		}
		
		b.clearStatements();
		for(Statement v: node.getStatementsList())
		{
			b.addStatements(visit(v));
		}
		
		b.clearTypeDeclarations();
		for(Declaration v: node.getTypeDeclarationsList())
		{
			b.addTypeDeclarations(visit(v));
		}
		
		b.clearMethods();
		for(Method v: node.getMethodsList())
		{
			b.addMethods(visit(v));
		}
		
		b.clearExpressions();
		for(Expression v: node.getExpressionsList())
		{
			b.addExpressions(visit(v));
		}
		
		b.clearConditions();
		for(Expression v: node.getConditionsList())
		{
			b.addConditions(visit(v));
		}
		
		return b.build();

	}

	Declaration visit(Declaration node) {

		Declaration.Builder b=node.toBuilder(); 
		
		b.clearStatements();
		for(Statement v: node.getStatementsList())
		{
			b.addStatements(visit(v));
		}
		
		b.clearNestedDeclarations();
		for(Declaration v: node.getNestedDeclarationsList())
		{
			b.addNestedDeclarations(visit(v));
		}
		
		b.clearMethods();
		for(Method v: node.getMethodsList())
		{
			b.addMethods(visit(v));
		}
		
		b.clearParents();
		for(Type v: node.getParentsList())
		{
			b.addParents(visit(v));
		}
		return b.build();
	}

	Method visit(Method node) {
	
		Method.Builder b=node.toBuilder(); 
		
		b.clearStatements();
		for(Statement v: node.getStatementsList())
		{
			b.addStatements(visit(v));
		}
		
		b.clearArguments();
		for(Variable v: node.getArgumentsList())
		{
			b.addArguments(visit(v));
		}
		return b.build();
	}

	Type visit(Type node) {
		Type.Builder b=node.toBuilder(); 
		
		if(!node.hasChange() && Status.slicedMap.containsKey(node.getId()))
			b.setChange(ChangeKind.IMPACTED);
		
		if(node.getComputedName()!=null)
			b.setComputedName(visit(node.getComputedName()));
		
		return b.build();
	}

	Expression visit(Expression node) {

		Expression.Builder b=node.toBuilder(); 
		
		if(node.getKind()==ExpressionKind.METHODCALL)
		{
//			System.out.println(node.getMethod()+" "+Status.slicedMap.containsKey(node.getId())+" "+(node.hasChange()?node.getChange().toString(): ""));
		}
		if(Status.slicedMap.containsKey(node.getId()))
		{
			if(node.getKind()==ExpressionKind.METHODCALL)
				b.setMethod(Status.slicedMap.get(node.getId()));
			if(!node.hasChange())
			{
				b.setChange(ChangeKind.IMPACTED);
			}
		}
		
		b.setKind(node.getKind());
		
		b.clearVariableDecls();
		for(Variable v: node.getVariableDeclsList())
		{
			b.addVariableDecls(visit(v));
		}
		
		b.clearStatements();
		for(Statement v: node.getStatementsList())
		{
			b.addStatements(visit(v));
		}
		
		
		b.clearMethods();
		for(Method v: node.getMethodsList())
		{
			b.addMethods(visit(v));
		}
		
		b.clearExpressions();
		for(Expression v: node.getExpressionsList())
		{
			b.addExpressions(visit(v));
		}
		b.clearMethodArgs();
		for(Expression v: node.getMethodArgsList())
		{
			b.addMethodArgs(visit(v));
		}
		return b.build();
	}

}
