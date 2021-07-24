/*
 * Copyright 2016-2021, Hridesh Rajan, Robert Dyer, Hoan Nguyen, Samuel W. Flint,
 *                 Iowa State University of Science and Technology
 *                 Bowling Green State University
 *                 and University of Nebraska Board of Regents
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package boa.datagen.scm;

import java.io.*;
import java.util.*;

import com.steadystate.css.dom.CSSStyleSheetImpl;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile.Writer;
import org.dom4j.dom.DOMDocument;
import org.dom4j.io.SAXReader;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.php.internal.core.PHPVersion;
import org.eclipse.php.internal.core.ast.nodes.Program;
import org.jetbrains.kotlin.psi.KtFile;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ast.AstRoot;
import org.w3c.css.sac.InputSource;

import boa.types.Ast.ASTRoot;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Diff.ChangedFile.Builder;
import boa.types.Diff.ChangedFile.FileKind;
import boa.types.Shared.ChangeKind;
import boa.types.Shared.Person;
import boa.datagen.DefaultProperties;
import boa.datagen.dependencies.PomFile;
import boa.datagen.util.CssVisitor;
import boa.datagen.util.FileIO;
import boa.datagen.util.HtmlVisitor;
import boa.datagen.util.JavaErrorCheckVisitor;
import boa.datagen.util.JavaScriptErrorCheckVisitor;
import boa.datagen.util.JavaScriptVisitor;
import boa.datagen.util.JavaVisitor;
import boa.datagen.util.KotlinVisitor;
import boa.datagen.util.PHPErrorCheckVisitor;
import boa.datagen.util.PHPVisitor;
import boa.datagen.util.Properties;
import boa.datagen.util.XMLVisitor;
import boa.functions.langmode.KotlinLangMode;


/**
 * @author rdyer
 */
public abstract class AbstractCommit {
	protected static final boolean debug = Properties.getBoolean("debug", DefaultProperties.DEBUG);
	protected static final boolean debugparse = Properties.getBoolean("debugparse", DefaultProperties.DEBUGPARSE);
	protected static final boolean STORE_ASCII_PRINTABLE_CONTENTS = Properties.getBoolean("ascii", DefaultProperties.STORE_ASCII_PRINTABLE_CONTENTS);

	protected AbstractConnector connector;
	protected String projectName;

	protected AbstractCommit(AbstractConnector cnn) {
		this.connector = cnn;
	}

	protected Map<String, Integer> fileNameIndices = new HashMap<String, Integer>();
	protected List<ChangedFile.Builder> changedFiles = new ArrayList<ChangedFile.Builder>();

	protected ChangedFile.Builder getChangedFile(String path, ChangeKind changeKind) {
		ChangedFile.Builder cfb = null;
		Integer index = fileNameIndices.get(path);
		if (index == null) {
			cfb = ChangedFile.newBuilder();
			cfb.setName(path);
			cfb.setKind(FileKind.OTHER);
			cfb.setChange(changeKind);
			cfb.setKey(0);
			cfb.setAst(false);
			fileNameIndices.put(path, changedFiles.size());
			changedFiles.add(cfb);
		} else {
			System.err.println("find redundant changed file during the update proccess");
			cfb = changedFiles.get(index);
		}
		return cfb;
	}

	protected String id = null;

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	protected Person author;

	public void setAuthor(final String username, final String realname, final String email) {
		final Person.Builder person = Person.newBuilder();
		person.setUsername(username);
		if (realname != null)
			person.setRealName(realname);
		person.setEmail(email);
		author = person.build();
	}

	protected Person committer;

	public void setCommitter(final String username, final String realname, final String email) {
		final Person.Builder person = Person.newBuilder();
		person.setUsername(username);
		if (realname != null)
			person.setRealName(realname);
		person.setEmail(email);
		committer = person.build();
	}

	protected String message;

	public void setMessage(final String message) {
		this.message = message;
	}

	protected Date date;

	public void setDate(final Date date) {
		this.date = date;
	}

