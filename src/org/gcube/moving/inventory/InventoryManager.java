package org.gcube.moving.inventory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;

import org.gcube.moving.nlphub.NLPHubCaller;

import com.opencsv.CSVReader;

public class InventoryManager {

	
	public static void main(String args[]) throws Exception{
		
		ValueChain.simulatecoordinates = false;
		//NOTES ON DATA PREPARATION: DELETE COMMAS IN THE COLUMN NAMES : "100," AND ", its specificity" 
		//try (CSVReader reader = new CSVReader(new FileReader("Dataset_VC card_Inventory_102021_db_updated01122021.csv"))) {
		HashSet<Character> strangecharacters = new HashSet<>();
		try (CSVReader reader = new CSVReader(new FileReader("MOVING_VCs_DATASET_FINAL_V2.csv"))) {
			
			  String header = "";
			    
		      List<String[]> r = reader.readAll();
		      int j = 0;
		      for (String[] row:r) {
		    	/*debugging*/
		    	  /*
		    	  if (j>0 && j!=137)
		    	  {
		    		  j++;
		    		  continue;
		    	  }
		    		*/
		    	  long t0 = System.currentTimeMillis();
		    	  
		    	  int i = 0;
		    	for (String rowi:row) {
		    		row[i] = NLPHubCaller.cleanCharacters(rowi).replace("\"", "'");
		    		HashSet<Character> str = NLPHubCaller.getStrangeCharacters(rowi);
		    		strangecharacters.addAll(str);
		    		//System.out.println("Row"+i+" : "+row[i]);
		    		i++;
		    	}
		    	
		    	if (j==0) {
		    		i=0;
		    		for (String rr:row)
		    		{
		    			header = header+rr;
		    			if (i<(row.length-1))
		    				header=header+",";
		    			i++;
		    		}
		    	}
		    	else {
		    		//if (j==240)
		    		{
		    			
		    		System.out.println("####STORY NUMBER "+j+" OF "+r.size()+"####");
		    		
		    		System.out.println("###BUILDING STORY BASED ON THE CSV ROW###");
		    		ValueChain vc = new ValueChain();
		    		vc.parseHeader(header);
		    		
		    		i = 0;
		    		for (String rowi:row) {
		    			vc.analyseField(rowi, i);
		    			i++;
		    		}
		    		System.out.println("###END - BUILDING STORY BASED ON THE CSV ROW###");
		    		
		    		System.out.println("###ENRICHING EVENT INFORMATION###");
		    		String story = vc.produceEvents();
		    		System.out.println("###END - ENRICHING EVENT INFORMATION###");
		    		
		    		System.out.println("###SAVING THE STORY###");
		    		story = "title,description,objects,objectlinks,lon,lat,barycenter_lon,barycenter_lat"+"\n"+story;
		    		System.out.println(story);
		    		File storyFile = new File("stories/"+j+".csv");
		    		FileOutputStream fos = new FileOutputStream(storyFile);
		    		Writer writer = new OutputStreamWriter(fos, "UTF-8");
		    		writer.write(story.replace("\t", ","));
					writer.close();
					System.out.println("Story "+j+" of "+r.size()+" finished");
					System.out.println("###END - SAVING THE STORY###");
					
					long t1 = System.currentTimeMillis();
					double elapsed = (double)(t1-t0)/(60*1000);
					System.out.println("ELAPSED TIME TO BUILD STORY "+j+":"+elapsed+" min");
		    		}
		    	}
		    		
		    	j++;
		    	//if (j>1)
		    		//break;
		      }
		      
		  }
		
		System.out.println("Detected strange chars :"+strangecharacters);
		
	}
}
