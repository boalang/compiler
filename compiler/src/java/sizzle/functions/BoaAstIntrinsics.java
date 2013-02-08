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

import sizzle.types.Ast.*;
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
	private static long counter = 0;

	private final static byte[] codeFamily = Bytes.toBytes("code");
	private final static byte[] locFamily = Bytes.toBytes("loc");
	private static String astTable;

	public static enum HBASE_COUNTER {
		GETS_ATTEMPTED,
		GETS_SUCCEED,
		GETS_FAILED,
		GETS_FAIL_MISSING,
		GETS_FAIL_BADPROTOBUF,
		GETS_FAIL_BADLOC,
	};

	/**
	 * Given a Revision and ChangedFile, return the AST for that file at that revision.
	 * 
	 * @param rev the Revision to get a snapshot of the AST from
	 * @param f the ChangedFile to get a snapshot of the AST for
	 * @return the AST, or an empty AST on any sort of error
	 */
	@SuppressWarnings("unchecked")
	@FunctionSpec(name = "getast", returnType = "ASTRoot", formalParameters = { "ChangedFile" })
	public static ASTRoot getast(final ChangedFile f) {
		// since we know only certain kinds have ASTs, filter before looking in HBase
		final ChangedFile.FileKind kind = f.getKind();
		if (kind != ChangedFile.FileKind.SOURCE_JAVA_ERROR
				&& kind != ChangedFile.FileKind.SOURCE_JAVA_JLS2
				&& kind != ChangedFile.FileKind.SOURCE_JAVA_JLS3
				&& kind != ChangedFile.FileKind.SOURCE_JAVA_JLS4)
			return ASTRoot.newBuilder().build();

		context.getCounter(HBASE_COUNTER.GETS_ATTEMPTED).increment(1);

		// let the task tracker know we are alive every so often
		if (++counter == 100) {
			counter = 0;
			context.progress();
		}

		final byte[] rowName = Bytes.toBytes(f.getKey());
		final Get get = new Get(rowName);
		final byte[] colName = Bytes.toBytes(f.getName());
		get.addColumn(codeFamily, colName);

		// retry on errors
		// sometimes HBase has connection problems which resolve fairly quick
		for (int i = 0; i < 10; i++)
			try {
				if (table == null)
					table = new HTable(HBaseConfiguration.create(), astTable);

				final Result res = table.get(get);
				if (!res.containsColumn(codeFamily, colName) || res.isEmpty())
					throw new RuntimeException("cell not found");
				final byte[] val = res.value();
	
				final CodedInputStream _stream = CodedInputStream.newInstance(val, 0, val.length);
				// defaults to 64, really big ASTs require more
				_stream.setRecursionLimit(Integer.MAX_VALUE);
				final ASTRoot root = ASTRoot.parseFrom(_stream);
				context.getCounter(HBASE_COUNTER.GETS_SUCCEED).increment(1);
				return root;
			} catch (final InvalidProtocolBufferException e) {
				e.printStackTrace();
				context.getCounter(HBASE_COUNTER.GETS_FAIL_BADPROTOBUF).increment(1);
				break;
			} catch (final IOException e) {
				System.err.println("hbase error: " + e.getMessage());
				close();
				try { Thread.sleep(500); } catch (InterruptedException e1) { }
			} catch (final RuntimeException e) {
				e.printStackTrace();
				context.getCounter(HBASE_COUNTER.GETS_FAIL_MISSING).increment(1);
				break;
			}

		System.err.println("error with ast: " + f.getKey() + "!!" + f.getName());
		context.getCounter(HBASE_COUNTER.GETS_FAILED).increment(1);
		return ASTRoot.newBuilder().build();
	}

	@SuppressWarnings("unchecked")
	@FunctionSpec(name = "loc", returnType = "int", formalParameters = { "ChangedFile" })
	public static long loc(final ChangedFile f) {
		context.getCounter(HBASE_COUNTER.GETS_ATTEMPTED).increment(1);

		final byte[] rowName = Bytes.toBytes(f.getKey());
		final Get get = new Get(rowName);
		final byte[] colName = Bytes.toBytes(f.getName());
		get.addColumn(locFamily, colName);

		// retry on errors
		// sometimes HBase has connection problems which resolve fairly quick
		for (int i = 0; i < 10; i++)
			try {
				if (table == null)
					table = new HTable(HBaseConfiguration.create(), astTable);

				final Result res = table.get(get);
				if (!res.containsColumn(locFamily, colName) || res.isEmpty()) {
					context.getCounter(HBASE_COUNTER.GETS_FAIL_MISSING).increment(1);
					throw new RuntimeException("cell not found");
				}
	
				final String s = Bytes.toString(res.value());
				final String[] parts = s.split(",");
				if (parts.length != 6) {
					context.getCounter(HBASE_COUNTER.GETS_FAIL_BADLOC).increment(1);
					throw new RuntimeException("contains invalid LOC: '" + s + "'");
				}
				context.getCounter(HBASE_COUNTER.GETS_SUCCEED).increment(1);
				return Long.parseLong(parts[1].trim()) + Long.parseLong(parts[2].trim());
			} catch (final IOException e) {
				System.err.println("hbase error: " + e.getMessage());
				close();
				try { Thread.sleep(500); } catch (InterruptedException e1) { }
			} catch (final RuntimeException e) {
				e.printStackTrace();
				break;
			}

		System.err.println("error with loc: " + f.getKey() + "!!" + f.getName());
		context.getCounter(HBASE_COUNTER.GETS_FAILED).increment(1);
		return -1;
	}

	@SuppressWarnings("rawtypes")
	public static void initialize(final Context context) {
		BoaAstIntrinsics.context = context;
		astTable = context.getConfiguration().get("boa.hbase.ast.table", "boa_input");
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

	@FunctionSpec(name = "type_name", returnType = "string", formalParameters = { "string" })
	public static String type_name(final String s) {
		// first, normalize the string
		final String t = s.replaceAll("<\\s+", "<")
			.replaceAll(",\\s+", ", ")
			.replaceAll("\\s*>\\s*", ">")
			.replaceAll("\\s*&\\s*", " & ")
			.replaceAll("\\s*\\|\\s*", " | ");

		if (!t.contains("."))
			return t;

		/*
		 * Remove qualifiers from anywhere in the string...
		 * 
		 * SomeType                               =>  SomeType
		 * foo.SomeType                           =>  SomeType
		 * foo.bar.SomeType                       =>  SomeType
		 * SomeType<T>                            =>  SomeType<T>
		 * SomeType<T, S>                         =>  SomeType<T, S>
		 * SomeType<foo.bar.T, S>                 =>  SomeType<T, S>
		 * SomeType<T, foo.bar.S>                 =>  SomeType<T, S>
		 * foo.bar.SomeType<T, foo.bar.S<bar.Q>>  =>  SomeType<T, S<Q>>
		 * SomeType|foo.Bar                       =>  SomeType|Bar
		 * foo<T>.bar<T>                          =>  foo<T>.bar<T>
		 */
		return t.replaceAll("[^\\s,<>|]+\\.([^\\s\\[.,><|]+)", "$1");
	}

	private static class ASTLenVisitor extends BoaCountingVisitor {
		@Override
		protected void defaultPreVisit() {
			count++;
		}
		@Override
		protected boolean preVisit(ASTRoot node) {
			return true;
		}
	}
	private static ASTLenVisitor lenVisitor = new ASTLenVisitor();

	@FunctionSpec(name = "len", returnType = "int", formalParameters = { "ASTRoot" })
	public static long ast_len(final ASTRoot f) throws Exception {
		lenVisitor.initialize().visit(f);
		return lenVisitor.count;
	}

	@FunctionSpec(name = "len", returnType = "int", formalParameters = { "Namespace" })
	public static long ast_len(final Namespace n) throws Exception {
		lenVisitor.initialize().visit(n);
		return lenVisitor.count;
	}

	@FunctionSpec(name = "len", returnType = "int", formalParameters = { "Declaration" })
	public static long ast_len(final Declaration d) throws Exception {
		lenVisitor.initialize().visit(d);
		return lenVisitor.count;
	}

	@FunctionSpec(name = "len", returnType = "int", formalParameters = { "Type" })
	public static long ast_len(final Type t) throws Exception {
		lenVisitor.initialize().visit(t);
		return lenVisitor.count;
	}

	@FunctionSpec(name = "len", returnType = "int", formalParameters = { "Method" })
	public static long ast_len(final Method m) throws Exception {
		lenVisitor.initialize().visit(m);
		return lenVisitor.count;
	}

	@FunctionSpec(name = "len", returnType = "int", formalParameters = { "Variable" })
	public static long ast_len(final Variable v) throws Exception {
		lenVisitor.initialize().visit(v);
		return lenVisitor.count;
	}

	@FunctionSpec(name = "len", returnType = "int", formalParameters = { "Statement" })
	public static long ast_len(final Statement s) throws Exception {
		lenVisitor.initialize().visit(s);
		return lenVisitor.count;
	}

	@FunctionSpec(name = "len", returnType = "int", formalParameters = { "Expression" })
	public static long ast_len(final Expression e) throws Exception {
		lenVisitor.initialize().visit(e);
		return lenVisitor.count;
	}

	@FunctionSpec(name = "len", returnType = "int", formalParameters = { "Modifier" })
	public static long ast_len(final Modifier m) throws Exception {
		lenVisitor.initialize().visit(m);
		return lenVisitor.count;
	}

	@FunctionSpec(name = "len", returnType = "int", formalParameters = { "Comment" })
	public static long ast_len(final Comment c) throws Exception {
		lenVisitor.initialize().visit(c);
		return lenVisitor.count;
	}
}
