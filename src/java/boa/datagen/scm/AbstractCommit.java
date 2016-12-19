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
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.SequenceFile.Writer;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.wst.jsdt.core.JavaScriptCore;
import org.eclipse.wst.jsdt.core.dom.JavaScriptUnit;

import boa.types.Ast.ASTRoot;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Diff.ChangedFile.Builder;
import boa.types.Diff.ChangedFile.FileKind;
import boa.types.Shared.ChangeKind;
import boa.types.Shared.Person;
import boa.datagen.util.FileIO;
import boa.datagen.util.JavaScriptErrorCheckVisitor;
import boa.datagen.util.JavaScriptVisitor;
import boa.datagen.util.Java7Visitor;
import boa.datagen.util.Java8Visitor;
import boa.datagen.util.JavaErrorCheckVisitor;
import boa.datagen.util.Properties;

/**
 * @author rdyer
 */
public abstract class AbstractCommit {
	protected static final boolean debug = false; //util.Properties.getBoolean("debug", main.DefaultProperties.DEBUG);
	
	protected AbstractConnector connector;
	protected AbstractCommit(AbstractConnector cnn) {
		this.connector = cnn;
	}
	
	protected String id = null;
	public void setId(final String id) { this.id = id; }

	protected String author;
	public void setAuthor(final String author) { this.author = author; }

	protected String committer;
	public void setCommitter(final String committer) { this.committer = committer; }

	protected String message;
	public void setMessage(final String message) { this.message = message; }

	protected Date date;
	public void setDate(final Date date) { this.date = date; }

	private Map<String, String> changedPaths = new HashMap<String, String>();
	public void setChangedPaths(final Map<String, String> changedPaths) { this.changedPaths = changedPaths; }

	private Map<String, String> addedPaths = new HashMap<String, String>();
	public void setAddedPaths(final Map<String, String> addedPaths) { this.addedPaths = addedPaths; }

	private Map<String, String> removedPaths = new HashMap<String, String>();
	public void setRemovedPaths(final Map<String, String> removedPaths) { this.removedPaths = removedPaths; }

	protected int[] parentIndices;

	protected void setParentIndices(final int[] parentList) {
		parentIndices = parentList;
	}

	protected int[] getParentIndices() { 
		return parentIndices;
	}

	protected static final ByteArrayOutputStream buffer = new ByteArrayOutputStream(4096);

	protected abstract String getFileContents(final String path);

	protected abstract Person parsePerson(final String s);

	public Revision asProtobuf(final boolean parse, final Writer astWriter, final String revKey, final String keyDelim) {
		final Revision.Builder revision = Revision.newBuilder();
		revision.setId(id);

		final Person author = parsePerson(this.author);
		final Person committer = parsePerson(this.committer);
		revision.setAuthor(author == null ? committer : author);
		revision.setCommitter(committer);

		long time = -1;
		if (date != null)
			time = date.getTime() * 1000;
		revision.setCommitDate(time);

		if (message != null)
			revision.setLog(message);
		else
			revision.setLog("");

		for (final String path : changedPaths.keySet()) {
			final ChangedFile.Builder fb = processChangeFile(path, parse, astWriter, revKey, keyDelim);
			fb.setChange(ChangeKind.MODIFIED);
			//fb.setKey("");
			revision.addFiles(fb.build());
		}
		for (final String path : addedPaths.keySet()) {
			final ChangedFile.Builder fb = processChangeFile(path, parse, astWriter, revKey, keyDelim);
			fb.setChange(ChangeKind.ADDED);
			//fb.setKey("");
			revision.addFiles(fb.build());
		}
		for (final String path : removedPaths.keySet()) {
			final ChangedFile.Builder fb = processChangeFile(path, false, null, revKey, keyDelim);
			fb.setChange(ChangeKind.DELETED);
			//fb.setKey("");
			revision.addFiles(fb.build());
		}

		return revision.build();
	}

