package boa.test.datagen.queries;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.junit.Test;

import boa.datagen.forges.github.RepositoryCloner;
import boa.datagen.scm.GitConnector;
import boa.test.datagen.queries.TestQ15.JavaVoidMethodCheckVisitor;
import boa.types.Diff.ChangedFile;

public class TestQ18 extends QueryTest {
	static Map<String, ObjectId> filePathGitObjectIds = new HashMap<String, ObjectId>();
	protected static final ByteArrayOutputStream buffer = new ByteArrayOutputStream(4096);
	private static Repository repository;
	private static RevWalk revwalk;

	@Test
	public void testQ18() throws MissingObjectException, IncorrectObjectTypeException, IOException {
		int thisKeyword = 0;
		int thisMax = 0;
		int thisMin = Integer.MAX_VALUE;
		int projectId = 140492550;
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
		List<ChangedFile> snapshot1 = gc.buildHeadSnapshot();
		System.out.println("Finish building head snapshot");
		List<String> snapshot2 = gc.getSnapshot(Constants.HEAD);
		gc.close();
		Set<String> s1 = new HashSet<String>();
		for (ChangedFile cf : snapshot1)
			s1.add(cf.getName());

		revwalk = new RevWalk(repository);
		revwalk.reset();
		Set<RevCommit> heads = getHeads();
		revwalk.markStart(heads);
		revwalk.sort(RevSort.TOPO, true);
		revwalk.sort(RevSort.COMMIT_TIME_DESC, true);
		revwalk.sort(RevSort.REVERSE, true);
		String expected = "";
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
		int methods = 0;
		for (String path : snapshot2) {
			ObjectId oi = filePathGitObjectIds.get(path);
			final org.eclipse.jdt.core.dom.ASTParser parser = org.eclipse.jdt.core.dom.ASTParser.newParser(AST.JLS8);
			parser.setKind(org.eclipse.jdt.core.dom.ASTParser.K_COMPILATION_UNIT);
			final String content = getFileContents(oi);

			parser.setSource(content.toCharArray());

			final Map<?, ?> options = JavaCore.getOptions();
			JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
			parser.setCompilerOptions(options);

			CompilationUnit cu = null;

			try {
				cu = (CompilationUnit) parser.createAST(null);
			} catch (Throwable e) {
			}
			JavaVoidThisCheckVisitor visitor = new JavaVoidThisCheckVisitor();
			cu.accept(visitor);
			int thisCount = visitor.thisKeyword;
			thisKeyword += thisCount;
			if (thisMax < visitor.maxThis)
				thisMax = visitor.maxThis;
			if (thisCount > 0 && thisMin > visitor.minThis)
				thisMin = visitor.minThis;
			methods += visitor.methods;
		}
		double mean = (double) thisKeyword / methods;
		expected += "ThisMax[] = " + projectId + ", " + (double) thisMax 
				+ "\nThisMean[] = " + mean
				+ "\nThisMin[] = " + projectId + ", " + (double) thisMin 
				+ "\nThisTotal[] = " + thisKeyword + "\n";
		queryTest("test/known-good/q18.boa", expected);
	}

	private static Set<RevCommit> getHeads() {
		Git git = new Git(repository);
		Set<RevCommit> heads = new HashSet<RevCommit>();
		try {
			for (final Ref ref : git.branchList().call()) {
				heads.add(revwalk.parseCommit(repository.resolve(ref.getName())));
			}
		} catch (final GitAPIException e) {
		} catch (final IOException e) {
		}
		git.close();
		return heads;
	}

	protected static String getFileContents(final ObjectId fileid) {
		// ObjectId fileid = filePathGitObjectIds.get(path);
		try {
			buffer.reset();
			buffer.write(repository.open(fileid, Constants.OBJ_BLOB).getCachedBytes());
		} catch (final Throwable e) {
		}
		return buffer.toString();
	}

	public class JavaVoidThisCheckVisitor extends ASTVisitor {
		public int thisKeyword = 0;
		public int this2 = 0;
		public int methods = 0;
		public int maxThis = 0;
		public int minThis = Integer.MAX_VALUE;
		private Stack<Integer> thisStack = new Stack<Integer>();

		@Override
		public void endVisit(AnnotationTypeMemberDeclaration node) {
			methods++;
			if (maxThis < this2)
				maxThis = this2;
			if (minThis > this2)
				minThis = this2;
			this2 = thisStack.pop();
		}

		@Override
		public boolean visit(AnnotationTypeMemberDeclaration node) {
			thisStack.push(this2);
			this2 = 0;
			return true;
		}
		@Override
		public void endVisit(MethodDeclaration node) {
			methods++;
			if (maxThis < this2)
				maxThis = this2;
			if (minThis > this2)
				minThis = this2;
			this2 = thisStack.pop();
		}

		@Override
		public boolean visit(MethodDeclaration node) {
			thisStack.push(this2);
			this2 = 0;
			return true;
		}
		
		
		public boolean visit(ThisExpression node) {
			thisKeyword ++;
			this2++;
			return true;
		}
	}
}
