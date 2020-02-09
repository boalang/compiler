package boa.functions.refactoring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import boa.types.Code.CodeRefactoring;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.ChangeKind;

public class FileChangeLinkedLists {

	private HashMap<String, FileLinkedList> lists = new HashMap<String, FileLinkedList>();
	private HashMap<String, String> fileNameToListId = new HashMap<String, String>();
	private int refactoringCount = 0;
	private int fileChangeCount = 0;
	
	public FileChangeLinkedLists() {}
	
	public FileChangeLinkedLists(HashMap<String, FileLinkedList> lists, HashMap<String, String> fileNameToListId) {
		this.lists = lists;
		this.fileNameToListId = fileNameToListId;
	}

	public boolean addAll(List<ChangedFile> cfs, Revision r, int revIdx, List<CodeRefactoring> refs) {
		for (int i = 0; i < cfs.size(); i++)
			update(new FileNode(revIdx, r, i));
		update(refs, revIdx);
		return true;
	}
	
	private boolean update(FileNode fn) {
		ChangedFile cf = fn.getChangedFile();
		if (!fileNameToListId.containsKey(cf.getName())) {
			String newListId = null;
			if (cf.getChange().equals(ChangeKind.RENAMED)) {
//				System.out.println(cf.getPreviousNames(0));
				String s = "org.ant4eclipse.ant.pydt/src/org/ant4eclipse/ant/pydt/usedargs/UsedProjectsArgumentComponent.java";
				if (cf.getPreviousNames(0).equals(s)) {
					System.err.println("!!!!!!!!!!!!!!!!!!!!");
				}
				
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
//		else if (cf.getChange().equals(ChangeKind.RENAMED))
//			System.err.println(cf.getPreviousNames(0));
		
		String listId = fileNameToListId.get(cf.getName());
		FileLinkedList list = lists.get(listId);
		
		if (list.getFileNodes().size() != 0
				&& list.getLastFileNode().getChangedFile().getChange().equals(ChangeKind.DELETED)) {
			
			System.out.println(fn.getChangedFile().getChange());
		}
		
		list.addLink(fn);
		return true;
	}

	private boolean update(List<CodeRefactoring> refs, int revIdx) {
		for (CodeRefactoring ref : refs) {
			String beforeFilePath = ref.getLeftSideLocations(0).getFilePath();
			if (!fileNameToListId.containsKey(beforeFilePath)) {
				System.out.println("FileLinkedLists ERR 3: " + beforeFilePath);
			}
			lists.get(fileNameToListId.get(beforeFilePath)).add(ref, revIdx);
		}
		return true;
	}
	
	public HashMap<Integer, List<FileLinkedList>> getRefCommitIdxToFileLinkedListsMap() {
		HashMap<Integer, List<FileLinkedList>> map = new HashMap<Integer, List<FileLinkedList>>();
		for (FileLinkedList list : lists.values()) {
			for (Entry<Integer, Integer> entry : list.getRefCommitIdxToFileListLocMap().entrySet()) {
				int refCommitIdx = entry.getKey();
				if (!map.containsKey(refCommitIdx))
					map.put(refCommitIdx, new ArrayList<FileLinkedList>());
				map.get(refCommitIdx).add(list);
			}
		}
		return map;
	}
	
	public HashMap<Integer, List<FileLinkedList>> getRefCommitIdToFileLinkedListsMap(Set<Integer> refCommitIdxs) {
		HashMap<Integer, List<FileLinkedList>> map = new HashMap<Integer, List<FileLinkedList>>();
		for (FileLinkedList list : lists.values()) {
			for (int refCommitIdx : refCommitIdxs) {
				if (list.getCommitIdxToFileListLocMap().containsKey(refCommitIdx)) {
					if (!map.containsKey(refCommitIdx))
						map.put(refCommitIdx, new ArrayList<FileLinkedList>());
					map.get(refCommitIdx).add(list);
				}
			}
		}
		return map;
	}
	
	public int getRefactoringCount() {
		if (refactoringCount == 0)
			for (FileLinkedList l : lists.values())
				refactoringCount += l.getRefactoringCount();
		return refactoringCount;
	}
	
	public int getFileChangeCount() {
		if (fileChangeCount == 0)
			for (FileLinkedList l : lists.values())
				fileChangeCount += l.getFileNodes().size();
		return fileChangeCount;
	}

	public FileChangeLinkedLists getNotRefactoringInvolvedLists() {
		HashMap<String, FileLinkedList> res = new HashMap<String, FileLinkedList>();
		for (Entry<String, FileLinkedList> entry : lists.entrySet()) {
			String key = entry.getKey();
			FileLinkedList list = entry.getValue();
			if (list.getRefactoringCount() == 0)
				res.put(key, list);
		}
		return new FileChangeLinkedLists(res, this.fileNameToListId);
	}
	
	public FileChangeLinkedLists getRefactoringInvolvedLists() {
		HashMap<String, FileLinkedList> res = new HashMap<String, FileLinkedList>();
		for (Entry<String, FileLinkedList> entry : lists.entrySet()) {
			String key = entry.getKey();
			FileLinkedList list = entry.getValue();
			if (list.getRefactoringCount() != 0)
				res.put(key, list);
		}
		return new FileChangeLinkedLists(res, this.fileNameToListId);
	}
	
	public HashMap<String, FileLinkedList> getLists() {
		return lists;
	}

}