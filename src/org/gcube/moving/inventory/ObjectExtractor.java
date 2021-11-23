package org.gcube.moving.inventory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.gcube.moving.nlphub.InvokeNLPHub;

public class ObjectExtractor {
	InvokeNLPHub nlphub = new InvokeNLPHub();
	public LinkedHashSet<String> allobjs = new LinkedHashSet<String>();
	public LinkedHashSet<String> potentialgeoobjs = new LinkedHashSet<String>();
	
	public void extractObjects(String description) throws Exception{
		
		File sha1 = new File("annotationcache/sha"+DigestUtils.sha1Hex(description)+".txt");
		File sha1geo = new File("annotationcache/shageo"+DigestUtils.sha1Hex(description)+".txt");
		
		LinkedHashMap<String,Set<String>> allEntities = new LinkedHashMap<>();
		LinkedHashMap<String,Set<String>> potentialGeospatialEntities = new LinkedHashMap<>();
		
		if (sha1.exists()) {
			
			FileInputStream fos = new FileInputStream(sha1);
			ObjectInputStream oos = new ObjectInputStream(fos);
			allEntities = (LinkedHashMap<String, Set<String>>) oos.readObject();
			oos.close();
			
			fos = new FileInputStream(sha1geo);
			oos = new ObjectInputStream(fos);
			potentialGeospatialEntities = (LinkedHashMap<String, Set<String>>) oos.readObject();
			oos.close();
			
		}else {
			
			File tempFile = new File("temp"+UUID.randomUUID()+".txt");
			Writer fstream = new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8);
			fstream.write(description);    
			fstream.close();
			
			List<String> entities = new ArrayList<>();
			entities.add("Location");
			entities.add("Person");
			entities.add("Organization");
			entities.add("Keyword");
			
			String language = "en";
			nlphub.getEntities(entities, tempFile, "en");
			
			FileOutputStream fos = new FileOutputStream(sha1);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(nlphub.allEntities);
			oos.close();
			tempFile.delete();
			
			fos = new FileOutputStream(sha1geo);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(nlphub.potentialGeospatialEntities);
			oos.close();
			tempFile.delete();
			
			allEntities = nlphub.allEntities;
			potentialGeospatialEntities = nlphub.potentialGeospatialEntities;
			
			
		}
		
		allobjs = new LinkedHashSet<String>();
		
		for (String key:allEntities.keySet()) {
			Set<String> caught = allEntities.get(key);
			for (String c:caught) {
				allobjs.add(c.toLowerCase().trim());
				
			}
		}
		
		
		potentialgeoobjs = new LinkedHashSet<String>();
		
		for (String key:potentialGeospatialEntities.keySet()) {
			Set<String> caught = potentialGeospatialEntities.get(key);
			for (String c:caught) {
				potentialgeoobjs.add(c.toLowerCase().trim());
				
			}
		}
		
	}
	
	
}
