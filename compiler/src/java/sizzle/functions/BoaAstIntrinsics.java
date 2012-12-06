package sizzle.functions;

import java.io.IOException;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import org.apache.hadoop.mapreduce.Mapper.Context;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.InvalidProtocolBufferException;

import sizzle.types.Ast.ASTRoot;
import sizzle.types.Ast.Comment;
import sizzle.types.Ast.Declaration;
import sizzle.types.Ast.Expression;
import sizzle.types.Ast.Method;
import sizzle.types.Ast.Modifier;
import sizzle.types.Ast.Namespace;
import sizzle.types.Ast.Statement;
import sizzle.types.Ast.Type;
import sizzle.types.Ast.Variable;
import sizzle.types.Code.CodeRepository;
import sizzle.types.Code.Revision;
import sizzle.types.Diff.ChangedFile;

/**
 * Boa functions for working with ASTs.
 * 
 * @author rdyer
 */
public class BoaAstIntrinsics {
	@SuppressWarnings("rawtypes")
	private static Context context;
	private static HTable table;

	private final static byte[] family = Bytes.toBytes("code");

	public static enum HBASE_COUNTER {
		GETS,
		GETS_EMPTY,
		GETS_BADPROTOBUF,
		GETS_SUCCEED,
		GETS_FAILED
	};

	/**
	 * Given a Revision and ChangedFile, return the AST for that file at that revision.
	 * 
	 * @param rev the Revision to get a snapshot of the AST from
	 * @param f the ChangedFile to get a snapshot of the AST for
	 * @return the AST, or an empty AST on any sort of error
	 */
	@SuppressWarnings("unchecked")
	@FunctionSpec(name = "getast", returnType = "ASTRoot", formalParameters = { "Revision", "ChangedFile" })
	public static ASTRoot getast(final Revision rev, final ChangedFile f) {
		context.getCounter(HBASE_COUNTER.GETS).increment(1);
		final ASTRoot.Builder b = ASTRoot.newBuilder();

		final byte[] rowName = Bytes.toBytes(rev.getKey());
		final Get get = new Get(rowName);
		final byte[] colName = Bytes.toBytes(f.getName());
		get.addColumn(family, colName);

		// retry on errors
		// sometimes HBase has connection problems which resolve fairly quick
		for (int i = 0; i < 10; i++)
			try {
				if (table == null)
					table = new HTable(HBaseConfiguration.create(), context.getConfiguration().get("boa.hbase.table"));

				Result res = table.get(get);
				if (!res.containsColumn(family, colName) || res.isEmpty())
					throw new RuntimeException("row '" + rowName + "' cell '" + colName + "' not found");
				final byte[] val = res.value();
	
				CodedInputStream _stream = CodedInputStream.newInstance(val, 0, val.length);
				// defaults to 64, really big ASTs require more
				_stream.setRecursionLimit(Integer.MAX_VALUE);
				b.mergeFrom(_stream);
				context.getCounter(HBASE_COUNTER.GETS_SUCCEED).increment(1);
				return b.build();
			} catch (final InvalidProtocolBufferException e) {
				e.printStackTrace();
				context.getCounter(HBASE_COUNTER.GETS_BADPROTOBUF).increment(1);
				break;
			} catch (final IOException e) {
				close();
				try { Thread.sleep(500); } catch (InterruptedException e1) { }
			} catch (final RuntimeException e) {
				e.printStackTrace();
				context.getCounter(HBASE_COUNTER.GETS_EMPTY).increment(1);
				break;
			}

		System.err.println("error with ast: " + rev.getKey() + "!!" + f.getName());
		context.getCounter(HBASE_COUNTER.GETS_FAILED).increment(1);
		return b.build();
	}

	@SuppressWarnings("rawtypes")
	public static void initialize(final Context context) {
		BoaAstIntrinsics.context = context;
	}

	public static void close() {
		if (table != null)
			try {
				table.close();
			} catch (final IOException e) {
				e.printStackTrace();
			} finally {
				table = null;
			}
	}

	@FunctionSpec(name = "len", returnType = "int", formalParameters = { "CodeRepository" })
	public static long ast_len(final CodeRepository r) {
		long count = 1;

		for (int i = 0; i < r.getRevisionsCount(); i++)
			count += ast_len(r.getRevisions(i));

		return count;
	}

	@FunctionSpec(name = "len", returnType = "int", formalParameters = { "Revision" })
	public static long ast_len(final Revision r) {
		long count = 1;

		for (int i = 0; i < r.getFilesCount(); i++) {
			ChangedFile.FileKind k = r.getFiles(i).getKind();
			if (k == ChangedFile.FileKind.UNKNOWN) continue;
			if (k == ChangedFile.FileKind.BINARY) continue;
			if (k == ChangedFile.FileKind.TEXT) continue;
			if (k == ChangedFile.FileKind.XML) continue;
			count += ast_len(BoaAstIntrinsics.getast(r, r.getFiles(i)));
		}

		return count;
	}

	@FunctionSpec(name = "len", returnType = "int", formalParameters = { "ASTRoot" })
	public static long ast_len(final ASTRoot f) {
		long count = 1;

		for (int i = 0; i < f.getNamespacesCount(); i++)
			for (int j = 0; j < f.getNamespaces(i).getDeclarationsCount(); j++)
				count += ast_len(f.getNamespaces(i).getDeclarations(j));

		return count;
	}

