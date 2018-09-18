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
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TryStatement;
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
import boa.types.Diff.ChangedFile;

public class TestQ25 extends QueryTest {
	static Map<String, ObjectId> filePathGitObjectIds = new HashMap<String, ObjectId>();
	protected static final ByteArrayOutputStream buffer = new ByteArrayOutputStream(4096);
	private static Repository repository;
	private static RevWalk revwalk;
	
	@Test
	public void testQ25 () throws MissingObjectException, IncorrectObjectTypeException, IOException {
		int finallys = 0;
		int tryStatements = 0;
		int tryMax = 0;
		int tryMin = Integer.MAX_VALUE;
		int projectId = 140492550;
		File gitDir = new File("test/datagen/boalang/repos/boalang/test-datagen");
		if (!gitDir.exists()) {
			String url = "https://github.com/boalang/test-datagen.git";
		try {
			RepositoryCloner.clone(new String[]{url, gitDir.getAbsolutePath()});
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
		repository = new FileRepositoryBuilder()
				.setGitDir(new File(gitDir + "/.git"))
				.build();
		
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
			int files = 0;
			for (String path: snapshot2) {
				ObjectId oi = filePathGitObjectIds.get(path);
				final org.eclipse.jdt.core.dom.ASTParser parser = org.eclipse.jdt.core.dom.ASTParser
						.newParser(AST.JLS8);
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
				JavaTryCheckVisitor visitor = new JavaTryCheckVisitor();
				cu.accept(visitor);
				int FieldCount = visitor.tryStatements;
				tryStatements += FieldCount;
				finallys += visitor.finallys;
				if (tryMax < visitor.maxTry)
					tryMax = visitor.maxTry;
				if (FieldCount > 0 && tryMin > visitor.minTry)
					tryMin = visitor.minTry;
				files += visitor.classes;
			}
	//	System.out.println("files " + files);
		double mean = (double)tryStatements /files;
		expected += "FinallyTotal[] = " + finallys
					+ "\nTryMax[] = " + projectId + ", " + (double)tryMax 
					+ "\nTryMean[] = " + mean 
					+ "\nTryMin[] = " + projectId + ", " + (double)tryMin 
					+ "\nTryTotal[] = " +  tryStatements + "\n";
		queryTest("test/known-good/q25.boa", expected);
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
	
	protected static String getFileContents(final ObjectId fileid) {
		//ObjectId fileid = filePathGitObjectIds.get(path);
		try {
			buffer.reset();
			buffer.write(repository.open(fileid, Constants.OBJ_BLOB).getCachedBytes());
		} catch (final Throwable e) {
		}
		return buffer.toString();
	}
	
	public class JavaTryCheckVisitor extends ASTVisitor {
		public int finallys = 0;
		public int tryStatements = 0;
		public int try2 = 0;
		public int classes = 0;
		public int maxTry = 0;
		public int minTry = Integer.MAX_VALUE;
		private Stack<Integer> tryStack = new Stack<Integer>();
		
		@Override
		public boolean preVisit2(ASTNode node) {
			if (node instanceof MethodDeclaration || node instanceof Initializer || node instanceof AnnotationTypeMemberDeclaration) {
				classes++;
				tryStack.push(try2);
				try2 = 0;
			}
			return true;
		}
	
		@Override
		public void endVisit(MethodDeclaration node) {
			if (maxTry < try2)
				maxTry = try2;
			if (minTry > try2)
				minTry = try2;
			try2 = tryStack.pop();
		}
		
		@Override
		public void endVisit(AnnotationTypeMemberDeclaration node) {
			if (maxTry < try2)
				maxTry = try2;
			if (minTry > try2)
				minTry = try2;
			try2 = tryStack.pop();
		}
		
		@Override
		public void endVisit(Initializer node) {
			if (maxTry < try2)
				maxTry = try2;
			if (minTry > try2)
				minTry = try2;
			try2 = tryStack.pop();
		}
		
		@Override
		public boolean visit(TryStatement node) {
			tryStatements ++;
			try2 ++;
			if (node.getFinally() != null)
				finallys ++;
			return true;
		}
	}
}
