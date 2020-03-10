package boa.functions.code.change;

import static boa.functions.code.change.refactoring.BoaRefactoringIntrinsics.isJavaFile;

import java.util.ArrayList;
import java.util.List;

import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;

public class RevNode {
	private int revIdx;
	private Revision rev;
	private int nContributorSoFar;

	public RevNode(int revIdx, Revision rev, int nContributor) {
		this.revIdx = revIdx;
		this.rev = rev;
		this.nContributorSoFar = nContributor;
	}

	public List<FileNode> getJavaFileNodes() {
		List<FileNode> fns = new ArrayList<FileNode>();
		for (int i = 0; i < rev.getFilesCount(); i++) {
			ChangedFile cf = rev.getFiles(i);
			if (isJavaFile(cf.getName()))
				fns.add(new FileNode(rev.getFiles(i), this, i));
		}
		return fns;
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

}