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
package boa.compiler.visitors;

import java.util.LinkedList;
import java.util.List;

import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import boa.compiler.ast.Node;

/**
 * The base class for any code generating visitors.
 * Contains the string template objects.
 * 
 * @author rdyer
 */
public abstract class AbstractCodeGeneratingVisitor extends AbstractVisitorNoArg {
	public static STGroup stg = new STGroupFile("templates/BoaJavaHadoop.stg");
	static {
		stg.importTemplates(new STGroupFile("templates/BoaJava.stg"));
	}

	protected final LinkedList<String> code = new LinkedList<String>();

	public String getCode() {
		String str = "";
		for (final String s : code)
			str += s;
		return str;
	}

	public boolean hasCode() {
		for (final String s : code)
			if (s.length() > 0)
				return true;
		return false;
	}

	/** {@inheritDoc} */
	@Override
	protected void initialize() {
		code.clear();
	}

	protected void visit(final List<? extends Node> nl) {
		String s = "";

		for (final Node n : nl) {
			n.accept(this);
			if (s.length() > 0)
				s += ", ";
			s += code.removeLast();
		}

		code.add(s);
	}
}
