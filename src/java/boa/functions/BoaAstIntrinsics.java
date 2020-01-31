/*
 * Copyright 2017, Hridesh Rajan, Robert Dyer,
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
package boa.functions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.lang.SerializationUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper.Context;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jgit.internal.storage.file.ByteArrayFile;
import org.eclipse.jgit.internal.storage.file.ByteArrayRepositoryBuilder;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jdt.core.JavaCore;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.InvalidProtocolBufferException;

import boa.datagen.DefaultProperties;
import boa.datagen.util.JavaErrorCheckVisitor;
import boa.datagen.util.JavaVisitor;
import boa.types.Ast.*;
import boa.types.Ast.Expression.ExpressionKind;
import boa.types.Code.CodeRepository;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Issues.IssueRepository;
import boa.types.Issues.IssuesRoot;
import boa.types.Shared.ChangeKind;
import boa.types.Shared.Person;
import boa.types.Toplevel.Project;

/**
 * Boa functions for working with ASTs.
 *
 * @author rdyer
 */
public class BoaAstIntrinsics {
	@SuppressWarnings("rawtypes")
	static Context context;
	private static MapFile.Reader map, commitsMap, reposMap, commentsMap, issuesMap, refactoringsMap, refactoringIdsMap;

	private static final Revision emptyRevision;
	static {
		Revision.Builder rb = Revision.newBuilder();
		rb.setCommitDate(0);
		Person.Builder pb = Person.newBuilder();
		pb.setUsername("");
		rb.setCommitter(pb);
		rb.setId("");
		rb.setLog("");
		emptyRevision = rb.build();
	}

	public static enum COMMITCOUNTER {
		GETS_ATTEMPTED, GETS_SUCCEED, GETS_FAILED, GETS_FAIL_MISSING, GETS_FAIL_BADPROTOBUF, GETS_FAIL_BADLOC,
	};

	public static enum ASTCOUNTER {
		GETS_ATTEMPTED, GETS_SUCCEED, GETS_FAILED, GETS_FAIL_MISSING, GETS_FAIL_BADPROTOBUF, GETS_FAIL_BADLOC,
	};

	@FunctionSpec(name = "url", returnType = "string", formalParameters = { "ChangedFile" })
	public static String changedfileToString(final ChangedFile f) {
		return f.getKey() + "!!" + f.getName();
	}

	private static final ASTRoot emptyAst = ASTRoot.newBuilder().setAstCount(0).build();
	private static final CommentsRoot emptyComments = CommentsRoot.newBuilder().build();
	private static final IssuesRoot emptyIssues = IssuesRoot.newBuilder().build();

	/**
	 * Given a ChangedFile, return the AST for that file at that revision.
	 *
	 * @param f the ChangedFile to get a snapshot of the AST for
	 * @return the AST, or an empty AST on any sort of error
	 */
	@SuppressWarnings("unchecked")
	@FunctionSpec(name = "getast", returnType = "ASTRoot", formalParameters = { "ChangedFile" })
	public static ASTRoot getast(ChangedFile f) {
		context.getCounter(ASTCOUNTER.GETS_ATTEMPTED).increment(1);

		// check new model
		if (f.hasRepoKey() && f.hasObjectId()) {
			ASTRoot r = getASTRoot(f);
			if (r != emptyAst)
				return r;
			System.err.print(" [New Model Getast] ");
		} else {
			if (map == null)
				openMap();

			try {
				final BytesWritable value = new BytesWritable();
				if (map.get(new LongWritable(f.getKey()), value) == null) {
					context.getCounter(ASTCOUNTER.GETS_FAIL_MISSING).increment(1);
				} else {
					final CodedInputStream _stream = CodedInputStream.newInstance(value.getBytes(), 0,
							value.getLength());
					// defaults to 64, really big ASTs require more
					_stream.setRecursionLimit(Integer.MAX_VALUE);
					final ASTRoot root = ASTRoot.parseFrom(_stream);
					context.getCounter(ASTCOUNTER.GETS_SUCCEED).increment(1);
					return root;
				}
			} catch (final InvalidProtocolBufferException e) {
				e.printStackTrace();
				context.getCounter(ASTCOUNTER.GETS_FAIL_BADPROTOBUF).increment(1);
			} catch (final IOException e) {
				e.printStackTrace();
				context.getCounter(ASTCOUNTER.GETS_FAIL_MISSING).increment(1);
			} catch (final RuntimeException e) {
				e.printStackTrace();
				context.getCounter(ASTCOUNTER.GETS_FAIL_MISSING).increment(1);
			} catch (final Error e) {
				e.printStackTrace();
				context.getCounter(ASTCOUNTER.GETS_FAIL_BADPROTOBUF).increment(1);
			}
		}

		System.err.println("error with ast: " + f.getChange() + " " + f.getKey() + " from " + f.getName());
		context.getCounter(ASTCOUNTER.GETS_FAILED).increment(1);
		return emptyAst;
	}

	@FunctionSpec(name = "getastcount", returnType = "int", formalParameters = { "ChangedFile" })
	public static int getAstCount(ChangedFile f) {
		return getast(f).getAstCount();
	}

	public static ASTRoot getASTRoot(ChangedFile f) {
		// if ChangedFile contains ast root
		if (f.hasRoot())
			return f.getRoot();
		return getASTRoot(getContent(f));
	}

	public static final ASTRoot getASTRoot(final String content) {
		if (content == null) {
			System.err.print(" [Null Content] ");
			return emptyAst;
		}
		try {
			final org.eclipse.jdt.core.dom.ASTParser parser = org.eclipse.jdt.core.dom.ASTParser.newParser(AST.JLS8);
			parser.setKind(org.eclipse.jdt.core.dom.ASTParser.K_COMPILATION_UNIT);
			parser.setSource(content.toCharArray());

			final Map<?, ?> options = JavaCore.getOptions();
			JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
			parser.setCompilerOptions(options);

			final CompilationUnit cu;
			try {
				cu = (CompilationUnit) parser.createAST(null);
			} catch (Throwable e) {
				return emptyAst;
			}

			final JavaErrorCheckVisitor errorCheck = new JavaErrorCheckVisitor();
			cu.accept(errorCheck);

			if (!errorCheck.hasError) {
				final ASTRoot.Builder ast = ASTRoot.newBuilder();
				final JavaVisitor visitor = new JavaVisitor(content);
				try {
					ast.addNamespaces(visitor.getNamespaces(cu));
					ast.setAstCount(visitor.getAstCount());
					return ast.build();
				} catch (final Throwable e) {
					System.exit(-1);
					System.err.print(" [Parser Error] ");
					return emptyAst;
				}
			} else {
				System.err.print(" [Java Error] ");
			}
			return emptyAst;
		} catch (final Throwable e) {
			return emptyAst;
		}
	}

	private static long currentRepoKey = Long.MIN_VALUE;
	private static Repository currentStoredRepository = null;

