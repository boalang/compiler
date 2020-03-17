package boa.functions.code.change;

import java.util.ArrayList;
import java.util.List;

public class ASTChange {
	
	private List<DeclarationNode> decls = new ArrayList<DeclarationNode>();
	private List<MethodNode> methods = new ArrayList<MethodNode>();
	private List<FieldNode> fields = new ArrayList<FieldNode>();
	
	public ASTChange() {
		
	}
	
	public List<DeclarationNode> getDecls() {
		return decls;
	}

	public List<MethodNode> getMethods() {
		return methods;
	}

	public List<FieldNode> getFields() {
		return fields;
	}
	
	public int getSize() {
		return decls.size() + methods.size() + fields.size();
	}
}