package boa.test.datagen;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.hamcrest.Matchers;
import org.junit.Test;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.Message;

import boa.datagen.BoaGenerator;
import boa.datagen.DefaultProperties;
import boa.datagen.util.FileIO;
import boa.datagen.util.ProtoMessageVisitor;
import boa.functions.BoaIntrinsics;
import boa.test.datagen.java.Java8BaseTest;
import boa.types.Ast.ASTRoot;
import boa.types.Ast.Declaration;
import boa.types.Code.CodeRepository;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.Person;
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
	public void projectSeqTest1() throws IOException {
		String path = "dataset/temp_data";
		File dataFile = new File(path);
    	path = dataFile.getAbsolutePath();
    	
//    	DefaultProperties.DEBUG = true;
    	DefaultProperties.localDataPath = path;
		
		new FileIO.DirectoryRemover(dataFile.getAbsolutePath()).run();
		
		String[] args = {	"-inputJson", "test/datagen/jsons", 
							"-inputRepo", "dataset/repos",
							"-output", path,
							"-commits", "1",
							"-threads", "2"};
		BoaGenerator.main(args);
		
		openMaps(path);
		fileSystem = FileSystem.get(conf);
		Path projectPath = new Path(path + "/projects.seq"), dataPath = new Path(path + "/ast/data");
		if (fileSystem.exists(projectPath) && fileSystem.exists(dataPath)) {
			pr = new SequenceFile.Reader(fileSystem, projectPath, conf);
			ar = new SequenceFile.Reader(fileSystem, dataPath, conf);
		}
		Set<Long> cfKeys = new HashSet<Long>(), astKeys = new HashSet<Long>();
		Writable key = new Text();
		BytesWritable val = new BytesWritable();
		while (pr.next(key, val)) {
			byte[] bytes = val.getBytes();
			Project project = Project.parseFrom(CodedInputStream.newInstance(bytes, 0, val.getLength()));
			for (CodeRepository cr : project.getCodeRepositoriesList()) {
				for (int i = 0; i < BoaIntrinsics.getRevisionsCount(cr); i++) {
					Revision rev = getRevision(cr, i);
					for (ChangedFile cf : rev.getFilesList()) {
						if (cf.getAst()) {
							if (DefaultProperties.DEBUG) {
								System.out.println(project.getName());
								System.out.println(rev.getId());
								System.out.println(cf);
							}
							assertThat(cfKeys.contains(cf.getKey()), Matchers.is(false));
							cfKeys.add(cf.getKey());
						}
					}
				}
				for (ChangedFile cf : cr.getHeadSnapshotList()) {
					if (cf.getAst()) {
						if (DefaultProperties.DEBUG) {
							System.out.println(project.getName());
							System.out.println(cf);
						}
						assertThat(cfKeys.contains(cf.getKey()), Matchers.is(false));
						cfKeys.add(cf.getKey());
					}
				}
			}
		}
		LongWritable lkey = new LongWritable();
		while (ar.next(lkey, val)) {
			assertThat(astKeys.contains(lkey.get()), Matchers.is(false));
			astKeys.add(lkey.get());
		}
		
		if (DefaultProperties.DEBUG) {
			Set<Long> inter = new HashSet<Long>(astKeys);
			inter.retainAll(cfKeys);
			
			System.out.println("In ASTs: " + astKeys.size());
			astKeys.removeAll(inter);
			System.out.println(astKeys.size());
	//		for (Long k : astKeys)
	//			System.out.println(k + " ");
			System.out.println("In changed files: " + cfKeys.size());
			cfKeys.removeAll(inter);
			System.out.println(cfKeys.size());
	//		for (Long k : cfKeys)
	//			System.out.println(k + " ");
		}
		assertThat(cfKeys, Matchers.is(astKeys));
		
		pr.close();
		ar.close();
		closeMaps();
		
		new FileIO.DirectoryRemover(dataFile.getAbsolutePath()).run();
	}

	private static final Revision emptyRevision;
	
	static {
		Revision.Builder rb = Revision.newBuilder();
		rb.setCommitDate(0);
		Person.Builder pb = Person.newBuilder();
		pb.setUsername("");
		rb.setCommitter(pb);
		rb.setId("");
		rb.setLog("");
		emptyRevision = rb.build();
	}
	
	public static Revision getRevision(final CodeRepository cr, final int index) {
		if (cr.getRevisionKeysCount() > 0) {
			long key = cr.getRevisionKeys(index);
			return getRevision(key);
		}
		return cr.getRevisions(index);
	}
	
	static Revision getRevision(long key) {
		try {
			final BytesWritable value = new BytesWritable();
			if (commitMap.get(new LongWritable(key), value) != null) {
				final CodedInputStream _stream = CodedInputStream.newInstance(value.getBytes(), 0, value.getLength());
				// defaults to 64, really big ASTs require more
				_stream.setRecursionLimit(Integer.MAX_VALUE);
				final Revision root = Revision.parseFrom(_stream);
				return root;
			}
		} catch (final Exception e) {
			e.printStackTrace();
		} catch (final Error e) {
			e.printStackTrace();
		}

		System.err.println("error with revision: " + key);
		return emptyRevision;
	}
	
	private static MapFile.Reader commitMap, astMap;

	private static void openMaps(String path) {
		try {
			final Configuration conf = new Configuration();
			final FileSystem fs;
			fs = FileSystem.getLocal(conf);
			astMap = new MapFile.Reader(fs, new Path(path + "/ast").toString(), conf);
			commitMap = new MapFile.Reader(fs, new Path(path + "/commit").toString(), conf);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private static void closeMaps() {
		closeMap(astMap);
		closeMap(commitMap);
	}
	
	private static void closeMap(MapFile.Reader map) {
		if (map != null)
			try {
				map.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		map = null;
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
