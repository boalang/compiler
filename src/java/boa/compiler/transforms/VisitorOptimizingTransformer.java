/*
 * Copyright 2014-2023, Hridesh Rajan, Robert Dyer,
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
package boa.compiler.transforms;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import boa.compiler.ast.Component;
import boa.compiler.ast.Identifier;
import boa.compiler.ast.expressions.VisitorExpression;
import boa.compiler.ast.statements.Block;
import boa.compiler.ast.statements.Statement;
import boa.compiler.ast.statements.StopStatement;
import boa.compiler.ast.statements.VisitStatement;
import boa.compiler.visitors.AbstractVisitorNoArgNoRet;
import boa.compiler.visitors.TypeCheckingVisitor;
import boa.types.BoaType;
import boa.types.proto.*;

/**
 * Optimizes a visitor by adding stop statements if the visitor doesn't look at
 * Revision or AST nodes.  This avoids calling getrevision()/getast(), saving a
 * significant amount of time.
 *
 * @author rdyer
 */
public class VisitorOptimizingTransformer extends AbstractVisitorNoArgNoRet {
	protected final static Set<Class<? extends BoaType>> astTypes = new HashSet<Class<? extends BoaType>>();
	protected final static Set<Class<? extends BoaType>> revTypes = new HashSet<Class<? extends BoaType>>();

	static {
		astTypes.addAll(new ASTRootProtoTuple().reachableTypes());
		revTypes.addAll(new RevisionProtoTuple().reachableTypes());
	}

	protected final static VariableRenameTransformer argumentRenamer = new VariableRenameTransformer();
	protected final static VariableDeclRenameTransformer declRenamer = new VariableDeclRenameTransformer();

	protected Set<Class<? extends BoaType>> types;
	protected final Stack<Set<Class<? extends BoaType>>> typeStack = new Stack<Set<Class<? extends BoaType>>>();

	protected VisitStatement beforeChangedFile;
	protected final Stack<VisitStatement> beforeChangedFileStack = new Stack<VisitStatement>();

	protected VisitStatement afterChangedFile;
	protected final Stack<VisitStatement> afterChangedFileStack = new Stack<VisitStatement>();

	protected VisitStatement beforeCodeRepository;
	protected final Stack<VisitStatement> beforeCodeRepositoryStack = new Stack<VisitStatement>();

	protected VisitStatement afterCodeRepository;
	protected final Stack<VisitStatement> afterCodeRepositoryStack = new Stack<VisitStatement>();

	/** {@inheritDoc} */
	@Override
	protected void initialize() {
		types = new HashSet<Class<? extends BoaType>>();
		beforeChangedFile = afterChangedFile = null;
		beforeCodeRepository = afterCodeRepository = null;

		typeStack.clear();
		beforeChangedFileStack.clear();
		afterChangedFileStack.clear();
		beforeCodeRepositoryStack.clear();
		afterCodeRepositoryStack.clear();
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VisitorExpression n) {
		typeStack.push(types);
		beforeChangedFileStack.push(beforeChangedFile);
		afterChangedFileStack.push(afterChangedFile);
		beforeCodeRepositoryStack.push(beforeCodeRepository);
		afterCodeRepositoryStack.push(afterCodeRepository);

		types = new HashSet<Class<? extends BoaType>>();
		beforeChangedFile = afterChangedFile = null;

		n.getBody().accept(this);

		// if the visitor doesnt use an AST type, we can enforce
		// a stop at the lowest level visited
		final Set<Class<? extends BoaType>> hasAst = new HashSet<Class<? extends BoaType>>(types);
		final Set<Class<? extends BoaType>> hasRevision = new HashSet<Class<? extends BoaType>>(types);
		hasAst.retainAll(astTypes);
		hasRevision.retainAll(revTypes);
		if (hasRevision.isEmpty()) {
			beforeCodeRepository = insertStop(n, beforeCodeRepository, afterCodeRepository, "CodeRepository");
		} else if (hasAst.isEmpty()) {
			beforeChangedFile = insertStop(n, beforeChangedFile, afterChangedFile, "ChangedFile");
		}

		types = typeStack.pop();
		beforeChangedFile = beforeChangedFileStack.pop();
		afterChangedFile = afterChangedFileStack.pop();
		beforeCodeRepository = beforeCodeRepositoryStack.pop();
		afterCodeRepository = afterCodeRepositoryStack.pop();
	}

	private VisitStatement insertStop(final VisitorExpression n, VisitStatement beforeStatement, final VisitStatement afterStatement, final String statementKind) {
		// if the before's last statement isnt a stop, merge in the after and add a stop
		if (beforeStatement == null
				|| beforeStatement.getBody().getStatementsSize() == 0
				|| !(beforeStatement.getBody().getStatement(beforeStatement.getBody().getStatementsSize() - 1) instanceof StopStatement)) {
			if (beforeStatement == null) {
				final String id;
				if (afterStatement != null && afterStatement.hasComponent())
					id = afterStatement.getComponent().getIdentifier().getToken();
				else
					id = "_n";

				beforeStatement = new VisitStatement(true, new Component(new Identifier(id), new Identifier(statementKind)), new Block());
				TypeCheckingVisitor.instance.start(beforeStatement, n.env);

				n.getBody().addStatement(beforeStatement);
			}

			if (afterStatement != null) {
				argumentRenamer.start(beforeStatement);
				argumentRenamer.start(afterStatement);

				for (final Statement s : afterStatement.getBody().getStatements()) {
					final Statement s2 = s.clone();
					beforeStatement.getBody().addStatement(s2);
					TypeCheckingVisitor.instance.start(s2, beforeStatement.getBody().env);
				}
				declRenamer.start(beforeStatement);
			}

			beforeStatement.getBody().addStatement(new StopStatement());
		}

		return beforeStatement;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VisitStatement n) {
		if (n.hasWildcard())
			logType(n, new ASTRootProtoTuple());
		else if (n.hasComponent())
			logType(n, n.getComponent().getType().type);
		else
			for (final Identifier id : n.getIdList())
				logType(n, id.type);

		super.visit(n);
	}

	protected void logType(final VisitStatement n, final BoaType t) {
		types.add(t.getClass());

		if (t instanceof ChangedFileProtoTuple) {
			if (n.isBefore())
				beforeChangedFile = n;
			else
				afterChangedFile = n;
		} else if (t instanceof CodeRepositoryProtoTuple) {
			if (n.isBefore())
				beforeCodeRepository = n;
			else
				afterCodeRepository = n;
		}
	}
}
