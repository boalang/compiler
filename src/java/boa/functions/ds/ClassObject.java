package boa.functions.ds;

import boa.types.Ast.Declaration;

import java.util.*;

public class ClassObject {
	public String fileName;
	public List<String> imports;
	public Declaration decl;

	public ClassObject(String fileName, List<String> imports, Declaration d) {
		this.fileName = fileName;
		this.imports = imports;
		this.decl = d;
	}
}