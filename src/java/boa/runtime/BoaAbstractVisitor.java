/*
 * Copyright 2014-2021, Hridesh Rajan, Robert Dyer,
 *                 Iowa State University of Science and Technology
 *                 and University of Nebraska Board of Regents
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package boa.runtime;

import java.util.List;

import boa.functions.BoaAstIntrinsics;
import boa.functions.BoaIntrinsics;
import boa.types.Ast.*;
import boa.types.Code.CodeRepository;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.Person;
import boa.types.Toplevel.Project;

/**
 * Boa abstract AST visitor.
 *
 * <p>The <code>visit()</code> methods first call <code>preVisit()</code> for the node.
 * If <code>preVisit()</code> returns <code>true</code>, then each of that node's children are visited and then <code>postVisit()</code> is called.
 *
 * <p>By default, all <code>preVisit()</code> methods call {@link #defaultPreVisit()} and return <code>true</code>.
 * By default, all <code>postVisit()</code> methods call {@link #defaultPostVisit()}.
 *
 * @author rdyer
 */
public abstract class BoaAbstractVisitor {
	/**
	 * Initializes any visitor-specific data before starting a visit.
	 *
	 * @return itself, to allow method chaining
	 */
	public BoaAbstractVisitor initialize() {
		return this;
	}

	/**
	 * Provides a default action for pre-visiting nodes.
	 * Any <code>preVisit()</code> method that is not overridden calls this method.
	 *
	 * @return always returns true
	 */
	protected boolean defaultPreVisit() throws Exception {
		return true;
	}

	protected boolean preVisit(final Project node) throws Exception {
		return defaultPreVisit();
	}
	protected boolean preVisit(final CodeRepository node) throws Exception {
		return defaultPreVisit();
	}
	protected boolean preVisit(final Revision node) throws Exception {
		return defaultPreVisit();
	}
	protected boolean preVisit(final ChangedFile node) throws Exception {
		return defaultPreVisit();
	}
	protected boolean preVisit(final ASTRoot node) throws Exception {
		return defaultPreVisit();
	}
	protected boolean preVisit(final Namespace node) throws Exception {
		return defaultPreVisit();
	}
	protected boolean preVisit(final Declaration node) throws Exception {
		return defaultPreVisit();
	}
	protected boolean preVisit(final Type node) throws Exception {
		return defaultPreVisit();
	}
	protected boolean preVisit(final Method node) throws Exception {
		return defaultPreVisit();
	}
	protected boolean preVisit(final Variable node) throws Exception {
		return defaultPreVisit();
	}
	protected boolean preVisit(final Statement node) throws Exception {
		return defaultPreVisit();
	}
	protected boolean preVisit(final Expression node) throws Exception {
		return defaultPreVisit();
	}
	protected boolean preVisit(final Modifier node) throws Exception {
		return defaultPreVisit();
	}
	protected boolean preVisit(final Comment node) throws Exception {
		return defaultPreVisit();
	}
	protected boolean preVisit(final Document node) throws Exception {
		return defaultPreVisit();
	}
	protected boolean preVisit(final Element node) throws Exception {
		return defaultPreVisit();
	}
	protected boolean preVisit(final Attribute node) throws Exception {
		return defaultPreVisit();
	}
	protected boolean preVisit(final Person node) throws Exception {
		return defaultPreVisit();
	}

	/**
	 * Provides a default action for post-visiting nodes.
	 * Any <code>postVisit()</code> method that is not overridden calls this method.
	 */
	protected void defaultPostVisit() throws Exception { }

	protected void postVisit(final Project node) throws Exception {
		defaultPostVisit();
	}
	protected void postVisit(final CodeRepository node) throws Exception {
		defaultPostVisit();
	}
	protected void postVisit(final Revision node) throws Exception {
		defaultPostVisit();
	}
	protected void postVisit(final ChangedFile node) throws Exception {
		defaultPostVisit();
	}
	protected void postVisit(final ASTRoot node) throws Exception {
		defaultPostVisit();
	}
	protected void postVisit(final Namespace node) throws Exception {
		defaultPostVisit();
	}
	protected void postVisit(final Declaration node) throws Exception {
		defaultPostVisit();
	}
	protected void postVisit(final Type node) throws Exception {
		defaultPostVisit();
	}
	protected void postVisit(final Method node) throws Exception {
		defaultPostVisit();
	}
	protected void postVisit(final Variable node) throws Exception {
		defaultPostVisit();
	}
	protected void postVisit(final Statement node) throws Exception {
		defaultPostVisit();
	}
	protected void postVisit(final Expression node) throws Exception {
		defaultPostVisit();
	}
	protected void postVisit(final Modifier node) throws Exception {
		defaultPostVisit();
	}
	protected void postVisit(final Comment node) throws Exception {
		defaultPostVisit();
	}
	protected void postVisit(final Document node) throws Exception {
		defaultPostVisit();
	}
	protected void postVisit(final Element node) throws Exception {
		defaultPostVisit();
	}
	protected void postVisit(final Attribute node) throws Exception {
		defaultPostVisit();
	}
	protected void postVisit(final Person node) throws Exception {
		defaultPostVisit();
	}