	private Builder processChangeFile(String path, boolean parse, Writer astWriter, String revKey, String keyDelim) {
		final ChangedFile.Builder fb = ChangedFile.newBuilder();
		fb.setName(path);
		fb.setKind(FileKind.OTHER);
		
		final String lowerPath = path.toLowerCase();
		if (lowerPath.endsWith(".txt"))
			fb.setKind(FileKind.TEXT);
		else if (lowerPath.endsWith(".xml"))
			fb.setKind(FileKind.XML);
		else if (lowerPath.endsWith(".jar") || lowerPath.endsWith(".class"))
			fb.setKind(FileKind.BINARY);
		else if (lowerPath.endsWith(".java") && parse) {
			final String content = getFileContents(path);

			fb.setKind(FileKind.SOURCE_JAVA_JLS2);
			if (!parseJavaFile(path, fb, content, JavaCore.VERSION_1_4, AST.JLS2, false, astWriter, revKey + keyDelim + path)) {
				if (debug)
					System.err.println("Found JLS2 parse error in: revision " + id + ": file " + path);

				fb.setKind(FileKind.SOURCE_JAVA_JLS3);
				if (!parseJavaFile(path, fb, content, JavaCore.VERSION_1_5, AST.JLS3, false, astWriter, revKey + keyDelim + path)) {
					if (debug)
						System.err.println("Found JLS3 parse error in: revision " + id + ": file " + path);

					fb.setKind(FileKind.SOURCE_JAVA_JLS4);
					if (!parseJavaFile(path, fb, content, JavaCore.VERSION_1_7, AST.JLS4, false, astWriter, revKey + keyDelim + path)) {
						if (debug)
							System.err.println("Found JLS4 parse error in: revision " + id + ": file " + path);

						fb.setKind(FileKind.SOURCE_JAVA_JLS8);
						if (!parseJavaFile(path, fb, content, JavaCore.VERSION_1_8, AST.JLS8, false, astWriter, revKey + keyDelim + path)) {
							if (debug)
								System.err.println("Found JLS8 parse error in: revision " + id + ": file " + path);

							fb.setKind(FileKind.SOURCE_JAVA_ERROR);
							try {
								astWriter.append(new Text(revKey + keyDelim + fb.getName()), new BytesWritable(ASTRoot.newBuilder().build().toByteArray()));
							} catch (IOException e) {
								e.printStackTrace();
							}
						} else
							if (debug)
								System.err.println("Accepted JLS8: revision " + id + ": file " + path);
					} else
						if (debug)
							System.err.println("Accepted JLS4: revision " + id + ": file " + path);
				} else
					if (debug)
						System.err.println("Accepted JLS3: revision " + id + ": file " + path);
			} else
				if (debug)
					System.err.println("Accepted JLS2: revision " + id + ": file " + path);
		}
		fb.setKey(revKey);

		return fb;
	}

