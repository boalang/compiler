// NOTE: This file was automatically generated - DO NOT EDIT
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

import java.util.ArrayList;
import java.util.List;

import boa.compiler.ast.expressions.Expression;
import boa.compiler.ast.Factor;
import boa.compiler.ast.Identifier;
import boa.compiler.ast.Node;
import boa.compiler.ast.statements.Block;
import boa.compiler.ast.statements.IfStatement;
import boa.compiler.SymbolTable;
import boa.compiler.transforms.ASTFactory;

/**
 * A shadow type for Expression.
 *
 * @author rdyer
 * @author kaushin
 */
public class InfixExpressionShadow extends boa.types.BoaShadowType  {
    /**
     * Construct a {@link InfixExpressionShadow}.
     */
    public InfixExpressionShadow() {
        super(new boa.types.proto.ExpressionProtoTuple());

        addShadow("left_operand", new boa.types.proto.ExpressionProtoTuple());
        addShadow("right_operand", new boa.types.proto.ExpressionProtoTuple());
        addShadow("extended_operands", new boa.types.BoaProtoList(new boa.types.proto.ExpressionProtoTuple()));
        addShadow("operator", new boa.types.proto.enums.ExpressionKindProtoMap());
    }

    /** {@inheritDoc} */
    @Override
    public boolean assigns(final boa.types.BoaType that) {
        if (that instanceof boa.types.BoaShadowType)
            return shadowedType.assigns(that);

        if (!super.assigns(that))
            return false;

        return this.getClass() == that.getClass();
    }

    /** {@inheritDoc} */
    @Override
    public Node lookupCodegen(final String name, final Factor fact, final SymbolTable env) {
        if ("left_operand".equals(name)) return ASTFactory.createSelector("expression_1", new boa.types.proto.ExpressionProtoTuple(), env);
        if ("right_operand".equals(name)) return ASTFactory.createSelector("expression_2", new boa.types.proto.ExpressionProtoTuple(), env);
        if ("extended_operands".equals(name)) return ASTFactory.createSelector("expressions_1", new boa.types.BoaProtoList(new boa.types.BoaProtoList(new boa.types.proto.ExpressionProtoTuple())), env);
        if ("operator".equals(name)) return ASTFactory.createSelector("kind", new boa.types.proto.enums.ExpressionKindProtoMap(), env);

        throw new RuntimeException("invalid shadow field '" + name + "' in shadow type InfixExpressionShadow");
    }

    /** {@inheritDoc} */
    @Override
    public Expression getKindExpression(final SymbolTable env) {
        return getKindExpression("ExpressionKind", "OP_ADD", new boa.types.proto.enums.ExpressionKindProtoMap(), env);
    }

    /** {@inheritDoc} */
    @Override
    public IfStatement getManytoOne(final SymbolTable env, final Block b) {
        return getManytoOne(env, b, "isinfix", new boa.types.proto.ExpressionProtoTuple());
    }

    /** {@inheritDoc} */
    @Override
    public List<Expression> getOneToMany(final SymbolTable env) {
        final List<Expression> l = new ArrayList<Expression>();

        l.add(getKindExpression("ExpressionKind", "OP_ADD", new boa.types.proto.enums.ExpressionKindProtoMap(), env));
        l.add(getKindExpression("ExpressionKind", "BIT_AND", new boa.types.proto.enums.ExpressionKindProtoMap(), env));
        l.add(getKindExpression("ExpressionKind", "LOGICAL_AND", new boa.types.proto.enums.ExpressionKindProtoMap(), env));
        l.add(getKindExpression("ExpressionKind", "LOGICAL_OR", new boa.types.proto.enums.ExpressionKindProtoMap(), env));
        l.add(getKindExpression("ExpressionKind", "OP_DIV", new boa.types.proto.enums.ExpressionKindProtoMap(), env));
        l.add(getKindExpression("ExpressionKind", "EQ", new boa.types.proto.enums.ExpressionKindProtoMap(), env));
        l.add(getKindExpression("ExpressionKind", "GT", new boa.types.proto.enums.ExpressionKindProtoMap(), env));
        l.add(getKindExpression("ExpressionKind", "GTEQ", new boa.types.proto.enums.ExpressionKindProtoMap(), env));
        l.add(getKindExpression("ExpressionKind", "BIT_LSHIFT", new boa.types.proto.enums.ExpressionKindProtoMap(), env));
        l.add(getKindExpression("ExpressionKind", "LT", new boa.types.proto.enums.ExpressionKindProtoMap(), env));
        l.add(getKindExpression("ExpressionKind", "LTEQ", new boa.types.proto.enums.ExpressionKindProtoMap(), env));
        l.add(getKindExpression("ExpressionKind", "OP_SUB", new boa.types.proto.enums.ExpressionKindProtoMap(), env));
        l.add(getKindExpression("ExpressionKind", "NEQ", new boa.types.proto.enums.ExpressionKindProtoMap(), env));
        l.add(getKindExpression("ExpressionKind", "BIT_OR", new boa.types.proto.enums.ExpressionKindProtoMap(), env));
        l.add(getKindExpression("ExpressionKind", "OP_MOD", new boa.types.proto.enums.ExpressionKindProtoMap(), env));
        l.add(getKindExpression("ExpressionKind", "BIT_RSHIFT", new boa.types.proto.enums.ExpressionKindProtoMap(), env));
        l.add(getKindExpression("ExpressionKind", "BIT_UNSIGNEDRSHIFT", new boa.types.proto.enums.ExpressionKindProtoMap(), env));
        l.add(getKindExpression("ExpressionKind", "OP_MULT", new boa.types.proto.enums.ExpressionKindProtoMap(), env));
        l.add(getKindExpression("ExpressionKind", "BIT_XOR", new boa.types.proto.enums.ExpressionKindProtoMap(), env));

        return l;
    }

    /**
     * Converts a shadow type message into a concrete type message.
     *
     * @param m the shadow type message
     * @return the concrete message
     */
    public boa.types.Ast.Expression flattenMessage(final boa.types.Ast.Expression.InfixExpression m) {
        final boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
        b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_ADD);
        b.setExpression1(m.getLeftOperand());
        b.setExpression2(m.getRightOperand());
        for (int i = 0; i < m.getExtendedOperandsCount(); i++) b.addExpressions1(m.getExtendedOperands(i));
        b.setKind(m.getOperator());
        return b.build();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "InfixExpression";
    }
}
