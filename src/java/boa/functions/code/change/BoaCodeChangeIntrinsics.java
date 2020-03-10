package boa.functions.code.change;

import static boa.functions.BoaIntrinsics.getRevisionsCount;
import static boa.functions.BoaIntrinsics.getSnapshot;
import java.util.List;

import boa.functions.FunctionSpec;
import boa.types.Code.CodeRepository;
import boa.types.Diff.ChangedFile;
import boa.types.Toplevel.Project;

public class BoaCodeChangeIntrinsics {

	
	@FunctionSpec(name = "test3", formalParameters = { "Project" })
	public static void test2(Project p) throws Exception {
		CodeRepository cr = p.getCodeRepositories(0);
		int revCount = getRevisionsCount(cr);
		FileChangeForest ht = new FileChangeForest(cr, revCount, false);
		List<FileTree> trees = ht.getTreesAsList();
		System.out.println(p.getName());
		System.out.println("Total Revs: " + revCount);
		System.out.println("lists count: " + trees.size());
		ChangedFile[] LatestSnapshot = getSnapshot(cr, revCount - 1, true);
		System.out.println("last snapshot size: " + LatestSnapshot.length);
	}

}
