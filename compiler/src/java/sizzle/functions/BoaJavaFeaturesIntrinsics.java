package sizzle.functions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sizzle.types.Ast.Expression.ExpressionKind;
import sizzle.types.Ast.*;
import sizzle.types.Code.*;
import sizzle.types.Diff.*;

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
	public static int usesEnhancedFor(final ChangedFile f) {
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
	public static int usesVarargs(final ChangedFile f) {
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
	public static int usesAssert(final ChangedFile f) {
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
	public static int usesEnums(final ChangedFile f) {
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
	public static int usesTryResources(final ChangedFile f) {
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

	@FunctionSpec(name = "uses_generics_define_type", returnType = "int", formalParameters = { "CodeRepository" })
	public static int usesGenericsDefineType(final CodeRepository r) {
		int count = 0;

		for (int i = 0; i < r.getRevisionsCount(); i++)
			count += usesGenericsDefineType(r.getRevisions(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_type", returnType = "int", formalParameters = { "Revision" })
	public static int usesGenericsDefineType(final Revision r) {
		int count = 0;

		for (int i = 0; i < r.getFilesCount(); i++)
			count += usesGenericsDefineType(r.getFiles(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_type", returnType = "int", formalParameters = { "File" })
	public static int usesGenericsDefineType(final ChangedFile f) {
		int count = 0;

		for (int i = 0; i < f.getNamespacesCount(); i++)
			for (int j = 0; j < f.getNamespaces(i).getDeclarationsCount(); j++)
				count += usesGenericsDefineType(f.getNamespaces(i).getDeclarations(j));

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_type", returnType = "int", formalParameters = { "Declaration" })
	public static int usesGenericsDefineType(final Declaration d) {
		int count = 0;

		if (d.getGenericParametersCount() > 0)
			count++;

		for (int i = 0; i < d.getMethodsCount(); i++)
			count += usesGenericsDefineType(d.getMethods(i));

		for (int i = 0; i < d.getFieldsCount(); i++)
			count += usesGenericsDefineType(d.getFields(i));

		for (int i = 0; i < d.getNestedDeclarationsCount(); i++)
			count += usesGenericsDefineType(d.getNestedDeclarations(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_type", returnType = "int", formalParameters = { "Method" })
	public static int usesGenericsDefineType(final Method m) {
		int count = 0;

		for (int i = 0; i < m.getArgumentsCount(); i++)
			count += usesGenericsDefineType(m.getArguments(i));

		for (int i = 0; i < m.getStatementsCount(); i++)
			count += usesGenericsDefineType(m.getStatements(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_type", returnType = "int", formalParameters = { "Variable" })
	public static int usesGenericsDefineType(final Variable v) {
		int count = 0;

		if (v.hasInitializer())
			count += usesGenericsDefineType(v.getInitializer());

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_type", returnType = "int", formalParameters = { "Statement" })
	public static int usesGenericsDefineType(final Statement s) {
		int count = 0;

		if (s.hasCondition())
			count += usesGenericsDefineType(s.getCondition());

		if (s.hasVariableDeclaration())
			count += usesGenericsDefineType(s.getVariableDeclaration());

		if (s.hasTypeDeclaration())
			count += usesGenericsDefineType(s.getTypeDeclaration());

		if (s.hasExpression())
			count += usesGenericsDefineType(s.getExpression());

		for (int i = 0; i < s.getStatementsCount(); i++)
			count += usesGenericsDefineType(s.getStatements(i));

		for (int i = 0; i < s.getInitializationsCount(); i++)
			count += usesGenericsDefineType(s.getInitializations(i));

		for (int i = 0; i < s.getUpdatesCount(); i++)
			count += usesGenericsDefineType(s.getUpdates(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_type", returnType = "int", formalParameters = { "Expression" })
	public static int usesGenericsDefineType(final Expression e) {
		int count = 0;

		for (int i = 0; i < e.getVariableDeclsCount(); i++)
			count += usesGenericsDefineType(e.getVariableDecls(i));

		for (int i = 0; i < e.getExpressionsCount(); i++)
			count += usesGenericsDefineType(e.getExpressions(i));

		for (int i = 0; i < e.getMethodArgsCount(); i++)
			count += usesGenericsDefineType(e.getMethodArgs(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_method", returnType = "int", formalParameters = { "CodeRepository" })
	public static int usesGenericsDefineMethod(final CodeRepository r) {
		int count = 0;

		for (int i = 0; i < r.getRevisionsCount(); i++)
			count += usesGenericsDefineMethod(r.getRevisions(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_method", returnType = "int", formalParameters = { "Revision" })
	public static int usesGenericsDefineMethod(final Revision r) {
		int count = 0;

		for (int i = 0; i < r.getFilesCount(); i++)
			count += usesGenericsDefineMethod(r.getFiles(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_method", returnType = "int", formalParameters = { "File" })
	public static int usesGenericsDefineMethod(final ChangedFile f) {
		int count = 0;

		for (int i = 0; i < f.getNamespacesCount(); i++)
			for (int j = 0; j < f.getNamespaces(i).getDeclarationsCount(); j++)
				count += usesGenericsDefineMethod(f.getNamespaces(i).getDeclarations(j));

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_method", returnType = "int", formalParameters = { "Declaration" })
	public static int usesGenericsDefineMethod(final Declaration d) {
		int count = 0;

		for (int i = 0; i < d.getMethodsCount(); i++)
			count += usesGenericsDefineMethod(d.getMethods(i));

		for (int i = 0; i < d.getFieldsCount(); i++)
			count += usesGenericsDefineMethod(d.getFields(i));

		for (int i = 0; i < d.getNestedDeclarationsCount(); i++)
			count += usesGenericsDefineMethod(d.getNestedDeclarations(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_method", returnType = "int", formalParameters = { "Method" })
	public static int usesGenericsDefineMethod(final Method m) {
		int count = 0;

		if (m.getGenericParametersCount() > 0)
			count++;

		for (int i = 0; i < m.getArgumentsCount(); i++)
			count += usesGenericsDefineMethod(m.getArguments(i));

		for (int i = 0; i < m.getStatementsCount(); i++)
			count += usesGenericsDefineMethod(m.getStatements(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_method", returnType = "int", formalParameters = { "Variable" })
	public static int usesGenericsDefineMethod(final Variable v) {
		int count = 0;

		if (v.hasInitializer())
			count += usesGenericsDefineMethod(v.getInitializer());

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_method", returnType = "int", formalParameters = { "Statement" })
	public static int usesGenericsDefineMethod(final Statement s) {
		int count = 0;

		if (s.hasCondition())
			count += usesGenericsDefineMethod(s.getCondition());

		if (s.hasVariableDeclaration())
			count += usesGenericsDefineMethod(s.getVariableDeclaration());

		if (s.hasTypeDeclaration())
			count += usesGenericsDefineMethod(s.getTypeDeclaration());

		if (s.hasExpression())
			count += usesGenericsDefineMethod(s.getExpression());

		for (int i = 0; i < s.getStatementsCount(); i++)
			count += usesGenericsDefineMethod(s.getStatements(i));

		for (int i = 0; i < s.getInitializationsCount(); i++)
			count += usesGenericsDefineMethod(s.getInitializations(i));

		for (int i = 0; i < s.getUpdatesCount(); i++)
			count += usesGenericsDefineMethod(s.getUpdates(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_method", returnType = "int", formalParameters = { "Expression" })
	public static int usesGenericsDefineMethod(final Expression e) {
		int count = 0;

		for (int i = 0; i < e.getVariableDeclsCount(); i++)
			count += usesGenericsDefineMethod(e.getVariableDecls(i));

		for (int i = 0; i < e.getExpressionsCount(); i++)
			count += usesGenericsDefineMethod(e.getExpressions(i));

		for (int i = 0; i < e.getMethodArgsCount(); i++)
			count += usesGenericsDefineMethod(e.getMethodArgs(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_field", returnType = "int", formalParameters = { "CodeRepository" })
	public static int usesGenericsDefineField(final CodeRepository r) {
		int count = 0;

		for (int i = 0; i < r.getRevisionsCount(); i++)
			count += usesGenericsDefineField(r.getRevisions(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_field", returnType = "int", formalParameters = { "Revision" })
	public static int usesGenericsDefineField(final Revision r) {
		int count = 0;

		for (int i = 0; i < r.getFilesCount(); i++)
			count += usesGenericsDefineField(r.getFiles(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_field", returnType = "int", formalParameters = { "File" })
	public static int usesGenericsDefineField(final ChangedFile f) {
		int count = 0;

		for (int i = 0; i < f.getNamespacesCount(); i++)
			for (int j = 0; j < f.getNamespaces(i).getDeclarationsCount(); j++)
				count += usesGenericsDefineField(f.getNamespaces(i).getDeclarations(j));

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_field", returnType = "int", formalParameters = { "Declaration" })
	public static int usesGenericsDefineField(final Declaration d) {
		int count = 0;

		for (int i = 0; i < d.getMethodsCount(); i++)
			count += usesGenericsDefineField(d.getMethods(i));

		for (int i = 0; i < d.getFieldsCount(); i++)
			count += usesGenericsDefineField(d.getFields(i));

		for (int i = 0; i < d.getNestedDeclarationsCount(); i++)
			count += usesGenericsDefineField(d.getNestedDeclarations(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_field", returnType = "int", formalParameters = { "Method" })
	public static int usesGenericsDefineField(final Method m) {
		int count = 0;

		for (int i = 0; i < m.getArgumentsCount(); i++)
			count += usesGenericsDefineField(m.getArguments(i));

		for (int i = 0; i < m.getStatementsCount(); i++)
			count += usesGenericsDefineField(m.getStatements(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_field", returnType = "int", formalParameters = { "Variable" })
	public static int usesGenericsDefineField(final Variable v) {
		int count = 0;

		count += usesGenericsDefineField(v.getVariableType());

		if (v.hasInitializer())
			count += usesGenericsDefineField(v.getInitializer());

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_field", returnType = "int", formalParameters = { "Statement" })
	public static int usesGenericsDefineField(final Statement s) {
		int count = 0;

		if (s.hasCondition())
			count += usesGenericsDefineField(s.getCondition());

		if (s.hasVariableDeclaration())
			count += usesGenericsDefineField(s.getVariableDeclaration());

		if (s.hasTypeDeclaration())
			count += usesGenericsDefineField(s.getTypeDeclaration());

		if (s.hasExpression())
			count += usesGenericsDefineField(s.getExpression());

		for (int i = 0; i < s.getStatementsCount(); i++)
			count += usesGenericsDefineField(s.getStatements(i));

		for (int i = 0; i < s.getInitializationsCount(); i++)
			count += usesGenericsDefineField(s.getInitializations(i));

		for (int i = 0; i < s.getUpdatesCount(); i++)
			count += usesGenericsDefineField(s.getUpdates(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_field", returnType = "int", formalParameters = { "Expression" })
	public static int usesGenericsDefineField(final Expression e) {
		int count = 0;

		for (int i = 0; i < e.getVariableDeclsCount(); i++)
			count += usesGenericsDefineField(e.getVariableDecls(i));

		for (int i = 0; i < e.getExpressionsCount(); i++)
			count += usesGenericsDefineField(e.getExpressions(i));

		for (int i = 0; i < e.getMethodArgsCount(); i++)
			count += usesGenericsDefineField(e.getMethodArgs(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_field", returnType = "int", formalParameters = { "Type" })
	public static int usesGenericsDefineField(final Type t) {
		int count = 0;

		if (t.getName().contains("<"))
			count++;

		return count;
	}

	@FunctionSpec(name = "uses_generics_wildcard_super", returnType = "int", formalParameters = { "CodeRepository" })
	public static int usesGenericsWildcardSuper(final CodeRepository r) {
		int count = 0;

		for (int i = 0; i < r.getRevisionsCount(); i++)
			count += usesGenericsWildcardSuper(r.getRevisions(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_wildcard_super", returnType = "int", formalParameters = { "Revision" })
	public static int usesGenericsWildcardSuper(final Revision r) {
		int count = 0;

		for (int i = 0; i < r.getFilesCount(); i++)
			count += usesGenericsWildcardSuper(r.getFiles(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_wildcard_super", returnType = "int", formalParameters = { "File" })
	public static int usesGenericsWildcardSuper(final ChangedFile f) {
		int count = 0;

		for (int i = 0; i < f.getNamespacesCount(); i++)
			for (int j = 0; j < f.getNamespaces(i).getDeclarationsCount(); j++)
				count += usesGenericsWildcardSuper(f.getNamespaces(i).getDeclarations(j));

		return count;
	}

	@FunctionSpec(name = "uses_generics_wildcard_super", returnType = "int", formalParameters = { "Declaration" })
	public static int usesGenericsWildcardSuper(final Declaration d) {
		int count = 0;

		for (int i = 0; i < d.getGenericParametersCount(); i++)
			count += usesGenericsWildcardSuper(d.getGenericParameters(i));

		for (int i = 0; i < d.getParentsCount(); i++)
			count += usesGenericsWildcardSuper(d.getParents(i));

		for (int i = 0; i < d.getMethodsCount(); i++)
			count += usesGenericsWildcardSuper(d.getMethods(i));

		for (int i = 0; i < d.getFieldsCount(); i++)
			count += usesGenericsWildcardSuper(d.getFields(i));

		for (int i = 0; i < d.getNestedDeclarationsCount(); i++)
			count += usesGenericsWildcardSuper(d.getNestedDeclarations(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_wildcard_super", returnType = "int", formalParameters = { "Method" })
	public static int usesGenericsWildcardSuper(final Method m) {
		int count = 0;

		for (int i = 0; i < m.getGenericParametersCount(); i++)
			count += usesGenericsWildcardSuper(m.getGenericParameters(i));

		count += usesGenericsWildcardSuper(m.getReturnType());

		for (int i = 0; i < m.getExceptionTypesCount(); i++)
			count += usesGenericsWildcardSuper(m.getExceptionTypes(i));

		for (int i = 0; i < m.getArgumentsCount(); i++)
			count += usesGenericsWildcardSuper(m.getArguments(i));

		for (int i = 0; i < m.getStatementsCount(); i++)
			count += usesGenericsWildcardSuper(m.getStatements(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_wildcard_super", returnType = "int", formalParameters = { "Variable" })
	public static int usesGenericsWildcardSuper(final Variable v) {
		int count = 0;

		count += usesGenericsWildcardSuper(v.getVariableType());

		if (v.hasInitializer())
			count += usesGenericsWildcardSuper(v.getInitializer());

		return count;
	}

	@FunctionSpec(name = "uses_generics_wildcard_super", returnType = "int", formalParameters = { "Statement" })
	public static int usesGenericsWildcardSuper(final Statement s) {
		int count = 0;

		if (s.hasCondition())
			count += usesGenericsWildcardSuper(s.getCondition());

		if (s.hasVariableDeclaration())
			count += usesGenericsWildcardSuper(s.getVariableDeclaration());

		if (s.hasTypeDeclaration())
			count += usesGenericsWildcardSuper(s.getTypeDeclaration());

		if (s.hasExpression())
			count += usesGenericsWildcardSuper(s.getExpression());

		for (int i = 0; i < s.getStatementsCount(); i++)
			count += usesGenericsWildcardSuper(s.getStatements(i));

		for (int i = 0; i < s.getInitializationsCount(); i++)
			count += usesGenericsWildcardSuper(s.getInitializations(i));

		for (int i = 0; i < s.getUpdatesCount(); i++)
			count += usesGenericsWildcardSuper(s.getUpdates(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_wildcard_super", returnType = "int", formalParameters = { "Expression" })
	public static int usesGenericsWildcardSuper(final Expression e) {
		int count = 0;

		for (int i = 0; i < e.getGenericParametersCount(); i++)
			count += usesGenericsWildcardSuper(e.getGenericParameters(i));

		if (e.hasNewType())
			count += usesGenericsWildcardSuper(e.getNewType());

		for (int i = 0; i < e.getVariableDeclsCount(); i++)
			count += usesGenericsWildcardSuper(e.getVariableDecls(i));

		for (int i = 0; i < e.getExpressionsCount(); i++)
			count += usesGenericsWildcardSuper(e.getExpressions(i));

		for (int i = 0; i < e.getMethodArgsCount(); i++)
			count += usesGenericsWildcardSuper(e.getMethodArgs(i));

		return count;
	}

	private static Matcher wildcardSuperMatcher = Pattern.compile("<\\s*\\?\\s+super\\s+[^>]+>").matcher("");

	@FunctionSpec(name = "uses_generics_wildcard_super", returnType = "int", formalParameters = { "Type" })
	public static int usesGenericsWildcardSuper(final Type t) {
		int count = 0;

		if (wildcardSuperMatcher.reset(t.getName()).matches())
			count++;

		return count;
	}

	@FunctionSpec(name = "uses_generics_wildcard_extends", returnType = "int", formalParameters = { "CodeRepository" })
	public static int usesGenericsWildcardExtends(final CodeRepository r) {
		int count = 0;

		for (int i = 0; i < r.getRevisionsCount(); i++)
			count += usesGenericsWildcardExtends(r.getRevisions(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_wildcard_extends", returnType = "int", formalParameters = { "Revision" })
	public static int usesGenericsWildcardExtends(final Revision r) {
		int count = 0;

		for (int i = 0; i < r.getFilesCount(); i++)
			count += usesGenericsWildcardExtends(r.getFiles(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_wildcard_extends", returnType = "int", formalParameters = { "File" })
	public static int usesGenericsWildcardExtends(final ChangedFile f) {
		int count = 0;

		for (int i = 0; i < f.getNamespacesCount(); i++)
			for (int j = 0; j < f.getNamespaces(i).getDeclarationsCount(); j++)
				count += usesGenericsWildcardExtends(f.getNamespaces(i).getDeclarations(j));

		return count;
	}

	@FunctionSpec(name = "uses_generics_wildcard_extends", returnType = "int", formalParameters = { "Declaration" })
	public static int usesGenericsWildcardExtends(final Declaration d) {
		int count = 0;

		for (int i = 0; i < d.getGenericParametersCount(); i++)
			count += usesGenericsWildcardSuper(d.getGenericParameters(i));

		for (int i = 0; i < d.getParentsCount(); i++)
			count += usesGenericsWildcardExtends(d.getParents(i));

		for (int i = 0; i < d.getMethodsCount(); i++)
			count += usesGenericsWildcardExtends(d.getMethods(i));

		for (int i = 0; i < d.getFieldsCount(); i++)
			count += usesGenericsWildcardExtends(d.getFields(i));

		for (int i = 0; i < d.getNestedDeclarationsCount(); i++)
			count += usesGenericsWildcardExtends(d.getNestedDeclarations(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_wildcard_extends", returnType = "int", formalParameters = { "Method" })
	public static int usesGenericsWildcardExtends(final Method m) {
		int count = 0;

		for (int i = 0; i < m.getGenericParametersCount(); i++)
			count += usesGenericsWildcardSuper(m.getGenericParameters(i));

		count += usesGenericsWildcardExtends(m.getReturnType());

		for (int i = 0; i < m.getExceptionTypesCount(); i++)
			count += usesGenericsWildcardExtends(m.getExceptionTypes(i));

		for (int i = 0; i < m.getArgumentsCount(); i++)
			count += usesGenericsWildcardExtends(m.getArguments(i));

		for (int i = 0; i < m.getStatementsCount(); i++)
			count += usesGenericsWildcardExtends(m.getStatements(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_wildcard_extends", returnType = "int", formalParameters = { "Variable" })
	public static int usesGenericsWildcardExtends(final Variable v) {
		int count = 0;

		count += usesGenericsWildcardExtends(v.getVariableType());

		if (v.hasInitializer())
			count += usesGenericsWildcardExtends(v.getInitializer());

		return count;
	}

	@FunctionSpec(name = "uses_generics_wildcard_extends", returnType = "int", formalParameters = { "Statement" })
	public static int usesGenericsWildcardExtends(final Statement s) {
		int count = 0;

		if (s.hasCondition())
			count += usesGenericsWildcardExtends(s.getCondition());

		if (s.hasVariableDeclaration())
			count += usesGenericsWildcardExtends(s.getVariableDeclaration());

		if (s.hasTypeDeclaration())
			count += usesGenericsWildcardExtends(s.getTypeDeclaration());

		if (s.hasExpression())
			count += usesGenericsWildcardExtends(s.getExpression());

		for (int i = 0; i < s.getStatementsCount(); i++)
			count += usesGenericsWildcardExtends(s.getStatements(i));

		for (int i = 0; i < s.getInitializationsCount(); i++)
			count += usesGenericsWildcardExtends(s.getInitializations(i));

		for (int i = 0; i < s.getUpdatesCount(); i++)
			count += usesGenericsWildcardExtends(s.getUpdates(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_wildcard_extends", returnType = "int", formalParameters = { "Expression" })
	public static int usesGenericsWildcardExtends(final Expression e) {
		int count = 0;

		for (int i = 0; i < e.getGenericParametersCount(); i++)
			count += usesGenericsWildcardSuper(e.getGenericParameters(i));

		if (e.hasNewType())
			count += usesGenericsWildcardExtends(e.getNewType());

		for (int i = 0; i < e.getVariableDeclsCount(); i++)
			count += usesGenericsWildcardExtends(e.getVariableDecls(i));

		for (int i = 0; i < e.getExpressionsCount(); i++)
			count += usesGenericsWildcardExtends(e.getExpressions(i));

		for (int i = 0; i < e.getMethodArgsCount(); i++)
			count += usesGenericsWildcardExtends(e.getMethodArgs(i));

		return count;
	}

	private static Matcher wildcardExtendsMatcher = Pattern.compile("<\\s*\\?\\s+extends\\s+[^>]+>").matcher("");

	@FunctionSpec(name = "uses_generics_wildcard_extends", returnType = "int", formalParameters = { "Type" })
	public static int usesGenericsWildcardExtends(final Type t) {
		int count = 0;

		if (wildcardExtendsMatcher.reset(t.getName()).matches())
			count++;

		return count;
	}

	@FunctionSpec(name = "uses_generics_wildcard", returnType = "int", formalParameters = { "CodeRepository" })
	public static int usesGenericsWildcard(final CodeRepository r) {
		int count = 0;

		for (int i = 0; i < r.getRevisionsCount(); i++)
			count += usesGenericsWildcard(r.getRevisions(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_wildcard", returnType = "int", formalParameters = { "Revision" })
	public static int usesGenericsWildcard(final Revision r) {
		int count = 0;

		for (int i = 0; i < r.getFilesCount(); i++)
			count += usesGenericsWildcard(r.getFiles(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_wildcard", returnType = "int", formalParameters = { "File" })
	public static int usesGenericsWildcard(final ChangedFile f) {
		int count = 0;

		for (int i = 0; i < f.getNamespacesCount(); i++)
			for (int j = 0; j < f.getNamespaces(i).getDeclarationsCount(); j++)
				count += usesGenericsWildcard(f.getNamespaces(i).getDeclarations(j));

		return count;
	}

	@FunctionSpec(name = "uses_generics_wildcard", returnType = "int", formalParameters = { "Declaration" })
	public static int usesGenericsWildcard(final Declaration d) {
		int count = 0;

		for (int i = 0; i < d.getGenericParametersCount(); i++)
			count += usesGenericsWildcardSuper(d.getGenericParameters(i));

		for (int i = 0; i < d.getParentsCount(); i++)
			count += usesGenericsWildcard(d.getParents(i));

		for (int i = 0; i < d.getMethodsCount(); i++)
			count += usesGenericsWildcard(d.getMethods(i));

		for (int i = 0; i < d.getFieldsCount(); i++)
			count += usesGenericsWildcard(d.getFields(i));

		for (int i = 0; i < d.getNestedDeclarationsCount(); i++)
			count += usesGenericsWildcard(d.getNestedDeclarations(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_wildcard", returnType = "int", formalParameters = { "Method" })
	public static int usesGenericsWildcard(final Method m) {
		int count = 0;

		for (int i = 0; i < m.getGenericParametersCount(); i++)
			count += usesGenericsWildcardSuper(m.getGenericParameters(i));

		count += usesGenericsWildcard(m.getReturnType());

		for (int i = 0; i < m.getExceptionTypesCount(); i++)
			count += usesGenericsWildcard(m.getExceptionTypes(i));

		for (int i = 0; i < m.getArgumentsCount(); i++)
			count += usesGenericsWildcard(m.getArguments(i));

		for (int i = 0; i < m.getStatementsCount(); i++)
			count += usesGenericsWildcard(m.getStatements(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_wildcard", returnType = "int", formalParameters = { "Variable" })
	public static int usesGenericsWildcard(final Variable v) {
		int count = 0;

		count += usesGenericsWildcard(v.getVariableType());

		if (v.hasInitializer())
			count += usesGenericsWildcard(v.getInitializer());

		return count;
	}

	@FunctionSpec(name = "uses_generics_wildcard", returnType = "int", formalParameters = { "Statement" })
	public static int usesGenericsWildcard(final Statement s) {
		int count = 0;

		if (s.hasCondition())
			count += usesGenericsWildcard(s.getCondition());

		if (s.hasVariableDeclaration())
			count += usesGenericsWildcard(s.getVariableDeclaration());

		if (s.hasTypeDeclaration())
			count += usesGenericsWildcard(s.getTypeDeclaration());

		if (s.hasExpression())
			count += usesGenericsWildcard(s.getExpression());

		for (int i = 0; i < s.getStatementsCount(); i++)
			count += usesGenericsWildcard(s.getStatements(i));

		for (int i = 0; i < s.getInitializationsCount(); i++)
			count += usesGenericsWildcard(s.getInitializations(i));

		for (int i = 0; i < s.getUpdatesCount(); i++)
			count += usesGenericsWildcard(s.getUpdates(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_wildcard", returnType = "int", formalParameters = { "Expression" })
	public static int usesGenericsWildcard(final Expression e) {
		int count = 0;

		for (int i = 0; i < e.getGenericParametersCount(); i++)
			count += usesGenericsWildcardSuper(e.getGenericParameters(i));

		if (e.hasNewType())
			count += usesGenericsWildcard(e.getNewType());

		for (int i = 0; i < e.getVariableDeclsCount(); i++)
			count += usesGenericsWildcard(e.getVariableDecls(i));

		for (int i = 0; i < e.getExpressionsCount(); i++)
			count += usesGenericsWildcard(e.getExpressions(i));

		for (int i = 0; i < e.getMethodArgsCount(); i++)
			count += usesGenericsWildcard(e.getMethodArgs(i));

		return count;
	}

	private static Matcher wildcardMatcher = Pattern.compile("<\\s*\\?\\s*>").matcher("");

	@FunctionSpec(name = "uses_generics_wildcard", returnType = "int", formalParameters = { "Type" })
	public static int usesGenericsWildcard(final Type t) {
		int count = 0;

		if (wildcardMatcher.reset(t.getName()).matches())
			count++;

		return count;
	}

	@FunctionSpec(name = "uses_annotations_define", returnType = "int", formalParameters = { "CodeRepository" })
	public static int usesAnnotationsDefine(final CodeRepository r) {
		int count = 0;

		for (int i = 0; i < r.getRevisionsCount(); i++)
			count += usesAnnotationsDefine(r.getRevisions(i));

		return count;
	}

	@FunctionSpec(name = "uses_annotations_define", returnType = "int", formalParameters = { "Revision" })
	public static int usesAnnotationsDefine(final Revision r) {
		int count = 0;

		for (int i = 0; i < r.getFilesCount(); i++)
			count += usesAnnotationsDefine(r.getFiles(i));

		return count;
	}

	@FunctionSpec(name = "uses_annotations_define", returnType = "int", formalParameters = { "File" })
	public static int usesAnnotationsDefine(final ChangedFile f) {
		int count = 0;

		for (int i = 0; i < f.getNamespacesCount(); i++)
			for (int j = 0; j < f.getNamespaces(i).getDeclarationsCount(); j++)
				count += usesAnnotationsDefine(f.getNamespaces(i).getDeclarations(j));

		return count;
	}

	@FunctionSpec(name = "uses_annotations_define", returnType = "int", formalParameters = { "Declaration" })
	public static int usesAnnotationsDefine(final Declaration d) {
		int count = 0;

		if (d.getKind() == TypeKind.ANNOTATION)
			count++;

		for (int i = 0; i < d.getMethodsCount(); i++)
			count += usesAnnotationsDefine(d.getMethods(i));

		for (int i = 0; i < d.getFieldsCount(); i++)
			count += usesAnnotationsDefine(d.getFields(i));

		for (int i = 0; i < d.getNestedDeclarationsCount(); i++)
			count += usesAnnotationsDefine(d.getNestedDeclarations(i));

		return count;
	}

	@FunctionSpec(name = "uses_annotations_define", returnType = "int", formalParameters = { "Method" })
	public static int usesAnnotationsDefine(final Method m) {
		int count = 0;

		for (int i = 0; i < m.getArgumentsCount(); i++)
			count += usesAnnotationsDefine(m.getArguments(i));

		for (int i = 0; i < m.getStatementsCount(); i++)
			count += usesAnnotationsDefine(m.getStatements(i));

		return count;
	}

	@FunctionSpec(name = "uses_annotations_define", returnType = "int", formalParameters = { "Variable" })
	public static int usesAnnotationsDefine(final Variable v) {
		int count = 0;

		if (v.hasInitializer())
			count += usesAnnotationsDefine(v.getInitializer());

		return count;
	}

	@FunctionSpec(name = "uses_annotations_define", returnType = "int", formalParameters = { "Statement" })
	public static int usesAnnotationsDefine(final Statement s) {
		int count = 0;

		if (s.hasCondition())
			count += usesAnnotationsDefine(s.getCondition());

		if (s.hasVariableDeclaration())
			count += usesAnnotationsDefine(s.getVariableDeclaration());

		if (s.hasTypeDeclaration())
			count += usesAnnotationsDefine(s.getTypeDeclaration());

		if (s.hasExpression())
			count += usesAnnotationsDefine(s.getExpression());

		for (int i = 0; i < s.getStatementsCount(); i++)
			count += usesAnnotationsDefine(s.getStatements(i));

		for (int i = 0; i < s.getInitializationsCount(); i++)
			count += usesAnnotationsDefine(s.getInitializations(i));

		for (int i = 0; i < s.getUpdatesCount(); i++)
			count += usesAnnotationsDefine(s.getUpdates(i));

		return count;
	}

	@FunctionSpec(name = "uses_annotations_define", returnType = "int", formalParameters = { "Expression" })
	public static int usesAnnotationsDefine(final Expression e) {
		int count = 0;

		for (int i = 0; i < e.getVariableDeclsCount(); i++)
			count += usesAnnotationsDefine(e.getVariableDecls(i));

		for (int i = 0; i < e.getExpressionsCount(); i++)
			count += usesAnnotationsDefine(e.getExpressions(i));

		for (int i = 0; i < e.getMethodArgsCount(); i++)
			count += usesAnnotationsDefine(e.getMethodArgs(i));

		return count;
	}

	@FunctionSpec(name = "uses_annotations_uses", returnType = "int", formalParameters = { "CodeRepository" })
	public static int usesAnnotationsUses(final CodeRepository r) {
		int count = 0;

		for (int i = 0; i < r.getRevisionsCount(); i++)
			count += usesAnnotationsUses(r.getRevisions(i));

		return count;
	}

	@FunctionSpec(name = "uses_annotations_uses", returnType = "int", formalParameters = { "Revision" })
	public static int usesAnnotationsUses(final Revision r) {
		int count = 0;

		for (int i = 0; i < r.getFilesCount(); i++)
			count += usesAnnotationsUses(r.getFiles(i));

		return count;
	}

	@FunctionSpec(name = "uses_annotations_uses", returnType = "int", formalParameters = { "File" })
	public static int usesAnnotationsUses(final ChangedFile f) {
		int count = 0;

		for (int i = 0; i < f.getNamespacesCount(); i++)
			for (int j = 0; j < f.getNamespaces(i).getDeclarationsCount(); j++) {
				if (BoaModifierIntrinsics.hasAnnotation(f.getNamespaces(i)))
					count++;

				count += usesAnnotationsUses(f.getNamespaces(i).getDeclarations(j));
			}

		return count;
	}

	@FunctionSpec(name = "uses_annotations_uses", returnType = "int", formalParameters = { "Declaration" })
	public static int usesAnnotationsUses(final Declaration d) {
		int count = 0;

		if (BoaModifierIntrinsics.hasAnnotation(d))
			count++;

		for (int i = 0; i < d.getMethodsCount(); i++)
			count += usesAnnotationsUses(d.getMethods(i));

		for (int i = 0; i < d.getFieldsCount(); i++)
			count += usesAnnotationsUses(d.getFields(i));

		for (int i = 0; i < d.getNestedDeclarationsCount(); i++)
			count += usesAnnotationsUses(d.getNestedDeclarations(i));

		return count;
	}

	@FunctionSpec(name = "uses_annotations_uses", returnType = "int", formalParameters = { "Method" })
	public static int usesAnnotationsUses(final Method m) {
		int count = 0;

		if (BoaModifierIntrinsics.hasAnnotation(m))
			count++;

		for (int i = 0; i < m.getArgumentsCount(); i++)
			count += usesAnnotationsUses(m.getArguments(i));

		for (int i = 0; i < m.getStatementsCount(); i++)
			count += usesAnnotationsUses(m.getStatements(i));

		return count;
	}

	@FunctionSpec(name = "uses_annotations_uses", returnType = "int", formalParameters = { "Variable" })
	public static int usesAnnotationsUses(final Variable v) {
		int count = 0;

		if (BoaModifierIntrinsics.hasAnnotation(v))
			count++;

		if (v.hasInitializer())
			count += usesAnnotationsUses(v.getInitializer());

		return count;
	}

	@FunctionSpec(name = "uses_annotations_uses", returnType = "int", formalParameters = { "Statement" })
	public static int usesAnnotationsUses(final Statement s) {
		int count = 0;

		if (s.hasCondition())
			count += usesAnnotationsUses(s.getCondition());

		if (s.hasVariableDeclaration())
			count += usesAnnotationsUses(s.getVariableDeclaration());

		if (s.hasTypeDeclaration())
			count += usesAnnotationsUses(s.getTypeDeclaration());

		if (s.hasExpression())
			count += usesAnnotationsUses(s.getExpression());

		for (int i = 0; i < s.getStatementsCount(); i++)
			count += usesAnnotationsUses(s.getStatements(i));

		for (int i = 0; i < s.getInitializationsCount(); i++)
			count += usesAnnotationsUses(s.getInitializations(i));

		for (int i = 0; i < s.getUpdatesCount(); i++)
			count += usesAnnotationsUses(s.getUpdates(i));

		return count;
	}

	@FunctionSpec(name = "uses_annotations_uses", returnType = "int", formalParameters = { "Expression" })
	public static int usesAnnotationsUses(final Expression e) {
		int count = 0;

		for (int i = 0; i < e.getVariableDeclsCount(); i++)
			count += usesAnnotationsUses(e.getVariableDecls(i));

		for (int i = 0; i < e.getExpressionsCount(); i++)
			count += usesAnnotationsUses(e.getExpressions(i));

		for (int i = 0; i < e.getMethodArgsCount(); i++)
			count += usesAnnotationsUses(e.getMethodArgs(i));

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
	public static int usesMultiCatch(final ChangedFile f) {
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
	public static int usesBinaryLit(final ChangedFile f) {
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
	public static int usesUnderscoreLit(final ChangedFile f) {
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
	public static int usesDiamond(final ChangedFile f) {
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

	@FunctionSpec(name = "uses_safe_varargs", returnType = "int", formalParameters = { "CodeRepository" })
	public static int usesSafeVarargs(final CodeRepository r) {
		int count = 0;

		for (int i = 0; i < r.getRevisionsCount(); i++)
			count += usesSafeVarargs(r.getRevisions(i));

		return count;
	}

	@FunctionSpec(name = "uses_safe_varargs", returnType = "int", formalParameters = { "Revision" })
	public static int usesSafeVarargs(final Revision r) {
		int count = 0;

		for (int i = 0; i < r.getFilesCount(); i++)
			count += usesSafeVarargs(r.getFiles(i));

		return count;
	}

	@FunctionSpec(name = "uses_safe_varargs", returnType = "int", formalParameters = { "File" })
	public static int usesSafeVarargs(final ChangedFile f) {
		int count = 0;

		for (int i = 0; i < f.getNamespacesCount(); i++)
			for (int j = 0; j < f.getNamespaces(i).getDeclarationsCount(); j++)
				count += usesSafeVarargs(f.getNamespaces(i).getDeclarations(j));

		return count;
	}

	@FunctionSpec(name = "uses_safe_varargs", returnType = "int", formalParameters = { "Declaration" })
	public static int usesSafeVarargs(final Declaration d) {
		int count = 0;

		for (int i = 0; i < d.getMethodsCount(); i++)
			count += usesSafeVarargs(d.getMethods(i));

		for (int i = 0; i < d.getFieldsCount(); i++)
			count += usesSafeVarargs(d.getFields(i));

		for (int i = 0; i < d.getNestedDeclarationsCount(); i++)
			count += usesSafeVarargs(d.getNestedDeclarations(i));

		return count;
	}

	@FunctionSpec(name = "uses_safe_varargs", returnType = "int", formalParameters = { "Method" })
	public static int usesSafeVarargs(final Method m) {
		int count = 0;

		// @SafeVarargs
		if (BoaModifierIntrinsics.hasAnnotation(m, "SafeVarargs"))
			count++;

		// @SuppressWarnings({"unchecked", "varargs"})
		Modifier mod = BoaModifierIntrinsics.getAnnotation(m, "SuppressWarnings");
		if (mod != null)
			for (int i = 0; i < mod.getAnnotationMembersCount(); i++)
				if (mod.getAnnotationMembers(i).equals("value")) {
					Expression e = mod.getAnnotationValues(i);
					if (e.getKind() == ExpressionKind.ARRAYINIT) {
						boolean foundUnchecked = false, foundVarargs = false;
						for (int j = 0; j < e.getExpressionsCount(); j++)
							if (e.getExpressions(j).getKind() == ExpressionKind.LITERAL) {
								if (e.getExpressions(j).getLiteral().equals("unchecked"))
									foundUnchecked = true;
								if (e.getExpressions(j).getLiteral().equals("varargs"))
									foundVarargs = true;
							}
						// TODO verify this works
						if (foundUnchecked && foundVarargs)
							count++;
					}
					break;
				}

		for (int i = 0; i < m.getArgumentsCount(); i++)
			count += usesSafeVarargs(m.getArguments(i));

		for (int i = 0; i < m.getStatementsCount(); i++)
			count += usesSafeVarargs(m.getStatements(i));

		return count;
	}

	@FunctionSpec(name = "uses_safe_varargs", returnType = "int", formalParameters = { "Variable" })
	public static int usesSafeVarargs(final Variable v) {
		int count = 0;

		if (v.hasInitializer())
			count += usesSafeVarargs(v.getInitializer());

		return count;
	}

	@FunctionSpec(name = "uses_safe_varargs", returnType = "int", formalParameters = { "Statement" })
	public static int usesSafeVarargs(final Statement s) {
		int count = 0;

		if (s.hasCondition())
			count += usesSafeVarargs(s.getCondition());

		if (s.hasVariableDeclaration())
			count += usesSafeVarargs(s.getVariableDeclaration());

		if (s.hasTypeDeclaration())
			count += usesSafeVarargs(s.getTypeDeclaration());

		if (s.hasExpression())
			count += usesSafeVarargs(s.getExpression());

		for (int i = 0; i < s.getStatementsCount(); i++)
			count += usesSafeVarargs(s.getStatements(i));

		for (int i = 0; i < s.getInitializationsCount(); i++)
			count += usesSafeVarargs(s.getInitializations(i));

		for (int i = 0; i < s.getUpdatesCount(); i++)
			count += usesSafeVarargs(s.getUpdates(i));

		return count;
	}

	@FunctionSpec(name = "uses_safe_varargs", returnType = "int", formalParameters = { "Expression" })
	public static int usesSafeVarargs(final Expression e) {
		int count = 0;

		for (int i = 0; i < e.getVariableDeclsCount(); i++)
			count += usesSafeVarargs(e.getVariableDecls(i));

		for (int i = 0; i < e.getExpressionsCount(); i++)
			count += usesSafeVarargs(e.getExpressions(i));

		for (int i = 0; i < e.getMethodArgsCount(); i++)
			count += usesSafeVarargs(e.getMethodArgs(i));

		return count;
	}
}
