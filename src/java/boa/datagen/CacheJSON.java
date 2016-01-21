package boa.datagen;

import java.io.File;
import java.util.concurrent.*;

import boa.datagen.forges.sfnet.JSONProjectCacher;
import boa.datagen.util.Properties;

/**
 * @author rdyer
 */
public class CacheJSON {
	final static File jsonDir = new File(Properties.getProperty("sf.json.path", DefaultProperties.SF_JSON_PATH));
	final static File jsonCacheDir = new File(Properties.getProperty("sf.json.cache.path", DefaultProperties.SF_JSON_CACHE_PATH));

	public static void main(String[] args) {
		final long startTime = System.currentTimeMillis();
		
		ExecutorService pool = Executors.newFixedThreadPool(Integer.parseInt(Properties.getProperty("num.threads", DefaultProperties.NUM_THREADS)));

		for (final File file : jsonDir.listFiles()) {
			if (!file.getName().endsWith(".json.txt"))
				continue;

			pool.submit(new CacheTask(file));
		}

		pool.shutdown();
		try {
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) { }

		System.out.println("Time: " + (System.currentTimeMillis() - startTime) / 1000);
	}

	public static class CacheTask implements Runnable {
		final File file;

		public CacheTask(final File file) {
			this.file = file;
		}

		public void run() {
			JSONProjectCacher.readJSONProject(file, new File(jsonCacheDir, file.getName().substring(0, file.getName().length() - 9)));
		}
	}
}
