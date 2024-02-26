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
import boa.functions.BoaIntrinsics;
import boa.test.datagen.java.JavaBaseTest;
import boa.types.Ast.ASTRoot;
import boa.types.Ast.Declaration;
import boa.types.Code.CodeRepository;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.Person;
import boa.types.Toplevel.Project;

public class TestSequenceFile extends JavaBaseTest {
	private final Configuration conf = new Configuration();
	private FileSystem fileSystem;
	private SequenceFile.Reader pr;
	private SequenceFile.Reader ar;

	public TestSequenceFile() throws IOException {
		fileSystem = FileSystem.get(conf);
		final Path projectPath = new Path("dataset/projects.seq");
		final Path dataPath = new Path("dataset/data");
		if (fileSystem.exists(projectPath) && fileSystem.exists(dataPath)) {
			pr = new SequenceFile.Reader(fileSystem, projectPath, conf);
			ar = new SequenceFile.Reader(fileSystem, dataPath, conf);
		}
	}

	@Test
	public void projectSeqTest1() throws IOException {
		final File dataFile = new File("dataset/temp_data");
    	final String path = dataFile.getAbsolutePath();

//    	DefaultProperties.DEBUG = true;
    	DefaultProperties.localDataPath = path;

		new FileIO.DirectoryRemover(dataFile.getAbsolutePath()).run();

		final String[] args = {	"-inputJson", "test/datagen/jsons",
							"-inputRepo", "dataset/repos",
							"-output", path,
							"-commits", "1",
							"-threads", "2"};
		BoaGenerator.main(args);

		openMaps(path);
		fileSystem = FileSystem.get(conf);
		final Path projectPath = new Path(path + "/projects.seq");
		final Path dataPath = new Path(path + "/ast/data");
		if (fileSystem.exists(projectPath) && fileSystem.exists(dataPath)) {
			pr = new SequenceFile.Reader(fileSystem, projectPath, conf);
			ar = new SequenceFile.Reader(fileSystem, dataPath, conf);
		}
		final Set<Long> cfKeys = new HashSet<Long>();
		final Set<Long> astKeys = new HashSet<Long>();
		final Writable key = new Text();
		final BytesWritable val = new BytesWritable();
		while (pr.next(key, val)) {
			final byte[] bytes = val.getBytes();
			final Project project = Project.parseFrom(CodedInputStream.newInstance(bytes, 0, val.getLength()));
			for (final CodeRepository cr : project.getCodeRepositoriesList()) {
				for (int i = 0; i < BoaIntrinsics.getRevisionsCount(cr); i++) {
					final Revision rev = getRevision(cr, i);
					for (final ChangedFile cf : rev.getFilesList()) {
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
				for (final ChangedFile cf : cr.getHeadSnapshotList()) {
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
		final LongWritable lkey = new LongWritable();
		while (ar.next(lkey, val)) {
			assertThat(astKeys.contains(lkey.get()), Matchers.is(false));
			astKeys.add(lkey.get());
		}

		if (DefaultProperties.DEBUG) {
			final Set<Long> inter = new HashSet<Long>(astKeys);
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
		final Revision.Builder rb = Revision.newBuilder();
		rb.setCommitDate(0);
		final Person.Builder pb = Person.newBuilder();
		pb.setUsername("");
		rb.setCommitter(pb);
		rb.setId("");
		rb.setLog("");
		emptyRevision = rb.build();
	}

	public static Revision getRevision(final CodeRepository cr, final int index) {
		if (cr.getRevisionKeysCount() > 0) {
			final long key = cr.getRevisionKeys(index);
			return getRevision(key);
		}
		return cr.getRevisions(index);
	}

	static Revision getRevision(final long key) {
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

	private static MapFile.Reader commitMap;
	private static MapFile.Reader astMap;

	private static void openMaps(final String path) {
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
		astMap = null;
		closeMap(commitMap);
		commitMap = null;
	}

	private static void closeMap(final MapFile.Reader map) {
		if (map != null)
			try {
				map.close();
			} catch (final IOException e) {
				e.printStackTrace();
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
			final Project project = Project.parseFrom(CodedInputStream.newInstance(bytes, 0, val.getLength()));
			final String name = project.getName();
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
			for (final ChangedFile cf : cr.getHeadSnapshotList()) {
				long astpos = cf.getKey();
				if (cf.getAst() && astpos > -1) {
					ar.seek(astpos);
					final Writable astkey = new LongWritable();
					val = new BytesWritable();
					ar.next(astkey, val);
					bytes = val.getBytes();
					final CodedInputStream cis = CodedInputStream.newInstance(bytes, 0, val.getLength());
					cis.setRecursionLimit(Integer.MAX_VALUE);
					final ASTRoot root = ASTRoot.parseFrom(cis);
//					System.out.println(root);
					final ProtoMessageVisitor v = new ProtoMessageVisitor() {
						@Override
						public boolean preVisit(final Message message) {
							if (message instanceof boa.types.Ast.Type) {
								final boa.types.Ast.Type type = (boa.types.Ast.Type) message;
								final String fqn = type.getFullyQualifiedName();
								final int fileId = type.getDeclarationFile();
								final int nodeId = type.getDeclaration();
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

	protected static Declaration getDeclaration(final SequenceFile.Reader ar, final ChangedFile cf, final int nodeId, final HashMap<Integer, Declaration> declarations) {
		long astpos = cf.getKey();
		if (cf.getAst() && astpos > -1) {
			try {
				ar.seek(astpos);
				final Writable astkey = new LongWritable();
				final BytesWritable val = new BytesWritable();
				ar.next(astkey, val);
				final byte[] bytes = val.getBytes();
				final ASTRoot root = ASTRoot.parseFrom(CodedInputStream.newInstance(bytes, 0, val.getLength()));
				final ProtoMessageVisitor v = new ProtoMessageVisitor() {
					private boolean found = false;

					@Override
					public boolean preVisit(final Message message) {
						if (found)
							return false;
						if (message instanceof Declaration) {
							final Declaration temp = (Declaration) message;
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
			} catch (final IOException e) {}
		}
		return declarations.get(nodeId);
	}
}