	@FunctionSpec(name = "len", returnType = "int", formalParameters = { "Namespace" })
	public static long ast_len(final Namespace n) {
		long count = 1;

		for (int i = 0; i < n.getDeclarationsCount(); i++)
			count += ast_len(n.getDeclarations(i));

		for (int i = 0; i < n.getModifiersCount(); i++)
			count += ast_len(n.getModifiers(i));

		return count;
	}

	@FunctionSpec(name = "len", returnType = "int", formalParameters = { "Declaration" })
	public static long ast_len(final Declaration d) {
		long count = 1;

		for (int i = 0; i < d.getModifiersCount(); i++)
			count += ast_len(d.getModifiers(i));

		for (int i = 0; i < d.getGenericParametersCount(); i++)
			count += ast_len(d.getGenericParameters(i));

		for (int i = 0; i < d.getParentsCount(); i++)
			count += ast_len(d.getParents(i));

		for (int i = 0; i < d.getMethodsCount(); i++)
			count += ast_len(d.getMethods(i));

		for (int i = 0; i < d.getFieldsCount(); i++)
			count += ast_len(d.getFields(i));

		for (int i = 0; i < d.getNestedDeclarationsCount(); i++)
			count += ast_len(d.getNestedDeclarations(i));

		for (int i = 0; i < d.getCommentsCount(); i++)
			count += ast_len(d.getComments(i));

		return count;
	}

	@FunctionSpec(name = "len", returnType = "int", formalParameters = { "Type" })
	public static long ast_len(final Type t) {
		long count = 1;

		return count;
	}

	@FunctionSpec(name = "len", returnType = "int", formalParameters = { "Method" })
	public static long ast_len(final Method m) {
		long count = 1;

		for (int i = 0; i < m.getModifiersCount(); i++)
			count += ast_len(m.getModifiers(i));

		count += ast_len(m.getReturnType());

		for (int i = 0; i < m.getGenericParametersCount(); i++)
			count += ast_len(m.getGenericParameters(i));

		for (int i = 0; i < m.getArgumentsCount(); i++)
			count += ast_len(m.getArguments(i));

		for (int i = 0; i < m.getExceptionTypesCount(); i++)
			count += ast_len(m.getExceptionTypes(i));

		for (int i = 0; i < m.getStatementsCount(); i++)
			count += ast_len(m.getStatements(i));

		for (int i = 0; i < m.getCommentsCount(); i++)
			count += ast_len(m.getComments(i));

		return count;
	}

	@FunctionSpec(name = "len", returnType = "int", formalParameters = { "Variable" })
	public static long ast_len(final Variable v) {
		long count = 1;

		count += ast_len(v.getVariableType());

		for (int i = 0; i < v.getModifiersCount(); i++)
			count += ast_len(v.getModifiers(i));

		if (v.hasInitializer())
			count += ast_len(v.getInitializer());

		for (int i = 0; i < v.getCommentsCount(); i++)
			count += ast_len(v.getComments(i));

		return count;
	}

	@FunctionSpec(name = "len", returnType = "int", formalParameters = { "Statement" })
	public static long ast_len(final Statement s) {
		long count = 1;

		for (int i = 0; i < s.getCommentsCount(); i++)
			count += ast_len(s.getComments(i));

		for (int i = 0; i < s.getStatementsCount(); i++)
			count += ast_len(s.getStatements(i));

		for (int i = 0; i < s.getInitializationsCount(); i++)
			count += ast_len(s.getInitializations(i));

		if (s.hasCondition())
			count += ast_len(s.getCondition());

		for (int i = 0; i < s.getUpdatesCount(); i++)
			count += ast_len(s.getUpdates(i));

		if (s.hasVariableDeclaration())
			count += ast_len(s.getVariableDeclaration());

		if (s.hasTypeDeclaration())
			count += ast_len(s.getTypeDeclaration());

		if (s.hasExpression())
			count += ast_len(s.getExpression());

		return count;
	}

	@FunctionSpec(name = "len", returnType = "int", formalParameters = { "Expression" })
	public static long ast_len(final Expression e) {
		long count = 1;

		for (int i = 0; i < e.getExpressionsCount(); i++)
			count += ast_len(e.getExpressions(i));

		for (int i = 0; i < e.getVariableDeclsCount(); i++)
			count += ast_len(e.getVariableDecls(i));

		if (e.hasNewType())
			count += ast_len(e.getNewType());

		for (int i = 0; i < e.getGenericParametersCount(); i++)
			count += ast_len(e.getGenericParameters(i));

		for (int i = 0; i < e.getMethodArgsCount(); i++)
			count += ast_len(e.getMethodArgs(i));

		if (e.hasAnonDeclaration())
			count += ast_len(e.getAnonDeclaration());

		return count;
	}

	@FunctionSpec(name = "len", returnType = "int", formalParameters = { "Modifier" })
	public static long ast_len(final Modifier m) {
		long count = 1;

		for (int i = 0; i < m.getAnnotationValuesCount(); i++)
			count += ast_len(m.getAnnotationValues(i));

		return count;
	}

	@FunctionSpec(name = "len", returnType = "int", formalParameters = { "Comment" })
	public static long ast_len(final Comment c) {
		long count = 1;

		return count;
	}
}
