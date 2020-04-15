package boa.functions.code.change;

import static boa.functions.code.change.refactoring.BoaRefactoringIntrinsics.isJavaFile;

import java.util.HashMap;
import boa.functions.code.change.file.FileNode;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;

public class RevNode {
	private int revIdx;
	private Revision rev;
	private int nContributorSoFar;

	// file changes
	private HashMap<String, FileNode> fileChangeMap = new HashMap<String, FileNode>();

	public RevNode(int revIdx, Revision rev, int nContributor) {
		this.revIdx = revIdx;
		this.rev = rev;
		this.nContributorSoFar = nContributor;
		updateFileNodes();
	}

	private void updateFileNodes() {
		for (int i = 0; i < rev.getFilesCount(); i++) {
			ChangedFile cf = rev.getFiles(i);
			if (isJavaFile(cf.getName()))
				fileChangeMap.put(cf.getName(), new FileNode(cf, this));
		}
	}

	public HashMap<String, FileNode> getFileChangeMap() {
		return fileChangeMap;
	}
	
	public FileNode getFileNode(String fileName) {
		return fileChangeMap.get(fileName);
	}
	
	public FileNode getFileNode(int fileIdx) {
		String fileName = rev.getFiles(fileIdx).getName();
		return getFileNode(fileName);
	}

	public int getRevIdx() {
		return revIdx;
	}

	public Revision getRevision() {
		return rev;
	}

	public int getContributorNumSoFar() {
		return nContributorSoFar;
	}

	@Override
	public String toString() {
		return revIdx + " " + rev.getId();
	}
}