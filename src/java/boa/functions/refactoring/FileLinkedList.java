package boa.functions.refactoring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import org.antlr.v4.runtime.misc.Array2DHashSet;

import boa.types.Code.CodeRefactoring;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.ChangeKind;

public class FileLinkedList {
	private String id;
	private HashSet<String> fileNames = new HashSet<String>();
	private List<FileNode> fileNodes = new ArrayList<FileNode>();
	private TreeMap<Integer, Integer> refCommitIdxToFileListLocMap = new TreeMap<Integer, Integer>();
	private TreeMap<Integer, Integer> commitIdxToFileListLocMap = new TreeMap<Integer, Integer>();
	private int refactoringCount;

	// ADDED, DELETED, MODIFIED, RENAMED, COPIED
	private Integer[] changeCount = new Integer[] { 0, 0, 0, 0, 0 };
	// package level ("Change Package")
	// class level ("Move Class","Rename Class", "Extract Superclass", "Extract
	// Interface")
	// method level ("Rename Method", "Inline Method", "Extract Method", "Extract
	// And Move Method", "Move Method", "Pull Up Method", "Push Down Method")
	// field level ("Move Attribute", "Pull Up Attribute", "Push Down Attribute")
	private Integer[] refTypeCount = new Integer[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

	public FileLinkedList(String id) {
		this.id = id;
	}
	
	public FileNode getLastFileNode() {
		return fileNodes.get(fileNodes.size() - 1);
	}
 
	public boolean addLink(FileNode fn) {
		ChangedFile cf = fn.getChangedFile();
		fileNames.add(cf.getName());
		fileNodes.add(fn);
		commitIdxToFileListLocMap.put(fn.getRevIdx(), fileNodes.size() - 1);
		changeCount[BoaRefactoringPredictionIntrinsics.FILE_CHANGE_IDX_MAP.get(cf.getChange())] += 1;
		return true;
	}

	public boolean add(CodeRefactoring ref, int revIdx) {
		refTypeCount[BoaRefactoringPredictionIntrinsics.DETECTED_TYPE_IDX_MAP.get(ref.getType())] += 1;
		int fileListLoc = fileNodes.size() - 1;
		fileNodes.get(fileListLoc).getRightRefactorings().add(ref);
		refCommitIdxToFileListLocMap.put(revIdx, fileListLoc);
		return true;
	}

	public String getCorrespondingFileNameAt(int commitIdx) {
//		try {
//			Thread.sleep(500);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		List<Integer> listIdxs = new ArrayList<Integer>(commitIdxToFileListLocMap.keySet());
		// out of list at left
		if (commitIdx < listIdxs.get(0)) {
			System.out.println("Stage 1");
			return null;
		}
		// in the list
		for (int i = 0; i < listIdxs.size() - 1; i++) {
			if (commitIdx >= listIdxs.get(i) && commitIdx < listIdxs.get(i + 1)) {
				int listIdx = listIdxs.get(i);
				System.out.println("Stage 2 at " + listIdx);
				return getFileNode(listIdx).getChangedFile().getName();	
			}
		}
		// out of list at right
		int idx = listIdxs.get(listIdxs.size() - 1);
		ChangedFile cf = getFileNode(idx).getChangedFile();
		if (idx < commitIdx 
				&& !cf.getChange().equals(ChangeKind.DELETED)) {
			System.out.println("Stage 3");
			return cf.getName();
		}
		System.out.println("Stage 4");
		return null;
	}

	public FileNode getFileNode(int refCommitIdx) {
		return fileNodes.get(commitIdxToFileListLocMap.get(refCommitIdx));
	}
	
	public FileNode getRefFileNode(int refCommitIdx) {
		return fileNodes.get(refCommitIdxToFileListLocMap.get(refCommitIdx));
	}

	public TreeMap<Integer, Integer> getRefCommitIdxToFileListLocMap() {
		return refCommitIdxToFileListLocMap;
	}

	public TreeMap<Integer, Integer> getCommitIdxToFileListLocMap() {
		return commitIdxToFileListLocMap;
	}

	public List<FileNode> getFileNodes() {
		return fileNodes;
	}

	public int getRefactoringCount() {
		if (refactoringCount == 0)
			refactoringCount = Arrays.stream(refTypeCount).mapToInt(Integer::intValue).sum();
		return refactoringCount;
	}

	public HashSet<String> getFileNames() {
		return fileNames;
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

	public Integer[] getChangeCount() {
		return changeCount;
	}

	public Integer[] getRefTypeCount() {
		return refTypeCount;
	}
}