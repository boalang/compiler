package boa.functions.refactoring;

import boa.types.Diff.ChangedFile;

public class FileNode {
	ChangedFile cf = null;
	int revIdx = -1;
	int fileIdx = -1;
	String locId = null;

	public FileNode(ChangedFile cf, int revIdx, int fileIdx) {
		if (cf == null)
			System.err.println("err null ChangedFile");
		this.cf = cf;
		this.revIdx = revIdx;
		this.fileIdx = fileIdx;
	}

	public String getLocId() {
		if (locId == null)
			locId = revIdx + " " + fileIdx;
		return locId;
	}
}