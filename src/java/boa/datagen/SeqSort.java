/*
 * Copyright 2015, Hridesh Rajan, Robert Dyer, Hoan Nguyen
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

package boa.datagen;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.SnappyCodec;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.mapred.lib.IdentityMapper;
import org.apache.hadoop.mapred.lib.IdentityReducer;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import boa.datagen.util.FileIO;
import boa.datagen.util.Properties;

/**
 * @author hoan
 * 
 */
public class SeqSort<K,V> extends Configured implements Tool {
	private String inPath = "", outPath = "";
	private RunningJob jobResult = null;

	public SeqSort(String inPath, String outPath) {
		this.inPath = inPath;
		this.outPath = outPath;
	}

	static int printUsage() {
		System.out.println("sort [-m <maps>] [-r <reduces>] " +
				"[-inFormat <input format class>] " +
				"[-outFormat <output format class>] " + 
				"[-outKey <output key class>] " +
				"[-outValue <output value class>] " +
				"[-totalOrder <pcnt> <num samples> <max splits>] " +
				"<input> <output>");
		ToolRunner.printGenericCommandUsage(System.out);
		return -1;
	}

	/**
	 * The main driver for sort program.
	 * Invoke this method to submit the map/reduce job.
	 * @throws IOException When there is communication problems with the 
	 *                     job tracker.
	 */
	@Override
	public int run(String[] args) throws Exception {
		System.out.println(inPath);

		JobConf jobConf = new JobConf(getConf(), SeqSort.class);
		jobConf.setJobName("sorter");

		jobConf.setMapperClass(IdentityMapper.class);        
		jobConf.setReducerClass(IdentityReducer.class);

		JobClient client = new JobClient(jobConf);
		ClusterStatus cluster = client.getClusterStatus();
		int num_reduces = (int) (cluster.getMaxReduceTasks() * 0.9);
		String sort_reduces = jobConf.get("test.sort.reduces_per_host");
		if (sort_reduces != null) {
			num_reduces = cluster.getTaskTrackers() * 
					Integer.parseInt(sort_reduces);
		}

		// Set user-supplied (possibly default) job configs
		jobConf.setNumReduceTasks(num_reduces);

		jobConf.setInputFormat(SequenceFileInputFormat.class);
		jobConf.setOutputFormat(SequenceFileOutputFormat.class);

		jobConf.setOutputKeyClass(Text.class);
		jobConf.setOutputValueClass(BytesWritable.class);
		
		SequenceFileOutputFormat.setCompressOutput(jobConf, true);
		SequenceFileOutputFormat.setOutputCompressorClass(jobConf, SnappyCodec.class);
		SequenceFileOutputFormat.setOutputCompressionType(jobConf, CompressionType.BLOCK);
		
		// Make sure there are exactly 2 parameters left.
		FileInputFormat.setInputPaths(jobConf, inPath);
		FileOutputFormat.setOutputPath(jobConf, new Path(outPath));
		
		System.out.println("Running on " +
				cluster.getTaskTrackers() +
				" nodes to sort from " + 
				FileInputFormat.getInputPaths(jobConf)[0] + " into " +
				FileOutputFormat.getOutputPath(jobConf) +
				" with " + num_reduces + " reduces.");
		Date startTime = new Date();
		System.out.println("Job started: " + startTime);
		jobResult = JobClient.runJob(jobConf);
		Date end_time = new Date();
		System.out.println("Job ended: " + end_time);
		System.out.println("The job took " + 
				(end_time.getTime() - startTime.getTime()) /1000 + " seconds.");
		return 0;
	}



	public static void main(String[] args) throws IOException {
		Configuration conf = new Configuration();
//		String base = "hdfs://boa-njt/";
//		conf.set("fs.default.name", base);
		String base = Properties.getProperty("gh.json.cache.path", DefaultProperties.GH_JSON_CACHE_PATH);
		FileSystem fs = FileSystem.get(conf);
		
		String inPath = "/tmprepcache/";
		StringBuilder sb = new StringBuilder();
		FileStatus[] files = fs.listStatus(new Path(base));
		for (int i = 0; i < files.length; i++) {
			FileStatus file = files[i];
			String name = file.getPath().getName();
			if (name.startsWith("ast-") && name.endsWith(".seq")) {
				try {
//					ToolRunner.run(new Configuration(), new SeqSort(inPath + name, "/tmprepcache/2015-07-sorted/" + name), null);
					sb.append(name + "\n");
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}
		FileIO.writeFileContents(new File("files2sort.txt"), sb.toString());
	}

	/**
	 * Get the last job that was run using this instance.
	 * @return the results of the last job that was run
	 */
	public RunningJob getResult() {
		return jobResult;
	}
}
