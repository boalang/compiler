package boa.functions.code.change;

import java.util.HashSet;
import java.util.Stack;

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
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.ChangeKind;

import static boa.functions.BoaIntrinsics.*;
import static boa.functions.code.change.ASTChange.*;

public class Validation {

	private ChangeDataBase db;

	private HashSet<Integer> visitedDeclTrees;
	private HashSet<Integer> visitedFieldTrees;
	private HashSet<Integer> visitedMethodTrees;

	private HashSet<Integer> visitedRevNodes;
	private HashSet<String> visitedASTSigs;

	private HashSet<String> declNodes;
	private HashSet<String> fieldNodes;
	private HashSet<String> methodNodes;

	private String s = "";

	// output
	private int declChanges;
	private int fieldChanges;
	private int methodChanges;
	private int unmatchedDeclChanges;
	private int unmatchedFieldChanges;
	private int unmatchedMethodChanges;

	public Validation(ChangeDataBase db) {
		this.db = db;
	}

	private void initial() {
		visitedFieldTrees = new HashSet<Integer>();
		visitedMethodTrees = new HashSet<Integer>();
		visitedDeclTrees = new HashSet<Integer>();
		visitedRevNodes = new HashSet<Integer>();
		declNodes = new HashSet<String>();
		fieldNodes = new HashSet<String>();
		methodNodes = new HashSet<String>();
		visitedASTSigs = new HashSet<String>();
		declChanges = 0;
		fieldChanges = 0;
		methodChanges = 0;
		unmatchedDeclChanges = 0;
		unmatchedFieldChanges = 0;
		unmatchedMethodChanges = 0;
	}

	public double[][] validate() throws Exception {

		ASTCollector astCollector = new ASTCollector();
		double[][] res = new double[db.cr.getBranchesCount()][6];

		for (int i = 0; i < db.cr.getBranchesCount(); i++) {
			int headIdx = db.cr.getBranches(i);
			String head = db.cr.getBranchNames(i);

			initial();
			for (ChangedFile cf : getSnapshot(db.cr, headIdx, true))
				astCollector.visit(cf);

			System.out.println(
					"branch Idx: " + headIdx + " " + db.revIdxMap.get(headIdx).getRevision().getId() + " " + head);
			System.out.println("declNodes in last snapshot: " + declNodes.size());
			System.out.println("fieldNodes in last snapshot: " + fieldNodes.size());
			System.out.println("methodNodes in last snapshot: " + methodNodes.size());

			double lastDecls = declNodes.size();
			double lastFields = fieldNodes.size();
			double lastMethods = methodNodes.size();

			validate(headIdx);

			System.out.println();

			double matchedLastDeclRatio = ratio(declNodes.size(), lastDecls);
			double matchedLastFieldRatio = ratio(fieldNodes.size(), lastFields);
			double matchedLastMethodRatio = ratio(methodNodes.size(), lastMethods);

			double matchedDeclRatio = ratio(unmatchedDeclChanges, declChanges);
			double matchedFieldRatio = ratio(unmatchedFieldChanges, fieldChanges);
			double matchedMethodRatio = ratio(unmatchedMethodChanges, methodChanges);

			System.out.println(
					"left declNodes: " + declNodes.size() + " " + matchedLastDeclRatio + " " + matchedDeclRatio);
			System.out.println(
					"left fieldNodes: " + fieldNodes.size() + " " + matchedLastFieldRatio + " " + matchedFieldRatio);
			System.out.println(
					"left methodNodes: " + methodNodes.size() + " " + matchedLastMethodRatio + " " + matchedMethodRatio);

			System.out.println();

			res[i] = new double[] { 
					matchedLastDeclRatio, matchedDeclRatio, 
					matchedLastFieldRatio, matchedFieldRatio,
					matchedLastMethodRatio, matchedMethodRatio 
			};
		}
		return res;
	}
	
	private double ratio(double unmatched, double total) {
		return total == 0 ? 1.0 : (1.0 - unmatched / total);
	}

	private void validate(int headIdx) {
		RevNode head = db.revIdxMap.get(headIdx);
		Stack<RevNode> stack = new Stack<RevNode>();
		stack.push(head);
		while (!stack.isEmpty()) {
			RevNode cur = stack.pop();
			visitedRevNodes.add(cur.getRevIdx());
			// match all ast changes in the revNode
			matchASTChanges(cur);
			// update stack
			Revision r = cur.getRevision();
			for (int i = r.getParentsCount() - 1; i >= 0; i--) {
				// ignore feature branches with no changed files in merge commit
				if (i == 1 && r.getFilesCount() == 0)
					continue;
				int parentIdx = r.getParents(i);
				if (!visitedRevNodes.contains(parentIdx))
					stack.push(db.revIdxMap.get(parentIdx));
			}
		}
	}

