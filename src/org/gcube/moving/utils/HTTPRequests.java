package org.gcube.moving.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class HTTPRequests {


	public static String getRequest(String input) throws Exception {
		String response = null;

		String userAgent = "Mozilla/6.0 (Windows; U; Windows NT 6.0;en-EN; rv:1.9.2) Gecko/20100115 Firefox/3.6";
		Map<String, String> map = new HashMap<String, String>();
		map.put("User-Agent", userAgent);
		map.put("Host", "www.google.com");
		map.put("Cache-Control", "no-cache");
		URL url = new URL(input);
		response = response + executeRequest(url, map);

		return response;
	}

	public static String getRedirectedPage(String url) throws Exception {
		
	
	URL hh= new URL(url);
	URLConnection connection = hh.openConnection();
	String redirect = connection.getHeaderField("Location");
	if (redirect != null){
	    connection = new URL(redirect).openConnection();
	}
	BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	StringBuffer sb = new StringBuffer();
	
	String inputLine;
	
	while ((inputLine = in.readLine()) != null) {
			sb.append(inputLine);
	}
		
		return sb.toString();
	}
	
	
	public static String executeRequest(URL url, Map<String, String> map) {
		String readLine = null;
		String responseGET = null;
		HttpURLConnection con = null;
		try {
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setConnectTimeout(5000);
			if (map != null) {
				for (Map.Entry<String, String> entry : map.entrySet()) {
					//System.out.println(entry.getKey() + "/" + entry.getValue());
					con.setRequestProperty(entry.getKey(), entry.getValue());
				}
			}

			int responseCode = con.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				StringBuffer response = new StringBuffer();
				while ((readLine = in.readLine()) != null) {
					response.append(readLine);
					response.append('\r');
				}
				in.close();
				responseGET = response.toString();
			} else {
				System.out.println("responseCode = " + responseCode);
			}
		} catch (Exception e) {
			System.out.println("Error");
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}
		return responseGET;
	}

	public static String getHTML(String urlToRead) throws Exception {
		StringBuilder result = new StringBuilder();
		URL url = new URL(urlToRead);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setRequestMethod("GET");
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
			for (String line; (line = reader.readLine()) != null;) {
				result.append(line);
			}
		}
		return result.toString();
	}

}
