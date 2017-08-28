package boa.datagen.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import org.eclipse.php.internal.core.compiler.ast.parser.*;
import org.eclipse.dltk.ast.Modifiers;
import org.eclipse.php.internal.core.ast.nodes.*;
import org.eclipse.php.internal.core.ast.visitor.AbstractVisitor;

import boa.types.Ast.ASTRoot;
import boa.types.Ast.Declaration;
import boa.types.Ast.Expression;
import boa.types.Ast.Method;
import boa.types.Ast.Modifier;
import boa.types.Ast.Namespace;
import boa.types.Ast.PositionInfo;
import boa.types.Ast.Statement;
import boa.types.Ast.Statement.StatementKind;
import boa.types.Ast.Type;
import boa.types.Ast.TypeKind;
import boa.types.Ast.Variable;
import boa.types.Ast.Expression.ExpressionKind;
import boa.types.Ast.Modifier.ModifierKind;

public class PHPVisitor extends AbstractVisitor {

	private PositionInfo.Builder pos = null;
	private String src = null;
	private HashMap<String, Integer> nameIndices;
	protected Namespace.Builder b = Namespace.newBuilder();
	protected List<boa.types.Ast.Comment> comments = new ArrayList<boa.types.Ast.Comment>();
	protected List<String> imports = new ArrayList<String>();
	protected Stack<List<boa.types.Ast.Declaration>> declarations = new Stack<List<boa.types.Ast.Declaration>>();
	protected Stack<boa.types.Ast.Modifier> modifiers = new Stack<boa.types.Ast.Modifier>();
	protected Stack<boa.types.Ast.Expression> expressions = new Stack<boa.types.Ast.Expression>();
	protected Stack<List<boa.types.Ast.Variable>> fields = new Stack<List<boa.types.Ast.Variable>>();
	protected Stack<List<boa.types.Ast.Method>> methods = new Stack<List<boa.types.Ast.Method>>();
	protected Stack<List<boa.types.Ast.Statement>> statements = new Stack<List<boa.types.Ast.Statement>>();
	protected Stack<List<boa.types.Ast.Namespace>> namespaces = new Stack<List<boa.types.Ast.Namespace>>();
	private Program root;
	protected boa.types.Ast.ASTRoot.Builder r = boa.types.Ast.ASTRoot.newBuilder();

	public PHPVisitor(String src) {
		super();
		this.src = src;
	}

	public Namespace getNamespace(Program node) {
		root = node;
		if (node == null)
			System.out.println("");
		node.accept(this);
		return b.build();
	}

	public List<boa.types.Ast.Comment> getComments() {
		return comments;
	}

	public List<String> getImports() {
		return imports;
	}

	private void buildPosition(final ASTNode node) {
		pos = PositionInfo.newBuilder();
		int start = node.getStart();
		int length = node.getLength();
		pos.setStartPos(start);
		pos.setLength(length);
		pos.setStartLine(root.getLineNumber(start));
		pos.setStartCol(root.getColumnNumber(start));
		pos.setEndLine(root.getLineNumber(start + length));
		pos.setEndCol(root.getColumnNumber(start + length));
	}

	@Override
	public boolean visit(Comment node) {
		boa.types.Ast.Comment.Builder b = boa.types.Ast.Comment.newBuilder();
		buildPosition(node);
		b.setPosition(pos.build());
		if (node.getCommentType() == Comment.TYPE_MULTILINE) {
			b.setKind(boa.types.Ast.Comment.CommentKind.BLOCK);
		} else if (node.getCommentType() == Comment.TYPE_SINGLE_LINE) {
			b.setKind(boa.types.Ast.Comment.CommentKind.LINE);
		} else if (node.getCommentType() == Comment.TYPE_PHPDOC) {
			b.setKind(boa.types.Ast.Comment.CommentKind.DOC);
		}
		b.setValue(src.substring(node.getStart(), node.getEnd()));

		comments.add(b.build());
		return false;
	}

	@Override
	public boolean visit(Program node) {
		for (ASTNode s : node.statements()) {
			if (s instanceof NamespaceDeclaration) {
				namespaces.push(new ArrayList<boa.types.Ast.Namespace>());
				s.accept(this);
				for (boa.types.Ast.Namespace d : namespaces.pop())
					b.addNamespaces(d);
			} else if (s instanceof MethodDeclaration || s instanceof FunctionDeclaration) {
				methods.push(new ArrayList<boa.types.Ast.Method>());
				s.accept(this);
				for (boa.types.Ast.Method m : methods.pop())
					b.addMethods(m);
			} else if (s instanceof ConstantDeclaration) {
				statements.push(new ArrayList<boa.types.Ast.Statement>());
				wrapFieldsInAStatetment(s);
				for (boa.types.Ast.Statement st : statements.pop())
					b.addStatements(st);
			} else if (s instanceof TypeDeclaration) {
				declarations.push(new ArrayList<boa.types.Ast.Declaration>());
				s.accept(this);
				for (boa.types.Ast.Declaration d : declarations.pop())
					b.addDeclarations(d);
			} else if (s instanceof org.eclipse.php.internal.core.ast.nodes.Expression) {
				s.accept(this);
				b.addExpressions(expressions.pop());
			} else if (s instanceof Include) {
				Include id = ((Include) s);
				String imp = "";
				if (id.isStaticScalar())
					imp += "static ";
				if (id.getExpression() instanceof Identifier) {
					imp += ((Identifier) id.getExpression()).getName();
				} else {
					id.getExpression().accept(this);// FIXME
					b.addExpressions(expressions.pop());
				}
				imports.add(imp);
			} else {
				statements.push(new ArrayList<boa.types.Ast.Statement>());
				s.accept(this);
				for (boa.types.Ast.Statement st : statements.pop())
					b.addStatements(st);
			}
		}
		b.setName("");
		for (Object c : node.comments())
			((Comment) c).accept(this);
		if (!expressions.isEmpty())
			throw new RuntimeException("expressions not empty");
		if (!statements.isEmpty())
			throw new RuntimeException("statements not empty");
		if (!methods.isEmpty())
			throw new RuntimeException("methods not empty");
		if (!namespaces.isEmpty())
			throw new RuntimeException("namespaces not empty");
		if (!declarations.isEmpty())
			throw new RuntimeException("declarations not empty");
		return false;
	}

