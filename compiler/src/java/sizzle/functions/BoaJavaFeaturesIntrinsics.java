package sizzle.functions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sizzle.types.Ast.*;
import sizzle.types.Code.*;

/**
 * Boa domain-specific functions for finding Java language features.
 * 
 * @author rdyer
 */
public class BoaJavaFeaturesIntrinsics {
	@FunctionSpec(name = "uses_enhanced_for", returnType = "int", formalParameters = { "CodeRepository" })
	public static int usesEnhancedFor(final CodeRepository r) {
		int count = 0;

		for (int i = 0; i < r.getRevisionsCount(); i++)
			count += usesEnhancedFor(r.getRevisions(i));

		return count;
	}

	@FunctionSpec(name = "uses_enhanced_for", returnType = "int", formalParameters = { "Revision" })
	public static int usesEnhancedFor(final Revision r) {
		int count = 0;

		for (int i = 0; i < r.getFilesCount(); i++)
			count += usesEnhancedFor(r.getFiles(i));

		return count;
	}

	@FunctionSpec(name = "uses_enhanced_for", returnType = "int", formalParameters = { "File" })
	public static int usesEnhancedFor(final File f) {
		int count = 0;

		for (int i = 0; i < f.getNamespacesCount(); i++)
			for (int j = 0; j < f.getNamespaces(i).getDeclarationsCount(); j++)
				count += usesEnhancedFor(f.getNamespaces(i).getDeclarations(j));

		return count;
	}

	@FunctionSpec(name = "uses_enhanced_for", returnType = "int", formalParameters = { "Declaration" })
	public static int usesEnhancedFor(final Declaration d) {
		int count = 0;

		for (int i = 0; i < d.getMethodsCount(); i++)
			count += usesEnhancedFor(d.getMethods(i));

		for (int i = 0; i < d.getFieldsCount(); i++)
			count += usesEnhancedFor(d.getFields(i));

		for (int i = 0; i < d.getNestedDeclarationsCount(); i++)
			count += usesEnhancedFor(d.getNestedDeclarations(i));

		return count;
	}

	@FunctionSpec(name = "uses_enhanced_for", returnType = "int", formalParameters = { "Method" })
	public static int usesEnhancedFor(final Method m) {
		int count = 0;

		for (int i = 0; i < m.getArgumentsCount(); i++)
			count += usesEnhancedFor(m.getArguments(i));

		for (int i = 0; i < m.getStatementsCount(); i++)
			count += usesEnhancedFor(m.getStatements(i));

		return count;
	}

	@FunctionSpec(name = "uses_enhanced_for", returnType = "int", formalParameters = { "Variable" })
	public static int usesEnhancedFor(final Variable v) {
		int count = 0;

		if (v.hasInitializer())
			count += usesEnhancedFor(v.getInitializer());

		return count;
	}

	@FunctionSpec(name = "uses_enhanced_for", returnType = "int", formalParameters = { "Statement" })
	public static int usesEnhancedFor(final Statement s) {
		int count = 0;

		if (s.getKind() == Statement.StatementKind.FOR && s.hasVariableDeclaration())
			count++;

		if (s.hasCondition())
			count += usesEnhancedFor(s.getCondition());

		if (s.hasVariableDeclaration())
			count += usesEnhancedFor(s.getVariableDeclaration());

		if (s.hasTypeDeclaration())
			count += usesEnhancedFor(s.getTypeDeclaration());

		if (s.hasExpression())
			count += usesEnhancedFor(s.getExpression());

		for (int i = 0; i < s.getStatementsCount(); i++)
			count += usesEnhancedFor(s.getStatements(i));

		for (int i = 0; i < s.getInitializationsCount(); i++)
			count += usesEnhancedFor(s.getInitializations(i));

		for (int i = 0; i < s.getUpdatesCount(); i++)
			count += usesEnhancedFor(s.getUpdates(i));

		return count;
	}

	@FunctionSpec(name = "uses_enhanced_for", returnType = "int", formalParameters = { "Expression" })
	public static int usesEnhancedFor(final Expression e) {
		int count = 0;

		for (int i = 0; i < e.getVariableDeclsCount(); i++)
			count += usesEnhancedFor(e.getVariableDecls(i));

		for (int i = 0; i < e.getExpressionsCount(); i++)
			count += usesEnhancedFor(e.getExpressions(i));

		for (int i = 0; i < e.getMethodArgsCount(); i++)
			count += usesEnhancedFor(e.getMethodArgs(i));

		return count;
	}

	@FunctionSpec(name = "uses_varargs", returnType = "int", formalParameters = { "CodeRepository" })
	public static int usesVarargs(final CodeRepository r) {
		int count = 0;

		for (int i = 0; i < r.getRevisionsCount(); i++)
			count += usesVarargs(r.getRevisions(i));

		return count;
	}

	@FunctionSpec(name = "uses_varargs", returnType = "int", formalParameters = { "Revision" })
	public static int usesVarargs(final Revision r) {
		int count = 0;

		for (int i = 0; i < r.getFilesCount(); i++)
			count += usesVarargs(r.getFiles(i));

		return count;
	}

	@FunctionSpec(name = "uses_varargs", returnType = "int", formalParameters = { "File" })
	public static int usesVarargs(final File f) {
		int count = 0;

		for (int i = 0; i < f.getNamespacesCount(); i++)
			for (int j = 0; j < f.getNamespaces(i).getDeclarationsCount(); j++)
				count += usesVarargs(f.getNamespaces(i).getDeclarations(j));

		return count;
	}

