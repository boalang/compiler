package boa.tuplefunctions;

import Jama.Matrix;
import Jama.examples.MagicSquareExample;
import boa.types.BoaBool;
import boa.types.BoaFloat;
import boa.types.BoaInt;
import boa.*;
import boa.types.BoaType;

public class TupleOps {
	
	/**
	 * 
	 * @param tup
	 * @param column
	 * @return a 2D matrix of String
	 */
	public static String [][] matrix(BoaTup tup,long column)
	{
		
		//int len=tup.getSize();
		long len=tup.getFieldNames().length;
		long row=len/column;
		String [][] mat=new String[(int)row][(int)column];
		int it=0;
		String [] values=tup.getValues();
		for(int i=0;i<row;i++)
		{
			for(int j=0;j<column;j++)
			{
				if(it<len)
				{
					
					mat[i][j]=values[i];
					
					it++;
				}
				else
				{
					mat[i][j]="0";
				}

			}
		}
		return mat;
	}
	
	
	/**
	 * 
	 * @param tup1
	 * @param tup2
	 * @param col
	 * @return a 2D matrix after addition
	 */
	public static String[][] add(BoaTup tup1,BoaTup tup2,int col)
	{
		String [][] mat1=matrix(tup1, col);
		String [][] mat2=matrix(tup2, col);
		if(!sizeSame(mat1, mat2))
		{
			throw new IllegalStateException("Matrix sizes are not same");
		}
		String [][] ret=new String[mat1.length][mat1[0].length];
		int row=mat1.length;
		int col1=mat1[0].length;
		for(int i=0;i<row;i++)
		{
			for(int j=0;j<col1;j++)
			{
				float val1=Float.parseFloat(mat1[i][j]);
				float val2=Float.parseFloat(mat2[i][j]);
				ret[i][j]=String.valueOf(val1+val2);
			}
		}
		
		return ret;
	}
	
	
	
	/**
	 * 
	 * @param tup1
	 * @param tup2
	 * @param col
	 * @return 2D matrix of String
	 */
	public static String[][] subtract(BoaTup tup1,BoaTup tup2,int col)
	{
		String [][] mat1=matrix(tup1, col);
		String [][] mat2=matrix(tup2, col);
		if(!sizeSame(mat1, mat2))
		{
			throw new IllegalStateException("Matrix sizes are not same");
		}
		String [][] ret=new String[mat1.length][mat1[0].length];
		int row=mat1.length;
		int col1=mat1[0].length;
		for(int i=0;i<row;i++)
		{
			for(int j=0;j<col1;j++)
			{
				float val1=Float.parseFloat(mat1[i][j]);
				float val2=Float.parseFloat(mat2[i][j]);
				ret[i][j]=String.valueOf(val1-val2);
			}
		}
		
		return ret;
	}
	
	
	/**
	 * 
	 * @param mat1
	 * @param mat2
	 * @return Checks whether the Matrices can be added
	 */
	public static boolean sizeSame(String [][] mat1,String [][] mat2)
	{
		int row1=mat1.length;
		int row2=mat2.length;
		int col1=mat1[0].length;
		int col2=mat2[0].length;
		return (row1==row2) && (col1==col2);
	}
	
	
	 /**
	  * 
	  * @param n
	  * @return an identity matrix of size n*n
	  */
	  public static String[][] identity(int n) {
	        String[][] a = new String[n][n];
	        for (int i = 0; i < n; i++)
	        {
	        	for(int j=0;j<n;j++)
	        	{
	        		if(i==j)
	        		{
	        			a[i][j]=String.valueOf(1);
	        		}
	        		else
	        		{
	        			a[i][j]=String.valueOf(0);
	        		}
	        	}
	        }
	        return a;
	    }
	  
	  
	  /**
	   * 
	   * @param m
	   * @param n
	   * @return zeros matrix of size n*n
	   */
	  public static String[][] zeros(int m,int n) {
	        String[][] a = new String[m][n];
	        for (int i = 0; i < m; i++)
	        {
	        	for(int j=0;j<n;j++)
	        	{
	        			a[i][j]=String.valueOf(0);
	        		
	        	}
	        }
	        return a;
	    }
	  
	  
	  /**
	   * 
	   * @param m
	   * @param n
	   * @return ones matrix of size m*n
	   */
	  public static String[][] ones(int m,int n) {
	        String[][] a = new String[m][n];
	        for (int i = 0; i < n; i++)
	        {
	        	for(int j=0;j<n;j++)
	        	{
	        			a[i][j]=String.valueOf(1);
	        		
	        	}
	        }
	        return a;
	    }
	  
	  /**
	   * Doing transpose of an input matrix
	   * @param a
	   * @return
	   */
	   public static String[][] transpose(String[][] a) {
	        int m = a.length;
	        int n = a[0].length;
	        String[][] b = new String[n][m];
	        for (int i = 0; i < m; i++)
	            for (int j = 0; j < n; j++)
	                b[j][i] = a[i][j];
	        return b;
	    }
	   
