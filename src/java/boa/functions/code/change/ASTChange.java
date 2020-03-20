package boa.functions.code.change;

import static boa.functions.BoaAstIntrinsics.prettyprint;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import boa.functions.code.change.declaration.ChangedDeclNode;
import boa.functions.code.change.field.ChangedFieldNode;
import boa.functions.code.change.file.ChangedFileNode;
import boa.functions.code.change.file.FileChangeForest.DeclCollector;
import boa.functions.code.change.method.ChangedMethodNode;
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

	public void update(ChangedFileNode fileNode, List<Declaration> decls, ChangeKind change) {
		for (int i = 0; i < decls.size(); i++) {
			Declaration decl = decls.get(i);
			ChangedDeclNode declNode = update(fileNode, decl, change);
			for (int j = 0; j < decl.getMethodsCount(); j++)
				update(declNode, decl.getMethods(j), change);
			for (int j = 0; j < decl.getFieldsCount(); j++)
				update(declNode, decl.getFields(j), change);
		}
	}

	private void update(ChangedDeclNode declNode, Variable v, ChangeKind change) {
		ChangedFieldNode fieldNode = declNode.getNewFieldNode(getSignature(v), change);
		db.fieldDB.put(fieldNode.getLoc(), fieldNode);
	}

	private void update(ChangedDeclNode declNode, Method m, ChangeKind change) {
		ChangedMethodNode methodNode = declNode.getNewMethodNode(getSignature(m), change);
		db.methodDB.put(methodNode.getLoc(), methodNode);
	}

	private ChangedDeclNode update(ChangedFileNode fileNode, Declaration decl, ChangeKind change) {
		ChangedDeclNode declNode = fileNode.getNewDeclNode(decl.getFullyQualifiedName(), change);
		db.declDB.put(declNode.getLoc(), declNode);
		return declNode;
	}

	public void compare(ChangedFileNode leftNode, ChangedFileNode rightNode, DeclCollector declCollector, int prevIdx)
			throws Exception {
		List<Declaration> leftDecls = null;
		
		if (prevIdx == 1 && rightNode.getASTChangeCount() == 0) {
			System.out.println(rightNode); 
		}

		// both have the same content id (COPIED)
		if (leftNode.getChangedFile().getObjectId().equals(rightNode.getChangedFile().getObjectId())) {
			// 2nd parent then add copied as 2nd change
			if (prevIdx == 1 && rightNode.getASTChangeCount() != 0)
				updateChange(rightNode, ChangeKind.COPIED);
			return;
		}

		// left is added
		if (leftNode.getChangedFile().getChange() == ChangeKind.ADDED) {
			if (leftDecls == null)
				leftDecls = declCollector.getDeclNodes(leftNode);
			update(leftNode, leftDecls, ChangeKind.ADDED);
		}

		// right is deleted
		if (rightNode.getChangedFile().getChange() == ChangeKind.DELETED) {
			if (leftDecls == null)
				leftDecls = declCollector.getDeclNodes(leftNode);
			update(rightNode, leftDecls, ChangeKind.DELETED);
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
							compareDecls(leftDecl, rightDecl, rightNode, prevIdx);
							break;
						}
					}
				}
			}
			for (int i : deleted)
				update(rightNode, leftDecls.get(i), ChangeKind.DELETED);
			for (int j : added)
				update(leftNode, rightDecls.get(j), ChangeKind.ADDED);
		}

	}

	private void compareDecls(Declaration leftDecl, Declaration rightDecl, ChangedFileNode rightNode, int prevIdx) {
		
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

		if (deleted1.size() + added1.size() + modified1.size() + deleted2.size() + added2.size()
				+ modified2.size() == 0) {
			if (prevIdx == 1 && rightNode.getASTChangeCount() != 0)
				updateChange(rightNode, ChangeKind.COPIED);
			return;
		}

		ChangedDeclNode declNode = update(rightNode, rightDecl, ChangeKind.MODIFIED);

		// update field changes
		for (int i : deleted1)
			update(declNode, leftDecl.getFields(i), ChangeKind.DELETED);
		for (int j : added1)
			update(declNode, rightDecl.getFields(j), ChangeKind.ADDED);
		for (int j : modified1)
			update(declNode, rightDecl.getFields(j), ChangeKind.MODIFIED);

		// update method chagnes
		for (int i : deleted2)
			update(declNode, leftDecl.getMethods(i), ChangeKind.DELETED);
		for (int j : added2)
			update(declNode, rightDecl.getMethods(j), ChangeKind.ADDED);
		for (int j : modified2)
			update(declNode, rightDecl.getMethods(j), ChangeKind.MODIFIED);

	}

	private void updateChange(ChangedFileNode rightNode, ChangeKind change) {
		for (ChangedDeclNode decl : rightNode.getDeclChanges()) {
			decl.getChanges().add(change);
			for (ChangedMethodNode method : decl.getMethodChanges())
				method.getChanges().add(change);
			for (ChangedFieldNode field : decl.getFieldChanges())
				field.getChanges().add(change);
		}
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