	@FunctionSpec(name = "uses_varargs", returnType = "int", formalParameters = { "Declaration" })
	public static int usesVarargs(final Declaration d) {
		int count = 0;

		for (int i = 0; i < d.getMethodsCount(); i++)
			count += usesVarargs(d.getMethods(i));

		for (int i = 0; i < d.getNestedDeclarationsCount(); i++)
			count += usesVarargs(d.getNestedDeclarations(i));

		return count;
	}

	@FunctionSpec(name = "uses_varargs", returnType = "int", formalParameters = { "Method" })
	public static int usesVarargs(final Method m) {
		int count = 0;

		if (m.getArgumentsCount() > 0)
			if (m.getArguments(m.getArgumentsCount() - 1).getVariableType().getName().contains("..."))
				count++;

		for (int i = 0; i < m.getStatementsCount(); i++)
			count += usesVarargs(m.getStatements(i));

		return count;
	}

	@FunctionSpec(name = "uses_varargs", returnType = "int", formalParameters = { "Statement" })
	public static int usesVarargs(final Statement s) {
		int count = 0;

		if (s.hasTypeDeclaration())
			count += usesVarargs(s.getTypeDeclaration());

		for (int i = 0; i < s.getStatementsCount(); i++)
			count += usesVarargs(s.getStatements(i));

		return count;
	}

	@FunctionSpec(name = "uses_assert", returnType = "int", formalParameters = { "CodeRepository" })
	public static int usesAssert(final CodeRepository r) {
		int count = 0;

		for (int i = 0; i < r.getRevisionsCount(); i++)
			count += usesAssert(r.getRevisions(i));

		return count;
	}

	@FunctionSpec(name = "uses_assert", returnType = "int", formalParameters = { "Revision" })
	public static int usesAssert(final Revision r) {
		int count = 0;

		for (int i = 0; i < r.getFilesCount(); i++)
			count += usesAssert(r.getFiles(i));

		return count;
	}

	@FunctionSpec(name = "uses_assert", returnType = "int", formalParameters = { "File" })
	public static int usesAssert(final File f) {
		int count = 0;

		for (int i = 0; i < f.getNamespacesCount(); i++)
			for (int j = 0; j < f.getNamespaces(i).getDeclarationsCount(); j++)
				count += usesAssert(f.getNamespaces(i).getDeclarations(j));

		return count;
	}

	@FunctionSpec(name = "uses_assert", returnType = "int", formalParameters = { "Declaration" })
	public static int usesAssert(final Declaration d) {
		int count = 0;

		for (int i = 0; i < d.getMethodsCount(); i++)
			count += usesAssert(d.getMethods(i));

		for (int i = 0; i < d.getFieldsCount(); i++)
			count += usesAssert(d.getFields(i));

		for (int i = 0; i < d.getNestedDeclarationsCount(); i++)
			count += usesAssert(d.getNestedDeclarations(i));

		return count;
	}

	@FunctionSpec(name = "uses_assert", returnType = "int", formalParameters = { "Method" })
	public static int usesAssert(final Method m) {
		int count = 0;

		for (int i = 0; i < m.getArgumentsCount(); i++)
			count += usesAssert(m.getArguments(i));

		for (int i = 0; i < m.getStatementsCount(); i++)
			count += usesAssert(m.getStatements(i));

		return count;
	}

	@FunctionSpec(name = "uses_assert", returnType = "int", formalParameters = { "Variable" })
	public static int usesAssert(final Variable v) {
		int count = 0;

		if (v.hasInitializer())
			count += usesAssert(v.getInitializer());

		return count;
	}

	@FunctionSpec(name = "uses_assert", returnType = "int", formalParameters = { "Statement" })
	public static int usesAssert(final Statement s) {
		int count = 0;

		if (s.getKind() == Statement.StatementKind.ASSERT)
			count++;

		if (s.hasCondition())
			count += usesAssert(s.getCondition());

		if (s.hasVariableDeclaration())
			count += usesAssert(s.getVariableDeclaration());

		if (s.hasTypeDeclaration())
			count += usesAssert(s.getTypeDeclaration());

		if (s.hasExpression())
			count += usesAssert(s.getExpression());

		for (int i = 0; i < s.getStatementsCount(); i++)
			count += usesAssert(s.getStatements(i));

		for (int i = 0; i < s.getInitializationsCount(); i++)
			count += usesAssert(s.getInitializations(i));

		for (int i = 0; i < s.getUpdatesCount(); i++)
			count += usesAssert(s.getUpdates(i));

		return count;
	}

	@FunctionSpec(name = "uses_assert", returnType = "int", formalParameters = { "Expression" })
	public static int usesAssert(final Expression e) {
		int count = 0;

		for (int i = 0; i < e.getVariableDeclsCount(); i++)
			count += usesAssert(e.getVariableDecls(i));

		for (int i = 0; i < e.getExpressionsCount(); i++)
			count += usesAssert(e.getExpressions(i));

		for (int i = 0; i < e.getMethodArgsCount(); i++)
			count += usesAssert(e.getMethodArgs(i));

		return count;
	}

	@FunctionSpec(name = "uses_enums", returnType = "int", formalParameters = { "CodeRepository" })
	public static int usesEnums(final CodeRepository r) {
		int count = 0;

		for (int i = 0; i < r.getRevisionsCount(); i++)
			count += usesEnums(r.getRevisions(i));

		return count;
	}

