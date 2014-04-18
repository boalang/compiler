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
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.types.BoaType;
import boa.types.proto.*;

/**
 * Optimizes a visitor by adding stop statements if the visitor doesn't look
 * at AST nodes.  This avoids calling getast() on each ChangedFile, saving a
 * significant amount of time.
 * 
 * @author rdyer
 */
public class VisitorOptimizingTransformer extends AbstractVisitorNoArg {
	protected final static Set<Class<? extends BoaType>> astTypes = new HashSet<Class<? extends BoaType>>();

	static {
		astTypes.addAll(new ASTRootProtoTuple().reachableTypes());
	}

	protected final static VariableRenameTransformer renamer = new VariableRenameTransformer();

	protected Set<Class<? extends BoaType>> types;
	protected final Stack<Set<Class<? extends BoaType>>> typeStack = new Stack<Set<Class<? extends BoaType>>>();

	protected VisitStatement beforeChangedFile;
	protected final Stack<VisitStatement> beforeStack = new Stack<VisitStatement>();

	protected VisitStatement afterChangedFile;
	protected final Stack<VisitStatement> afterStack = new Stack<VisitStatement>();

	/** {@inheritDoc} */
	@Override
	protected void initialize() {
		types = new HashSet<Class<? extends BoaType>>();
		beforeChangedFile = afterChangedFile = null;

		typeStack.clear();
		beforeStack.clear();
		afterStack.clear();
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final VisitorExpression n) {
		typeStack.push(types);
		beforeStack.push(beforeChangedFile);
		afterStack.push(afterChangedFile);

		types = new HashSet<Class<? extends BoaType>>();
		beforeChangedFile = afterChangedFile = null;

		n.getBody().accept(this);

		// if the visitor doesnt use an AST type, we can enforce
		// a stop at the lowest level visited
		types.retainAll(astTypes);
		if (types.isEmpty()) {
			if (beforeChangedFile == null) {
				final String id;
				if (afterChangedFile != null && afterChangedFile.hasComponent())
					id = afterChangedFile.getComponent().getIdentifier().getToken();
				else
					id = "_n";

				beforeChangedFile = new VisitStatement(true, new Component(new Identifier(id), new Identifier("ChangedFile")), new Block());
				beforeChangedFile.env = n.env;
				beforeChangedFile.getComponent().env = n.env;
				beforeChangedFile.getComponent().getType().type = new ChangedFileProtoTuple();

				n.getBody().addStatement(beforeChangedFile);
			} else if (afterChangedFile != null) {
				renamer.start(beforeChangedFile);
				renamer.start(afterChangedFile);
			}

			// if the before's last statement isnt a stop, merge in the after and add a stop
			if (beforeChangedFile.getBody().getStatementsSize() == 0 || !(beforeChangedFile.getBody().getStatement(beforeChangedFile.getBody().getStatementsSize() - 1) instanceof StopStatement)) {
				if (afterChangedFile != null)
					for (final Statement s : afterChangedFile.getBody().getStatements())
						beforeChangedFile.getBody().addStatement(s.clone());

				beforeChangedFile.getBody().addStatement(new StopStatement());
			}
		}

		types = typeStack.pop();
		beforeChangedFile = beforeStack.pop();
		afterChangedFile = afterStack.pop();
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
		}
	}
}
