package boa.functions.code.change;

import static boa.functions.BoaAstIntrinsics.getCodeChange;
import static boa.functions.code.change.refactoring.BoaRefactoringPredictionIntrinsics.getRefactorings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import boa.types.Code.CodeRefactoring;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.ChangeKind;
import boa.types.Toplevel.Project;

public class FileChangeForest {

	// tree properties
	protected List<FileTree> trees = new ArrayList<FileTree>();
	
	protected boolean debug = false;
	
	// global data
	protected final GlobalData gd;
	
	public FileChangeForest(GlobalData gd, boolean debug) {
		this.gd = gd;
		this.debug = debug;
		updateTrees();
	}

	private void updateTrees() {
		for (int i = gd.revIdxMap.size() - 1; i >= 0; i--) {
			RevNode r = gd.revIdxMap.get(i);
			for (FileNode fn : r.getJavaFileNodes()) {
				if (!gd.fileLocIdToNode.containsKey(fn.getLoc())) {
					FileTree list = new FileTree(this, fn, trees.size());
					if (list.linkAll())
						trees.add(list);
				}
			}
		}
	}

	public List<FileTree> getTreesAsList() {
		return this.trees;
	}
	
	// refactoring functions
	public void updateWithRefs(Project p, HashSet<String> refRevIds, Set<String> refTypes) {
		for (String id : refRevIds) {
			RevNode r = gd.revIdMap.get(id);
			List<CodeRefactoring> refs = refTypes == null ? getCodeChange(p, r.getRevision()).getRefactoringsList()
					: getRefactorings(p, r.getRevision(), refTypes);
			for (CodeRefactoring ref : refs) {
				String beforeFilePath = ref.getLeftSideLocations(0).getFilePath();
				FileNode fileBefore = findLastModification(beforeFilePath, r);
				String afterFilePath = ref.getRightSideLocations(0).getFilePath();
				FileNode fileAfter = getFileNodeFrom(afterFilePath, r);
				RefactoringBond refBond = new RefactoringBond(fileBefore.getLoc(), fileAfter.getLoc(), ref);
				int refBondIdx = gd.refBonds.size();
				gd.refBonds.add(refBond);
				// update file ref bonds
				fileBefore.getRightRefBondIdxs().add(refBondIdx);
				fileAfter.getLeftRefBondIdxs().add(refBondIdx);
				// update tree ref locations
				int beforeTreeIdx = gd.fileLocIdToNode.get(fileBefore.getLoc()).getTreeObjectId().getAsInt();
				FileTree beforeTree = trees.get(beforeTreeIdx);
				beforeTree.fileBeforeRef.add(fileBefore.getLoc());
				int afterTreeIdx = gd.fileLocIdToNode.get(fileAfter.getLoc()).getTreeObjectId().getAsInt();
				FileTree afterTree = trees.get(afterTreeIdx);
				afterTree.fileBeforeRef.add(fileAfter.getLoc());
			}
		}
	}
	
	private FileNode findLastModification(String fileName, RevNode r) {
		FileNode fn = getFileNodeFrom(fileName, r);
		if (fn != null && fn.getChangedFile().getChange() == ChangeKind.ADDED)
			return fn;
		RevNode cur = r;
		do {
			if (cur.getRevision().getParentsCount() == 0)
				return null;
			// first parent in main branch
			cur = gd.revIdxMap.get(cur.getRevision().getParents(0));
			fn = getFileNodeFrom(fileName, cur);
			if (fn != null) {
				return fn;
			}
			
		} while (true);
	}
	
	private FileNode getFileNodeFrom(String filePath, RevNode r) {
		for (ChangedFile cf : r.getRevision().getFilesList())
			if (cf.getName().equals(filePath))
				return gd.fileLocIdToNode.get(new FileLocation(cf.getRevisionIdx(), cf.getFileIdx()));
		return null;
	}
	
	
}