	protected int[] parentIndices;

	protected List<Integer> childrenIndices = new LinkedList<Integer>();

	protected static final ByteArrayOutputStream buffer = new ByteArrayOutputStream(4096);

	protected abstract String getFileContents(final String path);

	public abstract String writeFile(final String classpathRoot, final String path);

	public abstract Set<String> getGradleDependencies(final String classpathRoot, final String path);

	public abstract Set<String> getPomDependencies(String classpathroot, String name, HashSet<String> globalRepoLinks,
			HashMap<String, String> globalProperties, HashMap<String, String> globalManagedDependencies,
			Stack<PomFile> parentPomFiles);

	public Revision asProtobuf(final String projectName) {
		final Revision.Builder revision = Revision.newBuilder();
		revision.setId(id);
		this.projectName = projectName;

		if (this.author != null) {
			final Person author = Person.newBuilder(this.author).build();
			revision.setAuthor(author);
		}
		final Person committer = Person.newBuilder(this.committer).build();
		revision.setCommitter(committer);

		long time = -1;
		if (date != null)
			time = date.getTime() * 1000;
		revision.setCommitDate(time);

		if (message != null)
			revision.setLog(message);
		else
			revision.setLog("");

		if (this.parentIndices != null)
			for (int parentIndex : this.parentIndices)
				revision.addParents(parentIndex);

		for (ChangedFile.Builder cfb : changedFiles) {
			cfb.setKind(FileKind.OTHER);
			if (cfb.getChange() == ChangeKind.DELETED || cfb.getChange() == ChangeKind.UNKNOWN) {
				cfb.setKey(0);
//				cfb.setKind(connector.revisions.get(cfb.getPreviousVersions(0)).changedFiles.get(cfb.getPreviousIndices(0)).getKind());
			} else
				processChangeFile(cfb);
			revision.addFiles(cfb.build());
		}

		return revision.build();
	}

