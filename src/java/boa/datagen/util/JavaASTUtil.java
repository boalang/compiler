package boa.datagen.util;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;

import boa.types.Ast.Type;

public class JavaASTUtil {

	public static Type buildType(ITypeBinding itb) {
		if (itb.getTypeDeclaration() != null)
			itb = itb.getTypeDeclaration();
		boa.types.Ast.Type.Builder tb = boa.types.Ast.Type.newBuilder();
		tb.setName(itb.getName());
		if (itb.isInterface())
			tb.setKind(boa.types.Ast.TypeKind.INTERFACE);
		else if (itb.isEnum())
			tb.setKind(boa.types.Ast.TypeKind.ENUM);
		else if (itb.isAnnotation())
			tb.setKind(boa.types.Ast.TypeKind.ANNOTATION);
		else if (itb.isAnonymous())
			tb.setKind(boa.types.Ast.TypeKind.ANONYMOUS);
		else 
			tb.setKind(boa.types.Ast.TypeKind.OTHER);
		tb.setFullyQualifiedName(itb.getQualifiedName());
		return tb.build();
	}

	public static String getFullyQualifiedName(AbstractTypeDeclaration node) {
		StringBuilder sb = new StringBuilder();
		sb.append(node.getName().getIdentifier());
		ASTNode n = node;
		while (n.getParent() != null) {
			n = n.getParent();
			if (n instanceof CompilationUnit) {
				CompilationUnit cu = (CompilationUnit) n;
				if (cu.getPackage() != null)
					sb.insert(0, cu.getPackage().getName().getFullyQualifiedName() + ".");
			} else if (n instanceof AbstractTypeDeclaration)
				sb.insert(0, ((AbstractTypeDeclaration) n).getName().getIdentifier() + ".");
			else
				return "";
		}
		return sb.toString();
	}

}
