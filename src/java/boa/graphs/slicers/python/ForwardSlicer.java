package boa.graphs.slicers.python;

import boa.types.Ast.ASTRoot;

public class ForwardSlicer {
	
	ASTRoot root;
	
	public ForwardSlicer(ASTRoot _root,String[] moduleFilter, String[] filterCriteria)
	{
		this.root=_root;
		
		Status.setLibraryFilter(filterCriteria);
		Status.setModuleFilter(moduleFilter);
		
		SymbolTableGenerator st=new SymbolTableGenerator();
		NameResolver nr=new NameResolver();
		
		try {
			st.visit(this.root);
			
			SymbolTable.printSymbolTable();
			Status.printMap(Status.importMap);
			
			nr.visit(this.root);
			Status.printIntegerMap(Status.aliasName);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
