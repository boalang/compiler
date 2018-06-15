package boa.datagen.forges.github;

import java.io.File;



import boa.datagen.util.FileIO;


public class JsonSplitter {

	final private int Num_Threads;
	private String outDir;
	private String inDir;
	
	JsonSplitter(int threads, String outDir, String inDir){
		this.Num_Threads = threads;
		this.outDir = outDir;
		this.inDir = inDir;
	}
	
	public static void main(String[] args) {
		JsonSplitter JS = new JsonSplitter(Integer.parseInt(args[0]), args[1], args[2]);
		JS.split();
	}
	
	public void split () {
		Splitter[] workers = new Splitter[Num_Threads];
		for (int i = 0; i < Num_Threads; i++) {
			Splitter worker = new Splitter(i, outDir);
			workers[i] = worker;
		}
		File[] files = new File(inDir).listFiles();
		
		for (int i = 0; i < files.length; i ++) {
			if (!files[i].getPath().contains(".json"))
				continue;
			boolean assigned = false;
			while (!assigned) {
				for (int j = 0; j < Num_Threads; j++) {
					if (workers[j].isAvailable()){
						workers[j].setFile(files[i]);
						new Thread(workers[j]).start();
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						assigned = true;
						break;
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private class Splitter implements Runnable {
		private boolean available = true;
		private File file;
		private String dir;
		private int id;
		Splitter(int id, String outDir) {
			this.id = id;
			this.dir = outDir + id;
			File output = new File(dir);
			if (!output.exists())
				output.mkdirs();
		}
		
		public boolean isAvailable() { return available; }
		
		public void setFile(File file) {
			this.file = file;
		}
		
		private void proccessFile(){
			String content = FileIO.readFileContents(file);
			System.out.println(id + " is processing " + file.getName());
			FileIO.writeFileContents(new File(dir +"/" + file.getName()), content);
		}
		
		@Override
		public void run() {
			available = false;
			proccessFile();
			try {
				Thread.sleep(150 * Num_Threads + 1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			available = true;
		}
		
	}
}
