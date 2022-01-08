package boa.test.datagen;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.lib.Constants;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;

import boa.datagen.DefaultProperties;
import boa.datagen.scm.GitConnector;
import boa.functions.BoaIntrinsics;
import boa.types.Code.CodeRepository;
import boa.types.Diff.ChangedFile;

public class TestBuildSnapshot1 extends BuildSnapshotBase {
	@Test
	public void testBuildSnapshot() throws Exception {
		DefaultProperties.DEBUG = true;

		buildCodeRepository("google/gson");
	}

	@Test
	public void testGetSnapshotFromProtobuf1() throws Exception {
		DefaultProperties.DEBUG = true;

		final CodeRepository cr = buildCodeRepository("boalang/test-datagen");

		ChangedFile[] snapshot = BoaIntrinsics.getSnapshotById(cr, "8041f1281cf6b615861768631097e22127a1e32e", new String[]{"SOURCE_JAVA_JLS"});
		String[] fileNames = new String[snapshot.length];
		for (int i = 0; i < snapshot.length; i++)
			fileNames[i] = snapshot[i].getName();
		assertArrayEquals(new String[]{}, fileNames);

		snapshot = BoaIntrinsics.getSnapshotById(cr, "269424473466542fad9c426f7edf7d10a742e2be", new String[]{"SOURCE_JAVA_JLS"});
		fileNames = new String[snapshot.length];
		for (int i = 0; i < snapshot.length; i++)
			fileNames[i] = snapshot[i].getName();
		assertArrayEquals(new String[]{"src/Foo.java"}, fileNames);

		snapshot = BoaIntrinsics.getSnapshotById(cr, "5e9291c8e830754479bf836686734045faa5c021", new String[]{"SOURCE_JAVA_JLS"});
		fileNames = new String[snapshot.length];
		for (int i = 0; i < snapshot.length; i++)
			fileNames[i] = snapshot[i].getName();
		assertArrayEquals(new String[]{}, fileNames);

		snapshot = BoaIntrinsics.getSnapshotById(cr, "06288fd7cf36415629e3eafdce2448a5406a8c1e", new String[]{"SOURCE_JAVA_JLS"});
		fileNames = new String[snapshot.length];
		for (int i = 0; i < snapshot.length; i++)
			fileNames[i] = snapshot[i].getName();
		assertArrayEquals(new String[]{}, fileNames);
	}

	@Test
	public void testGetSnapshotFromProtobuf2() throws Exception {
		DefaultProperties.DEBUG = true;

		final CodeRepository cr = buildCodeRepository("hyjorc1/my-example");

		final ChangedFile[] snapshot = BoaIntrinsics.getSnapshotById(cr, "d7a4aced37af672f9a55238a47bb0e4974193ebe");
		final String[] fileNames = new String[snapshot.length];
		for (int i = 0; i < snapshot.length; i++)
			fileNames[i] = snapshot[i].getName();

		assertThat(fileNames, Matchers.hasItemInArray("src/org/birds/Bird.java"));
		assertThat(fileNames, Matchers.not(Matchers.hasItemInArray("src/org/animals/Bird.java")));
	}

	@Ignore
	@Test
	public void testBuildSnapshotWithTypes() throws Exception {
		DefaultProperties.DEBUG = true;

		final File gitDir = new File("D:/Projects/Boa-compiler/dataset/repos/candoia/candoia");
		if (!gitDir.exists())
			return;
		final GitConnector gc = new GitConnector(gitDir.getAbsolutePath(), "condoia");
		gc.setRevisions();
		final List<ChangedFile> snapshot1 = gc.buildHeadSnapshot();
		final List<String> snapshot2 = gc.getSnapshot(Constants.HEAD);
		gc.close();
		final Set<String> s1 = new HashSet<String>();
		final Set<String> s2 = new HashSet<String>(snapshot2);
		final Set<String> s = new HashSet<String>(s2);
		final Set<String> in2 = new HashSet<String>(s2);
		for (final ChangedFile cf : snapshot1)
			s1.add(cf.getName());
		assertEquals(s2,  s1);
	}
}
