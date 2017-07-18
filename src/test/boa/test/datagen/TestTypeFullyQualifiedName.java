package boa.test.datagen;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.hamcrest.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.junit.Test;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.Message;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.Type;
import com.googlecode.protobuf.format.JsonFormat;

import boa.datagen.util.ProtoMessageVisitor;
import boa.types.Ast.ASTRoot;
import boa.types.Ast.Declaration;
import boa.types.Ast.Variable;
import boa.types.Code.CodeRepository;
import boa.types.Diff.ChangedFile;
import boa.types.Toplevel.Project;

public class TestTypeFullyQualifiedName {
	private Configuration conf = new Configuration();
	private FileSystem fileSystem;
	private SequenceFile.Reader pr;
	private SequenceFile.Reader ar;
	private Declaration decl = null;

	public TestTypeFullyQualifiedName() throws IOException {
		fileSystem = FileSystem.get(conf);
		pr = new SequenceFile.Reader(fileSystem, new Path("dataset/projects.seq"), conf);
		ar = new SequenceFile.Reader(fileSystem, new Path("dataset/data"), conf);
	}

	@Test
	public void projectTypeNameTest() throws IOException {
		Writable key = new Text();
		BytesWritable val = new BytesWritable();
		while (pr.next(key, val)) {
			byte[] bytes = val.getBytes();
			Project project = Project.parseFrom(CodedInputStream.newInstance(bytes, 0, val.getLength()));
			final CodeRepository cr = project.getCodeRepositories(0);
			assertTrue(cr.getHeadSnapshotCount() > 0);
			for (ChangedFile cf : cr.getHeadSnapshotList()) {
				if (cf.getName().equals("src/java/boa/datagen/forges/github/GithubLanguageDownloadMaster.java")) {
					long astpos = cf.getKey();
					if (astpos > -1) {
						ar.seek(astpos);
						Writable astkey = new LongWritable();
						val = new BytesWritable();
						ar.next(astkey, val);
						bytes = val.getBytes();
						ASTRoot root = ASTRoot.parseFrom(CodedInputStream.newInstance(bytes, 0, val.getLength()));
					//	System.out.println(root);
						ProtoMessageVisitor v = new ProtoMessageVisitor() {
							@Override
							public boolean preVisit(Message message) {
								if (message instanceof boa.types.Ast.Method
										&& ((boa.types.Ast.Method) message).getName().equals("<init>")) {
									boa.types.Ast.Method init = (boa.types.Ast.Method) message;
									boa.types.Ast.Type returnType = init.getReturnType();
									String fqn = returnType.getName();
									System.out.println(init.getName() + " return type is " + fqn);
									assertEquals(true, fqn.equals("void"));
									for (boa.types.Ast.Variable arg : init.getArgumentsList()) {
										fqn = arg.getVariableType().getFullyQualifiedName();
										System.out.println(init.getName() + " has an argument type of " + fqn);
										assertEquals(true, fqn.equals("java.lang.String"));
									}
								}
								if (message instanceof boa.types.Ast.Expression) {
									boa.types.Ast.Expression exp = (boa.types.Ast.Expression) message;
									String varOrMeth = "";
									if(!exp.getVariable().isEmpty())
										varOrMeth = exp.getVariable();
									else if (!exp.getMethod().isEmpty())
										varOrMeth = exp.getMethod();
									else if (!exp.getLiteral().isEmpty())
										varOrMeth = exp.getLiteral();
										if (!exp.getExpressionsList().isEmpty() && exp.getLiteral().equals("this")) {
											boa.types.Ast.Expression lit = exp.getExpressions(0);
											String fqn = lit.getReturnType().getFullyQualifiedName();
											System.out.println("this is " + fqn);
											assertEquals(true, fqn
													.equals("boa.datagen.forges.github.GithubLanguageDownloadMaster"));
											fqn = exp.getDeclaringType().getFullyQualifiedName();
											System.out.println(varOrMeth + " delcaring type is " + fqn);
											assertEquals(true, fqn
													.equals("boa.datagen.forges.github.GithubLanguageDownloadMaster"));
											fqn = exp.getReturnType().getFullyQualifiedName();
											System.out.println(varOrMeth + " return type is " + fqn);
											assertEquals(true, fqn.equals("java.lang.String"));
										} else if (exp.getReturnType() != null) {
											if (exp.getReturnType().getName().equals("String")) {
												String fqn = exp.getReturnType().getFullyQualifiedName();
												System.out.println(exp.getKind() + " " + varOrMeth + " return type is " + fqn);
												assertEquals(true, fqn.equals("java.lang.String"));
											} else if (exp.getReturnType().getName().equals("File")) {
												String fqn = exp.getReturnType().getFullyQualifiedName();
												System.out.println(exp.getKind() + " " + varOrMeth + " return type is " + fqn);
												assertEquals(true, fqn.equals("java.io.File"));
											} else if (exp.getReturnType().getName().equals("String[]")) {
												String fqn = exp.getReturnType().getFullyQualifiedName();
												System.out.println(exp.getKind() + " " + varOrMeth + " return type is " + fqn);
												assertEquals(true, fqn.equals("java.lang.String[]"));
											} else if (exp.getReturnType().getName().equals("int")) {
												System.out.println(exp.getKind() + " " + varOrMeth + " return type is " + "int");
												assertEquals(true,
														exp.getReturnType().getFullyQualifiedName().isEmpty());
											} else if (exp.getReturnType().getName().equals("boolean")) {
												System.out.println(exp.getKind() + " " + varOrMeth + " return type is " + "boolean");
												assertEquals(true,
														exp.getReturnType().getFullyQualifiedName().isEmpty());
											} else if (exp.getReturnType().getName().equals("void")) {
												System.out.println(exp.getKind() + " " + varOrMeth + " return type is " + "void");
												assertEquals(true,
														exp.getReturnType().getFullyQualifiedName().isEmpty());
											}else if (exp.getReturnType().getName().equals("GithubLanguageDownloadMaster")){
												String fqn = exp.getReturnType().getFullyQualifiedName();
												System.out.println(exp.getKind() + " " + varOrMeth + " return type is " + fqn);
												assertEquals(true, fqn.equals("boa.datagen.forges.github.GithubLanguageDownloadMaster"));
											} else if (exp.getReturnType().getName().equals("LanguageDownloadWorker")){
												String fqn = exp.getReturnType().getFullyQualifiedName();
												System.out.println(exp.getKind() + " " + varOrMeth + " return type is " + fqn);
												assertEquals(true, fqn.equals("boa.datagen.forges.github.LanguageDownloadWorker"));
											} else if (exp.getReturnType().getName().equals("TokenList")){
												String fqn = exp.getReturnType().getFullyQualifiedName();
												System.out.println(exp.getKind() + " " + varOrMeth + " return type is " + fqn);
												assertEquals(true, fqn.equals("boa.datagen.forges.github.TokenList"));
											} else if (exp.getReturnType().getName().equals("PrintStream")){
												String fqn = exp.getReturnType().getFullyQualifiedName();
												System.out.println(exp.getKind() + " " + varOrMeth + " return type is " + fqn);
												assertEquals(true, fqn.equals("java.io.PrintStream"));
											} else if (exp.getDeclaringType().getName().equals("GithubLanguageDownloadMaster")){
												String fqn = exp.getDeclaringType().getFullyQualifiedName();
												System.out.println(exp.getKind() + " " + varOrMeth + " declaring type is " + fqn);
												assertEquals(true, fqn.equals("boa.datagen.forges.github.GithubLanguageDownloadMaster"));
											}  else if (exp.getDeclaringType().getName().equals("Thread")){
												String fqn = exp.getDeclaringType().getFullyQualifiedName();
												System.out.println(exp.getKind() +  " " + exp.getMethod() + " declaring type is " + fqn);
												assertEquals(true, fqn.equals("java.lang.Thread"));
											} else if (exp.getDeclaringType().equals("System")){
												String fqn = exp.getDeclaringType().getFullyQualifiedName();
												System.out.println(exp.getKind() + " " + varOrMeth + " declaring type is " + fqn);
												assertEquals(true, fqn.equals("java.lang.System"));
											} else if (exp.getDeclaringType().getName().equals("PrintStream")){
												String fqn = exp.getDeclaringType().getFullyQualifiedName();
												System.out.println(exp.getKind() + " " + varOrMeth + " declaring type is " + fqn);
												assertEquals(true, fqn.equals("java.io.PrintStream"));
											}   else if (exp.getDeclaringType().getName().equals("FileIO")){
												String fqn = exp.getDeclaringType().getFullyQualifiedName();
												System.out.println(exp.getKind() + " " + varOrMeth + " declaring type is " + fqn);
												assertEquals(true, fqn.equals("boa.datagen.util.FileIO"));
											}  
										}
										for (Variable var : exp.getVariableDeclsList()){
											if (var.getVariableType().getName().equals("LanguageDownloadWorker")){
												String fqn = var.getVariableType().getFullyQualifiedName();
												System.out.println(exp.getKind() + " " + var.getName() + " variable type is " + fqn);
												assertEquals(true, fqn.equals("boa.datagen.forges.github.LanguageDownloadWorker"));
											} else if (var.getVariableType().getName().equals("TokenList")){
												String fqn = var.getVariableType().getFullyQualifiedName();
												System.out.println(exp.getKind() + " " + var.getName() + " variable type is " + fqn);
												assertEquals(true, fqn.equals("boa.datagen.forges.github.TokenList"));
											} else if (var.getVariableType().getName().equals("File")){
												String fqn = var.getVariableType().getFullyQualifiedName();
												System.out.println(exp.getKind() + " " + var.getName() + " variable type is " + fqn);
												assertEquals(true, fqn.equals("java.io.File"));
											} else if (var.getVariableType().getName().equals("File[]")){
												String fqn = var.getVariableType().getFullyQualifiedName();
												System.out.println(exp.getKind() + " " + var.getName() + " variable type is " + fqn);
												assertEquals(true, fqn.equals("java.io.File[]"));
											} else if (var.getVariableType().getName().equals("String")){
												String fqn = var.getVariableType().getFullyQualifiedName();
												System.out.println(exp.getKind() + " " + var.getName() + " variable type is " + fqn);
												assertEquals(true, fqn.equals("java.lang.String"));
											} else if (var.getVariableType().getName().equals("GithubLanguageDownloadMaster")){
												String fqn = var.getVariableType().getFullyQualifiedName();
												System.out.println(exp.getKind() + " " + var.getName() + " variable type is " + fqn);
												assertEquals(true, fqn.equals("boa.datagen.forges.github.GithubLanguageDownloadMaster"));
											}
										}
								} else if ( message instanceof boa.types.Ast.Method){
									boa.types.Ast.Method meth = (boa.types.Ast.Method) message;
									if (meth.getName().equals("File")){
										
									}
								}
								return true;
							}
						};
						v.visit(root);
					}
				}
			}
		}
		pr.close();
		ar.close();
	}

}
