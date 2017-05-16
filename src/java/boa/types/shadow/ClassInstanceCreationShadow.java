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
import boa.types.proto.enums.ExpressionKindProtoMap;
import boa.types.proto.ExpressionProtoTuple;
import boa.types.proto.StatementProtoTuple;
import boa.types.proto.TypeProtoTuple;
/**
 * A shadow type for ClassInstanceCreation.
 * 
 * @author rdyer
 * @author kaushin
 */
public class ClassInstanceCreationShadow extends BoaShadowType  {
    /**
     * Construct a {@link ClassInstanceCreationShadow}.
     */
    public ClassInstanceCreationShadow() {
        super(new ExpressionProtoTuple());


        addShadow("expression", new ExpressionProtoTuple());
        addShadow("arguments",  new BoaProtoList(new ExpressionProtoTuple()));
        addShadow("anonymous_class_declaration", new ExpressionProtoTuple());
        addShadow("type", new TypeProtoTuple());
    }

    /** {@inheritDoc} */
    @Override
    public Node lookupCodegen(final String name, final String nodeId, final SymbolTable env) {
        final Identifier id = ASTFactory.createIdentifier(nodeId, env);
        id.type = new StatementProtoTuple();

        if ("expression".equals(name)) {
            // ${0}.expressions[0]

            // ${0}.expressions
            final Expression tree = ASTFactory.createSelector(id, "expressions", new BoaProtoList(new ExpressionProtoTuple()), new ExpressionProtoTuple(), env);
            // ${0}.expressions[0]
            ASTFactory.getFactorFromExp(tree).addOp(ASTFactory.createIndex(ASTFactory.createIntLiteral(0), env));

            return tree;
        }

        if ("arguments".equals(name)) {
            // ${0}.expressions[1]
           
            // ${0}.expressions
            final Expression tree = ASTFactory.createSelector(id, "expressions", new BoaProtoList(new ExpressionProtoTuple()), new ExpressionProtoTuple(), env);
            // ${0}.expressions[1]
            ASTFactory.getFactorFromExp(tree).addOp(ASTFactory.createIndex(ASTFactory.createIntLiteral(1), env));

            return tree;
        }

        if ("anonymous_class_declaration".equals(name)) {
            // ${0}.anon_declaration
           
             return ASTFactory.createSelector(id, "anon_declaration", new ExpressionProtoTuple(), new ExpressionProtoTuple(), env);
        }

        if ("type".equals(name)) {
            // ${0}.new_type
            return ASTFactory.createSelector(id, "new_type", new TypeProtoTuple(), new TypeProtoTuple(), env);
        }

        throw new RuntimeException("invalid shadow field: " + name);
    }

    /** {@inheritDoc} */
    @Override
    public Expression getKindExpression(final SymbolTable env) {
        return getKindExpression("ExpressionKind", "NEW", new ExpressionKindProtoMap(), env);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "ClassInstanceCreation";
    }
}