	@FunctionSpec(name = "uses_enums", returnType = "int", formalParameters = { "Revision" })
	public static int usesEnums(final Revision r) {
		int count = 0;

		for (int i = 0; i < r.getFilesCount(); i++)
			count += usesEnums(r.getFiles(i));

		return count;
	}

	@FunctionSpec(name = "uses_enums", returnType = "int", formalParameters = { "File" })
	public static int usesEnums(final File f) {
		int count = 0;

		for (int i = 0; i < f.getNamespacesCount(); i++)
			for (int j = 0; j < f.getNamespaces(i).getDeclarationsCount(); j++)
				count += usesEnums(f.getNamespaces(i).getDeclarations(j));

		return count;
	}

	@FunctionSpec(name = "uses_enums", returnType = "int", formalParameters = { "Declaration" })
	public static int usesEnums(final Declaration d) {
		int count = 0;

		if (d.getKind() == TypeKind.ENUM)
			count++;

		for (int i = 0; i < d.getMethodsCount(); i++)
			count += usesEnums(d.getMethods(i));

		for (int i = 0; i < d.getFieldsCount(); i++)
			count += usesEnums(d.getFields(i));

		for (int i = 0; i < d.getNestedDeclarationsCount(); i++)
			count += usesEnums(d.getNestedDeclarations(i));

		return count;
	}

	@FunctionSpec(name = "uses_enums", returnType = "int", formalParameters = { "Method" })
	public static int usesEnums(final Method m) {
		int count = 0;

		for (int i = 0; i < m.getArgumentsCount(); i++)
			count += usesEnums(m.getArguments(i));

		for (int i = 0; i < m.getStatementsCount(); i++)
			count += usesEnums(m.getStatements(i));

		return count;
	}

	@FunctionSpec(name = "uses_enums", returnType = "int", formalParameters = { "Variable" })
	public static int usesEnums(final Variable v) {
		int count = 0;

		if (v.hasInitializer())
			count += usesEnums(v.getInitializer());

		return count;
	}

	@FunctionSpec(name = "uses_enums", returnType = "int", formalParameters = { "Statement" })
	public static int usesEnums(final Statement s) {
		int count = 0;

		if (s.hasCondition())
			count += usesEnums(s.getCondition());

		if (s.hasVariableDeclaration())
			count += usesEnums(s.getVariableDeclaration());

		if (s.hasTypeDeclaration())
			count += usesEnums(s.getTypeDeclaration());

		if (s.hasExpression())
			count += usesEnums(s.getExpression());

		for (int i = 0; i < s.getStatementsCount(); i++)
			count += usesEnums(s.getStatements(i));

		for (int i = 0; i < s.getInitializationsCount(); i++)
			count += usesEnums(s.getInitializations(i));

		for (int i = 0; i < s.getUpdatesCount(); i++)
			count += usesEnums(s.getUpdates(i));

		return count;
	}

	@FunctionSpec(name = "uses_enums", returnType = "int", formalParameters = { "Expression" })
	public static int usesEnums(final Expression e) {
		int count = 0;

		for (int i = 0; i < e.getVariableDeclsCount(); i++)
			count += usesEnums(e.getVariableDecls(i));

		for (int i = 0; i < e.getExpressionsCount(); i++)
			count += usesEnums(e.getExpressions(i));

		for (int i = 0; i < e.getMethodArgsCount(); i++)
			count += usesEnums(e.getMethodArgs(i));

		return count;
	}

	@FunctionSpec(name = "uses_try_resources", returnType = "int", formalParameters = { "CodeRepository" })
	public static int usesTryResources(final CodeRepository r) {
		int count = 0;

		for (int i = 0; i < r.getRevisionsCount(); i++)
			count += usesTryResources(r.getRevisions(i));

		return count;
	}

	@FunctionSpec(name = "uses_try_resources", returnType = "int", formalParameters = { "Revision" })
	public static int usesTryResources(final Revision r) {
		int count = 0;

		for (int i = 0; i < r.getFilesCount(); i++)
			count += usesTryResources(r.getFiles(i));

		return count;
	}

	@FunctionSpec(name = "uses_try_resources", returnType = "int", formalParameters = { "File" })
	public static int usesTryResources(final File f) {
		int count = 0;

		for (int i = 0; i < f.getNamespacesCount(); i++)
			for (int j = 0; j < f.getNamespaces(i).getDeclarationsCount(); j++)
				count += usesTryResources(f.getNamespaces(i).getDeclarations(j));

		return count;
	}

	@FunctionSpec(name = "uses_try_resources", returnType = "int", formalParameters = { "Declaration" })
	public static int usesTryResources(final Declaration d) {
		int count = 0;

		for (int i = 0; i < d.getMethodsCount(); i++)
			count += usesTryResources(d.getMethods(i));

		for (int i = 0; i < d.getFieldsCount(); i++)
			count += usesTryResources(d.getFields(i));

		for (int i = 0; i < d.getNestedDeclarationsCount(); i++)
			count += usesTryResources(d.getNestedDeclarations(i));

		return count;
	}

	@FunctionSpec(name = "uses_try_resources", returnType = "int", formalParameters = { "Method" })
	public static int usesTryResources(final Method m) {
		int count = 0;

		for (int i = 0; i < m.getArgumentsCount(); i++)
			count += usesTryResources(m.getArguments(i));

		for (int i = 0; i < m.getStatementsCount(); i++)
			count += usesTryResources(m.getStatements(i));

		return count;
	}

