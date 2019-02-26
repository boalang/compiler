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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.Descriptors.FieldDescriptor;

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

	private void visitChildren(AbstractMessage message) throws Exception {
		for (Iterator<Map.Entry<FieldDescriptor, Object>> iter = message.getAllFields().entrySet().iterator(); iter.hasNext();) {
            Map.Entry<FieldDescriptor, Object> field = iter.next();
            visitField(field.getKey(), field.getValue());
        }
	}

	private void visitField(FieldDescriptor field, Object value) throws Exception {
		if (field.isRepeated())
            for (Iterator<?> iter = ((List<?>) value).iterator(); iter.hasNext();)
                visitFieldValue(field, iter.next());
        else
        	visitFieldValue(field, value);
	}

	private void visitFieldValue(FieldDescriptor field, Object value) throws Exception {
		if (field.getType() == com.google.protobuf.Descriptors.FieldDescriptor.Type.MESSAGE)
			visit((AbstractMessage) value);
	}

	private void visit(AbstractMessage value) throws Exception {
		if (value instanceof Project) {
			visit((Project) value);
		} else if (value instanceof CodeRepository) {
			visit((CodeRepository) value);
		} else if (value instanceof Revision) {
			visit((Revision) value);
		} else if (value instanceof ChangedFile) {
			visit((ChangedFile) value);
		} else if (value instanceof ASTRoot) {
			visit((ASTRoot) value);
		} else if (value instanceof Namespace) {
			visit((Namespace) value);
		} else if (value instanceof Declaration) {
			visit((Declaration) value);
		} else if (value instanceof Type) {
			visit((Type) value);
		} else if (value instanceof Method) {
			visit((Method) value);
		} else if (value instanceof Variable) {
			visit((Variable) value);
		} else if (value instanceof Statement) {
			visit((Statement) value);
		} else if (value instanceof Expression) {
			visit((Expression) value);
		} else if (value instanceof Modifier) {
			visit((Modifier) value);
		} else if (value instanceof Comment) {
			visit((Comment) value);
		} else if (value instanceof Person) {
			visit((Person) value);
		} else
			throw new UnsupportedOperationException("Unsupported Boa message type!");
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
			visitChildren(node);

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
			visitChildren(node);

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
			visitChildren(node);

			postVisit(node);
		}
	}
	public final void visit(final Namespace node) throws Exception {
		if (preVisit(node)) {
			visitChildren(node);

			postVisit(node);
		}
	}
	public final void visit(final Declaration node) throws Exception {
		if (preVisit(node)) {
			visitChildren(node);

			postVisit(node);
		}
	}
	public final void visit(final Type node) throws Exception {
		if (preVisit(node)) {
			visitChildren(node);
			
			postVisit(node);
		}
	}
	public final void visit(final Method node) throws Exception {
		if (preVisit(node)) {
			visitChildren(node);

			postVisit(node);
		}
	}
	public final void visit(final Variable node) throws Exception {
		if (preVisit(node)) {
			visitChildren(node);

			postVisit(node);
		}
	}
	public final void visit(final Statement node) throws Exception {
		if (preVisit(node)) {
			visitChildren(node);

			postVisit(node);
		}
	}
	public final void visit(final Expression node) throws Exception {
		if (preVisit(node)) {
			visitChildren(node);

			postVisit(node);
		}
	}
	public final void visit(final Modifier node) throws Exception {
		if (preVisit(node)) {
			visitChildren(node);

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
