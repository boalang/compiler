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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.apache.hadoop.io.SequenceFile.Writer;

import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.ChangeKind;

/**
 * @author rdyer
 */
public abstract class AbstractConnector implements AutoCloseable {
	protected List<AbstractCommit> revisions = null;
	protected List<String> branchNames = new ArrayList<String>(), tagNames = new ArrayList<String>();
	protected List<Integer> branchIndices = new ArrayList<Integer>(), tagIndices = new ArrayList<Integer>();
	protected HashMap<String, Integer> nameIndices = new HashMap<String, Integer>();
	protected Map<String, Integer> revisionMap = new HashMap<String, Integer>();
	protected int headCommitOffset = -1;

	public int getHeadCommitOffset() {
		return this.headCommitOffset;
	}
	
	public List<ChangedFile> buildHeadSnapshot() {
		List<ChangedFile> snapshot = getSnapshot(headCommitOffset);
		
		return snapshot;
	}

	public List<ChangedFile> getSnapshot(int commitOffset) {
		List<ChangedFile> snapshot = new ArrayList<ChangedFile>();
		Set<String> adds = new HashSet<String>(), dels = new HashSet<String>(); 
		PriorityQueue<Integer> pq = new PriorityQueue<Integer>(100, new Comparator<Integer>() {
			@Override
			public int compare(Integer i1, Integer i2) {
				return i2 - i1;
			}
		});
		pq.offer(commitOffset);
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
					}
					break;
				case COPIED:
					if (!adds.contains(cf.getName()) && !dels.contains(cf.getName())) {
						adds.add(cf.getName());
						snapshot.add(cf.build());
					}
					break;
				case DELETED:
					if (!adds.contains(cf.getName()) && !dels.contains(cf.getName()))
						dels.add(cf.getName());
					break;
				case MERGED:
					if (!adds.contains(cf.getName()) && !dels.contains(cf.getName()))
						dels.add(cf.getName());
					for (int i = 0; i < cf.getPreviousIndicesCount(); i++) {
						ChangedFile.Builder pcf = revisions.get(cf.getPreviousVersions(i)).changedFiles.get(cf.getPreviousIndices(i));
						ChangeKind pck = cf.getChanges(i);
						if (!adds.contains(pcf.getName()) && !dels.contains(pcf.getName()) && (pck == ChangeKind.DELETED || pck == ChangeKind.RENAMED))
							dels.add(pcf.getName());
					}
					break;
				case RENAMED:
					if (!adds.contains(cf.getName()) && !dels.contains(cf.getName())) {
						adds.add(cf.getName());
						snapshot.add(cf.build());
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
					}
					break;
				}
			}
			if (commit.parentIndices != null)
				for (int p : commit.parentIndices)
					pq.offer(p);
		}
		
		return snapshot;
	}
	
	public abstract void setRevisions();

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
