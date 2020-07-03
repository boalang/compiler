package boa.datagen.scm;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import boa.datagen.treed.TreedMapper;

public class test_treed {
	
	public ASTNode getAst(String content)
	{
		final org.eclipse.jdt.core.dom.ASTParser parser = org.eclipse.jdt.core.dom.ASTParser.newParser(AST.JLS8);
		parser.setKind(org.eclipse.jdt.core.dom.ASTParser.K_COMPILATION_UNIT);
		parser.setSource(content.toCharArray());

		final Map<?, ?> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
		parser.setCompilerOptions(options);

		final CompilationUnit cu;

		try {
			cu = (CompilationUnit) parser.createAST(null);
		} catch (Throwable e) {
			return null;
		}
		return cu;
	}
	public void test_treed_client() throws IOException
	{

		File file = new File("src/java/boa/datagen/scm/test/v1.java");
	    String version1=FileUtils.readFileToString(file, StandardCharsets.UTF_8);
	    file = new File("src/java/boa/datagen/scm/test/v2.java");
	    String version2=FileUtils.readFileToString(file, StandardCharsets.UTF_8);
	    
	    ASTNode v1=getAst(version1);
	    ASTNode v2=getAst(version2);
	    
	    TreedMapper tm=new TreedMapper(v1,v2);
	    
	    tm.map();
	    tm.printChanges();
	    System.out.println(tm.getNumOfChanges());
	}
	public static void main(String []args)
	{
		test_treed tt=new test_treed();
		try {
			tt.test_treed_client();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
