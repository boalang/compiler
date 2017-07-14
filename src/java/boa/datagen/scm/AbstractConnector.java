/*
 * Copyright 2015, Hridesh Rajan, Robert Dyer, Hoan Nguyen
 *                 and Iowa State University of Science and Technology
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Writer;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;

import boa.datagen.util.Java8Visitor;
import boa.types.Ast.ASTRoot;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.ChangeKind;

/**
 * @author rdyer
 */
public abstract class AbstractConnector implements AutoCloseable {
	protected static final boolean debug = boa.datagen.util.Properties.getBoolean("debug", boa.datagen.DefaultProperties.DEBUG);
	
	protected List<AbstractCommit> revisions = null;
	protected List<String> branchNames = new ArrayList<String>(), tagNames = new ArrayList<String>();
	protected List<Integer> branchIndices = new ArrayList<Integer>(), tagIndices = new ArrayList<Integer>();
	protected HashMap<String, Integer> nameIndices = new HashMap<String, Integer>();
	protected Map<String, Integer> revisionMap = new HashMap<String, Integer>();
	protected int headCommitOffset = -1;

	public int getHeadCommitOffset() {
		return this.headCommitOffset;
	}

	public List<ChangedFile> buildHeadSnapshot(final String[] languages, final SequenceFile.Writer astWriter) {
		return buildSnapshot(headCommitOffset, languages, astWriter);
	}
	
