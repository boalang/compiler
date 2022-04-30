/*
 * Copyright 2017-2022, Hridesh Rajan, Robert Dyer, Hoan Nguyen, Farheen Sultana, Huaiyao Ma
 *                 Iowa State University of Science and Technology
 *                 Bowling Green State University
 *                 and University of Nebraska Board of Regents
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package boa.datagen.util;

import static boa.datagen.util.JavaASTUtil.getFullyQualifiedName;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jdt.core.dom.*;

import boa.types.Ast;
import boa.types.Ast.Declaration;
import boa.types.Ast.Method;
import boa.types.Ast.Modifier;
import boa.types.Ast.Namespace;
import boa.types.Ast.PositionInfo;
import boa.types.Ast.Type;
import boa.types.Ast.TypeKind;
import boa.types.Ast.Variable;

/**
 * @author rdyer
 * @author hoan
 * @author farheen
 * @author huaiyao
 */
public class JavaVisitor extends ASTVisitor {
	public static final String PROPERTY_INDEX = "i";
	public static final int JLS1 = 1;
	public static final int JLS2 = 2;
	public static final int JLS3 = 3;
	public static final int JLS7 = 7;
	public static final int JLS8 = 8;
	public static final int JLS9 = 9;
	public static final int JLS10 = 10;
	public static final int JLS11 = 11;
	public static final int JLS12 = 12;
	public static final int JLS13 = 13;
	public static final int JLS14 = 14;
	public static final int JLS15 = 15;
	public static final int JLS16 = 16;
	public static final int SOURCE_JAVA_ERROR = 999;

	protected CompilationUnit root = null;
	protected PositionInfo.Builder pos = null;
	protected String src = null;
	protected Map<String, Integer> declarationFile;
	protected Map<String, Integer> declarationNode;

	protected Namespace.Builder b = Namespace.newBuilder();

	protected List<boa.types.Ast.Comment> comments = new ArrayList<boa.types.Ast.Comment>();
	protected Stack<List<boa.types.Ast.Declaration>> declarations = new Stack<List<boa.types.Ast.Declaration>>();
	protected Stack<boa.types.Ast.Modifier> modifiers = new Stack<boa.types.Ast.Modifier>();
	protected Stack<boa.types.Ast.Expression> expressions = new Stack<boa.types.Ast.Expression>();
	protected Stack<List<boa.types.Ast.Variable>> fields = new Stack<List<boa.types.Ast.Variable>>();
	protected Stack<List<boa.types.Ast.Method>> methods = new Stack<List<boa.types.Ast.Method>>();
	protected Stack<List<boa.types.Ast.Statement>> statements = new Stack<List<boa.types.Ast.Statement>>();

	protected int astLevel = JLS2;

	public int getAstLevel() {
		return astLevel;
	}

	public void setAstLevel(final int astLevel) {
		if (this.astLevel < astLevel)
			this.astLevel = astLevel;
	}

	public JavaVisitor(final String src) {
		super();
		this.src = src;
	}

	public JavaVisitor(final String src, final Map<String, Integer> declarationFile, final Map<String, Integer> declarationNode) {
		this(src);
		this.declarationFile = declarationFile;
		this.declarationNode = declarationNode;
	}

	public Namespace getNamespaces(final CompilationUnit node) {
		root = node;
		node.accept(this);
		return b.build();
	}

	public List<boa.types.Ast.Comment> getComments() {
		return comments;
	}

	public boa.types.Ast.Expression getExpression() {
		return expressions.pop();
	}

	// builds a Position message for every node and stores in the field pos
	//public void preVisit(ASTNode node) { buildPosition(node); }

	@Override
	public boolean visit(final CompilationUnit node) {
		final PackageDeclaration pkg = node.getPackage();
		if (pkg == null) {
			b.setName("");
		} else {
			b.setName(pkg.getName().getFullyQualifiedName());
			if (!pkg.annotations().isEmpty() || pkg.getJavadoc() != null)
				setAstLevel(JLS3);

			b.addAllModifiers(visitAnnotationsList(pkg.annotations()));
		}
		for (final Object i : node.imports()) {
			final ImportDeclaration id = (ImportDeclaration) i;
			String imp = "";
			if (id.isStatic()) {
				setAstLevel(JLS3);
				imp += "static ";
			}
			imp += id.getName().getFullyQualifiedName();
			if (id.isOnDemand())
				imp += ".*";
			b.addImports(imp);
		}

		if (node.getModule() != null)
			node.getModule().accept(this);

		for (final Object t : node.types()) {
			declarations.push(new ArrayList<boa.types.Ast.Declaration>());
			((AbstractTypeDeclaration) t).accept(this);
			for (final boa.types.Ast.Declaration d : declarations.pop())
				b.addDeclarations(d);
		}
		for (final Object c : node.getCommentList())
			((org.eclipse.jdt.core.dom.Comment) c).accept(this);
		return false;
	}

	@Override
	public boolean visit(final BlockComment node) {
		final boa.types.Ast.Comment.Builder b = boa.types.Ast.Comment.newBuilder();
		buildPosition(node);
		b.setPosition(pos.build());
		b.setKind(boa.types.Ast.Comment.CommentKind.BLOCK);
		b.setValue(src.substring(node.getStartPosition(), node.getStartPosition() + node.getLength()));
		comments.add(b.build());
		return false;
	}

	@Override
	public boolean visit(final LineComment node) {
		final boa.types.Ast.Comment.Builder b = boa.types.Ast.Comment.newBuilder();
		buildPosition(node);
		b.setPosition(pos.build());
		b.setKind(boa.types.Ast.Comment.CommentKind.LINE);
		b.setValue(src.substring(node.getStartPosition(), node.getStartPosition() + node.getLength()));
		comments.add(b.build());
		return false;
	}

