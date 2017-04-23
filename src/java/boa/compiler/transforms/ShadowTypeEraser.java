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
package boa.compiler.transforms;

import java.util.*;

import boa.compiler.ast.Factor;
import boa.compiler.ast.Selector;
import boa.compiler.ast.Term;
import boa.compiler.ast.Node;
import boa.compiler.ast.Component;
import boa.compiler.ast.Identifier;
import boa.compiler.ast.Conjunction;
import boa.compiler.ast.Call;
import boa.compiler.ast.expressions.*;
import boa.compiler.ast.statements.VarDeclStatement;
import boa.compiler.ast.statements.Statement;
import boa.compiler.ast.statements.VisitStatement;
import boa.compiler.ast.expressions.VisitorExpression;
import boa.compiler.ast.statements.Block;
import boa.compiler.ast.statements.SwitchStatement;
import boa.compiler.ast.statements.SwitchCase;
import boa.compiler.ast.statements.BreakStatement;
import boa.compiler.SymbolTable;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.types.BoaShadowType;
import boa.types.BoaTuple;
import boa.types.BoaProtoTuple;
import boa.types.BoaType;
import boa.types.proto.StatementProtoTuple;
import boa.types.proto.ExpressionProtoTuple;
import boa.types.proto.enums.StatementKindProtoMap;

import boa.compiler.transforms.ASTFactory;

/**
 * Converts a tree using shadow types into a tree without shadow types.
 *
 * Algorithm:-
 * 1) Find each instance of VisitorExpression, then for each:
 *   a) For each VisitStatement that is a ShadowType:
 *       i) Replace identifier to erase the shadow type
 *      ii) Find each use of identifier, replace subtree
 *     iii) Remove the VisitStatement from the VisitorExpression block statement and place it in a list
 *      iv) If VisitorExpression has no VisitStatement for the shadowed type, create an empty one - otherwise select it
 *       v) Insert into the shadowed type's VisitStatement a SwitchStatement sub tree
 *      vi) For all VisitStatement's in the list, create a CaseStatement in the above created SwitchStatement, with the case value being the shadowed type
 *     vii) If there is a wildcard and we created a visit in step iv, then add the wildcard's body as the default case in the SwitchStatement
 * @author rdyer
 * @author kaushin
 */
public class ShadowTypeEraser extends AbstractVisitorNoArg {
        
    @Override
    public void start(final Node n) {
        //FIRST STEP TO COLLECT AND TRANSFORM ALL VISITSTATEMENTS
        new VisitorReplace().start(n);

        //SECOND STEP TO TRANSFORM ALL THE SUB TREES
        new SubtreeEraser().start(n);

        
    }


    public class VisitorReplace  extends AbstractVisitorNoArg{

        private LinkedList<VisitStatement> visitStack = new LinkedList<VisitStatement>();
        private LinkedList<VisitStatement> shadowVisitStack = new LinkedList<VisitStatement>();
        private LinkedList<VisitorExpression> visitorExpStack = new LinkedList<VisitorExpression>();
        
        private HashMap<BoaProtoTuple,LinkedList<VisitStatement>> beforeShadowedMap = new HashMap<BoaProtoTuple,LinkedList<VisitStatement>>();
        private HashMap<BoaProtoTuple,LinkedList<VisitStatement>> afterShadowedMap = new HashMap<BoaProtoTuple,LinkedList<VisitStatement>>();
        private HashMap<BoaProtoTuple,LinkedList<VisitStatement>> shadowedMap = new HashMap<BoaProtoTuple,LinkedList<VisitStatement>>();
        private Block wildcardBlock = null;
        private boolean shadowedTypePresent = false;

        public class VisitTransfrom  extends AbstractVisitorNoArg{
            String oldId = null;
            String newId = null;

          
            public void start(final Node n , String oldId, String newId) {
                this.oldId = oldId;
                this.newId = newId;
                n.accept(this);
            }

            @Override
            public void visit(final Identifier n) {
               
                if(n.getToken().equals(oldId)){
                    n.setToken(newId);
                }
                
            }

