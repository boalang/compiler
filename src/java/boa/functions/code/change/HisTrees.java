package boa.functions.code.change;

import static boa.functions.BoaIntrinsics.getRevision;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import boa.types.Code.CodeRepository;
import boa.types.Code.Revision;

public class HisTrees {
	
	protected HashMap<Integer, Rev> revIdxMap = new HashMap<Integer, Rev>();
	protected HashMap<String, Rev> revIdMap = new HashMap<String, Rev>();
	protected List<FileTree> lists = new ArrayList<FileTree>();
	protected HashMap<String, HashSet<String>> fileObjectIdToLocs = new HashMap<String, HashSet<String>>();
	protected HashMap<String, FileNode> fileLocIdToNode = new HashMap<String, FileNode>();
	
	protected boolean debug = false;

	public HisTrees(boolean debug) {
		this.debug = debug;
		updateLists();
	}

	private void updateLists() {
		for (int i = revIdxMap.size() - 1; i >= 0; i--) {
			Rev r = revIdxMap.get(i);
			for (FileNode fn : r.getJavaFileNodes()) {
				if (!fileLocIdToNode.containsKey(fn.getLocId())) {
					FileTree list = new FileTree(this, fn, lists.size());
					if (list.linkAll())
						lists.add(list);
				}
			}
		}
	}
	
	// revision info
	private static HashSet<String> nContributor = new HashSet<String>();
	
	public Rev getRev(CodeRepository cr, int idx) {
		if (revIdxMap.containsKey(idx))
			return revIdxMap.get(idx);
		Revision r = getRevision(cr, idx);
		nContributor.add(r.getAuthor().getUsername());
		Rev rev = new Rev(idx, r, nContributor.size());
		revIdxMap.put(idx, rev);
		revIdMap.put(r.getId(), rev);
		return revIdxMap.get(idx);
	}

}