	@FunctionSpec(name = "uses_try_resources", returnType = "int", formalParameters = { "Variable" })
	public static int usesTryResources(final Variable v) {
		int count = 0;

		if (v.hasInitializer())
			count += usesTryResources(v.getInitializer());

		return count;
	}

	@FunctionSpec(name = "uses_try_resources", returnType = "int", formalParameters = { "Statement" })
	public static int usesTryResources(final Statement s) {
		int count = 0;

		if (s.getKind() == Statement.StatementKind.TRY && s.getInitializationsCount() > 0)
			count++;

		if (s.hasCondition())
			count += usesTryResources(s.getCondition());

		if (s.hasVariableDeclaration())
			count += usesTryResources(s.getVariableDeclaration());

		if (s.hasTypeDeclaration())
			count += usesTryResources(s.getTypeDeclaration());

		if (s.hasExpression())
			count += usesTryResources(s.getExpression());

		for (int i = 0; i < s.getStatementsCount(); i++)
			count += usesTryResources(s.getStatements(i));

		for (int i = 0; i < s.getInitializationsCount(); i++)
			count += usesTryResources(s.getInitializations(i));

		for (int i = 0; i < s.getUpdatesCount(); i++)
			count += usesTryResources(s.getUpdates(i));

		return count;
	}

