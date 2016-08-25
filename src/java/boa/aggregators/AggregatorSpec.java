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
package boa.aggregators;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specification annotation for Boa aggregators in Java.
 * 
 * @author anthonyu
 * @author rdyer
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AggregatorSpec {
	/**
	 * The name of the aggregator.
	 */
	String name();

	/**
	 * The Boa types of each of its formal parameters.
	 */
	String[] formalParameters() default {};

	/**
	 * The Boa type to be emitted to this aggregator. Defaults to "any",
	 * meaning it accepts all types.
	 */
	String type() default "any";

	/**
	 * The Boa type that emits to this table will be weighted by. Defaults to
	 * "none", meaning that it accepts no weights.
	 */
	String weightType() default "none";

	/**
	 * If the aggregator declares a weight type, can emits omit a value.
	 * If true, the aggregator should handle such a case (providing a default weight).
	 */
	boolean canOmitWeight() default false;

	/**
	 * Can this aggregator combine?
	 */
	boolean canCombine() default false;
}
