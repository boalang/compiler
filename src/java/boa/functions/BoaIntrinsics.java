/*
 * Copyright 2014-2023, Hridesh Rajan, Robert Dyer,
 *                 Iowa State University of Science and Technology
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
package boa.functions;

import java.io.BufferedInputStream;
import java.io.ObjectInputStream;
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
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import boa.datagen.DefaultProperties;
import boa.types.Code.CodeRepository;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.ChangeKind;
import boa.types.Toplevel.Project;

/**
 * Boa domain-specific functions.
 *
 * @author rdyer
 */
public class BoaIntrinsics {
	private static Set<Integer> forksData;

	private static void loadForksData() {
		try {
			final Configuration conf = BoaAstIntrinsics.context.getConfiguration();
			final FileSystem fs;
			final Path p;
			if (DefaultProperties.localDataPath != null) {
				p = new Path(DefaultProperties.localDataPath, "forks.bin");
				fs = FileSystem.getLocal(conf);
			} else {
				p = new Path(
					BoaAstIntrinsics.context.getConfiguration().get("fs.default.name", "hdfs://boa-njt/"),
					new Path(conf.get("boa.forks.file", conf.get("boa.ast.dir", conf.get("boa.input.dir", "")) + "/forks.bin"))
				);
				fs = FileSystem.get(conf);
			}

			try (final FSDataInputStream data = fs.open(p);
				final BufferedInputStream bis = new BufferedInputStream(data);
				final ObjectInputStream ois = new ObjectInputStream(bis)) {
				forksData = (Set<Integer>)ois.readObject();
			}
		} catch (final Exception e) {
			System.err.println("Error reading forks.bin: " + e.getMessage());
			e.printStackTrace();
			forksData = new HashSet<Integer>();
		}
	}

	@FunctionSpec(name = "isfork", returnType = "bool", formalParameters = { "Project" })
	public static boolean isfork(final Project p) {
		if (forksData == null)
			loadForksData();
		return p.getForked() || forksData.contains(Integer.parseInt(p.getId()));
	}

	private final static String[] fixingRegex = {
		"\\bfix(s|es|ing|ed)?\\b",
		"\\b(error|bug|issue)(s)?\\b",
		//"\\b(bug|issue|fix)(s)?\\b\\s*(#)?\\s*[0-9]+",
		//"\\b(bug|issue|fix)\\b\\s*id(s)?\\s*(=)?\\s*[0-9]+"
	};

	private final static List<Matcher> fixingMatchers = new ArrayList<Matcher>();

	static {
		for (final String s : fixingRegex)
			fixingMatchers.add(Pattern.compile(s).matcher(""));
	}

	private final static Comparator<Integer> snapshotComparator = new Comparator<Integer>() {
		@Override
		public int compare(final Integer i1, final Integer i2) {
			return i2 - i1;
		}
	};

	@FunctionSpec(name = "getrevisionindex", returnType = "int", formalParameters = { "CodeRepository", "int" })
	public static int getRevisionIndex(final CodeRepository cr, final long timestamp) {
		return getRevisionIndex(cr, cr.getHead(), timestamp);
	}

	@FunctionSpec(name = "getrevisionindex", returnType = "int", formalParameters = { "CodeRepository", "int", "int" })
	public static int getRevisionIndex(final CodeRepository cr, final long headId, final long timestamp) {
		final int revCount = getRevisionsCount(cr);
		if (headId < 0 || headId >= revCount) return -1;

		final PriorityQueue<Integer> pq = new PriorityQueue<Integer>(1 + revCount / 4);
		pq.offer((int)headId);

		final Set<Integer> seenIds = new HashSet<Integer>();
		int idx = -1;
		long lasttime = Long.MIN_VALUE;

		while (!pq.isEmpty()) {
			final int id = pq.poll();
			seenIds.add(id);
			final Revision commit = getRevision(cr, id);

			final long ts = commit.getCommitDate();
			if (lasttime < ts && ts <= timestamp) {
				idx = id;
				lasttime = ts;
			}

			// git system only consider diffs from the first parent
			if (ts > timestamp && commit.getParentsList() != null)
				for (int i = 0; i < commit.getParentsList().size(); i++) {
					final int p = commit.getParentsList().get(i);
					if (!seenIds.contains(p) && p > idx)
						pq.offer(p);
				}
		}

		return idx;
	}

