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
 * A shadow type for Statement.
 *
 * @author rdyer
 * @author kaushin
 */
public class SwitchCaseShadow extends boa.types.BoaShadowType  {
    /**
     * Construct a {@link SwitchCaseShadow}.
     */
    public SwitchCaseShadow() {
        super(new boa.types.proto.StatementProtoTuple());

        addShadow("expression", new boa.types.proto.ExpressionProtoTuple());
        addShadow("is_default", new boa.types.BoaBool());
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
        if ("expression".equals(name)) return ASTFactory.createSelector("expression_1", new boa.types.proto.ExpressionProtoTuple(), env);
        if ("is_default".equals(name)) return ASTFactory.createSelector("bool_1", new boa.types.BoaBool(), env);

        throw new RuntimeException("invalid shadow field '" + name + "' in shadow type SwitchCaseShadow");
    }

    /** {@inheritDoc} */
    @Override
    public Expression getKindExpression(final SymbolTable env) {
        return getKindExpression("StatementKind", "CASE", new boa.types.proto.enums.StatementKindProtoMap(), env);
    }

    /** {@inheritDoc} */
    @Override
    public IfStatement getManytoOne(final SymbolTable env, final Block b) {
        return null;
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
    public boa.types.Ast.Statement flattenMessage(final boa.types.Ast.Statement.SwitchCase m) {
        final boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
        b.setKind(boa.types.Ast.Statement.StatementKind.CASE);
        b.setExpression1(m.getExpression());
        if (m.hasIsDefault()) b.setBool1(m.getIsDefault());
        return b.build();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "SwitchCase";
    }
}
