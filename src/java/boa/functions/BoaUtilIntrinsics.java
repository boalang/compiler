package boa.functions;

import boa.types.Ast.ASTRoot;
import boa.types.Code.CodeRepository;
import boa.types.Diff.ChangedFile;
import boa.types.Toplevel.Project;


import static boa.functions.BoaIntrinsics.*;
import static boa.functions.BoaAstIntrinsics.*;

public class BoaUtilIntrinsics {
	
	@FunctionSpec(name = "stdout", returnType = "string", formalParameters = { "string" })
	public static String stdOut(final String s) {
		System.out.println(s);
		return s;
	}
	
	@FunctionSpec(name = "freemem", returnType = "string")
	public static String freemem() {
		int mb = 1024 * 1024;
		long freeMem = Runtime.getRuntime().freeMemory() / mb;
		String s = "Avaiable memory: " + freeMem + " MB";
		return s;
	}
	
	
	@FunctionSpec(name = "test", returnType = "string", formalParameters = { "Project" })
	public static String test(Project p) {
		
		System.out.println("hi");
		
		CodeRepository cr = p.getCodeRepositories(0);
		ChangedFile[] cfs = getSnapshot(cr);
		
		String name = "xstream-benchmark/src/java/com/thoughtworks/xstream/tools/benchmark/model/A50InnerClasses.java";
		for (ChangedFile cf : cfs) {
			
			if (cf.getName().equals(name)) {
				
				ASTRoot ast = getast(cf);
				test(ast);
				
				
				
			}
				
		}
		
		return "";
	}

	private static void test(ASTRoot ast) {
		
		
		
		ast.getNamespaces(0);
		
		System.out.println(ast);
	}
	
	

}
