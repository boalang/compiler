package boa.functions.code.change;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import boa.runtime.BoaAbstractVisitor;
import boa.types.Ast.Declaration;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.ChangeKind;

import static boa.functions.BoaAstIntrinsics.*;

public class DeclChangeForest {

	protected List<DeclTree> trees = new ArrayList<DeclTree>();
	protected FileChangeForest fcf;
	protected HashSet<String> visitedFileObjectIds = new HashSet<String>();
	protected HashSet<DeclLocation> visitedDecls = new HashSet<DeclLocation>();
	// considered ref types
	protected HashSet<String> refTypes = new HashSet<String>(
			Arrays.asList(new String[] { "Move Class", "Rename Class" }));

//	protected DeclarationCollector declCollector = new DeclarationCollector();
	
	public DeclChangeForest(FileChangeForest forest) throws Exception {
		this.fcf = forest;
//		this.updateTrees();
	}
	
//	private void updateTrees() throws Exception {
//		for (Entry<FileLocation, FileNode> e : fcf.gd.fileLocIdToNode.descendingMap().entrySet()) {
//			FileNode fn = e.getValue();
//			if (fn.getChangedFile().getChange() != ChangeKind.DELETED) {
//				for (DeclarationNode dn : declCollector.getDeclNodes(fn)) {
//					if (!visitedDecls.contains(dn.getLoc())) {
//						DeclarationTree tree = new DeclarationTree(this, dn, trees.size());
//						if (tree.linkAll()) {
//							trees.add(tree);
//							visitedDecls.addAll(tree.getDeclLocs());
//						}
//					}
//				}
//			}
//		}
//	}
//	
//	public class DeclarationCollector extends BoaAbstractVisitor {
//		private int declIdx;
//		private FileNode fn;
//		private List<DeclarationNode> nodes = new ArrayList<DeclarationNode>();
//		
//		@Override
//		public boolean preVisit(final Declaration node) throws Exception {
//			String fqn = node.getFullyQualifiedName();
//			DeclarationNode declNode = new DeclarationNode(fn, fqn, declIdx++);
//			fcf.gd.declLocToNode.put(declNode.getLoc(), declNode);
//			nodes.add(declNode);
//			System.out.println(declNode);
//			for (Declaration d : node.getNestedDeclarationsList())
//				visit(d);
//			return false;
//		}
//		
//		public List<DeclarationNode> getDeclNodes(FileNode fn) throws Exception {
//			this.declIdx = 0;
//			this.fn = fn;
//			this.nodes.clear();
//			this.visit(fn.getChangedFile());
//			return nodes;
//		}
//	}
}
