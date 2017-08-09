package boa.test.datagen.java;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.junit.Test;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.Message;
import boa.datagen.util.ProtoMessageVisitor;
import boa.types.Ast.ASTRoot;
import boa.types.Code.CodeRepository;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.ChangeKind;
import boa.types.Toplevel.Project;

public class TestDiff extends Java8BaseTest {
	private Configuration conf = new Configuration();
	private FileSystem fileSystem;
	private SequenceFile.Reader pr;
	private SequenceFile.Reader ar;
		
	public TestDiff() throws IOException {
		fileSystem = FileSystem.get(conf);
		pr = new SequenceFile.Reader(fileSystem, new Path("dataset/projects.seq"), conf);
		ar = new SequenceFile.Reader(fileSystem, new Path("dataset/data"), conf);
	}
	
	@Test
	public void testAstDiff() throws IOException {
		Writable key = new Text();
		BytesWritable val = new BytesWritable();
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
			final CodeRepository cr = project.getCodeRepositories(0);
			assertTrue(cr.getHeadSnapshotCount() > 0);
			for (final Revision r : cr.getRevisionsList()) {
				for (final ChangedFile cf : r.getFilesList()) {
					long astpos = cf.getKey();
					if (astpos > -1 && cf.getChange() == ChangeKind.MODIFIED && cf.getPreviousIndicesCount() == 1) {
						long mappedKey = cf.getMappedKey();
						if (mappedKey == -1)
							continue;
						assertNotEquals(0, mappedKey);
						ar.seek(astpos);
						Writable astkey = new LongWritable();
						val = new BytesWritable();
						ar.next(astkey, val);
						bytes = val.getBytes();
						ASTRoot root = ASTRoot.parseFrom(CodedInputStream.newInstance(bytes, 0, val.getLength()));
						ar.seek(mappedKey);
						ar.next(astkey, val);
						bytes = val.getBytes();
						final ASTRoot preroot = ASTRoot.parseFrom(CodedInputStream.newInstance(bytes, 0, val.getLength()));
						ProtoMessageVisitor v = new ProtoMessageVisitor() {
							@Override
							public boolean preVisit(Message message) {
								Object v = getFieldValue(message, "change_kind");
								if (v != null) {
									String kind = ((com.google.protobuf.Descriptors.EnumValueDescriptor) v).getName();
					            	if (!kind.equals(ChangeKind.UNCHANGED.name()) && !kind.equals(ChangeKind.MODIFIED.name()) && !kind.equals(ChangeKind.UNKNOWN.name())) {
//					            		System.out.println(r.getId() + " " + cf.getName());
//					            		System.out.println(message);
				            			Integer mappedNode = (Integer) getFieldValue(message, "mapped_node");
				            			if (mappedNode != null) {
					            			Message mappedMessage = getMessage(preroot, mappedNode);
					            			if (mappedMessage != null) {
//					            				if (!message.getClass().equals(mappedMessage.getClass()))
//					            					System.err.println();
						            			assertEquals(message.getClass(), mappedMessage.getClass());
						            			Object preV = getFieldValue(mappedMessage, "change_kind");
						            			assertEquals(v, preV);
	//					            			System.out.println(mappedMessage);
	//					            			System.out.println("===================");
					            			}
					            		}
					            	}
								}
								return true;
							}
						};
						v.visit(root);
					}
				}
			}
		}
		pr.close();
		ar.close();
	}
}