	@FunctionSpec(name = "uses_try_resources", returnType = "int", formalParameters = { "Expression" })
	public static int usesTryResources(final Expression e) {
		int count = 0;

		for (int i = 0; i < e.getVariableDeclsCount(); i++)
			count += usesTryResources(e.getVariableDecls(i));

		for (int i = 0; i < e.getExpressionsCount(); i++)
			count += usesTryResources(e.getExpressions(i));

		for (int i = 0; i < e.getMethodArgsCount(); i++)
			count += usesTryResources(e.getMethodArgs(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics", returnType = "int", formalParameters = { "CodeRepository" })
	public static int usesGenerics(final CodeRepository r) {
		int count = 0;

		for (int i = 0; i < r.getRevisionsCount(); i++)
			count += usesGenerics(r.getRevisions(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics", returnType = "int", formalParameters = { "Revision" })
	public static int usesGenerics(final Revision r) {
		int count = 0;

		for (int i = 0; i < r.getFilesCount(); i++)
			count += usesGenerics(r.getFiles(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics", returnType = "int", formalParameters = { "File" })
	public static int usesGenerics(final File f) {
		int count = 0;

		for (int i = 0; i < f.getNamespacesCount(); i++)
			for (int j = 0; j < f.getNamespaces(i).getDeclarationsCount(); j++)
				count += usesGenerics(f.getNamespaces(i).getDeclarations(j));

		return count;
	}

	@FunctionSpec(name = "uses_generics", returnType = "int", formalParameters = { "Declaration" })
	public static int usesGenerics(final Declaration d) {
		int count = 0;

		if (d.getGenericParametersCount() > 0)
			count++;

		for (int i = 0; i < d.getParentsCount(); i++)
			count += usesGenerics(d.getParents(i));

		for (int i = 0; i < d.getMethodsCount(); i++)
			count += usesGenerics(d.getMethods(i));

		for (int i = 0; i < d.getFieldsCount(); i++)
			count += usesGenerics(d.getFields(i));

		for (int i = 0; i < d.getNestedDeclarationsCount(); i++)
			count += usesGenerics(d.getNestedDeclarations(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics", returnType = "int", formalParameters = { "Method" })
	public static int usesGenerics(final Method m) {
		int count = 0;

		if (m.getGenericParametersCount() > 0)
			count++;

		count += usesGenerics(m.getReturnType());

		for (int i = 0; i < m.getExceptionTypesCount(); i++)
			count += usesGenerics(m.getExceptionTypes(i));

		for (int i = 0; i < m.getArgumentsCount(); i++)
			count += usesGenerics(m.getArguments(i));

		for (int i = 0; i < m.getStatementsCount(); i++)
			count += usesGenerics(m.getStatements(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics", returnType = "int", formalParameters = { "Variable" })
	public static int usesGenerics(final Variable v) {
		int count = 0;

		count += usesGenerics(v.getVariableType());

		if (v.hasInitializer())
			count += usesGenerics(v.getInitializer());

		return count;
	}

	@FunctionSpec(name = "uses_generics", returnType = "int", formalParameters = { "Statement" })
	public static int usesGenerics(final Statement s) {
		int count = 0;

		if (s.hasCondition())
			count += usesGenerics(s.getCondition());

		if (s.hasVariableDeclaration())
			count += usesGenerics(s.getVariableDeclaration());

		if (s.hasTypeDeclaration())
			count += usesGenerics(s.getTypeDeclaration());

		if (s.hasExpression())
			count += usesGenerics(s.getExpression());

		for (int i = 0; i < s.getStatementsCount(); i++)
			count += usesGenerics(s.getStatements(i));

		for (int i = 0; i < s.getInitializationsCount(); i++)
			count += usesGenerics(s.getInitializations(i));

		for (int i = 0; i < s.getUpdatesCount(); i++)
			count += usesGenerics(s.getUpdates(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics", returnType = "int", formalParameters = { "Expression" })
	public static int usesGenerics(final Expression e) {
		int count = 0;

		if (e.getGenericParametersCount() > 0)
			count++;

		if (e.hasNewType())
			count += usesGenerics(e.getNewType());

		for (int i = 0; i < e.getVariableDeclsCount(); i++)
			count += usesGenerics(e.getVariableDecls(i));

		for (int i = 0; i < e.getExpressionsCount(); i++)
			count += usesGenerics(e.getExpressions(i));

		for (int i = 0; i < e.getMethodArgsCount(); i++)
			count += usesGenerics(e.getMethodArgs(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics", returnType = "int", formalParameters = { "Type" })
	public static int usesGenerics(final Type t) {
		int count = 0;

		if (t.getKind() == TypeKind.GENERIC || t.getName().contains("<"))
			count++;

		return count;
	}

	@FunctionSpec(name = "uses_annotations", returnType = "int", formalParameters = { "CodeRepository" })
	public static int usesAnnotations(final CodeRepository r) {
		int count = 0;

		for (int i = 0; i < r.getRevisionsCount(); i++)
			count += usesAnnotations(r.getRevisions(i));

		return count;
	}

	@FunctionSpec(name = "uses_annotations", returnType = "int", formalParameters = { "Revision" })
	public static int usesAnnotations(final Revision r) {
		int count = 0;

		for (int i = 0; i < r.getFilesCount(); i++)
			count += usesAnnotations(r.getFiles(i));

		return count;
	}

	@FunctionSpec(name = "uses_annotations", returnType = "int", formalParameters = { "File" })
	public static int usesAnnotations(final File f) {
		int count = 0;

		for (int i = 0; i < f.getNamespacesCount(); i++)
			for (int j = 0; j < f.getNamespaces(i).getDeclarationsCount(); j++) {
				if (BoaModifierIntrinsics.hasAnnotation(f.getNamespaces(i)))
					count++;

				count += usesAnnotations(f.getNamespaces(i).getDeclarations(j));
			}

		return count;
	}

	@FunctionSpec(name = "uses_annotations", returnType = "int", formalParameters = { "Declaration" })
	public static int usesAnnotations(final Declaration d) {
		int count = 0;

		if (BoaModifierIntrinsics.hasAnnotation(d))
			count++;

		for (int i = 0; i < d.getMethodsCount(); i++)
			count += usesAnnotations(d.getMethods(i));

		for (int i = 0; i < d.getFieldsCount(); i++)
			count += usesAnnotations(d.getFields(i));

		for (int i = 0; i < d.getNestedDeclarationsCount(); i++)
			count += usesAnnotations(d.getNestedDeclarations(i));

		return count;
	}

	@FunctionSpec(name = "uses_annotations", returnType = "int", formalParameters = { "Method" })
	public static int usesAnnotations(final Method m) {
		int count = 0;

		if (BoaModifierIntrinsics.hasAnnotation(m))
			count++;

		for (int i = 0; i < m.getArgumentsCount(); i++)
			count += usesAnnotations(m.getArguments(i));

		for (int i = 0; i < m.getStatementsCount(); i++)
			count += usesAnnotations(m.getStatements(i));

		return count;
	}

	@FunctionSpec(name = "uses_annotations", returnType = "int", formalParameters = { "Variable" })
	public static int usesAnnotations(final Variable v) {
		int count = 0;

		if (BoaModifierIntrinsics.hasAnnotation(v))
			count++;

		if (v.hasInitializer())
			count += usesAnnotations(v.getInitializer());

		return count;
	}

	@FunctionSpec(name = "uses_annotations", returnType = "int", formalParameters = { "Statement" })
	public static int usesAnnotations(final Statement s) {
		int count = 0;

		if (s.hasCondition())
			count += usesAnnotations(s.getCondition());

		if (s.hasVariableDeclaration())
			count += usesAnnotations(s.getVariableDeclaration());

		if (s.hasTypeDeclaration())
			count += usesAnnotations(s.getTypeDeclaration());

		if (s.hasExpression())
			count += usesAnnotations(s.getExpression());

		for (int i = 0; i < s.getStatementsCount(); i++)
			count += usesAnnotations(s.getStatements(i));

		for (int i = 0; i < s.getInitializationsCount(); i++)
			count += usesAnnotations(s.getInitializations(i));

		for (int i = 0; i < s.getUpdatesCount(); i++)
			count += usesAnnotations(s.getUpdates(i));

		return count;
	}

	@FunctionSpec(name = "uses_annotations", returnType = "int", formalParameters = { "Expression" })
	public static int usesAnnotations(final Expression e) {
		int count = 0;

		for (int i = 0; i < e.getVariableDeclsCount(); i++)
			count += usesAnnotations(e.getVariableDecls(i));

		for (int i = 0; i < e.getExpressionsCount(); i++)
			count += usesAnnotations(e.getExpressions(i));

		for (int i = 0; i < e.getMethodArgsCount(); i++)
			count += usesAnnotations(e.getMethodArgs(i));

		return count;
	}

	@FunctionSpec(name = "uses_multi_catch", returnType = "int", formalParameters = { "CodeRepository" })
	public static int usesMultiCatch(final CodeRepository r) {
		int count = 0;

		for (int i = 0; i < r.getRevisionsCount(); i++)
			count += usesMultiCatch(r.getRevisions(i));

		return count;
	}

	@FunctionSpec(name = "uses_multi_catch", returnType = "int", formalParameters = { "Revision" })
	public static int usesMultiCatch(final Revision r) {
		int count = 0;

		for (int i = 0; i < r.getFilesCount(); i++)
			count += usesMultiCatch(r.getFiles(i));

		return count;
	}

	@FunctionSpec(name = "uses_multi_catch", returnType = "int", formalParameters = { "File" })
	public static int usesMultiCatch(final File f) {
		int count = 0;

		for (int i = 0; i < f.getNamespacesCount(); i++)
			for (int j = 0; j < f.getNamespaces(i).getDeclarationsCount(); j++)
				count += usesMultiCatch(f.getNamespaces(i).getDeclarations(j));

		return count;
	}

	@FunctionSpec(name = "uses_multi_catch", returnType = "int", formalParameters = { "Declaration" })
	public static int usesMultiCatch(final Declaration d) {
		int count = 0;

		for (int i = 0; i < d.getMethodsCount(); i++)
			count += usesMultiCatch(d.getMethods(i));

		for (int i = 0; i < d.getFieldsCount(); i++)
			count += usesMultiCatch(d.getFields(i));

		for (int i = 0; i < d.getNestedDeclarationsCount(); i++)
			count += usesMultiCatch(d.getNestedDeclarations(i));

		return count;
	}

	@FunctionSpec(name = "uses_multi_catch", returnType = "int", formalParameters = { "Method" })
	public static int usesMultiCatch(final Method m) {
		int count = 0;

		for (int i = 0; i < m.getArgumentsCount(); i++)
			count += usesMultiCatch(m.getArguments(i));

		for (int i = 0; i < m.getStatementsCount(); i++)
			count += usesMultiCatch(m.getStatements(i));

		return count;
	}

	@FunctionSpec(name = "uses_multi_catch", returnType = "int", formalParameters = { "Variable" })
	public static int usesMultiCatch(final Variable v) {
		int count = 0;

		if (v.hasInitializer())
			count += usesMultiCatch(v.getInitializer());

		return count;
	}

	@FunctionSpec(name = "uses_multi_catch", returnType = "int", formalParameters = { "Statement" })
	public static int usesMultiCatch(final Statement s) {
		int count = 0;

		if (s.getKind() == Statement.StatementKind.CATCH && s.getVariableDeclaration().getName().contains("|"))
			count++;

		if (s.hasCondition())
			count += usesMultiCatch(s.getCondition());

		if (s.hasVariableDeclaration())
			count += usesMultiCatch(s.getVariableDeclaration());

		if (s.hasTypeDeclaration())
			count += usesMultiCatch(s.getTypeDeclaration());

		if (s.hasExpression())
			count += usesMultiCatch(s.getExpression());

		for (int i = 0; i < s.getStatementsCount(); i++)
			count += usesMultiCatch(s.getStatements(i));

		for (int i = 0; i < s.getInitializationsCount(); i++)
			count += usesMultiCatch(s.getInitializations(i));

		for (int i = 0; i < s.getUpdatesCount(); i++)
			count += usesMultiCatch(s.getUpdates(i));

		return count;
	}

	@FunctionSpec(name = "uses_multi_catch", returnType = "int", formalParameters = { "Expression" })
	public static int usesMultiCatch(final Expression e) {
		int count = 0;

		for (int i = 0; i < e.getVariableDeclsCount(); i++)
			count += usesMultiCatch(e.getVariableDecls(i));

		for (int i = 0; i < e.getExpressionsCount(); i++)
			count += usesMultiCatch(e.getExpressions(i));

		for (int i = 0; i < e.getMethodArgsCount(); i++)
			count += usesMultiCatch(e.getMethodArgs(i));

		return count;
	}

	@FunctionSpec(name = "uses_binary_lit", returnType = "int", formalParameters = { "CodeRepository" })
	public static int usesBinaryLit(final CodeRepository r) {
		int count = 0;

		for (int i = 0; i < r.getRevisionsCount(); i++)
			count += usesBinaryLit(r.getRevisions(i));

		return count;
	}

	@FunctionSpec(name = "uses_binary_lit", returnType = "int", formalParameters = { "Revision" })
	public static int usesBinaryLit(final Revision r) {
		int count = 0;

		for (int i = 0; i < r.getFilesCount(); i++)
			count += usesBinaryLit(r.getFiles(i));

		return count;
	}

	@FunctionSpec(name = "uses_binary_lit", returnType = "int", formalParameters = { "File" })
	public static int usesBinaryLit(final File f) {
		int count = 0;

		for (int i = 0; i < f.getNamespacesCount(); i++)
			for (int j = 0; j < f.getNamespaces(i).getDeclarationsCount(); j++)
				count += usesBinaryLit(f.getNamespaces(i).getDeclarations(j));

		return count;
	}

	@FunctionSpec(name = "uses_binary_lit", returnType = "int", formalParameters = { "Declaration" })
	public static int usesBinaryLit(final Declaration d) {
		int count = 0;

		for (int i = 0; i < d.getMethodsCount(); i++)
			count += usesBinaryLit(d.getMethods(i));

		for (int i = 0; i < d.getFieldsCount(); i++)
			count += usesBinaryLit(d.getFields(i));

		for (int i = 0; i < d.getNestedDeclarationsCount(); i++)
			count += usesBinaryLit(d.getNestedDeclarations(i));

		return count;
	}

	@FunctionSpec(name = "uses_binary_lit", returnType = "int", formalParameters = { "Method" })
	public static int usesBinaryLit(final Method m) {
		int count = 0;

		for (int i = 0; i < m.getArgumentsCount(); i++)
			count += usesBinaryLit(m.getArguments(i));

		for (int i = 0; i < m.getStatementsCount(); i++)
			count += usesBinaryLit(m.getStatements(i));

		return count;
	}

	@FunctionSpec(name = "uses_binary_lit", returnType = "int", formalParameters = { "Variable" })
	public static int usesBinaryLit(final Variable v) {
		int count = 0;

		if (v.hasInitializer())
			count += usesBinaryLit(v.getInitializer());

		return count;
	}

	@FunctionSpec(name = "uses_binary_lit", returnType = "int", formalParameters = { "Statement" })
	public static int usesBinaryLit(final Statement s) {
		int count = 0;

		if (s.hasCondition())
			count += usesBinaryLit(s.getCondition());

		if (s.hasVariableDeclaration())
			count += usesBinaryLit(s.getVariableDeclaration());

		if (s.hasTypeDeclaration())
			count += usesBinaryLit(s.getTypeDeclaration());

		if (s.hasExpression())
			count += usesBinaryLit(s.getExpression());

		for (int i = 0; i < s.getStatementsCount(); i++)
			count += usesBinaryLit(s.getStatements(i));

		for (int i = 0; i < s.getInitializationsCount(); i++)
			count += usesBinaryLit(s.getInitializations(i));

		for (int i = 0; i < s.getUpdatesCount(); i++)
			count += usesBinaryLit(s.getUpdates(i));

		return count;
	}

	// this regex is a bit generous, but will definitely find what we want
	// the invalid things it allows are actually invalid syntax, so who
	// cares (we only deal with error-free parsed source)
	private static Matcher binaryMatcher = Pattern.compile("0[bB][01][01_]*[01][L]?").matcher("");

	@FunctionSpec(name = "uses_binary_lit", returnType = "int", formalParameters = { "Expression" })
	public static int usesBinaryLit(final Expression e) {
		int count = 0;

		if (e.getKind() == Expression.ExpressionKind.LITERAL && e.hasLiteral() && binaryMatcher.reset(e.getLiteral()).matches())
			count++;

		for (int i = 0; i < e.getVariableDeclsCount(); i++)
			count += usesBinaryLit(e.getVariableDecls(i));

		for (int i = 0; i < e.getExpressionsCount(); i++)
			count += usesBinaryLit(e.getExpressions(i));

		for (int i = 0; i < e.getMethodArgsCount(); i++)
			count += usesBinaryLit(e.getMethodArgs(i));

		return count;
	}

	@FunctionSpec(name = "uses_underscore_lit", returnType = "int", formalParameters = { "CodeRepository" })
	public static int usesUnderscoreLit(final CodeRepository r) {
		int count = 0;

		for (int i = 0; i < r.getRevisionsCount(); i++)
			count += usesUnderscoreLit(r.getRevisions(i));

		return count;
	}

	@FunctionSpec(name = "uses_underscore_lit", returnType = "int", formalParameters = { "Revision" })
	public static int usesUnderscoreLit(final Revision r) {
		int count = 0;

		for (int i = 0; i < r.getFilesCount(); i++)
			count += usesUnderscoreLit(r.getFiles(i));

		return count;
	}

	@FunctionSpec(name = "uses_underscore_lit", returnType = "int", formalParameters = { "File" })
	public static int usesUnderscoreLit(final File f) {
		int count = 0;

		for (int i = 0; i < f.getNamespacesCount(); i++)
			for (int j = 0; j < f.getNamespaces(i).getDeclarationsCount(); j++)
				count += usesUnderscoreLit(f.getNamespaces(i).getDeclarations(j));

		return count;
	}

	@FunctionSpec(name = "uses_underscore_lit", returnType = "int", formalParameters = { "Declaration" })
	public static int usesUnderscoreLit(final Declaration d) {
		int count = 0;

		for (int i = 0; i < d.getMethodsCount(); i++)
			count += usesUnderscoreLit(d.getMethods(i));

		for (int i = 0; i < d.getFieldsCount(); i++)
			count += usesUnderscoreLit(d.getFields(i));

		for (int i = 0; i < d.getNestedDeclarationsCount(); i++)
			count += usesUnderscoreLit(d.getNestedDeclarations(i));

		return count;
	}

	@FunctionSpec(name = "uses_underscore_lit", returnType = "int", formalParameters = { "Method" })
	public static int usesUnderscoreLit(final Method m) {
		int count = 0;

		for (int i = 0; i < m.getArgumentsCount(); i++)
			count += usesUnderscoreLit(m.getArguments(i));

		for (int i = 0; i < m.getStatementsCount(); i++)
			count += usesUnderscoreLit(m.getStatements(i));

		return count;
	}

	@FunctionSpec(name = "uses_underscore_lit", returnType = "int", formalParameters = { "Variable" })
	public static int usesUnderscoreLit(final Variable v) {
		int count = 0;

		if (v.hasInitializer())
			count += usesUnderscoreLit(v.getInitializer());

		return count;
	}

	@FunctionSpec(name = "uses_underscore_lit", returnType = "int", formalParameters = { "Statement" })
	public static int usesUnderscoreLit(final Statement s) {
		int count = 0;

		if (s.hasCondition())
			count += usesUnderscoreLit(s.getCondition());

		if (s.hasVariableDeclaration())
			count += usesUnderscoreLit(s.getVariableDeclaration());

		if (s.hasTypeDeclaration())
			count += usesUnderscoreLit(s.getTypeDeclaration());

		if (s.hasExpression())
			count += usesUnderscoreLit(s.getExpression());

		for (int i = 0; i < s.getStatementsCount(); i++)
			count += usesUnderscoreLit(s.getStatements(i));

		for (int i = 0; i < s.getInitializationsCount(); i++)
			count += usesUnderscoreLit(s.getInitializations(i));

		for (int i = 0; i < s.getUpdatesCount(); i++)
			count += usesUnderscoreLit(s.getUpdates(i));

		return count;
	}

	// this regex is a bit generous, but will definitely find what we want
	// the invalid things it allows are actually invalid syntax, so who
	// cares (we only deal with error-free parsed source)
	private static Matcher underscoreMatcher = Pattern.compile("(0[bBx])?([0-9]+.[0-9]+)?[0-9A-Fa-f]([0-9A-Fa-f_])*[0-9A-Fa-f][FL]?").matcher("");

	@FunctionSpec(name = "uses_underscore_lit", returnType = "int", formalParameters = { "Expression" })
	public static int usesUnderscoreLit(final Expression e) {
		int count = 0;

		if (e.getKind() == Expression.ExpressionKind.LITERAL && e.hasLiteral()
				&& e.getLiteral().contains("_") && underscoreMatcher.reset(e.getLiteral()).matches())
			count++;

		for (int i = 0; i < e.getVariableDeclsCount(); i++)
			count += usesUnderscoreLit(e.getVariableDecls(i));

		for (int i = 0; i < e.getExpressionsCount(); i++)
			count += usesUnderscoreLit(e.getExpressions(i));

		for (int i = 0; i < e.getMethodArgsCount(); i++)
			count += usesUnderscoreLit(e.getMethodArgs(i));

		return count;
	}

	@FunctionSpec(name = "uses_diamond", returnType = "int", formalParameters = { "CodeRepository" })
	public static int usesDiamond(final CodeRepository r) {
		int count = 0;

		for (int i = 0; i < r.getRevisionsCount(); i++)
			count += usesDiamond(r.getRevisions(i));

		return count;
	}

	@FunctionSpec(name = "uses_diamond", returnType = "int", formalParameters = { "Revision" })
	public static int usesDiamond(final Revision r) {
		int count = 0;

		for (int i = 0; i < r.getFilesCount(); i++)
			count += usesDiamond(r.getFiles(i));

		return count;
	}

	@FunctionSpec(name = "uses_diamond", returnType = "int", formalParameters = { "File" })
	public static int usesDiamond(final File f) {
		int count = 0;

		for (int i = 0; i < f.getNamespacesCount(); i++)
			for (int j = 0; j < f.getNamespaces(i).getDeclarationsCount(); j++)
				count += usesDiamond(f.getNamespaces(i).getDeclarations(j));

		return count;
	}

	@FunctionSpec(name = "uses_diamond", returnType = "int", formalParameters = { "Declaration" })
	public static int usesDiamond(final Declaration d) {
		int count = 0;

		for (int i = 0; i < d.getMethodsCount(); i++)
			count += usesDiamond(d.getMethods(i));

		for (int i = 0; i < d.getFieldsCount(); i++)
			count += usesDiamond(d.getFields(i));

		for (int i = 0; i < d.getNestedDeclarationsCount(); i++)
			count += usesDiamond(d.getNestedDeclarations(i));

		return count;
	}

	@FunctionSpec(name = "uses_diamond", returnType = "int", formalParameters = { "Method" })
	public static int usesDiamond(final Method m) {
		int count = 0;

		for (int i = 0; i < m.getStatementsCount(); i++)
			count += usesDiamond(m.getStatements(i));

		return count;
	}

	@FunctionSpec(name = "uses_diamond", returnType = "int", formalParameters = { "Variable" })
	public static int usesDiamond(final Variable v) {
		int count = 0;

		if (v.hasInitializer())
			count += usesDiamond(v.getInitializer());

		return count;
	}

	@FunctionSpec(name = "uses_diamond", returnType = "int", formalParameters = { "Statement" })
	public static int usesDiamond(final Statement s) {
		int count = 0;

		if (s.hasCondition())
			count += usesDiamond(s.getCondition());

		if (s.hasVariableDeclaration())
			count += usesDiamond(s.getVariableDeclaration());

		if (s.hasTypeDeclaration())
			count += usesDiamond(s.getTypeDeclaration());

		if (s.hasExpression())
			count += usesDiamond(s.getExpression());

		for (int i = 0; i < s.getStatementsCount(); i++)
			count += usesDiamond(s.getStatements(i));

		for (int i = 0; i < s.getInitializationsCount(); i++)
			count += usesDiamond(s.getInitializations(i));

		for (int i = 0; i < s.getUpdatesCount(); i++)
			count += usesDiamond(s.getUpdates(i));

		return count;
	}

	@FunctionSpec(name = "uses_diamond", returnType = "int", formalParameters = { "Expression" })
	public static int usesDiamond(final Expression e) {
		int count = 0;

		if (e.hasNewType())
			count += usesDiamond(e.getNewType());

		for (int i = 0; i < e.getVariableDeclsCount(); i++)
			count += usesDiamond(e.getVariableDecls(i));

		for (int i = 0; i < e.getExpressionsCount(); i++)
			count += usesDiamond(e.getExpressions(i));

		for (int i = 0; i < e.getMethodArgsCount(); i++)
			count += usesDiamond(e.getMethodArgs(i));

		return count;
	}

	@FunctionSpec(name = "uses_diamond", returnType = "int", formalParameters = { "Type" })
	public static int usesDiamond(final Type t) {
		int count = 0;

		if (t.getName().contains("<>"))
			count++;

		return count;
	}
}
