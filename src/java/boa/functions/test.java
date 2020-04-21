package boa.functions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import boa.datagen.util.JavaErrorCheckVisitor;
import boa.datagen.util.JavaVisitor;
import boa.types.Ast.ASTRoot;
import boa.types.Ast.Namespace;
import boa.types.Diff.ChangedFile;
import boa.types.Diff.ChangedFile.FileKind;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;

public class test {

	public static void main(String[] args) throws IOException {

		String cId = "a759bc73c577c41560444b6b4a15313ab95efbf5";
		String filePath = "activiti-spring-boot-starter/src/main/java/org/activiti/spring/ProcessDeployedEventProducer.java";
		
		String content = null;
		content = getFileContent(cId, filePath);
		ASTRoot ast = parseJavaFile(content);
		Namespace ns = ast.getNamespaces(0);
		System.out.println(ns.getImportsList());
		System.out.println(ast.getSerializedSize());
	}
	
	public static final ASTRoot emptyAst = ASTRoot.newBuilder().build();
	
	@SuppressWarnings("resource")
	public static final String getFileContent(String commitId, String filePath) throws IOException {
		String path = "/Users/hyj/git/BoaData/DataSet/new/repos/Activiti/Activiti";
		Repository repo = new FileRepositoryBuilder().setGitDir(new File(path + "/.git")).build();
		ObjectId oid = repo.resolve(commitId);
		String content = null;
		
        // a RevWalk allows to walk over commits based on some filtering that is defined
       
    	RevWalk revWalk = new RevWalk(repo);
        RevCommit commit = revWalk.parseCommit(oid);
        // and using commit's tree find the path
        RevTree tree = commit.getTree();
        try {
        	TreeWalk treeWalk = new TreeWalk(repo);
            treeWalk.addTree(tree);
            treeWalk.setRecursive(true);
            treeWalk.setFilter(PathFilter.create(filePath));
            if (!treeWalk.next()) 
                throw new IllegalStateException("Did not find expected file 'README.md'");

            ObjectLoader loader = repo.open(treeWalk.getObjectId(0));

            // and then one can the loader to read the file
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            loader.copyTo(baos);
            content = baos.toString();
            treeWalk.close();
        } catch (Exception e) {
        	System.err.println(e);
		} finally {
			revWalk.dispose();
            revWalk.close();
            repo.close();
		}
		return content;
	}
	
	public static final ASTRoot parseJavaFile(final String content) {
		try {
			final org.eclipse.jdt.core.dom.ASTParser parser = org.eclipse.jdt.core.dom.ASTParser.newParser(AST.JLS8);
			parser.setKind(org.eclipse.jdt.core.dom.ASTParser.K_COMPILATION_UNIT);
			parser.setSource(content.toCharArray());

			final Map<?, ?> options = JavaCore.getOptions();
			JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
			parser.setCompilerOptions(options);

			final CompilationUnit cu;
			try {
				cu = (CompilationUnit) parser.createAST(null);
			} catch(Throwable e) {
				return emptyAst;
			}

			final JavaErrorCheckVisitor errorCheck = new JavaErrorCheckVisitor();
			cu.accept(errorCheck);
			
			if (!errorCheck.hasError) {
				final ASTRoot.Builder ast = ASTRoot.newBuilder();
				final JavaVisitor visitor = new JavaVisitor(content);
				try {				
					ast.addNamespaces(visitor.getNamespaces(cu));
					System.out.println("get ast");
					return ast.build();
				} catch (final Throwable e) {
					System.exit(-1);
					return emptyAst;
				}
			}
			return emptyAst;
		} catch (final Throwable e) {
			return emptyAst;
		}
	}

}
