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
import boa.types.BoaString;
import boa.types.BoaProtoList;
import boa.types.BoaShadowType;
import boa.types.proto.enums.ExpressionKindProtoMap;
import boa.types.proto.ExpressionProtoTuple;
import boa.types.proto.StatementProtoTuple;

import boa.compiler.ast.statements.IfStatement;
import boa.compiler.ast.statements.Block;
/**
 * A shadow type for Literal.
 * 
 * @author rdyer
 * @author kaushin
 */
public class LiteralShadow extends BoaShadowType  {
    /**
     * Construct a {@link LiteralShadow}.
     */
    public LiteralShadow() {
        super(new ExpressionProtoTuple());

        
    }

    /** {@inheritDoc} */
    @Override
    public Node lookupCodegen(final String name, final String nodeId, final SymbolTable env) {
       
        throw new RuntimeException("invalid shadow field: " + name);
    }


    public IfStatement getManytoOne(final SymbolTable env ,Block b,String funcName) {
       
        // if(isboollit(${0})) b;

        final Expression tree = ASTFactory.createIdentifierExpr("node", env, new ExpressionProtoTuple());

        IfStatement ifstmt = new IfStatement(ASTFactory.createCallExpr(funcName, env, new ExpressionProtoTuple(), tree),b);
        return ifstmt ;   
    }


    /** {@inheritDoc} */
    @Override
    public Expression getKindExpression(final SymbolTable env) {
        return getKindExpression("ExpressionKind", "LITERAL", new ExpressionKindProtoMap(), env);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Literal";
    }
}
