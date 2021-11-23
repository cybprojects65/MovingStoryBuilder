package org.gcube.moving.semantic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URLEncoder;

import org.apache.commons.codec.digest.DigestUtils;
import org.gcube.moving.utils.HTTPRequests;
import org.gcube.moving.utils.Pair;


public class WikidataExplorer {

	// Wikidata query URL sparql

	static String WD_URL = "https://query.wikidata.org/sparql?query=";

	// URL per la query alle api della ricerca su Wikidata
	static String URL = "https://www.wikidata.org/w/api.php";

	public String queryWikidata(String entity) throws Exception {
		return queryWikidata(entity, false);

	}

	public Pair getCoordinates(String entity) throws Exception{
		queryWikidata(entity);
		return wikidataPair;
	}
	
	
	public String queryWikidata(String entity, boolean controlmode) throws Exception {
		File sha1 = new File("wikidatacache/sha" + DigestUtils.sha1Hex(entity) + ".txt");
		String uri = "";

		if (sha1.exists() && !controlmode) {
			FileInputStream fos = new FileInputStream(sha1);
			ObjectInputStream oos = new ObjectInputStream(fos);
			WikiObj wo =  (WikiObj) oos.readObject();
			uri = wo.uri;
			this.wikidataPair = wo.coordinates;
			//System.out.println("Recovered Pair "+this.wikidataPair);
			oos.close();

		} else {

			String ent = prepareEntity(entity);
			System.out.println("Analysing entity written as "+ent);
			uri = analyseResponse(ent);
			if (uri.length()==0) {
				
				ent = entity.trim();
				ent = ent.toLowerCase();
				ent = ent.replaceAll(" +", " ");
				System.out.println("Re-analysing entity written as "+ent);
				uri = analyseResponse(ent);
				
			}
			
			WikiObj wo = new WikiObj(uri, wikidataPair);
			
			FileOutputStream fos = new FileOutputStream(sha1);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(wo);
			oos.close();

		}

		System.out.println("Wikidata: " + entity + "->" + uri + " | coordinates "+wikidataPair );
		return uri;
	}

	public String analyseResponse(String ent) throws Exception{
		
		String q = "SELECT DISTINCT ?entity ?label WHERE {?entity rdfs:label \"" + ent
				+ "\"@en . SERVICE wikibase:label { bd:serviceParam wikibase:language \"[AUTO_LANGUAGE],en\" }}";

		String query = "https://query.wikidata.org/sparql?query=" + URLEncoder.encode(q, "UTF-8") + "&format=json";
		String url = query;

		// System.out.println("Query:\n" + url);

		String response = HTTPRequests.getRequest(url);
		String uri = parseResponse(response);

		if (uri.length() == 0 || !isValid(ent, uri))
			uri = "";
		
		return uri;
	}
	
