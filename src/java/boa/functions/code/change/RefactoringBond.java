package boa.functions.code.change;

import boa.types.Code.CodeRefactoring;

public class RefactoringBond {
	
	private Location leftLoc;
	private Location rightLoc;
	private CodeRefactoring refactoring;
	
	public RefactoringBond(Location leftLoc, Location rightLoc, CodeRefactoring ref) {
		this.leftLoc = leftLoc;
		this.rightLoc = rightLoc;
		this.refactoring = ref;
	}

	public Location getLeftLoc() {
		return leftLoc;
	}

	public void setLeftLoc(Location leftLoc) {
		this.leftLoc = leftLoc;
	}

	public Location getRightLoc() {
		return rightLoc;
	}

	public void setRightLoc(Location rightLoc) {
		this.rightLoc = rightLoc;
	}

	public CodeRefactoring getRefactoring() {
		return refactoring;
	}

	public void setRefactoring(CodeRefactoring refactoring) {
		this.refactoring = refactoring;
	}

}
