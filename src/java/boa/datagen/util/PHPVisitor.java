package boa.datagen.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.eclipse.dltk.ast.Modifiers;
import org.eclipse.php.internal.core.ast.nodes.*;
import org.eclipse.php.internal.core.ast.visitor.AbstractVisitor;

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

public class PHPVisitor extends AbstractVisitor {

	private PositionInfo.Builder pos = null;
	private String src = null;
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
		namespaces.push(new ArrayList<boa.types.Ast.Namespace>());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		declarations.push(new ArrayList<boa.types.Ast.Declaration>());
		fields.push(new ArrayList<boa.types.Ast.Variable>());
		methods.push(new ArrayList<boa.types.Ast.Method>());
		for (org.eclipse.php.internal.core.ast.nodes.Statement s : node.statements())
			s.accept(this);
		List<boa.types.Ast.Namespace> ns = namespaces.pop();
		List<boa.types.Ast.Statement> ss = statements.pop();
		List<boa.types.Ast.Declaration> ds = declarations.pop();
		List<boa.types.Ast.Method> ms = methods.pop();
		List<boa.types.Ast.Variable> fs = fields.pop();
		if (ns.size() == 1 && ss.isEmpty() && ds.isEmpty() && ms.isEmpty() && fs.isEmpty()) {
			b = Namespace.newBuilder(ns.get(0));
		} else {
			b.setName("");
			for (boa.types.Ast.Namespace d : ns)
				b.addNamespaces(d);
			for (boa.types.Ast.Statement st : ss)
				b.addStatements(st);
			for (boa.types.Ast.Declaration d : ds)
				b.addDeclarations(d);
			for (boa.types.Ast.Method m : ms)
				b.addMethods(m);
			for (boa.types.Ast.Variable v : fs)
				b.addVariables(v);
		}
//		for (Object c : node.comments())
//			((Comment) c).accept(this);
		if (!expressions.isEmpty())
			throw new RuntimeException("expressions not empty");
		if (!statements.isEmpty())
			throw new RuntimeException("statements not empty");
		if (!fields.isEmpty())
			throw new RuntimeException("fields not empty");
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
		b.setKind(ExpressionKind.NAMESPACENAME);
		b.setLiteral(node.getName());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(NamespaceDeclaration node) {
		Namespace.Builder nb = Namespace.newBuilder();
		List<boa.types.Ast.Namespace> list = namespaces.peek();
		nb.setName(node.getName().getName());
		namespaces.push(new ArrayList<boa.types.Ast.Namespace>());
		methods.push(new ArrayList<boa.types.Ast.Method>());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		declarations.push(new ArrayList<boa.types.Ast.Declaration>());
		fields.push(new ArrayList<boa.types.Ast.Variable>());
		for (org.eclipse.php.internal.core.ast.nodes.Statement s : node.getBody().statements())
			s.accept(this);
		for (boa.types.Ast.Namespace d : namespaces.pop())
			nb.addNamespaces(d);
		for (boa.types.Ast.Method m : methods.pop())
			nb.addMethods(m);
		for (boa.types.Ast.Statement st : statements.pop())
			nb.addStatements(st);
		for (boa.types.Ast.Declaration d : declarations.pop())
			nb.addDeclarations(d);
		for (boa.types.Ast.Variable v : fields.pop())
			nb.addVariables(v);
		list.add(nb.build());
		return false;
	}

