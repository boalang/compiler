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
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
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

public class TestQ23 extends QueryTest {
	static Map<String, ObjectId> filePathGitObjectIds = new HashMap<String, ObjectId>();
	protected static final ByteArrayOutputStream buffer = new ByteArrayOutputStream(4096);
	private static Repository repository;
	private static RevWalk revwalk;
	
	@Test
	public void testQ23 () throws MissingObjectException, IncorrectObjectTypeException, IOException {
		int fields = 0;
		int methodsMax = 0;
		int methodsMin = Integer.MAX_VALUE;
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
				JavaConditionCheckVisitor visitor = new JavaConditionCheckVisitor();
				cu.accept(visitor);
				 int FieldCount = visitor.conditions;
				fields += FieldCount;
				if (methodsMax < visitor.maxFields)
					methodsMax = visitor.maxFields;
				if (FieldCount > 0 && methodsMin > visitor.minFields)
					methodsMin = visitor.minFields;
				files += visitor.classes;
			}
	//	System.out.println("files " + files);
		double mean = (double)fields /files;
		expected += "ConditionMax[] = " + projectId + ", " + (double)methodsMax 
					+ "\nConditionMean[] = " + mean 
					+ "\nConditionMin[] = " + projectId + ", " + (double)methodsMin 
					+ "\nConditionTotal[] = " +  fields + "\n";
		queryTest("test/known-good/q23.boa", expected);
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
	
	public class JavaConditionCheckVisitor extends ASTVisitor {
		public int conditions = 0;
		public int condition2 = 0;
		public int classes = 0;
		public int maxFields = 0;
		public int minFields = Integer.MAX_VALUE;
		private Stack<Integer> conditionStack = new Stack<Integer>();
		
		@Override
		public boolean preVisit2(ASTNode node) {
			if (node instanceof MethodDeclaration || node instanceof Initializer || node instanceof AnnotationTypeMemberDeclaration) {
				classes++;
				conditionStack.push(condition2);
				condition2 = 0;
			}
			return true;
		}
	
		@Override
		public void endVisit(MethodDeclaration node) {
			if (maxFields < condition2)
				maxFields = condition2;
			if (minFields > condition2)
				minFields = condition2;
			condition2 = conditionStack.pop();
		}
		
		@Override
		public void endVisit(AnnotationTypeMemberDeclaration node) {
			if (maxFields < condition2)
				maxFields = condition2;
			if (minFields > condition2)
				minFields = condition2;
			condition2 = conditionStack.pop();
		}
		
		@Override
		public void endVisit(Initializer node) {
			if (maxFields < condition2)
				maxFields = condition2;
			if (minFields > condition2)
				minFields = condition2;
			condition2 = conditionStack.pop();
		}
		
		@Override
		public boolean visit(IfStatement node) {
			conditions ++;
			condition2 ++;
			return true;
		}
		
		@Override
		public boolean visit(ForStatement node) {
			conditions ++;
			condition2 ++;
			return true;
		}
		
		
		@Override
		public boolean visit(DoStatement node) {
			conditions ++;
			condition2 ++;
			return true;
		}
		
		@Override
		public boolean visit(SwitchStatement node) {
			conditions ++;
			condition2 ++;
			return true;
		}
		
		@Override
		public boolean visit(WhileStatement node) {
			conditions ++;
			condition2 ++;
			return true;
		}
		
	}
}
