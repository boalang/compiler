package boa.functions.refactoring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import boa.functions.FunctionSpec;
import boa.types.Code.CodeRefactoring;
import boa.types.Code.CodeRepository;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Shared.ChangeKind;
import boa.types.Toplevel.Project;

import static boa.functions.refactoring.BoaRefactoringIntrinsics.*;
import static boa.functions.BoaAstIntrinsics.*;
import static boa.functions.BoaIntrinsics.*;

public class BoaRefactoringPredictionIntrinsics {

	public static HashMap<String, Integer> DETECTED_TYPE_IDX_MAP = new HashMap<String, Integer>();
	public static HashMap<ChangeKind, Integer> FILE_CHANGE_IDX_MAP = new HashMap<ChangeKind, Integer>();
	public static HashSet<String> REFACTORING_COMMIT_IDS = null;

	public static HashSet<String> DETECTED_REFACTORING_TYPES = new HashSet<String>();
	public static HashSet<String> CLASS_LEVEL_REFACTORING_TYPES = new HashSet<String>();
	public static HashSet<String> METHOD_LEVEL_REFACTORING_TYPES = new HashSet<String>();
	public static HashSet<String> FIELD_LEVEL_REFACTORING_TYPES = new HashSet<String>();

	static {
		for (int i = 0; i < DETECTED_TYPES.length; i++)
			DETECTED_TYPE_IDX_MAP.put(DETECTED_TYPES[i], i);
		ChangeKind[] kinds = new ChangeKind[] { ChangeKind.ADDED, ChangeKind.DELETED, ChangeKind.MODIFIED,
				ChangeKind.RENAMED, ChangeKind.COPIED };
		for (int i = 0; i < kinds.length; i++)
			FILE_CHANGE_IDX_MAP.put(kinds[i], i);
		String[] classLevel = new String[] { "Move Class", "Rename Class", "Extract Superclass", "Extract Interface" };
		String[] methodLevel = new String[] { "Rename Method", "Inline Method", "Extract Method",
				"Extract And Move Method", "Move Method", "Pull Up Method", "Push Down Method" };
		String[] fieldLevel = new String[] { "Move Attribute", "Pull Up Attribute", "Push Down Attribute" };
		for (String s : DETECTED_TYPES)
			DETECTED_REFACTORING_TYPES.add(s);
		for (String s : classLevel)
			CLASS_LEVEL_REFACTORING_TYPES.add(s);
		for (String s : methodLevel)
			METHOD_LEVEL_REFACTORING_TYPES.add(s);
		for (String s : fieldLevel)
			FIELD_LEVEL_REFACTORING_TYPES.add(s);
	}

	private static HashSet<String> getRefactoringIds(Project p) {
		if (REFACTORING_COMMIT_IDS == null)
			REFACTORING_COMMIT_IDS = getRefactoringIdsInSet(p);
		return REFACTORING_COMMIT_IDS;
	}

	@FunctionSpec(name = "get_detected_types", returnType = "array of string")
	public static HashMap<String, Integer> getDetectedTypes() throws Exception {
		return DETECTED_TYPE_IDX_MAP;
	}

	@FunctionSpec(name = "print_ref_stat", formalParameters = { "Project" })
	public static void printRefStat(Project p, Set<String> refTypes) throws Exception {
		HashMap<String, Integer> typeCounts = new HashMap<String, Integer>();
		for (int i = 0; i < DETECTED_TYPES.length; i++)
			typeCounts.put(DETECTED_TYPES[i], 0);
		CodeRepository cr = p.getCodeRepositories(0);
		int revCount = getRevisionsCount(cr);
		HashSet<String> refRevIds = getRefactoringIds(p);
		if (refRevIds.size() == 0)
			return;
		for (int i = 0; i < revCount; i++) {
			Revision r = getRevision(cr, i);
			if (!refRevIds.contains(r.getId()))
				continue;
			List<CodeRefactoring> refs = refTypes == null ? getCodeChange(p, r).getRefactoringsList()
					: getRefactorings(p, r, refTypes);
			for (CodeRefactoring ref : refs) {
				String type = ref.getType();
				if (typeCounts.containsKey(type)) {
					typeCounts.put(type, typeCounts.get(type) + 1);
				}
			}
		}
		for (Entry<String, Integer> entry : typeCounts.entrySet())
			System.out.println(entry.getKey() + " " + entry.getValue());
	}

