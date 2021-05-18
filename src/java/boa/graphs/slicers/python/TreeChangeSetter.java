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

//		Status.globalScopeNameStack.push(node.getName().replace(".", "_"));

		Namespace.Builder b = node.toBuilder();

		b.clearVariables();
		for (Variable v : node.getVariablesList()) {
			b.addVariables(visit(v));
		}

		b.clearStatements();
		for (Statement v : node.getStatementsList()) {
			b.addStatements(visit(v));
		}

		b.clearDeclarations();
		for (Declaration v : node.getDeclarationsList()) {
			b.addDeclarations(visit(v));
		}

		b.clearMethods();
		for (Method v : node.getMethodsList()) {
			b.addMethods(visit(v));
		}

//		Status.globalScopeNameStack.pop();

		return b.build();

	}

	Variable visit(Variable node) {
		Variable.Builder b = node.toBuilder();

//		if(!node.hasChange() && Status.resolvedNameMap.containsKey(node.getId()))
//			b.setChange(ChangeKind.IMPACTED);

		if (node.getComputedName() != null)
			b.setComputedName(visit(node.getComputedName()));

		if (node.getInitializer() != null)
			b.setInitializer(visit(node.getInitializer()));

		return b.build();

	}

	Statement visit(Statement node) {

		Statement.Builder b = node.toBuilder();

//		if(!node.hasChange() && Status.resolvedNameMap.containsKey(node.getId()))
//			b.setChange(ChangeKind.IMPACTED);
//		
		b.clearVariableDeclarations();
		for (Variable v : node.getVariableDeclarationsList()) {
			b.addVariableDeclarations(visit(v));
		}

		b.clearStatements();
		for (Statement v : node.getStatementsList()) {
			b.addStatements(visit(v));
		}

		b.clearTypeDeclarations();
		for (Declaration v : node.getTypeDeclarationsList()) {
			b.addTypeDeclarations(visit(v));
		}

		b.clearMethods();
		for (Method v : node.getMethodsList()) {
			b.addMethods(visit(v));
		}

		b.clearExpressions();
		for (Expression v : node.getExpressionsList()) {
			b.addExpressions(visit(v));
		}

		b.clearConditions();
		for (Expression v : node.getConditionsList()) {
			b.addConditions(visit(v));
		}

		return b.build();

	}

	Declaration visit(Declaration node) {

//		Status.globalScopeNameStack.push(node.getName().replace(".", "_"));

		Declaration.Builder b = node.toBuilder();

		b.clearStatements();
		for (Statement v : node.getStatementsList()) {
			b.addStatements(visit(v));
		}

		b.clearNestedDeclarations();
		for (Declaration v : node.getNestedDeclarationsList()) {
			b.addNestedDeclarations(visit(v));
		}

		b.clearMethods();
		for (Method v : node.getMethodsList()) {
			b.addMethods(visit(v));
		}

		b.clearParents();
		for (Type v : node.getParentsList()) {
			if (!Status.CLASS_PARENT_NAME_RESOLVE)
				b.addParents(visit(v));
			else
				b.addParents(visitClassParent(v));
		}

//		Status.globalScopeNameStack.pop();
		return b.build();
	}

	Method visit(Method node) {

//		Status.globalScopeNameStack.push(node.getName().replace(".", "_"));

		Method.Builder b = node.toBuilder();

		b.clearStatements();
		for (Statement v : node.getStatementsList()) {
			b.addStatements(visit(v));
		}

		b.clearArguments();
		for (Variable v : node.getArgumentsList()) {
			b.addArguments(visit(v));
		}

//		Status.globalScopeNameStack.pop();
		return b.build();
	}

	Type visitClassParent(Type node) {
		Type.Builder b = node.toBuilder();

		String identifierName = node.getName();
		identifierName = NameResolver.resolveImport(identifierName, null, node.getId());
		if (!identifierName.equals("")) {
			b.setName(identifierName);
		} else {
			identifierName = node.getName();
			identifierName = NameResolver.resolveObjectName(identifierName, null, node.getId());
			if (!identifierName.equals("")) {
				b.setName(identifierName);
			}
		}

		return b.build();
	}

	Type visit(Type node) {
		Type.Builder b = node.toBuilder();

//		if(!node.hasChange() && Status.resolvedNameMap.containsKey(node.getId()))
//			b.setChange(ChangeKind.IMPACTED);

		if (node.getComputedName() != null)
			b.setComputedName(visit(node.getComputedName()));

		return b.build();
	}

	Expression handleExpressionMethodCall(Expression node, String resolvedName) {
		Expression.Builder b = node.toBuilder();
		b.setMethod(resolvedName);
		b.setKind(node.getKind());

		b.clearMethodArgs();
		for (Expression v : node.getMethodArgsList()) {
			b.addMethodArgs(visit(v));
		}
		return b.build();
	}

	boolean shouldModifyChange(ChangeKind node) {
		if (node == ChangeKind.UNCHANGED || node == ChangeKind.UNKNOWN || node == ChangeKind.UNMAPPED
				|| node == ChangeKind.MOVED)
			return true;
		return false;
	}

	Expression visit(Expression node) {

		Expression.Builder b = node.toBuilder();

		boolean methodCallExpAlreadyAdded = false;

		if (Status.CRITERIA_MODE == InitialSliceCriteriaMode.CHANGE) {
			b.setChange(node.getChange());

			if (shouldModifyChange(node.getChange())) {
				if (Status.sliceSet.contains(node.getId()))
					b.setChange(ChangeKind.IMPACTED);
			}

			if (node.getKind() == ExpressionKind.METHODCALL) {
				String identifierName = ForwardSlicerUtil.convertExpressionToString(node);
				identifierName = NameResolver.resolveImport(identifierName, null, node.getId());
				if (!identifierName.equals("")) {
					b.setMethod(identifierName);
					b.clearExpressions();
					int i = 0;
					for (Expression ex : node.getExpressionsList()) {
						if (ex.getKind() == ExpressionKind.METHODCALL) {
							identifierName = ForwardSlicerUtil.convertExpressionToString(node, i);
							identifierName = NameResolver.resolveImport(identifierName, null, node.getId());
							b.addExpressions(handleExpressionMethodCall(ex, identifierName));
						} else
							b.addExpressions(visit(ex));
						i++;
					}
					methodCallExpAlreadyAdded = true;
				} else
					b.setMethod(node.getMethod());
			}
		}
		if(Status.CRITERIA_MODE==InitialSliceCriteriaMode.STATEMENT)
		{
			if (Status.sliceSet.contains(node.getId()))
				b.setChange(ChangeKind.IMPACTED);
		}
//		if(Status.resolvedNameMap.containsKey(node.getId()))
//		{
//			if(node.getKind()==ExpressionKind.METHODCALL)
//				b.setMethod(Status.resolvedNameMap.get(node.getId()));
//		}

		b.setKind(node.getKind());

		b.clearVariableDecls();
		for (Variable v : node.getVariableDeclsList()) {
			b.addVariableDecls(visit(v));
		}

		b.clearStatements();
		for (Statement v : node.getStatementsList()) {
			b.addStatements(visit(v));
		}

		b.clearMethods();
		for (Method v : node.getMethodsList()) {
			b.addMethods(visit(v));
		}

		if (methodCallExpAlreadyAdded == false) // comment this line in normal mode
		{
			b.clearExpressions();
			for (Expression v : node.getExpressionsList()) {
				b.addExpressions(visit(v));
			}
		}
		b.clearMethodArgs();
		for (Expression v : node.getMethodArgsList()) {
			b.addMethodArgs(visit(v));
		}
		return b.build();
	}

}
