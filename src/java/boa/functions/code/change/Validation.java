package boa.functions.code.change;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import boa.functions.code.change.declaration.DeclNode;
import boa.functions.code.change.declaration.DeclTree;
import boa.functions.code.change.file.FileNode;
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

	private HashSet<String> declNodes;
	private HashSet<String> fieldNodes;
	private HashSet<String> methodNodes;

	public Validation(ChangeDataBase db) {
		this.db = db;
	}

	public Validation validate() throws Exception {

		System.out.println(db.cr.getBranchesList());
		System.out.println(db.cr.getBranchNamesList());

		ASTCollector astCollector = new ASTCollector();
		for (int headIdx : db.cr.getBranchesList()) {
			initial();
			for (ChangedFile cf : getSnapshot(db.cr, headIdx, true)) {
				astCollector.visit(cf);
			}

			System.out.println("declNodes in last snapshot: " + declNodes.size());

			validate(headIdx);

			System.out.println("branch Idx: " + headIdx);
			System.out.println("left declNodes: " + declNodes.size());

//			for (String s : declNodes)
//				System.out.println(s);

			System.out.println();

//			break;
		}
		return this;
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
				int parentIdx = r.getParents(i);				
				if (!visitedRevNodes.contains(parentIdx))
					stack.push(db.revIdxMap.get(parentIdx));
			}
		}
	}

	private void matchASTChanges(RevNode cur) {
		for (FileNode fn : cur.getFileChangeMap().values()) {			
			for (DeclNode dn : fn.getDeclChanges()) {
				matchASTChanges(dn);
			}
		}
	}

	private void matchASTChanges(DeclNode dn) {

		String sig = dn.getFileNode().getSignature() + " " + dn.getSignature();

		// check visited tree
		if (visitedDeclTrees.contains(dn.getTreeId())) {
			if (declNodes.contains(sig)) {
				declNodes.remove(sig);
			}
			return;
		}
		// ignore deleted type
		if (dn.getFirstChange() == ChangeKind.DELETED) {
			if (declNodes.contains(sig)) {
				declNodes.remove(sig);
			}
			visitedDeclTrees.add(dn.getTreeId());
			return;
		}
		// check astNodes
		if (declNodes.contains(sig)) {
			declNodes.remove(sig);
			visitedDeclTrees.add(dn.getTreeId());
		} else {
			int treeId = dn.getTreeId();
			DeclTree declTree = db.declForest.get(treeId);
			System.out.println("ERR: cannot find " + dn + " tree id: " + dn.getTreeId() + " tree size: "
					+ declTree.getDeclNodes().size());
		}
	}

	private void initial() {
		visitedFieldTrees = new HashSet<Integer>();
		visitedMethodTrees = new HashSet<Integer>();
		visitedDeclTrees = new HashSet<Integer>();
		visitedRevNodes = new HashSet<Integer>();
		declNodes = new HashSet<String>();
		fieldNodes = new HashSet<String>();
		methodNodes = new HashSet<String>();
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
			for (Method m : node.getMethodsList())
				methodNodes.add(declSig + " " + getSignature(m));
			for (Variable v : node.getFieldsList())
				fieldNodes.add(declSig + " " + getSignature(v));
			for (Declaration d : node.getNestedDeclarationsList())
				visit(d);
			return false;
		}

	}
}
