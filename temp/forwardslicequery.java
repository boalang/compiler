package boa;

public class Sample extends boa.runtime.BoaRunner {
	/** {@inheritDoc} */
	@Override
	public org.apache.hadoop.mapreduce.Job job(final org.apache.hadoop.fs.Path[] ins, final org.apache.hadoop.fs.Path out) throws java.io.IOException {
		final org.apache.hadoop.mapreduce.Job job = super.job(ins, out);

		job.setJobName("Sample: " + out);

		job.setJarByClass(SampleBoaMapper.class);

		job.setMapperClass(SampleBoaMapper.class);
		job.setCombinerClass(SampleBoaCombiner.class);
		job.getConfiguration().setClass("mapred.map.output.compression.codec", org.apache.hadoop.io.compress.DefaultCodec.class, org.apache.hadoop.io.compress.CompressionCodec.class);
		job.setReducerClass(SampleBoaReducer.class);

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
		org.apache.hadoop.util.ToolRunner.run(new Sample(), args);
	}

	@Override
	public int run(String[] args) throws Exception {
		final org.apache.commons.cli.CommandLine line = parseArgs(args, getUsage());
		args = line.getArgs();
		if (args.length != 2) {
			System.err.println("Not enough arguments. Must give input directory and output directory.");
			printHelp(getUsage());
		}

		final int id;
		if (line.hasOption("job"))
			id = Integer.parseInt(line.getOptionValue("job"));
		else
			id = 0;

		final org.apache.hadoop.fs.Path[] ins = new org.apache.hadoop.fs.Path[1];
		ins[0] = new org.apache.hadoop.fs.Path(args[0] + "/projects.seq");

		final org.apache.hadoop.mapreduce.Job jb = job(ins, new org.apache.hadoop.fs.Path(args[1]));

		org.apache.hadoop.conf.Configuration configuration = jb.getConfiguration();

		configuration.set("boa.input.dir", args[0]);
		if (line.hasOption("ast"))
			configuration.set("boa.ast.dir", line.getOptionValue("ast"));
		if (line.hasOption("comments"))
			configuration.set("boa.comments.dir", line.getOptionValue("comments"));

		if (line.hasOption("splitsize"))
			configuration.setInt("mapred.max.split.size", Integer.parseInt(line.getOptionValue("splitsize")));
		else
			configuration.setInt("mapred.max.split.size", 10485760);

		if (line.hasOption("profile")) {
			configuration.setBoolean("mapred.task.profile", true);
			configuration.set("mapred.task.profile.maps", "1");
			configuration.set("mapred.task.profile.reduces", "0");
			//configuration.set("mapred.task.profile.params", "-agentlib:hprof=cpu=samples,heap=sites,force=n,thread=y,verbose=n,file=%s");
			configuration.set("mapred.task.profile.params", "-agentlib:hprof=cpu=times,heap=sites,force=n,verbose=n,file=%s");
		}

		jb.setInputFormatClass(org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat.class);

		jb.setNumReduceTasks(26);

		if (id > 0)
			configuration.setInt("boa.hadoop.jobid", id);

		jb.submit();


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

	static class SampleBoaMapper extends boa.runtime.BoaMapper {
		static {
			boa.runtime.BoaPartitioner.setVariableNames(new String[] {"dnnModuleCount", "errorFileCount", "impactList", "kerasApiCallSiteCount", "kerasApiUniqueCount", "kerasModuleCount", "modifiedDnnModuleCount", "modifiedKerasApiUniqueCount", "modifiedKerasModuleCount", "modifiedTfApiUniqueCount", "modifiedTfModuleCount", "modifiedTorchApiUniqueCount", "modifiedTorchModuleCount", "modifiedkerasApiCallSiteCount", "modifiedtfApiCallSiteCount", "modifiedtorchApiCallSiteCount", "noCommit", "nodePrint", "testFileCount", "tfApiCallSiteCount", "tfApiUniqueCount", "tfModuleCount", "torchApiCallSiteCount", "torchApiUniqueCount", "torchModuleCount", "totalModuleCount"});
		}
		long ____local_aggregator_modifiedTfApiUniqueCount;
		long ____local_aggregator_tfApiUniqueCount;
		long ____local_aggregator_modifiedTorchApiUniqueCount;
		long ____local_aggregator_torchApiUniqueCount;
		long ____local_aggregator_modifiedKerasApiUniqueCount;
		long ____local_aggregator_kerasApiUniqueCount;
		long ____local_aggregator_modifiedtfApiCallSiteCount;
		long ____local_aggregator_tfApiCallSiteCount;
		long ____local_aggregator_modifiedtorchApiCallSiteCount;
		long ____local_aggregator_torchApiCallSiteCount;
		long ____local_aggregator_modifiedkerasApiCallSiteCount;
		long ____local_aggregator_kerasApiCallSiteCount;
		long ____local_aggregator_modifiedTfModuleCount;
		long ____local_aggregator_tfModuleCount;
		long ____local_aggregator_modifiedTorchModuleCount;
		long ____local_aggregator_torchModuleCount;
		long ____local_aggregator_modifiedKerasModuleCount;
		long ____local_aggregator_kerasModuleCount;
		long ____local_aggregator_totalModuleCount;
		long ____local_aggregator_modifiedDnnModuleCount;
		long ____local_aggregator_dnnModuleCount;
		long ____local_aggregator_errorFileCount;
		long ____local_aggregator_testFileCount;
		boa.types.Toplevel.Project ___p_0;
		java.util.HashMap<String, boa.types.Ast.Method> ___methodMap_1;
		java.util.Stack<String> ___containingTypes_2;
		java.util.Stack<String> ___acrossInContainingTypes_3;
		java.util.HashSet<String> ___propagationList_4;
		java.util.HashMap<String, String> ___dnnVariableMap_5;
		java.util.HashMap<String, String> ___objectMap_6;
		java.util.HashSet<String> ___currentImpactedAPI_7;
		java.util.HashSet<BoaTup_0> ___allDNNApi_8;
		java.util.HashSet<String> ___currentModuleLibs_9;
		java.util.HashMap<Long, Long> ___isApiCallSiteCounted_10;
		java.util.HashSet<String> ___overallModifiedApi_11;
		java.util.HashSet<String> ___overallApiSet_12;
		java.util.Stack<String> ___namespaceStack_13;
		java.util.Stack<String> ___statementStack_14;
		String ___current_method_name_15;
		String ___current_file_name_16;
		String ___revision_id_17;
		String ___current_stmt_id_18;
		String ___current_expr_id_19;
		java.util.HashMap<String, Long> ___visited_20;
		long ___currentCallDepth_21;
		java.util.HashMap<String, java.util.HashMap<Long, java.util.HashSet<Long>>> ___reachable_22;
		java.util.HashSet<Long> ___visitedCfgNode_23;
		java.util.HashMap<String, Long> ___idMap_24;
		java.util.HashMap<String, boa.graphs.cfg.CFG> ___cfgMap_25;
		boolean ___enableAcrossIn_26;
		long ___maximumCallDepth_27;
		java.util.HashMap<String, java.util.HashSet<Long>> ___acrossInIdMap_28;
		boolean ___acrossInActive_29;
		java.util.HashSet<String> ___returnImpacted_30;
		boolean ___moduleChanged_31;
		java.util.HashMap<String, java.util.HashMap<String, java.util.HashSet<Long>>> ___impactSet_32;
		java.util.HashMap<String, java.util.HashMap<String, java.util.HashSet<BoaTup_0>>> ___aliasSet_33;
		String[] ___dnnPackage_34;
		String[] ___otherPackage_35;
		BoaFunc_0 ___stackToStr_36;
		BoaFunc_1 ___getStringUntil_41;
		BoaFunc_1 ___getStringAfter_42;
		BoaFunc_2 ___isCfgDefined_43;
		BoaFunc_3 ___getCurrentScope_44;
		BoaFunc_4 ___getParentScope_45;
		BoaFunc_5 ___isMethodScope_46;
		BoaFunc_5 ___isClassScope_47;
		BoaFunc_5 ___isGlobalScope_48;
		BoaFunc_5 ___isCallScope_49;
		BoaFunc_6 ___isKindPrefix_50;
		BoaFunc_7 ___areExpressionsDefinedInStatement_52;
		BoaFunc_8 ___areExpressionsDefinedInExp_53;
		BoaFunc_2 ___isTfApi_54;
		BoaFunc_2 ___isKerasApi_55;
		BoaFunc_2 ___isTorchApi_56;
		BoaFunc_9 ___recordImpact_57;
		BoaFunc_10 ___libraryWiseCount_59;
		BoaFunc_8 ___isProperAssignment_60;
		BoaFunc_4 ___resolveMethodScope_61;
		BoaFunc_11 ___addInAliasTrackingSet_66;
		BoaFunc_12 ___addInDeepTrackingSet_71;
		BoaFunc_13 ___addInDeepTrackingSetFromArray_75;
		BoaFunc_14 ___isKindChange_76;
		BoaFunc_8 ___isChangeKindModification_77;
		BoaFunc_15 ___expressionToStr_78;
		BoaFunc_15 ___expressionsToStr_80;
		BoaFunc_16 ___expressionsToStrUntil_83;
		BoaFunc_17 ___getVariableAsSet_87;
		BoaFunc_18 ___getVariableList_89;
		boolean ___hasBeenRedefinedAnywhere_92;
		BoaFunc_19 ___isReachable_93;
		BoaFunc_20 ___isImpacted_105;
		BoaFunc_21 ___isSetImpacted_108;
		BoaFunc_22 ___copyOverForAcrossIn_109;
		BoaFunc_23 ___getReachableAliasMappedName_114;
		BoaFunc_22 ___copyOverAliasForAcrossIn_117;
		BoaFunc_24 ___copyOverMethod_122;
		BoaFunc_25 ___copyOverDeclaration_126;
		BoaFunc_8 ___isExpressionModified_130;
		BoaFunc_8 ___isExpressionImpacted_131;
		BoaFunc_26 ___isVariableModified_133;
		BoaFunc_27 ___getDeclaredVariableName_134;
		BoaFunc_28 ___getDeclaredVariables_135;
		BoaFunc_24 ___hasSelfArg_137;
		BoaFunc_29 ___getNumberofMethodArgs_139;
		BoaFunc_30 ___constructAliasName_141;
		BoaFunc_31 ___resolveAlias_148;
		BoaFunc_32 ___acrossInCallParameterMapping_158;
		BoaFunc_33 ___handleBinaryExpression_163;
		BoaFunc_34 ___handleMethodCall_174;
		BoaFunc_35 ___preProcessAcrossIn_179;
		BoaFunc_34 ___postProcessAcrosIn_187;
		boa.runtime.BoaAbstractVisitor ___acrossInVisitor_192;
		BoaFunc_36 ___makeJumpAcrossIn_201;
		boa.runtime.BoaAbstractVisitor ___impactDetectionVisitor_210;
		BoaFunc_37 ___idExpressionMapper_214;
		BoaFunc_38 ___idVariableMapper_215;
		BoaFunc_39 ___idStatementMapper_216;
		BoaFunc_9 ___idMapper_217;

		private interface BoaFunc_0
		{
			String invoke(final java.util.Stack<String> ___types, final String ___joinStr) throws Exception;
		}

		private interface BoaFunc_1
		{
			String invoke(final String ___str, final String ___untilstr) throws Exception;
		}

		private interface BoaFunc_2
		{
			Boolean invoke(final String ___scope) throws Exception;
		}

		private interface BoaFunc_3
		{
			String invoke() throws Exception;
		}

		private interface BoaFunc_4
		{
			String invoke(final String ___scope) throws Exception;
		}

		private interface BoaFunc_5
		{
			Boolean invoke() throws Exception;
		}

		private interface BoaFunc_6
		{
			Boolean invoke(final boa.types.Ast.Expression ___node, final String ___prf) throws Exception;
		}

		private interface BoaFunc_7
		{
			Boolean invoke(final boa.types.Ast.Statement ___node) throws Exception;
		}

		private interface BoaFunc_8
		{
			Boolean invoke(final boa.types.Ast.Expression ___node) throws Exception;
		}

		private interface BoaFunc_9
		{
			void invoke(final String ___api) throws Exception;
		}

		private interface BoaFunc_10
		{
			void invoke(final String ___api, final long ___id, final boolean ___impacted, final boolean ___forModule) throws Exception;
		}

		private interface BoaFunc_11
		{
			void invoke(final String ___method_name, final String ___lft, final String ___right, final long ___leftId) throws Exception;
		}

		private interface BoaFunc_12
		{
			void invoke(final java.util.HashMap<String, java.util.HashMap<String, java.util.HashSet<Long>>> ___tmpMap, final String ___idName, final long ___id, final String ___method_name) throws Exception;
		}

		private interface BoaFunc_13
		{
			void invoke(final java.util.HashMap<String, java.util.HashMap<String, java.util.HashSet<Long>>> ___tmpMap, final String[] ___identifierName_72, final long ___id, final String ___method_name) throws Exception;
		}

		private interface BoaFunc_14
		{
			Boolean invoke(final boa.types.Shared.ChangeKind ___node) throws Exception;
		}

		private interface BoaFunc_15
		{
			String invoke(final boa.types.Ast.Expression ___node) throws Exception;
		}

		private interface BoaFunc_16
		{
			String invoke(final boa.types.Ast.Expression ___node, final long ___lm) throws Exception;
		}

		private interface BoaFunc_17
		{
			void invoke(final boa.types.Ast.Expression ___node, final java.util.HashSet<String> ___out) throws Exception;
		}

		private interface BoaFunc_18
		{
			String[] invoke(final boa.types.Ast.Expression ___node) throws Exception;
		}

		private interface BoaFunc_19
		{
			Boolean invoke(final long ___astSourceId, final long ___astTargetId, final String ___identifierName_72, final boolean ___isTargetCfgId) throws Exception;
		}

		private interface BoaFunc_20
		{
			Boolean invoke(final String ___identifierName_72, final long ___targetId) throws Exception;
		}

		private interface BoaFunc_21
		{
			Boolean invoke(final String[] ___identifierName_72, final long ___targetId) throws Exception;
		}

		private interface BoaFunc_22
		{
			void invoke(final long ___targetAstId, final String ___calledMethodName, final boolean ___isTargetCfgId) throws Exception;
		}

		private interface BoaFunc_23
		{
			String invoke(final String ___usedIdentifierName, final long ___targetId) throws Exception;
		}

		private interface BoaFunc_24
		{
			Boolean invoke(final boa.types.Ast.Method ___method) throws Exception;
		}

		private interface BoaFunc_25
		{
			Boolean invoke(final boa.types.Ast.Declaration ___method) throws Exception;
		}

		private interface BoaFunc_26
		{
			Boolean invoke(final boa.types.Ast.Variable ___node) throws Exception;
		}

		private interface BoaFunc_27
		{
			String invoke(final boa.types.Ast.Variable ___node) throws Exception;
		}

		private interface BoaFunc_28
		{
			String[] invoke(final boa.types.Ast.Statement ___node) throws Exception;
		}

		private interface BoaFunc_29
		{
			Long invoke(final boa.types.Ast.Method ___m) throws Exception;
		}

		private interface BoaFunc_30
		{
			String invoke(final java.util.HashMap<String, String> ___mp, final String ___key) throws Exception;
		}

		private interface BoaFunc_31
		{
			String invoke(final String ___key, final long ___targetId, final boolean ___isResolveImport, final boolean ___isResolveMethodForJump, final java.util.HashMap<String, String> ___mp) throws Exception;
		}

		private interface BoaFunc_32
		{
			Boolean invoke(final boa.types.Ast.Method ___targetMethod, final boa.types.Ast.Expression ___callSite, final String ___str_150) throws Exception;
		}

		private interface BoaFunc_33
		{
			void invoke(final String[] ___leftVars, final boa.types.Ast.Expression ___right, final long ___nodeId) throws Exception;
		}

		private interface BoaFunc_34
		{
			Long invoke(final boa.types.Ast.Expression ___node) throws Exception;
		}

		private interface BoaFunc_35
		{
			String invoke(final boa.types.Ast.Expression ___mainNode, final boolean ___isCallback) throws Exception;
		}

		private interface BoaFunc_36
		{
			Long invoke(final boa.types.Ast.Expression ___mainNode, final boolean ___isCallback) throws Exception;
		}

		private interface BoaFunc_37
		{
			void invoke(final boa.types.Ast.Expression ___node_202, final long ___id) throws Exception;
		}

		private interface BoaFunc_38
		{
			void invoke(final boa.types.Ast.Variable ___node_202, final long ___id) throws Exception;
		}

		private interface BoaFunc_39
		{
			void invoke(final boa.types.Ast.Statement ___node_202, final long ___id) throws Exception;
		}


		private class BoaTup_0
		{
			Long ___id;
			String ___name;

			BoaTup_0(Long ___id, String ___name) {
				this.___id = new Long(___id);
				this.___name = new String(___name);
			}
			BoaTup_0(BoaTup_0 tmp) {
				 this.___id = new Long(tmp.___id);
				 this.___name = new String(tmp.___name);
			}

			public BoaTup_0 clone() {
				return new BoaTup_0(this);
			}

			public String toString() {
				String s = "{";
				if (s.length() > 1) s += ",";
				s += " id = " + this.___id;
				if (s.length() > 1) s += ",";
				s += " name = " + this.___name;
				s += " }";
				return s;
			}
		}

		/** {@inheritDoc} */
		@Override
		protected void map(final org.apache.hadoop.io.Text key, final org.apache.hadoop.io.BytesWritable value, final org.apache.hadoop.mapreduce.Mapper<org.apache.hadoop.io.Text, org.apache.hadoop.io.BytesWritable, boa.io.EmitKey, boa.io.EmitValue>.Context context) throws java.io.IOException {
			try {
				boa.functions.BoaMathIntrinsics.random = new java.util.Random(705667632 + key.hashCode());
				boa.types.Toplevel.Project _input = boa.types.Toplevel.Project.parseFrom(com.google.protobuf.CodedInputStream.newInstance(value.getBytes(), 0, value.getLength()));
				____local_aggregator_modifiedTfApiUniqueCount = 0l;

				____local_aggregator_tfApiUniqueCount = 0l;

				____local_aggregator_modifiedTorchApiUniqueCount = 0l;

				____local_aggregator_torchApiUniqueCount = 0l;

				____local_aggregator_modifiedKerasApiUniqueCount = 0l;

				____local_aggregator_kerasApiUniqueCount = 0l;

				____local_aggregator_modifiedtfApiCallSiteCount = 0l;

				____local_aggregator_tfApiCallSiteCount = 0l;

				____local_aggregator_modifiedtorchApiCallSiteCount = 0l;

				____local_aggregator_torchApiCallSiteCount = 0l;

				____local_aggregator_modifiedkerasApiCallSiteCount = 0l;

				____local_aggregator_kerasApiCallSiteCount = 0l;

				____local_aggregator_modifiedTfModuleCount = 0l;

				____local_aggregator_tfModuleCount = 0l;

				____local_aggregator_modifiedTorchModuleCount = 0l;

				____local_aggregator_torchModuleCount = 0l;

				____local_aggregator_modifiedKerasModuleCount = 0l;

				____local_aggregator_kerasModuleCount = 0l;

				____local_aggregator_totalModuleCount = 0l;

				____local_aggregator_modifiedDnnModuleCount = 0l;

				____local_aggregator_dnnModuleCount = 0l;

				____local_aggregator_errorFileCount = 0l;

				____local_aggregator_testFileCount = 0l;

				___p_0 = _input;

				___methodMap_1 = new java.util.HashMap<String, boa.types.Ast.Method>();

				___containingTypes_2 = new java.util.Stack<String>();

				___acrossInContainingTypes_3 = new java.util.Stack<String>();

				___propagationList_4 = new java.util.HashSet<String>();

				___dnnVariableMap_5 = new java.util.HashMap<String, String>();

				___objectMap_6 = new java.util.HashMap<String, String>();

				___currentImpactedAPI_7 = new java.util.HashSet<String>();

				___allDNNApi_8 = new java.util.HashSet<BoaTup_0>();

				___currentModuleLibs_9 = new java.util.HashSet<String>();

				___isApiCallSiteCounted_10 = new java.util.HashMap<Long, Long>();

				___overallModifiedApi_11 = new java.util.HashSet<String>();

				___overallApiSet_12 = new java.util.HashSet<String>();

				___namespaceStack_13 = new java.util.Stack<String>();

				___statementStack_14 = new java.util.Stack<String>();

				___current_method_name_15 = "";

				___current_file_name_16 = "";

				___revision_id_17 = "";

				___current_stmt_id_18 = null;

				___current_expr_id_19 = null;

				___visited_20 = new java.util.HashMap<String, Long>();

				___currentCallDepth_21 = 0l;

				___reachable_22 = new java.util.HashMap<String, java.util.HashMap<Long, java.util.HashSet<Long>>>();

				___visitedCfgNode_23 = new java.util.HashSet<Long>();

				___idMap_24 = new java.util.HashMap<String, Long>();

				___cfgMap_25 = new java.util.HashMap<String, boa.graphs.cfg.CFG>();

				___enableAcrossIn_26 = true;

				___maximumCallDepth_27 = 20l;

				___acrossInIdMap_28 = new java.util.HashMap<String, java.util.HashSet<Long>>();

				___acrossInActive_29 = false;

				___returnImpacted_30 = new java.util.HashSet<String>();

				___moduleChanged_31 = false;

				___impactSet_32 = new java.util.HashMap<String, java.util.HashMap<String, java.util.HashSet<Long>>>();

				___aliasSet_33 = new java.util.HashMap<String, java.util.HashMap<String, java.util.HashSet<BoaTup_0>>>();

				___dnnPackage_34 = new String[] {
				"torch", "keras", "tensorflow"
				};

				___otherPackage_35 = new String[] {
				"numpy", "scipy", "sklearn", "pandas", "nltk"
				};

				___stackToStr_36 = new BoaFunc_0()
				{
					String ___fqn_37;
					String ___t_38;
					java.util.Stack<String> ___stkTemp_39;
					boolean ___tfirst_40;

					@Override
					public String invoke(final java.util.Stack<String> ___types, final String ___joinStr) throws Exception
					{
						___fqn_37 = "";
						___t_38 = null;
						___stkTemp_39 = new java.util.Stack<String>();
						while (true)
						{
							___t_38 = boa.functions.BoaIntrinsics.stack_pop(___types);
							if ((___t_38 != null))
							{
								___stkTemp_39.push(___t_38);
							}
							else
							{
								break;
							}
						}
						___tfirst_40 = false;
						while (true)
						{
							___t_38 = boa.functions.BoaIntrinsics.stack_pop(___stkTemp_39);
							if ((___t_38 != null))
							{
								if (___tfirst_40)
								{
									___fqn_37 = ___fqn_37 + ___joinStr;
								}
								___fqn_37 = ___fqn_37 + ___t_38;
								___types.push(___t_38);
								___tfirst_40 = true;
							}
							else
							{
								break;
							}
						}
						return ___fqn_37;
					}
				};

				___getStringUntil_41 = new BoaFunc_1()
				{
					@Override
					public String invoke(final String ___str, final String ___untilstr) throws Exception
					{
						if (boa.functions.BoaStringIntrinsics.lastIndexOf(___untilstr, ___str) != -1l)
						{
							return boa.functions.BoaStringIntrinsics.substring(___str, 0l, boa.functions.BoaStringIntrinsics.lastIndexOf(___untilstr, ___str));
						}
						return "";
					}
				};

				___getStringAfter_42 = new BoaFunc_1()
				{
					@Override
					public String invoke(final String ___str, final String ___afterstr) throws Exception
					{
						if (boa.functions.BoaStringIntrinsics.lastIndexOf(___afterstr, ___str) != -1l)
						{
							return boa.functions.BoaStringIntrinsics.substring(___str, boa.functions.BoaStringIntrinsics.lastIndexOf(___afterstr, ___str) + 1l);
						}
						return "";
					}
				};

				___isCfgDefined_43 = new BoaFunc_2()
				{
					@Override
					public Boolean invoke(final String ___scope) throws Exception
					{
						if (!___cfgMap_25.containsKey(___scope))
						{
							return false;
						}
						if ((___cfgMap_25.get(___scope) != null) == false)
						{
							return false;
						}
						if ((___cfgMap_25.get(___scope).getNodes() != null) == false)
						{
							return false;
						}
						if (((long)___cfgMap_25.get(___scope).getNodes().size()) <= 0l)
						{
							return false;
						}
						return true;
					}
				};

				___getCurrentScope_44 = new BoaFunc_3()
				{
					@Override
					public String invoke() throws Exception
					{
						if (___acrossInActive_29)
						{
							return ___stackToStr_36.invoke(___acrossInContainingTypes_3, "-");
						}
						return ___stackToStr_36.invoke(___containingTypes_2, ".");
					}
				};

				___getParentScope_45 = new BoaFunc_4()
				{
					@Override
					public String invoke(final String ___scope) throws Exception
					{
						return ___getStringUntil_41.invoke(___scope, ".");
					}
				};

				___isMethodScope_46 = new BoaFunc_5()
				{
					@Override
					public Boolean invoke() throws Exception
					{
						if ("method".equals(boa.functions.BoaIntrinsics.stack_peek(___namespaceStack_13)))
						{
							return true;
						}
						return false;
					}
				};

				___isClassScope_47 = new BoaFunc_5()
				{
					@Override
					public Boolean invoke() throws Exception
					{
						if ("class".equals(boa.functions.BoaIntrinsics.stack_peek(___namespaceStack_13)))
						{
							return true;
						}
						return false;
					}
				};

				___isGlobalScope_48 = new BoaFunc_5()
				{
					@Override
					public Boolean invoke() throws Exception
					{
						if ("global".equals(boa.functions.BoaIntrinsics.stack_peek(___namespaceStack_13)))
						{
							return true;
						}
						return false;
					}
				};

				___isCallScope_49 = new BoaFunc_5()
				{
					@Override
					public Boolean invoke() throws Exception
					{
						if ("call".equals(boa.functions.BoaIntrinsics.stack_peek(___statementStack_14)))
						{
							return true;
						}
						return false;
					}
				};

				___isKindPrefix_50 = new BoaFunc_6()
				{
					String ___skind_51;

					@Override
					public Boolean invoke(final boa.types.Ast.Expression ___node, final String ___prf) throws Exception
					{
						___skind_51 = ___node.getKind().name();
						return boa.functions.BoaStringIntrinsics.match("^" + ___prf + ".*", ___skind_51);
					}
				};

				___areExpressionsDefinedInStatement_52 = new BoaFunc_7()
				{
					@Override
					public Boolean invoke(final boa.types.Ast.Statement ___node) throws Exception
					{
						if ((___node.getExpressionsList() != null))
						{
							if (((long)___node.getExpressionsList().size()) > 0l)
							{
								return true;
							}
						}
						return false;
					}
				};

				___areExpressionsDefinedInExp_53 = new BoaFunc_8()
				{
					@Override
					public Boolean invoke(final boa.types.Ast.Expression ___node) throws Exception
					{
						if ((___node.getExpressionsList() != null))
						{
							if (((long)___node.getExpressionsList().size()) > 0l)
							{
								return true;
							}
						}
						return false;
					}
				};

				___isTfApi_54 = new BoaFunc_2()
				{
					@Override
					public Boolean invoke(final String ___api) throws Exception
					{
						if (boa.functions.BoaStringIntrinsics.match("tensorflow", ___api))
						{
							return true;
						}
						if (boa.functions.BoaStringIntrinsics.match("^tensorflow\\..*", ___api))
						{
							return true;
						}
						return false;
					}
				};

				___isKerasApi_55 = new BoaFunc_2()
				{
					@Override
					public Boolean invoke(final String ___api) throws Exception
					{
						if (boa.functions.BoaStringIntrinsics.match("keras", ___api))
						{
							return true;
						}
						if (boa.functions.BoaStringIntrinsics.match("^keras\\..*", ___api))
						{
							return true;
						}
						return false;
					}
				};

				___isTorchApi_56 = new BoaFunc_2()
				{
					@Override
					public Boolean invoke(final String ___api) throws Exception
					{
						if (boa.functions.BoaStringIntrinsics.match("torch", ___api))
						{
							return true;
						}
						if (boa.functions.BoaStringIntrinsics.match("^torch\\..*", ___api))
						{
							return true;
						}
						return false;
					}
				};

				___recordImpact_57 = new BoaFunc_9()
				{
					String[] ___vsst_58;

					@Override
					public void invoke(final String ___api) throws Exception
					{
						___moduleChanged_31 = true;
						for (long ___j = 0; ___j < ___dnnPackage_34.length; ___j++)
						{
							if ((___dnnPackage_34[(int)(___j)] != null))
							{
								{
									if (boa.functions.BoaStringIntrinsics.match("^" + ___dnnPackage_34[(int)(___j)] + ".*", ___api))
									{
										context.write(new boa.io.EmitKey("[" + (___dnnPackage_34[(int)(___j)]) + "]" + "[" + (___api) + "]", "impactList"), new boa.io.EmitValue(1l));
										return;
									}
								}
							}
						}

						for (long ___i = 0; ___i < ___otherPackage_35.length; ___i++)
						{
							if ((___otherPackage_35[(int)(___i)] != null))
							{
								{
									if (boa.functions.BoaStringIntrinsics.match("^" + ___otherPackage_35[(int)(___i)] + ".*", ___api))
									{
										___vsst_58 = boa.functions.BoaIntrinsics.basic_array(___currentModuleLibs_9.toArray(new String[0]));
										for (long ___j = 0; ___j < ___vsst_58.length; ___j++)
										{
											if ((___vsst_58[(int)(___j)] != null))
											{
												{
													context.write(new boa.io.EmitKey("[" + (___vsst_58[(int)(___j)]) + "]" + "[" + (___api) + "]", "impactList"), new boa.io.EmitValue(1l));
												}
											}
										}

										return;
									}
								}
							}
						}

					}
				};

				___libraryWiseCount_59 = new BoaFunc_10()
				{
					@Override
					public void invoke(final String ___api, final long ___id, final boolean ___impacted, final boolean ___forModule) throws Exception
					{
						if (___forModule == false && ___isApiCallSiteCounted_10.containsKey(___id))
						{
							return;
						}
						if (___forModule == false)
						{
							___isApiCallSiteCounted_10.put(___id, 1l);}
						if (___isKerasApi_55.invoke(___api))
						{
							if (___forModule)
							{
								____local_aggregator_kerasModuleCount = ____local_aggregator_kerasModuleCount + (1l);
								if (___impacted)
								{
									____local_aggregator_modifiedKerasModuleCount = ____local_aggregator_modifiedKerasModuleCount + (1l);
								}
							}
							else
							{
								____local_aggregator_kerasApiCallSiteCount = ____local_aggregator_kerasApiCallSiteCount + (1l);
								if (___impacted)
								{
									____local_aggregator_modifiedkerasApiCallSiteCount = ____local_aggregator_modifiedkerasApiCallSiteCount + (1l);
								}
							}
						}
						else
						{
							if (___isTfApi_54.invoke(___api))
							{
								if (___forModule)
								{
									____local_aggregator_tfModuleCount = ____local_aggregator_tfModuleCount + (1l);
									if (___impacted)
									{
										____local_aggregator_modifiedTfModuleCount = ____local_aggregator_modifiedTfModuleCount + (1l);
									}
								}
								else
								{
									____local_aggregator_tfApiCallSiteCount = ____local_aggregator_tfApiCallSiteCount + (1l);
									if (___impacted)
									{
										____local_aggregator_modifiedtfApiCallSiteCount = ____local_aggregator_modifiedtfApiCallSiteCount + (1l);
									}
								}
							}
							else
							{
								if (___isTorchApi_56.invoke(___api))
								{
									if (___forModule)
									{
										____local_aggregator_torchModuleCount = ____local_aggregator_torchModuleCount + (1l);
										if (___impacted)
										{
											____local_aggregator_modifiedTorchModuleCount = ____local_aggregator_modifiedTorchModuleCount + (1l);
										}
									}
									else
									{
										____local_aggregator_torchApiCallSiteCount = ____local_aggregator_torchApiCallSiteCount + (1l);
										if (___impacted)
										{
											____local_aggregator_modifiedtorchApiCallSiteCount = ____local_aggregator_modifiedtorchApiCallSiteCount + (1l);
										}
									}
								}
							}
						}
					}
				};

				___isProperAssignment_60 = new BoaFunc_8()
				{
					@Override
					public Boolean invoke(final boa.types.Ast.Expression ___node) throws Exception
					{
						if (!(___node != null))
						{
							return false;
						}
						if (!___isKindPrefix_50.invoke(___node, "ASSIGN"))
						{
							return false;
						}
						if (___areExpressionsDefinedInExp_53.invoke(___node) == false)
						{
							return false;
						}
						if (((long)___node.getExpressionsList().size()) != 2l)
						{
							return false;
						}
						return true;
					}
				};

				___resolveMethodScope_61 = new BoaFunc_4()
				{
					String ___scope_62;
					long ___l_63;
					long ___i_64;
					String ___tmp_65;

					@Override
					public String invoke(final String ___key) throws Exception
					{
						___scope_62 = ___getCurrentScope_44.invoke();
						___l_63 = ((long)boa.functions.BoaStringIntrinsics.splitall(___scope_62, "\\.").length);
						for (___i_64 = 0l; ___i_64 <= ___l_63; ___i_64++)
						{
							___tmp_65 = ___scope_62;
							if ("".equals(___scope_62))
							{
								___tmp_65 = ___key;
							}
							else
							{
								___tmp_65 = ___scope_62 + "." + ___key;
							}
							if (___methodMap_1.containsKey(___tmp_65))
							{
								return ___tmp_65;
							}
							___scope_62 = ___getParentScope_45.invoke(___scope_62);
						}
						return "";
					}
				};

				___addInAliasTrackingSet_66 = new BoaFunc_11()
				{
					String ___left_67;
					java.util.HashMap<String, java.util.HashSet<BoaTup_0>> ___tp1_68;
					BoaTup_0 ___t1_69;
					java.util.HashSet<BoaTup_0> ___tp2_70;

					@Override
					public void invoke(final String ___method_name, final String ___lft, final String ___right, final long ___leftId) throws Exception
					{
						___left_67 = ___lft;
						if ("".equals(___left_67) || "".equals(___right))
						{
							return;
						}
						if (___isClassScope_47.invoke() && !boa.functions.BoaStringIntrinsics.match("^self\\..*", ___left_67))
						{
							___left_67 = "self." + ___left_67;
						}
						if (!___aliasSet_33.containsKey(___method_name))
						{
							___tp1_68 = new java.util.HashMap<String, java.util.HashSet<BoaTup_0>>();
							___aliasSet_33.put(___method_name, ___tp1_68);}
						___t1_69 = new BoaTup_0(___leftId, ___right) 
						;
						if (!___aliasSet_33.get(___method_name).containsKey(___left_67))
						{
							___tp2_70 = new java.util.HashSet<BoaTup_0>();
							___tp2_70.add(___t1_69);
							___aliasSet_33.get(___method_name).put(___left_67, ___tp2_70);}
						else
						{
							___aliasSet_33.get(___method_name).get(___left_67).add(___t1_69);
						}
					}
				};

				___addInDeepTrackingSet_71 = new BoaFunc_12()
				{
					String ___identifierName_72;
					java.util.HashMap<String, java.util.HashSet<Long>> ___tp1_73;
					java.util.HashSet<Long> ___tp2_74;

					@Override
					public void invoke(final java.util.HashMap<String, java.util.HashMap<String, java.util.HashSet<Long>>> ___tmpMap, final String ___idName, final long ___id, final String ___method_name) throws Exception
					{
						___moduleChanged_31 = true;
						___identifierName_72 = ___idName;
						if (!"".equals(___identifierName_72) && !"_".equals(___identifierName_72))
						{
							if (___isClassScope_47.invoke() && !boa.functions.BoaStringIntrinsics.match("^self\\..*", ___identifierName_72))
							{
								___identifierName_72 = "self." + ___identifierName_72;
							}
							if (!___tmpMap.containsKey(___method_name))
							{
								___tp1_73 = new java.util.HashMap<String, java.util.HashSet<Long>>();
								___tmpMap.put(___method_name, ___tp1_73);}
							if (!___tmpMap.get(___method_name).containsKey(___identifierName_72))
							{
								___tp2_74 = new java.util.HashSet<Long>();
								___tp2_74.add(___id);
								___tmpMap.get(___method_name).put(___identifierName_72, ___tp2_74);}
							else
							{
								___tmpMap.get(___method_name).get(___identifierName_72).add(___id);
							}
						}
					}
				};

				___addInDeepTrackingSetFromArray_75 = new BoaFunc_13()
				{
					@Override
					public void invoke(final java.util.HashMap<String, java.util.HashMap<String, java.util.HashSet<Long>>> ___tmpMap, final String[] ___identifierName_72, final long ___id, final String ___method_name) throws Exception
					{
						for (long ___i_64 = 0; ___i_64 < ___identifierName_72.length; ___i_64++)
						{
							if ((___identifierName_72[(int)(___i_64)] != null))
							{
								{
									___addInDeepTrackingSet_71.invoke(___tmpMap, ___identifierName_72[(int)(___i_64)], ___id, ___method_name);
								}
							}
						}

					}
				};

				___isKindChange_76 = new BoaFunc_14()
				{
					@Override
					public Boolean invoke(final boa.types.Shared.ChangeKind ___node) throws Exception
					{
						if (!boa.types.Shared.ChangeKind.UNCHANGED.equals(___node) && !boa.types.Shared.ChangeKind.UNKNOWN.equals(___node) && !boa.types.Shared.ChangeKind.UNMAPPED.equals(___node))
						{
							return true;
						}
						return false;
					}
				};

				___isChangeKindModification_77 = new BoaFunc_8()
				{
					@Override
					public Boolean invoke(final boa.types.Ast.Expression ___node) throws Exception
					{
						if (___node.hasChange() == false)
						{
							return false;
						}
						if (___isKindChange_76.invoke(___node.getChange()) == false)
						{
							return false;
						}
						if (boa.types.Shared.ChangeKind.ADDED.equals(___node.getChange()))
						{
							return false;
						}
						return true;
					}
				};

				___expressionToStr_78 = new BoaFunc_15()
				{
					String ___str_79;

					@Override
					public String invoke(final boa.types.Ast.Expression ___node) throws Exception
					{
						___str_79 = "";
						if (boa.types.Ast.Expression.ExpressionKind.VARACCESS.equals(___node.getKind()))
						{
							if (___node.hasVariable())
							{
								___str_79 = ___node.getVariable();
							}
						}
						else
						{
							if (boa.types.Ast.Expression.ExpressionKind.LITERAL.equals(___node.getKind()))
							{
								if (___node.hasLiteral())
								{
									___str_79 = ___node.getLiteral();
								}
							}
							else
							{
								if (boa.types.Ast.Expression.ExpressionKind.METHODCALL.equals(___node.getKind()))
								{
									if (___node.hasMethod())
									{
										___str_79 = ___node.getMethod();
									}
								}
							}
						}
						return ___str_79;
					}
				};

				___expressionsToStr_80 = new BoaFunc_15()
				{
					String ___str_81;
					String ___tmp_82;

					@Override
					public String invoke(final boa.types.Ast.Expression ___node) throws Exception
					{
						___str_81 = "";
						___tmp_82 = "";
						for (long ___i_64 = 0; ___i_64 < ___node.getExpressionsList().size(); ___i_64++)
						{
							if ((___node.getExpressionsList().get((int)(___i_64)) != null))
							{
								{
									___tmp_82 = "";
									if (boa.types.Ast.Expression.ExpressionKind.ARRAYACCESS.equals(___node.getExpressionsList().get((int)(___i_64)).getKind()))
									{
										if ((___node.getExpressionsList().get((int)(___i_64)).getExpressionsList() != null))
										{
											if (((long)___node.getExpressionsList().get((int)(___i_64)).getExpressionsList().size()) > 0l)
											{
												___tmp_82 = ___expressionToStr_78.invoke(___node.getExpressionsList().get((int)(___i_64)).getExpressionsList().get((int)(0l)));
											}
										}
									}
									else
									{
										___tmp_82 = ___expressionToStr_78.invoke(___node.getExpressionsList().get((int)(___i_64)));
									}
									if (!"".equals(___tmp_82))
									{
										___str_81 = ___str_81 + "." + ___tmp_82;
									}
								}
							}
						}

						___tmp_82 = ___expressionToStr_78.invoke(___node);
						if (!"".equals(___tmp_82))
						{
							___str_81 = ___str_81 + "." + ___tmp_82;
						}
						if (!"".equals(___str_81))
						{
							___str_81 = boa.functions.BoaStringIntrinsics.substring(___str_81, 1l);
						}
						return ___str_81;
					}
				};

				___expressionsToStrUntil_83 = new BoaFunc_16()
				{
					String ___str_84;
					String ___tmp_85;
					long ___j_86;

					@Override
					public String invoke(final boa.types.Ast.Expression ___node, final long ___lm) throws Exception
					{
						___str_84 = "";
						___tmp_85 = "";
						___j_86 = 0l;
						for (long ___i_64 = 0; ___i_64 < ___node.getExpressionsList().size(); ___i_64++)
						{
							if ((___node.getExpressionsList().get((int)(___i_64)) != null))
							{
								{
									if (___j_86 > ___lm)
									{
										break;
									}
									___tmp_85 = "";
									if (boa.types.Ast.Expression.ExpressionKind.ARRAYACCESS.equals(___node.getExpressionsList().get((int)(___i_64)).getKind()))
									{
										if ((___node.getExpressionsList().get((int)(___i_64)).getExpressionsList() != null))
										{
											if (((long)___node.getExpressionsList().get((int)(___i_64)).getExpressionsList().size()) > 0l)
											{
												___tmp_85 = ___expressionToStr_78.invoke(___node.getExpressionsList().get((int)(___i_64)).getExpressionsList().get((int)(0l)));
											}
										}
									}
									else
									{
										___tmp_85 = ___expressionToStr_78.invoke(___node.getExpressionsList().get((int)(___i_64)));
									}
									if (!"".equals(___tmp_85))
									{
										___str_84 = ___str_84 + "." + ___tmp_85;
									}
									___j_86 = ___j_86 + 1l;
								}
							}
						}

						___tmp_85 = ___expressionToStr_78.invoke(___node);
						if (!"".equals(___tmp_85))
						{
							___str_84 = ___str_84 + "." + ___tmp_85;
						}
						if (!"".equals(___str_84))
						{
							___str_84 = boa.functions.BoaStringIntrinsics.substring(___str_84, 1l);
						}
						return ___str_84;
					}
				};

				___getVariableAsSet_87 = new BoaFunc_17()
				{
					String ___str_88;

					@Override
					public void invoke(final boa.types.Ast.Expression ___node, final java.util.HashSet<String> ___out) throws Exception
					{
						if (boa.types.Ast.Expression.ExpressionKind.TUPLE.equals(___node.getKind()) || boa.types.Ast.Expression.ExpressionKind.OTHER.equals(___node.getKind()))
						{
							for (long ___i_64 = 0; ___i_64 < ___node.getExpressionsList().size(); ___i_64++)
							{
								if ((___node.getExpressionsList().get((int)(___i_64)) != null))
								{
									{
										___getVariableAsSet_87.invoke(___node.getExpressionsList().get((int)(___i_64)), ___out);
									}
								}
							}

						}
						else
						{
							___str_88 = ___expressionsToStr_80.invoke(___node);
							if (!"".equals(___str_88))
							{
								___out.add(___str_88);
							}
						}
					}
				};

				___getVariableList_89 = new BoaFunc_18()
				{
					java.util.HashSet<String> ___st_90;
					String[] ___vs_91;

					@Override
					public String[] invoke(final boa.types.Ast.Expression ___node) throws Exception
					{
						___st_90 = new java.util.HashSet<String>();
						___getVariableAsSet_87.invoke(___node, ___st_90);
						___vs_91 = boa.functions.BoaIntrinsics.basic_array(___st_90.toArray(new String[0]));
						return ___vs_91;
					}
				};

				___hasBeenRedefinedAnywhere_92 = false;

				___isReachable_93 = new BoaFunc_19()
				{
					long ___cfgSourceid_94;
					long ___cfgTargetid_95;
					String ___scope_96;
					boa.graphs.cfg.CFG ___cfg_97;
					java.util.Stack<boa.graphs.cfg.CFGNode> ___st_98;
					boa.graphs.cfg.CFGNode[] ___v1_99;
					boolean ___pathfound_100;
					boa.graphs.cfg.CFGNode ___t_101;
					java.util.HashSet<String> ___vs_102;
					java.util.List<boa.graphs.cfg.CFGNode> ___v_103;
					long ___i_104;

					@Override
					public Boolean invoke(final long ___astSourceId, final long ___astTargetId, final String ___identifierName_72, final boolean ___isTargetCfgId) throws Exception
					{
						___visitedCfgNode_23.clear();
						if (!___idMap_24.containsKey(boa.functions.BoaCasts.longToString(___astSourceId)))
						{
							context.write(new boa.io.EmitKey("nodePrint"), new boa.io.EmitValue("Source ID not mapped: " + boa.functions.BoaCasts.longToString(___astSourceId) + ", Target ID: " + boa.functions.BoaCasts.longToString(___astTargetId) + " Project: " + ___p_0.getName() + ", Revision: " + ___revision_id_17 + " file: " + ___current_file_name_16 + ", method: " + ___current_method_name_15 + ", identifier: " + ___identifierName_72));
							return false;
						}
						if (___isTargetCfgId == false && !___idMap_24.containsKey(boa.functions.BoaCasts.longToString(___astTargetId)))
						{
							context.write(new boa.io.EmitKey("nodePrint"), new boa.io.EmitValue("Target ID not mapped: " + boa.functions.BoaCasts.longToString(___astTargetId) + ", Project: " + ___p_0.getName() + ", Revision: " + ___revision_id_17 + " file: " + ___current_file_name_16 + ", method: " + ___current_method_name_15 + ", identifier: " + ___identifierName_72));
							return false;
						}
						___cfgSourceid_94 = ___idMap_24.get(boa.functions.BoaCasts.longToString(___astSourceId));
						___cfgTargetid_95 = 0L;
						if (___isTargetCfgId)
						{
							___cfgTargetid_95 = ___astTargetId;
						}
						else
						{
							___cfgTargetid_95 = ___idMap_24.get(boa.functions.BoaCasts.longToString(___astTargetId));
						}
						___scope_96 = ___getCurrentScope_44.invoke();
						if (___acrossInIdMap_28.containsKey(___scope_96))
						{
							if (___acrossInIdMap_28.get(___scope_96).contains(___astSourceId))
							{
								___cfgSourceid_94 = 0l;
							}
						}
						if (___acrossInActive_29)
						{
							___scope_96 = ___getStringAfter_42.invoke(___scope_96, "-");
						}
						if (!___isCfgDefined_43.invoke(___scope_96))
						{
							return false;
						}
						___cfg_97 = ___cfgMap_25.get(___scope_96);
						___st_98 = new java.util.Stack<boa.graphs.cfg.CFGNode>();
						___v1_99 = boa.functions.BoaIntrinsics.basic_array(___cfg_97.getNodes().toArray(new boa.graphs.cfg.CFGNode[0]));
						for (long ___i_64 = 0; ___i_64 < ___v1_99.length; ___i_64++)
						{
							if ((___v1_99[(int)(___i_64)] != null))
							{
								{
									if (___v1_99[(int)(___i_64)].getId() == ___cfgSourceid_94)
									{
										___st_98.push(___v1_99[(int)(___i_64)]);
										break;
									}
								}
							}
						}

						___hasBeenRedefinedAnywhere_92 = false;
						___pathfound_100 = false;
						while (true)
						{
							___t_101 = boa.functions.BoaIntrinsics.stack_pop(___st_98);
							if ((___t_101 != null))
							{
								___visitedCfgNode_23.add(___t_101.getId());
								if (___t_101.getId() != ___cfgSourceid_94 && ___t_101.hasExpr())
								{
									if (___isKindPrefix_50.invoke(___t_101.getExpr(), "ASSIGN"))
									{
										if (___areExpressionsDefinedInExp_53.invoke(___t_101.getExpr()))
										{
											___vs_102 = new java.util.HashSet<String>();
											___getVariableAsSet_87.invoke(___t_101.getExpr().getExpressionsList().get((int)(0l)), ___vs_102);
											if (___vs_102.contains(___identifierName_72))
											{
												___hasBeenRedefinedAnywhere_92 = true;
												continue;
											}
										}
									}
								}
								if (___t_101.getId() == ___cfgTargetid_95)
								{
									___pathfound_100 = true;
								}
								___v_103 = ___t_101.getSuccessorsList();
								for (___i_104 = 0l; ___i_104 < ((long)___v_103.size()); ___i_104++)
								{
									if (___visitedCfgNode_23.contains(___v_103.get((int)(___i_104)).getId()) == false)
									{
										___st_98.push(___v_103.get((int)(___i_104)));
									}
								}
							}
							else
							{
								break;
							}
						}
						return ___pathfound_100;
					}
				};

				___isImpacted_105 = new BoaFunc_20()
				{
					String ___scope_106;
					long[] ___tmpImpactList_107;

					@Override
					public Boolean invoke(final String ___identifierName_72, final long ___targetId) throws Exception
					{
						if ("".equals(___identifierName_72))
						{
							return false;
						}
						___scope_106 = ___getCurrentScope_44.invoke();
						if (!___impactSet_32.containsKey(___scope_106))
						{
							return false;
						}
						if (!___impactSet_32.get(___scope_106).containsKey(___identifierName_72))
						{
							return false;
						}
						___tmpImpactList_107 = boa.functions.BoaIntrinsics.basic_array(___impactSet_32.get(___scope_106).get(___identifierName_72).toArray(new Long[0]));
						for (long ___i_104 = 0; ___i_104 < ___tmpImpactList_107.length; ___i_104++)
						{
							if (true)
							{
								{
									if (___isReachable_93.invoke(___tmpImpactList_107[(int)(___i_104)], ___targetId, ___identifierName_72, false))
									{
										if (___acrossInIdMap_28.containsKey(___scope_106))
										{
											if (___acrossInIdMap_28.get(___scope_106).contains(___tmpImpactList_107[(int)(___i_104)]) && ___hasBeenRedefinedAnywhere_92)
											{
												continue;
											}
										}
										return true;
									}
								}
							}
						}

						return false;
					}
				};

				___isSetImpacted_108 = new BoaFunc_21()
				{
					@Override
					public Boolean invoke(final String[] ___identifierName_72, final long ___targetId) throws Exception
					{
						for (long ___i_104 = 0; ___i_104 < ___identifierName_72.length; ___i_104++)
						{
							if ((___identifierName_72[(int)(___i_104)] != null))
							{
								{
									if (___isImpacted_105.invoke(___identifierName_72[(int)(___i_104)], ___targetId))
									{
										return true;
									}
								}
							}
						}

						return false;
					}
				};

				___copyOverForAcrossIn_109 = new BoaFunc_22()
				{
					String ___scope_110;
					String[] ___keyList_111;
					long[] ___tmpImpactList_112;
					java.util.HashSet<Long> ___s2_113;

					@Override
					public void invoke(final long ___targetAstId, final String ___calledMethodName, final boolean ___isTargetCfgId) throws Exception
					{
						___scope_110 = ___getCurrentScope_44.invoke();
						if (___acrossInActive_29)
						{
							___scope_110 = ___getStringUntil_41.invoke(___getStringAfter_42.invoke(___calledMethodName, "-"), ".");
						}
						if (!___impactSet_32.containsKey(___scope_110))
						{
							return;
						}
						___keyList_111 = boa.functions.BoaIntrinsics.basic_array(___impactSet_32.get(___scope_110).keySet().toArray(new String[0]));
						for (long ___i_104 = 0; ___i_104 < ___keyList_111.length; ___i_104++)
						{
							if ((___keyList_111[(int)(___i_104)] != null))
							{
								{
									___tmpImpactList_112 = boa.functions.BoaIntrinsics.basic_array(___impactSet_32.get(___scope_110).get(___keyList_111[(int)(___i_104)]).toArray(new Long[0]));
									for (long ___j_86 = 0; ___j_86 < ___tmpImpactList_112.length; ___j_86++)
									{
										if (true)
										{
											{
												if (___isReachable_93.invoke(___tmpImpactList_112[(int)(___j_86)], ___targetAstId, ___keyList_111[(int)(___i_104)], ___isTargetCfgId))
												{
													___addInDeepTrackingSet_71.invoke(___impactSet_32, ___keyList_111[(int)(___i_104)], ___tmpImpactList_112[(int)(___j_86)], ___calledMethodName);
													if (!___acrossInIdMap_28.containsKey(___calledMethodName))
													{
														___s2_113 = new java.util.HashSet<Long>();
														___s2_113.add(___tmpImpactList_112[(int)(___j_86)]);
														___acrossInIdMap_28.put(___calledMethodName, ___s2_113);}
													else
													{
														___acrossInIdMap_28.get(___calledMethodName).add(___tmpImpactList_112[(int)(___j_86)]);
													}
													break;
												}
											}
										}
									}

								}
							}
						}

					}
				};

				___getReachableAliasMappedName_114 = new BoaFunc_23()
				{
					String ___scope_115;
					BoaTup_0[] ___tmpImpactList_116;

					@Override
					public String invoke(final String ___usedIdentifierName, final long ___targetId) throws Exception
					{
						if ("".equals(___usedIdentifierName))
						{
							return "";
						}
						___scope_115 = ___getCurrentScope_44.invoke();
						if (!___aliasSet_33.containsKey(___scope_115))
						{
							return "";
						}
						if (!___aliasSet_33.get(___scope_115).containsKey(___usedIdentifierName))
						{
							return "";
						}
						___tmpImpactList_116 = boa.functions.BoaIntrinsics.basic_array(___aliasSet_33.get(___scope_115).get(___usedIdentifierName).toArray(new BoaTup_0[0]));
						for (long ___i_104 = 0; ___i_104 < ___tmpImpactList_116.length; ___i_104++)
						{
							if ((___tmpImpactList_116[(int)(___i_104)] != null))
							{
								{
									if (___isReachable_93.invoke(___tmpImpactList_116[(int)(___i_104)].___id, ___targetId, ___usedIdentifierName, false))
									{
										if (___acrossInIdMap_28.containsKey(___scope_115))
										{
											if (___acrossInIdMap_28.get(___scope_115).contains(___tmpImpactList_116[(int)(___i_104)].___id) && ___hasBeenRedefinedAnywhere_92)
											{
												continue;
											}
										}
										return ___tmpImpactList_116[(int)(___i_104)].___name;
									}
								}
							}
						}

						return "-";
					}
				};

				___copyOverAliasForAcrossIn_117 = new BoaFunc_22()
				{
					String ___scope_118;
					String[] ___keyList_119;
					BoaTup_0[] ___tmpImpactList_120;
					java.util.HashSet<Long> ___s2_121;

					@Override
					public void invoke(final long ___targetAstId, final String ___calledMethodName, final boolean ___isTargetCfgId) throws Exception
					{
						___scope_118 = ___getCurrentScope_44.invoke();
						if (___acrossInActive_29)
						{
							___scope_118 = ___getStringUntil_41.invoke(___getStringAfter_42.invoke(___scope_118, "-"), "\\.");
						}
						if (!___aliasSet_33.containsKey(___scope_118))
						{
							return;
						}
						___keyList_119 = boa.functions.BoaIntrinsics.basic_array(___aliasSet_33.get(___scope_118).keySet().toArray(new String[0]));
						for (long ___i_104 = 0; ___i_104 < ___keyList_119.length; ___i_104++)
						{
							if ((___keyList_119[(int)(___i_104)] != null))
							{
								{
									___tmpImpactList_120 = boa.functions.BoaIntrinsics.basic_array(___aliasSet_33.get(___scope_118).get(___keyList_119[(int)(___i_104)]).toArray(new BoaTup_0[0]));
									for (long ___j_86 = 0; ___j_86 < ___tmpImpactList_120.length; ___j_86++)
									{
										if ((___tmpImpactList_120[(int)(___j_86)] != null))
										{
											{
												if (___isReachable_93.invoke(___tmpImpactList_120[(int)(___j_86)].___id, ___targetAstId, ___keyList_119[(int)(___i_104)], ___isTargetCfgId))
												{
													___addInAliasTrackingSet_66.invoke(___calledMethodName, ___keyList_119[(int)(___i_104)], ___tmpImpactList_120[(int)(___j_86)].___name, ___tmpImpactList_120[(int)(___j_86)].___id);
													if (!___acrossInIdMap_28.containsKey(___calledMethodName))
													{
														___s2_121 = new java.util.HashSet<Long>();
														___s2_121.add(___tmpImpactList_120[(int)(___j_86)].___id);
														___acrossInIdMap_28.put(___calledMethodName, ___s2_121);}
													else
													{
														___acrossInIdMap_28.get(___calledMethodName).add(___tmpImpactList_120[(int)(___j_86)].___id);
													}
													break;
												}
											}
										}
									}

								}
							}
						}

					}
				};

				___copyOverMethod_122 = new BoaFunc_24()
				{
					String ___scope_123;
					long ___exitId_124;
					String ___nm_125;

					@Override
					public Boolean invoke(final boa.types.Ast.Method ___method) throws Exception
					{
						if (___method.hasName() == false)
						{
							return false;
						}
						___scope_123 = ___getCurrentScope_44.invoke();
						if (!___isCfgDefined_43.invoke(___scope_123))
						{
							return false;
						}
						___exitId_124 = ((long)___cfgMap_25.get(___scope_123).getNodes().size()) - 1l;
						___nm_125 = "";
						if (!"".equals(___scope_123))
						{
							___nm_125 = ___scope_123 + "." + ___method.getName();
						}
						else
						{
							___nm_125 = ___method.getName();
						}
						if (___impactSet_32.containsKey(___scope_123))
						{
							___copyOverForAcrossIn_109.invoke(___exitId_124, ___nm_125, true);
						}
						if (___aliasSet_33.containsKey(___scope_123))
						{
							___copyOverAliasForAcrossIn_117.invoke(___exitId_124, ___nm_125, true);
						}
						return true;
					}
				};

				___copyOverDeclaration_126 = new BoaFunc_25()
				{
					String ___scope_127;
					long ___exitId_128;
					String ___nm_129;

					@Override
					public Boolean invoke(final boa.types.Ast.Declaration ___method) throws Exception
					{
						if (___method.hasName() == false)
						{
							return false;
						}
						___scope_127 = ___getCurrentScope_44.invoke();
						if (!___isCfgDefined_43.invoke(___scope_127))
						{
							return false;
						}
						___exitId_128 = ((long)___cfgMap_25.get(___scope_127).getNodes().size()) - 1l;
						___nm_129 = "";
						if (!"".equals(___scope_127))
						{
							___nm_129 = ___scope_127 + "." + ___method.getName();
						}
						else
						{
							___nm_129 = ___method.getName();
						}
						if (___impactSet_32.containsKey(___scope_127))
						{
							___copyOverForAcrossIn_109.invoke(___exitId_128, ___nm_129, true);
						}
						if (___aliasSet_33.containsKey(___scope_127))
						{
							___copyOverAliasForAcrossIn_117.invoke(___exitId_128, ___nm_129, true);
						}
						return true;
					}
				};

				___isExpressionModified_130 = new BoaFunc_8()
				{
					@Override
					public Boolean invoke(final boa.types.Ast.Expression ___node) throws Exception
					{
						if (___node.hasChange())
						{
							if (___isKindChange_76.invoke(___node.getChange()))
							{
								return true;
							}
						}
						for (long ___i_104 = 0; ___i_104 < ___node.getExpressionsList().size(); ___i_104++)
						{
							if ((___node.getExpressionsList().get((int)(___i_104)) != null))
							{
								{
									if (___isExpressionModified_130.invoke(___node.getExpressionsList().get((int)(___i_104))))
									{
										return true;
									}
								}
							}
						}

						return false;
					}
				};

				___isExpressionImpacted_131 = new BoaFunc_8()
				{
					long ___i_132;

					@Override
					public Boolean invoke(final boa.types.Ast.Expression ___node) throws Exception
					{
						if (___isKindPrefix_50.invoke(___node, "ASSIGN"))
						{
							if ((___node.getExpressionsList() != null))
							{
								if (((long)___node.getExpressionsList().size()) == 2l)
								{
									return ___isExpressionImpacted_131.invoke(___node.getExpressionsList().get((int)(1l)));
								}
							}
							return false;
						}
						if (boa.types.Ast.Expression.ExpressionKind.LAMBDA.equals(___node.getKind()))
						{
							return false;
						}
						if (boa.types.Ast.Expression.ExpressionKind.FOR_LIST.equals(___node.getKind()))
						{
							if (((long)___node.getExpressionsList().size()) > 1l)
							{
								if (___isExpressionImpacted_131.invoke(___node.getExpressionsList().get((int)(1l))))
								{
									return true;
								}
							}
							return false;
						}
						if (boa.types.Ast.Expression.ExpressionKind.ARRAY_COMPREHENSION.equals(___node.getKind()))
						{
							for (___i_132 = 1l; ___i_132 < ((long)___node.getExpressionsList().size()); ___i_132++)
							{
								if (___isExpressionImpacted_131.invoke(___node.getExpressionsList().get((int)(___i_132))))
								{
									return true;
								}
							}
							return false;
						}
						if (boa.types.Ast.Expression.ExpressionKind.VARACCESS.equals(___node.getKind()))
						{
							if (___node.hasId() && ___isSetImpacted_108.invoke(___getVariableList_89.invoke(___node), ___node.getId()))
							{
								return true;
							}
						}
						if (boa.types.Ast.Expression.ExpressionKind.METHODCALL.equals(___node.getKind()))
						{
							if (boa.types.Ast.Expression.ExpressionKind.METHODCALL.equals(___node.getKind()))
							{
								for (long ___i_132 = 0; ___i_132 < ___node.getMethodArgsList().size(); ___i_132++)
								{
									if ((___node.getMethodArgsList().get((int)(___i_132)) != null))
									{
										{
											if (___isExpressionImpacted_131.invoke(___node.getMethodArgsList().get((int)(___i_132))))
											{
												return true;
											}
											break;
										}
									}
								}

							}
						}
						if (___areExpressionsDefinedInExp_53.invoke(___node))
						{
							for (long ___i_132 = 0; ___i_132 < ___node.getExpressionsList().size(); ___i_132++)
							{
								if ((___node.getExpressionsList().get((int)(___i_132)) != null))
								{
									{
										if (___isExpressionImpacted_131.invoke(___node.getExpressionsList().get((int)(___i_132))))
										{
											return true;
										}
									}
								}
							}

						}
						return false;
					}
				};

				___isVariableModified_133 = new BoaFunc_26()
				{
					@Override
					public Boolean invoke(final boa.types.Ast.Variable ___node) throws Exception
					{
						if (___node.hasChange())
						{
							if (___isKindChange_76.invoke(___node.getChange()))
							{
								return true;
							}
						}
						if (___node.hasComputedName())
						{
							if ((___node.getComputedName().getChange() != null))
							{
								if (___isKindChange_76.invoke(___node.getComputedName().getChange()))
								{
									return true;
								}
							}
						}
						if (___node.hasInitializer())
						{
							if (___isExpressionModified_130.invoke(___node.getInitializer()))
							{
								return true;
							}
						}
						return false;
					}
				};

				___getDeclaredVariableName_134 = new BoaFunc_27()
				{
					@Override
					public String invoke(final boa.types.Ast.Variable ___node) throws Exception
					{
						if (___node.hasName())
						{
							return ___node.getName();
						}
						if (___node.hasComputedName())
						{
							if ((___node.getComputedName().getVariable() != null))
							{
								return ___node.getComputedName().getVariable();
							}
						}
						return "";
					}
				};

				___getDeclaredVariables_135 = new BoaFunc_28()
				{
					java.util.HashSet<String> ___out_136;

					@Override
					public String[] invoke(final boa.types.Ast.Statement ___node) throws Exception
					{
						___out_136 = new java.util.HashSet<String>();
						for (long ___i_132 = 0; ___i_132 < ___node.getVariableDeclarationsList().size(); ___i_132++)
						{
							if ((___node.getVariableDeclarationsList().get((int)(___i_132)) != null))
							{
								{
									___out_136.add(___getDeclaredVariableName_134.invoke(___node.getVariableDeclarationsList().get((int)(___i_132))));
								}
							}
						}

						return boa.functions.BoaIntrinsics.basic_array(___out_136.toArray(new String[0]));
					}
				};

				___hasSelfArg_137 = new BoaFunc_24()
				{
					long ___l_138;

					@Override
					public Boolean invoke(final boa.types.Ast.Method ___m) throws Exception
					{
						if ((___m.getArgumentsList() != null) == false)
						{
							return false;
						}
						___l_138 = ((long)___m.getArgumentsList().size());
						if (___l_138 == 0l)
						{
							return false;
						}
						if ("self".equals(___getDeclaredVariableName_134.invoke(___m.getArgumentsList().get((int)(0l)))))
						{
							return true;
						}
						return false;
					}
				};

				___getNumberofMethodArgs_139 = new BoaFunc_29()
				{
					long ___l_140;

					@Override
					public Long invoke(final boa.types.Ast.Method ___m) throws Exception
					{
						if ((___m.getArgumentsList() != null) == false)
						{
							return 0l;
						}
						___l_140 = ((long)___m.getArgumentsList().size());
						if (___l_140 == 0l)
						{
							return 0l;
						}
						if ("self".equals(___getDeclaredVariableName_134.invoke(___m.getArgumentsList().get((int)(0l)))))
						{
							return ___l_140 - 1l;
						}
						return ___l_140;
					}
				};

				___constructAliasName_141 = new BoaFunc_30()
				{
					String[] ___tarr_142;
					String ___ret_143;
					String ___str_144;
					long ___pos_145;
					long ___i_146;
					long ___j_147;

					@Override
					public String invoke(final java.util.HashMap<String, String> ___mp, final String ___key) throws Exception
					{
						___tarr_142 = boa.functions.BoaStringIntrinsics.splitall(___key, "\\.");
						___ret_143 = "";
						___str_144 = "";
						___pos_145 = 0l;
						for (___i_146 = 0l; ___i_146 < ((long)___tarr_142.length); ___i_146++)
						{
							if (___i_146 == 0l)
							{
								___str_144 = ___tarr_142[(int)(___i_146)];
							}
							else
							{
								___str_144 = ___str_144 + "." + ___tarr_142[(int)(___i_146)];
							}
							if (___mp.containsKey(___str_144))
							{
								___ret_143 = ___mp.get(___str_144);
								___pos_145 = ___i_146;
							}
						}
						if ("".equals(___ret_143))
						{
							return "";
						}
						for (___j_147 = ___pos_145 + 1l; ___j_147 < ((long)___tarr_142.length); ___j_147++)
						{
							___ret_143 = ___ret_143 + "." + ___tarr_142[(int)(___j_147)];
						}
						return ___ret_143;
					}
				};

				___resolveAlias_148 = new BoaFunc_31()
				{
					String[] ___tarr_149;
					String ___str_150;
					String ___mt1_151;
					String ___ret_152;
					boolean ___objectResolved_153;
					long ___i_154;
					long ___j_155;
					long ___j_156;
					long ___j_157;

					@Override
					public String invoke(final String ___key, final long ___targetId, final boolean ___isResolveImport, final boolean ___isResolveMethodForJump, final java.util.HashMap<String, String> ___mp) throws Exception
					{
						___tarr_149 = boa.functions.BoaStringIntrinsics.splitall(___key, "\\.");
						if (((long)___tarr_149.length) > 0l)
						{
							if (___isResolveMethodForJump && "self".equals(___tarr_149[(int)(0l)]))
							{
								if (___acrossInActive_29)
								{
									___tarr_149[(int)(0l)] = ___getStringAfter_42.invoke(___getStringUntil_41.invoke(___getCurrentScope_44.invoke(), "."), "-");
								}
								else
								{
									___tarr_149[(int)(0l)] = ___getStringUntil_41.invoke(___getCurrentScope_44.invoke(), ".");
								}
							}
							___str_150 = "";
							___mt1_151 = "";
							___ret_152 = "";
							___objectResolved_153 = false;
							for (___i_154 = ((long)___tarr_149.length) - 1l; ___i_154 >= 0l; ___i_154--)
							{
								___str_150 = "";
								for (___j_155 = 0l; ___j_155 <= ___i_154; ___j_155++)
								{
									___str_150 = ___str_150 + "." + ___tarr_149[(int)(___j_155)];
								}
								___str_150 = boa.functions.BoaStringIntrinsics.substring(___str_150, 1l);
								___mt1_151 = ___getReachableAliasMappedName_114.invoke(___str_150, ___targetId);
								if ("-".equals(___mt1_151))
								{
									return "";
								}
								if (!"".equals(___mt1_151))
								{
									___str_150 = ___mt1_151;
									___objectResolved_153 = true;
									for (___j_156 = ___i_154 + 1l; ___j_156 < ((long)___tarr_149.length); ___j_156++)
									{
										___str_150 = ___str_150 + "." + ___tarr_149[(int)(___j_156)];
										if (___isResolveImport == false)
										{
											if (___mp.containsKey(___str_150) == false)
											{
												___objectResolved_153 = false;
											}
										}
									}
									if (___isResolveImport == false && ___objectResolved_153 == true)
									{
										return ___str_150;
									}
									if (___isResolveImport == false)
									{
										continue;
									}
									___mt1_151 = ___constructAliasName_141.invoke(___mp, ___str_150);
									if (!"".equals(___mt1_151) && !"-".equals(___mt1_151))
									{
										return ___mt1_151;
									}
								}
							}
							if (___isResolveImport == false)
							{
								___str_150 = "";
								___objectResolved_153 = true;
								for (___j_157 = 0l; ___j_157 < ((long)___tarr_149.length); ___j_157++)
								{
									if (___j_157 == 0l)
									{
										___str_150 = ___tarr_149[(int)(___j_157)];
									}
									else
									{
										___str_150 = ___str_150 + "." + ___tarr_149[(int)(___j_157)];
									}
									if (___mp.containsKey(___str_150) == false)
									{
										___objectResolved_153 = false;
									}
								}
								if (___objectResolved_153 == false)
								{
									return "";
								}
								return ___str_150;
							}
							return ___constructAliasName_141.invoke(___mp, ___key);
						}
						return "";
					}
				};

				___acrossInCallParameterMapping_158 = new BoaFunc_32()
				{
					long ___methodArgLen_159;
					long ___callArgLen_160;
					long ___j_161;
					String ___nextScope_162;

					@Override
					public Boolean invoke(final boa.types.Ast.Method ___targetMethod, final boa.types.Ast.Expression ___callSite, final String ___str_150) throws Exception
					{
						___methodArgLen_159 = ___getNumberofMethodArgs_139.invoke(___targetMethod);
						___callArgLen_160 = 0l;
						if ((___callSite.getMethodArgsList() != null))
						{
							if (((long)___callSite.getMethodArgsList().size()) > 0l)
							{
								if ((___callSite.getMethodArgsList().get((int)(0l)).getExpressionsList() != null))
								{
									___callArgLen_160 = ((long)___callSite.getMethodArgsList().get((int)(0l)).getExpressionsList().size());
								}
							}
						}
						if (___methodArgLen_159 != ___callArgLen_160)
						{
							return false;
						}
						___j_161 = 0l;
						if (___hasSelfArg_137.invoke(___targetMethod))
						{
							___j_161 = 1l;
						}
						___nextScope_162 = ___stackToStr_36.invoke(___acrossInContainingTypes_3, "-");
						if (!"".equals(___nextScope_162))
						{
							___nextScope_162 = ___nextScope_162 + "-" + ___str_150;
						}
						else
						{
							___nextScope_162 = ___str_150;
						}
						for (long ___i_154 = 0; ___i_154 < ___callSite.getMethodArgsList().get((int)(0l)).getExpressionsList().size(); ___i_154++)
						{
							if ((___callSite.getMethodArgsList().get((int)(0l)).getExpressionsList().get((int)(___i_154)) != null))
							{
								{
									if (___isProperAssignment_60.invoke(___callSite.getMethodArgsList().get((int)(0l)).getExpressionsList().get((int)(___i_154))))
									{
										if (___isExpressionModified_130.invoke(___callSite.getMethodArgsList().get((int)(0l)).getExpressionsList().get((int)(___i_154)).getExpressionsList().get((int)(1l))) || ___isExpressionImpacted_131.invoke(___callSite.getMethodArgsList().get((int)(0l)).getExpressionsList().get((int)(___i_154)).getExpressionsList().get((int)(1l))))
										{
											if (true)
											{
												___addInDeepTrackingSet_71.invoke(___impactSet_32, ___expressionToStr_78.invoke(___callSite.getMethodArgsList().get((int)(0l)).getExpressionsList().get((int)(___i_154)).getExpressionsList().get((int)(0l))), ___targetMethod.getArgumentsList().get((int)(___j_161)).getId(), ___nextScope_162);
											}
										}
									}
									else
									{
										if (___isExpressionModified_130.invoke(___callSite.getMethodArgsList().get((int)(0l)).getExpressionsList().get((int)(___i_154))) || ___isExpressionImpacted_131.invoke(___callSite.getMethodArgsList().get((int)(0l)).getExpressionsList().get((int)(___i_154))))
										{
											if (true)
											{
												___addInDeepTrackingSet_71.invoke(___impactSet_32, ___getDeclaredVariableName_134.invoke(___targetMethod.getArgumentsList().get((int)(___j_161))), ___targetMethod.getArgumentsList().get((int)(___j_161)).getId(), ___nextScope_162);
											}
										}
									}
									___j_161 = ___j_161 + 1l;
								}
							}
						}

						return true;
					}
				};

				___handleBinaryExpression_163 = new BoaFunc_33()
				{
					String ___scope_164;
					long ___rightLen_165;
					String ___str_166;
					String ___mt2_167;
					String ___str_168;
					String ___mt2_169;
					String ___str_170;
					String ___mt2_171;
					String ___str_172;
					String ___mt3_173;

					@Override
					public void invoke(final String[] ___leftVars, final boa.types.Ast.Expression ___right, final long ___nodeId) throws Exception
					{
						if (___isCallScope_49.invoke())
						{
							return;
						}
						___scope_164 = ___getCurrentScope_44.invoke();
						___rightLen_165 = 1l;
						if (boa.types.Ast.Expression.ExpressionKind.TUPLE.equals(___right.getKind()) || boa.types.Ast.Expression.ExpressionKind.OTHER.equals(___right.getKind()))
						{
							if (___areExpressionsDefinedInExp_53.invoke(___right))
							{
								___rightLen_165 = ((long)___right.getExpressionsList().size());
							}
						}
						if (!boa.types.Ast.Expression.ExpressionKind.METHODCALL.equals(___right.getKind()))
						{
							if (___rightLen_165 == 1l)
							{
								if (___isExpressionModified_130.invoke(___right) || ___isExpressionImpacted_131.invoke(___right))
								{
									___addInDeepTrackingSetFromArray_75.invoke(___impactSet_32, ___leftVars, ___nodeId, ___scope_164);
								}
							}
							else
							{
								if (___rightLen_165 == ((long)___leftVars.length))
								{
									for (long ___i_154 = 0; ___i_154 < ___leftVars.length; ___i_154++)
									{
										if ((___leftVars[(int)(___i_154)] != null))
										{
											{
												if (boa.types.Ast.Expression.ExpressionKind.METHODCALL.equals(___right.getExpressionsList().get((int)(___i_154)).getKind()))
												{
													___str_166 = ___expressionsToStr_80.invoke(___right.getExpressionsList().get((int)(___i_154)));
													___mt2_167 = "conservative";
													if (!"".equals(___mt2_167))
													{
														if (___isExpressionModified_130.invoke(___right.getExpressionsList().get((int)(___i_154))) || ___isExpressionImpacted_131.invoke(___right.getExpressionsList().get((int)(___i_154))))
														{
															___addInDeepTrackingSet_71.invoke(___impactSet_32, ___leftVars[(int)(___i_154)], ___nodeId, ___scope_164);
														}
													}
												}
												else
												{
													if ((___isExpressionModified_130.invoke(___right.getExpressionsList().get((int)(___i_154))) || ___isExpressionImpacted_131.invoke(___right.getExpressionsList().get((int)(___i_154)))))
													{
														___addInDeepTrackingSet_71.invoke(___impactSet_32, ___leftVars[(int)(___i_154)], ___nodeId, ___scope_164);
													}
												}
											}
										}
									}

								}
							}
						}
						if (boa.types.Ast.Expression.ExpressionKind.METHODCALL.equals(___right.getKind()))
						{
							___str_168 = ___expressionsToStr_80.invoke(___right);
							___mt2_169 = "conservative";
							if (!"".equals(___mt2_169))
							{
								if (___isExpressionModified_130.invoke(___right) || ___isExpressionImpacted_131.invoke(___right))
								{
									___addInDeepTrackingSetFromArray_75.invoke(___impactSet_32, ___leftVars, ___nodeId, ___scope_164);
								}
							}
						}
						if (true)
						{
							if (___rightLen_165 == 1l)
							{
								if (boa.types.Ast.Expression.ExpressionKind.METHODCALL.equals(___right.getKind()) || boa.types.Ast.Expression.ExpressionKind.VARACCESS.equals(___right.getKind()))
								{
									___str_170 = ___expressionsToStr_80.invoke(___right);
									___mt2_171 = ___resolveAlias_148.invoke(___str_170, ___nodeId, true, false, ___dnnVariableMap_5);
									if ("".equals(___mt2_171))
									{
										___mt2_171 = ___resolveAlias_148.invoke(___str_170, ___nodeId, false, false, ___objectMap_6);
										if (!"".equals(___mt2_171))
										{
											___str_170 = ___mt2_171;
										}
										___mt2_171 = ___resolveMethodScope_61.invoke(___str_170);
									}
									if (!"".equals(___mt2_171))
									{
										for (long ___i_154 = 0; ___i_154 < ___leftVars.length; ___i_154++)
										{
											if ((___leftVars[(int)(___i_154)] != null))
											{
												{
													if (!"_".equals(___leftVars[(int)(___i_154)]) && !".".equals(___leftVars[(int)(___i_154)]) && !"".equals(___leftVars[(int)(___i_154)]))
													{
														___addInAliasTrackingSet_66.invoke(___scope_164, ___leftVars[(int)(___i_154)], ___mt2_171, ___nodeId);
													}
												}
											}
										}

									}
								}
							}
							else
							{
								if (___rightLen_165 == ((long)___leftVars.length))
								{
									for (long ___i_154 = 0; ___i_154 < ___leftVars.length; ___i_154++)
									{
										if ((___leftVars[(int)(___i_154)] != null))
										{
											{
												if (!"_".equals(___leftVars[(int)(___i_154)]) && !".".equals(___leftVars[(int)(___i_154)]) && !"".equals(___leftVars[(int)(___i_154)]))
												{
													if (!boa.types.Ast.Expression.ExpressionKind.METHODCALL.equals(___right.getExpressionsList().get((int)(___i_154)).getKind()) && !boa.types.Ast.Expression.ExpressionKind.VARACCESS.equals(___right.getExpressionsList().get((int)(___i_154)).getKind()))
													{
														continue;
													}
													___str_172 = ___expressionsToStr_80.invoke(___right.getExpressionsList().get((int)(___i_154)));
													___mt3_173 = ___resolveAlias_148.invoke(___str_172, ___nodeId, true, false, ___dnnVariableMap_5);
													if ("".equals(___mt3_173))
													{
														___mt3_173 = ___resolveAlias_148.invoke(___str_172, ___nodeId, false, false, ___objectMap_6);
														if (!"".equals(___mt3_173))
														{
															___str_172 = ___mt3_173;
														}
														___mt3_173 = ___resolveMethodScope_61.invoke(___str_172);
													}
													if (!"".equals(___mt3_173))
													{
														___addInAliasTrackingSet_66.invoke(___scope_164, ___leftVars[(int)(___i_154)], ___mt3_173, ___nodeId);
													}
												}
											}
										}
									}

								}
							}
						}
					}
				};

				___handleMethodCall_174 = new BoaFunc_34()
				{
					String ___str_175;
					String ___mt2_176;
					long ___vid_177;
					BoaTup_0 ___trt1_178;

					@Override
					public Long invoke(final boa.types.Ast.Expression ___node) throws Exception
					{
						if (___node.hasId())
						{
							___str_175 = ___expressionsToStr_80.invoke(___node);
							___mt2_176 = ___resolveAlias_148.invoke(___str_175, ___node.getId(), true, false, ___dnnVariableMap_5);
							if (!"".equals(___mt2_176))
							{
								___overallApiSet_12.add(___mt2_176);
								___vid_177 = ___node.getId();
								___trt1_178 = new BoaTup_0(___vid_177, ___mt2_176) 
								;
								___allDNNApi_8.add(___trt1_178);
								if (___isExpressionModified_130.invoke(___node) || ___isExpressionImpacted_131.invoke(___node))
								{
									___recordImpact_57.invoke(___mt2_176);
									___libraryWiseCount_59.invoke(___mt2_176, ___node.getId(), true, false);
									return 1l;
								}
								return 2l;
							}
						}
						return 0l;
					}
				};

				___preProcessAcrossIn_179 = new BoaFunc_35()
				{
					boa.types.Ast.Expression ___node_180;
					String ___str_181;
					long ___lt_182;
					String ___tstr_183;
					String ___tmp_184;
					long ___i_185;
					String ___nm_186;

					@Override
					public String invoke(final boa.types.Ast.Expression ___mainNode, final boolean ___isCallback) throws Exception
					{
						___node_180 = ___mainNode;
						if (___isKindPrefix_50.invoke(___mainNode, "ASSIGN"))
						{
							___node_180 = ___node_180.getExpressionsList().get((int)(1l));
						}
						if (___enableAcrossIn_26)
						{
							___str_181 = ___expressionsToStr_80.invoke(___node_180);
							___lt_182 = 1l;
							if (___isClassScope_47.invoke())
							{
								___lt_182 = 2l;
							}
							___tstr_183 = ___str_181;
							___tmp_184 = null;
							for (___i_185 = 0l; ___i_185 < ___lt_182; ___i_185++)
							{
								if (___i_185 == 1l)
								{
									___tmp_184 = ___resolveAlias_148.invoke(___tstr_183, ___node_180.getId(), false, false, ___objectMap_6);
								}
								else
								{
									___tmp_184 = ___resolveAlias_148.invoke(___tstr_183, ___node_180.getId(), false, true, ___objectMap_6);
								}
								if ("".equals(___tmp_184))
								{
									___tmp_184 = ___tstr_183;
								}
								___tmp_184 = ___resolveMethodScope_61.invoke(___tmp_184);
								if (!"".equals(___tmp_184))
								{
									___str_181 = ___tmp_184;
									break;
								}
								___tstr_183 = "self." + ___tstr_183;
							}
							if (!"".equals(___str_181) && ___currentCallDepth_21 < ___maximumCallDepth_27 && ___methodMap_1.containsKey(___str_181) && !___visited_20.containsKey(___str_181))
							{
								___visited_20.put(___str_181, 1l);___currentCallDepth_21 = ___currentCallDepth_21 + 1l;
								if (___isCallback || ___acrossInCallParameterMapping_158.invoke(___methodMap_1.get(___str_181), ___node_180, ___str_181))
								{
									___nm_186 = ___stackToStr_36.invoke(___acrossInContainingTypes_3, "-") + "-" + ___str_181;
									if (___isCallback == false)
									{
										___copyOverForAcrossIn_109.invoke(___node_180.getId(), ___nm_186, false);
										___copyOverAliasForAcrossIn_117.invoke(___node_180.getId(), ___nm_186, false);
									}
									___acrossInContainingTypes_3.push(___str_181);
									return ___str_181;
								}
								return "-";
							}
						}
						return "";
					}
				};

				___postProcessAcrosIn_187 = new BoaFunc_34()
				{
					long ___retStatus_188;
					boa.types.Ast.Expression ___node_189;
					String ___curScope_190;
					String ___str_191;

					@Override
					public Long invoke(final boa.types.Ast.Expression ___mainNode) throws Exception
					{
						___retStatus_188 = 0l;
						___node_189 = ___mainNode;
						___curScope_190 = ___stackToStr_36.invoke(___acrossInContainingTypes_3, "-");
						boa.functions.BoaIntrinsics.stack_pop(___acrossInContainingTypes_3);
						if (___returnImpacted_30.contains(___curScope_190))
						{
							___retStatus_188 = 2l;
						}
						if (___isProperAssignment_60.invoke(___mainNode))
						{
							___node_189 = ___node_189.getExpressionsList().get((int)(1l));
							if (___returnImpacted_30.contains(___curScope_190))
							{
								if (true)
								{
									___addInDeepTrackingSetFromArray_75.invoke(___impactSet_32, ___getVariableList_89.invoke(___mainNode.getExpressionsList().get((int)(0l))), ___mainNode.getExpressionsList().get((int)(0l)).getId(), ___stackToStr_36.invoke(___acrossInContainingTypes_3, "-"));
								}
							}
						}
						if (___retStatus_188 == 2l)
						{
							___returnImpacted_30.remove(___curScope_190);
						}
						___str_191 = ___expressionsToStr_80.invoke(___node_189);
						___currentCallDepth_21 = ___currentCallDepth_21 - 1l;
						___visited_20.remove(___str_191);
						return ___retStatus_188;
					}
				};

				___acrossInVisitor_192 = new boa.runtime.BoaAbstractVisitor()
				{
					String ___nm_193;
					String[] ___dvars_194;
					String ___tmpA_195;
					long ___ht_196;
					long ___exmcType_197;
					String ___tmpA_198;
					String ___mt2_199;
					String ___tmpA_200;

					@Override
					protected boolean preVisit(final boa.types.Ast.Method ___node_189) throws Exception
					{
						___namespaceStack_13.push("method");
						return true;
					}

					@Override
					protected void postVisit(final boa.types.Ast.Method ___node_189) throws Exception
					{
						boa.functions.BoaIntrinsics.stack_pop(___namespaceStack_13);
						___nm_193 = ___stackToStr_36.invoke(___acrossInContainingTypes_3, "-");
						if (___impactSet_32.containsKey(___nm_193))
						{
							___impactSet_32.remove(___nm_193);
						}
						if (___acrossInIdMap_28.containsKey(___nm_193))
						{
							___acrossInIdMap_28.remove(___nm_193);
						}
						if (___aliasSet_33.containsKey(___nm_193))
						{
							___aliasSet_33.remove(___nm_193);
						}
					}

					@Override
					protected boolean preVisit(final boa.types.Ast.Modifier ___node_189) throws Exception
					{
						if (boa.types.Ast.Modifier.ModifierKind.ANNOTATION.equals(___node_189.getKind()))
						{
							return false;
						}
						return true;
					}

					@Override
					protected boolean preVisit(final boa.types.Ast.Statement ___node_189) throws Exception
					{
						if (boa.types.Ast.Statement.StatementKind.ASSERT.equals(___node_189.getKind()))
						{
							return false;
						}
						___dvars_194 = null;
						if (boa.types.Ast.Statement.StatementKind.WITH.equals(___node_189.getKind()) || boa.types.Ast.Statement.StatementKind.FOREACH.equals(___node_189.getKind()))
						{
							if ((___node_189.getVariableDeclarationsList() != null))
							{
								if (___areExpressionsDefinedInStatement_52.invoke(___node_189))
								{
									if (___node_189.hasId())
									{
										___handleBinaryExpression_163.invoke(___getDeclaredVariables_135.invoke(___node_189), ___node_189.getExpressionsList().get((int)(0l)), ___node_189.getId());
									}
								}
							}
						}
						if (___enableAcrossIn_26 && (boa.types.Ast.Statement.StatementKind.RETURN.equals(___node_189.getKind())))
						{
							if (___areExpressionsDefinedInStatement_52.invoke(___node_189))
							{
								if (___isExpressionModified_130.invoke(___node_189.getExpressionsList().get((int)(0l))) || ___isExpressionImpacted_131.invoke(___node_189.getExpressionsList().get((int)(0l))))
								{
									___returnImpacted_30.add(___stackToStr_36.invoke(___acrossInContainingTypes_3, "-"));
								}
							}
						}
						return true;
					}

					@Override
					protected boolean preVisit(final boa.types.Ast.Expression ___node_189) throws Exception
					{
						if (___enableAcrossIn_26 && (boa.types.Ast.Expression.ExpressionKind.YIELD.equals(___node_189.getKind())))
						{
							if (___areExpressionsDefinedInExp_53.invoke(___node_189))
							{
								if (___isExpressionModified_130.invoke(___node_189.getExpressionsList().get((int)(0l))) || ___isExpressionImpacted_131.invoke(___node_189.getExpressionsList().get((int)(0l))))
								{
									___returnImpacted_30.add(___stackToStr_36.invoke(___acrossInContainingTypes_3, "-"));
								}
							}
						}
						if (___isProperAssignment_60.invoke(___node_189))
						{
							if (___node_189.hasId())
							{
								___handleBinaryExpression_163.invoke(___getVariableList_89.invoke(___node_189.getExpressionsList().get((int)(0l))), ___node_189.getExpressionsList().get((int)(1l)), ___node_189.getId());
							}
							if (___enableAcrossIn_26)
							{
								if (boa.types.Ast.Expression.ExpressionKind.METHODCALL.equals(___node_189.getExpressionsList().get((int)(1l)).getKind()))
								{
									___handleMethodCall_174.invoke(___node_189.getExpressionsList().get((int)(1l)));
									___tmpA_195 = ___preProcessAcrossIn_179.invoke(___node_189, false);
									if (!"".equals(___tmpA_195))
									{
										if (!"-".equals(___tmpA_195))
										{
											visit(___methodMap_1.get(___tmpA_195));
										}
										___postProcessAcrosIn_187.invoke(___node_189);
									}
								}
							}
						}
						if (boa.types.Ast.Expression.ExpressionKind.METHODCALL.equals(___node_189.getKind()))
						{
							___ht_196 = ___handleMethodCall_174.invoke(___node_189);
							if (___ht_196 == 2l)
							{
								___exmcType_197 = 0l;
								for (long ___j_161 = 0; ___j_161 < ___node_189.getMethodArgsList().get((int)(0l)).getExpressionsList().size(); ___j_161++)
								{
									if ((___node_189.getMethodArgsList().get((int)(0l)).getExpressionsList().get((int)(___j_161)) != null))
									{
										{
											___exmcType_197 = 0l;
											if (boa.types.Ast.Expression.ExpressionKind.METHODCALL.equals(___node_189.getMethodArgsList().get((int)(0l)).getExpressionsList().get((int)(___j_161)).getKind()))
											{
												___exmcType_197 = 1l;
											}
											else
											{
												if (boa.types.Ast.Expression.ExpressionKind.VARACCESS.equals(___node_189.getMethodArgsList().get((int)(0l)).getExpressionsList().get((int)(___j_161)).getKind()))
												{
													___exmcType_197 = 2l;
												}
												else
												{
													if (___isProperAssignment_60.invoke(___node_189.getMethodArgsList().get((int)(0l)).getExpressionsList().get((int)(___j_161))))
													{
														if (boa.types.Ast.Expression.ExpressionKind.METHODCALL.equals(___node_189.getMethodArgsList().get((int)(0l)).getExpressionsList().get((int)(___j_161)).getExpressionsList().get((int)(1l)).getKind()))
														{
															___exmcType_197 = 1l;
														}
														else
														{
															if (boa.types.Ast.Expression.ExpressionKind.VARACCESS.equals(___node_189.getMethodArgsList().get((int)(0l)).getExpressionsList().get((int)(___j_161)).getExpressionsList().get((int)(1l)).getKind()))
															{
																___exmcType_197 = 2l;
															}
														}
													}
												}
											}
											if (___exmcType_197 != 0l)
											{
												___tmpA_198 = ___preProcessAcrossIn_179.invoke(___node_189.getMethodArgsList().get((int)(0l)).getExpressionsList().get((int)(___j_161)), (___exmcType_197 == 2l));
												if (!"".equals(___tmpA_198))
												{
													if (!"-".equals(___tmpA_198))
													{
														visit(___methodMap_1.get(___tmpA_198));
													}
													if (___postProcessAcrosIn_187.invoke(___node_189.getMethodArgsList().get((int)(0l)).getExpressionsList().get((int)(___j_161))) == 2l)
													{
														___mt2_199 = ___resolveAlias_148.invoke(___expressionsToStr_80.invoke(___node_189), ___node_189.getId(), true, false, ___dnnVariableMap_5);
														___recordImpact_57.invoke(___mt2_199);
														___libraryWiseCount_59.invoke(___mt2_199, ___node_189.getId(), true, false);
														break;
													}
												}
											}
										}
									}
								}

							}
							else
							{
								if (___ht_196 == 0l)
								{
									___tmpA_200 = ___preProcessAcrossIn_179.invoke(___node_189, false);
									if (!"".equals(___tmpA_200))
									{
										if (!"-".equals(___tmpA_200))
										{
											visit(___methodMap_1.get(___tmpA_200));
										}
										___postProcessAcrosIn_187.invoke(___node_189);
									}
								}
							}
							___statementStack_14.push("call");
						}
						return true;
					}

					@Override
					protected void postVisit(final boa.types.Ast.Expression ___node_189) throws Exception
					{
						if (boa.types.Ast.Expression.ExpressionKind.METHODCALL.equals(___node_189.getKind()))
						{
							boa.functions.BoaIntrinsics.stack_pop(___statementStack_14);
						}
					}

				};

				___makeJumpAcrossIn_201 = new BoaFunc_36()
				{
					boa.types.Ast.Expression ___node_202;
					long ___retStatus_203;
					String ___str_204;
					long ___lt_205;
					String ___tstr_206;
					String ___tmp_207;
					long ___i_208;
					String ___nm_209;

					@Override
					public Long invoke(final boa.types.Ast.Expression ___mainNode, final boolean ___isCallback) throws Exception
					{
						___node_202 = ___mainNode;
						if (___isProperAssignment_60.invoke(___mainNode))
						{
							___node_202 = ___node_202.getExpressionsList().get((int)(1l));
						}
						___retStatus_203 = 0l;
						if (___enableAcrossIn_26)
						{
							___str_204 = ___expressionsToStr_80.invoke(___node_202);
							___lt_205 = 1l;
							if (___isClassScope_47.invoke())
							{
								___lt_205 = 2l;
							}
							___tstr_206 = ___str_204;
							___tmp_207 = null;
							for (___i_208 = 0l; ___i_208 < ___lt_205; ___i_208++)
							{
								if (___i_208 == 1l)
								{
									___tmp_207 = ___resolveAlias_148.invoke(___tstr_206, ___node_202.getId(), false, false, ___objectMap_6);
								}
								else
								{
									___tmp_207 = ___resolveAlias_148.invoke(___tstr_206, ___node_202.getId(), false, true, ___objectMap_6);
								}
								if ("".equals(___tmp_207))
								{
									___tmp_207 = ___tstr_206;
								}
								___tmp_207 = ___resolveMethodScope_61.invoke(___tmp_207);
								if (!"".equals(___tmp_207))
								{
									___str_204 = ___tmp_207;
									break;
								}
								___tstr_206 = "self." + ___tstr_206;
							}
							if (!"".equals(___str_204) && ___currentCallDepth_21 < ___maximumCallDepth_27 && ___methodMap_1.containsKey(___str_204) && !___visited_20.containsKey(___str_204))
							{
								___visited_20.put(___str_204, 1l);___nm_209 = ___stackToStr_36.invoke(___containingTypes_2, ".");
								___acrossInContainingTypes_3.push(___nm_209);
								if (___isCallback || ___acrossInCallParameterMapping_158.invoke(___methodMap_1.get(___str_204), ___node_202, ___str_204))
								{
									___acrossInActive_29 = true;
									___nm_209 = ___stackToStr_36.invoke(___acrossInContainingTypes_3, "-") + "-" + ___str_204;
									if (___isCallback == false)
									{
										___copyOverForAcrossIn_109.invoke(___node_202.getId(), ___nm_209, false);
										___copyOverAliasForAcrossIn_117.invoke(___node_202.getId(), ___nm_209, false);
									}
									___acrossInContainingTypes_3.push(___str_204);
									___currentCallDepth_21 = 0l;
									___retStatus_203 = 1l;
									___acrossInVisitor_192.visit(___methodMap_1.get(___str_204));
									if (___returnImpacted_30.contains(___stackToStr_36.invoke(___acrossInContainingTypes_3, "-")))
									{
										___retStatus_203 = 2l;
									}
									if (___isProperAssignment_60.invoke(___mainNode) && ___returnImpacted_30.contains(___stackToStr_36.invoke(___acrossInContainingTypes_3, "-")))
									{
										if (true)
										{
											___addInDeepTrackingSetFromArray_75.invoke(___impactSet_32, ___getVariableList_89.invoke(___mainNode.getExpressionsList().get((int)(0l))), ___mainNode.getExpressionsList().get((int)(0l)).getId(), ___stackToStr_36.invoke(___containingTypes_2, "."));
										}
									}
									___acrossInActive_29 = false;
								}
								___returnImpacted_30.clear();
								___acrossInContainingTypes_3.clear();
								___visited_20.remove(___str_204);
							}
						}
						return ___retStatus_203;
					}
				};

				___impactDetectionVisitor_210 = new boa.runtime.BoaAbstractVisitor()
				{
					long ___ht_211;
					long ___exmcType_212;
					String ___mt2_213;

					@Override
					protected boolean preVisit(final boa.types.Ast.Namespace ___node_202) throws Exception
					{
						___current_file_name_16 = ___node_202.getName();
						___namespaceStack_13.push("global");
						___current_method_name_15 = "";
						for (long ___i_208 = 0; ___i_208 < ___node_202.getStatementsList().size(); ___i_208++)
						{
							if ((___node_202.getStatementsList().get((int)(___i_208)) != null))
							{
								{
									visit(___node_202.getStatementsList().get((int)(___i_208)));
								}
							}
						}

						for (long ___i_208 = 0; ___i_208 < ___node_202.getMethodsList().size(); ___i_208++)
						{
							if ((___node_202.getMethodsList().get((int)(___i_208)) != null))
							{
								{
									___copyOverMethod_122.invoke(___node_202.getMethodsList().get((int)(___i_208)));
									visit(___node_202.getMethodsList().get((int)(___i_208)));
								}
							}
						}

						for (long ___i_208 = 0; ___i_208 < ___node_202.getDeclarationsList().size(); ___i_208++)
						{
							if ((___node_202.getDeclarationsList().get((int)(___i_208)) != null))
							{
								{
									___copyOverDeclaration_126.invoke(___node_202.getDeclarationsList().get((int)(___i_208)));
									visit(___node_202.getDeclarationsList().get((int)(___i_208)));
								}
							}
						}

						boa.functions.BoaIntrinsics.stack_pop(___namespaceStack_13);
						return false;
					}

					@Override
					protected boolean preVisit(final boa.types.Ast.Declaration ___node_202) throws Exception
					{
						___containingTypes_2.push(___node_202.getName());
						___namespaceStack_13.push("class");
						for (long ___i_208 = 0; ___i_208 < ___node_202.getStatementsList().size(); ___i_208++)
						{
							if ((___node_202.getStatementsList().get((int)(___i_208)) != null))
							{
								{
									visit(___node_202.getStatementsList().get((int)(___i_208)));
								}
							}
						}

						for (long ___i_208 = 0; ___i_208 < ___node_202.getMethodsList().size(); ___i_208++)
						{
							if ((___node_202.getMethodsList().get((int)(___i_208)) != null))
							{
								{
									___copyOverMethod_122.invoke(___node_202.getMethodsList().get((int)(___i_208)));
								}
							}
						}

						for (long ___i_208 = 0; ___i_208 < ___node_202.getNestedDeclarationsList().size(); ___i_208++)
						{
							if ((___node_202.getNestedDeclarationsList().get((int)(___i_208)) != null))
							{
								{
									___copyOverDeclaration_126.invoke(___node_202.getNestedDeclarationsList().get((int)(___i_208)));
								}
							}
						}

						return true;
					}

					@Override
					protected void postVisit(final boa.types.Ast.Declaration ___node_202) throws Exception
					{
						boa.functions.BoaIntrinsics.stack_pop(___containingTypes_2);
						boa.functions.BoaIntrinsics.stack_pop(___namespaceStack_13);
					}

					@Override
					protected boolean preVisit(final boa.types.Ast.Method ___node_202) throws Exception
					{
						___namespaceStack_13.push("method");
						___containingTypes_2.push(___node_202.getName());
						___current_method_name_15 = ___stackToStr_36.invoke(___containingTypes_2, ".");
						if ((___node_202.getArgumentsList() != null))
						{
							for (long ___kk = 0; ___kk < ___node_202.getArgumentsList().size(); ___kk++)
							{
								if ((___node_202.getArgumentsList().get((int)(___kk)) != null))
								{
									{
										if (___isVariableModified_133.invoke(___node_202.getArgumentsList().get((int)(___kk))) && true)
										{
											___addInDeepTrackingSet_71.invoke(___impactSet_32, ___getDeclaredVariableName_134.invoke(___node_202.getArgumentsList().get((int)(___kk))), ___node_202.getArgumentsList().get((int)(___kk)).getId(), ___current_method_name_15);
										}
									}
								}
							}

						}
						return true;
					}

					@Override
					protected void postVisit(final boa.types.Ast.Method ___node_202) throws Exception
					{
						boa.functions.BoaIntrinsics.stack_pop(___namespaceStack_13);
						boa.functions.BoaIntrinsics.stack_pop(___containingTypes_2);
						___current_method_name_15 = ___stackToStr_36.invoke(___containingTypes_2, ".");
					}

					@Override
					protected boolean preVisit(final boa.types.Ast.Modifier ___node_202) throws Exception
					{
						if (boa.types.Ast.Modifier.ModifierKind.ANNOTATION.equals(___node_202.getKind()))
						{
							return false;
						}
						return true;
					}

					@Override
					protected boolean preVisit(final boa.types.Ast.Statement ___node_202) throws Exception
					{
						if (boa.types.Ast.Statement.StatementKind.WITH.equals(___node_202.getKind()) || boa.types.Ast.Statement.StatementKind.FOREACH.equals(___node_202.getKind()))
						{
							if ((___node_202.getVariableDeclarationsList() != null))
							{
								if (___areExpressionsDefinedInStatement_52.invoke(___node_202))
								{
									if (___node_202.hasId())
									{
										___handleBinaryExpression_163.invoke(___getDeclaredVariables_135.invoke(___node_202), ___node_202.getExpressionsList().get((int)(0l)), ___node_202.getId());
									}
								}
							}
						}
						if (boa.types.Ast.Statement.StatementKind.ASSERT.equals(___node_202.getKind()))
						{
							return false;
						}
						return true;
					}

					@Override
					protected void postVisit(final boa.types.Ast.Statement ___node_202) throws Exception
					{
						if (boa.types.Ast.Statement.StatementKind.BLOCK.equals(___node_202.getKind()))
						{
							for (long ___i_208 = 0; ___i_208 < ___node_202.getMethodsList().size(); ___i_208++)
							{
								if ((___node_202.getMethodsList().get((int)(___i_208)) != null))
								{
									{
										___copyOverMethod_122.invoke(___node_202.getMethodsList().get((int)(___i_208)));
										visit(___node_202.getMethodsList().get((int)(___i_208)));
									}
								}
							}

							for (long ___i_208 = 0; ___i_208 < ___node_202.getTypeDeclarationsList().size(); ___i_208++)
							{
								if ((___node_202.getTypeDeclarationsList().get((int)(___i_208)) != null))
								{
									{
										___copyOverDeclaration_126.invoke(___node_202.getTypeDeclarationsList().get((int)(___i_208)));
										visit(___node_202.getTypeDeclarationsList().get((int)(___i_208)));
									}
								}
							}

						}
					}

					@Override
					protected boolean preVisit(final boa.types.Ast.Expression ___node_202) throws Exception
					{
						if (___isProperAssignment_60.invoke(___node_202))
						{
							if (___node_202.hasId())
							{
								___handleBinaryExpression_163.invoke(___getVariableList_89.invoke(___node_202.getExpressionsList().get((int)(0l))), ___node_202.getExpressionsList().get((int)(1l)), ___node_202.getId());
							}
							if (___enableAcrossIn_26)
							{
								if (boa.types.Ast.Expression.ExpressionKind.METHODCALL.equals(___node_202.getExpressionsList().get((int)(1l)).getKind()))
								{
									___handleMethodCall_174.invoke(___node_202.getExpressionsList().get((int)(1l)));
									___makeJumpAcrossIn_201.invoke(___node_202, false);
								}
							}
						}
						if (boa.types.Ast.Expression.ExpressionKind.METHODCALL.equals(___node_202.getKind()))
						{
							___ht_211 = ___handleMethodCall_174.invoke(___node_202);
							if (___ht_211 == 2l)
							{
								___exmcType_212 = 0l;
								for (long ___j_161 = 0; ___j_161 < ___node_202.getMethodArgsList().get((int)(0l)).getExpressionsList().size(); ___j_161++)
								{
									if ((___node_202.getMethodArgsList().get((int)(0l)).getExpressionsList().get((int)(___j_161)) != null))
									{
										{
											___exmcType_212 = 0l;
											if (boa.types.Ast.Expression.ExpressionKind.METHODCALL.equals(___node_202.getMethodArgsList().get((int)(0l)).getExpressionsList().get((int)(___j_161)).getKind()))
											{
												___exmcType_212 = 1l;
											}
											else
											{
												if (boa.types.Ast.Expression.ExpressionKind.VARACCESS.equals(___node_202.getMethodArgsList().get((int)(0l)).getExpressionsList().get((int)(___j_161)).getKind()))
												{
													___exmcType_212 = 2l;
												}
												else
												{
													if (___isProperAssignment_60.invoke(___node_202.getMethodArgsList().get((int)(0l)).getExpressionsList().get((int)(___j_161))))
													{
														if (boa.types.Ast.Expression.ExpressionKind.METHODCALL.equals(___node_202.getMethodArgsList().get((int)(0l)).getExpressionsList().get((int)(___j_161)).getExpressionsList().get((int)(1l)).getKind()))
														{
															___exmcType_212 = 1l;
														}
														else
														{
															if (boa.types.Ast.Expression.ExpressionKind.VARACCESS.equals(___node_202.getMethodArgsList().get((int)(0l)).getExpressionsList().get((int)(___j_161)).getExpressionsList().get((int)(1l)).getKind()))
															{
																___exmcType_212 = 2l;
															}
														}
													}
												}
											}
											if (___exmcType_212 != 0l)
											{
												if (___makeJumpAcrossIn_201.invoke(___node_202.getMethodArgsList().get((int)(0l)).getExpressionsList().get((int)(___j_161)), (___exmcType_212 == 2l)) == 2l)
												{
													___mt2_213 = ___resolveAlias_148.invoke(___expressionsToStr_80.invoke(___node_202), ___node_202.getId(), true, false, ___dnnVariableMap_5);
													___recordImpact_57.invoke(___mt2_213);
													___libraryWiseCount_59.invoke(___mt2_213, ___node_202.getId(), true, false);
													break;
												}
											}
										}
									}
								}

							}
							else
							{
								if (___ht_211 == 0l)
								{
									___makeJumpAcrossIn_201.invoke(___node_202, false);
								}
							}
							___statementStack_14.push("call");
						}
						return true;
					}

					@Override
					protected void postVisit(final boa.types.Ast.Expression ___node_202) throws Exception
					{
						if (boa.types.Ast.Expression.ExpressionKind.METHODCALL.equals(___node_202.getKind()))
						{
							boa.functions.BoaIntrinsics.stack_pop(___statementStack_14);
						}
					}

				};

				___idExpressionMapper_214 = new BoaFunc_37()
				{
					@Override
					public void invoke(final boa.types.Ast.Expression ___node_202, final long ___id) throws Exception
					{
						if (___node_202.hasId())
						{
							___idMap_24.put(boa.functions.BoaCasts.longToString(___node_202.getId()), ___id);}
						for (long ___i_208 = 0; ___i_208 < ___node_202.getExpressionsList().size(); ___i_208++)
						{
							if ((___node_202.getExpressionsList().get((int)(___i_208)) != null))
							{
								{
									___idExpressionMapper_214.invoke(___node_202.getExpressionsList().get((int)(___i_208)), ___id);
								}
							}
						}

						if (boa.types.Ast.Expression.ExpressionKind.METHODCALL.equals(___node_202.getKind()))
						{
							for (long ___i_208 = 0; ___i_208 < ___node_202.getMethodArgsList().size(); ___i_208++)
							{
								if ((___node_202.getMethodArgsList().get((int)(___i_208)) != null))
								{
									{
										___idExpressionMapper_214.invoke(___node_202.getMethodArgsList().get((int)(___i_208)), ___id);
									}
								}
							}

						}
					}
				};

				___idVariableMapper_215 = new BoaFunc_38()
				{
					@Override
					public void invoke(final boa.types.Ast.Variable ___node_202, final long ___id) throws Exception
					{
						if (___node_202.hasId())
						{
							___idMap_24.put(boa.functions.BoaCasts.longToString(___node_202.getId()), ___id);}
						if (___node_202.hasComputedName())
						{
							___idExpressionMapper_214.invoke(___node_202.getComputedName(), ___id);
						}
						if (___node_202.hasInitializer())
						{
							___idExpressionMapper_214.invoke(___node_202.getInitializer(), ___id);
						}
					}
				};

				___idStatementMapper_216 = new BoaFunc_39()
				{
					@Override
					public void invoke(final boa.types.Ast.Statement ___node_202, final long ___id) throws Exception
					{
						if (___node_202.hasId())
						{
							if ((___node_202.getVariableDeclarationsList() != null))
							{
								for (long ___kk = 0; ___kk < ___node_202.getVariableDeclarationsList().size(); ___kk++)
								{
									if ((___node_202.getVariableDeclarationsList().get((int)(___kk)) != null))
									{
										{
											___idVariableMapper_215.invoke(___node_202.getVariableDeclarationsList().get((int)(___kk)), ___id);
										}
									}
								}

							}
							___idMap_24.put(boa.functions.BoaCasts.longToString(___node_202.getId()), ___id);if (___areExpressionsDefinedInStatement_52.invoke(___node_202))
							{
								for (long ___jj = 0; ___jj < ___node_202.getExpressionsList().size(); ___jj++)
								{
									if ((___node_202.getExpressionsList().get((int)(___jj)) != null))
									{
										{
											___idExpressionMapper_214.invoke(___node_202.getExpressionsList().get((int)(___jj)), ___id);
										}
									}
								}

							}
							if ((___node_202.getConditionsList() != null))
							{
								for (long ___jj = 0; ___jj < ___node_202.getConditionsList().size(); ___jj++)
								{
									if ((___node_202.getConditionsList().get((int)(___jj)) != null))
									{
										{
											___idExpressionMapper_214.invoke(___node_202.getConditionsList().get((int)(___jj)), ___id);
										}
									}
								}

							}
						}
					}
				};

				___idMapper_217 = new BoaFunc_9()
				{
					boa.graphs.cfg.CFG ___cfg_218;
					boa.graphs.cfg.CFGNode[] ___v_219;

					@Override
					public void invoke(final String ___scope_164) throws Exception
					{
						if (!___isCfgDefined_43.invoke(___scope_164))
						{
							return;
						}
						___cfg_218 = ___cfgMap_25.get(___scope_164);
						___v_219 = boa.functions.BoaIntrinsics.basic_array(___cfg_218.getNodes().toArray(new boa.graphs.cfg.CFGNode[0]));
						for (long ___i_208 = 0; ___i_208 < ___v_219.length; ___i_208++)
						{
							if ((___v_219[(int)(___i_208)] != null))
							{
								{
									if ((___v_219[(int)(___i_208)].getExpr() != null))
									{
										___idExpressionMapper_214.invoke(___v_219[(int)(___i_208)].getExpr(), ___v_219[(int)(___i_208)].getId());
									}
									if ((___v_219[(int)(___i_208)].getStmt() != null))
									{
										___idStatementMapper_216.invoke(___v_219[(int)(___i_208)].getStmt(), ___v_219[(int)(___i_208)].getId());
									}
								}
							}
						}

					}
				};

				new boa.runtime.BoaAbstractVisitor()
				{
					boa.types.Diff.ChangedFile[] ___snapshot_220;
					boolean ___isML_221;
					boolean ___isMLRelatedImport_222;
					String ___import_223;
					String[] ___p1_224;
					long ___v_225;
					String[] ___p2_226;
					long ___v2_227;
					String[] ___vsst_228;
					BoaTup_0[] ___vsstt_229;
					String ___ns_230;

					@Override
					protected boolean preVisit(final boa.types.Code.CodeRepository ___node_202) throws Exception
					{
						___snapshot_220 = boa.functions.BoaIntrinsics.getSnapshot(___node_202, "SOURCE_PY_3");
						for (long ___i_208 = 0; ___i_208 < ___snapshot_220.length; ___i_208++)
						{
							if ((___snapshot_220[(int)(___i_208)] != null))
							{
								{
									visit(___snapshot_220[(int)(___i_208)]);
								}
							}
						}

						return false;
					}

					@Override
					protected boolean preVisit(final boa.types.Code.Revision ___node_202) throws Exception
					{
						___revision_id_17 = ___node_202.getId();
						return true;
					}

					@Override
					protected boolean preVisit(final boa.types.Diff.ChangedFile ___node_202) throws Exception
					{
						____local_aggregator_totalModuleCount = ____local_aggregator_totalModuleCount + (1l);
						if (___node_202.hasName() && boa.functions.BoaStringIntrinsics.match(".*test.*", boa.functions.BoaStringIntrinsics.lowerCase(___node_202.getName())))
						{
							____local_aggregator_testFileCount = ____local_aggregator_testFileCount + (1l);
							return false;
						}
						if (___node_202.hasKind())
						{
							if (boa.types.Diff.ChangedFile.FileKind.SOURCE_PY_ERROR.equals(___node_202.getKind()))
							{
								____local_aggregator_errorFileCount = ____local_aggregator_errorFileCount + (1l);
								return false;
							}
						}
						if (!"program2.py".equals(___node_202.getName()))
						{
							return false;
						}
						if (boa.types.Shared.ChangeKind.ADDED.equals(___node_202.getChange()))
						{
							return false;
						}
						return true;
					}

					@Override
					protected boolean preVisit(final boa.types.Ast.Namespace ___node_202) throws Exception
					{
						context.write(new boa.io.EmitKey("nodePrint"), new boa.io.EmitValue(com.googlecode.protobuf.format.JsonFormat.printToString(___node_202)));
						___moduleChanged_31 = false;
						___isML_221 = false;
						for (long ___j_161 = 0; ___j_161 < ___dnnPackage_34.length; ___j_161++)
						{
							if ((___dnnPackage_34[(int)(___j_161)] != null))
							{
								{
									___dnnVariableMap_5.put(___dnnPackage_34[(int)(___j_161)], ___dnnPackage_34[(int)(___j_161)]);}
							}
						}

						for (long ___j_161 = 0; ___j_161 < ___otherPackage_35.length; ___j_161++)
						{
							if ((___otherPackage_35[(int)(___j_161)] != null))
							{
								{
									___dnnVariableMap_5.put(___otherPackage_35[(int)(___j_161)], ___otherPackage_35[(int)(___j_161)]);}
							}
						}

						for (long ___i_208 = 0; ___i_208 < ___node_202.getImportsList().size(); ___i_208++)
						{
							if ((___node_202.getImportsList().get((int)(___i_208)) != null))
							{
								{
									___isMLRelatedImport_222 = false;
									___import_223 = boa.functions.BoaStringIntrinsics.trim(___node_202.getImportsList().get((int)(___i_208)));
									if (boa.functions.BoaStringIntrinsics.match("^\\..*", ___import_223) || "".equals(___import_223))
									{
										continue;
									}
									for (long ___j_161 = 0; ___j_161 < ___dnnPackage_34.length; ___j_161++)
									{
										if ((___dnnPackage_34[(int)(___j_161)] != null))
										{
											{
												if (boa.functions.BoaStringIntrinsics.match("^" + ___dnnPackage_34[(int)(___j_161)] + ".*", ___import_223) || boa.functions.BoaStringIntrinsics.match("^from " + ___dnnPackage_34[(int)(___j_161)] + ".*", ___import_223))
												{
													___currentModuleLibs_9.add(___dnnPackage_34[(int)(___j_161)]);
													___isML_221 = true;
													___isMLRelatedImport_222 = true;
													break;
												}
											}
										}
									}

									for (long ___j_161 = 0; ___j_161 < ___otherPackage_35.length; ___j_161++)
									{
										if ((___otherPackage_35[(int)(___j_161)] != null))
										{
											{
												if (boa.functions.BoaStringIntrinsics.match("^" + ___otherPackage_35[(int)(___j_161)] + ".*", ___import_223) || boa.functions.BoaStringIntrinsics.match("^from " + ___otherPackage_35[(int)(___j_161)] + ".*", ___import_223))
												{
													___isMLRelatedImport_222 = true;
													break;
												}
											}
										}
									}

									if (___isMLRelatedImport_222)
									{
										if (boa.functions.BoaStringIntrinsics.match("^from .*", ___import_223))
										{
											___p1_224 = boa.functions.BoaStringIntrinsics.splitall(___import_223, "from");
											if (((long)___p1_224.length) > 1l)
											{
												___import_223 = boa.functions.BoaStringIntrinsics.trim(___p1_224[(int)(1l)]);
												if (boa.functions.BoaStringIntrinsics.match("^\\..*", ___import_223))
												{
													continue;
												}
												___v_225 = boa.functions.BoaStringIntrinsics.indexOf(" as ", ___import_223);
												if (___v_225 == -1l)
												{
													___p2_226 = boa.functions.BoaStringIntrinsics.splitall(___import_223, " ");
													if (((long)___p2_226.length) == 2l)
													{
														___dnnVariableMap_5.put(___p2_226[(int)(1l)], ___p2_226[(int)(0l)] + "." + ___p2_226[(int)(1l)]);}
												}
												else
												{
													___dnnVariableMap_5.put(boa.functions.BoaStringIntrinsics.substring(___import_223, ___v_225 + 4l), boa.functions.BoaStringIntrinsics.stringReplace(boa.functions.BoaStringIntrinsics.substring(___import_223, 0l, ___v_225), " ", ".", true));}
											}
										}
										else
										{
											___v2_227 = boa.functions.BoaStringIntrinsics.indexOf(" as ", ___import_223);
											if (___v2_227 != -1l)
											{
												___dnnVariableMap_5.put(boa.functions.BoaStringIntrinsics.substring(___import_223, ___v2_227 + 4l), boa.functions.BoaStringIntrinsics.substring(___import_223, 0l, ___v2_227));}
											else
											{
												___dnnVariableMap_5.put(___import_223, ___import_223);___v2_227 = boa.functions.BoaStringIntrinsics.indexOf(".", ___import_223);
												if (___v2_227 > 0l)
												{
													___dnnVariableMap_5.put(boa.functions.BoaStringIntrinsics.substring(___import_223, 0l, ___v2_227), boa.functions.BoaStringIntrinsics.substring(___import_223, 0l, ___v2_227));}
											}
										}
									}
								}
							}
						}

						if (!___isML_221)
						{
							___dnnVariableMap_5.clear();
							return false;
						}
						____local_aggregator_dnnModuleCount = ____local_aggregator_dnnModuleCount + (1l);
						___cfgMap_25.put("", boa.functions.BoaGraphIntrinsics.getcfg(___node_202));___idMapper_217.invoke("");
						for (long ___i_208 = 0; ___i_208 < ___node_202.getStatementsList().size(); ___i_208++)
						{
							if ((___node_202.getStatementsList().get((int)(___i_208)) != null))
							{
								{
									visit(___node_202.getStatementsList().get((int)(___i_208)));
								}
							}
						}

						for (long ___i_208 = 0; ___i_208 < ___node_202.getMethodsList().size(); ___i_208++)
						{
							if ((___node_202.getMethodsList().get((int)(___i_208)) != null))
							{
								{
									visit(___node_202.getMethodsList().get((int)(___i_208)));
								}
							}
						}

						for (long ___i_208 = 0; ___i_208 < ___node_202.getDeclarationsList().size(); ___i_208++)
						{
							if ((___node_202.getDeclarationsList().get((int)(___i_208)) != null))
							{
								{
									visit(___node_202.getDeclarationsList().get((int)(___i_208)));
								}
							}
						}

						___containingTypes_2.clear();
						___visited_20.clear();
						___impactSet_32.clear();
						___impactDetectionVisitor_210.visit(___node_202);
						if (___moduleChanged_31)
						{
							____local_aggregator_modifiedDnnModuleCount = ____local_aggregator_modifiedDnnModuleCount + (1l);
						}
						___vsst_228 = boa.functions.BoaIntrinsics.basic_array(___currentModuleLibs_9.toArray(new String[0]));
						for (long ___i_208 = 0; ___i_208 < ___vsst_228.length; ___i_208++)
						{
							if ((___vsst_228[(int)(___i_208)] != null))
							{
								{
									___libraryWiseCount_59.invoke(___vsst_228[(int)(___i_208)], 0l, ___moduleChanged_31, true);
								}
							}
						}

						___vsstt_229 = boa.functions.BoaIntrinsics.basic_array(___allDNNApi_8.toArray(new BoaTup_0[0]));
						for (long ___i_208 = 0; ___i_208 < ___vsstt_229.length; ___i_208++)
						{
							if ((___vsstt_229[(int)(___i_208)] != null))
							{
								{
									if (___isApiCallSiteCounted_10.containsKey(___vsstt_229[(int)(___i_208)].___id) == false)
									{
										___libraryWiseCount_59.invoke(___vsstt_229[(int)(___i_208)].___name, ___vsstt_229[(int)(___i_208)].___id, false, false);
									}
								}
							}
						}

						___allDNNApi_8.clear();
						___methodMap_1.clear();
						___dnnVariableMap_5.clear();
						___objectMap_6.clear();
						___idMap_24.clear();
						___cfgMap_25.clear();
						___acrossInContainingTypes_3.clear();
						___returnImpacted_30.clear();
						___aliasSet_33.clear();
						___namespaceStack_13.clear();
						___statementStack_14.clear();
						___currentImpactedAPI_7.clear();
						___isApiCallSiteCounted_10.clear();
						___currentModuleLibs_9.clear();
						return false;
					}

					@Override
					protected boolean preVisit(final boa.types.Ast.Declaration ___node_202) throws Exception
					{
						___containingTypes_2.push(___node_202.getName());
						___ns_230 = ___stackToStr_36.invoke(___containingTypes_2, ".");
						___cfgMap_25.put(___ns_230, boa.functions.BoaGraphIntrinsics.getcfg(___node_202));___idMapper_217.invoke(___ns_230);
						___objectMap_6.put(___ns_230, ___ns_230);for (long ___i_208 = 0; ___i_208 < ___node_202.getStatementsList().size(); ___i_208++)
						{
							if ((___node_202.getStatementsList().get((int)(___i_208)) != null))
							{
								{
									visit(___node_202.getStatementsList().get((int)(___i_208)));
								}
							}
						}

						return true;
					}

					@Override
					protected void postVisit(final boa.types.Ast.Declaration ___node_202) throws Exception
					{
						boa.functions.BoaIntrinsics.stack_pop(___containingTypes_2);
					}

					@Override
					protected boolean preVisit(final boa.types.Ast.Method ___node_202) throws Exception
					{
						___containingTypes_2.push(___node_202.getName());
						___current_method_name_15 = ___stackToStr_36.invoke(___containingTypes_2, ".");
						___objectMap_6.put(___current_method_name_15, ___current_method_name_15);___methodMap_1.put(___current_method_name_15, ___node_202);___cfgMap_25.put(___current_method_name_15, boa.functions.BoaGraphIntrinsics.getcfg(___node_202));___idMapper_217.invoke(___current_method_name_15);
						if ((___node_202.getArgumentsList() != null))
						{
							for (long ___kk = 0; ___kk < ___node_202.getArgumentsList().size(); ___kk++)
							{
								if ((___node_202.getArgumentsList().get((int)(___kk)) != null))
								{
									{
										___idVariableMapper_215.invoke(___node_202.getArgumentsList().get((int)(___kk)), 0l);
									}
								}
							}

						}
						return true;
					}

					@Override
					protected void postVisit(final boa.types.Ast.Method ___node_202) throws Exception
					{
						boa.functions.BoaIntrinsics.stack_pop(___containingTypes_2);
					}

					@Override
					protected void postVisit(final boa.types.Ast.Statement ___node_202) throws Exception
					{
						if (boa.types.Ast.Statement.StatementKind.BLOCK.equals(___node_202.getKind()))
						{
							for (long ___i_208 = 0; ___i_208 < ___node_202.getMethodsList().size(); ___i_208++)
							{
								if ((___node_202.getMethodsList().get((int)(___i_208)) != null))
								{
									{
										visit(___node_202.getMethodsList().get((int)(___i_208)));
									}
								}
							}

							for (long ___i_208 = 0; ___i_208 < ___node_202.getTypeDeclarationsList().size(); ___i_208++)
							{
								if ((___node_202.getTypeDeclarationsList().get((int)(___i_208)) != null))
								{
									{
										visit(___node_202.getTypeDeclarationsList().get((int)(___i_208)));
									}
								}
							}

						}
					}

				}.visit(___p_0);

				if (____local_aggregator_testFileCount != 0l)
				{
					context.write(new boa.io.EmitKey("testFileCount"), new boa.io.EmitValue(____local_aggregator_testFileCount));
				}

				if (____local_aggregator_errorFileCount != 0l)
				{
					context.write(new boa.io.EmitKey("errorFileCount"), new boa.io.EmitValue(____local_aggregator_errorFileCount));
				}

				if (____local_aggregator_dnnModuleCount != 0l)
				{
					context.write(new boa.io.EmitKey("dnnModuleCount"), new boa.io.EmitValue(____local_aggregator_dnnModuleCount));
				}

				if (____local_aggregator_modifiedDnnModuleCount != 0l)
				{
					context.write(new boa.io.EmitKey("modifiedDnnModuleCount"), new boa.io.EmitValue(____local_aggregator_modifiedDnnModuleCount));
				}

				if (____local_aggregator_totalModuleCount != 0l)
				{
					context.write(new boa.io.EmitKey("totalModuleCount"), new boa.io.EmitValue(____local_aggregator_totalModuleCount));
				}

				if (____local_aggregator_kerasModuleCount != 0l)
				{
					context.write(new boa.io.EmitKey("kerasModuleCount"), new boa.io.EmitValue(____local_aggregator_kerasModuleCount));
				}

				if (____local_aggregator_modifiedKerasModuleCount != 0l)
				{
					context.write(new boa.io.EmitKey("modifiedKerasModuleCount"), new boa.io.EmitValue(____local_aggregator_modifiedKerasModuleCount));
				}

				if (____local_aggregator_torchModuleCount != 0l)
				{
					context.write(new boa.io.EmitKey("torchModuleCount"), new boa.io.EmitValue(____local_aggregator_torchModuleCount));
				}

				if (____local_aggregator_modifiedTorchModuleCount != 0l)
				{
					context.write(new boa.io.EmitKey("modifiedTorchModuleCount"), new boa.io.EmitValue(____local_aggregator_modifiedTorchModuleCount));
				}

				if (____local_aggregator_tfModuleCount != 0l)
				{
					context.write(new boa.io.EmitKey("tfModuleCount"), new boa.io.EmitValue(____local_aggregator_tfModuleCount));
				}

				if (____local_aggregator_modifiedTfModuleCount != 0l)
				{
					context.write(new boa.io.EmitKey("modifiedTfModuleCount"), new boa.io.EmitValue(____local_aggregator_modifiedTfModuleCount));
				}

				if (____local_aggregator_kerasApiCallSiteCount != 0l)
				{
					context.write(new boa.io.EmitKey("kerasApiCallSiteCount"), new boa.io.EmitValue(____local_aggregator_kerasApiCallSiteCount));
				}

				if (____local_aggregator_modifiedkerasApiCallSiteCount != 0l)
				{
					context.write(new boa.io.EmitKey("modifiedkerasApiCallSiteCount"), new boa.io.EmitValue(____local_aggregator_modifiedkerasApiCallSiteCount));
				}

				if (____local_aggregator_torchApiCallSiteCount != 0l)
				{
					context.write(new boa.io.EmitKey("torchApiCallSiteCount"), new boa.io.EmitValue(____local_aggregator_torchApiCallSiteCount));
				}

				if (____local_aggregator_modifiedtorchApiCallSiteCount != 0l)
				{
					context.write(new boa.io.EmitKey("modifiedtorchApiCallSiteCount"), new boa.io.EmitValue(____local_aggregator_modifiedtorchApiCallSiteCount));
				}

				if (____local_aggregator_tfApiCallSiteCount != 0l)
				{
					context.write(new boa.io.EmitKey("tfApiCallSiteCount"), new boa.io.EmitValue(____local_aggregator_tfApiCallSiteCount));
				}

				if (____local_aggregator_modifiedtfApiCallSiteCount != 0l)
				{
					context.write(new boa.io.EmitKey("modifiedtfApiCallSiteCount"), new boa.io.EmitValue(____local_aggregator_modifiedtfApiCallSiteCount));
				}

				if (____local_aggregator_kerasApiUniqueCount != 0l)
				{
					context.write(new boa.io.EmitKey("kerasApiUniqueCount"), new boa.io.EmitValue(____local_aggregator_kerasApiUniqueCount));
				}

				if (____local_aggregator_modifiedKerasApiUniqueCount != 0l)
				{
					context.write(new boa.io.EmitKey("modifiedKerasApiUniqueCount"), new boa.io.EmitValue(____local_aggregator_modifiedKerasApiUniqueCount));
				}

				if (____local_aggregator_torchApiUniqueCount != 0l)
				{
					context.write(new boa.io.EmitKey("torchApiUniqueCount"), new boa.io.EmitValue(____local_aggregator_torchApiUniqueCount));
				}

				if (____local_aggregator_modifiedTorchApiUniqueCount != 0l)
				{
					context.write(new boa.io.EmitKey("modifiedTorchApiUniqueCount"), new boa.io.EmitValue(____local_aggregator_modifiedTorchApiUniqueCount));
				}

				if (____local_aggregator_tfApiUniqueCount != 0l)
				{
					context.write(new boa.io.EmitKey("tfApiUniqueCount"), new boa.io.EmitValue(____local_aggregator_tfApiUniqueCount));
				}

				if (____local_aggregator_modifiedTfApiUniqueCount != 0l)
				{
					context.write(new boa.io.EmitKey("modifiedTfApiUniqueCount"), new boa.io.EmitValue(____local_aggregator_modifiedTfApiUniqueCount));
				}


			} catch (final Throwable e) {
				LOG.error(e.getClass().getName() + " caught", e);
				boa.io.BoaOutputCommitter.lastSeenEx = e;
				throw new java.io.IOException("map failure for key '" + key.toString() + "'", e);
			}
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

	static class SampleBoaCombiner extends boa.runtime.BoaCombiner {
		static {
			boa.runtime.BoaPartitioner.setVariableNames(new String[] {"dnnModuleCount", "errorFileCount", "impactList", "kerasApiCallSiteCount", "kerasApiUniqueCount", "kerasModuleCount", "modifiedDnnModuleCount", "modifiedKerasApiUniqueCount", "modifiedKerasModuleCount", "modifiedTfApiUniqueCount", "modifiedTfModuleCount", "modifiedTorchApiUniqueCount", "modifiedTorchModuleCount", "modifiedkerasApiCallSiteCount", "modifiedtfApiCallSiteCount", "modifiedtorchApiCallSiteCount", "noCommit", "nodePrint", "testFileCount", "tfApiCallSiteCount", "tfApiUniqueCount", "tfModuleCount", "torchApiCallSiteCount", "torchApiUniqueCount", "torchModuleCount", "totalModuleCount"});
		}
		public SampleBoaCombiner() {
			super();

			this.aggregators.put("kerasApiUniqueCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("errorFileCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("kerasApiCallSiteCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("tfApiCallSiteCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("dnnModuleCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("modifiedKerasModuleCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("torchApiCallSiteCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("modifiedtorchApiCallSiteCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("torchModuleCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("modifiedkerasApiCallSiteCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("modifiedTfApiUniqueCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("noCommit", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("modifiedTorchApiUniqueCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("testFileCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("totalModuleCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("modifiedKerasApiUniqueCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("torchApiUniqueCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("modifiedDnnModuleCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("tfApiUniqueCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("modifiedTfModuleCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("modifiedTorchModuleCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("modifiedtfApiCallSiteCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("impactList", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("kerasModuleCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("tfModuleCount", new boa.aggregators.IntSumAggregator());
		}
	}

	static class SampleBoaReducer extends boa.runtime.BoaReducer {
		static {
			boa.runtime.BoaPartitioner.setVariableNames(new String[] {"dnnModuleCount", "errorFileCount", "impactList", "kerasApiCallSiteCount", "kerasApiUniqueCount", "kerasModuleCount", "modifiedDnnModuleCount", "modifiedKerasApiUniqueCount", "modifiedKerasModuleCount", "modifiedTfApiUniqueCount", "modifiedTfModuleCount", "modifiedTorchApiUniqueCount", "modifiedTorchModuleCount", "modifiedkerasApiCallSiteCount", "modifiedtfApiCallSiteCount", "modifiedtorchApiCallSiteCount", "noCommit", "nodePrint", "testFileCount", "tfApiCallSiteCount", "tfApiUniqueCount", "tfModuleCount", "torchApiCallSiteCount", "torchApiUniqueCount", "torchModuleCount", "totalModuleCount"});
		}
		public SampleBoaReducer() {
			super();

			this.aggregators.put("kerasApiUniqueCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("errorFileCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("kerasApiCallSiteCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("tfApiCallSiteCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("dnnModuleCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("modifiedKerasModuleCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("torchApiCallSiteCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("modifiedtorchApiCallSiteCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("torchModuleCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("modifiedkerasApiCallSiteCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("modifiedTfApiUniqueCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("noCommit", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("nodePrint", new boa.aggregators.CollectionAggregator());
			this.aggregators.put("modifiedTorchApiUniqueCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("testFileCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("totalModuleCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("modifiedKerasApiUniqueCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("torchApiUniqueCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("modifiedDnnModuleCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("tfApiUniqueCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("modifiedTfModuleCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("modifiedTorchModuleCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("modifiedtfApiCallSiteCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("impactList", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("kerasModuleCount", new boa.aggregators.IntSumAggregator());
			this.aggregators.put("tfModuleCount", new boa.aggregators.IntSumAggregator());
		}
	}

	@Override
	public org.apache.hadoop.mapreduce.Mapper getMapper() {
		return new SampleBoaMapper();
	}

	@Override
	public boa.runtime.BoaCombiner getCombiner() {
		return new SampleBoaCombiner();
	}

	@Override
	public boa.runtime.BoaReducer getReducer() {
		return new SampleBoaReducer();
	}
}