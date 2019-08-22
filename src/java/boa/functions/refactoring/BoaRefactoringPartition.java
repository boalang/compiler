package boa.functions.refactoring;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import boa.datagen.util.FileIO;

public class BoaRefactoringPartition {

	public static void main(String[] args) {
		int size = 40;
//		int[] inputSizes = new int[size];
		String path = "/Users/hyj/test3/sutton_names.txt";
		String output = "/Users/hyj/test3/partition/";
		
		// even partition
		String input = FileIO.readFileContents(new File(path));
		String[] lines = input.split("\\r?\\n");
		
		List<Pair> pairs = new ArrayList<Pair>();
		for (String line : lines) {
			String[] splits = line.split(" ");
			System.out.println("line: " + line);
			pairs.add(new Pair(splits[0], Integer.parseInt(splits[1])));
		}
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
			BoaRefactoringDetectAll.writeOutputs(partitions.get(j), output + "p" + j + ".txt");
		}
		
		// filter
//		String path1 = "/Users/hyj/test3/output";
//		File dir = new File(path1);
//		Set<Integer> setAll = new HashSet<Integer>();
//		for (int i = 0; i < size; i++)
//			setAll.add(i);
//		
//		for (File file : dir.listFiles()) {
//			String fileName = file.getName();
//			if (fileName.startsWith("o")) {
//				String idxString = fileName.substring(fileName.indexOf('o') + 1, fileName.indexOf('.'));
//				int idx = Integer.parseInt(idxString);
//				setAll.remove(idx);
//			}
//		}
		
//		List<String> names = getNames(output, setAll);
//		for  (int i = 0; i < names.size(); i++)
//			FileIO.writeFileContents(new File("/Users/hyj/test3/undone/u" + i + ".txt"), names.get(i));
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
			return size - o.size;
		}
		
		
	}

//	private static List<String> getNames(String output, Set<Integer> setAll) {
//		List<String> names = new ArrayList<String>();
//		for (int i : setAll) {
//			String input = FileIO.readFileContents(new File(output + "p" + i + ".txt"));
//			String[] projectNames = input.split("\\r?\\n");
//			for (String name : projectNames)
//				names.add(name);
//		}
//		return names;
//	}
	
}