	public List<ChangedFile> buildSnapshot(final int commitOffset, final String[] languages, final SequenceFile.Writer astWriter) {
		final List<ChangedFile> snapshot = new ArrayList<ChangedFile>();
		final Map<String, AbstractCommit> commits = new HashMap<String, AbstractCommit>();
		getSnapshot(commitOffset, snapshot, commits);
		
		if (languages == null)
			return snapshot;
		
		boolean hasJava = false;
		for (String lang : languages)
			if (lang.toLowerCase().equals("java")) {
				hasJava = true;
				break;
			}
		
		if (hasJava) {
			final Map<String, ChangedFile> changedFiles = new HashMap<String, ChangedFile>();
			final Map<String, String> fileContents = new HashMap<String, String>();
			int i = 0;
			while (i < snapshot.size()) {
				ChangedFile cf = snapshot.get(i);
				if (cf.getName().endsWith(".java") && cf.getKind() != null && cf.getKind().name().startsWith("SOURCE_JAVA_JLS")) {
					String path = cf.getName();
					fileContents.put(path, commits.get(path).getFileContents(path));
					changedFiles.put(path, cf);
					snapshot.remove(i);
				} else
					i++;
			}
			final String[] paths = changedFiles.keySet().toArray(new String[0]);
			final String[] classpaths = null; // TODO
			final FileASTRequestor r = new FileASTRequestor() {
				@Override
				public void acceptAST(String sourceFilePath, CompilationUnit cu) {
					sourceFilePath = sourceFilePath.replace('\\', '/');
					ChangedFile cf = changedFiles.get(sourceFilePath);
					ChangedFile.Builder fb = ChangedFile.newBuilder(cf);
					long len = -1;
					if (astWriter != null) {
						try {
							len = astWriter.getLength();
						} catch (IOException e1) {}
					}
					
					String content = fileContents.get(sourceFilePath);
					final Java8Visitor visitor = new Java8Visitor(content, new HashMap<String, Integer>());
					final ASTRoot.Builder ast = ASTRoot.newBuilder();
					try {
						ast.addNamespaces(visitor.getNamespaces(cu));
						for (final String s : visitor.getImports())
							ast.addImports(s);
						/*for (final Comment c : visitor.getComments())
							comments.addComments(c);*/
					} catch (final UnsupportedOperationException e) {
						return;
					} catch (final Exception e) {
						System.err.println("Error visiting " + sourceFilePath + " when parsing head snapshot!!!");
						e.printStackTrace();
						return;
					}
					if (astWriter != null) {
						try {
							astWriter.append(new LongWritable(astWriter.getLength()), new BytesWritable(ast.build().toByteArray()));
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else
						fb.setAst(ast);
					
					try {
						if (astWriter != null && astWriter.getLength() == len + 1)
							fb.setKey(len);
						else
							fb.setKey(-1);
					} catch (IOException e) {}
					snapshot.add(fb.build());
				}
			};
			@SuppressWarnings("rawtypes")
			Map options = JavaCore.getOptions();
			options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
			options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
			options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
			ASTParser parser = ASTParser.newParser(AST.JLS8);
			parser.setCompilerOptions(options);
			parser.setEnvironment(
					classpaths == null ? new String[0] : classpaths,
					new String[]{}, 
					new String[]{}, 
					true);
			parser.setResolveBindings(true);
	//		parser.setBindingsRecovery(true);
			parser.createASTs(fileContents, paths, null, new String[0], r, null);
		}
		return snapshot;
	}

	public void getSnapshot(int commitOffset, List<ChangedFile> snapshot, Map<String, AbstractCommit> commits) {
		Set<String> adds = new HashSet<String>(), dels = new HashSet<String>(); 
		PriorityQueue<Integer> pq = new PriorityQueue<Integer>(100, new Comparator<Integer>() {
			@Override
			public int compare(Integer i1, Integer i2) {
				return i2 - i1;
			}
		});
		Set<Integer> queuedCommitIds = new HashSet<Integer>();
		pq.offer(commitOffset);
		queuedCommitIds.add(commitOffset);
		while (!pq.isEmpty()) {
			int offset = pq.poll();
			AbstractCommit commit = revisions.get(offset);
			for (ChangedFile.Builder cf : commit.changedFiles) {
				ChangeKind ck = cf.getChange();
				switch (ck) {
				case ADDED:
					if (!adds.contains(cf.getName()) && !dels.contains(cf.getName())) {
						adds.add(cf.getName());
						snapshot.add(cf.build());
						commits.put(cf.getName(), commit);
					}
					break;
				case COPIED:
					if (!adds.contains(cf.getName()) && !dels.contains(cf.getName())) {
						adds.add(cf.getName());
						snapshot.add(cf.build());
						commits.put(cf.getName(), commit);
					}
					break;
				case DELETED:
					if (!adds.contains(cf.getName()) && !dels.contains(cf.getName()))
						dels.add(cf.getName());
					break;
				case MERGED:
					if (!adds.contains(cf.getName()) && !dels.contains(cf.getName())) {
						adds.add(cf.getName());
						snapshot.add(cf.build());
						commits.put(cf.getName(), commit);
					}
					for (int i = 0; i < cf.getPreviousIndicesCount(); i++) {
						if (cf.getChanges(i) != ChangeKind.ADDED) {
							ChangedFile.Builder pcf = revisions.get(cf.getPreviousVersions(i)).changedFiles.get(cf.getPreviousIndices(i));
							ChangeKind pck = cf.getChanges(i);
							if (!adds.contains(pcf.getName()) && !dels.contains(pcf.getName()) && (pck == ChangeKind.DELETED || pck == ChangeKind.RENAMED))
								dels.add(pcf.getName());
						}
					}
					break;
				case RENAMED:
					if (!adds.contains(cf.getName()) && !dels.contains(cf.getName())) {
						adds.add(cf.getName());
						snapshot.add(cf.build());
						commits.put(cf.getName(), commit);
					}
					for (int i = 0; i < cf.getPreviousIndicesCount(); i++) {
						ChangedFile.Builder pcf = revisions.get(cf.getPreviousVersions(i)).changedFiles.get(cf.getPreviousIndices(i));
						if (!adds.contains(pcf.getName()) && !dels.contains(pcf.getName()))
							dels.add(pcf.getName());
					}
					break;
				default:
					if (!adds.contains(cf.getName()) && !dels.contains(cf.getName())) {
						adds.add(cf.getName());
						snapshot.add(cf.build());
						commits.put(cf.getName(), commit);
					}
					break;
				}
			}
			if (commit.parentIndices != null)
				for (int p : commit.parentIndices) {
					if (!queuedCommitIds.contains(p)) {
						pq.offer(p);
						queuedCommitIds.add(p);
					}
				}
		}
	}
	
	public abstract void setRevisions();

	public List<AbstractCommit> getRevisions() {
		return revisions;
	}

	abstract void getTags();

	abstract void getBranches();

	public List<String> getBranchNames() {
		return branchNames;
	}
	
	public List<String> getTagNames() {
		return tagNames;
	}
	
	public List<Integer> getBranchIndices() {
		return branchIndices;
	}
	
	public List<Integer> getTagIndices() {
		return tagIndices;
	}
	
	public List<Revision> getCommits(final boolean parse, final Writer astWriter) {
		if (revisions == null) {
			revisions = new ArrayList<AbstractCommit>();
			setRevisions();
		}
		final List<Revision> revs = new ArrayList<Revision>();
		for (final AbstractCommit rev : revisions)
			revs.add(rev.asProtobuf(parse, astWriter));

		return revs;
	}

}
