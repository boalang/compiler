package boa.functions.code.change.file;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import boa.functions.code.change.ChangedASTNode;
import boa.functions.code.change.RevNode;
import boa.functions.code.change.declaration.DeclNode;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.ChangeKind;

public class FileNode extends ChangedASTNode implements Comparable<FileNode> {

	private RevNode r;
	private ChangedFile cf;
	private FileLocation loc;

	private FileNode firstParent;
	private FileNode secondParent;

	// changes
	private HashMap<String, Integer> declChangeMap = new HashMap<String, Integer>();
	private List<DeclNode> declChanges = new ArrayList<DeclNode>();

	public FileNode(ChangedFile cf, RevNode r) {
		super(cf.getName());
		this.cf = cf;
		this.r = r;
		this.loc = new FileLocation(cf.getRevisionIdx(), cf.getFileIdx());
	}

	public DeclNode updateDeclChange(String fqn) {
		if (!declChangeMap.containsKey(fqn)) {
			int idx = declChanges.size();
			DeclNode declNode = new DeclNode(fqn, this, idx);
			declChangeMap.put(fqn, idx);
			declChanges.add(declNode);
			return declNode;
		}
		return declChanges.get(declChangeMap.get(fqn));
	}

	public DeclNode getDeclChange(String fqn) {
		if (declChangeMap.containsKey(fqn))
			return declChanges.get(declChangeMap.get(fqn));
		return null;
	}

	public FileLocation getLoc() {
		return loc;
	}

	public int getRevIdx() {
		return cf.getRevisionIdx();
	}

	public int getFileIdx() {
		return cf.getFileIdx();
	}

	public int getTreeId() {
		return treeId;
	}

	public void setTreeId(int treeId) {
		this.treeId = treeId;
	}

	public ChangedFile getChangedFile() {
		return cf;
	}

	public RevNode getRev() {
		return r;
	}

	public List<Integer> getRevisionParents() {
		return r.getRevision().getParentsList();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((loc == null) ? 0 : loc.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileNode other = (FileNode) obj;
		if (loc == null) {
			if (other.loc != null)
				return false;
		} else if (!loc.equals(other.loc))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return r.getRevision().getId() + " " + loc + " " + cf.getName() + " " + firstChange + " " + secondChange;
	}

	public List<DeclNode> getDeclChanges() {
		return declChanges;
	}

	public int getASTChangeCount() {
		int count = 0;
		for (DeclNode declNode : declChanges)
			count += declNode.getASTChangeCount();
		return count;
	}

	public FileNode getFirstParent() {
		return firstParent;
	}

	public void setFirstParent(FileNode firstParent) {
		this.firstParent = firstParent;
	}

	public boolean hasFirstParent() {
		return firstParent != null;
	}

	public FileNode getSecondParent() {
		return secondParent;
	}

	public void setSecondParent(FileNode secondParent) {
		this.secondParent = secondParent;
	}

	public boolean hasSecondParent() {
		return secondParent != null;
	}

	public ChangeKind getFirstChange() {
		return firstChange;
	}

	public void setFirstChange(ChangeKind firstChange) {
		this.firstChange = firstChange;
	}

	public ChangeKind getSecondChange() {
		return secondChange;
	}

	public void setSecondChange(ChangeKind secondChange) {
		this.secondChange = secondChange;
	}

	public HashMap<String, Integer> getDeclChangeMap() {
		return declChangeMap;
	}

	@Override
	public int compareTo(FileNode o) {
		return this.loc.compareTo(o.getLoc());
	}

}