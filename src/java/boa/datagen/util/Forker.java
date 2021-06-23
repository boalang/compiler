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
	private String username;
	private String password;
	private HttpURLConnection connection = null;
	private String urlHeader = "https://api.github.com/repos/";
	private String urlFooter = "/forks";
	private String content = "";
	
	public static void main(final String[] args){
		final int numThreads = 1;
		final int totalFiles = new File(args[0]).listFiles().length;
		final int shareSize = totalFiles/numThreads;
		int start = 0;
		int end = 0;
		
		for (int i = 0; i < numThreads -1; i++){
			start = end;
			end = start + shareSize;
			final ForkerWorker worker = new ForkerWorker(args[0], args[1], args[2], start, end);
			new Thread(worker).start();
		}
		start = end;
		end = totalFiles;
		final ForkerWorker worker = new ForkerWorker(args[0], args[1], args[2], start, end);
		new Thread(worker).start();
	}
	
	
	public Forker(final String username, final String password) {
		this.username = username;
		this.password = password;
	}
	
	public void fork(final String name){
		final String url = urlHeader + name + urlFooter;
		try {
			connection  = (HttpURLConnection) new URL(url).openConnection();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		try {
			connection.setRequestMethod("POST");
		} catch (final ProtocolException e) {
			e.printStackTrace();
		}
		connection.setDoOutput(true);
		connection.setDoInput(true);
		final String authenStr = username + ":" + password;
		final String encodedAuthenStr = Base64.encode(authenStr.getBytes());
		this.connection.setRequestProperty("Authorization", "Basic " + encodedAuthenStr);
		try {
			connection.connect();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		this.getHttpResponseContent();
		System.out.println(content);
	}
	
	public void rename(final String name, final String originalOwner){
		final String url = urlHeader + username + "/" + name;
		final String newName = "{\"name\": " + "\"" + originalOwner + "___" + name + "\"}";
		final String[] command = {"curl" ,  "-u" , username +":" +  password , "-X" , "PATCH", "-d" , newName  , url };
		final ProcessBuilder process = new ProcessBuilder(command);
		try {
			process.start();
		} catch (final IOException e) {
			System.out.print("error");
			e.printStackTrace();
		}
	}
	
	private void getHttpResponseContent() {
		final StringBuilder sb = new StringBuilder();
		try {
			final InputStream response = connection.getInputStream();
			final BufferedInputStream in = new BufferedInputStream(response);
			final byte[] bytes = new byte[10000];
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
		} catch (final IOException e) {
			//e.printStackTrace();
			System.out.println(e.getMessage());
		}
		this.content = "";
	}
}
