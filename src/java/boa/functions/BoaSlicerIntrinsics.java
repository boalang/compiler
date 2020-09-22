/*
 * Copyright 2019, Hridesh Rajan, Ganesha Upadhyaya, Ramanathan Ramu, Robert Dyer, Che Shian Hung
 *                 Bowling Green State University
 *                 and Iowa State University of Science and Technology
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
package boa.functions;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.eclipse.dltk.ast.ASTNode;
import org.eclipse.dltk.python.parser.ast.PythonModuleDeclaration;

import boa.datagen.treed.python.BoaToPythonConverter;
import boa.datagen.util.NewPythonVisitor;
import boa.types.Ast.ASTRoot;
import boa.types.Code.CodeRepository;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.ChangeKind;

import static boa.functions.BoaIntrinsics.*;
import static boa.functions.BoaAstIntrinsics.*;

/**
 * Boa functions for working with control flow graphs.
 *
 * @author sayem
 */
public class BoaSlicerIntrinsics {
	
	private static final ASTRoot emptyAst = ASTRoot.newBuilder().build();
	
	@SuppressWarnings("unchecked")
	@FunctionSpec(name = "getdiff", returnType = "ASTRoot", formalParameters = { "ChangedFile","ChangedFile","string" })
	public static ASTRoot getdiff(final ChangedFile current,final ChangedFile previous
			,final String direction) {
		ASTRoot currentAst=getast(current);

		if (currentAst.getNamespacesCount()==0)
			return emptyAst;
		
		ASTRoot previousAst=getast(previous);
		
		if (previousAst.getNamespacesCount()==0)
			return emptyAst;
		
		PythonModuleDeclaration currentModule,previousModule;
		BoaToPythonConverter boaToPythonConverter=new BoaToPythonConverter();
		try {
			
			currentModule=(PythonModuleDeclaration) 
					boaToPythonConverter.visit(currentAst.getNamespaces(0));
			buildChildParentBonding(currentModule, null);
			
			previousModule=(PythonModuleDeclaration) 
					boaToPythonConverter.visit(previousAst.getNamespaces(0));
			buildChildParentBonding(previousModule, null);
			
			boa.datagen.treed.python.TreedMapper tm = new boa.datagen.treed.python.TreedMapper(
					previousModule, currentModule);

			tm.map();
			
			tm.clear();
			tm=null;
			
			currentAst=pythonAstToBoaAST(currentModule, currentAst.getNamespaces(0).getName());
			previousAst=pythonAstToBoaAST(previousModule, previousAst.getNamespaces(0).getName());

		} catch (Exception e1) {
			e1.printStackTrace();
			return emptyAst;
		}
		
		if(direction.equalsIgnoreCase("forward"))
			return currentAst;
		return previousAst;
	}
	
	@FunctionSpec(name = "get_previous_file", returnType = "ChangedFile", formalParameters = { "CodeRepository",
			"Revision", "ChangedFile" })
	public static ChangedFile getPreviousFile(CodeRepository cr, Revision rev, ChangedFile cf) {
		String prevName = cf.getChange() == ChangeKind.RENAMED ? cf.getPreviousNames(0) : cf.getName();
		rev = rev.getParentsCount() == 0 ? null : getRevision(cr, rev.getParents(0));
		while (rev != null) {
			int l = 0, r = rev.getFilesCount() - 1;
			while (l <= r) {
				int mid = (l + r) / 2;
				String fileName = rev.getFiles(mid).getName();
				if (fileName.equals(prevName))
					return rev.getFiles(mid);
				else if (prevName.compareTo(fileName) > 0)
					l = mid + 1;
				else
					r = mid - 1;
			}
			// look for first-parent branch
			rev = rev.getParentsCount() == 0 ? null : getRevision(cr, rev.getParents(0));
		}
		return null;
	}
	
	public static ASTRoot pythonAstToBoaAST(final PythonModuleDeclaration module, final String path) throws Exception {
		NewPythonVisitor visitor = new NewPythonVisitor();
		visitor.enableDiff=true;
		final ASTRoot.Builder ast = ASTRoot.newBuilder();
		ast.addNamespaces(visitor.getNamespace(module, path));
		return ast.build();

	}
	public static void buildChildParentBonding(ASTNode current, ASTNode parent)
	{
		current.setParent(parent);
		for(ASTNode child: current.getChilds())
		{
			buildChildParentBonding(child, current);
		}
	}
	
}