/*
 * Copyright 2017-2021, Hridesh Rajan, Robert Dyer, Hoan Nguyen, Farheen Sultana
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
 */
public class JavaVisitor extends ASTVisitor {
	public static final String PROPERTY_INDEX = "i";
	@SuppressWarnings("deprecation")
	public static final int JLS1 = 1;
	public static final int JLS2 = 2;
	public static final int JLS3 = 3;
	public static final int JLS4 = 4;
	public static final int JLS8 = 8;
	public static final int JLS9 = 9;
	public static final int JLS10 = 10;
	public static final int JLS12 = 12;
	public static final int JLS13 = 13;
	public static final int JLS14 = 14;
	public static final int JLS15 = 15;
	public static final int JLS16 = 16;
	
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

	public void setAstLevel(int astLevel) {
		if (this.astLevel < astLevel)
			this.astLevel = astLevel;
	}

	public JavaVisitor(String src) {
		super();
		this.src = src;
	}

	public JavaVisitor(String src, Map<String, Integer> declarationFile, Map<String, Integer> declarationNode) {
		this(src);
		this.declarationFile = declarationFile;
		this.declarationNode = declarationNode;
	}

	public Namespace getNamespaces(CompilationUnit node) {
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

/*
	// builds a Position message for every node and stores in the field pos
	public void preVisit(ASTNode node) {
		buildPosition(node);
	}
*/

	@Override
	public boolean visit(CompilationUnit node) {
		System.out.println("============= This is from CompilationUnit ");
//		for(int i = 0; i < node.getLength(); i ++) {
//			System.out.println("This is length: " + i + "----------" + node.getNodeType());
//		}
		PackageDeclaration pkg = node.getPackage();
		if (pkg == null) {
			b.setName("");
		} else {
			b.setName(pkg.getName().getFullyQualifiedName());
			if (!pkg.annotations().isEmpty() || pkg.getJavadoc() != null)
				setAstLevel(JLS3);

			b.addAllModifiers(visitAnnotationsList(pkg.annotations()));
		}
		for (Object i : node.imports()) {
			ImportDeclaration id = (ImportDeclaration)i;
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
		
		System.out.println("types size is: " + node.types().size());
		
		for (Object t : node.types()) {
			System.out.println("************** Body from method declaration and the statements size is: " + (TypeDeclaration)t);
			declarations.push(new ArrayList<boa.types.Ast.Declaration>());
			((AbstractTypeDeclaration)t).accept(this);
			for (boa.types.Ast.Declaration d : declarations.pop())
				b.addDeclarations(d);
		}
		for (Object c : node.getCommentList())
			((org.eclipse.jdt.core.dom.Comment)c).accept(this);
		return false;
	}

	@Override
	public boolean visit(BlockComment node) {
		boa.types.Ast.Comment.Builder b = boa.types.Ast.Comment.newBuilder();
		buildPosition(node);
		b.setPosition(pos.build());
		b.setKind(boa.types.Ast.Comment.CommentKind.BLOCK);
		b.setValue(src.substring(node.getStartPosition(), node.getStartPosition() + node.getLength()));
		comments.add(b.build());
		return false;
	}

	@Override
	public boolean visit(LineComment node) {
		boa.types.Ast.Comment.Builder b = boa.types.Ast.Comment.newBuilder();
		buildPosition(node);
		b.setPosition(pos.build());
		b.setKind(boa.types.Ast.Comment.CommentKind.LINE);
		b.setValue(src.substring(node.getStartPosition(), node.getStartPosition() + node.getLength()));
		comments.add(b.build());
		return false;
	}

	@Override
	public boolean visit(Javadoc node) {
		boa.types.Ast.Comment.Builder b = boa.types.Ast.Comment.newBuilder();
		buildPosition(node);
		b.setPosition(pos.build());
		b.setKind(boa.types.Ast.Comment.CommentKind.DOC);
		b.setValue(src.substring(node.getStartPosition(), node.getStartPosition() + node.getLength()));
		comments.add(b.build());
		for (Iterator<?> it = node.tags().iterator(); it.hasNext(); ) {
			ASTNode e = (ASTNode) it.next();
			e.accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(TagElement node) {
		setAstLevel(JLS2);

		if (node.getTagName() != null) {
			String name = node.getTagName();
			if (name.equals("@literal") || name.equals("@code"))
				setAstLevel(JLS3);
		}
		for (Iterator<?> it = node.fragments().iterator(); it.hasNext(); ) {
			ASTNode e = (ASTNode) it.next();
			e.accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(TextElement node) {
		return false;
	}

	@Override
	public boolean visit(MemberRef node) {
		setAstLevel(JLS2);

		return false;
	}

	@Override
	public boolean visit(MethodRef node) {
		setAstLevel(JLS2);

		for (Iterator<?> it = node.parameters().iterator(); it.hasNext(); ) {
			MethodRefParameter e = (MethodRefParameter) it.next();
			e.accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(MethodRefParameter node) {
		typeName(node.getType());
		if (node.isVarargs()) {
			setAstLevel(JLS3);
		}
		return false;
	}

	//////////////////////////////////////////////////////////////
	// Type Declarations

	@Override
	public boolean visit(TypeDeclaration node) {
		boa.types.Ast.Declaration.Builder b = boa.types.Ast.Declaration.newBuilder();
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

			for (Object t : node.typeParameters()) {
				if (!((TypeParameter) t).modifiers().isEmpty())
					setAstLevel(JLS8);

				boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
				String name = ((TypeParameter)t).getName().getFullyQualifiedName();
				String bounds = "";
				for (Object o : ((TypeParameter)t).typeBounds()) {
					if (bounds.length() > 0)
						bounds += " & ";
					bounds += typeName((org.eclipse.jdt.core.dom.Type)o);
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
			boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
			tb.setName(typeName(node.getSuperclassType()));
			tb.setKind(boa.types.Ast.TypeKind.CLASS);
			setTypeBinding(tb, node.getSuperclassType());
			b.addParents(tb.build());
		}
		for (Object t : node.superInterfaceTypes()) {
			boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
			tb.setName(typeName((org.eclipse.jdt.core.dom.Type) t));
			tb.setKind(boa.types.Ast.TypeKind.INTERFACE);
			setTypeBinding(tb, (org.eclipse.jdt.core.dom.Type) t);
			b.addParents(tb.build());
		}
		for (Object d : node.bodyDeclarations()) {
			if (d instanceof FieldDeclaration) {
				fields.push(new ArrayList<boa.types.Ast.Variable>());
				((FieldDeclaration) d).accept(this);
				for (boa.types.Ast.Variable v : fields.pop())
					b.addFields(v);
			} else if (d instanceof MethodDeclaration) {
				System.out.println("||||||||||||||||||||||||||||This is from TypeDeclaration and statements size is: " + ((MethodDeclaration) d));
				methods.push(new ArrayList<boa.types.Ast.Method>());
				((MethodDeclaration) d).accept(this);
				for (boa.types.Ast.Method m : methods.pop()){
					if (b.getKind().equals(boa.types.Ast.TypeKind.INTERFACE)) {
						for (Modifier mod: m.getModifiersList()) {
							if (mod.getKind().equals(boa.types.Ast.Modifier.ModifierKind.STATIC))
								setAstLevel(JLS8);
						}
					}

					b.addMethods(m);
				}
			} else if (d instanceof Initializer) {
				methods.push(new ArrayList<boa.types.Ast.Method>());
				((Initializer) d).accept(this);
				for (boa.types.Ast.Method m : methods.pop())
					b.addMethods(m);
			} else {
				declarations.push(new ArrayList<boa.types.Ast.Declaration>());
				((BodyDeclaration) d).accept(this);
				for (boa.types.Ast.Declaration nd : declarations.pop())
					b.addNestedDeclarations(nd);
			}
		}
		declarations.peek().add(b.build());
		return false;
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		boa.types.Ast.Declaration.Builder b = boa.types.Ast.Declaration.newBuilder();
		b.setName("");
		b.setKind(boa.types.Ast.TypeKind.ANONYMOUS);
//		b.setFullyQualifiedName(getFullyQualifiedName(node));
		for (Object d : node.bodyDeclarations()) {
			if (d instanceof FieldDeclaration) {
				fields.push(new ArrayList<boa.types.Ast.Variable>());
				((FieldDeclaration)d).accept(this);
				for (boa.types.Ast.Variable v : fields.pop())
					b.addFields(v);
			} else if (d instanceof MethodDeclaration) {
				methods.push(new ArrayList<boa.types.Ast.Method>());
				((MethodDeclaration)d).accept(this);
				for (boa.types.Ast.Method m : methods.pop())
					b.addMethods(m);
			} else if (d instanceof Initializer) {
				methods.push(new ArrayList<boa.types.Ast.Method>());
				((Initializer)d).accept(this);
				for (boa.types.Ast.Method m : methods.pop())
					b.addMethods(m);
			} else {
				declarations.push(new ArrayList<boa.types.Ast.Declaration>());
				((BodyDeclaration)d).accept(this);
				for (boa.types.Ast.Declaration nd : declarations.pop())
					b.addNestedDeclarations(nd);
			}
		}
		declarations.peek().add(b.build());
		return false;
	}

	@Override
	public boolean visit(EnumDeclaration node) {
		setAstLevel(JLS3);

		boa.types.Ast.Declaration.Builder b = boa.types.Ast.Declaration.newBuilder();
		b.setName(node.getName().getIdentifier());
		System.out.println("+++++++++++++++++++++++++ enum name is: " + node.getName().toString()+ "+++++++++++++++++++++++++");
		b.setKind(boa.types.Ast.TypeKind.ENUM);
		b.setFullyQualifiedName(getFullyQualifiedName(node));
		setDeclaringClass(b, node.resolveBinding());
		for (Object c : node.enumConstants()) {
			fields.push(new ArrayList<boa.types.Ast.Variable>());
			((EnumConstantDeclaration) c).accept(this);
			for (boa.types.Ast.Variable v : fields.pop())
				b.addFields(v);
		}
		for (Object t : node.superInterfaceTypes()) {
			boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
			tb.setName(typeName((org.eclipse.jdt.core.dom.Type) t));
			tb.setKind(boa.types.Ast.TypeKind.INTERFACE);
			setTypeBinding(tb, (org.eclipse.jdt.core.dom.Type) t);
			b.addParents(tb.build());
		}
		for (Object d : node.bodyDeclarations()) {
			if (d instanceof FieldDeclaration) {
				fields.push(new ArrayList<boa.types.Ast.Variable>());
				((FieldDeclaration)d).accept(this);
				for (boa.types.Ast.Variable v : fields.pop())
					b.addFields(v);
			} else if (d instanceof MethodDeclaration) {
				methods.push(new ArrayList<boa.types.Ast.Method>());
				((MethodDeclaration)d).accept(this);
				for (boa.types.Ast.Method m : methods.pop())
					b.addMethods(m);
			} else if (d instanceof Initializer) {
				methods.push(new ArrayList<boa.types.Ast.Method>());
				((Initializer)d).accept(this);
				for (boa.types.Ast.Method m : methods.pop())
					b.addMethods(m);
			} else {
				declarations.push(new ArrayList<boa.types.Ast.Declaration>());
				((BodyDeclaration)d).accept(this);
				for (boa.types.Ast.Declaration nd : declarations.pop())
					b.addNestedDeclarations(nd);
			}
		}
		declarations.peek().add(b.build());
		return false;
	}

	@Override
	public boolean visit(AnnotationTypeDeclaration node) {
		setAstLevel(JLS3);

		boa.types.Ast.Declaration.Builder b = boa.types.Ast.Declaration.newBuilder();
		b.setName(node.getName().getFullyQualifiedName());
		b.setKind(boa.types.Ast.TypeKind.ANNOTATION);
		b.setFullyQualifiedName(getFullyQualifiedName(node));
		setDeclaringClass(b, node.resolveBinding());
		b.addAllModifiers(buildModifiers(node.modifiers()));
		for (Object d : node.bodyDeclarations()) {
			if (d instanceof FieldDeclaration) {
				fields.push(new ArrayList<boa.types.Ast.Variable>());
				((FieldDeclaration)d).accept(this);
				for (boa.types.Ast.Variable v : fields.pop())
					b.addFields(v);
			} else if (d instanceof Initializer) {
				methods.push(new ArrayList<boa.types.Ast.Method>());
				((Initializer)d).accept(this);
				for (boa.types.Ast.Method m : methods.pop())
					b.addMethods(m);
			} else if (d instanceof AnnotationTypeMemberDeclaration) {
				methods.push(new ArrayList<boa.types.Ast.Method>());
				((AnnotationTypeMemberDeclaration)d).accept(this);
				for (boa.types.Ast.Method m : methods.pop())
					b.addMethods(m);
			} else if (d instanceof MethodDeclaration) {
				methods.push(new ArrayList<boa.types.Ast.Method>());
				((MethodDeclaration)d).accept(this);
				for (boa.types.Ast.Method m : methods.pop())
					b.addMethods(m);
			} else {
				declarations.push(new ArrayList<boa.types.Ast.Declaration>());
				((BodyDeclaration)d).accept(this);
				for (boa.types.Ast.Declaration nd : declarations.pop())
					b.addNestedDeclarations(nd);
			}
		}
		declarations.peek().add(b.build());
		return false;
	}

	//////////////////////////////////////////////////////////////
	// Utilities

	protected void setDeclaringClass(Declaration.Builder db, ITypeBinding binding) {
		if (declarationNode == null || binding == null)
			return;
		ITypeBinding tb = binding.getDeclaringClass();
		if (tb != null) {
			if (tb.getTypeDeclaration() != null)
				tb = tb.getTypeDeclaration();
			String key = tb.getKey();
			Integer index = declarationNode.get(key);
			if (index != null)
				db.setDeclaringType(index);
		}
	}

	protected void setDeclaringClass(Method.Builder b, IMethodBinding binding) {
		if (declarationNode == null || binding == null)
			return;
		ITypeBinding tb = binding.getDeclaringClass();
		if (tb == null)
			return;
		if (tb.getTypeDeclaration() != null)
			tb = tb.getTypeDeclaration();
		String key = tb.getKey();
		Integer index = declarationNode.get(key);
		if (index != null)
			b.setDeclaringType(index);
	}

	protected void setDeclaringClass(Variable.Builder b, IVariableBinding binding) {
		if (declarationNode == null || binding == null)
			return;
		ITypeBinding tb = binding.getDeclaringClass();
		if (tb == null)
			return;
		if (tb.getTypeDeclaration() != null)
			tb = tb.getTypeDeclaration();
		String key = tb.getKey();
		Integer index = declarationNode.get(key);
		if (index != null)
			b.setDeclaringType(index);
	}
	
	
	protected void setTypeBinding(boa.types.Ast.Type.Builder b, org.eclipse.jdt.core.dom.Type type) {
		ITypeBinding tb = type.resolveBinding();
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
					name = tb.getQualifiedName();
				} catch (java.lang.NullPointerException e) {

				}
				b.setFullyQualifiedName(name);
				if (declarationFile != null && !tb.isArray()) {
					String key = tb.getKey();
					Integer index = declarationFile.get(key);
					if (index != null) {
						b.setDeclarationFile(index);
						b.setDeclaration(declarationNode.get(key));
					}
				}
			}
		}
	}

	protected void setTypeBinding(boa.types.Ast.Type.Builder b, org.eclipse.jdt.core.dom.Expression e) {
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
				} catch (Exception ex) {
					System.err.println("Error getting type name while visiting java file" );
					ex.printStackTrace();
				}
				b.setFullyQualifiedName(name);
				if (declarationFile != null && !tb.isArray()) {
					String key = tb.getKey();
					Integer index = declarationFile.get(key);
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
		boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
		String name = "";
		try {
			name = itb.getName();
		} catch (Exception e) {
			System.err.println("Error getting type name while visiting java file" );
			e.printStackTrace();
		}
		tb.setName(name); //itb.getName());
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
			} catch (java.lang.NullPointerException e) {
				System.err.println("Error getting qualified type name while visiting java file" );
				e.printStackTrace();
			}
			tb.setFullyQualifiedName(name);
			if (declarationFile != null && !itb.isArray()) {
				String key = itb.getKey();
				Integer index = declarationFile.get(key);
				if (index != null) {
					tb.setDeclarationFile(index);
					tb.setDeclaration(declarationNode.get(key));
				}
			}
		}
		return tb.build();
	}

	protected void buildPosition(final ASTNode node) {
		pos = PositionInfo.newBuilder();
		int start = node.getStartPosition();
		int length = node.getLength();
		pos.setStartPos(start);
		pos.setLength(length);
		pos.setStartLine(root.getLineNumber(start));
		pos.setStartCol(root.getColumnNumber(start));
		pos.setEndLine(root.getLineNumber(start + length));
		pos.setEndCol(root.getColumnNumber(start + length));
	}

	private List<Modifier> buildModifiers(List<?> modifiers) {
		List<Modifier> ms = new ArrayList<Modifier>();
		Set<String> names = new HashSet<String>();
		for (Object m : modifiers) {
			if (((IExtendedModifier) m).isAnnotation()) {
				setAstLevel(JLS3);
				Annotation annot = (Annotation) m;
				String name = annot.getTypeName().getFullyQualifiedName();
				if (names.contains(name))
					setAstLevel(JLS8);
				else
					names.add(name);
				annot.accept(this);
			} else
				((org.eclipse.jdt.core.dom.Modifier) m).accept(this);
			ms.add(this.modifiers.pop());
		}
		return ms;
	}

	private void visitTypeAnnotations(AnnotatableType t) {
		if (!t.annotations().isEmpty())
			setAstLevel(JLS8);
//		visitAnnotationsList(node.annotations());
	}

	private List<Modifier> visitAnnotationsList(List<?> annotations) {
		List<Modifier> ms = new ArrayList<Modifier>();
		Set<String> names = new HashSet<String>();
		for (Object a : annotations) {
			Annotation annot = (Annotation) a;
			String name = annot.getTypeName().getFullyQualifiedName();
			if (names.contains(name))
				setAstLevel(JLS8);
			else
				names.add(name);
			annot.accept(this);
			ms.add(modifiers.pop());
		}

		return ms;
	}

	private Variable.Builder build(SingleVariableDeclaration svd, TypeKind kind) {
		Variable.Builder vb = Variable.newBuilder();
		vb.setName(svd.getName().getFullyQualifiedName());
		if (svd.isVarargs()) {
			setAstLevel(JLS3);
			if (!svd.varargsAnnotations().isEmpty())
				setAstLevel(JLS8);
		}
		vb.addAllModifiers(buildModifiers(svd.modifiers()));
		boa.types.Ast.Type.Builder tp = boa.types.Ast.Type.newBuilder();
		String name = typeName(svd.getType());
		// FIXME process extra dimensions in JLS 8
		visitDimensions(svd.extraDimensions());
		for (int i = 0; i < svd.extraDimensions().size(); i++)
			name += "[]";
		if (svd.isVarargs())
			name += "...";
		tp.setName(name);
		tp.setKind(kind);
		setTypeBinding(tp, svd.getType());
		vb.setVariableType(tp.build());
		if (svd.getInitializer() != null) {
			svd.getInitializer().accept(this);
			vb.setInitializer(expressions.pop());
		}

		return vb;
	}

	//////////////////////////////////////////////////////////////
	// Field/Method Declarations

	@Override
	public boolean visit(MethodDeclaration node) {
		List<boa.types.Ast.Method> list = methods.peek();
		Method.Builder b = Method.newBuilder();
		if (node.isConstructor())
			b.setName("<init>");
		else {
			b.setName(node.getName().getFullyQualifiedName());

			boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
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

			for (Object t : node.typeParameters()) {
				if (!((TypeParameter) t).modifiers().isEmpty())
					setAstLevel(JLS8);

				boa.types.Ast.Type.Builder tp = boa.types.Ast.Type.newBuilder();
				String name = ((TypeParameter)t).getName().getFullyQualifiedName();
				String bounds = "";
				for (Object o : ((TypeParameter)t).typeBounds()) {
					if (bounds.length() > 0)
						bounds += " & ";
					bounds += typeName((org.eclipse.jdt.core.dom.Type)o);
				}
				if (bounds.length() > 0)
					name = name + " extends " + bounds;
				tp.setName(name);
				tp.setKind(boa.types.Ast.TypeKind.GENERIC);
				setTypeBinding(tp, ((TypeParameter)t).getName());
				b.addGenericParameters(tp.build());
			}
		}
		if (node.getReceiverType() != null) {
			setAstLevel(JLS8);

			Variable.Builder vb = Variable.newBuilder();
			vb.setName("this");
			boa.types.Ast.Type.Builder tp = boa.types.Ast.Type.newBuilder();
			String name = typeName(node.getReceiverType());
			if (node.getReceiverQualifier() != null) name = node.getReceiverQualifier().getFullyQualifiedName() + "." + name;
			tp.setName(name);
			tp.setKind(boa.types.Ast.TypeKind.OTHER);
			setTypeBinding(tp, node.getReceiverType());
			vb.setVariableType(tp.build());
			b.addArguments(vb.build());
		}
		for (Object o : node.parameters()) {
			SingleVariableDeclaration ex = (SingleVariableDeclaration)o;
			b.addArguments(build(ex, TypeKind.OTHER));
		}
		for (Object o : node.thrownExceptionTypes()) {
			boa.types.Ast.Type.Builder tp = boa.types.Ast.Type.newBuilder();
			tp.setName(typeName((org.eclipse.jdt.core.dom.Type) o));
			tp.setKind(boa.types.Ast.TypeKind.CLASS);
			setTypeBinding(tp, (org.eclipse.jdt.core.dom.Type) o);
			b.addExceptionTypes(tp.build());
		}
		if (node.getBody() != null) {
			statements.push(new ArrayList<boa.types.Ast.Statement>());
			node.getBody().accept(this);
			for (boa.types.Ast.Statement s : statements.pop())
				b.addStatements(s);
		}
		setDeclaringClass(b, node.resolveBinding());
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(AnnotationTypeMemberDeclaration node) {
		setAstLevel(JLS3);

		List<boa.types.Ast.Method> list = methods.peek();
		Method.Builder b = Method.newBuilder();
		b.setName(node.getName().getFullyQualifiedName());
		b.addAllModifiers(buildModifiers(node.modifiers()));
		boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
		tb.setName(typeName(node.getType()));
		tb.setKind(boa.types.Ast.TypeKind.OTHER);
		setTypeBinding(tb, node.getType());
		b.setReturnType(tb.build());
		if (node.getDefault() != null) {
			if (node.getDefault() instanceof Annotation) {
				// FIXME
			} else {
				boa.types.Ast.Statement.Builder sb = boa.types.Ast.Statement.newBuilder();
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
	public boolean visit(FieldDeclaration node) {
		List<boa.types.Ast.Variable> list = fields.peek();
		for (Object o : node.fragments()) {
			VariableDeclarationFragment f = (VariableDeclarationFragment)o;
			list.add(build(f, node.getType(), node.modifiers(), TypeKind.OTHER).build());
		}
		return false;
	}

	private Variable.Builder build(VariableDeclarationFragment f, org.eclipse.jdt.core.dom.Type type, List<?> modifiers, TypeKind kind) {
		Variable.Builder b = Variable.newBuilder();
		b.setName(f.getName().getFullyQualifiedName());
		b.addAllModifiers(buildModifiers(modifiers));
		boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
		String name = typeName(type);
		// FIXME process extra dimensions in JLS 8
		visitDimensions(f.extraDimensions());
		for (int i = 0; i < f.getExtraDimensions(); i++)
			name += "[]";
		tb.setName(name);
		tb.setKind(kind);
		setTypeBinding(tb, type);
		b.setVariableType(tb.build());
		if (f.getInitializer() != null) {
			f.getInitializer().accept(this);
			b.setInitializer(expressions.pop());
		}
		setDeclaringClass(b, f.resolveBinding());
		return b;
	}

	@Override
	public boolean visit(EnumConstantDeclaration node) {
		setAstLevel(JLS3);

		List<boa.types.Ast.Variable> list = fields.peek();
		Variable.Builder b = Variable.newBuilder();
		b.setName(node.getName().getIdentifier());
		b.addAllModifiers(buildModifiers(node.modifiers()));
		for (Object arg : node.arguments()) {
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

	protected boa.types.Ast.Modifier.Builder getAnnotationBuilder(Annotation node) {
		boa.types.Ast.Modifier.Builder b = boa.types.Ast.Modifier.newBuilder();
		b.setKind(boa.types.Ast.Modifier.ModifierKind.ANNOTATION);
		b.setAnnotationName(node.getTypeName().getFullyQualifiedName());
		return b;
	}

	@Override
	public boolean visit(MarkerAnnotation node) {
		setAstLevel(JLS3);

		String name = node.getTypeName().getFullyQualifiedName();
		if (name.equals("SafeVarargs") || name.equals("SuppressWarnings"))
			setAstLevel(JLS4);

		modifiers.push(getAnnotationBuilder(node).build());
		return false;
	}

	@Override
	public boolean visit(SingleMemberAnnotation node) {
		setAstLevel(JLS3);

		String name = node.getTypeName().getFullyQualifiedName();
		if (name.equals("SafeVarargs") || name.equals("SuppressWarnings"))
			setAstLevel(JLS4);

		boa.types.Ast.Modifier.Builder b = getAnnotationBuilder(node);
		node.getValue().accept(this);
		if (expressions.empty()) {
			boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
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
	public boolean visit(NormalAnnotation node) {
		setAstLevel(JLS3);

		String name = node.getTypeName().getFullyQualifiedName();
		if (name.equals("SafeVarargs") || name.equals("SuppressWarnings"))
			setAstLevel(JLS4);

		boa.types.Ast.Modifier.Builder b = getAnnotationBuilder(node);
		for (Object v : node.values()) {
			MemberValuePair pair = (MemberValuePair)v;
			pair.getValue().accept(this);
			if (expressions.empty()) {
				boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
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
	public boolean visit(org.eclipse.jdt.core.dom.Modifier node) {
		boa.types.Ast.Modifier.Builder b = boa.types.Ast.Modifier.newBuilder();
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
	public boolean visit(Dimension node) {
		if (!node.annotations().isEmpty())
			setAstLevel(JLS8);
		return false;
	}

	//////////////////////////////////////////////////////////////
	// Statements

	@Override
	public boolean visit(AssertStatement node) {
		setAstLevel(JLS2);

		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
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
	public boolean visit(Block node) {
		System.out.println("======================================================================");
		System.out.println("************The statements size block is: " + node.statements().size());
		System.out.println("======================================================================");
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.BLOCK);
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		for (Object s : node.statements()) {
			if(s instanceof VariableDeclarationStatement) {
				System.out.println("========================== Statement pass block ============================================");
			}
			((org.eclipse.jdt.core.dom.Statement)s).accept(this);
		}
		for (boa.types.Ast.Statement st : statements.pop())
			b.addStatements(st);
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(BreakStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.BREAK);
		if (node.getLabel() != null) {
			boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
			eb.setLiteral(node.getLabel().getFullyQualifiedName());
			eb.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
			b.addExpressions(eb.build());
		}
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(CatchClause node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.CATCH);
		SingleVariableDeclaration ex = node.getException();
		b.setVariableDeclaration(build(ex, TypeKind.CLASS));
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		for (Object s : node.getBody().statements())
			((org.eclipse.jdt.core.dom.Statement)s).accept(this);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(ConstructorInvocation node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.EXPRESSION);
		boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		if (node.resolveConstructorBinding() != null) {
			IMethodBinding mb = node.resolveConstructorBinding();
			if (mb.getReturnType() != null)
				eb.setReturnType(buildType(mb.getReturnType()));
			if (mb.getDeclaringClass() != null)
				eb.setDeclaringType(buildType(mb.getDeclaringClass()));
		}
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.METHODCALL);
		eb.setMethod("<init>");
		for (Object a : node.arguments()) {
			((org.eclipse.jdt.core.dom.Expression)a).accept(this);
			eb.addMethodArgs(expressions.pop());
		}
		if (node.typeArguments() != null && !node.typeArguments().isEmpty()) {
			setAstLevel(JLS3);
			for (Object t : node.typeArguments()) {
				boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
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
	public boolean visit(ContinueStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.CONTINUE);
		if (node.getLabel() != null) {
			boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
			eb.setLiteral(node.getLabel().getFullyQualifiedName());
			eb.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
			b.addExpressions(eb.build());
		}
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(DoStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.DO);
		node.getExpression().accept(this);
		b.addConditions(expressions.pop());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getBody().accept(this);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(EmptyStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.EMPTY);
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		setAstLevel(JLS3);

		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.FOREACH);
		SingleVariableDeclaration ex = node.getParameter();
		b.setVariableDeclaration(build(ex, TypeKind.OTHER));
		node.getExpression().accept(this);
		b.addExpressions(expressions.pop());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getBody().accept(this);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(ExpressionStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.EXPRESSION);
		node.getExpression().accept(this);
		b.addExpressions(expressions.pop());
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(ForStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.FOR);
		for (Object e : node.initializers()) {
			((org.eclipse.jdt.core.dom.Expression)e).accept(this);
			b.addInitializations(expressions.pop());
		}
		for (Object e : node.updaters()) {
			((org.eclipse.jdt.core.dom.Expression)e).accept(this);
			b.addUpdates(expressions.pop());
		}
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
			b.addConditions(expressions.pop());
		}
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getBody().accept(this);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(IfStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.IF);
		node.getExpression().accept(this);
		b.addConditions(expressions.pop());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getThenStatement().accept(this);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		if (node.getElseStatement() != null) {
			statements.push(new ArrayList<boa.types.Ast.Statement>());
			node.getElseStatement().accept(this);
			for (boa.types.Ast.Statement s : statements.pop())
				b.addStatements(s);
		}
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(Initializer node) {
		List<boa.types.Ast.Method> list = methods.peek();
		Method.Builder b = Method.newBuilder();
		b.setName("<clinit>");
		b.addAllModifiers(buildModifiers(node.modifiers()));
		boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
		tb.setName("void");
		tb.setKind(boa.types.Ast.TypeKind.PRIMITIVE);
		b.setReturnType(tb.build());
		if (node.getBody() != null) {
			statements.push(new ArrayList<boa.types.Ast.Statement>());
			node.getBody().accept(this);
			for (boa.types.Ast.Statement s : statements.pop())
				b.addStatements(s);
		}
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(LabeledStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.LABEL);
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getBody().accept(this);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		eb.setLiteral(node.getLabel().getFullyQualifiedName());
		b.addExpressions(eb.build());
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(ReturnStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.RETURN);
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
			b.addExpressions(expressions.pop());
		}
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(SuperConstructorInvocation node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.EXPRESSION);
		boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		if (node.resolveConstructorBinding() != null) {
			IMethodBinding mb = node.resolveConstructorBinding();
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
		for (Object a : node.arguments()) {
			((org.eclipse.jdt.core.dom.Expression)a).accept(this);
			eb.addMethodArgs(expressions.pop());
		}
		if (!node.typeArguments().isEmpty()) {
			setAstLevel(JLS3);

			for (Object t : node.typeArguments()) {
				boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
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
	public boolean visit(SwitchCase node) {
		if (node.expressions() != null) {
			if (node.expressions().size() > 1)
				setAstLevel(JLS14);
			else if (node.expressions().size() > 0 && node.expressions().get(0) instanceof StringLiteral)
				setAstLevel(JLS4);
		}
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		if (node.isDefault())
			b.setKind(boa.types.Ast.Statement.StatementKind.DEFAULT);
		else
			b.setKind(boa.types.Ast.Statement.StatementKind.CASE);
		if (node.expressions() != null) {
			for (Object o : node.expressions()) {
				((ASTNode)o).accept(this);
				b.addExpressions(expressions.pop());
			}
		}
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(SwitchStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.SWITCH);
		node.getExpression().accept(this);
		b.addExpressions(expressions.pop());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		for (Object s : node.statements())
			((org.eclipse.jdt.core.dom.Statement)s).accept(this);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(SynchronizedStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.SYNCHRONIZED);
		node.getExpression().accept(this);
		b.addExpressions(expressions.pop());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		for (Object s : node.getBody().statements())
			((org.eclipse.jdt.core.dom.Statement)s).accept(this);
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
		b.addExpressions(expressions.pop());
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
		if (node.getFinally() != null)
			visitFinally(node.getFinally());
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		if (!node.resources().isEmpty())
			setAstLevel(JLS4);

			for (Object v : node.resources()) {
				((VariableDeclarationExpression)v).accept(this);
				b.addInitializations(expressions.pop());
			}
		list.add(b.build());
		return false;
	}

	private void visitFinally(Block node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		final List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.FINALLY);
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		for (final Object s : node.statements()) {
			((org.eclipse.jdt.core.dom.Statement)s).accept(this);
		}
		for (boa.types.Ast.Statement st : statements.pop())
			b.addStatements(st);
		list.add(b.build());
	}

	@Override
	public boolean visit(TypeDeclarationStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.TYPEDECL);
		declarations.push(new ArrayList<boa.types.Ast.Declaration>());
		node.getDeclaration().accept(this);
		for (boa.types.Ast.Declaration d : declarations.pop())
			b.setTypeDeclaration(d);
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.EXPRESSION);
		boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.VARDECL);
		for (Object o : node.fragments()) {
			VariableDeclarationFragment f = (VariableDeclarationFragment) o;
			eb.addVariableDecls(build(f, node.getType(), node.modifiers(), TypeKind.OTHER));
		}
		b.addExpressions(eb.build());
		list.add(b.build());
		return false;
	}

	@Override
	public boolean visit(WhileStatement node) {
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.WHILE);
		node.getExpression().accept(this);
		b.addConditions(expressions.pop());
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		node.getBody().accept(this);
		for (boa.types.Ast.Statement s : statements.pop())
			b.addStatements(s);
		list.add(b.build());
		return false;
	}

	//////////////////////////////////////////////////////////////
	// Expressions

	@Override
	public boolean visit(ArrayAccess node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
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
	public boolean visit(ArrayCreation node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		if (node.resolveTypeBinding() != null) {
			b.setReturnType(buildType(node.resolveTypeBinding()));
		}
		b.setKind(boa.types.Ast.Expression.ExpressionKind.NEWARRAY);
		boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
		tb.setName(typeName(node.getType()));
		tb.setKind(boa.types.Ast.TypeKind.ARRAY);
		setTypeBinding(tb, node.getType());
		b.setNewType(tb.build());
		for (Object e : node.dimensions()) {
			((org.eclipse.jdt.core.dom.Expression)e).accept(this);
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
	public boolean visit(ArrayInitializer node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		if (node.resolveTypeBinding() != null) {
			b.setReturnType(buildType(node.resolveTypeBinding()));
		}
		b.setKind(boa.types.Ast.Expression.ExpressionKind.ARRAYINIT);
		for (Object e : node.expressions()) {
			((org.eclipse.jdt.core.dom.Expression)e).accept(this);
			if (expressions.empty()) {
				// FIXME is it only possible from JLS8
				boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
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
	public boolean visit(Assignment node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
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
	public boolean visit(BooleanLiteral node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		if (node.booleanValue())
			b.setLiteral("true");
		else
			b.setLiteral("false");
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(CastExpression node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.CAST);
		boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
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
	public boolean visit(CharacterLiteral node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		b.setLiteral(node.getEscapedValue());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.NEW);
		boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
		tb.setName(typeName(node.getType()));
		setTypeBinding(tb, node.getType());
		tb.setKind(boa.types.Ast.TypeKind.CLASS);
		b.setNewType(tb.build());
		if (node.typeArguments() != null && !node.typeArguments().isEmpty()) {
			setAstLevel(JLS3);
			for (Object t : node.typeArguments()) {
				boa.types.Ast.Type.Builder gtb = boa.types.Ast.Type.newBuilder();
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
		for (Object a : node.arguments()) {
			((org.eclipse.jdt.core.dom.Expression)a).accept(this);
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
	public boolean visit(ConditionalExpression node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
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
	public boolean visit(FieldAccess node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		if (node.resolveFieldBinding() != null) {
			IVariableBinding vb = node.resolveFieldBinding();
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
	public boolean visit(SimpleName node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		if (node.resolveBinding() != null) {
			if (node.resolveBinding() instanceof IVariableBinding) {
				IVariableBinding vb = (IVariableBinding) node.resolveBinding();
				if (vb.isField())
					b.setIsMemberAccess(true);
				if(vb.getType() != null)
					b.setReturnType(buildType(vb.getType()));
				if (vb.getDeclaringClass() != null)
					b.setDeclaringType(buildType(vb.getDeclaringClass()));
			} else
				b.setReturnType(buildType(node.resolveTypeBinding()));
		}
		b.setKind(boa.types.Ast.Expression.ExpressionKind.VARACCESS);
		b.setVariable(node.getFullyQualifiedName());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(QualifiedName node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		if (node.resolveBinding() != null && node.resolveBinding() instanceof IVariableBinding) {
			IVariableBinding vb = (IVariableBinding) node.resolveBinding();
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
	public boolean visit(InfixExpression node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
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
		List<org.eclipse.jdt.core.dom.Expression> operands = new ArrayList<org.eclipse.jdt.core.dom.Expression>();
		getOperands(node, operands);
		for (org.eclipse.jdt.core.dom.Expression e : operands) {
			e.accept(this);
			b.addExpressions(expressions.pop());
		}
		expressions.push(b.build());
		return false;
	}

	private void getOperands(InfixExpression node, List<org.eclipse.jdt.core.dom.Expression> operands) {
		addOperand(node.getOperator(), operands, node.getLeftOperand());
		addOperand(node.getOperator(), operands, node.getRightOperand());
		for (int i = 0; i < node.extendedOperands().size(); i++) {
			org.eclipse.jdt.core.dom.Expression e = (org.eclipse.jdt.core.dom.Expression) node.extendedOperands().get(i);
			addOperand(node.getOperator(), operands, e);
		}
	}

	public void addOperand(InfixExpression.Operator operator, List<org.eclipse.jdt.core.dom.Expression> operands,
			org.eclipse.jdt.core.dom.Expression e) {
		if (e instanceof org.eclipse.jdt.core.dom.InfixExpression
				&& ((org.eclipse.jdt.core.dom.InfixExpression) e).getOperator() == operator)
			getOperands((InfixExpression) e, operands);
		else
			operands.add(e);
	}

	@Override
	public boolean visit(InstanceofExpression node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.TYPECOMPARE);
		node.getLeftOperand().accept(this);
		b.addExpressions(expressions.pop());
		boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
		tb.setName(typeName(node.getRightOperand()));
		tb.setKind(boa.types.Ast.TypeKind.OTHER);
		setTypeBinding(tb, node.getRightOperand());
		b.setNewType(tb.build());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		if (node.resolveMethodBinding() != null) {
			IMethodBinding mb = node.resolveMethodBinding();
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
		for (Object a : node.arguments()) {
			((org.eclipse.jdt.core.dom.Expression) a).accept(this);
			b.addMethodArgs(expressions.pop());
		}
		if (!node.typeArguments().isEmpty()) {
			setAstLevel(JLS3);

			for (Object t : node.typeArguments()) {
				boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
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
	public boolean visit(NullLiteral node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		b.setLiteral("null");
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(NumberLiteral node) {
		if (node.getToken().toLowerCase().startsWith("0b"))
			setAstLevel(JLS4);
		if (node.getToken().contains("_"))
			setAstLevel(JLS4);
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		b.setLiteral(node.getToken());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(ParenthesizedExpression node) {
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
	public boolean visit(PrefixExpression node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
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
	public boolean visit(StringLiteral node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		b.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		b.setLiteral(node.getEscapedValue());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(SuperFieldAccess node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		if (node.resolveFieldBinding() != null) {
			IVariableBinding vb = node.resolveFieldBinding();
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
		boa.types.Ast.Expression.Builder qb = boa.types.Ast.Expression.newBuilder();
		qb.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		qb.setLiteral(name);
		b.addExpressions(qb);
		b.setVariable(node.getName().getFullyQualifiedName());
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(SuperMethodInvocation node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		if (node.resolveMethodBinding() != null) {
			IMethodBinding mb = node.resolveMethodBinding();
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
		for (Object a : node.arguments()) {
			((org.eclipse.jdt.core.dom.Expression)a).accept(this);
			b.addMethodArgs(expressions.pop());
		}
		if (!node.typeArguments().isEmpty()) {
			setAstLevel(JLS3);

			for (Object t : node.typeArguments()) {
				boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
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
	public boolean visit(ThisExpression node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		if (node.resolveTypeBinding() != null) {
			b.setReturnType(buildType(node.resolveTypeBinding()));
		}
		String name = "";
		if (node.getQualifier() != null)
			name += node.getQualifier().getFullyQualifiedName() + ".";
		b.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		b.setLiteral(name + "this");
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(TypeLiteral node) {
		boa.types.Ast.Expression.Builder b = boa.types.Ast.Expression.newBuilder();
		if (node.resolveTypeBinding() != null) {
			b.setReturnType(buildType(node.resolveTypeBinding()));
		}
		b.setKind(boa.types.Ast.Expression.ExpressionKind.LITERAL);
		b.setLiteral(typeName(node.getType()) + ".class");
		expressions.push(b.build());
		return false;
	}

	@Override
	public boolean visit(VariableDeclarationExpression node) {
		boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.VARDECL);
		for (Object o : node.fragments()) {
			VariableDeclarationFragment f = (VariableDeclarationFragment) o;
			eb.addVariableDecls(build(f, node.getType(), node.modifiers(), TypeKind.OTHER));
		}
		expressions.push(eb.build());
		return false;
	}

	// begin java 8
	@Override
	public boolean visit(LambdaExpression node) {
		setAstLevel(JLS8);

		boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.LAMBDA);
		if (!node.hasParentheses())
			eb.setNoParens(true);
		for (Object o : node.parameters()) {
			if (o instanceof SingleVariableDeclaration) {
				SingleVariableDeclaration svd = (SingleVariableDeclaration)o;
				Variable.Builder vb = build(svd, boa.types.Ast.TypeKind.OTHER);
				eb.addVariableDecls(vb);
			} else {
				VariableDeclarationFragment vdf = (VariableDeclarationFragment) o;
				Variable.Builder vb = Variable.newBuilder();
				vb.setName(vdf.getName().getFullyQualifiedName());
				// FIXME
				boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
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
				boa.types.Ast.Expression e = expressions.pop();
				eb.addExpressions(e);
			} else {
				statements.push(new ArrayList<boa.types.Ast.Statement>());
				node.getBody().accept(this);
				for (boa.types.Ast.Statement s : statements.pop())
					eb.addStatements(s);
			}
		}
		expressions.push(eb.build());

		return false;
	}

	@Override
	public boolean visit(CreationReference node) {
		setAstLevel(JLS8);

		boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		if (node.resolveMethodBinding() != null) {
			IMethodBinding mb = node.resolveMethodBinding();
			if (mb.getReturnType() != null)
				eb.setReturnType(buildType(mb.getReturnType()));
			if (mb.getDeclaringClass() != null)
				eb.setDeclaringType(buildType(mb.getDeclaringClass()));
		}
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.METHOD_REFERENCE);

		boa.types.Ast.Type.Builder tb1 = boa.types.Ast.Type.newBuilder();
		tb1.setName(typeName(node.getType()));
		tb1.setKind(boa.types.Ast.TypeKind.OTHER);
		setTypeBinding(tb1, node.getType());
		eb.setNewType(tb1.build());

		for (Object t : node.typeArguments()) {
			boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
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
	public boolean visit(ExpressionMethodReference node) {
		setAstLevel(JLS8);

		boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		if (node.resolveMethodBinding() != null) {
			IMethodBinding mb = node.resolveMethodBinding();
			if (mb.getReturnType() != null)
				eb.setReturnType(buildType(mb.getReturnType()));
			if (mb.getDeclaringClass() != null)
				eb.setDeclaringType(buildType(mb.getDeclaringClass()));
		}
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.METHOD_REFERENCE);

		node.getExpression().accept(this);
		eb.addExpressions(expressions.pop());

		for (Object t : node.typeArguments()) {
			boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
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
	public boolean visit(SuperMethodReference node) {
		setAstLevel(JLS8);

		boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		if (node.resolveMethodBinding() != null) {
			IMethodBinding mb = node.resolveMethodBinding();
			if (mb.getReturnType() != null)
				eb.setReturnType(buildType(mb.getReturnType()));
			if (mb.getDeclaringClass() != null)
				eb.setDeclaringType(buildType(mb.getDeclaringClass()));
		}
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.METHOD_REFERENCE);

		if (node.getQualifier() != null)
			eb.setLiteral((node.getQualifier()) +".super");
		else
			eb.setLiteral("super");

		for (Object t : node.typeArguments()) {
			boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
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
	public boolean visit(TypeMethodReference node) {
		setAstLevel(JLS8);

		boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		if (node.resolveMethodBinding() != null) {
			IMethodBinding mb = node.resolveMethodBinding();
			if (mb.getReturnType() != null)
				eb.setReturnType(buildType(mb.getReturnType()));
			if (mb.getDeclaringClass() != null)
				eb.setDeclaringType(buildType(mb.getDeclaringClass()));
		}
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.METHOD_REFERENCE);

		boa.types.Ast.Type.Builder tb1 = boa.types.Ast.Type.newBuilder();
		tb1.setName(typeName(node.getType()));
		tb1.setKind(boa.types.Ast.TypeKind.OTHER);
		setTypeBinding(tb1, node.getType());
		eb.setNewType(tb1.build());

		for (Object t : node.typeArguments()) {
			boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
			tb.setName(typeName((org.eclipse.jdt.core.dom.Type)t));
			tb.setKind(boa.types.Ast.TypeKind.GENERIC);
			setTypeBinding(tb, (org.eclipse.jdt.core.dom.Type) t);
			eb.addGenericParameters(tb.build());
		}

		eb.setMethod(node.getName().getIdentifier());
		expressions.push(eb.build());

		return false;
	}
	
	//begin Java 9 ()
	@Override
	public boolean visit(ModuleDeclaration node) {
		System.out.println("This is ModuleDeclaration node: " + node.getLength());
		setAstLevel(JLS9);
		
		b.setName(node.getName().getFullyQualifiedName());
		
		if(!node.annotations().isEmpty()) {
//			not sure what's the name should be 
//			b.setName(node.getName().getFullyQualifiedName());
			b.addAllModifiers(visitAnnotationsList(node.annotations()));
		}
		
		if(node.isOpen()) {
			boa.types.Ast.Modifier.Builder mb = boa.types.Ast.Modifier.newBuilder();
			mb.setKind(boa.types.Ast.Modifier.ModifierKind.OTHER);
			mb.setAnnotationName("open");
			b.addModifiers(mb);
		}
		
		//How should we deal with IModuleBinding
		if(node.resolveBinding() != null) {
//			IModuleBinding mb = node.resolveBinding();
			
		}
		
		boa.types.Ast.Statement.Builder sb = boa.types.Ast.Statement.newBuilder();
		statements.push(new ArrayList<boa.types.Ast.Statement>());
		sb.setKind(boa.types.Ast.Statement.StatementKind.OTHER);
		for (Object s : node.moduleStatements()) {
			((org.eclipse.jdt.core.dom.Statement)s).accept(this);
			
		}
		for (boa.types.Ast.Statement st : statements.pop())
			sb.addStatements(st);
		
		b.addStatements(sb.build());
			
		return false;
	}
	
	//begin java 12 
	@Override
	public boolean visit(SwitchExpression node) {
		setAstLevel(JLS12);
		
		boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.SWITCH);
		node.getExpression().accept(this);
		System.out.println("++++++++++++Expression is: " + node.getExpression().toString() + "+++++++++++");
		eb.addExpressions(expressions.pop());
		

		statements.push(new ArrayList<boa.types.Ast.Statement>());
		
		for (Object s: node.statements()) {	
			((org.eclipse.jdt.core.dom.Statement) s).accept(this);
		}
		
//		while(!statements.isEmpty()) {
//			List<boa.types.Ast.Statement> tempList = statements.pop();
//			int s = tempList.size();
//			if (s != 0) {
//				System.out.println("//////////////This is a statement poped: " + tempList.toString() + "/////////////////////////");
//			}
//			System.out.println("//////////////This is a statement poped: " + tempList.toString() + "/////////////////////////");
//			System.out.println("//////////////This is a statement size is: " + s + "/////////////////////////");
//		
//		}
		
//		System.out.println("//////////////This is a statement poped: " + statements.pop().toString() + "/////////////////////////");
//		System.out.println("//////////////This is a statement poped: " + statements.pop().toString() + "/////////////////////////");
//		System.out.println("//////////////This is a statement poped: " + statements.pop().toString() + "/////////////////////////");
		
//		while(!statements.isEmpty()) {
//			List<boa.types.Ast.Statement> tempList = statements.pop();
//			int s = tempList.size();
//			if (s > 0) {
//				System.out.println("//////////////This is a statement poped: " + tempList.toString() + "/////////////////////////");
//				for(boa.types.Ast.Statement st: statements.pop()) {	
//					eb.addStatements(st);
//				}
//			}
//		}
		
		for(boa.types.Ast.Statement st: statements.pop()) {	
			eb.addStatements(st);
		}
		
		expressions.push(eb.build());
		return false;
	}
	
	//begin java 13
	@Override
	public boolean visit(YieldStatement node) {
		setAstLevel(JLS13);
		System.out.println("=============================================================");
		System.out.println("Yield is implicit: " + node.isImplicit());
		boa.types.Ast.Statement.Builder b = boa.types.Ast.Statement.newBuilder();
		List<boa.types.Ast.Statement> list = statements.peek();
		b.setKind(boa.types.Ast.Statement.StatementKind.YIELD);
		node.getExpression().accept(this);
		b.addExpressions(expressions.pop());
		list.add(b.build());
		return false;
	}
	
	// begin java 15 (expression)
	@Override
	public boolean visit(TextBlock node) {
		setAstLevel(JLS15);
		
		boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.TEXTBLOCK);
		eb.setLiteral(node.getEscapedValue());
		expressions.push(eb.build());
		
		return false;
	}
	
	//begin Java 16 (Declaration)
	@Override
	public boolean visit(RecordDeclaration node) {
		setAstLevel(JLS16);
		
		boa.types.Ast.Declaration.Builder b = boa.types.Ast.Declaration.newBuilder();
		b.setName(node.getName().getIdentifier());
		System.out.println("+++++++++++++++++++++++++ record name is: " + node.getName().toString()+ "+++++++++++++++++++++++++");
		b.setKind(boa.types.Ast.TypeKind.IMMUTABLE);
		b.setFullyQualifiedName(getFullyQualifiedName(node));
		setDeclaringClass(b, node.resolveBinding());
		
		//Should we set kind to OTHER or Record? Use OTHER for now 
		
		b.addAllModifiers(buildModifiers(node.modifiers()));
		
		if (!node.typeParameters().isEmpty()) {
			for (Object t : node.typeParameters()) {
				boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
				String name = ((TypeParameter)t).getName().getFullyQualifiedName();
				String bounds = "";
				for (Object o : ((TypeParameter)t).typeBounds()) {
					if (bounds.length() > 0)
						bounds += " & ";
					bounds += typeName((org.eclipse.jdt.core.dom.Type)o);
				}
				if (bounds.length() > 0)
					name = name + " extends " + bounds;
				tb.setName(name);
				tb.setKind(boa.types.Ast.TypeKind.GENERIC);
				setTypeBinding(tb, ((TypeParameter) t).getName());
				b.addGenericParameters(tb.build());
			}
		}
		
		//element type: SingleVariableDeclaration
		//how to set variable declaration
		//Boa book: fields are array 
		for(Object o: node.recordComponents()) {
			fields.push(new ArrayList<boa.types.Ast.Variable>());
			((SingleVariableDeclaration) o).accept(this);
			for (boa.types.Ast.Variable v : fields.pop())
				b.addFields(v);
		}
	
		for (Object t : node.superInterfaceTypes()) {
			boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
			tb.setName(typeName((org.eclipse.jdt.core.dom.Type) t));
			tb.setKind(boa.types.Ast.TypeKind.INTERFACE);
			setTypeBinding(tb, (org.eclipse.jdt.core.dom.Type) t);
			b.addParents(tb.build());
		}
		
		for (Object d : node.bodyDeclarations()) {
			if (d instanceof FieldDeclaration) {
				fields.push(new ArrayList<boa.types.Ast.Variable>());
				((FieldDeclaration) d).accept(this);
				for (boa.types.Ast.Variable v : fields.pop())
					b.addFields(v);
			} else if (d instanceof MethodDeclaration) {
				methods.push(new ArrayList<boa.types.Ast.Method>());
				((MethodDeclaration) d).accept(this);
				for (boa.types.Ast.Method m : methods.pop()){
//					if (b.getKind().equals(boa.types.Ast.TypeKind.INTERFACE)) {
//						for (Modifier mod: m.getModifiersList()) {
//							if (mod.getKind().equals(boa.types.Ast.Modifier.ModifierKind.STATIC))
//								setAstLevel(JLS8);
//						}
//					}

					b.addMethods(m);
				}
			} else if (d instanceof Initializer) {
				methods.push(new ArrayList<boa.types.Ast.Method>());
				((Initializer) d).accept(this);
				for (boa.types.Ast.Method m : methods.pop())
					b.addMethods(m);
			} else {
				declarations.push(new ArrayList<boa.types.Ast.Declaration>());
				((BodyDeclaration) d).accept(this);
				for (boa.types.Ast.Declaration nd : declarations.pop())
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
			return typeName((ArrayType)t);
		if (t.isParameterizedType())
			return typeName((ParameterizedType)t);
		if (t.isPrimitiveType())
			return typeName((PrimitiveType)t);
		if (t.isQualifiedType())
			return typeName((QualifiedType)t);
		if (t.isNameQualifiedType())
			return typeName((NameQualifiedType)t);
		if (t.isIntersectionType())
			return typeName((IntersectionType)t);
		if (t.isUnionType())
			return typeName((UnionType)t);
		if (t.isWildcardType())
			return typeName((WildcardType)t);
		return typeName((SimpleType)t);
	}

	protected String typeName(final ArrayType t) {
		String name = typeName(t.getElementType());
		// FIXME JLS8: Deprecated getDimensions() and added dimensions()
		for (int i = 0; i < t.getDimensions(); i++)
			name += "[]";
		visitDimensions(t.dimensions());
		return name;
	}

	private void visitDimensions(List<?> dimensions) {
		for (Object d : dimensions)
			((Dimension) d).accept(this);
	}

	protected String typeName(final ParameterizedType t) {
		setAstLevel(JLS3);

		if (t.typeArguments().isEmpty())
			setAstLevel(JLS4);

		String name = "";
		for (final Object o : t.typeArguments()) {
			if (name.length() > 0) name += ", ";
			name += typeName((org.eclipse.jdt.core.dom.Type)o);
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
			name += typeName((org.eclipse.jdt.core.dom.Type)o);
		}
		return name;
	}

	protected String typeName(final UnionType t) {
		setAstLevel(JLS4);

		String name = "";
		for (final Object o : t.types()) {
			if (name.length() > 0) name += " | ";
			name += typeName((org.eclipse.jdt.core.dom.Type)o);
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
	public boolean visit(ArrayType node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(ParameterizedType node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(PrimitiveType node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(QualifiedType node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(NameQualifiedType node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(SimpleType node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(IntersectionType node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(UnionType node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(WildcardType node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(ImportDeclaration node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(PackageDeclaration node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(SingleVariableDeclaration node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(MemberValuePair node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(TypeParameter node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		throw new RuntimeException("visited unused node " + node.getClass().getSimpleName());
	}
}