	@FunctionSpec(name = "getrevisionindex2", returnType = "int", formalParameters = { "CodeRepository", "int" })
	public static int getRevisionIndex2(final CodeRepository cr, final long timestamp) {
		int low = 0;
		int high = getRevisionsCount(cr) - 1;

		while (low <= high) {
			final int mid = low + (high - low) / 2;
			final Revision midRev = getRevision(cr, mid);
			final long cmp = midRev.getCommitDate() - timestamp;

			if (cmp == 0)
				return mid; // key found: return index

			if (cmp < 0)
				low = mid + 1;
			else
				high = mid - 1;
		}
		return high; // key not found
	}

	@FunctionSpec(name = "getrevisionindex", returnType = "int", formalParameters = { "CodeRepository", "string" })
	public static int getRevisionIndex(final CodeRepository cr, final String id) {
		if (cr.getRevisionKeysCount() > 0) {
			for (int i = 0; i < cr.getRevisionKeysCount(); i++)
				if (BoaAstIntrinsics.getRevision(cr.getRevisionKeys(i)).getId().equals(id))
					return i;
		} else {
			for (int i = 0; i < cr.getRevisionsCount(); i++)
				if (cr.getRevisions(i).getId().equals(id))
					return i;
		}
		return -1;
	}

	@FunctionSpec(name = "getrevisionscount", returnType = "int", formalParameters = { "CodeRepository" })
	public static int getRevisionsCount(final CodeRepository cr) {
		return Math.max(cr.getRevisionKeysCount(), cr.getRevisionsCount());
	}

	@FunctionSpec(name = "getrevision", returnType = "Revision", formalParameters = { "CodeRepository", "int" })
	public static Revision getRevision(final CodeRepository cr, final long index) {
		if (cr.getRevisionKeysCount() > 0)
			return BoaAstIntrinsics.getRevision(cr.getRevisionKeys((int) index));
		return cr.getRevisions((int) index);
	}

	@FunctionSpec(name = "getsnapshotbyindex", returnType = "array of ChangedFile", formalParameters = { "CodeRepository", "int"})
	public static ChangedFile[] getSnapshotByIndex(final CodeRepository cr, final long commitOffset) {
		return getSnapshotByIndex(cr, commitOffset, new String[0]);
	}

	@FunctionSpec(name = "getsnapshotbyindex", returnType = "array of ChangedFile", formalParameters = { "CodeRepository", "int", "string..." })
	public static ChangedFile[] getSnapshotByIndex(final CodeRepository cr, final long commitOffset, final String... kinds) {
		if (commitOffset == cr.getHead())
			return getSnapshot(cr, kinds);
		if (commitOffset < 0)
			return new ChangedFile[0];
		final List<ChangedFile> snapshot = new LinkedList<ChangedFile>();
		final Set<String> adds = new HashSet<String>();
		final Set<String> dels = new HashSet<String>();
		final PriorityQueue<Integer> pq = new PriorityQueue<Integer>(100, snapshotComparator);
		final Set<Integer> queuedCommitIds = new HashSet<Integer>();
		pq.offer((int) commitOffset);
		queuedCommitIds.add((int) commitOffset);
		while (!pq.isEmpty()) {
			final int offset = pq.poll();
			final Revision commit = getRevision(cr, offset);
			update(snapshot, commit, adds, dels, pq, queuedCommitIds, kinds);
		}
		return snapshot.toArray(new ChangedFile[0]);
	}

