package boa.functions;

import java.util.HashMap;

import boa.runtime.BoaAbstractVisitor;

/**
 * Boa AST visitor that aggregates using a map.
 * 
 * @author rdyer
 */
public class BoaCollectingVisitor<K,V> extends BoaAbstractVisitor {
	public HashMap<K,V> map;

	public BoaCollectingVisitor<K,V> initialize(final HashMap<K,V> map) {
		initialize();
		this.map = map;
		return this;
	}
}
