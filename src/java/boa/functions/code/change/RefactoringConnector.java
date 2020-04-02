package boa.functions.code.change;

import static boa.functions.BoaAstIntrinsics.prettyprint;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import boa.functions.code.change.declaration.DeclNode;
import boa.functions.code.change.declaration.DeclTree;
import boa.functions.code.change.field.FieldNode;
import boa.functions.code.change.field.FieldTree;
import boa.functions.code.change.file.FileNode;
import boa.functions.code.change.method.MethodNode;
import boa.functions.code.change.method.MethodTree;
import boa.functions.code.change.refactoring.BoaCodeElementLevel;
import boa.runtime.BoaAbstractVisitor;
import boa.types.Ast.Declaration;
import boa.types.Ast.Method;
import boa.types.Ast.Variable;
import boa.types.Code.CodeRefactoring;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.ChangeKind;

import static boa.functions.code.change.ASTChange.*;

public class RefactoringConnector {

	private ChangeDataBase db;
	private RefactoringBonds bonds;
	private RevNode rev;
	private DeclFider finder;

	public RefactoringConnector(ChangeDataBase db) {
		this.db = db;
	}

	public void connect(RefactoringBonds bonds, RevNode r) throws Exception {
		this.bonds = bonds;
		this.rev = r;
		this.finder = new DeclFider();
		connectClassLevel();
		connectFieldLevel();
	}

	private void connectClassLevel() throws Exception {
		for (int idx : bonds.getClassLevel()) {
			RefactoringBond bond = db.refDB.get(idx);
			FileNode fileBefore = rev.getFileChangeMap()
					.get(bond.getRefactoring().getLeftSideLocations(0).getFilePath());
			FileNode fileAfter = rev.getFileChangeMap()
					.get(bond.getRefactoring().getRightSideLocations(0).getFilePath());
			updateDeclConnections(fileBefore, fileAfter, bond);
		}
	}

	private void updateDeclConnections(FileNode fileBefore, FileNode fileAfter, RefactoringBond bond) throws Exception {
		// If a file is renamed in this revision, then its fileBefore is null.
		// But, it's changes is
		FileNode leftFile = fileBefore == null ? fileAfter : fileBefore;
		FileNode rightFile = fileAfter;

		String leftDeclSig = bond.getLeftElement();
		String rightDeclSig = bond.getRightElement();

		DeclNode leftDecl = leftFile.getDeclChange(leftDeclSig);
		DeclNode rightDecl = rightFile.getDeclChange(rightDeclSig);

		if (leftDecl.getTreeId() != rightDecl.getTreeId()) {
			DeclTree leftTree = db.declForest.get(leftDecl.getTreeId());
			DeclTree rightTree = db.declForest.get(rightDecl.getTreeId());
			leftTree.merge(rightTree);
		}

		if (rightDecl.getFirstParent() == null) {
			rightDecl.setFirstParent(leftDecl);
			rightDecl.setFirstChange(getChangeKind(bond));
			connectNodesWithSameSig(leftDecl, rightDecl);
		} else {
			System.out.println("not null decl " + fileAfter);
		}
	}

	private void connectNodesWithSameSig(DeclNode leftDecl, DeclNode rightDecl) throws Exception {
		Declaration left = finder.getDecl(leftDecl);
		Declaration right = finder.getDecl(rightDecl);

		// compare fields
		Set<Integer> deleted1 = Stream.iterate(0, n -> n + 1).limit(left.getFieldsCount()).collect(Collectors.toSet());
		Set<Integer> added1 = Stream.iterate(0, n -> n + 1).limit(right.getFieldsCount()).collect(Collectors.toSet());
		Set<Integer> modified1 = new HashSet<Integer>();
		Set<Integer> matched1 = new HashSet<Integer>();

		for (int i = 0; i < left.getFieldsCount(); i++) {
			Variable leftVar = left.getFields(i);
			for (int j = 0; j < right.getFieldsCount(); j++) {
				if (added1.contains(j)) {
					Variable rightVar = right.getFields(j);
					if (getSignature(leftVar).equals(getSignature(rightVar))) {
						if (!prettyprint(leftVar).equals(prettyprint(rightVar))) {
							modified1.add(j);
						} else {
							matched1.add(j);
						}
						deleted1.remove(i);
						added1.remove(j);
						break;
					}
				}
			}
		}

		// compare methods
		Set<Integer> deleted2 = Stream.iterate(0, n -> n + 1).limit(left.getMethodsCount()).collect(Collectors.toSet());
		Set<Integer> added2 = Stream.iterate(0, n -> n + 1).limit(right.getMethodsCount()).collect(Collectors.toSet());
		Set<Integer> modified2 = new HashSet<Integer>();
		Set<Integer> matched2 = new HashSet<Integer>();

		for (int i = 0; i < left.getMethodsCount(); i++) {
			Method leftMethod = left.getMethods(i);
			for (int j = 0; j < right.getMethodsCount(); j++) {
				if (added2.contains(j)) {
					Method rightMethod = right.getMethods(j);
					if (getSignature(leftMethod).equals(getSignature(rightMethod))) {
						if (!prettyprint(leftMethod).equals(prettyprint(rightMethod))) {
							modified2.add(j);
						} else {
							matched2.add(j);
						}
						deleted2.remove(i);
						added2.remove(j);
						break;
					}
				}
			}
		}

		// update field changes
		for (int j : modified1)
			connectFieldsWithSameSig(getSignature(right.getFields(j)), leftDecl, rightDecl, ChangeKind.MODIFIED);
		for (int j : matched1)
			connectFieldsWithSameSig(getSignature(right.getFields(j)), leftDecl, rightDecl, ChangeKind.MOVED);

		// update method changes
		for (int j : modified2)
			connectMethodsWithSameSig(getSignature(right.getMethods(j)), leftDecl, rightDecl, ChangeKind.MODIFIED);
		for (int j : matched2)
			connectMethodsWithSameSig(getSignature(right.getMethods(j)), leftDecl, rightDecl, ChangeKind.MOVED);
	}

