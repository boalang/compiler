package boa.functions.refactoring;

import java.util.HashMap;
import java.util.List;

import boa.types.Code.CodeRefactoring;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.ChangeKind;

public class FileChangeLinkedLists extends FileLinkedLists {

	protected HashMap<String, String> fileNameToListId = new HashMap<String, String>();
	
	@Override
	protected boolean update(ChangedFile cf, Revision r) {
		if (!fileNameToListId.containsKey(cf.getName())) {
			String newListId = null;
			if (cf.getChange().equals(ChangeKind.RENAMED)) {
				if (!fileNameToListId.containsKey(cf.getPreviousNames(0)))
					System.out.println("FileLinkedLists ERR 1");
				newListId = fileNameToListId.get(cf.getPreviousNames(0));
			} else if (cf.getChange().equals(ChangeKind.ADDED)) {
				newListId = Integer.toString(lists.size());
				lists.put(newListId, new FileLinkedList(newListId));
			} else {
				System.out.println("FileLinkedLists ERR 2");
			}
			fileNameToListId.put(cf.getName(), newListId);
		}
		String listId = fileNameToListId.get(cf.getName());
		lists.get(listId).addLink(cf, r);
		return true;
	}

	@Override
	protected boolean update(List<CodeRefactoring> refs) {
		for (CodeRefactoring ref : refs) {
			String beforeFilePath = ref.getLeftSideLocations(0).getFilePath();
			if (!fileNameToListId.containsKey(beforeFilePath)) {
				System.out.println("FileLinkedLists ERR 3: " + beforeFilePath);
			}
			lists.get(fileNameToListId.get(beforeFilePath)).add(ref);
		}
		return true;
	}

}