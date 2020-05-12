package boa.functions.code.change;

import boa.functions.code.change.declaration.DeclTree;
import boa.functions.code.change.field.FieldTree;
import boa.functions.code.change.method.MethodTree;

public class Entity {
	
	long declRefEntityCount = 0;
	long declNoRefEntityCount = 0;
	
	long fieldRefEntityCount = 0;
	long fieldNoRefEntityCount = 0;
	
	long methodRefEntityCount = 0;
	long methodNoRefEntityCount = 0;
	
	ChangeDataBase db = null;

	public Entity(ChangeDataBase db) {
		this.db = db;
	}

	public long[] execute() {
		for (DeclTree declTree : db.declForest.values()) {
			if (declTree.getRefNodes().size() == 0)
				declNoRefEntityCount += declTree.getDeclNodes().size();
			else
				declRefEntityCount += declTree.getRefNodes().size();
		}
		
		for (FieldTree fieldTree : db.fieldForest.values()) {
			if (fieldTree.getRefNodes().size() == 0)
				fieldNoRefEntityCount += fieldTree.getFieldNodes().size();
			else
				fieldRefEntityCount += fieldTree.getRefNodes().size();
		}
		
		for (MethodTree methodTree : db.methodForest.values()) {
			if (methodTree.getRefNodes().size() == 0)
				methodNoRefEntityCount += methodTree.getMethodNodes().size();
			else
				methodRefEntityCount += methodTree.getRefNodes().size();
		}
		
		return new long[] { 
				declRefEntityCount, declNoRefEntityCount,
				fieldRefEntityCount, fieldNoRefEntityCount,
				methodRefEntityCount, methodNoRefEntityCount
		};
	}
	
	

}
