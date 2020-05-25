package boa.functions.code.change;

import static boa.functions.BoaAstIntrinsics.cleanup;
import static boa.functions.BoaAstIntrinsics.getRefactoringIdsInSet;
import static boa.functions.BoaIntrinsics.getRevisionsCount;
import static boa.functions.BoaIntrinsics.getSnapshot;

import java.util.HashMap;
import java.util.HashSet;
import boa.functions.FunctionSpec;
import boa.functions.code.change.declaration.DeclForest;
import boa.functions.code.change.declaration.DeclNode;
import boa.functions.code.change.declaration.DeclTree;
import boa.functions.code.change.field.FieldForest;
import boa.functions.code.change.field.FieldNode;
import boa.functions.code.change.field.FieldTree;
import boa.functions.code.change.file.FileForest;
import boa.functions.code.change.file.FileTree;
import boa.functions.code.change.method.MethodForest;
import boa.functions.code.change.method.MethodNode;
import boa.functions.code.change.method.MethodTree;
import boa.functions.code.change.refactoring.BoaCodeElementLevel;
import boa.types.Code.CodeRepository;
import boa.types.Diff.ChangedFile;
import boa.types.Toplevel.Project;

public class BoaCodeChangeIntrinsics {

	@FunctionSpec(name = "change_graph_validation", returnType = "array of array of float", formalParameters = { "Project" })
	public static double[][] changeGraphValidation(Project p) throws Exception {

		long beforeUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

		CodeRepository cr = p.getCodeRepositories(0);
		HashSet<String> refRevIds = getRefactoringIdsInSet(p);
		System.out.println(p.getName() + " " + refRevIds.size());

		int revCount = getRevisionsCount(cr);
		ChangeDataBase db = new ChangeDataBase(cr, revCount);
		FileForest forest = new FileForest(db, false);
		HashMap<Integer, FileTree> fileTrees = forest.getTrees();
		
		System.out.println("done file");

		long afterUsedMem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

		forest.updateASTChanges();
		DeclForest declForest = new DeclForest(db, false);
		HashMap<Integer, DeclTree> declTrees = declForest.getTrees();
		
		System.out.println("done decl");
		
		MethodForest methodForest = new MethodForest(db, false);
		HashMap<Integer, MethodTree> methodTrees = methodForest.getTrees();
		
		System.out.println("done method");
		
		FieldForest fieldForest = new FieldForest(db, false);
		HashMap<Integer, FieldTree> fieldTrees = fieldForest.getTrees();
		
		System.out.println("done field");
		
//		forest.updateWithRefs(p, refRevIds);

		double[][] res = new Validation(db).validate();

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

		return res;
	}
	
	@FunctionSpec(name = "entity_count", returnType = "array of int", formalParameters = { "Project" })
	public static long[] entityCount(Project p) throws Exception {
		
		long beforeUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

		CodeRepository cr = p.getCodeRepositories(0);
		HashSet<String> refRevIds = getRefactoringIdsInSet(p);
		System.out.println(p.getName() + " " + refRevIds.size());

		int revCount = getRevisionsCount(cr);
		ChangeDataBase db = new ChangeDataBase(cr, revCount);
		FileForest forest = new FileForest(db, false);
		HashMap<Integer, FileTree> fileTrees = forest.getTrees();
		
		System.out.println("done file");

		long afterUsedMem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

		forest.updateASTChanges();
		DeclForest declForest = new DeclForest(db, false);
		HashMap<Integer, DeclTree> declTrees = declForest.getTrees();
		
		System.out.println("done decl");
		
		MethodForest methodForest = new MethodForest(db, false);
		HashMap<Integer, MethodTree> methodTrees = methodForest.getTrees();
		
		System.out.println("done method");
		
		FieldForest fieldForest = new FieldForest(db, false);
		HashMap<Integer, FieldTree> fieldTrees = fieldForest.getTrees();
		
		System.out.println("done field");
		
		forest.updateWithRefs(p, refRevIds);

//		double[][] res = 
				new Validation(db).validate();
		
		// entity count
		long[] res = new Entity(db).execute();

		cleanup();

		System.out.println("Distinct Files: " + forest.db.fileNames.size());

		System.out.println("Total Revs: " + revCount);

		System.out.println("Total FileTrees: " + fileTrees.size());

		System.out.println("Total refs: " + db.refDB.size());
		
		int classC = 0;
		int methodC = 0;
		int fieldC = 0;
		for (RefactoringBond bond : db.refDB) {
			if (bond.getLevel() == BoaCodeElementLevel.CLASS_LEVEL)
				classC++;
			if (bond.getLevel() == BoaCodeElementLevel.METHOD_LEVEL)
				methodC++;
			if (bond.getLevel() == BoaCodeElementLevel.FIELD_LEVEL)
				fieldC++;
		}
		
		System.out.println("Class Level: " + classC);
		System.out.println("Method Level: " + methodC);
		System.out.println("Field Level: " + fieldC);
		

		System.out.println("Total decl changes: " + db.declDB.size());
		
		int c = 0;
		for (DeclNode n : db.declDB.values()) {
			if (n.leftRefBonds.size() != 0)
				c += n.leftRefBonds.size();
//				c++;
		}
		System.out.println("decls with refs: " + c);

		System.out.println("Total DeclTrees: " + declTrees.size());

		System.out.println("Total method changes: " + db.methodDB.size());
		
		c = 0;
		for (MethodNode n : db.methodDB.values()) {
			if (n.leftRefBonds.size() != 0)
				c += n.leftRefBonds.size();
//				c++;
		}
		System.out.println("methods with refs: " + c);

		System.out.println("Total methodTrees: " + methodTrees.size());

		System.out.println("Total field changes: " + db.fieldDB.size());
		
		c = 0;
		for (FieldNode n : db.fieldDB.values()) {
			if (n.leftRefBonds.size() != 0)
				c += n.leftRefBonds.size();
//				c++;
		}
		System.out.println("fields with refs: " + c);

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
		
		return res;
	}
	
	@FunctionSpec(name = "refactoring_entity_count", returnType = "array of int", formalParameters = { "Project", "string" })
	public static long[] refactoringEntityCount(Project p, String type) throws Exception {
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
		MethodForest methodForest = new MethodForest(db, false);
		FieldForest fieldForest = new FieldForest(db, false);
		forest.updateWithRefs(p, refRevIds, type);
		
		long[] res = new Entity(db).execute();

		cleanup();
		long afterUsedMem2 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		
		System.err.println("Before Used " + beforeUsedMem / 1000000.0 + " MB");

		System.err.println("File Tree Used " + afterUsedMem1 / 1000000.0 + " MB");

		System.err.println("Parse Files Used " + afterUsedMem2 / 1000000.0 + " MB");
		
		return res;
	}

}