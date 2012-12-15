package sizzle.functions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sizzle.types.Ast.Expression.ExpressionKind;
import sizzle.types.Ast.*;
import sizzle.types.Code.*;

/**
 * Boa domain-specific functions for finding Java language features.
 * 
 * @author rdyer
 */
public class BoaJavaFeaturesIntrinsics {
	@FunctionSpec(name = "uses_enhanced_for", returnType = "int", formalParameters = { "CodeRepository" })
	public static long usesEnhancedFor(final CodeRepository r) {
		long count = 0;

		for (int i = 0; i < r.getRevisionsCount(); i++)
			count += usesEnhancedFor(r.getRevisions(i));

		return count;
	}

	@FunctionSpec(name = "uses_enhanced_for", returnType = "int", formalParameters = { "Revision" })
	public static long usesEnhancedFor(final Revision r) {
		long count = 0;

		for (int i = 0; i < r.getFilesCount(); i++)
			count += usesEnhancedFor(BoaAstIntrinsics.getast(r, r.getFiles(i)));

		return count;
	}

	@FunctionSpec(name = "uses_enhanced_for", returnType = "int", formalParameters = { "ASTRoot" })
	public static long usesEnhancedFor(final ASTRoot f) {
		long count = 0;

		for (int i = 0; i < f.getNamespacesCount(); i++)
			for (int j = 0; j < f.getNamespaces(i).getDeclarationsCount(); j++)
				count += usesEnhancedFor(f.getNamespaces(i).getDeclarations(j));

		return count;
	}

	@FunctionSpec(name = "uses_enhanced_for", returnType = "int", formalParameters = { "Declaration" })
	public static long usesEnhancedFor(final Declaration d) {
		long count = 0;

		for (int i = 0; i < d.getMethodsCount(); i++)
			count += usesEnhancedFor(d.getMethods(i));

		for (int i = 0; i < d.getFieldsCount(); i++)
			count += usesEnhancedFor(d.getFields(i));

		for (int i = 0; i < d.getNestedDeclarationsCount(); i++)
			count += usesEnhancedFor(d.getNestedDeclarations(i));

		return count;
	}

	@FunctionSpec(name = "uses_enhanced_for", returnType = "int", formalParameters = { "Method" })
	public static long usesEnhancedFor(final Method m) {
		long count = 0;

		for (int i = 0; i < m.getArgumentsCount(); i++)
			count += usesEnhancedFor(m.getArguments(i));

		for (int i = 0; i < m.getStatementsCount(); i++)
			count += usesEnhancedFor(m.getStatements(i));

		return count;
	}

	@FunctionSpec(name = "uses_enhanced_for", returnType = "int", formalParameters = { "Variable" })
	public static long usesEnhancedFor(final Variable v) {
		long count = 0;

		if (v.hasInitializer())
			count += usesEnhancedFor(v.getInitializer());

		return count;
	}

