package boa.datagen.util;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class OutputStatusChecker {

	public static void main(String[] args) throws InterruptedException {
		String path = args[0];
		int interval = 10;
		if (args.length > 1)
			interval = Integer.parseInt(args[1]);
		File dir = new File(path);
		Map<String, Long> fileTimes = new HashMap<String, Long>();
		File[] files = dir.listFiles();
		Arrays.sort(files, new Comparator<File>() {
			@Override
			public int compare(File f1, File f2) {
				return f1.compareTo(f2);
			}
		});
		while (true) {
			System.out.println(new Date());
			for (File sub : files) {
				if (!isUpdated(new File(sub, "ast"), fileTimes) && !isUpdated(new File(sub, "project"), fileTimes))
					System.err.println(sub.getAbsolutePath() + " was not updated in the last " + interval + " seconds!!!");
				else
					System.out.println(sub.getAbsolutePath() + " was updated in the last " + interval + " seconds.");
			}
			System.out.println();
			Thread.sleep(interval * 1000);
		}
	}

	private static boolean isUpdated(File dir, Map<String, Long> fileTimes) {
		for (File file : dir.listFiles()) {
			long time = file.lastModified();
			Long t = fileTimes.get(file.getAbsolutePath());
			if (t == null)
				fileTimes.put(file.getAbsolutePath(), time);
			else if (t != time) {
				fileTimes.put(file.getAbsolutePath(), time);
				return true;
			}
		}
		return false;
	}

}
