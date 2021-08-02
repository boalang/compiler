/*
 * Copyright 2021, Robert Dyer
 *                 and University of Nebraska Board of Regents
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
package boa.functions.langmode;

import boa.types.Ast.ASTRoot;
import boa.types.Ast.Declaration;
import boa.types.Ast.Expression;
import boa.types.Ast.Method;
import boa.types.Ast.Modifier;
import boa.types.Ast.Namespace;
import boa.types.Ast.Statement;
import boa.types.Ast.Type;
import boa.types.Ast.Variable;

/**
 * Boa functions for working with ASTs.
 *
 * @author rdyer
 */
public interface LangMode {
	public String type_name(final String s);

	public boolean isIntLit(final Expression e) throws Exception;
	public boolean isFloatLit(final Expression e) throws Exception;
	public boolean isCharLit(final Expression e) throws Exception;
	public boolean isStringLit(final Expression e) throws Exception;
	public boolean isTypeLit(final Expression e) throws Exception;
	public boolean isBoolLit(final Expression e) throws Exception;
	public boolean isNullLit(final Expression e) throws Exception;
	public boolean isLiteral(final Expression e, final String lit) throws Exception;

	public String prettyprint(final ASTRoot r);
	public String prettyprint(final Namespace n);
	public String prettyprint(final Declaration d);
	public String prettyprint(final Type t);
	public String prettyprint(final Method m);
	public String prettyprint(final Variable v);
	public String prettyprint(final Statement stmt);
	public String prettyprint(final Expression e);
	public String prettyprint(final Modifier m);

	public Expression parseexpression(final String s);
	public ASTRoot parse(final String s);
}
