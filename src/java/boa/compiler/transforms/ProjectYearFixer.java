/*
 * Copyright 2019, Robert Dyer, 
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
package boa.compiler.transforms;

import java.util.Stack;

import boa.compiler.ast.expressions.Expression;
import boa.compiler.ast.expressions.ParenExpression;
import boa.compiler.ast.Factor;
import boa.compiler.ast.Identifier;
import boa.compiler.ast.Node;
import boa.compiler.ast.Selector;
import boa.compiler.ast.statements.VarDeclStatement;
import boa.compiler.ast.statements.VisitStatement;
import boa.compiler.visitors.AbstractVisitorNoArg;

/**
 * Fixes the bug with project creation years being off by 1000.
 * 
 * @author rdyer
 */
public class ProjectYearFixer extends AbstractVisitorNoArg {
/* OLD AST
Expression
    Conjunction
        Comparison
            SimpleExpr
                Term
                    Factor
                        Identifier
                        Selector
                            Identifier
*/
/* NEW AST
Expression
    Conjunction
        Comparison
            SimpleExpr
                Term
                    Factor
                        ParenExpression
                            Expression
                                Conjunction
                                    Comparison
                                        SimpleExpr
                                            Term
                                                Factor
                                                    Identifier
                                                    Selector
                                                        Identifier
                                                Factor
                                                    IntegerLiteral
*/
    private Expression lastExp = null;
    private boolean isProject = false;
    private boolean hasCreatedDate = false;

	/** {@inheritDoc} */
	@Override
	protected void initialize() {
		lastExp = null;
        isProject = false;
        hasCreatedDate = false;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Expression n) {
        lastExp = n;
		super.visit(n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Factor n) {
        isProject = false;
        hasCreatedDate = false;
		super.visit(n);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Identifier n) {
	}

    private ParenExpression createParenExp() {
/*
Factor
    ParenExpression
        Expression
            Conjunction
                Comparison
                    SimpleExpr
                        Term
                            Factor
                                Identifier
                                Selector
                                    Identifier
                            Factor
                                IntegerLiteral
*/
        return null;
    }
}