	private static void update(final List<ChangedFile> snapshot, final Revision commit, final Set<String> adds, final Set<String> dels,
			final PriorityQueue<Integer> pq, final Set<Integer> queuedCommitIds, final String... kinds) {
		for (final ChangedFile cf : commit.getFilesList()) {
			final ChangeKind ck = cf.getChange();
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
						final ChangeKind pck = cf.getChanges(i);
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
					// In git system, some renamed files might not have previous names
					if (cf.getPreviousNamesCount() != 0) {
						final String name = cf.getPreviousNames(i);
						if (!adds.contains(name) && !dels.contains(name))
							dels.add(name);
					}
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
		// git system only consider diffs from the first parent
		if (commit.getParentsList() != null && commit.getParentsList().size() != 0) {
			final int p = commit.getParentsList().get(0);
			if (!queuedCommitIds.contains(p)) {
				pq.offer(p);
				queuedCommitIds.add(p);
			}
		}
	}

	private static boolean isIncluded(final ChangedFile cf, final String[] kinds) {
		if (kinds == null || kinds.length == 0)
			return true;
		final String kindName = cf.getKind().name();
		for (final String kind : kinds)
			if (kindName.startsWith(kind))
				return true;
		return false;
	}

	@FunctionSpec(name = "getsnapshotbyid", returnType = "array of ChangedFile", formalParameters = { "CodeRepository", "string" })
	public static ChangedFile[] getSnapshotById(final CodeRepository cr, final String id) {
		return getSnapshotById(cr, id, new String[0]);
	}

	@FunctionSpec(name = "getsnapshotbyid", returnType = "array of ChangedFile", formalParameters = { "CodeRepository", "string", "string..." })
	public static ChangedFile[] getSnapshotById(final CodeRepository cr, final String id, final String... kinds) {
		final int revisionOffset = getRevisionIndex(cr, id);
		if (revisionOffset < 0)
			return new ChangedFile[0];
		return getSnapshotByIndex(cr, revisionOffset, kinds);
	}

	@FunctionSpec(name = "getsnapshot", returnType = "array of ChangedFile", formalParameters = { "CodeRepository", "Revision"})
	public static ChangedFile[] getSnapshot(final CodeRepository cr, final Revision commit) {
		return getSnapshot(cr, commit, new String[0]);
	}

	@FunctionSpec(name = "getsnapshot", returnType = "array of ChangedFile", formalParameters = { "CodeRepository", "Revision", "string..." })
	public static ChangedFile[] getSnapshot(final CodeRepository cr, final Revision commit, final String... kinds) {
		final List<ChangedFile> snapshot = new LinkedList<ChangedFile>();
		final Set<String> adds = new HashSet<String>();
		final Set<String> dels = new HashSet<String>();
		final PriorityQueue<Integer> pq = new PriorityQueue<Integer>(100, snapshotComparator);
		final Set<Integer> queuedCommitIds = new HashSet<Integer>();
		update(snapshot, commit, adds, dels, pq, queuedCommitIds, kinds);
		while (!pq.isEmpty()) {
			final int offset = pq.poll();
			final Revision c = getRevision(cr, offset);
			update(snapshot, c, adds, dels, pq, queuedCommitIds, kinds);
		}
		return snapshot.toArray(new ChangedFile[0]);
	}

	@FunctionSpec(name = "getsnapshot", returnType = "array of ChangedFile", formalParameters = { "CodeRepository", "time", "string..." })
	public static ChangedFile[] getSnapshot(final CodeRepository cr, final long timestamp, final String... kinds) throws Exception {
		return getSnapshotByIndex(cr, getRevisionIndex(cr, timestamp), kinds);
	}

	@FunctionSpec(name = "getsnapshot", returnType = "array of ChangedFile", formalParameters = { "CodeRepository", "time" })
	public static ChangedFile[] getSnapshot(final CodeRepository cr, final long timestamp) throws Exception {
		return getSnapshot(cr, timestamp, new String[0]);
	}

	@FunctionSpec(name = "getsnapshot", returnType = "array of ChangedFile", formalParameters = { "CodeRepository", "string..." })
	public static ChangedFile[] getSnapshot(final CodeRepository cr, final String... kinds) {
		final List<ChangedFile> files = new ArrayList<ChangedFile>();
		for (final ChangedFile file : cr.getHeadSnapshotList())
			if (isIncluded(file, kinds))
				files.add(file);
		return files.toArray(new ChangedFile[0]);
	}

	@FunctionSpec(name = "getsnapshot", returnType = "array of ChangedFile", formalParameters = { "CodeRepository" })
	public static ChangedFile[] getSnapshot(final CodeRepository cr) {
		return cr.getHeadSnapshotList().toArray(new ChangedFile[0]);
	}

	@FunctionSpec(name = "getpreviousversion", returnType = "array of ChangedFile", formalParameters = { "CodeRepository", "ChangedFile" })
	public static ChangedFile[] getPreviousVersion(final CodeRepository cr, final ChangedFile cf) throws Exception {
		final List<ChangedFile> l = new ArrayList<ChangedFile>();
		for (int i = 0; i < cf.getChangesCount(); i++) {
			final ChangeKind kind = cf.getChanges(i);
			if (kind == ChangeKind.ADDED || kind == ChangeKind.COPIED)
				continue;
			final ChangedFile.Builder fb = ChangedFile.newBuilder(cf);
			if (!cf.getPreviousNames(i).isEmpty())
				fb.setName(cf.getPreviousNames(i));
			final ChangedFile key = fb.build();
			int revisionIndex = cf.getPreviousVersions(i);
			final Set<Integer> queuedRevisionIds = new HashSet<Integer>();
			final PriorityQueue<Integer> pq = new PriorityQueue<Integer>(100, snapshotComparator);
			pq.offer(revisionIndex);
			queuedRevisionIds.add(revisionIndex);
			while (!pq.isEmpty()) {
				revisionIndex = pq.poll();
				final Revision rev = getRevision(cr, revisionIndex);
				final int index = Collections.binarySearch(rev.getFilesList(), key, new Comparator<ChangedFile>() {
					@Override
					public int compare(final ChangedFile f1, final ChangedFile f2) {
						return f1.getName().compareTo(f2.getName());
					}
				});
				if (index >= 0) {
					final ChangedFile ocf = rev.getFiles(index);
					if (ocf.getChange() != ChangeKind.DELETED)
						l.add(ocf);
				} else {
					for (final int parentId : rev.getParentsList()) {
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
		for (int i = 0; i < getRevisionsCount(cr); i++)
			if (hasfile(getRevision(cr, i), ext))
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
	 * Does a ChangedFile contain a file of the specified type? This compares based on file extension.
	 *
	 * @param rev the ChangedFile to examine
	 * @param ext the file extension to look for
	 * @return true if the ChangedFile contains at least 1 file with the specified extension
	 */
	@FunctionSpec(name = "hasfiletype", returnType = "bool", formalParameters = { "ChangedFile", "string" })
	public static boolean hasfile(final ChangedFile cf, final String ext) {
		return cf.getName().toLowerCase().endsWith("." + ext.toLowerCase());
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
		final long[] arr2 = new long[arr.length];
		for (int i = 0; i < arr.length; i++)
			arr2[i] = arr[i];
		return arr2;
	}

	public static <T> double[] basic_array(final Double[] arr) {
		final double[] arr2 = new double[arr.length];
		for (int i = 0; i < arr.length; i++)
			arr2[i] = arr[i];
		return arr2;
	}

	public static <T> boolean[] basic_array(final Boolean[] arr) {
		final boolean[] arr2 = new boolean[arr.length];
		for (int i = 0; i < arr.length; i++)
			arr2[i] = arr[i];
		return arr2;
	}

	@SafeVarargs
	public static <T> T[] concat(final T[] first, final T[]... rest) {
		int totalLength = first.length;
		for (final T[] array : rest)
			totalLength += array.length;

		final T[] result = Arrays.copyOf(first, totalLength);
		int offset = first.length;
		for (final T[] array : rest) {
			System.arraycopy(array, 0, result, offset, array.length);
			offset += array.length;
		}
		return result;
	}

	public static long[] concat(final long[] first, final long[]... rest) {
		int totalLength = first.length;
		for (final long[] array : rest)
			totalLength += array.length;

		final long[] result = new long[totalLength];
		System.arraycopy(first, 0, result, 0, first.length);

		int offset = first.length;
		for (final long[] array : rest) {
			System.arraycopy(array, 0, result, offset, array.length);
			offset += array.length;
		}
		return result;
	}

	public static double[] concat(final double[] first, final double[]... rest) {
		int totalLength = first.length;
		for (final double[] array : rest)
			totalLength += array.length;

		final double[] result = new double[totalLength];
		System.arraycopy(first, 0, result, 0, first.length);

		int offset = first.length;
		for (final double[] array : rest) {
			System.arraycopy(array, 0, result, offset, array.length);
			offset += array.length;
		}
		return result;
	}

	public static boolean[] concat(final boolean[] first, final boolean[]... rest) {
		int totalLength = first.length;
		for (final boolean[] array : rest)
			totalLength += array.length;

		final boolean[] result = new boolean[totalLength];
		System.arraycopy(first, 0, result, 0, first.length);

		int offset = first.length;
		for (final boolean[] array : rest) {
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
