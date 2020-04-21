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
import boa.runtime.BoaAbstractVisitor;
import boa.types.Ast.Declaration;
import boa.types.Ast.Method;
import boa.types.Ast.Variable;
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
		connectMethodLevel();
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
		FileNode leftFile = fileBefore == null ? fileAfter : fileBefore;
		FileNode rightFile = fileAfter;

		String leftDeclSig = bond.getLeftElement();
		String rightDeclSig = bond.getRightElement();

		DeclNode leftDecl = leftFile.getDeclChange(leftDeclSig);
		DeclNode rightDecl = rightFile.getDeclChange(rightDeclSig);

		if (leftDecl == null)
			leftDecl = findDeclNode(leftDeclSig);
		if (rightDecl == null)
			rightDecl = findDeclNode(rightDeclSig);

		// java parser only support java 8
		if (leftDecl == null || rightDecl == null)
			return;

//		System.out.println(rev);
//		
//		System.out.println(bond.getType());
//		System.out.println(bond.getRefactoring().getLeftSideLocations(0).getFilePath());
//		System.out.println(bond.getRefactoring().getRightSideLocations(0).getFilePath());
//		
//		System.out.println(bond.getLeftDecl());
//		System.out.println(bond.getRightDecl());
//		
//		System.out.println();
//		
//		System.out.println(leftFile);
//		System.out.println(rightFile);
//		
//		System.out.println(rightFile.getDeclChangeMap().keySet());
//		
//		System.out.println(leftDecl);
//		System.out.println(rightDecl);
//		
//		System.out.println();

		// merge trees
		if (leftDecl.getTreeId() != rightDecl.getTreeId()) {
			DeclTree leftTree = db.declForest.get(leftDecl.getTreeId());
			DeclTree rightTree = db.declForest.get(rightDecl.getTreeId());
			leftTree.merge(rightTree);
		}

		// update refactoirng-based edges and changes
		rightDecl.getLeftRefDecls().add(leftDecl);
		rightDecl.getLeftRefBonds().add(bond);

		connectNodesWithSameSig(leftDecl, rightDecl);
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
			connectFieldsWithSameSig(getSignature(right.getFields(j)), leftDecl, rightDecl, ChangeKind.COPIED);

		// update method changes
		for (int j : modified2)
			connectMethodsWithSameSig(getSignature(right.getMethods(j)), leftDecl, rightDecl, ChangeKind.MODIFIED);
		for (int j : matched2)
			connectMethodsWithSameSig(getSignature(right.getMethods(j)), leftDecl, rightDecl, ChangeKind.COPIED);
	}

	private void connectFieldsWithSameSig(String sig, DeclNode leftDecl, DeclNode rightDecl, ChangeKind change) {
		FieldNode leftField = leftDecl.getFieldChange(sig);
		FieldNode rightField = rightDecl.getFieldChange(sig);

		if (leftField == null || rightField == null)
			return;

		// merge trees
		if (leftField.getTreeId() != rightField.getTreeId()) {
			FieldTree leftTree = db.fieldForest.get(leftField.getTreeId());
			FieldTree rightTree = db.fieldForest.get(rightField.getTreeId());
			leftTree.merge(rightTree);
		}

		// update name-based edges and changes
		if (rightField.getFirstParent() == null) {
			rightField.setFirstParent(leftField);
			rightField.setFirstChange(change);
		} else {
//			if (rightField.getFirstParent() != leftField)
//				System.out.println("parent not null field " + rightField);
		}
	}

	private void connectMethodsWithSameSig(String sig, DeclNode leftDecl, DeclNode rightDecl, ChangeKind change) {
		MethodNode leftMethod = leftDecl.getMethodChange(sig);
		MethodNode rightMethod = rightDecl.getMethodChange(sig);

		if (leftMethod == null || rightMethod == null)
			return;

		// merge trees
		if (leftMethod.getTreeId() != rightMethod.getTreeId()) {
			MethodTree leftTree = db.methodForest.get(leftMethod.getTreeId());
			MethodTree rightTree = db.methodForest.get(rightMethod.getTreeId());

			if (rightMethod.getTreeId() == 277)
				System.out.println("here 1");

			leftTree.merge(rightTree);
		}

		// update name-based edges and changes
		if (rightMethod.getFirstParent() == null) {
			rightMethod.setFirstParent(leftMethod);
			rightMethod.setFirstChange(change);
		} else {
//			if (rightMethod.getFirstParent() != leftMethod)
//				System.out.println("parent not null method " + rightMethod);
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
			FileNode leftFile = fileBefore == null ? fileAfter : fileBefore;
			FileNode rightFile = fileAfter;

			String leftFieldSig = bond.getLeftElement();
			String rightFieldSig = bond.getRightElement();

			DeclNode leftDecl = leftFile.getDeclChange(bond.getLeftDecl());
			DeclNode rightDecl = rightFile.getDeclChange(bond.getRightDecl());

			if (leftDecl == null)
				leftDecl = findDeclNode(bond.getLeftDecl());
			if (rightDecl == null)
				rightDecl = findDeclNode(bond.getRightDecl());

			// java parser only support java 8
			if (leftDecl == null || rightDecl == null)
				return;

			FieldNode leftField = leftDecl.getFieldChange(leftFieldSig);
			FieldNode rightField = rightDecl.getFieldChange(rightFieldSig);

			if (leftField == null)
				leftField = findFieldNode(leftFieldSig, ChangeKind.DELETED);
			if (rightField == null)
				rightField = findFieldNode(rightFieldSig, ChangeKind.ADDED);

			// java parser only support java 8
			if (leftField == null || rightField == null)
				continue;

//			System.out.println(rev);
//			
//			System.out.println(bond.getRefactoring().getLeftSideLocations(0).getFilePath());
//			System.out.println(bond.getRefactoring().getRightSideLocations(0).getFilePath());
//			
//			System.out.println(bond.getLeftDecl());
//			System.out.println(bond.getRightDecl());
//			
//			System.out.println(leftFieldSig);
//			System.out.println(rightFieldSig);
//			
//			System.out.println();
//			
//			System.out.println(leftFile);
//			System.out.println(rightFile);
//			
//			System.out.println(leftDecl);
//			System.out.println(rightDecl);

			// merge trees
			if (leftField.getTreeId() != rightField.getTreeId()) {
				FieldTree leftTree = db.fieldForest.get(leftField.getTreeId());
				FieldTree rightTree = db.fieldForest.get(rightField.getTreeId());
				leftTree.merge(rightTree);
			}

			// update refactoring-based edges and changes
			rightField.getLeftRefFields().add(leftField);
			rightField.getLeftRefBonds().add(bond);
		}
	}

	private FieldNode findFieldNode(String sig, ChangeKind change) {
		for (FileNode fn : rev.getFileChangeMap().values())
			for (DeclNode dn : fn.getDeclChanges())
				if (dn.getFieldChangeMap().containsKey(sig)) {
					FieldNode temp = dn.getFieldChange(sig);
					if (temp.getFirstChange() == change)
						return temp;
				}
		return null;
	}

	private void connectMethodLevel() {
		for (int idx : bonds.getMethodLevel()) {
			RefactoringBond bond = db.refDB.get(idx);
			FileNode fileBefore = rev.getFileChangeMap()
					.get(bond.getRefactoring().getLeftSideLocations(0).getFilePath());
			FileNode fileAfter = rev.getFileChangeMap()
					.get(bond.getRefactoring().getRightSideLocations(0).getFilePath());

			// If a file is renamed in this revision, then its fileBefore is null.
			FileNode leftFile = fileBefore == null ? fileAfter : fileBefore;
			FileNode rightFile = fileAfter;

			String leftMethodSig = bond.getLeftElement();
			String rightMethodSig = bond.getRightElement();

			DeclNode leftDecl = leftFile.getDeclChange(bond.getLeftDecl());
			DeclNode rightDecl = rightFile.getDeclChange(bond.getRightDecl());

			if (leftDecl == null)
				leftDecl = findDeclNode(bond.getLeftDecl());
			if (rightDecl == null)
				rightDecl = findDeclNode(bond.getRightDecl());

			// java parser only support java 8
			if (leftDecl == null || rightDecl == null)
				return;

			MethodNode leftMethod = leftDecl.getMethodChange(leftMethodSig);
			MethodNode rightMethod = rightDecl.getMethodChange(rightMethodSig);

			if (leftMethod == null)
				leftMethod = findMethodNode(leftMethodSig, ChangeKind.DELETED);
			if (rightMethod == null)
				rightMethod = findMethodNode(rightMethodSig, ChangeKind.ADDED);

			// java parser only support java 8
			if (leftMethod == null || rightMethod == null)
				continue;

			// merge trees
			if (leftMethod.getTreeId() != rightMethod.getTreeId()) {
				MethodTree leftTree = db.methodForest.get(leftMethod.getTreeId());
				MethodTree rightTree = db.methodForest.get(rightMethod.getTreeId());
				leftTree.merge(rightTree);
			}

			// update refactoring-based edges and changes
			rightMethod.getLeftRefMethods().add(leftMethod);
			rightMethod.getLeftRefBonds().add(bond);
		}
	}

	private MethodNode findMethodNode(String sig, ChangeKind change) {
		for (FileNode fn : rev.getFileChangeMap().values())
			for (DeclNode dn : fn.getDeclChanges())
				if (dn.getMethodChangeMap().containsKey(sig)) {
					MethodNode temp = dn.getMethodChange(sig);
					if (temp.getFirstChange() == change)
						return temp;
				}
		return null;
	}

	private DeclNode findDeclNode(String declSig) {
		for (FileNode fn : rev.getFileChangeMap().values())
			if (fn.getDeclChangeMap().containsKey(declSig))
				return fn.getDeclChange(declSig);
		return null;
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
