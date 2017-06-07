/*
 * Copyright 2017, Robert Dyer, Kaushik Nimmala
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
import boa.compiler.ast.Selector;
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
 * A shadow type for ConstructorInvocation.
 * 
 * @author rdyer
 * @author kaushin
 */
public class ConstructorInvocationShadow extends BoaShadowType  {
    /**
     * Construct a {@link ConstructorInvocationShadow}.
     */
    public ConstructorInvocationShadow() {
        super(new StatementProtoTuple());

        addShadow("arguments", new BoaProtoList(new ExpressionProtoTuple()));
        
    }

    /** {@inheritDoc} */
    @Override
    public Node lookupCodegen(final String name, final String nodeId, final SymbolTable env) {
        final Identifier id = ASTFactory.createIdentifier(nodeId, env);
        id.type = new StatementProtoTuple();

        if ("arguments".equals(name)) {
            // TODO ${0}.expression.method_args
            

            final Selector s1 = new Selector(ASTFactory.createIdentifier("expression", env));
            final Selector s2 = new Selector(ASTFactory.createIdentifier("method_args", env));
            final Factor f = new Factor(id).addOp(s1);
            f.addOp(s2);
            final Expression tree = ASTFactory.createFactorExpr(f);

            s1.env=s2.env = f.env = env;

            s1.type = new ExpressionProtoTuple();
            s2.type = new ExpressionProtoTuple();
            f.type = tree.type = new BoaProtoList(new ExpressionProtoTuple());

            return tree;


        }

        

        throw new RuntimeException("invalid shadow field: " + name);
    }

    /** {@inheritDoc} */
    @Override
    public Expression getKindExpression(final SymbolTable env) {
        return getKindExpression("StatementKind", "EXPRESSION", new StatementKindProtoMap(), env);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "ConstructorInvocation";
    }
}
