/*
 * Copyright 2017, Hridesh Rajan, Robert Dyer, Kaushik Nimmala
 *                 Iowa State University of Science and Technology
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
import boa.types.proto.enums.ExpressionKindProtoMap;
import boa.types.proto.ExpressionProtoTuple;
import boa.types.proto.StatementProtoTuple;
import boa.types.proto.VariableProtoTuple;
import boa.types.proto.ModifierProtoTuple;
import boa.types.proto.TypeProtoTuple;

/**
 * A shadow type for VariableDeclarationExpression.
 * 
 * @author rdyer
 * @author kaushin
 */
public class VariableDeclarationExpressionShadow extends BoaShadowType  {
    /**
     * Construct a {@link VariableDeclarationExpressionShadow}.
     */
    public VariableDeclarationExpressionShadow() {
        super(new ExpressionProtoTuple());

        
        addShadow("fragments",new BoaProtoList(new VariableProtoTuple()));
        addShadow("modifiers", new BoaProtoList(new ModifierProtoTuple()));
        addShadow("type", new TypeProtoTuple());
    }

    /** {@inheritDoc} */
    @Override
    public Node lookupCodegen(final String name, final String nodeId, final SymbolTable env) {
        final Identifier id = ASTFactory.createIdentifier(nodeId, env);
        id.type = new StatementProtoTuple();

      
        if ("fragments".equals(name)) {
            // ${0}. $0.expression.variable_decls
            
            final Selector s1 = new Selector(ASTFactory.createIdentifier("expression", env));
            final Selector s2 = new Selector(ASTFactory.createIdentifier("variable_decls", env));
            final Factor f = new Factor(id).addOp(s1);
            f.addOp(s2);
            final Expression tree = ASTFactory.createFactorExpr(f);

            s1.env = s2.env  = f.env = env;

           // s1.type = new ExpressionProtoTuple();
            //s2.type = new VariableProtoTuple();
            s1.type = new BoaProtoList(new VariableProtoTuple());
            s2.type = new BoaProtoList(new VariableProtoTuple());
           
            f.type = tree.type = new BoaProtoList(new VariableProtoTuple());

            return tree;


        }                            
                      
        if ("modifiers".equals(name)) {
            // ${0}.expression.variable_decls[0].modifiers 
            
            final Selector s1 = new Selector(ASTFactory.createIdentifier("expression", env));
            final Selector s2 = new Selector(ASTFactory.createIdentifier("variable_decls", env));
            final Selector s3 = new Selector(ASTFactory.createIdentifier("modifiers", env));
            final Factor f = new Factor(id).addOp(s1);
            f.addOp(s2);
            f.addOp(ASTFactory.createIndex(ASTFactory.createIntLiteral(0), env));
            f.addOp(s3);
            final Expression tree = ASTFactory.createFactorExpr(f);

            s1.env = s2.env = s3.env = f.env = env;

           // s1.type = new ExpressionProtoTuple();
           // s2.type = new VariableProtoTuple();
           // s3.type = new ModifierProtoTuple()
            s1.type = new BoaProtoList(new ModifierProtoTuple());
            s2.type = new BoaProtoList(new ModifierProtoTuple());
            s3.type = new BoaProtoList(new ModifierProtoTuple());
            
            f.type = tree.type = new BoaProtoList(new ModifierProtoTuple());

            return tree;
        }
           
        if ("type".equals(name)) {
            // ${0}.$0.expression.variable_decls[0].variable_type
           

            final Selector s1 = new Selector(ASTFactory.createIdentifier("expression", env));
            final Selector s2 = new Selector(ASTFactory.createIdentifier("variable_decls", env));
            final Selector s3 = new Selector(ASTFactory.createIdentifier("variable_type", env));
            final Factor f = new Factor(id).addOp(s1);
            f.addOp(s2);
            f.addOp(ASTFactory.createIndex(ASTFactory.createIntLiteral(0), env));
            f.addOp(s3);
            final Expression tree = ASTFactory.createFactorExpr(f);

            s1.env = s2.env = s3.env = f.env = env;

            s1.type = new TypeProtoTuple();
            s2.type = new TypeProtoTuple();
            s3.type = new TypeProtoTuple();
            
            f.type = tree.type = new TypeProtoTuple();

        }

        throw new RuntimeException("invalid shadow field: " + name);
    }

    /** {@inheritDoc} */
    @Override
    public Expression getKindExpression(final SymbolTable env) {
        return getKindExpression("ExpressionKind", "VARDECL", new ExpressionKindProtoMap(), env);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "VariableDeclarationExpression";
    }
}
