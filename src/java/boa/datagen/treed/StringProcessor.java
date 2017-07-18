package boa.datagen.treed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class StringProcessor {
	public static void main(String[] args) {
		System.out.println(serialize2("Set"));
	}
	
	public static String[] splitAttributeValue(String attributeValue)
	{
		int index = attributeValue.indexOf('>');
		String[] parts = new String[2];
		parts[0] = attributeValue.substring(0, index+1);
		parts[1] = attributeValue.substring(index+1);
		return parts;
	}
	
	public static String trimQuotes(String value)
	{
		if (value.length() >= 2 && (value.charAt(0) == '\'' || value.charAt(0) == '\"') && value.charAt(0) == value.charAt(value.length()-1))
			return trimQuotes(value.substring(1, value.length()-1));
		return value;
	}
	
	public ArrayList<ArrayList<Integer>> match(String str, String target)
	{
		return match(serialize2(str), serialize2(target));
	}
	public static ArrayList<ArrayList<Integer>> match(ArrayList<String> str, ArrayList<String> target)
	{
		int lenM = str.size(), lenN = target.size();
		int[][] d = new int[lenM+1][lenN+1];
		String[] codeM = new String[lenM+1];
		String[] codeN = new String[lenN+1];
		char[][] p = new char[lenM+1][lenN+1];
		d[0][0] = 0;
		for(int i = 1; i <= lenM; i++)
		{
			d[i][0] = 0;
			codeM[i] = str.get(i-1);
		}
		for(int i = 1; i <= lenN; i++)
		{
			d[0][i] = 0;
			codeN[i] = target.get(i-1);
		}
		for(int i = 1; i <= lenM; i++)
		{
			for(int j = 1; j <= lenN; j++)
			{
				if(codeM[i].equals(codeN[j]))
				{
					d[i][j] = d[i-1][j-1] + 1;
					p[i][j] = 'D';
				}
				else if(d[i-1][j] >= d[i][j-1])
				{
					d[i][j] = d[i-1][j];
					p[i][j] = 'U';
				}
				else
				{
					d[i][j] = d[i][j-1];
					p[i][j] = 'L';
				}
			}
		}
		ArrayList<Integer> lcsM = new ArrayList<Integer>();
		ArrayList<Integer> lcsN = new ArrayList<Integer>();
		printLCS(p, lenM, lenN, lcsM, lcsN);
		
		ArrayList<ArrayList<Integer>> pos = new ArrayList<ArrayList<Integer>>();
		pos.add(lcsM); pos.add(lcsN);
		return pos;
	}
	private static void printLCS(char[][] p, int lenM, int lenN, ArrayList<Integer> lcsM, ArrayList<Integer> lcsN)
	{
		int i = lenM, j = lenN;
		while(i > 0 && j > 0)
		{
			if(p[i][j] =='D')
			{
				lcsM.add(0, i-1);
				lcsN.add(0, j-1);
				i--; j--;
			}
			else if(p[i][j] == 'U')
				i--;
			else
				j--;
		}
	}
	public static ArrayList<String> serialize(String text)
	{
		ArrayList<String> list = new ArrayList<String>();
		int start = 0;
		while(start < text.length())
		{
			String word = "";
			while(start < text.length() && !Character.isLetterOrDigit(text.charAt(start)))
				start++;
			if(start < text.length())
			{
				int end = start;
				if(Character.isDigit(text.charAt(start)))
				{
					while(end+1 < text.length() && Character.isDigit(text.charAt(end+1)))
						end++;
				}
				else if(Character.isLowerCase(text.charAt(start)))
				{
					while(end+1 < text.length() && Character.isLowerCase(text.charAt(end+1)))
						end++;
				}
				else if(Character.isUpperCase(text.charAt(start)))
				{
					if(end+1 < text.length())
					{
						char ch = text.charAt(end+1);
						if(Character.isUpperCase(ch))
						{
							end++;
							while(end+1 < text.length() && Character.isUpperCase(text.charAt(end+1)))
								end++;
							if(end+1 < text.length() && Character.isLowerCase(text.charAt(end+1)))
								end--;
						}
						else if(Character.isLowerCase(ch))
						{
							end++;
							while(end+1 < text.length() && Character.isLowerCase(text.charAt(end+1)))
								end++;
						}
					}
				}
				else
				{
					System.out.println("What should be else???");
				}
				word = text.substring(start, end+1);
				start = end + 1;
			}
			if(word != "")
				list.add(word.toLowerCase());
		}
		
		return list;
	}
	public static ArrayList<String> serialize2(String text)
	{
		ArrayList<String> list = new ArrayList<String>();
		int start = 0;
		while(start < text.length())
		{
			String word = "";
			while(start < text.length() && !Character.isLetterOrDigit(text.charAt(start)))
			{
				word += text.charAt(start);
				start++;
			}
			if (!word.isEmpty())
			{
				list.add(word);
				word = "";
			}
			if(start < text.length())
			{
				int end = start;
				if(Character.isDigit(text.charAt(start)))
				{
					while(end+1 < text.length() && Character.isDigit(text.charAt(end+1)))
						end++;
				}
				else if(Character.isLowerCase(text.charAt(start)))
				{
					while(end+1 < text.length() && Character.isLowerCase(text.charAt(end+1)))
						end++;
				}
				else if(Character.isUpperCase(text.charAt(start)))
				{
					if(end+1 < text.length())
					{
						char ch = text.charAt(end+1);
						if(Character.isUpperCase(ch))
						{
							end++;
							while(end+1 < text.length() && Character.isUpperCase(text.charAt(end+1)))
								end++;
							if(end+1 < text.length() && Character.isLowerCase(text.charAt(end+1)))
								end--;
						}
						else if(Character.isLowerCase(ch))
						{
							end++;
							while(end+1 < text.length() && Character.isLowerCase(text.charAt(end+1)))
								end++;
						}
					}
				}
				else
				{
					System.out.println("What should be else???");
				}
				word = text.substring(start, end+1);
				start = end + 1;
			}
			list.add(word);
		}
		
		return list;
	}
	public static String getToken(String source, String target)
	{
		char ch = source.charAt(0);
		if(!Character.isLetter(ch) || Character.isLowerCase(ch))
			return target;
		if(source.length() == 1 || Character.isUpperCase(source.charAt(1)))
			return target.toUpperCase();
		return target.substring(0, 1).toUpperCase() + target.substring(1);
	}
	/**
	 * 
	 * @param sequence1 input sequence 1
	 * @param sequence2 input sequence 2
	 * @param neighborhood the number of surrounding matched lines
	 * @param minNeighborhood should be 0
	 * @param lcsM indexes of matches in sequence 1
	 * @param lcsN indexes of matches in sequence 2
	 */
	public static void doLCS(ArrayList<String> sequence1, ArrayList<String> sequence2, int neighborhood, int minNeighborhood, ArrayList<Integer> lcsM, ArrayList<Integer> lcsN)
	{
		HashMap<String, Integer> itemIndexes = new HashMap<>();
		index(sequence1, itemIndexes);
		index(sequence2, itemIndexes);
		ArrayList<Integer> matchesM = new ArrayList<Integer>();
		for (int i = 0; i < sequence1.size(); i++)
			matchesM.add(-1);
		ArrayList<Integer> matchesN = new ArrayList<Integer>();
		for (int i = 0; i < sequence2.size(); i++)
			matchesN.add(-1);
		doLCS(sequence1, sequence2, 0, sequence1.size()-1, 0, sequence2.size()-1, neighborhood, minNeighborhood, matchesM, matchesN);
		for (int i = 0; i < matchesM.size(); i++)
		{
			int j = matchesM.get(i);
			if (j > -1)
			{
				lcsM.add(i);
				lcsN.add(j);
			}
		}
	}
	private static void index(ArrayList<String> sequence, HashMap<String, Integer> itemIndexes) {
		for (int i = 0; i < sequence.size(); i++) {
			String item = sequence.get(i);
			int index = itemIndexes.size();
			if (itemIndexes.containsKey(item)) index = itemIndexes.get(item);
			else itemIndexes.put(item, index);
			sequence.set(i, String.valueOf(index));
		}
	}

	public static void getLCS(ArrayList<String> term1, ArrayList<String> term2, int start1, int end1, int start2, int end2, int neighborhood, int min, ArrayList<Integer> lcsM, ArrayList<Integer> lcsN)
	{
		ArrayList<Integer> matchesM = new ArrayList<Integer>();
		for (int i = 0; i < term1.size(); i++)
			matchesM.add(-1);
		ArrayList<Integer> matchesN = new ArrayList<Integer>();
		for (int i = 0; i < term2.size(); i++)
			matchesN.add(-1);
		doLCS(term1, term2, start1, end1, start2, end2, neighborhood, min, matchesM, matchesN);
		for (int i = 0; i < matchesM.size(); i++)
		{
			int j = matchesM.get(i);
			if (matchesM.get(i) > -1)
			{
				lcsM.add(i);
				lcsN.add(j);
			}
		}
	}
	/**
	 * Largest Common Sub-sequence
	 * @param sequence1
	 * @param sequence2
	 * @param neighborhood
	 * @param lcsM
	 * @param lcsN
	 * @param startM
	 * @param endM
	 * @param startN
	 * @param endN
	 */
	public static void doLCS(ArrayList<String> sequence1, ArrayList<String> sequence2, int startM, int endM, int startN, int endN, int neighborhood, int minNeighborhood, ArrayList<Integer> lcsM, ArrayList<Integer> lcsN)
	{
		int lenM = endM - startM + 1, lenN = endN - startN + 1;
		int[][] d = new int[2][lenN+1];
		String[] codeM = new String[lenM+1];
		String[] codeN = new String[lenN+1];
		char[][] p = new char[lenM+1][lenN+1];
		d[1][0] = 0;
		for(int i = 1; i <= lenM; i++)
		{
			codeM[i] = sequence1.get(startM+i-1);
		}
		for(int j = 1; j <= lenN; j++)
		{
			d[1][j] = 0;
			codeN[j] = sequence2.get(startN+j-1);
		}
		ArrayList<String> iNeighbors = new ArrayList<String>();
		for (int i = 0; i < neighborhood+1; i++)
		{
			if (startM-1+i < sequence1.size() && startM-1+i >= neighborhood)
			{
				StringBuffer buf = new StringBuffer();
				for (int l = 0; l < neighborhood+1; l++)
					buf.append(sequence1.get(startM-1+i-neighborhood+l) + " ");
				iNeighbors.add(buf.toString());
			}
			else
				iNeighbors.add("");
		}
		for(int i = 1; i <= lenM; i++)
		{
			for (int j = 0; j <= lenN; j++) {
				d[0][j] = d[1][j];
			}
			if (neighborhood > 0)
			{
				iNeighbors.remove(0);
				if (lenM-i >= neighborhood)
				{
					String pre = iNeighbors.get(neighborhood-1);
					//if (pre.equals(""))
					if (startM+i-1+neighborhood < sequence1.size())
					{
						StringBuffer buf = new StringBuffer();
						for (int k = i; k <= i+neighborhood; k++)
							//buf.append(codeM[k] + " ");
							buf.append(sequence1.get(startM+k-1) + " ");
						iNeighbors.add(buf.toString());
					}
					else
						//iNeighbors.add(pre.substring(codeM[i-1].length()+1) + codeM[i+neighborhood] + " ");
						iNeighbors.add(pre.substring(sequence1.get(startM+i-2).length()+1) + sequence1.get(startM+i-1+neighborhood) + " ");
				}
				else
					iNeighbors.add("");
			}
			ArrayList<String> jNeighbors = new ArrayList<String>();
			for (int k = 0; k < neighborhood+1; k++)
			{
				if (startN-1+k < sequence2.size() && startN-1+k >= neighborhood)
				{
					StringBuffer buf = new StringBuffer();
					for (int l = 0; l < neighborhood+1; l++)
						buf.append(sequence2.get(startN-1+k-neighborhood+l) + " ");
					jNeighbors.add(buf.toString());
				}
				else
					jNeighbors.add("");
			}
			for(int j = 1; j <= lenN; j++)
			{
				if (neighborhood > 0)
				{
					jNeighbors.remove(0);
					//if (lenN-j >= neighborhood)
					if (startN+j-1+neighborhood < sequence2.size())
					{
						String pre = jNeighbors.get(neighborhood-1);
						if (pre.equals(""))
						{
							StringBuffer buf = new StringBuffer();
							for (int k = j; k <= j+neighborhood; k++)
								//buf.append(codeN[k] + " ");
								buf.append(sequence2.get(startN+k-1) + " ");
							jNeighbors.add(buf.toString());
						}
						else
						{
							//if (codeN[j-1].length() < 0)
							if (sequence2.get(startN+j-2).length() < 0)
								System.err.println("WTF");
							//jNeighbors.add(pre.substring(codeN[j-1].length()+1) + codeN[j+neighborhood] + " ");
							jNeighbors.add(pre.substring(sequence2.get(startN+j-2).length()+1) + sequence2.get(startN+j-1+neighborhood) + " ");
						}
					}
					else
						jNeighbors.add("");
				}
				boolean isMatched = false;
				if (codeM[i].equals(codeN[j]))
				{
					if (neighborhood == 0)
						isMatched = true;
					else 
						for (int k = 0; k < neighborhood+1; k++)
							if (!iNeighbors.get(k).equals("") && iNeighbors.get(k).equals(jNeighbors.get(k)))
							{
								isMatched = true;
								break;
							}
				}
				if(isMatched)
				{
					d[1][j] = d[0][j-1] + 1;
					p[i][j] = 'D';
				}
				else if(d[0][j] >= d[1][j-1])
				{
					d[1][j] = d[0][j];
					p[i][j] = 'U';
				}
				else
				{
					d[1][j] = d[1][j-1];
					p[i][j] = 'L';
				}
			}
		}
		int i = lenM, j = lenN;
		int preM = lenM+1, preN = lenN+1;
		while(i > 0 && j > 0)
		{
			if(p[i][j] == 'D')
			{
				lcsM.set(startM+i-1, startN+j-1);
				lcsN.set(startN+j-1, startM+i-1);
				if (neighborhood > 0 && neighborhood >= 2 * minNeighborhood)
				{
					if (i < preM-1 && j < preN-1)
						doLCS(sequence1, sequence2, startM+i, startM+preM-2, startN+j, startN+preN-2, (neighborhood-1)/2, minNeighborhood, lcsM, lcsN);
					preM = i; preN = j;
				}	
				i--; j--;
			}
			else if(p[i][j] == 'U')
				i--;
			else
				j--;
		}
		if (neighborhood > 0 && neighborhood >= 2 * minNeighborhood && preM > 1 && preN > 1)
			doLCS(sequence1, sequence2, startM, startM+preM-2, startN, startN+preN-2, (neighborhood-1)/2, minNeighborhood, lcsM, lcsN);
	}
	/**
	 * Largest Similar Sub-sequence
	 * @param term1
	 * @param term2
	 * @param neighborhood
	 * @param lcsM
	 * @param lcsN
	 * @param startM
	 * @param endM
	 * @param startN
	 * @param endN
	 */
	public static void doLSS(ArrayList<String> term1, ArrayList<String> term2, int neighborhood, ArrayList<Integer> lcsM, ArrayList<Integer> lcsN, int startM, int endM, int startN, int endN) {
		int lenM = endM - startM + 1, lenN = endN - startN + 1;
		double[][] d = new double[lenM + 1][lenN + 1];
		String[] codeM = new String[lenM + 1];
		String[] codeN = new String[lenN + 1];
		String[][] p = new String[lenM + 1][lenN + 1];
		d[0][0] = 0;
		for (int i = 1; i <= lenM; i++) {
			d[i][0] = 0;
			codeM[i] = term1.get(startM + i - 1);
		}
		for (int i = 1; i <= lenN; i++) {
			d[0][i] = 0;
			codeN[i] = term2.get(startN + i - 1);
		}
		for (int i = 1; i <= lenM; i++) {
			for (int j = 1; j <= lenN; j++) {
				double sim = 1;
				if (!codeM[i].equals(codeN[j])) {
					sim = computeCharLCS(serializeToChars(codeM[i]), serializeToChars(codeN[j]));
				}
				if (sim >= 0.8) {
					d[i][j] = d[i - 1][j - 1] + sim;
					p[i][j] = "LU";
				} else if (d[i - 1][j] >= d[i][j - 1]) {
					d[i][j] = d[i - 1][j];
					p[i][j] = "U";
				} else {
					d[i][j] = d[i][j - 1];
					p[i][j] = "L";
				}
			}
		}
		int i = lenM, j = lenN;
		while (i > 0 && j > 0) {
			if (p[i][j].equals("LU")) {
				lcsM.set(startM + i - 1, startN + j - 1);
				lcsN.set(startN + j - 1, startM + i - 1);
				i--;
				j--;
			} else if (p[i][j].equals("U"))
				i--;
			else
				j--;
		}
	}

	public static ArrayList<Character> serializeToChars(String str) {
		ArrayList<Character> list = new ArrayList<Character>();
		for (int i = 0; i < str.length(); i++)
			list.add(str.charAt(i));
		return list;
	}
	
	public static double computeCharLCS(ArrayList<Character> term1, ArrayList<Character> term2) {
		int lenM = term1.size(), lenN = term2.size();
		int[][] d = new int[lenM + 1][lenN + 1];
		char[] codeM = new char[lenM + 1];
		char[] codeN = new char[lenN + 1];
		String[][] p = new String[lenM + 1][lenN + 1];
		d[0][0] = 0;
		for (int i = 1; i <= lenM; i++) {
			d[i][0] = 0;
			codeM[i] = term1.get(i - 1);
		}
		for (int i = 1; i <= lenN; i++) {
			d[0][i] = 0;
			codeN[i] = term2.get(i - 1);
		}
		for (int i = 1; i <= lenM; i++) {
			for (int j = 1; j <= lenN; j++) {
				if (codeM[i] == codeN[j]) {
					d[i][j] = d[i - 1][j - 1] + 1;
					p[i][j] = "LU";
				} else if (d[i - 1][j] >= d[i][j - 1]) {
					d[i][j] = d[i - 1][j];
					p[i][j] = "U";
				} else {
					d[i][j] = d[i][j - 1];
					p[i][j] = "L";
				}
			}
		}
		int matches = d[lenM][lenN];

		return matches * 2.0 / (lenM + lenN);
	}

	public static void doLCS(String s1, String s2, ArrayList<Integer> lcs1, ArrayList<Integer> lcs2) {
		int lenM = s1.length(), lenN = s2.length();
		double[][] d = new double[lenM+1][lenN+1];
		char[] codeM = new char[lenM+1];
		char[] codeN = new char[lenN+1];
		char[][] p = new char[lenM+1][lenN+1];
		d[0][0] = 0;
		for(int i = 1; i <= lenM; i++)
		{
			d[i][0] = 0;
			codeM[i] = s1.charAt(i-1);
		}
		for(int i = 1; i <= lenN; i++)
		{
			d[0][i] = 0;
			codeN[i] = s2.charAt(i-1);
		}
		for(int i = 1; i <= lenM; i++)
		{
			for(int j = 1; j <= lenN; j++)
			{
				if(codeM[i] == codeN[j])
				{
					d[i][j] = d[i-1][j-1] + 1;
					p[i][j] = 'D';
				}
				else if(d[i-1][j] >= d[i][j-1])
				{
					d[i][j] = d[i-1][j];
					p[i][j] = 'U';
				}
				else
				{
					d[i][j] = d[i][j-1];
					p[i][j] = 'L';
				}
			}
		}
		printLCS(p, lenM, lenN, lcs1, lcs2);
	}
	
	public static ArrayList<ArrayList<Integer>> doMSA(ArrayList<String> sequences) {
		ArrayList<ArrayList<Integer>> alignments = new ArrayList<ArrayList<Integer>>();
		String s0 = sequences.get(0);
		for (int i = 0; i < s0.length(); i++) {
			ArrayList<Integer> a = new ArrayList<Integer>();
			a.add(i);
			alignments.add(a);
		}
		for (int i = 1; i < sequences.size(); i++) {
			ArrayList<Integer> lcs1 = new ArrayList<Integer>(), lcs2 = new ArrayList<Integer>();
			doLCS(s0, sequences.get(i), lcs1, lcs2);
			update(alignments, lcs1, lcs2);
		}
		
		return alignments;
	}

	private static void update(ArrayList<ArrayList<Integer>> alignments,
			ArrayList<Integer> lcs0, ArrayList<Integer> lcs) {
		int i = 0;
		while (i < alignments.size()) {
			int loc = alignments.get(i).get(0);
			int id = Collections.binarySearch(lcs0, loc);
			if (id < 0) {
				alignments.remove(i);
			}
			else {
				alignments.get(i).add(lcs.get(id));
				i++;
			}
		}
	}
}
