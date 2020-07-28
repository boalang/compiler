/*
 * Copyright 2016, Hridesh Rajan, Robert Dyer, Hoan Nguyen
 *                 Iowa State University of Science and Technology
 *                 and Bowling Green State University
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

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile.Writer;
import org.dom4j.dom.DOMDocument;
import org.dom4j.io.SAXReader;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.php.internal.core.PHPVersion;
import org.eclipse.php.internal.core.ast.nodes.Program;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ast.AstRoot;
import org.w3c.css.sac.InputSource;

import com.steadystate.css.dom.CSSStyleSheetImpl;

import boa.types.Ast.ASTRoot;
import boa.types.Ast.Cell;
import boa.types.Ast.Cell.CellKind;
import boa.types.Ast.Namespace;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Diff.ChangedFile.Builder;
import boa.types.Diff.ChangedFile.FileKind;
import boa.types.Shared.ChangeKind;
import boa.types.Shared.Person;
import javafx.scene.control.CellBuilder;
import boa.datagen.DefaultProperties;
import boa.datagen.dependencies.PomFile;
import boa.datagen.util.CssVisitor;
import boa.datagen.util.FileIO;
import boa.datagen.util.HtmlVisitor;
import boa.datagen.util.JavaScriptErrorCheckVisitor;
import boa.datagen.util.JavaScriptVisitor;
import boa.datagen.util.JavaVisitor;
import boa.datagen.util.NewPythonVisitor;
import boa.datagen.util.PHPErrorCheckVisitor;
import boa.datagen.util.PHPVisitor;
import boa.datagen.util.Properties;
import boa.datagen.util.XMLVisitor;
import boa.datagen.util.JavaErrorCheckVisitor;

import org.eclipse.dltk.python.core.PythonNature;
import org.eclipse.dltk.python.internal.core.parser.PythonSourceParser;
import org.eclipse.dltk.python.parser.ast.PythonModuleDeclaration;
import org.eclipse.dltk.ast.ASTNode;
import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
import org.eclipse.dltk.ast.parser.IModuleDeclaration;
import org.eclipse.dltk.ast.parser.ISourceParser;
import org.eclipse.dltk.ast.parser.SourceParserManager;
import org.eclipse.dltk.compiler.IElementRequestor;
import org.eclipse.dltk.compiler.env.IModuleSource;
import org.eclipse.dltk.compiler.env.ModuleSource;
import org.eclipse.dltk.compiler.problem.IProblemReporter;
import org.eclipse.dltk.compiler.problem.AbstractProblemReporter;
import org.eclipse.dltk.compiler.problem.IProblem;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang.exception.ExceptionUtils;

/**
 * @author rdyer
 */
public abstract class AbstractCommit {
	protected static final boolean debug = Properties.getBoolean("debug", DefaultProperties.DEBUG);
	protected static final boolean debugparse = Properties.getBoolean("debugparse", DefaultProperties.DEBUGPARSE);
	protected static final boolean STORE_ASCII_PRINTABLE_CONTENTS = Properties.getBoolean("ascii",
			DefaultProperties.STORE_ASCII_PRINTABLE_CONTENTS);

