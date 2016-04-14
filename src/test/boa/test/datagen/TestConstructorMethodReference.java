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

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/*
 * @author sfarheen
 * @author rdyer
 */
public class TestConstructorMethodReference extends BaseTest {
	@Test
	public void methodReference() {
		assertEquals(parseWrapped(
			"    ConstructorReference cref = Item::new;\n" +
            "    Item item = cref.constructor();"), 
			"{\n" +
			"   \"namespaces\": [\n" +
			"      {\n" +
			"         \"name\": \"\",\n" +
			"         \"declarations\": [\n" +
			"            {\n" +
			"               \"name\": \"t\",\n" +
			"               \"kind\": \"CLASS\",\n" +
			"               \"methods\": [\n" +
			"                  {\n" +
			"                     \"name\": \"m\",\n" +
			"                     \"return_type\": {\n" +
			"                        \"kind\": \"OTHER\",\n" +
			"                        \"name\": 0\n" +
			"                     },\n" +
			"                     \"statements\": [\n" +
			"                        {\n" +
			"                           \"kind\": \"BLOCK\",\n" +
			"                           \"statements\": [\n" +
			"                              {\n" +
			"                                 \"kind\": \"EXPRESSION\",\n" +
			"                                 \"expression\": {\n" +
			"                                    \"kind\": \"VARDECL\",\n" +
			"                                    \"variable_decls\": [\n" +
			"                                       {\n" +
			"                                          \"name\": \"cref\",\n" +
			"                                          \"variable_type\": {\n" +
			"                                             \"kind\": \"OTHER\",\n" +
			"                                             \"name\": 1\n" +
			"                                          },\n" +
			"                                          \"initializer\": {\n" +
			"                                             \"kind\": \"METHOD_REFERENCE\",\n" +
			"                                             \"new_type\": {\n" +
			"                                                \"kind\": \"OTHER\",\n" +
			"                                                \"name\": 2\n" +
			"                                             },\n" +
			"                                             \"method\": \"new\"\n" +
			"                                          }\n" +
			"                                       }\n" +
			"                                    ]\n" +
			"                                 }\n" +
			"                              },\n" +
			"                              {\n" +
			"                                 \"kind\": \"EXPRESSION\",\n" +
			"                                 \"expression\": {\n" +
			"                                    \"kind\": \"VARDECL\",\n" +
			"                                    \"variable_decls\": [\n" +
			"                                       {\n" +
			"                                          \"name\": \"item\",\n" +
			"                                          \"variable_type\": {\n" +
			"                                             \"kind\": \"OTHER\",\n" +
			"                                             \"name\": 2\n" +
			"                                          },\n" +
			"                                          \"initializer\": {\n" +
			"                                             \"kind\": \"METHODCALL\",\n" +
			"                                             \"expressions\": [\n" +
			"                                                {\n" +
			"                                                   \"kind\": \"VARACCESS\",\n" +
			"                                                   \"variable\": \"cref\"\n" +
			"                                                }\n" +
			"                                             ],\n" +
			"                                             \"method\": \"constructor\"\n" +
			"                                          }\n" +
			"                                       }\n" +
			"                                    ]\n" +
			"                                 }\n" +
			"                              }\n" +
			"                           ]\n" +
			"                        }\n" +
			"                     ]\n" +
			"                  }\n" +
			"               ]\n" +
			"            }\n" +
			"         ]\n" +
			"      }\n" +
			"   ]\n" +
			"}"
		);
	}
}
