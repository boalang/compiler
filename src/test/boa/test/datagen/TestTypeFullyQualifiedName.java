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
										assertEquals(true,
												fqn.equals("boa.datagen.forges.github.GithubLanguageDownloadMaster"));
										fqn = exp.getDeclaringType().getFullyQualifiedName();
										System.out.println(varOrMeth + " delcaring type is " + fqn);
										assertEquals(true,
												fqn.equals("boa.datagen.forges.github.GithubLanguageDownloadMaster"));
										fqn = exp.getReturnType().getFullyQualifiedName();
										System.out.println(varOrMeth + " return type is " + fqn);
										assertEquals(true, fqn.equals("java.lang.String"));
									} else if (exp.getReturnType() != null) {
										if (exp.getReturnType().getName().equals("String")) {
											String fqn = exp.getReturnType().getFullyQualifiedName();
											System.out.println(
													exp.getKind() + " " + varOrMeth + " return type is " + fqn);
											assertEquals(true, fqn.equals("java.lang.String"));
										} else if (exp.getReturnType().getName().equals("File")) {
											String fqn = exp.getReturnType().getFullyQualifiedName();
											System.out.println(
													exp.getKind() + " " + varOrMeth + " return type is " + fqn);
											assertEquals(true, fqn.equals("java.io.File"));
										} else if (exp.getReturnType().getName().equals("String[]")) {
											String fqn = exp.getReturnType().getFullyQualifiedName();
											System.out.println(
													exp.getKind() + " " + varOrMeth + " return type is " + fqn);
											assertEquals(true, fqn.equals("java.lang.String[]"));
										} else if (exp.getReturnType().getName().equals("int")) {
											System.out.println(
													exp.getKind() + " " + varOrMeth + " return type is " + "int");
											assertEquals(true, exp.getReturnType().getFullyQualifiedName().isEmpty());
										} else if (exp.getReturnType().getName().equals("boolean")) {
											System.out.println(
													exp.getKind() + " " + varOrMeth + " return type is " + "boolean");
											assertEquals(true, exp.getReturnType().getFullyQualifiedName().isEmpty());
										} else if (exp.getReturnType().getName().equals("void")) {
											System.out.println(
													exp.getKind() + " " + varOrMeth + " return type is " + "void");
											assertEquals(true, exp.getReturnType().getFullyQualifiedName().isEmpty());
										} else if (exp.getReturnType().getName()
												.equals("GithubLanguageDownloadMaster")) {
											String fqn = exp.getReturnType().getFullyQualifiedName();
											System.out.println(
													exp.getKind() + " " + varOrMeth + " return type is " + fqn);
											assertEquals(true, fqn
													.equals("boa.datagen.forges.github.GithubLanguageDownloadMaster"));
										} else if (exp.getReturnType().getName().equals("LanguageDownloadWorker")) {
											String fqn = exp.getReturnType().getFullyQualifiedName();
											System.out.println(
													exp.getKind() + " " + varOrMeth + " return type is " + fqn);
											assertEquals(true,
													fqn.equals("boa.datagen.forges.github.LanguageDownloadWorker"));
										} else if (exp.getReturnType().getName().equals("TokenList")) {
											String fqn = exp.getReturnType().getFullyQualifiedName();
											System.out.println(
													exp.getKind() + " " + varOrMeth + " return type is " + fqn);
											assertEquals(true, fqn.equals("boa.datagen.forges.github.TokenList"));
										} else if (exp.getReturnType().getName().equals("PrintStream")) {
											String fqn = exp.getReturnType().getFullyQualifiedName();
											System.out.println(
													exp.getKind() + " " + varOrMeth + " return type is " + fqn);
											assertEquals(true, fqn.equals("java.io.PrintStream"));
										}
									}
									if (exp.getDeclaringType() != null) {
										if (exp.getDeclaringType().getName().equals("GithubLanguageDownloadMaster")) {
											String fqn = exp.getDeclaringType().getFullyQualifiedName();
											System.out.println(
													exp.getKind() + " " + varOrMeth + " declaring type is " + fqn);
											assertEquals(true, fqn
													.equals("boa.datagen.forges.github.GithubLanguageDownloadMaster"));
										} else if (exp.getDeclaringType().getName().equals("Thread")) {
											String fqn = exp.getDeclaringType().getFullyQualifiedName();
											System.out.println(exp.getKind() + " " + exp.getMethod()
													+ " declaring type is " + fqn);
											assertEquals(true, fqn.equals("java.lang.Thread"));
										} else if (exp.getDeclaringType().equals("System")) {
											String fqn = exp.getDeclaringType().getFullyQualifiedName();
											System.out.println(
													exp.getKind() + " " + varOrMeth + " declaring type is " + fqn);
											assertEquals(true, fqn.equals("java.lang.System"));
										} else if (exp.getDeclaringType().getName().equals("PrintStream")) {
											String fqn = exp.getDeclaringType().getFullyQualifiedName();
											System.out.println(
													exp.getKind() + " " + varOrMeth + " declaring type is " + fqn);
											assertEquals(true, fqn.equals("java.io.PrintStream"));
										} else if (exp.getDeclaringType().getName().equals("FileIO")) {
											String fqn = exp.getDeclaringType().getFullyQualifiedName();
											System.out.println(
													exp.getKind() + " " + varOrMeth + " declaring type is " + fqn);
											assertEquals(true, fqn.equals("boa.datagen.util.FileIO"));
										}
									}
									for (Variable var : exp.getVariableDeclsList()) {
										if (var.getVariableType().getName().equals("LanguageDownloadWorker")) {
											String fqn = var.getVariableType().getFullyQualifiedName();
											System.out.println(
													exp.getKind() + " " + var.getName() + " variable type is " + fqn);
											assertEquals(true,
													fqn.equals("boa.datagen.forges.github.LanguageDownloadWorker"));
										} else if (var.getVariableType().getName().equals("TokenList")) {
											String fqn = var.getVariableType().getFullyQualifiedName();
											System.out.println(
													exp.getKind() + " " + var.getName() + " variable type is " + fqn);
											assertEquals(true, fqn.equals("boa.datagen.forges.github.TokenList"));
										} else if (var.getVariableType().getName().equals("File")) {
											String fqn = var.getVariableType().getFullyQualifiedName();
											System.out.println(
													exp.getKind() + " " + var.getName() + " variable type is " + fqn);
											assertEquals(true, fqn.equals("java.io.File"));
										} else if (var.getVariableType().getName().equals("File[]")) {
											String fqn = var.getVariableType().getFullyQualifiedName();
											System.out.println(
													exp.getKind() + " " + var.getName() + " variable type is " + fqn);
											assertEquals(true, fqn.equals("java.io.File[]"));
										} else if (var.getVariableType().getName().equals("String")) {
											String fqn = var.getVariableType().getFullyQualifiedName();
											System.out.println(
													exp.getKind() + " " + var.getName() + " variable type is " + fqn);
											assertEquals(true, fqn.equals("java.lang.String"));
										} else if (var.getVariableType().getName()
												.equals("GithubLanguageDownloadMaster")) {
											String fqn = var.getVariableType().getFullyQualifiedName();
											System.out.println(
													exp.getKind() + " " + var.getName() + " variable type is " + fqn);
											assertEquals(true, fqn
													.equals("boa.datagen.forges.github.GithubLanguageDownloadMaster"));
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
										switch (var) {
										case "repoNameDir":
											System.out.println(exp.getReturnType().getFullyQualifiedName()
													+ " is java.lang.String");
											assertEquals(true, exp.getReturnType().getFullyQualifiedName()
													.equals("java.lang.String"));
											System.out.println(exp.getDeclaringType().getFullyQualifiedName()
													+ " is boa.datagen.forges.github.GithubLanguageDownloadMaster");
											assertEquals(true, exp.getDeclaringType().getFullyQualifiedName()
													.equals("boa.datagen.forges.github.GithubLanguageDownloadMaster"));
											break;
										case "langNameDir":
											System.out.println(exp.getReturnType() + " is java.lang.String");
											assertEquals(true, exp.getReturnType().getFullyQualifiedName()
													.equals("java.lang.String"));
											System.out.println(exp.getDeclaringType().getFullyQualifiedName()
													+ " is boa.datagen.forges.github.GithubLanguageDownloadMaster");
											assertEquals(true, exp.getDeclaringType().getFullyQualifiedName()
													.equals("boa.datagen.forges.github.GithubLanguageDownloadMaster"));
											break;
										case "tokenFile":
											System.out.println(exp.getReturnType().getFullyQualifiedName()
													+ " is java.lang.String");
											assertEquals(true, exp.getReturnType().getFullyQualifiedName()
													.equals("java.lang.String"));
											System.out.println(exp.getDeclaringType().getFullyQualifiedName()
													+ " is boa.datagen.forges.github.GithubLanguageDownloadMaster");
											// FIXME assertEquals(true,
											// exp.getDeclaringType().getFullyQualifiedName().equals("boa.datagen.forges.github.GithubLanguageDownloadMaster"));
											break;
										case "MAX_NUM_THREADS":
											System.out.println(exp.getReturnType().getName() + " is int");
											assertEquals(true, exp.getReturnType().getName().equals("int"));
											System.out.println(exp.getDeclaringType().getFullyQualifiedName()
													+ " is boa.datagen.forges.github.GithubLanguageDownloadMaster");
											assertEquals(true, exp.getDeclaringType().getFullyQualifiedName()
													.equals("boa.datagen.forges.github.GithubLanguageDownloadMaster"));
											break;
										case "names":
											System.out.println(exp.getReturnType().getFullyQualifiedName()
													+ " is gnu.trove.set.hash.THashSet<String>");
											System.out
													.println(var + " return type is " + exp.getReturnType().getName());
											// FIXME assertEquals(true,
											// exp.getReturnType().getFullyQualifiedName().equals("gnu.trove.set.hash.THashSet<String>"));
											System.out.println(exp.getDeclaringType().getFullyQualifiedName()
													+ " is boa.datagen.forges.github.GithubLanguageDownloadMaster");
											System.out.println(
													var + " declaring type is " + exp.getDeclaringType().getName());
											// FIXME assertEquals(true,
											// exp.getDeclaringType().getFullyQualifiedName().equals("boa.datagen.forges.github.GithubLanguageDownloadMaster"));
											break;
										case "input":
											System.out.println(exp.getReturnType().getFullyQualifiedName()
													+ " is java.lang.String");
											assertEquals(true, exp.getReturnType().getFullyQualifiedName()
													.equals("java.lang.String"));
											break;
										case "output":
											System.out.println(exp.getReturnType().getFullyQualifiedName()
													+ " is java.lang.String");
											assertEquals(true, exp.getReturnType().getFullyQualifiedName()
													.equals("java.lang.String"));
											break;
										case "token":
											System.out.println(exp.getReturnType().getFullyQualifiedName()
													+ " is java.lang.String");
											assertEquals(true, exp.getReturnType().getFullyQualifiedName()
													.equals("java.lang.String"));
											break;
										case "outputDir":
											System.out.println(
													exp.getReturnType().getFullyQualifiedName() + " is java.io.File");
											assertEquals(true,
													exp.getReturnType().getFullyQualifiedName().equals("java.io.File"));
											break;
										case "args":
											System.out.println(exp.getReturnType().getFullyQualifiedName()
													+ " is java.lang.String[]");
											assertEquals(true, exp.getReturnType().getFullyQualifiedName()
													.equals("java.lang.String[]"));
											break;
										case "master":
											System.out.println(exp.getReturnType().getFullyQualifiedName()
													+ " is boa.datagen.forges.github.GithubLanguageDownloadMaster");
											assertEquals(true, exp.getReturnType().getFullyQualifiedName()
													.equals("boa.datagen.forges.github.GithubLanguageDownloadMaster"));
											break;
										case "start":
											System.out.println(exp.getReturnType().getName() + " is int");
											assertEquals(true, exp.getReturnType().getName().equals("int"));
											break;
										case "end":
											System.out.println(exp.getReturnType().getName() + " is int");
											assertEquals(true, exp.getReturnType().getName().equals("int"));
											break;
										case "shareSize":
											System.out.println(exp.getReturnType().getName() + " is int");
											assertEquals(true, exp.getReturnType().getName().equals("int"));
											break;
										case "i":
											System.out.println(exp.getReturnType().getName() + " is int");
											assertEquals(true, exp.getReturnType().getName().equals("int"));
											break;
										case "tokens":
											System.out.println(exp.getReturnType().getFullyQualifiedName()
													+ " is boa.datagen.forges.github.TokenList");
											assertEquals(true, exp.getReturnType().getFullyQualifiedName()
													.equals("boa.datagen.forges.github.TokenList"));
											break;
										case "worker":
											System.out.println(exp.getReturnType().getFullyQualifiedName()
													+ " is boa.datagen.forges.github.LanguageDownloadWorker");
											assertEquals(true, exp.getReturnType().getFullyQualifiedName()
													.equals("boa.datagen.forges.github.LanguageDownloadWorker"));
											break;
										case "totalFies":
											System.out.println(exp.getReturnType().getName() + " is int");
											assertEquals(true, exp.getReturnType().getName().equals("int"));
											break;
										case "files.length":
											System.out.println(exp.getReturnType().getName() + " is int");
											assertEquals(true, exp.getReturnType().getName().equals("int"));
											break;
										case "args.length":
											System.out.println(exp.getReturnType().getName() + " is int");
											assertEquals(true, exp.getReturnType().getName().equals("int"));
											break;
										case "length":
											System.out.println(exp.getReturnType().getName() + " is int");
											assertEquals(true, exp.getReturnType().getName().equals("int"));
											break;
										case "dir":
											System.out.println(
													exp.getReturnType().getFullyQualifiedName() + " is java.io.File");
											assertEquals(true,
													exp.getReturnType().getFullyQualifiedName().equals("java.io.File"));
											break;
										case "files":
											System.out.println(
													exp.getReturnType().getFullyQualifiedName() + " is java.io.File[]");
											assertEquals(true, exp.getReturnType().getFullyQualifiedName()
													.equals("java.io.File[]"));
											break;
										case "content":
											System.out.println(exp.getReturnType().getFullyQualifiedName()
													+ " is java.lang.String");
											assertEquals(true, exp.getReturnType().getFullyQualifiedName()
													.equals("java.lang.String"));
											break;
										case "filePath":
											System.out.println(exp.getReturnType().getFullyQualifiedName()
													+ " is java.lang.String");
											assertEquals(true, exp.getReturnType().getFullyQualifiedName()
													.equals("java.lang.String"));
											break;
										case "repos":
											System.out.println(exp.getReturnType().getFullyQualifiedName()
													+ " is com.google.gson");
											// FIXME System.out.println(var + "
											// is " +
											// exp.getReturnType().getName());
											// FIXME assertEquals(true,
											// exp.getReturnType().getFullyQualifiedName().equals("com.google.gson"));
											break;
										case "parser":
											System.out.println(exp.getReturnType().getFullyQualifiedName()
													+ " is com.google.gson");
											// FIXME assertEquals(true,
											// exp.getReturnType().getFullyQualifiedName().equals("com.google.gson"));
											break;
										case "repoE":
											System.out.println(exp.getReturnType().getFullyQualifiedName()
													+ " is com.google.gson");
											// FIXME assertEquals(true,
											// exp.getReturnType().getFullyQualifiedName().equals("com.google.gson"));
											break;
										case "repo":
											System.out.println(exp.getReturnType().getFullyQualifiedName()
													+ " is com.google.gson");
											// FXIME assertEquals(true,
											// exp.getReturnType().getFullyQualifiedName().equals("com.google.gson"));
											break;
										case "FileIO":
											System.out.println(exp.getReturnType().getFullyQualifiedName()
													+ " is boa.datagen.util.FileIO");
											// FXIME assertEquals(true,
											// exp.getReturnType().getFullyQualifiedName().equals("boa.datagen.util.FileIO"));
											break;
										case "System.out":
											System.out.println(exp.getReturnType().getFullyQualifiedName()
													+ " is java.io.PrintStream");
											assertEquals(true, exp.getReturnType().getFullyQualifiedName()
													.equals("java.io.PrintStream"));
											break;
										case "master.repoNameDir":
											System.out.println(exp.getReturnType().getFullyQualifiedName()
													+ " is java.lang.String");
											assertEquals(true, exp.getReturnType().getFullyQualifiedName()
													.equals("java.lang.String"));
											break;
										default:
											System.out.println("missed " + var + " is "
													+ exp.getReturnType().getFullyQualifiedName());
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
										switch (var.getName()) {
										case "repoNameDir":
											System.out.println(var.getName() + " is "
													+ var.getVariableType().getFullyQualifiedName()
													+ " is java.lang.String");
											assertEquals(true, var.getVariableType().getFullyQualifiedName()
													.equals("java.lang.String"));
											System.out.println(var.getName() + " declaring is "
													+ decl.getFullyQualifiedName()
													+ " is boa.datagen.forges.github.GithubLanguageDownloadMaster");
											assertEquals(true, decl.getFullyQualifiedName()
													.equals("boa.datagen.forges.github.GithubLanguageDownloadMaster"));
											break;
										case "langNameDir":
											System.out.println(var.getName() + " is "
													+ var.getVariableType().getFullyQualifiedName()
													+ " is java.lang.String");
											assertEquals(true, var.getVariableType().getFullyQualifiedName()
													.equals("java.lang.String"));
											System.out.println(var.getName() + " declaring type is "
													+ decl.getFullyQualifiedName()
													+ " is boa.datagen.forges.github.GithubLanguageDownloadMaster");
											assertEquals(true, decl.getFullyQualifiedName()
													.equals("boa.datagen.forges.github.GithubLanguageDownloadMaster"));
											break;
										case "tokenFile":
											System.out.println(var.getName() + " is "
													+ var.getVariableType().getFullyQualifiedName()
													+ " is java.lang.String");
											assertEquals(true, var.getVariableType().getFullyQualifiedName()
													.equals("java.lang.String"));
											System.out.println(var.getName() + " declaring type is "
													+ decl.getFullyQualifiedName()
													+ " is boa.datagen.forges.github.GithubLanguageDownloadMaster");
											assertEquals(true, decl.getFullyQualifiedName()
													.equals("boa.datagen.forges.github.GithubLanguageDownloadMaster"));
											break;
										case "MAX_NUM_THREADS":
											System.out.println(var.getName() + " is " + var.getVariableType().getName()
													+ " is int");
											assertEquals(true, var.getVariableType().getName().equals("int"));
											System.out.println(var.getName() + " declaring type is "
													+ decl.getFullyQualifiedName()
													+ " is boa.datagen.forges.github.GithubLanguageDownloadMaster");
											assertEquals(true, decl.getFullyQualifiedName()
													.equals("boa.datagen.forges.github.GithubLanguageDownloadMaster"));
											break;
										case "names":
											System.out.println(var.getName() + " is "
													+ var.getVariableType().getFullyQualifiedName()
													+ " is gnu.trove.set.hash.THashSet<String>");
											// FIXME assertEquals(true,
											// var.getVariableType().getFullyQualifiedName().equals("gnu.trove.set.hash.THashSet<String>"));
											System.out.println(var.getName() + " declaring type is "
													+ decl.getFullyQualifiedName()
													+ " is boa.datagen.forges.github.GithubLanguageDownloadMaster");
											assertEquals(true, decl.getFullyQualifiedName()
													.equals("boa.datagen.forges.github.GithubLanguageDownloadMaster"));
											break;
										default:
											System.out.println("missed " + var.getName() + " is "
													+ var.getVariableType().getFullyQualifiedName());
										}
									}
								}
								if (message instanceof boa.types.Ast.Expression) {
									boa.types.Ast.Expression exp = (boa.types.Ast.Expression) message;
									if (exp.getKind().equals(Expression.ExpressionKind.VARDECL)) {
										for (Variable var : exp.getVariableDeclsList()) {
											switch (var.getName()) {
											case "outputDir":
												System.out.println(var.getName() + " "
														+ var.getVariableType().getFullyQualifiedName()
														+ " is java.io.File");
												assertEquals(true, var.getVariableType().getFullyQualifiedName()
														.equals("java.io.File"));
												break;
											case "master":
												System.out.println(var.getName() + " "
														+ var.getVariableType().getFullyQualifiedName()
														+ " is boa.datagen.forges.github.GithubLanguageDownloadMaster");
												assertEquals(true, var.getVariableType().getFullyQualifiedName().equals(
														"boa.datagen.forges.github.GithubLanguageDownloadMaster"));
												break;
											case "start":
												System.out.println(var.getName() + " " + var.getVariableType().getName()
														+ " is int");
												assertEquals(true, var.getVariableType().getName().equals("int"));
												break;
											case "end":
												System.out.println(var.getName() + " " + var.getVariableType().getName()
														+ " is int");
												assertEquals(true, var.getVariableType().getName().equals("int"));
												break;
											case "shareSize":
												System.out.println(var.getName() + " " + var.getVariableType().getName()
														+ " is int");
												assertEquals(true, var.getVariableType().getName().equals("int"));
												break;
											case "i":
												System.out.println(var.getName() + " " + var.getVariableType().getName()
														+ " is int");
												assertEquals(true, var.getVariableType().getName().equals("int"));
												break;
											case "tokens":
												System.out.println(var.getName() + " "
														+ var.getVariableType().getFullyQualifiedName()
														+ " is boa.datagen.forges.github.TokenList");
												assertEquals(true, var.getVariableType().getFullyQualifiedName()
														.equals("boa.datagen.forges.github.TokenList"));
												break;
											case "worker":
												System.out.println(var.getName() + " "
														+ var.getVariableType().getFullyQualifiedName()
														+ " is boa.datagen.forges.github.LanguageDownloadWorker");
												assertEquals(true, var.getVariableType().getFullyQualifiedName()
														.equals("boa.datagen.forges.github.LanguageDownloadWorker"));
												break;
											case "dir":
												System.out.println(var.getName() + " "
														+ var.getVariableType().getFullyQualifiedName()
														+ " is java.io.File");
												assertEquals(true, var.getVariableType().getFullyQualifiedName()
														.equals("java.io.File"));
												break;
											case "files":
												System.out.println(var.getName() + " "
														+ var.getVariableType().getFullyQualifiedName()
														+ " is java.io.File[]");
												assertEquals(true, var.getVariableType().getFullyQualifiedName()
														.equals("java.io.File[]"));
												break;
											case "content":
												System.out.println(var.getName() + " "
														+ var.getVariableType().getFullyQualifiedName()
														+ " is java.lang.String");
												assertEquals(true, var.getVariableType().getFullyQualifiedName()
														.equals("java.lang.String"));
												break;
											case "repos":
												System.out.println(var.getName() + " "
														+ var.getVariableType().getFullyQualifiedName()
														+ " is com.google.gson");
												System.out.println(
														var.getName() + " is " + var.getVariableType().getName());
												// FIXME assertEquals(true,
												// var.getVariableType().getFullyQualifiedName().equals("com.google.gson"));
												break;
											case "parser":
												System.out.println(var.getName() + " "
														+ var.getVariableType().getFullyQualifiedName()
														+ " is com.google.gson");
												// FXIME assertEquals(true,
												// var.getVariableType().getFullyQualifiedName().equals("com.google.gson"));
												break;
											case "repo":
												System.out.println(var.getName() + " "
														+ var.getVariableType().getFullyQualifiedName()
														+ " is com.google.gson");
												// FIXME assertEquals(true,
												// var.getVariableType().getFullyQualifiedName().equals("com.google.gson"));
												break;
											default:
												System.out.println("missed " + var.getName() + " is "
														+ var.getVariableType().getFullyQualifiedName());
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
										switch (methName) {
										case "exists":
											System.out.println(
													methName + " return type is " + exp.getReturnType().getName());
											assertEquals(true, exp.getReturnType().getName().equals("boolean"));
											System.out.println(methName + " declaring type is "
													+ exp.getDeclaringType().getFullyQualifiedName());
											assertEquals(true, exp.getDeclaringType().getFullyQualifiedName()
													.equals("java.io.File"));
											break;
										case "mkdirs":
											System.out.println(
													methName + " return type is " + exp.getReturnType().getName());
											assertEquals(true, exp.getReturnType().getName().equals("boolean"));
											System.out.println(methName + " declaring type is "
													+ exp.getDeclaringType().getFullyQualifiedName());
											assertEquals(true, exp.getDeclaringType().getFullyQualifiedName()
													.equals("java.io.File"));
											break;
										case "addNames":
											System.out.println(
													methName + " return type is " + exp.getReturnType().getName());
											assertEquals(true, exp.getReturnType().getName().equals("void"));
											System.out.println(methName + " declaring type is "
													+ exp.getDeclaringType().getFullyQualifiedName());
											assertEquals(true, exp.getDeclaringType().getFullyQualifiedName()
													.equals("boa.datagen.forges.github.GithubLanguageDownloadMaster"));
											break;
										case "orchastrate":
											System.out.println(
													methName + " return type is " + exp.getReturnType().getName());
											assertEquals(true, exp.getReturnType().getName().equals("void"));
											System.out.println(methName + " declaring type is "
													+ exp.getDeclaringType().getFullyQualifiedName());
											assertEquals(true, exp.getDeclaringType().getFullyQualifiedName()
													.equals("boa.datagen.forges.github.GithubLanguageDownloadMaster"));
											break;
										case "start":
											System.out.println(
													methName + " return type is " + exp.getReturnType().getName());
											assertEquals(true, exp.getReturnType().getName().equals("void"));
											System.out.println(methName + " declaring type is "
													+ exp.getDeclaringType().getFullyQualifiedName());
											assertEquals(true, exp.getDeclaringType().getFullyQualifiedName()
													.equals("java.lang.Thread"));
											break;
										case "listFiles":
											System.out.println(
													methName + " return type is " + exp.getReturnType().getName());
											assertEquals(true, exp.getReturnType().getFullyQualifiedName()
													.equals("java.io.File[]"));
											System.out.println(methName + " declaring type is "
													+ exp.getDeclaringType().getFullyQualifiedName());
											assertEquals(true, exp.getDeclaringType().getFullyQualifiedName()
													.equals("java.io.File"));
											break;
										case "println":
											System.out.println(
													methName + " return type is " + exp.getReturnType().getName());
											assertEquals(true, exp.getReturnType().getName().equals("void"));
											System.out.println(methName + " declaring type is "
													+ exp.getDeclaringType().getFullyQualifiedName());
											// FIXME assertEquals(true,
											// exp.getDeclaringType().getFullyQualifiedName().equals("java.io.PrintStream."));
											break;
										case "getAsJsonObject":
											System.out.println(
													methName + " return type is " + exp.getReturnType().getName());
											// FIXME assertEquals(true,
											// exp.getReturnType().getFullyQualifiedName().equals("com.google.gson.JsonObject"));
											System.out.println(methName + " declaring type is "
													+ exp.getDeclaringType().getFullyQualifiedName());
											// FIXME assertEquals(true,
											// exp.getDeclaringType().getFullyQualifiedName().equals("com.google.gson.JsonElement"));
											break;
										case "fromJson":
											System.out.println(
													methName + " return type is " + exp.getReturnType().getName());
											// FIXME assertEquals(true,
											// exp.getReturnType().getFullyQualifiedName().equals("com.google.gson.JsonElement"));
											System.out.println(methName + " declaring type is "
													+ exp.getDeclaringType().getFullyQualifiedName());
											// FIXME assertEquals(true,
											// exp.getDeclaringType().getFullyQualifiedName().equals("com.google.gson.JsonElement"));
											break;
										case "getAsJsonArray":
											System.out.println(
													methName + " return type is " + exp.getReturnType().getName());
											// FXIME assertEquals(true,
											// exp.getReturnType().getFullyQualifiedName().equals("com.google.gson.JsonArray"));
											System.out.println(methName + " declaring type is "
													+ exp.getDeclaringType().getFullyQualifiedName());
											// FIXME assertEquals(true,
											// exp.getDeclaringType().getFullyQualifiedName().equals("com.google.gson.JsonElement"));
											break;
										case "readFileContents":
											System.out.println(
													methName + " return type is " + exp.getReturnType().getName());
											assertEquals(true, exp.getReturnType().getFullyQualifiedName()
													.equals("java.lang.String"));
											System.out.println(methName + " declaring type is "
													+ exp.getDeclaringType().getFullyQualifiedName());
											assertEquals(true, exp.getDeclaringType().getFullyQualifiedName()
													.equals("boa.datagen.util.FileIO"));
											break;
										case "getName":
											System.out.println(
													methName + " return type is " + exp.getReturnType().getName());
											assertEquals(true, exp.getReturnType().getFullyQualifiedName()
													.equals("java.lang.String"));
											System.out.println(methName + " declaring type is "
													+ exp.getDeclaringType().getFullyQualifiedName());
											assertEquals(true, exp.getDeclaringType().getFullyQualifiedName()
													.equals("java.io.File"));
											break;
										case "getAsString":
											System.out.println(
													methName + " return type is " + exp.getReturnType().getName());
											// FIXME assertEquals(true,
											// exp.getReturnType().getFullyQualifiedName().equals("java.lang.String"));
											System.out.println(methName + " declaring type is "
													+ exp.getDeclaringType().getFullyQualifiedName());
											// FIXME assertEquals(true,
											// exp.getDeclaringType().getFullyQualifiedName().equals("com.google.gson.JsonElement"));
											break;
										case "get":
											System.out.println(
													methName + " return type is " + exp.getReturnType().getName());
											// FIXME assertEquals(true,
											// exp.getReturnType().getFullyQualifiedName().equals("com.google.gson.JsonElement"));
											System.out.println(methName + " declaring type is "
													+ exp.getDeclaringType().getFullyQualifiedName());
											// FIXME assertEquals(true,
											// exp.getDeclaringType().getFullyQualifiedName().equals("com.google.gson.JsonObject"));
											break;
										case "add":
											System.out.println(
													methName + " return type is " + exp.getReturnType().getName());
											// FIXME assertEquals(true,
											// exp.getReturnType().getName().equals("boolean"));
											System.out.println(methName + " declaring type is "
													+ exp.getDeclaringType().getFullyQualifiedName());
											// FIXME assertEquals(true,
											// exp.getDeclaringType().getFullyQualifiedName().equals("gnu.trove.set.hash.THashSet"));
											break;
										default:
											System.out.println("missed " + methName + " is "
													+ exp.getReturnType().getFullyQualifiedName());
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
}
