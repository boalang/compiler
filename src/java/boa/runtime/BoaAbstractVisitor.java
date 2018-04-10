/*
 * Copyright 2014, Hridesh Rajan, Robert Dyer, 
 *                 and Iowa State University of Science and Technology
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

import boa.types.Ast.*;
import boa.types.Code.CodeRepository;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.Person;
import boa.types.Toplevel.Project;

/**
 * Boa abstract AST visitor.
 * 
 * The <code>visit()</code> methods first call <code>preVisit()</code> for the node.
 * If <code>preVisit()</code> returns <code>true</code>, then each of that node's children are visited and then <code>postVisit()</code> is called.
 * 
 * By default, all <code>preVisit()</code> methods call {@link #defaultPreVisit()} and return <code>true</code>.
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
			final List<Revision> revisionsList = node.getRevisionsList();
			final int revisionsSize = revisionsList.size();
			for (int i = 0; i < revisionsSize; i++)
				visit(revisionsList.get(i));

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

			postVisit(node);
		}
	}
	public final void visit(final Namespace node) throws Exception {
		if (preVisit(node)) {
			final List<Declaration> declarationsList = node.getDeclarationsList();
			final int declarationsSize = declarationsList.size();
			for (int i = 0; i < declarationsSize; i++)
				visit(declarationsList.get(i));

			final List<Modifier> modifiersList = node.getModifiersList();
			final int modifiersSize = modifiersList.size();
			for (int i = 0; i < modifiersSize; i++)
				visit(modifiersList.get(i));

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

			postVisit(node);
		}
	}
	public final void visit(final Type node) throws Exception {
		if (preVisit(node)) {
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

			postVisit(node);
		}
	}
	public final void visit(final Statement node) throws Exception {
		if (preVisit(node)) {
            if (node.hasStatement1())
                visit(node.getStatement1());
            if (node.hasStatement2())
                visit(node.getStatement2());

			final List<Statement> statements1List = node.getStatements1List();
			final int statements1Size = statements1List.size();
			for (int i = 0; i < statements1Size; i++)
				visit(statements1List.get(i));
			final List<Statement> statements2List = node.getStatements2List();
			final int statements2Size = statements2List.size();
			for (int i = 0; i < statements2Size; i++)
				visit(statements2List.get(i));

            if (node.hasExpression1())
                visit(node.getExpression1());
            if (node.hasExpression2())
                visit(node.getExpression2());

			final List<Expression> exps1List = node.getExpressions1List();
			final int exps1Size = exps1List.size();
			for (int i = 0; i < exps1Size; i++)
				visit(exps1List.get(i));
			final List<Expression> exps2List = node.getExpressions2List();
			final int exps2Size = exps2List.size();
			for (int i = 0; i < exps2Size; i++)
				visit(exps2List.get(i));

            if (node.hasVariable1())
                visit(node.getVariable1());

            if (node.hasDeclaration1())
                visit(node.getDeclaration1());

			postVisit(node);
		}
	}
	public final void visit(final Expression node) throws Exception {
		if (preVisit(node)) {
            if (node.hasExpression1())
                visit(node.getExpression1());
            if (node.hasExpression2())
                visit(node.getExpression2());
            if (node.hasExpression3())
                visit(node.getExpression3());

			final List<Expression> expressions1List = node.getExpressions1List();
			final int expressions1Size = expressions1List.size();
			for (int i = 0; i < expressions1Size; i++)
				visit(expressions1List.get(i));

            if (node.hasType1())
                visit(node.getType1());

			final List<Type> types1List = node.getTypes1List();
			final int types1Size = types1List.size();
			for (int i = 0; i < types1Size; i++)
				visit(types1List.get(i));

            if (node.hasDeclaration1())
                visit(node.getDeclaration1());

			final List<Variable> variables1List = node.getVariables1List();
			final int variables1Size = variables1List.size();
			for (int i = 0; i < variables1Size; i++)
				visit(variables1List.get(i));

            if (node.hasStatement1())
                visit(node.getStatement1());

			final List<Modifier> modifiers1List = node.getModifiers1List();
			final int modifiers1Size = modifiers1List.size();
			for (int i = 0; i < modifiers1Size; i++)
				visit(modifiers1List.get(i));

            if (node.hasModifier1())
                visit(node.getModifier1());

			postVisit(node);
		}
	}
	public final void visit(final Modifier node) throws Exception {
		if (preVisit(node)) {
            if (node.hasExpression1())
                visit(node.getExpression1());

			final List<Expression> expressions1List = node.getExpressions1List();
			final int expressions1Size = expressions1List.size();
			for (int i = 0; i < expressions1Size; i++)
				visit(expressions1List.get(i));

			postVisit(node);
		}
	}
	public final void visit(final Comment node) throws Exception {
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