	@Override
	public boolean visit(final Javadoc node) {
		final boa.types.Ast.Comment.Builder b = boa.types.Ast.Comment.newBuilder();
		buildPosition(node);
		b.setPosition(pos.build());
		b.setKind(boa.types.Ast.Comment.CommentKind.DOC);
		b.setValue(src.substring(node.getStartPosition(), node.getStartPosition() + node.getLength()));
		comments.add(b.build());
		for (Iterator<?> it = node.tags().iterator(); it.hasNext();) {
			final ASTNode e = (ASTNode) it.next();
			e.accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(final TagElement node) {
		setAstLevel(JLS2);

		if (node.getTagName() != null) {
			final String name = node.getTagName();
			if (name.equals("@literal") || name.equals("@code"))
				setAstLevel(JLS3);
		}
		for (Iterator<?> it = node.fragments().iterator(); it.hasNext();) {
			final ASTNode e = (ASTNode) it.next();
			e.accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(final TextElement node) {
		return false;
	}

	@Override
	public boolean visit(final MemberRef node) {
		setAstLevel(JLS2);

		return false;
	}

	@Override
	public boolean visit(final MethodRef node) {
		setAstLevel(JLS2);

		for (Iterator<?> it = node.parameters().iterator(); it.hasNext();) {
			MethodRefParameter e = (MethodRefParameter) it.next();
			e.accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(final MethodRefParameter node) {
		typeName(node.getType());
		if (node.isVarargs())
			setAstLevel(JLS3);
		return false;
	}

	//////////////////////////////////////////////////////////////
	// Type Declarations

	@Override
	public boolean visit(final TypeDeclaration node) {
		final boa.types.Ast.Declaration.Builder b = boa.types.Ast.Declaration.newBuilder();
		b.setName(node.getName().getIdentifier());
		b.setFullyQualifiedName(getFullyQualifiedName(node));
		setDeclaringClass(b, node.resolveBinding());
		if (node.isInterface())
			b.setKind(boa.types.Ast.TypeKind.INTERFACE);
		else
			b.setKind(boa.types.Ast.TypeKind.CLASS);
		b.addAllModifiers(buildModifiers(node.modifiers()));
		if (!node.typeParameters().isEmpty()) {
			setAstLevel(JLS3);

			for (final Object t : node.typeParameters()) {
				if (!((TypeParameter) t).modifiers().isEmpty())
					setAstLevel(JLS8);

				final boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
				String name = ((TypeParameter) t).getName().getFullyQualifiedName();
				String bounds = "";
				for (final Object o : ((TypeParameter) t).typeBounds()) {
					if (bounds.length() > 0)
						bounds += " & ";
					bounds += typeName((org.eclipse.jdt.core.dom.Type) o);
				}
				if (bounds.length() > 0)
					name = name + " extends " + bounds;
				tb.setName(name);
				tb.setKind(boa.types.Ast.TypeKind.GENERIC);
				setTypeBinding(tb, ((TypeParameter) t).getName());
				b.addGenericParameters(tb.build());
			}
		}
		if (node.getSuperclassType() != null) {
			final boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
			tb.setName(typeName(node.getSuperclassType()));
			tb.setKind(boa.types.Ast.TypeKind.CLASS);
			setTypeBinding(tb, node.getSuperclassType());
			b.addParents(tb.build());
		}
		for (final Object t : node.superInterfaceTypes()) {
			final boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
			tb.setName(typeName((org.eclipse.jdt.core.dom.Type) t));
			tb.setKind(boa.types.Ast.TypeKind.INTERFACE);
			setTypeBinding(tb, (org.eclipse.jdt.core.dom.Type) t);
			b.addParents(tb.build());
		}
		for (final Object d : node.bodyDeclarations()) {
			if (d instanceof FieldDeclaration) {
				fields.push(new ArrayList<boa.types.Ast.Variable>());
				((FieldDeclaration) d).accept(this);
				for (final boa.types.Ast.Variable v : fields.pop())
					b.addFields(v);
			} else if (d instanceof MethodDeclaration) {
				methods.push(new ArrayList<boa.types.Ast.Method>());
				((MethodDeclaration) d).accept(this);
				for (final boa.types.Ast.Method m : methods.pop()) {
					if (b.getKind().equals(boa.types.Ast.TypeKind.INTERFACE)) {
						for (final Modifier mod : m.getModifiersList()) {
							if (mod.getKind().equals(boa.types.Ast.Modifier.ModifierKind.STATIC))
								setAstLevel(JLS8);
						}
					}

					b.addMethods(m);
				}
			} else if (d instanceof Initializer) {
				methods.push(new ArrayList<boa.types.Ast.Method>());
				((Initializer) d).accept(this);
				for (final boa.types.Ast.Method m : methods.pop())
					b.addMethods(m);
			} else {
				declarations.push(new ArrayList<boa.types.Ast.Declaration>());
				((BodyDeclaration) d).accept(this);
				for (final boa.types.Ast.Declaration nd : declarations.pop())
					b.addNestedDeclarations(nd);
			}
		}
		declarations.peek().add(b.build());
		return false;
	}

	@Override
	public boolean visit(final AnonymousClassDeclaration node) {
		final boa.types.Ast.Declaration.Builder b = boa.types.Ast.Declaration.newBuilder();
		b.setName("");
		b.setKind(boa.types.Ast.TypeKind.ANONYMOUS);
		// b.setFullyQualifiedName(getFullyQualifiedName(node));
		for (final Object d : node.bodyDeclarations()) {
			if (d instanceof FieldDeclaration) {
				fields.push(new ArrayList<boa.types.Ast.Variable>());
				((FieldDeclaration) d).accept(this);
				for (final boa.types.Ast.Variable v : fields.pop())
					b.addFields(v);
			} else if (d instanceof MethodDeclaration) {
				methods.push(new ArrayList<boa.types.Ast.Method>());
				((MethodDeclaration) d).accept(this);
				for (final boa.types.Ast.Method m : methods.pop())
					b.addMethods(m);
			} else if (d instanceof Initializer) {
				methods.push(new ArrayList<boa.types.Ast.Method>());
				((Initializer) d).accept(this);
				for (final boa.types.Ast.Method m : methods.pop())
					b.addMethods(m);
			} else {
				declarations.push(new ArrayList<boa.types.Ast.Declaration>());
				((BodyDeclaration) d).accept(this);
				for (final boa.types.Ast.Declaration nd : declarations.pop())
					b.addNestedDeclarations(nd);
			}
		}
		declarations.peek().add(b.build());
		return false;
	}

	@Override
	public boolean visit(final EnumDeclaration node) {
		setAstLevel(JLS3);

		final boa.types.Ast.Declaration.Builder b = boa.types.Ast.Declaration.newBuilder();
		b.setName(node.getName().getIdentifier());
		b.setKind(boa.types.Ast.TypeKind.ENUM);
		b.setFullyQualifiedName(getFullyQualifiedName(node));
		setDeclaringClass(b, node.resolveBinding());
		for (final Object c : node.enumConstants()) {
			fields.push(new ArrayList<boa.types.Ast.Variable>());
			((EnumConstantDeclaration) c).accept(this);
			for (final boa.types.Ast.Variable v : fields.pop())
				b.addFields(v);
		}
		for (final Object t : node.superInterfaceTypes()) {
			final boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
			tb.setName(typeName((org.eclipse.jdt.core.dom.Type) t));
			tb.setKind(boa.types.Ast.TypeKind.INTERFACE);
			setTypeBinding(tb, (org.eclipse.jdt.core.dom.Type) t);
			b.addParents(tb.build());
		}
		for (final Object d : node.bodyDeclarations()) {
			if (d instanceof FieldDeclaration) {
				fields.push(new ArrayList<boa.types.Ast.Variable>());
				((FieldDeclaration) d).accept(this);
				for (final boa.types.Ast.Variable v : fields.pop())
					b.addFields(v);
			} else if (d instanceof MethodDeclaration) {
				methods.push(new ArrayList<boa.types.Ast.Method>());
				((MethodDeclaration) d).accept(this);
				for (final boa.types.Ast.Method m : methods.pop())
					b.addMethods(m);
			} else if (d instanceof Initializer) {
				methods.push(new ArrayList<boa.types.Ast.Method>());
				((Initializer) d).accept(this);
				for (final boa.types.Ast.Method m : methods.pop())
					b.addMethods(m);
			} else {
				declarations.push(new ArrayList<boa.types.Ast.Declaration>());
				((BodyDeclaration) d).accept(this);
				for (final boa.types.Ast.Declaration nd : declarations.pop())
					b.addNestedDeclarations(nd);
			}
		}
		declarations.peek().add(b.build());
		return false;
	}

	@Override
	public boolean visit(final AnnotationTypeDeclaration node) {
		setAstLevel(JLS3);

		final boa.types.Ast.Declaration.Builder b = boa.types.Ast.Declaration.newBuilder();
		b.setName(node.getName().getFullyQualifiedName());
		b.setKind(boa.types.Ast.TypeKind.ANNOTATION);
		b.setFullyQualifiedName(getFullyQualifiedName(node));
		setDeclaringClass(b, node.resolveBinding());
		b.addAllModifiers(buildModifiers(node.modifiers()));
		for (final Object d : node.bodyDeclarations()) {
			if (d instanceof FieldDeclaration) {
				fields.push(new ArrayList<boa.types.Ast.Variable>());
				((FieldDeclaration) d).accept(this);
				for (final boa.types.Ast.Variable v : fields.pop())
					b.addFields(v);
			} else if (d instanceof Initializer) {
				methods.push(new ArrayList<boa.types.Ast.Method>());
				((Initializer) d).accept(this);
				for (final boa.types.Ast.Method m : methods.pop())
					b.addMethods(m);
			} else if (d instanceof AnnotationTypeMemberDeclaration) {
				methods.push(new ArrayList<boa.types.Ast.Method>());
				((AnnotationTypeMemberDeclaration) d).accept(this);
				for (final boa.types.Ast.Method m : methods.pop())
					b.addMethods(m);
			} else if (d instanceof MethodDeclaration) {
				methods.push(new ArrayList<boa.types.Ast.Method>());
				((MethodDeclaration) d).accept(this);
				for (final boa.types.Ast.Method m : methods.pop())
					b.addMethods(m);
			} else {
				declarations.push(new ArrayList<boa.types.Ast.Declaration>());
				((BodyDeclaration) d).accept(this);
				for (final boa.types.Ast.Declaration nd : declarations.pop())
					b.addNestedDeclarations(nd);
			}
		}
		declarations.peek().add(b.build());
		return false;
	}

	//////////////////////////////////////////////////////////////
	// Utilities

	protected void setDeclaringClass(final Declaration.Builder db, final ITypeBinding binding) {
		if (declarationNode == null || binding == null)
			return;
		ITypeBinding tb = binding.getDeclaringClass();
		if (tb != null) {
			if (tb.getTypeDeclaration() != null)
				tb = tb.getTypeDeclaration();
			final String key = tb.getKey();
			final Integer index = declarationNode.get(key);
			if (index != null)
				db.setDeclaringType(index);
		}
	}

	protected void setDeclaringClass(final Method.Builder b, final IMethodBinding binding) {
		if (declarationNode == null || binding == null)
			return;
		ITypeBinding tb = binding.getDeclaringClass();
		if (tb == null)
			return;
		if (tb.getTypeDeclaration() != null)
			tb = tb.getTypeDeclaration();
		final String key = tb.getKey();
		final Integer index = declarationNode.get(key);
		if (index != null)
			b.setDeclaringType(index);
	}

	protected void setDeclaringClass(final Variable.Builder b, final IVariableBinding binding) {
		if (declarationNode == null || binding == null)
			return;
		ITypeBinding tb = binding.getDeclaringClass();
		if (tb == null)
			return;
		if (tb.getTypeDeclaration() != null)
			tb = tb.getTypeDeclaration();
		final String key = tb.getKey();
		final Integer index = declarationNode.get(key);
		if (index != null)
			b.setDeclaringType(index);
	}

	protected void setTypeBinding(final boa.types.Ast.Type.Builder b, final org.eclipse.jdt.core.dom.Type type) {
		ITypeBinding tb = type.resolveBinding();
		if (tb != null) {
			if (tb.getTypeDeclaration() != null)
				tb = tb.getTypeDeclaration();

			if (type.isVar() && !tb.getQualifiedName().equals("java.lang.var")) {
				setAstLevel(JLS10);
				b.setKind(boa.types.Ast.TypeKind.INFERRED);
			} else if (tb.isClass()) {
				b.setKind(boa.types.Ast.TypeKind.CLASS);
			} else if (tb.isInterface()) {
				b.setKind(boa.types.Ast.TypeKind.INTERFACE);
			} else if (tb.isEnum()) {
				b.setKind(boa.types.Ast.TypeKind.ENUM);
			} else if (tb.isAnnotation()) {
				b.setKind(boa.types.Ast.TypeKind.ANNOTATION);
			} else if (tb.isAnonymous()) {
				b.setKind(boa.types.Ast.TypeKind.ANONYMOUS);
			} else if (tb.isPrimitive()) {
				b.setKind(boa.types.Ast.TypeKind.PRIMITIVE);
			} else if (tb.isArray()) {
				b.setKind(boa.types.Ast.TypeKind.ARRAY);
			} else if (tb.isRecord()) {
				b.setKind(boa.types.Ast.TypeKind.IMMUTABLE);
			} else {
				b.setKind(boa.types.Ast.TypeKind.OTHER);
			}
			if (!tb.isPrimitive()) {
				String name = "";
				try {
					name = tb.getQualifiedName();
				} catch (final java.lang.NullPointerException e) {
				}
				b.setFullyQualifiedName(name);
				if (declarationFile != null && !tb.isArray()) {
					final String key = tb.getKey();
					final Integer index = declarationFile.get(key);
					if (index != null) {
						b.setDeclarationFile(index);
						b.setDeclaration(declarationNode.get(key));
					}
				}
			}
		}
	}

	protected void setTypeBinding(final boa.types.Ast.Type.Builder b, final org.eclipse.jdt.core.dom.Expression e) {
		ITypeBinding tb = e.resolveTypeBinding();
		if (tb != null) {
			if (tb.getTypeDeclaration() != null)
				tb = tb.getTypeDeclaration();
			if (tb.isClass())
				b.setKind(boa.types.Ast.TypeKind.CLASS);
			else if (tb.isInterface())
				b.setKind(boa.types.Ast.TypeKind.INTERFACE);
			else if (tb.isEnum())
				b.setKind(boa.types.Ast.TypeKind.ENUM);
			else if (tb.isAnnotation())
				b.setKind(boa.types.Ast.TypeKind.ANNOTATION);
			else if (tb.isAnonymous())
				b.setKind(boa.types.Ast.TypeKind.ANONYMOUS);
			else if (tb.isPrimitive())
				b.setKind(boa.types.Ast.TypeKind.PRIMITIVE);
			else if (tb.isArray())
				b.setKind(boa.types.Ast.TypeKind.ARRAY);
			else if (tb.isRecord())
				b.setKind(boa.types.Ast.TypeKind.IMMUTABLE);
			else
				b.setKind(boa.types.Ast.TypeKind.OTHER);
			if (!tb.isPrimitive()) {
				String name = "";
				try {
					name = tb.getName();
				} catch (final Exception ex) {
					System.err.println("Error getting type name while visiting java file");
					ex.printStackTrace();
				}
				b.setFullyQualifiedName(name);
				if (declarationFile != null && !tb.isArray()) {
					final String key = tb.getKey();
					final Integer index = declarationFile.get(key);
					if (index != null) {
						b.setDeclarationFile(index);
						b.setDeclaration(declarationNode.get(key));
					}
				}
			}
		}
	}

	protected Type buildType(ITypeBinding itb) {
		if (itb.getTypeDeclaration() != null)
			itb = itb.getTypeDeclaration();
		final boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
		String name = "";
		try {
			name = itb.getName();
		} catch (final Exception e) {
			System.err.println("Error getting type name while visiting java file");
			e.printStackTrace();
		}
		tb.setName(name);
		if (itb.isClass())
			tb.setKind(boa.types.Ast.TypeKind.CLASS);
		else if (itb.isInterface())
			tb.setKind(boa.types.Ast.TypeKind.INTERFACE);
		else if (itb.isEnum())
			tb.setKind(boa.types.Ast.TypeKind.ENUM);
		else if (itb.isAnnotation())
			tb.setKind(boa.types.Ast.TypeKind.ANNOTATION);
		else if (itb.isAnonymous())
			tb.setKind(boa.types.Ast.TypeKind.ANONYMOUS);
		else if (itb.isPrimitive())
			tb.setKind(boa.types.Ast.TypeKind.PRIMITIVE);
		else if (itb.isArray())
			tb.setKind(boa.types.Ast.TypeKind.ARRAY);
		else if (itb.isRecord())
			tb.setKind(boa.types.Ast.TypeKind.IMMUTABLE);
		else
			tb.setKind(boa.types.Ast.TypeKind.OTHER);

		if (!itb.isPrimitive()) {
			name = "";
			try {
				name = itb.getQualifiedName();
			} catch (final java.lang.NullPointerException e) {
				System.err.println("Error getting qualified type name while visiting java file");
				e.printStackTrace();
			}
			tb.setFullyQualifiedName(name);
			if (declarationFile != null && !itb.isArray()) {
				final String key = itb.getKey();
				final Integer index = declarationFile.get(key);
				if (index != null) {
					tb.setDeclarationFile(index);
					tb.setDeclaration(declarationNode.get(key));
				}
			}
		}

		// Example scenario
		// var t1 = new var();
		// var t2 = t1;
		try {
			name = "";
			name = itb.getQualifiedName();
			if (itb.getName().equals("var") && !name.equals("java.lang.var")) {
				setAstLevel(JLS10);
				tb.setKind(boa.types.Ast.TypeKind.INFERRED);
			}
		} catch (final java.lang.NullPointerException e) {
				System.err.println("Error getting qualified type name while visiting java file");
				e.printStackTrace();
		}

		return tb.build();
	}

	protected void buildPosition(final ASTNode node) {
		pos = PositionInfo.newBuilder();
		final int start = node.getStartPosition();
		final int length = node.getLength();
		pos.setStartPos(start);
		pos.setLength(length);
		pos.setStartLine(root.getLineNumber(start));
		pos.setStartCol(root.getColumnNumber(start));
		pos.setEndLine(root.getLineNumber(start + length));
		pos.setEndCol(root.getColumnNumber(start + length));
	}

	private List<Modifier> buildModifiers(final List<?> modifiers) {
		final List<Modifier> ms = new ArrayList<Modifier>();
		final Set<String> names = new HashSet<String>();
		for (final Object m : modifiers) {
			if (((IExtendedModifier) m).isAnnotation()) {
				setAstLevel(JLS3);
				final Annotation annot = (Annotation) m;
				final String name = annot.getTypeName().getFullyQualifiedName();
				if (names.contains(name))
					setAstLevel(JLS8);
				else
					names.add(name);
				annot.accept(this);
			} else {
				((org.eclipse.jdt.core.dom.Modifier) m).accept(this);
			}
			ms.add(this.modifiers.pop());
		}
		return ms;
	}

	private void visitTypeAnnotations(final AnnotatableType t) {
		if (!t.annotations().isEmpty())
			setAstLevel(JLS8);
		// FIXME
		// visitAnnotationsList(node.annotations());
	}

	private List<Modifier> visitAnnotationsList(final List<?> annotations) {
		final List<Modifier> ms = new ArrayList<Modifier>();
		final Set<String> names = new HashSet<String>();
		for (final Object a : annotations) {
			final Annotation annot = (Annotation) a;
			final String name = annot.getTypeName().getFullyQualifiedName();
			if (names.contains(name))
				setAstLevel(JLS8);
			else
				names.add(name);
			annot.accept(this);
			ms.add(modifiers.pop());
		}

		return ms;
	}

	private Variable.Builder build(final SingleVariableDeclaration svd, final TypeKind kind) {
		final Variable.Builder vb = Variable.newBuilder();
		vb.setName(svd.getName().getFullyQualifiedName());
		if (svd.isVarargs()) {
			setAstLevel(JLS3);
			if (!svd.varargsAnnotations().isEmpty())
				setAstLevel(JLS8);
		}
		vb.addAllModifiers(buildModifiers(svd.modifiers()));
		final boa.types.Ast.Type.Builder tp = boa.types.Ast.Type.newBuilder();
		String name = typeName(svd.getType());
		tp.setKind(kind);

		if (svd.getInitializer() != null) {
			svd.getInitializer().accept(this);
			vb.setInitializer(expressions.pop());

			if (svd.getType().isVar() && !svd.getInitializer().getClass().getName().equals("var")) {
				setAstLevel(JLS10);
				tp.setKind(TypeKind.INFERRED);
			}
		} else {
			if (svd.getType().isVar()){
				setAstLevel(JLS10);
				tp.setKind(TypeKind.INFERRED);
			}
		}

		// FIXME process extra dimensions in JLS 8
		visitDimensions(svd.extraDimensions());
		for (int i = 0; i < svd.extraDimensions().size(); i++)
			name += "[]";
		if (svd.isVarargs())
			name += "...";
		tp.setName(name);

		setTypeBinding(tp, svd.getType());
		vb.setVariableType(tp.build());

		return vb;
	}

	//////////////////////////////////////////////////////////////
	// Field/Method Declarations

	@Override
	public boolean visit(final MethodDeclaration node) {
		final List<boa.types.Ast.Method> list = methods.peek();
		final Method.Builder b = Method.newBuilder();
		if (node.isConstructor())
			b.setName("<init>");
		else {
			b.setName(node.getName().getFullyQualifiedName());

			final boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
			if (node.getReturnType2() != null) {
				String name = typeName(node.getReturnType2());
				// FIXME process extra dimensions in JLS 8
				visitDimensions(node.extraDimensions());
				for (int i = 0; i < node.getExtraDimensions(); i++)
					name += "[]";
				tb.setName(name);
				tb.setKind(boa.types.Ast.TypeKind.OTHER);
				setTypeBinding(tb, node.getReturnType2());
			} else {
				tb.setName("void");
				tb.setKind(boa.types.Ast.TypeKind.PRIMITIVE);
			}
			b.setReturnType(tb.build());
		}
		b.addAllModifiers(buildModifiers(node.modifiers()));
		if (!node.typeParameters().isEmpty()) {
			setAstLevel(JLS3);

			for (final Object t : node.typeParameters()) {
				if (!((TypeParameter) t).modifiers().isEmpty())
					setAstLevel(JLS8);

				final boa.types.Ast.Type.Builder tp = boa.types.Ast.Type.newBuilder();
				String name = ((TypeParameter) t).getName().getFullyQualifiedName();
				String bounds = "";
				for (final Object o : ((TypeParameter) t).typeBounds()) {
					if (bounds.length() > 0)
						bounds += " & ";
					bounds += typeName((org.eclipse.jdt.core.dom.Type) o);
				}
				if (bounds.length() > 0)
					name = name + " extends " + bounds;
				tp.setName(name);
				tp.setKind(boa.types.Ast.TypeKind.GENERIC);
				setTypeBinding(tp, ((TypeParameter) t).getName());
				b.addGenericParameters(tp.build());
			}
		}
		if (node.getReceiverType() != null) {
			setAstLevel(JLS8);

			final Variable.Builder vb = Variable.newBuilder();
			vb.setName("this");
			final boa.types.Ast.Type.Builder tp = boa.types.Ast.Type.newBuilder();
			String name = typeName(node.getReceiverType());
			if (node.getReceiverQualifier() != null)
				name = node.getReceiverQualifier().getFullyQualifiedName() + "." + name;
			tp.setName(name);
			tp.setKind(boa.types.Ast.TypeKind.OTHER);
			setTypeBinding(tp, node.getReceiverType());
			vb.setVariableType(tp.build());
			b.addArguments(vb.build());
		}
		for (final Object o : node.parameters()) {
			final SingleVariableDeclaration ex = (SingleVariableDeclaration) o;
			b.addArguments(build(ex, TypeKind.OTHER));
		}
		for (final Object o : node.thrownExceptionTypes()) {
			final boa.types.Ast.Type.Builder tp = boa.types.Ast.Type.newBuilder();
			tp.setName(typeName((org.eclipse.jdt.core.dom.Type) o));
			tp.setKind(boa.types.Ast.TypeKind.CLASS);
			setTypeBinding(tp, (org.eclipse.jdt.core.dom.Type) o);
			b.addExceptionTypes(tp.build());
		}
		if (node.getBody() != null) {
			statements.push(new ArrayList<boa.types.Ast.Statement>());
			node.getBody().accept(this);
			for (final boa.types.Ast.Statement s : statements.pop())
				b.addStatements(s);
		}
		setDeclaringClass(b, node.resolveBinding());
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(final AnnotationTypeMemberDeclaration node) {
		setAstLevel(JLS3);

		final List<boa.types.Ast.Method> list = methods.peek();
		final Method.Builder b = Method.newBuilder();
		b.setName(node.getName().getFullyQualifiedName());
		b.addAllModifiers(buildModifiers(node.modifiers()));
		final boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
		tb.setName(typeName(node.getType()));
		tb.setKind(boa.types.Ast.TypeKind.OTHER);
		setTypeBinding(tb, node.getType());
		b.setReturnType(tb.build());
		if (node.getDefault() != null) {
			if (node.getDefault() instanceof Annotation) {
				// FIXME
			} else {
				final boa.types.Ast.Statement.Builder sb = boa.types.Ast.Statement.newBuilder();
				sb.setKind(boa.types.Ast.Statement.StatementKind.EXPRESSION);
				node.getDefault().accept(this);
				sb.addExpressions(expressions.pop());
				b.addStatements(sb.build());
			}
		}
		setDeclaringClass(b, node.resolveBinding());
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(final FieldDeclaration node) {
		final List<boa.types.Ast.Variable> list = fields.peek();
		for (final Object o : node.fragments()) {
			VariableDeclarationFragment f = (VariableDeclarationFragment) o;
			list.add(build(f, node.getType(), node.modifiers(), TypeKind.OTHER).build());
		}
		return false;
	}

	private Variable.Builder build(final VariableDeclarationFragment f, final org.eclipse.jdt.core.dom.Type type, final List<?> modifiers, final TypeKind kind) {
		final Variable.Builder b = Variable.newBuilder();
		b.setName(f.getName().getFullyQualifiedName());
		b.addAllModifiers(buildModifiers(modifiers));
		final boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
		String name = typeName(type);
		tb.setKind(kind);
		if (kind.equals(TypeKind.INFERRED))
			setAstLevel(JLS10);

		if (f.getInitializer() != null) {
			f.getInitializer().accept(this);
			b.setInitializer(expressions.pop());
		}

		// FIXME process extra dimensions in JLS 8
		visitDimensions(f.extraDimensions());
		for (int i = 0; i < f.getExtraDimensions(); i++)
			name += "[]";
		tb.setName(name);
		setTypeBinding(tb, type);
		b.setVariableType(tb.build());

		setDeclaringClass(b, f.resolveBinding());
		return b;
	}

	@Override
	public boolean visit(final EnumConstantDeclaration node) {
		setAstLevel(JLS3);

		final List<boa.types.Ast.Variable> list = fields.peek();
		final Variable.Builder b = Variable.newBuilder();
		b.setName(node.getName().getIdentifier());
		b.addAllModifiers(buildModifiers(node.modifiers()));
		for (final Object arg : node.arguments()) {
			((org.eclipse.jdt.core.dom.Expression) arg).accept(this);
			b.addExpressions(expressions.pop());
		}
		if (node.getAnonymousClassDeclaration() != null) {
			// FIXME skip anonymous class declaration
		}
		setDeclaringClass(b, node.resolveVariable());
		list.add(b.build());
		return false;
	}

	//////////////////////////////////////////////////////////////
	// Modifiers and Annotations

	protected boa.types.Ast.Modifier.Builder getAnnotationBuilder(final Annotation node) {
		final boa.types.Ast.Modifier.Builder b = boa.types.Ast.Modifier.newBuilder();
		b.setKind(boa.types.Ast.Modifier.ModifierKind.ANNOTATION);
		b.setAnnotationName(node.getTypeName().getFullyQualifiedName());
		return b;
	}

	@Override
	public boolean visit(final MarkerAnnotation node) {
		setAstLevel(JLS3);

		final String name = node.getTypeName().getFullyQualifiedName();
		if (name.equals("SafeVarargs") || name.equals("SuppressWarnings"))
			setAstLevel(JLS7);

		modifiers.push(getAnnotationBuilder(node).build());
		return false;
	}

	@Override
	public boolean visit(final SingleMemberAnnotation node) {
		setAstLevel(JLS3);

		final String name = node.getTypeName().getFullyQualifiedName();
		if (name.equals("SafeVarargs") || name.equals("SuppressWarnings"))
			setAstLevel(JLS7);

		final boa.types.Ast.Modifier.Builder b = getAnnotationBuilder(node);
		node.getValue().accept(this);
		if (expressions.empty()) {
			final boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
			eb.setKind(boa.types.Ast.Expression.ExpressionKind.ANNOTATION);
			eb.setAnnotation(modifiers.pop());
			b.addAnnotationMembers("value");
			b.addAnnotationValues(eb.build());
		} else {
			b.addAnnotationMembers("value");
			b.addAnnotationValues(expressions.pop());
		}
		modifiers.push(b.build());
		return false;
	}

	@Override
	public boolean visit(final NormalAnnotation node) {
		setAstLevel(JLS3);

		final String name = node.getTypeName().getFullyQualifiedName();
		if (name.equals("SafeVarargs") || name.equals("SuppressWarnings"))
			setAstLevel(JLS7);
		final boa.types.Ast.Modifier.Builder b = getAnnotationBuilder(node);
		for (final Object v : node.values()) {
			final MemberValuePair pair = (MemberValuePair) v;
			pair.getValue().accept(this);
			if (expressions.empty()) {
				final boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
				eb.setKind(boa.types.Ast.Expression.ExpressionKind.ANNOTATION);
				eb.setAnnotation(modifiers.pop());
				b.addAnnotationMembers(pair.getName().getFullyQualifiedName());
				b.addAnnotationValues(eb.build());
			} else {
				b.addAnnotationMembers(pair.getName().getFullyQualifiedName());
				b.addAnnotationValues(expressions.pop());
			}
		}
		modifiers.push(b.build());
		return false;
	}

	@Override
	public boolean visit(final org.eclipse.jdt.core.dom.Modifier node) {
		final boa.types.Ast.Modifier.Builder b = boa.types.Ast.Modifier.newBuilder();
		if (node.isPublic()) {
			b.setKind(boa.types.Ast.Modifier.ModifierKind.VISIBILITY);
			b.setVisibility(boa.types.Ast.Modifier.Visibility.PUBLIC);
		} else if (node.isPrivate()) {
			b.setKind(boa.types.Ast.Modifier.ModifierKind.VISIBILITY);
			b.setVisibility(boa.types.Ast.Modifier.Visibility.PRIVATE);
		} else if (node.isProtected()) {
			b.setKind(boa.types.Ast.Modifier.ModifierKind.VISIBILITY);
			b.setVisibility(boa.types.Ast.Modifier.Visibility.PROTECTED);
		} else if (node.isDefault()) {
			setAstLevel(JLS8);

			b.setKind(boa.types.Ast.Modifier.ModifierKind.VISIBILITY);
			b.setVisibility(boa.types.Ast.Modifier.Visibility.DEFAULT);
		} else if (node.isAbstract())
			b.setKind(boa.types.Ast.Modifier.ModifierKind.ABSTRACT);
		else if (node.isStatic())
			b.setKind(boa.types.Ast.Modifier.ModifierKind.STATIC);
		else if (node.isFinal())
			b.setKind(boa.types.Ast.Modifier.ModifierKind.FINAL);
		else if (node.isTransient())
			b.setKind(boa.types.Ast.Modifier.ModifierKind.TRANSIENT);
		else if (node.isVolatile())
			b.setKind(boa.types.Ast.Modifier.ModifierKind.VOLATILE);
		else if (node.isSynchronized())
			b.setKind(boa.types.Ast.Modifier.ModifierKind.SYNCHRONIZED);
		else if (node.isNative())
			b.setKind(boa.types.Ast.Modifier.ModifierKind.NATIVE);
		else if (node.isStrictfp())
			b.setKind(boa.types.Ast.Modifier.ModifierKind.STRICTFP);
		else {
			b.setKind(boa.types.Ast.Modifier.ModifierKind.OTHER);
			b.setOther(node.getKeyword().toString());
		}
		modifiers.push(b.build());
		return false;
	}

	@Override
	public boolean visit(final Dimension node) {
		if (!node.annotations().isEmpty())
			setAstLevel(JLS8);
		return false;
	}

	//////////////////////////////////////////////////////////////
	// Statements

	@Override
	public boolean visit(final AssertStatement node) {
		setAstLevel(JLS2);

		final boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		final List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.ASSERT);
		node.getExpression().accept(this);
		b.addConditions(expressions.pop());
		if (node.getMessage() != null) {
			node.getMessage().accept(this);
			b.addExpressions(expressions.pop());
		}
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(final Block node) {
		final boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		final List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.BLOCK);
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		for (final Object s : node.statements()) {
			((org.eclipse.jdt.core.dom.Statement) s).accept(this);
		}
		for (final boa.types.Ast.Statement st : statements.pop())
			b.addStatements(st);
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(final BreakStatement node) {
		final boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		final List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.BREAK);
		if (node.getLabel() != null) {
			final boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
			eb.setLiteral(node.getLabel().getFullyQualifiedName());
			eb.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
			b.addExpressions(eb.build());
		}
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(final CatchClause node) {
		final boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		final List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.CATCH);
		final SingleVariableDeclaration ex = node.getException();
		b.setVariableDeclaration(build(ex, TypeKind.CLASS));
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		for (final Object s : node.getBody().statements())
			((org.eclipse.jdt.core.dom.Statement) s).accept(this);
		for (final boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(final ConstructorInvocation node) {
		final boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		final List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.EXPRESSION);
		final boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		if (node.resolveConstructorBinding() != null) {
			final IMethodBinding mb = node.resolveConstructorBinding();
			if (mb.getReturnType() != null)
				eb.setReturnType(buildType(mb.getReturnType()));
			if (mb.getDeclaringClass() != null)
				eb.setDeclaringType(buildType(mb.getDeclaringClass()));
		}
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.METHODCALL);
		eb.setMethod("<init>");
		for (final Object a : node.arguments()) {
			((org.eclipse.jdt.core.dom.Expression) a).accept(this);
			eb.addMethodArgs(expressions.pop());
		}
		if (node.typeArguments() != null && !node.typeArguments().isEmpty()) {
			setAstLevel(JLS3);
			for (final Object t : node.typeArguments()) {
				final boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
				tb.setName(typeName((org.eclipse.jdt.core.dom.Type) t));
				tb.setKind(boa.types.Ast.TypeKind.GENERIC);
				setTypeBinding(tb, (org.eclipse.jdt.core.dom.Type) t);
				eb.addGenericParameters(tb.build());
			}
		}
		b.addExpressions(eb.build());
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(final ContinueStatement node) {
		final boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		final List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.CONTINUE);
		if (node.getLabel() != null) {
			final boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
			eb.setLiteral(node.getLabel().getFullyQualifiedName());
			eb.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
			b.addExpressions(eb.build());
		}
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(final DoStatement node) {
		final boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		final List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.DO);
		node.getExpression().accept(this);
		b.addConditions(expressions.pop());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getBody().accept(this);
		for (final boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(final EmptyStatement node) {
		final boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		final List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.EMPTY);
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(final EnhancedForStatement node) {
		setAstLevel(JLS3);

		final boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		final List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.FOREACH);
		final SingleVariableDeclaration ex = node.getParameter();
		b.setVariableDeclaration(build(ex, TypeKind.OTHER));
		node.getExpression().accept(this);
		b.addExpressions(expressions.pop());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getBody().accept(this);
		for (final boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(final ExpressionStatement node) {
		final boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		final List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.EXPRESSION);
		node.getExpression().accept(this);
		b.addExpressions(expressions.pop());
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(final ForStatement node) {
		final boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		final List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.FOR);
		for (final Object e : node.initializers()) {
			((org.eclipse.jdt.core.dom.Expression) e).accept(this);
			b.addInitializations(expressions.pop());
		}
		for (final Object e : node.updaters()) {
			((org.eclipse.jdt.core.dom.Expression) e).accept(this);
			b.addUpdates(expressions.pop());
		}
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
			b.addConditions(expressions.pop());
		}
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getBody().accept(this);
		for (final boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(final IfStatement node) {
		final boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		final List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.IF);
		node.getExpression().accept(this);
		b.addConditions(expressions.pop());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getThenStatement().accept(this);
		for (final boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		if (node.getElseStatement() != null) {
			statements.push(new ArrayList<boa.types.Ast.Statement>());
			node.getElseStatement().accept(this);
			for (final boa.types.Ast.Statement s : statements.pop())
				b.addStatements(s);
		}
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(final Initializer node) {
		final List<boa.types.Ast.Method> list = methods.peek();
		final Method.Builder b = Method.newBuilder();
		b.setName("<clinit>");
		b.addAllModifiers(buildModifiers(node.modifiers()));
		final boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
		tb.setName("void");
		tb.setKind(boa.types.Ast.TypeKind.PRIMITIVE);
		b.setReturnType(tb.build());
		if (node.getBody() != null) {
			statements.push(new ArrayList<boa.types.Ast.Statement>());
			node.getBody().accept(this);
			for (final boa.types.Ast.Statement s : statements.pop())
				b.addStatements(s);
		}
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(final LabeledStatement node) {
		final boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		final List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.LABEL);
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getBody().accept(this);
		for (final boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		final boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		eb.setLiteral(node.getLabel().getFullyQualifiedName());
		b.addExpressions(eb.build());
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(final ReturnStatement node) {
		final boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		final List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.RETURN);
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
			b.addExpressions(expressions.pop());
		}
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(final SuperConstructorInvocation node) {
		final boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		final List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.EXPRESSION);
		final boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		if (node.resolveConstructorBinding() != null) {
			final IMethodBinding mb = node.resolveConstructorBinding();
			if (mb.getReturnType() != null)
				eb.setReturnType(buildType(mb.getReturnType()));
			if (mb.getDeclaringClass() != null)
				eb.setDeclaringType(buildType(mb.getDeclaringClass()));
		}
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.METHODCALL);
		eb.setMethod("super");
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
			eb.addExpressions(expressions.pop());
		}
		for (final Object a : node.arguments()) {
			((org.eclipse.jdt.core.dom.Expression) a).accept(this);
			eb.addMethodArgs(expressions.pop());
		}
		if (!node.typeArguments().isEmpty()) {
			setAstLevel(JLS3);

			for (final Object t : node.typeArguments()) {
				final boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
				tb.setName(typeName((org.eclipse.jdt.core.dom.Type) t));
				tb.setKind(boa.types.Ast.TypeKind.GENERIC);
				setTypeBinding(tb, (org.eclipse.jdt.core.dom.Type) t);
				eb.addGenericParameters(tb.build());
			}
		}
		b.addExpressions(eb.build());
		list.add(b.build());
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean visit(final SwitchCase node) {
		if (node.expressions() != null) {
			if (node.expressions().size() > 1)
				setAstLevel(JLS14);
			else if (node.expressions().size() > 0 && node.expressions().get(0) instanceof StringLiteral)
				setAstLevel(JLS7);
		}
		final boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		final List<boa.types.Ast.Statement> list = statements.peek();
		if (node.isDefault())
			b.setKind(boa.types.Ast.Statement.StatementKind.DEFAULT);
		else
			b.setKind(boa.types.Ast.Statement.StatementKind.CASE);
		if (node.expressions() != null) {
			for (final Object o : node.expressions()) {
				((ASTNode) o).accept(this);
				b.addExpressions(expressions.pop());
			}
		}
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(final SwitchStatement node) {
		final boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		final List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.SWITCH);
		node.getExpression().accept(this);
		b.addExpressions(expressions.pop());
		statements.push(new ArrayList<boa.types.Ast.Statement>());

		Boolean hasArrow = null;

		if (node.statements() != null && node.statements().size() > 0 && node.statements().get(0) instanceof SwitchCase) {
			boolean temp = ((SwitchCase) node.statements().get(0)).isSwitchLabeledRule();
			hasArrow = temp;
			for (int i = 1; i < node.statements().size(); i ++)
				if (node.statements().get(i) instanceof SwitchCase && temp != ((SwitchCase)node.statements().get(i)).isSwitchLabeledRule())
					setAstLevel(SOURCE_JAVA_ERROR);
		}

		if (hasArrow != null)
			b.setIsArrow(hasArrow);

		for (final Object s : node.statements())
			((org.eclipse.jdt.core.dom.Statement) s).accept(this);
		for (final boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(final SynchronizedStatement node) {
		final boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		final List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.SYNCHRONIZED);
		node.getExpression().accept(this);
		b.addExpressions(expressions.pop());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		for (final Object s : node.getBody().statements())
			((org.eclipse.jdt.core.dom.Statement) s).accept(this);
		for (final boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(final ThrowStatement node) {
		final boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		final List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.THROW);
		node.getExpression().accept(this);
		b.addExpressions(expressions.pop());
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(final TryStatement node) {
		final boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		final List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.TRY);
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getBody().accept(this);
		for (final Object c : node.catchClauses())
			((CatchClause) c).accept(this);
		if (node.getFinally() != null)
			visitFinally(node.getFinally());
		for (final boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		if (!node.resources().isEmpty())
			setAstLevel(JLS7);

		for (final Object v : node.resources()) {
			((Expression) v).accept(this);
			b.addInitializations(expressions.pop());
		}
		list.add(b.build());
		return false;
	}

	private void visitFinally(final Block node) {
		final boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		final List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.FINALLY);
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		for (final Object s : node.statements())
			((org.eclipse.jdt.core.dom.Statement) s).accept(this);
		for (final boa.types.Ast.Statement st : statements.pop())
			b.addStatements(st);
		list.add(b.build());
	}

	@Override
	public boolean visit(final TypeDeclarationStatement node) {
		final boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		final List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.TYPEDECL);
		declarations.push(new ArrayList<boa.types.Ast.Declaration>());
		node.getDeclaration().accept(this);
		for (final boa.types.Ast.Declaration d : declarations.pop())
			b.setTypeDeclaration(d);
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(final VariableDeclarationStatement node) {
		final boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		final List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.EXPRESSION);
		final boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.VARDECL);

		for (final Object o : node.fragments()) {
			final VariableDeclarationFragment f = (VariableDeclarationFragment) o;
			if (node.getType().isVar() && !node.fragments().toString().contains("new var("))
				eb.addVariableDecls(build(f, node.getType(), node.modifiers(), TypeKind.INFERRED));
			else
				eb.addVariableDecls(build(f, node.getType(), node.modifiers(), TypeKind.OTHER));
		}
		b.addExpressions(eb.build());
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(final WhileStatement node) {
		final boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		final List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.WHILE);
		node.getExpression().accept(this);
		b.addConditions(expressions.pop());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getBody().accept(this);
		for (final boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		list.add(b.build());
		return false;
	}

	//////////////////////////////////////////////////////////////
	// Expressions

	@Override
	public boolean visit(final ArrayAccess node) {
		final boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		if (node.resolveTypeBinding() != null) {
			b.setReturnType(buildType(node.resolveTypeBinding()));
		}
		b.setKind(boa.types.Ast.Expression.ExpressionKind.ARRAYACCESS);
		node.getArray().accept(this);
		b.addExpressions(expressions.pop());
		node.getIndex().accept(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(final ArrayCreation node) {
		final boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		if (node.resolveTypeBinding() != null) {
			b.setReturnType(buildType(node.resolveTypeBinding()));
		}
		b.setKind(boa.types.Ast.Expression.ExpressionKind.NEWARRAY);
		final boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
		tb.setName(typeName(node.getType()));
		tb.setKind(boa.types.Ast.TypeKind.ARRAY);
		setTypeBinding(tb, node.getType());
		b.setNewType(tb.build());
		for (final Object e : node.dimensions()) {
			((org.eclipse.jdt.core.dom.Expression) e).accept(this);
			b.addExpressions(expressions.pop());
		}
		if (node.getInitializer() != null) {
			node.getInitializer().accept(this);
			b.addExpressions(expressions.pop());
		}
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(final ArrayInitializer node) {
		final boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		if (node.resolveTypeBinding() != null) {
			b.setReturnType(buildType(node.resolveTypeBinding()));
		}
		b.setKind(boa.types.Ast.Expression.ExpressionKind.ARRAYINIT);
		for (final Object e : node.expressions()) {
			((org.eclipse.jdt.core.dom.Expression) e).accept(this);
			if (expressions.empty()) {
				// FIXME is it only possible from JLS8
				final boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
				eb.setKind(boa.types.Ast.Expression.ExpressionKind.ANNOTATION);
				eb.setAnnotation(modifiers.pop());
				b.addExpressions(eb.build());
			} else {
				b.addExpressions(expressions.pop());
			}
		}
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(final Assignment node) {
		final boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		node.getLeftHandSide().accept(this);
		b.addExpressions(expressions.pop());
		node.getRightHandSide().accept(this);
		b.addExpressions(expressions.pop());
		if (node.getOperator() == Assignment.Operator.ASSIGN)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN);
		else if (node.getOperator() == Assignment.Operator.BIT_AND_ASSIGN)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_BITAND);
		else if (node.getOperator() == Assignment.Operator.BIT_OR_ASSIGN)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_BITOR);
		else if (node.getOperator() == Assignment.Operator.BIT_XOR_ASSIGN)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_BITXOR);
		else if (node.getOperator() == Assignment.Operator.DIVIDE_ASSIGN)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_DIV);
		else if (node.getOperator() == Assignment.Operator.LEFT_SHIFT_ASSIGN)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_LSHIFT);
		else if (node.getOperator() == Assignment.Operator.MINUS_ASSIGN)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_SUB);
		else if (node.getOperator() == Assignment.Operator.PLUS_ASSIGN)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_ADD);
		else if (node.getOperator() == Assignment.Operator.REMAINDER_ASSIGN)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_MOD);
		else if (node.getOperator() == Assignment.Operator.RIGHT_SHIFT_SIGNED_ASSIGN)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_RSHIFT);
		else if (node.getOperator() == Assignment.Operator.RIGHT_SHIFT_UNSIGNED_ASSIGN)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_UNSIGNEDRSHIFT);
		else if (node.getOperator() == Assignment.Operator.TIMES_ASSIGN)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.ASSIGN_MULT);
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(final BooleanLiteral node) {
		final boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		if (node.booleanValue())
			b.setLiteral("true");
		else
			b.setLiteral("false");
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(final CastExpression node) {
		final boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.CAST);
		final boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
		tb.setName(typeName(node.getType()));
		tb.setKind(boa.types.Ast.TypeKind.OTHER);
		setTypeBinding(tb, node.getType());
		b.setNewType(tb.build());
		node.getExpression().accept(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(final CharacterLiteral node) {
		final boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		b.setLiteral(node.getEscapedValue());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(final ClassInstanceCreation node) {
		final boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.NEW);
		final boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
		tb.setName(typeName(node.getType()));
		setTypeBinding(tb, node.getType());
		tb.setKind(boa.types.Ast.TypeKind.CLASS);
		b.setNewType(tb.build());
		if (node.typeArguments() != null && !node.typeArguments().isEmpty()) {
			setAstLevel(JLS3);
			for (final Object t : node.typeArguments()) {
				final boa.types.Ast.Type.Builder gtb = boa.types.Ast.Type.newBuilder();
				gtb.setName(typeName((org.eclipse.jdt.core.dom.Type) t));
				gtb.setKind(boa.types.Ast.TypeKind.GENERIC);
				setTypeBinding(gtb, (org.eclipse.jdt.core.dom.Type) t);
				b.addGenericParameters(gtb.build());
			}
		}
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
			b.addExpressions(expressions.pop());
		}
		for (final Object a : node.arguments()) {
			((org.eclipse.jdt.core.dom.Expression) a).accept(this);
			b.addMethodArgs(expressions.pop());
		}
		if (node.getAnonymousClassDeclaration() != null) {
			declarations.push(new ArrayList<boa.types.Ast.Declaration>());
			node.getAnonymousClassDeclaration().accept(this);
			for (final boa.types.Ast.Declaration d : declarations.pop())
				b.setAnonDeclaration(d);
		}
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(final ConditionalExpression node) {
		final boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.CONDITIONAL);
		node.getExpression().accept(this);
		b.addExpressions(expressions.pop());
		node.getThenExpression().accept(this);
		b.addExpressions(expressions.pop());
		node.getElseExpression().accept(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(final FieldAccess node) {
		final boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		if (node.resolveFieldBinding() != null) {
			final IVariableBinding vb = node.resolveFieldBinding();
			if (vb.getType() != null)
				b.setReturnType(buildType(vb.getType()));
			if (vb.getDeclaringClass() != null)
				b.setDeclaringType(buildType(vb.getDeclaringClass()));
		}
		b.setKind(boa.types.Ast.Expression.ExpressionKind.VARACCESS);
		b.setIsMemberAccess(true);
		node.getExpression().accept(this);
		b.addExpressions(expressions.pop());
		b.setVariable(node.getName().getFullyQualifiedName());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(final SimpleName node) {
		final boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		if (node.resolveBinding() != null) {
			if (node.resolveBinding() instanceof IVariableBinding) {
				final IVariableBinding vb = (IVariableBinding) node.resolveBinding();
				if (vb.isField())
					b.setIsMemberAccess(true);
				if (vb.getType() != null)
					b.setReturnType(buildType(vb.getType()));
				if (vb.getDeclaringClass() != null)
					b.setDeclaringType(buildType(vb.getDeclaringClass()));
			} else {
				b.setReturnType(buildType(node.resolveTypeBinding()));
			}
		}
		b.setKind(boa.types.Ast.Expression.ExpressionKind.VARACCESS);
		b.setVariable(node.getFullyQualifiedName());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(final QualifiedName node) {
		final boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		if (node.resolveBinding() != null && node.resolveBinding() instanceof IVariableBinding) {
			final IVariableBinding vb = (IVariableBinding) node.resolveBinding();
			if (vb.getType() != null)
				b.setReturnType(buildType(vb.getType()));
			if (vb.getDeclaringClass() != null)
				b.setDeclaringType(buildType(vb.getDeclaringClass()));
		} else if (node.resolveTypeBinding() != null) {
			b.setReturnType(buildType(node.resolveTypeBinding()));
		}
		b.setKind(boa.types.Ast.Expression.ExpressionKind.VARACCESS);
		b.setIsMemberAccess(true);
		b.setVariable(node.getFullyQualifiedName());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(final InfixExpression node) {
		final boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		if (node.getOperator() == InfixExpression.Operator.AND)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.BIT_AND);
		else if (node.getOperator() == InfixExpression.Operator.CONDITIONAL_AND)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.LOGICAL_AND);
		else if (node.getOperator() == InfixExpression.Operator.CONDITIONAL_OR)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.LOGICAL_OR);
		else if (node.getOperator() == InfixExpression.Operator.DIVIDE)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_DIV);
		else if (node.getOperator() == InfixExpression.Operator.EQUALS)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.EQ);
		else if (node.getOperator() == InfixExpression.Operator.GREATER)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.GT);
		else if (node.getOperator() == InfixExpression.Operator.GREATER_EQUALS)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.GTEQ);
		else if (node.getOperator() == InfixExpression.Operator.LEFT_SHIFT)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.BIT_LSHIFT);
		else if (node.getOperator() == InfixExpression.Operator.LESS)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.LT);
		else if (node.getOperator() == InfixExpression.Operator.LESS_EQUALS)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.LTEQ);
		else if (node.getOperator() == InfixExpression.Operator.MINUS)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_SUB);
		else if (node.getOperator() == InfixExpression.Operator.NOT_EQUALS)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.NEQ);
		else if (node.getOperator() == InfixExpression.Operator.OR)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.BIT_OR);
		else if (node.getOperator() == InfixExpression.Operator.PLUS)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_ADD);
		else if (node.getOperator() == InfixExpression.Operator.REMAINDER)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_MOD);
		else if (node.getOperator() == InfixExpression.Operator.RIGHT_SHIFT_SIGNED)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.BIT_RSHIFT);
		else if (node.getOperator() == InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.BIT_UNSIGNEDRSHIFT);
		else if (node.getOperator() == InfixExpression.Operator.TIMES)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_MULT);
		else if (node.getOperator() == InfixExpression.Operator.XOR)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.BIT_XOR);
		final List<org.eclipse.jdt.core.dom.Expression> operands = new ArrayList<org.eclipse.jdt.core.dom.Expression>();
		getOperands(node, operands);
		for (final org.eclipse.jdt.core.dom.Expression e : operands) {
			e.accept(this);
			b.addExpressions(expressions.pop());
		}
		expressions.push(b.build());
		return false;
	}

	private void getOperands(final InfixExpression node, final List<org.eclipse.jdt.core.dom.Expression> operands) {
		addOperand(node.getOperator(), operands, node.getLeftOperand());
		addOperand(node.getOperator(), operands, node.getRightOperand());
		for (int i = 0; i < node.extendedOperands().size(); i++) {
			org.eclipse.jdt.core.dom.Expression e = (org.eclipse.jdt.core.dom.Expression) node.extendedOperands().get(i);
			addOperand(node.getOperator(), operands, e);
		}
	}

	public void addOperand(final InfixExpression.Operator operator, final List<org.eclipse.jdt.core.dom.Expression> operands, final org.eclipse.jdt.core.dom.Expression e) {
		if (e instanceof org.eclipse.jdt.core.dom.InfixExpression && ((org.eclipse.jdt.core.dom.InfixExpression) e).getOperator() == operator)
			getOperands((InfixExpression) e, operands);
		else
			operands.add(e);
	}

	@Override
	public boolean visit(final InstanceofExpression node) {
		final boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.TYPECOMPARE);
		node.getLeftOperand().accept(this);
		b.addExpressions(expressions.pop());
		final boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
		tb.setName(typeName(node.getRightOperand()));
		tb.setKind(boa.types.Ast.TypeKind.OTHER);
		setTypeBinding(tb, node.getRightOperand());
		b.setNewType(tb.build());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(final MethodInvocation node) {
		final boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		if (node.resolveMethodBinding() != null) {
			final IMethodBinding mb = node.resolveMethodBinding();
			if (mb.getReturnType() != null)
				b.setReturnType(buildType(mb.getReturnType()));
			if (mb.getDeclaringClass() != null)
				b.setDeclaringType(buildType(mb.getDeclaringClass()));
		}
		b.setKind(boa.types.Ast.Expression.ExpressionKind.METHODCALL);
		b.setMethod(node.getName().getFullyQualifiedName());
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
			b.addExpressions(expressions.pop());
		}
		for (final Object a : node.arguments()) {
			((org.eclipse.jdt.core.dom.Expression) a).accept(this);
			b.addMethodArgs(expressions.pop());
		}
		if (!node.typeArguments().isEmpty()) {
			setAstLevel(JLS3);

			for (final Object t : node.typeArguments()) {
				final boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
				tb.setName(typeName((org.eclipse.jdt.core.dom.Type) t));
				tb.setKind(boa.types.Ast.TypeKind.GENERIC);
				setTypeBinding(tb, (org.eclipse.jdt.core.dom.Type) t);
				b.addGenericParameters(tb.build());
			}
		}
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(final NullLiteral node) {
		final boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		b.setLiteral("null");
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(final NumberLiteral node) {
		if (node.getToken().toLowerCase().startsWith("0b"))
			setAstLevel(JLS7);
		if (node.getToken().contains("_"))
			setAstLevel(JLS7);
		final boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		b.setLiteral(node.getToken());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(final ParenthesizedExpression node) {
		final boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.PAREN);
		node.getExpression().accept(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(final PostfixExpression node) {
		final boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		if (node.getOperator() == PostfixExpression.Operator.DECREMENT)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_DEC);
		else if (node.getOperator() == PostfixExpression.Operator.INCREMENT)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_INC);
		node.getOperand().accept(this);
		b.addExpressions(expressions.pop());
		b.setIsPostfix(true);
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(final PrefixExpression node) {
		final boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		if (node.getOperator() == PrefixExpression.Operator.DECREMENT)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_DEC);
		else if (node.getOperator() == PrefixExpression.Operator.INCREMENT)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_INC);
		else if (node.getOperator() == PrefixExpression.Operator.PLUS)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_ADD);
		else if (node.getOperator() == PrefixExpression.Operator.MINUS)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.OP_SUB);
		else if (node.getOperator() == PrefixExpression.Operator.COMPLEMENT)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.BIT_NOT);
		else if (node.getOperator() == PrefixExpression.Operator.NOT)
			b.setKind(boa.types.Ast.Expression.ExpressionKind.LOGICAL_NOT);
		node.getOperand().accept(this);
		b.addExpressions(expressions.pop());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(final StringLiteral node) {
		final boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		b.setLiteral(node.getEscapedValue());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(final SuperFieldAccess node) {
		final boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		if (node.resolveFieldBinding() != null) {
			final IVariableBinding vb = node.resolveFieldBinding();
			if (vb.getType() != null)
				b.setReturnType(buildType(vb.getType()));
			if (vb.getDeclaringClass() != null)
				b.setDeclaringType(buildType(vb.getDeclaringClass()));
		}
		b.setKind(boa.types.Ast.Expression.ExpressionKind.VARACCESS);
		b.setIsMemberAccess(true);
		String name = "super";
		if (node.getQualifier() != null)
			name = node.getQualifier().getFullyQualifiedName() + "." + name;
		final boa.types.Ast.Expression.Builder qb = boa.types.Ast.Expression.newBuilder();
		qb.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		qb.setLiteral(name);
		b.addExpressions(qb);
		b.setVariable(node.getName().getFullyQualifiedName());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(final SuperMethodInvocation node) {
		final boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		if (node.resolveMethodBinding() != null) {
			final IMethodBinding mb = node.resolveMethodBinding();
			if (mb.getReturnType() != null)
				b.setReturnType(buildType(mb.getReturnType()));
			if (mb.getDeclaringClass() != null)
				b.setDeclaringType(buildType(mb.getDeclaringClass()));
		}
		b.setKind(boa.types.Ast.Expression.ExpressionKind.METHODCALL);
		String name = "super." + node.getName().getFullyQualifiedName();
		if (node.getQualifier() != null)
			name = node.getQualifier().getFullyQualifiedName() + "." + name;
		b.setMethod(name);
		for (final Object a : node.arguments()) {
			((org.eclipse.jdt.core.dom.Expression) a).accept(this);
			b.addMethodArgs(expressions.pop());
		}
		if (!node.typeArguments().isEmpty()) {
			setAstLevel(JLS3);

			for (final Object t : node.typeArguments()) {
				final boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
				tb.setName(typeName((org.eclipse.jdt.core.dom.Type) t));
				tb.setKind(boa.types.Ast.TypeKind.GENERIC);
				setTypeBinding(tb, (org.eclipse.jdt.core.dom.Type) t);
				b.addGenericParameters(tb.build());
			}
		}
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(final ThisExpression node) {
		final boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		if (node.resolveTypeBinding() != null)
			b.setReturnType(buildType(node.resolveTypeBinding()));
		String name = "";
		if (node.getQualifier() != null)
			name += node.getQualifier().getFullyQualifiedName() + ".";
		b.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		b.setLiteral(name + "this");
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(final TypeLiteral node) {
		final boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		if (node.resolveTypeBinding() != null)
			b.setReturnType(buildType(node.resolveTypeBinding()));
		b.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		b.setLiteral(typeName(node.getType()) + ".class");
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(final VariableDeclarationExpression node) {
		final boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.VARDECL);

		for (final Object o : node.fragments()) {
			final VariableDeclarationFragment f = (VariableDeclarationFragment) o;
			if (node.getType().isVar() && !node.fragments().toString().contains("new var()"))
				eb.addVariableDecls(build(f, node.getType(), node.modifiers(), TypeKind.INFERRED));
			else
				eb.addVariableDecls(build(f, node.getType(), node.modifiers(), TypeKind.OTHER));
		}
		expressions.push(eb.build());
		return false;
	}

	// begin Java 8
	@Override
	public boolean visit(final LambdaExpression node) {
		setAstLevel(JLS8);

		final boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.LAMBDA);
		if (!node.hasParentheses())
			eb.setNoParens(true);
		for (final Object o : node.parameters()) {
			if (o instanceof SingleVariableDeclaration) {
				final SingleVariableDeclaration svd = (SingleVariableDeclaration) o;
				final Variable.Builder vb = build(svd, boa.types.Ast.TypeKind.OTHER);
				eb.addVariableDecls(vb);
			} else {
				final VariableDeclarationFragment vdf = (VariableDeclarationFragment) o;
				final Variable.Builder vb = Variable.newBuilder();
				vb.setName(vdf.getName().getFullyQualifiedName());
				// FIXME
				final boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
				tb.setName("");
				tb.setKind(boa.types.Ast.TypeKind.OTHER);
				setTypeBinding(tb, vdf.getName());
				visitDimensions(vdf.extraDimensions());
				vb.setVariableType(tb.build());
				eb.addVariableDecls(vb);
			}
		}
		if (node.getBody() != null) {
			if (node.getBody() instanceof org.eclipse.jdt.core.dom.Expression) {
				node.getBody().accept(this);
				eb.addExpressions(expressions.pop());
			} else {
				statements.push(new ArrayList<boa.types.Ast.Statement>());
				node.getBody().accept(this);
				for (final boa.types.Ast.Statement s : statements.pop())
					eb.addStatements(s);
			}
		}
		expressions.push(eb.build());

		return false;
	}

	@Override
	public boolean visit(final CreationReference node) {
		setAstLevel(JLS8);

		final boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		if (node.resolveMethodBinding() != null) {
			final IMethodBinding mb = node.resolveMethodBinding();
			if (mb.getReturnType() != null)
				eb.setReturnType(buildType(mb.getReturnType()));
			if (mb.getDeclaringClass() != null)
				eb.setDeclaringType(buildType(mb.getDeclaringClass()));
		}
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.METHOD_REFERENCE);

		final boa.types.Ast.Type.Builder tb1 = boa.types.Ast.Type.newBuilder();
		tb1.setName(typeName(node.getType()));
		tb1.setKind(boa.types.Ast.TypeKind.OTHER);
		setTypeBinding(tb1, node.getType());
		eb.setNewType(tb1.build());

		for (final Object t : node.typeArguments()) {
			final boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
			tb.setName(typeName((org.eclipse.jdt.core.dom.Type) t));
			tb.setKind(boa.types.Ast.TypeKind.GENERIC);
			setTypeBinding(tb, (org.eclipse.jdt.core.dom.Type) t);
			eb.addGenericParameters(tb.build());
		}

		eb.setMethod("new");
		expressions.push(eb.build());

		return false;
	}

	@Override
	public boolean visit(final ExpressionMethodReference node) {
		setAstLevel(JLS8);

		final boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		if (node.resolveMethodBinding() != null) {
			final IMethodBinding mb = node.resolveMethodBinding();
			if (mb.getReturnType() != null)
				eb.setReturnType(buildType(mb.getReturnType()));
			if (mb.getDeclaringClass() != null)
				eb.setDeclaringType(buildType(mb.getDeclaringClass()));
		}
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.METHOD_REFERENCE);

		node.getExpression().accept(this);
		eb.addExpressions(expressions.pop());

		for (final Object t : node.typeArguments()) {
			final boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
			tb.setName(typeName((org.eclipse.jdt.core.dom.Type) t));
			tb.setKind(boa.types.Ast.TypeKind.GENERIC);
			setTypeBinding(tb, (org.eclipse.jdt.core.dom.Type) t);
			eb.addGenericParameters(tb.build());
		}

		eb.setMethod(node.getName().getIdentifier());
		expressions.push(eb.build());

		return false;
	}

	@Override
	public boolean visit(final SuperMethodReference node) {
		setAstLevel(JLS8);

		final boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		if (node.resolveMethodBinding() != null) {
			final IMethodBinding mb = node.resolveMethodBinding();
			if (mb.getReturnType() != null)
				eb.setReturnType(buildType(mb.getReturnType()));
			if (mb.getDeclaringClass() != null)
				eb.setDeclaringType(buildType(mb.getDeclaringClass()));
		}
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.METHOD_REFERENCE);

		if (node.getQualifier() != null)
			eb.setLiteral((node.getQualifier()) + ".super");
		else
			eb.setLiteral("super");

		for (final Object t : node.typeArguments()) {
			final boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
			tb.setName(typeName((org.eclipse.jdt.core.dom.Type) t));
			tb.setKind(boa.types.Ast.TypeKind.GENERIC);
			setTypeBinding(tb, (org.eclipse.jdt.core.dom.Type) t);
			eb.addGenericParameters(tb.build());
		}

		eb.setMethod(node.getName().getIdentifier());
		expressions.push(eb.build());

		return false;
	}

	@Override
	public boolean visit(final TypeMethodReference node) {
		setAstLevel(JLS8);

		final boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		if (node.resolveMethodBinding() != null) {
			final IMethodBinding mb = node.resolveMethodBinding();
			if (mb.getReturnType() != null)
				eb.setReturnType(buildType(mb.getReturnType()));
			if (mb.getDeclaringClass() != null)
				eb.setDeclaringType(buildType(mb.getDeclaringClass()));
		}
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.METHOD_REFERENCE);

		final boa.types.Ast.Type.Builder tb1 = boa.types.Ast.Type.newBuilder();
		tb1.setName(typeName(node.getType()));
		tb1.setKind(boa.types.Ast.TypeKind.OTHER);
		setTypeBinding(tb1, node.getType());
		eb.setNewType(tb1.build());

		for (final Object t : node.typeArguments()) {
			final boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
			tb.setName(typeName((org.eclipse.jdt.core.dom.Type) t));
			tb.setKind(boa.types.Ast.TypeKind.GENERIC);
			setTypeBinding(tb, (org.eclipse.jdt.core.dom.Type) t);
			eb.addGenericParameters(tb.build());
		}

		eb.setMethod(node.getName().getIdentifier());
		expressions.push(eb.build());

		return false;
	}

	// begin Java 9
	@Override
	public boolean visit(final ModuleDeclaration node) {
		setAstLevel(JLS9);

		if (node.isOpen()) {
			final boa.types.Ast.Modifier.Builder ob = boa.types.Ast.Modifier.newBuilder();
			ob.setKind(boa.types.Ast.Modifier.ModifierKind.OTHER);
			ob.setOther("open");
			b.addModifiers(ob.build());
		}

		b.setName(node.getName().getFullyQualifiedName());
		final boa.types.Ast.Modifier.Builder m = boa.types.Ast.Modifier.newBuilder();
		m.setKind(boa.types.Ast.Modifier.ModifierKind.MODULE);
		b.addModifiers(m.build());

		if (!node.annotations().isEmpty())
			b.addAllModifiers(visitAnnotationsList(node.annotations()));

		for (final Object s : node.moduleStatements()) {
			((org.eclipse.jdt.core.dom.ModuleDirective) s).accept(this);
			b.addExpressions(expressions.pop());
		}

		return false;
	}

	@Override
	public boolean visit(final ModuleModifier node) {
		final boa.types.Ast.Modifier.Builder b = boa.types.Ast.Modifier.newBuilder();

		if (node.isStatic())
			b.setKind(boa.types.Ast.Modifier.ModifierKind.STATIC);
		else if (node.isTransitive())
			b.setKind(boa.types.Ast.Modifier.ModifierKind.TRANSITIVE);

		modifiers.push(b.build());
		return false;
	}

	@Override
	public boolean visit(final RequiresDirective node) {
		setAstLevel(JLS9);

		final boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.REQUIRES);

		if (node.modifiers() != null) {
			for (final Object o : node.modifiers()) {
				((ModuleModifier) o).accept(this);
				b.setAnnotation(modifiers.pop());
			}
		}

		final boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		eb.setVariable(node.getName().getFullyQualifiedName());
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.MODULE_NAME);
		b.addExpressions(eb.build());

		expressions.push(b.build());

		return false;
	}

	@Override
	public boolean visit(final ExportsDirective node) {
		setAstLevel(JLS9);

		final boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.EXPORTS);

		final boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		eb.setVariable(node.getName().getFullyQualifiedName());
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.PACKAGE_NAME);
		b.addExpressions(eb.build());

		if (node.modules() != null) {
			for (final Object o : node.modules()) {
				final boa.types.Ast.Expression.Builder mb = boa.types.Ast.Expression.newBuilder();
				mb.setVariable(((Name) o).getFullyQualifiedName());
				mb.setKind(boa.types.Ast.Expression.ExpressionKind.MODULE_NAME);
				b.addExpressions(mb.build());
			}
		}

		expressions.push(b.build());

		return false;
	}

	@Override
	public boolean visit(final OpensDirective node) {
		setAstLevel(JLS9);

		final boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.OPENS);

		final boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		eb.setVariable(node.getName().getFullyQualifiedName());
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.PACKAGE_NAME);
		b.addExpressions(eb.build());

		if (node.modules() != null) {
			for (final Object o : node.modules()) {
				final boa.types.Ast.Expression.Builder mb = boa.types.Ast.Expression.newBuilder();
				mb.setVariable(((Name) o).getFullyQualifiedName());
				mb.setKind(boa.types.Ast.Expression.ExpressionKind.MODULE_NAME);
				b.addExpressions(mb.build());
			}
		}

		expressions.push(b.build());

		return false;
	}

	@Override
	public boolean visit(final UsesDirective node) {
		setAstLevel(JLS9);

		final boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.USES);

		final boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		eb.setVariable(node.getName().getFullyQualifiedName());
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.NAMESPACENAME);
		eb.setVariable("service");
		b.addExpressions(eb.build());

		expressions.push(b.build());

		return false;
	}

	@Override
	public boolean visit(final ProvidesDirective node) {
		setAstLevel(JLS9);

		final boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.PROVIDES);

		final boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		eb.setLiteral(node.getName().getFullyQualifiedName());
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.NAMESPACENAME);
		eb.setVariable("service");
		b.addExpressions(eb.build());

		if (node.implementations() != null) {
			for (final Object o : node.implementations()) {
				final boa.types.Ast.Expression.Builder ebs = boa.types.Ast.Expression.newBuilder();
				ebs.setLiteral(((Name) o).getFullyQualifiedName());
				ebs.setKind(boa.types.Ast.Expression.ExpressionKind.NAMESPACENAME);
				ebs.setVariable("implementations");
				b.addExpressions(ebs.build());
			}
		}

		expressions.push(b.build());

		return false;
	}

	// begin Java 14
	@Override
	public boolean visit(final SwitchExpression node) {
		setAstLevel(JLS14);

		final boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.STATEMENT);

		final boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		b.setKind(boa.types.Ast.Statement.StatementKind.SWITCH);
		node.getExpression().accept(this);
		b.addExpressions(expressions.pop());

		Boolean hasArrow = null;
		if (node.statements() != null && node.statements().size() > 0 && node.statements().get(0) instanceof SwitchCase) {
			boolean temp = ((SwitchCase) node.statements().get(0)).isSwitchLabeledRule();
			hasArrow = temp;
			for (int i = 1; i < node.statements().size(); i ++)
				if (node.statements().get(i) instanceof SwitchCase && temp != ((SwitchCase)node.statements().get(i)).isSwitchLabeledRule()) {
					setAstLevel(SOURCE_JAVA_ERROR);
                    break;
				}
		}

		if (hasArrow != null)
			b.setIsArrow(hasArrow);
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		for (final Object s : node.statements())
			((org.eclipse.jdt.core.dom.Statement) s).accept(this);
		for (final boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);

		eb.addStatements(b.build());
		expressions.push(eb.build());
		return false;
	}

	@Override
	public boolean visit(final YieldStatement node) {
		setAstLevel(JLS14);

		final boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		final List<boa.types.Ast.Statement> list = statements.peek();

		if (node.isImplicit())
			b.setKind(boa.types.Ast.Statement.StatementKind.YIELD_IMPLICIT);
		else
			b.setKind(boa.types.Ast.Statement.StatementKind.YIELD);

		node.getExpression().accept(this);
		b.addExpressions(expressions.pop());

		list.add(b.build());

		return false;
	}

	// begin Java 15
	@Override
	public boolean visit(final TextBlock node) {
		setAstLevel(JLS15);

		final boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		eb.setLiteral(node.getEscapedValue());
		expressions.push(eb.build());

		return false;
	}

	// begin Java 16
	@Override
	public boolean visit(final RecordDeclaration node) {
		setAstLevel(JLS16);

		final boa.types.Ast.Declaration.Builder b = boa.types.Ast.Declaration.newBuilder();
		b.setName(node.getName().getIdentifier());
		b.setKind(boa.types.Ast.TypeKind.IMMUTABLE);
		b.setFullyQualifiedName(getFullyQualifiedName(node));

		setDeclaringClass(b, node.resolveBinding());
		b.addAllModifiers(buildModifiers(node.modifiers()));

		if (!node.typeParameters().isEmpty()) {
			for (final Object t : node.typeParameters()) {
				final boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
				String name = ((TypeParameter) t).getName().getFullyQualifiedName();
				String bounds = "";
				for (final Object o : ((TypeParameter) t).typeBounds()) {
					if (bounds.length() > 0)
						bounds += " & ";
					bounds += typeName((org.eclipse.jdt.core.dom.Type) o);
				}
				if (bounds.length() > 0)
					name = name + " extends " + bounds;
				tb.setName(name);
				tb.setKind(boa.types.Ast.TypeKind.GENERIC);
				setTypeBinding(tb, ((TypeParameter) t).getName());
				b.addGenericParameters(tb.build());
			}
		}

		for (final Object o : node.recordComponents()) {
			fields.push(new ArrayList<boa.types.Ast.Variable>());
			((SingleVariableDeclaration) o).accept(this);
			for (final boa.types.Ast.Variable v : fields.pop())
				b.addFields(v);
		}

		for (final Object t : node.superInterfaceTypes()) {
			final boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
			tb.setName(typeName((org.eclipse.jdt.core.dom.Type) t));
			tb.setKind(boa.types.Ast.TypeKind.INTERFACE);
			setTypeBinding(tb, (org.eclipse.jdt.core.dom.Type) t);
			b.addParents(tb.build());
		}

		for (final Object d : node.bodyDeclarations()) {
			if (d instanceof FieldDeclaration) {
				fields.push(new ArrayList<boa.types.Ast.Variable>());
				((FieldDeclaration) d).accept(this);
				for (final boa.types.Ast.Variable v : fields.pop())
					b.addFields(v);
			} else if (d instanceof MethodDeclaration) {
				methods.push(new ArrayList<boa.types.Ast.Method>());
				((MethodDeclaration) d).accept(this);
				for (final boa.types.Ast.Method m : methods.pop())
					b.addMethods(m);
			} else if (d instanceof Initializer) {
				methods.push(new ArrayList<boa.types.Ast.Method>());
				((Initializer) d).accept(this);
				for (final boa.types.Ast.Method m : methods.pop())
					b.addMethods(m);
			} else {
				declarations.push(new ArrayList<boa.types.Ast.Declaration>());
				((BodyDeclaration) d).accept(this);
				for (final boa.types.Ast.Declaration nd : declarations.pop())
					b.addNestedDeclarations(nd);
			}
		}
		declarations.peek().add(b.build());
		return false;
	}

	//////////////////////////////////////////////////////////////
	// Utility methods

	protected String typeName(final org.eclipse.jdt.core.dom.Type t) {
		if (t.isArrayType())
			return typeName((ArrayType) t);
		if (t.isParameterizedType())
			return typeName((ParameterizedType) t);
		if (t.isPrimitiveType())
			return typeName((PrimitiveType) t);
		if (t.isQualifiedType())
			return typeName((QualifiedType) t);
		if (t.isNameQualifiedType())
			return typeName((NameQualifiedType) t);
		if (t.isIntersectionType())
			return typeName((IntersectionType) t);
		if (t.isUnionType())
			return typeName((UnionType) t);
		if (t.isWildcardType())
			return typeName((WildcardType) t);
		return typeName((SimpleType) t);
	}

	protected String typeName(final ArrayType t) {
		String name = typeName(t.getElementType());
		// FIXME JLS8: Deprecated getDimensions() and added dimensions()
		for (int i = 0; i < t.getDimensions(); i++)
			name += "[]";
		visitDimensions(t.dimensions());
		return name;
	}

	private void visitDimensions(final List<?> dimensions) {
		for (final Object d : dimensions)
			((Dimension) d).accept(this);
	}

	protected String typeName(final ParameterizedType t) {
		setAstLevel(JLS3);

		if (t.typeArguments().isEmpty())
			setAstLevel(JLS7);

		String name = "";
		for (final Object o : t.typeArguments()) {
			if (name.length() > 0) name += ", ";
			name += typeName((org.eclipse.jdt.core.dom.Type) o);
		}
		return typeName(t.getType()) + "<" + name + ">";
	}

	protected String typeName(final PrimitiveType t) {
		visitTypeAnnotations(t);

		return t.getPrimitiveTypeCode().toString();
	}

	protected String typeName(final NameQualifiedType t) {
		setAstLevel(JLS8);

		visitTypeAnnotations(t);

		return t.getQualifier().getFullyQualifiedName() + "." + t.getName().getFullyQualifiedName();
	}

	protected String typeName(final QualifiedType t) {
		visitTypeAnnotations(t);

		return typeName(t.getQualifier()) + "." + t.getName().getFullyQualifiedName();
	}

	protected String typeName(final IntersectionType t) {
		setAstLevel(JLS8);

		String name = "";
		for (final Object o : t.types()) {
			if (name.length() > 0) name += " & ";
			name += typeName((org.eclipse.jdt.core.dom.Type) o);
		}
		return name;
	}

	protected String typeName(final UnionType t) {
		setAstLevel(JLS7);

		String name = "";
		for (final Object o : t.types()) {
			if (name.length() > 0) name += " | ";
			name += typeName((org.eclipse.jdt.core.dom.Type) o);
		}
		return name;
	}

	protected String typeName(final WildcardType t) {
		setAstLevel(JLS3);

		visitTypeAnnotations(t);

		String name = "?";
		if (t.getBound() != null) {
			name += " " + (t.isUpperBound() ? "extends" : "super");
			name += " " + typeName(t.getBound());
		}
		return name;
	}

	protected String typeName(final SimpleType t) {
		visitTypeAnnotations(t);

		return t.getName().getFullyQualifiedName();
	}

	//////////////////////////////////////////////////////////////
	// Unused node types

	@Override
	public boolean visit(final ArrayType node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(final ParameterizedType node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(final PrimitiveType node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(final QualifiedType node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(final NameQualifiedType node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(final SimpleType node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(final IntersectionType node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(final UnionType node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(final WildcardType node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(final ImportDeclaration node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(final PackageDeclaration node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(final SingleVariableDeclaration node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(final MemberValuePair node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(final TypeParameter node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(final VariableDeclarationFragment node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}
}
