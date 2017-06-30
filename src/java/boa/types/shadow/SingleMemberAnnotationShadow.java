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
import boa.compiler.ast.Selector;
import boa.compiler.ast.statements.Block;

/**
 * A shadow type for SingleMemberAnnotation.
 * 
 * @author rdyer
 * @author kaushin
 */
public class SingleMemberAnnotationShadow extends AnnotationShadow  {
    /**
     * Construct a {@link SingleMemberAnnotationShadow}.
     */
    public SingleMemberAnnotationShadow() {
        super();

        addShadow("value", new BoaString());
        
    }

    /** {@inheritDoc} */
    @Override
	public Node lookupCodegen(final String name, final Factor node, final SymbolTable env) { 

        if ("value".equals(name)) {
            // ${0}.annotation.annotation_values[0]

            final Selector s1 = new Selector(ASTFactory.createIdentifier("annotation", env));
            final Selector s2 = new Selector(ASTFactory.createIdentifier("annotation_values", env));
            final Factor f = new Factor(null).addOp(s1);
            f.addOp(s2);
            
            s1.env=s2.env = f.env = env;

            s1.type = new ExpressionProtoTuple();
            s2.type = new ExpressionProtoTuple();
            f.type  = new ExpressionProtoTuple();


            // ${0}.annotation.annotation_values[0]
           f.addOp(ASTFactory.createIndex(ASTFactory.createIntLiteral(0), env));


            return f;     
        }

        

        throw new RuntimeException("invalid shadow field: " + name);
    }

    /** {@inheritDoc} */
    @Override
    public Expression getKindExpression(final SymbolTable env) {
        return getKindExpression("ExpressionKind", "ANNOTATION", new ExpressionKindProtoMap(), env);
    }

     /** {@inheritDoc} */
    @Override
    public IfStatement getManytoOne(final SymbolTable env ,Block b) {
       
        // if(isboollit(${0})) b;
        return getManytoOne( env , b, "isstringlit");
        
    }
    
}
