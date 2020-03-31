package boa.functions.code.change;

import static boa.functions.BoaIntrinsics.getRevision;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import boa.functions.code.change.declaration.DeclLocation;
import boa.functions.code.change.declaration.DeclNode;
import boa.functions.code.change.declaration.DeclTree;
import boa.functions.code.change.field.FieldLocation;
import boa.functions.code.change.field.FieldNode;
import boa.functions.code.change.field.FieldTree;
import boa.functions.code.change.file.FileLocation;
import boa.functions.code.change.file.FileNode;
import boa.functions.code.change.file.FileTree;
import boa.functions.code.change.method.MethodLocation;
import boa.functions.code.change.method.MethodNode;
import boa.functions.code.change.method.MethodTree;
import boa.types.Code.CodeRepository;
import boa.types.Code.Revision;

public class ChangeDataBase {

	// revision data
	public HashSet<String> nContributor = new HashSet<String>();
	public HashMap<Integer, RevNode> revIdxMap = new HashMap<Integer, RevNode>();
	public HashMap<String, RevNode> revIdMap = new HashMap<String, RevNode>();

	// file change data
	public TreeMap<FileLocation, FileNode> fileDB = new TreeMap<FileLocation, FileNode>();
	// file change forest
	public HashMap<Integer, FileTree> fileForest = new HashMap<Integer, FileTree>();

	// declaration data
	public TreeMap<DeclLocation, DeclNode> declDB = new TreeMap<DeclLocation, DeclNode>();
	// declaration change forest
	public HashMap<Integer, DeclTree> declForest = new HashMap<Integer, DeclTree>();

	// field change data
	public TreeMap<MethodLocation, MethodNode> methodDB = new TreeMap<MethodLocation, MethodNode>();
	// field change forest
	public HashMap<Integer, FieldTree> fieldForest = new HashMap<Integer, FieldTree>();
	
	// method change data
	public TreeMap<FieldLocation, FieldNode> fieldDB = new TreeMap<FieldLocation, FieldNode>();
	// method change forest
	public HashMap<Integer, MethodTree> methodForest = new HashMap<Integer, MethodTree>();

	// refactoring data
	public List<RefactoringBond> refDB = new ArrayList<RefactoringBond>();

	public ChangeDataBase(CodeRepository cr, int revCount) {
		for (int i = 0; i < revCount; i++)
			getRev(cr, i);
	}

	// test
	public HashSet<String> fileNames = new HashSet<String>();

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
