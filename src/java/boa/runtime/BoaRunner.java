/*
 * Copyright 2014, Anthony Urso, Hridesh Rajan, Robert Dyer, 
 *                 and Iowa State University of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package boa.runtime;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.SnappyCodec;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.Tool;

import boa.io.BoaOutputFormat;
import boa.io.EmitKey;
import boa.io.EmitValue;

/**
 * @author anthonyu
 * @author rdyer
 */
@SuppressWarnings("static-access")
public abstract class BoaRunner extends Configured implements Tool {
	/**
	 * Create a {@link Job} describing the work to be done by this Boa job.
	 * 
	 * @param ins
	 *            An array of {@link Path} containing the locations of the input
	 *            files
	 * 
	 * @param out
	 *            A {@link Path} containing the location of the output file
	 * 
	 * @param robust
	 *            A boolean representing whether the job should ignore most
	 *            exceptions
	 * 
	 * @return A {@link Job} describing the work to be done by this Boa job
	 * @throws IOException
	 */
	public Job job(final Path[] ins, final Path out, final boolean robust) throws IOException {
		final Configuration configuration = getConf();

		configuration.setBoolean("boa.runtime.robust", robust);

		// faster local reads
		configuration.setBoolean("dfs.client.read.shortcircuit", true);
		configuration.setBoolean("dfs.client.read.shortcircuit.skip.checksum", true);

		// by default our MapFile's index every key, which takes up
		// a lot of memory - this lets you skip keys in the index and
		// control the memory requirements (as a tradeoff of slower gets)
		//configuration.setLong("io.map.index.skip", 128);

		// map output compression
		configuration.setBoolean("mapred.compress.map.output", true);
		configuration.set("mapred.map.output.compression.type", "BLOCK");
		configuration.setClass("mapred.map.output.compression.codec", SnappyCodec.class, CompressionCodec.class);

		configuration.setBoolean("mapred.map.tasks.speculative.execution", false);
		configuration.setBoolean("mapred.reduce.tasks.speculative.execution", false);
		configuration.setLong("mapred.job.reuse.jvm.num.tasks", -1);

		final Job job = new Job(configuration);

		if (ins != null)
			for (final Path in : ins)
				FileInputFormat.addInputPath(job, in);
		FileOutputFormat.setOutputPath(job, out);

		job.setPartitionerClass(BoaPartitioner.class);

		job.setMapOutputKeyClass(EmitKey.class);
		job.setMapOutputValueClass(EmitValue.class);

		job.setOutputFormatClass(BoaOutputFormat.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(NullWritable.class);

		return job;
	}

	protected static Options options = new Options();

	static {
		options.addOption("p", "profile", false, "if true, profiles the execution of 1 map task");
		options.addOption("r", "robust", false, "if true, logs non-IO exceptions and continues");
		options.addOption("b", "block", false, "if true, wait for job to finish and show status");
		options.addOption(OptionBuilder.withLongOpt("job")
										.withDescription("sets the MySql ID to update with this job's status")
										.hasArg()
										.withArgName("ID")
										.create("j"));
		options.addOption(org.apache.commons.cli.OptionBuilder.withLongOpt("ast")
										.withDescription("which INPUT to use for ASTs")
										.hasArg()
										.withArgName("INPUT")
										.create("a"));
		options.addOption(org.apache.commons.cli.OptionBuilder.withLongOpt("comments")
										.withDescription("which INPUT to use for comments")
										.hasArg()
										.withArgName("INPUT")
										.create("c"));
	}

	protected static Options getOptions() { return options; }

	public static CommandLine parseArgs(String[] args, String usage) {
		CommandLine line = null;

		try {
			line = new PosixParser().parse(options, args);
		} catch (ParseException exp) {
			System.err.println(exp.getMessage());
			printHelp(usage);
		}

		return line;
	}

	public static void printHelp(String usage) {
		new HelpFormatter().printHelp("[options] " + usage, options);
		System.exit(-1);
	}

	public abstract String getUsage();

	public abstract Mapper<?,?,?,?> getMapper();

	public abstract BoaCombiner getCombiner();

	public abstract BoaReducer getReducer();
}
