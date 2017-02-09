package generatedsrc.boa;

public class Ml extends boa.runtime.BoaRunner {
	/** {@inheritDoc} */
	@Override
	public org.apache.hadoop.mapreduce.Job job(final org.apache.hadoop.fs.Path[] ins, final org.apache.hadoop.fs.Path out, final boolean robust) throws java.io.IOException {
		final org.apache.hadoop.mapreduce.Job job = super.job(ins, out, robust);

		job.setJobName("Ml: " + out);

		job.setJarByClass(MlBoaMapper.class);

		job.setMapperClass(MlBoaMapper.class);
		job.setCombinerClass(MlBoaCombiner.class);
		job.setReducerClass(MlBoaReducer.class);

		return job;
	}

	static {
		getOptions().addOption(org.apache.commons.cli.OptionBuilder.withLongOpt("splitsize")
										.withDescription("split size in BYTES")
										.hasArg()
										.withArgName("BYTES")
										.create("s"));
	}

	public static void main(String[] args) throws Exception {
	org.apache.hadoop.util.ToolRunner.run(new Ml(), args);
		//System.exit();
	}

	@Override
	public int run(String[] args) throws Exception {
		final org.apache.commons.cli.CommandLine line = parseArgs(args, getUsage());
		args = line.getArgs();
		if (args.length != 2) {
			System.err.println("Not enough arguments. Must give input directory and output directory.");
			printHelp(getUsage());
		}

		final boolean robust = line.hasOption("robust");
		final int id;
		if (line.hasOption("job"))
			id = Integer.parseInt(line.getOptionValue("job"));
		else
			id = 0;

		final org.apache.hadoop.fs.Path[] ins = new org.apache.hadoop.fs.Path[1];
		ins[0] = new org.apache.hadoop.fs.Path(args[0] + "/projects.seq");

		final org.apache.hadoop.mapreduce.Job jb = job(ins, new org.apache.hadoop.fs.Path(args[1]), robust);

		org.apache.hadoop.conf.Configuration configuration = jb.getConfiguration();

		configuration.set("boa.input.dir", args[0]);
		if (line.hasOption("ast"))
			configuration.set("boa.ast.dir", line.getOptionValue("ast"));
		if (line.hasOption("comments"))
			configuration.set("boa.comments.dir", line.getOptionValue("comments"));

		if (line.hasOption("splitsize"))
			configuration.setInt("mapred.max.split.size", Integer.parseInt(line.getOptionValue("splitsize")));
		else
			configuration.setInt("mapred.max.split.size", 67108864);

		if (line.hasOption("profile")) {
			configuration.setBoolean("mapred.task.profile", true);
			configuration.set("mapred.task.profile.maps", "1");
			configuration.set("mapred.task.profile.reduces", "0");
			//configuration.set("mapred.task.profile.params", "-agentlib:hprof=cpu=samples,heap=sites,force=n,thread=y,verbose=n,file=%s");
			configuration.set("mapred.task.profile.params", "-agentlib:hprof=cpu=times,heap=sites,force=n,verbose=n,file=%s");
		}

		jb.setInputFormatClass(org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat.class);

		jb.setNumReduceTasks(1);

		if (id > 0)
			configuration.setInt("boa.hadoop.jobid", id);
		jb.waitForCompletion(false);
		if (id > 0)
			boa.io.BoaOutputCommitter.setJobID(jb.getJobID().toString(), id);
		System.err.println("Job ID: " + jb.getJobID().toString());

		if (line.hasOption("block")) {
			double lastSetup = -1;
			double lastMap = -1;
			double lastReduce = -1;
			while (!jb.isComplete()) {
				final double newSetup = jb.setupProgress();
				final double newMap = jb.mapProgress();
				final double newReduce = jb.reduceProgress();
				if (newSetup != lastSetup) {
					lastSetup = newSetup;
					System.err.println("SETUP : " + (newSetup * 100) + "%");
				}
				if (newMap != lastMap) {
					lastMap = newMap;
					System.err.println("MAP   : " + (newMap * 100) + "%");
				}
				if (newReduce != lastReduce) {
					lastReduce = newReduce;
					System.err.println("REDUCE: " + (newReduce * 100) + "%");
				}
				try {
					Thread.sleep (500);
				} catch (final Exception e) {}
			}
			System.err.println("JOB FINISHED: " + (jb.isSuccessful() ? "Success" : "Failed"));
			return jb.isSuccessful() ? 0 : 1;
		}
		return 0;
	}

