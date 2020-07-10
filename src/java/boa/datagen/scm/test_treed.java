package boa.datagen.scm;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.dltk.compiler.env.IModuleSource;
import org.eclipse.dltk.compiler.env.ModuleSource;
import org.eclipse.dltk.compiler.problem.IProblem;
import org.eclipse.dltk.compiler.problem.IProblemReporter;
import org.eclipse.dltk.python.internal.core.parser.PythonSourceParser;
import org.eclipse.dltk.python.parser.ast.PythonModuleDeclaration;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import boa.datagen.treed.TreedMapper;

public class test_treed {
	
	public int language=1; //1: Python, 2: Java
	
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
	public org.eclipse.dltk.ast.ASTNode getPythonAst(String content)
	{
		PythonSourceParser parser = new PythonSourceParser();
		IModuleSource input = new ModuleSource(content);

		IProblemReporter reporter = new IProblemReporter() {
			@Override
			public void reportProblem(IProblem arg0) {

			}
		};
		
		// System.out.println("actual source: " + content);
		PythonModuleDeclaration module=null;		

		try {
			module = (PythonModuleDeclaration) parser.parse(input, reporter);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return module;
	}
	public void test_treed_client() throws IOException
	{
		String fileName1="",fileName2="";
		if(this.language==1)
		{
			fileName1="src/java/boa/datagen/scm/test/v11.py";
			fileName2="src/java/boa/datagen/scm/test/v12.py";
		}
		else if(this.language==2)
		{
			fileName1="src/java/boa/datagen/scm/test/v1.java";
			fileName2="src/java/boa/datagen/scm/test/v2.java";
		}
		File file = new File(fileName1);
	    String version1=FileUtils.readFileToString(file, StandardCharsets.UTF_8);
	    file = new File(fileName2);
	    String version2=FileUtils.readFileToString(file, StandardCharsets.UTF_8);
	    
	    if(this.language==2)
	    {
		    ASTNode v1=getAst(version1);
		    ASTNode v2=getAst(version2);
		    
		    TreedMapper tm=new TreedMapper(v1,v2);
		    
		    tm.map();
		    tm.printChanges();
	    }
	    else
	    {
	    	 org.eclipse.dltk.ast.ASTNode v1=getPythonAst(version1);
			 org.eclipse.dltk.ast.ASTNode v2=getPythonAst(version2);
			    
			 boa.datagen.treed.python.TreedMapper tm=new boa.datagen.treed.python.TreedMapper(v1,v2);
			    
			  try {
				tm.map();
				tm.printChanges();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
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
