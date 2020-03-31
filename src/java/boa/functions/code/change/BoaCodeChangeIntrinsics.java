package boa.functions.code.change;

import static boa.functions.BoaAstIntrinsics.cleanup;
import static boa.functions.BoaAstIntrinsics.getRefactoringIdsInSet;
import static boa.functions.BoaIntrinsics.getRevisionsCount;
import static boa.functions.BoaIntrinsics.getSnapshot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import boa.functions.FunctionSpec;
import boa.functions.code.change.declaration.DeclForest;
import boa.functions.code.change.declaration.DeclTree;
import boa.functions.code.change.field.FieldForest;
import boa.functions.code.change.field.FieldTree;
import boa.functions.code.change.file.FileForest;
import boa.functions.code.change.file.FileTree;
import boa.functions.code.change.method.MethodForest;
import boa.functions.code.change.method.MethodTree;
import boa.types.Code.CodeRepository;
import boa.types.Diff.ChangedFile;
import boa.types.Toplevel.Project;

public class BoaCodeChangeIntrinsics {

	@FunctionSpec(name = "test3", formalParameters = { "Project" })
	public static void test2(Project p) throws Exception {

//		if (p.getName().equals("ant4eclipse/ant4eclipse"))
//			return;

		long beforeUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

		CodeRepository cr = p.getCodeRepositories(0);
		HashSet<String> refRevIds = getRefactoringIdsInSet(p);
		System.out.println(p.getName() + " " + refRevIds.size());

		int revCount = getRevisionsCount(cr);
		ChangeDataBase db = new ChangeDataBase(cr, revCount);
		FileForest forest = new FileForest(db, false);
		HashMap<Integer, FileTree> fileTrees = forest.getTrees();

		long afterUsedMem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

		forest.updateASTChanges();
		DeclForest declForest = new DeclForest(db, false);
		HashMap<Integer, DeclTree> declTrees = declForest.getTrees();
		MethodForest methodForest = new MethodForest(db, false);
		HashMap<Integer, MethodTree> methodTrees = methodForest.getTrees();
		FieldForest fieldForest = new FieldForest(db, false);
		HashMap<Integer, FieldTree> fieldTrees = fieldForest.getTrees();
		cleanup();

		forest.updateWithRefs(p, refRevIds, null); //TODO

		System.out.println("Distinct Files: " + forest.db.fileNames.size());
		System.out.println("Total Revs: " + revCount);
		System.out.println("Total FileTrees: " + fileTrees.size());
		System.out.println("Total refs: " + db.refDB.size());

		System.out.println("Total decl changes: " + db.declDB.size());
		System.out.println("Total DeclTrees: " + declTrees.size());

		System.out.println("Total method changes: " + db.methodDB.size());
		System.out.println("Total methodTrees: " + methodTrees.size());

		System.out.println("Total field changes: " + db.fieldDB.size());
		System.out.println("Total fieldTrees: " + fieldTrees.size());

		ChangedFile[] LatestSnapshot = getSnapshot(cr, revCount - 1, false);
		int count = 0;
		for (ChangedFile cf : LatestSnapshot)
			if (cf.getName().endsWith(".java"))
				count++;
		System.out.println("last snapshot size: " + count);

		long afterUsedMem2 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		System.err.println("Before Used " + beforeUsedMem / 1000000.0 + " MB");
		System.err.println("File Tree Used " + afterUsedMem1 / 1000000.0 + " MB");
		System.err.println("Parse Files Used " + afterUsedMem2 / 1000000.0 + " MB");
	}

}