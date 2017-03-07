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

import boa.compiler.visitors.AbstractVisitorNoArg;

import boa.compiler.SymbolTable;

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

import boa.types.BoaShadowType;
import boa.types.proto.StatementProtoTuple;
import boa.types.BoaTuple;
/**
 * Converts a tree using shadow types into a tree without shadow types.
 *
 * @author rdyer
 * @author kaushin
 */
public class ShadowTypeEraser extends AbstractVisitorNoArg {

	
	private SymbolTable env;
	private LinkedList<Expression> expressionStack = new LinkedList<Expression>();

	// Class to help walkthrough replacement sub-trees to make nessasary changes
	private class Replace extends AbstractVisitorNoArg{
		
		protected String CallingVariableName ;
	
		protected void initialize() { 
		}

		public void start(final Node n, String CallingVariableName) {
			initialize();
			this.CallingVariableName = CallingVariableName;
			n.accept(this);
		}

		public void visit(final Identifier n) {
			super.visit(n);
		
			System.out.println("---->"+n.getToken() + " string: " + CallingVariableName);
			if(n.getToken().equals("${0}")){
				System.out.println("----->"+n.getToken() + " string: " + CallingVariableName);
				n.setToken(CallingVariableName);		
			}	
		}
	}

	// Populating Stack of expressions for later use
	public void visit(final Expression n) {
		expressionStack.push(n);
		super.visit(n);
		expressionStack.pop();
	}

	// Replacing selector trees of shadow types all teh way upto its expression node
	@Override
	public void visit(final Selector n) {
		super.visit(n);

		env = n.env;
		Factor test =  (Factor)n.getParent();
		if ( test.getOperand().type instanceof BoaShadowType){
			//get parent Expression 
			Expression parentExp = expressionStack.peek();
				
			
			// Getting Shadow type used
			Identifier id = (Identifier)test.getOperand();
			System.out.println(id.getToken());
			BoaShadowType typeUsed = (BoaShadowType)env.get(id.getToken());	
			Expression replacement = (Expression)typeUsed.lookupCodegen(n.getId().getToken()).clone();

			//working through to all identifiers to replace required identitiers!!
			Replace rep = new Replace();
			rep.start(replacement,id.getToken());

			//use replaceStatement or the like of expression node
			parentExp.replaceExpression(parentExp,replacement);
		}
	}

	// Changing type of variable used in before or after statement to type Statement
	
	@Override
	public void visit(final Component n) {
		super.visit(n);
		if(n.type instanceof BoaShadowType){
			//Change the Identifier in the ast
			BoaShadowType typeUsed = (BoaShadowType)env.get(n.getType().toString());
			Identifier temp = (Identifier)n.getType();
			System.out.println("Shadow Type Before/After Found = "+ temp.getToken());
			temp.setToken(typeUsed.getDeclarationIdentifierEraser);		
		}
	}
	// Changing type of variable used in before or after statement to type Statement
	@Override
	public void visit(final VarDeclStatement n) {
		super.visit(n);
		//get Symbol Table
		env = n.env;
		if (n.hasType()){
			if(n.type instanceof BoaShadowType){

				//Change the Identifier in the ast
				BoaShadowType typeUsed = (BoaShadowType)env.get(n.getType().toString());
				Identifier temp = (Identifier)n.getType();
				System.out.println("Shadow Type Declaration Found = "+ temp.getToken());
				temp.setToken(typeUsed.getDeclarationIdentifierEraser);
				
				
				//Change the Type in the SymbolTable
				env.setType(n.getId().getToken(),typeUsed.getDeclarationSymbolTableEraser);
				n.type = typeUsed.getDeclarationSymbolTableEraser;
			}
		}	
	}
}
