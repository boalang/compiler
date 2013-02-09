package sizzle.functions;

import java.util.HashMap;

import sizzle.runtime.BoaAbstractVisitor;
import sizzle.types.Ast.ASTRoot;
import sizzle.types.Ast.Comment;
import sizzle.types.Ast.Declaration;
import sizzle.types.Ast.Expression;
import sizzle.types.Ast.Method;
import sizzle.types.Ast.Modifier;
import sizzle.types.Ast.Namespace;
import sizzle.types.Ast.Statement;
import sizzle.types.Ast.Type;
import sizzle.types.Ast.Variable;
import sizzle.types.Code.CodeRepository;
import sizzle.types.Code.Revision;
import sizzle.types.Diff.ChangedFile;
import sizzle.types.Toplevel.Project;

/**
 * Boa AST visitor that aggregates using a map.
 * 
 * @author rdyer
 */
public class BoaCollectingVisitor<K,V> extends BoaAbstractVisitor {
	public HashMap<K,V> map;

	public BoaCollectingVisitor<K,V> initialize(final HashMap<K,V> map) {
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
