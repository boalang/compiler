/*
 * Copyright 2017, Anthony Urso, Hridesh Rajan, Robert Dyer, 
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
package boa.compiler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import boa.types.BoaArray;
import boa.types.BoaFunction;
import boa.types.BoaMap;
import boa.types.BoaName;
import boa.types.BoaSet;
import boa.types.BoaStack;
import boa.types.BoaType;
import boa.types.BoaTypeVar;
import boa.types.BoaVarargs;


/**
 * @author anthonyu
 * @author rdyer
 */
public class FunctionTrie {
	@SuppressWarnings("rawtypes")
	private final HashMap trie;

	@SuppressWarnings("rawtypes")
	public FunctionTrie() {
		this.trie = new HashMap();
	}

	private BoaType replaceVar(final BoaType formal, final BoaType actual, final Map<String, BoaType> typeVars) {
		BoaType t = formal;
		BoaType t2 = actual;

		if (t instanceof BoaArray && t2 instanceof BoaArray) {
			t = ((BoaArray)t).getType();
			t2 = ((BoaArray)t2).getType();
			if (t instanceof BoaTypeVar)
				return new BoaArray(replaceVar(t, t2, typeVars));
		} else if (t instanceof BoaMap && t2 instanceof BoaMap) {
			final BoaType i = ((BoaMap)t).getIndexType();
			final BoaType i2 = ((BoaMap)t2).getIndexType();
			t = ((BoaMap)t).getType();
			t2 = ((BoaMap)t2).getType();
			if (t instanceof BoaTypeVar || i instanceof BoaTypeVar)
				return new BoaMap(replaceVar(t, t2, typeVars), replaceVar(i, i2, typeVars));
		} else if (t instanceof BoaStack && t2 instanceof BoaStack) {
			t = ((BoaStack)t).getType();
			t2 = ((BoaStack)t2).getType();
			if (t instanceof BoaTypeVar)
				return new BoaStack(replaceVar(t, t2, typeVars));
		} else if (t instanceof BoaSet && t2 instanceof BoaSet) {
			t = ((BoaSet)t).getType();
			t2 = ((BoaSet)t2).getType();
			if (t instanceof BoaTypeVar)
				return new BoaSet(replaceVar(t, t2, typeVars));
		} else if (t instanceof BoaTypeVar) {
			final String name = ((BoaTypeVar)t).getName();
			if (typeVars.containsKey(name))
				return typeVars.get(name);
			typeVars.put(name, t2);
			return t2;
		}

		return t;
	}

	private BoaFunction getFunction(final Object[] ids, final Map<String, BoaType> typeVars) {
		if (this.trie.containsKey(ids[0])) {
			if (ids[0].equals(""))
				return getFunction();
			else
				return ((FunctionTrie) this.trie.get(ids[0])).getFunction(Arrays.copyOfRange(ids, 1, ids.length), typeVars);
		} else {
			for (final Object o : this.trie.keySet()) {
				if (o instanceof BoaVarargs && ((BoaVarargs) o).accepts((BoaType) ids[0]))
					return ((FunctionTrie) this.trie.get(o)).getFunction();

				if (o instanceof BoaType && !(ids[0] instanceof String)) {
					BoaType o2 = (BoaType)o;

					// if the function argument has a type var, bind a mapping to the actual param's type
					if (o2.hasTypeVar())
						o2 = replaceVar(o2, (BoaType)ids[0], typeVars);

					if (((BoaType) o2).accepts((BoaType) ids[0])) {
						final BoaFunction function = ((FunctionTrie) this.trie.get(o)).getFunction(Arrays.copyOfRange(ids, 1, ids.length), typeVars);

						if (function != null && !((BoaType) o2).hasTypeVar())
							return function;
					}
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

		return this.getFunction(ids, new HashMap<String, BoaType>());
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
			if (formalParameters[i] instanceof BoaName)
				ids[i + 1] = ((BoaName)formalParameters[i]).getType();
			else
				ids[i + 1] = formalParameters[i];

		ids[ids.length - 1] = "";

		this.addFunction(ids, boaFunction);
	}
}