	public Pair wikidataPair = null;
	
	
	public double dmsToDecimal(String DMS) throws Exception {
		
		DMS = DMS.replaceAll( "[^0-9\\.'°NSWE]+" , "");
		
		int degIdx = DMS.indexOf("°");
		int minIdx = DMS.indexOf("'");
		int len = DMS.length();
		
		double deg = Integer.parseInt(DMS.substring(0,degIdx));
		
		if (minIdx>-1) {
			double minutes = Double.parseDouble(DMS.substring(degIdx+1,minIdx));
			//minutes = minutes/60d;
			String secondsS = DMS.substring(minIdx+1,len-1);
			double seconds = 0;
			if (secondsS.length()>0) {
				seconds = Double.parseDouble(secondsS);
				seconds = seconds/60d;
			}
			minutes = minutes+seconds;
			minutes = minutes/60d;

			deg = deg+minutes;
		}
		
		if (DMS.charAt(len-1) == 'S' || DMS.charAt(len-1) == 'W')
			deg = -deg;
		
		
		return deg;
	}
	
	
	public void extractCoordinates(String response) throws Exception {
		
		try {
		response = response.substring(response.indexOf(">coordinate location<") + 1);
		response = response.substring(response.indexOf("wikibase-kartographer-caption\">") + 1);
		response = response.substring(response.indexOf(">")+1,response.indexOf("<"));
		response = response.trim();
		System.out.println("COORDINATE String "+response);
		
		String latString = response.substring(0,response.indexOf(","));
		double lat = dmsToDecimal(latString);
		String lonString = response.substring(response.indexOf(",")+1);
		double lon = dmsToDecimal(lonString);
		
		System.out.println("COORDINATES "+lon+";"+lat);
		wikidataPair = new Pair(lon,lat);
		}catch(Exception e ) {
			System.out.println("UNPARSABLE COORDINATES");
		}
		System.out.println("Estimated Pair "+wikidataPair);
	}
	
	
	public boolean isValid(String entity, String uri) throws Exception {

		try {
			String response = HTTPRequests.getRedirectedPage(uri);
			String wikipage = new String(response);
			
			System.out.println("wikidata check:\n" + response);

			if (response.toLowerCase().contains("wikimedia disambiguation page")) {
				System.out.println("Ambiguous content");
				return false;
			} else {
				try {
					response = response.substring(response.indexOf("wikibase-sitelinkgrouplistview") + 1);
					response = response.substring(response.indexOf("title=\"English\">enwiki</span>") + 1);
					response = response.substring(response.indexOf("title=\"") + "title=\"".length());
					response = response.substring(0, response.indexOf("\""));
					response = response.trim();
					System.out.println("NAME IN ENGLISH WIKI:" + response);
				} catch (Exception e) {
					System.out.println("Error finding the wikipedia en page:" + e.getLocalizedMessage());

				}
				if (entity.equalsIgnoreCase(response)) {
					extractCoordinates(wikipage);
					return true;
				}
				else
					return false;

			}

		} catch (Exception e) {
			System.out.println("Issue on the page " + uri + " : " + e.getLocalizedMessage());
			e.printStackTrace();
			System.exit(0); 
			return false;
		}
	}

	

	public static String prepareEntity(String entity) {

			String e = new String(entity);
			e = e.trim();
			e = e.toLowerCase();
			e = e.replaceAll(" +", " ");
			String[] es = e.split(" ");
			StringBuffer sb = new StringBuffer();

			for (int i = 0; i < es.length; i++) {

				String ent = es[i];
				if (ent.trim().length() > 0) {
					String c1 = ("" + ent.charAt(0)).toUpperCase();
					String c2 = ent.substring(1);

					sb.append(c1 + c2);
					if (i < es.length - 1)
						sb.append(" ");
				}
			}

			return sb.toString();
		
	}

	public static String parseResponse(String response) throws Exception {

		int idx = response.indexOf("\"value\"");
		String minimalURI = "";
		while (idx > -1) {

			String res = response.substring(idx);
			res = res.substring(res.indexOf(":") + 1);
			res = res.substring(0, res.indexOf("}"));

			String uri = res.replace("\"", "");
			uri = uri.trim();
			//System.out.println("URI->" + uri);

			if (uri.startsWith("http")) {
				if (minimalURI.length() == 0)
					minimalURI = uri;
				else if (minimalURI.length() > uri.length()) {
					minimalURI = uri;
				}

			}
			response = response.substring(idx + 1);

			idx = response.indexOf("\"value\"");

		}

		return minimalURI;

	}

	public static void main(String args[]) throws Exception {
		// "Apuan Alps"
		// String entity = "Dante Alighieri";
		// String entity = "sector";
		// String entity = "apuan alps";
		//String entity = "Austria";
		//System.out.println("result:\n" + new WikidataExplorer().dmsToDecimal("77°59'59S"));
		//System.out.println("result:\n" + new WikidataExplorer().dmsToDecimal("46°19'12W"));
		//System.out.println("result:\n" + new WikidataExplorer().dmsToDecimal("46°19'W"));
		
		//String entity = "Vienna";
		String entity = "Dante Alighieri";
		String result = new WikidataExplorer().queryWikidata(entity, true);
		System.out.println("result:\n" + result);

	}
}
