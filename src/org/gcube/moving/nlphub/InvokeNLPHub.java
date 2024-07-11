package org.gcube.moving.nlphub;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class InvokeNLPHub {

	static String dataMinerURL = "dataminer-prototypes.cloud.d4science.org";
	static String token = "fea75a5a-d84c-495f-b0ca-09cdd95bacce-843339462"; //statistical.manager
	public LinkedHashMap<String,Set<String>> allEntities = new LinkedHashMap<>();
	public LinkedHashMap<String,Set<String>> potentialGeospatialEntities = new LinkedHashMap<>();
	
	public LinkedHashMap<String,Set<String>> getEntities(List<String> entities,File inputFileUTF8, String language) throws Exception {
		
		
		List<String> annotations = null;
		annotations = entities;
		
		/*
		String allAnnotations = NLPHubCaller.loadString("Annotations_example.txt", "UTF-8");
		String alllogs = "";
		*/
				
		NLPHubCaller caller = new NLPHubCaller(dataMinerURL, token);
		caller.run(language, inputFileUTF8, annotations);

		System.out.println("JSON output is in: " + caller.getOutputJsonFile());
		System.out.println("Annotated text is in: " + caller.getOutputAnnotationFile());
		// example of output
		
		
		String allAnnotations = NLPHubCaller.loadString(caller.getOutputAnnotationFile().getAbsolutePath(), "UTF-8");
		String alllogs = NLPHubCaller.loadString(caller.getLogFile().getAbsolutePath(), "UTF-8");
		
		
		
		
		String allAnnotationsNotExtracted = allAnnotations.replace("##", "");
		
		int nAnn = ((allAnnotations.length() - allAnnotationsNotExtracted.length()) / 4) - 1;
		
		

		System.out.println(allAnnotations);
		String splitAnnotations[] = allAnnotations.split("\n");
		boolean collecting = false;
		for (String a:splitAnnotations) {
			a = a.trim();
			if (a.equals("##MERGED##")) {
				
				collecting = true;
				
			}else if ( (a.length()==0 || a.startsWith("##")) && collecting) {
				collecting = false;
			}else if (a.startsWith("#") && collecting) {
				int column = a.indexOf(":");
				String ne = a.substring(0,column);
				ne = ne.replace("#", "").trim();
				String sentence = a.substring(column+1);
				Set<String> extrentities = extractEntities(sentence); 
				System.out.println("Named entity: "+ne+" -> "+extrentities);
				allEntities.put(ne, extrentities);
				if (ne.equalsIgnoreCase("Location") || ne.equalsIgnoreCase("Keyword"))
					potentialGeospatialEntities.put(ne, extrentities);
			}
			
		}
		
		
		System.out.println("N of algorithms that extracted information " + nAnn);

		caller.outputAnnotationFile.delete();
		caller.outputJsonFile.delete();
		caller.outputLogFile.delete();
		
		if (alllogs.contains("Unparsable"))
			throw new Exception("Something went wrong");
		
		return allEntities;
	}
	
	public static Set<String> extractEntities(String annotatedString){
		Set<String> entities = new LinkedHashSet();
		
		int idx = annotatedString.indexOf("[");
		while (idx>-1) {
			int idxclose = annotatedString.indexOf("]");
			if (idxclose<0)
				break;
			String en = annotatedString.substring(idx+1,idxclose);
			en = en.trim();
			entities.add(en);
			annotatedString = annotatedString.substring(idxclose+1);
			idx = annotatedString.indexOf("[");
		}
		
		return entities;
		
	}
	public static void main(String[] args) throws Exception {
		//String s = "A review by US regulators of the single-shot [Johnson Johnson] coronavirus vaccine has found it is safe and effective. It paves the way for it to become the third Covid-19 vaccine to be authorised in the US, possibly within days. The vaccine would be a cost-effective alternative to the Pfizer and Moderna vaccines, and can be stored in a refrigerator instead of a freezer. In Milan things are going well";
		//System.out.println(extractEntities(s));
		List<String> entities = new ArrayList<>();
		entities.add("Location");
		entities.add("Person");
		entities.add("Organization");
		entities.add("Keyword");
		File input = new File("sampleTextBBC.txt");
		String language = "en";
		
		System.out.println("RESULT:");
		InvokeNLPHub nlp = new InvokeNLPHub();
		System.out.println(nlp.getEntities(entities,input,language));
	}
	
	public static void main1(String[] args) throws Exception {

		String annotationsParsed[] = null;
		List<String> annotations = null;
		String language = null;
		File inputText = null; //NOTE: INPUT MUST BE UTF-8

		if (args.length < 3) {
			System.out.println("Using sample input");
			args= new String[3];
			//args[0] = "Location#Person#Organization#Keyword";
			args[0] = "Location";
			args[1] = "en";
			args[2] = "sampleTextBBC.txt";
		}
		
		annotationsParsed = args[0].split("#");
		annotations = Arrays.asList(annotationsParsed);
		language = args[1]; // it en de fr es
		inputText = new File(args[2]);
	
		NLPHubCaller caller = new NLPHubCaller(dataMinerURL, token);
		caller.run(language, inputText, annotations);

		System.out.println("JSON output is in: " + caller.getOutputJsonFile());
		System.out.println("Annotated text is in: " + caller.getOutputAnnotationFile());
		// example of output
		String allAnnotations = NLPHubCaller.loadString(caller.getOutputAnnotationFile().getAbsolutePath(), "UTF-8");
		String allAnnotationsNotExtracted = allAnnotations.replace("##", "");
		int nAnn = ((allAnnotations.length() - allAnnotationsNotExtracted.length()) / 4) - 1;
		String alllogs = NLPHubCaller.loadString(caller.getLogFile().getAbsolutePath(), "UTF-8");

		System.out.println(allAnnotations);
		System.out.println("N of algorithms that extracted information " + nAnn);

		if (alllogs.contains("Unparsable"))
			throw new Exception("Something went wrong");

	}
	
}
