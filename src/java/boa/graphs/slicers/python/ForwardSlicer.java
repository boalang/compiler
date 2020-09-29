package boa.graphs.slicers.python;

import boa.types.Ast.ASTRoot;

public class ForwardSlicer {
	
	ASTRoot root;
	
	public ForwardSlicer(ASTRoot _root)
	{
		this.root=_root;
		
		SymbolTableGenerator st=new SymbolTableGenerator();
		try {
			st.visit(this.root);
			SymbolTable.printSymbolTable();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
