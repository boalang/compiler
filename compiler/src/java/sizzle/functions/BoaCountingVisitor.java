package sizzle.functions;

import sizzle.runtime.BoaAbstractVisitor;
import sizzle.types.Ast.*;
import sizzle.types.Code.CodeRepository;
import sizzle.types.Code.Revision;
import sizzle.types.Diff.ChangedFile;
import sizzle.types.Toplevel.Project;

/**
 * Boa AST visitor that aggregates using a counter.
 * 
 * @author rdyer
 */
public class BoaCountingVisitor extends BoaAbstractVisitor {
	public long count;

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
