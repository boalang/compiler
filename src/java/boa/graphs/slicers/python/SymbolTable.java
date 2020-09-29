package boa.graphs.slicers.python;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymbolTable {
	HashMap<String, ArrayList<Integer> > defintions = new HashMap<String, ArrayList<Integer>>();
	HashMap<String, ArrayList<Integer> > uses = new HashMap<String, ArrayList<Integer>>();
	
	public static void addToDefintions(String scope, String key, Integer location)
	{
		SymbolTable st=getSymbolTableForScope(scope);
		
		if(!st.defintions.containsKey(key))
		{
			st.defintions.put(key, new ArrayList<Integer>());
		}
		
		ArrayList al=st.getLocations(scope, key);
		
		al.add(location);
		st.defintions.put(key, al);
	}
	
	public static ArrayList<Integer> getLocations(String scope, String key)
	{
		SymbolTable st=getSymbolTableForScope(scope);
		
		if(!st.defintions.containsKey(key))
		{
			return new ArrayList<Integer>();
		}
		return st.defintions.get(key);
	}
	
	public static SymbolTable getSymbolTableForScope(String scope)
	{
		if(!Status.symbolTable.containsKey(scope))
			Status.symbolTable.put(scope, new SymbolTable());
		return Status.symbolTable.get(scope);
	}
	
	public static void printDefintions(SymbolTable st)
	{
		for (Map.Entry<String,  ArrayList<Integer>> entry : st.defintions.entrySet()) {
		    String identifierName = entry.getKey();
		    ArrayList<Integer> locations = entry.getValue();
		    
		    System.out.print("Identifer Name: "+identifierName+" (");
		    for(Integer l: locations)
		    {
		    	System.out.print(l+" ");
		    }
		    System.out.print(")\n");
		    
		}
	}
	public static void printSymbolTable()
	{
		for (Map.Entry<String, SymbolTable> entry : Status.symbolTable.entrySet()) {
		    String scope = entry.getKey();
		    SymbolTable st = entry.getValue();
		    
		    System.out.println("Identifers for scope: "+scope);
		    
		    printDefintions(st);
		}
	}
}