	private void connectFieldsWithSameSig(String sig, DeclNode leftDecl, DeclNode rightDecl, ChangeKind change) {
		FieldNode leftField = leftDecl.getFieldChange(sig);
		FieldNode rightField = rightDecl.getFieldChange(sig);

		if (leftField.getTreeId() != rightField.getTreeId()) {
			FieldTree leftTree = db.fieldForest.get(leftField.getTreeId());
			FieldTree rightTree = db.fieldForest.get(rightField.getTreeId());
			leftTree.merge(rightTree);
		}

		if (rightField.getFirstParent() == null) {
			rightField.setFirstParent(leftField);
			rightField.setFirstChange(change);
		} else {
			System.out.println("not null field " + rightField);
		}
	}

	private void connectMethodsWithSameSig(String sig, DeclNode leftDecl, DeclNode rightDecl, ChangeKind change) {
		MethodNode leftMethod = leftDecl.getMethodChange(sig);
		MethodNode rightMethod = rightDecl.getMethodChange(sig);

		if (leftMethod.getTreeId() != rightMethod.getTreeId()) {
			MethodTree leftTree = db.methodForest.get(leftMethod.getTreeId());
			MethodTree rightTree = db.methodForest.get(rightMethod.getTreeId());
			leftTree.merge(rightTree);
		}

		if (rightMethod.getFirstParent() == null) {
			rightMethod.setFirstParent(leftMethod);
			rightMethod.setFirstChange(change);
		} else {
			System.out.println("not null field " + rightMethod);
		}
	}

	private void connectFieldLevel() {
		for (int idx : bonds.getFieldLevel()) {
			RefactoringBond bond = db.refDB.get(idx);
			FileNode fileBefore = rev.getFileChangeMap()
					.get(bond.getRefactoring().getLeftSideLocations(0).getFilePath());
			FileNode fileAfter = rev.getFileChangeMap()
					.get(bond.getRefactoring().getRightSideLocations(0).getFilePath());
		
			// If a file is renamed in this revision, then its fileBefore is null.
			// But, it's changes is
			FileNode leftFile = fileBefore == null ? fileAfter : fileBefore;
			FileNode rightFile = fileAfter;

			DeclNode leftDecl = leftFile.getDeclChange(bond.getLeftDecl());
			DeclNode rightDecl = rightFile.getDeclChange(bond.getRightDecl());
			
			String leftFieldSig = bond.getLeftElement();
			String rightFieldSig = bond.getRightElement();
			
			FieldNode leftField = leftDecl.getFieldChange(leftFieldSig);
			FieldNode rightField = rightDecl.getFieldChange(rightFieldSig);

			if (leftField.getTreeId() != rightField.getTreeId()) {
				FieldTree leftTree = db.fieldForest.get(leftField.getTreeId());
				FieldTree rightTree = db.fieldForest.get(rightField.getTreeId());
				leftTree.merge(rightTree);
			}

			if (rightField.getFirstParent() == null) {
				rightField.setFirstParent(leftField);
				rightField.setFirstChange(getChangeKind(bond));
			} else {
				System.out.println("not null field " + rightField);
			}
		}
	}

	public void connect(FileNode fileBefore, FileNode fileAfter, CodeRefactoring ref) {

		switch (BoaCodeElementLevel.getCodeElementLevel(ref.getType())) {
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
		default:
			break;
		}

	}

	private ChangeKind getChangeKind(RefactoringBond bond) {
		switch (bond.getType()) {
		case "Move Class":
		case "Move Method":
		case "Move Attribute":
		case "Pull Up Attribute":
		case "Push Down Attribute":
			return ChangeKind.MOVED;
		case "Rename Class":
		case "Rename Method":
			return ChangeKind.RENAMED;
		default:
			return null;
		}
	}

	public class DeclFider extends BoaAbstractVisitor {
		private Declaration decl;
		private String declSig;
		private boolean found;

		@Override
		public boolean preVisit(final Declaration node) throws Exception {
			if (found)
				return false;
			if (node.getFullyQualifiedName().equals(declSig)) {
				this.decl = node;
				return false;
			}
			for (Declaration d : node.getNestedDeclarationsList())
				visit(d);
			return false;
		}

		public Declaration getDecl(DeclNode declNode) throws Exception {
			this.decl = null;
			this.declSig = declNode.getSignature();
			this.found = false;
			ChangedFile cf = declNode.getFirstChange() == ChangeKind.DELETED
					? declNode.getFileNode().getFirstParent().getChangedFile()
					: declNode.getFileNode().getChangedFile();
			this.visit(cf);
			return decl;
		}
	}

}