	@FunctionSpec(name = "uses_enhanced_for", returnType = "int", formalParameters = { "Statement" })
	public static long usesEnhancedFor(final Statement s) {
		long count = 0;

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
	public static long usesEnhancedFor(final Expression e) {
		long count = 0;

		for (int i = 0; i < e.getVariableDeclsCount(); i++)
			count += usesEnhancedFor(e.getVariableDecls(i));

		for (int i = 0; i < e.getExpressionsCount(); i++)
			count += usesEnhancedFor(e.getExpressions(i));

		for (int i = 0; i < e.getMethodArgsCount(); i++)
			count += usesEnhancedFor(e.getMethodArgs(i));

		return count;
	}

	@FunctionSpec(name = "uses_varargs", returnType = "int", formalParameters = { "CodeRepository" })
	public static long usesVarargs(final CodeRepository r) {
		long count = 0;

		for (int i = 0; i < r.getRevisionsCount(); i++)
			count += usesVarargs(r.getRevisions(i));

		return count;
	}

	@FunctionSpec(name = "uses_varargs", returnType = "int", formalParameters = { "Revision" })
	public static long usesVarargs(final Revision r) {
		long count = 0;

		for (int i = 0; i < r.getFilesCount(); i++)
			count += usesVarargs(BoaAstIntrinsics.getast(r, r.getFiles(i)));

		return count;
	}

	@FunctionSpec(name = "uses_varargs", returnType = "int", formalParameters = { "ASTRoot" })
	public static long usesVarargs(final ASTRoot f) {
		long count = 0;

		for (int i = 0; i < f.getNamespacesCount(); i++)
			for (int j = 0; j < f.getNamespaces(i).getDeclarationsCount(); j++)
				count += usesVarargs(f.getNamespaces(i).getDeclarations(j));

		return count;
	}

	@FunctionSpec(name = "uses_varargs", returnType = "int", formalParameters = { "Declaration" })
	public static long usesVarargs(final Declaration d) {
		long count = 0;

		for (int i = 0; i < d.getMethodsCount(); i++)
			count += usesVarargs(d.getMethods(i));

		for (int i = 0; i < d.getNestedDeclarationsCount(); i++)
			count += usesVarargs(d.getNestedDeclarations(i));

		return count;
	}

	@FunctionSpec(name = "uses_varargs", returnType = "int", formalParameters = { "Method" })
	public static long usesVarargs(final Method m) {
		long count = 0;

		if (m.getArgumentsCount() > 0)
			if (m.getArguments(m.getArgumentsCount() - 1).getVariableType().getName().contains("..."))
				count++;

		for (int i = 0; i < m.getStatementsCount(); i++)
			count += usesVarargs(m.getStatements(i));

		return count;
	}

	@FunctionSpec(name = "uses_varargs", returnType = "int", formalParameters = { "Statement" })
	public static long usesVarargs(final Statement s) {
		long count = 0;

		if (s.hasTypeDeclaration())
			count += usesVarargs(s.getTypeDeclaration());

		for (int i = 0; i < s.getStatementsCount(); i++)
			count += usesVarargs(s.getStatements(i));

		return count;
	}

	@FunctionSpec(name = "uses_assert", returnType = "int", formalParameters = { "CodeRepository" })
	public static long usesAssert(final CodeRepository r) {
		long count = 0;

		for (int i = 0; i < r.getRevisionsCount(); i++)
			count += usesAssert(r.getRevisions(i));

		return count;
	}

	@FunctionSpec(name = "uses_assert", returnType = "int", formalParameters = { "Revision" })
	public static long usesAssert(final Revision r) {
		long count = 0;

		for (int i = 0; i < r.getFilesCount(); i++)
			count += usesAssert(BoaAstIntrinsics.getast(r, r.getFiles(i)));

		return count;
	}

	@FunctionSpec(name = "uses_assert", returnType = "int", formalParameters = { "ASTRoot" })
	public static long usesAssert(final ASTRoot f) {
		long count = 0;

		for (int i = 0; i < f.getNamespacesCount(); i++)
			for (int j = 0; j < f.getNamespaces(i).getDeclarationsCount(); j++)
				count += usesAssert(f.getNamespaces(i).getDeclarations(j));

		return count;
	}

	@FunctionSpec(name = "uses_assert", returnType = "int", formalParameters = { "Declaration" })
	public static long usesAssert(final Declaration d) {
		long count = 0;

		for (int i = 0; i < d.getMethodsCount(); i++)
			count += usesAssert(d.getMethods(i));

		for (int i = 0; i < d.getFieldsCount(); i++)
			count += usesAssert(d.getFields(i));

		for (int i = 0; i < d.getNestedDeclarationsCount(); i++)
			count += usesAssert(d.getNestedDeclarations(i));

		return count;
	}

	@FunctionSpec(name = "uses_assert", returnType = "int", formalParameters = { "Method" })
	public static long usesAssert(final Method m) {
		long count = 0;

		for (int i = 0; i < m.getArgumentsCount(); i++)
			count += usesAssert(m.getArguments(i));

		for (int i = 0; i < m.getStatementsCount(); i++)
			count += usesAssert(m.getStatements(i));

		return count;
	}

	@FunctionSpec(name = "uses_assert", returnType = "int", formalParameters = { "Variable" })
	public static long usesAssert(final Variable v) {
		long count = 0;

		if (v.hasInitializer())
			count += usesAssert(v.getInitializer());

		return count;
	}

	@FunctionSpec(name = "uses_assert", returnType = "int", formalParameters = { "Statement" })
	public static long usesAssert(final Statement s) {
		long count = 0;

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
	public static long usesAssert(final Expression e) {
		long count = 0;

		for (int i = 0; i < e.getVariableDeclsCount(); i++)
			count += usesAssert(e.getVariableDecls(i));

		for (int i = 0; i < e.getExpressionsCount(); i++)
			count += usesAssert(e.getExpressions(i));

		for (int i = 0; i < e.getMethodArgsCount(); i++)
			count += usesAssert(e.getMethodArgs(i));

		return count;
	}

	@FunctionSpec(name = "uses_enums", returnType = "int", formalParameters = { "CodeRepository" })
	public static long usesEnums(final CodeRepository r) {
		long count = 0;

		for (int i = 0; i < r.getRevisionsCount(); i++)
			count += usesEnums(r.getRevisions(i));

		return count;
	}

	@FunctionSpec(name = "uses_enums", returnType = "int", formalParameters = { "Revision" })
	public static long usesEnums(final Revision r) {
		long count = 0;

		for (int i = 0; i < r.getFilesCount(); i++)
			count += usesEnums(BoaAstIntrinsics.getast(r, r.getFiles(i)));

		return count;
	}

	@FunctionSpec(name = "uses_enums", returnType = "int", formalParameters = { "ASTRoot" })
	public static long usesEnums(final ASTRoot f) {
		long count = 0;

		for (int i = 0; i < f.getNamespacesCount(); i++)
			for (int j = 0; j < f.getNamespaces(i).getDeclarationsCount(); j++)
				count += usesEnums(f.getNamespaces(i).getDeclarations(j));

		return count;
	}

	@FunctionSpec(name = "uses_enums", returnType = "int", formalParameters = { "Declaration" })
	public static long usesEnums(final Declaration d) {
		long count = 0;

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
	public static long usesEnums(final Method m) {
		long count = 0;

		for (int i = 0; i < m.getArgumentsCount(); i++)
			count += usesEnums(m.getArguments(i));

		for (int i = 0; i < m.getStatementsCount(); i++)
			count += usesEnums(m.getStatements(i));

		return count;
	}

	@FunctionSpec(name = "uses_enums", returnType = "int", formalParameters = { "Variable" })
	public static long usesEnums(final Variable v) {
		long count = 0;

		if (v.hasInitializer())
			count += usesEnums(v.getInitializer());

		return count;
	}

	@FunctionSpec(name = "uses_enums", returnType = "int", formalParameters = { "Statement" })
	public static long usesEnums(final Statement s) {
		long count = 0;

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
	public static long usesEnums(final Expression e) {
		long count = 0;

		for (int i = 0; i < e.getVariableDeclsCount(); i++)
			count += usesEnums(e.getVariableDecls(i));

		for (int i = 0; i < e.getExpressionsCount(); i++)
			count += usesEnums(e.getExpressions(i));

		for (int i = 0; i < e.getMethodArgsCount(); i++)
			count += usesEnums(e.getMethodArgs(i));

		return count;
	}

	@FunctionSpec(name = "uses_try_resources", returnType = "int", formalParameters = { "CodeRepository" })
	public static long usesTryResources(final CodeRepository r) {
		long count = 0;

		for (int i = 0; i < r.getRevisionsCount(); i++)
			count += usesTryResources(r.getRevisions(i));

		return count;
	}

	@FunctionSpec(name = "uses_try_resources", returnType = "int", formalParameters = { "Revision" })
	public static long usesTryResources(final Revision r) {
		long count = 0;

		for (int i = 0; i < r.getFilesCount(); i++)
			count += usesTryResources(BoaAstIntrinsics.getast(r, r.getFiles(i)));

		return count;
	}

	@FunctionSpec(name = "uses_try_resources", returnType = "int", formalParameters = { "ASTRoot" })
	public static long usesTryResources(final ASTRoot f) {
		long count = 0;

		for (int i = 0; i < f.getNamespacesCount(); i++)
			for (int j = 0; j < f.getNamespaces(i).getDeclarationsCount(); j++)
				count += usesTryResources(f.getNamespaces(i).getDeclarations(j));

		return count;
	}

	@FunctionSpec(name = "uses_try_resources", returnType = "int", formalParameters = { "Declaration" })
	public static long usesTryResources(final Declaration d) {
		long count = 0;

		for (int i = 0; i < d.getMethodsCount(); i++)
			count += usesTryResources(d.getMethods(i));

		for (int i = 0; i < d.getFieldsCount(); i++)
			count += usesTryResources(d.getFields(i));

		for (int i = 0; i < d.getNestedDeclarationsCount(); i++)
			count += usesTryResources(d.getNestedDeclarations(i));

		return count;
	}

	@FunctionSpec(name = "uses_try_resources", returnType = "int", formalParameters = { "Method" })
	public static long usesTryResources(final Method m) {
		long count = 0;

		for (int i = 0; i < m.getArgumentsCount(); i++)
			count += usesTryResources(m.getArguments(i));

		for (int i = 0; i < m.getStatementsCount(); i++)
			count += usesTryResources(m.getStatements(i));

		return count;
	}

	@FunctionSpec(name = "uses_try_resources", returnType = "int", formalParameters = { "Variable" })
	public static long usesTryResources(final Variable v) {
		long count = 0;

		if (v.hasInitializer())
			count += usesTryResources(v.getInitializer());

		return count;
	}

	@FunctionSpec(name = "uses_try_resources", returnType = "int", formalParameters = { "Statement" })
	public static long usesTryResources(final Statement s) {
		long count = 0;

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
	public static long usesTryResources(final Expression e) {
		long count = 0;

		for (int i = 0; i < e.getVariableDeclsCount(); i++)
			count += usesTryResources(e.getVariableDecls(i));

		for (int i = 0; i < e.getExpressionsCount(); i++)
			count += usesTryResources(e.getExpressions(i));

		for (int i = 0; i < e.getMethodArgsCount(); i++)
			count += usesTryResources(e.getMethodArgs(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_type", returnType = "int", formalParameters = { "CodeRepository" })
	public static long usesGenericsDefineType(final CodeRepository r) {
		long count = 0;

		for (int i = 0; i < r.getRevisionsCount(); i++)
			count += usesGenericsDefineType(r.getRevisions(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_type", returnType = "int", formalParameters = { "Revision" })
	public static long usesGenericsDefineType(final Revision r) {
		long count = 0;

		for (int i = 0; i < r.getFilesCount(); i++)
			count += usesGenericsDefineType(BoaAstIntrinsics.getast(r, r.getFiles(i)));

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_type", returnType = "int", formalParameters = { "ASTRoot" })
	public static long usesGenericsDefineType(final ASTRoot f) {
		long count = 0;

		for (int i = 0; i < f.getNamespacesCount(); i++)
			for (int j = 0; j < f.getNamespaces(i).getDeclarationsCount(); j++)
				count += usesGenericsDefineType(f.getNamespaces(i).getDeclarations(j));

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_type", returnType = "int", formalParameters = { "Declaration" })
	public static long usesGenericsDefineType(final Declaration d) {
		long count = 0;

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
	public static long usesGenericsDefineType(final Method m) {
		long count = 0;

		for (int i = 0; i < m.getArgumentsCount(); i++)
			count += usesGenericsDefineType(m.getArguments(i));

		for (int i = 0; i < m.getStatementsCount(); i++)
			count += usesGenericsDefineType(m.getStatements(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_type", returnType = "int", formalParameters = { "Variable" })
	public static long usesGenericsDefineType(final Variable v) {
		long count = 0;

		if (v.hasInitializer())
			count += usesGenericsDefineType(v.getInitializer());

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_type", returnType = "int", formalParameters = { "Statement" })
	public static long usesGenericsDefineType(final Statement s) {
		long count = 0;

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
	public static long usesGenericsDefineType(final Expression e) {
		long count = 0;

		for (int i = 0; i < e.getVariableDeclsCount(); i++)
			count += usesGenericsDefineType(e.getVariableDecls(i));

		for (int i = 0; i < e.getExpressionsCount(); i++)
			count += usesGenericsDefineType(e.getExpressions(i));

		for (int i = 0; i < e.getMethodArgsCount(); i++)
			count += usesGenericsDefineType(e.getMethodArgs(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_method", returnType = "int", formalParameters = { "CodeRepository" })
	public static long usesGenericsDefineMethod(final CodeRepository r) {
		long count = 0;

		for (int i = 0; i < r.getRevisionsCount(); i++)
			count += usesGenericsDefineMethod(r.getRevisions(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_method", returnType = "int", formalParameters = { "Revision" })
	public static long usesGenericsDefineMethod(final Revision r) {
		long count = 0;

		for (int i = 0; i < r.getFilesCount(); i++)
			count += usesGenericsDefineMethod(BoaAstIntrinsics.getast(r, r.getFiles(i)));

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_method", returnType = "int", formalParameters = { "ASTRoot" })
	public static long usesGenericsDefineMethod(final ASTRoot f) {
		long count = 0;

		for (int i = 0; i < f.getNamespacesCount(); i++)
			for (int j = 0; j < f.getNamespaces(i).getDeclarationsCount(); j++)
				count += usesGenericsDefineMethod(f.getNamespaces(i).getDeclarations(j));

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_method", returnType = "int", formalParameters = { "Declaration" })
	public static long usesGenericsDefineMethod(final Declaration d) {
		long count = 0;

		for (int i = 0; i < d.getMethodsCount(); i++)
			count += usesGenericsDefineMethod(d.getMethods(i));

		for (int i = 0; i < d.getFieldsCount(); i++)
			count += usesGenericsDefineMethod(d.getFields(i));

		for (int i = 0; i < d.getNestedDeclarationsCount(); i++)
			count += usesGenericsDefineMethod(d.getNestedDeclarations(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_method", returnType = "int", formalParameters = { "Method" })
	public static long usesGenericsDefineMethod(final Method m) {
		long count = 0;

		if (m.getGenericParametersCount() > 0)
			count++;

		for (int i = 0; i < m.getArgumentsCount(); i++)
			count += usesGenericsDefineMethod(m.getArguments(i));

		for (int i = 0; i < m.getStatementsCount(); i++)
			count += usesGenericsDefineMethod(m.getStatements(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_method", returnType = "int", formalParameters = { "Variable" })
	public static long usesGenericsDefineMethod(final Variable v) {
		long count = 0;

		if (v.hasInitializer())
			count += usesGenericsDefineMethod(v.getInitializer());

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_method", returnType = "int", formalParameters = { "Statement" })
	public static long usesGenericsDefineMethod(final Statement s) {
		long count = 0;

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
	public static long usesGenericsDefineMethod(final Expression e) {
		long count = 0;

		for (int i = 0; i < e.getVariableDeclsCount(); i++)
			count += usesGenericsDefineMethod(e.getVariableDecls(i));

		for (int i = 0; i < e.getExpressionsCount(); i++)
			count += usesGenericsDefineMethod(e.getExpressions(i));

		for (int i = 0; i < e.getMethodArgsCount(); i++)
			count += usesGenericsDefineMethod(e.getMethodArgs(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_field", returnType = "int", formalParameters = { "CodeRepository" })
	public static long usesGenericsDefineField(final CodeRepository r) {
		long count = 0;

		for (int i = 0; i < r.getRevisionsCount(); i++)
			count += usesGenericsDefineField(r.getRevisions(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_field", returnType = "int", formalParameters = { "Revision" })
	public static long usesGenericsDefineField(final Revision r) {
		long count = 0;

		for (int i = 0; i < r.getFilesCount(); i++)
			count += usesGenericsDefineField(BoaAstIntrinsics.getast(r, r.getFiles(i)));

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_field", returnType = "int", formalParameters = { "ASTRoot" })
	public static long usesGenericsDefineField(final ASTRoot f) {
		long count = 0;

		for (int i = 0; i < f.getNamespacesCount(); i++)
			for (int j = 0; j < f.getNamespaces(i).getDeclarationsCount(); j++)
				count += usesGenericsDefineField(f.getNamespaces(i).getDeclarations(j));

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_field", returnType = "int", formalParameters = { "Declaration" })
	public static long usesGenericsDefineField(final Declaration d) {
		long count = 0;

		for (int i = 0; i < d.getMethodsCount(); i++)
			count += usesGenericsDefineField(d.getMethods(i));

		for (int i = 0; i < d.getFieldsCount(); i++)
			count += usesGenericsDefineField(d.getFields(i));

		for (int i = 0; i < d.getNestedDeclarationsCount(); i++)
			count += usesGenericsDefineField(d.getNestedDeclarations(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_field", returnType = "int", formalParameters = { "Method" })
	public static long usesGenericsDefineField(final Method m) {
		long count = 0;

		for (int i = 0; i < m.getArgumentsCount(); i++)
			count += usesGenericsDefineField(m.getArguments(i));

		for (int i = 0; i < m.getStatementsCount(); i++)
			count += usesGenericsDefineField(m.getStatements(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_field", returnType = "int", formalParameters = { "Variable" })
	public static long usesGenericsDefineField(final Variable v) {
		long count = 0;

		count += usesGenericsDefineField(v.getVariableType());

		if (v.hasInitializer())
			count += usesGenericsDefineField(v.getInitializer());

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_field", returnType = "int", formalParameters = { "Statement" })
	public static long usesGenericsDefineField(final Statement s) {
		long count = 0;

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
	public static long usesGenericsDefineField(final Expression e) {
		long count = 0;

		for (int i = 0; i < e.getVariableDeclsCount(); i++)
			count += usesGenericsDefineField(e.getVariableDecls(i));

		for (int i = 0; i < e.getExpressionsCount(); i++)
			count += usesGenericsDefineField(e.getExpressions(i));

		for (int i = 0; i < e.getMethodArgsCount(); i++)
			count += usesGenericsDefineField(e.getMethodArgs(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_define_field", returnType = "int", formalParameters = { "Type" })
	public static long usesGenericsDefineField(final Type t) {
		long count = 0;

		if (t.getName().contains("<"))
			count++;

		return count;
	}

	@FunctionSpec(name = "uses_generics_wildcard_super", returnType = "int", formalParameters = { "CodeRepository" })
	public static long usesGenericsWildcardSuper(final CodeRepository r) {
		long count = 0;

		for (int i = 0; i < r.getRevisionsCount(); i++)
			count += usesGenericsWildcardSuper(r.getRevisions(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_wildcard_super", returnType = "int", formalParameters = { "Revision" })
	public static long usesGenericsWildcardSuper(final Revision r) {
		long count = 0;

		for (int i = 0; i < r.getFilesCount(); i++)
			count += usesGenericsWildcardSuper(BoaAstIntrinsics.getast(r, r.getFiles(i)));

		return count;
	}

	@FunctionSpec(name = "uses_generics_wildcard_super", returnType = "int", formalParameters = { "ASTRoot" })
	public static long usesGenericsWildcardSuper(final ASTRoot f) {
		long count = 0;

		for (int i = 0; i < f.getNamespacesCount(); i++)
			for (int j = 0; j < f.getNamespaces(i).getDeclarationsCount(); j++)
				count += usesGenericsWildcardSuper(f.getNamespaces(i).getDeclarations(j));

		return count;
	}

	@FunctionSpec(name = "uses_generics_wildcard_super", returnType = "int", formalParameters = { "Declaration" })
	public static long usesGenericsWildcardSuper(final Declaration d) {
		long count = 0;

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
	public static long usesGenericsWildcardSuper(final Method m) {
		long count = 0;

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
	public static long usesGenericsWildcardSuper(final Variable v) {
		long count = 0;

		count += usesGenericsWildcardSuper(v.getVariableType());

		if (v.hasInitializer())
			count += usesGenericsWildcardSuper(v.getInitializer());

		return count;
	}

	@FunctionSpec(name = "uses_generics_wildcard_super", returnType = "int", formalParameters = { "Statement" })
	public static long usesGenericsWildcardSuper(final Statement s) {
		long count = 0;

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
	public static long usesGenericsWildcardSuper(final Expression e) {
		long count = 0;

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

	private static Matcher wildcardSuperMatcher = Pattern.compile("\\?\\s*super\\s+.+").matcher("");

	@FunctionSpec(name = "uses_generics_wildcard_super", returnType = "int", formalParameters = { "Type" })
	public static long usesGenericsWildcardSuper(final Type t) {
		long count = 0;

		if (wildcardSuperMatcher.reset(t.getName()).find())
			count++;

		return count;
	}

	@FunctionSpec(name = "uses_generics_wildcard_extends", returnType = "int", formalParameters = { "CodeRepository" })
	public static long usesGenericsWildcardExtends(final CodeRepository r) {
		long count = 0;

		for (int i = 0; i < r.getRevisionsCount(); i++)
			count += usesGenericsWildcardExtends(r.getRevisions(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_wildcard_extends", returnType = "int", formalParameters = { "Revision" })
	public static long usesGenericsWildcardExtends(final Revision r) {
		long count = 0;

		for (int i = 0; i < r.getFilesCount(); i++)
			count += usesGenericsWildcardExtends(BoaAstIntrinsics.getast(r, r.getFiles(i)));

		return count;
	}

	@FunctionSpec(name = "uses_generics_wildcard_extends", returnType = "int", formalParameters = { "ASTRoot" })
	public static long usesGenericsWildcardExtends(final ASTRoot f) {
		long count = 0;

		for (int i = 0; i < f.getNamespacesCount(); i++)
			for (int j = 0; j < f.getNamespaces(i).getDeclarationsCount(); j++)
				count += usesGenericsWildcardExtends(f.getNamespaces(i).getDeclarations(j));

		return count;
	}

	@FunctionSpec(name = "uses_generics_wildcard_extends", returnType = "int", formalParameters = { "Declaration" })
	public static long usesGenericsWildcardExtends(final Declaration d) {
		long count = 0;

		for (int i = 0; i < d.getGenericParametersCount(); i++)
			count += usesGenericsWildcardExtends(d.getGenericParameters(i));

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
	public static long usesGenericsWildcardExtends(final Method m) {
		long count = 0;

		for (int i = 0; i < m.getGenericParametersCount(); i++)
			count += usesGenericsWildcardExtends(m.getGenericParameters(i));

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
	public static long usesGenericsWildcardExtends(final Variable v) {
		long count = 0;

		count += usesGenericsWildcardExtends(v.getVariableType());

		if (v.hasInitializer())
			count += usesGenericsWildcardExtends(v.getInitializer());

		return count;
	}

	@FunctionSpec(name = "uses_generics_wildcard_extends", returnType = "int", formalParameters = { "Statement" })
	public static long usesGenericsWildcardExtends(final Statement s) {
		long count = 0;

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
	public static long usesGenericsWildcardExtends(final Expression e) {
		long count = 0;

		for (int i = 0; i < e.getGenericParametersCount(); i++)
			count += usesGenericsWildcardExtends(e.getGenericParameters(i));

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

	private static Matcher wildcardExtendsMatcher = Pattern.compile("\\?\\s*extends\\s+.+").matcher("");

	@FunctionSpec(name = "uses_generics_wildcard_extends", returnType = "int", formalParameters = { "Type" })
	public static long usesGenericsWildcardExtends(final Type t) {
		long count = 0;

		if (wildcardExtendsMatcher.reset(t.getName()).find())
			count++;

		return count;
	}

	@FunctionSpec(name = "uses_generics_wildcard", returnType = "int", formalParameters = { "CodeRepository" })
	public static long usesGenericsWildcard(final CodeRepository r) {
		long count = 0;

		for (int i = 0; i < r.getRevisionsCount(); i++)
			count += usesGenericsWildcard(r.getRevisions(i));

		return count;
	}

	@FunctionSpec(name = "uses_generics_wildcard", returnType = "int", formalParameters = { "Revision" })
	public static long usesGenericsWildcard(final Revision r) {
		long count = 0;

		for (int i = 0; i < r.getFilesCount(); i++)
			count += usesGenericsWildcard(BoaAstIntrinsics.getast(r, r.getFiles(i)));

		return count;
	}

	@FunctionSpec(name = "uses_generics_wildcard", returnType = "int", formalParameters = { "ASTRoot" })
	public static long usesGenericsWildcard(final ASTRoot f) {
		long count = 0;

		for (int i = 0; i < f.getNamespacesCount(); i++)
			for (int j = 0; j < f.getNamespaces(i).getDeclarationsCount(); j++)
				count += usesGenericsWildcard(f.getNamespaces(i).getDeclarations(j));

		return count;
	}

	@FunctionSpec(name = "uses_generics_wildcard", returnType = "int", formalParameters = { "Declaration" })
	public static long usesGenericsWildcard(final Declaration d) {
		long count = 0;

		for (int i = 0; i < d.getGenericParametersCount(); i++)
			count += usesGenericsWildcard(d.getGenericParameters(i));

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
	public static long usesGenericsWildcard(final Method m) {
		long count = 0;

		for (int i = 0; i < m.getGenericParametersCount(); i++)
			count += usesGenericsWildcard(m.getGenericParameters(i));

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
	public static long usesGenericsWildcard(final Variable v) {
		long count = 0;

		count += usesGenericsWildcard(v.getVariableType());

		if (v.hasInitializer())
			count += usesGenericsWildcard(v.getInitializer());

		return count;
	}

	@FunctionSpec(name = "uses_generics_wildcard", returnType = "int", formalParameters = { "Statement" })
	public static long usesGenericsWildcard(final Statement s) {
		long count = 0;

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
	public static long usesGenericsWildcard(final Expression e) {
		long count = 0;

		for (int i = 0; i < e.getGenericParametersCount(); i++)
			count += usesGenericsWildcard(e.getGenericParameters(i));

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

	@FunctionSpec(name = "uses_generics_wildcard", returnType = "int", formalParameters = { "Type" })
	public static long usesGenericsWildcard(final Type t) {
		long count = 0;

		if (t.getName().contains("?")
				&& !wildcardExtendsMatcher.reset(t.getName()).find()
				&& !wildcardSuperMatcher.reset(t.getName()).find())
			count++;

		return count;
	}

	@FunctionSpec(name = "uses_annotations_define", returnType = "int", formalParameters = { "CodeRepository" })
	public static long usesAnnotationsDefine(final CodeRepository r) {
		long count = 0;

		for (int i = 0; i < r.getRevisionsCount(); i++)
			count += usesAnnotationsDefine(r.getRevisions(i));

		return count;
	}

	@FunctionSpec(name = "uses_annotations_define", returnType = "int", formalParameters = { "Revision" })
	public static long usesAnnotationsDefine(final Revision r) {
		long count = 0;

		for (int i = 0; i < r.getFilesCount(); i++)
			count += usesAnnotationsDefine(BoaAstIntrinsics.getast(r, r.getFiles(i)));

		return count;
	}

	@FunctionSpec(name = "uses_annotations_define", returnType = "int", formalParameters = { "ASTRoot" })
	public static long usesAnnotationsDefine(final ASTRoot f) {
		long count = 0;

		for (int i = 0; i < f.getNamespacesCount(); i++)
			for (int j = 0; j < f.getNamespaces(i).getDeclarationsCount(); j++)
				count += usesAnnotationsDefine(f.getNamespaces(i).getDeclarations(j));

		return count;
	}

	@FunctionSpec(name = "uses_annotations_define", returnType = "int", formalParameters = { "Declaration" })
	public static long usesAnnotationsDefine(final Declaration d) {
		long count = 0;

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
	public static long usesAnnotationsDefine(final Method m) {
		long count = 0;

		for (int i = 0; i < m.getArgumentsCount(); i++)
			count += usesAnnotationsDefine(m.getArguments(i));

		for (int i = 0; i < m.getStatementsCount(); i++)
			count += usesAnnotationsDefine(m.getStatements(i));

		return count;
	}

	@FunctionSpec(name = "uses_annotations_define", returnType = "int", formalParameters = { "Variable" })
	public static long usesAnnotationsDefine(final Variable v) {
		long count = 0;

		if (v.hasInitializer())
			count += usesAnnotationsDefine(v.getInitializer());

		return count;
	}

	@FunctionSpec(name = "uses_annotations_define", returnType = "int", formalParameters = { "Statement" })
	public static long usesAnnotationsDefine(final Statement s) {
		long count = 0;

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
	public static long usesAnnotationsDefine(final Expression e) {
		long count = 0;

		for (int i = 0; i < e.getVariableDeclsCount(); i++)
			count += usesAnnotationsDefine(e.getVariableDecls(i));

		for (int i = 0; i < e.getExpressionsCount(); i++)
			count += usesAnnotationsDefine(e.getExpressions(i));

		for (int i = 0; i < e.getMethodArgsCount(); i++)
			count += usesAnnotationsDefine(e.getMethodArgs(i));

		return count;
	}

	@FunctionSpec(name = "uses_annotations_uses", returnType = "int", formalParameters = { "CodeRepository" })
	public static long usesAnnotationsUses(final CodeRepository r) {
		long count = 0;

		for (int i = 0; i < r.getRevisionsCount(); i++)
			count += usesAnnotationsUses(r.getRevisions(i));

		return count;
	}

	@FunctionSpec(name = "uses_annotations_uses", returnType = "int", formalParameters = { "Revision" })
	public static long usesAnnotationsUses(final Revision r) {
		long count = 0;

		for (int i = 0; i < r.getFilesCount(); i++)
			count += usesAnnotationsUses(BoaAstIntrinsics.getast(r, r.getFiles(i)));

		return count;
	}

	@FunctionSpec(name = "uses_annotations_uses", returnType = "int", formalParameters = { "ASTRoot" })
	public static long usesAnnotationsUses(final ASTRoot f) {
		long count = 0;

		for (int i = 0; i < f.getNamespacesCount(); i++)
			for (int j = 0; j < f.getNamespaces(i).getDeclarationsCount(); j++) {
				if (BoaModifierIntrinsics.hasAnnotation(f.getNamespaces(i)))
					count++;

				count += usesAnnotationsUses(f.getNamespaces(i).getDeclarations(j));
			}

		return count;
	}

	@FunctionSpec(name = "uses_annotations_uses", returnType = "int", formalParameters = { "Declaration" })
	public static long usesAnnotationsUses(final Declaration d) {
		long count = 0;

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
	public static long usesAnnotationsUses(final Method m) {
		long count = 0;

		if (BoaModifierIntrinsics.hasAnnotation(m))
			count++;

		for (int i = 0; i < m.getArgumentsCount(); i++)
			count += usesAnnotationsUses(m.getArguments(i));

		for (int i = 0; i < m.getStatementsCount(); i++)
			count += usesAnnotationsUses(m.getStatements(i));

		return count;
	}

	@FunctionSpec(name = "uses_annotations_uses", returnType = "int", formalParameters = { "Variable" })
	public static long usesAnnotationsUses(final Variable v) {
		long count = 0;

		if (BoaModifierIntrinsics.hasAnnotation(v))
			count++;

		if (v.hasInitializer())
			count += usesAnnotationsUses(v.getInitializer());

		return count;
	}

	@FunctionSpec(name = "uses_annotations_uses", returnType = "int", formalParameters = { "Statement" })
	public static long usesAnnotationsUses(final Statement s) {
		long count = 0;

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
	public static long usesAnnotationsUses(final Expression e) {
		long count = 0;

		for (int i = 0; i < e.getVariableDeclsCount(); i++)
			count += usesAnnotationsUses(e.getVariableDecls(i));

		for (int i = 0; i < e.getExpressionsCount(); i++)
			count += usesAnnotationsUses(e.getExpressions(i));

		for (int i = 0; i < e.getMethodArgsCount(); i++)
			count += usesAnnotationsUses(e.getMethodArgs(i));

		return count;
	}

	@FunctionSpec(name = "uses_multi_catch", returnType = "int", formalParameters = { "CodeRepository" })
	public static long usesMultiCatch(final CodeRepository r) {
		long count = 0;

		for (int i = 0; i < r.getRevisionsCount(); i++)
			count += usesMultiCatch(r.getRevisions(i));

		return count;
	}

	@FunctionSpec(name = "uses_multi_catch", returnType = "int", formalParameters = { "Revision" })
	public static long usesMultiCatch(final Revision r) {
		long count = 0;

		for (int i = 0; i < r.getFilesCount(); i++)
			count += usesMultiCatch(BoaAstIntrinsics.getast(r, r.getFiles(i)));

		return count;
	}

	@FunctionSpec(name = "uses_multi_catch", returnType = "int", formalParameters = { "ASTRoot" })
	public static long usesMultiCatch(final ASTRoot f) {
		long count = 0;

		for (int i = 0; i < f.getNamespacesCount(); i++)
			for (int j = 0; j < f.getNamespaces(i).getDeclarationsCount(); j++)
				count += usesMultiCatch(f.getNamespaces(i).getDeclarations(j));

		return count;
	}

	@FunctionSpec(name = "uses_multi_catch", returnType = "int", formalParameters = { "Declaration" })
	public static long usesMultiCatch(final Declaration d) {
		long count = 0;

		for (int i = 0; i < d.getMethodsCount(); i++)
			count += usesMultiCatch(d.getMethods(i));

		for (int i = 0; i < d.getFieldsCount(); i++)
			count += usesMultiCatch(d.getFields(i));

		for (int i = 0; i < d.getNestedDeclarationsCount(); i++)
			count += usesMultiCatch(d.getNestedDeclarations(i));

		return count;
	}

	@FunctionSpec(name = "uses_multi_catch", returnType = "int", formalParameters = { "Method" })
	public static long usesMultiCatch(final Method m) {
		long count = 0;

		for (int i = 0; i < m.getArgumentsCount(); i++)
			count += usesMultiCatch(m.getArguments(i));

		for (int i = 0; i < m.getStatementsCount(); i++)
			count += usesMultiCatch(m.getStatements(i));

		return count;
	}

	@FunctionSpec(name = "uses_multi_catch", returnType = "int", formalParameters = { "Variable" })
	public static long usesMultiCatch(final Variable v) {
		long count = 0;

		if (v.hasInitializer())
			count += usesMultiCatch(v.getInitializer());

		return count;
	}

	@FunctionSpec(name = "uses_multi_catch", returnType = "int", formalParameters = { "Statement" })
	public static long usesMultiCatch(final Statement s) {
		long count = 0;

		if (s.getKind() == Statement.StatementKind.CATCH && s.getVariableDeclaration().getVariableType().getName().contains("|"))
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
	public static long usesMultiCatch(final Expression e) {
		long count = 0;

		for (int i = 0; i < e.getVariableDeclsCount(); i++)
			count += usesMultiCatch(e.getVariableDecls(i));

		for (int i = 0; i < e.getExpressionsCount(); i++)
			count += usesMultiCatch(e.getExpressions(i));

		for (int i = 0; i < e.getMethodArgsCount(); i++)
			count += usesMultiCatch(e.getMethodArgs(i));

		return count;
	}

	@FunctionSpec(name = "uses_binary_lit", returnType = "int", formalParameters = { "CodeRepository" })
	public static long usesBinaryLit(final CodeRepository r) {
		long count = 0;

		for (int i = 0; i < r.getRevisionsCount(); i++)
			count += usesBinaryLit(r.getRevisions(i));

		return count;
	}

	@FunctionSpec(name = "uses_binary_lit", returnType = "int", formalParameters = { "Revision" })
	public static long usesBinaryLit(final Revision r) {
		long count = 0;

		for (int i = 0; i < r.getFilesCount(); i++)
			count += usesBinaryLit(BoaAstIntrinsics.getast(r, r.getFiles(i)));

		return count;
	}

	@FunctionSpec(name = "uses_binary_lit", returnType = "int", formalParameters = { "ASTRoot" })
	public static long usesBinaryLit(final ASTRoot f) {
		long count = 0;

		for (int i = 0; i < f.getNamespacesCount(); i++)
			for (int j = 0; j < f.getNamespaces(i).getDeclarationsCount(); j++)
				count += usesBinaryLit(f.getNamespaces(i).getDeclarations(j));

		return count;
	}

	@FunctionSpec(name = "uses_binary_lit", returnType = "int", formalParameters = { "Declaration" })
	public static long usesBinaryLit(final Declaration d) {
		long count = 0;

		for (int i = 0; i < d.getMethodsCount(); i++)
			count += usesBinaryLit(d.getMethods(i));

		for (int i = 0; i < d.getFieldsCount(); i++)
			count += usesBinaryLit(d.getFields(i));

		for (int i = 0; i < d.getNestedDeclarationsCount(); i++)
			count += usesBinaryLit(d.getNestedDeclarations(i));

		return count;
	}

	@FunctionSpec(name = "uses_binary_lit", returnType = "int", formalParameters = { "Method" })
	public static long usesBinaryLit(final Method m) {
		long count = 0;

		for (int i = 0; i < m.getArgumentsCount(); i++)
			count += usesBinaryLit(m.getArguments(i));

		for (int i = 0; i < m.getStatementsCount(); i++)
			count += usesBinaryLit(m.getStatements(i));

		return count;
	}

	@FunctionSpec(name = "uses_binary_lit", returnType = "int", formalParameters = { "Variable" })
	public static long usesBinaryLit(final Variable v) {
		long count = 0;

		if (v.hasInitializer())
			count += usesBinaryLit(v.getInitializer());

		return count;
	}

	@FunctionSpec(name = "uses_binary_lit", returnType = "int", formalParameters = { "Statement" })
	public static long usesBinaryLit(final Statement s) {
		long count = 0;

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
	public static long usesBinaryLit(final Expression e) {
		long count = 0;

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
	public static long usesUnderscoreLit(final CodeRepository r) {
		long count = 0;

		for (int i = 0; i < r.getRevisionsCount(); i++)
			count += usesUnderscoreLit(r.getRevisions(i));

		return count;
	}

	@FunctionSpec(name = "uses_underscore_lit", returnType = "int", formalParameters = { "Revision" })
	public static long usesUnderscoreLit(final Revision r) {
		long count = 0;

		for (int i = 0; i < r.getFilesCount(); i++)
			count += usesUnderscoreLit(BoaAstIntrinsics.getast(r, r.getFiles(i)));

		return count;
	}

	@FunctionSpec(name = "uses_underscore_lit", returnType = "int", formalParameters = { "ASTRoot" })
	public static long usesUnderscoreLit(final ASTRoot f) {
		long count = 0;

		for (int i = 0; i < f.getNamespacesCount(); i++)
			for (int j = 0; j < f.getNamespaces(i).getDeclarationsCount(); j++)
				count += usesUnderscoreLit(f.getNamespaces(i).getDeclarations(j));

		return count;
	}

	@FunctionSpec(name = "uses_underscore_lit", returnType = "int", formalParameters = { "Declaration" })
	public static long usesUnderscoreLit(final Declaration d) {
		long count = 0;

		for (int i = 0; i < d.getMethodsCount(); i++)
			count += usesUnderscoreLit(d.getMethods(i));

		for (int i = 0; i < d.getFieldsCount(); i++)
			count += usesUnderscoreLit(d.getFields(i));

		for (int i = 0; i < d.getNestedDeclarationsCount(); i++)
			count += usesUnderscoreLit(d.getNestedDeclarations(i));

		return count;
	}

	@FunctionSpec(name = "uses_underscore_lit", returnType = "int", formalParameters = { "Method" })
	public static long usesUnderscoreLit(final Method m) {
		long count = 0;

		for (int i = 0; i < m.getArgumentsCount(); i++)
			count += usesUnderscoreLit(m.getArguments(i));

		for (int i = 0; i < m.getStatementsCount(); i++)
			count += usesUnderscoreLit(m.getStatements(i));

		return count;
	}

	@FunctionSpec(name = "uses_underscore_lit", returnType = "int", formalParameters = { "Variable" })
	public static long usesUnderscoreLit(final Variable v) {
		long count = 0;

		if (v.hasInitializer())
			count += usesUnderscoreLit(v.getInitializer());

		return count;
	}

	@FunctionSpec(name = "uses_underscore_lit", returnType = "int", formalParameters = { "Statement" })
	public static long usesUnderscoreLit(final Statement s) {
		long count = 0;

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
	public static long usesUnderscoreLit(final Expression e) {
		long count = 0;

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
	public static long usesDiamond(final CodeRepository r) {
		long count = 0;

		for (int i = 0; i < r.getRevisionsCount(); i++)
			count += usesDiamond(r.getRevisions(i));

		return count;
	}

	@FunctionSpec(name = "uses_diamond", returnType = "int", formalParameters = { "Revision" })
	public static long usesDiamond(final Revision r) {
		long count = 0;

		for (int i = 0; i < r.getFilesCount(); i++)
			count += usesDiamond(BoaAstIntrinsics.getast(r, r.getFiles(i)));

		return count;
	}

	@FunctionSpec(name = "uses_diamond", returnType = "int", formalParameters = { "ASTRoot" })
	public static long usesDiamond(final ASTRoot f) {
		long count = 0;

		for (int i = 0; i < f.getNamespacesCount(); i++)
			for (int j = 0; j < f.getNamespaces(i).getDeclarationsCount(); j++)
				count += usesDiamond(f.getNamespaces(i).getDeclarations(j));

		return count;
	}

	@FunctionSpec(name = "uses_diamond", returnType = "int", formalParameters = { "Declaration" })
	public static long usesDiamond(final Declaration d) {
		long count = 0;

		for (int i = 0; i < d.getMethodsCount(); i++)
			count += usesDiamond(d.getMethods(i));

		for (int i = 0; i < d.getFieldsCount(); i++)
			count += usesDiamond(d.getFields(i));

		for (int i = 0; i < d.getNestedDeclarationsCount(); i++)
			count += usesDiamond(d.getNestedDeclarations(i));

		return count;
	}

	@FunctionSpec(name = "uses_diamond", returnType = "int", formalParameters = { "Method" })
	public static long usesDiamond(final Method m) {
		long count = 0;

		for (int i = 0; i < m.getStatementsCount(); i++)
			count += usesDiamond(m.getStatements(i));

		return count;
	}

	@FunctionSpec(name = "uses_diamond", returnType = "int", formalParameters = { "Variable" })
	public static long usesDiamond(final Variable v) {
		long count = 0;

		if (v.hasInitializer())
			count += usesDiamond(v.getInitializer());

		return count;
	}

	@FunctionSpec(name = "uses_diamond", returnType = "int", formalParameters = { "Statement" })
	public static long usesDiamond(final Statement s) {
		long count = 0;

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
	public static long usesDiamond(final Expression e) {
		long count = 0;

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
	public static long usesDiamond(final Type t) {
		long count = 0;

		if (t.getName().contains("<>"))
			count++;

		return count;
	}

	@FunctionSpec(name = "uses_safe_varargs", returnType = "int", formalParameters = { "CodeRepository" })
	public static long usesSafeVarargs(final CodeRepository r) {
		long count = 0;

		for (int i = 0; i < r.getRevisionsCount(); i++)
			count += usesSafeVarargs(r.getRevisions(i));

		return count;
	}

	@FunctionSpec(name = "uses_safe_varargs", returnType = "int", formalParameters = { "Revision" })
	public static long usesSafeVarargs(final Revision r) {
		long count = 0;

		for (int i = 0; i < r.getFilesCount(); i++)
			count += usesSafeVarargs(BoaAstIntrinsics.getast(r, r.getFiles(i)));

		return count;
	}

	@FunctionSpec(name = "uses_safe_varargs", returnType = "int", formalParameters = { "ASTRoot" })
	public static long usesSafeVarargs(final ASTRoot f) {
		long count = 0;

		for (int i = 0; i < f.getNamespacesCount(); i++)
			for (int j = 0; j < f.getNamespaces(i).getDeclarationsCount(); j++)
				count += usesSafeVarargs(f.getNamespaces(i).getDeclarations(j));

		return count;
	}

	@FunctionSpec(name = "uses_safe_varargs", returnType = "int", formalParameters = { "Declaration" })
	public static long usesSafeVarargs(final Declaration d) {
		long count = 0;

		for (int i = 0; i < d.getMethodsCount(); i++)
			count += usesSafeVarargs(d.getMethods(i));

		for (int i = 0; i < d.getFieldsCount(); i++)
			count += usesSafeVarargs(d.getFields(i));

		for (int i = 0; i < d.getNestedDeclarationsCount(); i++)
			count += usesSafeVarargs(d.getNestedDeclarations(i));

		return count;
	}

	@FunctionSpec(name = "uses_safe_varargs", returnType = "int", formalParameters = { "Method" })
	public static long usesSafeVarargs(final Method m) {
		long count = 0;

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
	public static long usesSafeVarargs(final Variable v) {
		long count = 0;

		if (v.hasInitializer())
			count += usesSafeVarargs(v.getInitializer());

		return count;
	}

	@FunctionSpec(name = "uses_safe_varargs", returnType = "int", formalParameters = { "Statement" })
	public static long usesSafeVarargs(final Statement s) {
		long count = 0;

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
	public static long usesSafeVarargs(final Expression e) {
		long count = 0;

		for (int i = 0; i < e.getVariableDeclsCount(); i++)
			count += usesSafeVarargs(e.getVariableDecls(i));

		for (int i = 0; i < e.getExpressionsCount(); i++)
			count += usesSafeVarargs(e.getExpressions(i));

		for (int i = 0; i < e.getMethodArgsCount(); i++)
			count += usesSafeVarargs(e.getMethodArgs(i));

		return count;
	}
}
