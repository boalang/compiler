package boa.datagen.util;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;

import boa.types.Ast.Type;

public class JavaASTUtil {
	private static final HashMap<ModifierKeyword, Integer> modifierType = new HashMap<>();
	
	static {
		modifierType.put(ModifierKeyword.ABSTRACT_KEYWORD, 1);
		modifierType.put(ModifierKeyword.DEFAULT_KEYWORD, 2);
		modifierType.put(ModifierKeyword.FINAL_KEYWORD, 3);
		modifierType.put(ModifierKeyword.NATIVE_KEYWORD, 4);
		modifierType.put(ModifierKeyword.PRIVATE_KEYWORD, 2);
		modifierType.put(ModifierKeyword.PROTECTED_KEYWORD, 2);
		modifierType.put(ModifierKeyword.PUBLIC_KEYWORD, 2);
		modifierType.put(ModifierKeyword.STATIC_KEYWORD, 5);
		modifierType.put(ModifierKeyword.STRICTFP_KEYWORD, 6);
		modifierType.put(ModifierKeyword.SYNCHRONIZED_KEYWORD, 7);
		modifierType.put(ModifierKeyword.TRANSIENT_KEYWORD, 8);
		modifierType.put(ModifierKeyword.VOLATILE_KEYWORD, 7);
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

	public static int getType(Modifier mn) {
		return modifierType.get(mn.getKeyword());
	}

}
