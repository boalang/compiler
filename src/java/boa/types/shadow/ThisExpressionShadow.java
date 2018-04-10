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
public class ThisExpressionShadow extends boa.types.BoaShadowType  {
    /**
     * Construct a {@link ThisExpressionShadow}.
     */
    public ThisExpressionShadow() {
        super(new boa.types.proto.ExpressionProtoTuple());

        addShadow("qualifier", new boa.types.BoaString());
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
        if ("qualifier".equals(name)) return ASTFactory.createSelector("string_1", new boa.types.BoaString(), env);

        throw new RuntimeException("invalid shadow field '" + name + "' in shadow type ThisExpressionShadow");
    }

    /** {@inheritDoc} */
    @Override
    public Expression getKindExpression(final SymbolTable env) {
        return getKindExpression("ExpressionKind", "LITERAL", new boa.types.proto.enums.ExpressionKindProtoMap(), env);
    }

    /** {@inheritDoc} */
    @Override
    public IfStatement getManytoOne(final SymbolTable env, final Block b) {
        return getManytoOne(env, b, "isthislit", new boa.types.proto.ExpressionProtoTuple());
    }

    /** {@inheritDoc} */
    @Override
    public List<Expression> getOneToMany(final SymbolTable env) {
        final List<Expression> l = new ArrayList<Expression>();


        return l;
    }

    /**
     * Converts a shadow type message into a concrete type message.
     *
     * @param m the shadow type message
     * @return the concrete message
     */
    public boa.types.Ast.Expression flattenMessage(final boa.types.Ast.Expression.ThisExpression m) {
        final boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
        b.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
        if (m.hasQualifier()) b.setString1(m.getQualifier());
        return b.build();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "ThisExpression";
    }
}
