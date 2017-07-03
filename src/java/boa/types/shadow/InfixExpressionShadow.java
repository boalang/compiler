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
        addShadow("extended_operands", new BoaProtoList(new ExpressionProtoTuple()));
        addShadow("operator", new ExpressionKindProtoMap());
    }

    /** {@inheritDoc} */
    @Override
	public Node lookupCodegen(final String name, final Factor node, final SymbolTable env) { 

        if ("left_operand".equals(name)) {
            // ${0}.expressions[0]

            return ASTFactory.createFactor("expressions",ASTFactory.createIntLiteral(0),new BoaProtoList(new ExpressionProtoTuple()), new ExpressionProtoTuple(),env);
        }

        if ("right_operand".equals(name)) {
            // ${0}.expressions[1]
           
            return ASTFactory.createFactor("expressions",ASTFactory.createIntLiteral(1),new BoaProtoList(new ExpressionProtoTuple()), new ExpressionProtoTuple(),env);

        }

        if ("extended_operands".equals(name)) {
        
            // ${0}.expressions
            node.addOp(ASTFactory.createSelector("expressions", new BoaProtoList(new ExpressionProtoTuple()), env));

            
            // splice(${0}.expressions, 1, ...)
			return ASTFactory.createCallFactor("subList", env, new BoaProtoList(new ExpressionProtoTuple()), ASTFactory.createFactorExpr(node), ASTFactory.createIntLiteral(2), ASTFactory.createIntLiteral(-1));
            


        }

        if ("operator".equals(name)) {
			// ${0}.kind
			return ASTFactory.createSelector("kind", new ExpressionKindProtoMap(), env);
        }
       

        throw new RuntimeException("invalid shadow field: " + name);
    }

    /** {@inheritDoc} */
    @Override
    public Expression getKindExpression(final SymbolTable env) {
        
        return getKindExpression("ExpressionKind", "BIT_XOR", new ExpressionKindProtoMap(), env);  
    }


   /** {@inheritDoc} */
    @Override
    public IfStatement getManytoOne(final SymbolTable env ,Block b) {
       
        // if(isboollit(${0})) b;

         final Expression tree = ASTFactory.createIdentifierExpr(boa.compiler.transforms.ShadowTypeEraser.NODE_ID, env, new ExpressionProtoTuple());

        IfStatement ifstmt = new IfStatement(ASTFactory.createCallExpr("isinfix", env, new ExpressionProtoTuple(), tree),b);
        return ifstmt ;   
    }

  

    /** {@inheritDoc} */
    @Override
    public List<Expression> getOneToMany(final SymbolTable env) {
        List<Expression> infixList = new ArrayList<Expression>(); 
        

        infixList.add(getKindExpression("ExpressionKind", "BIT_XOR", new ExpressionKindProtoMap(), env));
        infixList.add(getKindExpression("ExpressionKind", "OP_MULT", new ExpressionKindProtoMap(), env));
        infixList.add(getKindExpression("ExpressionKind", "BIT_UNSIGNEDRSHIFT", new ExpressionKindProtoMap(), env));
        infixList.add(getKindExpression("ExpressionKind", "BIT_RSHIFT", new ExpressionKindProtoMap(), env));
        infixList.add(getKindExpression("ExpressionKind", "OP_MOD", new ExpressionKindProtoMap(), env));
        infixList.add(getKindExpression("ExpressionKind", "OP_ADD", new ExpressionKindProtoMap(), env));
        infixList.add(getKindExpression("ExpressionKind", "BIT_OR", new ExpressionKindProtoMap(), env));
        infixList.add(getKindExpression("ExpressionKind", "NEQ", new ExpressionKindProtoMap(), env));
        infixList.add(getKindExpression("ExpressionKind", "OP_SUB", new ExpressionKindProtoMap(), env));
        infixList.add(getKindExpression("ExpressionKind", "LT", new ExpressionKindProtoMap(), env));
        infixList.add(getKindExpression("ExpressionKind", "LTEQ", new ExpressionKindProtoMap(), env));
        infixList.add(getKindExpression("ExpressionKind", "BIT_LSHIFT", new ExpressionKindProtoMap(), env));
        infixList.add(getKindExpression("ExpressionKind", "GT", new ExpressionKindProtoMap(), env));
        infixList.add(getKindExpression("ExpressionKind", "GTEQ", new ExpressionKindProtoMap(), env));
        infixList.add(getKindExpression("ExpressionKind", "EQ", new ExpressionKindProtoMap(), env));
        infixList.add(getKindExpression("ExpressionKind", "OP_DIV", new ExpressionKindProtoMap(), env));
        infixList.add(getKindExpression("ExpressionKind", "LOGICAL_OR", new ExpressionKindProtoMap(), env));
        infixList.add(getKindExpression("ExpressionKind", "LOGICAL_AND", new ExpressionKindProtoMap(), env));
        infixList.add(getKindExpression("ExpressionKind", "BIT_AND", new ExpressionKindProtoMap(), env));

 
                
        return infixList;  
    }



    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "InfixExpression";
    }
}
