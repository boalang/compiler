package boa.functions.code.change;

import static boa.functions.BoaAstIntrinsics.prettyprint;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import boa.functions.code.change.declaration.DeclNode;
import boa.functions.code.change.field.FieldNode;
import boa.functions.code.change.file.FileNode;
import boa.functions.code.change.file.FileForest.DeclCollector;
import boa.functions.code.change.method.MethodNode;
import boa.types.Ast.Declaration;
import boa.types.Ast.Method;
import boa.types.Ast.Variable;
import boa.types.Shared.ChangeKind;

public class ASTChange {

	private ChangeDataBase db;

	public ASTChange(ChangeDataBase db) {
		this.db = db;
	}

	public ChangeDataBase getDb() {
		return db;
	}

	public void update(FileNode fileNode, List<Declaration> decls, ChangeKind change, boolean isFirstParent) {
		for (int i = 0; i < decls.size(); i++) {
			Declaration decl = decls.get(i);
			DeclNode declNode = update(fileNode, decl, change, isFirstParent);
			for (int j = 0; j < decl.getMethodsCount(); j++)
				update(declNode, decl.getMethods(j), change, isFirstParent);
			for (int j = 0; j < decl.getFieldsCount(); j++)
				update(declNode, decl.getFields(j), change, isFirstParent);
		}
	}

	private void update(DeclNode declNode, Variable v, ChangeKind change, boolean isFirstParent) {
		FieldNode fieldNode = declNode.getFieldNode(getSignature(v));
		updateChange(fieldNode, change, isFirstParent);
	}

	private void update(DeclNode declNode, Method m, ChangeKind change, boolean isFirstParent) {
		MethodNode methodNode = declNode.getMethodNode(getSignature(m));
		updateChange(methodNode, change, isFirstParent);
	}

	private DeclNode update(FileNode fileNode, Declaration decl, ChangeKind change, boolean isFirstParent) {
		DeclNode declNode = fileNode.getDeclNode(decl.getFullyQualifiedName());
		updateChange(declNode, change, isFirstParent);
		return declNode;
	}
	
	private void updateChange(ChangedNode node, ChangeKind change, boolean isFirstParent) {
		if (isFirstParent)
			node.setFirstChange(change);
		else
			node.setSecondChange(change);
	}

	private void updateAllChanges(FileNode rightNode, ChangeKind change, boolean isFirstParent) {
		for (DeclNode decl : rightNode.getDeclChanges()) {
			updateChange(decl, change, isFirstParent);
			decl.setSecondChange(change);
			for (MethodNode method : decl.getMethodChanges())
				updateChange(method, change, isFirstParent);
			for (FieldNode field : decl.getFieldChanges())
				updateChange(field, change, isFirstParent);
		}
	}

	public void compare(FileNode leftNode, FileNode rightNode, DeclCollector declCollector, boolean isFirstParent)
			throws Exception {
		List<Declaration> leftDecls = null;


		// both have the same content id (COPIED)
		if (leftNode.getChangedFile().getObjectId().equals(rightNode.getChangedFile().getObjectId())) {
			// 2nd parent then add copied as 2nd change
			if (!isFirstParent && rightNode.getASTChangeCount() != 0)
				updateAllChanges(rightNode, ChangeKind.COPIED, false);
			return;
		}

		// left is added
		if (leftNode.getChangedFile().getChange() == ChangeKind.ADDED) {
			if (leftDecls == null)
				leftDecls = declCollector.getDeclNodes(leftNode);
			update(leftNode, leftDecls, ChangeKind.ADDED, true);
		}
		
		// left is deleted
		if (leftNode.getChangedFile().getChange() == ChangeKind.DELETED) {
			return;
		}

		// right is deleted
		if (rightNode.getChangedFile().getChange() == ChangeKind.DELETED) {
			if (leftDecls == null)
				leftDecls = declCollector.getDeclNodes(leftNode);
			update(rightNode, leftDecls, ChangeKind.DELETED, true);
		}

		// right is modified/renamed/added
		if (rightNode.getChangedFile().getChange() != ChangeKind.DELETED) {
			if (leftDecls == null)
				leftDecls = declCollector.getDeclNodes(leftNode);
			
			List<Declaration> rightDecls = declCollector.getDeclNodes(rightNode);
			Set<Integer> deleted = Stream.iterate(0, n -> n + 1).limit(leftDecls.size()).collect(Collectors.toSet());
			Set<Integer> added = Stream.iterate(0, n -> n + 1).limit(rightDecls.size()).collect(Collectors.toSet());
			for (int i = 0; i < leftDecls.size(); i++) {
				Declaration leftDecl = leftDecls.get(i);
				for (int j = 0; j < rightDecls.size(); j++) {
					if (added.contains(j)) {
						Declaration rightDecl = rightDecls.get(j);
						if (leftDecl.getFullyQualifiedName().equals(rightDecl.getFullyQualifiedName())) {
							deleted.remove(i);
							added.remove(j);
							compareDecls(leftDecl, rightDecl, rightNode, isFirstParent);
							break;
						}
					}
				}
			}
			for (int i : deleted)
				update(rightNode, leftDecls.get(i), ChangeKind.DELETED, isFirstParent);
			for (int j : added)
				update(rightNode, rightDecls.get(j), ChangeKind.ADDED, isFirstParent);
		}

	}

