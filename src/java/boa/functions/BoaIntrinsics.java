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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.mapreduce.Mapper.Context;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.InvalidProtocolBufferException;

import boa.types.Code.CodeRepository;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.ChangeKind;
import boa.types.Shared.Person;
import boa.types.Toplevel.Project;

/**
 * Boa domain-specific functions.
 * 
 * @author rdyer
 */
public class BoaIntrinsics {
	private final static String[] fixingRegex = {
		"\\bfix(s|es|ing|ed)?\\b",
		"\\b(error|bug|issue)(s)?\\b",
		//"\\b(bug|issue|fix)(s)?\\b\\s*(#)?\\s*[0-9]+",
		//"\\b(bug|issue|fix)\\b\\s*id(s)?\\s*(=)?\\s*[0-9]+"
	};

	public static enum COMMITCOUNTER {
		GETS_ATTEMPTED,
		GETS_SUCCEED,
		GETS_FAILED,
		GETS_FAIL_MISSING,
		GETS_FAIL_BADPROTOBUF,
		GETS_FAIL_BADLOC,
	};

	private static final Revision emptyRevision;
	
	private static MapFile.Reader commitMap;

	private final static List<Matcher> fixingMatchers = new ArrayList<Matcher>();

	static {
		for (final String s : fixingRegex)
			fixingMatchers.add(Pattern.compile(s).matcher(""));
		Revision.Builder rb = Revision.newBuilder();
		rb.setCommitDate(0);
		Person.Builder pb = Person.newBuilder();
		pb.setUsername("");
		rb.setCommitter(pb);
		rb.setId("");
		rb.setLog("");
		emptyRevision = rb.build();
	}

