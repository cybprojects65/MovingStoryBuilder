package org.gcube.moving.inventory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import org.gcube.moving.nlphub.NLPHubCaller;

import com.opencsv.CSVReader;

public class InventoryManager {

	
	public static void main(String args[]) throws Exception{
		
		ValueChain.simulatecoordinates = false;
		
		try (CSVReader reader = new CSVReader(new FileReader("Dataset_VC card_Inventory_102021_db.csv"))) {
			  String header = "";
			  
		      List<String[]> r = reader.readAll();
		      int j = 0;
		      for (String[] row:r) {
		    	  int i = 0;
		    	for (String rowi:row) {
		    		row[i] = NLPHubCaller.cleanCharacters(rowi).replace("\"", "'");
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
		    		
		    		ValueChain vc = new ValueChain();
		    		vc.parseHeader(header);
		    		
		    		i = 0;
		    		for (String rowi:row) {
		    			vc.analyseField(rowi, i);
		    			i++;
		    		}
		    		
		    		System.out.println("Producing events");
		    		
		    		String story = vc.produceEvents();
		    		story = "title,description,objects,objectlinks,lon,lat"+"\n"+story;
		    		System.out.println(story);
		    		File storyFile = new File("stories/"+j+".csv");
		    		FileOutputStream fos = new FileOutputStream(storyFile);
		    		Writer writer = new OutputStreamWriter(fos, "UTF-8");
		    		writer.write(story.replace("\t", ","));
					writer.close();
					System.out.println("Story "+j+" of "+r.size()+" finished");
		    		
		    	}
		    		
		    	j++;
		    	if (j>1)
		    		break;
		      }
		      
		  }
		
	}
}
