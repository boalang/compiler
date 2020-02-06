package boa.functions.refactoring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import boa.types.Code.CodeRefactoring;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;

public class FileLinkedList {
	protected String id;
	protected List<ChangedFile> files = new ArrayList<ChangedFile>();
	protected List<Revision> revisions = new ArrayList<Revision>();
	protected HashMap<Integer, CodeRefactoring> filesBeforeRefactoirngMap = new HashMap<Integer, CodeRefactoring>();
	// ADDED, DELETED, MODIFIED, RENAMED, COPIED
	protected Integer[] changeCount = new Integer[] { 0, 0, 0, 0, 0 };
	// package level ("Change Package") 
	// class level ("Move Class","Rename Class", "Extract Superclass", "Extract Interface") 
	// method level ("Rename Method", "Inline Method", "Extract Method", "Extract And Move Method", "Move Method", "Pull Up Method", "Push Down Method") 
	// field level ("Move Attribute", "Pull Up Attribute", "Push Down Attribute")
	protected Integer[] refTypeCount = new Integer[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

	public FileLinkedList(String id) {
		this.id = id;
	}

	public boolean addLink(ChangedFile cf, Revision r) {
		files.add(cf);
		revisions.add(r);
		changeCount[BoaRefactoringPredictionIntrinsics.FILE_CHANGE_IDX_MAP.get(cf.getChange())] += 1;
		return true;
	}
	
	public boolean add(CodeRefactoring ref) {
		refTypeCount[BoaRefactoringPredictionIntrinsics.DETECTED_TYPE_IDX_MAP.get(ref.getType())] += 1;
		filesBeforeRefactoirngMap.put(files.size() - 1, ref);
		return true;
	}
	
	public String getChangeCountAsString() {
		return Arrays.toString(changeCount);
	}
	
	public String getRefactoringTypeCountAsString() {
		return Arrays.toString(refTypeCount);
	}
	
	public String getId() {
		return id;
	}

	public List<ChangedFile> getFiles() {
		return files;
	}

	public List<Revision> getRevisions() {
		return revisions;
	}

	public HashMap<Integer, CodeRefactoring> getFilesBeforeRefactoringMap() {
		return filesBeforeRefactoirngMap;
	}

	public Integer[] getChangeCount() {
		return changeCount;
	}

	public Integer[] getRefTypeCount() {
		return refTypeCount;
	}
}