package boa.functions;

import boa.graphs.cfg.CFG;
import boa.types.Ast.Method;

/**
 * Boa functions for working with control flow graphs.
 * 
 * @author ganeshau
 * @author rramu
 *
 */
public class BoaGraphIntrinsics {
<<<<<<< HEAD
	
	@FunctionSpec(name = "getcfg", returnType = "Cfg", formalParameters = { "Method", "string" })
	public static Graph getcfg(final Method method, final String cls) {
		CFG cfg = new CFG(method, cls);
=======

	@FunctionSpec(name = "getcfg", returnType = "CFG", formalParameters = { "Method" })
	public static boa.types.Control.CFG getcfg(final Method method) {
		CFG cfg = new CFG(method);
>>>>>>> 07b80102c81a5c83936cb34dc6b33f099ed18341
		cfg.astToCFG();
		return cfg.newBuilder().build();
	}

}
