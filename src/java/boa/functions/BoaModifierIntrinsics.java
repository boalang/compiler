/*
 * Copyright 2014, Hridesh Rajan, Robert Dyer, 
 *                 and Iowa State University of Science and Technology
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
package boa.functions;

import boa.types.Ast.Declaration;
import boa.types.Ast.Method;
import boa.types.Ast.Modifier;
import boa.types.Ast.Modifier.ModifierKind;
import boa.types.Ast.Modifier.Visibility;
import boa.types.Ast.Namespace;
import boa.types.Ast.Variable;

/**
 * Boa domain-specific functions for working with the Modifier type.
 * 
 * @author rdyer
 */
public class BoaModifierIntrinsics {
	/**
	 * Returns a specific Annotation Modifier, if it exists otherwise returns null.
	 * 
	 * @param m the Method to check
	 * @param name the annotation to look for
	 * @return the annotation Modifier or null
	 */
	@FunctionSpec(name = "get_annotation", returnType = "Modifier", formalParameters = { "Method", "string" })
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
	@FunctionSpec(name = "has_visibility", returnType = "bool", formalParameters = { "Method", "Visibility" })
	public static boolean hasVisibility(final Method m, final Visibility v) {
		for (int i = 0; i < m.getModifiersCount(); i++)
			if (m.getModifiers(i).getKind() == ModifierKind.VISIBILITY && m.getModifiers(i).getVisibility() == v)
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
	 * Returns if the Method has an Annotation.
	 * 
	 * @param m the Method to check
	 * @return true if m has an annotation
	 */
	@FunctionSpec(name = "has_annotation", returnType = "bool", formalParameters = { "Method" })
	public static boolean hasAnnotation(final Method m) {
		return hasModifier(m, ModifierKind.ANNOTATION);
	}

	/**
	 * Returns if the Method has an Annotation with the given name.
	 * 
	 * @param m the Method to check
	 * @param name the annotation name to look for
	 * @return true if m has an annotation with the given name
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
		return hasVisibility(m, Visibility.PUBLIC);
	}

	/**
	 * Returns if the Method has a PRIVATE visibility modifier.
	 * 
	 * @param m the Method to check
	 * @return true if m has a PRIVATE visibility modifier
	 */
	@FunctionSpec(name = "has_modifier_private", returnType = "bool", formalParameters = { "Method" })
	public static boolean hasModifierPrivate(final Method m) {
		return hasVisibility(m, Visibility.PRIVATE);
	}

	/**
	 * Returns if the Method has a PROTECTED visibility modifier.
	 * 
	 * @param m the Method to check
	 * @return true if m has a PROTECTED visibility modifier
	 */
	@FunctionSpec(name = "has_modifier_protected", returnType = "bool", formalParameters = { "Method" })
	public static boolean hasModifierProtected(final Method m) {
		return hasVisibility(m, Visibility.PROTECTED);
	}

	/**
	 * Returns if the Method has a NAMESPACE visibility modifier.
	 * 
	 * @param m the Method to check
	 * @return true if m has a NAMESPACE visibility modifier
	 */
	@FunctionSpec(name = "has_modifier_namespace", returnType = "bool", formalParameters = { "Method" })
	public static boolean hasModifierNamespace(final Method m) {
		return hasVisibility(m, Visibility.NAMESPACE);
	}

	/**
	 * Returns a specific Annotation Modifier, if it exists otherwise returns null.
	 * 
	 * @param v the Variable to check
	 * @param name the annotation to look for
	 * @return the annotation Modifier or null
	 */
	@FunctionSpec(name = "get_annotation", returnType = "Modifier", formalParameters = { "Variable", "string" })
	public static Modifier getAnnotation(final Variable v, final String name) {
		for (int i = 0; i < v.getModifiersCount(); i++) {
			Modifier mod = v.getModifiers(i);
			if (mod.getKind() == ModifierKind.ANNOTATION && mod.getAnnotationName().equals(name))
				return mod;
		}

		return null;
	}

	/**
	 * Returns if the Variable has the specified modifier.
	 * 
	 * @param v the Variable to examine
	 * @param kind the ModifierKind to test for
	 * @return true if v contains a modifier kind
	 */
	@FunctionSpec(name = "has_modifier", returnType = "bool", formalParameters = { "Variable", "ModifierKind" })
	public static boolean hasModifier(final Variable v, final ModifierKind kind) {
		for (int i = 0; i < v.getModifiersCount(); i++)
			if (v.getModifiers(i).getKind() == kind)
				return true;

		return false;
	}

	/**
	 * Returns if the Variable has the specified visibility modifier.
	 * 
	 * @param var the Variable to examine
	 * @param v the Visibility modifier to test for
	 * @return true if v contains a visibility modifier v
	 */
	@FunctionSpec(name = "has_visibility", returnType = "bool", formalParameters = { "Variable", "Visibility" })
	public static boolean hasVisibility(final Variable var, final Visibility v) {
		for (int i = 0; i < var.getModifiersCount(); i++)
			if (var.getModifiers(i).getKind() == ModifierKind.VISIBILITY && var.getModifiers(i).getVisibility() == v)
				return true;

		return false;
	}

	/**
	 * Returns if the Variable has a FINAL modifier.
	 * 
	 * @param v the Variable to check
	 * @return true if v has a FINAL modifier
	 */
	@FunctionSpec(name = "has_modifier_final", returnType = "bool", formalParameters = { "Variable" })
	public static boolean hasModifierFinal(final Variable v) {
		return hasModifier(v, ModifierKind.FINAL);
	}

	/**
	 * Returns if the Variable has a STATIC modifier.
	 * 
	 * @param v the Variable to check
	 * @return true if v has a STATIC modifier
	 */
	@FunctionSpec(name = "has_modifier_static", returnType = "bool", formalParameters = { "Variable" })
	public static boolean hasModifierStatic(final Variable v) {
		return hasModifier(v, ModifierKind.STATIC);
	}

	/**
	 * Returns if the Variable has a SYNCHRONIZED modifier.
	 * 
	 * @param v the Variable to check
	 * @return true if v has a SYNCHRONIZED modifier
	 */
	@FunctionSpec(name = "has_modifier_synchronized", returnType = "bool", formalParameters = { "Variable" })
	public static boolean hasModifierSynchronized(final Variable v) {
		return hasModifier(v, ModifierKind.SYNCHRONIZED);
	}

	/**
	 * Returns if the Variable has an Annotation.
	 * 
	 * @param v the Variable to check
	 * @return true if v has an annotation
	 */
	@FunctionSpec(name = "has_annotation", returnType = "bool", formalParameters = { "Variable" })
	public static boolean hasAnnotation(final Variable v) {
		return hasModifier(v, ModifierKind.ANNOTATION);
	}

	/**
	 * Returns if the Variable has an Annotation with the given name.
	 * 
	 * @param v the Variable to check
	 * @param name the annotation name to look for
	 * @return true if v has an annotation with the given name
	 */
	@FunctionSpec(name = "has_annotation", returnType = "bool", formalParameters = { "Variable", "string" })
	public static boolean hasAnnotation(final Variable v, final String name) {
		return getAnnotation(v, name) != null;
	}

	/**
	 * Returns if the Variable has a PUBLIC visibility modifier.
	 * 
	 * @param v the Variable to check
	 * @return true if v has a PUBLIC visibility modifier
	 */
	@FunctionSpec(name = "has_modifier_public", returnType = "bool", formalParameters = { "Variable" })
	public static boolean hasModifierPublic(final Variable v) {
		return hasVisibility(v, Visibility.PUBLIC);
	}

	/**
	 * Returns if the Variable has a PRIVATE visibility modifier.
	 * 
	 * @param v the Variable to check
	 * @return true if v has a PRIVATE visibility modifier
	 */
	@FunctionSpec(name = "has_modifier_private", returnType = "bool", formalParameters = { "Variable" })
	public static boolean hasModifierPrivate(final Variable v) {
		return hasVisibility(v, Visibility.PRIVATE);
	}

	/**
	 * Returns if the Variable has a PROTECTED visibility modifier.
	 * 
	 * @param v the Variable to check
	 * @return true if v has a PROTECTED visibility modifier
	 */
	@FunctionSpec(name = "has_modifier_protected", returnType = "bool", formalParameters = { "Variable" })
	public static boolean hasModifierProtected(final Variable v) {
		return hasVisibility(v, Visibility.PROTECTED);
	}

	/**
	 * Returns if the Variable has a NAMESPACE visibility modifier.
	 * 
	 * @param v the Variable to check
	 * @return true if v has a NAMESPACE visibility modifier
	 */
	@FunctionSpec(name = "has_modifier_namespace", returnType = "bool", formalParameters = { "Variable" })
	public static boolean hasModifierNamespace(final Variable v) {
		return hasVisibility(v, Visibility.NAMESPACE);
	}

	/**
	 * Returns a specific Annotation Modifier, if it exists otherwise returns null.
	 * 
	 * @param d the Declaration to check
	 * @param name the annotation to look for
	 * @return the annotation Modifier or null
	 */
	@FunctionSpec(name = "get_annotation", returnType = "Modifier", formalParameters = { "Declaration", "string" })
	public static Modifier getAnnotation(final Declaration d, final String name) {
		for (int i = 0; i < d.getModifiersCount(); i++) {
			Modifier mod = d.getModifiers(i);
			if (mod.getKind() == ModifierKind.ANNOTATION && mod.getAnnotationName().equals(name))
				return mod;
		}

		return null;
	}

	/**
	 * Returns if the Declaration has the specified modifier.
	 * 
	 * @param d the Declaration to examine
	 * @param kind the ModifierKind to test for
	 * @return true if d contains a modifier kind
	 */
	@FunctionSpec(name = "has_modifier", returnType = "bool", formalParameters = { "Declaration", "ModifierKind" })
	public static boolean hasModifier(final Declaration d, final ModifierKind kind) {
		for (int i = 0; i < d.getModifiersCount(); i++)
			if (d.getModifiers(i).getKind() == kind)
				return true;

		return false;
	}

	/**
	 * Returns if the Declaration has the specified visibility modifier.
	 * 
	 * @param d the Declaration to examine
	 * @param v the Visibility modifier to test for
	 * @return true if d contains a visibility modifier v
	 */
	@FunctionSpec(name = "has_visibility", returnType = "bool", formalParameters = { "Declaration", "Visibility" })
	public static boolean hasVisibility(final Declaration d, final Visibility v) {
		for (int i = 0; i < d.getModifiersCount(); i++)
			if (d.getModifiers(i).getKind() == ModifierKind.VISIBILITY && d.getModifiers(i).getVisibility() == v)
				return true;

		return false;
	}

	/**
	 * Returns if the Declaration has a FINAL modifier.
	 * 
	 * @param d the Declaration to check
	 * @return true if d has a FINAL modifier
	 */
	@FunctionSpec(name = "has_modifier_final", returnType = "bool", formalParameters = { "Declaration" })
	public static boolean hasModifierFinal(final Declaration d) {
		return hasModifier(d, ModifierKind.FINAL);
	}

	/**
	 * Returns if the Declaration has a STATIC modifier.
	 * 
	 * @param d the Declaration to check
	 * @return true if d has a STATIC modifier
	 */
	@FunctionSpec(name = "has_modifier_static", returnType = "bool", formalParameters = { "Declaration" })
	public static boolean hasModifierStatic(final Declaration d) {
		return hasModifier(d, ModifierKind.STATIC);
	}

	/**
	 * Returns if the Declaration has a SYNCHRONIZED modifier.
	 * 
	 * @param d the Declaration to check
	 * @return true if d has a SYNCHRONIZED modifier
	 */
	@FunctionSpec(name = "has_modifier_synchronized", returnType = "bool", formalParameters = { "Declaration" })
	public static boolean hasModifierSynchronized(final Declaration d) {
		return hasModifier(d, ModifierKind.SYNCHRONIZED);
	}

	/**
	 * Returns if the Declaration has an Annotation.
	 * 
	 * @param d the Declaration to check
	 * @return true if d has an annotation
	 */
	@FunctionSpec(name = "has_annotation", returnType = "bool", formalParameters = { "Declaration" })
	public static boolean hasAnnotation(final Declaration d) {
		return hasModifier(d, ModifierKind.ANNOTATION);
	}

	/**
	 * Returns if the Declaration has an Annotation with the given name.
	 * 
	 * @param d the Declaration to check
	 * @param name the annotation name to look for
	 * @return true if d has an annotation with the given name
	 */
	@FunctionSpec(name = "has_annotation", returnType = "bool", formalParameters = { "Declaration", "string" })
	public static boolean hasAnnotation(final Declaration d, final String name) {
		return getAnnotation(d, name) != null;
	}

	/**
	 * Returns if the Declaration has a PUBLIC visibility modifier.
	 * 
	 * @param d the Declaration to check
	 * @return true if d has a PUBLIC visibility modifier
	 */
	@FunctionSpec(name = "has_modifier_public", returnType = "bool", formalParameters = { "Declaration" })
	public static boolean hasModifierPublic(final Declaration d) {
		return hasVisibility(d, Visibility.PUBLIC);
	}

	/**
	 * Returns if the Declaration has a PRIVATE visibility modifier.
	 * 
	 * @param d the Declaration to check
	 * @return true if d has a PRIVATE visibility modifier
	 */
	@FunctionSpec(name = "has_modifier_private", returnType = "bool", formalParameters = { "Declaration" })
	public static boolean hasModifierPrivate(final Declaration d) {
		return hasVisibility(d, Visibility.PRIVATE);
	}

	/**
	 * Returns if the Declaration has a PROTECTED visibility modifier.
	 * 
	 * @param d the Declaration to check
	 * @return true if d has a PROTECTED visibility modifier
	 */
	@FunctionSpec(name = "has_modifier_protected", returnType = "bool", formalParameters = { "Declaration" })
	public static boolean hasModifierProtected(final Declaration d) {
		return hasVisibility(d, Visibility.PROTECTED);
	}

	/**
	 * Returns if the Declaration has a NAMESPACE visibility modifier.
	 * 
	 * @param d the Declaration to check
	 * @return true if d has a NAMESPACE visibility modifier
	 */
	@FunctionSpec(name = "has_modifier_namespace", returnType = "bool", formalParameters = { "Declaration" })
	public static boolean hasModifierNamespace(final Declaration d) {
		return hasVisibility(d, Visibility.NAMESPACE);
	}

	/**
	 * Returns a specific Annotation Modifier, if it exists otherwise returns null.
	 * 
	 * @param d the Namespace to check
	 * @param name the annotation to look for
	 * @return the annotation Modifier or null
	 */
	@FunctionSpec(name = "get_annotation", returnType = "Modifier", formalParameters = { "Namespace", "string" })
	public static Modifier getAnnotation(final Namespace n, final String name) {
		for (int i = 0; i < n.getModifiersCount(); i++) {
			Modifier mod = n.getModifiers(i);
			if (mod.getKind() == ModifierKind.ANNOTATION && mod.getAnnotationName().equals(name))
				return mod;
		}

		return null;
	}

	/**
	 * Returns if the Namespace has the specified modifier.
	 * 
	 * @param d the Namespace to examine
	 * @param kind the ModifierKind to test for
	 * @return true if d contains a modifier kind
	 */
	@FunctionSpec(name = "has_modifier", returnType = "bool", formalParameters = { "Namespace", "ModifierKind" })
	public static boolean hasModifier(final Namespace n, final ModifierKind kind) {
		for (int i = 0; i < n.getModifiersCount(); i++)
			if (n.getModifiers(i).getKind() == kind)
				return true;

		return false;
	}

	/**
	 * Returns if the Namespace has the specified visibility modifier.
	 * 
	 * @param d the Namespace to examine
	 * @param v the Visibility modifier to test for
	 * @return true if d contains a visibility modifier v
	 */
	@FunctionSpec(name = "has_visibility", returnType = "bool", formalParameters = { "Namespace", "Visibility" })
	public static boolean hasVisibility(final Namespace n, final Visibility v) {
		for (int i = 0; i < n.getModifiersCount(); i++)
			if (n.getModifiers(i).getKind() == ModifierKind.VISIBILITY && n.getModifiers(i).getVisibility() == v)
				return true;

		return false;
	}

	/**
	 * Returns if the Namespace has a FINAL modifier.
	 * 
	 * @param d the Namespace to check
	 * @return true if d has a FINAL modifier
	 */
	@FunctionSpec(name = "has_modifier_final", returnType = "bool", formalParameters = { "Namespace" })
	public static boolean hasModifierFinal(final Namespace n) {
		return hasModifier(n, ModifierKind.FINAL);
	}

	/**
	 * Returns if the Namespace has a STATIC modifier.
	 * 
	 * @param d the Namespace to check
	 * @return true if d has a STATIC modifier
	 */
	@FunctionSpec(name = "has_modifier_static", returnType = "bool", formalParameters = { "Namespace" })
	public static boolean hasModifierStatic(final Namespace n) {
		return hasModifier(n, ModifierKind.STATIC);
	}

	/**
	 * Returns if the Namespace has a SYNCHRONIZED modifier.
	 * 
	 * @param d the Namespace to check
	 * @return true if d has a SYNCHRONIZED modifier
	 */
	@FunctionSpec(name = "has_modifier_synchronized", returnType = "bool", formalParameters = { "Namespace" })
	public static boolean hasModifierSynchronized(final Namespace n) {
		return hasModifier(n, ModifierKind.SYNCHRONIZED);
	}

	/**
	 * Returns if the Namespace has an Annotation.
	 * 
	 * @param d the Namespace to check
	 * @return true if d has an annotation
	 */
	@FunctionSpec(name = "has_annotation", returnType = "bool", formalParameters = { "Namespace" })
	public static boolean hasAnnotation(final Namespace n) {
		return hasModifier(n, ModifierKind.ANNOTATION);
	}

	/**
	 * Returns if the Namespace has an Annotation with the given name.
	 * 
	 * @param d the Namespace to check
	 * @param name the annotation name to look for
	 * @return true if d has an annotation with the given name
	 */
	@FunctionSpec(name = "has_annotation", returnType = "bool", formalParameters = { "Namespace", "string" })
	public static boolean hasAnnotation(final Namespace n, final String name) {
		return getAnnotation(n, name) != null;
	}

	/**
	 * Returns if the Namespace has a PUBLIC visibility modifier.
	 * 
	 * @param d the Namespace to check
	 * @return true if d has a PUBLIC visibility modifier
	 */
	@FunctionSpec(name = "has_modifier_public", returnType = "bool", formalParameters = { "Namespace" })
	public static boolean hasModifierPublic(final Namespace n) {
		return hasVisibility(n, Visibility.PUBLIC);
	}

	/**
	 * Returns if the Namespace has a PRIVATE visibility modifier.
	 * 
	 * @param d the Namespace to check
	 * @return true if d has a PRIVATE visibility modifier
	 */
	@FunctionSpec(name = "has_modifier_private", returnType = "bool", formalParameters = { "Namespace" })
	public static boolean hasModifierPrivate(final Namespace n) {
		return hasVisibility(n, Visibility.PRIVATE);
	}

	/**
	 * Returns if the Namespace has a PROTECTED visibility modifier.
	 * 
	 * @param d the Namespace to check
	 * @return true if d has a PROTECTED visibility modifier
	 */
	@FunctionSpec(name = "has_modifier_protected", returnType = "bool", formalParameters = { "Namespace" })
	public static boolean hasModifierProtected(final Namespace n) {
		return hasVisibility(n, Visibility.PROTECTED);
	}

	/**
	 * Returns if the Namespace has a NAMESPACE visibility modifier.
	 * 
	 * @param d the Namespace to check
	 * @return true if d has a NAMESPACE visibility modifier
	 */
	@FunctionSpec(name = "has_modifier_namespace", returnType = "bool", formalParameters = { "Namespace" })
	public static boolean hasModifierNamespace(final Namespace n) {
		return hasVisibility(n, Visibility.NAMESPACE);
	}
}
