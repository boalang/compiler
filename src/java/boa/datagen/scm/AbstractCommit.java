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
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ast.AstRoot;

import boa.types.Ast.ASTRoot;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Diff.ChangedFile.Builder;
import boa.types.Diff.ChangedFile.FileKind;
import boa.types.Shared.ChangeKind;
import boa.types.Shared.Person;
import boa.datagen.DefaultProperties;
import boa.datagen.util.FileIO;
import boa.datagen.util.JavaScriptErrorCheckVisitor;
import boa.datagen.util.JavaScriptVisitor;
import boa.datagen.util.Properties;
import boa.datagen.util.Java7Visitor;
import boa.datagen.util.Java8Visitor;
import boa.datagen.util.JavaErrorCheckVisitor;

/**
 * @author rdyer
 */
public abstract class AbstractCommit {
	protected static final boolean debug = Properties.getBoolean("debug", DefaultProperties.DEBUG);
	protected static final boolean debugparse = Properties.getBoolean("debugparse", DefaultProperties.DEBUGPARSE);
	
	protected AbstractConnector connector;
	protected AbstractCommit(AbstractConnector cnn) {
		this.connector = cnn;
	}

	protected Map<String, Integer> fileNameIndices = new HashMap<String, Integer>();
	
	protected List<ChangedFile.Builder> changedFiles = new ArrayList<ChangedFile.Builder>();

	protected ChangedFile.Builder getChangeFile(String path) {
		ChangedFile.Builder cfb = null;
		Integer index = fileNameIndices.get(path);
		if (index == null) {
			cfb = ChangedFile.newBuilder();
			cfb.setKind(FileKind.OTHER);
			cfb.setKey(-1);
			fileNameIndices.put(path, changedFiles.size());
			changedFiles.add(cfb);
		} else
			cfb = changedFiles.get(index);
		return cfb;
	}
	
	protected String id = null;
	public String getId() { return id; }
	public void setId(final String id) { this.id = id; }

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
	public void setMessage(final String message) { this.message = message; }

	protected Date date;
	public void setDate(final Date date) { this.date = date; }
	
	protected int[] parentIndices;

	protected List<Integer> childrenIndices = new LinkedList<Integer>();
	
	protected static final ByteArrayOutputStream buffer = new ByteArrayOutputStream(4096);

	protected abstract String getFileContents(final String path);

	public Revision asProtobuf(final boolean parse, final Writer astWriter) {
		final Revision.Builder revision = Revision.newBuilder();
		revision.setId(id);

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
		
		for (ChangedFile.Builder cfb : changedFiles) {
			if (cfb.getChange() == ChangeKind.DELETED || cfb.getChange() == ChangeKind.UNKNOWN) {
				cfb.setKey(-1);
				cfb.setKind(connector.revisions.get(cfb.getPreviousVersions(0)).changedFiles.get(cfb.getPreviousIndices(0)).getKind());
			} else
				processChangeFile(cfb, parse, astWriter);
			revision.addFiles(cfb.build());
		}

		return revision.build();
	}