            @Override
            public void visit(final Component n) {
                super.visit(n);

                if (n.type instanceof BoaShadowType) {
                    final BoaShadowType shadow = (BoaShadowType)n.type;

                    // change the identifier
                    final Identifier id = (Identifier)n.getType();
                    id.setToken(shadow.shadowedName());

                    // update types
                    n.type = n.getType().type = shadow.shadowedType;
                    n.env.set(n.getIdentifier().getToken(), n.type);
                }
            }

        }


        @Override
        public void visit(final VisitStatement n) {
            visitStack.push(n);
            if(n.hasComponent()){
                n.getComponent().accept(this);
            }else{
                wildcardBlock = n.getBody();
            }
            visitStack.pop();
        }

        @Override
        public void visit(final Component n) {
            if(n.getParent() instanceof VisitStatement){

                VisitStatement parentVisit = visitStack.peek(); 
                if (parentVisit == null) return;

                BoaProtoTuple key;

                if (n.type instanceof BoaShadowType) {
                    shadowVisitStack.push(parentVisit);
                    final BoaShadowType shadow = (BoaShadowType)n.type;
                   
                    key = shadow.shadowedType;
                    shadowedTypePresent = true;

                } else {
                    key = (BoaProtoTuple)n.type;
                    shadowVisitStack.push(parentVisit);
                }

                if(parentVisit.isBefore()){
                    if(!beforeShadowedMap.containsKey(key)){
                        beforeShadowedMap.put(key,new LinkedList<VisitStatement>());
                    }
                    beforeShadowedMap.get(key).add(parentVisit);
                } else{
                    if(!afterShadowedMap.containsKey(key)){
                        afterShadowedMap.put(key,new LinkedList<VisitStatement>());
                    }
                    afterShadowedMap.get(key).add(parentVisit);
                }
            }
        }

        @Override
        public void visit(final VisitorExpression n) {
            visitorExpStack.push(n);
            super.visit(n);
            visitorExpStack.pop();
            if(shadowedTypePresent){
                // Remove the shadow type visit statements from the VisitorExpression Block.
                if(!shadowVisitStack.isEmpty()){
                    for(VisitStatement v : shadowVisitStack){
                        n.getBody().getStatements().remove(v);           
                    }
                }

                transformVisitor(n,beforeShadowedMap,true,wildcardBlock);
                transformVisitor(n,afterShadowedMap,false,wildcardBlock);

            }
            // just clearing out variables
            shadowVisitStack = new LinkedList<VisitStatement>();
            shadowedTypePresent = false;
            wildcardBlock = null;
        }

        public void transformVisitor (VisitorExpression n, HashMap<BoaProtoTuple,LinkedList<VisitStatement>> shadowedMap,boolean beforeBool,Block wildcardBlock){
                            //TODO : Create a Visit Statement of the shadowed type and attach the block to it
                for (Map.Entry<BoaProtoTuple, LinkedList<VisitStatement>> entry : shadowedMap.entrySet()) {
                    Block afterTransformation = new Block();

                    BoaProtoTuple shadowedType = entry.getKey();
                    LinkedList<VisitStatement> beforeVisits =  entry.getValue();
                    

                    Factor f = new Factor(ASTFactory.createIdentifier("node", n.env));//here i am just assuming a new identifier "node"
                    f.env = n.env;
                    //TODO : Extend to other types
                    //if(shadowedType.equals("Statement"))
                    f.getOperand().type = shadowedType; // need to add support for other types
                    

                    Selector selec = new Selector(ASTFactory.createIdentifier("kind", n.env));
                    selec.env = n.env;
                    f.addOp(selec);

                    Expression exp = ASTFactory.createFactorExpr(f);
                    //TODO : Extend to other types
                    exp.type = shadowedType.getMember("kind");
                    exp.env = n.env;

                    SwitchStatement switchS = new SwitchStatement(exp);

                    SwitchCase defaultSc = new SwitchCase(true, new Block());
                    switchS.setDefault(defaultSc);
                    
                    for(VisitStatement visit : beforeVisits ){
                        
                        Block b = visit.getBody();
                        SwitchCase sc;
                        if(visit.getComponent().type.toString().equals(shadowedType.toString())){
                            // Setting Default if present
                            defaultSc.getBody().getStatements().addAll(b.getStatements());
                        }else{
                            LinkedList<Expression> listExp = new LinkedList<Expression>();
                            listExp.add(((BoaShadowType)visit.getComponent().type).getKindExpression(n.env));
                            sc = new SwitchCase(false,b,listExp);
                            sc.getBody().getStatements().add(new BreakStatement());
                            switchS.addCase(sc);
                        }

                        //Transfroming sub tree by replacing the identifiers and type
                        new VisitTransfrom().start(visit, visit.getComponent().getIdentifier().getToken(),"node");
                    }

                   
                    if( wildcardBlock != null){
                        // trying to add wildcard to default
                        defaultSc.getBody().getStatements().addAll(wildcardBlock.getStatements());
                    }
                    if(defaultSc.getBody().getStatementsSize() == 0 ){
                        // Setting Default to a break statement if no default is present
                        defaultSc.getBody().getStatements().add(new BreakStatement());
                    } 

                    afterTransformation.addStatement(switchS);

                    // Creating a new visit and adding everything to it
                    VisitStatement shadowedTypeVisit = new VisitStatement(beforeBool, new Component(new Identifier("node"),new Identifier(shadowedType.toString())), afterTransformation);
                    shadowedTypeVisit.env = n.env;
                    shadowedTypeVisit.getComponent().env = n.env;
                    //TODO : Extend to other types
                    shadowedTypeVisit.getComponent().getType().type = shadowedType;

                    n.getBody().addStatement(shadowedTypeVisit); 
                }
        }
    }

