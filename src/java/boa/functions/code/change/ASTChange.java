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
			// update decl
			Declaration decl = decls.get(i);
			ChangedDeclNode declNode = update(fileNode, decl, change);
			// update method
			for (int j = 0; j < decl.getMethodsCount(); j++) {
				Method m = decl.getMethods(j);
				ChangedMethodNode methodNode = declNode.getNewMethodNode(getSignature(m), change);
				db.methodDB.put(methodNode.getLoc(), methodNode);
			}
			// update field
			for (int j = 0; j < decl.getFieldsCount(); j++) {
				Variable v = decl.getFields(j);
				ChangedFieldNode fieldNode = declNode.getNewFieldNode(getSignature(v), change);
				db.fieldDB.put(fieldNode.getLoc(), fieldNode);
			}
		}
	}
	

	private ChangedDeclNode update(ChangedFileNode fileNode, Declaration decl, ChangeKind change) {
		ChangedDeclNode declNode = fileNode.getNewDeclNode(decl.getFullyQualifiedName(), change);
		db.declDB.put(declNode.getLoc(), declNode);
		return declNode;
	}

	public void compare(ChangedFileNode leftNode, ChangedFileNode rightNode, DeclCollector declCollector, int prevIdx) throws Exception {
		List<Declaration> leftDecls = null;

		if (prevIdx == 1) {
			System.out.println(leftNode.getChangedFile().getChange() + " " + rightNode.getChangedFile().getChange()
					+ " " + leftNode.getChangedFile().getObjectId().equals(rightNode.getChangedFile().getObjectId()));
			return;
		}
		
		// both have the same content id (COPIED)
		if (leftNode.getChangedFile().getObjectId().equals(rightNode.getChangedFile().getObjectId())) {

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

		// right is modified/renamed
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
							compareDecls(leftDecl, rightDecl, j, rightNode);
							break;
						}
					}
				}
			}
			for (int i : deleted) {
				Declaration decl = leftDecls.get(i);
				update(rightNode, decl, ChangeKind.DELETED);
			}
			for (int j : added) {
				Declaration decl = rightDecls.get(j);
				update(leftNode, decl, ChangeKind.ADDED);
			}
		}

	}


	private void compareDecls(Declaration leftDecl, Declaration rightDecl, int rightDeclIdx, ChangedFileNode rightNode) {

		// compare fields
		Set<Integer> deleted = Stream.iterate(0, n -> n + 1).limit(leftDecl.getFieldsCount())
				.collect(Collectors.toSet());
		Set<Integer> added = Stream.iterate(0, n -> n + 1).limit(rightDecl.getFieldsCount())
				.collect(Collectors.toSet());
		Set<Integer> modified = new HashSet<Integer>();

		for (int i = 0; i < leftDecl.getFieldsCount(); i++) {
			Variable leftVar = leftDecl.getFields(i);
			for (int j = 0; j < rightDecl.getFieldsCount(); j++) {
				if (added.contains(j)) {
					Variable rightVar = rightDecl.getFields(j);
					if (leftVar.getName().equals(rightVar.getName())) {
						if (!prettyprint(leftVar).equals(prettyprint(rightVar)))
							modified.add(j);
						deleted.remove(i);
						added.remove(j);
						break;
					}
				}
			}
		}

		if (deleted.size() + added.size() == 0)
			return;
//		ChangedDeclNode declNode = new ChangedDeclNode(rightNode, rightDecl.getFullyQualifiedName(), rightDeclIdx,
//				ChangeKind.MODIFIED);
//
//		for (int i : deleted) {
//			updateFieldChange(leftDecl, i, declNode, rightASTChange, ChangeKind.DELETED);
//		}
//		for (int j : added) {
//			updateFieldChange(rightDecl, j, declNode, rightASTChange, ChangeKind.ADDED);
//		}

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