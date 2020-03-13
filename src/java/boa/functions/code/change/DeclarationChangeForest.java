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

	
	private HashSet<String> visitedFileObjectIds = new HashSet<String>();
	
	
	private FileLocation curFL;
	private BoaAbstractVisitor visitor = new BoaAbstractVisitor() {
		private int declIdx = 0;
		@Override
		public void postVisit(final ChangedFile node) throws Exception { 
			declIdx = 0; 
		}
		@Override
		public boolean preVisit(final Declaration node) throws Exception {
			String fqn = node.getFullyQualifiedName();
			DeclarationLocation declLoc = new DeclarationLocation(curFL, declIdx++, fqn);
//			System.out.println(declLoc);
			for (Declaration d : node.getNestedDeclarationsList())
				visit(d);
			return false;
		}
	};
	
	public DeclarationChangeForest(FileChangeForest forest) throws Exception {
		
		for (Entry<FileLocation, FileNode> e : forest.gd.fileLocIdToNode.descendingMap().entrySet()) {
//			FileNode fn = forest.gd.fileLocIdToNode.get(loc);
//			System.out.println(e.getKey());`
			FileNode fn = e.getValue();
			if (fn.getChangedFile().getChange() != ChangeKind.DELETED) {
				curFL = fn.getLocId();
				visitor.visit(fn.getChangedFile());
			}
		}
		
		
		
		System.out.println(forest.gd.fileObjectIdToLocs.size());
		
	}
}
