package boa.functions.refactoring;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import boa.datagen.util.FileIO;

public class BoaRefactoringPartition {

	public static void main(String[] args) {
		
		int[] inputSizes = new int[300];
		String path = "/Users/hyj/test3/sutton_names.txt";
		String output = "/Users/hyj/test3/partition/";
		
		String input = FileIO.readFileContents(new File(path));
		String[] projectNames = input.split("\\r?\\n");
		
		int num = projectNames.length / inputSizes.length;
		for (int i = 0; i < inputSizes.length; i++)
			inputSizes[i] = num;
		int remainder = projectNames.length - num * inputSizes.length;
		for (int i = 0; i < remainder; i++)
			inputSizes[i] += 1;
		
		List<List<String>> partitions = new ArrayList<List<String>>(inputSizes.length);
		for (int i = 0; i < inputSizes.length; i++)
			partitions.add(new ArrayList<String>());
		
		int i = 0;
		for (String name : projectNames) {
			partitions.get(i++).add(name);
			if (i == inputSizes.length)
				i = 0;
		}
		
		for (int j = 0; j < partitions.size(); j++) {
			BoaRefactoringDetectAll.writeOutputs(partitions.get(j), output + "p" + j + ".txt");
		}
	}
	
}