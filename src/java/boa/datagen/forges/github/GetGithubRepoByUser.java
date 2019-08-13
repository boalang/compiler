package boa.datagen.forges.github;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import boa.datagen.util.FileIO;

public class GetGithubRepoByUser {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (args == null || args.length < 5) {
//			System.out.println("Command line arguments are not correct");
			return;
		}
		String outDir = args[0];
		String username = args[1];
		String password = args[2];

		String url = "https://api.github.com/repos";
		String pageContent = "";
		MetadataCacher mc = new MetadataCacher(url, username, password);
		int pageNumber = 0;
		String id = "";
		File dir = new File(outDir);
		if (!dir.exists())
			dir.mkdirs();
		File[] files = dir.listFiles();
//		Arrays.sort(files, new Comparator<File>() {
//			@Override
//			public int compare(File f1, File f2) {
//				int n1 = getNumber(f1.getName()), n2 = getNumber(f2.getName());
//				return n1 - n2;
//			}
//
//			private int getNumber(String name) {
//				String s = name.substring(5, name.length());
//				return Integer.valueOf(s);
//			}
//		});
		pageNumber = files.length;
		if (pageNumber > 0)
			pageContent = FileIO.readFileContents(files[pageNumber - 1]);
		mc = new MetadataCacher(url + "/"+args[3]+"/"+args[4], username, password);
		System.out.println(mc.getUrl());
		if (mc.authenticate()) {
			while (true) {
					mc.getResponseJson();
					pageContent = mc.getContent();
					if (pageContent.equals("[]"))
						break;
					if (!pageContent.isEmpty()) {
						String path = outDir  + "/repos/";
						File f = new File(path);
						if(!f.exists()){
							f.mkdirs();
						}
						path =outDir  + "/repos/repo"+".json";
						f= new File(path);
						FileWriter file = null;
						try {
							file = new FileWriter(path);
							file.write(pageContent);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}finally {
							try {
								file.flush();
								file.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}
							
						}
						break;
					}

				}
		else {
			System.out.println("Authentication failed!");
		}
		
		
		
		
		mc = new MetadataCacher(url + "/"+args[3]+"/"+args[4]+"/languages", username, password);
//		System.out.println(mc.getUrl());
		if (mc.authenticate()) {
			while (true) {
					mc.getResponseJson();
					pageContent = mc.getContent();
					if (pageContent.equals("[]"))
						break;
					if (!pageContent.isEmpty()) {
						String path = outDir  + "/languages/";
						File f = new File(path);
						if(!f.exists()){
							f.mkdirs();
						}
						path =outDir  + "/languages/lang"+".json";
						f= new File(path);
						FileWriter file = null;
						try {
							file = new FileWriter(path);
							file.write(pageContent);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}finally {
							try {
								file.flush();
								file.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}
							
						}
						break;
					}

				}
		else {
			System.out.println("Authentication failed!");
		}
	}
}
