package boa.functions.code.change.refactoring;

import java.util.ArrayList;
import java.util.List;

public class RefactoringBonds {
	private List<Integer> packageLevel = new ArrayList<Integer>();
	private List<Integer> classLevel = new ArrayList<Integer>();
	private List<Integer> methodLevel = new ArrayList<Integer>();
	private List<Integer> fieldLevel = new ArrayList<Integer>();

	public void add(RefactoringBond refBond, int refBondIdx) {
		switch (refBond.getLevel()) {
		case PACKAGE_LEVEL:
			packageLevel.add(refBondIdx);
			break;
		case CLASS_LEVEL:
			classLevel.add(refBondIdx);
			break;
		case METHOD_LEVEL:
			methodLevel.add(refBondIdx);
			break;
		case FIELD_LEVEL:
			fieldLevel.add(refBondIdx);
			break;
		default:
			break;
		}
	}

	public List<Integer> getPackageLevel() {
		return packageLevel;
	}

	public List<Integer> getClassLevel() {
		return classLevel;
	}

	public List<Integer> getMethodLevel() {
		return methodLevel;
	}

	public List<Integer> getFieldLevel() {
		return fieldLevel;
	}
}
