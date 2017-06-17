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
import boa.compiler.visitors.AbstractVisitorNoArgNoRet;
import boa.types.BoaShadowType;
import boa.types.BoaTuple;
import boa.types.BoaProtoTuple;
import boa.types.BoaType;
import boa.types.proto.StatementProtoTuple;
import boa.types.proto.ExpressionProtoTuple;
import boa.types.proto.enums.StatementKindProtoMap;
import boa.types.shadow.*;

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
 *
 * @author rdyer
 * @author kaushin
 */
public class ShadowTypeEraser extends AbstractVisitorNoArgNoRet {
    @Override
    public void start(final Node n) {
        // first step to collect and transform all VisitStatements
        new VisitorReplace().start(n);

        // second step to transform all the sub trees
        new SubtreeEraser().start(n);
    }

    protected class VisitTransform extends AbstractVisitorNoArgNoRet {
        String oldId;
        String newId;

        public void start(final Node n, final String oldId, final String newId) {
            this.oldId = oldId;
            this.newId = newId;
            n.accept(this);
        }

        @Override
        public void visit(final Identifier n) {
            if (n.getToken().equals(oldId)) {
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

    protected class VisitorReplace extends AbstractVisitorNoArgNoRet {
        private Deque<VisitStatement> visitStack = new ArrayDeque<VisitStatement>();
        private Deque<VisitStatement> shadowVisitStack = new ArrayDeque<VisitStatement>();
        private Deque<VisitorExpression> visitorExpStack = new ArrayDeque<VisitorExpression>();

        private HashMap<BoaProtoTuple, LinkedList<VisitStatement>> beforeShadowedMap = new HashMap<BoaProtoTuple, LinkedList<VisitStatement>>();
        private HashMap<BoaProtoTuple, LinkedList<VisitStatement>> afterShadowedMap = new HashMap<BoaProtoTuple, LinkedList<VisitStatement>>();
        private HashMap<BoaProtoTuple, LinkedList<VisitStatement>> shadowedMap = new HashMap<BoaProtoTuple, LinkedList<VisitStatement>>();

        private HashMap<BoaShadowType,BoaShadowType> manytomanyMap = new HashMap< BoaShadowType,BoaShadowType>();
        

        private Block wildcardBlock = null;
        private boolean shadowedTypePresent = false;

        @Override
        public void visit(final VisitStatement n) {
            visitStack.push(n);
            if (n.hasComponent()) {
                n.getComponent().accept(this);
            } else {
                wildcardBlock = n.getBody();
            }
            visitStack.pop();
        }

        @Override
        public void visit(final Component n) {
            if (n.getParent() instanceof VisitStatement) {
                final VisitStatement parentVisit = visitStack.peek();
                if (parentVisit == null) {
                    return;
                }

                final BoaProtoTuple key;

                if (n.type instanceof BoaShadowType) {
                    shadowVisitStack.push(parentVisit);
                    final BoaShadowType shadow = (BoaShadowType)n.type;

                    key = shadow.shadowedType;
                    shadowedTypePresent = true;
                } else {
                    key = (BoaProtoTuple)n.type;
                    shadowVisitStack.push(parentVisit);
                }

                if (parentVisit.isBefore()) {
                    if (!beforeShadowedMap.containsKey(key)) {
                        beforeShadowedMap.put(key, new LinkedList<VisitStatement>());
                    }
                    beforeShadowedMap.get(key).add(parentVisit);
                } else {
                    if (!afterShadowedMap.containsKey(key)) {
                        afterShadowedMap.put(key, new LinkedList<VisitStatement>());
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

            if (shadowedTypePresent) {
                // remove the shadow type VisitStatements from the VisitorExpression block
                if (!shadowVisitStack.isEmpty()) {
                    for (final VisitStatement v : shadowVisitStack) {
                        n.getBody().getStatements().remove(v);
                    }
                }

                transformVisitor(n, beforeShadowedMap, true, wildcardBlock);
                transformVisitor(n, afterShadowedMap, false, wildcardBlock);
            }

            // just clearing out variables
            shadowVisitStack = new LinkedList<VisitStatement>();
            shadowedTypePresent = false;
            wildcardBlock = null;
        }

        public void transformVisitor (final VisitorExpression n, final HashMap<BoaProtoTuple, LinkedList<VisitStatement>> shadowedMap, final boolean isBefore, final Block wildcardBlock) {
            for (final Map.Entry<BoaProtoTuple, LinkedList<VisitStatement>> entry : shadowedMap.entrySet()) {
                final Block afterTransformation = new Block();

                final BoaProtoTuple shadowedType = entry.getKey();
                LinkedList<VisitStatement> visits =  entry.getValue();

                final Factor f = new Factor(ASTFactory.createIdentifier("node", n.env));// here i am just assuming a new identifier "node"
                f.env = n.env;
                f.getOperand().type = shadowedType;

                final Selector selec = new Selector(ASTFactory.createIdentifier("kind", n.env));
                selec.env = n.env;
                f.addOp(selec);

                final Expression exp = ASTFactory.createFactorExpr(f);
                exp.type = shadowedType.getMember("kind");
                exp.env = n.env;

                final SwitchStatement switchS = new SwitchStatement(exp);

                final SwitchCase defaultSc = new SwitchCase(true, new Block());
                switchS.setDefault(defaultSc);

                for (final VisitStatement visit : visits) {
                    final Block b = visit.getBody().clone();

                    // transfroming subtree by replacing the identifiers and type 
                    new VisitTransform().start(b, visit.getComponent().getIdentifier().getToken(), "node");

                    b.addStatement(new BreakStatement());

                    if (visit.getComponent().type.toString().equals(shadowedType.toString())) {
                        // setting default if present
                        for (final Statement s : b.getStatements())
                            defaultSc.getBody().addStatement(s.clone());
                    } else {
                        // checking if shadow has a one-many mapping
                        if ((((BoaShadowType)visit.getComponent().type).getOneToMany(n.env)) == null) {
                            final LinkedList<Expression> listExp = new LinkedList<Expression>();
                            listExp.add(((BoaShadowType)visit.getComponent().type).getKindExpression(n.env));
                            //checking for many-one mapping
                            if(((BoaShadowType)visit.getComponent().type).getManytoOne(n.env,b) == null){

                                switchS.addCase(new SwitchCase(false, b, listExp));
                            
                            }else {
                                boolean flg = false;
                                //checking to see presence of kind in cases
                                for(SwitchCase sCase : switchS.getCases()){
                                   Selector s = (Selector)(sCase.getCase(0).getLhs().getLhs().getLhs().getLhs().getLhs().getOp(0));
                                    Identifier i =  s.getId();
                                    if(visit.getComponent().type.toString().toLowerCase().equals(i.getToken().toLowerCase())){
                                        flg = true;
                                        sCase.getBody().addStatement(((BoaShadowType)visit.getComponent().type).getManytoOne(n.env,b));
                                    }

                                }
                                if(!flg){
                                    Block manyToOneBlock = new Block();
                                    manyToOneBlock.addStatement(((BoaShadowType)visit.getComponent().type).getManytoOne(n.env,b));
                                    switchS.addCase(new SwitchCase(false, manyToOneBlock, listExp));
                                }
                            }
                        } else {

                            boolean flg = false;



                            for (final BoaShadowType shadow : (((BoaShadowType)visit.getComponent().type).getOneToMany(n.env))) {
                                final LinkedList<Expression> listExp = new LinkedList<Expression>();
                                Block toCombine = new Block();
                                List<SwitchCase> toRemove = new LinkedList<SwitchCase>();

                                Expression styKind = shadow.getKindExpression(n.env);
                                Selector test = (Selector)styKind.getLhs().getLhs().getLhs().getLhs().getLhs().getOp(0);
                                Identifier testi = test.getId();
                               
                                                           
                               

                                for(SwitchCase sCase : switchS.getCases()){
                                    Selector s = (Selector)(sCase.getCase(0).getLhs().getLhs().getLhs().getLhs().getLhs().getOp(0));
                                    Identifier i =  s.getId();
                                    if(testi.getToken().toLowerCase().equals(i.getToken().toLowerCase())){
                                       
                                        flg = true;
                                        Block temp = sCase.getBody();
                                        toRemove.add(sCase);

                                        for(  Map.Entry<BoaShadowType,BoaShadowType> iter : manytomanyMap.entrySet() ){
                                            BoaShadowType shadowty = iter.getKey();

                                            Expression styKindOld = shadowty.getKindExpression(n.env);
                                            Selector testOld = (Selector)styKindOld.getLhs().getLhs().getLhs().getLhs().getLhs().getOp(0);
                                            Identifier testiOld = testOld.getId();
                                            if(testiOld.getToken().toLowerCase().equals(i.getToken().toLowerCase())){
                                                    toCombine.addStatement(shadowty.getManytoOne(n.env,temp));
                                            }        

                                           
                                        }

                                        //switchS.getCases().remove(sCase);
                                        // TODO : resolve type from previous case kinds
                                        //sCase.getBody().addStatement(shadow.getManytoOne(n.env,b));

                                       

                                        toCombine.addStatement(shadow.getManytoOne(n.env,b));

                                        
                                        
                                        // FIXME : find solution to problem faced by getiing block from old case 
                                       // sCase.getBody().addStatement(shadow.getManytoOne(n.env,temp));
                                    }
                                }
                                manytomanyMap.put(shadow,(BoaShadowType)visit.getComponent().type);
                                if(toCombine.getStatementsSize() >0){
                                    listExp.add(styKind);
                                    switchS.getCases().removeAll(toRemove);
                                    switchS.addCase(new SwitchCase(false, toCombine, listExp));
                                }
                                
                                if(!flg){
                                   
                                    listExp.add(styKind);
                                    switchS.addCase(new SwitchCase(false, b, listExp));
                                }    
                            }
                        }
                    }
                }

                if (defaultSc.getBody().getStatementsSize() == 0 && wildcardBlock != null) {
                    // add wildcard to default
                    if (wildcardBlock != null) {
                        for (final Statement s : wildcardBlock.getStatements()) {
                            defaultSc.getBody().addStatement(s.clone());
                        }
                    }
                    defaultSc.getBody().addStatement(new BreakStatement());
                }

                afterTransformation.addStatement(switchS);

                // create a new VisitStatement and add everything to it
                final VisitStatement shadowedTypeVisit = new VisitStatement(isBefore, new Component(new Identifier("node"), new Identifier(shadowedType.toString())), afterTransformation);

                shadowedTypeVisit.env = n.env;
                shadowedTypeVisit.getComponent().env = n.env;
                shadowedTypeVisit.getComponent().getType().type = shadowedType;

                n.getBody().addStatement(shadowedTypeVisit);
            }
        }
    }

    protected class SubtreeEraser extends AbstractVisitorNoArgNoRet {
        private Deque<Expression> expressionStack = new ArrayDeque<Expression>();

        private boolean flag = false;
        private Deque<Boolean> flagStack = new ArrayDeque<Boolean>();

        private List<Node> ops = new ArrayList<Node>();
        private Deque<List<Node>> opsStack = new ArrayDeque<List<Node>>();

        // track nearest Expression node
        public void visit(final Expression n) {
            expressionStack.push(n);
            super.visit(n);
            expressionStack.pop();
        }

        @Override
        public void visit(final Factor n) {
            flagStack.push(flag);
            opsStack.push(ops);

            flag = false;
            ops = n.getOps();

            super.visit(n);

            ops = opsStack.pop();
            flag = flagStack.pop();
        }

        // replacing shadow type selectors
        @Override
        public void visit(final Selector n) {
            super.visit(n);

            final Factor fact = (Factor)n.getParent();

            if (!flag && fact.getOperand().type instanceof BoaShadowType) {
                final Expression parentExp = expressionStack.peek();

                // avoid replacing past the first selector
                flag = true;

                // get shadow type used
                final Identifier id = (Identifier)fact.getOperand();
                final BoaShadowType shadow = (BoaShadowType)fact.getOperand().type;

                // replace the selector
                final Expression replacement = (Expression)shadow.lookupCodegen(n.getId().getToken(), id.getToken(), parentExp.env);
                final ParenExpression paren = new ParenExpression(replacement);
                final Factor newFact = new Factor(paren);
                final Expression newExp = ASTFactory.createFactorExpr(newFact);

                newFact.env = parentExp.env;
                paren.type = replacement.type;
                newExp.type = paren.type;

                if (ops != null) {
                    for (int i = 1; i < ops.size(); i++) {
                        newFact.addOp(ops.get(i));
                        newExp.type = ops.get(i).type;
                    }
                }

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
                    final BoaShadowType shadow = (BoaShadowType)n.type;

                    // change the identifier
                    final Identifier id = (Identifier)n.getType();
                    id.setToken(shadow.shadowedName());

                    // update types
                    n.type = shadow.shadowedType;
                    n.env.set(n.getId().getToken(), shadow.shadowedType);
                }
            }
        }
    }
}