	private static List<CodeRefactoring> getRefactorings(Project p, Revision r, Set<String> set) {
		List<CodeRefactoring> refs = getCodeChange(p, r).getRefactoringsList();
		refs = filterOutChangePackage(refs);
		refs = filterOutNestedClass(refs);
		List<CodeRefactoring> res = new ArrayList<CodeRefactoring>();
		for (CodeRefactoring ref : refs)
			if (set.contains(ref.getType()))
				res.add(ref);
		return res;
	}

	private static List<CodeRefactoring> filterOutNestedClass(List<CodeRefactoring> refs) {
		List<CodeRefactoring> res = new ArrayList<CodeRefactoring>();
		for (CodeRefactoring ref : refs) {
			if (CLASS_LEVEL_REFACTORING_TYPES.contains(ref.getType())) {
				String filePath = ref.getLeftSideLocations(0).getFilePath();
				String left = ref.getLeftSideLocations(0).getCodeElement();
				left = left.replace(".", "/");
				if (!filePath.contains(left)) {
					continue;
				}
			}
			res.add(ref);
		}
		return res;
	}

	private static List<CodeRefactoring> filterOutChangePackage(List<CodeRefactoring> refs) {
		// remove move class due to change package
		for (CodeRefactoring ref : refs)
			if (ref.getType().equals("Change Package")) {
				String leftPackage = getPackageFromFullyQualifiedName(ref.getLeftSideLocations(0).getCodeElement());
				String rightPackage = getPackageFromFullyQualifiedName(ref.getRightSideLocations(0).getCodeElement());
				List<CodeRefactoring> res = new ArrayList<CodeRefactoring>();
				for (CodeRefactoring refactoring : refs) {
					if (refactoring.getType().equals("Move Class")
							&& refactoring.getLeftSideLocations(0).getCodeElement().contains(leftPackage)
							&& refactoring.getRightSideLocations(0).getCodeElement().contains(rightPackage))
						continue;
					res.add(refactoring);
				}
				return res;
			}
		return refs;
	}

	private static String getPackageFromFullyQualifiedName(String fqn) {
		int idx = fqn.lastIndexOf('.');
		if (idx > 0)
			return fqn.substring(0, idx);
		return fqn;
	}

