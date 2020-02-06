package boa.functions.refactoring;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import boa.datagen.util.FileIO;
import boa.types.Code.Change;

public class BoaRefactoringSeqGenerator {

	private static String OUTPUT_PATH;
	private static String INPUT_PATH;

	public static void main(String[] args) throws IOException {
//		args = new String[] { "/Users/hyj/test6", "/Users/hyj/test6" };
//		args = new String[] { "/Users/hyj/git/BoaData/DataSet/aa", "/Users/hyj/git/BoaData/DataSet/aa" };
		args = new String[] { "/Users/hyj/hpc_repo_json/detect/output", "/Users/hyj/hpc_repo_json/detect/output" };
		if (args.length < 2) {
			System.err.println("args: INPUT_PATH, OUTPUT_PATH");
		} else {
			
			INPUT_PATH = args[0];
			OUTPUT_PATH = args[1];
			
			Map<String, boa.types.Code.Change> refactoringMap = new TreeMap<String, boa.types.Code.Change>();
			Map<String, StringBuilder> refactoringIdMap = new TreeMap<String, StringBuilder>();
			
			File dir1 = new File(INPUT_PATH);
//			File dir1 = new File(INPUT_PATH + "/output");
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
			for (Entry<String, boa.types.Code.Change> entry : refactoringMap.entrySet()) {
				refactoringWriter.append(new Text(entry.getKey()), new BytesWritable(entry.getValue().toByteArray()));
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

	private static void updateMaps(File dir, Map<String, Change> refactoringMap,
			Map<String, StringBuilder> refactoringIdMap) {
		String previous = "";
		for (File file : dir.listFiles()) {
			if (!file.getName().endsWith(".json"))
				continue;
			String input = FileIO.readFileContents(file);
			JsonParser parser = new JsonParser();
			JsonElement jsonTree = parser.parse(input);
			JsonObject jsonObject = jsonTree.getAsJsonObject();
			JsonElement je = jsonObject.get("commits");
			if (je.isJsonArray()) {
				JsonArray commits = je.getAsJsonArray();
				if (commits.size() > 0) {
					for (JsonElement commmitJE : commits) {
						JsonObject commit = commmitJE.getAsJsonObject();
						String projectName = commit.get("project_name").getAsString();
						String commitId = commit.get("sha1").getAsString();
						boa.types.Code.Change.Builder cb = boa.types.Code.Change.newBuilder();
						cb.addAllRefactorings(getRefactorings(commit.get("refactorings").getAsJsonArray()));
						String key = projectName + " " + commitId;
						refactoringMap.put(key, cb.build());
						
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

	private static List<boa.types.Code.CodeRefactoring> getRefactorings(JsonArray refJA) {
		List<boa.types.Code.CodeRefactoring> refactorings = new ArrayList<boa.types.Code.CodeRefactoring>();
		for (JsonElement refJE : refJA) {
			JsonObject refactoring = refJE.getAsJsonObject();
			boa.types.Code.CodeRefactoring.Builder crb = boa.types.Code.CodeRefactoring.newBuilder();
			crb.setType(refactoring.get("type").getAsString());
			crb.setDescription(refactoring.get("description").getAsString());
			crb.addAllLeftSideLocations(getLoctions(refactoring.get("leftSideLocations").getAsJsonArray()));
			crb.addAllRightSideLocations(getLoctions(refactoring.get("leftSideLocations").getAsJsonArray()));
			refactorings.add(crb.build());
		}
		return refactorings;
	}
	
	private static List<boa.types.Code.Location> getLoctions(JsonArray locationJA) {
		List<boa.types.Code.Location> locations = new ArrayList<boa.types.Code.Location>();
		JsonObject location = locationJA.get(0).getAsJsonObject();
		boa.types.Code.Location.Builder lb = boa.types.Code.Location.newBuilder();
		lb.setFilePath(location.get("filePath").getAsString());
		lb.setCodeElement(location.get("codeElement").getAsString());
		lb.setCodeElementType(location.get("codeElementType").getAsString());
		lb.setDescription(location.get("description").getAsString());
		locations.add(lb.build());
		return locations;
	}
}