	   /**
	    * Multiply operation
	    * @param a
	    * @param b
	    * @return
	    */
	    public static String[][] multiply(String[][] a, String[][] b) {
	        int m1 = a.length;
	        int n1 = a[0].length;
	        int m2 = b.length;
	        int n2 = b[0].length;
	        if (n1 != m2) throw new RuntimeException("Illegal matrix dimensions.");
	        float[][] c = new float[m1][n2];
	        String [][] ret=new String[m1][n2];
	        for (int i = 0; i < m1; i++)
	            for (int j = 0; j < n2; j++)
	                for (int k = 0; k < n1; k++)
	                {
	                	float val1=Float.parseFloat(a[i][k]);
	                	float val2=Float.parseFloat(b[i][k]);
	                	c[i][j] += val1 * val2;
	                }
	        for(int i=0;i<m1;i++)
	        {
	        	for(int j=0;j<n2;j++)
	        	{
	        		ret[i][j]=String.valueOf(c[i][j]);
	        	}
	        }
	        return ret;
	    }
	    
	    // matrix-vector multiplication (y = A * x)
	    
	    /**
	     * Return mutliplication of a matrix with a vector
	     * @param a
	     * @param x
	     * @return
	     */
	    public static String[] multiply(String[][] a, String[] x) {
	        int m = a.length;
	        int n = a[0].length;
	        if (x.length != n) throw new RuntimeException("Illegal matrix dimensions.");
	        double[] y = new double[m];
	        String [] ret=new String[m];
	        for (int i = 0; i < m; i++)
	            for (int j = 0; j < n; j++)
	            {
	            	float val1=Float.parseFloat(a[i][j]);
                	float val2=Float.parseFloat(x[j]);
	            	y[i] += val1 * val2;
	            }
	        for(int i=0;i<m;i++)
	        {
	        	ret[i]=String.valueOf(y[i]);
	        }
	        return ret;
	    }
	    
	    
	    /**
	     * Inversing a matrix using Jama Library
	     * @param mat
	     * @return
	     */
	    public static String [][] inverse(String [][] mat)
	    {
	    	String [][] ret=new String[mat.length][mat[0].length];
	    	double [][] md=new double[mat.length][mat[0].length];
	    	int m = mat.length;
	        int n = mat[0].length;
	        for(int i=0;i<m;i++)
	        {
	        	for(int j=0;j<n;j++)
	        	{
	        		md[i][j]=Double.parseDouble(mat[i][j]);
	        	}
	        }
	    	Matrix mt=new Matrix(md);
	    	Matrix inv=mt.inverse();
	    	for(int i=0;i<m;i++)
	    	{
	    		for(int j=0;j<n;j++)
	    		{
	    			ret[i][j]=String.valueOf(inv.get(i, j));
	    		}
	    	}
	    	return ret;
	    }
	    
	    
	    /**
	     * Element Wise Add
	     * @param mat
	     * @param val
	     * @return
	     */
	    public static String [][] elementWiseAdd(String [][] mat,int val)
	    {
	    	int m=mat.length;
	    	int n=mat[0].length;
	    	String [][]ret=new String[m][n];
	    	for (int i = 0; i < m; i++) {
				for (int j = 0; j < n; j++) {
					float v=Float.parseFloat(mat[i][j])+val;
					ret[i][j]=String.valueOf(v);
				}
			}
	    	return ret;
	    }
	    
	    /**
	     * Element wise
	     * @param mat
	     * @param val
	     * @return
	     */
	    public static String [][] elementWiseSubtract(String [][] mat,int val)
	    {
	    	int m=mat.length;
	    	int n=mat[0].length;
	    	String [][]ret=new String[m][n];
	    	for (int i = 0; i < m; i++) {
				for (int j = 0; j < n; j++) {
					float v=Float.parseFloat(mat[i][j])-val;
					ret[i][j]=String.valueOf(v);
				}
			}
	    	return ret;
	    }
	    
	    
	    /**
	     * ElementWuse Production
	     * @param mat
	     * @param val
	     * @return
	     */
	    public static String [][] elementWiseMultiply(String [][] mat,int val)
	    {
	    	int m=mat.length;
	    	int n=mat[0].length;
	    	String [][]ret=new String[m][n];
	    	for (int i = 0; i < m; i++) {
				for (int j = 0; j < n; j++) {
					float v=Float.parseFloat(mat[i][j])*val;
					ret[i][j]=String.valueOf(v);
				}
			}
	    	return ret;
	    }
	    
	    
	    /**
	     * Element Wise Division
	     * @param mat
	     * @param val
	     * @return
	     */
	    public static String [][] elementWiseDivide(String [][] mat,int val)
	    {
	    	int m=mat.length;
	    	int n=mat[0].length;
	    	String [][]ret=new String[m][n];
	    	for (int i = 0; i < m; i++) {
				for (int j = 0; j < n; j++) {
					float v=Float.parseFloat(mat[i][j])/val;
					ret[i][j]=String.valueOf(v);
				}
			}
	    	return ret;
	    }
	    
	    public static String [][] magicMatrix(int n)
	    {
	    	String [][]ret = new String[n][n];
	    	
	    //	Matrix m=new Matnm
	    	return ret;
	    }
	    

}
