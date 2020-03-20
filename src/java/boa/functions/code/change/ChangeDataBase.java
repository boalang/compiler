package boa.functions.code.change;

import static boa.functions.BoaIntrinsics.getRevision;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import boa.functions.code.change.declaration.ChangedDeclLocation;
import boa.functions.code.change.declaration.ChangedDeclNode;
import boa.functions.code.change.field.ChangedFieldLocation;
import boa.functions.code.change.field.ChangedFieldNode;
import boa.functions.code.change.file.ChangedFileLocation;
import boa.functions.code.change.file.ChangedFileNode;
import boa.functions.code.change.method.ChangedMethodLocation;
import boa.functions.code.change.method.ChangedMethodNode;
import boa.functions.code.change.refactoring.RefactoringBond;
import boa.types.Code.CodeRepository;
import boa.types.Code.Revision;

public class ChangeDataBase {

	// revision data
	public HashSet<String> nContributor = new HashSet<String>();
	public HashMap<Integer, RevNode> revIdxMap = new HashMap<Integer, RevNode>();
	public HashMap<String, RevNode> revIdMap = new HashMap<String, RevNode>();

	// file change data
	public TreeMap<ChangedFileLocation, ChangedFileNode> fileDB = new TreeMap<ChangedFileLocation, ChangedFileNode>();

	// refactoring data
	public List<RefactoringBond> refDB = new ArrayList<RefactoringBond>();

	// declaration data
	public TreeMap<ChangedDeclLocation, ChangedDeclNode> declDB = new TreeMap<ChangedDeclLocation, ChangedDeclNode>();

	// field change data
	public TreeMap<ChangedMethodLocation, ChangedMethodNode> methodDB = new TreeMap<ChangedMethodLocation, ChangedMethodNode>();

	// method change data
	public TreeMap<ChangedFieldLocation, ChangedFieldNode> fieldDB = new TreeMap<ChangedFieldLocation, ChangedFieldNode>();

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
