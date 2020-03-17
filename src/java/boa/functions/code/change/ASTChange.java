package boa.functions.code.change;

import java.util.ArrayList;
import java.util.List;

public class ASTChange {
	
	private List<DeclarationNode> decls = new ArrayList<DeclarationNode>();
	private List<DeclarationNode> methods = new ArrayList<DeclarationNode>();
	private List<DeclarationNode> fields = new ArrayList<DeclarationNode>();
	
	public ASTChange() {
		
	}
	
	public List<DeclarationNode> getDecls() {
		return decls;
	}

	public List<DeclarationNode> getMethods() {
		return methods;
	}

	public List<DeclarationNode> getFields() {
		return fields;
	}
}
