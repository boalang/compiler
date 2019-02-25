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
	
	private static int getRevisionIndex(final CodeRepository cr, final long timestamp) {
		int low = 0;
        int high = getRevisionsCount(cr) - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            Revision midRev = getRevision(cr, mid);
            long cmp = midRev.getCommitDate() - timestamp;

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid; // key found: return index
        }
        return low;  // key not found: return low index
	}
	
	private static int getRevisionIndex(final CodeRepository cr, final String id) {
		for (int i = 0; i < getRevisionsCount(cr); i++) {
			if (getRevision(cr, i).getId().equals(id))
				return i;
		}
		return -1;
	}

	@FunctionSpec(name = "getrevisionscount", returnType = "int", formalParameters = { "CodeRepository" })
	public static int getRevisionsCount(CodeRepository cr) {
		return Math.max(cr.getRevisionKeysCount(), cr.getRevisionsCount());
	}
	
	@FunctionSpec(name = "getrevision", returnType = "Revision", formalParameters = { "CodeRepository", "int" })
	public static Revision getRevision(final CodeRepository cr, final long index) {
		if (cr.getRevisionKeysCount() > 0) {
			long key = cr.getRevisionKeys((int) index);
			return BoaAstIntrinsics.getRevision(key);
		}
		return cr.getRevisions((int) index);
	}

	@FunctionSpec(name = "getsnapshot", returnType = "array of ChangedFile", formalParameters = { "CodeRepository", "time", "string..." })
	public static ChangedFile[] getSnapshot(final CodeRepository cr, final long timestamp, final String... kinds) throws Exception {
//		snapshot.initialize(timestamp, kinds).visit(cr);
//		return snapshot.map.values().toArray(new ChangedFile[0]);
		if (getRevisionsCount(cr) == 0)
			return new ChangedFile[0];
		int revisionOffset = getRevisionIndex(cr, timestamp);
		return getSnapshotByIndex(cr, revisionOffset, kinds);
	}
	
	@FunctionSpec(name = "getsnapshotbyindex", returnType = "array of ChangedFile", formalParameters = { "CodeRepository", "int"})
	public static ChangedFile[] getSnapshotByIndex(final CodeRepository cr, final long commitOffset) {
		if (commitOffset == cr.getHead())
			return getSnapshot(cr);
		return getSnapshotByIndex(cr, commitOffset, new String[0]);
	}

	@FunctionSpec(name = "getsnapshotbyindex", returnType = "array of ChangedFile", formalParameters = { "CodeRepository", "int", "string..." })
	public static ChangedFile[] getSnapshotByIndex(final CodeRepository cr, final long commitOffset, final String... kinds) {
		if (commitOffset == cr.getHead())
			return getSnapshot(cr, kinds);
		List<ChangedFile> snapshot = new LinkedList<ChangedFile>();
		Set<String> adds = new HashSet<String>(), dels = new HashSet<String>(); 
		PriorityQueue<Integer> pq = new PriorityQueue<Integer>(100, new Comparator<Integer>() {
			@Override
			public int compare(Integer i1, Integer i2) {
				return i2 - i1;
			}
		});
		Set<Integer> queuedCommitIds = new HashSet<Integer>();
		pq.offer((int) commitOffset);
		queuedCommitIds.add((int) commitOffset);
		while (!pq.isEmpty()) {
			int offset = pq.poll();
			Revision commit = getRevision(cr, offset);
			update(snapshot, commit, adds, dels, pq, queuedCommitIds, kinds);
		}
		return snapshot.toArray(new ChangedFile[0]);
	}

	@FunctionSpec(name = "getsnapshot", returnType = "array of ChangedFile", formalParameters = { "CodeRepository", "Revision"})
	public static ChangedFile[] getSnapshot(final CodeRepository cr, final Revision commit) {
		return getSnapshot(cr, commit, new String[0]);
	}

	@FunctionSpec(name = "getsnapshot", returnType = "array of ChangedFile", formalParameters = { "CodeRepository", "Revision", "string..." })
	public static ChangedFile[] getSnapshot(final CodeRepository cr, final Revision commit, final String... kinds) {
		List<ChangedFile> snapshot = new LinkedList<ChangedFile>();
		Set<String> adds = new HashSet<String>(), dels = new HashSet<String>(); 
		PriorityQueue<Integer> pq = new PriorityQueue<Integer>(100, new Comparator<Integer>() {
			@Override
			public int compare(Integer i1, Integer i2) {
				return i2 - i1;
			}
		});
		Set<Integer> queuedCommitIds = new HashSet<Integer>();
		update(snapshot, commit, adds, dels, pq, queuedCommitIds, kinds);
		while (!pq.isEmpty()) {
			int offset = pq.poll();
			Revision c = getRevision(cr, offset);
			update(snapshot, c, adds, dels, pq, queuedCommitIds, kinds);
		}
		return snapshot.toArray(new ChangedFile[0]);
	}

	private static void update(List<ChangedFile> snapshot, Revision commit, Set<String> adds, Set<String> dels,
			PriorityQueue<Integer> pq, Set<Integer> queuedCommitIds, final String... kinds) {
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

	private static boolean isIncluded(ChangedFile cf, String[] kinds) {
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
		if (getRevisionsCount(cr) == 0)
			return new ChangedFile[0];
		int revisionOffset = getRevisionIndex(cr, id);
		if (revisionOffset < 0)
			return new ChangedFile[0];
		return getSnapshotByIndex(cr, revisionOffset, kinds);
	}

	@FunctionSpec(name = "getsnapshot", returnType = "array of ChangedFile", formalParameters = { "CodeRepository", "string..." })
	public static ChangedFile[] getSnapshot(final CodeRepository cr, final String... kinds) {
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
	public static ChangedFile[] getSnapshot(final CodeRepository cr) {
//		return getSnapshot(cr, Long.MAX_VALUE, new String[0]);
		return cr.getHeadSnapshotList().toArray(new ChangedFile[0]);
	}

	@FunctionSpec(name = "getpreviousversion", returnType = "array of ChangedFile", formalParameters = { "CodeRepository", "ChangedFile" })
	public static ChangedFile[] getPreviousVersion(final CodeRepository cr, final ChangedFile cf) throws Exception {
		List<ChangedFile> l = new ArrayList<ChangedFile>();
		for (int i = 0; i < cf.getChangesCount(); i++) {
			ChangeKind kind = cf.getChanges(i);
			if (kind == ChangeKind.ADDED || kind == ChangeKind.COPIED)
				continue;
			ChangedFile.Builder fb = ChangedFile.newBuilder(cf);
			if (!cf.getPreviousNames(i).isEmpty())
				fb.setName(cf.getPreviousNames(i));
			ChangedFile key = fb.build();
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
				int index = Collections.binarySearch(rev.getFilesList(), key, new Comparator<ChangedFile>() {
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
	
	@FunctionSpec(name = "editdistance", returnType = "int", formalParameters = { "string", "string" })
	public static int editDistance(String x, String y) {
		int[][] dp = new int[x.length() + 1][y.length() + 1];
		 
	    for (int i = 0; i <= x.length(); i++) {
	        for (int j = 0; j <= y.length(); j++) {
	            if (i == 0) {
	                dp[i][j] = j;
	            }
	            else if (j == 0) {
	                dp[i][j] = i;
	            }
	            else {
	                dp[i][j] = min(dp[i - 1][j - 1] 
	                 + costOfSubstitution(x.charAt(i - 1), y.charAt(j - 1)), 
	                  dp[i - 1][j] + 1, 
	                  dp[i][j - 1] + 1);
	            }
	        }
	    }
	 
	    return dp[x.length()][y.length()];
	}
	
	public static int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }
 
    public static int min(int... numbers) {
        return Arrays.stream(numbers)
          .min().orElse(Integer.MAX_VALUE);
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
