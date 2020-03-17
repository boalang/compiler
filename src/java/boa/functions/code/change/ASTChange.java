package boa.functions.code.change;

import java.util.ArrayList;
import java.util.List;

public class ASTChange {
	
	private List<DeclNode> decls = new ArrayList<DeclNode>();
	private List<MethodNode> methods = new ArrayList<MethodNode>();
	private List<FieldNode> fields = new ArrayList<FieldNode>();
	
	public ASTChange() {
		
	}
	
	public List<DeclNode> getDecls() {
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