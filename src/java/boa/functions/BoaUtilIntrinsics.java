package boa.functions;

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
		System.out.println(s);
		return s;
	}

}