	private boolean parseJavaScriptFile(final String path,
			final ChangedFile.Builder fb, final String content,
			final String compliance, final int astLevel,
			final boolean storeOnError, Writer astWriter, String key) {
		try {
			//System.out.println("parsing=" + (++count) + "\t" + path);
			final org.eclipse.wst.jsdt.core.dom.ASTParser parser = org.eclipse.wst.jsdt.core.dom.ASTParser
					.newParser(astLevel);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setResolveBindings(true);
			parser.setSource(content.toCharArray());

			final Map options = JavaCore.getOptions();
			JavaCore.setComplianceOptions(compliance, options);
			parser.setCompilerOptions(options);

			JavaScriptUnit cu;
			try{
				cu = (JavaScriptUnit) parser.createAST(null);
			}catch(java.lang.IllegalArgumentException ex){
				return false;
			}

			final JavaScriptErrorCheckVisitor errorCheck = new JavaScriptErrorCheckVisitor();
			cu.accept(errorCheck);

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
				} catch (final Exception e) {
					if (debug)
						System.err.println("Error visiting: " + path);
					//e.printStackTrace();
					return false;
				}

				if (astWriter != null) {
					try {
					//	System.out.println("writing=" + count + "\t" + path);
						astWriter.append(new Text(key), new BytesWritable(ast
								.build().toByteArray()));
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else
					fb.setAst(ast);
				// fb.setComments(comments);
			}

			return !errorCheck.hasError;
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public Revision asProtobuf(final boolean parse) {
		final Revision.Builder revision = Revision.newBuilder();
		revision.setId(id);

		final Person author = parsePerson(this.author);
		final Person committer = parsePerson(this.committer);
		revision.setAuthor(author == null ? committer : author);
		revision.setCommitter(committer);

		long time = -1;
		if (date != null)
			time = date.getTime() * 1000;
		revision.setCommitDate(time);

		if (message != null)
			revision.setLog(message);
		else
			revision.setLog("");

		for (final String path : changedPaths.keySet()) {
			final ChangedFile.Builder fb = processChangeFile(path, parse);
			fb.setChange(ChangeKind.MODIFIED);
			fb.setKey("");
			revision.addFiles(fb.build());
		}
		for (final String path : addedPaths.keySet()) {
			final ChangedFile.Builder fb = processChangeFile(path, parse);
			fb.setChange(ChangeKind.ADDED);
			fb.setKey("");
			revision.addFiles(fb.build());
		}
		for (final String path : removedPaths.keySet()) {
			final ChangedFile.Builder fb = processChangeFile(path, false);
			fb.setChange(ChangeKind.DELETED);
			fb.setKey("");
			revision.addFiles(fb.build());
		}

		return revision.build();
	}

	public Map<String,String> getLOC() {
		final Map<String,String> l = new HashMap<String,String>();

		for (final String path : changedPaths.keySet())
			l.put(path, processLOC(path));
		for (final String path : addedPaths.keySet())
			l.put(path, processLOC(path));

		return l;
	}

	protected ChangedFile.Builder processChangeFile(final String path, final boolean attemptParse) {
		final ChangedFile.Builder fb = ChangedFile.newBuilder();
		fb.setName(path);
		fb.setKind(FileKind.OTHER);
		
		final String lowerPath = path.toLowerCase();
		if (lowerPath.endsWith(".txt"))
			fb.setKind(FileKind.TEXT);
		else if (lowerPath.endsWith(".xml"))
			fb.setKind(FileKind.XML);
		else if (lowerPath.endsWith(".jar") || lowerPath.endsWith(".class"))
			fb.setKind(FileKind.BINARY);
		else if (lowerPath.endsWith(".java") && attemptParse) {
			final String content = getFileContents(path);

			fb.setKind(FileKind.SOURCE_JAVA_JLS2);
			if (!parseJavaFile(path, fb, content, JavaCore.VERSION_1_4, AST.JLS2, false, null, null)) {
				if (debug)
					System.err.println("Found JLS2 parse error in: revision " + id + ": file " + path);

				fb.setKind(FileKind.SOURCE_JAVA_JLS3);
				if (!parseJavaFile(path, fb, content, JavaCore.VERSION_1_5, AST.JLS3, false, null, null)) {
					if (debug)
						System.err.println("Found JLS3 parse error in: revision " + id + ": file " + path);

					fb.setKind(FileKind.SOURCE_JAVA_JLS4);
					if (!parseJavaFile(path, fb, content, JavaCore.VERSION_1_7, AST.JLS4, false, null, null)) {
						if (debug)
							System.err.println("Found JLS4 parse error in: revision " + id + ": file " + path);

						fb.setKind(FileKind.SOURCE_JAVA_JLS8);
						if (!parseJavaFile(path, fb, content, JavaCore.VERSION_1_8, AST.JLS8, false, null, null)) {
							if (debug)
								System.err.println("Found JLS8 parse error in: revision " + id + ": file " + path);

							//fb.setContent(content);
							fb.setKind(FileKind.SOURCE_JAVA_ERROR);
						} else
							if (debug)
								System.err.println("Accepted JLS8: revision " + id + ": file " + path);
					} else
						if (debug)
							System.err.println("Accepted JLS4: revision " + id + ": file " + path);
				} else
					if (debug)
						System.err.println("Accepted JLS3: revision " + id + ": file " + path);
			} else
				if (debug)
					System.err.println("Accepted JLS2: revision " + id + ": file " + path);
		}

		return fb;
	}

	private boolean parseJavaFile(final String path, final ChangedFile.Builder fb, final String content, final String compliance, final int astLevel, final boolean storeOnError, Writer astWriter, String key) {
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
				//final CommentsRoot.Builder comments = CommentsRoot.newBuilder();
				final Java7Visitor visitor;
				if (astLevel == AST.JLS8)
					visitor = new Java8Visitor(content, connector.nameIndices);
				else
					visitor = new Java7Visitor(content, connector.nameIndices);
				try {
					ast.addNamespaces(visitor.getNamespaces(cu));
					for (final String s : visitor.getImports())
						ast.addImports(s);
					/*for (final Comment c : visitor.getComments())
						comments.addComments(c);*/
				} catch (final UnsupportedOperationException e) {
					return false;
				} catch (final Exception e) {
					if (debug)
						System.err.println("Error visiting: " + path);
					e.printStackTrace();
					return false;
				}
				
				if (astWriter != null) {
					try {
						astWriter.append(new Text(key), new BytesWritable(ast.build().toByteArray()));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				else
					fb.setAst(ast);
				//fb.setComments(comments);
			}

			return !errorCheck.hasError;
		} catch (final Exception e) {
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
			final Process proc = Runtime.getRuntime().exec(new String[] {"/home/boa/ohcount/bin/ohcount", "-i", tmpFile.getPath()});

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
