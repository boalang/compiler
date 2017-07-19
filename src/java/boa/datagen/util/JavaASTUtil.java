package boa.datagen.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;

import boa.types.Diff.ChangedFile.FileKind;

public class JavaASTUtil {
	private static final HashMap<ModifierKeyword, Integer> modifierType = new HashMap<ModifierKeyword, Integer>();
	
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
	
	@SuppressWarnings("deprecation")
	public static ASTParser buildParser(FileKind fileKind) {
		int astLevel = -1;
		String compliance = null;
		switch (fileKind) {
		case SOURCE_JAVA_JLS2:
			astLevel = AST.JLS2;
			compliance = JavaCore.VERSION_1_4;
			break;
		case SOURCE_JAVA_JLS3:
			astLevel = AST.JLS3;
			compliance = JavaCore.VERSION_1_5;
			break;
		case SOURCE_JAVA_JLS4:
			astLevel = AST.JLS4;
			compliance = JavaCore.VERSION_1_7;
			break;
		case SOURCE_JAVA_JLS8:
			astLevel = AST.JLS8;
			compliance = JavaCore.VERSION_1_8;
			break;
		default:
			break;
		}
		if (compliance != null) {
			ASTParser parser = ASTParser.newParser(astLevel);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
	
			final Map<?, ?> options = JavaCore.getOptions();
			JavaCore.setComplianceOptions(compliance, options);
			parser.setCompilerOptions(options);
			return parser;
		}
		return null;
	}

}
