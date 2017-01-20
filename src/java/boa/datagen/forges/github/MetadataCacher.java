package boa.datagen.forges.github;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import com.sun.org.apache.xml.internal.security.utils.Base64;

public class MetadataCacher {
	private String url;

	private String username, password;
	private boolean authenticated = false;
	private HttpURLConnection connection = null;
	private int responseCode = -1;
	private String responseMessage;
	private String content = "";
	
	public MetadataCacher(String url) {
		setUrl(url);
	}

	public MetadataCacher(String url, String username, String password) {
		this(url);
		this.username = username;
		this.password = password;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
		try {
			connection  = (HttpURLConnection) new URL(url).openConnection();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isAuthenticated() {
		return authenticated;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public String getContent() {
		return content;
	}
	
	public boolean authenticate() {
		return authenticate(this.username, this.password);
	}

	public boolean authenticate(String username, String password) {
		String authenStr = username + ":" + password;
		String encodedAuthenStr = Base64.encode(authenStr.getBytes());
		this.connection.setRequestProperty("Authorization", "Basic " + encodedAuthenStr);
		try {
			this.responseCode = this.connection.getResponseCode();
			this.authenticated = (this.responseCode / 100 == 2);
		} catch (IOException e) {
			// considered as failed
		}
		return this.authenticated;
	}
	
	public void getResponse() {
		getHttpResponseCode();
		getHttpResponseContent();
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

	private void getHttpResponseCode() {
		try {
			this.responseCode = this.connection.getResponseCode();
			this.responseMessage = this.connection.getResponseMessage();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void getResponseJson() {
		this.content = "";
		getHttpResponseContent();
		if (this.content.startsWith("[") && this.content.endsWith("]")) {
			return;
		}
		if (this.content.startsWith("{") && this.content.endsWith("}")) {
			return;
		}
		System.err.println("Error: getting json response!");
		this.content = "";
	}
	
	public void showHeaderFields() {
		Map<String, List<String>> map = this.connection.getHeaderFields();
		for (String key : map.keySet()) {
			System.out.println(key + ": " + map.get(key));
		}
	}

	public Map<String, List<String>> getHeaderFields() {
		return this.connection.getHeaderFields();
	}
	
	public int getNumberOfMaxLimit() {
		return Integer.parseInt(this.connection.getHeaderField("X-RateLimit-Limit"));
	}
	
	public int getNumberOfRemainingLimit() {
		return Integer.parseInt(this.connection.getHeaderField("X-RateLimit-Remaining"));
	}
	
	public long getLimitResetTime() {
		return Long.parseLong(this.connection.getHeaderField("X-RateLimit-Reset"));
	}
}