	Builder processChangeFile(final ChangedFile.Builder fb) {
		long len = connector.astWriterLen;
		String path = fb.getName();

		final String lowerPath = path.toLowerCase();
		if (lowerPath.endsWith(".txt"))
			fb.setKind(FileKind.TEXT);
		else if (lowerPath.endsWith(".xml"))
			fb.setKind(FileKind.XML);
		else if (lowerPath.endsWith(".jar") || lowerPath.endsWith(".class"))
			fb.setKind(FileKind.BINARY);
		else if (lowerPath.endsWith(".java")) {
			fb.setKind(FileKind.SOURCE_JAVA_ERROR);
			parseJavaFile(path, fb, getFileContents(path), false); // parse java file
		} else if (lowerPath.endsWith(".kt") || lowerPath.endsWith(".kts")) {
			fb.setKind(FileKind.SOURCE_KOTLIN_ERROR);
			parseKotlinFile(path, fb, getFileContents(path), false);
		} else if (lowerPath.endsWith(".js")) {
			final String content = getFileContents(path);

			fb.setKind(FileKind.SOURCE_JS_ES1);
			if (!parseJavaScriptFile(path, fb, content, Context.VERSION_1_1, false)) {
				if (debugparse)
					System.err.println("Found ES3 parse error in: revision " + id + ": file " + path);
				fb.setKind(FileKind.SOURCE_JS_ES2);
				if (!parseJavaScriptFile(path, fb, content, Context.VERSION_1_2, false)) {
					if (debugparse)
						System.err.println("Found ES3 parse error in: revision " + id + ": file " + path);
					fb.setKind(FileKind.SOURCE_JS_ES3);
					if (!parseJavaScriptFile(path, fb, content, Context.VERSION_1_3, false)) {
						if (debugparse)
							System.err.println("Found ES3 parse error in: revision " + id + ": file " + path);
						fb.setKind(FileKind.SOURCE_JS_ES5);
						if (!parseJavaScriptFile(path, fb, content, Context.VERSION_1_5, false)) {
							if (debugparse)
								System.err.println("Found ES4 parse error in: revision " + id + ": file " + path);
							fb.setKind(FileKind.SOURCE_JS_ES6);
							if (!parseJavaScriptFile(path, fb, content, Context.VERSION_1_6, false)) {
								if (debugparse)
									System.err.println("Found ES4 parse error in: revision " + id + ": file " + path);
								fb.setKind(FileKind.SOURCE_JS_ES7);
								if (!parseJavaScriptFile(path, fb, content, Context.VERSION_1_7, false)) {
									if (debugparse)
										System.err
												.println("Found ES3 parse error in: revision " + id + ": file " + path);
									fb.setKind(FileKind.SOURCE_JS_ES8);
									if (!parseJavaScriptFile(path, fb, content, Context.VERSION_1_8, false)) {
										if (debugparse)
											System.err.println(
													"Found ES4 parse error in: revision " + id + ": file " + path);
										fb.setKind(FileKind.SOURCE_JS_ERROR);
									} else if (debugparse)
										System.err.println("Accepted ES8: revision " + id + ": file " + path);
								} else if (debugparse)
									System.err.println("Accepted ES7: revision " + id + ": file " + path);
							} else if (debugparse)
								System.err.println("Accepted ES6: revision " + id + ": file " + path);
						} else if (debugparse)
							System.err.println("Accepted ES5: revision " + id + ": file " + path);
					} else if (debugparse)
						System.err.println("Accepted ES3: revision " + id + ": file " + path);
				} else if (debugparse)
					System.err.println("Accepted ES2: revision " + id + ": file " + path);
			} else if (debugparse)
				System.err.println("Accepted ES1: revision " + id + ": file " + path);
		} else if (lowerPath.endsWith(".php")) {
			final String content = getFileContents(path);

			fb.setKind(FileKind.SOURCE_PHP5);
			if (!parsePHPFile(path, fb, content, PHPVersion.PHP5, false)) {
				if (debugparse)
					System.err.println("Found ES3 parse error in: revision " + id + ": file " + path);
				fb.setKind(FileKind.SOURCE_PHP5_3);
				if (!parsePHPFile(path, fb, content, PHPVersion.PHP5_3, false)) {
					if (debugparse)
						System.err.println("Found ES3 parse error in: revision " + id + ": file " + path);
					fb.setKind(FileKind.SOURCE_PHP5_4);
					if (!parsePHPFile(path, fb, content, PHPVersion.PHP5_4, false)) {
						if (debugparse)
							System.err.println("Found ES3 parse error in: revision " + id + ": file " + path);
						fb.setKind(FileKind.SOURCE_PHP5_5);
						if (!parsePHPFile(path, fb, content, PHPVersion.PHP5_5, false)) {
							if (debugparse)
								System.err.println("Found ES4 parse error in: revision " + id + ": file " + path);
							fb.setKind(FileKind.SOURCE_PHP5_6);
							if (!parsePHPFile(path, fb, content, PHPVersion.PHP5_6, false)) {
								if (debugparse)
									System.err.println("Found ES4 parse error in: revision " + id + ": file " + path);
								fb.setKind(FileKind.SOURCE_PHP7_0);
								if (!parsePHPFile(path, fb, content, PHPVersion.PHP7_0, false)) {
									if (debugparse)
										System.err
												.println("Found ES3 parse error in: revision " + id + ": file " + path);
									fb.setKind(FileKind.SOURCE_PHP7_1);
									if (!parsePHPFile(path, fb, content, PHPVersion.PHP7_1, false)) {
										if (debugparse)
											System.err.println(
													"Found ES4 parse error in: revision " + id + ": file " + path);
										fb.setKind(FileKind.SOURCE_PHP_ERROR);
									} else if (debugparse)
										System.err.println("Accepted PHP7_1: revision " + id + ": file " + path);
								} else if (debugparse)
									System.err.println("Accepted PHP7_0: revision " + id + ": file " + path);
							} else if (debugparse)
								System.err.println("Accepted PHP5_6: revision " + id + ": file " + path);
						} else if (debugparse)
							System.err.println("Accepted PHP5_5: revision " + id + ": file " + path);
					} else if (debugparse)
						System.err.println("Accepted PHP5_4: revision " + id + ": file " + path);
				} else if (debugparse)
					System.err.println("Accepted PHP5_3: revision " + id + ": file " + path);
			} else if (debugparse)
				System.err.println("Accepted PHP5: revision " + id + ": file " + path);
		}/* else if (lowerPath.endsWith(".html") && parse) {
			final String content = getFileContents(path);

			fb.setKind(FileKind.Source_HTML);
			if (!HTMLParse(path, fb, content, false, astWriter)) {
				if (debugparse)
					System.err.println("Found an HTML parse error in : revision " + id + ": file " + path);
				fb.setKind(FileKind.SOURCE_HTML_ERROR);

			} else if (debugparse)
				System.err.println("Accepted HTML: revisison " + id + ": file " + path);
		} else if (lowerPath.endsWith(".xml") && parse) {
			final String content = getFileContents(path);

			fb.setKind(FileKind.Source_XML);
			if (!XMLParse(path, fb, content, false, astWriter)) {
				if (debugparse)
					System.err.println("Found an XML parse error in : revision " + id + ": file " + path);
				fb.setKind(FileKind.SOURCE_XML_ERROR);
			}else if (debugparse)
				System.err.println("Accepted XML: revisison " + id + ": file " + path);
		} else if (lowerPath.endsWith(".css") && parse) {
			final String content = getFileContents(path);

			fb.setKind(FileKind.Source_CSS);
			if (!CSSParse(path, fb, content, false, astWriter)) {
				if (debugparse)
					System.err.println("Found an CSS parse error in : revision " + id + ": file " + path);
				fb.setKind(FileKind.SOURCE_CSS_ERROR);
			}else if (debugparse)
				System.err.println("Accepted CSS: revisison " + id + ": file " + path);
		}*/
		/*else {
			final String content = getFileContents(path);
			if (STORE_ASCII_PRINTABLE_CONTENTS && StringUtils.isAsciiPrintable(content)) {
				try {
					fb.setKey(connector.contentWriterLen);
					BytesWritable bw = new BytesWritable(content.getBytes());
					connector.contentWriter.append(new LongWritable(connector.contentWriterLen), bw);
					connector.contentWriterLen += bw.getLength();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}*/

		if (connector.astWriterLen > len) {
			fb.setKey(len);
			fb.setAst(true);
		}

		return fb;
	}

