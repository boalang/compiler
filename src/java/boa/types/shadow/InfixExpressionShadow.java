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

import boa.compiler.ast.statements.IfStatement;
import boa.compiler.ast.statements.Block;
/**
 * A shadow type for InfixExpression.
 * 
 * @author rdyer
 * @author kaushin
 */
public class InfixExpressionShadow extends BoaShadowType  {
    /**
     * Construct a {@link InfixExpressionShadow}.
     */
    public InfixExpressionShadow() {
        super(new ExpressionProtoTuple());

        addShadow("left_operand", new ExpressionProtoTuple());
        addShadow("right_operand", new ExpressionProtoTuple());
        addShadow("extended_operands", new ExpressionProtoTuple());
        addShadow("operator", new TypeProtoTuple());
    }

    /** {@inheritDoc} */
    @Override
    public Node lookupCodegen(final String name, final String nodeId, final SymbolTable env) {
        final Identifier id = ASTFactory.createIdentifier(nodeId, env);
        id.type = new ExpressionProtoTuple();

        if ("left_operand".equals(name)) {
            // ${0}.expressions[0]

            // ${0}.expressions
            final Expression tree = ASTFactory.createSelector(id, "expressions",new BoaProtoList(new ExpressionProtoTuple()), new ExpressionProtoTuple(), env);
            // ${0}.expressions[0]
            ASTFactory.getFactorFromExp(tree).addOp(ASTFactory.createIndex(ASTFactory.createIntLiteral(0), env));

            return tree;
        }

        if ("right_operand".equals(name)) {
            // ${0}.expressions[1]
           
            // ${0}.expressions
            final Expression tree = ASTFactory.createSelector(id, "expressions", new BoaProtoList(new ExpressionProtoTuple()), new ExpressionProtoTuple(), env);
            // ${0}.expressions[1]
            ASTFactory.getFactorFromExp(tree).addOp(ASTFactory.createIndex(ASTFactory.createIntLiteral(1), env));

            return tree;
        }

        if ("extended_operands".equals(name)) {
            // ${0}.expressions[2]
           
            // ${0}.expressions
            final Expression tree = ASTFactory.createSelector(id, "expressions", new BoaProtoList(new ExpressionProtoTuple()), new ExpressionProtoTuple(), env);
            // ${0}.expressions[2]
            ASTFactory.getFactorFromExp(tree).addOp(ASTFactory.createIndex(ASTFactory.createIntLiteral(2), env));

            return tree;
        }

        if ("operator".equals(name)) {
            // TODO : InFix Operator
            return null;
        }
       

        throw new RuntimeException("invalid shadow field: " + name);
    }

    /** {@inheritDoc} */
    @Override
    public Expression getKindExpression(final SymbolTable env) {
        
        return getKindExpression("ExpressionKind", "BIT_XOR", new ExpressionKindProtoMap(), env);  
    }


    public IfStatement getManytoOne(final SymbolTable env ,Block b,String funcName) {
       
        // if(isboollit(${0})) b;

        final Expression tree = ASTFactory.createIdentifierExpr(boa.compiler.transforms.ShadowTypeEraser.NODE_ID, env, new ExpressionProtoTuple());

        IfStatement ifstmt = new IfStatement(ASTFactory.createCallExpr(funcName, env, new ExpressionProtoTuple(), tree),b);
        return ifstmt ;   
    }

    /** {@inheritDoc} */
    @Override
    public LinkedList<BoaShadowType> getOneToMany(final SymbolTable env) {
        LinkedList<BoaShadowType> infixList = new LinkedList<BoaShadowType>(); 
        

        infixList.add(new XorInFixExpressionShadow());
        infixList.add(new TimesInFixExpressionShadow());
        infixList.add(new RightShiftUnSignedInFixExpressionShadow());
        infixList.add(new RightShiftSignedInFixExpressionShadow());
        infixList.add(new RemainderInFixExpressionShadow());
        infixList.add(new PlusInFixExpressionShadow());
        infixList.add(new OrInFixExpressionShadow());
        infixList.add(new NotEqualInFixExpressionShadow());
        infixList.add(new MinusInFixExpressionShadow());
        infixList.add(new LessInFixExpressionShadow());
        infixList.add(new LessEqualsInFixExpressionShadow());
        infixList.add(new LeftShiftInFixExpressionShadow());
        infixList.add(new GreaterInFixExpressionShadow());
        infixList.add(new GreaterEqualsInFixExpressionShadow());
        infixList.add(new EqualsInFixExpressionShadow());
        infixList.add(new DivideInFixExpressionShadow());
        infixList.add(new ConditionalOrInFixExpressionShadow());
        infixList.add(new ConditionalAndInFixExpressionShadow());
        infixList.add(new AndInFixExpressionShadow());

 
                
        return infixList;  
    }




    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "InfixExpression";
    }
}
