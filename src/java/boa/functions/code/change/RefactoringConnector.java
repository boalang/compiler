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

	// considered ref types
	protected HashSet<String> refTypes = new HashSet<String>(Arrays.asList(new String[] { "Move Class", "Rename Class", // CLASS_LEVEL
			"Rename Method", "Move Method" // METHOD_LEVEL
	}));

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
					DeclTree leftTree = db.declForest.get(leftDecl.getTreeId());
					DeclTree rightTree = db.declForest.get(rightDecl.getTreeId());
					leftTree.merge(rightTree);
				}
			}
			break;
		case METHOD_LEVEL:
			if (refTypes.contains(ref.getType())) {
				String leftMethodSig = ref.getLeftSideLocations(0).getCodeElement();
				String rightMethodSig = ref.getRightSideLocations(0).getCodeElement();

				FileNode leftFileNode = fileBefore == null ? fileAfter : fileBefore;
				FileNode rightFileNode = fileAfter;

				String[] leftRightDeclSigs = getDeclSigsFromMethodLevelRef(ref.getDescription());
				
				String leftDeclSig = leftRightDeclSigs[0];
				String rightDeclSig = leftRightDeclSigs[1];
				MethodNode leftMethod = leftFileNode.getMethodChange(leftMethodSig, leftRightDeclSigs[0]);
				MethodNode rightMethod = rightFileNode.getMethodChange(rightMethodSig, leftRightDeclSigs[1]);
				
				
				
				if (leftMethod == null) {
					System.out.println("left");
					System.out.println(ref.getDescription());
					System.out.println(leftMethodSig);
					System.out.println(leftDeclSig);
					System.out.println(leftFileNode);
					System.out.println(leftFileNode.getDeclChange(leftDeclSig).getMethodChangeMap());
					System.out.println();
				}

				if (rightMethod == null) {
					System.out.println("right");
					System.out.println(ref.getDescription());
					System.out.println(rightMethodSig);
					System.out.println(rightDeclSig);
					System.out.println(rightFileNode);
					System.out.println(rightFileNode.getDeclChange(rightDeclSig).getMethodChangeMap());
					System.out.println();
				}
				
			}
			break;
		case FIELD_LEVEL:
			;
			break;
		default:
			break;
		}

	}

	private String[] getDeclSigsFromMethodLevelRef(String description) {
		BoaRefactoringType type = BoaRefactoringType.extractFromDescription(description);
		Matcher m = type.getRegex().matcher(description);
		String[] decls = new String[2];
		if (m.matches()) {
			switch (type) {
			case MOVE_OPERATION: {
				decls[0] = m.group(2);
				decls[1] = m.group(4);
				break;
			}
			case RENAME_METHOD: {
				decls[0] = m.group(3);
				decls[1] = m.group(3);
				break;
			}
			default:
				return null;
			}
		}
		return decls;
	}

}
