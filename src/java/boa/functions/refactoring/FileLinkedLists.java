package boa.functions.refactoring;

import java.util.HashMap;
import java.util.List;

import boa.types.Code.CodeRefactoring;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;

public class FileLinkedLists {
	protected HashMap<String, FileLinkedList> lists = new HashMap<String, FileLinkedList>();
	
	public boolean update(List<ChangedFile> cfs, Revision r, List<CodeRefactoring> refs) {
		for (ChangedFile cf : r.getFilesList())
			update(cf, r);
		update(refs);
		return true;
	}

	protected boolean update(ChangedFile cf, Revision r) {
		if (!lists.containsKey(cf.getName()))
			lists.put(cf.getName(), new FileLinkedList(cf.getName()));
		lists.get(cf.getName()).addLink(cf, r);
		return true;
	}

	protected boolean update(List<CodeRefactoring> refs) {
		for (CodeRefactoring ref : refs) {
			String beforeFilePath = ref.getLeftSideLocations(0).getFilePath();
			if (!lists.containsKey(beforeFilePath)) {
				System.out.println("FileLinkedLists ERR: " + beforeFilePath);
			}
			lists.get(beforeFilePath).add(ref);
		}
		return true;
	}

	public HashMap<String, FileLinkedList> getLists() {
		return lists;
	}

}

