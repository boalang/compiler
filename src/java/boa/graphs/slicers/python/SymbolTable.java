package boa.graphs.slicers.python;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymbolTable {
	HashMap<String, ArrayList<Integer> > defintions = new HashMap<String, ArrayList<Integer>>();
	HashMap<String, ArrayList<Integer> > uses = new HashMap<String, ArrayList<Integer>>();
	
	HashMap<String, ArrayList<Integer> > criteria = new HashMap<String, ArrayList<Integer>>();

	HashMap<Integer, String > aliasSet = new HashMap<Integer, String>();

	public static void addToAliasSet(Integer location, String resolvedName, String scope)
	{
		SymbolTable st=getSymbolTableForScope(scope);
		
		if(Status.isDirectClassScope() && !resolvedName.startsWith("self."))
		{
			resolvedName="self."+resolvedName;
		}

		st.aliasSet.put(location, resolvedName);
	}
	public static void addToAliasSet(Integer location, String resolvedName)
	{
		addToAliasSet(location, resolvedName, Status.getCurrentScope());
	}
	
	public static void addToDefintions(String key, Integer location)
	{
		String scope=Status.getCurrentScope();

		SymbolTable st=getSymbolTableForScope(scope);
		
		if(Status.isDirectClassScope() && !key.startsWith("self."))
		{
			key="self."+key;
		}
		
		if(!st.defintions.containsKey(key))
		{
			st.defintions.put(key, new ArrayList<Integer>());
		}
		
		ArrayList al=st.getDefLocations(scope, key);
		
		al.add(location);
		st.defintions.put(key, al);
	}
	
	public static void addToCriteria(String key, Integer location, String scope)
	{
		SymbolTable st=getSymbolTableForScope(scope);
		
		if(Status.isDirectClassScope() && !key.startsWith("self."))
		{
			key="self."+key;
		}
				
		if(!st.criteria.containsKey(key))
		{
			st.criteria.put(key, new ArrayList<Integer>());
		}
		
		ArrayList al=st.getCriteriaLocations(scope, key);
		
		al.add(location);
		st.criteria.put(key, al);
	}
	
	public static void addToCriteria(String key, Integer location)
	{
		addToCriteria(key, location, Status.getCurrentScope());
	}
	
	public static ArrayList<Integer> getDefLocations(String scope, String key)
	{
		SymbolTable st=getSymbolTableForScope(scope);
		
		if(!st.defintions.containsKey(key))
		{
			return new ArrayList<Integer>();
		}
		return st.defintions.get(key);
	}
	
	public static String getAliasResolvedName(String scope, Integer location)
	{
		SymbolTable st=getSymbolTableForScope(scope);
		
		if(!st.aliasSet.containsKey(location))
		{
			return "";
		}
		return st.aliasSet.get(location);
	}
	
	public static ArrayList<Integer> getCriteriaLocations(String scope, String key)
	{
		SymbolTable st=getSymbolTableForScope(scope);
		
		if(!st.criteria.containsKey(key))
		{
			return new ArrayList<Integer>();
		}
		return st.criteria.get(key);
	}
	public static void removeAliasMap(String scope)
	{
		SymbolTable st=getSymbolTableForScope(scope);
		
		st.aliasSet.clear();
	}
	public static void removeCriteriaMap(String scope)
	{
		SymbolTable st=getSymbolTableForScope(scope);
		
		st.criteria.clear();
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
	
	public void clear()
	{
		this.defintions.clear();
		this.uses.clear();
		this.aliasSet.clear();
		this.criteria.clear();
	}
}
