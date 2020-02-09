package boa.functions.refactoring;

import java.util.ArrayList;
import java.util.List;

import boa.types.Code.CodeRefactoring;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;

public class FileNode {
	private int revIdx = -1;
	private Revision revision = null;
	private int fileRevLoc = -1;
	private List<CodeRefactoring> rightRefactorings = new ArrayList<CodeRefactoring>();
	
	public FileNode(int revIdx, Revision revision, int fileRevLoc) {
		this.revIdx = revIdx;
		this.revision = revision;
		this.fileRevLoc = fileRevLoc;
	}
	
	public ChangedFile getChangedFile() {
		return revision.getFiles(fileRevLoc);
	}
	
	public int getRevIdx() {
		return revIdx;
	}
	
	public Revision getRevision() {
		return revision;
	}

	public int getFileRevLoc() {
		return fileRevLoc;
	}

	public List<CodeRefactoring> getRightRefactorings() {
		return rightRefactorings;
	}

	public void setRevIdx(int revIdx) {
		this.revIdx = revIdx;
	}

}
