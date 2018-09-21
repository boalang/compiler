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
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
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
import boa.test.datagen.queries.TestQ26.JavaThrowCheckVisitor;
import boa.types.Diff.ChangedFile;

public class TestQ28 extends QueryTest {
	static Map<String, ObjectId> filePathGitObjectIds = new HashMap<String, ObjectId>();
	protected static final ByteArrayOutputStream buffer = new ByteArrayOutputStream(4096);
	private static Repository repository;
	private static RevWalk revwalk;
	
	@Test
	public void testQ28 () throws MissingObjectException, IncorrectObjectTypeException, IOException {
		int throwStatements = 0;
		int throwMax = 0;
		int throwMin = Integer.MAX_VALUE;
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
				JavaLocalsCheckVisitor visitor = new JavaLocalsCheckVisitor();
				cu.accept(visitor);
				int FieldCount = visitor.locals;
				throwStatements += FieldCount;
				if (throwMax < visitor.maxLocals)
					throwMax = visitor.maxLocals;
				if (FieldCount > 0 && throwMin > visitor.minLocals)
					throwMin = visitor.minLocals;
				files += visitor.classes;
			}
	//	System.out.println("files " + files);
		double mean = (double)throwStatements /files;
		expected += "LocalsMax[] = " + projectId + ", " + (double)throwMax 
					+ "\nLocalsMean[] = " + mean 
					+ "\nLocalsMin[] = " + projectId + ", " + (double)throwMin 
					+ "\nLocalsTotal[] = " +  throwStatements + "\n";
		queryTest("test/known-good/q28.boa", expected);
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
	
	public class JavaLocalsCheckVisitor extends ASTVisitor {
		public int locals = 0;
		public int locals2 = 0;
		public int classes = 0;
		public int maxLocals = 0;
		public int minLocals = Integer.MAX_VALUE;
		private Stack<Integer> tryStack = new Stack<Integer>();
		
		@Override
		public boolean preVisit2(ASTNode node) {
			if (node instanceof MethodDeclaration || node instanceof Initializer || node instanceof AnnotationTypeMemberDeclaration) {
				classes++;
				tryStack.push(locals2);
				locals2 = 0;
			}
			return true;
		}
	
		@Override
		public void endVisit(MethodDeclaration node) {
			if (maxLocals < locals2)
				maxLocals = locals2;
			if (minLocals > locals2)
				minLocals = locals2;
			locals2 = tryStack.pop();
		}
		
		@Override
		public void endVisit(AnnotationTypeMemberDeclaration node) {
			if (maxLocals < locals2)
				maxLocals = locals2;
			if (minLocals > locals2)
				minLocals = locals2;
			locals2 = tryStack.pop();
		}
		
		@Override
		public void endVisit(Initializer node) {
			if (maxLocals < locals2)
				maxLocals = locals2;
			if (minLocals > locals2)
				minLocals = locals2;
			locals2 = tryStack.pop();
		}
		
		@Override
		public boolean visit(VariableDeclarationStatement node) {
			for (Object o : node.fragments()) {
				locals++;
				locals2++;
			}
			return true;
		}

		@Override
		public boolean visit(VariableDeclarationExpression node) {
			for (Object o : node.fragments()) {
				locals++;
				locals2++;
			}
			return true;
		}
		
		@Override
		public boolean visit(EnhancedForStatement node) {
			locals++;
			locals2++;
			return true;
		}
		
		@Override
		public boolean visit(CatchClause node) {
			locals++;
			locals2++;
			return true;
		}
		
		@Override
		public boolean visit(LambdaExpression node) {
			for (Object o : node.parameters()) {
				locals++;
				locals2++;
			}
			return true;
		}
	}
}
