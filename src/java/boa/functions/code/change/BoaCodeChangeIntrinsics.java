package boa.functions.code.change;

import static boa.functions.BoaAstIntrinsics.getRefactoringIdsInSet;
import static boa.functions.BoaIntrinsics.getRevisionsCount;
import static boa.functions.BoaIntrinsics.getSnapshot;
import java.util.HashSet;
import java.util.List;

import boa.functions.FunctionSpec;
import boa.types.Code.CodeRepository;
import boa.types.Diff.ChangedFile;
import boa.types.Toplevel.Project;

public class BoaCodeChangeIntrinsics {

	@FunctionSpec(name = "test3", formalParameters = { "Project" })
	public static void test2(Project p) throws Exception {
		CodeRepository cr = p.getCodeRepositories(0);
		HashSet<String> refRevIds = getRefactoringIdsInSet(p);
		System.out.println(p.getName() + " " + refRevIds.size());

		int revCount = getRevisionsCount(cr);
		GlobalData gd = new GlobalData(cr, revCount);
		FileChangeForest forest = new FileChangeForest(gd, false);
		forest.updateWithRefs(p, refRevIds, null);
		List<FileTree> trees = forest.getTreesAsList();
		
		
		System.out.println("Total Revs: " + revCount);
		System.out.println("lists count: " + trees.size());
		ChangedFile[] LatestSnapshot = getSnapshot(cr, revCount - 1, true);
		System.out.println("last snapshot size: " + LatestSnapshot.length);
	}

}
