package boa.functions.refactoring;

import static boa.functions.refactoring.BoaRefactoringPredictionIntrinsics.revIdxMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Map.Entry;

public class FileChangeLinkedList {

	private final FileChangeLinkedLists fileChangeLinkedLists;
	public int id;
	public HashMap<String, FileNode> fileLocIdToNode = new HashMap<String, FileNode>();
	public Queue<Integer> prevRevIdxs = new LinkedList<Integer>();
	public Queue<Integer> prevFileIdxs = new LinkedList<Integer>();
	// refactoring
	public HashSet<String> refLocs = new HashSet<String>();

	public FileChangeLinkedList(FileChangeLinkedLists fileChangeLinkedLists, FileNode node, int listIdx) {
		this.fileChangeLinkedLists = fileChangeLinkedLists;
		this.id = listIdx;
		add(node);
	}

	public boolean linkAll() {
		while (!prevRevIdxs.isEmpty()) {
			int prevRevIdx = prevRevIdxs.poll();
			int prevFileIdx = prevFileIdxs.poll();
			Rev prevRev = revIdxMap.get(prevRevIdx);
			if (!add(new FileNode(prevRev.rev.getFiles(prevFileIdx), prevRev, prevFileIdx)))
				return false;
		}
		return true;
	}

	private boolean add(FileNode node) {
		if (this.fileChangeLinkedLists.debug)
			System.out.println("try to add node " 
					+ node.getLocId() + " " + node.cf.getChange() + " to list " + this.id);
		// check if the node is added by some lists
		if (this.fileChangeLinkedLists.fileLocIdToListIdx.containsKey(node.getLocId())) {
			int listIdx = this.fileChangeLinkedLists.fileLocIdToListIdx.get(node.getLocId());
			if (listIdx != this.id) {
				if (this.fileChangeLinkedLists.debug)
					System.out.println("node " + node.getLocId() + " already added to list " + listIdx);
				this.fileChangeLinkedLists.lists.get(listIdx).merge(this);
				if (this.fileChangeLinkedLists.debug)
					System.out.println("drop list " + this.id);
				return false;
			}
			return true;
		}
		// update list
		if (fileLocIdToNode.containsKey(node.getLocId()))
			System.out.println("err!!!!!!!!!!!!!!!!!");
		fileLocIdToNode.put(node.getLocId(), node);
		this.fileChangeLinkedLists.fileLocIdToListIdx.put(node.getLocId(), this.id);
		if (node.cf.getPreviousVersionsCount() != 0 && node.cf.getPreviousIndicesCount() != 0) {
			prevRevIdxs.addAll(node.cf.getPreviousVersionsList());
			prevFileIdxs.addAll(node.cf.getPreviousIndicesList());
		}
		return true;
	}

	public void merge(FileChangeLinkedList list) {
		if (this.fileChangeLinkedLists.debug)
			System.out.println("list " + this.id + " merge list " + list.id);
		for (Entry<String, FileNode> entry : list.fileLocIdToNode.entrySet()) {
			this.fileLocIdToNode.put(entry.getKey(), entry.getValue());
			this.fileChangeLinkedLists.fileLocIdToListIdx.put(entry.getKey(), this.id);
		}
		this.fileLocIdToNode.putAll(list.fileLocIdToNode);
		// merge queues
		while (!list.prevRevIdxs.isEmpty()) {
			int prevRevIdx = list.prevRevIdxs.poll();
			int prevFileIdx = list.prevFileIdxs.poll();
			if (!fileLocIdToNode.containsKey(prevRevIdx + " " + prevFileIdx)) {
				this.prevRevIdxs.offer(prevRevIdx);
				this.prevFileIdxs.offer(prevFileIdx);
			}
		}
		this.refLocs.addAll(list.refLocs);
		if (list.id < this.fileChangeLinkedLists.lists.size())
			this.fileChangeLinkedLists.lists.set(list.id, null);
		linkAll();
	}
}