package boa.functions.refactoring;

import static boa.functions.refactoring.BoaRefactoringIntrinsics.isJavaFile;

import java.util.ArrayList;
import java.util.List;

import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;

public class Rev {
	public int revIdx;
	public Revision rev;
	public int nContributorSoFar;

	public Rev(int revIdx, Revision rev, int nContributor) {
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
}