	static Map<String, ASTNode> previousAst = new HashMap<>();
	boolean lastRevision = false;

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
				System.out.println("UNKNOWN");
				cfb.setKey(0);
//				cfb.setKind(connector.revisions.get(cfb.getPreviousVersions(0)).changedFiles.get(cfb.getPreviousIndices(0)).getKind());
			} else
				processPythonChangeFile(cfb); // Only process the Python files and ignore other files e.g., .java, .js
				//processChangeFile(cfb);
			revision.addFiles(cfb.build());
		}

		return revision.build();
	}

	String[] badpaths = { "spacy/lang/ca/lemmatizer.py", "spacy/lang/da/lemmatizer.py", "spacy/lang/de/lemmatizer.py",
			"spacy/lang/es/lemmatizer.py", "spacy/lang/fr/lemmatizer/lookup.py", "spacy/lang/hu/lemmatizer.py",
			"spacy/lang/id/lemmatizer.py", "spacy/lang/it/lemmatizer.py", "spacy/lang/pt/lemmatizer.py",
			"spacy/lang/ro/lemmatizer.py", "spacy/lang/sv/lemmatizer/lookup.py", "spacy/lang/tr/lemmatizer.py",
			"spacy/lang/ur/lemmatizer.py" };
	Set<String> badp = new HashSet<String>(Arrays.asList(badpaths));
	boolean include_notebooks = false;

	Builder processPythonChangeFile(final ChangedFile.Builder fb) {
		long len = connector.astWriterLen;
		String path = fb.getName();

		final String lowerPath = path.toLowerCase();
		if (lowerPath.endsWith(".txt"))
			fb.setKind(FileKind.TEXT);
		else if (lowerPath.endsWith(".xml"))
			fb.setKind(FileKind.XML);
		else if (lowerPath.endsWith(".jar") || lowerPath.endsWith(".class"))
			fb.setKind(FileKind.BINARY);
		////// Python AST generation will be handled here ///////
		else if (lowerPath.endsWith(".py")) {
			if (badp.contains(lowerPath)) {
				fb.setKind(FileKind.SOURCE_PY_ERROR);
			} else {
				final String content = getFileContents(path);
				fb.setKind(FileKind.SOURCE_PY_ERROR);
				System.out.println(projectName + ": " + path);
				parsePythonFile(path, fb, content, false);
			}
		}
		else if (lowerPath.endsWith(".ipynb")) {
			if(include_notebooks)
				if(!path.startsWith(".ipynb_checkpoints/") && !path.contains("/.ipynb_checkpoints/")) {
					final String content = getFileContents(path);
					fb.setKind(FileKind.SOURCE_PY_ERROR);
					System.out.println(projectName + ": " + path);
					parseNotebookFile(path, fb, content, false);
				}
		}

		if (connector.astWriterLen > len) {
			fb.setKey(len);
			fb.setAst(true);
		}

		return fb;
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
			final String content = getFileContents(path);
			fb.setKind(FileKind.SOURCE_JAVA_ERROR);
			parseJavaFile(path, fb, content, false); // parse java file
		} else if (lowerPath.endsWith(".py")) {
			if (badp.contains(lowerPath)) {
				fb.setKind(FileKind.SOURCE_PY_ERROR);
			} else {
				final String content = getFileContents(path);
				fb.setKind(FileKind.SOURCE_PY_ERROR);
				// System.out.println(projectName + ": " + path);
				parsePythonFile(path, fb, content, false);
			}
		} else if (lowerPath.endsWith(".ipynb")) {
			final String content = getFileContents(path);
			fb.setKind(FileKind.SOURCE_PY_ERROR);
			System.out.println(projectName + ": " + path);
			parseNotebookFile(path, fb, content, false);
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
		} /*
			 * else if (lowerPath.endsWith(".html") && parse) { final String content =
			 * getFileContents(path); fb.setKind(FileKind.Source_HTML); if (!HTMLParse(path,
			 * fb, content, false, astWriter)) { if (debugparse)
			 * System.err.println("Found an HTML parse error in : revision " + id +
			 * ": file " + path); fb.setKind(FileKind.SOURCE_HTML_ERROR); } else if
			 * (debugparse) System.err.println("Accepted HTML: revisison " + id + ": file "
			 * + path); } else if (lowerPath.endsWith(".xml") && parse) { final String
			 * content = getFileContents(path); fb.setKind(FileKind.Source_XML); if
			 * (!XMLParse(path, fb, content, false, astWriter)) { if (debugparse)
			 * System.err.println("Found an XML parse error in : revision " + id + ": file "
			 * + path); fb.setKind(FileKind.SOURCE_XML_ERROR); }else if (debugparse)
			 * System.err.println("Accepted XML: revisison " + id + ": file " + path); }
			 * else if (lowerPath.endsWith(".css") && parse) { final String content =
			 * getFileContents(path); fb.setKind(FileKind.Source_CSS); if (!CSSParse(path,
			 * fb, content, false, astWriter)) { if (debugparse)
			 * System.err.println("Found an CSS parse error in : revision " + id + ": file "
			 * + path); fb.setKind(FileKind.SOURCE_CSS_ERROR); }else if (debugparse)
			 * System.err.println("Accepted CSS: revisison " + id + ": file " + path); }
			 */
		/*
		 * else { final String content = getFileContents(path); if
		 * (STORE_ASCII_PRINTABLE_CONTENTS && StringUtils.isAsciiPrintable(content)) {
		 * try { fb.setKey(connector.contentWriterLen); BytesWritable bw = new
		 * BytesWritable(content.getBytes()); connector.contentWriter.append(new
		 * LongWritable(connector.contentWriterLen), bw); connector.contentWriterLen +=
		 * bw.getLength(); } catch (IOException e) { e.printStackTrace(); } } }
		 */

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
		} catch (Exception e) {
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
		} catch (IOException e) {
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
		} catch (Exception e) {
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
		} catch (IOException e) {
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
		} catch (Exception e) {
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
		} catch (IOException e) {
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
		} catch (Exception e) {
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
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return !errorCheck.hasError;
	}

	boolean pythonParsingError;

	private boolean parsePythonFile(final String path, final ChangedFile.Builder fb, final String content,
			final boolean storeOnError) {
		pythonParsingError = false;

		String fullPath = this.projectName + "/" + path;

//		if(this.lastRevision)
//			System.out.println("commit " + this.id);
		
		PythonSourceParser parser = new PythonSourceParser();
		IModuleSource input = new ModuleSource(content);

		IProblemReporter reporter = new IProblemReporter() {
			@Override
			public void reportProblem(IProblem arg0) {
				pythonParsingError = true;

			}
		};

		// System.out.println("actual source: " + content);
		PythonModuleDeclaration module;

		try {
			module = (PythonModuleDeclaration) parser.parse(input, reporter);

			if (previousAst.containsKey(fullPath)) {
				boa.datagen.treed.python.TreedMapper tm = new boa.datagen.treed.python.TreedMapper(
						previousAst.get(fullPath), module);

				try {
					tm.map();
				} catch (Exception e) {
					if (debug) {
						System.err.println("Tree mapping error" + path + " from: " + projectName + "\n");
						writeToCsv(projectName, path, ExceptionUtils.getStackTrace(e).replace("\n", " ## "));
					}
					e.printStackTrace();
				}
			}

			if (!this.lastRevision)
				previousAst.put(fullPath, module);

		} catch (Exception e) {
			if (debug) {
				System.err.println("Error visiting Python file: " + path + " from: " + projectName + "\n");
				writeToCsv(projectName, path, ExceptionUtils.getStackTrace(e).replace("\n", " ## "));
			}
			e.printStackTrace();
			return false;
		}

		if (true) {
			final ASTRoot.Builder ast = ASTRoot.newBuilder();
			NewPythonVisitor visitor = new NewPythonVisitor();
			visitor.enableDiff = true;

			try {
				ast.addNamespaces(visitor.getNamespace(module, path));
			} catch (final UnsupportedOperationException e) {
				if (debug) {
					System.err.println("Unsupported operation Error visiting Python file: " + path + " from: " + projectName + "\n");
					writeToCsv(projectName, path, ExceptionUtils.getStackTrace(e).replace("\n", " ## "));
				}
				return false;
			} catch (final Throwable e) {
				if (debug) {
					System.err.println("Error visiting Python file: " + path + " from: " + projectName + "\n");
					writeToCsv(projectName, path, ExceptionUtils.getStackTrace(e).replace("\n", " ## "));
				}
				e.printStackTrace();
				return false;
			}

			if (!pythonParsingError)
				fb.setKind(FileKind.SOURCE_PY_3);

			try {
				BytesWritable bw = new BytesWritable(ast.build().toByteArray());
				connector.astWriter.append(new LongWritable(connector.astWriterLen), bw);
				connector.astWriterLen += bw.getLength();
			} catch (IOException e) {
				if (debug) {
					System.err.println("AST writing error: " + path + " from: " + projectName + "\n");
					writeToCsv(projectName, path, ExceptionUtils.getStackTrace(e).replace("\n", " ## "));
				}
				e.printStackTrace();
			}

		}
		return !pythonParsingError;
	}

	boolean cellParseError;
	boolean notebookParseError;
	
	private boolean parseNotebookFile(final String path, final ChangedFile.Builder fb, final String content,
			final boolean storeOnError) {
		System.out.println("commit " + this.id);
		// System.out.println("@@@@@@@@ " + path);
		notebookParseError = false;
		cellParseError = false;

		int cellCount = 0;
		final ASTRoot.Builder ast = ASTRoot.newBuilder();
		JsonArray cells = parseNotebookJson(content);

		if (cells == null) {
			if (debug) {
				System.err.println("Notebook JSON parse error: " + path + " from: " + projectName + "\n");
			}
			notebookParseError = true;
			return false;
		}

		for (JsonElement cell : cells) {
			cellParseError = false;
			JsonObject c = cell.getAsJsonObject();
			if (!c.get("cell_type").getAsString().equals("code") || !c.has("source"))
				continue;

			cellCount += 1;
			int exec_count; // Execution count = -1 if its null
			try {
				exec_count = c.get("execution_count").getAsInt();
			} catch (Exception e) {
				exec_count = -1;
			}

			JsonArray lines = c.getAsJsonArray("source");
			Iterator<JsonElement> iterator = lines.iterator();

			String codeCell = "";
			
			while (iterator.hasNext()) {
				String line = "";
				try {
					line = iterator.next().getAsString();
				} catch (Exception e) {
					e.printStackTrace();
					line = "";
					if (debug) {
						System.err.println("Error parsing one line in a cell: " + path + ", Cell:" + cellCount + " from: " + projectName + "\n");
						writeToCsv(projectName, path + ", Cell:" + cellCount, ExceptionUtils.getStackTrace(e).replace("\n", " ## "));
					}
				}
				if (NoNotebookErrors(line))
					codeCell += line;
				else
					codeCell += "#" + line;
			}
			
			Cell.Builder cb = Cell.newBuilder();
			cb.setCellKind(CellKind.CODE);
			cb.setCellId(cellCount);
			cb.setExecutionCount(exec_count);

			////////// Parse code here ////////////////////

			PythonSourceParser parser = new PythonSourceParser();
			IModuleSource input = new ModuleSource(codeCell);

			IProblemReporter reporter = new IProblemReporter() {
				@Override
				public void reportProblem(IProblem arg0) {
					cellParseError = true;
				}
			};

			// System.out.println("actual source: " + codeCell);
			PythonModuleDeclaration module = null;

			try {
				module = (PythonModuleDeclaration) parser.parse(input, reporter);
			} catch (Exception e) {
				if (debug) {
					System.err.println("Error parsing notebook cell: " + path + " from: " + projectName + "\n");
					writeToCsv(projectName, path + ", Cell:" + cellCount, ExceptionUtils.getStackTrace(e).replace("\n", " ## "));
				}
				e.printStackTrace();
				cellParseError = true;
				cb.setParseError(cellParseError);
				ast.addCells(cb.build());
				continue;
			}
			
			NewPythonVisitor visitor = new NewPythonVisitor();
			visitor.enableDiff = false;
			
			try {
				cb.addNamespaces(visitor.getCellAsNamespace(module, "code_cell:" + cellCount));
			} catch (final UnsupportedOperationException e) {
				if (debug) {
					System.err.println("Error getting AST for a notebook cell: " + path + ", Cell:" + cellCount + " from: " + projectName + "\n");
					writeToCsv(projectName, path + ", Cell:" + cellCount, ExceptionUtils.getStackTrace(e).replace("\n", " ## "));
				}
				e.printStackTrace();
				cellParseError = true;
			} catch (final Throwable e) {
				if (debug) {
					System.err.println("Error getting AST for a notebook cell: " + path + ", Cell:" + cellCount + " from: " + projectName + "\n");
					writeToCsv(projectName, path + ", Cell:" + cellCount, ExceptionUtils.getStackTrace(e).replace("\n", " ## "));
				}
				e.printStackTrace();
				cellParseError = true;
			}
			
			cb.setParseError(cellParseError);
			ast.addCells(cb.build());
			///////// Parsing each cell ends ////////////////////
		}
		////////// Loop ends for all cells

		if(cellCount > 0)
			fb.setKind(FileKind.SOURCE_PY_3);
		
		try {
			BytesWritable bw = new BytesWritable(ast.build().toByteArray());
			connector.astWriter.append(new LongWritable(connector.astWriterLen), bw);
			connector.astWriterLen += bw.getLength();
		} catch (IOException e) {
			if (debug) {
				System.err.println("Error writing AST of a notebook file: " + path + ", Cell:" + cellCount + " from: " + projectName + "\n");
				writeToCsv(projectName, path + ", Cell:" + cellCount, ExceptionUtils.getStackTrace(e).replace("\n", " ## "));
			}
			e.printStackTrace();
		}

		return !notebookParseError;
	}

	private JsonArray parseNotebookJson(final String content) {
		int nbformat = -1;
		JsonObject jobject = null;
		JsonArray jarray = null;
		try {
			JsonElement jelement = new JsonParser().parse(content);
			jobject = jelement.getAsJsonObject();
			nbformat = jobject.get("nbformat").getAsInt();
		} catch (Exception e) {
			System.out.println("Notebook JSON does not follow nbformat library requirements.");
			return null;
		}
		if(nbformat >= 4) {
			try {
				jarray = jobject.getAsJsonArray("cells");
			} catch (Exception e) {
				System.out.println("Notebook JSON cells are not present.");
			}
		}
		else {
			System.out.println("Notebook JSON does not follow nbformat library version requirement.");
		}
		return jarray;
	}

	boolean NoNotebookErrors(String line) {
		if (line.startsWith("%") ||
			line.startsWith("!") ||
			line.startsWith("?") ||
			line.endsWith("?"))
			return false;
		return true;
	}

	private void writeToCsv(String project, String file, String error) {
		String file_name = "error-log.csv";
		try {
			File f = new File(file_name);
			if (!f.exists()) {
				f.createNewFile();
				FileWriter fw = new FileWriter(f, true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter pw = new PrintWriter(bw);
				pw.println("Project" + ", " + "File" + ", " + "Error");
				pw.println(project + ", " + file + ", " + error);
				pw.close();
			} else {
				FileWriter fw = new FileWriter(f, true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter pw = new PrintWriter(bw);
				pw.println(project + ", " + file + ", " + error);
				pw.close();
			}
		} catch (IOException ioe) {
			System.out.println("Exception occurred:");
			ioe.printStackTrace();
		}
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
			} catch (java.lang.IllegalArgumentException ex) {
				return false;
			} catch (org.mozilla.javascript.EvaluatorException ex) {
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
					 * for (final Comment c : visitor.getComments()) comments.addComments(c);
					 */
				} catch (final UnsupportedOperationException e) {
					return false;
				} catch (final Throwable e) {
					if (debug)
						System.err.println("Error visiting JS file: " + path + " from: " + projectName);
					e.printStackTrace();
					System.exit(-1);
					return false;
				}
				try {
					// System.out.println("writing=" + count + "\t" + path);
					BytesWritable bw = new BytesWritable(ast.build().toByteArray());
					connector.astWriter.append(new LongWritable(connector.astWriterLen), bw);
					connector.astWriterLen += bw.getLength();
				} catch (IOException e) {
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

	public Map<String, String> getLOC() {
		final Map<String, String> l = new HashMap<String, String>();

		for (final ChangedFile.Builder cf : changedFiles)
			if (cf.getChange() != ChangeKind.DELETED)
				l.put(cf.getName(), processLOC(cf.getName()));

		return l;
	}

	private boolean parseJavaFile(final String path, final ChangedFile.Builder fb, final String content,
			final boolean storeOnError) {
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
			} catch (Throwable e) {
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

//					for (final Comment c : visitor.getComments()) comments.addComments(c);

				} catch (final Throwable e) {
					if (debug) {
						System.err.println("Error visiting Java file: " + path + " from: " + projectName);
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
				} catch (IOException e) {
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

	/*
	 * This is the old version of parsePython, will be deleted when the new one is
	 * fully functional. private boolean parsePythonFile(final String path, final
	 * ChangedFile.Builder fb, final String content, final boolean storeOnError) {
	 * final ASTRoot.Builder ast = ASTRoot.newBuilder(); try { Python3Visitor
	 * visitor = new Python3Visitor(); fb.setKind(FileKind.SOURCE_PY_3);
	 * visitor.visit(path, content); if(!visitor.isPython3)
	 * fb.setKind(FileKind.SOURCE_PY_2); ast.addNamespaces(visitor.getNamespaces());
	 * 
	 * } catch (Exception e1) { e1.printStackTrace();
	 * System.out.println("Error in Python parse. " + e1.getMessage()); } try {
	 * BytesWritable bw = new BytesWritable(ast.build().toByteArray());
	 * connector.astWriter.append(new LongWritable(connector.astWriterLen), bw);
	 * connector.astWriterLen += bw.getLength(); } catch (IOException e) {
	 * e.printStackTrace(); } return false; }
	 */
}
