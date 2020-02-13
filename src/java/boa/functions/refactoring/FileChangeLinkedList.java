package boa.functions.refactoring;

import static boa.functions.refactoring.FileChangeLinkedLists.revIdxMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.TreeMap;
import java.util.Map.Entry;

public class FileChangeLinkedList {

	private final FileChangeLinkedLists fileChangeLinkedLists;
	public int id;
	public HashMap<String, FileNode> fileLocIdToNode = new HashMap<String, FileNode>();
	public TreeMap<Integer, List<Integer>> revIdxToLocs = new TreeMap<Integer, List<Integer>>();
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
		if (!revIdxToLocs.containsKey(node.getRevIdx()))
			revIdxToLocs.put(node.getRevIdx(), new ArrayList<Integer>());
		revIdxToLocs.get(node.getRevIdx()).add(node.getRevIdx());
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
		for (Entry<Integer, List<Integer>> entry : list.revIdxToLocs.entrySet())
			if (!revIdxToLocs.containsKey(entry.getKey()))
				revIdxToLocs.put(entry.getKey(), entry.getValue());
			else
				revIdxToLocs.get(entry.getKey()).addAll(entry.getValue());
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
	
	public boolean validation() {
		int size = 0;
		for (Entry<Integer, List<Integer>> entry : revIdxToLocs.entrySet())
			size += entry.getValue().size();
		return size == fileLocIdToNode.size() ? true : false;
	}
}