/*
 * Copyright 2022, Robert Dyer
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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Files;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.fs.FileSystem;
import org.junit.Test;

import boa.datagen.DefaultProperties;
import boa.datagen.forges.github.RepositoryCloner;
import boa.datagen.scm.GitConnector;
import boa.datagen.util.FileIO;
import boa.functions.BoaIntrinsics;
import boa.functions.BoaCasts;
import boa.types.Code.CodeRepository;
import boa.types.Code.CodeRepository.RepositoryKind;
import boa.types.Code.Revision;

public class TestBuildSnapshot4 extends BuildSnapshotBase {
	protected CodeRepository buildCodeRepository(final String repoName) throws Exception {
		fileSystem = FileSystem.get(conf);

		final File gitDir = Files.createTempDirectory(repoName.replaceAll("/", "_")).toFile();
		openWriters(gitDir.getAbsolutePath());
		FileIO.DirectoryRemover filecheck = new FileIO.DirectoryRemover(gitDir.getAbsolutePath());
		filecheck.run();
		final String url = "https://github.com/" + repoName + ".git";
		RepositoryCloner.clone(new String[]{url, gitDir.getAbsolutePath()});
		final GitConnector conn = new GitConnector(gitDir.getAbsolutePath(), repoName, astWriter, astWriterLen, commitWriter, commitWriterLen, contentWriter, contentWriterLen);
		final CodeRepository.Builder repoBuilder = CodeRepository.newBuilder();
		repoBuilder.setKind(RepositoryKind.GIT);
		repoBuilder.setUrl(url);
		for (final Object rev : conn.getRevisions(repoName)) {
			final Revision.Builder revBuilder = Revision.newBuilder((Revision) rev);
			repoBuilder.addRevisions(revBuilder);
		}
		if (repoBuilder.getRevisionsCount() > 0) {
			repoBuilder.setHead(conn.getHeadCommitOffset());
			repoBuilder.addAllHeadSnapshot(conn.buildHeadSnapshot());
		}
		repoBuilder.addAllBranches(conn.getBranchIndices());
		repoBuilder.addAllBranchNames(conn.getBranchNames());
		repoBuilder.addAllTags(conn.getTagIndices());
		repoBuilder.addAllTagNames(conn.getTagNames());

		closeWriters();

		new Thread(new FileIO.DirectoryRemover(gitDir.getAbsolutePath())).start();
		conn.close();

		return repoBuilder.build();
	}

	@Test
	public void testGetRevisionIndex() throws Exception {
		final Map<Integer, String> m = new HashMap<>();

		m.put(2006, "9b8b4d90279e917eaa25122c171cd36f1fa8f056");
		m.put(2007, "a29839774a1200b48b6ac5b15473a7d1d5d8e67f");
		m.put(2008, "a3bbeb031736e6afe60d483c0530f91a8ff4bf58");
		m.put(2009, "9a6cf71b8584493fd54aa74b5352263e025068aa");
		m.put(2010, "87c032fbb93e764d5f636659486d1831b297b7fd");
		m.put(2011, "566214b296f126d144777b40ca81d63cff89bd41");
		m.put(2012, "867cdb24ffeeddb1987678ef34a8b845ee4fa60f");
		m.put(2013, "e5848faea2b9452bb07770cdfb224cc85723f164");
		m.put(2014, "3b9d3e279c080cff1a16c7adc9378813819f0ff7");
		m.put(2015, "fd625b525668b5610b9488c0d6aa75cb95850a46");
		m.put(2016, "cfde080f48ef2c98aca908c1c7b7f64303a48f74");
		m.put(2017, "4aada82d17abf01a7ea0d8bce755866ebaa6e235");
		m.put(2018, "60bff8bd3eb03479c6be2e7974979faaf5f002c8");
		m.put(2019, "4f62d3a3ace770ffca3f3ea7c404d7cdb5a530ed");
		m.put(2020, "ca49d5d7b93957f05db8d954fdc173386a1fc40e");
		m.put(2021, "772f05e0e62a2a5238ea4206a6d6859aade5fa44");

		final CodeRepository cr = buildCodeRepository("boalang/marytts");

		for (int year = 2006; year <= 2021; year++) {
			final Date date = new GregorianCalendar(year + 1, Calendar.JANUARY, 1).getTime();
			final long ts = BoaCasts.stringToTime("January 1, " + (year + 1) + ", 12:00:00 AM UTC");
			int revIdx = (int)BoaIntrinsics.getRevisionIndex(cr, ts);
			assertEquals(m.get(year), revIdx > -1 ? cr.getRevisions(revIdx).getId() : "");
		}
	}
}
