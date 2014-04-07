/*
 * Copyright 2014, Hridesh Rajan, Robert Dyer, 
 *                 and Iowa State University of Science and Technology
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
package boa.test.compiler;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author rdyer
 */
@RunWith(JUnit4.class)
public class TestParser extends BaseTest {
	final private static String rootDir = "test/parsing/";
	final private static String badDir = rootDir + "errors/";


	@Test
	public void empty() throws IOException {
		parse(load(badDir + "empty.boa"),
			new String[] { "1,0: no viable alternative at input '<EOF>'" });
	}

	@Test
	public void keywordAsId() throws IOException {
		parse(load(badDir + "keyword-as-id.boa"),
			new String[] { "2,7: keyword 'output' can not be used as an identifier" });
	}
}
