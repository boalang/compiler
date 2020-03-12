package boa.functions.code.change;

import static boa.functions.BoaIntrinsics.getRevision;

import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

import boa.types.Code.CodeRepository;
import boa.types.Code.Revision;

public class GlobalData {

	// revision info
	protected HashSet<String> nContributor = new HashSet<String>();
	protected HashMap<Integer, RevNode> revIdxMap = new HashMap<Integer, RevNode>();
	protected HashMap<String, RevNode> revIdMap = new HashMap<String, RevNode>();
	
	// file info
	protected HashMap<String, TreeSet<FileLocation>> fileObjectIdToLocs = new HashMap<String, TreeSet<FileLocation>>();
	protected TreeMap<FileLocation, FileNode> fileLocIdToNode = new TreeMap<FileLocation, FileNode>();
	
	public GlobalData(CodeRepository cr, int revCount) {
		for (int i = 0; i < revCount; i++)
			getRev(cr, i);
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

}
