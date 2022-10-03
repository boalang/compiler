package boa.aggregators;

import boa.graphs.cfg.*;

import java.util.HashMap;

public class GspanSearcher<G extends CFG> { //TODO: currently is only working with CFGs, will extend to more later.
	
	private G my_graph;
	private int maxSize;
	
	public GspanSearcher(G my_graph, int maxSize) {
		this.maxSize = maxSize;
		this.my_graph = my_graph;
	}
	
	public HashMap<String,Integer> search() {
		
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		
		//TODO: don't just start at the root...
		for(CFGNode start : my_graph.getNodes()) {
			result.putAll(dfs(start, null, "", 0, result));
		}
		
		return result;
	}
	
	public HashMap<String, Integer> dfs(CFGNode currNode, CFGEdge currEdge, String currString, int currSize, HashMap<String, Integer> currHM) {
		
		if (currSize == maxSize) {
			return currHM;
		}
		
		//Construct our extension of current string.
		if (currEdge == null) {
			currString = currNode.getName().toString();
		}
		else {
			currString = currString + "," + currEdge.getLabel().toString() + "," + currNode.getKind().toString();
		}
		
		//Go to every outward neighbor, expanding our results
		//NOTE: because we are only assigning 1 as our values for each pattern, 
		//      we can use HashMap.putall without worrying about overwriting data.
		for (CFGEdge next : currNode.getOutEdges()) {
			currHM.putAll(dfs(next.getDest(), next, currString, currSize++, currHM));
		}
		
		return currHM;
		
	}
	
	
}