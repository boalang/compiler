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
	
				b.mergeFrom(CodedInputStream.newInstance(val, 0, val.length));
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
}
