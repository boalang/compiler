package boa.functions.code.change.refactoring;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import boa.datagen.util.FileIO;

public class BoaRefactoringPartition {

	public static void main(String[] args) {
		int size = 300;
		String path = "/Users/hyj/hpc_repo_json/names.txt";
		String firstPartition = "/Users/hyj/hpc_repo_json/partition";
		
		// even partition
		String input = FileIO.readFileContents(new File(path));
		String[] lines = input.split("\\r?\\n");
		HashMap<String, Pair> projectNameToPairMap = new HashMap<String, Pair>();
		
		List<Pair> pairs = new ArrayList<Pair>();
		for (String line : lines) {
			String[] splits = line.split(" ");
			Pair pair = new Pair(splits[0], Integer.parseInt(splits[1]));
			pairs.add(pair);
			projectNameToPairMap.put(splits[0], pair);
		}
		
		partitionAndWrite(pairs, firstPartition, size);
		
		
		// filter
//		String outputPath = "/Users/hyj/test4/output";
//		String undonePartitionPath = "/Users/hyj/test4/undone";
//		String undoneOutputPath = "/Users/hyj/test4/undone_output";
//		String unundonePartitionPath = "/Users/hyj/test4/unundone";
//		String unundoneOutputPath = "/Users/hyj/test4/unundone_output";
		
		// undone
//		Set<Integer> undoneFromFirstPartition = getUndoneFileIndexs(outputPath, 300);
//		System.out.println(undoneFromFirstPartition);
//		System.out.println(undoneFromFirstPartition.size());
//		Set<String> undoneNames = getNamesByFileIndexs(firstPartition, undoneFromFirstPartition);
//		System.out.println("Undone names from first partition: " + undoneNames.size());
		
		// undone validation
//		Set<String> undoneNames = getProjectNamesFromPartitionPath(new File(undonePartitionPath));
//		names.removeAll(undoneNames);
//		System.out.println(names);
		
		// unundone
//		Set<Integer> unundoneFromundonePartition = getUndoneFileIndexs(undoneOutputPath, 100);
//		System.out.println(unundoneFromundonePartition);
//		System.out.println(unundoneFromundonePartition.size());
//		Set<String> unundoneNames = getNamesByFileIndexs(undonePartitionPath, unundoneFromundonePartition);
//		System.out.println("Unundone names from undone partition: " + unundoneNames.size());
		
		// ununundone
//		Set<Integer> ununundoneFromUndonePartition = getUndoneFileIndexs(unundoneOutputPath, 100);
//		System.out.println(ununundoneFromUndonePartition);
//		System.out.println(ununundoneFromUndonePartition.size());
//		Set<String> ununundoneNames = getNamesByFileIndexs(unundonePartitionPath, ununundoneFromUndonePartition);
//		System.out.println("Ununundone names from unundone partition: " + ununundoneNames.size());
//		
//		System.out.println(ununundoneNames);
		
		// parition unundone
//		List<Pair> undonePairs = new ArrayList<Pair>();
//		for (String name : ununundoneNames)
//			undonePairs.add(projectNameToPairMap.get(name));
//		String output1 = "/Users/hyj/test4/ununundone/";
//		partitionAndWrite(undonePairs, output1, 11);
		
		// unundone validation
//		Set<String> names = getProjectNamesFromPartitionPath(new File(unundonePartitionPath));
//		unundoneNames.removeAll(names);
//		System.out.println(unundoneNames);
		
		
		
	}
	
	private static HashSet<Integer> getUndoneFileIndexs(String path, int size) {
		File dir = new File(path);
		HashSet<Integer> set = new HashSet<Integer>();
		for (int i = 0; i < size; i++)
			set.add(i);
		
		for (File file : dir.listFiles()) {
			String fileName = file.getName();
			if (fileName.startsWith("o")) {
				String idxString = fileName.substring(fileName.indexOf('o') + 1, fileName.indexOf('.'));
				int idx = Integer.parseInt(idxString);
				set.remove(idx);
			}
		}
		return set;
	}
	
	private static HashSet<String> getProjectNamesFromPartitionPath(File file) {
		HashSet<String> names = new HashSet<String>();
		for (File nameList : file.listFiles()) {
			String input = FileIO.readFileContents(nameList);
			String[] projectNames = input.split("\\r?\\n");
			for (String name : projectNames)
				names.add(name);
		}
		return names;
	}
	
	static class Pair implements Comparable<Pair>{
		int size;
		String name;
		
		Pair(String name, int size) {
			this.name = name;
			this.size = size;
		}

		@Override
		public int compareTo(Pair o) {
			return this.size - o.size;
		}
		
		
	}
	
	private static void partitionAndWrite(List<Pair> pairs, String output, int size) {
		Collections.sort(pairs);
		List<List<String>> partitions = new ArrayList<List<String>>(size);
		for (int i = 0; i < size; i++)
			partitions.add(new ArrayList<String>());
		
		int i = 0;
		for (Pair p : pairs) {
			partitions.get(i++).add(p.name);
			if (i == size)
				i = 0;
		}
		
		for (int j = 0; j < partitions.size(); j++) {
			BoaRefactoringDetectAll.writeOutputs(partitions.get(j), output + "/p" + j + ".txt");
		}
	}

	private static HashSet<String> getNamesByFileIndexs(String partitionPath, Set<Integer> indexs) {
		HashSet<String> names = new HashSet<String>();
		for (int i : indexs) {
			String input = FileIO.readFileContents(new File(partitionPath + "/p" + i + ".txt"));
			String[] projectNames = input.split("\\r?\\n");
			for (String name : projectNames)
				names.add(name);
		}
		return names;
	}
	
}