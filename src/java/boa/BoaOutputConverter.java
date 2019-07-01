/*
 * Copyright 2019, Robert Dyer, Che Shian Hung,
 *                 and Bowling Green State University
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
package boa;

import java.io.IOException;

import com.google.protobuf.CodedInputStream;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Reader;
import org.apache.hadoop.util.ReflectionUtils;
import org.apache.hadoop.util.StringUtils;

import boa.BoaMain;
import boa.io.EmitKey;
import boa.output.Output.Row;
import boa.output.Output.Value;

/**
 * The main entry point for the Boa output converter.
 *
 * @author rdyer
 * @author hungc
 */
public class BoaOutputConverter extends BoaMain {
	static Configuration conf = new Configuration();
	static FileSystem fs;

	public static void main(final String[] args) throws IOException {
		fs = FileSystem.get(conf);

		final CommandLine cl = processCommandLineOptions(args);
		if (cl == null) return;

		convert(inputPath);
	}

	static Path inputPath = null;

	private static CommandLine processCommandLineOptions(final String[] args) {
		final Options options = new Options();
		options.addOption("i", "in", true, "output file to be converted into text");

		final CommandLine cl;
		try {
			cl = new PosixParser().parse(options, args);
		} catch (final org.apache.commons.cli.ParseException e) {
			System.err.println(e.getMessage());
			new HelpFormatter().printHelp("Boa Compiler", options);
			return null;
		}

		inputPath = null;
		if (cl.hasOption('i')) {
			final String input = cl.getOptionValue('i');

			final Path p = new Path(
				conf.get("fs.default.name", "hdfs://boa-njt/"),
				new Path(input)
			);
			try {
				if (!fs.exists(p))
					System.err.println("Path '" + p + "' does not exist");
				else
					inputPath = p;
			} catch (final Exception e) {
				System.err.println("Path '" + p + "' does not exist");
			}
		}

		if (inputPath == null) {
			System.err.println("no valid input file found - did you use the --in option?");
			new HelpFormatter().printHelp("Boa Output Converter", options);
			return null;
		}

		return cl;
	}

	public static void convert(final Path inputPath) {
		SequenceFile.Reader reader = null;

		try {
			reader = new SequenceFile.Reader(fs, inputPath, conf);
		} catch (final Exception e) {
			reader = null;
		}

		final NullWritable key = (NullWritable) ReflectionUtils.newInstance(reader.getKeyClass(), conf);
		final BytesWritable value = (BytesWritable) ReflectionUtils.newInstance(reader.getValueClass(), conf);

		final String tableName = inputPath.getName();
		try {
			while (reader.next(key, value))
				System.out.println(tableName + convertLine(value));
		} catch (final IOException e) {
		}
	}

	private static String convertLine(final BytesWritable value) {
		String line = "";
		Row row = null;

		try {
			row = Row.parseFrom(CodedInputStream.newInstance(value.getBytes(), 0, value.getLength()));
		} catch (final IOException e) {
			return "<<ERROR CONVERTING ROW TO TEXT>>";
		}

		for (final Value v : row.getColsList())
			line += "[" + EmitKey.valueToString(v) + "]";

		line += " = " + EmitKey.valueToString(row.getVal());

		return line;
	}
}
