package boa.compiler.ast;

import boa.compiler.ast.types.AbstractType;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * 
 * @author rdyer
 */
public class Identifier extends AbstractType {
	protected String token;
	
	public String getToken() {
		return token;
	}

	public Identifier (final String token) {
		this.token = token;
	}

	public <A> void accept(AbstractVisitor<A> v, A arg) {
		v.visit(this, arg);
	}

	public void accept(AbstractVisitorNoArg v) {
		v.visit(this);
	}
}
