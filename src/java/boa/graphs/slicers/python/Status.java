package boa.graphs.slicers.python;

import java.util.HashMap;
import java.util.Stack;

public class Status {
	public static Stack<String> scopeTracer;
	public static HashMap<String, SymbolTable> symbolTable;
	
	static
	{
		scopeTracer=new Stack<String>();
		symbolTable=new HashMap<String, SymbolTable>();
	}
	
	public static String getCurrentScope()
	{
		return convertStackToString(scopeTracer, ".");
	}
	
	public static String convertStackToString(Stack<String> st, String joinStr) {
		String str = "";
		Stack<String> tmp=(Stack<String>) st.clone();
		while (!tmp.isEmpty()) {
			if (str != "")
				str = tmp.pop() + joinStr + str;
			else
				str = tmp.pop();
		}
		return str;
	}
}
