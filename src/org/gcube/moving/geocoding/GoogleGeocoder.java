package org.gcube.moving.geocoding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.gcube.moving.utils.HTTPRequests;
import org.gcube.moving.utils.OSCommands;

public class GoogleGeocoder {

	// instructions here:
	// https://developers.google.com/maps/documentation/geocoding/overview
	// for authorizations see here:
	// https://stackoverflow.com/questions/32994634/this-api-project-is-not-authorized-to-use-this-api-please-ensure-that-this-api
	public void geocode(String address) throws Exception {

		String xmlcached = cacheRetrieve(address);
		String geocoding = xmlcached;

		if (xmlcached == null) {
			String googleKey = Files.readString(new File("geocodekey.txt").toPath()); // https://console.cloud.google.com/apis/credentials?project=geocoding-332513

			String request = "https://maps.googleapis.com/maps/api/geocode/xml?address="
					+ URLEncoder.encode(address, "UTF-8") + "&key=" + googleKey;

			geocoding = HTTPRequests.getRequest(request);

			cacheIt(address, geocoding);
			System.out.println("GEOCODING:\n" + geocoding);
		}

		try {
		parse(geocoding, address);
		}catch(Exception e) {
			System.out.println("Zero results");
			this.address = "";
		}
	}

	private void parse(String geocoding, String input) {
		String toSearch = "<formatted_address>";
		address = geocoding.substring(geocoding.indexOf(toSearch) + toSearch.length());
		address = address.substring(0, address.indexOf("</")).trim();
		
		if (address.equalsIgnoreCase(input))
			isApproximate = false;
		else {
		LevenshteinDistance ld = new LevenshteinDistance();
		String input4L = input.toLowerCase().replaceAll("[^a-z ]", " ");
		input4L = input4L.replaceAll(" +", " ");
		String address4L = address.toLowerCase().replaceAll("[^a-z ]", " ");
		address4L = address4L.replaceAll(" +", " ");
		
		double score = 1;
		
		if (address4L.length()>input4L.length()) {
			Integer lev = ld.apply(address4L, input4L);
			score = (double)lev/(double)address4L.length();
		}else {
			Integer lev = ld.apply(input4L,address4L);
			score = (double)lev/(double)input4L.length();
		}
		
		System.out.println("Score "+address4L+" vs "+input4L+" = "+score);
		
		if (score<0.7) {
			isApproximate = false;
			System.out.println("#########SIMILAR########");
		}
		else
			isApproximate = true;
		}
		
		toSearch = "<lat>";
		String lat = geocoding.substring(geocoding.indexOf(toSearch) + toSearch.length());
		lat = lat.substring(0, lat.indexOf("</"));
		latitude = Double.parseDouble(lat);

		toSearch = "<lng>";
		String lon = geocoding.substring(geocoding.indexOf(toSearch) + toSearch.length());
		lon = lon.substring(0, lon.indexOf("</"));
		longitude = Double.parseDouble(lon);
	}

	public double longitude;
	public double latitude;
	public String address = "";
	public boolean isApproximate = false;

	public String toString() {

		return address + " [" + longitude + ";" + latitude + "] - approximate " + isApproximate;
	}

	public String cacheRetrieve(String address) throws Exception {

		File sha1 = new File("geocache/sha" + DigestUtils.sha1Hex(address) + ".txt");
		if (sha1.exists()) {
			String xml = new String(Files.readAllBytes(sha1.toPath()), "UTF-8");
			return xml;
		}
		return null;

	}

	public void cacheIt(String address, String xml) throws Exception {

		File sha1 = new File("geocache/sha" + DigestUtils.sha1Hex(address) + ".txt");

		Writer fstream = new OutputStreamWriter(new FileOutputStream(sha1), StandardCharsets.UTF_8);
		fstream.write(xml);
		fstream.close();

	}

	public static void main(String[] args) throws Exception {
		GoogleGeocoder gsst = new GoogleGeocoder();
		String address = "ashjhjhjhj";
		// String address = "Australopithecus";
		gsst.geocode(address);
		System.out.println(gsst.toString());

	}
}
