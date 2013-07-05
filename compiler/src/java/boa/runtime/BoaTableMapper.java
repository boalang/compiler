package boa.runtime;

import java.io.IOException;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;

import org.apache.hadoop.hbase.mapreduce.TableMapper;

import org.apache.log4j.Logger;

import boa.io.EmitKey;
import boa.io.EmitValue;


public abstract class BoaTableMapper extends TableMapper<EmitKey, EmitValue> implements Configurable {
	protected static final Logger LOG = Logger.getLogger(BoaTableMapper.class);

	private Configuration conf;
	protected Context context;
	protected boolean robust;

	/** {@inheritDoc} */
	@Override
	public Configuration getConf() {
		return this.conf;
	}

	/** {@inheritDoc} */
	@Override
	public void setConf(final Configuration conf) {
		this.conf = conf;
		this.robust = conf.getBoolean("boa.runtime.robust", false);
	}

	/** {@inheritDoc} */
	@Override
	protected void setup(final Context context) throws IOException, InterruptedException {
		super.setup(context);

		this.context = context;
	}
}
