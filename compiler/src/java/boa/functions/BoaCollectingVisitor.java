package boa.functions;

import java.util.HashMap;

import boa.runtime.BoaAbstractVisitor;

import boa.types.Ast.*;
import boa.types.Code.CodeRepository;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Toplevel.Project;

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

	public HashMap<K,V> getMap(final Project node) throws Exception {
		initialize().visit(node);
		return map;
	}
	public HashMap<K,V> getMap(final CodeRepository node) throws Exception {
		initialize().visit(node);
		return map;
	}
	public HashMap<K,V> getMap(final Revision node) throws Exception {
		initialize().visit(node);
		return map;
	}
	public HashMap<K,V> getMap(final ChangedFile node) throws Exception {
		initialize().visit(node);
		return map;
	}
	public HashMap<K,V> getMap(final ASTRoot node) throws Exception {
		initialize().visit(node);
		return map;
	}
	public HashMap<K,V> getMap(final Namespace node) throws Exception {
		initialize().visit(node);
		return map;
	}
	public HashMap<K,V> getMap(final Declaration node) throws Exception {
		initialize().visit(node);
		return map;
	}
	public HashMap<K,V> getMap(final Type node) throws Exception {
		initialize().visit(node);
		return map;
	}
	public HashMap<K,V> getMap(final Method node) throws Exception {
		initialize().visit(node);
		return map;
	}
	public HashMap<K,V> getMap(final Variable node) throws Exception {
		initialize().visit(node);
		return map;
	}
	public HashMap<K,V> getMap(final Statement node) throws Exception {
		initialize().visit(node);
		return map;
	}
	public HashMap<K,V> getMap(final Expression node) throws Exception {
		initialize().visit(node);
		return map;
	}
	public HashMap<K,V> getMap(final Modifier node) throws Exception {
		initialize().visit(node);
		return map;
	}
	public HashMap<K,V> getMap(final Comment node) throws Exception {
		initialize().visit(node);
		return map;
	}
}
