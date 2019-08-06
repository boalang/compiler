package boa.functions.refactoring;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import boa.datagen.util.FileIO;

public class BoaRefactoringPartition {

	public static void main(String[] args) {
		int size = 300;
		int[] inputSizes = new int[size];
		String path = "/Users/hyj/test3/sutton_names.txt";
		String output = "/Users/hyj/test3/partition/";
		
		// partition
//		String input = FileIO.readFileContents(new File(path));
//		String[] projectNames = input.split("\\r?\\n");
		
//		int num = projectNames.length / inputSizes.length;
//		for (int i = 0; i < inputSizes.length; i++)
//			inputSizes[i] = num;
//		int remainder = projectNames.length - num * inputSizes.length;
//		for (int i = 0; i < remainder; i++)
//			inputSizes[i] += 1;
//		
//		List<List<String>> partitions = new ArrayList<List<String>>(inputSizes.length);
//		for (int i = 0; i < inputSizes.length; i++)
//			partitions.add(new ArrayList<String>());
//		
//		int i = 0;
//		for (String name : projectNames) {
//			partitions.get(i++).add(name);
//			if (i == inputSizes.length)
//				i = 0;
//		}
//		
//		for (int j = 0; j < partitions.size(); j++) {
//			BoaRefactoringDetectAll.writeOutputs(partitions.get(j), output + "p" + j + ".txt");
//		}
		
		// filter
		String path1 = "/Users/hyj/test3/output";
		File dir = new File(path1);
		Set<Integer> setAll = new HashSet<Integer>();
		for (int i = 0; i < size; i++)
			setAll.add(i);
		
		for (File file : dir.listFiles()) {
			String fileName = file.getName();
			if (fileName.startsWith("o")) {
				String idxString = fileName.substring(fileName.indexOf('o') + 1, fileName.indexOf('.'));
				int idx = Integer.parseInt(idxString);
				setAll.remove(idx);
			}
		}
		
		List<String> names = getNames(output, setAll);
		for  (int i = 0; i < names.size(); i++)
			FileIO.writeFileContents(new File("/Users/hyj/test3/undone/u" + i + ".txt"), names.get(i));
	}

	private static List<String> getNames(String output, Set<Integer> setAll) {
		List<String> names = new ArrayList<String>();
		for (int i : setAll) {
			String input = FileIO.readFileContents(new File(output + "p" + i + ".txt"));
			String[] projectNames = input.split("\\r?\\n");
			for (String name : projectNames)
				names.add(name);
		}
		return names;
	}
	
}