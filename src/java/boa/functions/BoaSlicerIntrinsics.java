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
import boa.graphs.slicers.python.ForwardSlicer;
import boa.graphs.slicers.python.Status;
import boa.types.Ast.ASTRoot;
import boa.types.Ast.Statement;
import boa.types.Code.CodeRepository;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.ChangeKind;

import static boa.functions.BoaIntrinsics.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static boa.functions.BoaAstIntrinsics.*;

/**
 * Boa functions for working with control flow graphs.
 *
 * @author sayem
 */
public class BoaSlicerIntrinsics {
	
	private static final ASTRoot emptyAst = ASTRoot.newBuilder().build();
	
	@SuppressWarnings("unchecked")
//	@FunctionSpec(name = "getdiff", returnType = "ASTRoot", formalParameters = { "ChangedFile","ChangedFile","string" })
//	public static ASTRoot getdiff(final ChangedFile current,final ChangedFile previous
//			,final String direction) {
//		ASTRoot.Builder modifiedCurrentAst = ASTRoot.newBuilder();
//		ASTRoot.Builder modifiedPreviousAst = ASTRoot.newBuilder();
//		
//		ASTRoot currentAst=getast(current);
//
//		if (currentAst.getNamespacesCount()==0)
//			return emptyAst;
//		
//		ASTRoot previousAst=getast(previous);
//		
//		if (previousAst.getNamespacesCount()==0)
//			return emptyAst;
//		
//		try {
//			
//			boa.datagen.treed.generic.TreedMapper tm = new boa.datagen.treed.generic.TreedMapper(
//					previousAst.getNamespaces(0), currentAst.getNamespaces(0));
//
//			tm.map();
//			
//			
//			modifiedCurrentAst.addNamespaces(tm.getCurrentChanges());
//			modifiedPreviousAst.addNamespaces(tm.getPreviousChanges());
//			
//			tm.clear();
//			
//			tm=null;
//			
//		} catch (Exception e1) {
//			e1.printStackTrace();
//			return emptyAst;
//		}
//		
//		if(direction.equalsIgnoreCase("forward"))
//			return modifiedCurrentAst.build();
//		return modifiedPreviousAst.build();
//	}
	
//	@SuppressWarnings("unchecked")
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
//			new Exception(current.getName()+": "+e1.getMessage()).printStackTrace();
			return emptyAst;
		}
		
		if(direction.equalsIgnoreCase("forward"))
			return currentAst;
		return previousAst;
	}
	
	@FunctionSpec(name = "get_previous_file", returnType = "ChangedFile", formalParameters = { "CodeRepository",
			"Revision", "ChangedFile" })
	public static ChangedFile getPreviousFile(CodeRepository cr, Revision rev, ChangedFile cf) {
//		System.out.println("Retreiving old revision from: "+rev.getId());
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
	
	@FunctionSpec(name = "getmodification", returnType = "ASTRoot", formalParameters = { "ASTRoot", "array of string", "array of string" })
	public static ASTRoot getmodification(final ASTRoot changedFile, String[] moduleFilter, 
			String[] filterCriteria) {
		
		if(changedFile.getNamespacesCount()==0) return changedFile;
		
		ForwardSlicer slicer=new ForwardSlicer(changedFile, moduleFilter, filterCriteria, true);
		ASTRoot retAst= slicer.initiateVisit(true);
		
		if(retAst==null) return emptyAst;
		
		return retAst;
		
	}
	
	@FunctionSpec(name = "getmodificationbackward", returnType = "ASTRoot", formalParameters = { "ASTRoot", "array of string", "array of string" })
	public static ASTRoot getmodificationbackward(final ASTRoot changedFile, String[] moduleFilter, 
			String[] filterCriteria) {
		
		if(changedFile.getNamespacesCount()!=2) return changedFile;
		
		Status.BACKWARD=true;
		
		ForwardSlicer slicer=new ForwardSlicer(changedFile, moduleFilter, filterCriteria, true);
		ASTRoot retAst= slicer.initiateVisit(true);
		
		if(retAst==null) return emptyAst;
		
		return retAst;
		
	}
	
	@FunctionSpec(name = "getpythonimportmodules", returnType = "array of string", formalParameters = { "ASTRoot",  "array of string" })
	public static String[] getpythonimportmodules(final ASTRoot ast, String[] moduleFilter) {
		if(ast==null || ast.getNamespacesCount()==0 ||
				ast.getNamespaces(0).getImportsCount()==0) return new String[0];

		Set<String> result=new HashSet<String>();
		for (String imp : ast.getNamespaces(0).getImportsList()) {
			if (imp.matches("^\\..*") || imp.equals("")) // ignore relative imports
				continue;

			for (String lib : moduleFilter) {
				if (imp.matches("^" + lib + ".*") || imp.matches("^from " + lib + ".*")) {
					result.add(lib);
					break;
				}
			}
		}
		return result.toArray(new String[0]);
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
