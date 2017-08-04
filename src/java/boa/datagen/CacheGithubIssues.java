package boa.datagen;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import boa.datagen.forges.github.IssueMetaData;
import boa.datagen.util.FileIO;
import boa.datagen.util.Properties;
import boa.types.Issues.Issue;

public class CacheGithubIssues {
	final static String jsonPath = Properties.getProperty("gh.issue.path", DefaultProperties.GH_ISSUE_PATH);
	final static String jsonCachePath = Properties.getProperty("gh.json.cache.path",
			DefaultProperties.GH_JSON_CACHE_PATH);
	private static Configuration conf;
	private static FileSystem fileSystem;
	private static String suffix = "";
	private static SequenceFile.Writer issueWriter;

	public static void main(String[] args) {
		conf = new Configuration();
		try {
			fileSystem = FileSystem.get(conf);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		File output = new File(jsonCachePath);
		if (!output.exists())
			output.mkdirs();
		CacheGithubIssues.openWriters();
		HashMap<String, byte[]> repos = new HashMap<String, byte[]>();
		File dir = new File(jsonPath + "/issues");
		for (File file : dir.listFiles()) {
			if (file.getName().endsWith(".json")) {
				String[] filePathName = file.getAbsolutePath().split("/");
				String[] fileName = filePathName[filePathName.length - 1].split("-");
				String projectId = fileName[0];
				System.out.println(projectId);
				String content = FileIO.readFileContents(file);
				Gson parser = new Gson();
				JsonArray repoArray = parser.fromJson(content, JsonElement.class).getAsJsonArray();
				for (int i = 0; i < repoArray.size(); i++) {
					IssueMetaData repo = new IssueMetaData(repoArray.get(i).getAsJsonObject());
					if (repo.id != null && repo.summary != null) {
						Issue protobufRepo = repo.toBoaMetaDataProtobuf();
						// System.out.println(jRepo.toString());
						repos.put(repo.id, protobufRepo.toByteArray());
					}
					System.out.println(repos.size() + ": " + file.getName());
				}
				try {
					issueWriter.append(new Text(projectId), new BytesWritable(repos.toString().getBytes()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		CacheGithubIssues.closeWriters();
	}

	private static void openWriters() {
		long time = System.currentTimeMillis() / 1000;
		suffix = "-" + time + ".seq";
		while (true) {
			try {
				issueWriter = SequenceFile.createWriter(fileSystem, conf, new Path(jsonCachePath + "/issues" + suffix),
						Text.class, BytesWritable.class);
				break;
			} catch (Throwable t) {
				t.printStackTrace();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	private static void closeWriters() {
		while (true) {
			try {
				issueWriter.close();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				fileSystem.delete(new Path(new Path(jsonCachePath), "." + "issues" + suffix + ".crc"), false);
				break;
			} catch (Throwable t) {
				t.printStackTrace();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		}
	}

}
