package boa.test.datagen;

import static org.junit.Assert.*;
import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.junit.Test;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.Message;
import boa.datagen.util.ProtoMessageVisitor;
import boa.types.Ast.ASTRoot;
import boa.types.Ast.Declaration;
import boa.types.Ast.Expression;
import boa.types.Ast.Variable;
import boa.types.Code.CodeRepository;
import boa.types.Diff.ChangedFile;
import boa.types.Toplevel.Project;

public class TestTypeFullyQualifiedName {
	private Configuration conf = new Configuration();
	private FileSystem fileSystem;
	private SequenceFile.Reader pr;
	private SequenceFile.Reader ar;

	public TestTypeFullyQualifiedName() throws IOException {
		fileSystem = FileSystem.get(conf);
		Path projectPath = new Path("dataset/projects.seq"), dataPath = new Path("dataset/data");
		if (fileSystem.exists(projectPath) && fileSystem.exists(dataPath)) {
			pr = new SequenceFile.Reader(fileSystem, projectPath, conf);
			ar = new SequenceFile.Reader(fileSystem, dataPath, conf);
		}
	}

	@Test
	public void projectTypeNameTest() throws IOException {
		if (pr == null || ar == null)
			return;
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
						// System.out.println(root);
						ProtoMessageVisitor v = new ProtoMessageVisitor() {
							@Override
							public boolean preVisit(Message message) {
								if (message instanceof boa.types.Ast.Method
										&& ((boa.types.Ast.Method) message).getName().equals("<init>")) {
									boa.types.Ast.Method init = (boa.types.Ast.Method) message;
									boa.types.Ast.Type returnType = init.getReturnType();
									String fqn = returnType.getName();
									System.out.println(init.getName() + " return type is " + fqn);
									assertEquals("void", fqn);
									for (boa.types.Ast.Variable arg : init.getArgumentsList()) {
										fqn = arg.getVariableType().getFullyQualifiedName();
										System.out.println(init.getName() + " has an argument type of " + fqn);
										assertEquals("java.lang.String", fqn);
									}
								}
								if (message instanceof boa.types.Ast.Expression) {
									boa.types.Ast.Expression exp = (boa.types.Ast.Expression) message;
									String varOrMeth = "";
									if (!exp.getVariable().isEmpty())
										varOrMeth = exp.getVariable();
									else if (!exp.getMethod().isEmpty())
										varOrMeth = exp.getMethod();
									else if (!exp.getLiteral().isEmpty())
										varOrMeth = exp.getLiteral();
									if (!exp.getExpressionsList().isEmpty() && exp.getLiteral().equals("this")) {
										boa.types.Ast.Expression lit = exp.getExpressions(0);
										String fqn = lit.getReturnType().getFullyQualifiedName();
										System.out.println("this is " + fqn);
										assertEquals("boa.datagen.forges.github.GithubLanguageDownloadMaster", fqn);
										fqn = exp.getDeclaringType().getFullyQualifiedName();
										System.out.println(varOrMeth + " delcaring type is " + fqn);
										assertEquals("boa.datagen.forges.github.GithubLanguageDownloadMaster", fqn);
										fqn = exp.getReturnType().getFullyQualifiedName();
										System.out.println(varOrMeth + " return type is " + fqn);
										assertEquals("java.lang.String", fqn);
									} else if (exp.getReturnType() != null) {
										if (exp.getReturnType().getName().equals("String")) {
											String fqn = exp.getReturnType().getFullyQualifiedName();
											System.out.println(exp.getKind() + " " + varOrMeth + " return type is " + fqn);
											assertEquals("java.lang.String", fqn);
										} else if (exp.getReturnType().getName().equals("File")) {
											String fqn = exp.getReturnType().getFullyQualifiedName();
											System.out.println(exp.getKind() + " " + varOrMeth + " return type is " + fqn);
											assertEquals("java.io.File", fqn);
										} else if (exp.getReturnType().getName().equals("String[]")) {
											String fqn = exp.getReturnType().getFullyQualifiedName();
											System.out.println(exp.getKind() + " " + varOrMeth + " return type is " + fqn);
											assertEquals("java.lang.String[]", fqn);
										} else if (exp.getReturnType().getName().equals("int")) {
											System.out.println(exp.getKind() + " " + varOrMeth + " return type is " + "int");
											assertEquals(true, exp.getReturnType().getFullyQualifiedName().isEmpty());
										} else if (exp.getReturnType().getName().equals("boolean")) {
											System.out.println(exp.getKind() + " " + varOrMeth + " return type is " + "boolean");
											assertEquals(true, exp.getReturnType().getFullyQualifiedName().isEmpty());
										} else if (exp.getReturnType().getName().equals("void")) {
											System.out.println(exp.getKind() + " " + varOrMeth + " return type is " + "void");
											assertEquals(true, exp.getReturnType().getFullyQualifiedName().isEmpty());
										} else if (exp.getReturnType().getName().equals("GithubLanguageDownloadMaster")) {
											String fqn = exp.getReturnType().getFullyQualifiedName();
											System.out.println(exp.getKind() + " " + varOrMeth + " return type is " + fqn);
											assertEquals("boa.datagen.forges.github.GithubLanguageDownloadMaster", fqn);
										} else if (exp.getReturnType().getName().equals("LanguageDownloadWorker")) {
											String fqn = exp.getReturnType().getFullyQualifiedName();
											System.out.println(exp.getKind() + " " + varOrMeth + " return type is " + fqn);
											assertEquals("boa.datagen.forges.github.LanguageDownloadWorker", fqn);
										} else if (exp.getReturnType().getName().equals("TokenList")) {
											String fqn = exp.getReturnType().getFullyQualifiedName();
											System.out.println(exp.getKind() + " " + varOrMeth + " return type is " + fqn);
											assertEquals("boa.datagen.forges.github.TokenList", fqn);
										} else if (exp.getReturnType().getName().equals("PrintStream")) {
											String fqn = exp.getReturnType().getFullyQualifiedName();
											System.out.println(exp.getKind() + " " + varOrMeth + " return type is " + fqn);
											assertEquals("java.io.PrintStream", fqn);
										}
									}
									if (exp.getDeclaringType() != null) {
										if (exp.getDeclaringType().getName().equals("GithubLanguageDownloadMaster")) {
											String fqn = exp.getDeclaringType().getFullyQualifiedName();
											System.out.println(exp.getKind() + " " + varOrMeth + " declaring type is " + fqn);
											assertEquals("boa.datagen.forges.github.GithubLanguageDownloadMaster", fqn);
										} else if (exp.getDeclaringType().getName().equals("Thread")) {
											String fqn = exp.getDeclaringType().getFullyQualifiedName();
											System.out.println(exp.getKind() + " " + exp.getMethod() + " declaring type is " + fqn);
											assertEquals("java.lang.Thread", fqn);
										} else if (exp.getDeclaringType().equals("System")) {
											String fqn = exp.getDeclaringType().getFullyQualifiedName();
											System.out.println(exp.getKind() + " " + varOrMeth + " declaring type is " + fqn);
											assertEquals("java.lang.System", fqn);
										} else if (exp.getDeclaringType().getName().equals("PrintStream")) {
											String fqn = exp.getDeclaringType().getFullyQualifiedName();
											System.out.println(exp.getKind() + " " + varOrMeth + " declaring type is " + fqn);
											assertEquals("java.io.PrintStream", fqn);
										} else if (exp.getDeclaringType().getName().equals("FileIO")) {
											String fqn = exp.getDeclaringType().getFullyQualifiedName();
											System.out.println(exp.getKind() + " " + varOrMeth + " declaring type is " + fqn);
											assertEquals("boa.datagen.util.FileIO", fqn);
										}
									}
									for (Variable var : exp.getVariableDeclsList()) {
										if (var.getVariableType().getName().equals("LanguageDownloadWorker")) {
											String fqn = var.getVariableType().getFullyQualifiedName();
											System.out.println(exp.getKind() + " " + var.getName() + " variable type is " + fqn);
											assertEquals("boa.datagen.forges.github.LanguageDownloadWorker", fqn);
										} else if (var.getVariableType().getName().equals("TokenList")) {
											String fqn = var.getVariableType().getFullyQualifiedName();
											System.out.println(exp.getKind() + " " + var.getName() + " variable type is " + fqn);
											assertEquals("boa.datagen.forges.github.TokenList", fqn);
										} else if (var.getVariableType().getName().equals("File")) {
											String fqn = var.getVariableType().getFullyQualifiedName();
											System.out.println(exp.getKind() + " " + var.getName() + " variable type is " + fqn);
											assertEquals("java.io.File", fqn);
										} else if (var.getVariableType().getName().equals("File[]")) {
											String fqn = var.getVariableType().getFullyQualifiedName();
											System.out.println(exp.getKind() + " " + var.getName() + " variable type is " + fqn);
											assertEquals("java.io.File[]", fqn);
										} else if (var.getVariableType().getName().equals("String")) {
											String fqn = var.getVariableType().getFullyQualifiedName();
											System.out.println(exp.getKind() + " " + var.getName() + " variable type is " + fqn);
											assertEquals("java.lang.String", fqn);
										} else if (var.getVariableType().getName().equals("GithubLanguageDownloadMaster")) {
											String fqn = var.getVariableType().getFullyQualifiedName();
											System.out.println(exp.getKind() + " " + var.getName() + " variable type is " + fqn);
											assertEquals("boa.datagen.forges.github.GithubLanguageDownloadMaster", fqn);
										}
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

	@Test
	public void VariableTypeName() throws IOException {
		if (pr == null || ar == null)
			return;
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
						// System.out.println(root);
						ProtoMessageVisitor v = new ProtoMessageVisitor() {
							@Override
							public boolean preVisit(Message message) {
								if (message instanceof boa.types.Ast.Expression) {
									boa.types.Ast.Expression exp = (boa.types.Ast.Expression) message;
									if (exp.getKind().equals(Expression.ExpressionKind.VARACCESS)) {
										String var = exp.getVariable();
										if (var.equals("repoNameDir")) {
											System.out.println(exp.getReturnType().getFullyQualifiedName() + " is java.lang.String");
											assertEquals("java.lang.String", exp.getReturnType().getFullyQualifiedName());
											System.out.println(exp.getDeclaringType().getFullyQualifiedName() + " is boa.datagen.forges.github.GithubLanguageDownloadMaster");
											assertEquals("boa.datagen.forges.github.GithubLanguageDownloadMaster", exp.getDeclaringType().getFullyQualifiedName());
										} else if (var.equals("langNameDir")) {
											System.out.println(exp.getReturnType() + " is java.lang.String");
											assertEquals("java.lang.String", exp.getReturnType().getFullyQualifiedName());
											System.out.println(exp.getDeclaringType().getFullyQualifiedName() + " is boa.datagen.forges.github.GithubLanguageDownloadMaster");
											assertEquals("boa.datagen.forges.github.GithubLanguageDownloadMaster", exp.getDeclaringType().getFullyQualifiedName());
										} else if (var.equals("tokenFile")) {
											System.out.println(exp.getReturnType().getFullyQualifiedName() + " is java.lang.String");
											assertEquals("java.lang.String", exp.getReturnType().getFullyQualifiedName());
											System.out.println(exp.getDeclaringType().getFullyQualifiedName() + " is boa.datagen.forges.github.GithubLanguageDownloadMaster");
			//FIXME							assertEquals("boa.datagen.forges.github.GithubLanguageDownloadMaster", exp.getDeclaringType().getFullyQualifiedName());
										} else if (var.equals("MAX_NUM_THREADS")) {
											System.out.println(exp.getReturnType().getName() + " is int");
											assertEquals("int", exp.getReturnType().getName());
											System.out.println(exp.getDeclaringType().getFullyQualifiedName() + " is boa.datagen.forges.github.GithubLanguageDownloadMaster");
											assertEquals("boa.datagen.forges.github.GithubLanguageDownloadMaster", exp.getDeclaringType().getFullyQualifiedName());
										} else if (var.equals("names")) {
											System.out.println(exp.getReturnType().getFullyQualifiedName()
													+ " is gnu.trove.set.hash.THashSet<String>");
											System.out
											.println(var + " return type is " + exp.getReturnType().getName());
											// FIXME assertEquals("gnu.trove.set.hash.THashSet<String>", exp.getReturnType().getFullyQualifiedName());
											System.out.println(exp.getDeclaringType().getFullyQualifiedName() + " is boa.datagen.forges.github.GithubLanguageDownloadMaster");
											System.out.println(var + " declaring type is " + exp.getDeclaringType().getName());
			//FIXME								assertEquals("boa.datagen.forges.github.GithubLanguageDownloadMaster", exp.getDeclaringType().getFullyQualifiedName());
										} else if (var.equals("input")) {
											System.out.println(exp.getReturnType().getFullyQualifiedName() + " is java.lang.String");
											assertEquals("java.lang.String", exp.getReturnType().getFullyQualifiedName());
										} else if (var.equals("output")) {
											System.out.println(exp.getReturnType().getFullyQualifiedName() + " is java.lang.String");
											assertEquals("java.lang.String", exp.getReturnType().getFullyQualifiedName());
										} else if (var.equals("token")) {
											System.out.println(exp.getReturnType().getFullyQualifiedName() + " is java.lang.String");
											assertEquals("java.lang.String", exp.getReturnType().getFullyQualifiedName());
										} else if (var.equals("outputDir")) {
											System.out.println(exp.getReturnType().getFullyQualifiedName() + " is java.io.File");
											assertEquals("java.io.File", exp.getReturnType().getFullyQualifiedName());
										} else if (var.equals("args")) {
											System.out.println(exp.getReturnType().getFullyQualifiedName() + " is java.lang.String[]");
											assertEquals("java.lang.String[]", exp.getReturnType().getFullyQualifiedName());
										} else if (var.equals("master")) {
											System.out.println(exp.getReturnType().getFullyQualifiedName() + " is boa.datagen.forges.github.GithubLanguageDownloadMaster");
											assertEquals("boa.datagen.forges.github.GithubLanguageDownloadMaster", exp.getReturnType().getFullyQualifiedName());
										} else if (var.equals("start")) {
											System.out.println(exp.getReturnType().getName() + " is int");
											assertEquals("int", exp.getReturnType().getName());
										} else if (var.equals("end")) {
											System.out.println(exp.getReturnType().getName() + " is int");
											assertEquals("int", exp.getReturnType().getName());
										} else if (var.equals("shareSize")) {
											System.out.println(exp.getReturnType().getName() + " is int");
											assertEquals("int", exp.getReturnType().getName());
										} else if (var.equals("i")) {
											System.out.println(exp.getReturnType().getName() + " is int");
											assertEquals("int", exp.getReturnType().getName());
										} else if (var.equals("tokens")) {
											System.out.println(exp.getReturnType().getFullyQualifiedName() + " is boa.datagen.forges.github.TokenList");
											assertEquals("boa.datagen.forges.github.TokenList", exp.getReturnType().getFullyQualifiedName());
										} else if (var.equals("worker")) {
											System.out.println(exp.getReturnType().getFullyQualifiedName() + " is boa.datagen.forges.github.LanguageDownloadWorker");
											assertEquals("boa.datagen.forges.github.LanguageDownloadWorker", exp.getReturnType().getFullyQualifiedName());
										} else if (var.equals("totalFies")) {
											System.out.println(exp.getReturnType().getName() + " is int");
											assertEquals("int", exp.getReturnType().getName());
										} else if (var.equals("files.length")) {
											System.out.println(exp.getReturnType().getName() + " is int");
											assertEquals("int", exp.getReturnType().getName());
										} else if (var.equals("args.length")) {
											System.out.println(exp.getReturnType().getName() + " is int");
											assertEquals("int", exp.getReturnType().getName());
										} else if (var.equals("length")) {
											System.out.println(exp.getReturnType().getName() + " is int");
											assertEquals("int", exp.getReturnType().getName());
										} else if (var.equals("dir")) {
											System.out.println(exp.getReturnType().getFullyQualifiedName() + " is java.io.File");
											assertEquals("java.io.File", exp.getReturnType().getFullyQualifiedName());
										} else if (var.equals("files")) {
											System.out.println(exp.getReturnType().getFullyQualifiedName() + " is java.io.File[]");
											assertEquals("java.io.File[]", exp.getReturnType().getFullyQualifiedName());
										} else if (var.equals("content")) {
											System.out.println(exp.getReturnType().getFullyQualifiedName() + " is java.lang.String");
											assertEquals("java.lang.String", exp.getReturnType().getFullyQualifiedName());
										} else if (var.equals("filePath")) {
											System.out.println(exp.getReturnType().getFullyQualifiedName() + " is java.lang.String");
											assertEquals("java.lang.String", exp.getReturnType().getFullyQualifiedName());
										} else if (var.equals("repos")) {
											System.out.println(exp.getReturnType().getFullyQualifiedName() + " is com.google.gson");
											 System.out.println(var + "  is " +  exp.getReturnType().getName());
											// FIXME assertEquals("com.google.gson", exp.getReturnType().getFullyQualifiedName());
										} else if (var.equals("parser")) {
											System.out.println(exp.getReturnType().getFullyQualifiedName() + " is com.google.gson");
											// FIXME assertEquals("com.google.gson", exp.getReturnType().getFullyQualifiedName());
										} else if (var.equals("repoE")) {
											System.out.println(exp.getReturnType().getFullyQualifiedName() + " is com.google.gson");
											// FIXME assertEquals("com.google.gson", exp.getReturnType().getFullyQualifiedName());
										} else if (var.equals("repo")) {
											System.out.println(exp.getReturnType().getFullyQualifiedName() + " is com.google.gson");
											// FXIME assertEquals("com.google.gson", exp.getReturnType().getFullyQualifiedName());
										} else if (var.equals("FileIO")) {
											System.out.println(exp.getReturnType().getFullyQualifiedName() + " is boa.datagen.util.FileIO");
											assertEquals("boa.datagen.util.FileIO", exp.getReturnType().getFullyQualifiedName());
										} else if (var.equals("System.out")) {
											System.out.println(exp.getReturnType().getFullyQualifiedName() + " is java.io.PrintStream");
											assertEquals("java.io.PrintStream", exp.getReturnType().getFullyQualifiedName());
										} else if (var.equals("master.repoNameDir")) {
											System.out.println(exp.getReturnType().getFullyQualifiedName() + " is java.lang.String");
											assertEquals("java.lang.String", exp.getReturnType().getFullyQualifiedName());
										} else {
											System.out.println("missed " + var + " is " + exp.getReturnType().getFullyQualifiedName());
										}
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
	}

	@Test
	public void VarDeclTypeName() throws IOException {
		if (pr == null || ar == null)
			return;
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
						System.out.println(root);
						ProtoMessageVisitor v = new ProtoMessageVisitor() {
							@Override
							public boolean preVisit(Message message) {
								if (message instanceof Declaration) {
									Declaration decl = (Declaration) message;
									for (Variable var : decl.getFieldsList()) {
										String name = var.getName();
										if (name.equals("repoNameDir")) {
											System.out.println(var.getName() + " is " + var.getVariableType().getFullyQualifiedName() + " is java.lang.String");
											assertEquals("java.lang.String", var.getVariableType().getFullyQualifiedName());
											System.out.println(var.getName() + " declaring is " + decl.getFullyQualifiedName() 
												+ " is boa.datagen.forges.github.GithubLanguageDownloadMaster");
											assertEquals("boa.datagen.forges.github.GithubLanguageDownloadMaster", decl.getFullyQualifiedName());
										} else if (name.equals("langNameDir")) {
											System.out.println(var.getName() + " is " + var.getVariableType().getFullyQualifiedName() + " is java.lang.String");
											assertEquals("java.lang.String", var.getVariableType().getFullyQualifiedName());
											System.out.println(var.getName() + " declaring type is " + decl.getFullyQualifiedName()
													+ " is boa.datagen.forges.github.GithubLanguageDownloadMaster");
											assertEquals("boa.datagen.forges.github.GithubLanguageDownloadMaster", decl.getFullyQualifiedName());
										} else if (name.equals("tokenFile")) {
											System.out.println(var.getName() + " is " + var.getVariableType().getFullyQualifiedName() + " is java.lang.String");
											assertEquals("java.lang.String", var.getVariableType().getFullyQualifiedName());
											System.out.println(var.getName() + " declaring type is " + decl.getFullyQualifiedName()
													+ " is boa.datagen.forges.github.GithubLanguageDownloadMaster");
											assertEquals("boa.datagen.forges.github.GithubLanguageDownloadMaster", decl.getFullyQualifiedName());
										} else if (name.equals("MAX_NUM_THREADS")) {
											System.out.println(var.getName() + " is " + var.getVariableType().getName() + " is int");
											assertEquals("int", var.getVariableType().getName());
											System.out.println(var.getName() + " declaring type is " + decl.getFullyQualifiedName()
													+ " is boa.datagen.forges.github.GithubLanguageDownloadMaster");
											assertEquals("boa.datagen.forges.github.GithubLanguageDownloadMaster", decl.getFullyQualifiedName());
										} else if (name.equals("names")) {
											System.out.println(var.getName() + " is " + var.getVariableType().getFullyQualifiedName() + " is gnu.trove.set.hash.THashSet<String>");
											// FIXME assertEquals("gnu.trove.set.hash.THashSet<String>", var.getVariableType().getFullyQualifiedName());
											System.out.println(var.getName() + " declaring type is " + decl.getFullyQualifiedName()
													+ " is boa.datagen.forges.github.GithubLanguageDownloadMaster");
											assertEquals("boa.datagen.forges.github.GithubLanguageDownloadMaster", decl.getFullyQualifiedName());
										} else {
											System.out.println("missed " + var.getName() + " is " + var.getVariableType().getFullyQualifiedName());
										}
									}
								}
								if (message instanceof boa.types.Ast.Expression) {
									boa.types.Ast.Expression exp = (boa.types.Ast.Expression) message;
									if (exp.getKind().equals(Expression.ExpressionKind.VARDECL)) {
										for (Variable var : exp.getVariableDeclsList()) {
											String name = var.getName();
											if (name.equals("outputDir")) {
												System.out.println(var.getName() + " " + var.getVariableType().getFullyQualifiedName() + " is java.io.File");
												assertEquals("java.io.File", var.getVariableType().getFullyQualifiedName());
											} else if (name.equals("repoNameDir")) {
												System.out.println(var.getName() + " is " + var.getVariableType().getFullyQualifiedName() + " is java.lang.String");
												assertEquals("java.lang.String", var.getVariableType().getFullyQualifiedName());
												System.out.println(var.getName() + " declaring is " + var.getVariableType().getFullyQualifiedName()
													+ " is boa.datagen.forges.github.GithubLanguageDownloadMaster");
												assertEquals("boa.datagen.forges.github.GithubLanguageDownloadMaster", exp.getDeclaringType().getFullyQualifiedName());
											} else if (name.equals("langNameDir")) {
												System.out.println(var.getName() + " is " + var.getVariableType().getFullyQualifiedName() + " is java.lang.String");
												assertEquals("java.lang.String", var.getVariableType().getFullyQualifiedName());
												System.out.println(var.getName() + " declaring type is " + exp.getDeclaringType().getFullyQualifiedName()
														+ " is boa.datagen.forges.github.GithubLanguageDownloadMaster");
												assertEquals("boa.datagen.forges.github.GithubLanguageDownloadMaster", exp.getDeclaringType().getFullyQualifiedName());
											} else if (name.equals("tokenFile")) {
												System.out.println(var.getName() + " is " + var.getVariableType().getFullyQualifiedName() + " is java.lang.String");
												assertEquals("java.lang.String", var.getVariableType().getFullyQualifiedName());
												System.out.println(var.getName() + " declaring type is " + exp.getDeclaringType().getFullyQualifiedName()
														+ " is boa.datagen.forges.github.GithubLanguageDownloadMaster");
												assertEquals("boa.datagen.forges.github.GithubLanguageDownloadMaster", exp.getDeclaringType().getFullyQualifiedName());
											} else if (name.equals("MAX_NUM_THREADS")) {
												System.out.println(var.getName() + " is " + var.getVariableType().getName() + " is int");
												assertEquals("int", var.getVariableType().getName());
												System.out.println(var.getName() + " declaring type is " + exp.getDeclaringType().getFullyQualifiedName()
														+ " is boa.datagen.forges.github.GithubLanguageDownloadMaster");
												assertEquals("boa.datagen.forges.github.GithubLanguageDownloadMaster", exp.getDeclaringType().getFullyQualifiedName());
											} else if (name.equals("names")) {
												System.out.println(var.getName() + " is " + var.getVariableType().getFullyQualifiedName() + " is gnu.trove.set.hash.THashSet<String>");
												// FIXME assertEquals("gnu.trove.set.hash.THashSet<String>", var.getVariableType().getFullyQualifiedName());
												System.out.println(var.getName() + " declaring type is " + exp.getDeclaringType().getFullyQualifiedName()
														+ " is boa.datagen.forges.github.GithubLanguageDownloadMaster");
												assertEquals("boa.datagen.forges.github.GithubLanguageDownloadMaster", exp.getDeclaringType().getFullyQualifiedName());
											} else if (name.equals("master")) {
												System.out.println(var.getName() + " " + var.getVariableType().getFullyQualifiedName()
														+ " is boa.datagen.forges.github.GithubLanguageDownloadMaster");
												assertEquals("boa.datagen.forges.github.GithubLanguageDownloadMaster", var.getVariableType().getFullyQualifiedName());
											} else if (var.equals("start")) {
												System.out.println(var.getName() + " " + var.getVariableType().getName() + " is int");
												assertEquals("int", var.getVariableType().getName());
											} else if (name.equals("end")) {
												System.out.println(var.getName() + " " + var.getVariableType().getName() + " is int");
												assertEquals("int", var.getVariableType().getName());
											} else if (name.equals("shareSize")) {
												System.out.println(var.getName() + " " + var.getVariableType().getName() + " is int");
												assertEquals("int", var.getVariableType().getName());
											} else if (name.equals("i")) {
												System.out.println(var.getName() + " " + var.getVariableType().getName() + " is int");
												assertEquals("int", var.getVariableType().getName());
											} else if (name.equals("tokens")) {
												System.out.println(var.getName() + " " + var.getVariableType().getFullyQualifiedName()
														+ " is boa.datagen.forges.github.TokenList");
												assertEquals("boa.datagen.forges.github.TokenList", var.getVariableType().getFullyQualifiedName());
											} else if (name.equals("worker")) {
												System.out.println(var.getName() + " " + var.getVariableType().getFullyQualifiedName()
														+ " is boa.datagen.forges.github.LanguageDownloadWorker");
												assertEquals("boa.datagen.forges.github.LanguageDownloadWorker", var.getVariableType().getFullyQualifiedName());
											} else if (name.equals("dir")) {
												System.out.println(var.getName() + " " + var.getVariableType().getFullyQualifiedName() + " is java.io.File");
												assertEquals("java.io.File", var.getVariableType().getFullyQualifiedName());
											} else if (name.equals("files")) {
												System.out.println(var.getName() + " " + var.getVariableType().getFullyQualifiedName() + " is java.io.File[]");
												assertEquals("java.io.File[]", var.getVariableType().getFullyQualifiedName());
											} else if (name.equals("content")) {
												System.out.println(var.getName() + " " + var.getVariableType().getFullyQualifiedName() + " is java.lang.String");
												assertEquals("java.lang.String", var.getVariableType().getFullyQualifiedName());
											} else if (name.equals("repos")) {
												System.out.println(var.getName() + " " + var.getVariableType().getFullyQualifiedName() + " is com.google.gson");
												System.out.println(var.getName() + " is " + var.getVariableType().getName());
												// FIXME assertEquals("com.google.gson", var.getVariableType().getFullyQualifiedName());
											} else if (name.equals("parser")) {
												System.out.println(var.getName() + " " + var.getVariableType().getFullyQualifiedName() + " is com.google.gson");
												// FXIME assertEquals("com.google.gson", var.getVariableType().getFullyQualifiedName());
											} else if (name.equals("repo")) {
												System.out.println(var.getName() + " " + var.getVariableType().getFullyQualifiedName() + " is com.google.gson");
												// FIXME assertEquals("com.google.gson", var.getVariableType().getFullyQualifiedName());
											} else {
												System.out.println("missed " + var.getName() + " is " + var.getVariableType().getFullyQualifiedName());
											}
										}
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
	}

	@Test
	public void methodCallTypeName() throws IOException {
		if (pr == null || ar == null)
			return;
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
						// System.out.println(root);
						ProtoMessageVisitor v = new ProtoMessageVisitor() {
							@Override
							public boolean preVisit(Message message) {
								if (message instanceof Expression) {
									Expression exp = (Expression) message;
									if (exp.getKind().equals(Expression.ExpressionKind.METHODCALL)) {
										String methName = exp.getMethod();
										if (methName.equals("exists")) {
											System.out.println( methName + " return type is " + exp.getReturnType().getName());
											assertEquals("boolean", exp.getReturnType().getName());
											System.out.println(methName + " declaring type is " + exp.getDeclaringType().getFullyQualifiedName());
											assertEquals("java.io.File", exp.getDeclaringType().getFullyQualifiedName());
										} else if (methName.equals("mkdirs")) {
											System.out.println(methName + " return type is " + exp.getReturnType().getName());
											assertEquals("boolean", exp.getReturnType().getName());
											System.out.println(methName + " declaring type is " + exp.getDeclaringType().getFullyQualifiedName());
											assertEquals("java.io.File", exp.getDeclaringType().getFullyQualifiedName());
										} else if (methName.equals("addNames")) {
											System.out.println(methName + " return type is " + exp.getReturnType().getName());
											assertEquals("void", exp.getReturnType().getName());
											System.out.println(methName + " declaring type is " + exp.getDeclaringType().getFullyQualifiedName());
											assertEquals("boa.datagen.forges.github.GithubLanguageDownloadMaster", exp.getDeclaringType().getFullyQualifiedName());
										} else if (methName.equals("orchastrate")) {
											System.out.println(methName + " return type is " + exp.getReturnType().getName());
											assertEquals("void", exp.getReturnType().getName());
											System.out.println(methName + " declaring type is " + exp.getDeclaringType().getFullyQualifiedName());
											assertEquals("boa.datagen.forges.github.GithubLanguageDownloadMaster", exp.getDeclaringType().getFullyQualifiedName());
										} else if (methName.equals("start")) {
											System.out.println(methName + " return type is " + exp.getReturnType().getName());
											assertEquals("void", exp.getReturnType().getName());
											System.out.println(methName + " declaring type is " + exp.getDeclaringType().getFullyQualifiedName());
											assertEquals("java.lang.Thread", exp.getDeclaringType().getFullyQualifiedName());
										} else if (methName.equals("listFiles")) {
											System.out.println(methName + " return type is " + exp.getReturnType().getName());
											assertEquals("java.io.File[]", exp.getReturnType().getFullyQualifiedName());
											System.out.println(methName + " declaring type is " + exp.getDeclaringType().getFullyQualifiedName());
											assertEquals("java.io.File", exp.getDeclaringType().getFullyQualifiedName());
										} else if (methName.equals("println")) {
											System.out.println(methName + " return type is " + exp.getReturnType().getName());
											assertEquals("void", exp.getReturnType().getName());
											System.out.println(methName + " declaring type is " + exp.getDeclaringType().getFullyQualifiedName());
											assertEquals("java.io.PrintStream", exp.getDeclaringType().getFullyQualifiedName());
										} else if (methName.equals("getAsJsonObject")) {
											System.out.println(methName + " return type is " + exp.getReturnType().getName());
											// FIXME assertEquals("com.google.gson.JsonObject", exp.getReturnType().getFullyQualifiedName());
											System.out.println(methName + " declaring type is " + exp.getDeclaringType().getFullyQualifiedName());
											// FIXME assertEquals("com.google.gson.JsonElement", exp.getDeclaringType().getFullyQualifiedName());
										} else if (methName.equals("fromJson")) {
											System.out.println(methName + " return type is " + exp.getReturnType().getName());
											// FIXME assertEquals("com.google.gson.JsonElement", exp.getReturnType().getFullyQualifiedName());
											System.out.println(methName + " declaring type is " + exp.getDeclaringType().getFullyQualifiedName());
											// FIXME assertEquals("com.google.gson.JsonElement", exp.getDeclaringType().getFullyQualifiedName());
										} else if (methName.equals("getAsJsonArray")) {
											System.out.println(methName + " return type is " + exp.getReturnType().getName());
											// FXIME assertEquals("com.google.gson.JsonArray", exp.getReturnType().getFullyQualifiedName());
											System.out.println(methName + " declaring type is " + exp.getDeclaringType().getFullyQualifiedName());
											// FIXME assertEquals("com.google.gson.JsonElement", exp.getDeclaringType().getFullyQualifiedName());
										} else if (methName.equals("readFileContents")) {
											System.out.println(methName + " return type is " + exp.getReturnType().getName());
											assertEquals("java.lang.String", exp.getReturnType().getFullyQualifiedName());
											System.out.println(methName + " declaring type is " + exp.getDeclaringType().getFullyQualifiedName());
											assertEquals("boa.datagen.util.FileIO", exp.getDeclaringType().getFullyQualifiedName());
										} else if (methName.equals("getName")) {
											System.out.println(methName + " return type is " + exp.getReturnType().getName());
											assertEquals("java.lang.String", exp.getReturnType().getFullyQualifiedName());
											System.out.println(methName + " declaring type is " + exp.getDeclaringType().getFullyQualifiedName());
											assertEquals("java.io.File", exp.getDeclaringType().getFullyQualifiedName());
										} else if (methName.equals("getAsString")) {
											System.out.println(methName + " return type is " + exp.getReturnType().getName());
							//FIXME				assertEquals("java.lang.String", exp.getReturnType().getFullyQualifiedName());
											System.out.println(methName + " declaring type is " + exp.getDeclaringType().getFullyQualifiedName());
											// FIXME assertEquals("com.google.gson.JsonElement", exp.getDeclaringType().getFullyQualifiedName());
										} else if (methName.equals("get")) {
											System.out.println(methName + " return type is " + exp.getReturnType().getName());
											// FIXME assertEquals("com.google.gson.JsonElement", exp.getReturnType().getFullyQualifiedName());
											System.out.println(methName + " declaring type is " + exp.getDeclaringType().getFullyQualifiedName());
											// FIXME assertEquals("com.google.gson.JsonObject", exp.getDeclaringType().getFullyQualifiedName());
										} else if (methName.equals("add")) {
											System.out.println(methName + " return type is " + exp.getReturnType().getName());
							//FIXME				assertEquals("boolean", exp.getReturnType().getName());
											System.out.println(methName + " declaring type is "
													+ exp.getDeclaringType().getFullyQualifiedName());
											// FIXME assertEquals("gnu.trove.set.hash.THashSet", exp.getDeclaringType().getFullyQualifiedName());
										} else
											System.out.println("missed " + methName + " is " + exp.getReturnType().getFullyQualifiedName());
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
	}
}