    public class SubtreeEraser extends AbstractVisitorNoArg{
        private LinkedList<Expression> expressionStack = new LinkedList<Expression>();
        private boolean flag = false;
        private List<Node> ops = null;

        // track nearest Expression node
        public void visit(final Expression n) {
            expressionStack.push(n);
            super.visit(n);
            expressionStack.pop();
        }

        @Override
        public void visit(final Factor n) {
            flag = false;
            ops = n.getOps();
            super.visit(n);
        }

        // replacing shadow type selectors
        @Override
        public void visit(final Selector n) {
            super.visit(n);

            final Factor fact = (Factor)n.getParent();

            if (!flag && fact.getOperand().type instanceof BoaShadowType) {
                // avoid replacing past the first selector
                flag = true;
                final Expression parentExp = expressionStack.peek();

                // get shadow type used
                final Identifier id = (Identifier)fact.getOperand();
                final BoaShadowType shadow = (BoaShadowType)fact.getOperand().type;

                // replace the selector
                final Expression replacement = (Expression)shadow.lookupCodegen(n.getId().getToken(), id.getToken(), parentExp.env);
                final ParenExpression paren = new ParenExpression(replacement);
                final Factor newFact = new Factor(paren);
                final Expression newExp = ASTFactory.createFactorExpr(newFact);

                if (ops != null)
                    for (int i = 1; i < ops.size(); i++)
                        newFact.addOp(ops.get(i));

                newFact.env = parentExp.env;
                paren.type = replacement.type;
                newExp.type = paren.type;

                parentExp.replaceExpression(parentExp, newExp);
            }
        }

        // removing shadow types in before/after visit
        @Override
        public void visit(final Component n) {
            super.visit(n);
           
            if (n.type instanceof BoaShadowType) {
                final BoaShadowType shadow = (BoaShadowType)n.type;
                // change the identifier
                final Identifier id = (Identifier)n.getType();
                id.setToken(shadow.shadowedName());

                // update types
                n.type = n.getType().type = shadow.shadowedType;
                n.env.set(n.getIdentifier().getToken(), n.type);
            }
        }

        // removing shadow types in variable declarations
        @Override
        public void visit(final VarDeclStatement n) {
            super.visit(n);

            if (n.hasType()) {
                if (n.type instanceof BoaShadowType) {
                    final BoaShadowType shadow = (BoaShadowType)n.env.get(n.getType().toString());

                    // change the identifier
                    final Identifier id = (Identifier)n.getType();
                    id.setToken(shadow.shadowedName());

                    // update types
                    n.type = shadow.shadowedType;
                    n.env.setType(n.getId().getToken(), shadow.shadowedType);
                }
            }
        }
    }
}
