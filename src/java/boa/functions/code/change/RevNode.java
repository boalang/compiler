package boa.functions.code.change;

import static boa.functions.code.change.refactoring.BoaRefactoringIntrinsics.isJavaFile;

import java.util.ArrayList;
import java.util.List;

import boa.functions.code.change.file.ChangedFileNode;
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

	public List<ChangedFileNode> getJavaFileNodes() {
		List<ChangedFileNode> fns = new ArrayList<ChangedFileNode>();
		for (int i = 0; i < rev.getFilesCount(); i++) {
			ChangedFile cf = rev.getFiles(i);
			if (isJavaFile(cf.getName()))
				fns.add(new ChangedFileNode(rev.getFiles(i), this));
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