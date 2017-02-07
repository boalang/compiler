/*
 * Copyright 2014, Hridesh Rajan, Robert Dyer, 
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
package boa.functions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Stack;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper.Context;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.InvalidProtocolBufferException;

import boa.datagen.DefaultProperties;
import boa.types.Ast.*;
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
	private static Context context;
	private static MapFile.Reader map, commentsMap, issuesMap;

	public static enum AST_COUNTER {
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
		// since we know only certain kinds have ASTs, filter before looking up
		final ChangedFile.FileKind kind = f.getKind();
		if (kind != ChangedFile.FileKind.SOURCE_JAVA_ERROR
				&& kind != ChangedFile.FileKind.SOURCE_JAVA_JLS2
				&& kind != ChangedFile.FileKind.SOURCE_JAVA_JLS3
				&& kind != ChangedFile.FileKind.SOURCE_JAVA_JLS4
				&& kind != ChangedFile.FileKind.SOURCE_JAVA_JLS8)
			return emptyAst;

		context.getCounter(AST_COUNTER.GETS_ATTEMPTED).increment(1);

		final String rowName = f.getKey() + "!!" + f.getName();

		if (map == null)
			openMap();

		try {
			final BytesWritable value = new BytesWritable();
			if (map.get(new Text(rowName), value) == null) {
				context.getCounter(AST_COUNTER.GETS_FAIL_MISSING).increment(1);
			} else {
				final CodedInputStream _stream = CodedInputStream.newInstance(value.getBytes(), 0, value.getLength());
				// defaults to 64, really big ASTs require more
				_stream.setRecursionLimit(Integer.MAX_VALUE);
				final ASTRoot root = ASTRoot.parseFrom(_stream);
				context.getCounter(AST_COUNTER.GETS_SUCCEED).increment(1);
				return root;
			}
		} catch (final InvalidProtocolBufferException e) {
			e.printStackTrace();
			context.getCounter(AST_COUNTER.GETS_FAIL_BADPROTOBUF).increment(1);
		} catch (final IOException e) {
			e.printStackTrace();
			context.getCounter(AST_COUNTER.GETS_FAIL_MISSING).increment(1);
		} catch (final RuntimeException e) {
			e.printStackTrace();
			context.getCounter(AST_COUNTER.GETS_FAIL_MISSING).increment(1);
		} catch (final Error e) {
			e.printStackTrace();
			context.getCounter(AST_COUNTER.GETS_FAIL_BADPROTOBUF).increment(1);
		}

		System.err.println("error with ast: " + rowName);
		context.getCounter(AST_COUNTER.GETS_FAILED).increment(1);
		return emptyAst;
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
		final Configuration conf = new Configuration();
		try {
			final FileSystem fs = FileSystem.get(conf);
			Path p = new Path("hdfs://boa-njt/",
								new Path(context.getConfiguration().get("boa.ast.dir", context.getConfiguration().get("boa.input.dir", "repcache/live")),
								new Path("ast")));
			if(DefaultProperties.localDataPath != null) {
				p = new Path(DefaultProperties.localDataPath);
			}
			map = new MapFile.Reader(fs, p.toString(), conf);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private static void openCommentMap() {
		final Configuration conf = new Configuration();
		try {
			final FileSystem fs = FileSystem.get(conf);
			Path p = new Path("hdfs://boa-njt/",
								new Path(context.getConfiguration().get("boa.comments.dir", context.getConfiguration().get("boa.input.dir", "repcache/live")),
								new Path("comments")));
			if(DefaultProperties.localDataPath != null) {
				p = new Path(DefaultProperties.localCommentPath);
			}
			commentsMap = new MapFile.Reader(fs, p.toString(), conf);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private static void openIssuesMap() {
		final Configuration conf = new Configuration();
		try {
			final FileSystem fs = FileSystem.get(conf);
			Path p = new Path("hdfs://boa-njt/",
								new Path(context.getConfiguration().get("boa.issues.dir", context.getConfiguration().get("boa.input.dir", "repcache/live")),
								new Path("issues")));
			if(DefaultProperties.localDataPath != null) {
				p = new Path(DefaultProperties.localIssuePath);
			}
			issuesMap = new MapFile.Reader(fs, p.toString(), conf);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("rawtypes")
	public static void cleanup(final Context context) {
		closeMap();
		closeCommentMap();
		closeIssuesMap();
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

	@FunctionSpec(name = "type_name", returnType = "string", formalParameters = { "string" })
	public static String type_name(final String s) {
		// first, normalize the string
		final String t = s.replaceAll("<\\s+", "<")
			.replaceAll(",\\s+", ", ")
			.replaceAll("\\s*>\\s*", ">")
			.replaceAll("\\s*&\\s*", " & ")
			.replaceAll("\\s*\\|\\s*", " | ");

		if (!t.contains("."))
			return t;

		/*
		 * Remove qualifiers from anywhere in the string...
		 * 
		 * SomeType                               =>  SomeType
		 * foo.SomeType                           =>  SomeType
		 * foo.bar.SomeType                       =>  SomeType
		 * SomeType<T>                            =>  SomeType<T>
		 * SomeType<T, S>                         =>  SomeType<T, S>
		 * SomeType<foo.bar.T, S>                 =>  SomeType<T, S>
		 * SomeType<T, foo.bar.S>                 =>  SomeType<T, S>
		 * foo.bar.SomeType<T, foo.bar.S<bar.Q>>  =>  SomeType<T, S<Q>>
		 * SomeType|foo.Bar                       =>  SomeType|Bar
		 * foo<T>.bar<T>                          =>  foo<T>.bar<T>
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

	@FunctionSpec(name = "getsnapshot", returnType = "array of ChangedFile", formalParameters = { "CodeRepository", "time", "string..." })
	public static ChangedFile[] getSnapshot(final CodeRepository cr, final long timestamp, final String... kinds) throws Exception {
		snapshot.initialize(timestamp, kinds).visit(cr);
		return snapshot.map.values().toArray(new ChangedFile[0]);
	}

	@FunctionSpec(name = "getsnapshot", returnType = "array of ChangedFile", formalParameters = { "CodeRepository", "string..." })
	public static ChangedFile[] getSnapshot(final CodeRepository cr, final String... kinds) throws Exception {
		return getSnapshot(cr, Long.MAX_VALUE, kinds);
	}

	@FunctionSpec(name = "getsnapshot", returnType = "array of ChangedFile", formalParameters = { "CodeRepository", "time" })
	public static ChangedFile[] getSnapshot(final CodeRepository cr, final long timestamp) throws Exception {
		return getSnapshot(cr, timestamp, new String[0]);
	}

	@FunctionSpec(name = "getsnapshot", returnType = "array of ChangedFile", formalParameters = { "CodeRepository" })
	public static ChangedFile[] getSnapshot(final CodeRepository cr) throws Exception {
		return getSnapshot(cr, Long.MAX_VALUE, new String[0]);
	}

	@FunctionSpec(name = "isliteral", returnType = "bool", formalParameters = { "Expression", "string" })
	public static boolean isLiteral(final Expression e, final String lit) throws Exception {
		return e.getKind() == Expression.ExpressionKind.LITERAL && e.hasLiteral() && e.getLiteral().equals(lit);
	}

	//////////////////////////////
	// Collect Annotations Used //
	//////////////////////////////

	private static class AnnotationCollectingVisitor extends BoaCollectingVisitor<String,Long> {
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
	public static HashMap<String,Long> collect_annotations(final ASTRoot f, final HashMap<String,Long> map) throws Exception {
		annotationCollectingVisitor.initialize(map).visit(f);
		return annotationCollectingVisitor.map;
	}

	///////////////////////////
	// Collect Generics Used //
	///////////////////////////

	private static class GenericsCollectingVisitor extends BoaCollectingVisitor<String,Long> {
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
	public static HashMap<String,Long> collect_generic_types(final ASTRoot f, final HashMap<String,Long> map) throws Exception {
		genericsCollectingVisitor.initialize(map).visit(f);
		return genericsCollectingVisitor.map;
	}

	private static void parseGenericType(final String name, final HashMap<String,Long> counts) {
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
			default:
				break;
			}
	}

	private static void foundType(final String name, final HashMap<String,Long> counts) {
		final String type = name.endsWith("...") ? name.substring(0, name.length() - 3).trim() : name.trim();
		final long count = counts.containsKey(type) ? counts.get(type) : 0;
		counts.put(type, count + 1);

		String rawType = type.substring(0, type.indexOf("<")).trim();
		if (!type.endsWith(">"))
			rawType += type.substring(type.lastIndexOf(">") + 1).trim();
		final long rawCount = counts.containsKey(rawType) ? counts.get(rawType) : 0;
		counts.put(rawType, rawCount + 1);
	}
}
