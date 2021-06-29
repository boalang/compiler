package boa.test.datagen.queries;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.junit.Before;

import boa.datagen.DefaultProperties;
import boa.datagen.forges.github.RepositoryCloner;
import boa.datagen.scm.GitConnector;
import boa.datagen.util.FileIO;
import boa.evaluator.BoaEvaluator;


public abstract class QueryTest {
	static Map<String, ObjectId> filePathGitObjectIds = new HashMap<String, ObjectId>();
	protected static final ByteArrayOutputStream buffer = new ByteArrayOutputStream(4096);
	private static Repository repository;
	private static RevWalk revwalk;
	
	@Before
	public void prep() {
		File outputDir = new File("test/datagen/temp_output");
		if (outputDir.exists()) {
			try {
				FileUtils.deleteDirectory(outputDir);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public String getResults(File outputDir) {
		for (final File f : outputDir.listFiles()) {
			if (f.getName().startsWith("part")) {
				return FileIO.readFileContents(f);
			}
		}
		return "";
	}

	public void queryTest(String inputPath, String expected) {
		String[] args = { "-i", inputPath, "-d", "test/datagen/test_datagen", "-o", "test/datagen/temp_output" };
		BoaEvaluator.main(args);
		File outputDir = new File("test/datagen/temp_output");
		String actual = getResults(outputDir);
		try {
			FileUtils.deleteDirectory(outputDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertEquals(expected, actual);
	}
	
	protected static String getFileContents(final ObjectId fileid) {
		//ObjectId fileid = filePathGitObjectIds.get(path);
		try {
			buffer.reset();
			buffer.write(repository.open(fileid, Constants.OBJ_BLOB).getCachedBytes());
		} catch (final Throwable e) {
		}
		return buffer.toString();
	}
	
	public List<String> setPaths() throws IOException {
		File gitDir = new File("test/datagen/boalang/repos/boalang/test-datagen");
		if (!gitDir.exists()) {
			String url = "https://github.com/boalang/test-datagen.git";
			try {
				RepositoryCloner.clone(new String[] { url, gitDir.getAbsolutePath() });
			} catch (InvalidRemoteException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (TransportException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (GitAPIException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		repository = new FileRepositoryBuilder().setGitDir(new File(gitDir + "/.git")).build();

		GitConnector gc = new GitConnector(gitDir.getAbsolutePath(), "test-datagen");
		gc.setRevisions();
		System.out.println("Finish processing commits");
		System.out.println("Finish building head snapshot");
		List<String> snapshot2 = gc.getSnapshot(Constants.HEAD);
		gc.close();
		revwalk = new RevWalk(repository);
		revwalk.reset();
		Set<RevCommit> heads = getHeads();
		revwalk.markStart(heads);
		revwalk.sort(RevSort.TOPO, true);
		revwalk.sort(RevSort.COMMIT_TIME_DESC, true);
		revwalk.sort(RevSort.REVERSE, true);

		for (RevCommit rc : revwalk) {
			TreeWalk tw = new TreeWalk(repository);
			tw.reset();
			try {
				tw.addTree(rc.getTree());
				tw.setRecursive(true);
				while (tw.next())
					if (!tw.isSubtree())
						filePathGitObjectIds.put(tw.getPathString(), tw.getObjectId(0));
			} catch (IOException e) {
			}
			tw.close();
		}
		return snapshot2;
	}
	
	protected void visitPath(String path, org.eclipse.jdt.core.dom.ASTVisitor visitor) {
		ObjectId oi = filePathGitObjectIds.get(path);
		final org.eclipse.jdt.core.dom.ASTParser parser = org.eclipse.jdt.core.dom.ASTParser.newParser(DefaultProperties.DEFAULT_JAVA_ASTLEVEL);
		parser.setKind(org.eclipse.jdt.core.dom.ASTParser.K_COMPILATION_UNIT);
		final String content = getFileContents(oi);

		parser.setSource(content.toCharArray());

		final Map<String, String> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
		parser.setCompilerOptions(options);

		CompilationUnit cu = null;

		try {
			cu = (CompilationUnit) parser.createAST(null);
		} catch (Throwable e) {
		}
		
		cu.accept(visitor);
	}
	
	private static Set<RevCommit> getHeads() {
		Git git = new Git(repository);
		Set<RevCommit> heads = new HashSet<RevCommit>();
		try {
			for (final Ref ref : git.branchList().call()) {
				heads.add(revwalk.parseCommit(repository.resolve(ref.getName())));
			}
		} catch (final GitAPIException e) {
		}catch (final IOException e) {
		}
		git.close();
		return heads;
	}
}
