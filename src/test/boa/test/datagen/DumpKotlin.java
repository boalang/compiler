/*
 * Copyright 2021, Robert Dyer
 *                 and University of Nebraska Board of Regents
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


/*
 * @author rdyer
 */
public class DumpKotlin extends KotlinBaseTest {
	public static void main(String[] args) {
		if (args[1].equals("${kotlin.name}"))
			dumpKotlin(args[0], "test.kt", true);
		else
			dumpKotlin(args[0], args[1], true);
	}
}
