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
public class TestLambda extends BaseTest {
	@Test
	public void lambda() {
		assertEquals(parse(
			"public class LambdaExample {\n" +
			"   public static void main(String args[]){\n" +
			"      LambdaExample le= new LambdaExample();\n" +

			"      //with type declaration\n" +
			"      MathOperation addition = (int a, int b) -> a + b;\n" +
			"      //with out type declaration\n" +
			"      MathOperation subtraction = (a, b) -> a - b;\n" +

			"      //with return statement along with curly braces\n" +
			"      MathOperation multiplication = (int a, int b) -> { return a * b; };\n" +

			"      //without return statement and without curly braces\n" +
			"      MathOperation division = (int a, int b) -> a / b;\n" +

			"      System.out.println(\"10 + 5 = \" + le.operate(10, 5, addition));\n" +
			"      System.out.println(\"10 - 5 = \" + le.operate(10, 5, subtraction));\n" +
			"      System.out.println(\"10 x 5 = \" + le.operate(10, 5, multiplication));\n" +
			"      System.out.println(\"10 / 5 = \" + le.operate(10, 5, division));\n" +

			"      //without parenthesis\n" +
			"      GreetingService greetService1 = message ->\n" +
			"      System.out.println(\"Hello \" + message);\n" +

			"      //with parenthesis\n" +
			"      GreetingService greetService2 = (message) ->\n" +
			"      System.out.println(\"Hello \" + message);\n" +

			"      //without arguments\n" +
			"      System.out.println(\"Hello \" + () -> \"World\");\n" +

			"      greetService1.sayMessage(\"Mahesh\");\n" +
			"      greetService2.sayMessage(\"Suresh\");\n" +
			"   }\n" +

			"   interface MathOperation {\n" +
			"      int operation(int a, int b);\n" +
			"   }\n" +

			"   interface GreetingService {\n" +
			"      void sayMessage(String message);\n" +
			"   }\n" +

			"   private int operate(int a, int b, MathOperation mathOperation){\n" +
			"      return mathOperation.operation(a, b);\n" +
			"   }\n" +
			"}"), null);
	}

	@Test
	public void lambda2() {
		assertEquals(parse(
			"class t {\n" +
			"	void m() {\n" + 
			"		int x = 0; x += (x) -> x + x;\n" + 
			"	}\n" +
			"}"), null);
	}
}
