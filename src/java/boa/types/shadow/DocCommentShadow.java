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
 * A shadow type for Comment.
 *
 * @author rdyer
 * @author kaushin
 */
public class DocCommentShadow extends boa.types.BoaShadowType  {
    /**
     * Construct a {@link DocCommentShadow}.
     */
    public DocCommentShadow() {
        super(new boa.types.proto.CommentProtoTuple());

        addShadow("value", new boa.types.BoaString());
        addShadow("position", new boa.types.proto.PositionInfoProtoTuple());
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
        if ("value".equals(name)) return ASTFactory.createSelector("string_1", new boa.types.BoaString(), env);
        if ("position".equals(name)) return ASTFactory.createSelector("positioninfo_1", new boa.types.proto.PositionInfoProtoTuple(), env);

        throw new RuntimeException("invalid shadow field '" + name + "' in shadow type DocCommentShadow");
    }

    /** {@inheritDoc} */
    @Override
    public Expression getKindExpression(final SymbolTable env) {
        return getKindExpression("CommentKind", "DOC", new boa.types.proto.enums.CommentKindProtoMap(), env);
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
    public boa.types.Ast.Comment flattenMessage(final boa.types.Ast.Comment.DocComment m) {
        final boa.types.Ast.Comment.Builder b = boa.types.Ast.Comment.newBuilder();
        b.setKind(boa.types.Ast.Comment.CommentKind.DOC);
        b.setString1(m.getValue());
        b.setPositioninfo1(m.getPosition());
        return b.build();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "DocComment";
    }
}