	@Override
	public boolean visit(NamespaceName node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(ExpressionKind.OTHER);// FIXME
		b.setVariable(node.getName());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(NamespaceDeclaration node) {
		Namespace.Builder nb = Namespace.newBuilder();
		List<boa.types.Ast.Namespace> list = namespaces.peek();
		nb.setName(node.getName().getName());
		for (Object s : node.getBody().statements()) {
			if (s instanceof NamespaceDeclaration) {
				namespaces.push(new ArrayList<boa.types.Ast.Namespace>());
				((NamespaceDeclaration) s).accept(this);
				for (boa.types.Ast.Namespace d : namespaces.pop())
					nb.addNamespaces(d);
			} else if (s instanceof MethodDeclaration || s instanceof FunctionDeclaration) {
				methods.push(new ArrayList<boa.types.Ast.Method>());
				((ASTNode) s).accept(this);
				for (boa.types.Ast.Method m : methods.pop())
					nb.addMethods(m);
			} else if (s instanceof ConstantDeclaration) {
				statements.push(new ArrayList<boa.types.Ast.Statement>());
				wrapFieldsInAStatetment((ASTNode)s);
				for (boa.types.Ast.Statement st : statements.pop())
					nb.addStatements(st);
			} else if (s instanceof TypeDeclaration) {
				declarations.push(new ArrayList<boa.types.Ast.Declaration>());
				((TypeDeclaration) s).accept(this);
				for (boa.types.Ast.Declaration d : declarations.pop())
					nb.addDeclarations(d);
			} else if (s instanceof org.eclipse.php.internal.core.ast.nodes.Expression) {
				((ASTNode) s).accept(this);
				nb.addExpressions(expressions.pop());
			} else if (s instanceof Include) {
				Include id = ((Include) s);
				String imp = "";
				if (id.isStaticScalar())
					imp += "static ";
				if (id.getExpression() instanceof Identifier) {
					imp += ((Identifier) id.getExpression()).getName();
				} else {
					id.getExpression().accept(this);// FIXME
					nb.addExpressions(expressions.pop());
				}
				nb.addImports(imp);
			} else {
				statements.push(new ArrayList<boa.types.Ast.Statement>());
				((ASTNode) s).accept(this);
				for (boa.types.Ast.Statement st : statements.pop())
					nb.addStatements(st);
			}
		}
		list.add(nb.build());
		return false;
	}

	@Override
	public boolean visit(ArrayAccess node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.ARRAYINDEX);
		node.getName().accept(this);
		b.addExpressions(expressions.pop());
		if (node.getIndex() != null) {
			node.getIndex().accept(this);
			b.addExpressions(expressions.pop());
		}
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(ArrayCreation node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.NEWARRAY);
		for (Object e : node.elements()) {
			((ASTNode) e).accept(this);
			b.addExpressions(expressions.pop());
		}
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(ArrayElement node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);// FIXME
		if (node.getKey() != null) {
			node.getKey().accept(this);
			b.addExpressions(expressions.pop());
		}
		node.getValue().accept(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(Assignment node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		node.getLeftHandSide().accept(this);
		b.addExpressions(expressions.pop());
		node.getRightHandSide().accept(this);
		b.addExpressions(expressions.pop());
		if (node.getOperator() == Assignment.OP_EQUAL)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN);
		else if (node.getOperator() == Assignment.OP_AND_EQUAL)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_BITAND);
		else if (node.getOperator() == Assignment.OP_OR_EQUAL)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_BITOR);
		else if (node.getOperator() == Assignment.OP_XOR_EQUAL)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_BITXOR);
		else if (node.getOperator() == Assignment.OP_DIV_EQUAL)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_DIV);
		else if (node.getOperator() == Assignment.OP_SL_EQUAL)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_LSHIFT);
		else if (node.getOperator() == Assignment.OP_MINUS_EQUAL)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_SUB);
		else if (node.getOperator() == Assignment.OP_PLUS_EQUAL)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_ADD);
		else if (node.getOperator() == Assignment.OP_MOD_EQUAL)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_MOD);
		else if (node.getOperator() == Assignment.OP_SR_EQUAL)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_RSHIFT);
		else if (node.getOperator() == Assignment.OP_CONCAT_EQUAL)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_ADD);// FIXME
		else if (node.getOperator() == Assignment.OP_MUL_EQUAL)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_MULT);
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(BackTickExpression node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.METHODCALL);// FIXME
		for (org.eclipse.php.internal.core.ast.nodes.Expression e : node.expressions()) {
			e.accept(this);
			b.addExpressions(expressions.pop());
		}
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(Block node) {
		Statement.Builder b = Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.BLOCK);
		for (Object s : node.statements()) {
			if (s instanceof MethodDeclaration) {
				methods.push(new ArrayList<boa.types.Ast.Method>());
				((MethodDeclaration) s).accept(this);
				for (boa.types.Ast.Method m : methods.pop())
					b.addMethods(m);
			} else if (s instanceof FunctionDeclaration) {
				methods.push(new ArrayList<boa.types.Ast.Method>());
				((FunctionDeclaration) s).accept(this);
				for (boa.types.Ast.Method m : methods.pop())
					b.addMethods(m);
			} else if (s instanceof TypeDeclaration) {
				declarations.push(new ArrayList<boa.types.Ast.Declaration>());
				((TypeDeclaration) s).accept(this);
				for (Declaration decl : declarations.pop())
					b.setTypeDeclaration(decl); // FIXME add repeated
												// declarations?
			} else if (s instanceof org.eclipse.php.internal.core.ast.nodes.Expression) {
				((ASTNode) s).accept(this);
				b.addExpressions(expressions.pop());
			} else {
				statements.push(new ArrayList<boa.types.Ast.Statement>());
				((ASTNode) s).accept(this);
				for (boa.types.Ast.Statement st : statements.pop())
					b.addStatements(st);
			}
		}
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(BreakStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.BREAK);
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
			b.setExpression(expressions.pop());
		}
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(CastExpression node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.CAST);
		boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
		tb.setName(CastExpression.getCastType(node.getCastingType()));
		tb.setKind(boa.types.Ast.TypeKind.OTHER);
		b.setNewType(tb.build());
		node.getExpression().accept(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(CatchClause node) {
		Statement.Builder b = Statement.newBuilder();
		List<Statement> list = statements.peek();
		b.setKind(Statement.StatementKind.CATCH);
		org.eclipse.php.internal.core.ast.nodes.Variable ex = node.getVariable();
		boa.types.Ast.Variable.Builder vb = boa.types.Ast.Variable.newBuilder();
		String name = "";
		if (ex.isDollared()) {
			name = "$";
		}
		if (ex.getName() instanceof Identifier) {
			name += ((Identifier) ex.getName()).getName();
		} // FIXME
		vb.setName(name);
		b.setVariableDeclaration(vb.build());
		methods.push(new ArrayList<Method>());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		for (Object s : node.getBody().statements())
			((ASTNode) s).accept(this);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		for (Method m : methods.pop())
			b.addMethods(m);
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(ConstantDeclaration node) {
		List<Variable> list = fields.peek();
		List<Identifier> variableNames = node.names();
		List<org.eclipse.php.internal.core.ast.nodes.Expression> constantValues = node.initializers();
		int mod = node.getModifier();
		for (int i = 0; i < node.names().size(); i++) {
			boa.types.Ast.Variable.Builder vb = boa.types.Ast.Variable.newBuilder();
			if ((mod & Modifiers.AccPublic) != 0) {
				boa.types.Ast.Modifier.Builder mb = boa.types.Ast.Modifier.newBuilder();
				mb.setKind(boa.types.Ast.Modifier.ModifierKind.VISIBILITY);
				mb.setVisibility(boa.types.Ast.Modifier.Visibility.PUBLIC);
				b.addModifiers(mb.build());
			}
			if ((mod & Modifiers.AccPrivate) != 0) {
				boa.types.Ast.Modifier.Builder mb = boa.types.Ast.Modifier.newBuilder();
				mb.setKind(boa.types.Ast.Modifier.ModifierKind.VISIBILITY);
				mb.setVisibility(boa.types.Ast.Modifier.Visibility.PRIVATE);
				b.addModifiers(mb.build());
			}
			if ((mod & Modifiers.AccProtected) != 0) {
				boa.types.Ast.Modifier.Builder mb = boa.types.Ast.Modifier.newBuilder();
				mb.setKind(boa.types.Ast.Modifier.ModifierKind.VISIBILITY);
				mb.setVisibility(boa.types.Ast.Modifier.Visibility.PROTECTED);
				b.addModifiers(mb.build());
			}
			if ((mod & Modifiers.AccStatic) != 0) {
				boa.types.Ast.Modifier.Builder mb = boa.types.Ast.Modifier.newBuilder();
				mb.setKind(boa.types.Ast.Modifier.ModifierKind.STATIC);
				b.addModifiers(mb.build());
			}
			Modifier.Builder mb = Modifier.newBuilder(); // FIXME const is
															// similiar to
															// final?
			mb.setKind(Modifier.ModifierKind.FINAL);
			vb.addModifiers(mb.build());
			vb.setName(variableNames.get(i).getName());
			constantValues.get(i).accept(this);
			vb.setInitializer(expressions.pop());
			boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
			tb.setName("");
			tb.setKind(boa.types.Ast.TypeKind.OTHER);
			vb.setVariableType(tb.build());
			list.add(vb.build());
		}
		return false;
	}

	@Override
	public boolean visit(ClassDeclaration node) {
		boa.types.Ast.Declaration.Builder b = boa.types.Ast.Declaration.newBuilder();
		b.setName("");
		int mod = node.getModifier();
		if (mod != ClassDeclaration.MODIFIER_NONE) {
			Modifier.Builder mb = Modifier.newBuilder();
			switch (mod) {
			case ClassDeclaration.MODIFIER_ABSTRACT:
				mb.setKind(Modifier.ModifierKind.ABSTRACT);
				break;
			case ClassDeclaration.MODIFIER_FINAL:
				mb.setKind(Modifier.ModifierKind.FINAL);
				break;
			case ClassDeclaration.MODIFIER_TRAIT:
				mb.setKind(Modifier.ModifierKind.OTHER);
				mb.setOther("trait");
				break;
			}
			b.addModifiers(mb.build());
		}
		b.setKind(boa.types.Ast.TypeKind.CLASS);
		// FIXME getSuperClass can return expressions.
		if (node.getSuperClass() != null) {
			if (node.getSuperClass() instanceof Identifier) {
				Type.Builder tb = Type.newBuilder();
				tb.setKind(TypeKind.OTHER);// FIXME
				tb.setName(((Identifier) node.getSuperClass()).getName());
				b.addParents(tb.build());
			} else {
				node.getSuperClass().accept(this);
				Variable.Builder vb = Variable.newBuilder();// FIXME
				Type.Builder tb = Type.newBuilder();
				tb.setKind(TypeKind.OTHER);
				tb.setName("Super_Class");
				vb.setVariableType(tb.build());
				vb.setComputedName(expressions.pop());
				b.addFields(vb.build());
			}
		}
		fields.push(new ArrayList<boa.types.Ast.Variable>());
		methods.push(new ArrayList<boa.types.Ast.Method>());
		for (ASTNode d : node.getBody().statements())
			d.accept(this);
		for (boa.types.Ast.Variable v : fields.pop())
			b.addFields(v);
		for (boa.types.Ast.Method m : methods.pop())
			b.addMethods(m);
		declarations.peek().add(b.build());
		return false;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.NEW);
		boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
		tb.setKind(boa.types.Ast.TypeKind.CLASS);
		String className = "";
		if (node.getClassName().getName() instanceof Identifier)
			className = ((Identifier) node.getClassName().getName()).getName();
		else {
			node.getClassName().getName().accept(this);// FIXME Add
														// computed_name to
														// Type?
			b.addExpressions(expressions.pop());
		}
		tb.setName(className);
		b.setNewType(tb.build());
		for (Object a : node.ctorParams()) {
			((ASTNode) a).accept(this);
			b.addMethodArgs(expressions.pop());
		}
		if (node.getAnonymousClassDeclaration() != null) {
			declarations.push(new ArrayList<boa.types.Ast.Declaration>());
			node.getAnonymousClassDeclaration().accept(this);
			for (boa.types.Ast.Declaration d : declarations.pop())
				b.setAnonDeclaration(d);
		}
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(ClassName node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(Expression.ExpressionKind.OTHER);
		node.getName().accept(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(CloneExpression node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(Expression.ExpressionKind.METHODCALL);
		b.setMethod("CLONE");
		node.getExpression().accept(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(ConditionalExpression node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.CONDITIONAL);
		node.getCondition().accept(this);
		b.addExpressions(expressions.pop());// FIXME
		if (node.getIfTrue() != null) {
			node.getIfTrue().accept(this);
			b.addExpressions(expressions.pop());// FIXME
		}
		if (node.getIfFalse() != null) {
			node.getIfFalse().accept(this);
			b.addExpressions(expressions.pop());// FIXME
		}
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(ContinueStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.CONTINUE);
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
			b.setExpression(expressions.pop());
		}
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(DeclareStatement node) {
		Statement.Builder sb = Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		sb.setKind(Statement.StatementKind.OTHER);// FIXME
		Expression.Builder b = Expression.newBuilder();
		b.setKind(Expression.ExpressionKind.VARDECL);
		List<Identifier> variableNames = node.directiveNames();
		List<org.eclipse.php.internal.core.ast.nodes.Expression> directiveValues = node.directiveValues();
		for (int i = 0; i < node.directiveNames().size(); i++) {
			boa.types.Ast.Variable.Builder vb = boa.types.Ast.Variable.newBuilder();
			vb.setName(variableNames.get(i).getName());
			directiveValues.get(i).accept(this);
			vb.setInitializer(expressions.pop());
			boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
			tb.setName("");
			tb.setKind(boa.types.Ast.TypeKind.OTHER);
			vb.setVariableType(tb.build());
			b.addVariableDecls(vb.build());
		}
		sb.setExpression(b.build());
		statements.push(new ArrayList<Statement>());
		node.getBody().accept(this);
		for (boa.types.Ast.Statement s : statements.pop())
			sb.addStatements(s);
		list.add(sb.build());
		return false;
	}

	@Override
	public boolean visit(DoStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.DO);
		if (node.getCondition() != null) {
			node.getCondition().accept(this);
			b.setExpression(expressions.pop());
		}

		if (node.getBody() != null) {
			methods.push(new ArrayList<Method>());
			statements.push(new ArrayList<boa.types.Ast.Statement>());
			node.getBody().accept(this);
			for (boa.types.Ast.Statement s : statements.pop())
				b.addStatements(s);
			for (Method m : methods.pop())
				b.addMethods(m);
		}
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(EchoStatement node) {
		Statement.Builder b = Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(Statement.StatementKind.OTHER);// FIXME print?
		for (org.eclipse.php.internal.core.ast.nodes.Expression e : node.expressions()) {
			e.accept(this);
			b.addInitializations(expressions.pop());// FIXME
		}
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(EmptyStatement node) {
		Statement.Builder b = Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(Statement.StatementKind.EMPTY);
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(ExpressionStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.EXPRESSION);
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
			b.setExpression(expressions.pop());
		}
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(FieldAccess node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.VARACCESS);
		node.getDispatcher().accept(this);
		b.addExpressions(expressions.pop());
		if (node.getField().getName() instanceof Identifier)
			b.setVariable(((Identifier) node.getField().getName()).getName());
		else {
			node.getField().accept(this);
			b.addExpressions(expressions.pop());
		}
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(FieldsDeclaration node) {
		List<Variable> list = fields.peek();
		org.eclipse.php.internal.core.ast.nodes.Variable[] variableNames = node.getVariableNames();
		org.eclipse.php.internal.core.ast.nodes.Expression[] initialValues = node.getInitialValues();
		int mod = node.getModifier();
		node.getModifierString();
		for (int i = 0; i < node.getVariableNames().length; i++) {
			Variable.Builder b = Variable.newBuilder();
			if ((mod & Modifiers.AccPublic) != 0) {
				boa.types.Ast.Modifier.Builder mb = boa.types.Ast.Modifier.newBuilder();
				mb.setKind(boa.types.Ast.Modifier.ModifierKind.VISIBILITY);
				mb.setVisibility(boa.types.Ast.Modifier.Visibility.PUBLIC);
				b.addModifiers(mb.build());
			}
			if ((mod & Modifiers.AccPrivate) != 0) {
				boa.types.Ast.Modifier.Builder mb = boa.types.Ast.Modifier.newBuilder();
				mb.setKind(boa.types.Ast.Modifier.ModifierKind.VISIBILITY);
				mb.setVisibility(boa.types.Ast.Modifier.Visibility.PRIVATE);
				b.addModifiers(mb.build());
			}
			if ((mod & Modifiers.AccProtected) != 0) {
				boa.types.Ast.Modifier.Builder mb = boa.types.Ast.Modifier.newBuilder();
				mb.setKind(boa.types.Ast.Modifier.ModifierKind.VISIBILITY);
				mb.setVisibility(boa.types.Ast.Modifier.Visibility.PROTECTED);
				b.addModifiers(mb.build());
			}
			if ((mod & Modifiers.AccStatic) != 0) {
				boa.types.Ast.Modifier.Builder mb = boa.types.Ast.Modifier.newBuilder();
				mb.setKind(boa.types.Ast.Modifier.ModifierKind.STATIC);
				b.addModifiers(mb.build());
			}
			if ((mod & Modifiers.AccFinal) != 0) {
				boa.types.Ast.Modifier.Builder mb = boa.types.Ast.Modifier.newBuilder();
				mb.setKind(boa.types.Ast.Modifier.ModifierKind.FINAL);
				b.addModifiers(mb.build());
			}
			if (variableNames[i].getName() instanceof Identifier) {
				b.setName(((Identifier) variableNames[i].getName()).getName());
			} else {
				b.setName("");
				variableNames[i].accept(this);
				b.setComputedName(expressions.pop());
			}
			if (initialValues[i] != null) {
				initialValues[i].accept(this);
				b.setInitializer(expressions.pop());
			}
			list.add(b.build());
		}
		return false;
	}

	@Override
	public boolean visit(ForEachStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.FOREACH);
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
			b.addInitializations(expressions.pop());
		}
		if (node.getKey() != null) {
			node.getKey().accept(this);
			b.addInitializations(expressions.pop());
		}
		if (node.getValue() != null) {
			node.getValue().accept(this);
			b.addUpdates(expressions.pop());
		}
		methods.push(new ArrayList<Method>());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getStatement().accept(this);
		for (Statement s : statements.pop())
			b.addStatements(s);
		for (Method m : methods.pop())
			b.addMethods(m);
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(FormalParameter node) {
		Variable.Builder b = Variable.newBuilder();
		List<Variable> list = fields.peek();
		Type.Builder tb = Type.newBuilder();
		String name = "";
		if (node.getParameterType() != null)
			if (node.getParameterType() instanceof Identifier)
				name = (((Identifier) node.getParameterType()).getName());
			else {
				node.getParameterType().accept(this);
				b.setComputedName(expressions.pop());// FIXME
			}
		tb.setName(name);
		tb.setKind(boa.types.Ast.TypeKind.OTHER);
		b.setVariableType(tb.build());
		b.setName(node.getParameterNameIdentifier().getName());
		if (node.getDefaultValue() != null) {
			node.getDefaultValue().accept(this);
			b.setInitializer(expressions.pop());
		}
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(ForStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.FOR);
		for (org.eclipse.php.internal.core.ast.nodes.Expression e : node.initializers()) {
			e.accept(this);
			b.addInitializations(expressions.pop());
		}
		for (org.eclipse.php.internal.core.ast.nodes.Expression e : node.conditions()) {
			e.accept(this);
			b.addUpdates(expressions.pop()); // FIXME condition is optional not
												// repeated?
		}
		for (org.eclipse.php.internal.core.ast.nodes.Expression e : node.updaters()) {
			e.accept(this);
			b.addUpdates(expressions.pop());
		}
		methods.push(new ArrayList<Method>());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getBody().accept(this);
		for (Statement s : statements.pop())
			b.addStatements(s);
		for (Method m : methods.pop())
			b.addMethods(m);
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(FunctionDeclaration node) {
		Method.Builder b = Method.newBuilder();
		b.setName(node.getFunctionName().getName());
		for (FormalParameter p : node.formalParameters()) {
			fields.push(new ArrayList<Variable>());
			p.accept(this);
			for (Variable v : fields.pop())
				b.addArguments(v);
		}
		if (node.getBody() != null) {
			statements.push(new ArrayList<boa.types.Ast.Statement>());
			node.getBody().accept(this);
			for (boa.types.Ast.Statement s : statements.pop())
				b.addStatements(s);
		}
		Type.Builder tb = Type.newBuilder();
		String name = "";
		if (node.getReturnType() != null)
			name = node.getReturnType().getName();
		tb.setKind(boa.types.Ast.TypeKind.OTHER);
		tb.setName(name);
		b.setReturnType(tb.build());
		methods.peek().add(b.build());
		return false;
	}

	@Override
	public boolean visit(FunctionInvocation node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.METHODCALL);
		FunctionName fn = node.getFunctionName();
		if (fn.getName() instanceof Identifier)
			b.setMethod(((Identifier) node.getFunctionName().getName()).getName());// FIXME
		else {
			fn.getName().accept(this);
			b.addExpressions(expressions.pop());
		}
		for (org.eclipse.php.internal.core.ast.nodes.Expression a : node.parameters()) {
			a.accept(this);
			b.addMethodArgs(expressions.pop());
		}
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(FunctionName node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(Expression.ExpressionKind.OTHER);// FIXME
		node.getName().accept(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(GlobalStatement node) {
		Statement.Builder b = Statement.newBuilder();
		List<Statement> list = statements.peek();
		b.setKind(StatementKind.EXPRESSION);// FIXME
		Expression.Builder eb = Expression.newBuilder();
		eb.setKind(ExpressionKind.VARDECL);
		for (org.eclipse.php.internal.core.ast.nodes.Variable v : node.variables()) {
			Variable.Builder vb = Variable.newBuilder();
			Type.Builder tb = Type.newBuilder();
			tb.setKind(TypeKind.OTHER);
			tb.setName("");
			vb.setVariableType(tb);
			if (v.getName() instanceof Identifier) {
				vb.setName(((Identifier) v.getName()).getName());
			} else {
				vb.setName("");
				v.accept(this);
				vb.setComputedName(expressions.pop());
			}
			eb.addVariableDecls(vb.build());
		}
		b.setExpression(eb.build());
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(GotoLabel node) {
		Statement.Builder b = Statement.newBuilder();
		b.setKind(StatementKind.LABEL);
		Expression.Builder eb = Expression.newBuilder();
		eb.setKind(ExpressionKind.LITERAL);
		eb.setLiteral(node.getName().getName());
		b.setExpression(eb.build());
		statements.peek().add(b.build());
		return false;
	}

	@Override
	public boolean visit(GotoStatement node) {
		Statement.Builder b = Statement.newBuilder();
		List<Statement> list = statements.peek();
		b.setKind(StatementKind.OTHER);// FIXME add goto
		Expression.Builder eb = Expression.newBuilder();
		eb.setKind(Expression.ExpressionKind.LITERAL);
		eb.setLiteral(node.getLabel().getName());// FIXME
		b.setExpression(eb.build());
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(Identifier node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(ExpressionKind.OTHER);
		b.setVariable(node.getName());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(IfStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		node.getCondition().accept(this);
		b.setCondition(expressions.pop());
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.IF);
		methods.push(new ArrayList<Method>());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getTrueStatement().accept(this);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		for (Method m : methods.pop())
			b.addMethods(m);
		if (node.getFalseStatement() != null) {
			methods.push(new ArrayList<Method>());
			statements.push(new ArrayList<boa.types.Ast.Statement>());
			node.getFalseStatement().accept(this);
			for (boa.types.Ast.Statement s : statements.pop())
				b.addStatements(s);
			for (Method m : methods.pop())
				b.addMethods(m);
		}
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(IgnoreError node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(Expression.ExpressionKind.OTHER);
		node.getExpression().accept(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(Include node) {
		Expression.Builder eb = Expression.newBuilder();
		eb.setKind(ExpressionKind.OTHER);// FIXME
		Type.Builder tb = Type.newBuilder();
		tb.setKind(TypeKind.OTHER);// FIXME
		tb.setName(Include.getType(node.getIncludeType()));
		eb.setNewType(tb.build());// FIXME
		node.getExpression().accept(this);
		eb.addExpressions(expressions.pop());
		expressions.push(eb.build());
		return false;
	}

	@Override
	public boolean visit(InfixExpression node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		if (node.getOperator() == InfixExpression.OP_AND)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.BIT_AND);
		else if (node.getOperator() == InfixExpression.OP_BOOL_AND)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.LOGICAL_AND);
		else if (node.getOperator() == InfixExpression.OP_BOOL_OR)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.LOGICAL_OR);
		else if (node.getOperator() == InfixExpression.OP_DIV)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_DIV);
		else if (node.getOperator() == InfixExpression.OP_IS_EQUAL)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.EQ);
		else if (node.getOperator() == InfixExpression.OP_IS_IDENTICAL)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.EQ);
		else if (node.getOperator() == InfixExpression.OP_LGREATER)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.GT);
		else if (node.getOperator() == InfixExpression.OP_IS_GREATER_OR_EQUAL)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.GTEQ);
		else if (node.getOperator() == InfixExpression.OP_SL)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.BIT_LSHIFT);
		else if (node.getOperator() == InfixExpression.OP_RGREATER)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.LT);
		else if (node.getOperator() == InfixExpression.OP_IS_SMALLER_OR_EQUAL)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.LTEQ);
		else if (node.getOperator() == InfixExpression.OP_MINUS)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_SUB);
		else if (node.getOperator() == InfixExpression.OP_IS_NOT_EQUAL)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.NEQ);
		else if (node.getOperator() == InfixExpression.OP_IS_NOT_IDENTICAL)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.NEQ);
		else if (node.getOperator() == InfixExpression.OP_OR)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.BIT_OR);
		else if (node.getOperator() == InfixExpression.OP_PLUS)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_ADD);
		else if (node.getOperator() == InfixExpression.OP_MOD)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_MOD);
		else if (node.getOperator() == InfixExpression.OP_SR)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.BIT_RSHIFT);
		else if (node.getOperator() == InfixExpression.OP_STRING_XOR)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.BIT_XOR);// FIXME
		else if (node.getOperator() == InfixExpression.OP_STRING_OR)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.LOGICAL_OR);
		else if (node.getOperator() == InfixExpression.OP_STRING_AND)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.LOGICAL_AND);
		else if (node.getOperator() == InfixExpression.OP_MUL)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_MULT);
		else if (node.getOperator() == InfixExpression.OP_XOR)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.BIT_XOR);
		else
			b.setKind(ExpressionKind.OTHER);// FIXME
		node.getLeft().accept(this);
		b.addExpressions(expressions.pop());
		node.getRight().accept(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(InLineHtml node) {
		// FIXME
		return false;
	}

	@Override
	public boolean visit(InstanceOfExpression node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.TYPECOMPARE);
		node.getExpression().accept(this);
		b.addExpressions(expressions.pop());
		if (node.getClassName().getName() instanceof Identifier) {
			b.setVariable(((Identifier) node.getClassName().getName()).getName());
		} else {
			node.getClassName().accept(this);
			b.addExpressions(expressions.pop());
		}
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(InterfaceDeclaration node) {
		boa.types.Ast.Declaration.Builder b = boa.types.Ast.Declaration.newBuilder();
		b.setName(node.getName().getName());
		b.setKind(boa.types.Ast.TypeKind.INTERFACE);
		for (Identifier t : node.interfaces()) {
			boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
			tb.setName(t.getName());
			tb.setKind(boa.types.Ast.TypeKind.INTERFACE);
			b.addParents(tb.build());
		}
		for (Object d : node.getBody().statements()) {
			if (d instanceof MethodDeclaration) {
				methods.push(new ArrayList<boa.types.Ast.Method>());
				((MethodDeclaration) d).accept(this);
				for (boa.types.Ast.Method m : methods.pop())
					b.addMethods(m);
			} else if (d instanceof FieldsDeclaration || d instanceof ConstantDeclaration) {
				fields.push(new ArrayList<boa.types.Ast.Variable>());
				((BodyDeclaration) d).accept(this);
				for (boa.types.Ast.Variable v : fields.pop())
					b.addFields(v);
			} else if (d instanceof org.eclipse.php.internal.core.ast.nodes.Expression) {
				((ASTNode) d).accept(this);
				// FIXME b.addExpressions(expressions.pop());
			} else {
				statements.push(new ArrayList<boa.types.Ast.Statement>());
				((ASTNode) d).accept(this);
				// for (boa.types.Ast.Statement st : statements.pop())
				// FIXME b.addStatements(st);
			}
		}
		declarations.peek().add(b.build());
		return false;
	}

	@Override
	public boolean visit(LambdaFunctionDeclaration node) {
		Expression.Builder eb = Expression.newBuilder();
		eb.setKind(ExpressionKind.LAMBDA);
		fields.push(new ArrayList<Variable>());
		for (FormalParameter p : node.formalParameters())
			p.accept(this);
		for (Variable v : fields.pop())
			eb.addVariableDecls(v);
		for (org.eclipse.php.internal.core.ast.nodes.Expression p : node.lexicalVariables()) {
			p.accept(this);
			eb.addMethodArgs(expressions.pop());
		}
		if (node.getBody() != null) {
			statements.push(new ArrayList<boa.types.Ast.Statement>());
			node.getBody().accept(this);
			for (boa.types.Ast.Statement s : statements.pop())
				eb.addStatements(s);
		}
		expressions.push(eb.build());
		return false;
	}

	@Override
	public boolean visit(ListVariable node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(Expression.ExpressionKind.VARDECL);
		for (org.eclipse.php.internal.core.ast.nodes.Expression v : node.variables()) {
			Variable.Builder vb = Variable.newBuilder();
			if (v instanceof org.eclipse.php.internal.core.ast.nodes.Variable
					&& ((org.eclipse.php.internal.core.ast.nodes.Variable) v).getName() instanceof Identifier) {
				vb.setName(((Identifier) ((org.eclipse.php.internal.core.ast.nodes.Variable) v).getName()).getName());
			} else {
				v.accept(this);
				vb.setComputedName(expressions.pop());
			}
			b.addVariableDecls(vb.build());
		}
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		List<boa.types.Ast.Method> list = methods.peek();
		Method.Builder b = Method.newBuilder();
		int mod = node.getModifier();
		if ((mod & Modifiers.AccPrivate) != 0) {
			Modifier.Builder mb = Modifier.newBuilder();
			mb.setKind(Modifier.ModifierKind.VISIBILITY);
			mb.setVisibility(Modifier.Visibility.PRIVATE);
			b.addModifiers(mb.build());
		}
		if ((mod & Modifiers.AccPublic) != 0) {
			Modifier.Builder mb = Modifier.newBuilder();
			mb.setKind(Modifier.ModifierKind.VISIBILITY);
			mb.setVisibility(Modifier.Visibility.PUBLIC);
			b.addModifiers(mb.build());
		}
		if ((mod & Modifiers.AccProtected) != 0) {
			Modifier.Builder mb = Modifier.newBuilder();
			mb.setKind(Modifier.ModifierKind.VISIBILITY);
			mb.setVisibility(Modifier.Visibility.PROTECTED);
			b.addModifiers(mb.build());
		}
		if ((mod & Modifiers.AccAbstract) != 0) {
			Modifier.Builder mb = Modifier.newBuilder();
			mb.setKind(Modifier.ModifierKind.ABSTRACT);
			b.addModifiers(mb.build());
		}
		if ((mod & Modifiers.AccStatic) != 0) {
			Modifier.Builder mb = Modifier.newBuilder();
			mb.setKind(Modifier.ModifierKind.STATIC);
			b.addModifiers(mb.build());
		}
		if ((mod & Modifiers.AccFinal) != 0) {
			Modifier.Builder mb = Modifier.newBuilder();
			mb.setKind(Modifier.ModifierKind.FINAL);
			b.addModifiers(mb.build());
		}
		b.setName(node.getFunction().getFunctionName().getName());
		for (FormalParameter p : node.getFunction().formalParameters()) {
			fields.push(new ArrayList<Variable>());
			p.accept(this);
			for (Variable v : fields.pop())
				b.addArguments(v);
		}
		if (node.getFunction().getBody() != null) {
			statements.push(new ArrayList<boa.types.Ast.Statement>());
			node.getFunction().getBody().accept(this);
			for (boa.types.Ast.Statement s : statements.pop())
				b.addStatements(s);
		}
		Type.Builder tb = Type.newBuilder();
		String name = "";
		if (node.getFunction().getReturnType() != null)
			name = node.getFunction().getReturnType().getName();
		tb.setKind(boa.types.Ast.TypeKind.OTHER);
		tb.setName(name);
		b.setReturnType(tb.build());
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.METHODCALL);
		FunctionName fn = node.getMethod().getFunctionName();
		node.getDispatcher().accept(this);
		b.addExpressions(expressions.pop());
		if (fn.getName() instanceof Identifier)
			b.setMethod(((Identifier) node.getMethod().getFunctionName().getName()).getName());// FIXME
		else {
			fn.getName().accept(this);
			b.addExpressions(expressions.pop());
		}
		for (org.eclipse.php.internal.core.ast.nodes.Expression a : node.getMethod().parameters()) {
			a.accept(this);
			b.addMethodArgs(expressions.pop());
		}
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(ParenthesisExpression node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.PAREN);
		node.getExpression().accept(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(PostfixExpression node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		if (node.getOperator() == PostfixExpression.OP_DEC)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_DEC);
		else if (node.getOperator() == PostfixExpression.OP_INC)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_INC);
		node.getVariable().accept(this);
		b.addExpressions(expressions.pop());
		b.setIsPostfix(true);
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(PrefixExpression node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		if (node.getOperator() == PrefixExpression.OP_DEC)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_DEC);
		else if (node.getOperator() == PrefixExpression.OP_INC)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_INC);
		else if (node.getOperator() == PrefixExpression.OP_UNPACK)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);// FIXME
		node.getVariable().accept(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(Quote node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(Expression.ExpressionKind.LITERAL);// FIXME
		String quote = "";
		for (org.eclipse.php.internal.core.ast.nodes.Expression e : node.expressions()) {
			if (e instanceof Identifier)
				quote += ((Identifier) e).getName();
			else if (e instanceof Scalar) {
				quote += ((Scalar) e).getStringValue();
			} else {
				e.accept(this);// FIXME
				b.addExpressions(expressions.pop());
			}
		}
		b.setLiteral(quote);
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(Reference node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(Expression.ExpressionKind.OTHER);// FIXME Add Reference to
													// ExpressionKind?
		node.getExpression().accept(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(ReflectionVariable node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(ExpressionKind.VARACCESS);
		if (node.getName() instanceof Identifier)
			b.setVariable(((Identifier) node.getName()).getName());
		else {
			node.getName().accept(this);
			b.addExpressions(expressions.pop());
		}
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(ReturnStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.RETURN);
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
			b.setExpression(expressions.pop());
		}
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(Scalar node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(ExpressionKind.LITERAL);
		if (node.getScalarType() != Scalar.TYPE_UNKNOWN)
			b.setLiteral(node.getStringValue());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(SingleFieldDeclaration node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(ExpressionKind.VARDECL);
		Variable.Builder vb = Variable.newBuilder();
		if (node.getName().getName() instanceof Identifier)
			vb.setName(((Identifier) node.getName().getName()).getName());
		else {
			node.getName().accept(this);
			vb.setComputedName(expressions.pop());
		}
		node.getValue().accept(this);
		vb.setInitializer(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(StaticConstantAccess node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(ExpressionKind.VARACCESS);
		node.getClassName().accept(this);
		b.addExpressions(expressions.pop());
		b.setVariable(node.getConstant().getName());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(StaticFieldAccess node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(ExpressionKind.VARACCESS);
		node.getClassName().accept(this);
		b.addExpressions(expressions.pop());
		if (node.getField().getName() instanceof Identifier)
			b.setVariable(((Identifier) node.getField().getName()).getName());
		else {
			node.getField().accept(this);
			b.addExpressions(expressions.pop());
		}
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(StaticMethodInvocation node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.METHODCALL);
		FunctionName fn = node.getMethod().getFunctionName();
		node.getClassName().accept(this);
		b.addExpressions(expressions.pop());
		if (fn.getName() instanceof Identifier) 
			b.setMethod(((Identifier) node.getMethod().getFunctionName().getName()).getName());// FIXME
		else {// FIXME could be a variable with a name that could be Identifier add another check?
			fn.getName().accept(this);
			b.addExpressions(expressions.pop());
		}
		for (org.eclipse.php.internal.core.ast.nodes.Expression a : node.getMethod().parameters()) {
			a.accept(this);
			b.addMethodArgs(expressions.pop());
		}
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(StaticStatement node) {
		Statement.Builder sb = Statement.newBuilder();
		sb.setKind(StatementKind.EXPRESSION);
		Expression.Builder b = Expression.newBuilder();
		b.setKind(ExpressionKind.VARDECL);// FIXME
		Modifier.Builder mb = Modifier.newBuilder();
		mb.setKind(ModifierKind.STATIC);
		Modifier mod = mb.build();
		Type.Builder tb = Type.newBuilder();
		tb.setKind(TypeKind.OTHER);
		tb.setName("");
		Type type = tb.build();
		for (org.eclipse.php.internal.core.ast.nodes.Expression e : node.expressions()) {
			Variable.Builder vb = Variable.newBuilder();
			vb.setVariableType(type);
			vb.addModifiers(mod);
			if (e instanceof org.eclipse.php.internal.core.ast.nodes.Variable) {
				org.eclipse.php.internal.core.ast.nodes.Expression name = ((org.eclipse.php.internal.core.ast.nodes.Variable) e)
						.getName();
				if (name instanceof Identifier)
					vb.setName(((Identifier) name).getName());
				else {
					name.accept(this);
					vb.setComputedName(expressions.pop());
				}
			} else if (e instanceof Assignment) {
				Assignment assign = (Assignment) e;
				VariableBase var = assign.getLeftHandSide();
				if (var instanceof org.eclipse.php.internal.core.ast.nodes.Variable
						&& ((org.eclipse.php.internal.core.ast.nodes.Variable) var).getName() instanceof Identifier) {
					vb.setName(((Identifier) ((org.eclipse.php.internal.core.ast.nodes.Variable) var).getName())
							.getName());
				} else {
					var.accept(this);
					vb.setComputedName(expressions.pop());
				}
				assign.getRightHandSide().accept(this);
				vb.setInitializer(expressions.pop());
			} else {
				e.accept(this);
				b.addExpressions(expressions.pop());
			}
			b.addVariableDecls(vb.build());
		}
		sb.setExpression(b.build());
		statements.peek().add(sb.build());
		return false;
	}

	@Override
	public boolean visit(SwitchCase node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.CASE);
		if (node.getValue() != null) {
			node.getValue().accept(this);
			b.setExpression(expressions.pop());
		}
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		methods.push(new ArrayList<Method>());
		for (org.eclipse.php.internal.core.ast.nodes.Statement s : node.actions())
			s.accept(this);
		for (Statement st : statements.pop())
			b.addStatements(st);
		for (Method m : methods.pop())
			b.addMethods(m);
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(SwitchStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.SWITCH);
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
			b.setExpression(expressions.pop());
		}
		methods.push(new ArrayList<Method>());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getBody().accept(this);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		for (Method m : methods.pop())
			b.addMethods(m);
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(ThrowStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.THROW);
		node.getExpression().accept(this);
		b.setExpression(expressions.pop());
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(TryStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.TRY);
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		methods.push(new ArrayList<Method>());
		node.getBody().accept(this);
		for (Object c : node.catchClauses())
			((CatchClause) c).accept(this);
		if (node.finallyClause() != null)
			node.finallyClause().accept(this);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		for (Method m : methods.pop())
			b.addMethods(m);
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(FinallyClause node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.FINALLY);
		methods.push(new ArrayList<Method>());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getBody().accept(this);
		for (Statement s : statements.pop())
			b.addStatements(s);
		for (Method m : methods.pop())
			b.addMethods(m);
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(UnaryOperation node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		if (node.getOperator() == UnaryOperation.OP_MINUS)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_SUB);
		else if (node.getOperator() == UnaryOperation.OP_PLUS)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_ADD);
		else if (node.getOperator() == UnaryOperation.OP_TILDA)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);// FIXME
		else if (node.getOperator() == UnaryOperation.OP_NOT)
			b.setKind(ExpressionKind.LOGICAL_NOT);
		node.getExpression().accept(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(org.eclipse.php.internal.core.ast.nodes.Variable node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(ExpressionKind.VARACCESS); // FIXME could be a decleration

		Variable.Builder vb = Variable.newBuilder();
		String name = "";
		if (node.isDollared())
			name += "$";
		if (node.getName() instanceof Identifier)
			name += (((Identifier) node.getName()).getName());
		else {
			node.getName().accept(this);
			vb.setComputedName(expressions.pop());
		}
		vb.setName(name);
		b.addVariableDecls(vb.build());

		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(UseStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.OTHER);// FIXME
		for (UseStatementPart usp : node.parts()) {
			usp.accept(this); //FIXME add String namespacename and String aliasName to Statements?
			b.addExpressions(expressions.pop());
		}
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(UseStatementPart node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(ExpressionKind.OTHER);// FIXME
		Variable.Builder vb = Variable.newBuilder();
		vb.setName(node.getName().getName());
		b.addVariableDecls(vb.build());
		if (node.getAlias() != null) {
			Expression.Builder eb = Expression.newBuilder();
			eb.setKind(ExpressionKind.OTHER);// FIXME
			eb.setVariable(node.getAlias().getName());
			vb.setInitializer(eb.build());
		}
		b.addVariableDecls(vb.build());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(WhileStatement node) {
		Statement.Builder b = Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.WHILE);
		if (node.getCondition() != null) {
			node.getCondition().accept(this);
			b.setExpression(expressions.pop());
		}
		methods.push(new ArrayList<Method>());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getBody().accept(this);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		for (Method m : methods.pop())
			b.addMethods(m);
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(YieldExpression node) {
		boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.YIELD);
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
			eb.addExpressions(expressions.pop());
		}
		expressions.push(eb.build());
		return false;
	}

	@Override
	public boolean visit(ASTNode node) {
		throw new RuntimeException("visited unused node " + node.getClass());
	}

	@Override
	public boolean visit(FullyQualifiedTraitMethodReference node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(ExpressionKind.METHOD_REFERENCE);
		b.setMethod(node.getFunctionName().getName());
		b.setVariable(node.getClassName().getName());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(TraitAlias node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(ExpressionKind.OTHER);// FIXME
		b.setMethod(node.getFunctionName().getName());
		node.getTraitMethod().accept(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(TraitAliasStatement node) {
		Statement.Builder b = Statement.newBuilder();
		b.setKind(StatementKind.EXPRESSION);// FIXME
		node.getAlias().accept(this);
		b.setExpression(expressions.pop());
		statements.peek().add(b.build());
		return false;
	}

	@Override
	public boolean visit(TraitDeclaration node) {
		boa.types.Ast.Declaration.Builder b = boa.types.Ast.Declaration.newBuilder();
		b.setName(node.getName().getName());
		int mod = node.getModifier();
		Modifier.Builder mb = Modifier.newBuilder();
		mb.setKind(Modifier.ModifierKind.OTHER);
		mb.setOther("trait");
		b.addModifiers(mb.build());
		if (mod != ClassDeclaration.MODIFIER_NONE) {
			 mb = Modifier.newBuilder();
			switch (mod) {
			case ClassDeclaration.MODIFIER_ABSTRACT:
				mb.setKind(Modifier.ModifierKind.ABSTRACT);
				break;
			case ClassDeclaration.MODIFIER_FINAL:
				mb.setKind(Modifier.ModifierKind.FINAL);
				break;
			}
			b.addModifiers(mb.build());
		}
		b.setKind(boa.types.Ast.TypeKind.OTHER);// FIXME
		for (Object d : node.getBody().statements()) {
			if (d instanceof FieldsDeclaration || d instanceof ConstantDeclaration) {
				fields.push(new ArrayList<boa.types.Ast.Variable>());
				((ASTNode) d).accept(this);
				for (boa.types.Ast.Variable v : fields.pop())
					b.addFields(v);
			} else if (d instanceof MethodDeclaration) {
				methods.push(new ArrayList<boa.types.Ast.Method>());
				((MethodDeclaration) d).accept(this);
				for (boa.types.Ast.Method m : methods.pop())
					b.addMethods(m);
			}
		}
		declarations.peek().add(b.build());
		return false;
	}

	@Override
	public boolean visit(TraitPrecedence node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(ExpressionKind.OTHER);// FIXME
		for (NamespaceName n : node.getTrList()) {
			n.accept(this);
			b.addExpressions(expressions.pop());
		}
		node.getMethodReference().accept(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(TraitPrecedenceStatement node) {
		Statement.Builder b = Statement.newBuilder();
		b.setKind(StatementKind.EXPRESSION);// FIXME
		node.getPrecedence().accept(this);
		b.setExpression(expressions.pop());
		statements.peek().add(b.build());
		return false;
	}

	@Override
	public boolean visit(TraitUseStatement node) {
		Method.Builder b = Method.newBuilder();
		b.setName("use");
		for (NamespaceName n : node.getTraitList()){ 
			Variable.Builder vb = Variable.newBuilder();
			vb.setName(n.getName());
			b.addArguments(vb.build());
		}
		statements.push(new ArrayList<Statement>());
		for (TraitStatement ts : node.getTsList())
			ts.accept(this);
		for (Statement s : statements.pop())
			b.addStatements(s);
		methods.peek().add(b.build());
		return false;
	}
	
	private void wrapFieldsInAStatetment(ASTNode node){
		Statement.Builder b = Statement.newBuilder();
		Expression.Builder eb = Expression.newBuilder();
		eb.setKind(ExpressionKind.VARDECL);
		b.setKind(StatementKind.EXPRESSION);//FIXME
		fields.push(new ArrayList<Variable>());
		node.accept(this);
		for (Variable v : fields.pop())
			eb.addVariableDecls(v);
		b.setExpression(eb.build());
		statements.peek().add(b.build());
	}
}
