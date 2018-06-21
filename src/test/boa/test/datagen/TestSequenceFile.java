package boa.test.datagen;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import java.io.IOException;
import java.util.HashMap;
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
import boa.test.datagen.java.Java8BaseTest;
import boa.types.Ast.ASTRoot;
import boa.types.Ast.Declaration;
import boa.types.Code.CodeRepository;
import boa.types.Diff.ChangedFile;
import boa.types.Toplevel.Project;

public class TestSequenceFile extends Java8BaseTest {
	private Configuration conf = new Configuration();
	private FileSystem fileSystem;
	private SequenceFile.Reader pr;
	private SequenceFile.Reader ar;
		
	public TestSequenceFile() throws IOException {
		fileSystem = FileSystem.get(conf);
		Path projectPath = new Path("dataset/projects.seq"), dataPath = new Path("dataset/data");
		if (fileSystem.exists(projectPath) && fileSystem.exists(dataPath)) {
			pr = new SequenceFile.Reader(fileSystem, projectPath, conf);
			ar = new SequenceFile.Reader(fileSystem, dataPath, conf);
		}
	}
	
	@Test
	public void projectSeqTest() throws IOException {
		if (pr == null || ar == null)
			return;
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
//			HashMap<Integer, HashMap<Integer, Declaration>> fileNodeDeclaration = collectDeclarations(cr.getHeadSnapshotList());
			final HashMap<Integer, HashMap<Integer, Declaration>> fileNodeDeclaration = new HashMap<Integer, HashMap<Integer, Declaration>>();
			for (ChangedFile cf : cr.getHeadSnapshotList()) {
				long astpos = cf.getKey();
				if (cf.getAst() && astpos > -1) {
					ar.seek(astpos);
					Writable astkey = new LongWritable();
					val = new BytesWritable();
					ar.next(astkey, val);
					bytes = val.getBytes();
					CodedInputStream cis = CodedInputStream.newInstance(bytes, 0, val.getLength());
					cis.setRecursionLimit(Integer.MAX_VALUE);
					ASTRoot root = ASTRoot.parseFrom(cis);
//					System.out.println(root);
					ProtoMessageVisitor v = new ProtoMessageVisitor() {
						@Override
						public boolean preVisit(Message message) {
							if (message instanceof boa.types.Ast.Type) {
								boa.types.Ast.Type type = (boa.types.Ast.Type) message;
								String fqn = type.getFullyQualifiedName();
								final int fileId = type.getDeclarationFile(), nodeId = type.getDeclaration();
								if (fqn != null && !fqn.isEmpty() && fileId > 0) {
									Declaration decl = null;
									HashMap<Integer, Declaration> declarations = fileNodeDeclaration.get(fileId);
									if (declarations != null) {
										decl = declarations.get(nodeId);
									} else {
										declarations = new HashMap<Integer, Declaration>();
										fileNodeDeclaration.put(fileId, declarations);
									}
									if (decl == null) {
										ChangedFile dcf = cr.getHeadSnapshot(fileId);
										decl = getDeclaration(ar, dcf, nodeId, declarations);
									}
									System.out.println(fqn);
									assertEquals(true, decl != null && fqn.equals(decl.getFullyQualifiedName()));
								}
							}
							return true;
						}
					};
					v.visit(root);
				}
			}
		}
		pr.close();
		ar.close();
	}
}
