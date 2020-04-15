package boa.functions.code.change;

import static boa.functions.BoaAstIntrinsics.cleanup;
import static boa.functions.BoaAstIntrinsics.getRefactoringIdsInSet;
import static boa.functions.BoaIntrinsics.getRevisionsCount;
import static boa.functions.BoaIntrinsics.getSnapshot;

import java.util.HashMap;
import java.util.HashSet;
import boa.functions.FunctionSpec;
import boa.functions.code.change.declaration.DeclForest;
import boa.functions.code.change.declaration.DeclTree;
import boa.functions.code.change.field.FieldForest;
import boa.functions.code.change.field.FieldTree;
import boa.functions.code.change.file.FileForest;
import boa.functions.code.change.file.FileNode;
import boa.functions.code.change.file.FileTree;
import boa.functions.code.change.method.MethodForest;
import boa.functions.code.change.method.MethodTree;
import boa.types.Code.CodeRepository;
import boa.types.Diff.ChangedFile;
import boa.types.Toplevel.Project;

public class BoaCodeChangeIntrinsics {

	@FunctionSpec(name = "test3", returnType = "string", formalParameters = { "Project" })
	public static String test3(Project p) throws Exception {

		String s = "";

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
		
		Validation v = new Validation(db).validate();

		s += v.getOutput();

		cleanup();

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
		
		
		RevNode r = db.revIdMap.get("9fe955e88c345e14ffbae516bbefb0b9022198d3");
		FileNode fn = r.getFileNode("src/java/org/jetbrains/plugins/clojure/ClojureLoader.java");
		System.out.println(fn.getFirstParent());

		return s;
	}

//	@FunctionSpec(name = "test3", returnType = "string", formalParameters = { "Project" })
//	public static String test3(Project p) throws Exception {
//		
////		if (!p.getName().equals("ant4eclipse/ant4eclipse"))
////			return null;
//		
//		String s = "";
//
//		long beforeUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
//
//		CodeRepository cr = p.getCodeRepositories(0);
//		HashSet<String> refRevIds = getRefactoringIdsInSet(p);
////		System.out.println(p.getName() + " " + refRevIds.size());
//		s += p.getName() + " " + refRevIds.size() + "\n";
//
//		int revCount = getRevisionsCount(cr);
//		ChangeDataBase db = new ChangeDataBase(cr, revCount);
//		FileForest forest = new FileForest(db, false);
//		HashMap<Integer, FileTree> fileTrees = forest.getTrees();
//
//		long afterUsedMem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
//
//		forest.updateASTChanges();
//		DeclForest declForest = new DeclForest(db, false);
//		HashMap<Integer, DeclTree> declTrees = declForest.getTrees();
//		MethodForest methodForest = new MethodForest(db, false);
//		HashMap<Integer, MethodTree> methodTrees = methodForest.getTrees();
//		FieldForest fieldForest = new FieldForest(db, false);
//		HashMap<Integer, FieldTree> fieldTrees = fieldForest.getTrees();
//
//		forest.updateWithRefs(p, refRevIds);
//		Validation v = new Validation(db).validate();
//		
//		s += v.getOutput();
//		
//		
//		cleanup();
//
////		System.out.println("Distinct Files: " + forest.db.fileNames.size());
//		
//		s += "Distinct Files: " + forest.db.fileNames.size() + "\n";
//		
////		System.out.println("Total Revs: " + revCount);
//		
//		s += "Total Revs: " + revCount + "\n";
//		
////		System.out.println("Total FileTrees: " + fileTrees.size());
//		
//		s += "Total FileTrees: " + fileTrees.size() + "\n";
//		
////		System.out.println("Total refs: " + db.refDB.size());
//		
//		s += "Total refs: " + db.refDB.size() + "\n";
//
////		System.out.println("Total decl changes: " + db.declDB.size());
//		
//		s += "Total decl changes: " + db.declDB.size() + "\n";
//		
////		System.out.println("Total DeclTrees: " + declTrees.size());
//		
//		s += "Total DeclTrees: " + declTrees.size() + "\n";
//
////		System.out.println("Total method changes: " + db.methodDB.size());
//		
//		s += "Total method changes: " + db.methodDB.size() + "\n";
//		
////		System.out.println("Total methodTrees: " + methodTrees.size());
//		
//		s += "Total methodTrees: " + methodTrees.size() + "\n";
//
////		System.out.println("Total field changes: " + db.fieldDB.size());
//		
//		s += "Total field changes: " + db.fieldDB.size() + "\n";
//		
////		System.out.println("Total fieldTrees: " + fieldTrees.size());
//		
//		s += "Total fieldTrees: " + fieldTrees.size() + "\n";
//
//		ChangedFile[] LatestSnapshot = getSnapshot(cr, revCount - 1, false);
//		int count = 0;
//		for (ChangedFile cf : LatestSnapshot)
//			if (cf.getName().endsWith(".java"))
//				count++;
////		System.out.println("last snapshot size: " + count);
//		s += "last snapshot size: " + count + "\n";
//
//		long afterUsedMem2 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
////		System.err.println("Before Used " + beforeUsedMem / 1000000.0 + " MB");
//		s += "Before Used " + beforeUsedMem / 1000000.0 + " MB" + "\n";
////		System.err.println("File Tree Used " + afterUsedMem1 / 1000000.0 + " MB");
//		s += "File Tree Used " + afterUsedMem1 / 1000000.0 + " MB" + "\n";
////		System.err.println("Parse Files Used " + afterUsedMem2 / 1000000.0 + " MB");
//		s += "Parse Files Used " + afterUsedMem2 / 1000000.0 + " MB" + "\n";
//		
//		return s;
//	}

}