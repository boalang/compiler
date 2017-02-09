package boa;

import java.io.IOException;
import java.lang.ClassNotFoundException;

public interface BoaTup {
	public String[] getValues();
	public String[] getFieldNames();
	public byte[] serialize(Object o) throws IOException;
	public Object getValue(String f);
	
	
	//Code added By Johir
	public String [] addTuple(BoaTup other);
	public String [] getTypes();
	/*public String [][] getMatrix(BoaTup tups,int column);
	public String [][] transPose(String [][] values);
	public String [][] inverse(String [][] matrix);
	public String [][] mutliplyMatrix(BoaTup tup);
	public String [][] addMatrix(BoaTup tup);
	public String [][] subtractMatrix(BoaTup tup);
	public void SplitRandom(String [][] mat,String [][] test, String [][] train);
	public String [][] ColWise(String [][] mat,String op);
	public String [][] RowWise(String [][] mat,String op);
	public String [][] zeros(int m,int n);
	public String [][] ones(int m,int n);
	public String [][] identity(int n);*/
	
	
}
