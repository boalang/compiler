/*
 * Copyright 2016, Hridesh Rajan, Robert Dyer, Farheen Sultana
 *                 Iowa State University of Science and Technology,
 *                 and Bowling Green State University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package boa.test.datagen;

import java.io.IOException;

import org.junit.Test;

/*
 * @author rdyer
 * @author sfarheen
 */
public class TestLambda extends Java8BaseTest {
	@Test
	public void lambda() throws IOException {
		testWrapped(
			load("test/datagen/java/lambda.java").trim(),
			load("test/datagen/boa/lambda.boa").trim()
		);
	}

	@Test
	public void lambda2() throws IOException {
		testWrapped(
			load("test/datagen/java/lambda2.java").trim(),
			load("test/datagen/boa/lambda2.boa").trim()
		);
	}

	@Test
	public void lambdaWithReturn() throws IOException {
		testWrapped(
			load("test/datagen/java/lambda-ret.java").trim(),
			load("test/datagen/boa/lambda-ret.boa").trim()
		);
	}

	@Test
	public void lambdaWithTypeDecl() throws IOException {
		testWrapped(
			load("test/datagen/java/lambda-withtype.java").trim(),
			load("test/datagen/boa/lambda-withtype.boa").trim()
		);
	}

	@Test
	public void lambdaNoArg() throws IOException {
		testWrapped(
			load("test/datagen/java/lambda-noarg.java").trim(),
			load("test/datagen/boa/lambda-noarg.boa").trim()
		);
	}

	@Test
	public void lambdaNoReturn() throws IOException {
		testWrapped(
			load("test/datagen/java/lambda-noret.java").trim(),
			load("test/datagen/boa/lambda-noret.boa").trim()
		);
	}

	@Test
	public void lambdaNoTypeDecl() throws IOException {
		testWrapped(
			load("test/datagen/java/lambda-notype.java").trim(),
			load("test/datagen/boa/lambda-notype.boa").trim()
		);
	}
}
