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
import boa.compiler.ast.Selector;
import boa.compiler.ast.Identifier;
import boa.compiler.ast.Node;
import boa.compiler.SymbolTable;
import boa.compiler.transforms.ASTFactory;
import boa.types.BoaInt;
import boa.types.BoaProtoList;
import boa.types.BoaShadowType;
import boa.types.proto.enums.StatementKindProtoMap;
import boa.types.proto.ExpressionProtoTuple;
import boa.types.proto.StatementProtoTuple;
import boa.types.proto.VariableProtoTuple;
import boa.types.proto.TypeProtoTuple;


/**
 * A shadow type for CatchClause.
 * 
 * @author rdyer
 * @author kaushin
 */
public class CatchClauseShadow extends BoaShadowType  {
    /**
     * Construct a {@link CatchClauseShadow}.
     */
    public CatchClauseShadow() {
        super(new StatementProtoTuple());

        
        addShadow("body", new BoaProtoList(new StatementProtoTuple()));
        addShadow("exception", new VariableProtoTuple());
        addShadow("variable_type", new TypeProtoTuple());
        
    }

    /** {@inheritDoc} */
    @Override
	public Node lookupCodegen(final String name, final Factor node, final SymbolTable env) { 

        if ("exception".equals(name)) {
            
           // ${0}.variable_declaration.initializer
            final Selector s1 = new Selector(ASTFactory.createIdentifier("variable_declaration", env));
            final Selector s2 = new Selector(ASTFactory.createIdentifier("initializer", env));
            final Factor f = new Factor(null).addOp(s1);
            f.addOp(s2);
            

            s1.env = s2.env  = f.env = env;

           // s1.type = new ExpressionProtoTuple();
            //s2.type = new VariableProtoTuple();
            s1.type = new BoaProtoList(new VariableProtoTuple());
            s2.type = new BoaProtoList(new VariableProtoTuple());
           
            f.type  = new VariableProtoTuple();

            return f;

        }

        if ("variable_type".equals(name)) {
            
           // ${0}.variable_declaration.initializer
            final Selector s1 = new Selector(ASTFactory.createIdentifier("variable_declaration", env));
            final Selector s2 = new Selector(ASTFactory.createIdentifier("variable_type", env));
            final Factor f = new Factor(null).addOp(s1);
            f.addOp(s2);
            

            s1.env = s2.env  = f.env = env;

           // s1.type = new ExpressionProtoTuple();
            //s2.type = new VariableProtoTuple();
            s1.type = new BoaProtoList(new VariableProtoTuple());
            s2.type = new BoaProtoList(new TypeProtoTuple());
           
            f.type  = new TypeProtoTuple();

            return f;

        }



        if ("body".equals(name)) {
            // ${0}.statements
            return ASTFactory.createSelector( "statements", new BoaProtoList(new StatementProtoTuple()), env);
            
            
        }


        throw new RuntimeException("invalid shadow field: " + name);
    }

    /** {@inheritDoc} */
    @Override
    public Expression getKindExpression(final SymbolTable env) {
        return getKindExpression("StatementKind", "CATCH", new StatementKindProtoMap(), env);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "CatchClause";
    }
}
