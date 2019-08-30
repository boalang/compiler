package boa.functions.refactoring;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
		args = new String[] { "/Users/hyj/test4", "/Users/hyj/test4" };
		if (args.length < 2) {
			System.err.println("args: INPUT_PATH, OUTPUT_PATH");
		} else {
			
			INPUT_PATH = args[0];
			OUTPUT_PATH = args[1];
			
			Map<String, StringBuilder> refactoringMap = new TreeMap<String, StringBuilder>();
			Map<String, StringBuilder> refactoringIdMap = new TreeMap<String, StringBuilder>();
			
			File dir1 = new File(INPUT_PATH + "/test_output");
//			File dir2 = new File(INPUT_PATH + "/undone_output");
//			File dir3 = new File(INPUT_PATH + "/unundone_output");
//			
			updateMaps(dir1, refactoringMap, refactoringIdMap);
//			updateMaps(dir2, refactoringMap, refactoringIdMap);
//			updateMaps(dir3, refactoringMap, refactoringIdMap);
			
			// write refactorings into sequence files
			Configuration conf = new Configuration();
			FileSystem fileSystem = FileSystem.get(conf);
			CompressionType compressionType = CompressionType.BLOCK;
			CompressionCodec compressionCode = new DefaultCodec();
			MapFile.Writer refactoringWriter = new MapFile.Writer(conf, fileSystem, OUTPUT_PATH + "/refactoring", Text.class,
					BytesWritable.class, compressionType, compressionCode, null);
			MapFile.Writer hasRefactoringWriter = new MapFile.Writer(conf, fileSystem, OUTPUT_PATH + "/refactoringId", Text.class,
					BytesWritable.class, compressionType, compressionCode, null);			
			for (Entry<String, StringBuilder> entry : refactoringMap.entrySet()) {
				String commitRefactorings = entry.getValue().toString();
				byte[] data = commitRefactorings.getBytes(StandardCharsets.UTF_8);
				refactoringWriter.append(new Text(entry.getKey()), new BytesWritable(data));
//				System.out.println("appending refactoring");
			}
			for (Entry<String, StringBuilder> entry : refactoringIdMap.entrySet()) {
				String commitIds = entry.getValue().toString();
				byte[] data = commitIds.getBytes(StandardCharsets.UTF_8);
				hasRefactoringWriter.append(new Text(entry.getKey()), new BytesWritable(data));
//				System.out.println("appending hasRefactoring");
			}
			refactoringWriter.close();
			hasRefactoringWriter.close();
		}
		
	}

	private static void updateMaps(File dir, Map<String, StringBuilder> refactoringMap,
			Map<String, StringBuilder> refactoringIdMap) {
		String previous = "";
		for (File file : dir.listFiles()) {
			
			String input = FileIO.readFileContents(file);
			String[] refactorings = input.split("\\r?\\n");
			System.out.println(file.getName() + " start");
			for (String rf : refactorings) {
				if (rf != null && !rf.equals("") ) {
					int idx = rf.indexOf('=');
					if (idx != -1) {
						// key: bluesoft-rnd/aperte-workflow-core 1ef031d4656da761f2be2df171dcde581d7c2e29 Move Attribute
						String lhs = rf.substring(0, idx);
						String[] splits = lhs.split(" ");
						String projectName = splits[0];
						String commitId = splits[1];
						// key: bluesoft-rnd/aperte-workflow-core 1ef031d4656da761f2be2df171dcde581d7c2e29
						String key = projectName + " " + commitId;
						String value = rf.substring(idx + 1);
						if (!refactoringMap.containsKey(key)) {
							refactoringMap.put(key, new StringBuilder(value));
						} else {
							refactoringMap.get(key).append("\n" + value);
						}
						if (!refactoringIdMap.containsKey(projectName)) {
							refactoringIdMap.put(projectName, new StringBuilder(commitId));
							previous = commitId;
						} else {
							if (!previous.equals(commitId)) {
								refactoringIdMap.get(projectName).append("\n" + commitId);
								previous = commitId;
							}
						}
					}
				}
			}
			System.out.println(file.getName() + " end");
		}
	}

}