	@FunctionSpec(name = "test", formalParameters = { "Project" })
	public static void test(Project p) throws Exception {
		Set<String> refTypes = CLASS_LEVEL_REFACTORING_TYPES;
		refTypes.remove("Rename Class");
		refTypes.remove("Extract Superclass");
		refTypes.remove("Extract Interface");

//		printRefStat(p, refTypes);
//		printRefStat(p, DETECTED_REFACTORING_TYPES);
		printRefStat(p, null);

		FileChangeLinkedLists fileLinkedLists = getFileChangeLinkedLists(p, refTypes);
		System.out.println(fileLinkedLists.getLists().size());

		FileChangeLinkedLists refactoringLists = fileLinkedLists.getRefactoringInvolvedLists();
		FileChangeLinkedLists noRefactoringLists = fileLinkedLists.getNotRefactoringInvolvedLists();

		List<Double> nums = new ArrayList<Double>();
		for (Entry<String, FileLinkedList> entry : refactoringLists.getLists().entrySet()) {
			FileLinkedList list = entry.getValue();
			for (int idx : list.getRefCommitIdxToFileListLocMap().values()) {
				nums.add((double) (idx));
//				System.out.println(entry.getKey() + " " + list.getChangeCountAsString() + " "
//						+ list.getRefactoringTypeCountAsString());
			}
		}
		System.out.println(Arrays.toString(getStatisitcs(nums)));
		
		System.out.println("refactoringLists");
		System.out.println(refactoringLists.getLists().size());
		System.out.println(refactoringLists.getRefactoringCount());
		System.out.println(refactoringLists.getFileChangeCount());
		
		System.out.println("noRefactoringLists");
		System.out.println(noRefactoringLists.getLists().size());
		System.out.println(noRefactoringLists.getRefactoringCount());
		System.out.println(noRefactoringLists.getFileChangeCount());
		
		System.out.println("RFileMap");
		HashMap<Integer, List<FileLinkedList>> RFileMap = refactoringLists.getRefCommitIdxToFileLinkedListsMap();
		System.out.println(RFileMap.size());
		System.out.println(getValueCount(RFileMap));
		
		System.out.println("NRFileMap");
		HashMap<Integer, List<FileLinkedList>> NRFileMap = noRefactoringLists.getRefCommitIdToFileLinkedListsMap(RFileMap.keySet());
		System.out.println(NRFileMap.size());
		System.out.println(getValueCount(NRFileMap));
		
		System.out.println("window");
		CodeRepository cr = p.getCodeRepositories(0);
		Window w = new Window(3);
		Set<Integer> refCommitIdxs = RFileMap.keySet();
		int revCount = getRevisionsCount(cr);
//		for (int i = 1; i < revCount; i++) {
		int i = 1654;
			w.slideTo(i);
			List<Integer> refIdxs = w.getContains(refCommitIdxs);
//			if (refIdxs.size() == 0)
//				continue;
			System.out.println("find at " + i);
			System.out.println("refIdxs: " + refIdxs.toString());
			for (int refIdx : refIdxs) {
				if (!RFileMap.containsKey(refIdx))
					System.out.println("ERR");
				for (FileLinkedList list : RFileMap.get(refIdx)) {
					
					FileNode fn = list.getRefFileNode(refIdx);
					fn.getRightRefactorings().get(0);
					
					Revision r = getRevision(cr, refIdx);
					
					System.out.println(r.getId());
					
					System.out.println(r.getFilesCount());
					System.out.println(r.getFiles(0).getChange());
					System.out.println(r.getFiles(0).getPreviousNames(0));
					System.out.println(r.getFiles(0).getName());
					
					System.out.println(fn.getRevision().getId());
					System.out.println(fn.getChangedFile().getName());
					
					System.out.println("list: " + list.getId() + " " + list.getCommitIdxToFileListLocMap().keySet());
					System.out.println(list.getCorrespondingFileNameAt(1448));
					String fileName = list.getCorrespondingFileNameAt(i);
//					System.out.println(fileName);
				}
			}
//			System.out.println(i);
//			ChangedFile[] snapshot = getSnapshot(cr, i, true);
//		}
	}
	
	public static class Window {
		int range = 0;
		int start = -1;
		int end = -1;
		public Window(int range) {
			this.range = range;
		}
		public List<Integer> getContains(Set<Integer> refCommitIdxs) {
			List<Integer> list = new ArrayList<Integer>();
			for (int i = start; i <= end; i++)
				if (refCommitIdxs.contains(i))
					list.add(i);
			return list;
		}
		public void slideTo(int idx) {
			this.start = idx + 1;
			this.end = idx + range;
		}
		public boolean has(int idx) {
			return idx >= start && idx <= end ? true : false;
		}
 	}
	
	private static int getValueCount(HashMap<Integer, List<FileLinkedList>> rFileMap) {
		int count = 0;
		for (List<?> v : rFileMap.values())
			count += v.size();
		return count;
	}

	public static FileChangeLinkedLists getFileChangeLinkedLists(Project p, Set<String> refTypes) throws Exception {
		CodeRepository cr = p.getCodeRepositories(0);
		HashSet<String> refRevIds = getRefactoringIds(p);
		if (refRevIds.size() == 0)
			return null;
		int revCount = getRevisionsCount(cr);
		System.out.println(revCount);
		FileChangeLinkedLists fileLinkedLists = new FileChangeLinkedLists();
		for (int i = 0; i < revCount; i++) {
			Revision r = getRevision(cr, i);
			List<CodeRefactoring> refs = refRevIds.contains(r.getId()) ? getRefactorings(p, r, refTypes)
					: new ArrayList<CodeRefactoring>();
			fileLinkedLists.addAll(r.getFilesList(), r, i, refs);
		}
		return fileLinkedLists;
	}

}
