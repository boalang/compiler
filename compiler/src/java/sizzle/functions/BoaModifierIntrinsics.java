package sizzle.functions;

import sizzle.types.Ast.Method;
import sizzle.types.Ast.Modifier;
import sizzle.types.Ast.Modifier.ModifierKind;
import sizzle.types.Ast.Modifier.Visibility;

/**
 * Boa domain-specific functions for working with the Modifier type.
 * 
 * @author rdyer
 */
public class BoaModifierIntrinsics {
	/**
	 * Returns if the Method has a SYNCHRONIZED modifier.
	 * 
	 * @param m the Method to check
	 * @return true if m has a SYNCHRONIZED modifier
	 */
	@FunctionSpec(name = "get_annotation", returnType = "bool", formalParameters = { "Method", "string" })
	public static Modifier getAnnotation(final Method m, final String name) {
		for (int i = 0; i < m.getModifiersCount(); i++) {
			Modifier mod = m.getModifiers(i);
			if (mod.getKind() == ModifierKind.ANNOTATION && mod.getAnnotationName().equals(name))
				return mod;
		}

		return null;
	}

	/**
	 * Returns if the Method has the specified modifier.
	 * 
	 * @param m the Method to examine
	 * @param kind the ModifierKind to test for
	 * @return true if m contains a modifier kind
	 */
	@FunctionSpec(name = "has_modifier", returnType = "bool", formalParameters = { "Method", "ModifierKind" })
	public static boolean hasModifier(final Method m, final ModifierKind kind) {
		for (int i = 0; i < m.getModifiersCount(); i++)
			if (m.getModifiers(i).getKind() == kind)
				return true;

		return false;
	}

	/**
	 * Returns if the Method has the specified visibility modifier.
	 * 
	 * @param m the Method to examine
	 * @param v the Visibility modifier to test for
	 * @return true if m contains a visibility modifier v
	 */
	@FunctionSpec(name = "has_modifier", returnType = "bool", formalParameters = { "Method", "Visibility" })
	public static boolean hasModifier(final Method m, final Visibility v) {
		for (int i = 0; i < m.getModifiersCount(); i++)
			if (m.getModifiers(i).getKind() == ModifierKind.VISIBILITY
				&& (m.getModifiers(i).getVisibility() & v.getNumber()) == v.getNumber())
				return true;

		return false;
	}

	/**
	 * Returns if the Method has a FINAL modifier.
	 * 
	 * @param m the Method to check
	 * @return true if m has a FINAL modifier
	 */
	@FunctionSpec(name = "has_modifier_final", returnType = "bool", formalParameters = { "Method" })
	public static boolean hasModifierFinal(final Method m) {
		return hasModifier(m, ModifierKind.FINAL);
	}

	/**
	 * Returns if the Method has a STATIC modifier.
	 * 
	 * @param m the Method to check
	 * @return true if m has a STATIC modifier
	 */
	@FunctionSpec(name = "has_modifier_static", returnType = "bool", formalParameters = { "Method" })
	public static boolean hasModifierStatic(final Method m) {
		return hasModifier(m, ModifierKind.STATIC);
	}

	/**
	 * Returns if the Method has a SYNCHRONIZED modifier.
	 * 
	 * @param m the Method to check
	 * @return true if m has a SYNCHRONIZED modifier
	 */
	@FunctionSpec(name = "has_modifier_synchronized", returnType = "bool", formalParameters = { "Method" })
	public static boolean hasModifierSynchronized(final Method m) {
		return hasModifier(m, ModifierKind.SYNCHRONIZED);
	}

	/**
	 * Returns if the Method has a SYNCHRONIZED modifier.
	 * 
	 * @param m the Method to check
	 * @return true if m has a SYNCHRONIZED modifier
	 */
	@FunctionSpec(name = "has_annotation", returnType = "bool", formalParameters = { "Method" })
	public static boolean hasAnnotation(final Method m) {
		return hasModifier(m, ModifierKind.ANNOTATION);
	}

	/**
	 * Returns if the Method has a SYNCHRONIZED modifier.
	 * 
	 * @param m the Method to check
	 * @return true if m has a SYNCHRONIZED modifier
	 */
	@FunctionSpec(name = "has_annotation", returnType = "bool", formalParameters = { "Method", "string" })
	public static boolean hasAnnotation(final Method m, final String name) {
		return getAnnotation(m, name) != null;
	}

	/**
	 * Returns if the Method has a PUBLIC visibility modifier.
	 * 
	 * @param m the Method to check
	 * @return true if m has a PUBLIC visibility modifier
	 */
	@FunctionSpec(name = "has_modifier_public", returnType = "bool", formalParameters = { "Method" })
	public static boolean hasModifierPublic(final Method m) {
		return hasModifier(m, Visibility.PUBLIC);
	}

	/**
	 * Returns if the Method has a PRIVATE visibility modifier.
	 * 
	 * @param m the Method to check
	 * @return true if m has a PRIVATE visibility modifier
	 */
	@FunctionSpec(name = "has_modifier_private", returnType = "bool", formalParameters = { "Method" })
	public static boolean hasModifierPrivate(final Method m) {
		return hasModifier(m, Visibility.PRIVATE);
	}

	/**
	 * Returns if the Method has a PROTECTED visibility modifier.
	 * 
	 * @param m the Method to check
	 * @return true if m has a PROTECTED visibility modifier
	 */
	@FunctionSpec(name = "has_modifier_protected", returnType = "bool", formalParameters = { "Method" })
	public static boolean hasModifierProtected(final Method m) {
		return hasModifier(m, Visibility.PROTECTED);
	}

	/**
	 * Returns if the Method has a NAMESPACE visibility modifier.
	 * 
	 * @param m the Method to check
	 * @return true if m has a NAMESPACE visibility modifier
	 */
	@FunctionSpec(name = "has_modifier_namespace", returnType = "bool", formalParameters = { "Method" })
	public static boolean hasModifierNamespace(final Method m) {
		return hasModifier(m, Visibility.NAMESPACE);
	}
}