	private void compareDecls(Declaration leftDecl, Declaration rightDecl, FileNode rightNode, boolean isFirstParent) {
		
		// compare fields
		Set<Integer> deleted1 = Stream.iterate(0, n -> n + 1).limit(leftDecl.getFieldsCount())
				.collect(Collectors.toSet());
		Set<Integer> added1 = Stream.iterate(0, n -> n + 1).limit(rightDecl.getFieldsCount())
				.collect(Collectors.toSet());
		Set<Integer> modified1 = new HashSet<Integer>();

		for (int i = 0; i < leftDecl.getFieldsCount(); i++) {
			Variable leftVar = leftDecl.getFields(i);
			for (int j = 0; j < rightDecl.getFieldsCount(); j++) {
				if (added1.contains(j)) {
					Variable rightVar = rightDecl.getFields(j);
					if (getSignature(leftVar).equals(getSignature(rightVar))) {
						if (!prettyprint(leftVar).equals(prettyprint(rightVar)))
							modified1.add(j);
						deleted1.remove(i);
						added1.remove(j);
						break;
					}
				}
			}
		}

		// compare methods
		Set<Integer> deleted2 = Stream.iterate(0, n -> n + 1).limit(leftDecl.getMethodsCount())
				.collect(Collectors.toSet());
		Set<Integer> added2 = Stream.iterate(0, n -> n + 1).limit(rightDecl.getMethodsCount())
				.collect(Collectors.toSet());
		Set<Integer> modified2 = new HashSet<Integer>();

		for (int i = 0; i < leftDecl.getMethodsCount(); i++) {
			Method leftMethod = leftDecl.getMethods(i);
			for (int j = 0; j < rightDecl.getMethodsCount(); j++) {
				if (added2.contains(j)) {
					Method rightMethod = rightDecl.getMethods(j);
					if (getSignature(leftMethod).equals(getSignature(rightMethod))) {
						if (!prettyprint(leftMethod).equals(prettyprint(rightMethod)))
							modified2.add(j);
						deleted2.remove(i);
						added2.remove(j);
						break;
					}
				}
			}
		}

		// no ast changes
		if (deleted1.size() + added1.size() + modified1.size() + deleted2.size() + added2.size()
				+ modified2.size() == 0) {
			// 2nd parent then add copied as 2nd change
			if (!isFirstParent && rightNode.getASTChangeCount() != 0)
				updateAllChanges(rightNode, ChangeKind.COPIED, true);
			return;
		}

		DeclNode declNode = update(rightNode, rightDecl, ChangeKind.MODIFIED, isFirstParent);

		// update field changes
		for (int i : deleted1)
			update(declNode, leftDecl.getFields(i), ChangeKind.DELETED, isFirstParent);
		for (int j : added1)
			update(declNode, rightDecl.getFields(j), ChangeKind.ADDED, isFirstParent);
		for (int j : modified1)
			update(declNode, rightDecl.getFields(j), ChangeKind.MODIFIED, isFirstParent);

		// update method chagnes
		for (int i : deleted2)
			update(declNode, leftDecl.getMethods(i), ChangeKind.DELETED, isFirstParent);
		for (int j : added2)
			update(declNode, rightDecl.getMethods(j), ChangeKind.ADDED, isFirstParent);
		for (int j : modified2)
			update(declNode, rightDecl.getMethods(j), ChangeKind.MODIFIED, isFirstParent);

	}


	private String getSignature(Method m) {
		StringBuilder sb = new StringBuilder();
		if (m.getModifiersCount() > 0)
			sb.append(m.getModifiers(0).getVisibility().toString().toLowerCase() + " ");
		sb.append(m.getName() + "(");
		for (int i = 0; i < m.getArgumentsCount(); i++) {
			if (i > 0)
				sb.append(", ");
			Variable v = m.getArguments(i);
			sb.append(v.getName() + " ");
			if (v.hasVariableType())
				sb.append(v.getVariableType().getName());
		}
		sb.append(")");
		if (m.hasReturnType())
			sb.append(" : " + m.getReturnType().getName());
		return sb.toString();
	}

	private String getSignature(Variable v) {
		StringBuilder sb = new StringBuilder();
		if (v.getModifiersCount() > 0)
			sb.append(v.getModifiers(0).getVisibility().toString().toLowerCase() + " ");
		sb.append(v.getName());
		if (v.hasVariableType())
			sb.append(" : " + v.getVariableType().getName());
		return sb.toString();
	}

}