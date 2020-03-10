package boa.functions.code.change;

import static boa.functions.BoaIntrinsics.getRevision;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import boa.types.Code.CodeRepository;
import boa.types.Code.Revision;

public class HisTrees {

	// tree properties
	protected List<FileTree> trees = new ArrayList<FileTree>();
	protected HashMap<String, HashSet<String>> fileObjectIdToLocs = new HashMap<String, HashSet<String>>();
	protected HashMap<String, FileNode> fileLocIdToNode = new HashMap<String, FileNode>();
	
	protected boolean debug = false;
	
	// revision info
	protected HashSet<String> nContributor = new HashSet<String>();
	protected HashMap<Integer, RevNode> revIdxMap = new HashMap<Integer, RevNode>();
	protected HashMap<String, RevNode> revIdMap = new HashMap<String, RevNode>();

	public HisTrees(CodeRepository cr, int revCount, boolean debug) {
		for (int i = 0; i < revCount; i++)
			getRev(cr, i);
		this.debug = debug;
		updateLists();
	}

	private void updateLists() {
		for (int i = revIdxMap.size() - 1; i >= 0; i--) {
			RevNode r = revIdxMap.get(i);
			for (FileNode fn : r.getJavaFileNodes()) {
				if (!fileLocIdToNode.containsKey(fn.getLocId())) {
					FileTree list = new FileTree(this, fn, trees.size());
					if (list.linkAll())
						trees.add(list);
				}
			}
		}
	}
	
	private RevNode getRev(CodeRepository cr, int idx) {
		if (revIdxMap.containsKey(idx))
			return revIdxMap.get(idx);
		Revision r = getRevision(cr, idx);
		nContributor.add(r.getAuthor().getUsername());
		RevNode rev = new RevNode(idx, r, nContributor.size());
		revIdxMap.put(idx, rev);
		revIdMap.put(r.getId(), rev);
		return revIdxMap.get(idx);
	}

	public List<FileTree> getTreesAsList() {
		return this.trees;
	}

}
