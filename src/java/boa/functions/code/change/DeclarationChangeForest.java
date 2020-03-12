package boa.functions.code.change;

import java.util.ArrayList;
import java.util.List;

public class DeclarationChangeForest {

	protected List<DeclarationTree> trees = new ArrayList<DeclarationTree>();

	
	public DeclarationChangeForest(FileChangeForest forest) {
		
//		for (FileLocation loc : forest.gd.fileLocIdToNode.descendingKeySet()) {
//			System.out.println(loc);
//			
//			
//			
//		}
		System.out.println(forest.gd.fileObjectIdToLocs.size());
	}
}