	@SuppressWarnings("deprecation")
	private Builder processChangeFile(final ChangedFile.Builder fb, boolean parse, Writer astWriter) {
		long len = -1;
		try {
			len = astWriter.getLength();
		} catch (IOException e1) {
			if (debug)
				System.err.println("Error getting length of sequence file writer!!!");
		}
		String path = fb.getName();
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
			if (!parseJavaFile(path, fb, content, JavaCore.VERSION_1_4, AST.JLS2, false, astWriter)) {
				if (debugparse)
					System.err.println("Found JLS2 parse error in: revision " + id + ": file " + path);

				fb.setKind(FileKind.SOURCE_JAVA_JLS3);
				if (!parseJavaFile(path, fb, content, JavaCore.VERSION_1_5, AST.JLS3, false, astWriter)) {
					if (debugparse)
						System.err.println("Found JLS3 parse error in: revision " + id + ": file " + path);

					fb.setKind(FileKind.SOURCE_JAVA_JLS4);
					if (!parseJavaFile(path, fb, content, JavaCore.VERSION_1_7, AST.JLS4, false, astWriter)) {
						if (debugparse)
							System.err.println("Found JLS4 parse error in: revision " + id + ": file " + path);

						fb.setKind(FileKind.SOURCE_JAVA_JLS8);
						if (!parseJavaFile(path, fb, content, JavaCore.VERSION_1_8, AST.JLS8, false, astWriter)) {
							if (debugparse)
								System.err.println("Found JLS8 parse error in: revision " + id + ": file " + path);

							fb.setKind(FileKind.SOURCE_JAVA_ERROR);
//							try {
//								astWriter.append(new LongWritable(len), new BytesWritable(ASTRoot.newBuilder().build().toByteArray()));
//							} catch (IOException e) {
//								e.printStackTrace();
//							}
						} else if (debugparse)
							System.err.println("Accepted JLS8: revision " + id + ": file " + path);
					} else if (debugparse)
						System.err.println("Accepted JLS4: revision " + id + ": file " + path);
				} else if (debugparse)
					System.err.println("Accepted JLS3: revision " + id + ": file " + path);
			} else if (debugparse)
				System.err.println("Accepted JLS2: revision " + id + ": file " + path);
		} else if (lowerPath.endsWith(".js") && parse) {
			final String content = getFileContents(path);

			fb.setKind(FileKind.SOURCE_JS_ES1);
			if (!parseJavaScriptFile(path, fb, content, Context.VERSION_1_1, false, astWriter)) {
				if (debugparse)
					System.err.println("Found ES3 parse error in: revision " + id + ": file " + path);
				fb.setKind(FileKind.SOURCE_JS_ES2);
				if (!parseJavaScriptFile(path, fb, content, Context.VERSION_1_2, false, astWriter)) {
					if (debugparse)
						System.err.println("Found ES3 parse error in: revision " + id + ": file " + path);
					fb.setKind(FileKind.SOURCE_JS_ES3);
					if (!parseJavaScriptFile(path, fb, content, Context.VERSION_1_3, false, astWriter)) {
						if (debugparse)
							System.err.println("Found ES3 parse error in: revision " + id + ": file " + path);
						fb.setKind(FileKind.SOURCE_JS_ES5);
						if (!parseJavaScriptFile(path, fb, content, Context.VERSION_1_5, false, astWriter)) {
							if (debugparse)
								System.err.println("Found ES4 parse error in: revision " + id + ": file " + path);
							fb.setKind(FileKind.SOURCE_JS_ES6);
							if (!parseJavaScriptFile(path, fb, content, Context.VERSION_1_6, false, astWriter)) {
								if (debugparse)
									System.err.println("Found ES4 parse error in: revision " + id + ": file " + path);
								fb.setKind(FileKind.SOURCE_JS_ES7);
								if (!parseJavaScriptFile(path, fb, content, Context.VERSION_1_7, false, astWriter)) {
									if (debugparse)
										System.err
												.println("Found ES3 parse error in: revision " + id + ": file " + path);
									fb.setKind(FileKind.SOURCE_JS_ES8);
									if (!parseJavaScriptFile(path, fb, content, Context.VERSION_1_8, false, astWriter)) {
										if (debugparse)
											System.err.println(
													"Found ES4 parse error in: revision " + id + ": file " + path);
										fb.setKind(FileKind.SOURCE_JS_ERROR);
//										try {
//											astWriter.append(new LongWritable(len), new BytesWritable(ASTRoot.newBuilder().build().toByteArray()));
//										} catch (IOException e) {
//											e.printStackTrace();
//										}
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
		}
		try {
			if (astWriter.getLength() > len)
				fb.setKey(len);
			else
				fb.setKey(-1);
		} catch (IOException e) {
			if (debug)
				System.err.println("Error getting length of sequence file writer!!!");
		}

		return fb;
	}

	private boolean parseJavaScriptFile(final String path,
			final ChangedFile.Builder fb, final String content, final int astLevel,
			final boolean storeOnError, Writer astWriter) {
		try {
			//System.out.println("parsing=" + (++count) + "\t" + path);
			CompilerEnvirons cp = new CompilerEnvirons();
			cp.setLanguageVersion(astLevel);
			final org.mozilla.javascript.Parser parser = new org.mozilla.javascript.Parser(cp);

			AstRoot cu;
			try{
				cu =  parser.parse(content, null, 0);
			}catch(java.lang.IllegalArgumentException ex){
				return false;
			}catch(org.mozilla.javascript.EvaluatorException ex){
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
				} catch (final Exception e) {
					if (debug)
						System.err.println("Error visiting: " + path);
					//e.printStackTrace();
					return false;
				}

				try {
				//	System.out.println("writing=" + count + "\t" + path);
					astWriter.append(new LongWritable(astWriter.getLength()), new BytesWritable(ast.build().toByteArray()));
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

	public Map<String,String> getLOC() {
		final Map<String,String> l = new HashMap<String,String>();
		
		for (final ChangedFile.Builder cf : changedFiles)
			if (cf.getChange() != ChangeKind.DELETED)
				l.put(cf.getName(), processLOC(cf.getName()));

		return l;
	}

	private boolean parseJavaFile(final String path, final ChangedFile.Builder fb, final String content, final String compliance, final int astLevel, final boolean storeOnError, Writer astWriter) {
		try {
			final ASTParser parser = ASTParser.newParser(astLevel);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setResolveBindings(true);
			parser.setSource(content.toCharArray());

			final Map<?, ?> options = JavaCore.getOptions();
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
					visitor = new Java8Visitor(content);
				else
					visitor = new Java7Visitor(content);
				try {
					ast.addNamespaces(visitor.getNamespaces(cu));
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
				
				try {
					astWriter.append(new LongWritable(astWriter.getLength()), new BytesWritable(ast.build().toByteArray()));
				} catch (IOException e) {
					e.printStackTrace();
				}
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

	protected List<int[]> getPreviousFiles(String parentName, String path) {
		int commitId = connector.revisionMap.get(parentName);
		Set<Integer> queuedCommitIds = new HashSet<Integer>();
		List<int[]> l = new ArrayList<int[]>();
		PriorityQueue<Integer> pq = new PriorityQueue<Integer>(100, new Comparator<Integer>() {
			@Override
			public int compare(Integer i1, Integer i2) {
				return i2 - i1;
			}
		});
		pq.offer(commitId);
		queuedCommitIds.add(commitId);
		while (!pq.isEmpty()) {
			commitId = pq.poll();
			AbstractCommit commit = connector.revisions.get(commitId);
			boolean found = false;
			for (int i = 0; i < commit.changedFiles.size(); i++) {
				ChangedFile.Builder cfb = commit.changedFiles.get(i);
				if (cfb.getName().equals(path) && cfb.getChange() != ChangeKind.DELETED) {
					l.add(new int[]{i, commitId});
					found = true;
					break;
				}
			}
			if (!found && commit.parentIndices != null) {
				for (int parentId : commit.parentIndices) {
					if (!queuedCommitIds.contains(parentId)) {
						pq.offer(parentId);
						queuedCommitIds.add(parentId);
					}
				}
			}
		}
		return l;
	}

}
