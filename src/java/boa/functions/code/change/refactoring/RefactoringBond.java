package boa.functions.code.change.refactoring;

import boa.functions.code.change.ChangedNode;
import boa.types.Code.CodeRefactoring;

public class RefactoringBond<T extends ChangedNode> {
	
	private T leftNode;
	private T rightNode;
	private CodeRefactoring refactoring;
	private BoaCodeElementLevel level;
	
	public RefactoringBond(T leftNode, T rightNode, CodeRefactoring ref) {
		this.leftNode = leftNode;
		this.rightNode = rightNode;
		this.refactoring = ref;
		this.level = BoaCodeElementLevel.getCodeElementLevel(ref.getType());
	}

	public CodeRefactoring getRefactoring() {
		return refactoring;
	}
	
	public String getType() {
		return refactoring.getType();
	}
	
	public T getLeftNode() {
		return leftNode;
	}

	public T getRightNode() {
		return rightNode;
	}

	public BoaCodeElementLevel getLevel() {
		return level;
	}

}
