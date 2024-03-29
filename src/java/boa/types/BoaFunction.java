/*
 * Copyright 2017, Anthony Urso, Hridesh Rajan, Robert Dyer,
 *                 Iowa State University of Science and Technology
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
package boa.types;

import java.util.Arrays;
import java.util.List;

import boa.compiler.ast.Component;
import boa.compiler.ast.types.AbstractType;
import boa.compiler.ast.types.FunctionType;
import boa.compiler.SymbolTable;

/**
 * A {@link BoaType} that represents a function, its return value, and its
 * formal parameters.
 *
 * @author anthonyu
 * @author rdyer
 */
public class BoaFunction extends BoaType {
	private BoaType type;
	private BoaType[] formalParameters;
	private String name;
	private String macro;

	/**
	 * Construct a BoaFunction.
	 *
	 * @param type
	 *            A {@link BoaType} representing the return type
	 */
	public BoaFunction(final BoaType type) {
		this.type = type;
	}

	/**
	 * Construct a BoaFunction.
	 *
	 * @param type
	 *            A {@link BoaType} representing the return type
	 *
	 * @param formalParameters
	 *            An array of {@link BoaType} containing the type of each
	 *            formal parameter
	 */
	public BoaFunction(final BoaType type, final BoaType[] formalParameters) {
		this(type);

		this.formalParameters = formalParameters;
	}

	/**
	 * Construct a BoaFunction.
	 *
	 * @param type
	 *            A {@link BoaType} representing the return type
	 *
	 * @param formalParameters
	 *            An array of {@link BoaType} containing the type of each
	 *            formal parameter
	 *
	 * @param macro
	 *            A snippet of Java code that can be used as a macro
	 */
	public BoaFunction(final BoaType type, final BoaType[] formalParameters, final String macro) {
		this(type, formalParameters);

		this.macro = macro;
	}

	/**
	 * Construct a BoaFunction.
	 *
	 * @param name
	 *            A {@link String} containing the canonical name of the function
	 *
	 * @param type
	 *            A {@link BoaType} representing the return type
	 *
	 * @param formalParameters
	 *            An array of {@link BoaType} containing the type of each
	 *            formal parameter
	 */
	public BoaFunction(final String name, final BoaType type, final BoaType[] formalParameters) {
		this(type, formalParameters);

		this.name = name;
	}

