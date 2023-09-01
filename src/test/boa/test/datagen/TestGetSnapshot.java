/*
 * Copyright 2021-2022, Robert Dyer,
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
package boa.test.datagen;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import boa.functions.BoaIntrinsics;
import boa.types.Code.CodeRepository;
import boa.types.Code.CodeRepository.RepositoryKind;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.Person;
import boa.types.Shared.ChangeKind;

/**
 * Test getsnapshot() functionality.
 *
 * @author rdyer
 */
public class TestGetSnapshot {
	private static Person committer;
	private static CodeRepository repo;
	private static int revcount = 0;

	private final static Map<String, ChangedFile> headSnapshot = new HashMap<String, ChangedFile>();

	private static ChangedFile makeFile(final String name, final ChangeKind change) {
		return makeFile(name, change, ChangedFile.FileKind.OTHER);
	}

	private static ChangedFile makeFile(final String name, final ChangeKind change, final ChangedFile.FileKind kind) {
		final ChangedFile.Builder f = ChangedFile.newBuilder();
		f.setName(name);
		f.setChange(change);
		f.setKind(kind);
		f.setKey(0);
		f.setAst(false);
		return f.build();
	}

	private static Revision makeRev(final ChangedFile[] files) {
		final Revision.Builder rev = Revision.newBuilder();
		if (revcount > 0)
			rev.addParents(revcount - 1);
		rev.setId("" + revcount++);
		rev.setCommitter(committer);
		rev.setCommitDate(100L * revcount);
		rev.setLog("");
		for (final ChangedFile f : files) {
			rev.addFiles(f);
			if (f.getChange() == ChangeKind.DELETED)
				headSnapshot.remove(f.getName());
			else
				headSnapshot.put(f.getName(), f);
		}
		return rev.build();
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		final Person.Builder p = Person.newBuilder();
		p.setUsername("");
		committer = p.build();

		final CodeRepository.Builder b = CodeRepository.newBuilder();
		b.setUrl("");
		b.setKind(RepositoryKind.GIT);

		// in each rev the total # files increase by 1, but otherwise can do anything
		b.addRevisions(makeRev(new ChangedFile[] {
			makeFile("foo", ChangeKind.ADDED)
		}));
		b.addRevisions(makeRev(new ChangedFile[] {
			makeFile("bar", ChangeKind.ADDED),
			makeFile("foo", ChangeKind.MODIFIED)
		}));
		b.addRevisions(makeRev(new ChangedFile[] {
			makeFile("baz", ChangeKind.ADDED),
			makeFile("foo", ChangeKind.DELETED),
			makeFile("bar", ChangeKind.MODIFIED),
			makeFile("fud", ChangeKind.ADDED)
		}));

		b.setHead(b.getRevisionsCount() - 1);
		b.addAllHeadSnapshot(headSnapshot.values());

		repo = b.build();
	}

	@Test
	public void testRepoOk() throws Exception {
		assertEquals(revcount, repo.getRevisionsCount());
	}

	@Test
	public void testGetSnapshotEmptyRepo() throws Exception {
		final CodeRepository.Builder b = CodeRepository.newBuilder();
		b.setUrl("");
		b.setKind(RepositoryKind.GIT);
		b.setHead(0);
		final CodeRepository r = b.build();
		assertEquals(0, r.getRevisionsCount());

		assertEquals(0, BoaIntrinsics.getSnapshot(r, 0L).length);
		assertEquals(0, BoaIntrinsics.getSnapshot(r, 1000L).length);
		assertEquals(0, BoaIntrinsics.getSnapshot(r, 100000000L).length);
	}

	@Test
	public void testGetSnapshotHead() throws Exception {
		assertEquals(revcount, BoaIntrinsics.getSnapshot(repo, repo.getRevisions(revcount - 1)).length);
	}

	@Test
	public void testGetSnapshotOld() throws Exception {
		assertEquals(0, BoaIntrinsics.getSnapshot(repo, 0L).length);
	}

	@Test
	public void testGetSnapshotExactDates() throws Exception {
		for (int i = 0; i < revcount; i++) {
			assertEquals(i + 1, BoaIntrinsics.getSnapshot(repo, repo.getRevisions(i)).length);
			assertEquals(i + 1, BoaIntrinsics.getSnapshotById(repo, repo.getRevisions(i).getId()).length);
			assertEquals(i + 1, BoaIntrinsics.getSnapshot(repo, 100L * (i + 1)).length);
		}
	}

	@Test
	public void testGetSnapshotInbetween() throws Exception {
		for (int i = 1; i < revcount; i++)
			assertEquals(i, BoaIntrinsics.getSnapshot(repo, 100L * i + 10).length);
	}

	@Test
	public void testGetSnapshotFuture() throws Exception {
		assertEquals(revcount, BoaIntrinsics.getSnapshot(repo, 1000L * revcount).length);
	}
}
