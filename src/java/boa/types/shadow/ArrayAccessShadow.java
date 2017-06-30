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

/**
 * A shadow type for ArrayAccess.
 *
 * @author rdyer
 * @author kaushin
 */
public class ArrayAccessShadow extends BoaShadowType  {
    /**
     * Construct a {@link ArrayAccessShadow}.
     */
    public ArrayAccessShadow() {
        super(new ExpressionProtoTuple());

        addShadow("array", new ExpressionProtoTuple());
        addShadow("index", new ExpressionProtoTuple());
    }

    /** {@inheritDoc} */
    @Override
	public Node lookupCodegen(final String name, final Factor node, final SymbolTable env) { 

        if ("array".equals(name)) {
            // ${0}.expressions[0]
            return ASTFactory.createFactor("expressions",ASTFactory.createIntLiteral(0),new BoaProtoList(new ExpressionProtoTuple()), new ExpressionProtoTuple(),env);
        }

        if ("index".equals(name)) {
            // ${0}.expressions[1]
            return ASTFactory.createFactor("expressions",ASTFactory.createIntLiteral(1),new BoaProtoList(new ExpressionProtoTuple()), new ExpressionProtoTuple(),env);
        }

        throw new RuntimeException("invalid shadow field: " + name);
    }

    /** {@inheritDoc} */
    @Override
    public Expression getKindExpression(final SymbolTable env) {
        return getKindExpression("ExpressionKind", "ARRAYINDEX", new ExpressionKindProtoMap(), env);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "ArrayAccess";
    }
}