	/** {@inheritDoc} */
	@Override
	public boolean assigns(final BoaType that) {
		if (!(that instanceof BoaFunction))
			return false;

		if (!((BoaFunction) that).getType().assigns(this.getType()))
			return false;

		if (((BoaFunction) that).getFormalParameters().length != this.getFormalParameters().length)
			return false;

		for (int i = 0; i < this.getFormalParameters().length; i++)
			if (!((BoaFunction) that).getParameter(i).assigns(this.getParameter(i)))
				return false;

		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accepts(final BoaType that) {
		if (!(that instanceof BoaFunction))
			return false;

		if (!((BoaFunction) that).getType().assigns(this.getType()))
			return false;

		if (((BoaFunction) that).getFormalParameters().length != this.getFormalParameters().length)
			return false;

		for (int i = 0; i < this.getFormalParameters().length; i++)
			if (!((BoaFunction) that).getParameter(i).assigns(this.getParameter(i)))
				return false;

		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean compares(final BoaType that) {
		return this.type.compares(that);
	}

	/** {@inheritDoc} */
	@Override
	public BoaScalar arithmetics(final BoaType that) {
		return this.type.arithmetics(that);
	}

	/**
	 * Return the type of the parameter at a given position.
	 *
	 * @param position
	 *            An int containing the desired position
	 *
	 * @return A {@link BoaType} representing the type of that parameter
	 */
	public BoaType getParameter(final int position) {
		return this.formalParameters[position];
	}

	/**
	 * Returns the number of formal parameters for this function.
	 *
	 * @return An int containing the number of formal parameters for this
	 *         function
	 */
	public int countParameters() {
		return this.formalParameters.length;
	}

	/**
	 * Returns whether this function has a name.
	 *
	 * @return True iff this function has a name
	 */
	public boolean hasName() {
		return this.name != null;
	}

	/**
	 * Returns whether this function has a macro.
	 *
	 * @return True iff this function has a macro
	 */
	public boolean hasMacro() {
		return this.macro != null;
	}

	/**
	 * Get the return type of this function.
	 *
	 * @return A {@link BoaType} representing the return type of this
	 *         function
	 */
	public BoaType getType() {
		return this.type;
	}

	/**
	 * Set the return type of this function.
	 *
	 * @param type
	 *            A {@link BoaType} representing the return type of this
	 *            function
	 */
	public void setType(final BoaType type) {
		this.type = type;
	}

	/**
	 * Get the types of the formal parameters of this function.
	 *
	 * @return An array of {@link BoaType} containing the types of the formal
	 *         arguments of this function
	 */
	public BoaType[] getFormalParameters() {
		return this.formalParameters;
	}

	/**
	 * Set the types of the formal parameters of this function.
	 *
	 * @param formalArgs
	 *            An array of {@link BoaType} containing the types of the
	 *            formal arguments of this function
	 */
	public void setFormalParameters(final BoaType[] formalParameters) {
		this.formalParameters = formalParameters;
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getMacro() {
		return this.macro;
	}

	public void setMacro(final String macro) {
		this.macro = macro;
	}

	/** {@inheritDoc} */
	@Override
	public AbstractType toAST(final SymbolTable env) {
		final Component retType = new Component(this.type.toAST(env));
		final FunctionType t = new FunctionType(retType);
		for (final BoaType arg : this.formalParameters) {
			final Component c = new Component(arg.toAST(env));
			c.env = env;
			t.addArg(c);
		}
		retType.env = t.env = env;
		t.type = this;
		return t;
	}

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		String s = cleanType(type.toJavaType()) + "_";

		for (final BoaType t : this.formalParameters)
			s += "_" + cleanType(t.toJavaType());

		return shortenedType(s, "BoaFunc");
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "function" + Arrays.toString(this.formalParameters) + ": " + this.type.toString();
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(this.formalParameters);
		result = prime * result + (this.type == null ? 0 : this.type.hashCode());
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		final BoaFunction other = (BoaFunction) obj;
		if (!Arrays.equals(this.formalParameters, other.formalParameters))
			return false;
		if (this.type == null) {
			if (other.type != null)
				return false;
		} else if (!this.type.equals(other.type))
			return false;
		return true;
	}

	public BoaType erase(final List<BoaType> actualParameters) {
		BoaType t = type;

		if (t instanceof BoaArray) {
			t = ((BoaArray)t).getType();
			if (t instanceof BoaTypeVar)
				return new BoaArray(replaceVar(((BoaTypeVar)t).getName(), actualParameters));
		} else if (t instanceof BoaStack) {
			t = ((BoaStack)t).getType();
			if (t instanceof BoaTypeVar)
				return new BoaStack(replaceVar(((BoaTypeVar)t).getName(), actualParameters));
		} else if (t instanceof BoaSet) {
			t = ((BoaSet)t).getType();
			if (t instanceof BoaTypeVar)
				return new BoaSet(replaceVar(((BoaTypeVar)t).getName(), actualParameters));
		} else if (t.getClass() == BoaMap.class) {
			BoaType i = ((BoaMap)t).getIndexType();
			BoaType t2 = ((BoaMap)t).getType();
			if (t2 instanceof BoaTypeVar)
				t2 = replaceVar(((BoaTypeVar)t2).getName(), actualParameters);
			if (i instanceof BoaTypeVar)
				i = replaceVar(((BoaTypeVar)i).getName(), actualParameters);
			return new BoaMap(t2, i);
		} else if (t instanceof BoaQueue) {
			t = ((BoaQueue)t).getType();
			if (t instanceof BoaTypeVar)
				return new BoaQueue(replaceVar(((BoaTypeVar)t).getName(), actualParameters));
		} else if (t instanceof BoaTraversal) {
			t = ((BoaTraversal)t).getIndex();
			if (t instanceof BoaTypeVar)
				return new BoaTraversal(replaceVar(((BoaTypeVar)t).getName(), actualParameters));
		} else if (t instanceof BoaTypeVar) {
			return replaceVar(((BoaTypeVar)t).getName(), actualParameters);
		}

		return type;
	}

	private BoaType replaceVar(final String var, final List<BoaType> actual) {
		for (int i = 0; i < formalParameters.length; i++) {
			final BoaType t = replaceVar(var, formalParameters[i], actual.get(i));
			if (t != null)
				return t;
		}
		throw new RuntimeException("Invalid type parameter");
	}

	private BoaType replaceVar(final String var, final BoaType formal, final BoaType actual) {
		if (formal instanceof BoaTypeVar) {
			final BoaTypeVar tv = (BoaTypeVar)formal;
			if (tv.getName().equals(var))
				return actual;
		}
		if (formal instanceof BoaArray)
			return replaceVar(var, ((BoaArray)formal).getType(), ((BoaArray)actual).getType());
		if (formal instanceof BoaSet)
			return replaceVar(var, ((BoaSet)formal).getType(), ((BoaSet)actual).getType());
		if (formal instanceof BoaStack)
			return replaceVar(var, ((BoaStack)formal).getType(), ((BoaStack)actual).getType());
		if (formal instanceof BoaQueue)
			return replaceVar(var, ((BoaQueue)formal).getType(), ((BoaQueue)actual).getType());
		if (formal instanceof BoaTraversal)
			return replaceVar(var, ((BoaTraversal)formal).getIndex(), ((BoaTraversal)actual).getIndex());
		if (formal instanceof BoaMap) {
			final BoaType t = replaceVar(var, ((BoaMap)formal).getType(), ((BoaMap)actual).getType());
			if (t != null)
				return t;
			return replaceVar(var, ((BoaMap)formal).getIndexType(), ((BoaMap)actual).getIndexType());
		}
		return null;
	}
}
