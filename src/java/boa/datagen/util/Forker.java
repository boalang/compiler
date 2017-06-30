package boa.datagen.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import com.sun.org.apache.xml.internal.security.utils.Base64;

public class Forker {
	private String username, password;
	private HttpURLConnection connection = null;
	private String urlHeader = "https://api.github.com/repos/";
	private String urlFooter = "/forks";
	private String content = "";
	
	public static void main(String args[]){
		int numThreads = 3;
		int totalFiles = new File(args[0]).listFiles().length;
		int shareSize = totalFiles/numThreads;
		int start = 0, end = 0;
		
		for (int i = 0; i < numThreads -1; i++){
			start = end;
			end = start + shareSize;
			ForkerWorker worker = new ForkerWorker(args[0], args[1], args[2], start, end);
			new Thread(worker).start();
		}
		start = end;
		end = totalFiles;
		ForkerWorker worker = new ForkerWorker(args[0], args[1], args[2], start, end);
		new Thread(worker).start();
	}
	
	
	public Forker(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	public void fork(String name){
		String url = urlHeader + name + urlFooter;
		try {
			connection  = (HttpURLConnection) new URL(url).openConnection();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			connection.setRequestMethod("POST");
		} catch (ProtocolException e) {
			e.printStackTrace();
		}
		connection.setDoOutput(true);
		connection.setDoInput(true);
		String authenStr = username + ":" + password;
		String encodedAuthenStr = Base64.encode(authenStr.getBytes());
		this.connection.setRequestProperty("Authorization", "Basic " + encodedAuthenStr);
		try {
			connection.connect();
		} catch (IOException e) {
			e.printStackTrace();
		}
	//	this.getHttpResponseContent();
	//	System.out.println(content);
	}
	
	public void rename(String name, String originalOwner){
		String url = urlHeader + username + "/" + name;
		String newName = "{\"name\": " + "\"" + originalOwner + "_" + name + "\"}";
		String[] command = {"curl" ,  "-u" , username +":" +  password , "-X" , "PATCH", "-d" , newName  , url };
		ProcessBuilder process = new ProcessBuilder(command);
		try {
			process.start();
		} catch (IOException e) {
			System.out.print("error");
			e.printStackTrace();
		}
	}
	
	private void getHttpResponseContent() {
		StringBuilder sb = new StringBuilder();
		try {
			InputStream response = connection.getInputStream();
			BufferedInputStream in = new BufferedInputStream(response);
			byte[] bytes = new byte[10000];
			int len = in.read(bytes);
			while (len != -1)
			{
				//System.out.println(len);
				//System.out.println(new String(bytes, 0, len));
				sb.append(new String(bytes, 0, len));
				//Thread.sleep(100);
				len = in.read(bytes);
			}
			in.close();
			//System.out.println(len);
			//System.out.println(sb.toString());
			this.content = sb.toString();
			return;
		} catch (IOException e) {
			//e.printStackTrace();
			System.out.println(e.getMessage());
		}
		this.content = "";
	}
}
