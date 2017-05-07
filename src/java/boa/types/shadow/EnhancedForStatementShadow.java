/*
 * Copyright 2017, Hridesh Rajan, Robert Dyer, Kaushik Nimmala
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
package boa.types.shadow;

import boa.compiler.ast.Call;
import boa.compiler.ast.expressions.Expression;
import boa.compiler.ast.Factor;
import boa.compiler.ast.Identifier;
import boa.compiler.ast.Node;
import boa.compiler.SymbolTable;
import boa.compiler.transforms.ASTFactory;
import boa.types.BoaInt;
import boa.types.BoaProtoList;
import boa.types.BoaShadowType;
import boa.types.proto.enums.StatementKindProtoMap;
import boa.types.proto.ExpressionProtoTuple;
import boa.types.proto.StatementProtoTuple;

/**
 * A shadow type for EnhancedForStatement.
 * 
 * @author rdyer
 * @author kaushin
 */
public class EnhancedForStatementShadow extends BoaShadowType  {
    /**
     * Construct a {@link EnhancedForStatementShadow}.
     */
    public EnhancedForStatementShadow() {
        super(new StatementProtoTuple());

        addShadow("parameter", new BoaProtoList(new ExpressionProtoTuple()));
        addShadow("expression", new ExpressionProtoTuple());
        addShadow("body", new StatementProtoTuple());
    }

    /** {@inheritDoc} */
    @Override
    public Node lookupCodegen(final String name, final String nodeId, final SymbolTable env) {
        final Identifier id = ASTFactory.createIdentifier(nodeId, env);
        id.type = new StatementProtoTuple();

        if ("parameter".equals(name)) {
            // ${0}.parameter
            return ASTFactory.createSelector(id, "initializations", new BoaProtoList(new ExpressionProtoTuple()), new BoaProtoList(new ExpressionProtoTuple()), env);
        }

        if ("expression".equals(name)) {
            // ${0}.expression
            return ASTFactory.createSelector(id, "expression", new ExpressionProtoTuple(), new ExpressionProtoTuple(), env);
        }

        if ("body".equals(name)) {
            // ${0}.statements
            final Expression tree = ASTFactory.createSelector(id, "statements", new BoaProtoList(new StatementProtoTuple()), new StatementProtoTuple(), env);
            // ${0}.statements[0]
            ASTFactory.getFactorFromExp(tree).addOp(ASTFactory.createIndex(ASTFactory.createIntLiteral(0), env));

            return tree;
        }

        throw new RuntimeException("invalid shadow field: " + name);
    }

    /** {@inheritDoc} */
    @Override
    public Expression getKindExpression(final SymbolTable env) {
        return getKindExpression("StatementKind", "ENHANCEDFOR", new StatementKindProtoMap(), env);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "EnhancedForStatement";
    }
}
