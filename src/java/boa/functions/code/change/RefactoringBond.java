package boa.functions.code.change;

import boa.types.Code.CodeRefactoring;

public class RefactoringBond {
	
	private FileLocation leftLoc;
	private FileLocation rightLoc;
	private CodeRefactoring refactoring;
	
	public RefactoringBond(FileLocation leftLoc, FileLocation rightLoc, CodeRefactoring ref) {
		this.leftLoc = leftLoc;
		this.rightLoc = rightLoc;
		this.refactoring = ref;
	}

	public FileLocation getLeftLoc() {
		return leftLoc;
	}

	public void setLeftLoc(FileLocation leftLoc) {
		this.leftLoc = leftLoc;
	}

	public FileLocation getRightLoc() {
		return rightLoc;
	}

	public void setRightLoc(FileLocation rightLoc) {
		this.rightLoc = rightLoc;
	}

	public CodeRefactoring getRefactoring() {
		return refactoring;
	}

	public void setRefactoring(CodeRefactoring refactoring) {
		this.refactoring = refactoring;
	}

}
