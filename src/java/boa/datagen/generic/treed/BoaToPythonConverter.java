package boa.datagen.generic.treed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.dltk.ast.ASTNode;
import org.eclipse.dltk.ast.ASTVisitor;
import org.eclipse.dltk.ast.declarations.MethodDeclaration;
import org.eclipse.dltk.ast.declarations.TypeDeclaration;
import org.eclipse.dltk.ast.expressions.ExpressionList;
import org.eclipse.dltk.ast.references.SimpleReference;
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
import org.eclipse.dltk.python.parser.ast.statements.BreakStatement;
import org.eclipse.dltk.python.parser.ast.statements.ContinueStatement;
import org.eclipse.dltk.python.parser.ast.statements.EmptyStatement;
import org.eclipse.dltk.python.parser.ast.statements.ExecStatement;
import org.eclipse.dltk.python.parser.ast.statements.GlobalStatement;
import org.eclipse.dltk.python.parser.ast.statements.IfStatement;
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

public class BoaToPythonConverter {

	public final ASTNode visit(final Namespace node) throws Exception {
		
		PythonModuleDeclaration ast=new PythonModuleDeclaration(0);
		
		final List<Declaration> declarationsList = node.getDeclarationsList();
		final int declarationsSize = declarationsList.size();
		for (int i = 0; i < declarationsSize; i++)
			visit(declarationsList.get(i));

		final List<Modifier> modifiersList = node.getModifiersList();
		final int modifiersSize = modifiersList.size();
		for (int i = 0; i < modifiersSize; i++)
			visit(modifiersList.get(i));
		
		return ast;
	}

	public final ASTNode visit(final Declaration node) throws Exception {
		TypeDeclaration ast=new TypeDeclaration(node.getName(), 0, 0, 0, 0);
		
		final List<Modifier> modifiersList = node.getModifiersList();
		final int modifiersSize = modifiersList.size();
		for (int i = 0; i < modifiersSize; i++)
			visit(modifiersList.get(i));

		final List<Type> genericParamsList = node.getGenericParametersList();
		final int genericParamsSize = genericParamsList.size();
		for (int i = 0; i < genericParamsSize; i++)
			visit(genericParamsList.get(i));

		final List<Type> parentsList = node.getParentsList();
		final int parentsSize = parentsList.size();
		for (int i = 0; i < parentsSize; i++)
			visit(parentsList.get(i));

		final List<Method> methodsList = node.getMethodsList();
		final int methodsSize = methodsList.size();
		for (int i = 0; i < methodsSize; i++)
			visit(methodsList.get(i));

		final List<Variable> fieldsList = node.getFieldsList();
		final int fieldsSize = fieldsList.size();
		for (int i = 0; i < fieldsSize; i++)
			visit(fieldsList.get(i));

		final List<Declaration> nestedList = node.getNestedDeclarationsList();
		final int nestedSize = nestedList.size();
		for (int i = 0; i < nestedSize; i++)
			visit(nestedList.get(i));
		
		return ast;
	}

	public final void visit(final Type node) throws Exception {

	}

	public final ASTNode visit(final Method node) throws Exception {
		MethodDeclaration ast=new MethodDeclaration(node.getName(), 0, 0, 0, 0);
		visit(node.getReturnType());

		final List<Modifier> modifiersList = node.getModifiersList();
		final int modifiersSize = modifiersList.size();
		for (int i = 0; i < modifiersSize; i++)
			visit(modifiersList.get(i));

		final List<Type> genericParametersList = node.getGenericParametersList();
		final int genericParametersSize = genericParametersList.size();
		for (int i = 0; i < genericParametersSize; i++)
			visit(genericParametersList.get(i));

		final List<Variable> argumentsList = node.getArgumentsList();
		final int argumentsSize = argumentsList.size();
		for (int i = 0; i < argumentsSize; i++)
			visit(argumentsList.get(i));

		final List<Type> exceptionTypesList = node.getExceptionTypesList();
		final int exceptionTypesSize = exceptionTypesList.size();
		for (int i = 0; i < exceptionTypesSize; i++)
			visit(exceptionTypesList.get(i));

		final List<Statement> statementsList = node.getStatementsList();
		final int statementsSize = statementsList.size();
		for (int i = 0; i < statementsSize; i++)
			visit(statementsList.get(i));
		
		return ast;

	}

	public final ASTNode visit(final Variable node) throws Exception {
		SimpleReference ast=new SimpleReference(0, 0, node.getName());
		
		visit(node.getVariableType());

		final List<Modifier> modifiersList = node.getModifiersList();
		final int modifiersSize = modifiersList.size();
		for (int i = 0; i < modifiersSize; i++)
			visit(modifiersList.get(i));

		if (node.hasInitializer())
			visit(node.getInitializer());
		
		return ast;
	}

	public final void visit(final Statement node) throws Exception {
		final List<Statement> statementsList = node.getStatementsList();
		final int statementsSize = statementsList.size();
		for (int i = 0; i < statementsSize; i++)
			visit(statementsList.get(i));

		final List<Expression> initsList = node.getInitializationsList();
		final int initsSize = initsList.size();
		for (int i = 0; i < initsSize; i++)
			visit(initsList.get(i));

		final List<Expression> conditionsList = node.getConditionsList();
		final int conditionsSize = conditionsList.size();
		for (int i = 0; i < conditionsSize; i++)
			visit(conditionsList.get(i));

		final List<Expression> updatesList = node.getUpdatesList();
		final int updatesSize = updatesList.size();
		for (int i = 0; i < updatesSize; i++)
			visit(updatesList.get(i));

		if (node.hasVariableDeclaration())
			visit(node.getVariableDeclaration());

		if (node.hasTypeDeclaration())
			visit(node.getTypeDeclaration());

		if (node.getExpressionsCount() > 0)
			visit(node.getExpressions(0));
	}

	public final void visit(final Expression node) throws Exception {
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

	}

	public final void visit(final Modifier node) throws Exception {
		final List<Expression> annotationValuesList = node.getAnnotationValuesList();
		final int annotationValuesSize = annotationValuesList.size();
		for (int i = 0; i < annotationValuesSize; i++)
			visit(annotationValuesList.get(i));
	}
	
	public final ASTNode visitGlobalStatement(final Statement node) throws Exception {
		GlobalStatement ast=new GlobalStatement(0, 0, null);
		return ast;
	}
	public final ASTNode visitEmptyStatement(final Statement node) throws Exception {
		EmptyStatement ast=new EmptyStatement(null);
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
	
	public final ASTNode visitOtherExpression(final Expression node) throws Exception {
		ExpressionList ast=null;
		return ast;
	}
	
}
