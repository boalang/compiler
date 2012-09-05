package sizzle.runtime;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.io.compress.CompressionCodec;

import sizzle.io.SizzleOutputFormat;
import sizzle.io.EmitKey;
import sizzle.io.EmitValue;

public abstract class SizzleRunner {
	/**
	 * Create a {@link Job} describing the work to be done by this Sizzle job.
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
	 * @return A {@link Job} describing the work to be done by this Sizzle job
	 * @throws IOException
	 */
	public Job job(final Configuration configuration, final Path[] ins, final Path out, final boolean robust) throws IOException {
		configuration.setBoolean("sizzle.runtime.robust", robust);

		// map output compression
		configuration.setBoolean("mapred.compress.map.output", true);
		configuration.setClass("mapred.map.output.compression.codec", GzipCodec.class, CompressionCodec.class);

		final Job job = new Job(configuration);

		for (final Path in : ins)
			FileInputFormat.addInputPath(job, in);
		FileOutputFormat.setOutputPath(job, out);

		job.setMapOutputKeyClass(EmitKey.class);
		job.setMapOutputValueClass(EmitValue.class);

		// TODO: support protobufs/sequence files/avro here
		job.setOutputFormatClass(SizzleOutputFormat.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(NullWritable.class);

		return job;
	}

	protected static Options options = new Options();

	static {
		options.addOption("r", "robust", false, "if true, logs non-IO exceptions and continues");
		options.addOption("b", "block", false, "if true, wait for job to finish and show status");
		options.addOption(OptionBuilder.withLongOpt("job")
										.withDescription("sets the MySql ID to update with this job's status")
										.hasArg()
										.withArgName("ID")
										.create("j"));
	}

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

	public abstract Mapper getMapper();

	public abstract SizzleCombiner getCombiner();

	public abstract SizzleReducer getReducer();
}
