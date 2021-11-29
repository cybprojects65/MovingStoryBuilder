package org.gcube.moving.geocoding;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;

import com.opencsv.CSVReader;

public class ArrangeStoriesToMap {

	
	public static void main(String[] args) throws Exception {
		
		
		File storiesFolder = new File("stories/");
		File uberStoryFile = new File("uberstory.csv");
		
		File [] allStories = storiesFolder.listFiles();
		StringBuffer uberStory = new StringBuffer();
		int storyCounter = 0;
		
		System.out.println("Parsing all stories..");
		
		uberStory.append("story_number,longitude,latitude\n");
		
		for (File story:allStories) {
			
			if (story.getName().endsWith(".csv")) {
				
				CSVReader reader = new CSVReader(new FileReader(story));
				String storyName = story.getName().replace(".csv", "");
				
				List<String[]> allLines = reader.readAll();
				int lineCounter = 0;
				for (String []line:allLines) {
					
					if (lineCounter>0) {
						
						double lon = Double.parseDouble(line[line.length-2]);
						double lat = Double.parseDouble(line[line.length-1]);
						uberStory.append(storyName+","+lon+","+lat+"\n");
						
					}
					
					
					lineCounter++;
				}
				
				
				
				storyCounter++;	
			}
			
		}
		
		FileWriter fw = new FileWriter(uberStoryFile);
		fw.write(uberStory.toString());
		fw.close();
		
		System.out.println("All stories parsed");
	}
	
	
	
	
	
}
