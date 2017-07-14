package boa.datagen.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import org.eclipse.php.internal.core.ast.nodes.*;
import org.eclipse.php.internal.core.ast.visitor.AbstractVisitor;

import boa.types.Ast.ASTRoot;
import boa.types.Ast.Expression;
import boa.types.Ast.Method;
import boa.types.Ast.Modifier;
import boa.types.Ast.Namespace;
import boa.types.Ast.PositionInfo;
import boa.types.Ast.Statement;
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
	protected boa.types.Ast.ASTRoot.Builder r;
	
	public PHPVisitor(String src, HashMap<String, Integer> nameIndices) {
		super();
		this.src = src;
		this.nameIndices = nameIndices;
	}

	public  ASTRoot getRoot(Program node) {
		root = node;
		node.accept(this);
		return r.build();
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
		boolean hasNamespace = false;
		for (ASTNode s : node.statements()) {
			if (s instanceof NamespaceDeclaration) {
				namespaces.push(new ArrayList<boa.types.Ast.Namespace>());
				s.accept(this);
				for (boa.types.Ast.Namespace d : namespaces.pop())
					r.addNamespaces(d);
				hasNamespace = true;
			} else if (s instanceof MethodDeclaration) {
				methods.push(new ArrayList<boa.types.Ast.Method>());
				s.accept(this);
				for (boa.types.Ast.Method m : methods.pop())
					b.addMethods(m);
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
		if (!hasNamespace) {
			b.setName("");
			r.addNamespaces(b.build());
		}
		for (Object c : node.comments())
			((Comment) c).accept(this);
		return false;
	}

	@Override	
	public boolean visit(NamespaceName node) {
		throw new RuntimeException("visited unused node NamespaceName");
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
					r.addNamespaces(d); // FIXME
			} else if (s instanceof MethodDeclaration) {
				methods.push(new ArrayList<boa.types.Ast.Method>());
				((MethodDeclaration) s).accept(this);
				for (boa.types.Ast.Method m : methods.pop())
					nb.addMethods(m);
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
				imports.add(imp);
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
		node.getIndex().accept(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(ArrayCreation node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.NEWARRAY);
		for (Object e : node.elements()) {
			((ASTNode)e).accept(this);
			b.addExpressions(expressions.pop());
		}
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(ArrayElement node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);// FIXME
		if (node.getKey() != null){
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
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_ADD);//FIXME
		else if (node.getOperator() == Assignment.OP_MUL_EQUAL)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_MULT);
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(BackTickExpression node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.METHODCALL);//FIXME
		for (org.eclipse.php.internal.core.ast.nodes.Expression e: node.expressions()){
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
			statements.push(new ArrayList<boa.types.Ast.Statement>());
			((ASTNode) s).accept(this);
			for (boa.types.Ast.Statement st : statements.pop())
				b.addStatements(st);
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
			boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
			eb.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
			b.setExpression(eb.build());
		}
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(CastExpression node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.CAST);
		boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
// FIXME		tb.setName( (node.getType()));
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
		if (ex.isDollared()){
			name = "$";
		}
		if (ex.getName() instanceof Identifier){
		name +=	((Identifier) ex.getName()).getName();
		} // FIXME
		vb.setName(name);
		boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
		name = ""; //FIXME php Is Dynamically Typed
		tb.setName(name);
		tb.setKind(boa.types.Ast.TypeKind.OTHER);
		vb.setVariableType(tb.build());
		b.setVariableDeclaration(vb.build());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		for (Object s : node.getBody().statements())
			((ASTNode)s).accept(this);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		list.add(b.build());
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(ConstantDeclaration node) {
		boa.types.Ast.Declaration.Builder b = boa.types.Ast.Declaration.newBuilder();
		b.setKind(boa.types.Ast.TypeKind.OTHER);// VARDECL);
		
		String mod = node.getModifierString();
		
		if (mod.contains("private")){
			Modifier.Builder mb = Modifier.newBuilder();
			mb.setKind(Modifier.ModifierKind.VISIBILITY);
			mb.setVisibility(Modifier.Visibility.PRIVATE);
			b.addModifiers(mb.build());
		}
		if (mod.contains("public")){
			Modifier.Builder mb = Modifier.newBuilder();
			mb.setKind(Modifier.ModifierKind.VISIBILITY);
			mb.setVisibility(Modifier.Visibility.PUBLIC);
			b.addModifiers(mb.build());
		}
		if (mod.contains("protected")){
			Modifier.Builder mb = Modifier.newBuilder();
			mb.setKind(Modifier.ModifierKind.VISIBILITY);
			mb.setVisibility(Modifier.Visibility.PROTECTED);
			b.addModifiers(mb.build());
		}
		if (mod.contains("abstract")){
			Modifier.Builder mb = Modifier.newBuilder();
			mb.setKind(Modifier.ModifierKind.ABSTRACT);
			b.addModifiers(mb.build());
		}
		if (mod.contains("static")){
			Modifier.Builder mb = Modifier.newBuilder();
			mb.setKind(Modifier.ModifierKind.FINAL);
			b.addModifiers(mb.build());
		}
		if (mod.contains("final")){
			Modifier.Builder mb = Modifier.newBuilder();
			mb.setKind(Modifier.ModifierKind.FINAL);
			b.addModifiers(mb.build());
		}
		
		Identifier[] variableNames = (Identifier[])node.names().toArray();
		org.eclipse.php.internal.core.ast.nodes.Expression[] constantValues = (org.eclipse.php.internal.core.ast.nodes.Expression[])node.initializers().toArray();
		for (int i = 0; i < node.names().size(); i++){
			boa.types.Ast.Variable.Builder vb = boa.types.Ast.Variable.newBuilder();
			vb.setName(variableNames[i].getName());
			constantValues[i].accept(this);
			vb.setInitializer(expressions.pop());
			boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
			tb.setName("");
			tb.setKind(boa.types.Ast.TypeKind.OTHER);
			vb.setVariableType(tb.build());
			b.addFields(vb.build());
		}
		declarations.peek().add(b.build());
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
		for (Object d : node.getBody().statements()) {
			if (d instanceof VariableBase) {
				fields.push(new ArrayList<boa.types.Ast.Variable>());
				((ASTNode)d).accept(this);
				for (boa.types.Ast.Variable v : fields.pop())
					b.addFields(v);
			} else if (d instanceof MethodDeclaration) {
				methods.push(new ArrayList<boa.types.Ast.Method>());
				((MethodDeclaration)d).accept(this);
				for (boa.types.Ast.Method m : methods.pop())
					b.addMethods(m);
			} else if (d instanceof org.eclipse.php.internal.core.ast.nodes.Expression) {
				methods.push(new ArrayList<boa.types.Ast.Method>());
				((ASTNode)d).accept(this);
				for (boa.types.Ast.Method m : methods.pop())
					b.addMethods(m);
			} else {
				declarations.push(new ArrayList<boa.types.Ast.Declaration>());
				((ASTNode)d).accept(this);
				for (boa.types.Ast.Declaration nd : declarations.pop())
					b.addNestedDeclarations(nd);
			}
		}
		
		declarations.peek().add(b.build());
		return false;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.NEW);
		boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
		node.getClassName().accept(this);
		b.addExpressions(expressions.pop());
		tb.setKind(boa.types.Ast.TypeKind.CLASS);
		b.setNewType(tb.build());
		for (Object a : node.ctorParams()) {
			((ASTNode)a).accept(this);
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
		b.setKind(Expression.ExpressionKind.OTHER);
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
		b.addExpressions(expressions.pop());
		node.getIfTrue().accept(this);
		b.addExpressions(expressions.pop());
		node.getIfFalse().accept(this);
		b.addExpressions(expressions.pop());
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
		sb.setKind(Statement.StatementKind.OTHER);//FIXME
		Expression.Builder b = Expression.newBuilder();
		b.setKind(Expression.ExpressionKind.VARDECL); 
		Identifier[] variableNames = (Identifier[])node.directiveNames().toArray();
		org.eclipse.php.internal.core.ast.nodes.Expression[] directiveValues = (org.eclipse.php.internal.core.ast.nodes.Expression[])node.directiveValues().toArray();
		for (int i = 0; i < node.directiveNames().size(); i++){
			boa.types.Ast.Variable.Builder vb = boa.types.Ast.Variable.newBuilder();
			vb.setName(variableNames[i].getName());
			directiveValues[i].accept(this);
			vb.setInitializer(expressions.pop());
			boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
			tb.setName("");
			tb.setKind(boa.types.Ast.TypeKind.OTHER);
			vb.setVariableType(tb.build());
			b.addVariableDecls(vb.build());
		}
		sb.setExpression(b.build());
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
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		if (node.getBody() != null) {
			node.getBody().accept(this);
			for (boa.types.Ast.Statement s : statements.pop())
				b.addStatements(s);
		}
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(EchoStatement node) {
		Statement.Builder b = Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(Statement.StatementKind.OTHER);//FIXME
		for (org.eclipse.php.internal.core.ast.nodes.Expression e: node.expressions()){
			e.accept(this);
			b.addInitializations(expressions.pop());
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
//FIXME		b.setVariable(node.getName().getFullyQualifiedName());
		node.getField().accept(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(FieldsDeclaration node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(Expression.ExpressionKind.VARDECL); 
		 org.eclipse.php.internal.core.ast.nodes.Variable[] variableNames = node.getVariableNames();
		org.eclipse.php.internal.core.ast.nodes.Expression[] initialValues = node.getInitialValues();
		for (int i = 0; i < node.getVariableNames().length; i++){
			variableNames[i].accept(this);
			b.addExpressions(expressions.pop());
			initialValues[i].accept(this);
			b.addExpressions(expressions.pop());
		}
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(ForEachStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.FOR);
		if (node.getExpression() != null){
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
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getStatement().accept(this);
		for (Statement s: statements.pop())
			b.addStatements(s);
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(FormalParameter node) {
		Variable.Builder b = Variable.newBuilder();
		List<boa.types.Ast.Variable> list = fields.peek();
		Type.Builder tb = Type.newBuilder();
		if (node.getParameterType() != null && node.getParameterType() instanceof Identifier)
			//FIXME
			tb.setName(((Identifier)node.getParameterType()).getName());
		else
			tb.setName("");
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
		for (org.eclipse.php.internal.core.ast.nodes.Expression e : node.conditions()){
			e.accept(this);
			b.addUpdates(expressions.pop()); //FIXME condition is optional not repeated?
		}
		for (org.eclipse.php.internal.core.ast.nodes.Expression e : node.updaters()) {
			e.accept(this);
			b.addUpdates(expressions.pop());
		}
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getBody().accept(this);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(FunctionDeclaration node) {
		List<boa.types.Ast.Method> list = methods.peek();
		Method.Builder b = Method.newBuilder();
		b.setName(node.getFunctionName().getName());
		for (FormalParameter p : node.formalParameters()) {
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
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(FunctionInvocation node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.METHODCALL);
		FunctionName fn = node.getFunctionName();
		if (fn.getName() instanceof Identifier)
			b.setMethod(((Identifier)node.getFunctionName().getName()).getName());//FIXME
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
		b.setKind(Expression.ExpressionKind.OTHER);//FIXME
		node.getName().accept(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(GlobalStatement node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(Expression.ExpressionKind.VARDECL);
		for (org.eclipse.php.internal.core.ast.nodes.Variable v : node.variables()){
			v.accept(this);
			for (Variable var : fields.pop())
				b.addVariableDecls(var);
		}
		return false;
	}

	@Override
	public boolean visit(GotoLabel node) {
		throw new RuntimeException("visited unused node " + node.getClass());
	}

	@Override
	public boolean visit(GotoStatement node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(Expression.ExpressionKind.OTHER);
		Type.Builder tb = Type.newBuilder();
		tb.setKind(boa.types.Ast.TypeKind.OTHER);
		tb.setName("goto");
		b.setNewType(tb.build());
		b.setLiteral(node.getLabel().getName());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(Identifier node) {
		throw new RuntimeException("visited unused node " + node.getClass());
	}

	@Override
	public boolean visit(IfStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		
		node.getCondition().accept(this);List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.IF);
		b.setExpression(expressions.pop());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getTrueStatement().accept(this);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		if (node.getFalseStatement() != null) {
			statements.push(new ArrayList<boa.types.Ast.Statement>());
			node.getFalseStatement().accept(this);
			for (boa.types.Ast.Statement s : statements.pop())
				b.addStatements(s);
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
		throw new RuntimeException("visited unused node " + node.getClass());
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
			b.setKind(boa.types.Ast.Expression.ExpressionKind.BIT_XOR);//FIXME
		else if (node.getOperator() == InfixExpression.OP_STRING_OR)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.LOGICAL_OR);
		else if (node.getOperator() == InfixExpression.OP_STRING_AND)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.LOGICAL_AND);
		else if (node.getOperator() == InfixExpression.OP_MUL)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_MULT);
		else if (node.getOperator() == InfixExpression.OP_XOR)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.BIT_XOR);
		node.getLeft().accept(this);
		b.addExpressions(expressions.pop());
		node.getRight().accept(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(InLineHtml node) {
		//FIXME
		return false;
	}

	@Override
	public boolean visit(InstanceOfExpression node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.TYPECOMPARE);
		node.getExpression().accept(this);
		b.addExpressions(expressions.pop());
		if (node.getClassName().getName() instanceof Identifier) {
			boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
			tb.setName(((Identifier) node.getClassName().getName()).getName());
			tb.setKind(boa.types.Ast.TypeKind.OTHER);
			b.setNewType(tb.build());
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
			} else if (d instanceof FieldsDeclaration) {
				fields.push(new ArrayList<boa.types.Ast.Variable>());
				((FieldsDeclaration)d).accept(this);
				for (boa.types.Ast.Variable v : fields.pop())
					b.addFields(v);
			} else if (d instanceof org.eclipse.php.internal.core.ast.nodes.Expression) {
				((ASTNode) d).accept(this);
	//FIXME			b.addExpressions(expressions.pop());
			} else {
				statements.push(new ArrayList<boa.types.Ast.Statement>());
				((ASTNode) d).accept(this);
		//		for (boa.types.Ast.Statement st : statements.pop())
	//FIXME				b.addStatements(st);
			}
		}
		declarations.peek().add(b.build());
		return false;
	}

	@Override
	public boolean visit(LambdaFunctionDeclaration node) {
		List<boa.types.Ast.Method> list = methods.peek();
		Method.Builder b = Method.newBuilder();
		b.setName("LambdaFunction");
		for (FormalParameter p : node.formalParameters()) {
			p.accept(this);
			for (Variable v : fields.pop())
				b.addArguments(v);
		}
		for (org.eclipse.php.internal.core.ast.nodes.Expression p : node.lexicalVariables()) {
			p.accept(this);
		}
		if (node.getBody() != null) {
			statements.push(new ArrayList<boa.types.Ast.Statement>());
			node.getBody().accept(this);
			for (boa.types.Ast.Statement s : statements.pop()) {
				b.addStatements(s);
				for (Variable v : fields.pop())
					b.addArguments(v);
			}
		}
		Type.Builder tb = Type.newBuilder();
		String name = "";
		if (node.getReturnType() != null)
			name = node.getReturnType().getName();
		tb.setKind(boa.types.Ast.TypeKind.OTHER);
		tb.setName(name);
		b.setReturnType(tb.build());
		list.add(b.build());
		return false;
	}

	@Override	
	public boolean visit(ListVariable node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(Expression.ExpressionKind.VARDECL);
		for (org.eclipse.php.internal.core.ast.nodes.Expression v : node.variables()){
			if (v instanceof org.eclipse.php.internal.core.ast.nodes.Variable && 
					((org.eclipse.php.internal.core.ast.nodes.Variable)v).getName() instanceof Identifier){
				Variable.Builder vb = Variable.newBuilder();
				vb.setName(((Identifier)((org.eclipse.php.internal.core.ast.nodes.Variable) v).getName()).getName());
				boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
				tb.setName("");
				tb.setKind(boa.types.Ast.TypeKind.OTHER);
				vb.setVariableType(tb.build());
				b.addVariableDecls(vb.build());
			}else {
				v.accept(this);
				b.addExpressions(expressions.pop());
			}
		}
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		List<boa.types.Ast.Method> list = methods.peek();
		Method.Builder b = Method.newBuilder();
		String mod = node.getModifierString();
		
		if (mod.contains("private")){
			Modifier.Builder mb = Modifier.newBuilder();
			mb.setKind(Modifier.ModifierKind.VISIBILITY);
			mb.setVisibility(Modifier.Visibility.PRIVATE);
			b.addModifiers(mb.build());
		}
		if (mod.contains("public")){
			Modifier.Builder mb = Modifier.newBuilder();
			mb.setKind(Modifier.ModifierKind.VISIBILITY);
			mb.setVisibility(Modifier.Visibility.PUBLIC);
			b.addModifiers(mb.build());
		}
		if (mod.contains("protected")){
			Modifier.Builder mb = Modifier.newBuilder();
			mb.setKind(Modifier.ModifierKind.VISIBILITY);
			mb.setVisibility(Modifier.Visibility.PROTECTED);
			b.addModifiers(mb.build());
		}
		if (mod.contains("abstract")){
			Modifier.Builder mb = Modifier.newBuilder();
			mb.setKind(Modifier.ModifierKind.ABSTRACT);
			b.addModifiers(mb.build());
		}
		if (mod.contains("static")){
			Modifier.Builder mb = Modifier.newBuilder();
			mb.setKind(Modifier.ModifierKind.FINAL);
			b.addModifiers(mb.build());
		}
		if (mod.contains("final")){
			Modifier.Builder mb = Modifier.newBuilder();
			mb.setKind(Modifier.ModifierKind.FINAL);
			b.addModifiers(mb.build());
		}
		
		b.setName(node.getFunction().getFunctionName().getName());
		for (FormalParameter p : node.getFunction().formalParameters()) {
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
			b.setMethod(((Identifier)node.getMethod().getFunctionName().getName()).getName());//FIXME
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
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);//FIXME
		node.getVariable().accept(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(Quote node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(Expression.ExpressionKind.LITERAL);
		Type.Builder tb = Type.newBuilder();
		tb.setName(Quote.getType(node.getQuoteType()));
		tb.setKind(TypeKind.OTHER);
		b.setNewType(tb.build());
		String quote = "";
		for( org.eclipse.php.internal.core.ast.nodes.Expression e : node.expressions()){
			if (e instanceof Identifier)
				quote += ((Identifier)e).getName();
			else{
				e.accept(this);
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
		b.setKind(Expression.ExpressionKind.OTHER);//FIXME Add Reference to ExpressionKind?
		node.getExpression().accept(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(ReflectionVariable node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(ExpressionKind.VARACCESS);
		if (node.getName() instanceof Identifier){
			Variable.Builder vb = Variable.newBuilder();
			Type.Builder tb = Type.newBuilder();
			tb.setKind(TypeKind.OTHER);
			tb.setName("");
			vb.setVariableType(tb.build());
			vb.setName(((Identifier)node.getName()).getName());
			b.addVariableDecls(vb.build());
		} else{
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
		Type.Builder tb = Type.newBuilder();
		tb.setKind(TypeKind.OTHER);
		tb.setName(Scalar.getType(node.getScalarType()));
		b.setNewType(tb.build());
		if(node.getScalarType() != Scalar.TYPE_UNKNOWN)
			b.setLiteral(node.getStringValue());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(SingleFieldDeclaration node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(ExpressionKind.VARDECL);
		if (node.getName().getName() instanceof Identifier){
			Variable.Builder vb = Variable.newBuilder();
			Type.Builder tb = Type.newBuilder();
			tb.setKind(TypeKind.OTHER);
			tb.setName("");
			vb.setVariableType(tb.build());
			vb.setName(((Identifier)node.getName().getName()).getName());
			node.getValue().accept(this);
			vb.setInitializer(expressions.pop());
		} else {
			node.getName().accept(this);
			b.addExpressions(expressions.pop());
			node.getValue();
			b.addExpressions(expressions.pop());
		}
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
			b.setVariable(((Identifier)node.getField().getName()).getName());
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
		b.addExpressions(expressions.pop());
		if (fn.getName() instanceof Identifier)
			b.setMethod(((Identifier)node.getMethod().getFunctionName().getName()).getName());//FIXME
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
	public boolean visit(StaticStatement node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(ExpressionKind.VARDECL);//FIXME
		Modifier.Builder mb = Modifier.newBuilder();
		mb.setKind(ModifierKind.STATIC);
		Modifier mod = mb.build();
		Type.Builder tb = Type.newBuilder();
		tb.setKind(TypeKind.OTHER);
		tb.setName("");
		Type type = tb.build();
		for (org.eclipse.php.internal.core.ast.nodes.Expression e : node.expressions()){
			Variable.Builder vb = Variable.newBuilder();
			vb.setVariableType(type);
			vb.addModifiers(mod);
			if (e instanceof org.eclipse.php.internal.core.ast.nodes.Variable){
				org.eclipse.php.internal.core.ast.nodes.Expression name = 
						((org.eclipse.php.internal.core.ast.nodes.Variable)e).getName();
				if (name instanceof Identifier)
					vb.setName(((Identifier)name).getName());
				else {
					vb.setName(""); //FIXME
					name.accept(this);
					b.addExpressions(expressions.pop());
				}
			} else {
				assert e instanceof Assignment;
				Assignment assign = (Assignment)e;
				VariableBase var = assign.getLeftHandSide();
				if ( var instanceof org.eclipse.php.internal.core.ast.nodes.Variable && 
						((org.eclipse.php.internal.core.ast.nodes.Variable)var).getName()
							instanceof Identifier){
						vb.setName(((Identifier)((org.eclipse.php.internal.core.ast.nodes.Variable)var).
								getName()).getName());
					} else {
						vb.setName("");//FIXME variable names are not always Identifier
						var.accept(this);
						b.addExpressions(expressions.pop());
				}
				assign.getRightHandSide().accept(this);
				vb.setInitializer(expressions.pop());
			}
			b.addVariableDecls(vb.build());
		}
		expressions.push(b.build());
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
		for (org.eclipse.php.internal.core.ast.nodes.Statement s : node.actions())
			s.accept(this);
		for (Statement st : statements.pop())
			b.addStatements(st);
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
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getBody().accept(this);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
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
			((CatchClause)c).accept(this);
		if (node.finallyClause()  != null) {
			node.finallyClause().accept(this);
		}
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
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getBody().accept(this);
		for (Statement s : statements.pop())
			b.addStatements(s);
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
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);//FIXME
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
		b.setKind(ExpressionKind.VARACCESS); //FIXME could be a decleration
		if (node.getName() instanceof Identifier) {
			Variable.Builder vb = Variable.newBuilder();
			Type.Builder tb = Type.newBuilder();
			tb.setKind(TypeKind.OTHER);
			tb.setName("");
			vb.setVariableType(tb.build());
			vb.setName(((Identifier)node.getName()).getName());
			b.addVariableDecls(vb.build());
		} else {
			node.getName().accept(this);
			b.addExpressions(expressions.pop());
		}
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(UseStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.OTHER);//FIXME
		for (UseStatementPart usp : node.parts()){
			usp.accept(this);
			b.addInitializations(expressions.pop());
		}
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(UseStatementPart node) {
		Expression.Builder b = Expression.newBuilder();
		b.setKind(ExpressionKind.OTHER);// FIXME
		Variable.Builder vb = Variable.newBuilder();
		Type.Builder tb = Type.newBuilder();
		tb.setKind(TypeKind.OTHER);
		tb.setName("NamespaceName");
		vb.setVariableType(tb.build());
		vb.setName(node.getName().getName());
		b.addVariableDecls(vb.build());
		if (node.getAlias() != null) {
			tb.setName("Alias");
			vb.setVariableType(tb.build());
			vb.setName(node.getAlias().getName());
			b.addVariableDecls(vb.build());
		}
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
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getBody().accept(this);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		list.add(b.build());
		return false;
	}


	@Override
	public boolean visit(YieldExpression node) {
		boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.OTHER);
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
		throw new RuntimeException("visited unused node " + node.getClass());
	}

	@Override
	public boolean visit(TraitAlias node) {
		throw new RuntimeException("visited unused node " + node.getClass());
	}

	@Override
	public boolean visit(TraitAliasStatement node) {
		throw new RuntimeException("visited unused node " + node.getClass());
	}

	@Override
	public boolean visit(TraitDeclaration node) {
		throw new RuntimeException("visited unused node " + node.getClass());
	}

	@Override
	public boolean visit(TraitPrecedence node) {
		throw new RuntimeException("visited unused node " + node.getClass());
	}

	@Override
	public boolean visit(TraitPrecedenceStatement node) {
		throw new RuntimeException("visited unused node " + node.getClass());
	}

	@Override
	public boolean visit(TraitUseStatement node) {
		throw new RuntimeException("visited unused node " + node.getClass());
	}
}
