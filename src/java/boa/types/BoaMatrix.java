package boa.types;

import java.util.List;

/**
 * 
 * @author mislam
 *
 */
public class BoaMatrix extends BoaScalar {
	protected final List<Double> members;
	private int ROW=0;
	private int COLUMN=0;
	private double[][] mat;
	
	public BoaMatrix(final List<Double> members) {
		this.members=members;
		
	}
	
	public int getRow()
	{
		return this.ROW;
	}
	public int getColumn()
	{
		return this.COLUMN;
	}
	public boolean sizeEqual(BoaMatrix m)
	{
		return this.getRow()==m.getRow() && this.getColumn() == m.getColumn();
	}
	
	// FIXME add logic of multiplication
	public boolean multiplicationPossible(BoaMatrix m)
	{
		return true;
	}
	
	//FIXME add inversion possible logic
	public boolean inversionPossible(BoaMatrix m)
	{
		return true;
	}
	//FIXME add inversion logic
	public boolean inversionPossible()
	{
		return true;
	}
	
}
