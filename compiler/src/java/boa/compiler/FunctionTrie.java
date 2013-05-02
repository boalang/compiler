package boa.compiler;

import java.util.Arrays;
import java.util.HashMap;

import boa.types.BoaFunction;
import boa.types.BoaType;
import boa.types.BoaVarargs;


public class FunctionTrie {
	@SuppressWarnings("rawtypes")
	private final HashMap trie;

	@SuppressWarnings("rawtypes")
	public FunctionTrie() {
		this.trie = new HashMap();
	}

	private BoaFunction getFunction(final Object[] ids) {
		if (this.trie.containsKey(ids[0])) {
			if (ids[0].equals(""))
				return getFunction();
			else
				return ((FunctionTrie) this.trie.get(ids[0])).getFunction(Arrays.copyOfRange(ids, 1, ids.length));
		} else {
			for (final Object o : this.trie.keySet()) {
				if (o instanceof BoaVarargs && ((BoaVarargs) o).accepts((BoaType) ids[0]))
					return ((FunctionTrie) this.trie.get(o)).getFunction();

				if (o instanceof BoaType && !(ids[0] instanceof String) && ((BoaType) o).accepts((BoaType) ids[0])) {
					final BoaFunction function = ((FunctionTrie) this.trie.get(o)).getFunction(Arrays.copyOfRange(ids, 1, ids.length));

					if (function != null)
						return function;
				}
			}
		}

		return null;
	}

	private BoaFunction getFunction() {
		return (BoaFunction) this.trie.get("");
	}

	public boolean hasFunction(final String name) {
		return this.trie.containsKey(name);
	}

	public BoaFunction getFunction(final String name, final BoaType[] formalParameters) {
		final Object[] ids = new Object[formalParameters.length + 2];

		ids[0] = name;

		for (int i = 0; i < formalParameters.length; i++)
			ids[i + 1] = formalParameters[i];

		ids[ids.length - 1] = "";

		return this.getFunction(ids);
	}

	@SuppressWarnings("unchecked")
	private void addFunction(final Object[] ids, final BoaFunction boaFunction) {
		if (this.trie.containsKey(ids[0])) {
			if (ids[0].equals("")) {
				throw new RuntimeException("function " + boaFunction + " already defined");
			} else {
				((FunctionTrie) this.trie.get(ids[0])).addFunction(Arrays.copyOfRange(ids, 1, ids.length), boaFunction);
			}
		} else {
			if (ids[0].equals("")) {
				this.trie.put("", boaFunction);
			} else {
				final FunctionTrie functionTrie = new FunctionTrie();
				functionTrie.addFunction(Arrays.copyOfRange(ids, 1, ids.length), boaFunction);
				this.trie.put(ids[0], functionTrie);
			}
		}
	}

	public void addFunction(final String name, final BoaFunction boaFunction) {
		final BoaType[] formalParameters = boaFunction.getFormalParameters();

		final Object[] ids = new Object[formalParameters.length + 2];

		ids[0] = name;

		for (int i = 0; i < formalParameters.length; i++)
			ids[i + 1] = formalParameters[i];

		ids[ids.length - 1] = "";

		this.addFunction(ids, boaFunction);
	}
}