	private void matchASTChanges(RevNode cur) {
		for (FileNode fn : cur.getFileChangeMap().values()) {
			for (DeclNode dn : fn.getDeclChanges()) {
				String declSig = fn.getSignature() + " " + dn.getSignature();
				matchChanges(dn, declSig);
				visitedASTSigs.add(declSig);
				declChanges++;
				for (FieldNode fieldNode : dn.getFieldChanges()) {
					String fieldSig = declSig + " " + fieldNode.getSignature();
					matchChanges(fieldNode, fieldSig);
					visitedASTSigs.add(fieldSig);
					fieldChanges++;
				}
				for (MethodNode methodNode : dn.getMethodChanges()) {
					String methodSig = declSig + " " + methodNode.getSignature();
					matchChanges(methodNode, methodSig);
					visitedASTSigs.add(methodSig);
					methodChanges++;
				}
			}
		}
	}

	private void matchChanges(DeclNode dn, String sig) {
		// check visited tree
		if (visitedDeclTrees.contains(dn.getTreeId())) {
			if (declNodes.contains(sig))
				declNodes.remove(sig);
			return;
		}
		// ignore deleted type
		if (dn.getFirstChange() == ChangeKind.DELETED) {
			visitedDeclTrees.add(dn.getTreeId());
			return;
		}
		// check astNodes
		if (declNodes.contains(sig)) {
			declNodes.remove(sig);
			visitedDeclTrees.add(dn.getTreeId());
		} else if (visitedASTSigs.contains(sig)) {
			return;
		} else {
			int treeId = dn.getTreeId();
			DeclTree declTree = db.declForest.get(treeId);
			System.out.println("ERR: cannot find decl " + dn + " tree id: " + dn.getTreeId() + " tree size: "
					+ declTree.getDeclNodes().size());
			unmatchedDeclChanges++;
		}
	}

	private void matchChanges(FieldNode fn, String sig) {
		// check visited tree
		if (visitedFieldTrees.contains(fn.getTreeId())) {
			if (fieldNodes.contains(sig))
				fieldNodes.remove(sig);
			return;
		}
		// ignore deleted type
		if (fn.getFirstChange() == ChangeKind.DELETED) {
			visitedFieldTrees.add(fn.getTreeId());
			return;
		}
		// check astNodes
		if (fieldNodes.contains(sig)) {
			fieldNodes.remove(sig);
			visitedFieldTrees.add(fn.getTreeId());
		} else if (visitedASTSigs.contains(sig)) {
			return;
		} else {
			int treeId = fn.getTreeId();
			FieldTree fieldTree = db.fieldForest.get(treeId);
			System.out.println("ERR: cannot find field " + fn + " tree id: " + fn.getTreeId() + " tree size: "
					+ fieldTree.getFieldNodes().size());
			unmatchedFieldChanges++;
		}
	}

	private void matchChanges(MethodNode mn, String sig) {
		// check visited tree
		if (visitedMethodTrees.contains(mn.getTreeId())) {
			if (methodNodes.contains(sig))
				methodNodes.remove(sig);
			return;
		}
		// ignore deleted type
		if (mn.getFirstChange() == ChangeKind.DELETED) {
			visitedMethodTrees.add(mn.getTreeId());
			return;
		}
		// check astNodes
		if (methodNodes.contains(sig)) {
			methodNodes.remove(sig);
			visitedMethodTrees.add(mn.getTreeId());
		} else if (visitedASTSigs.contains(sig)) {
			return;
		} else {
			int treeId = mn.getTreeId();
			MethodTree methodTree = db.methodForest.get(treeId);
			System.out.println("ERR: cannot find method " + mn + " tree id: " + mn.getTreeId() + " tree size: "
					+ methodTree.getMethodNodes().size());
			unmatchedMethodChanges++;
		}
	}

	public class ASTCollector extends BoaAbstractVisitor {
		private String fileName;

		@Override
		public boolean preVisit(final ChangedFile node) throws Exception {
			fileName = node.getName();
			return true;
		}

		@Override
		public boolean preVisit(final Declaration node) throws Exception {
			String declSig = fileName + " " + node.getFullyQualifiedName();
			declNodes.add(declSig);
			for (Method m : node.getMethodsList()) {
				String methodSig = declSig + " " + getSignature(m);
				methodNodes.add(methodSig);
			}
			for (Variable v : node.getFieldsList()) {
				String fieldSig = declSig + " " + getSignature(v);
				fieldNodes.add(fieldSig);
			}
			for (Declaration d : node.getNestedDeclarationsList())
				visit(d);
			return false;
		}

	}

	public String getOutput() {
		String res = s;
		s = null;
		return res;
	}
}
