package boa.datascience.externalDataSources.javadata;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.google.protobuf.GeneratedMessage;

import boa.datagen.util.FileIO;
import boa.datagen.util.Java7Visitor;
import boa.datagen.util.Java8Visitor;
import boa.datagen.util.JavaErrorCheckVisitor;
import boa.datascience.externalDataSources.AbstractDataReader;
import boa.types.Ast.ASTRoot;
import boa.types.Diff.ChangedFile.FileKind;

public class JavaDataReader extends AbstractDataReader {
	private final String JAVAPARSERCLASS = "boa.types.code.CodeRepository.ASTRoot";
	private static final boolean debug = false;
	private String content;
	private ASTRoot.Builder ast;
	private FileKind kind;

	public JavaDataReader(String source, String content) {
		super(source);
		this.content = content;
	}

	public JavaDataReader(String source) {
		super(source);
		this.content = FileIO.readFileContents(new File(source));
	}

	@Override
	public boolean isReadable(String source) {
		return source.endsWith(".java");
	}

	@Override
	public List<GeneratedMessage> getData() {
		ArrayList<GeneratedMessage> file = new ArrayList<GeneratedMessage>();
		file.add(processChangeFile(this.dataSource, true));
		return file;
	}

	public FileKind getKind() {
		return kind;
	}

	public void setKind(FileKind kind) {
		this.kind = kind;
	}

	@Override
	public String getParserClassName() {
		return this.JAVAPARSERCLASS;
	}

	private ASTRoot processChangeFile(String path, boolean parse) {
		final String lowerPath = path.toLowerCase();
		if (lowerPath.endsWith(".java") && parse) {
			this.kind = FileKind.SOURCE_JAVA_JLS2;
			if (!parseJavaFile(path, content, JavaCore.VERSION_1_4, AST.JLS2, false)) {
				if (debug)
					System.err.println("Found JLS2 parse error in: revision " + ": file " + path);

				this.kind = FileKind.SOURCE_JAVA_JLS3;
				if (!parseJavaFile(path, content, JavaCore.VERSION_1_5, AST.JLS3, false)) {
					if (debug)
						System.err.println("Found JLS3 parse error in: revision " + ": file " + path);

					this.kind = FileKind.SOURCE_JAVA_JLS4;
					if (!parseJavaFile(path, content, JavaCore.VERSION_1_7, AST.JLS4, false)) {
						if (debug)
							System.err.println("Found JLS4 parse error in: revision " + ": file " + path);

						this.kind = FileKind.SOURCE_JAVA_JLS8;
						if (!parseJavaFile(path, content, JavaCore.VERSION_1_8, AST.JLS8, false)) {
							if (debug)
								System.err.println("Found JLS8 parse error in: revision " + ": file " + path);

							this.kind = FileKind.SOURCE_JAVA_ERROR;
						} else if (debug)
							System.err.println("Accepted JLS8: revision " + ": file " + path);
					} else if (debug)
						System.err.println("Accepted JLS4: revision " + ": file " + path);
				} else if (debug)
					System.err.println("Accepted JLS3: revision " + ": file " + path);
			} else if (debug)
				System.err.println("Accepted JLS2: revision " + ": file " + path);
		}
		return this.ast != null ? this.ast.build(): null;
	}

	private boolean parseJavaFile(final String path, final String content, final String compliance, final int astLevel,
			final boolean storeOnError) {
		try {
			final ASTParser parser = ASTParser.newParser(astLevel);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setResolveBindings(true);
			parser.setSource(content.toCharArray());

			final Map options = JavaCore.getOptions();
			JavaCore.setComplianceOptions(compliance, options);
			parser.setCompilerOptions(options);

			final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

			final JavaErrorCheckVisitor errorCheck = new JavaErrorCheckVisitor();
			cu.accept(errorCheck);

			if (!errorCheck.hasError || storeOnError) {
				final ASTRoot.Builder ast = ASTRoot.newBuilder();
				// final CommentsRoot.Builder comments =
				// CommentsRoot.newBuilder();
				final Java7Visitor visitor;
				if (astLevel == AST.JLS8)
					visitor = new Java8Visitor(content, new HashMap<String, Integer>());
				else
					visitor = new Java7Visitor(content, new HashMap<String, Integer>());
				try {
					ast.addNamespaces(visitor.getNamespaces(cu));
					for (final String s : visitor.getImports())
						ast.addImports(s);
					/*
					 * for (final Comment c : visitor.getComments())
					 * comments.addComments(c);
					 */
				} catch (final UnsupportedOperationException e) {
					return false;
				} catch (final Exception e) {
					if (debug)
						System.err.println("Error visiting: " + path);
					e.printStackTrace();
					return false;
				}
				this.ast = ast;
				;
				// fb.setComments(comments);
			}

			return !errorCheck.hasError;
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}