	@SuppressWarnings("unused")
	private boolean HTMLParse(String path, Builder fb, String content, boolean b, Writer astWriter) {
		Document doc;
		HtmlVisitor visitor = new HtmlVisitor();
		final ASTRoot.Builder ast = ASTRoot.newBuilder();
		try {
			doc = Jsoup.parse(content);
		} catch (final Exception e) {
			if (debug) {
				System.err.println("Error parsing HTML file: " + path);
				e.printStackTrace();
			}
			return false;
		}
		try {
			ast.setDocument(visitor.getDocument(doc));
		} catch (final UnsupportedOperationException e) {
			return false;
		} catch (final Throwable e) {
			if (debug)
				System.err.println("Error visiting HTML file: " + path);
			e.printStackTrace();
			System.exit(-1);
			return false;
		}
		try {
			// System.out.println("writing=" + count + "\t" + path);
			BytesWritable bw = new BytesWritable(ast.build().toByteArray());
			connector.astWriter.append(new LongWritable(connector.astWriterLen), bw);
			connector.astWriterLen += bw.getLength();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	@SuppressWarnings("unused")
	private boolean XMLParse(String path, Builder fb, String content, boolean b, Writer astWriter) {
		org.dom4j.Document doc;
		XMLVisitor visitor = new XMLVisitor();
		final ASTRoot.Builder ast = ASTRoot.newBuilder();
		try {
			org.dom4j.dom.DOMDocumentFactory di = new org.dom4j.dom.DOMDocumentFactory();
			SAXReader reader = new SAXReader(di);
			doc = reader.read(content);
		} catch (final Exception e) {
			if (debug) {
				System.err.println("Error parsing HTML file: " + path);
				e.printStackTrace();
			}
			return false;
		}
		try {
			ast.setDocument(visitor.getDocument((DOMDocument) doc));
		} catch (final UnsupportedOperationException e) {
			return false;
		} catch (final Throwable e) {
			if (debug)
				System.err.println("Error visiting HTML file: " + path);
			e.printStackTrace();
			System.exit(-1);
			return false;
		}
		try {
			// System.out.println("writing=" + count + "\t" + path);
			BytesWritable bw = new BytesWritable(ast.build().toByteArray());
			connector.astWriter.append(new LongWritable(connector.astWriterLen), bw);
			connector.astWriterLen += bw.getLength();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	@SuppressWarnings("unused")
	private boolean CSSParse(String path, Builder fb, String content, boolean b, Writer astWriter) {
		com.steadystate.css.dom.CSSStyleSheetImpl sSheet = null;
		CssVisitor visitor = new CssVisitor();
		final ASTRoot.Builder ast = ASTRoot.newBuilder();
		try {
			com.steadystate.css.parser.CSSOMParser parser = new com.steadystate.css.parser.CSSOMParser();
			InputSource source = new InputSource(new StringReader(content));
			sSheet = (CSSStyleSheetImpl) parser.parseStyleSheet(source, null, null);
		} catch (final Exception e) {
			if (debug) {
				System.err.println("Error parsing HTML file: " + path);
				e.printStackTrace();
			}
			return false;
		}
		try {
			ast.setDocument(visitor.getDocument(sSheet));
		} catch (final UnsupportedOperationException e) {
			return false;
		} catch (final Throwable e) {
			if (debug)
				System.err.println("Error visiting HTML file: " + path);
			e.printStackTrace();
			System.exit(-1);
			return false;
		}
		try {
			// System.out.println("writing=" + count + "\t" + path);
			BytesWritable bw = new BytesWritable(ast.build().toByteArray());
			connector.astWriter.append(new LongWritable(connector.astWriterLen), bw);
			connector.astWriterLen += bw.getLength();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	private boolean parsePHPFile(final String path, final ChangedFile.Builder fb, final String content,
			final PHPVersion astLevel, final boolean storeOnError) {
		org.eclipse.php.internal.core.ast.nodes.ASTParser parser = org.eclipse.php.internal.core.ast.nodes.ASTParser
				.newParser(astLevel);
		Program cu = null;
		try {
			parser.setSource(content.toCharArray());
			cu = parser.createAST(null);
			if (cu == null)
				return false;
		} catch (final Exception e) {
			if (debug)
				System.err.println("Error parsing PHP file: " + path + " from: " + projectName);
			// e.printStackTrace();
			return false;
		}
		PHPErrorCheckVisitor errorCheck = new PHPErrorCheckVisitor();
		if (!errorCheck.hasError || storeOnError) {
			final ASTRoot.Builder ast = ASTRoot.newBuilder();
			PHPVisitor visitor = new PHPVisitor(content);
			try {
				ast.addNamespaces(visitor.getNamespace(cu));
			} catch (final UnsupportedOperationException e) {
				return false;
			} catch (final Throwable e) {
				if (debug)
					System.err.println("Error visiting PHP file: " + path + " from: " + projectName);
				e.printStackTrace();
				System.exit(-1);
				return false;
			}
			try {
				// System.out.println("writing=" + count + "\t" + path);
				BytesWritable bw = new BytesWritable(ast.build().toByteArray());
				connector.astWriter.append(new LongWritable(connector.astWriterLen), bw);
				connector.astWriterLen += bw.getLength();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		return !errorCheck.hasError;
	}

	private boolean parseJavaScriptFile(final String path, final ChangedFile.Builder fb, final String content,
			final int astLevel, final boolean storeOnError) {
		try {
			// System.out.println("parsing=" + (++count) + "\t" + path);
			CompilerEnvirons cp = new CompilerEnvirons();
			cp.setLanguageVersion(astLevel);
			final org.mozilla.javascript.Parser parser = new org.mozilla.javascript.Parser(cp);

			AstRoot cu;
			try {
				cu = parser.parse(content, null, 0);
			} catch (final java.lang.IllegalArgumentException ex) {
				return false;
			} catch (final org.mozilla.javascript.EvaluatorException ex) {
				return false;
			}

			final JavaScriptErrorCheckVisitor errorCheck = new JavaScriptErrorCheckVisitor();
			cu.visit(errorCheck);

			if (!errorCheck.hasError || storeOnError) {
				final ASTRoot.Builder ast = ASTRoot.newBuilder();
				// final CommentsRoot.Builder comments =
				// CommentsRoot.newBuilder();
				final JavaScriptVisitor visitor = new JavaScriptVisitor(content);
				try {
					ast.addNamespaces(visitor.getNamespaces(cu));
					// for (final String s : visitor.getImports())
					// ast.addImports(s);
					/*
					 * for (final Comment c : visitor.getComments())
					 * comments.addComments(c);
					 */
				} catch (final UnsupportedOperationException e) {
					return false;
				} catch (final Throwable e) {
					if (debug)
						System.err.println("Error visiting JS file: " + path  + " from: " + projectName);
					e.printStackTrace();
					System.exit(-1);
					return false;
				}

				try {
					// System.out.println("writing=" + count + "\t" + path);
					BytesWritable bw = new BytesWritable(ast.build().toByteArray());
					connector.astWriter.append(new LongWritable(connector.astWriterLen), bw);
					connector.astWriterLen += bw.getLength();
				} catch (final IOException e) {
					e.printStackTrace();
				}
				// fb.setComments(comments);
			}

			return !errorCheck.hasError;
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private final KotlinVisitor visitor = new KotlinVisitor();

	private boolean parseKotlinFile(final String path, final ChangedFile.Builder fb, final String content, final boolean storeOnError) {
		final KtFile theKt = KotlinLangMode.tryparse(path, content, false);

		if (theKt == null)
			return false;

		try {
			final ASTRoot.Builder ast = ASTRoot.newBuilder();

			ast.addNamespaces(visitor.getNamespace(theKt));

			switch (visitor.getAstLevel()) {
				case KotlinVisitor.KLS10:
					fb.setKind(FileKind.SOURCE_KOTLIN_1_0);
					break;
				case KotlinVisitor.KLS11:
					fb.setKind(FileKind.SOURCE_KOTLIN_1_1);
					break;
				case KotlinVisitor.KLS12:
					fb.setKind(FileKind.SOURCE_KOTLIN_1_2);
					break;
				case KotlinVisitor.KLS13:
					fb.setKind(FileKind.SOURCE_KOTLIN_1_3);
					break;
				case KotlinVisitor.KLS14:
					fb.setKind(FileKind.SOURCE_KOTLIN_1_4);
					break;
				case KotlinVisitor.KLS15:
					fb.setKind(FileKind.SOURCE_KOTLIN_1_5);
					break;
				default:
					fb.setKind(FileKind.SOURCE_KOTLIN_ERROR);
					break;
			}

			final BytesWritable bw = new BytesWritable(ast.build().toByteArray());
			connector.astWriter.append(new LongWritable(connector.astWriterLen), bw);
			connector.astWriterLen += bw.getLength();

			return true;
		} catch (final IOException e) {
			if (debug)
				e.printStackTrace();
		}

		return false;
	}

	private boolean parseJavaFile(final String path, final ChangedFile.Builder fb, final String content, final boolean storeOnError) {
		try {
			final org.eclipse.jdt.core.dom.ASTParser parser = org.eclipse.jdt.core.dom.ASTParser.newParser(AST.JLS8);
			parser.setKind(org.eclipse.jdt.core.dom.ASTParser.K_COMPILATION_UNIT);
//			parser.setResolveBindings(true);
//			parser.setUnitName(FileIO.getFileName(path));
//			parser.setEnvironment(null, null, null, true);
			parser.setSource(content.toCharArray());

			final Map<?, ?> options = JavaCore.getOptions();
			JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
			parser.setCompilerOptions(options);

			final CompilationUnit cu;

			try {
				cu = (CompilationUnit) parser.createAST(null);
			} catch (final Throwable e) {
				return false;
			}

			final JavaErrorCheckVisitor errorCheck = new JavaErrorCheckVisitor();
			cu.accept(errorCheck);

			if (!errorCheck.hasError || storeOnError) {
				final ASTRoot.Builder ast = ASTRoot.newBuilder();
				// final CommentsRoot.Builder comments = CommentsRoot.newBuilder();
				final JavaVisitor visitor = new JavaVisitor(content);
				try {

					ast.addNamespaces(visitor.getNamespaces(cu));

//					for (final Comment c : visitor.getComments())
//						comments.addComments(c);

				} catch (final Throwable e) {
					if (debug) {
						System.err.println("Error visiting Java file: " + path  + " from: " + projectName);
						e.printStackTrace();
					}
					System.exit(-1);
					return false;
				}

				switch (visitor.getAstLevel()) {
					case JavaVisitor.JLS2:
						fb.setKind(FileKind.SOURCE_JAVA_JLS2);
						break;
					case JavaVisitor.JLS3:
						fb.setKind(FileKind.SOURCE_JAVA_JLS3);
						break;
					case JavaVisitor.JLS4:
						fb.setKind(FileKind.SOURCE_JAVA_JLS4);
						break;
					case JavaVisitor.JLS8:
						fb.setKind(FileKind.SOURCE_JAVA_JLS8);
						break;
					default:
						fb.setKind(FileKind.SOURCE_JAVA_ERROR);
				}

				try {
					BytesWritable bw = new BytesWritable(ast.build().toByteArray());
					connector.astWriter.append(new LongWritable(connector.astWriterLen), bw);
					connector.astWriterLen += bw.getLength();
				} catch (final IOException e) {
					if (debug)
						e.printStackTrace();
				}
				// fb.setComments(comments);
			}

			return !errorCheck.hasError;
		} catch (final Throwable e) {
			if (debug)
				e.printStackTrace();
			return false;
		}
	}

	public Map<String, String> getLOC() {
		final Map<String, String> l = new HashMap<String, String>();

		for (final ChangedFile.Builder cf : changedFiles)
			if (cf.getChange() != ChangeKind.DELETED)
				l.put(cf.getName(), processLOC(cf.getName()));

		return l;
	}

	protected String processLOC(final String path) {
		String loc = "";

		final String lowerPath = path.toLowerCase();
		if (!(lowerPath.endsWith(".txt") || lowerPath.endsWith(".xml") || lowerPath.endsWith(".java")))
			return loc;

		final String content = getFileContents(path);

		final File dir = new File(new File(System.getProperty("java.io.tmpdir")), UUID.randomUUID().toString());
		final File tmpPath = new File(dir, path.substring(0, path.lastIndexOf("/")));
		tmpPath.mkdirs();
		final File tmpFile = new File(tmpPath, path.substring(path.lastIndexOf("/") + 1));
		FileIO.writeFileContents(tmpFile, content);

		try {
			final Process proc = Runtime.getRuntime()
					.exec(new String[] { "/home/boa/ohcount/bin/ohcount", "-i", tmpFile.getPath() });

			final BufferedReader outStream = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line = null;
			while ((line = outStream.readLine()) != null)
				loc += line;
			outStream.close();

			proc.waitFor();
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}

		try {
			FileIO.delete(dir);
		} catch (final IOException e) {
			e.printStackTrace();
		}

		return loc;
	}
}