	public final void visit(final Project node) throws Exception {
		if (preVisit(node)) {
			final List<CodeRepository> reposList = node.getCodeRepositoriesList();
			final int reposSize = reposList.size();
			for (int i = 0; i < reposSize; i++)
				visit(reposList.get(i));

			final List<Person> devsList = node.getDevelopersList();
			final int devsSize = devsList.size();
			for (int i = 0; i < devsSize; i++)
				visit(devsList.get(i));

			final List<Person> maintsList = node.getMaintainersList();
			final int maintsSize = maintsList.size();
			for (int i = 0; i < maintsSize; i++)
				visit(maintsList.get(i));

			postVisit(node);
		}
	}
	public final void visit(final CodeRepository node) throws Exception {
		if (preVisit(node)) {
			final int revisionsSize = BoaIntrinsics.getRevisionsCount(node);
			for (int i = 0; i < revisionsSize; i++)
				visit(BoaIntrinsics.getRevision(node, i));

			postVisit(node);
		}
	}
	public final void visit(final Revision node) throws Exception {
		if (preVisit(node)) {
			final List<ChangedFile> filesList = node.getFilesList();
			final int filesSize = filesList.size();
			for (int i = 0; i < filesSize; i++)
				visit(filesList.get(i));

			if (node.hasAuthor())
				visit(node.getAuthor());

			if (node.hasCommitter())
				visit(node.getCommitter());

			postVisit(node);
		}
	}
	public final void visit(final ChangedFile node) throws Exception {
		if (preVisit(node)) {
			visit(BoaAstIntrinsics.getast(node));

			postVisit(node);
		}
	}
	public final void visit(final ASTRoot node) throws Exception {
		if (preVisit(node)) {
			final List<Namespace> namespacesList = node.getNamespacesList();
			final int namespacesSize = namespacesList.size();
			for (int i = 0; i < namespacesSize; i++)
				visit(namespacesList.get(i));

			if (node.hasDocument())
				visit(node.getDocument());

			postVisit(node);
		}
	}
	public final void visit(final Namespace node) throws Exception {
		if (preVisit(node)) {
			final List<Modifier> modifiersList = node.getModifiersList();
			final int modifiersSize = modifiersList.size();
			for (int i = 0; i < modifiersSize; i++)
				visit(modifiersList.get(i));

			final List<Declaration> declarationsList = node.getDeclarationsList();
			final int declarationsSize = declarationsList.size();
			for (int i = 0; i < declarationsSize; i++)
				visit(declarationsList.get(i));

			final List<Statement> statementsList = node.getStatementsList();
			final int statementsSize = statementsList.size();
			for (int i = 0; i < statementsSize; i++)
				visit(statementsList.get(i));

			final List<Method> methodsList = node.getMethodsList();
			final int methodsSize = methodsList.size();
			for (int i = 0; i < methodsSize; i++)
				visit(methodsList.get(i));

			final List<Expression> expressionsList = node.getExpressionsList();
			final int expressionsSize = expressionsList.size();
			for (int i = 0; i < expressionsSize; i++)
				visit(expressionsList.get(i));

			final List<Namespace> namespacesList = node.getNamespacesList();
			final int namespacesSize = namespacesList.size();
			for (int i = 0; i < namespacesSize; i++)
				visit(namespacesList.get(i));

			final List<Variable> variablesList = node.getVariablesList();
			final int variablesSize = variablesList.size();
			for (int i = 0; i < variablesSize; i++)
				visit(variablesList.get(i));

			postVisit(node);
		}
	}
	public final void visit(final Declaration node) throws Exception {
		if (preVisit(node)) {
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

			final List<Statement> statementsList = node.getStatementsList();
			final int statementsSize = statementsList.size();
			for (int i = 0; i < statementsSize; i++)
				visit(statementsList.get(i));

			postVisit(node);
		}
	}
	public final void visit(final Type node) throws Exception {
		if (preVisit(node)) {
			if (node.hasComputedName())
				visit(node.getComputedName());

			if (node.hasDelegate())
				visit(node.getDelegate());

			postVisit(node);
		}
	}
	public final void visit(final Method node) throws Exception {
		if (preVisit(node)) {
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

			if (node.hasComputedName())
				visit(node.getComputedName());

			if (node.hasExpression())
				visit(node.getExpression());

			if (node.hasReceiverType())
				visit(node.getReceiverType());

			postVisit(node);
		}
	}
	public final void visit(final Variable node) throws Exception {
		if (preVisit(node)) {
			visit(node.getVariableType());

			final List<Modifier> modifiersList = node.getModifiersList();
			final int modifiersSize = modifiersList.size();
			for (int i = 0; i < modifiersSize; i++)
				visit(modifiersList.get(i));

			if (node.hasInitializer())
				visit(node.getInitializer());

			if (node.hasComputedName())
				visit(node.getComputedName());

			final List<Expression> expressionsList = node.getExpressionsList();
			final int expressionsSize = expressionsList.size();
			for (int i = 0; i < expressionsSize; i++)
				visit(expressionsList.get(i));

			postVisit(node);
		}
	}
	public final void visit(final Statement node) throws Exception {
		if (preVisit(node)) {
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

			final List<Method> methodsList = node.getMethodsList();
			final int methodsSize = methodsList.size();
			for (int i = 0; i < methodsSize; i++)
				visit(methodsList.get(i));

			final List<Variable> variableDeclarationsList = node.getVariableDeclarationsList();
			final int variableDeclarationsSize = variableDeclarationsList.size();
			for (int i = 0; i < variableDeclarationsSize; i++)
				visit(variableDeclarationsList.get(i));

			final List<Declaration> typeDeclarationsList = node.getTypeDeclarationsList();
			final int typeDeclarationsSize = typeDeclarationsList.size();
			for (int i = 0; i < typeDeclarationsSize; i++)
				visit(typeDeclarationsList.get(i));

			postVisit(node);
		}
	}
	public final void visit(final Expression node) throws Exception {
		if (preVisit(node)) {
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

			final List<Modifier> modifiersList = node.getModifiersList();
			final int modifiersSize = modifiersList.size();
			for (int i = 0 ; i < modifiersSize; i++)
				visit(modifiersList.get(i));

			if (node.hasDeclaringType())
				visit(node.getDeclaringType());

			if (node.hasReturnType())
				visit(node.getReturnType());

			final List<Method> methodsList = node.getMethodsList();
			final int methodsSize = methodsList.size();
			for (int i = 0; i < methodsSize; i++)
				visit(methodsList.get(i));

			final List<Statement> statementsList = node.getStatementsList();
			final int statementsSize = statementsList.size();
			for (int i = 0; i < statementsSize; i++)
				visit(statementsList.get(i));

			if (node.hasComputedVariable())
				visit(node.getComputedVariable());

			if (node.hasComputedMethod())
				visit(node.getComputedMethod());

			if (node.hasTrait())
				visit(node.getTrait());

			postVisit(node);
		}
	}
	public final void visit(final Modifier node) throws Exception {
		if (preVisit(node)) {
			final List<Expression> annotationValuesList = node.getAnnotationValuesList();
			final int annotationValuesSize = annotationValuesList.size();
			for (int i = 0; i < annotationValuesSize; i++)
				visit(annotationValuesList.get(i));

			postVisit(node);
		}
	}
	public final void visit(final Comment node) throws Exception {
		if (preVisit(node)) {
			postVisit(node);
		}
	}
	public final void visit(final Document node) throws Exception {
		if (preVisit(node)) {
			final List<Element> elementsList = node.getElementsList();
			final int elementsSize = elementsList.size();
			for (int i = 0; i < elementsSize; i++)
				visit(elementsList.get(i));

			if (node.hasDocType())
				visit(node.getDocType());

			final List<Attribute> processingInstructionList = node.getProcessingInstructionList();
			final int processingInstructionSize = processingInstructionList.size();
			for (int i = 0; i < processingInstructionSize; i++)
				visit(processingInstructionList.get(i));

			postVisit(node);
		}
	}
	public final void visit(final Element node) throws Exception {
		if (preVisit(node)) {
			final List<Element> elementsList = node.getElementsList();
			final int elementsSize = elementsList.size();
			for (int i = 0; i < elementsSize; i++)
				visit(elementsList.get(i));

			final List<Attribute> attributesList = node.getAttributesList();
			final int attributesSize = attributesList.size();
			for (int i = 0; i < attributesSize; i++)
				visit(attributesList.get(i));

			if (node.hasScript())
				visit(node.getScript());

			if (node.hasPhp())
				visit(node.getPhp());

			final List<Variable> varDeclList = node.getVarDeclList();
			final int varDeclSize = varDeclList.size();
			for (int i = 0; i < varDeclSize; i++)
				visit(varDeclList.get(i));

			final List<Attribute> processingInstructionList = node.getProcessingInstructionList();
			final int processingInstructionSize = processingInstructionList.size();
			for (int i = 0; i < processingInstructionSize; i++)
				visit(processingInstructionList.get(i));

			postVisit(node);
		}
	}
	public final void visit(final Attribute node) throws Exception {
		if (preVisit(node)) {
			postVisit(node);
		}
	}
	public final void visit(final Person node) throws Exception {
		if (preVisit(node)) {
			postVisit(node);
		}
	}
}
