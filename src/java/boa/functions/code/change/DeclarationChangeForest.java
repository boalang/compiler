package boa.functions.code.change;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import boa.runtime.BoaAbstractVisitor;
import boa.types.Ast.Declaration;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.ChangeKind;

import static boa.functions.BoaAstIntrinsics.*;

public class DeclarationChangeForest {

	protected List<DeclarationTree> trees = new ArrayList<DeclarationTree>();

	private FileChangeForest fcf;
	private HashSet<String> visitedFileObjectIds = new HashSet<String>();
	
	
	private FileNode curFN;
	private BoaAbstractVisitor visitor = new BoaAbstractVisitor() {
		private int declIdx = 0;
		@Override
		public void postVisit(final ChangedFile node) throws Exception { 
			declIdx = 0; 
		}
		@Override
		public boolean preVisit(final Declaration node) throws Exception {
			String fqn = node.getFullyQualifiedName();
			DeclarationNode declNode = new DeclarationNode(curFN, fqn, declIdx++);
			if (!fcf.gd.declLocToNode.containsKey(declNode.getLoc())) {
				fcf.gd.declLocToNode.put(declNode.getLoc(), declNode);
			}
			System.out.println(declNode);
			for (Declaration d : node.getNestedDeclarationsList())
				visit(d);
			return false;
		}
	};
	
	public DeclarationChangeForest(FileChangeForest forest) throws Exception {
		this.fcf = forest;
		for (Entry<FileLocation, FileNode> e : fcf.gd.fileLocIdToNode.descendingMap().entrySet()) {
			FileNode fn = e.getValue();
			if (fn.getChangedFile().getChange() != ChangeKind.DELETED) {
				curFN = fn;
				visitor.visit(fn.getChangedFile());
			}
		}
	}
}
