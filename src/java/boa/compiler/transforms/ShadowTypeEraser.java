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
import boa.compiler.ast.Comparison;
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
import boa.compiler.ast.statements.IfStatement;
import boa.compiler.ast.statements.StopStatement;
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
    /** The identifier used for visit() argument node */
    public static final String NODE_ID = "node";

    @Override
    public void visit(final VisitorExpression n) {
        super.visit(n);

        // first step to collect and transform all VisitStatements
        new VisitorReplace().start(n);
    }

    @Override
    public void start(final Node n) {
        super.start(n);

        // second step to transform all the sub trees
        new SelectorTransformer().start(n);
        new SubtreeEraser().start(n);
    }

    protected class VisitorReplace extends AbstractVisitorNoArgNoRet {
        private final Deque<VisitStatement> shadowVisitStack   = new ArrayDeque<VisitStatement>();
        private final Deque<VisitorExpression> visitorExpStack = new ArrayDeque<VisitorExpression>();

        private final HashMap<BoaProtoTuple, LinkedList<VisitStatement>> beforeShadowedMap = new HashMap<BoaProtoTuple, LinkedList<VisitStatement>>();
        private final HashMap<BoaProtoTuple, LinkedList<VisitStatement>> afterShadowedMap  = new HashMap<BoaProtoTuple, LinkedList<VisitStatement>>();
        private final HashMap<BoaProtoTuple, LinkedList<VisitStatement>> shadowedMap       = new HashMap<BoaProtoTuple, LinkedList<VisitStatement>>();

        private final HashMap<String, BoaShadowType> manytomanyMap = new HashMap< String, BoaShadowType>();

        private Block wildcardBlock = null;
        private boolean shadowedTypePresent = false;

        protected class VisitTransform extends AbstractVisitorNoArgNoRet {
            private String oldId;
            private String newId;

            public void start(final Node n, final String oldId, final String newId) {
                this.oldId = oldId;
                this.newId = newId;
                start(n);
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
                    n.type = n.getType().type = shadow.shadowedType();
                    n.env.set(n.getIdentifier().getToken(), n.type);
                }
            }
        }

        private boolean nested = false;

        @Override
        public void visit(final VisitStatement n) {
            if (n.hasComponent()) {
                n.getComponent().accept(this);
            } else {
                wildcardBlock = n.getBody();
            }
        }

        @Override
        public void visit(final Component n) {
            if (n.getParent() instanceof VisitStatement) {
                final VisitStatement parentVisit = (VisitStatement)n.getParent();

                final BoaProtoTuple key;

                if (n.type instanceof BoaShadowType) {
                    shadowVisitStack.push(parentVisit);
                    final BoaShadowType shadow = (BoaShadowType)n.type;

                    key = shadow.shadowedType();
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
            if (nested) return;
            nested = true;

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

                transformVisitor(n, beforeShadowedMap, true);
                transformVisitor(n, afterShadowedMap, false);
            }

            // just clearing out variables
            shadowVisitStack.clear();
            shadowedTypePresent = false;
            wildcardBlock = null;
        }

        public void transformVisitor (final VisitorExpression n, final HashMap<BoaProtoTuple, LinkedList<VisitStatement>> shadowedMap, final boolean isBefore) {
            for (final Map.Entry<BoaProtoTuple, LinkedList<VisitStatement>> entry : shadowedMap.entrySet()) {
                final Block afterTransformation = new Block();

                final BoaProtoTuple shadowedType = entry.getKey();
                LinkedList<VisitStatement> visits =  entry.getValue();

                if (visits.size() == 1 && !(visits.get(0).getComponent().type instanceof BoaShadowType)) {
                    n.getBody().addStatement(visits.get(0).clone());
                    continue;
                }

                final Factor f = new Factor(ASTFactory.createIdentifier(NODE_ID, n.env));
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

                    // transforming subtree by replacing the identifiers and type
                    new VisitTransform().start(b, visit.getComponent().getIdentifier().getToken(), NODE_ID);

                    if (visit.getComponent().type.toString().equals(shadowedType.toString())) {
                        // setting default if present
                        for (final Statement s : b.getStatements())
                            defaultSc.getBody().addStatement(s.clone());
                    } else {
                        // checking if shadow has a one-many mapping
                        if ((((BoaShadowType)visit.getComponent().type).getOneToMany(n.env)).size() == 0) {
                            final LinkedList<Expression> listExp = new LinkedList<Expression>();
                            listExp.add(((BoaShadowType)visit.getComponent().type).getKindExpression(n.env));

                            // checking for many-one mapping
                            if (((BoaShadowType)visit.getComponent().type).getManytoOne(n.env, b) == null) {
                                switchS.addCase(new SwitchCase(false, b.clone(), listExp));
                            } else {
                                boolean flg = false;

                                // checking to see presence of kind in cases
                                for (final SwitchCase sCase : switchS.getCases()) {
                                    final Selector s = (Selector)(sCase.getCase(0).getLhs().getLhs().getLhs().getLhs().getLhs().getOp(0));
                                    final Identifier i = s.getId();

                                    if (visit.getComponent().type.toString().toLowerCase().equals(i.getToken().toLowerCase())) {
                                        flg = true;
                                        sCase.getBody().addStatement(((BoaShadowType)visit.getComponent().type).getManytoOne(n.env, b));
                                    }
                                }

                                if (!flg) {
                                    final Block manyToOneBlock = new Block();
                                    manyToOneBlock.addStatement(((BoaShadowType)visit.getComponent().type).getManytoOne(n.env, b));
                                    switchS.addCase(new SwitchCase(false, manyToOneBlock.clone(), listExp));
                                }
                            }
                        } else {
                            boolean flg = false;
                            // this for loop handles many-many cases
                            for (final Expression styKind : (((BoaShadowType)visit.getComponent().type).getOneToMany(n.env))) {
                                final LinkedList<Expression> listExp = new LinkedList<Expression>();
                                final Block toCombine = new Block();
                                final List<SwitchCase> toRemove = new LinkedList<SwitchCase>();

                                final Selector test = (Selector)styKind.getLhs().getLhs().getLhs().getLhs().getLhs().getOp(0);
                                final Identifier testi = test.getId();
                                // for each switch case see if the kind expression matches
                                if (((BoaShadowType)visit.getComponent().type).getManytoOne(n.env, b) != null) {
                                    for (final SwitchCase sCase : switchS.getCases()) {
                                        final Selector s = (Selector)(sCase.getCase(0).getLhs().getLhs().getLhs().getLhs().getLhs().getOp(0));
                                        final Identifier i = s.getId();
                                        // for each switch case see if the kind expression matches
                                        if (testi.getToken().toLowerCase().equals(i.getToken().toLowerCase())) {
                                            flg = true;
                                            final Block temp = sCase.getBody();
                                            // add all cases that match to be removed later
                                            toRemove.add(sCase);
                                            // check if the kind is already there if so check the manytomanyMap for already existing cases
                                            // and get the appropriate tranformantion (eg . if(isinfix))
                                            for (final Map.Entry<String, BoaShadowType> iter : manytomanyMap.entrySet()) {
                                                final String shadowtyKind = iter.getKey();
                                                final BoaShadowType higherType = iter.getValue();

                                                if (shadowtyKind.toLowerCase().equals(i.getToken().toLowerCase())) {
                                                    toCombine.addStatement(higherType.getManytoOne(n.env, temp));
                                                }
                                            }

                                            toCombine.addStatement(((BoaShadowType)visit.getComponent().type).getManytoOne(n.env, b));
                                        }
                                    }

                                    if (!flg) {
                                        final Block manyToOneBlock = new Block();
                                        listExp.add(styKind);
                                        manyToOneBlock.addStatement(((BoaShadowType)visit.getComponent().type).getManytoOne(n.env, b));
                                        switchS.addCase(new SwitchCase(false, manyToOneBlock.clone(), listExp));
                                    }
                                }

                                // add cases to a map so that we can resolve their origin type (eg . infix vs prefix)
                                manytomanyMap.put(testi.getToken(), (BoaShadowType)visit.getComponent().type);

                                if (toCombine.getStatementsSize() > 0 && flg) {
                                    listExp.add(styKind);
                                    switchS.getCases().removeAll(toRemove);
                                    switchS.addCase(new SwitchCase(false, toCombine.clone(), listExp));
                                }

                                if (((BoaShadowType)visit.getComponent().type).getManytoOne(n.env, b) == null && !flg) {
                                    listExp.add(styKind);
                                    switchS.addCase(new SwitchCase(false, b.clone(), listExp));
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
                }

                // adding breaks
                if (!lastStatementIsStop(defaultSc.getBody())) {
                    defaultSc.getBody().addStatement(new BreakStatement());
                }

                final List<SwitchCase> listOfCases = switchS.getCases();
                for (final SwitchCase scase : listOfCases) {
                    if (!lastStatementIsStop(scase.getBody())) {
                        scase.getBody().addStatement(new BreakStatement());
                    }
                }

                afterTransformation.addStatement(switchS);

                // create a new VisitStatement and add everything to it
                final VisitStatement shadowedTypeVisit = new VisitStatement(isBefore, new Component(new Identifier(NODE_ID), new Identifier(shadowedType.toString())), afterTransformation);

                shadowedTypeVisit.env = n.env;
                shadowedTypeVisit.getComponent().env = n.env;
                shadowedTypeVisit.getComponent().getType().type = shadowedType;

                n.getBody().addStatement(shadowedTypeVisit);
            }
        }

        protected boolean lastStatementIsStop(final Statement s) {
            if (s instanceof StopStatement)
                return true;

            if (s instanceof IfStatement) {
                final IfStatement ifs = (IfStatement)s;
                if (ifs.hasElse())
                    return lastStatementIsStop(ifs.getBody()) && lastStatementIsStop(ifs.getElse());
                return false;
            }

            if (s instanceof Block) {
                final List<Statement> stmts = ((Block)s).getStatements();
                if (stmts.size() > 0)
                    return lastStatementIsStop(stmts.get(stmts.size() - 1));
            }

            return false;
        }
    }

    protected class SelectorTransformer extends AbstractVisitorNoArgNoRet {
        private boolean flag = false;
        private final Deque<Boolean> flagStack = new ArrayDeque<Boolean>();

        @Override
        public void visit(final Factor n) {
            flagStack.push(flag);
            flag = false;

            super.visit(n);

            flag = flagStack.pop();
        }

        // replacing shadow type selectors
        @Override
        public void visit(final Selector n) {
            super.visit(n);

            final Factor fact = (Factor)n.getParent();

            if (!flag && fact.getOperand().type instanceof BoaShadowType) {
                // avoid replacing past the first selector
                flag = true;

                // get shadow type used
                final BoaShadowType shadow = (BoaShadowType)fact.getOperand().type;

                // replace the selector
                final Node replacement = shadow.lookupCodegen(n.getId().getToken(), fact, n.env);
                final int idx = fact.getOps().indexOf(n);

                if (replacement instanceof Selector) {
                    fact.getOps().set(idx, replacement);
                } else if (((Factor)replacement).getOperand() == null) {
                    fact.getOps().set(idx, ((Factor)replacement).getOp(0));
                    fact.getOps().add(idx + 1, ((Factor)replacement).getOp(1));
                } else {
                    // TODO
                    //fact.getParent().setLhs(replacement)
                    //for (int i = idx + 1; . .; i++)
                    //    replacement.addOp(fact.getOps().get(i))
                }

                /*
                final ParenExpression paren = new ParenExpression(replacement);
                final Factor newFact = new Factor(paren);
                final Expression newExp = ASTFactory.createFactorExpr(newFact);

                newFact.env = parentExp.env;
                paren.type = replacement.type;
                newExp.type = paren.type;

                for (int i = 1; i <fact.getOps().size(); i++) {
                    newFact.addOp(fact.getOps().get(i));
                    newExp.type = fact.getOps().get(i).type;
                }
                */
            }
        }
    }

    protected class SubtreeEraser extends AbstractVisitorNoArgNoRet {
        @Override
        public void visit(final Identifier n) {
            super.visit(n);

            if (n.type instanceof BoaShadowType) {
                n.type = ((BoaShadowType)n.type).shadowedType();
            }
        }

        // removing shadow types in before/after visit
        @Override
        public void visit(final Component n) {
            super.visit(n);

            if (n.type instanceof BoaShadowType) {
                final BoaShadowType shadow = (BoaShadowType)n.type;

                // change the identifier
                ((Identifier)n.getType()).setToken(shadow.shadowedName());

                // update types
                n.type = n.getType().type = shadow.shadowedType();
                n.env.set(n.getIdentifier().getToken(), n.type);
            }
        }

        // removing shadow types in variable declarations
        @Override
        public void visit(final VarDeclStatement n) {
            super.visit(n);

            if (n.type instanceof BoaShadowType) {
                final BoaShadowType shadow = (BoaShadowType)n.type;

                // change the identifier
                if (n.hasType()) {
                    ((Identifier)n.getType()).setToken(shadow.shadowedName());
                    n.getType().type = shadow.shadowedType();
                }

                // update types
                n.type = shadow.shadowedType();
                n.env.set(n.getId().getToken(), shadow.shadowedType());
            }
        }
    }
}
