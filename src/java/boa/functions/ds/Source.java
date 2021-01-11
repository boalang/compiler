package boa.functions.ds;

import boa.functions.ds.type.*;

public class Source {
	public String name;
	public DSDatabase db;

	public Source(String name, ClassTrie trie) throws Exception {
		this.name = name;
		db = new DSDatabase(trie);
		DSGraphBuilder graphBuilder = new DSGraphBuilder(db);
		graphBuilder.build();
	}

	
}