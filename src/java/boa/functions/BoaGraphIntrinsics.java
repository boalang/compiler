package boa.functions;

import boa.graphs.cfg.CFG;
import boa.types.Ast.Method;

/**
 * Boa functions for working with control flow graphs.
 * 
 * @author ganeshau
 *
 */
public class BoaGraphIntrinsics {

	@FunctionSpec(name = "getcfg", returnType = "CFG", formalParameters = { "Method" })
	public static boa.types.Control.CFG getcfg(final Method method) {
		CFG cfg = new CFG(method);
		cfg.astToCFG();
		return cfg.newBuilder().build();
	}
}
