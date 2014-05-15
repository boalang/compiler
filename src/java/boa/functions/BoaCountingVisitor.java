package boa.functions;

import boa.runtime.BoaAbstractVisitor;
import boa.types.Ast.*;
import boa.types.Code.CodeRepository;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Toplevel.Project;

/**
 * Boa AST visitor that aggregates using a counter.
 * 
 * @author rdyer
 */
public class BoaCountingVisitor extends BoaAbstractVisitor {
	public long count;

	/** {@inheritDoc} */
	@Override
	public BoaAbstractVisitor initialize() {
		count = 0;
		return super.initialize();
	}

	public long getCount(final Project node) throws Exception {
		initialize().visit(node);
		return count;
	}
	public long getCount(final CodeRepository node) throws Exception {
		initialize().visit(node);
		return count;
	}
	public long getCount(final Revision node) throws Exception {
		initialize().visit(node);
		return count;
	}
	public long getCount(final ChangedFile node) throws Exception {
		initialize().visit(node);
		return count;
	}
	public long getCount(final ASTRoot node) throws Exception {
		initialize().visit(node);
		return count;
	}
	public long getCount(final Namespace node) throws Exception {
		initialize().visit(node);
		return count;
	}
	public long getCount(final Declaration node) throws Exception {
		initialize().visit(node);
		return count;
	}
	public long getCount(final Type node) throws Exception {
		initialize().visit(node);
		return count;
	}
	public long getCount(final Method node) throws Exception {
		initialize().visit(node);
		return count;
	}
	public long getCount(final Variable node) throws Exception {
		initialize().visit(node);
		return count;
	}
	public long getCount(final Statement node) throws Exception {
		initialize().visit(node);
		return count;
	}
	public long getCount(final Expression node) throws Exception {
		initialize().visit(node);
		return count;
	}
	public long getCount(final Modifier node) throws Exception {
		initialize().visit(node);
		return count;
	}
	public long getCount(final Comment node) throws Exception {
		initialize().visit(node);
		return count;
	}
}
