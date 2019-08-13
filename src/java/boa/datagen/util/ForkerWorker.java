package boa.datagen.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ForkerWorker implements Runnable{
	private final String USER;
	private final String PASS;
	private final String INPATH;
	private final int FROM;
	private final int TO;
	
	public ForkerWorker(String inPath, String user, String pass, int from, int to){
		this.INPATH = inPath;
		this.USER = user;
		this.PASS = pass;
		this.FROM = from;
		this.TO = to;
	}
	
	public void forkAndRename(){
		File[] files = new File(INPATH).listFiles();
		Forker forker = new Forker(USER, PASS);
		for (int i = FROM; i < TO; i++){
			System.out.println(Thread.currentThread().getId() +" processing page-" + i);
			try {
				Scanner sc = new Scanner(files[1]);
				while (sc.hasNextLine()){
					String fullName = sc.nextLine();
					forker.fork(fullName);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String[] names = fullName.split("/");
					forker.rename(names[1], names[0]);
					System.out.println("forked " + fullName);
				}
				sc.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		forkAndRename();
	}
	
}