	public String getUsage() {
		return "<inputDir> <outputDir>";
	}

	static interface BoaJob {
		void map(final boa.types.Transport.Crash _input, final org.apache.hadoop.mapreduce.Mapper<org.apache.hadoop.io.Text, org.apache.hadoop.io.BytesWritable, boa.io.EmitKey, boa.io.EmitValue>.Context context) throws Exception;
	}

	static class MlBoaMapper extends boa.runtime.BoaMapper {
		private static class Job0 implements BoaJob {
			boa.types.Transport.Crash ___p;
			BoaTup_0 ___timing;
			BoaTup_0 ___timing2;

			public static class BoaTup_0 implements boa.BoaTup, java.io.Serializable
			{
				BoaTup_0(){}

				public long ___field0; public long ___field1; public long ___field2; public long ___field3; public long ___field4; public long ___field5;
				BoaTup_0( long ___field0, long ___field1, long ___field2, long ___field3, long ___field4, long ___field5 ){
					this.___field0 = ___field0; this.___field1 = ___field1; this.___field2 = ___field2; this.___field3 = ___field3; this.___field4 = ___field4; this.___field5 = ___field5;

				}


				public byte[] serialize(Object o) throws java.io.IOException { 
					java.io.ByteArrayOutputStream byteOutStream = new java.io.ByteArrayOutputStream();
					java.io.ObjectOutputStream objectOut = new java.io.ObjectOutputStream(byteOutStream);
					objectOut.writeObject(o);
					objectOut.close();
					return byteOutStream.toByteArray();
				}

				public String[] getValues() {
					return new String[] {String.valueOf(this.___field0), String.valueOf(this.___field1), String.valueOf(this.___field2), String.valueOf(this.___field3), String.valueOf(this.___field4), String.valueOf(this.___field5) };
				}
				//Add By Johir
				public String[] addTuple(boa.BoaTup other) {
				    String [] values=other.getValues();
				    String [] types=other.getTypes();
				    int len=values.length;
				    String [] thisValues=this.getValues();
				    String [] thisTypes=this.getTypes();
				    int thislen=thisValues.length;
				    if(len!=thislen)
				    {
				    throw new IllegalStateException("The lenghts of two tuples are not equal");
				    }
				    String [] retValues=new String [len];
				    for(int i=0;i < len;i++)
				    {
				    // For now considering only double
				    double a=Double.parseDouble(values[i]);
				    double b=Double.parseDouble(thisValues[i]);
				    double c=a+b;
				    retValues[i]=String.valueOf(c);
				    }
				    return retValues;
				}

				public String[] getTypes(){
				return new String[] {"long", "long", "long", "long", "long", "long" };
				}


				//End Added By Johir
				public String[] getFieldNames() { 
					return new String[] {"___field0", "___field1", "___field2", "___field3", "___field4", "___field5" };
				}

				public Object getValue(String f) {
							if(f.equals("___field0")) 
								return ___field0;
					 if(f.equals("___field1")) 
								return ___field1;
					 if(f.equals("___field2")) 
								return ___field2;
					 if(f.equals("___field3")) 
								return ___field3;
					 if(f.equals("___field4")) 
								return ___field4;
					 if(f.equals("___field5")) 
								return ___field5;

					return "empty";
				}
			}

