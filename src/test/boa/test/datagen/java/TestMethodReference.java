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
 * @author sfarheen
 * @author rdyer
 */
public class TestMethodReference extends Java8BaseTest {
	@Test
	public void staticMethodReference() throws IOException {
		testWrapped(
			load("test/datagen/java/static-meth-ref.java").trim(),
			load("test/datagen/boa/static-meth-ref.boa").trim()
		);
	}

	@Test
	public void instanceMethodReference() throws IOException {
		testWrapped(
			load("test/datagen/java/inst-meth-ref.java").trim(),
			load("test/datagen/boa/inst-meth-ref.boa").trim()
		);
	}

	@Test
	public void arbitraryObjectMethodReference() throws IOException {
		testWrapped(
			load("test/datagen/java/obj-meth-ref.java").trim(),
			load("test/datagen/boa/obj-meth-ref.boa").trim()
		);
	}

	@Test
	public void constructorMethodReference() throws IOException {
		testWrapped(
			load("test/datagen/java/new-meth-ref.java").trim(),
			load("test/datagen/boa/new-meth-ref.boa").trim()
		);
	}
	
	@Test
	public void superMethodReference() throws IOException {
		testWrapped(
			load("test/datagen/java/super-meth-ref.java").trim(),
			load("test/datagen/boa/super-meth-ref.boa").trim()
		);
	}
}
