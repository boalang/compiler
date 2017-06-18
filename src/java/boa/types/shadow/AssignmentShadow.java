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

import java.util.*;

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
 * A shadow type for Assignment.
 * 
 * @author rdyer
 * @author kaushin
 */
public class AssignmentShadow extends BoaShadowType  {
    /**
     * Construct a {@link AssignmentShadow}.
     */
    public AssignmentShadow() {
        super(new ExpressionProtoTuple());

        addShadow("lefthandside", new ExpressionProtoTuple());
        addShadow("righthandside", new ExpressionProtoTuple());
        addShadow("operator", new TypeProtoTuple());
        
    }

    /** {@inheritDoc} */
    @Override
    public Node lookupCodegen(final String name, final String nodeId, final SymbolTable env) {
        final Identifier id = ASTFactory.createIdentifier(nodeId, env);
        id.type = new ExpressionProtoTuple();

        if ("lefthandside".equals(name)) {
            // ${0}.expressions[0]

            // ${0}.expressions
            final Expression tree = ASTFactory.createSelector(id, "expressions", new BoaProtoList(new ExpressionProtoTuple()), new ExpressionProtoTuple(), env);
            // ${0}.expressions[0]
            ASTFactory.getFactorFromExp(tree).addOp(ASTFactory.createIndex(ASTFactory.createIntLiteral(0), env));

            return tree;
        }

        if ("righthandside".equals(name)) {
            // ${0}.expressions[1]
           
            // ${0}.expressions
            final Expression tree = ASTFactory.createSelector(id, "expressions", new BoaProtoList(new ExpressionProtoTuple()), new ExpressionProtoTuple(), env);
            // ${0}.expressions[1]
            ASTFactory.getFactorFromExp(tree).addOp(ASTFactory.createIndex(ASTFactory.createIntLiteral(1), env));

            return tree;
        }

        if ("operator".equals(name)) {
            // TODO : Assignment Operator
            return null;
        }

        throw new RuntimeException("invalid shadow field: " + name);
    }

    /** {@inheritDoc} */
    @Override
    public Expression getKindExpression(final SymbolTable env) {
        return getKindExpression("ExpressionKind", "ASSIGN", new ExpressionKindProtoMap(), env);
    }

    // /** {@inheritDoc} */
    @Override
    public LinkedList<Expression> getOneToMany(final SymbolTable env) {
        LinkedList<Expression> assignList = new LinkedList<Expression>(); 
        
      
        assignList.add(getKindExpression("ExpressionKind", "ASSIGN_BITAND", new ExpressionKindProtoMap(), env));
        assignList.add(getKindExpression("ExpressionKind", "ASSIGN_BITOR", new ExpressionKindProtoMap(), env));
        assignList.add(getKindExpression("ExpressionKind", "ASSIGN_BITXOR", new ExpressionKindProtoMap(), env));
        assignList.add(getKindExpression("ExpressionKind", "ASSIGN_DIV", new ExpressionKindProtoMap(), env));
        assignList.add(getKindExpression("ExpressionKind", "LEFT_SHIFT_ASSIGN", new ExpressionKindProtoMap(), env));
        assignList.add(getKindExpression("ExpressionKind", "ASSIGN_SUB", new ExpressionKindProtoMap(), env));
        assignList.add(getKindExpression("ExpressionKind", "ASSIGN_ADD", new ExpressionKindProtoMap(), env));
        assignList.add(getKindExpression("ExpressionKind", "ASSIGN_MOD", new ExpressionKindProtoMap(), env));
        assignList.add(getKindExpression("ExpressionKind", "ASSIGN_RSHIFT", new ExpressionKindProtoMap(), env));
        assignList.add(getKindExpression("ExpressionKind", "ASSIGN_UNSIGNEDRSHIFT", new ExpressionKindProtoMap(), env));
        assignList.add(getKindExpression("ExpressionKind", "ASSIGN_MULT", new ExpressionKindProtoMap(), env));
        assignList.add(getKindExpression("ExpressionKind", "ASSIGN", new ExpressionKindProtoMap(), env));
      

        return assignList;  
    }


    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Assignment";
    }
}
