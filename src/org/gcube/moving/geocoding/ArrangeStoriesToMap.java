package org.gcube.moving.geocoding;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.gcube.moving.events.Event;
import org.gcube.moving.semantic.WikidataExplorer;
import org.gcube.moving.utils.Pair;

import com.opencsv.CSVReader;

public class ArrangeStoriesToMap {

	public static void main(String[] args) throws Exception {

		File storiesFolder = new File("stories/");
		File uberStoryFile = new File("uberstory.csv");
		File storyDistributionFile = new File("storydistribution.csv");
		
		File[] allStories = storiesFolder.listFiles();
		StringBuffer uberStory = new StringBuffer();
		StringBuffer storyDistribution = new StringBuffer();
		int storyCounter = 0;

		System.out.println("Parsing all stories..");

		uberStory.append("Story fragment,Story number,Wikipedia concepts,longitude,latitude\n");
		storyDistribution.append("Story intro,Story number,Wikipedia concepts,longitude,latitude\n");
		HashSet<String> places = new HashSet<>();
		for (File story : allStories) {

			if (story.getName().endsWith(".csv")) {

				CSVReader reader = new CSVReader(new FileReader(story));
				String storyName = story.getName().replace(".csv", "");
				double storylon = 0;
				double storylat = 0;
				String storyIntro = "";
				StringBuffer allConcepts = new StringBuffer();
				
				List<String[]> allLines = reader.readAll();
				int lineCounter = 0;
				String storyfullName = "";
				String storyTitle = "";
				
				for (String[] line : allLines) {

					if (lineCounter > 0) {
						if (lineCounter == 1) {
							// storyfullName = "Title: "+line[0]+". "+line
							// [1].substring(0,line[1].indexOf("."))+".";
							storyfullName = "Title: " + line[0]; // +". "+line [1];
							storyTitle = line[0];
							storylon = Double.parseDouble(line[line.length - 2]);
							storylat = Double.parseDouble(line[line.length - 1]);
							
							String toSearch = "The MOVING reference member state of this VC is";
							
							String mainPlace = line [1].substring(line[1].indexOf(toSearch)+toSearch.length()).trim();
							mainPlace = Event.processPlaceName(mainPlace);
							
							System.out.println("Main Place:"+mainPlace);
							places.add(mainPlace);
							WikidataExplorer explorer = new WikidataExplorer();
							Pair p = explorer.getCoordinates(mainPlace);
							
							if (p!=null && p.longitude!=0) {
								System.out.println(mainPlace+"->"+p);
								
							}else {
								System.out.println(mainPlace+"!->"+new Pair(storylon,storylat));
							}
							
						}
						if (line[1].length() > 0 && !line[1].equals("N/A")) {
							
							String description = storyfullName+". " ;
							if (storyTitle.equals(line[0]))
								description = description+"Event: Story begin. ";
							
							else
								description = description+"Event: "+line[0]+". ";
							
								description = description+ "Description: " + line[1];
							description = description.replace(",", ";");
							description = description.replaceAll(" +", " ");
							String concepts = line[2].replace(",", ";");
							double lon = Double.parseDouble(line[line.length - 4]);
							double lat = Double.parseDouble(line[line.length - 3]);
							uberStory.append(
									description + "," + storyName + "," + concepts + "," + lon + "," + lat + "\n");
							if (lineCounter==1) {
								storyIntro = description;
							}else {
								allConcepts.append(";");	
							}
							
							allConcepts.append(concepts);
						}
					}

					lineCounter++;
				}

				String [] conceptAll = allConcepts.toString().split(";");
				HashSet<String> ca = new HashSet();
				List<String> cs = Arrays.asList(conceptAll);
				for (String c:cs) {
					c = c.trim();
					if (c.length()>1)
						ca.add(c);
				}
				String concs = ca.toString();
				concs = concs.replace(" +", " ").replace("[", "").replace("]", "").replace(",", ";");
				
				storyDistribution.append(
						storyIntro + "," + storyName + "," + concs + "," + storylon + "," + storylat + "\n");
				
				storyCounter++;
			}

		}

		FileWriter fw = new FileWriter(uberStoryFile);
		fw.write(uberStory.toString());
		fw.close();

		FileWriter fws = new FileWriter(storyDistributionFile);
		fws.write(storyDistribution.toString());
		fws.close();

		System.out.println(places);
		System.out.println("All stories parsed");
	}

}
