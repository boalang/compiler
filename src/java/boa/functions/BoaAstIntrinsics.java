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

import java.io.IOException;
import java.util.HashMap;
import java.util.Stack;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.InvalidProtocolBufferException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper.Context;

import boa.datagen.DefaultProperties;
import boa.functions.langmode.JavaLangMode;
import boa.functions.langmode.LangMode;
import boa.types.Ast.ASTRoot;
import boa.types.Ast.CommentsRoot;
import boa.types.Ast.Declaration;
import boa.types.Ast.Expression;
import boa.types.Ast.Method;
import boa.types.Ast.Modifier;
import boa.types.Ast.Namespace;
import boa.types.Ast.Statement;
import boa.types.Ast.Type;
import boa.types.Ast.Variable;
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
	private static MapFile.Reader map;
	private static MapFile.Reader commentsMap;
	private static MapFile.Reader issuesMap;

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

	private static MapFile.Reader commitMap;

	public static enum COMMITCOUNTER {
		GETS_ATTEMPTED,
		GETS_SUCCEED,
		GETS_FAILED,
		GETS_FAIL_MISSING,
		GETS_FAIL_BADPROTOBUF,
		GETS_FAIL_BADLOC,
	};

	public static enum ASTCOUNTER {
		GETS_ATTEMPTED,
		GETS_SUCCEED,
		GETS_FAILED,
		GETS_FAIL_MISSING,
		GETS_FAIL_BADPROTOBUF,
		GETS_FAIL_BADLOC,
	};

	@FunctionSpec(name = "url", returnType = "string", formalParameters = { "ChangedFile" })
	public static String changedfileToString(final ChangedFile f) {
		return f.getKey() + "!!" + f.getName();
	}

	private static final ASTRoot emptyAst = ASTRoot.newBuilder().build();
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
	public static ASTRoot getast(final ChangedFile f) {
		if (!f.getAst())
			return emptyAst;

		context.getCounter(ASTCOUNTER.GETS_ATTEMPTED).increment(1);

		if (map == null)
			openMap();

		try {
			final BytesWritable value = new BytesWritable();
			if (map.get(new LongWritable(f.getKey()), value) == null) {
				context.getCounter(ASTCOUNTER.GETS_FAIL_MISSING).increment(1);
			} else {
				final CodedInputStream _stream = CodedInputStream.newInstance(value.getBytes(), 0, value.getLength());
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

		System.err.println("error with ast: " + f.getKey() + " from " + f.getName());
		context.getCounter(ASTCOUNTER.GETS_FAILED).increment(1);
		return emptyAst;
	}

	@SuppressWarnings("unchecked")
	static Revision getRevision(long key) {
		context.getCounter(COMMITCOUNTER.GETS_ATTEMPTED).increment(1);

		if (commitMap == null)
			openCommitMap();

		try {
			final BytesWritable value = new BytesWritable();
			if (commitMap.get(new LongWritable(key), value) == null) {
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
		if (kind != ChangedFile.FileKind.SOURCE_JAVA_ERROR
				&& kind != ChangedFile.FileKind.SOURCE_JAVA_JLS2
				&& kind != ChangedFile.FileKind.SOURCE_JAVA_JLS3
				&& kind != ChangedFile.FileKind.SOURCE_JAVA_JLS4
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

	private static void openMap() {
		try {
			final Configuration conf = context.getConfiguration();
			final FileSystem fs;
			final Path p;
			if (DefaultProperties.localDataPath != null) {
				p = new Path(DefaultProperties.localDataPath, "ast");
				fs = FileSystem.getLocal(conf);
			} else {
				p = new Path(
					context.getConfiguration().get("fs.default.name", "hdfs://boa-njt/"),
					new Path(
						conf.get("boa.ast.dir", conf.get("boa.input.dir", "repcache/live")),
						new Path("ast")
					)
				);
				fs = FileSystem.get(conf);
			}
			map = new MapFile.Reader(fs, p.toString(), conf);
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
				p = new Path(
					context.getConfiguration().get("fs.default.name", "hdfs://boa-njt/"),
					new Path(
						conf.get("boa.comments.dir", conf.get("boa.input.dir", "repcache/live")),
						new Path("comments")
					)
				);
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
				p = new Path(
					context.getConfiguration().get("fs.default.name", "hdfs://boa-njt/"),
					new Path(
						conf.get("boa.issues.dir", conf.get("boa.input.dir", "repcache/live")),
						new Path("issues")
					)
				);
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
				p = new Path(context.getConfiguration().get("fs.default.name", "hdfs://boa-njt/"),
						new Path(conf.get("boa.ast.dir", conf.get("boa.input.dir", "repcache/live")), new Path("commit")));
				fs = FileSystem.get(conf);
			}
			commitMap = new MapFile.Reader(fs, p.toString(), conf);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("rawtypes")
	public static void cleanup(final Context context) {
		closeMap();
		closeCommentMap();
		closeIssuesMap();
		closeCommitMap();
	}

	private static void closeMap() {
		if (map != null)
			try {
				map.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		map = null;
	}

	private static void closeCommentMap() {
		if (commentsMap != null)
			try {
				commentsMap.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		commentsMap = null;
	}

	private static void closeIssuesMap() {
		if (issuesMap != null)
			try {
				issuesMap.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		issuesMap = null;
	}

	private static void closeCommitMap() {
		if (commitMap != null)
			try {
				commitMap.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		commitMap = null;
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

	@FunctionSpec(name = "collect_annotations", returnType = "map[string] of int", formalParameters = { "ASTRoot", "map[string] of int" })
	public static HashMap<String, Long> collect_annotations(final ASTRoot f, final HashMap<String, Long> map) throws Exception {
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
			try {
				parseGenericType(BoaAstIntrinsics.type_name(node.getName()).trim(), map);
			} catch (final StackOverflowError e) {
				System.err.println("STACK ERR: " + node.getName() + " -> " + BoaAstIntrinsics.type_name(node.getName()).trim());
			}
			*/
			return true;
		}
	}
	private static GenericsCollectingVisitor genericsCollectingVisitor = new GenericsCollectingVisitor();

	@FunctionSpec(name = "collect_generic_types", returnType = "map[string] of int", formalParameters = { "ASTRoot", "map[string] of int" })
	public static HashMap<String, Long> collect_generic_types(final ASTRoot f, final HashMap<String, Long> map) throws Exception {
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

	@FunctionSpec(name = "getastcount", returnType = "int", formalParameters = { "ChangedFile" })
	public static long getastcount(final ChangedFile n) {
		try {
			return lenVisitor.getCount(n);
		} catch (Exception e) {
			return 0;
		}
	}

	@FunctionSpec(name = "debug", returnType = "string", formalParameters = { "string" })
	public static String debug(final String s) {
		System.err.println(s);
		return null;
	}

	private static final LangMode javaLang = new JavaLangMode();

	private static ChangedFile.FileKind curLang = ChangedFile.FileKind.SOURCE_JAVA_JLS8;
	private static LangMode lang = javaLang;

	@FunctionSpec(name = "getlang", returnType = "FileKind", formalParameters = {})
	public static ChangedFile.FileKind getlang() {
		return curLang;
	}

	@FunctionSpec(name = "setlang", returnType = "any", formalParameters = { "FileKind" })
	public static void setlang(final ChangedFile.FileKind l) {
		if (curLang == l) return;

		curLang = l;

		switch (curLang) {
		default:
			lang = javaLang;
			break;
		}
	}

	@FunctionSpec(name = "type_name", returnType = "string", formalParameters = { "string" })
	public static String type_name(final String s) {
		return lang.type_name(s);
	}

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
	 * DecimalNumeral:
	 * 	[0-9] [lL]?
	 * 	[1-9] [0-9] ([0-9_]* [0-9])? [lL]?
	 * 	[1-9] [_]+ [0-9] ([0-9_]* [0-9])? [lL]?
	 *
	 * HexNumeral:
	 * 	0 [xX] [0-9a-fA-F] ([0-9a-fA-F_]* [0-9a-fA-F])? [lL]?
	 *
	 * OctalNumeral:
	 * 	0 [_]* [0-7] ([0-7_]* [0-7])? [lL]?
	 *
	 * BinaryNumeral:
	 * 	0 [bB] [01] ([01_]* [01])? [lL]?
	 *
	 * If any of these match, it returns <code>true</code>.  Otherwise it
	 * returns <code>false</code>.
	 *
	 * @param e the expression to test
	 * @return true if the expression is an integer literal, otherwise false
	 */
	@FunctionSpec(name = "isintlit", returnType = "bool", formalParameters = { "Expression" })
	public static boolean isIntLit(final Expression e) throws Exception {
		return lang.isIntLit(e);
	}

	/**
	 * Returns <code>true</code> if the expression <code>e</code> is of kind
	 * <code>LITERAL</code> and is a float literal.
	 *
	 * The test is a simplified grammar, based on the one from:
	 * https://docs.oracle.com/javase/specs/jls/se8/html/jls-3.html#jls-3.10
	 *
	 * DecimalFloatingPointLiteral:
	 *  [0-9] ([0-9_]* [0-9])? \\. ([0-9] ([0-9_]* [0-9])?)? ([eE] [+-]? [0-9] ([0-9_]* [0-9])?)? [fFdD]?
	 *  \\. [0-9] ([0-9_]* [0-9])? ([eE] [+-]? [0-9] ([0-9_]* [0-9])?)? [fFdD]?
	 *  [0-9] ([0-9_]* [0-9])? [eE] [+-]? [0-9] ([0-9_]* [0-9])? [fFdD]?
	 *  [0-9] ([0-9_]* [0-9])? ([eE] [+-]? [0-9] ([0-9_]* [0-9])?)? [fFdD]
	 *
	 * HexadecimalFloatingPointLiteral:
	 *  0 [Xx] [0-9a-fA-F] ([0-9a-fA-F_]* [0-9a-fA-F])? \\.? [pP] [+-]? [0-9] ([0-9_]* [0-9])? [fFdD]?
	 *  0 [Xx] ([0-9a-fA-F] ([0-9a-fA-F_]* [0-9a-fA-F])?)? \\. [0-9a-fA-F] ([0-9a-fA-F_]* [0-9a-fA-F])? [pP] [+-]? [0-9] ([0-9_]* [0-9])? [fFdD]?
	 *
	 * @param e the expression to test
	 * @return true if the expression is a char literal, otherwise false
	 */
	@FunctionSpec(name = "isfloatlit", returnType = "bool", formalParameters = { "Expression" })
	public static boolean isFloatLit(final Expression e) throws Exception {
		return lang.isFloatLit(e);
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
		return lang.isCharLit(e);
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
		return lang.isStringLit(e);
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
		return lang.isTypeLit(e);
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
		return lang.isBoolLit(e);
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
		return lang.isNullLit(e);
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
		return lang.isLiteral(e, lit);
	}

	@FunctionSpec(name = "prettyprint", returnType = "string", formalParameters = { "ASTRoot" })
	public static String prettyprint(final ASTRoot r) {
		return lang.prettyprint(r);
	}

	@FunctionSpec(name = "prettyprint", returnType = "string", formalParameters = { "Namespace" })
	public static String prettyprint(final Namespace n) {
		return lang.prettyprint(n);
	}

	@FunctionSpec(name = "prettyprint", returnType = "string", formalParameters = { "Declaration" })
	public static String prettyprint(final Declaration d) {
		return lang.prettyprint(d);
	}

	@FunctionSpec(name = "prettyprint", returnType = "string", formalParameters = { "Type" })
	public static String prettyprint(final Type t) {
		return lang.prettyprint(t);
	}

	@FunctionSpec(name = "prettyprint", returnType = "string", formalParameters = { "Method" })
	public static String prettyprint(final Method m) {
		return lang.prettyprint(m);
	}

	@FunctionSpec(name = "prettyprint", returnType = "string", formalParameters = { "Variable" })
	public static String prettyprint(final Variable v) {
		return lang.prettyprint(v);
	}

	@FunctionSpec(name = "prettyprint", returnType = "string", formalParameters = { "Statement" })
	public static String prettyprint(final Statement stmt) {
		return lang.prettyprint(stmt);
	}

	@FunctionSpec(name = "prettyprint", returnType = "string", formalParameters = { "Expression" })
	public static String prettyprint(final Expression e) {
		return lang.prettyprint(e);
	}

	@FunctionSpec(name = "prettyprint", returnType = "string", formalParameters = { "Modifier" })
	public static String prettyprint(final Modifier m) {
		return lang.prettyprint(m);
	}

	/**
	 * Converts a string expression into an AST.
	 *
	 * @param s the string to parse/convert
	 * @return the AST representation of the string
	 */
	@FunctionSpec(name = "parseexpression", returnType = "Expression", formalParameters = { "string" })
	public static Expression parseexpression(final String s) {
		return lang.parseexpression(s);
	}

	/**
	 * Converts a string into an AST.
	 *
	 * @param s the string to parse/convert
	 * @return the AST representation of the string
	 */
	@FunctionSpec(name = "parse", returnType = "ASTRoot", formalParameters = { "string" })
	public static ASTRoot parse(final String s) {
		return lang.parse(s);
	}
}
