package boa.compiler.ast.statements;

import java.util.ArrayList;
import java.util.List;

import boa.compiler.ast.expressions.Expression;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * 
 * @author rdyer
 */
public class SwitchCase extends Statement {
	protected boolean isDefault;
	protected final List<Expression> cases = new ArrayList<Expression>();
	protected final List<Statement> stmts = new ArrayList<Statement>();

	public boolean isDefault() {
		return isDefault;
	}

	public List<Expression> getCases() {
		return cases;
	}

	public int getCasesSize() {
		return cases.size();
	}

	public Expression getCase(final int index) {
		return cases.get(index);
	}

	public void addCase(final Expression e) {
		e.setParent(this);
		cases.add(e);
	}

	public List<Statement> getStmts() {
		return stmts;
	}

	public int getStmtsSize() {
		return stmts.size();
	}

	public Statement getStmt(final int index) {
		return stmts.get(index);
	}

	public void addStatement(final Statement s) {
		s.setParent(this);
		stmts.add(s);
	}

	public SwitchCase(final boolean isDefault) {
		this.isDefault = isDefault;
	}

	public <A> void accept(AbstractVisitor<A> v, A arg) {
		v.visit(this, arg);
	}

	public void accept(AbstractVisitorNoArg v) {
		v.visit(this);
	}
}
