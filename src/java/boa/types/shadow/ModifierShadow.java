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
import boa.types.proto.enums.ModifierKindProtoMap;
import boa.types.proto.ModifierProtoTuple;
import boa.types.proto.StatementProtoTuple;
import boa.types.proto.TypeProtoTuple;
/**
 * A shadow type for Modifier.
 * 
 * @author rdyer
 * @author kaushin
 */
public class ModifierShadow extends BoaShadowType  {
    /**
     * Construct a {@link ModifierShadow}.
     */
    public ModifierShadow() {
        super(new ModifierProtoTuple());

    }

    /** {@inheritDoc} */
    @Override
	public Node lookupCodegen(final String name, final Factor node, final SymbolTable env) { 
       

        throw new RuntimeException("invalid shadow field: " + name);
    }

    /** {@inheritDoc} */
    @Override
    public Expression getKindExpression(final SymbolTable env) {
        
        return getKindExpression("ModifierKind", "FINAL", new ModifierKindProtoMap(), env);  
    }


    /** {@inheritDoc} */
    @Override
    public List<Expression> getOneToMany(final SymbolTable env) {
        List<Expression> modList = new ArrayList<Expression>(); 

        modList.add(getKindExpression("ModifierKind", "ABSTRACT", new ModifierKindProtoMap(), env));
        modList.add(getKindExpression("ModifierKind", "FINAL", new ModifierKindProtoMap(), env));
        modList.add(getKindExpression("ModifierKind", "OTHER", new ModifierKindProtoMap(), env));
        modList.add(getKindExpression("ModifierKind", "STATIC", new ModifierKindProtoMap(), env));
        modList.add(getKindExpression("ModifierKind", "SYNCHRONIZED", new ModifierKindProtoMap(), env));
        modList.add(getKindExpression("ModifierKind", "VISIBILITY", new ModifierKindProtoMap(), env));
    
        return modList;  
    }


    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Modifier";
    }
}
