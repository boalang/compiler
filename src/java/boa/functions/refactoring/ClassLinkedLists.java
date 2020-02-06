package boa.functions.refactoring;

import java.util.HashMap;
import java.util.List;

import boa.types.Code.CodeRefactoring;
import boa.types.Code.Revision;

public class ClassLinkedLists {
	protected HashMap<String, ClassLinkedList> lists = new HashMap<String, ClassLinkedList>();
	protected HashMap<String, String> classIdToListId = new HashMap<String, String>();
	
	public boolean update(Revision r, List<CodeRefactoring> refs) {
		
		return true;
	}
}