	private static void openCommitMap() {
		try {
			final Configuration conf = BoaAstIntrinsics.context.getConfiguration();
			final FileSystem fs;
			final Path p = new Path(BoaAstIntrinsics.context.getConfiguration().get("fs.default.name", "hdfs://boa-njt/"),
						new Path(conf.get("boa.ast.dir", conf.get("boa.input.dir", "repcache/live")), new Path("commit")));
			fs = FileSystem.get(conf);
			commitMap = new MapFile.Reader(fs, p.toString(), conf);
		} catch (final Exception e) {
			e.printStackTrace();
		}
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

	@SuppressWarnings("rawtypes")
	public static void cleanup(final Context context) {
		closeCommitMap();
	}

	private static int getRevisionIndex(final CodeRepository cr, final long timestamp) {
		Revision.Builder rb = Revision.newBuilder();
		Person.Builder pb = Person.newBuilder();
		pb.setUsername("");
		rb.setCommitDate(timestamp);
		rb.setCommitter(pb);
		rb.setId("");
		rb.setLog("");
		int index = Collections.binarySearch(cr.getRevisionsList(), rb.build(), new Comparator<Revision>() {
			@Override
			public int compare(Revision r1, Revision r2) {
				return (int) (r1.getCommitDate() - r2.getCommitDate());
			}
		});
		if (index < 0)
			index = -index - 1;
		return index;
	}
	
	private static int getRevisionIndex(final CodeRepository cr, final String id) {
		for (int i = 0; i < cr.getRevisionsCount(); i++) {
			if (cr.getRevisions(i).getId().equals(id))
				return i;
		}
		return -1;
	}
	
	@FunctionSpec(name = "getrevision", returnType = "Revision", formalParameters = { "CodeRepository", "int" })
	public static Revision getRevision(final CodeRepository cr, final int index) {
		if (cr.getRevisionKeysCount() > 0) {
			long key = cr.getRevisionKeys(index);
			return getRevision(key);
		}
		return cr.getRevisions(index);
	}

	private static Revision getRevision(long key) {
		BoaAstIntrinsics.context.getCounter(COMMITCOUNTER.GETS_ATTEMPTED).increment(1);
		
		if (commitMap == null)
			openCommitMap();
		
		try {
			final BytesWritable value = new BytesWritable();
			if (commitMap.get(new LongWritable(key), value) == null) {
				BoaAstIntrinsics.context.getCounter(COMMITCOUNTER.GETS_FAIL_MISSING).increment(1);
			} else {
				final CodedInputStream _stream = CodedInputStream.newInstance(value.getBytes(), 0, value.getLength());
				// defaults to 64, really big ASTs require more
				_stream.setRecursionLimit(Integer.MAX_VALUE);
				final Revision root = Revision.parseFrom(_stream);
				BoaAstIntrinsics.context.getCounter(COMMITCOUNTER.GETS_SUCCEED).increment(1);
				return root;
			}
		} catch (final InvalidProtocolBufferException e) {
			e.printStackTrace();
			BoaAstIntrinsics.context.getCounter(COMMITCOUNTER.GETS_FAIL_BADPROTOBUF).increment(1);
		} catch (final IOException e) {
			e.printStackTrace();
			BoaAstIntrinsics.context.getCounter(COMMITCOUNTER.GETS_FAIL_MISSING).increment(1);
		} catch (final RuntimeException e) {
			e.printStackTrace();
			BoaAstIntrinsics.context.getCounter(COMMITCOUNTER.GETS_FAIL_MISSING).increment(1);
		} catch (final Error e) {
			e.printStackTrace();
			BoaAstIntrinsics.context.getCounter(COMMITCOUNTER.GETS_FAIL_BADPROTOBUF).increment(1);
		}

		System.err.println("error with revision: " + key);
		BoaAstIntrinsics.context.getCounter(COMMITCOUNTER.GETS_FAILED).increment(1);
		return emptyRevision;
	}

	@FunctionSpec(name = "getsnapshot", returnType = "array of ChangedFile", formalParameters = { "CodeRepository", "time", "string..." })
	public static ChangedFile[] getSnapshot(final CodeRepository cr, final long timestamp, final String... kinds) throws Exception {
//		snapshot.initialize(timestamp, kinds).visit(cr);
//		return snapshot.map.values().toArray(new ChangedFile[0]);
		if (cr.getRevisionsCount() == 0)
			return new ChangedFile[0];
		int revisionOffset = getRevisionIndex(cr, timestamp);
		return getSnapshot(cr, revisionOffset, kinds);
	}

	private static ChangedFile[] getSnapshot(final CodeRepository cr, final int commitOffset, final String... kinds) {
		List<ChangedFile> snapshot = new LinkedList<ChangedFile>();
		Set<String> adds = new HashSet<String>(), dels = new HashSet<String>(); 
		PriorityQueue<Integer> pq = new PriorityQueue<Integer>(100, new Comparator<Integer>() {
			@Override
			public int compare(Integer i1, Integer i2) {
				return i2 - i1;
			}
		});
		List<Revision> revisions = cr.getRevisionsList();
		Set<Integer> queuedCommitIds = new HashSet<Integer>();
		pq.offer(commitOffset);
		queuedCommitIds.add(commitOffset);
		while (!pq.isEmpty()) {
			int offset = pq.poll();
			Revision commit = revisions.get(offset);
			for (ChangedFile cf : commit.getFilesList()) {
				ChangeKind ck = cf.getChange();
				switch (ck) {
				case ADDED:
					if (!adds.contains(cf.getName()) && !dels.contains(cf.getName())) {
						adds.add(cf.getName());
						if (isIncluded(cf, kinds))
							snapshot.add(cf);
					}
					break;
				case COPIED:
					if (!adds.contains(cf.getName()) && !dels.contains(cf.getName())) {
						adds.add(cf.getName());
						if (isIncluded(cf, kinds))
							snapshot.add(cf);
					}
					break;
				case DELETED:
					if (!adds.contains(cf.getName()) && !dels.contains(cf.getName()))
						dels.add(cf.getName());
					break;
				case MERGED:
					if (!adds.contains(cf.getName()) && !dels.contains(cf.getName())) {
						adds.add(cf.getName());
						if (isIncluded(cf, kinds))
							snapshot.add(cf);
					}
					for (int i = 0; i < cf.getChangesCount(); i++) {
						if (cf.getChanges(i) != ChangeKind.ADDED) {
							ChangeKind pck = cf.getChanges(i);
//							ChangedFile pcf = revisions.get(cf.getPreviousVersions(i)).getFiles(cf.getPreviousIndices(i));
//							String name = pcf.getName();
							String name = cf.getPreviousNames(i);
							if (name.isEmpty())
								name = cf.getName();
							if (!adds.contains(name) && !dels.contains(name) && (pck == ChangeKind.DELETED || pck == ChangeKind.RENAMED))
								dels.add(name);
						}
					}
					break;
				case RENAMED:
					if (!adds.contains(cf.getName()) && !dels.contains(cf.getName())) {
						adds.add(cf.getName());
						if (isIncluded(cf, kinds))
							snapshot.add(cf);
					}
					for (int i = 0; i < cf.getChangesCount(); i++) {
//						ChangedFile pcf = revisions.get(cf.getPreviousVersions(i)).getFiles(cf.getPreviousIndices(i));
//						String name = pcf.getName();
						String name = cf.getPreviousNames(i);
						if (!adds.contains(name) && !dels.contains(name))
							dels.add(name);
					}
					break;
				default:
					if (!adds.contains(cf.getName()) && !dels.contains(cf.getName())) {
						adds.add(cf.getName());
						if (isIncluded(cf, kinds))
							snapshot.add(cf);
					}
					break;
				}
			}
			for (int p : commit.getParentsList()) {
				if (!queuedCommitIds.contains(p)) {
					pq.offer(p);
					queuedCommitIds.add(p);
				}
			}
		}
		return snapshot.toArray(new ChangedFile[0]);
	}

	private static boolean isIncluded(ChangedFile cf, String[] kinds) {
		if (kinds == null || kinds.length == 0)
			return true;
		final String kindName = cf.getKind().name();
		for (final String kind : kinds)
			if (kindName.startsWith(kind))
				return true;
		return false;
	}
	
	@FunctionSpec(name = "getsnapshot", returnType = "array of ChangedFile", formalParameters = { "CodeRepository", "string" })
	public static ChangedFile[] getSnapshot(final CodeRepository cr, final String id) {
		return getSnapshot(cr, id, new String[0]);
	}
	
	@FunctionSpec(name = "getsnapshot", returnType = "array of ChangedFile", formalParameters = { "CodeRepository", "string", "string..." })
	public static ChangedFile[] getSnapshot(final CodeRepository cr, final String id, final String... kinds) {
		if (cr.getRevisionsCount() == 0)
			return new ChangedFile[0];
		int revisionOffset = getRevisionIndex(cr, id);
		return getSnapshot(cr, revisionOffset, kinds);
	}

	@FunctionSpec(name = "getsnapshot", returnType = "array of ChangedFile", formalParameters = { "CodeRepository", "string..." })
	public static ChangedFile[] getSnapshot(final CodeRepository cr, final String... kinds) throws Exception {
//		return getSnapshot(cr, Long.MAX_VALUE, kinds);
		List<ChangedFile> files = new ArrayList<ChangedFile>();
		for (ChangedFile file : cr.getHeadSnapshotList()) {
			if (isIncluded(file, kinds))
				files.add(file);
		}
		return files.toArray(new ChangedFile[0]);
	}

	@FunctionSpec(name = "getsnapshot", returnType = "array of ChangedFile", formalParameters = { "CodeRepository", "time" })
	public static ChangedFile[] getSnapshot(final CodeRepository cr, final long timestamp) throws Exception {
		return getSnapshot(cr, timestamp, new String[0]);
	}

	@FunctionSpec(name = "getsnapshot", returnType = "array of ChangedFile", formalParameters = { "CodeRepository" })
	public static ChangedFile[] getSnapshot(final CodeRepository cr) throws Exception {
//		return getSnapshot(cr, Long.MAX_VALUE, new String[0]);
		return cr.getHeadSnapshotList().toArray(new ChangedFile[0]);
	}

	@FunctionSpec(name = "getpreviousversion", returnType = "array of ChangedFile", formalParameters = { "CodeRepository", "ChangedFile" })
	public static ChangedFile[] getPreviousVersion(final CodeRepository cr, final ChangedFile cf) throws Exception {
		List<ChangedFile> l = new ArrayList<ChangedFile>();
		for (int i = 0; i < cf.getChangesCount(); i++) {
			int revisionIndex = cf.getPreviousVersions(i);
			Set<Integer> queuedRevisionIds = new HashSet<Integer>();
			PriorityQueue<Integer> pq = new PriorityQueue<Integer>(100, new Comparator<Integer>() {
				@Override
				public int compare(Integer i1, Integer i2) {
					return i2 - i1;
				}
			});
			pq.offer(revisionIndex);
			queuedRevisionIds.add(revisionIndex);
			while (!pq.isEmpty()) {
				revisionIndex = pq.poll();
				Revision rev = getRevision(cr, revisionIndex);
				int index = Collections.binarySearch(rev.getFilesList(), cf, new Comparator<ChangedFile>() {
					@Override
					public int compare(ChangedFile f1, ChangedFile f2) {
						return f1.getName().compareTo(f2.getName());
					}
				});
				if (index >= 0) {
					ChangedFile ocf = rev.getFiles(index);
					if (ocf.getChange() != ChangeKind.DELETED)
						l.add(ocf);
				} else {
					for (int parentId : rev.getParentsList()) {
						if (!queuedRevisionIds.contains(parentId)) {
							pq.offer(parentId);
							queuedRevisionIds.add(parentId);
						}
					}
				}
			}
		}
		return l.toArray(new ChangedFile[0]);
	}
	
	/**
	 * Is a Revision's log message indicating it is a fixing revision?
	 * 
	 * @param rev the revision to mine
	 * @return true if the revision's log indicates a fixing revision
	 */
	@FunctionSpec(name = "isfixingrevision", returnType = "bool", formalParameters = { "Revision" })
	public static boolean isfixingrevision(final Revision rev) {
		return isfixingrevision(rev.getLog());
	}

	/**
	 * Is a log message indicating it is a fixing revision?
	 * 
	 * @param log the revision's log message to mine
	 * @return true if the log indicates a fixing revision
	 */
	@FunctionSpec(name = "isfixingrevision", returnType = "bool", formalParameters = { "string" })
	public static boolean isfixingrevision(final String log) {
		final String lower = log.toLowerCase();
		for (final Matcher m : fixingMatchers)
			if (m.reset(lower).find())
				return true;

		return false;
	}

	/**
	 * Does a Project contain a file of the specified type? This compares based on file extension.
	 * 
	 * @param p the Project to examine
	 * @param ext the file extension to look for
	 * @return true if the Project contains at least 1 file with the specified extension
	 */
	@FunctionSpec(name = "hasfiletype", returnType = "bool", formalParameters = { "Project", "string" })
	public static boolean hasfile(final Project p, final String ext) {
		for (int i = 0; i < p.getCodeRepositoriesCount(); i++)
			if (hasfile(p.getCodeRepositories(i), ext))
				return true;
		return false;
	}

	/**
	 * Does a CodeRepository contain a file of the specified type? This compares based on file extension.
	 * 
	 * @param cr the CodeRepository to examine
	 * @param ext the file extension to look for
	 * @return true if the CodeRepository contains at least 1 file with the specified extension
	 */
	@FunctionSpec(name = "hasfiletype", returnType = "bool", formalParameters = { "CodeRepository", "string" })
	public static boolean hasfile(final CodeRepository cr, final String ext) {
		for (int i = 0; i < cr.getRevisionsCount(); i++)
			if (hasfile(cr.getRevisions(i), ext))
				return true;
		return false;
	}

	/**
	 * Does a Revision contain a file of the specified type? This compares based on file extension.
	 * 
	 * @param rev the Revision to examine
	 * @param ext the file extension to look for
	 * @return true if the Revision contains at least 1 file with the specified extension
	 */
	@FunctionSpec(name = "hasfiletype", returnType = "bool", formalParameters = { "Revision", "string" })
	public static boolean hasfile(final Revision rev, final String ext) {
		for (int i = 0; i < rev.getFilesCount(); i++)
			if (rev.getFiles(i).getName().toLowerCase().endsWith("." + ext.toLowerCase()))
				return true;
		return false;
	}

	/**
	 * Matches a FileKind enum to the given string.
	 * 
	 * @param s the string to match against
	 * @param kind the FileKind to match
	 * @return true if the string matches the given kind
	 */
	@FunctionSpec(name = "iskind", returnType = "bool", formalParameters = { "string", "FileKind" })
	public static boolean iskind(final String s, final ChangedFile.FileKind kind) {
		return kind.name().startsWith(s);
	}

	public static <T> T stack_pop(final java.util.Stack<T> s) {
		if (s.empty())
			return null;
		return s.pop();
	}

	public static <T> T stack_peek(final java.util.Stack<T> s) {
		if (s.empty())
			return null;
		return s.peek();
	}

	public static String protolistToString(final List<String> l) {
		String s = "";
		for (final String str : l)
			if (s.isEmpty())
				s += str;
			else
				s += ", " + str;
		return s;
	}

	public static <T> String arrayToString(final T[] arr) {
		String s = "";
		for (final T val : arr)
			if (s.isEmpty())
				s += val;
			else
				s += ", " + val;
		return s;
	}

	public static String arrayToString(final long[] arr) {
		String s = "";
		for (final long val : arr)
			if (s.isEmpty())
				s += val;
			else
				s += ", " + val;
		return s;
	}

	public static String arrayToString(final double[] arr) {
		String s = "";
		for (final double val : arr)
			if (s.isEmpty())
				s += val;
			else
				s += ", " + val;
		return s;
	}

	public static String arrayToString(final boolean[] arr) {
		String s = "";
		for (final boolean val : arr)
			if (s.isEmpty())
				s += val;
			else
				s += ", " + val;
		return s;
	}

	public static <T> boolean deepEquals(final T[] arr, final T[] arr2) {
		return java.util.Arrays.deepEquals(arr, arr2);
	}

	public static boolean deepEquals(final long[] arr, final long[] arr2) {
		if (arr.length != arr2.length) return false;
		for (int i = 0; i < arr.length; i++)
			if (arr2[i] != arr[i]) return false;
		return true;
	}

	public static boolean deepEquals(final double[] arr, final double[] arr2) {
		if (arr.length != arr2.length) return false;
		for (int i = 0; i < arr.length; i++)
			if (arr2[i] != arr[i]) return false;
		return true;
	}

	public static boolean deepEquals(final boolean[] arr, final boolean[] arr2) {
		if (arr.length != arr2.length) return false;
		for (int i = 0; i < arr.length; i++)
			if (arr2[i] != arr[i]) return false;
		return true;
	}


	public static <T> T[] basic_array(final T[] arr) {
		return arr;
	}

	public static <T> long[] basic_array(final Long[] arr) {
		long[] arr2 = new long[arr.length];
		for (int i = 0; i < arr.length; i++)
			arr2[i] = arr[i];
		return arr2;
	}

	public static <T> double[] basic_array(final Double[] arr) {
		double[] arr2 = new double[arr.length];
		for (int i = 0; i < arr.length; i++)
			arr2[i] = arr[i];
		return arr2;
	}

	public static <T> boolean[] basic_array(final Boolean[] arr) {
		boolean[] arr2 = new boolean[arr.length];
		for (int i = 0; i < arr.length; i++)
			arr2[i] = arr[i];
		return arr2;
	}

	public static <T> T[] concat(final T[] first, final T[]... rest) {
		int totalLength = first.length;
		for (T[] array : rest)
			totalLength += array.length;
		
		final T[] result = Arrays.copyOf(first, totalLength);
		int offset = first.length;
		for (T[] array : rest) {
			System.arraycopy(array, 0, result, offset, array.length);
			offset += array.length;
		}
		return result;
	}

	public static long[] concat(final long[] first, final long[]... rest) {
		int totalLength = first.length;
		for (long[] array : rest)
			totalLength += array.length;
		
		final long[] result = new long[totalLength];
		System.arraycopy(first, 0, result, 0, first.length);

		int offset = first.length;
		for (long[] array : rest) {
			System.arraycopy(array, 0, result, offset, array.length);
			offset += array.length;
		}
		return result;
	}

	public static double[] concat(final double[] first, final double[]... rest) {
		int totalLength = first.length;
		for (double[] array : rest)
			totalLength += array.length;
		
		final double[] result = new double[totalLength];
		System.arraycopy(first, 0, result, 0, first.length);

		int offset = first.length;
		for (double[] array : rest) {
			System.arraycopy(array, 0, result, offset, array.length);
			offset += array.length;
		}
		return result;
	}

	public static boolean[] concat(final boolean[] first, final boolean[]... rest) {
		int totalLength = first.length;
		for (boolean[] array : rest)
			totalLength += array.length;
		
		final boolean[] result = new boolean[totalLength];
		System.arraycopy(first, 0, result, 0, first.length);

		int offset = first.length;
		for (boolean[] array : rest) {
			System.arraycopy(array, 0, result, offset, array.length);
			offset += array.length;
		}
		return result;
	}

	public static <T> java.util.HashSet<T> set_union(final java.util.Set<T> s1, final java.util.Set<T> s2) {
		final java.util.HashSet<T> s = new java.util.HashSet<T>(s1);
		s.addAll(s2);
		return s;
	}

	public static <T> java.util.HashSet<T> set_intersect(final java.util.Set<T> s1, final java.util.Set<T> s2) {
		final java.util.HashSet<T> s = new java.util.HashSet<T>(s1);
		s.retainAll(s2);
		return s;
	}

	public static <T> java.util.HashSet<T> set_difference(final java.util.Set<T> s1, final java.util.Set<T> s2) {
		final java.util.HashSet<T> s = new java.util.HashSet<T>(s1);
		s.removeAll(s2);
		return s;
	}

	public static <T> java.util.HashSet<T> set_symdiff(final java.util.Set<T> s1, final java.util.Set<T> s2) {
		return set_union(set_difference(s1, s2), set_difference(s2, s1));
	}
}
