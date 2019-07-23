package boa.functions;

import boa.types.Ast.Method;
import boa.types.Code.CodeRepository;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.ChangeKind;
import gr.uom.java.xmi.UMLModel;

import static boa.functions.BoaAstIntrinsics.*;
import static boa.functions.BoaIntrinsics.*;
//import static org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl.*;
import static boa.functions.BoaRefactoringDetectionIntrinsics.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.refactoringminer.api.Refactoring;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;

public class BoaRefactoringDetectionUtil {

	@FunctionSpec(name = "getmethodname", returnType = "string", formalParameters = { "Method" })
	public static String getMethodName(Method m) throws Exception {
		String res = "";
		res += m.getName();
		for (int i = 0; i < m.getArgumentsCount(); i++) {
			if (i == 0)
				res += "(" + m.getArguments(0).getName() + " " + m.getArguments(0).getVariableType().getName();
			res += ", " + m.getArguments(0).getName() + " " + m.getArguments(0).getVariableType().getName();
			if (i == m.getArgumentsCount() - 1)
				res += ")";
		}
		if (!m.getName().equals("<init>"))
			res += " : " + m.getReturnType().getName();
		return res;
	}

	@FunctionSpec(name = "detectrefactorings", returnType = "array of string", formalParameters = { "CodeRepository", "string" })
	public static String[] detectRefactorings(final CodeRepository cr, final String id) throws Exception {
		Revision currentRevision = getRevisionById(cr, id);
		Map<String, String> renamedFilesHint = new HashMap<String, String>();
		
		if (updateRenamedFilesHintAndCheckRefactoringPossibility(currentRevision, renamedFilesHint)) {
			// before
			Map<String, String> fileContentsBefore = new LinkedHashMap<String, String>();
			Set<String> repositoryDirectoriesBefore = new LinkedHashSet<String>();
			ChangedFile[] snapshotBefore = getSnapshotByIndex(cr, currentRevision.getParents(0));
			updateFileContents(fileContentsBefore, repositoryDirectoriesBefore, snapshotBefore);
			UMLModel modelBefore = GitHistoryRefactoringMinerImpl.createModel(fileContentsBefore, repositoryDirectoriesBefore);
			// current
			Map<String, String> fileContentsCurrent = new LinkedHashMap<String, String>();
			Set<String> repositoryDirectoriesCurrent = new LinkedHashSet<String>();
			ChangedFile[] snapshotCurrent = updateSnapshotByRevision(snapshotBefore, currentRevision);
			updateFileContents(fileContentsCurrent, repositoryDirectoriesCurrent, snapshotCurrent);
			UMLModel modelCurrent = GitHistoryRefactoringMinerImpl.createModel(fileContentsCurrent, repositoryDirectoriesCurrent);
			
			List<Refactoring> refactoringsAtRevision = modelBefore.diff(modelCurrent, renamedFilesHint)
					.getRefactorings();
			String[] res = new String[refactoringsAtRevision.size()];
			for (int i = 0; i < res.length; i++)
				res[i] = refactoringsAtRevision.get(i).toString();
			return res;
		}
		
		return null;
	}

	private static void updateFileContents(Map<String, String> fileContents, Set<String> repositoryDirectories,
			ChangedFile[] snapshot) {
		for (ChangedFile cf : snapshot) {
			String pathString = cf.getName();
			if (isJavaFile(pathString)) {
				fileContents.put(pathString, getContent(cf));
				String directory = pathString.substring(0, pathString.lastIndexOf("/"));
				repositoryDirectories.add(directory);
				String subDirectory = new String(directory);
				while (subDirectory.contains("/")) {
					subDirectory = subDirectory.substring(0, subDirectory.lastIndexOf("/"));
					repositoryDirectories.add(subDirectory);
				}
			}
		}
	}
	
	@FunctionSpec(name = "updatesnapshotbyrevision", returnType = "array of ChangedFile", formalParameters = { "array of ChangedFile", "Revision" })
	public static ChangedFile[] updateSnapshotByRevision(ChangedFile[] snapshot, Revision r) {
		HashMap<String, ChangedFile> map = new HashMap<String, ChangedFile>();
		for (ChangedFile cf : snapshot)
			map.put(cf.getName(), cf);
		ArrayList<ChangedFile> files = new ArrayList<ChangedFile>();
		for (ChangedFile cf : r.getFilesList())
			files.add(getParsedChangedFile(cf));
		ArrayList<ChangedFile> copies = new ArrayList<ChangedFile>();
		for (ChangedFile cf : files) {
			ChangeKind ck = cf.getChange();
			switch (ck) {
				case ADDED:
					map.put(cf.getName(), cf);
					break;
				case COPIED:
					copies.add(cf);
					break;
				case DELETED:
					map.remove(cf.getName());
					break;
				case RENAMED:
					for (String name : cf.getPreviousNamesList())
						map.remove(name);
					map.put(cf.getName(), cf);
					break;
				case MODIFIED:
					map.replace(cf.getName(), cf);
					break;
				default:
					map.put(cf.getName(), cf);
					break;
			}
		}
		return map.values().toArray(new ChangedFile[0]);
	}
	
	private static boolean updateRenamedFilesHintAndCheckRefactoringPossibility(Revision r, Map<String, String> renamedFilesHint) {
		int javaFileCount = 0;
		int adds = 0;
		int removes = 0;
		for (ChangedFile cf : r.getFilesList()) {
			if (isJavaFile(cf.getName())) {
				javaFileCount++;
				switch (cf.getChange()) {
					case RENAMED:
						renamedFilesHint.put(cf.getPreviousNames(0), cf.getName());
						break;
					case ADDED:
						adds++;
						break;
					case DELETED:
						removes++;
						break;
					default:
						break;
				}
			}
		}
		// If all files are added or deleted or non-java files, then there is no refactoring.
		return !(Math.max(adds, removes) == javaFileCount || javaFileCount == 0);
	}

	private static boolean isJavaFile(String path) {
		return path.endsWith(".java");
	}
}
