package boa.functions.refactoring;

import java.util.HashSet;

public class BoaRefactoringAnalysis {
	
	public static void main(String[] args) {
		int size = 0;
		for (HashSet<String> s : BoaRefactoringDetectionIntrinsics.oracle().values())
			size += s.size();
		System.out.println(size);
	}
	
}
