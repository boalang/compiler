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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.hadoop.io.SequenceFile.Writer;

import boa.types.Code.Revision;

/**
 * @author rdyer
 */
public abstract class AbstractConnector implements AutoCloseable {
	protected List<AbstractCommit> revisions = null;
	protected HashMap<String, Integer> nameIndices = new HashMap<String, Integer>();

	public abstract String getLastCommitId();
	public abstract void setLastSeenCommitId(final String id);

	public List<Revision> getCommits(final boolean parse) {
		if (revisions == null) {
			revisions = new ArrayList<AbstractCommit>();
			setRevisions();
		}
		final List<Revision> revs = new ArrayList<Revision>();
		for (final AbstractCommit rev : revisions)
			revs.add(rev.asProtobuf(parse));

		return revs;
	}

	protected abstract void setRevisions();

	public abstract void getTags(final List<String> names, final List<String> commits);

	public abstract void getBranches(final List<String> names, final List<String> commits);

	protected Map<String, Integer> revisionMap;

	public List<Revision> getCommits(final boolean parse, final Writer astWriter, final String repoKey, final String keyDelim) {
		if (revisions == null) {
			revisions = new ArrayList<AbstractCommit>();
			setRevisions();
		}
		final List<Revision> revs = new ArrayList<Revision>();
		int i = 0;
		for (final AbstractCommit rev : revisions)
			revs.add(rev.asProtobuf(parse, astWriter, repoKey + keyDelim + (++i), keyDelim));

		return revs;
	}

}