	@Override
	public boolean visit(ArrayAccess node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		if (node.getArrayType() == ArrayAccess.VARIABLE_HASHTABLE)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.HASHTABLEACCESS);
		else
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ARRAYACCESS);
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
		b.setKind(boa.types.Ast.Expression.ExpressionKind.ARRAYELEMENT);
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
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_CONCAT);
		else if (node.getOperator() == Assignment.OP_MUL_EQUAL)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_MULT);
		else if (node.getOperator() == Assignment.OP_POW_EQUAL)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_POW);
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(BackTickExpression node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.BACKTICK);
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
		b.setKind(boa.types.Ast.Statement.StatementKind.BLOCK);
		fields.push(new ArrayList<boa.types.Ast.Variable>());
		methods.push(new ArrayList<boa.types.Ast.Method>());
		declarations.push(new ArrayList<boa.types.Ast.Declaration>());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		for (org.eclipse.php.internal.core.ast.nodes.Statement s : node.statements())
			s.accept(this);
		for (boa.types.Ast.Method m : methods.pop())
			b.addMethods(m);
		for (Declaration decl : declarations.pop())
			b.addTypeDeclarations(decl);
		for (boa.types.Ast.Statement st : statements.pop())
			b.addStatements(st);
		for (boa.types.Ast.Variable v : fields.pop())
			b.addVariableDeclarations(v);
		statements.peek().add(b.build());
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
		if (ex.isDollared())
			vb.setDollarSign(true);
		if (ex.getName() instanceof Identifier) {
			vb.setName(((Identifier) ex.getName()).getName());
		} else {
			ex.getName().accept(this);
			vb.setComputedName(expressions.pop());
		}
		boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
		org.eclipse.php.internal.core.ast.nodes.Expression en = node.getClassNames().get(0);
		if (en instanceof Identifier)
			tb.setName(((Identifier) en).getName());
		else {
			en.accept(this);
			tb.setComputedName(expressions.pop());
		}
		tb.setKind(boa.types.Ast.TypeKind.CLASS);
		vb.setVariableType(tb.build());
		b.setVariableDeclaration(vb.build());
		fields.push(new ArrayList<boa.types.Ast.Variable>());
		methods.push(new ArrayList<boa.types.Ast.Method>());
		declarations.push(new ArrayList<boa.types.Ast.Declaration>());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getBody().accept(this);
		for (boa.types.Ast.Method m : methods.pop())
			b.addMethods(m);
		for (Declaration decl : declarations.pop())
			b.addTypeDeclarations(decl);
		for (boa.types.Ast.Statement st : statements.pop())
			b.addStatements(st);
		for (boa.types.Ast.Variable v : fields.pop())
			b.addVariableDeclarations(v);
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(ConstantDeclaration node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.EXPRESSION);
		boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.VARDECL);
		List<Identifier> variableNames = node.names();
		List<org.eclipse.php.internal.core.ast.nodes.Expression> constantValues = node.initializers();
		int mod = node.getModifier();
		for (int i = 0; i < node.names().size(); i++) {
			boa.types.Ast.Variable.Builder vb = boa.types.Ast.Variable.newBuilder();
			Modifier.Builder mb = Modifier.newBuilder();
			mb.setKind(Modifier.ModifierKind.CONSTANT);
			vb.addModifiers(mb.build());
			vb.setName(variableNames.get(i).getName());
			if ((mod & Modifiers.AccPublic) != 0) {
				mb = boa.types.Ast.Modifier.newBuilder();
				mb.setKind(boa.types.Ast.Modifier.ModifierKind.VISIBILITY);
				mb.setVisibility(boa.types.Ast.Modifier.Visibility.PUBLIC);
				vb.addModifiers(mb.build());
			}
			if ((mod & Modifiers.AccPrivate) != 0) {
				mb = boa.types.Ast.Modifier.newBuilder();
				mb.setKind(boa.types.Ast.Modifier.ModifierKind.VISIBILITY);
				mb.setVisibility(boa.types.Ast.Modifier.Visibility.PRIVATE);
				vb.addModifiers(mb.build());
			}
			if ((mod & Modifiers.AccProtected) != 0) {
				mb = boa.types.Ast.Modifier.newBuilder();
				mb.setKind(boa.types.Ast.Modifier.ModifierKind.VISIBILITY);
				mb.setVisibility(boa.types.Ast.Modifier.Visibility.PROTECTED);
				vb.addModifiers(mb.build());
			}
			if ((mod & Modifiers.AccAbstract) != 0) {
				mb = boa.types.Ast.Modifier.newBuilder();
				mb.setKind(boa.types.Ast.Modifier.ModifierKind.ABSTRACT);
				vb.addModifiers(mb.build());
			}
			if ((mod & Modifiers.AccStatic) != 0) {
				mb = boa.types.Ast.Modifier.newBuilder();
				mb.setKind(boa.types.Ast.Modifier.ModifierKind.STATIC);
				vb.addModifiers(mb.build());
			}
			if ((mod & Modifiers.AccFinal) != 0) {
				mb = boa.types.Ast.Modifier.newBuilder();
				mb.setKind(boa.types.Ast.Modifier.ModifierKind.FINAL);
				vb.addModifiers(mb.build());
			}
			org.eclipse.php.internal.core.ast.nodes.Expression v = constantValues.get(i);
			if (v != null) {
				v.accept(this);
				vb.setInitializer(expressions.pop());
			}
			eb.addVariableDecls(vb);
		}
		b.setExpression(eb.build());
		list.add(b.build());
		return false;
	}
	
	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		boa.types.Ast.Declaration.Builder b = boa.types.Ast.Declaration.newBuilder();
		b.setName("");
		b.setKind(boa.types.Ast.TypeKind.ANONYMOUS);
		if (node.getSuperClass() != null) {
			Type.Builder tb = Type.newBuilder();
			tb.setKind(TypeKind.CLASS);
			if (node.getSuperClass() instanceof Identifier) {
				tb.setName(((Identifier) node.getSuperClass()).getName());
			} else {
				node.getSuperClass().accept(this);
				tb.setComputedName(expressions.pop());
			}
			b.addParents(tb.build());
		}
		if (node.getInterfaces() != null) {
			for (Identifier i : node.getInterfaces()) {
				Type.Builder tb = Type.newBuilder();
				tb.setKind(TypeKind.INTERFACE);
				tb.setName(i.getName());
				b.addParents(tb.build());
			}
		}
		if (node.getBody() != null && node.getBody().statements() != null) {
			fields.push(new ArrayList<boa.types.Ast.Variable>());
			methods.push(new ArrayList<boa.types.Ast.Method>());
			declarations.push(new ArrayList<boa.types.Ast.Declaration>());
			statements.push(new ArrayList<boa.types.Ast.Statement>());
			for (ASTNode d : node.getBody().statements())
				d.accept(this);
			for (boa.types.Ast.Variable v : fields.pop())
				b.addFields(v);
			for (boa.types.Ast.Method m : methods.pop())
				b.addMethods(m);
			for (boa.types.Ast.Declaration d : declarations.pop())
				b.addNestedDeclarations(d);
			for (boa.types.Ast.Statement st : statements.pop())
				b.addStatements(st);
		}
		declarations.peek().add(b.build());
		return false;
	}
	
	@Override
	public boolean visit(ClassDeclaration node) {
		boa.types.Ast.Declaration.Builder b = boa.types.Ast.Declaration.newBuilder();
		b.setName(node.getName().getName());
		int mod = node.getModifier();
		if (mod != ClassDeclaration.MODIFIER_NONE) {
			Modifier.Builder mb = null;
			switch (mod) {
			case ClassDeclaration.MODIFIER_ABSTRACT:
				mb = Modifier.newBuilder();
				mb.setKind(Modifier.ModifierKind.ABSTRACT);
				b.addModifiers(mb.build());
				break;
			case ClassDeclaration.MODIFIER_FINAL:
				mb = Modifier.newBuilder();
				mb.setKind(Modifier.ModifierKind.FINAL);
				b.addModifiers(mb.build());
				break;
			}
		}
		b.setKind(boa.types.Ast.TypeKind.CLASS);
		if (node.getSuperClass() != null) {
			Type.Builder tb = Type.newBuilder();
			tb.setKind(TypeKind.CLASS);
			if (node.getSuperClass() instanceof Identifier) {
				tb.setName(((Identifier) node.getSuperClass()).getName());
			} else {
				node.getSuperClass().accept(this);
				tb.setComputedName(expressions.pop());
			}
			b.addParents(tb.build());
		}
		for (Identifier i : node.interfaces()) {
			Type.Builder tb = Type.newBuilder();
			tb.setKind(TypeKind.INTERFACE);
			tb.setName(i.getName());
			b.addParents(tb.build());
		}
		fields.push(new ArrayList<boa.types.Ast.Variable>());
		methods.push(new ArrayList<boa.types.Ast.Method>());
		declarations.push(new ArrayList<boa.types.Ast.Declaration>());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		for (ASTNode d : node.getBody().statements())
			d.accept(this);
		for (boa.types.Ast.Variable v : fields.pop())
			b.addFields(v);
		for (boa.types.Ast.Method m : methods.pop())
			b.addMethods(m);
		for (boa.types.Ast.Declaration d : declarations.pop())
			b.addNestedDeclarations(d);
		for (boa.types.Ast.Statement st : statements.pop())
			b.addStatements(st);
		declarations.peek().add(b.build());
		return false;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.NEW);
		boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
		tb.setKind(boa.types.Ast.TypeKind.CLASS);
		if (node.getClassName().getName() instanceof Identifier)
			tb.setName(((Identifier) node.getClassName().getName()).getName());
		else {
			node.getClassName().getName().accept(this);
			tb.setComputedName(expressions.pop());
		}
		b.setNewType(tb.build());
		for (org.eclipse.php.internal.core.ast.nodes.Expression a : node.ctorParams()) {
			a.accept(this);
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
//		Expression.Builder b = Expression.newBuilder();
//		b.setKind(Expression.ExpressionKind.OTHER);
//		node.getName().accept(this);
//		b.addExpressions(expressions.pop());
//		expressions.push(b.build());
//		return false;
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(CloneExpression node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(Expression.ExpressionKind.CLONE);
		node.getExpression().accept(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(ConditionalExpression node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		node.getCondition().accept(this);
		b.addExpressions(expressions.pop());
		if (node.getOperatorType() == ConditionalExpression.OP_TERNARY) {
			b.setKind(boa.types.Ast.Expression.ExpressionKind.CONDITIONAL);
			if (node.getIfTrue() != null) {
				node.getIfTrue().accept(this);
				b.addExpressions(expressions.pop());
			}
			if (node.getIfFalse() != null) {
				node.getIfFalse().accept(this);
				b.addExpressions(expressions.pop());
			}
		} else if (node.getOperatorType() == ConditionalExpression.OP_COALESCE) {
			b.setKind(boa.types.Ast.Expression.ExpressionKind.NULLCOALESCE);
			node.getIfTrue().accept(this);
			b.addExpressions(expressions.pop());
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
		List<boa.types.Ast.Statement> list = statements.peek();
		Statement.Builder b = Statement.newBuilder();
		b.setKind(Statement.StatementKind.DECLARE);
		List<Identifier> directiveNames = node.directiveNames();
		List<org.eclipse.php.internal.core.ast.nodes.Expression> directiveValues = node.directiveValues();
		for (int i = 0; i < node.directiveNames().size(); i++) {
			boa.types.Ast.Variable.Builder vb = boa.types.Ast.Variable.newBuilder();
			vb.setName(directiveNames.get(i).getName());
			directiveValues.get(i).accept(this);
			vb.setInitializer(expressions.pop());
			b.addVariableDeclarations(vb.build());
		}
		statements.push(new ArrayList<Statement>());
		node.getBody().accept(this);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(DoStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.DO);
		node.getCondition().accept(this);
		b.addConditions(expressions.pop());
		
		declarations.push(new ArrayList<boa.types.Ast.Declaration>());
		fields.push(new ArrayList<boa.types.Ast.Variable>());
		methods.push(new ArrayList<Method>());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getBody().accept(this);
		for (boa.types.Ast.Declaration d : declarations.pop())
			b.addTypeDeclarations(d);
		for (boa.types.Ast.Variable v : fields.pop())
			b.addVariableDeclarations(v);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		for (Method m : methods.pop())
			b.addMethods(m);
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(EchoStatement node) {
		Statement.Builder b = Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(Statement.StatementKind.ECHO);
		for (org.eclipse.php.internal.core.ast.nodes.Expression e : node.expressions()) {
			e.accept(this);
			b.addExpressions(expressions.pop());
		}
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(EmptyStatement node) {
//		Statement.Builder b = Statement.newBuilder();
//		List<boa.types.Ast.Statement> list = statements.peek();
//		b.setKind(Statement.StatementKind.EMPTY);
//		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(ExpressionStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.EXPRESSION);
		node.getExpression().accept(this);
		b.setExpression(expressions.pop());
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(FieldAccess node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.VARACCESS);
		b.setIsMemberAccess(true);
		node.getDispatcher().accept(this);
		b.addExpressions(expressions.pop());
		node.getField().accept(this);
		b.addExpressions(expressions.pop());
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
			if ((mod & Modifiers.AccAbstract) != 0) {
				boa.types.Ast.Modifier.Builder mb = boa.types.Ast.Modifier.newBuilder();
				mb.setKind(boa.types.Ast.Modifier.ModifierKind.ABSTRACT);
				b.addModifiers(mb.build());
			}
			if (variableNames[i].isDollared())
				b.setDollarSign(true);
			if (variableNames[i].getName() instanceof Identifier) {
				b.setName(((Identifier) variableNames[i].getName()).getName());
			} else {
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
		node.getExpression().accept(this);
		b.addExpressions(expressions.pop());
		if (node.getKey() != null) {
			node.getKey().accept(this);
			b.addExpressions(expressions.pop());
		}
		if (node.getValue() != null) {
			node.getValue().accept(this);
			b.addExpressions(expressions.pop());
		}
		declarations.push(new ArrayList<boa.types.Ast.Declaration>());
		fields.push(new ArrayList<boa.types.Ast.Variable>());
		methods.push(new ArrayList<Method>());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getStatement().accept(this);
		for (boa.types.Ast.Declaration d : declarations.pop())
			b.addTypeDeclarations(d);
		for (boa.types.Ast.Variable v : fields.pop())
			b.addVariableDeclarations(v);
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
		if (node.getParameterType() != null) {
			Type.Builder tb = Type.newBuilder();
			if (node.getParameterType() instanceof Identifier)
				tb.setName(((Identifier) node.getParameterType()).getName());
			else {
				node.getParameterType().accept(this);
				tb.setComputedName(expressions.pop());
			}
			tb.setKind(boa.types.Ast.TypeKind.OTHER);
			b.setVariableType(tb.build());
		}
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
			b.addConditions(expressions.pop());
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
			b.setMethod(((Identifier) fn.getName()).getName());
		else if (fn.getName() instanceof org.eclipse.php.internal.core.ast.nodes.Variable) {
			org.eclipse.php.internal.core.ast.nodes.Variable vn = (org.eclipse.php.internal.core.ast.nodes.Variable) fn.getName();
			if (!vn.isDollared() && vn.getName() instanceof Identifier)
				b.setMethod(((Identifier)vn.getName()).getName());
			else {
				vn.accept(this);
				b.setComputedMethod(expressions.pop());
			}
		} else {
			fn.getName().accept(this);
			b.setComputedMethod(expressions.pop());
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
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(GlobalStatement node) {
		Statement.Builder b = Statement.newBuilder();
		List<Statement> list = statements.peek();
		b.setKind(StatementKind.GLOBAL);
		Expression.Builder eb = Expression.newBuilder();
		eb.setKind(ExpressionKind.VARDECL);
		for (org.eclipse.php.internal.core.ast.nodes.Variable v : node.variables()) {
			Variable.Builder vb = Variable.newBuilder();
			if (v.isDollared())
				vb.setDollarSign(true);
			if (v.getName() instanceof Identifier) {
				vb.setName(((Identifier) v.getName()).getName());
			} else {
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
		b.setKind(StatementKind.GOTO);
		Expression.Builder eb = Expression.newBuilder();
		eb.setKind(Expression.ExpressionKind.LITERAL);
		eb.setLiteral(node.getLabel().getName());
		b.setExpression(eb.build());
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(Identifier node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(IfStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		node.getCondition().accept(this);
		b.addConditions(expressions.pop());
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
		b.setKind(Expression.ExpressionKind.IGNORE_ERROR);
		node.getExpression().accept(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(Include node) {
		Expression.Builder eb = Expression.newBuilder();
		if (node.getIncludeType() == Include.IT_INCLUDE_ONCE)
			eb.setKind(ExpressionKind.INCLUDE_ONCE);
		else if (node.getIncludeType() == Include.IT_REQUIRE)
			eb.setKind(ExpressionKind.REQUIRE);
		else if (node.getIncludeType() == Include.IT_REQUIRE_ONCE)
			eb.setKind(ExpressionKind.REQUIRE_ONCE);
		else
			eb.setKind(ExpressionKind.INCLUDE);
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
			b.setKind(boa.types.Ast.Expression.ExpressionKind.IDENTICAL);
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
			b.setKind(boa.types.Ast.Expression.ExpressionKind.NOTIDENTICAL);
		else if (node.getOperator() == InfixExpression.OP_OR)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.BIT_OR);
		else if (node.getOperator() == InfixExpression.OP_PLUS)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_ADD);
		else if (node.getOperator() == InfixExpression.OP_MOD)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_MOD);
		else if (node.getOperator() == InfixExpression.OP_SR)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.BIT_RSHIFT);
		else if (node.getOperator() == InfixExpression.OP_STRING_XOR)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.STRING_XOR);
		else if (node.getOperator() == InfixExpression.OP_STRING_OR)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.STRING_OR);
		else if (node.getOperator() == InfixExpression.OP_STRING_AND)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.STRING_AND);
		else if (node.getOperator() == InfixExpression.OP_MUL)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_MULT);
		else if (node.getOperator() == InfixExpression.OP_XOR)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.BIT_XOR);
		else if (node.getOperator() == InfixExpression.OP_CONCAT)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_CONCAT);
		else if (node.getOperator() == InfixExpression.OP_POW)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_POW);
		else if (node.getOperator() == InfixExpression.OP_SPACESHIP)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_THREE_WAY_COMPARE);
		node.getLeft().accept(this);
		b.addExpressions(expressions.pop());
		node.getRight().accept(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(InLineHtml node) {
		String source = src.substring(node.getStart(), node.getEnd()).trim();
		if (source.isEmpty())
			return false;
		Statement.Builder b = Statement.newBuilder();
		List<Statement> list = statements.peek();
		b.setKind(StatementKind.INLINE_HTML);
		Expression.Builder eb = Expression.newBuilder();
		eb.setKind(Expression.ExpressionKind.LITERAL);
		eb.setLiteral(source);
		b.setExpression(eb.build());
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(InstanceOfExpression node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.TYPECOMPARE);
		node.getExpression().accept(this);
		b.addExpressions(expressions.pop());
		boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
		tb.setKind(boa.types.Ast.TypeKind.OTHER);
		org.eclipse.php.internal.core.ast.nodes.Expression name = node.getClassName().getName();
		if (name instanceof Identifier) {
			tb.setName(((Identifier) name).getName());
		} else {
			name.accept(this);
			tb.setComputedName(expressions.pop());
		}
		b.setNewType(tb.build());
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
		fields.push(new ArrayList<boa.types.Ast.Variable>());
		methods.push(new ArrayList<boa.types.Ast.Method>());
		declarations.push(new ArrayList<boa.types.Ast.Declaration>());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		for (org.eclipse.php.internal.core.ast.nodes.Statement d : node.getBody().statements())
			d.accept(this);
		for (boa.types.Ast.Variable v : fields.pop())
			b.addFields(v);
		for (boa.types.Ast.Method m : methods.pop())
			b.addMethods(m);
		for (boa.types.Ast.Declaration d : declarations.pop())
			b.addNestedDeclarations(d);
		for (boa.types.Ast.Statement st : statements.pop())
			b.addStatements(st);
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
		b.setKind(Expression.ExpressionKind.LIST);
		for (org.eclipse.php.internal.core.ast.nodes.Expression v : node.variables()) {
			Variable.Builder vb = Variable.newBuilder();
			if (v instanceof org.eclipse.php.internal.core.ast.nodes.Variable
					&& ((org.eclipse.php.internal.core.ast.nodes.Variable) v).getName() instanceof Identifier) {
				if (((org.eclipse.php.internal.core.ast.nodes.Variable) v).isDollared())
					vb.setDollarSign(true);
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
			b.setMethod(((Identifier) node.getMethod().getFunctionName().getName()).getName());
		else if (fn.getName() instanceof org.eclipse.php.internal.core.ast.nodes.Variable) {
			org.eclipse.php.internal.core.ast.nodes.Variable vn = (org.eclipse.php.internal.core.ast.nodes.Variable) fn.getName();
			if (!vn.isDollared() && vn.getName() instanceof Identifier)
				b.setMethod(((Identifier)vn.getName()).getName());
			else {
				vn.accept(this);
				b.setComputedMethod(expressions.pop());
			}
		} else {
			fn.getName().accept(this);
			b.setComputedMethod(expressions.pop());
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
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_UNPACK);
		node.getVariable().accept(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(Quote node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(Expression.ExpressionKind.QUOTE);
		for (org.eclipse.php.internal.core.ast.nodes.Expression e : node.expressions()) {
			e.accept(this);
			b.addExpressions(expressions.pop());
		}
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(Reference node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(Expression.ExpressionKind.REFERENCE);
		node.getExpression().accept(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(ReflectionVariable node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(ExpressionKind.REFLECTION);
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
		b.setLiteral(node.getStringValue());
//		boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
//		tb.setKind(TypeKind.PRIMITIVE);
//		tb.setName(Scalar.getType(node.getScalarType()));
//		b.setReturnType(tb);
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(SingleFieldDeclaration node) {
		Variable.Builder vb = Variable.newBuilder();
		if (node.getName().getName() instanceof Identifier)
			vb.setName(((Identifier) node.getName().getName()).getName());
		else {
			node.getName().accept(this);
			vb.setComputedName(expressions.pop());
		}
		if (node.getValue() != null) {
			node.getValue().accept(this);
			vb.setInitializer(expressions.pop());
		}
		fields.peek().add(vb.build());
		return false;
	}

	@Override
	public boolean visit(StaticConstantAccess node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(ExpressionKind.VARACCESS);
		b.setIsStatic(true);
		b.setIsMemberAccess(true);
		org.eclipse.php.internal.core.ast.nodes.Expression qual = node.getClassName();
		boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
		tb.setKind(TypeKind.OTHER);
		if (qual instanceof Identifier)
			tb.setName(((Identifier) qual).getName());
		else {
			qual.accept(this);
			tb.setComputedName(expressions.pop());
		}
		b.setDeclaringType(tb);
		b.setVariable(node.getConstant().getName());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(StaticFieldAccess node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(ExpressionKind.VARACCESS);
		b.setIsStatic(true);
		b.setIsMemberAccess(true);
		org.eclipse.php.internal.core.ast.nodes.Expression qual = node.getClassName();
		boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
		tb.setKind(TypeKind.OTHER);
		if (qual instanceof Identifier)
			tb.setName(((Identifier) qual).getName());
		else {
			qual.accept(this);
			tb.setComputedName(expressions.pop());
		}
		b.setDeclaringType(tb);
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
		b.setIsStatic(true);
		org.eclipse.php.internal.core.ast.nodes.Expression qual = node.getClassName();
		boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
		tb.setKind(TypeKind.OTHER);
		if (qual instanceof Identifier)
			tb.setName(((Identifier) qual).getName());
		else {
			qual.accept(this);
			tb.setComputedName(expressions.pop());
		}
		b.setDeclaringType(tb);
		FunctionName fn = node.getMethod().getFunctionName();
		if (fn.getName() instanceof Identifier)
			b.setMethod(((Identifier) fn.getName()).getName());
		else if (fn.getName() instanceof org.eclipse.php.internal.core.ast.nodes.Variable) {
			org.eclipse.php.internal.core.ast.nodes.Variable vn = (org.eclipse.php.internal.core.ast.nodes.Variable) fn.getName();
			if (!vn.isDollared() && vn.getName() instanceof Identifier)
				b.setMethod(((Identifier)vn.getName()).getName());
			else {
				vn.accept(this);
				b.setComputedMethod(expressions.pop());
			}
		} else {
			fn.getName().accept(this);
			b.setComputedMethod(expressions.pop());
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
		sb.setKind(StatementKind.STATIC);
		for (org.eclipse.php.internal.core.ast.nodes.Expression e : node.expressions()) {
			e.accept(this);
			sb.addExpressions(expressions.pop());
		}
		statements.peek().add(sb.build());
		return false;
	}

	@Override
	public boolean visit(SwitchCase node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		if (node.isDefault())
			b.setKind(boa.types.Ast.Statement.StatementKind.DEFAULT);
		else
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
		node.getBody().accept(this);
		for (Object c : node.catchClauses())
			((CatchClause) c).accept(this);
		if (node.finallyClause() != null)
			node.finallyClause().accept(this);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
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
			b.setKind(boa.types.Ast.Expression.ExpressionKind.BIT_NOT);
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
		b.setKind(ExpressionKind.VARACCESS);
		if (node.isDollared())
			b.setDollarSign(true);
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
	public boolean visit(UseStatement node) {
		for (UseStatementPart usp : node.parts())
			usp.accept(this);
		return false;
	}

	@Override
	public boolean visit(UseStatementPart node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		if (node.getStatementType() == UseStatement.T_CONST)
			b.setKind(boa.types.Ast.Statement.StatementKind.USE_CONSTANT);
		else if (node.getStatementType() == UseStatement.T_FUNCTION)
			b.setKind(boa.types.Ast.Statement.StatementKind.USE_FUNCTION);
		else
			b.setKind(boa.types.Ast.Statement.StatementKind.USE_NAMESPACE);
		b.addNames(node.getName().getName());
		if (node.getAlias() != null)
			b.addNames(node.getAlias().getName());
		statements.peek().add(b.build());
		return false;
	}

	@Override
	public boolean visit(WhileStatement node) {
		Statement.Builder b = Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.WHILE);
		if (node.getCondition() != null) {
			node.getCondition().accept(this);
			b.addConditions(expressions.pop());
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
		if (node.getOperator() == YieldExpression.OP_FROM)
			eb.setHasFrom(true);
		if (node.getKey() != null) {
			node.getKey().accept(this);
			eb.addExpressions(expressions.pop());
		}
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
			eb.addExpressions(expressions.pop());
		}
		expressions.push(eb.build());
		return false;
	}

	@Override
	public boolean visit(FullyQualifiedTraitMethodReference node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(ExpressionKind.METHOD_REFERENCE);
		b.setMethod(node.getFunctionName().getName());
		node.getClassName().accept(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(TraitAlias node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(ExpressionKind.TRAIT_ALIAS);
		if (node.getFunctionName() != null)
			b.setMethod(node.getFunctionName().getName());
		boa.types.Ast.Method.Builder trait = boa.types.Ast.Method.newBuilder();
		if (node.getTraitMethod() instanceof Identifier) {
			trait.setName(((Identifier) node.getTraitMethod()).getName());
		} else {
			node.getTraitMethod().accept(this);
			trait.setComputedName(expressions.pop());
		}
		b.setTrait(trait);
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(TraitAliasStatement node) {
		Statement.Builder b = Statement.newBuilder();
		b.setKind(StatementKind.TRAIT_ALIAS);
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
		if (mod != ClassDeclaration.MODIFIER_NONE) {
			Modifier.Builder mb = null;
			switch (mod) {
			case ClassDeclaration.MODIFIER_ABSTRACT:
				mb = Modifier.newBuilder();
				mb.setKind(Modifier.ModifierKind.ABSTRACT);
				b.addModifiers(mb.build());
				break;
			case ClassDeclaration.MODIFIER_FINAL:
				mb = Modifier.newBuilder();
				mb.setKind(Modifier.ModifierKind.FINAL);
				b.addModifiers(mb.build());
				break;
			}
		}
		b.setKind(boa.types.Ast.TypeKind.TRAIT);
		fields.push(new ArrayList<boa.types.Ast.Variable>());
		methods.push(new ArrayList<boa.types.Ast.Method>());
		declarations.push(new ArrayList<boa.types.Ast.Declaration>());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		for (org.eclipse.php.internal.core.ast.nodes.Statement s : node.getBody().statements())
			s.accept(this);
		for (boa.types.Ast.Variable v : fields.pop())
			b.addFields(v);
		for (boa.types.Ast.Method m : methods.pop())
			b.addMethods(m);
		for (boa.types.Ast.Declaration d : declarations.pop())
			b.addNestedDeclarations(d);
		for (boa.types.Ast.Statement st : statements.pop())
			b.addStatements(st);
		declarations.peek().add(b.build());
		return false;
	}

	@Override
	public boolean visit(TraitPrecedence node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(ExpressionKind.TRAIT_PRECEDENCE);
		node.getMethodReference().accept(this);
		b.addExpressions(expressions.pop());
		for (NamespaceName n : node.getTrList()) {
			n.accept(this);
			b.addExpressions(expressions.pop());
		}
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(TraitPrecedenceStatement node) {
		Statement.Builder b = Statement.newBuilder();
		b.setKind(StatementKind.TRAIT_PRECEDENCE);
		node.getPrecedence().accept(this);
		b.setExpression(expressions.pop());
		statements.peek().add(b.build());
		return false;
	}

	@Override
	public boolean visit(TraitUseStatement node) {
		Statement.Builder b = Statement.newBuilder();
		b.setKind(StatementKind.TRAIT_USE);
		for (NamespaceName n : node.getTraitList())
			b.addNames(n.getName());
		statements.push(new ArrayList<Statement>());
		for (TraitStatement ts : node.getTsList())
			ts.accept(this);
		for (Statement s : statements.pop())
			b.addStatements(s);
		statements.peek().add(b.build());
		return false;
	}
	
}
