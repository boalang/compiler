/*
 * Copyright 2016, Hridesh Rajan, Robert Dyer, Hoan Nguyen, Farheen Sultana
 *                 Iowa State University of Science and Technology
 *                 and Bowling Green State University
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

import java.util.*;

import org.eclipse.jdt.core.dom.*;

import boa.datagen.treed.TreedConstants;
import boa.types.Ast.*;
import boa.types.Shared.ChangeKind;

/**
 * @author rdyer
 * @author sfarheen
 */
public class Java8Visitor extends Java7Visitor {
	public Java8Visitor(String src) {
		super(src);
	}

	// Field/Method Declarations

	public Java8Visitor(String src, Map<String, Integer> declarationFile, Map<String, Integer> declarationNode) {
		super(src, declarationFile, declarationNode);
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		List<boa.types.Ast.Method> list = methods.peek();
		Method.Builder b = Method.newBuilder();
		Integer index = (Integer) node.getProperty(Java7Visitor.PROPERTY_INDEX);
		if (index != null) {
			b.setKey(index);
			ChangeKind status = (ChangeKind) node.getProperty(TreedConstants.PROPERTY_STATUS);
			if (status != null) {
				b.setChangeKind(status);
				ASTNode mappedNode = (ASTNode) node.getProperty(TreedConstants.PROPERTY_MAP);
				if (mappedNode != null)
					b.setMappedNode((Integer) mappedNode.getProperty(TreedConstants.PROPERTY_INDEX));
			}
		}
		if (node.isConstructor())
			b.setName("<init>");
		else
			b.setName(node.getName().getFullyQualifiedName());
		for (Object m : node.modifiers()) {
			if (((IExtendedModifier)m).isAnnotation())
				((Annotation)m).accept(this);
			else
				((org.eclipse.jdt.core.dom.Modifier)m).accept(this);
			b.addModifiers(modifiers.pop());
		}
		boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
		if (node.getReturnType2() != null) {
			String name = typeName(node.getReturnType2());
			// FIXME JLS8: Deprecated getExtraDimensions() and added extraDimensions()
			for (int i = 0; i < node.getExtraDimensions(); i++)
				name += "[]";
			tb.setName(name);
			tb.setKind(boa.types.Ast.TypeKind.OTHER);
			setTypeBinding(tb, node.getReturnType2());
			index = (Integer) node.getReturnType2().getProperty(Java7Visitor.PROPERTY_INDEX);
			if (index != null) {
				tb.setKey(index);
				ChangeKind status = (ChangeKind) node.getReturnType2().getProperty(TreedConstants.PROPERTY_STATUS);
				if (status != null) {
					tb.setChangeKind(status);
					ASTNode mappedNode = (ASTNode) node.getReturnType2().getProperty(TreedConstants.PROPERTY_MAP);
					if (mappedNode != null)
						tb.setMappedNode((Integer) mappedNode.getProperty(TreedConstants.PROPERTY_INDEX));
				}
			}
			b.setReturnType(tb.build());
		} else {
			tb.setName("void");
			tb.setKind(boa.types.Ast.TypeKind.OTHER);
			b.setReturnType(tb.build());
		}
		for (Object t : node.typeParameters()) {
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
			index = (Integer) ((ASTNode) t).getProperty(Java7Visitor.PROPERTY_INDEX);
			if (index != null) {
				tp.setKey(index);
				ChangeKind status = (ChangeKind) ((ASTNode) t).getProperty(TreedConstants.PROPERTY_STATUS);
				if (status != null) {
					tp.setChangeKind(status);
					ASTNode mappedNode = (ASTNode) ((ASTNode) t).getProperty(TreedConstants.PROPERTY_MAP);
					if (mappedNode != null)
						tp.setMappedNode((Integer) mappedNode.getProperty(TreedConstants.PROPERTY_INDEX));
				}
			}
			b.addGenericParameters(tp.build());
		}
		if (node.getReceiverType() != null) {
			Variable.Builder vb = Variable.newBuilder();
			vb.setName("this");
			boa.types.Ast.Type.Builder tp = boa.types.Ast.Type.newBuilder();
			String name = typeName(node.getReceiverType());
			if (node.getReceiverQualifier() != null) name = node.getReceiverQualifier().getFullyQualifiedName() + "." + name;
			tp.setName(name);
			tp.setKind(boa.types.Ast.TypeKind.OTHER); // FIXME change to receiver? or something?
			setTypeBinding(tp, node.getReceiverType());
			index = (Integer) node.getReceiverType().getProperty(Java7Visitor.PROPERTY_INDEX);
			if (index != null) {
				tp.setKey(index);
				ChangeKind status = (ChangeKind) node.getReceiverType().getProperty(TreedConstants.PROPERTY_STATUS);
				if (status != null) {
					tp.setChangeKind(status);
					ASTNode mappedNode = (ASTNode) node.getReceiverType().getProperty(TreedConstants.PROPERTY_MAP);
					if (mappedNode != null)
						tp.setMappedNode((Integer) mappedNode.getProperty(TreedConstants.PROPERTY_INDEX));
				}
			}
			vb.setVariableType(tp.build());
			b.addArguments(vb.build());
		}
		for (Object o : node.parameters()) {
			SingleVariableDeclaration ex = (SingleVariableDeclaration)o;
			Variable.Builder vb = Variable.newBuilder();
			index = (Integer) ex.getProperty(Java7Visitor.PROPERTY_INDEX);
			if (index != null) {
				vb.setKey(index);
				ChangeKind status = (ChangeKind) ex.getProperty(TreedConstants.PROPERTY_STATUS);
				if (status != null) {
					vb.setChangeKind(status);
					ASTNode mappedNode = (ASTNode) ex.getProperty(TreedConstants.PROPERTY_MAP);
					if (mappedNode != null)
						vb.setMappedNode((Integer) mappedNode.getProperty(TreedConstants.PROPERTY_INDEX));
				}
			}
			vb.setName(ex.getName().getFullyQualifiedName());
			for (Object m : ex.modifiers()) {
				if (((IExtendedModifier)m).isAnnotation())
					((Annotation)m).accept(this);
				else
					((org.eclipse.jdt.core.dom.Modifier)m).accept(this);
				vb.addModifiers(modifiers.pop());
			}
			boa.types.Ast.Type.Builder tp = boa.types.Ast.Type.newBuilder();
			String name = typeName(ex.getType());
			// FIXME JLS8: Deprecated getExtraDimensions() and added extraDimensions()
			for (int i = 0; i < ex.getExtraDimensions(); i++)
				name += "[]";
			if (ex.isVarargs())
				name += "...";
			tp.setName(name);
			tp.setKind(boa.types.Ast.TypeKind.OTHER);
			setTypeBinding(tp, ex.getType());
			index = (Integer) ex.getType().getProperty(Java7Visitor.PROPERTY_INDEX);
			if (index != null) {
				tp.setKey(index);
				ChangeKind status = (ChangeKind) ex.getType().getProperty(TreedConstants.PROPERTY_STATUS);
				if (status != null) {
					tp.setChangeKind(status);
					ASTNode mappedNode = (ASTNode) ex.getType().getProperty(TreedConstants.PROPERTY_MAP);
					if (mappedNode != null)
						tp.setMappedNode((Integer) mappedNode.getProperty(TreedConstants.PROPERTY_INDEX));
				}
			}
			vb.setVariableType(tp.build());
			if (ex.getInitializer() != null) {
				ex.getInitializer().accept(this);
				vb.setInitializer(expressions.pop());
			}
			b.addArguments(vb.build());
		}
		for (Object o : node.thrownExceptionTypes()) {
				boa.types.Ast.Type.Builder tp = boa.types.Ast.Type.newBuilder();
				tp.setName(typeName((org.eclipse.jdt.core.dom.Type) o));
				tp.setKind(boa.types.Ast.TypeKind.CLASS);
				setTypeBinding(tp, (org.eclipse.jdt.core.dom.Type) o);
				index = (Integer) ((ASTNode) o).getProperty(Java7Visitor.PROPERTY_INDEX);
				if (index != null) {
					tp.setKey(index);
					ChangeKind status = (ChangeKind) ((ASTNode) o).getProperty(TreedConstants.PROPERTY_STATUS);
					if (status != null) {
						tp.setChangeKind(status);
						ASTNode mappedNode = (ASTNode) ((ASTNode) o).getProperty(TreedConstants.PROPERTY_MAP);
						if (mappedNode != null)
							tp.setMappedNode((Integer) mappedNode.getProperty(TreedConstants.PROPERTY_INDEX));
					}
				}
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

	// begin java 8
	@Override
	public boolean visit(LambdaExpression node) {
		boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		Integer index = (Integer) node.getProperty(Java7Visitor.PROPERTY_INDEX);
		if (index != null) {
			eb.setKey(index);
			ChangeKind status = (ChangeKind) node.getProperty(TreedConstants.PROPERTY_STATUS);
			if (status != null) {
				eb.setChangeKind(status);
				ASTNode mappedNode = (ASTNode) node.getProperty(TreedConstants.PROPERTY_MAP);
				if (mappedNode != null)
					eb.setMappedNode((Integer) mappedNode.getProperty(TreedConstants.PROPERTY_INDEX));
			}
		}
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.LAMBDA);
        if (!node.hasParentheses())
            eb.setNoParens(true);
		for (Object o : node.parameters()) {
			VariableDeclaration ex = (VariableDeclaration)o;
			Variable.Builder vb = Variable.newBuilder();
			index = (Integer) ex.getProperty(Java7Visitor.PROPERTY_INDEX);
			if (index != null) {
				vb.setKey(index);
				ChangeKind status = (ChangeKind) ex.getProperty(TreedConstants.PROPERTY_STATUS);
				if (status != null) {
					vb.setChangeKind(status);
					ASTNode mappedNode = (ASTNode) ex.getProperty(TreedConstants.PROPERTY_MAP);
					if (mappedNode != null)
						vb.setMappedNode((Integer) mappedNode.getProperty(TreedConstants.PROPERTY_INDEX));
				}
			}
			vb.setName(ex.getName().getFullyQualifiedName());
			if (o instanceof SingleVariableDeclaration) {
				SingleVariableDeclaration svd = (SingleVariableDeclaration)o;
				boa.types.Ast.Type.Builder tp = boa.types.Ast.Type.newBuilder();
				String name = typeName(svd.getType());
				// FIXME JLS8: Deprecated getExtraDimensions() and added extraDimensions()
				for (int i = 0; i < svd.getExtraDimensions(); i++)
					name += "[]";
				if (svd.isVarargs())
					name += "...";
				tp.setName(name);
				tp.setKind(boa.types.Ast.TypeKind.OTHER);
				setTypeBinding(tp, svd.getType());
				index = (Integer) svd.getType().getProperty(Java7Visitor.PROPERTY_INDEX);
				if (index != null) {
					tp.setKey(index);
					ChangeKind status = (ChangeKind) svd.getType().getProperty(TreedConstants.PROPERTY_STATUS);
					if (status != null) {
						tp.setChangeKind(status);
						ASTNode mappedNode = (ASTNode) svd.getType().getProperty(TreedConstants.PROPERTY_MAP);
						if (mappedNode != null)
							tp.setMappedNode((Integer) mappedNode.getProperty(TreedConstants.PROPERTY_INDEX));
					}
				}
				vb.setVariableType(tp.build());
			} else {
				VariableDeclarationFragment vdf = (VariableDeclarationFragment)o;
				boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
				tb.setName("");
				tb.setKind(boa.types.Ast.TypeKind.OTHER);
				setTypeBinding(tb, vdf.getName());
				vb.setVariableType(tb.build());
			}
			eb.addVariableDecls(vb.build());
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
		boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		Integer index = (Integer) node.getProperty(Java7Visitor.PROPERTY_INDEX);
		if (index != null) {
			eb.setKey(index);
			ChangeKind status = (ChangeKind) node.getProperty(TreedConstants.PROPERTY_STATUS);
			if (status != null) {
				eb.setChangeKind(status);
				ASTNode mappedNode = (ASTNode) node.getProperty(TreedConstants.PROPERTY_MAP);
				if (mappedNode != null)
					eb.setMappedNode((Integer) mappedNode.getProperty(TreedConstants.PROPERTY_INDEX));
			}
		}
		if (node.resolveMethodBinding() != null) {
			IMethodBinding mb = node.resolveMethodBinding();
			eb.setReturnType(buildType(mb.getReturnType()));
			if (mb.getDeclaringClass() != null)
				eb.setDeclaringType(buildType(mb.getDeclaringClass()));
		}
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.METHOD_REFERENCE);
		
		boa.types.Ast.Type.Builder tb1 = boa.types.Ast.Type.newBuilder();
		tb1.setName(typeName(node.getType()));
		tb1.setKind(boa.types.Ast.TypeKind.OTHER);
		setTypeBinding(tb1, node.getType());
		index = (Integer) node.getType().getProperty(Java7Visitor.PROPERTY_INDEX);
		if (index != null) {
			tb1.setKey(index);
			ChangeKind status = (ChangeKind) node.getType().getProperty(TreedConstants.PROPERTY_STATUS);
			if (status != null) {
				tb1.setChangeKind(status);
				ASTNode mappedNode = (ASTNode) node.getType().getProperty(TreedConstants.PROPERTY_MAP);
				if (mappedNode != null)
					tb1.setMappedNode((Integer) mappedNode.getProperty(TreedConstants.PROPERTY_INDEX));
			}
		}
		eb.setNewType(tb1.build());

		for (Object t : node.typeArguments()) {
			boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
			tb.setName(typeName((org.eclipse.jdt.core.dom.Type) t));
			tb.setKind(boa.types.Ast.TypeKind.GENERIC);
			setTypeBinding(tb, (org.eclipse.jdt.core.dom.Type) t);
			index = (Integer) ((ASTNode) t).getProperty(Java7Visitor.PROPERTY_INDEX);
			if (index != null) {
				tb.setKey(index);
				ChangeKind status = (ChangeKind) ((ASTNode) t).getProperty(TreedConstants.PROPERTY_STATUS);
				if (status != null) {
					tb.setChangeKind(status);
					ASTNode mappedNode = (ASTNode) ((ASTNode) t).getProperty(TreedConstants.PROPERTY_MAP);
					if (mappedNode != null)
						tb.setMappedNode((Integer) mappedNode.getProperty(TreedConstants.PROPERTY_INDEX));
				}
			}
			eb.addGenericParameters(tb.build());
		}

		eb.setMethod("new");
		expressions.push(eb.build());

		return false;
	}
	
	@Override
	public boolean visit(ExpressionMethodReference node) {
		boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		Integer index = (Integer) node.getProperty(Java7Visitor.PROPERTY_INDEX);
		if (index != null) {
			eb.setKey(index);
			ChangeKind status = (ChangeKind) node.getProperty(TreedConstants.PROPERTY_STATUS);
			if (status != null) {
				eb.setChangeKind(status);
				ASTNode mappedNode = (ASTNode) node.getProperty(TreedConstants.PROPERTY_MAP);
				if (mappedNode != null)
					eb.setMappedNode((Integer) mappedNode.getProperty(TreedConstants.PROPERTY_INDEX));
			}
		}
		if (node.resolveMethodBinding() != null) {
			IMethodBinding mb = node.resolveMethodBinding();
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
			index = (Integer) ((ASTNode) t).getProperty(Java7Visitor.PROPERTY_INDEX);
			if (index != null) {
				tb.setKey(index);
				ChangeKind status = (ChangeKind) ((ASTNode) t).getProperty(TreedConstants.PROPERTY_STATUS);
				if (status != null) {
					tb.setChangeKind(status);
					ASTNode mappedNode = (ASTNode) ((ASTNode) t).getProperty(TreedConstants.PROPERTY_MAP);
					if (mappedNode != null)
						tb.setMappedNode((Integer) mappedNode.getProperty(TreedConstants.PROPERTY_INDEX));
				}
			}
			eb.addGenericParameters(tb.build());
		}

		eb.setMethod(node.getName().getIdentifier());
		expressions.push(eb.build());

		return false;
	}
	
	@Override
	public boolean visit(SuperMethodReference node) {
		boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		Integer index = (Integer) node.getProperty(Java7Visitor.PROPERTY_INDEX);
		if (index != null) {
			eb.setKey(index);
			ChangeKind status = (ChangeKind) node.getProperty(TreedConstants.PROPERTY_STATUS);
			if (status != null) {
				eb.setChangeKind(status);
				ASTNode mappedNode = (ASTNode) node.getProperty(TreedConstants.PROPERTY_MAP);
				if (mappedNode != null)
					eb.setMappedNode((Integer) mappedNode.getProperty(TreedConstants.PROPERTY_INDEX));
			}
		}
		if (node.resolveMethodBinding() != null) {
			IMethodBinding mb = node.resolveMethodBinding();
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
			index = (Integer) ((ASTNode) t).getProperty(Java7Visitor.PROPERTY_INDEX);
			if (index != null) {
				tb.setKey(index);
				ChangeKind status = (ChangeKind) ((ASTNode) t).getProperty(TreedConstants.PROPERTY_STATUS);
				if (status != null) {
					tb.setChangeKind(status);
					ASTNode mappedNode = (ASTNode) ((ASTNode) t).getProperty(TreedConstants.PROPERTY_MAP);
					if (mappedNode != null)
						tb.setMappedNode((Integer) mappedNode.getProperty(TreedConstants.PROPERTY_INDEX));
				}
			}
			eb.addGenericParameters(tb.build());
		}

		eb.setMethod(node.getName().getIdentifier());
		expressions.push(eb.build());

		return false;
	 }
	
	@Override
	public boolean visit(TypeMethodReference node) {
		boa.types.Ast.Expression.Builder eb = boa.types.Ast.Expression.newBuilder();
		Integer index = (Integer) node.getProperty(Java7Visitor.PROPERTY_INDEX);
		if (index != null) {
			eb.setKey(index);
			ChangeKind status = (ChangeKind) node.getProperty(TreedConstants.PROPERTY_STATUS);
			if (status != null) {
				eb.setChangeKind(status);
				ASTNode mappedNode = (ASTNode) node.getProperty(TreedConstants.PROPERTY_MAP);
				if (mappedNode != null)
					eb.setMappedNode((Integer) mappedNode.getProperty(TreedConstants.PROPERTY_INDEX));
			}
		}
		if (node.resolveMethodBinding() != null) {
			IMethodBinding mb = node.resolveMethodBinding();
			eb.setReturnType(buildType(mb.getReturnType()));
			if (mb.getDeclaringClass() != null)
				eb.setDeclaringType(buildType(mb.getDeclaringClass()));
		}
		eb.setKind(boa.types.Ast.Expression.ExpressionKind.METHOD_REFERENCE);
		
		boa.types.Ast.Type.Builder tb1 = boa.types.Ast.Type.newBuilder();
		tb1.setName(typeName(node.getType()));
		tb1.setKind(boa.types.Ast.TypeKind.OTHER);
		setTypeBinding(tb1, node.getType());
		index = (Integer) node.getType().getProperty(Java7Visitor.PROPERTY_INDEX);
		if (index != null) {
			tb1.setKey(index);
			ChangeKind status = (ChangeKind) node.getType().getProperty(TreedConstants.PROPERTY_STATUS);
			if (status != null) {
				tb1.setChangeKind(status);
				ASTNode mappedNode = (ASTNode) node.getType().getProperty(TreedConstants.PROPERTY_MAP);
				if (mappedNode != null)
					tb1.setMappedNode((Integer) mappedNode.getProperty(TreedConstants.PROPERTY_INDEX));
			}
		}
		eb.setNewType(tb1.build());

		for (Object t : node.typeArguments()) {
			boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
			tb.setName(typeName((org.eclipse.jdt.core.dom.Type)t));
			tb.setKind(boa.types.Ast.TypeKind.GENERIC);
			setTypeBinding(tb, (org.eclipse.jdt.core.dom.Type) t);
			index = (Integer) ((ASTNode) t).getProperty(Java7Visitor.PROPERTY_INDEX);
			if (index != null) {
				tb.setKey(index);
				ChangeKind status = (ChangeKind) ((ASTNode) t).getProperty(TreedConstants.PROPERTY_STATUS);
				if (status != null) {
					tb.setChangeKind(status);
					ASTNode mappedNode = (ASTNode) ((ASTNode) t).getProperty(TreedConstants.PROPERTY_MAP);
					if (mappedNode != null)
						tb.setMappedNode((Integer) mappedNode.getProperty(TreedConstants.PROPERTY_INDEX));
				}
			}
			eb.addGenericParameters(tb.build());
		}

		eb.setMethod(node.getName().getIdentifier());
		expressions.push(eb.build());

		return false;
	}

	@Override
	protected String typeName(final ArrayType t) {
		String name = typeName(t.getElementType());
		// FIXME JLS8: Deprecated getDimensions() and added dimensions()
		for (int i = 0; i < t.getDimensions(); i++)
			name += "[]";
		return name;
	}

	/*
	 * FIXME
	 * SingleVariableDeclaration (JLS8 Changes):
	 * 		Added varargsAnnotations()
	 */

	/*
	 * FIXME
	 * TypeParameter (JLS8 Changes):
	 * 		Added modifiers()
	 */
}
