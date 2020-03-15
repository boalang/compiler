package boa.functions.code.change.refactoring;

import boa.functions.code.change.Location;
import boa.types.Code.CodeRefactoring;

public class RefactoringBond {
	
	private Location leftLoc;
	private Location rightLoc;
	private CodeRefactoring refactoring;
	private BoaCodeElementLevel level;
	
	public RefactoringBond(Location leftLoc, Location rightLoc, CodeRefactoring ref) {
		this.leftLoc = leftLoc;
		this.rightLoc = rightLoc;
		this.refactoring = ref;
		this.level = BoaCodeElementLevel.getCodeElementLevel(ref.getType());
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
	
	public String getType() {
		return refactoring.getType();
	}

	public BoaCodeElementLevel getLevel() {
		return level;
	}
	
	public String getRightElement() {
		return refactoring.getRightSideLocations(0).getCodeElement();
	}

	public String getLeftElement() {
		return refactoring.getLeftSideLocations(0).getCodeElement();
	}
}
