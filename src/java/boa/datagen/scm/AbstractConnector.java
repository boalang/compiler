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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Writer;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.dom.ITypeBinding;

import boa.datagen.dependencies.DependencyMangementUtil;
import boa.datagen.dependencies.PomFile;
import boa.datagen.util.FileIO;
import boa.datagen.util.Java7Visitor;
import boa.datagen.util.Java8Visitor;
import boa.types.Ast.ASTRoot;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Diff.ChangedFile.FileKind;
import boa.types.Shared.ChangeKind;

/**
 * @author rdyer
 */
public abstract class AbstractConnector implements AutoCloseable {
	
	protected static final boolean debug = boa.datagen.util.Properties.getBoolean("debug", boa.datagen.DefaultProperties.DEBUG);
	protected static final String classpathRoot = boa.datagen.util.Properties.getProperty("libs", boa.datagen.DefaultProperties.CLASSPATH_ROOT);

	protected String path;
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
			if (changedFiles.isEmpty())
				return snapshot;
			final String[] paths = changedFiles.keySet().toArray(new String[0]);
			final String[] classpaths = buildClassPaths(commitOffset, fileContents, snapshot, commits);
			final Map<String, CompilationUnit> cus = new HashMap<String, CompilationUnit>();
			final FileASTRequestor r = new FileASTRequestor() {
				@Override
				public void acceptAST(String sourceFilePath, CompilationUnit cu) {
					sourceFilePath = sourceFilePath.replace('\\', '/');
					cus.put(sourceFilePath, cu);
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
			try {
				parser.createASTs(fileContents, paths, null, new String[0], r, null);
			} catch (Exception e) {
				return new ArrayList<ChangedFile>();
			}

			final Map<String, Integer> declarationFile = new HashMap<String, Integer>(), declarationNode = new HashMap<String, Integer>();
			collectDeclarations(paths, cus, snapshot.size(), declarationFile, declarationNode);
			
			for (i = 0; i < paths.length; i++) {
				String sourceFilePath = paths[i];
				CompilationUnit cu = cus.get(sourceFilePath);
				ChangedFile cf = changedFiles.get(sourceFilePath);
				ChangedFile.Builder fb = ChangedFile.newBuilder(cf);
				fb.setAst(false);
				fb.setKey(-1);
				
				long len = -1;
				if (astWriter != null) {
					try {
						len = astWriter.getLength();
					} catch (IOException e1) {}
				}
				
				String content = fileContents.get(sourceFilePath);
				Java8Visitor visitor = new Java8Visitor(content, declarationFile, declarationNode);
				final ASTRoot.Builder ast = ASTRoot.newBuilder();
				try {
					ast.addNamespaces(visitor.getNamespaces(cu));
					/*for (final Comment c : visitor.getComments())
						comments.addComments(c);*/
				} catch (final Throwable e) {
					System.err.println("Error visiting " + sourceFilePath + " when parsing head snapshot!!!");
					e.printStackTrace();
					System.exit(-1);
					continue;
				}
				if (astWriter != null && len > -1) {
					try {
						astWriter.append(new LongWritable(len), new BytesWritable(ast.build().toByteArray()));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				try {
					if (astWriter != null && astWriter.getLength() > len) {
						fb.setKey(len);
						fb.setAst(true);
					}
				} catch (IOException e) {}
				snapshot.add(fb.build());
			}
		}
		return snapshot;
	}

	private String[] buildClassPaths(int commitOffset, Map<String, String> fileContents, List<ChangedFile> snapshot, Map<String, AbstractCommit> commits) {
		Set<String> paths = new HashSet<String>();
		for (ChangedFile cf : snapshot) {
			if (cf.getName().endsWith(".jar")) {
				AbstractCommit commit = commits.get(cf.getName());
				String path = commit.writeFile(classpathRoot, cf.getName());
				if (path != null)
					paths.add(path);
			}
		}
		HashSet<String> globalRepoLinks = new HashSet<String>(DependencyMangementUtil.repositoryLinks);
		HashMap<String, String> globalProperties = new HashMap<String, String>();
		HashMap<String, String> globalManagedDependencies = new HashMap<String, String>();
		Stack<PomFile> parentPomFiles = new Stack<PomFile>();
		List<ChangedFile> configFiles = new ArrayList<ChangedFile>();
		final Map<ChangedFile, String> fileDir = new HashMap<ChangedFile, String>();
		for (ChangedFile cf : snapshot) {
			String name = FileIO.getFileName(cf.getName());
			if (name.equals("build.gradle")) {
				AbstractCommit commit = commits.get(cf.getName());
				Set<String> dependencies = commit.getGradleDependencies(classpathRoot, cf.getName());
				paths.addAll(dependencies);
			} else if (name.equals("pom.xml")) {
				String dir = cf.getName().substring(0, cf.getName().length() - name.length());
				fileDir.put(cf, dir);
				configFiles.add(cf);
			}
		}
		Collections.sort(configFiles, new Comparator<ChangedFile>() {
			@Override
			public int compare(ChangedFile f1, ChangedFile f2) {
				return fileDir.get(f1).compareTo(fileDir.get(f2));
			}
		});
		for (ChangedFile cf : configFiles) {
			String fileName = cf.getName(), name = FileIO.getFileName(fileName);
			if (name.equals("pom.xml")) {
				while (!parentPomFiles.isEmpty()) {
					PomFile pf = parentPomFiles.peek();
					if (pf.getPath().length() < fileName.length())
						break;
					parentPomFiles.pop();
				}
				AbstractCommit commit = commits.get(fileName);
				Set<String> dependencies = commit.getPomDependencies(classpathRoot, fileName, globalRepoLinks, globalProperties, globalManagedDependencies, parentPomFiles);
				paths.addAll(dependencies);
			}
		}
		return paths.toArray(new String[0]);
	}

	private void collectDeclarations(String[] paths, Map<String, CompilationUnit> cus, int startFileIndex, final Map<String, Integer> declarationFile, final Map<String, Integer> declarationNode) {
		for (int i = 0; i < paths.length; i++) {
			final int fileIndex = startFileIndex + i;
			String path = paths[i];
			CompilationUnit cu = cus.get(path);
			cu.accept(new ASTVisitor() {
				private int index = 1;
				@Override
				public void preVisit(ASTNode node) {
					node.setProperty(Java7Visitor.PROPERTY_INDEX, index++);
				}
				
				@Override
				public void postVisit(ASTNode node) {
					if (node instanceof AbstractTypeDeclaration) {
						AbstractTypeDeclaration t = (AbstractTypeDeclaration) node;
						ITypeBinding tb = t.resolveBinding();
						if (tb != null) {
							if (tb.getTypeDeclaration() != null)
								tb = tb.getTypeDeclaration();
							String key = tb.getKey();
							declarationFile.put(key, fileIndex);
							declarationNode.put(key, (Integer) node.getProperty(Java7Visitor.PROPERTY_INDEX));
						}
					}
				}
			});
		}
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
	
	public List<Revision> getCommits(final boolean parse, final Writer astWriter, final Writer contentWriter) {
		if (revisions == null) {
			revisions = new ArrayList<AbstractCommit>();
			setRevisions();
		}
		long maxTime = 1000;
		final List<Revision> revs = new ArrayList<Revision>();
		for (int i = 0; i < revisions.size(); i++) {
			long startTime = System.currentTimeMillis();
			final AbstractCommit rev = revisions.get(i);
			revs.add(rev.asProtobuf(parse, astWriter, contentWriter));
			
			if (debug) {
				long endTime = System.currentTimeMillis();
				long time = endTime - startTime;
				if (time > maxTime) {
					System.out.println("Max time " + (time / 1000) + " writing to protobuf commit " + (i+1)  + " " + rev.id);
					maxTime = time;
				}
			}
		}

		return revs;
	}

}