	public static String getContent(ChangedFile f) {
		if (f.hasRepoKey() && f.hasObjectId()) {
			if (f.getRepoKey() != currentRepoKey || currentStoredRepository == null) {
				currentRepoKey = f.getRepoKey();
				BytesWritable value = getValueFromRepoMap(f);
				if (value != null) {
					ByteArrayFile file = (ByteArrayFile) SerializationUtils.deserialize(value.getBytes());
					try {
						cleanup(null);
						currentStoredRepository = new ByteArrayRepositoryBuilder().setGitDir(file).build();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					System.err.print(" [Repo Map Value Null] ");
					cleanup(null);
					return null;
				}
			}
			try {
				return getContent(currentStoredRepository, f.getObjectId());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.err.print(" [No reposkey or objectid] ");
		return null;
	}

	@FunctionSpec(name = "closerepo")
	public static void closeRepo() {
		if (currentStoredRepository != null) {
			currentStoredRepository.close();
			currentStoredRepository = null;
		}
	}

	public static final String getContent(Repository repo, String oid) throws IOException {
		if (repo == null) {
			System.err.println("repo null");
			return null;
		}
		ObjectId fileid = ObjectId.fromString(oid);
		ByteArrayOutputStream buffer = new ByteArrayOutputStream(4096);
		String content;
		try {
			buffer.reset();
			byte[] arr = repo.open(fileid, Constants.OBJ_BLOB).getCachedBytes();
			buffer.write(arr);
			content = buffer.toString();
			return content;
		} catch (final Throwable e) {
			e.printStackTrace();
			return null;
		} finally {
			buffer.flush();
			buffer.close();
		}
	}

	@FunctionSpec(name = "getParsedChangedFile", returnType = "ChangedFile", formalParameters = { "ChangedFile" })
	public static ChangedFile getParsedChangedFile(ChangedFile f) {
		ASTRoot ast = getASTRoot(f);
		if (ast != emptyAst)
			return f.toBuilder().setRoot(ast).setAst(true).build();
		else {
			System.err.println(" [getParsedChangedFile: emptyAst] " + f.getName());
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public static final BytesWritable getValueFromRepoMap(ChangedFile f) {
		if (reposMap == null)
			openRepoMap();
		try {
			BytesWritable value = new BytesWritable();
			if (reposMap.get(new LongWritable(f.getRepoKey()), value) == null) {
				context.getCounter(ASTCOUNTER.GETS_FAIL_MISSING).increment(1);
				return null;
			} else {
				context.getCounter(ASTCOUNTER.GETS_SUCCEED).increment(1);
				return value;
			}
		} catch (final InvalidProtocolBufferException e) {
			e.printStackTrace();
			context.getCounter(ASTCOUNTER.GETS_FAIL_BADPROTOBUF).increment(1);
		} catch (final IOException e) {
			e.printStackTrace();
			context.getCounter(ASTCOUNTER.GETS_FAIL_MISSING).increment(1);
		} catch (final RuntimeException e) {
			e.printStackTrace();
			context.getCounter(ASTCOUNTER.GETS_FAIL_MISSING).increment(1);
		} catch (final Error e) {
			e.printStackTrace();
			context.getCounter(ASTCOUNTER.GETS_FAIL_BADPROTOBUF).increment(1);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	static Revision getRevision(long key) {
		context.getCounter(COMMITCOUNTER.GETS_ATTEMPTED).increment(1);

		if (commitsMap == null)
			openCommitMap();

		try {
			final BytesWritable value = new BytesWritable();
			if (commitsMap.get(new LongWritable(key), value) == null) {
				context.getCounter(COMMITCOUNTER.GETS_FAIL_MISSING).increment(1);
			} else {
				final CodedInputStream _stream = CodedInputStream.newInstance(value.getBytes(), 0, value.getLength());
				// defaults to 64, really big ASTs require more
				_stream.setRecursionLimit(Integer.MAX_VALUE);
				final Revision root = Revision.parseFrom(_stream);
				context.getCounter(COMMITCOUNTER.GETS_SUCCEED).increment(1);
				return root;
			}
		} catch (final InvalidProtocolBufferException e) {
			e.printStackTrace();
			context.getCounter(COMMITCOUNTER.GETS_FAIL_BADPROTOBUF).increment(1);
		} catch (final IOException e) {
			e.printStackTrace();
			context.getCounter(COMMITCOUNTER.GETS_FAIL_MISSING).increment(1);
		} catch (final RuntimeException e) {
			e.printStackTrace();
			context.getCounter(COMMITCOUNTER.GETS_FAIL_MISSING).increment(1);
		} catch (final Error e) {
			e.printStackTrace();
			context.getCounter(COMMITCOUNTER.GETS_FAIL_BADPROTOBUF).increment(1);
		}

		System.err.println("error with revision: " + key);
		context.getCounter(COMMITCOUNTER.GETS_FAILED).increment(1);
		return emptyRevision;
	}

	/**
	 * Given a ChangedFile, return the comments for that file at that revision.
	 *
	 * @param f the ChangedFile to get a snapshot of the comments for
	 * @return the comments list, or an empty list on any sort of error
	 */
	@FunctionSpec(name = "getcomments", returnType = "CommentsRoot", formalParameters = { "ChangedFile" })
	public static CommentsRoot getcomments(final ChangedFile f) {
		// since we know only certain kinds have comments, filter before looking up
		final ChangedFile.FileKind kind = f.getKind();
		if (kind != ChangedFile.FileKind.SOURCE_JAVA_ERROR && kind != ChangedFile.FileKind.SOURCE_JAVA_JLS2
				&& kind != ChangedFile.FileKind.SOURCE_JAVA_JLS3 && kind != ChangedFile.FileKind.SOURCE_JAVA_JLS4
				&& kind != ChangedFile.FileKind.SOURCE_JAVA_JLS8)
			return emptyComments;

		final String rowName = f.getKey() + "!!" + f.getName();

		if (commentsMap == null)
			openCommentMap();

		try {
			final BytesWritable value = new BytesWritable();
			if (commentsMap.get(new Text(rowName), value) != null) {
				final CodedInputStream _stream = CodedInputStream.newInstance(value.getBytes(), 0, value.getLength());
				final CommentsRoot root = CommentsRoot.parseFrom(_stream);
				return root;
			}
		} catch (final InvalidProtocolBufferException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final RuntimeException e) {
			e.printStackTrace();
		} catch (final Error e) {
			e.printStackTrace();
		}

		System.err.println("error with comments: " + rowName);
		return emptyComments;
	}

	/**
	 * Given an IssueRepository, return the issues.
	 *
	 * @param f the IssueRepository to get issues for
	 * @return the issues list, or an empty list on any sort of error
	 */
	@FunctionSpec(name = "getissues", returnType = "IssuesRoot", formalParameters = { "IssueRepository" })
	public static IssuesRoot getissues(final IssueRepository f) {
		if (issuesMap == null)
			openIssuesMap();

		try {
			final BytesWritable value = new BytesWritable();
			if (issuesMap.get(new Text(f.getKey()), value) != null) {
				final CodedInputStream _stream = CodedInputStream.newInstance(value.getBytes(), 0, value.getLength());
				final IssuesRoot root = IssuesRoot.parseFrom(_stream);
				return root;
			}
		} catch (final InvalidProtocolBufferException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final RuntimeException e) {
			e.printStackTrace();
		} catch (final Error e) {
			e.printStackTrace();
		}

		System.err.println("error with issues: " + f.getKey());
		return emptyIssues;
	}

	@SuppressWarnings("rawtypes")
	public static void setup(final Context context) {
		BoaAstIntrinsics.context = context;
	}
	
	@FunctionSpec(name = "get_code_change", returnType = "Change", formalParameters = { "Project",
			"Revision" })
	public static boa.types.Code.Change getCodeChange(Project p, Revision r) {

		if (refactoringsMap == null)
			openRefactoringMap();

		try {
			final BytesWritable value = new BytesWritable();
			if (refactoringsMap.get(new Text(p.getName() + " " + r.getId()), value) != null) {
				// use array copy to avoid extra bytes
				byte[] data = Arrays.copyOf(value.getBytes(), value.getLength());
				String[] temp = new String(data, StandardCharsets.UTF_8).split("\\r?\\n");			
				HashSet<String> set = new HashSet<String>(Arrays.asList(temp));
				
				final CodedInputStream _stream = CodedInputStream.newInstance(value.getBytes(), 0, value.getLength());
				// defaults to 64, really big ASTs require more
				_stream.setRecursionLimit(Integer.MAX_VALUE);
				final boa.types.Code.Change change = boa.types.Code.Change.parseFrom(_stream);
				
				return change;
			}
		} catch (final Throwable e) {
			e.printStackTrace();
		}
		return boa.types.Code.Change.newBuilder().build();
	}

	private static void openRefactoringMap() {
		try {
			final Configuration conf = context.getConfiguration();
			final FileSystem fs;
			final Path p;
			if (DefaultProperties.localDataPath != null) {
				p = new Path(DefaultProperties.localDataPath, "refactoring");
				fs = FileSystem.getLocal(conf);
			} else {
				p = new Path(context.getConfiguration().get("fs.default.name", "hdfs://boa-njt/"), new Path(
						conf.get("boa.ast.dir", conf.get("boa.input.dir", "repcache/live")), new Path("refactoring")));
				fs = FileSystem.get(conf);
			}
			refactoringsMap = new MapFile.Reader(fs, p.toString(), conf);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
	
	@FunctionSpec(name = "getrefactoringidsinset", returnType = "set of string", formalParameters = { "Project" })
	public static HashSet<String> getRefactoringIdsInSet(Project p) {
		return new HashSet<String>(Arrays.asList(getRefactoringIds(p)));
	}
	
	
	
	@FunctionSpec(name = "getrefactoringids", returnType = "array of string", formalParameters = { "Project" })
	public static String[] getRefactoringIds(Project p) {
	
		if (refactoringIdsMap == null)
			openRefactoringIdMap();
		
		try {
			final BytesWritable value = new BytesWritable();
			if (refactoringIdsMap.get(new Text(p.getName()), value) != null) {
				// use array copy to avoid extra bytes
				byte[] data = Arrays.copyOf(value.getBytes(), value.getLength());
				String[] temp = new String(data, StandardCharsets.UTF_8).split("\\r?\\n");
				return temp;
			}
		} catch (final Throwable e) {
			e.printStackTrace();
		} finally {
			closeRefactoringIdsMap();
		}
		return new String[0];
	}

	private static void openRefactoringIdMap() {
		try {
			final Configuration conf = context.getConfiguration();
			final FileSystem fs;
			final Path p;
			if (DefaultProperties.localDataPath != null) {
				p = new Path(DefaultProperties.localDataPath, "refactoringId");
				fs = FileSystem.getLocal(conf);
			} else {
				p = new Path(context.getConfiguration().get("fs.default.name", "hdfs://boa-njt/"),
						new Path(conf.get("boa.ast.dir", conf.get("boa.input.dir", "repcache/live")), new Path("refactoringId")));
				fs = FileSystem.get(conf);
			}
			refactoringIdsMap = new MapFile.Reader(fs, p.toString(), conf);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private static void openMap() {
		try {
			final Configuration conf = context.getConfiguration();
			final FileSystem fs;
			final Path p;
			if (DefaultProperties.localDataPath != null) {
				p = new Path(DefaultProperties.localDataPath, "ast");
				fs = FileSystem.getLocal(conf);
			} else {
				p = new Path(context.getConfiguration().get("fs.default.name", "hdfs://boa-njt/"),
						new Path(conf.get("boa.ast.dir", conf.get("boa.input.dir", "repcache/live")), new Path("ast")));
				fs = FileSystem.get(conf);
			}
			map = new MapFile.Reader(fs, p.toString(), conf);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private static void openRepoMap() {
		try {
			final Configuration conf = context.getConfiguration();
			final FileSystem fs;
			final Path p;
			if (DefaultProperties.localDataPath != null) {
				p = new Path(DefaultProperties.localDataPath, "repo");
				fs = FileSystem.getLocal(conf);
			} else {
				p = new Path(context.getConfiguration().get("fs.default.name", "hdfs://boa-njt/"), new Path(
						conf.get("boa.ast.dir", conf.get("boa.input.dir", "repcache/live")), new Path("repo")));
				fs = FileSystem.get(conf);
			}
			reposMap = new MapFile.Reader(fs, p.toString(), conf);
			System.err.println(" [open repo map] ");
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private static void openCommentMap() {
		try {
			final Configuration conf = context.getConfiguration();
			final FileSystem fs;
			final Path p;
			if (DefaultProperties.localDataPath != null) {
				p = new Path(DefaultProperties.localDataPath, "comments");
				fs = FileSystem.getLocal(conf);
			} else {
				p = new Path(context.getConfiguration().get("fs.default.name", "hdfs://boa-njt/"),
						new Path(conf.get("boa.comments.dir", conf.get("boa.input.dir", "repcache/live")),
								new Path("comments")));
				fs = FileSystem.get(conf);
			}
			commentsMap = new MapFile.Reader(fs, p.toString(), conf);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private static void openIssuesMap() {
		try {
			final Configuration conf = context.getConfiguration();
			final FileSystem fs;
			final Path p;
			if (DefaultProperties.localDataPath != null) {
				p = new Path(DefaultProperties.localDataPath, "issues");
				fs = FileSystem.getLocal(conf);
			} else {
				p = new Path(context.getConfiguration().get("fs.default.name", "hdfs://boa-njt/"), new Path(
						conf.get("boa.issues.dir", conf.get("boa.input.dir", "repcache/live")), new Path("issues")));
				fs = FileSystem.get(conf);
			}
			issuesMap = new MapFile.Reader(fs, p.toString(), conf);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private static void openCommitMap() {
		try {
			final Configuration conf = context.getConfiguration();
			final FileSystem fs;
			final Path p;
			if (DefaultProperties.localDataPath != null) {
				p = new Path(DefaultProperties.localDataPath, "commit");
				fs = FileSystem.getLocal(conf);
			} else {
				p = new Path(context.getConfiguration().get("fs.default.name", "hdfs://boa-njt/"), new Path(
						conf.get("boa.ast.dir", conf.get("boa.input.dir", "repcache/live")), new Path("commit")));
				fs = FileSystem.get(conf);
			}
			commitsMap = new MapFile.Reader(fs, p.toString(), conf);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("rawtypes")
	public static void cleanup(final Context context) {
		closeRepo();
		closeAllMaps();
		System.gc();
	}
	
	public static void closeAllMaps() {
		closeMap();
		closeReposMap();
		closeCommentsMap();
		closeIssuesMap();
		closeCommitsMap();
		closeRefactoringsMap();
		closeRefactoringIdsMap();
	}
	
	@FunctionSpec(name = "clean_up")
	public static void cleanup() {
		cleanup(null);
	}

	private static void closeMap(MapFile.Reader map) {
		if (map != null)
			try {
				map.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
	}
	
	private static void closeMap() {
		closeMap(map);
		map = null;
	}
	
	private static void closeReposMap() {
		closeMap(reposMap);
		reposMap = null;
	}
	
	private static void closeCommentsMap() {
		closeMap(commentsMap);
		commentsMap = null;
	}
	
	private static void closeIssuesMap() {
		closeMap(issuesMap);
		issuesMap = null;
	}
	
	private static void closeCommitsMap() {
		closeMap(commitsMap);
		commitsMap = null;
	}
	
	private static void closeRefactoringsMap() {
		closeMap(refactoringsMap);
		refactoringsMap = null;
	}
	
	private static void closeRefactoringIdsMap() {
		closeMap(refactoringIdsMap);
		refactoringIdsMap = null;
	}

	@FunctionSpec(name = "type_name", returnType = "string", formalParameters = { "string" })
	public static String type_name(final String s) {
		// first, normalize the string
		final String t = s.replaceAll("<\\s+", "<").replaceAll(",\\s+", ", ").replaceAll("\\s*>\\s*", ">")
				.replaceAll("\\s*&\\s*", " & ").replaceAll("\\s*\\|\\s*", " | ");

		if (!t.contains("."))
			return t;

		/*
		 * Remove qualifiers from anywhere in the string...
		 *
		 * SomeType => SomeType foo.SomeType => SomeType foo.bar.SomeType => SomeType
		 * SomeType<T> => SomeType<T> SomeType<T, S> => SomeType<T, S>
		 * SomeType<foo.bar.T, S> => SomeType<T, S> SomeType<T, foo.bar.S> =>
		 * SomeType<T, S> foo.bar.SomeType<T, foo.bar.S<bar.Q>> => SomeType<T, S<Q>>
		 * SomeType|foo.Bar => SomeType|Bar foo<T>.bar<T> => foo<T>.bar<T>
		 */
		return t.replaceAll("[^\\s,<>|]+\\.([^\\s\\[.,><|]+)", "$1");
	}

	/**
	 * A visitor that returns the total number of AST nodes.
	 */
	public final static BoaCountingVisitor lenVisitor = new BoaCountingVisitor() {
		/** {@inheritDoc} */
		@Override
		protected boolean defaultPreVisit() {
			count++;
			return true;
		}

		/** {@inheritDoc} */
		@Override
		protected boolean preVisit(final Project node) throws Exception {
			return true;
		}

		/** {@inheritDoc} */
		@Override
		protected boolean preVisit(final CodeRepository node) throws Exception {
			return true;
		}

		/** {@inheritDoc} */
		@Override
		protected boolean preVisit(final Revision node) throws Exception {
			return true;
		}

		/** {@inheritDoc} */
		@Override
		protected boolean preVisit(final ChangedFile node) throws Exception {
			return true;
		}

		/** {@inheritDoc} */
		@Override
		protected boolean preVisit(final ASTRoot node) throws Exception {
			return true;
		}

		/** {@inheritDoc} */
		@Override
		protected boolean preVisit(final Person node) throws Exception {
			return true;
		}
	};

	/**
	 *
	 */
	public static class SnapshotVisitor extends BoaCollectingVisitor<String, ChangedFile> {
		private long timestamp;
		private String[] kinds;

		public SnapshotVisitor initialize(final long timestamp, final String... kinds) {
			initialize(new HashMap<String, ChangedFile>());
			this.timestamp = timestamp;
			this.kinds = kinds;
			return this;
		}

		/** {@inheritDoc} */
		@Override
		protected boolean preVisit(final Revision node) throws Exception {
			return node.getCommitDate() <= timestamp;
		}

		/** {@inheritDoc} */
		@Override
		protected boolean preVisit(final ChangedFile node) throws Exception {
			if (node.getChange() == ChangeKind.DELETED) {
				map.remove(node.getName());
				return false;
			}

			boolean filter = kinds.length > 0;

			if (filter) {
				final String kindName = node.getKind().name();
				for (final String kind : kinds)
					if (kindName.startsWith(kind)) {
						filter = false;
						break;
					}
			}

			if (!filter)
				map.put(node.getName(), node);

			return false;
		}
	}

	public final static SnapshotVisitor snapshot = new SnapshotVisitor();

	///////////////////////////////
	// Literal testing functions */
	///////////////////////////////

	/**
	 * Returns <code>true</code> if the expression <code>e</code> is of kind
	 * <code>LITERAL</code> and is an integer literal.
	 *
	 * The test is a simplified grammar, based on the one from:
	 * https://docs.oracle.com/javase/specs/jls/se8/html/jls-3.html#jls-3.10
	 *
	 * DecimalNumeral: [0-9] [lL]? [1-9] [0-9] ([0-9_]* [0-9])? [lL]? [1-9] [_]+
	 * [0-9] ([0-9_]* [0-9])? [lL]?
	 *
	 * HexNumeral: 0 [xX] [0-9a-fA-F] ([0-9a-fA-F_]* [0-9a-fA-F])? [lL]?
	 *
	 * OctalNumeral: 0 [_]* [0-7] ([0-7_]* [0-7])? [lL]?
	 *
	 * BinaryNumeral: 0 [bB] [01] ([01_]* [01])? [lL]?
	 *
	 * If any of these match, it returns <code>true</code>. Otherwise it returns
	 * <code>false</code>.
	 *
	 * @param e the expression to test
	 * @return true if the expression is an integer literal, otherwise false
	 */
	@FunctionSpec(name = "isintlit", returnType = "bool", formalParameters = { "Expression" })
	public static boolean isIntLit(final Expression e) throws Exception {
		if (e.getKind() != Expression.ExpressionKind.LITERAL)
			return false;
		if (!e.hasLiteral())
			return false;
		if (e.getLiteral().matches("^[0-9][lL]?$"))
			return true;
		if (e.getLiteral().matches("^[1-9][0-9]([0-9_]*[0-9])?[lL]?$"))
			return true;
		if (e.getLiteral().matches("^[1-9][_]+[0-9]([0-9_]*[0-9])?[lL]?$"))
			return true;
		if (e.getLiteral().matches("^0[xX][0-9a-fA-F]([0-9a-fA-F_]*[0-9a-fA-F])?[lL]?$"))
			return true;
		if (e.getLiteral().matches("^0[_]*[0-7]([0-7_]*[0-7])?[lL]?$"))
			return true;
		return e.getLiteral().matches("^0[bB][01]([01_]*[01])?[lL]?$");
	}

	/**
	 * Returns <code>true</code> if the expression <code>e</code> is of kind
	 * <code>LITERAL</code> and is a float literal.
	 *
	 * The test is a simplified grammar, based on the one from:
	 * https://docs.oracle.com/javase/specs/jls/se8/html/jls-3.html#jls-3.10
	 *
	 * DecimalFloatingPointLiteral: [0-9] ([0-9_]* [0-9])? \\. ([0-9] ([0-9_]*
	 * [0-9])?)? ([eE] [+-]? [0-9] ([0-9_]* [0-9])?)? [fFdD]? \\. [0-9] ([0-9_]*
	 * [0-9])? ([eE] [+-]? [0-9] ([0-9_]* [0-9])?)? [fFdD]? [0-9] ([0-9_]* [0-9])?
	 * [eE] [+-]? [0-9] ([0-9_]* [0-9])? [fFdD]? [0-9] ([0-9_]* [0-9])? ([eE] [+-]?
	 * [0-9] ([0-9_]* [0-9])?)? [fFdD]
	 *
	 * HexadecimalFloatingPointLiteral: 0 [Xx] [0-9a-fA-F] ([0-9a-fA-F_]*
	 * [0-9a-fA-F])? \\.? [pP] [+-]? [0-9] ([0-9_]* [0-9])? [fFdD]? 0 [Xx]
	 * ([0-9a-fA-F] ([0-9a-fA-F_]* [0-9a-fA-F])?)? \\. [0-9a-fA-F] ([0-9a-fA-F_]*
	 * [0-9a-fA-F])? [pP] [+-]? [0-9] ([0-9_]* [0-9])? [fFdD]?
	 *
	 * @param e the expression to test
	 * @return true if the expression is a char literal, otherwise false
	 */
	@FunctionSpec(name = "isfloatlit", returnType = "bool", formalParameters = { "Expression" })
	public static boolean isFloatLit(final Expression e) throws Exception {
		if (e.getKind() != Expression.ExpressionKind.LITERAL)
			return false;
		if (!e.hasLiteral())
			return false;
		if (e.getLiteral()
				.matches("^[0-9]([0-9_]*[0-9])?\\.([0-9]([0-9_]*[0-9])?)?([eE][+-]?[0-9]([0-9_]*[0-9])?)?[fFdD]?$"))
			return true;
		if (e.getLiteral().matches("^\\.[0-9]([0-9_]*[0-9])?([eE][+-]?[0-9]([0-9_]*[0-9])?)?[fFdD]?$"))
			return true;
		if (e.getLiteral().matches("^[0-9]([0-9_]*[0-9])?[eE][+-]?[0-9]([0-9_]*[0-9])?[fFdD]?$"))
			return true;
		if (e.getLiteral().matches("^[0-9]([0-9_]*[0-9])?([eE][+-]?[0-9]([0-9_]*[0-9])?)?[fFdD]$"))
			return true;
		if (e.getLiteral()
				.matches("^0[Xx][0-9a-fA-F]([0-9a-fA-F_]*[0-9a-fA-F])?\\.?[pP][+-]?[0-9]([0-9_]*[0-9])?[fFdD]?$"))
			return true;
		return e.getLiteral().matches(
				"^0[Xx]([0-9a-fA-F]([0-9a-fA-F_]*[0-9a-fA-F])?)?\\.[0-9a-fA-F]([0-9a-fA-F_]*[0-9a-fA-F])?[pP][+-]?[0-9]([0-9_]*[0-9])?[fFdD]?$");
	}

	/**
	 * Returns <code>true</code> if the expression <code>e</code> is of kind
	 * <code>LITERAL</code> and is a char literal.
	 *
	 * @param e the expression to test
	 * @return true if the expression is a char literal, otherwise false
	 */
	@FunctionSpec(name = "ischarlit", returnType = "bool", formalParameters = { "Expression" })
	public static boolean isCharLit(final Expression e) throws Exception {
		if (e.getKind() != Expression.ExpressionKind.LITERAL)
			return false;
		if (!e.hasLiteral())
			return false;
		return e.getLiteral().startsWith("'");
	}

	/**
	 * Returns <code>true</code> if the expression <code>e</code> is of kind
	 * <code>LITERAL</code> and is a string literal.
	 *
	 * @param e the expression to test
	 * @return true if the expression is a string literal, otherwise false
	 */
	@FunctionSpec(name = "isstringlit", returnType = "bool", formalParameters = { "Expression" })
	public static boolean isStringLit(final Expression e) throws Exception {
		if (e.getKind() != Expression.ExpressionKind.LITERAL)
			return false;
		if (!e.hasLiteral())
			return false;
		return e.getLiteral().startsWith("\"");
	}

	/**
	 * Returns <code>true</code> if the expression <code>e</code> is of kind
	 * <code>LITERAL</code> and is a type literal.
	 *
	 * @param e the expression to test
	 * @return true if the expression is a type literal, otherwise false
	 */
	@FunctionSpec(name = "istypelit", returnType = "bool", formalParameters = { "Expression" })
	public static boolean isTypeLit(final Expression e) throws Exception {
		if (e.getKind() != Expression.ExpressionKind.LITERAL)
			return false;
		if (!e.hasLiteral())
			return false;
		return e.getLiteral().endsWith(".class");
	}

	/**
	 * Returns <code>true</code> if the expression <code>e</code> is of kind
	 * <code>LITERAL</code> and is a bool literal.
	 *
	 * @param e the expression to test
	 * @return true if the expression is a bool literal, otherwise false
	 */
	@FunctionSpec(name = "isboollit", returnType = "bool", formalParameters = { "Expression" })
	public static boolean isBoolLit(final Expression e) throws Exception {
		if (e.getKind() != Expression.ExpressionKind.LITERAL)
			return false;
		if (!e.hasLiteral())
			return false;
		return e.getLiteral().equals("true") || e.getLiteral().equals("false");
	}

	/**
	 * Returns <code>true</code> if the expression <code>e</code> is of kind
	 * <code>LITERAL</code> and is a null literal.
	 *
	 * @param e the expression to test
	 * @return true if the expression is a null literal, otherwise false
	 */
	@FunctionSpec(name = "isnulllit", returnType = "bool", formalParameters = { "Expression" })
	public static boolean isNullLit(final Expression e) throws Exception {
		if (e.getKind() != Expression.ExpressionKind.LITERAL)
			return false;
		if (!e.hasLiteral())
			return false;
		return e.getLiteral().equals("null");
	}

	/**
	 * Returns <code>true</code> if the expression <code>e</code> is of kind
	 * <code>LITERAL</code> and the literal matches the string <code>lit</code>.
	 *
	 * @param e the expression to test
	 * @return true if the expression is a string literal, otherwise false
	 */
	@FunctionSpec(name = "isliteral", returnType = "bool", formalParameters = { "Expression", "string" })
	public static boolean isLiteral(final Expression e, final String lit) throws Exception {
		return e.getKind() == Expression.ExpressionKind.LITERAL && e.hasLiteral() && e.getLiteral().equals(lit);
	}

	////////////////////////////////
	// Operator testing functions */
	////////////////////////////////
	@FunctionSpec(name = "isoperator", returnType = "bool", formalParameters = { "Expression" })
	public static boolean isOperator(final Expression e) throws Exception {
		return isArithmeticOperator(e) || isBitwiseOperator(e) || isLogicalOperator(e) || isRelationalOperator(e);
	}

	@FunctionSpec(name = "isarithmeticoperator", returnType = "bool", formalParameters = { "Expression" })
	public static boolean isArithmeticOperator(final Expression e) throws Exception {
		switch (e.getKind()) {
		case OP_ADD:
			return true;
		case OP_SUB:
			return true;
		case OP_MULT:
			return true;
		case OP_DIV:
			return true;
		case OP_MOD:
			return true;
		case OP_INC:
			return true;
		case OP_DEC:
			return true;
		default:
			return false;
		}
	}

	@FunctionSpec(name = "isbitwiseoperator", returnType = "bool", formalParameters = { "Expression" })
	public static boolean isBitwiseOperator(final Expression e) throws Exception {
		switch (e.getKind()) {
		case BIT_LSHIFT:
			return true;
		case BIT_RSHIFT:
			return true;
		case BIT_UNSIGNEDRSHIFT:
			return true;
		case BIT_AND:
			return true;
		case BIT_OR:
			return true;
		case BIT_NOT:
			return true;
		case BIT_XOR:
			return true;
		default:
			return false;
		}
	}

	@FunctionSpec(name = "islogicaloperator", returnType = "bool", formalParameters = { "Expression" })
	public static boolean isLogicalOperator(final Expression e) throws Exception {
		switch (e.getKind()) {
		case LOGICAL_NOT:
			return true;
		case LOGICAL_AND:
			return true;
		case LOGICAL_OR:
			return true;
		default:
			return false;
		}
	}

	@FunctionSpec(name = "isrelationaloperator", returnType = "bool", formalParameters = { "Expression" })
	public static boolean isRelationalOperator(final Expression e) throws Exception {
		switch (e.getKind()) {
		case EQ:
			return true;
		case NEQ:
			return true;
		case LT:
			return true;
		case GT:
			return true;
		case LTEQ:
			return true;
		case GTEQ:
			return true;
		default:
			return false;
		}
	}

	@FunctionSpec(name = "isassignmentoperator", returnType = "bool", formalParameters = { "Expression" })
	public static boolean isAssignmentOperator(final Expression e) throws Exception {
		switch (e.getKind()) {
		case ASSIGN:
			return true;
		case ASSIGN_ADD:
			return true;
		case ASSIGN_SUB:
			return true;
		case ASSIGN_MULT:
			return true;
		case ASSIGN_DIV:
			return true;
		case ASSIGN_MOD:
			return true;
		case ASSIGN_BITXOR:
			return true;
		case ASSIGN_BITAND:
			return true;
		case ASSIGN_BITOR:
			return true;
		case ASSIGN_LSHIFT:
			return true;
		case ASSIGN_RSHIFT:
			return true;
		case ASSIGN_UNSIGNEDRSHIFT:
			return true;
		default:
			return false;
		}
	}

	////////////////////////////////
	// Creation testing functions */
	////////////////////////////////
	@FunctionSpec(name = "iscreation", returnType = "bool", formalParameters = { "Expression" })
	public static boolean isCreation(final Expression e) throws Exception {
		switch (e.getKind()) {
		case NEW:
			return true;
		case NEWARRAY:
			return true;
		default:
			return false;
		}
	}

	//////////////////////////////
	// Collect Annotations Used //
	//////////////////////////////

	private static class AnnotationCollectingVisitor extends BoaCollectingVisitor<String, Long> {
		@Override
		protected boolean preVisit(Modifier node) {
			if (node.getKind() == Modifier.ModifierKind.ANNOTATION) {
				final String name = BoaAstIntrinsics.type_name(node.getAnnotationName());
				final long count = map.containsKey(name) ? map.get(name) : 0;
				map.put(name, count + 1);
			}
			return true;
		}
	}

	private static AnnotationCollectingVisitor annotationCollectingVisitor = new AnnotationCollectingVisitor();

	@FunctionSpec(name = "collect_annotations", returnType = "map[string] of int", formalParameters = { "ASTRoot",
			"map[string] of int" })
	public static HashMap<String, Long> collect_annotations(final ASTRoot f, final HashMap<String, Long> map)
			throws Exception {
		annotationCollectingVisitor.initialize(map).visit(f);
		return annotationCollectingVisitor.map;
	}

	///////////////////////////
	// Collect Generics Used //
	///////////////////////////

	private static class GenericsCollectingVisitor extends BoaCollectingVisitor<String, Long> {
		@Override
		protected boolean preVisit(Type node) {
			// FIXME
			/*
			 * try { parseGenericType(BoaAstIntrinsics.type_name(node.getName()).trim(),
			 * map); } catch (final StackOverflowError e) { System.err.println("STACK ERR: "
			 * + node.getName() + " -> " +
			 * BoaAstIntrinsics.type_name(node.getName()).trim()); }
			 */
			return true;
		}
	}

	private static GenericsCollectingVisitor genericsCollectingVisitor = new GenericsCollectingVisitor();

	@FunctionSpec(name = "collect_generic_types", returnType = "map[string] of int", formalParameters = { "ASTRoot",
			"map[string] of int" })
	public static HashMap<String, Long> collect_generic_types(final ASTRoot f, final HashMap<String, Long> map)
			throws Exception {
		genericsCollectingVisitor.initialize(map).visit(f);
		return genericsCollectingVisitor.map;
	}

	@SuppressWarnings("unused")
	private static void parseGenericType(final String name, final HashMap<String, Long> counts) {
		if (!name.contains("<") || name.startsWith("<"))
			return;

		if (name.contains("|")) {
			for (final String s : name.split("\\|"))
				parseGenericType(s.trim(), counts);
			return;
		}

		if (name.contains("&")) {
			int count = 0;
			int last = 0;
			for (int i = 0; i < name.length(); i++)
				switch (name.charAt(i)) {
				case '<':
					count++;
					break;
				case '>':
					count--;
					break;
				case '&':
					if (count == 0) {
						parseGenericType(name.substring(last, i).trim(), counts);
						last = i + 1;
					}
					break;
				default:
					break;
				}
			parseGenericType(name.substring(last).trim(), counts);
			return;
		}

		foundType(name, counts);

		int start = name.indexOf("<");

		final Stack<Integer> starts = new Stack<Integer>();
		int lastStart = start + 1;
		for (int i = lastStart; i < name.lastIndexOf(">"); i++)
			switch (name.charAt(i)) {
			case '<':
				starts.push(lastStart);
				lastStart = i + 1;
				break;
			case '>':
				if (!starts.empty())
					foundType(name.substring(starts.pop(), i + 1).trim(), counts);
				break;
			case '&':
			case '|':
			case ',':
			case ' ':
			case '.':
			case '\t':
				lastStart = i + 1;
				break;
			default:
				break;
			}
	}

	private static void foundType(final String name, final HashMap<String, Long> counts) {
		final String type = name.endsWith("...") ? name.substring(0, name.length() - 3).trim() : name.trim();
		final long count = counts.containsKey(type) ? counts.get(type) : 0;
		counts.put(type, count + 1);

		String rawType = type.substring(0, type.indexOf("<")).trim();
		if (!type.endsWith(">"))
			rawType += type.substring(type.lastIndexOf(">") + 1).trim();
		final long rawCount = counts.containsKey(rawType) ? counts.get(rawType) : 0;
		counts.put(rawType, rawCount + 1);
	}

	static int indent = 0;

	private static String indent() {
		String s = "";
		for (int i = 0; i < indent; i++)
			s += "\t";
		return s;
	}

	@FunctionSpec(name = "prettyprint", returnType = "string", formalParameters = { "ASTRoot" })
	public static String prettyprint(final ASTRoot r) {
		if (r == null)
			return "";

		String s = "";

		for (final Namespace n : r.getNamespacesList())
			s += prettyprint(n);

		return s;
	}

	@FunctionSpec(name = "prettyprint", returnType = "string", formalParameters = { "Namespace" })
	public static String prettyprint(final Namespace n) {
		if (n == null)
			return "";

		String s = "";

		if (n.getName().length() > 0) {
			s += prettyprint(n.getModifiersList());
			s += indent() + "package " + n.getName() + ";\n";
		}

		for (final String i : n.getImportsList())
			s += indent() + "import " + i + "\n";

		for (final Declaration d : n.getDeclarationsList())
			s += prettyprint(d);

		return s;
	}

	@FunctionSpec(name = "prettyprint", returnType = "string", formalParameters = { "Declaration" })
	public static String prettyprint(final Declaration d) {
		if (d == null)
			return "";

		String s = indent() + prettyprint(d.getModifiersList());

		switch (d.getKind()) {
		case INTERFACE:
			s += "interface " + d.getName();
			if (d.getGenericParametersCount() > 0) {
				s += "<";
				for (int i = 0; i < d.getGenericParametersCount(); i++) {
					if (i != 0)
						s += ", ";
					s += prettyprint(d.getGenericParameters(i));
				}
				s += ">";
			}
			if (d.getParentsCount() > 0) {
				s += " extends ";
				for (int i = 0; i < d.getParentsCount(); i++) {
					if (i != 0)
						s += ", ";
					s += prettyprint(d.getParents(i));
				}
			}
			s += " {\n";
			break;
		case ANONYMOUS:
			break;
		case ENUM:
			s += "enum " + d.getName();
			break;
		case ANNOTATION:
			s += "@interface class " + d.getName();
			if (d.getGenericParametersCount() > 0) {
				s += "<";
				for (int i = 0; i < d.getGenericParametersCount(); i++) {
					if (i != 0)
						s += ", ";
					s += prettyprint(d.getGenericParameters(i));
				}
				s += ">";
			}
			if (d.getParentsCount() > 0) {
				int i = 0;
				if (d.getParents(i).getKind() == TypeKind.CLASS)
					s += " extends " + prettyprint(d.getParents(i++));
				if (i < d.getParentsCount()) {
					s += " implements ";
					for (int j = i; i < d.getParentsCount(); i++) {
						if (i != j)
							s += ", ";
						s += prettyprint(d.getParents(i));
					}
				}
			}
			break;
		default:
		case CLASS:
			s += "class " + d.getName();
			if (d.getGenericParametersCount() > 0) {
				s += "<";
				for (int i = 0; i < d.getGenericParametersCount(); i++) {
					if (i != 0)
						s += ", ";
					s += prettyprint(d.getGenericParameters(i));
				}
				s += ">";
			}
			if (d.getParentsCount() > 0) {
				int i = 0;
				if (d.getParents(i).getKind() == TypeKind.CLASS)
					s += " extends " + prettyprint(d.getParents(i++));
				if (i < d.getParentsCount()) {
					s += " implements ";
					for (int j = i; i < d.getParentsCount(); i++) {
						if (i != j)
							s += ", ";
						s += prettyprint(d.getParents(i));
					}
				}
			}
			break;
		}

		s += " {\n";

		indent++;
		for (int i = 0; i < d.getFieldsCount(); i++) {
			s += indent() + prettyprint(d.getFieldsList().get(i));
			s += (!d.getFieldsList().get(i).hasVariableType() && i < d.getFieldsCount() - 1
					&& !d.getFieldsList().get(i + 1).hasVariableType()) ? ",\n" : ";\n";
		}
		for (final Method m : d.getMethodsList())
			s += m.getName().equals("<init>") ? prettyprint(m).replace(" <init>", d.getName()) : prettyprint(m);
		for (final Declaration d2 : d.getNestedDeclarationsList())
			s += prettyprint(d2);

		indent--;

		s += indent() + "}\n";

		return s;
	}

	@FunctionSpec(name = "prettyprint", returnType = "string", formalParameters = { "Type" })
	public static String prettyprint(final Type t) {
		if (t == null)
			return "";

		return t.getName();
	}

	@FunctionSpec(name = "prettyprint", returnType = "string", formalParameters = { "Method" })
	public static String prettyprint(final Method m) {
		if (m == null)
			return "";
		String s = indent() + prettyprint(m.getModifiersList());

		if (m.getGenericParametersCount() > 0) {
			s += "<";
			for (int i = 0; i < m.getGenericParametersCount(); i++) {
				if (i > 0)
					s += ", ";
				s += prettyprint(m.getGenericParameters(i));
			}
			s += "> ";
		}

		s += prettyprint(m.getReturnType()) + " " + m.getName() + "(";
		for (int i = 0; i < m.getArgumentsCount(); i++) {
			if (i > 0)
				s += ", ";
			s += prettyprint(m.getArguments(i));
		}
		s += ")";

		if (m.getExceptionTypesCount() > 0) {
			s += " throws";
			for (int i = 0; i < m.getExceptionTypesCount(); i++)
				s += " " + prettyprint(m.getExceptionTypes(i));
		}

		s += "\n";
		for (int i = 0; i < m.getStatementsCount(); i++)
			s += indent() + prettyprint(m.getStatements(i)) + "\n";

		return s;
	}

	@FunctionSpec(name = "prettyprint", returnType = "string", formalParameters = { "Variable" })
	public static String prettyprint(final Variable v) {
		if (v == null)
			return "";

		String s = "";
		if (v.getModifiersCount() > 0)
			s += prettyprint(v.getModifiersList());

		if (v.hasVariableType())
			s += prettyprint(v.getVariableType()) + " ";

		s += v.getName();

		if (v.getExpressionsCount() != 0)
			s += "(" + prettyprint(v.getExpressions(0)) + ")";

		if (v.hasInitializer())
			s += " = " + prettyprint(v.getInitializer());

		return s;
	}

	private static String prettyprint(final List<Modifier> mods) {
		String s = "";

		for (final Modifier m : mods)
			s += prettyprint(m) + " ";

		return s;
	}

	@FunctionSpec(name = "prettyprint", returnType = "string", formalParameters = { "Statement" })
	public static String prettyprint(final Statement stmt) {
		if (stmt == null)
			return "";

		String s = "";

		switch (stmt.getKind()) {
		case EMPTY:
			return ";";

		case BLOCK:
			s += "{\n";
			indent++;
			for (int i = 0; i < stmt.getStatementsCount(); i++)
				s += indent() + prettyprint(stmt.getStatements(i)) + "\n";
			indent--;
			s += indent() + "}";
			return s;

		case RETURN:
			s += "return";
			if (stmt.getExpressionsCount() > 0)
				s += " " + prettyprint(stmt.getExpressions(0));
			s += ";";
			return s;
		case BREAK:
			s += "break";
			if (stmt.getExpressionsCount() > 0)
				s += " " + prettyprint(stmt.getExpressions(0));
			s += ";";
			return s;
		case CONTINUE:
			s += "continue";
			if (stmt.getExpressionsCount() > 0)
				s += " " + prettyprint(stmt.getExpressions(0));
			s += ";";
			return s;

		case ASSERT:
			s += "assert ";
			s += prettyprint(stmt.getConditions(0));
			if (stmt.getExpressionsCount() > 0)
				s += " " + prettyprint(stmt.getExpressions(0));
			s += ";";
			return s;

		case LABEL:
			return prettyprint(stmt.getExpressions(0)) + ": " + prettyprint(stmt.getStatements(0));

		case CASE:
			return "case " + prettyprint(stmt.getExpressions(0)) + ":";

		case DEFAULT:
			return "default:";

		case EXPRESSION:
			return prettyprint(stmt.getExpressions(0)) + ";";

		case TYPEDECL:
			return prettyprint(stmt.getTypeDeclaration());

		case SYNCHRONIZED:
			s += "synchronized () {\n";
			indent++;
			for (int i = 0; i < stmt.getStatementsCount(); i++)
				s += indent() + prettyprint(stmt.getStatements(i)) + "\n";
			indent--;
			s += "}";
			return s;

		case CATCH:
			s += indent() + "catch (";
			s += prettyprint(stmt.getVariableDeclaration());
			s += ") {\n";
			indent++;
			for (int i = 0; i < stmt.getStatementsCount(); i++)
				s += indent() + prettyprint(stmt.getStatements(i)) + "\n";
			indent--;
			s += indent() + "}";
			return s;

		case FINALLY:
			s += indent() + "finally {\n";
			indent++;
			for (int i = 0; i < stmt.getStatementsCount(); i++)
				s += indent() + prettyprint(stmt.getStatements(i)) + "\n";
			indent--;
			s += indent() + "}";
			return s;

		case TRY:
			s += "try";
			if (stmt.getInitializationsCount() > 0) {
				s += "(";
				for (int i = 0; i < stmt.getInitializationsCount(); i++) {
					if (i > 0)
						s += ", ";
					s += prettyprint(stmt.getInitializations(i));
				}
				s += ")";
			}
			s += " ";
			for (int i = 0; i < stmt.getStatementsCount(); i++) {
				s += prettyprint(stmt.getStatements(i)) + "\n";
			}
			return s;

		case FOR:
			s += "for (";
			if (stmt.hasVariableDeclaration()) {
				s += prettyprint(stmt.getVariableDeclaration()) + " : " + prettyprint(stmt.getConditions(0));
			} else {
				for (int i = 0; i < stmt.getInitializationsCount(); i++) {
					if (i > 0)
						s += ", ";
					s += prettyprint(stmt.getInitializations(i));
				}
				String condition = stmt.getConditionsCount() == 1 ? prettyprint(stmt.getConditions(0)) : " ";
				s += "; " + condition + "; ";
				for (int i = 0; i < stmt.getUpdatesCount(); i++) {
					if (i > 0)
						s += ", ";
					s += prettyprint(stmt.getUpdates(i));
				}
			}
			s += ")\n";
			indent++;
			s += indent() + prettyprint(stmt.getStatements(0)) + "\n";
			indent--;
			return s;

		case FOREACH:
			s += "for (" + prettyprint(stmt.getVariableDeclaration()) + " : " + prettyprint(stmt.getExpressions(0))
					+ ")\n";
			s += indent() + prettyprint(stmt.getStatements(0));
			return s;

		case DO:
			s += "do\n";
			indent++;
			for (int i = 0; i < stmt.getStatementsCount(); i++)
				s += indent() + prettyprint(stmt.getStatements(i)) + "\n";
			indent--;
			s += indent() + "while (" + prettyprint(stmt.getConditions(0)) + ");";
			return s;

		case WHILE:
			s += "while (" + prettyprint(stmt.getConditions(0)) + ") {\n";
			indent++;
			for (int i = 0; i < stmt.getStatementsCount(); i++)
				s += indent() + prettyprint(stmt.getStatements(i)) + "\n";
			indent--;
			s += indent() + "}";
			return s;

		case IF:
			s += "if (" + prettyprint(stmt.getConditions(0)) + ")\n";
			indent++;
			s += indent() + prettyprint(stmt.getStatements(0)) + "\n";
			indent--;
			if (stmt.getStatementsCount() > 1) {
				s += indent() + "else\n";
				indent++;
				s += indent() + prettyprint(stmt.getStatements(1)) + "\n";
				indent--;
			}
			return s;

		case SWITCH:
			s += "switch (" + prettyprint(stmt.getExpressions(0)) + ") {\n";
			indent++;
			for (int i = 0; i < stmt.getStatementsCount(); i++)
				s += indent() + prettyprint(stmt.getStatements(i)) + "\n";
			indent--;
			s += indent() + "}";
			return s;

		case THROW:
			return "throw " + prettyprint(stmt.getExpressions(0)) + ";";

		default:
			return s;
		}
	}

	@FunctionSpec(name = "prettyprint", returnType = "string", formalParameters = { "Expression" })
	public static String prettyprint(final Expression e) {
		if (e == null)
			return "";

		String s = "";

		switch (e.getKind()) {
		case OP_ADD:
			if (e.getExpressionsCount() == 1)
				return ppPrefix("+", e);
			return ppInfix("+", e.getExpressionsList());
		case OP_SUB:
			if (e.getExpressionsCount() == 1)
				return ppPrefix("-", e);
			return ppInfix("-", e.getExpressionsList());

		case LOGICAL_AND:
			return "(" + ppInfix("&&", e.getExpressionsList()) + ")";
		case LOGICAL_OR:
			return "(" + ppInfix("||", e.getExpressionsList()) + ")";

		case EQ:
			return ppInfix("==", e.getExpressionsList());
		case NEQ:
			return ppInfix("!=", e.getExpressionsList());
		case LT:
			return ppInfix("<", e.getExpressionsList());
		case GT:
			return ppInfix(">", e.getExpressionsList());
		case LTEQ:
			return ppInfix("<=", e.getExpressionsList());
		case GTEQ:
			return ppInfix(">=", e.getExpressionsList());
		case OP_DIV:
			return ppInfix("/", e.getExpressionsList());
		case OP_MULT:
			return ppInfix("*", e.getExpressionsList());
		case OP_MOD:
			return ppInfix("%", e.getExpressionsList());
		case BIT_AND:
			return ppInfix("&", e.getExpressionsList());
		case BIT_OR:
			return ppInfix("|", e.getExpressionsList());
		case BIT_XOR:
			return ppInfix("^", e.getExpressionsList());
		case BIT_LSHIFT:
			return ppInfix("<<", e.getExpressionsList());
		case BIT_RSHIFT:
			return ppInfix(">>", e.getExpressionsList());
		case BIT_UNSIGNEDRSHIFT:
			return ppInfix(">>>", e.getExpressionsList());
		case ASSIGN:
			return ppInfix("=", e.getExpressionsList());
		case ASSIGN_ADD:
			return ppInfix("+=", e.getExpressionsList());
		case ASSIGN_SUB:
			return ppInfix("-=", e.getExpressionsList());
		case ASSIGN_MULT:
			return ppInfix("*=", e.getExpressionsList());
		case ASSIGN_DIV:
			return ppInfix("/=", e.getExpressionsList());
		case ASSIGN_MOD:
			return ppInfix("%=", e.getExpressionsList());
		case ASSIGN_BITXOR:
			return ppInfix("^=", e.getExpressionsList());
		case ASSIGN_BITAND:
			return ppInfix("&=", e.getExpressionsList());
		case ASSIGN_BITOR:
			return ppInfix("|=", e.getExpressionsList());
		case ASSIGN_LSHIFT:
			return ppInfix("<<=", e.getExpressionsList());
		case ASSIGN_RSHIFT:
			return ppInfix(">>=", e.getExpressionsList());
		case ASSIGN_UNSIGNEDRSHIFT:
			return ppInfix(">>>=", e.getExpressionsList());

		case LOGICAL_NOT:
			return ppPrefix("!", e);
		case BIT_NOT:
			return ppPrefix("~", e);

		case OP_DEC:
			if (e.getIsPostfix())
				return ppPostfix("--", e);
			return ppPrefix("--", e);
		case OP_INC:
			if (e.getIsPostfix())
				return ppPostfix("++", e);
			return ppPrefix("++", e);

		case PAREN:
			return "(" + prettyprint(e.getExpressions(0)) + ")";
		case LITERAL:
			return e.getLiteral();
		case VARACCESS:
			for (int i = 0; i < e.getExpressionsCount(); i++)
				s += prettyprint(e.getExpressions(i)) + ".";
			s += e.getVariable();
			return s;
		case CAST:
			return "(" + e.getNewType().getName() + ")" + prettyprint(e.getExpressions(0));
		case CONDITIONAL:
			return prettyprint(e.getExpressions(0)) + " ? " + prettyprint(e.getExpressions(1)) + " : "
					+ prettyprint(e.getExpressions(2));
		case NULLCOALESCE:
			return prettyprint(e.getExpressions(0)) + " ?? " + prettyprint(e.getExpressions(1));

		case METHODCALL:
			for (int i = 0; i < e.getExpressionsCount(); i++)
				s += prettyprint(e.getExpressions(i)) + ".";
			if (e.getGenericParametersCount() > 0) {
				s += "<";
				for (int i = 0; i < e.getGenericParametersCount(); i++) {
					if (i > 0)
						s += ", ";
					s += prettyprint(e.getGenericParameters(i));
				}
				s += ">";
			}
			s += e.getMethod() + "(";
			for (int i = 0; i < e.getMethodArgsCount(); i++) {
				if (i > 0)
					s += ", ";
				s += prettyprint(e.getMethodArgs(i));
			}
			s += ")";
			return s;

		case TYPECOMPARE:
			return prettyprint(e.getExpressions(0)) + " instanceof " + prettyprint(e.getNewType());

		case NEWARRAY:
			s += "new ";
			String ty = prettyprint(e.getNewType());
			if (e.getExpressionsCount() == 1 && e.getExpressions(0).getKind().equals(ExpressionKind.ARRAYINIT))
				s += ty + prettyprint(e.getExpressions(0));
			else if (e.getExpressionsCount() == 1)
				s += ty.substring(0, ty.lastIndexOf(']')) + prettyprint(e.getExpressions(0)) + ']';
			return s;

		case NEW:
			s += "new ";
			s += prettyprint(e.getNewType());
			if (e.getGenericParametersCount() > 0) {
				s += "<";
				for (int i = 0; i < e.getGenericParametersCount(); i++) {
					if (i > 0)
						s += ", ";
					s += prettyprint(e.getGenericParameters(i));
				}
				s += ">";
			}
			s += "(";
			for (int i = 0; i < e.getMethodArgsCount(); i++) {
				if (i > 0)
					s += ", ";
				s += prettyprint(e.getMethodArgs(i));
			}
			s += ")";
			if (e.hasAnonDeclaration())
				s += prettyprint(e.getAnonDeclaration());
			return s;

		case ARRAYACCESS:
			return prettyprint(e.getExpressions(0)) + "[" + prettyprint(e.getExpressions(1)) + "]";

		case ARRAYINIT:
			s += "{";
			for (int i = 0; i < e.getExpressionsCount(); i++) {
				if (i > 0)
					s += ", ";
				s += prettyprint(e.getExpressions(i));
			}
			s += "}";
			return s;

		case ANNOTATION:
			return prettyprint(e.getAnnotation());

		case VARDECL:
			s += prettyprint(e.getVariableDecls(0).getModifiersList());
			s += prettyprint(e.getVariableDecls(0).getVariableType()) + " ";
			for (int i = 0; i < e.getVariableDeclsCount(); i++) {
				if (i > 0)
					s += ", ";
				s += e.getVariableDecls(i).getName();
				if (e.getVariableDecls(i).hasInitializer())
					s += " = " + prettyprint(e.getVariableDecls(i).getInitializer());
			}
			return s;

		// TODO
		case METHOD_REFERENCE:
			// TODO
		case LAMBDA:
			s += "(";
			for (int i = 0; i < e.getVariableDeclsCount(); i++) {
				if (i > 0)
					s += ", ";
				String type = prettyprint(e.getVariableDecls(i).getVariableType());
				if (!type.equals(""))
					s += type + " ";
				s += e.getVariableDecls(i).getName();
			}
			s += ") -> ";
			if (e.getStatementsCount() != 0)
				s += prettyprint(e.getStatements(0));
			if (e.getExpressionsCount() != 0)
				s += prettyprint(e.getExpressions(0));
		default:
			return s;
		}
	}

	@FunctionSpec(name = "prettyprint", returnType = "string", formalParameters = { "array of Expression" })
	public static String prettyprint(final Expression[] es) {
		String s = "";
		for (int i = 0; i < es.length; i++) {
			if (i > 0)
				s += ", ";
			s += prettyprint(es[i]);
		}
		return s;
	}

	private static String ppPrefix(final String op, final Expression e) {
		return op + prettyprint(e.getExpressions(0));
	}

	private static String ppPostfix(final String op, final Expression e) {
		return prettyprint(e.getExpressions(0)) + op;
	}

	private static String ppInfix(final String op, final List<Expression> exps) {
		StringBuilder s = new StringBuilder();

		s.append(prettyprint(exps.get(0)));
		for (int i = 1; i < exps.size(); i++) {
			s.append(" ");
			s.append(op);
			s.append(" ");
			s.append(prettyprint(exps.get(i)));
		}

		return s.toString();
	}

	@FunctionSpec(name = "prettyprint", returnType = "string", formalParameters = { "Modifier" })
	public static String prettyprint(final Modifier m) {
		if (m == null)
			return "";

		String s = "";

		switch (m.getKind()) {
		case OTHER:
			return m.getOther();

		case VISIBILITY:
			switch (m.getVisibility()) {
			case PUBLIC:
				return "public";
			case PRIVATE:
				return "private";
			case PROTECTED:
				return "protected";
			case NAMESPACE:
				return "namespace";
			default:
				return s;
			}

		case ANNOTATION:
			s = "@" + m.getAnnotationName();
			if (m.getAnnotationMembersCount() > 0)
				s += "(";
			for (int i = 0; i < m.getAnnotationMembersCount(); i++) {
				if (i > 0)
					s += ", ";
				s += m.getAnnotationMembers(i) + " = " + prettyprint(m.getAnnotationValues(i));
			}
			if (m.getAnnotationMembersCount() > 0)
				s += ")";
			return s;

		case FINAL:
			return "final";
		case STATIC:
			return "static";
		case SYNCHRONIZED:
			return "synchronized";
		case ABSTRACT:
			return "abstract";

		default:
			return s;
		}
	}

	/**
	 * Converts a string expression into an AST.
	 *
	 * @param s the string to parse/convert
	 * @return the AST representation of the string
	 */
	@FunctionSpec(name = "parseexpression", returnType = "Expression", formalParameters = { "string" })
	public static Expression parseexpression(final String s) {
		final ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_EXPRESSION);
		parser.setSource(s.toCharArray());

		@SuppressWarnings("rawtypes")
		final Map options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
		parser.setCompilerOptions(options);

		try {
			final org.eclipse.jdt.core.dom.Expression e = (org.eclipse.jdt.core.dom.Expression) parser.createAST(null);
			final JavaVisitor visitor = new JavaVisitor(s);
			e.accept(visitor);
			return visitor.getExpression();
		} catch (final Exception e) {
			// do nothing
		}

		final Expression.Builder eb = Expression.newBuilder();
		eb.setKind(Expression.ExpressionKind.OTHER);
		return eb.build();
	}

	/**
	 * Converts a string into an AST.
	 *
	 * @param s the string to parse/convert
	 * @return the AST representation of the string
	 */
	@FunctionSpec(name = "parse", returnType = "ASTRoot", formalParameters = { "string" })
	public static ASTRoot parse(final String s) {
		final ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(s.toCharArray());

		@SuppressWarnings("rawtypes")
		final Map options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
		parser.setCompilerOptions(options);

		final ASTRoot.Builder ast = ASTRoot.newBuilder();
		try {
			final org.eclipse.jdt.core.dom.CompilationUnit cu = (org.eclipse.jdt.core.dom.CompilationUnit) parser
					.createAST(null);
			final JavaErrorCheckVisitor errorCheck = new JavaErrorCheckVisitor();
			cu.accept(errorCheck);

			if (!errorCheck.hasError) {
				final JavaVisitor visitor = new JavaVisitor(s);
				ast.addNamespaces(visitor.getNamespaces(cu));
			}
		} catch (final Exception e) {
			// do nothing
		}

		return ast.build();
	}
}
