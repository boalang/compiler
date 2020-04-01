package boa.functions.code.change;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;

import boa.functions.code.change.declaration.DeclNode;
import boa.functions.code.change.declaration.DeclTree;
import boa.functions.code.change.file.FileForest;
import boa.functions.code.change.file.FileNode;
import boa.functions.code.change.file.FileTree;
import boa.functions.code.change.method.MethodNode;
import boa.functions.code.change.refactoring.BoaCodeElementLevel;
import boa.functions.code.change.refactoring.BoaRefactoringType;
import boa.types.Code.CodeRefactoring;

public class RefactoringConnector {

	private ChangeDataBase db;
	private RefactoringBonds bonds;
	private RevNode rev;

	public RefactoringConnector(ChangeDataBase db) {
		this.db = db;
	}

	public void connect(RefactoringBonds bonds, RevNode r) {
		this.bonds = bonds;
		this.rev = r;
		connectClassLevel();
	}

	private void connectClassLevel() {
		for (int idx : bonds.getClassLevel()) {
			RefactoringBond bond = db.refDB.get(idx);
			String beforeFilePath = bond.getRefactoring().getLeftSideLocations(0).getFilePath();
			String afterFilePath = bond.getRefactoring().getRightSideLocations(0).getFilePath();
			FileNode fileBefore = rev.getFileChangeMap().get(beforeFilePath);
			FileNode fileAfter = rev.getFileChangeMap().get(afterFilePath);

//			System.out.println(bond.getRefactoring().getDescription());
			
			String leftDeclSig = bond.getLeftElement();
			String rightDeclSig = bond.getRightElement();
			DeclNode leftDecl = fileBefore == null ? fileAfter.getDeclChange(leftDeclSig)
					: fileBefore.getDeclChange(leftDeclSig);
			DeclNode rightDecl = fileAfter.getDeclChange(rightDeclSig);
			
//			System.out.println(fileBefore);
//			System.out.println(fileAfter);
//			System.out.println(leftDecl);
//			System.out.println(rightDecl);
//			System.out.println();
			if (leftDecl.getTreeId() != rightDecl.getTreeId()) {
				DeclTree leftTree = db.declForest.get(leftDecl.getTreeId());
				DeclTree rightTree = db.declForest.get(rightDecl.getTreeId());
				leftTree.merge(rightTree);
			}
			
			if (rightDecl.getFirstParent() != null) {
				System.out.println("not null " + fileAfter);
				if (!rightDecl.getFirstParent().equals(leftDecl)) {
					System.out.println(rightDecl);
					System.out.println(rightDecl.getFirstParent());
				}
				System.out.println();
			}
		}
	}

	public void connect(FileNode fileBefore, FileNode fileAfter, CodeRefactoring ref) {

		switch (BoaCodeElementLevel.getCodeElementLevel(ref.getType())) {
		case CLASS_LEVEL:

			String leftDeclSig = ref.getLeftSideLocations(0).getCodeElement();
			String rightDeclSig = ref.getRightSideLocations(0).getCodeElement();

			DeclNode leftDecl = fileBefore == null ? fileAfter.getDeclChange(leftDeclSig)
					: fileBefore.getDeclChange(leftDeclSig);
			DeclNode rightDecl = fileAfter.getDeclChange(rightDeclSig);

			if (leftDecl.getTreeId() != rightDecl.getTreeId()) {
				DeclTree leftTree = db.declForest.get(leftDecl.getTreeId());
				DeclTree rightTree = db.declForest.get(rightDecl.getTreeId());
				leftTree.merge(rightTree);
			}
			break;
		case METHOD_LEVEL:
			String leftMethodSig = ref.getLeftSideLocations(0).getCodeElement();
			String rightMethodSig = ref.getRightSideLocations(0).getCodeElement();

			FileNode leftFileNode = fileBefore == null ? fileAfter : fileBefore;
			FileNode rightFileNode = fileAfter;

//			String leftDeclSig = "";
//			String rightDeclSig = "";
//				MethodNode leftMethod = leftFileNode.getMethodChange(leftMethodSig, leftRightDeclSigs[0]);
//				MethodNode rightMethod = rightFileNode.getMethodChange(rightMethodSig, leftRightDeclSigs[1]);

			break;
		case FIELD_LEVEL:
			;
			break;
		default:
			break;
		}

	}

}
