/*
 * Copyright 2014, Anthony Urso, Hridesh Rajan, Robert Dyer, 
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
package boa.functions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specification annotation for Boa functions in Java.
 * 
 * @author anthonyu
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FunctionSpec {
	/**
	 * The name of the function.
	 * 
	 */
	String name();

	/**
	 * The Boa type of its return value.
	 * 
	 */
	String returnType() default "none";

	/**
	 * The Boa types of each of its formal parameters.
	 * 
	 */
	String[] formalParameters() default {};

	/**
	 * Any type dependencies that need to be handled prior to importing this
	 * function.
	 * 
	 */
	String[] typeDependencies() default {};
}
