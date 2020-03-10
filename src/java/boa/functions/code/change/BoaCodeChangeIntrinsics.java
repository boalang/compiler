package boa.functions.code.change;

import boa.functions.FunctionSpec;
import boa.types.Toplevel.Project;

public class BoaCodeChangeIntrinsics {

	@FunctionSpec(name = "test3", formalParameters = { "Project" })
	public static void test2(Project p) throws Exception {
		System.out.println("hi");
	}

}
