package sizzle.functions;

import java.io.IOException;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapreduce.Mapper.Context;

import com.google.protobuf.CodedInputStream;

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
		GETS_FAILED
	};

	@SuppressWarnings("unchecked")
	@FunctionSpec(name = "getast", returnType = "ASTRoot", formalParameters = { "Revision", "ChangedFile" })
	public static ASTRoot getast(final Revision rev, final ChangedFile f) {
		context.getCounter(HBASE_COUNTER.GETS).increment(1);
		final ASTRoot.Builder b = ASTRoot.newBuilder();

		try {
			if (table == null)
				table = new HTable(HBaseConfiguration.create(), context.getConfiguration().get("boa.hbase.table"));

			System.out.println("getting ast: " + rev.getKey() + "!!" + f.getName());
			final Get get = new Get(Bytes.toBytes(rev.getKey()));
			get.addColumn(family, Bytes.toBytes(f.getName()));

			final Result result = table.get(get);
			final byte[] val = result.value();
			if (val == null) throw new IOException("null value found");

			b.mergeFrom(CodedInputStream.newInstance(val, 0, val.length));
		} catch (final IOException e) {
			e.printStackTrace();
			context.getCounter(HBASE_COUNTER.GETS_FAILED).increment(1);
		}

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
			}
	}
}
