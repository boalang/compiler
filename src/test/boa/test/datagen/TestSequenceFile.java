package boa.test.datagen;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.Type;
import boa.datagen.util.ProtoMessageVisitor;
import boa.types.Ast.ASTRoot;
import boa.types.Ast.Declaration;
import boa.types.Code.CodeRepository;
import boa.types.Diff.ChangedFile;
import boa.types.Toplevel.Project;

public class TestSequenceFile {
	private Configuration conf = new Configuration();
	private FileSystem fileSystem;
	private SequenceFile.Reader pr;
	private SequenceFile.Reader ar;
		
	public TestSequenceFile() throws IOException {
		fileSystem = FileSystem.get(conf);
		pr = new SequenceFile.Reader(fileSystem, new Path("dataset/projects.seq"), conf);
		ar = new SequenceFile.Reader(fileSystem, new Path("dataset/data"), conf);
	}
	
	@Test
	public void projectSeqTest() throws IOException {
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
				if (astpos > -1) {
					ar.seek(astpos);
					Writable astkey = new LongWritable();
					val = new BytesWritable();
					ar.next(astkey, val);
					bytes = val.getBytes();
					ASTRoot root = ASTRoot.parseFrom(CodedInputStream.newInstance(bytes, 0, val.getLength()));
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
										decl = getDeclaration(dcf, nodeId, declarations);
									}
									System.out.println(fqn);
									assertEquals(true, decl != null && fqn.equals(decl.getFullyQualifiedName()));
								}
							}
							return true;
						}

						private Declaration getDeclaration(final ChangedFile cf, final int nodeId, final HashMap<Integer, Declaration> declarations) {
							long astpos = cf.getKey();
							if (astpos > -1) {
								try {
									ar.seek(astpos);
									Writable astkey = new LongWritable();
									BytesWritable val = new BytesWritable();
									ar.next(astkey, val);
									byte[] bytes = val.getBytes();
									ASTRoot root = ASTRoot.parseFrom(CodedInputStream.newInstance(bytes, 0, val.getLength()));
									ProtoMessageVisitor v = new ProtoMessageVisitor() {
										private boolean found = false;
										
										@Override
										public boolean preVisit(Message message) {
											if (found)
												return false;
											if (message instanceof Declaration) {
												Declaration temp = (Declaration) message;
												Declaration type = declarations.get(temp.getKey());
												if (type == null) {
													type = Declaration.newBuilder(temp).build();
													declarations.put(type.getKey(), type);
												}
												if (type.getKey() == nodeId) {
													found = true;
													return false;
												}
											}
											return true;
										};
									};
									v.visit(root);
								} catch (IOException e) {}
							}
							return declarations.get(nodeId);
						}
					};
					v.visit(root);
				}
			}
		}
		pr.close();
		ar.close();
	}

	@SuppressWarnings("unused")
	private HashMap<Integer, HashMap<Integer, Declaration>> collectDeclarations(List<ChangedFile> snapshot) throws IOException {
		HashMap<Integer, HashMap<Integer, Declaration>> fileNodeDeclaration = new HashMap<Integer, HashMap<Integer, Declaration>>();
		for (int fileIndex = 0; fileIndex < snapshot.size(); fileIndex++) {
			ChangedFile cf = snapshot.get(fileIndex);
			long astpos = cf.getKey();
			if (astpos > -1) {
				ar.seek(astpos);
				Writable astkey = new LongWritable();
				BytesWritable val = new BytesWritable();
				ar.next(astkey, val);
				byte[] bytes = val.getBytes();
				ASTRoot root = ASTRoot.parseFrom(CodedInputStream.newInstance(bytes, 0, val.getLength()));
				HashMap<Integer, Declaration> nodeDeclaration = collectDeclarations(root);
				fileNodeDeclaration.put(fileIndex, nodeDeclaration);
			}
		}
		return fileNodeDeclaration;
	}

	private HashMap<Integer, Declaration> collectDeclarations(Message message) {
		HashMap<Integer, Declaration> nodeDeclaration = new HashMap<Integer, Declaration>();
		if (message instanceof Declaration) {
			nodeDeclaration.put(((Declaration) message).getKey(), (Declaration) message);
		}
		for (Iterator<Map.Entry<FieldDescriptor, Object>> iter = message.getAllFields().entrySet().iterator(); iter.hasNext();) {
            Map.Entry<FieldDescriptor, Object> field = iter.next();
            nodeDeclaration.putAll(collectDeclarations(field.getKey(), field.getValue()));
        }
		return nodeDeclaration;
	}

	private HashMap<Integer, Declaration> collectDeclarations(FieldDescriptor field, Object value) {
		HashMap<Integer, Declaration> nodeDeclaration = new HashMap<Integer, Declaration>();
        if (field.isRepeated()) {
            // Repeated field. Print each element.
            for (Iterator<?> iter = ((List<?>) value).iterator(); iter.hasNext();)
            	if (field.getType() == Type.MESSAGE)
            		nodeDeclaration.putAll(collectDeclarations((Message) iter.next()));
        }
		return nodeDeclaration;
	}
}
