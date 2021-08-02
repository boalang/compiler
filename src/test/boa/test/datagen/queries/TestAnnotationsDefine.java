package boa.test.datagen.queries;

import org.junit.Test;

public class TestAnnotationsDefine extends QueryTest {
	
	@Test
	public void testAnnotations_define() {
		String expected = "USES[https://github.com/boalang/test-datagen][src/JLS3/AnnDemo/v1/ThreadSafe.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/AnnDemo/v2/ToDo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/AnnDemo/v3/ToDo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/AnnDemo/v4/ToDo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/AnnDemo/v5/ToDo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/AnnProcDemo/ToDo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/AnnProcDemo/ToDo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/RADemo/ToDo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/RADemo/ToDos.java][1531520725000000] = 1\n";
		queryTest("test/known-good/annotations-define.boa", expected);
	}
	
}
