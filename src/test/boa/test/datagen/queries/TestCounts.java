package boa.test.datagen.queries;

import org.junit.Test;

public class TestCounts extends QueryTest{
	
	@Test
	public void tesCounts() {
		String expected = "AST[] = 11639\n"
				+ "ASTMax[] = 140492550, 235.0\n"
				+ "ASTMean[] = 65.75706214689265\n"
				+ "ASTMin[] = 140492550, 2.0\n"
				+ "ASTProjects[] = 1\n"
				+ "ASTRepositories[] = 1\n"
				+ "JavaSnapshots[] = 177\n"
				+ "JavaSnapshotsMax[] = 140492550, 93.0\n"
				+ "JavaSnapshotsMean[] = 3.339622641509434\n"
				+ "JavaSnapshotsMin[] = 140492550, 0.0\n"
				+ "Projects[] = 1\n"
				+ "Repositories[] = 1\n"
				+ "Revisions[] = 53\n"
				+ "RevisionsMax[] = 140492550, 53.0\n"
				+ "RevisionsMean[] = 53.0\n"
				+ "RevisionsMin[] = 140492550, 53.0\n";
		queryTest("test/known-good/counts.boa", expected);
	}

}
