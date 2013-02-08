package sizzle.runtime;

import java.util.List;

import sizzle.functions.BoaAstIntrinsics;
import sizzle.types.Ast.*;
import sizzle.types.Code.CodeRepository;
import sizzle.types.Code.Revision;
import sizzle.types.Diff.ChangedFile;
import sizzle.types.Toplevel.Project;

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
	 */
	protected void defaultPreVisit() throws Exception { }

	protected boolean preVisit(final Project node) throws Exception {
		defaultPreVisit();
		return true;
	}
	protected boolean preVisit(final CodeRepository node) throws Exception {
		defaultPreVisit();
		return true;
	}
	protected boolean preVisit(final Revision node) throws Exception {
		defaultPreVisit();
		return true;
	}
	protected boolean preVisit(final ChangedFile node) throws Exception {
		defaultPreVisit();
		return true;
	}
	protected boolean preVisit(final ASTRoot node) throws Exception {
		defaultPreVisit();
		return true;
	}
	protected boolean preVisit(final Namespace node) throws Exception {
		defaultPreVisit();
		return true;
	}
	protected boolean preVisit(final Declaration node) throws Exception {
		defaultPreVisit();
		return true;
	}
	protected boolean preVisit(final Type node) throws Exception {
		defaultPreVisit();
		return true;
	}
	protected boolean preVisit(final Method node) throws Exception {
		defaultPreVisit();
		return true;
	}
	protected boolean preVisit(final Variable node) throws Exception {
		defaultPreVisit();
		return true;
	}
	protected boolean preVisit(final Statement node) throws Exception {
		defaultPreVisit();
		return true;
	}
	protected boolean preVisit(final Expression node) throws Exception {
		defaultPreVisit();
		return true;
	}
	protected boolean preVisit(final Modifier node) throws Exception {
		defaultPreVisit();
		return true;
	}
	protected boolean preVisit(final Comment node) throws Exception {
		defaultPreVisit();
		return true;
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

	public final void visit(final Project node) throws Exception {
		if (preVisit(node)) {
			final List<CodeRepository> reposList = node.getCodeRepositoriesList();
			for (int i = 0; i < reposList.size(); i++)
				visit(reposList.get(i));

			postVisit(node);
		}
	}
	public final void visit(final CodeRepository node) throws Exception {
		if (preVisit(node)) {
			final List<Revision> revisionsList = node.getRevisionsList();
			for (int i = 0; i < revisionsList.size(); i++)
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
			for (int i = 0; i < namespacesList.size(); i++)
				visit(namespacesList.get(i));

			postVisit(node);
		}
	}
	public final void visit(final Namespace node) throws Exception {
		if (preVisit(node)) {
			final List<Declaration> declarationsList = node.getDeclarationsList();
			for (int i = 0; i < declarationsList.size(); i++)
				visit(declarationsList.get(i));

			final List<Modifier> modifiersList = node.getModifiersList();
			for (int i = 0; i < modifiersList.size(); i++)
				visit(modifiersList.get(i));

			postVisit(node);
		}
	}
	public final void visit(final Declaration node) throws Exception {
		if (preVisit(node)) {
			final List<Modifier> modifiersList = node.getModifiersList();
			for (int i = 0; i < modifiersList.size(); i++)
				visit(modifiersList.get(i));

			final List<Type> genericParamsList = node.getGenericParametersList();
			for (int i = 0; i < genericParamsList.size(); i++)
				visit(genericParamsList.get(i));

			final List<Type> parentsList = node.getParentsList();
			for (int i = 0; i < parentsList.size(); i++)
				visit(parentsList.get(i));

			final List<Method> methodsList = node.getMethodsList();
			for (int i = 0; i < methodsList.size(); i++)
				visit(methodsList.get(i));

			final List<Variable> fieldsList = node.getFieldsList();
			for (int i = 0; i < fieldsList.size(); i++)
				visit(fieldsList.get(i));

			final List<Declaration> nestedList = node.getNestedDeclarationsList();
			for (int i = 0; i < nestedList.size(); i++)
				visit(nestedList.get(i));

			final List<Comment> commentsList = node.getCommentsList();
			for (int i = 0; i < commentsList.size(); i++)
				visit(commentsList.get(i));

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
			for (int i = 0; i < modifiersList.size(); i++)
				visit(modifiersList.get(i));

			final List<Type> genericParametersList = node.getGenericParametersList();
			for (int i = 0; i < genericParametersList.size(); i++)
				visit(genericParametersList.get(i));

			final List<Variable> argumentsList = node.getArgumentsList();
			for (int i = 0; i < argumentsList.size(); i++)
				visit(argumentsList.get(i));

			final List<Type> exceptionTypesList = node.getExceptionTypesList();
			for (int i = 0; i < exceptionTypesList.size(); i++)
				visit(exceptionTypesList.get(i));

			final List<Statement> statementsList = node.getStatementsList();
			for (int i = 0; i < statementsList.size(); i++)
				visit(statementsList.get(i));

			final List<Comment> commentsList = node.getCommentsList();
			for (int i = 0; i < commentsList.size(); i++)
				visit(commentsList.get(i));

			postVisit(node);
		}
	}
	public final void visit(final Variable node) throws Exception {
		if (preVisit(node)) {
			visit(node.getVariableType());

			final List<Modifier> modifiersList = node.getModifiersList();
			for (int i = 0; i < modifiersList.size(); i++)
				visit(modifiersList.get(i));

			if (node.hasInitializer())
				visit(node.getInitializer());

			final List<Comment> commentsList = node.getCommentsList();
			for (int i = 0; i < commentsList.size(); i++)
				visit(commentsList.get(i));

			postVisit(node);
		}
	}
	public final void visit(final Statement node) throws Exception {
		if (preVisit(node)) {
			final List<Comment> commentsList = node.getCommentsList();
			for (int i = 0; i < commentsList.size(); i++)
				visit(commentsList.get(i));

			final List<Statement> statementsList = node.getStatementsList();
			for (int i = 0; i < statementsList.size(); i++)
				visit(statementsList.get(i));

			final List<Expression> initsList = node.getInitializationsList();
			for (int i = 0; i < initsList.size(); i++)
				visit(initsList.get(i));

			if (node.hasCondition())
				visit(node.getCondition());

			final List<Expression> updatesList = node.getUpdatesList();
			for (int i = 0; i < updatesList.size(); i++)
				visit(updatesList.get(i));

			if (node.hasVariableDeclaration())
				visit(node.getVariableDeclaration());

			if (node.hasTypeDeclaration())
				visit(node.getTypeDeclaration());

			if (node.hasExpression())
				visit(node.getExpression());

			postVisit(node);
		}
	}
	public final void visit(final Expression node) throws Exception {
		if (preVisit(node)) {
			final List<Expression> expressionsList = node.getExpressionsList();
			for (int i = 0; i < expressionsList.size(); i++)
				visit(expressionsList.get(i));

			final List<Variable> varDeclsList = node.getVariableDeclsList();
			for (int i = 0; i < varDeclsList.size(); i++)
				visit(varDeclsList.get(i));

			if (node.hasNewType())
				visit(node.getNewType());

			final List<Type> genericParametersList = node.getGenericParametersList();
			for (int i = 0; i < genericParametersList.size(); i++)
				visit(genericParametersList.get(i));

			final List<Expression> methodArgsList = node.getMethodArgsList();
			for (int i = 0; i < methodArgsList.size(); i++)
				visit(methodArgsList.get(i));

			if (node.hasAnonDeclaration())
				visit(node.getAnonDeclaration());

			postVisit(node);
		}
	}
	public final void visit(final Modifier node) throws Exception {
		if (preVisit(node)) {
			final List<Expression> annotationValuesList = node.getAnnotationValuesList();
			for (int i = 0; i < annotationValuesList.size(); i++)
				visit(annotationValuesList.get(i));

			postVisit(node);
		}
	}
	public final void visit(final Comment node) throws Exception {
		if (preVisit(node)) {
			postVisit(node);
		}
	}
}
