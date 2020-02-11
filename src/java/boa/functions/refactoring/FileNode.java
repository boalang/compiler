package boa.functions.refactoring;

import boa.functions.refactoring.BoaRefactoringPredictionIntrinsics.Rev;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;

public class FileNode {
	ChangedFile cf = null;
	Revision rev = null;
	int revIdx = -1;
	int fileIdx = -1;
	String locId = null;

	public FileNode(ChangedFile cf, Rev r, int fileIdx) {
		if (cf == null)
			System.err.println("err null ChangedFile");
		this.cf = cf;
		this.rev = r.rev;
		this.revIdx = r.revIdx;
		this.fileIdx = fileIdx;
	}

	public String getLocId() {
		if (locId == null)
			locId = revIdx + " " + fileIdx;
		return locId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cf == null) ? 0 : cf.hashCode());
		result = prime * result + fileIdx;
		result = prime * result + ((locId == null) ? 0 : locId.hashCode());
		result = prime * result + revIdx;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileNode other = (FileNode) obj;
		if (cf == null) {
			if (other.cf != null)
				return false;
		} else if (!cf.equals(other.cf))
			return false;
		if (fileIdx != other.fileIdx)
			return false;
//		if (locId == null) {
//			if (other.locId != null)
//				return false;
//		} else if (!locId.equals(other.locId))
//			return false;
		if (revIdx != other.revIdx)
			return false;
		return true;
	}

}