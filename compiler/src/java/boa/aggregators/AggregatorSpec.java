package boa.aggregators;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specification annotation for Boa aggregators in Java.
 * 
 * @author anthonyu
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AggregatorSpec {
	/**
	 * The name of the aggregator.
	 */
	String name();

	/**
	 * The Boa type to be emitted to this aggregator. Defaults to "any",
	 * meaning it accepts all types.
	 */
	String type() default "any";

	/**
	 * The Boa types of each of its formal parameters.
	 */
	String[] formalParameters() default {};

	/**
	 * The Boa type that emits to this table will be weighted by. Defaults to
	 * "none", meaning that it accepts no weights.
	 */
	String weightType() default "none";
}
