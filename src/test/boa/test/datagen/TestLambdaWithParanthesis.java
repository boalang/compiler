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
 * @author rdyer
 * @author sfarheen
 */
public class TestLambdaWithParanthesis extends BaseTest {
	@Test
	public void lambda() {
		assertEquals(parseWrapped("      MathOperation division = ( int a, int b) -> a / b;"), 
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
					    "                                          \"name\": \"division\",\n" +
					    "                                          \"variable_type\": {\n" +
					    "                                             \"kind\": \"OTHER\",\n" +
					    "                                             \"name\": 1\n" +
					    "                                          },\n" +
					    "                                          \"initializer\": {\n" +
					    "                                             \"kind\": \"LAMBDA\",\n" +
					    "                                             \"lambda\": {\n" +
					    "                                                \"name\": \"\",\n" +
					    "                                                \"return_type\": {\n" +
					    "                                                   \"kind\": \"OTHER\",\n" +
					    "                                                   \"name\": 2\n" +
					    "                                                },\n" +
					    "                                                \"arguments\": [\n" +
					    "                                                   {\n" +
					    "                                                      \"name\": \"a\",\n" +
					    "                                                      \"variable_type\": {\n" +
					    "                                                         \"kind\": \"OTHER\",\n" +
					    "                                                         \"name\": 3\n" +
					    "                                                      }\n" +
					    "                                                   },\n" +
					    "                                                   {\n" +
					    "                                                      \"name\": \"b\",\n" +
					    "                                                      \"variable_type\": {\n" +
					    "                                                         \"kind\": \"OTHER\",\n" +
					    "                                                         \"name\": 3\n" +
					    "                                                      }\n" +
					    "                                                   }\n" +
					    "                                                ]\n" +
					    "                                             }\n" +
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

	@Test
	public void lambda2() {
		assertEquals(parseWrapped("x += ( int x ) -> x + x;"), 
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
		    "                                    \"kind\": \"ASSIGN_ADD\",\n" +
		    "                                    \"expressions\": [\n" +
		    "                                       {\n" +
		    "                                          \"kind\": \"VARACCESS\",\n" +
		    "                                          \"variable\": \"x\"\n" +
		    "                                       },\n" +
		    "                                       {\n" +
		    "                                          \"kind\": \"LAMBDA\",\n" +
			"                                          \"lambda\": {\n" +
			"                                             \"name\": \"\",\n" +
			"                                             \"return_type\": {\n" +
			"                                                \"kind\": \"OTHER\",\n" +
			"                                                \"name\": 1\n" +
			"                                             },\n" +
			"                                             \"arguments\": [\n" +
			"                                                {\n" +
			"                                                   \"name\": \"x\",\n" +
			"                                                   \"variable_type\": {\n" +
			"                                                      \"kind\": \"OTHER\",\n" +
			"                                                      \"name\": 2\n" +
			"                                                   }\n" +
			"                                                }\n" +
			"                                             ]\n" +
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
