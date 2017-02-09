package boa.compiler.ast.types;

import java.util.ArrayList;
import java.util.List;

import boa.compiler.ast.Component;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.compiler.visitors.AbstractVisitorNoReturn;

public class MatrixType extends AbstractType{

	protected Component[][] members;
	public MatrixType() {
		members=new Component[3][3];
	}
	public MatrixType(int row,int column)
	{
		members=new Component[row][column];
	}
	public Component[][] getMembers() {
		return members;
	}
	public int getMembersColSize()
	{
		return members[0].length;
	}

	public int getMembersRowSize() {
		return members.length;
	}

	public Component getMember(final int row,final int column) {
		return members[row][column];
	}
	
	public void addMembers(final Component[][] c) {
		
		this.members=c;
	}
	
	@Override
	public MatrixType clone() {
		final MatrixType t=new MatrixType();
		t.members=this.members;
		return t;
	}

	@Override
	public <T, A> T accept(AbstractVisitor<T, A> v, A arg) {
		return v.visit(this, arg);
	}

	@Override
	public <A> void accept(AbstractVisitorNoReturn<A> v, A arg) {
		v.visit(this, arg);
		
	}

	@Override
	public void accept(AbstractVisitorNoArg v) {
		v.visit(this);
		
	}
}