			public void map(final boa.types.Transport.Crash _input, final org.apache.hadoop.mapreduce.Mapper<org.apache.hadoop.io.Text, org.apache.hadoop.io.BytesWritable, boa.io.EmitKey, boa.io.EmitValue>.Context context) throws Exception {
				___p = _input;

				context.write(new boa.io.EmitKey("[" + (___p.getHOUR()) + "]", "data", 0), new boa.io.EmitValue(1l));

				___timing = new BoaTup_0(___p.getDAY(), ___p.getMONTH(), ___p.getYEAR(), ___p.getDAYWEEK(), ___p.getHOUR(), ___p.getMINUTE()) 
				;

				___timing2 = new BoaTup_0(___p.getDAY(), ___p.getMONTH(), ___p.getYEAR(), ___p.getDAYWEEK(), ___p.getHOUR(), ___p.getMINUTE()) 
				;

				java.lang.Math.max(5l, 3l);

				boa.tuplefunctions.TupleOps.matrix(___timing,3l);

				context.write(new boa.io.EmitKey("train", 0), new boa.io.EmitValue(___timing));

			}
		}
		private static BoaJob _job_0 = new Job0();

		/** {@inheritDoc} */
		@Override
		protected void map(final org.apache.hadoop.io.Text key, final org.apache.hadoop.io.BytesWritable value, final org.apache.hadoop.mapreduce.Mapper<org.apache.hadoop.io.Text, org.apache.hadoop.io.BytesWritable, boa.io.EmitKey, boa.io.EmitValue>.Context context) throws java.io.IOException {
			try {
				boa.types.Transport.Crash _input = boa.types.Transport.Crash.parseFrom(com.google.protobuf.CodedInputStream.newInstance(value.getBytes(), 0, value.getLength()));
				runJob("Job0", _job_0, _input, context);
			} catch (final Throwable e) {
				boa.io.BoaOutputCommitter.lastSeenEx = e;
				throw new java.io.IOException("map failure for key '" + key.toString() + "'", e);
			}
		}

		private void runJob(final String name, final BoaJob job, final boa.types.Transport.Crash input, final org.apache.hadoop.mapreduce.Mapper<org.apache.hadoop.io.Text, org.apache.hadoop.io.BytesWritable, boa.io.EmitKey, boa.io.EmitValue>.Context context) throws Throwable {
			try {
				job.map(input, context);
			} catch (final Throwable e) {
				LOG.error(name + ": " + e.getClass().getName() + " caught", e);
				throw e;
			}
			context.progress();
		}

		/** {@inheritDoc} */
		@Override
		protected void setup(final org.apache.hadoop.mapreduce.Mapper.Context context) throws java.io.IOException, java.lang.InterruptedException {
			boa.functions.BoaAstIntrinsics.setup(context);
			super.setup(context);
		}

		/** {@inheritDoc} */
		@Override
		protected void cleanup(final org.apache.hadoop.mapreduce.Mapper.Context context) throws java.io.IOException, java.lang.InterruptedException {
			boa.functions.BoaAstIntrinsics.cleanup(context);
			super.cleanup(context);
		}
	}

	static class MlBoaCombiner extends boa.runtime.BoaCombiner {
		public MlBoaCombiner() {
			super();

			this.aggregators.put("0::data", new boa.aggregators.IntSumAggregator());
		}
	}

	static class MlBoaReducer extends boa.runtime.BoaReducer {
		public MlBoaReducer() {
			super();

			this.aggregators.put("0::data", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("0::train", new boa.aggregators.ml.SimpleKMeansAggregator("-init 1 -N 30 -O "));
		}
	}

	@Override
	public org.apache.hadoop.mapreduce.Mapper getMapper() {
		return new MlBoaMapper();
	}

	@Override
	public boa.runtime.BoaCombiner getCombiner() {
		return new MlBoaCombiner();
	}

	@Override
	public boa.runtime.BoaReducer getReducer() {
		return new MlBoaReducer();
	}
}