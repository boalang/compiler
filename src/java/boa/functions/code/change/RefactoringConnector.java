package boa.functions.code.change;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import boa.functions.code.change.declaration.DeclNode;
import boa.functions.code.change.declaration.DeclTree;
import boa.functions.code.change.file.FileForest;
import boa.functions.code.change.file.FileNode;
import boa.functions.code.change.file.FileTree;
import boa.functions.code.change.refactoring.BoaCodeElementLevel;
import boa.types.Code.CodeRefactoring;

public class RefactoringConnector {

	private ChangeDataBase db;

	// considered ref types
	protected HashSet<String> refTypes = new HashSet<String>(
			Arrays.asList(new String[] { "Move Class", "Rename Class" }));

	public RefactoringConnector(ChangeDataBase db) {
		this.db = db;
	}

	public void connect(FileNode fileBefore, FileNode fileAfter, CodeRefactoring ref) {

		switch (BoaCodeElementLevel.getCodeElementLevel(ref.getType())) {
		case CLASS_LEVEL:
			if (refTypes.contains(ref.getType())) {

				String leftDeclSig = ref.getLeftSideLocations(0).getCodeElement();
				String rightDeclSig = ref.getRightSideLocations(0).getCodeElement();

				DeclNode leftDecl = fileBefore == null ? fileAfter.getDeclChange(leftDeclSig)
						: fileBefore.getDeclChange(leftDeclSig);
				DeclNode rightDecl = fileAfter.getDeclChange(rightDeclSig);

				if (leftDecl.getTreeId() != rightDecl.getTreeId()) {
					
//					if (rightDecl.getTreeId().id == 3385) {
//						System.out.println(leftDecl.getTreeId() + " " + rightDecl.getTreeId());
//						System.out.println(db.declForest.keySet());	
//						
//						DeclTree leftTree = db.declForest.get(leftDecl.getTreeId());
//						DeclTree rightTree = db.declForest.get(rightDecl.getTreeId());
//						leftTree.merge(rightTree);
//						
//						System.out.println(leftDecl.getTreeId() + " " + rightDecl.getTreeId());
//						System.out.println(db.declForest.keySet());	
//					} else {
//						DeclTree leftTree = db.declForest.get(leftDecl.getTreeId());
//						DeclTree rightTree = db.declForest.get(rightDecl.getTreeId());
//						leftTree.merge(rightTree);
//					}
					
				}
			}
			break;
		case METHOD_LEVEL:
			;
			break;
		case FIELD_LEVEL:
			;
			break;
		default:
			break;
		}

	}

}
