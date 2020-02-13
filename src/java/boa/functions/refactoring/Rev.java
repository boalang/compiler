package boa.functions.refactoring;

import static boa.functions.refactoring.BoaRefactoringIntrinsics.isJavaFile;

import java.util.ArrayList;
import java.util.List;

import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;

public class Rev {
	int revIdx;
	Revision rev;
	int nContributorSoFar;

	public Rev(int revIdx, Revision rev) {
		this.revIdx = revIdx;
		this.rev = rev;
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