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

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/*
 * @author sfarheen
 * @author rdyer
 */
public class TestReceiverParameter extends Java8BaseTest {
	@Test
	public void receiverParameter() throws IOException {
		assertEquals(
			load("test/datagen/boa/rcv-param.boa").trim(),
			parseJava(load("test/datagen/java/rcv-param.java")).trim()
		);
	}

	@Test
	public void receiverParameterInner() throws IOException {
		assertEquals(
			load("test/datagen/boa/rcv-param-inner.boa").trim(),
			parseJava(load("test/datagen/java/rcv-param-inner.java")).trim()
		);
	}
}
