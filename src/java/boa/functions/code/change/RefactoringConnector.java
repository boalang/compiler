package boa.functions.code.change;

import java.util.Arrays;
import java.util.HashSet;

import boa.functions.code.change.declaration.DeclNode;
import boa.functions.code.change.file.FileNode;
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
				
				if (fileBefore == null) {
					DeclNode leftDecl = fileAfter.getDeclChange(leftDeclSig);
					DeclNode rightDecl = fileAfter.getDeclChange(rightDeclSig);
//					System.out.println(fileAfter.getDeclChangeMap());
//					System.out.println(leftDeclSig + " " + leftDecl);
//					System.out.println(rightDeclSig + " " + rightDecl);
					if (!leftDecl.getTreeId().equals(rightDecl.getTreeId())) {
//						System.out.println("1 need to link " + ref.getType());
					} else {
						System.out.println("no need linking " + ref.getType());
					}
				} else {
					DeclNode leftDecl = fileBefore.getDeclChange(leftDeclSig);
					DeclNode rightDecl = fileAfter.getDeclChange(rightDeclSig);
					if (!leftDecl.getTreeId().equals(rightDecl.getTreeId())) {
//						System.out.println("2 need to link " + ref.getType());
					} else {
						System.out.println("no need linking " + ref.getType());
					}
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
