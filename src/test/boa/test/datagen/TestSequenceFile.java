package boa.test.datagen;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.hamcrest.*;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.junit.Test;

import com.google.protobuf.CodedInputStream;

import boa.types.Ast.ASTRoot;
import boa.types.Code.CodeRepository;
import boa.types.Diff.ChangedFile;
import boa.types.Toplevel.Project;

public class TestSequenceFile {

	@Test
	public void astPrint() throws IOException{
		Configuration conf = new Configuration();
		FileSystem fileSystem = FileSystem.get(conf);
		Writable key = new LongWritable();
	//	Writable key = new Text();
		BytesWritable val = new BytesWritable();
		SequenceFile.Reader ar = new SequenceFile.Reader(fileSystem, new Path("dataset/data"), conf);
		//WritableComparable key = (WritableComparable) r.getKeyClass().newInstance();
		//System.out.println("Key class is: " + key.getClass().getName());
		while (ar.next(key, val)) {
		//	System.out.println("key is " + key);
		//	System.out.println("next ast");
			byte[] bytes = val.getBytes();
		//	System.out.print("Parse after writing to sequence file: ");
			//System.out.println(ASTRoot.parseFrom(bytes).getImportsList());
			ASTRoot root = ASTRoot.parseFrom(CodedInputStream.newInstance(bytes, 0, val.getLength()));
			System.out.println(root);
		}
		ar.close();
	}
	
	@Test
	public void projectSeqTest() throws IOException{
		Configuration conf = new Configuration();
		FileSystem fileSystem = FileSystem.get(conf);
		Writable key = new Text();
		BytesWritable val = new BytesWritable();
		SequenceFile.Reader pr = new SequenceFile.Reader(fileSystem, new Path("dataset/projects.seq"), conf);
		SequenceFile.Reader ar = new SequenceFile.Reader(fileSystem, new Path("dataset/data"), conf);
		while (pr.next(key, val)) {
			byte[] bytes = val.getBytes();
			Project project = Project.parseFrom(CodedInputStream.newInstance(bytes, 0, val.getLength()));
			String name = project.getName();
			System.out.println(name);
			assertThat(name, anyOf(is("junit-team/junit4"), is("boalang/compiler"), is("candoia/candoia")));
			assertFalse(project.getForked());
			System.out.println("Forked " + project.getForked());
			System.out.println(project.getProgrammingLanguagesList().toString());
			System.out.println(project.getProgrammingLanguagesLocsList().toString());
			assertThat(project.getProgrammingLanguagesList().toString(), containsString("Java"));
			System.out.println("Stars " + project.getStars());
			assertTrue(project.getStars() > -1);
			System.out.println("Forks " + project.getForks());
			assertTrue(project.getForks() > -1);
			assertTrue(project.getCreatedDate() > -1);
			System.out.println(project.getCreatedDate());
			System.out.println(project.getHomepageUrl());
			System.out.println(project.getProjectUrl());
			System.out.println(project.getDescription() + "\n");
			CodeRepository cr = project.getCodeRepositories(0);
			for (ChangedFile cf : cr.getHeadSnapshotList()) {
				long astkey = cf.getKey();
				if (astkey > -1) {
					ar.seek(astkey);
					key = new LongWritable();
					val = new BytesWritable();
					ar.next(key, val);
					bytes = val.getBytes();
					ASTRoot root = ASTRoot.parseFrom(CodedInputStream.newInstance(bytes, 0, val.getLength()));
					System.out.println(root);
				}
			}
		}
		pr.close();
		ar.close();
	}
}
