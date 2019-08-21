package boa.functions.refactoring;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.DefaultCodec;

import boa.datagen.util.FileIO;

public class BoaRefactoringSeqGenerator {

	private static String OUTPUT_PATH;
	private static String INPUT_PATH;

	public static void main(String[] args) throws IOException {
//		args = new String[] { "/Users/hyj/test4/output", "/Users/hyj/test4" };
		if (args.length < 2) {
			System.err.println("args: INPUT_PATH, OUTPUT_PATH");
		} else {
			
			INPUT_PATH = args[0];
			OUTPUT_PATH = args[1];
			File dir = new File(INPUT_PATH);
			
			Map<String, StringBuilder> map = new TreeMap<String, StringBuilder>();
			
			for (File file : dir.listFiles()) {
				
				String input = FileIO.readFileContents(file);
				String[] refactorings = input.split("\\r?\\n");
				System.out.println(file.getName() + " start");
				for (String rf : refactorings) {
					System.out.println("get " + rf);
					if (rf != null && !rf.equals("") ) {
						int idx = rf.indexOf('=');
						if (idx != -1) {
							// key: bluesoft-rnd/aperte-workflow-core 1ef031d4656da761f2be2df171dcde581d7c2e29
							String key = rf.substring(0, idx);
							String value = rf.substring(idx + 1);
							if (!map.containsKey(key))
								map.put(key, new StringBuilder(value));
							else
								map.get(key).append("\n" + value);
						}
					}
				}
				System.out.println(file.getName() + " end");
				
			}
			Configuration conf = new Configuration();
			FileSystem fileSystem = FileSystem.get(conf);
			CompressionType compressionType = CompressionType.BLOCK;
			CompressionCodec compressionCode = new DefaultCodec();
			MapFile.Writer repoWriter = new MapFile.Writer(conf, fileSystem, OUTPUT_PATH + "/refactorings", Text.class,
					BytesWritable.class, compressionType, compressionCode, null);			
			for (Entry<String, StringBuilder> entry : map.entrySet()) {
				repoWriter.append(new Text(entry.getKey()), new BytesWritable(entry.getValue().toString().getBytes()));
				System.out.println("appending");
			}
			repoWriter.close();
		}
		
	}

}
