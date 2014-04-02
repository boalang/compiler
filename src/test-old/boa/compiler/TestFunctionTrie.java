package boa.compiler;

import junit.framework.Assert;

import org.junit.Test;

import boa.types.BoaAny;
import boa.types.BoaBool;
import boa.types.BoaFloat;
import boa.types.BoaFunction;
import boa.types.BoaInt;
import boa.types.BoaString;
import boa.types.BoaType;
import boa.types.BoaVarargs;

public class TestFunctionTrie {
	@Test
	public void testFunctionTrieSingleParameter() {
		final FunctionTrie functionTrie = new FunctionTrie();

		final BoaFunction boaFunction = new BoaFunction(new BoaBool(), new BoaType[] { new BoaString() });

		functionTrie.addFunction("function", boaFunction);
		functionTrie.addFunction("function", new BoaFunction(new BoaBool(), new BoaType[] { new BoaInt() }));

		Assert.assertEquals("did not return correct function", boaFunction, functionTrie.getFunction("function", new BoaType[] { new BoaString() }));
	}

	@Test
	public void testFunctionTrieMultiParameter() {
		final FunctionTrie functionTrie = new FunctionTrie();

		final BoaFunction boaFunction = new BoaFunction(new BoaBool(), new BoaType[] { new BoaInt(), new BoaFloat(), new BoaString() });

		functionTrie.addFunction("function", boaFunction);
		functionTrie.addFunction("function", new BoaFunction(new BoaBool(), new BoaType[] { new BoaString(), new BoaFloat(), new BoaInt() }));

		Assert.assertEquals("did not return correct function", boaFunction,
				functionTrie.getFunction("function", new BoaType[] { new BoaInt(), new BoaFloat(), new BoaString() }));
	}

	@Test
	public void testFunctionTrieOverloadedArgsShort() {
		final FunctionTrie functionTrie = new FunctionTrie();

		final BoaFunction boaFunction = new BoaFunction(new BoaBool(), new BoaType[] { new BoaInt() });

		functionTrie.addFunction("function", boaFunction);
		functionTrie.addFunction("function", new BoaFunction(new BoaBool(), new BoaType[] { new BoaInt(), new BoaString() }));

		Assert.assertEquals("did not return correct function", boaFunction, functionTrie.getFunction("function", new BoaType[] { new BoaInt() }));
	}

	@Test
	public void testFunctionTrieOverloadedArgsLong() {
		final FunctionTrie functionTrie = new FunctionTrie();

		final BoaFunction boaFunction = new BoaFunction(new BoaBool(), new BoaType[] { new BoaInt(), new BoaString() });

		functionTrie.addFunction("function", boaFunction);
		functionTrie.addFunction("function", new BoaFunction(new BoaBool(), new BoaType[] { new BoaInt() }));

		Assert.assertEquals("did not return correct function", boaFunction,
				functionTrie.getFunction("function", new BoaType[] { new BoaInt(), new BoaString() }));
	}

	@Test
	public void testFunctionTrieGeneric() {
		final FunctionTrie functionTrie = new FunctionTrie();

		final BoaFunction boaFunction = new BoaFunction(new BoaBool(), new BoaType[] { new BoaAny() });

		functionTrie.addFunction("function", boaFunction);
		functionTrie.addFunction("function", new BoaFunction(new BoaBool(), new BoaType[] { new BoaInt() }));

		Assert.assertEquals("did not return correct function", boaFunction, functionTrie.getFunction("function", new BoaType[] { new BoaString() }));
	}

	@Test
	public void testFunctionTrieVarargs() {
		final FunctionTrie functionTrie = new FunctionTrie();

		final BoaFunction boaFunction = new BoaFunction(new BoaBool(),
				new BoaType[] { new BoaString(), new BoaVarargs(new BoaString()) });

		functionTrie.addFunction("function", boaFunction);
		functionTrie.addFunction("function", new BoaFunction(new BoaBool(), new BoaType[] { new BoaString(), new BoaVarargs(new BoaInt()) }));

		Assert.assertEquals("did not return correct function", boaFunction,
				functionTrie.getFunction("function", new BoaType[] { new BoaString(), new BoaString(), new BoaString(), new BoaString() }));
	}
}
