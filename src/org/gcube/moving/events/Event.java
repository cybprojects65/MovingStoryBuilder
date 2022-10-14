package org.gcube.moving.events;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.gcube.moving.geocoding.CoordinateDistributor;
import org.gcube.moving.semantic.WikidataExplorer;
import org.gcube.moving.utils.Pair;

public class Event {

	public String title;
	public String description;

	public LinkedHashSet<String> associatedObjects = new LinkedHashSet<>();
	public LinkedHashSet<String> wikidatalinks = new LinkedHashSet<>();
	
	public List<Pair> candidateCoordinates = new ArrayList();

	public double longitude;
	
	public double latitude;

	public double barycenterlongitude;
	public double barycenterlatitude;
	
	public Event(String title) {
		this.title = title;
		this.description = "";
		associatedObjects = new LinkedHashSet<>();
		longitude = 0;
		latitude = 0;
	}

	public void updateDescription(String descr) {
		descr = descr.trim();
		if (!descr.endsWith("."))
			descr = descr + ".";
		if (description.length() == 0)
			description = descr;
		else
			description = description + " " + descr;
	}

	public void updateObjects(LinkedHashSet<String> objs) {
		if (objs != null)
			associatedObjects.addAll(objs);
	}

	public void assignCoordinates(double longitude, double latitude) {
		this.longitude = longitude;
		this.latitude = latitude;
	}

	public void assignBarycenterCoordinates(double longitude, double latitude) {
		this.barycenterlongitude= longitude;
		this.barycenterlatitude = latitude;
	}
	
	public static String processPlaceName(String mainPlace) {
		
		mainPlace =mainPlace.substring(0,mainPlace.indexOf("."));
		mainPlace = mainPlace.replace("_", " ");
		mainPlace = mainPlace.replaceAll(" +", " ").trim();
		mainPlace = mainPlace.replaceAll("[0-9]+", "").trim();
		mainPlace = mainPlace.replace("UCO", "").replace("HUTTON", "").replace("ADEGUA", "").replace("UNIMOL and UNIPI", "").replace("UNIMOL & UNIPI","").
				replace("INRAE", "").replace("ESTRELA", "").replace("VINIDEA", "").replace("CCVD", "").replace("NMK", "North Macedonia").replace("SKANDINAVIAN", "Scandinavia");
		return mainPlace;
	}
	public Pair deduceBarycenter() throws Exception{
		
		String toSearch = "The MOVING reference member state of this VC is";
		String mainPlace = description.substring(description.indexOf(toSearch)+toSearch.length()).trim();
		mainPlace = processPlaceName(mainPlace);
		
		System.out.println("Main Place of this event:"+mainPlace);
		WikidataExplorer explorer = new WikidataExplorer();
		Pair p = explorer.getCoordinates(mainPlace);
		
		if (p!=null && p.longitude!=0) {
			System.out.println("BARYCENTER:"+mainPlace+"->"+p);
			return new Pair(p.longitude,p.latitude);
		}else {
			Pair pp = new Pair(this.longitude,this.latitude);
			System.out.println("NO BARYCENTER:"+mainPlace+"!->"+pp);
			return pp;
		}
		
	}
	
	
	public void addCandidateCoordinates(List<Pair> coords) {

		candidateCoordinates.addAll(coords);

	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String toString() {
		String objs = associatedObjects.toString().replace("[", "").replace("]", "");
		String links = wikidatalinks.toString().replace("[", "").replace("]", "");

		String sep = "\t";
		String titleEvent = new String(title);
		if (titleEvent.contains("N/A"))
			titleEvent = titleEvent.replace("N/A", "");
		else
			titleEvent = titleEvent.replace("Value Chain", "");
		titleEvent = titleEvent.trim();
		if (titleEvent.endsWith("."))
			titleEvent = titleEvent.substring(0,titleEvent.length()-1);
		
		
		String descriptionEvent = new String(description);
		descriptionEvent = descriptionEvent.replace(". N/A.", ".");
		descriptionEvent = descriptionEvent.replace("is N/A", "is unspecified");
		descriptionEvent = descriptionEvent.replace("related to N/A.", "related to the whole VC.");
		descriptionEvent = descriptionEvent.replace("About the LAU: N/A.", "LAU is unspecified");
		descriptionEvent = descriptionEvent.replace("About the LAU: N/A.", "LAU is unspecified");
		descriptionEvent = descriptionEvent.replace("has been N/A", "is unspecified");
		descriptionEvent = descriptionEvent.replace("In this region protected areas N/A present.", "");
		if (descriptionEvent.trim().equals("are."))
			descriptionEvent = "";
		if (descriptionEvent.trim().equals("Yes"))
			descriptionEvent = "";
		
		if (descriptionEvent.trim().startsWith("Yes,"))
			descriptionEvent = descriptionEvent.replace("Yes,", "");
		
		
		descriptionEvent = descriptionEvent.replace("..", ".");
		if (descriptionEvent.equals("N/A") || descriptionEvent.equals("N/A."))
			descriptionEvent = "";
		descriptionEvent=descriptionEvent.trim();
		
		String row = "\"" + titleEvent + "\"" + sep + "\"" + descriptionEvent + "\"" + sep + "\"" + objs + "\"" + sep + "\""
				+ links + "\"" + sep + "\"" + longitude + "\"" + sep + "\"" + latitude + "\""+sep+"\""+barycenterlongitude+"\""+sep+"\""+barycenterlatitude+"\"";
		
		
		return row;
	}

	public Pair decideBestCoordinatesWithRedistribution(List<Pair> allCoordinates, List<Boolean> allCoordinatesFitness,
			List<Pair> assignedCoordinates) {
		CoordinateDistributor cd = new CoordinateDistributor();
		Pair best = cd.decideBestCoordinates(allCoordinates, allCoordinatesFitness, assignedCoordinates, candidateCoordinates, title);
		assignCoordinates(best.longitude,best.latitude);
		return best; 
	}
	
	public Pair assignRedistributedCoordinates(List<Pair> allCoordinates, List<Boolean> allCoordinatesFitness,
			List<Pair> assignedCoordinates) {
		CoordinateDistributor cd = new CoordinateDistributor();
		Pair best = cd.redistributeCoordinates(allCoordinates, allCoordinatesFitness, assignedCoordinates, title, new Pair(longitude,latitude));
		assignCoordinates(best.longitude,best.latitude);
		return best;
	}
	
	public Pair decideBestCoordinates(List<Pair> allCoordinates, List<Boolean> allCoordinatesFitness,
			List<Pair> assignedCoordinates) {

		// if empty, assign the most common coordinate with a high score
		if (candidateCoordinates.size() == 0 || (candidateCoordinates.size() == 1
				&& candidateCoordinates.get(0).longitude == 0 && candidateCoordinates.get(0).latitude == 0)) {

			int highestoccs = 0;
			Pair highestPair = null;
			int counter = 0;
			for (Pair candidate : allCoordinates) {

				if (allCoordinatesFitness.get(counter)) {

					int nocc = Pair.contains(allCoordinates, candidate);
					if (nocc > highestoccs) {
						highestoccs = nocc;
						highestPair = new Pair(candidate.longitude, candidate.latitude);

					}
				}
			}

			System.out.println(
					"Event '" + title + "' is Trivial -> " + highestPair + " (n. occurr. " + highestoccs + ")");
			assignCoordinates(highestPair.longitude, highestPair.latitude);
		} else {
			// select only good coordinates
			List<Pair> goodCandidates = new ArrayList();
			for (Pair candidate : candidateCoordinates) {
				int idx = Pair.getIndex(allCoordinates, candidate);
				boolean b = allCoordinatesFitness.get(idx);
				if (b)
					goodCandidates.add(candidate);
			}

			int lowestoccs = allCoordinates.size();
			Pair lowestPair = null;
			// sort goodCandidates by inverse n occurrences
			List<Integer> goodCandidatesNocc = new ArrayList();

			for (Pair candidate : goodCandidates) {

				int nocc = Pair.contains(allCoordinates, candidate);
				goodCandidatesNocc.add(nocc);
				if (nocc < lowestoccs) {
					lowestoccs = nocc;
					lowestPair = new Pair(candidate.longitude, candidate.latitude);
					if (nocc <= 1)
						break;
				}
			}

			// if unique among all -> assign
			if (lowestoccs <= 1) {
				System.out.println("Event '" + title + "' has unique coordinates -> " + lowestPair + " (n. occurr. "
						+ lowestoccs + ")");
				assignCoordinates(lowestPair.longitude, lowestPair.latitude);
			} else {
				List<Pair> goodCandidatesSorted = new ArrayList();
				List<Integer> goodCandidatesSortedNocc = new ArrayList();
				int countercand = 0;
				for (Pair candidate : goodCandidates) {
					int nocc = goodCandidatesNocc.get(countercand);
					int bestposition = goodCandidatesSortedNocc.size();
					for (int k = 0; k < goodCandidatesSortedNocc.size(); k++) {
						int n = goodCandidatesSortedNocc.get(k);
						if (nocc < n) {
							bestposition = k;
							break;
						}
					}

					if (bestposition == goodCandidatesSortedNocc.size()) {
						goodCandidatesSorted.add(candidate);
						goodCandidatesSortedNocc.add(nocc);
					} else {
						goodCandidatesSorted.add(bestposition, candidate);
						goodCandidatesSortedNocc.add(bestposition, nocc);
					}

					countercand++;
				}

				System.out.println("Sorted check: " + goodCandidates.size() + " vs " + goodCandidatesSorted.size());
				System.out.println("Sorted : " + goodCandidatesNocc + " -> " + goodCandidatesSortedNocc);
				System.out.println("Sorted : " + goodCandidates + " -> " + goodCandidatesSorted);
				System.out.println("All : " + allCoordinates);

				// act on sorted goodCandidates by inverse n occurrences and get the first
				// unseen before

				// else if unique respect to the previous best ones -> assign
				lowestoccs = assignedCoordinates.size() + 1;
				lowestPair = null;

				for (Pair candidate : goodCandidatesSorted) {
					int nocc = Pair.contains(assignedCoordinates, candidate);

					if (nocc < lowestoccs) {
						lowestoccs = nocc;
						lowestPair = new Pair(candidate.longitude, candidate.latitude);
						if (nocc == 0)
							break;
					}

				}

				if (lowestoccs == 0) {
					System.out.println("Event '" + title + "' has unprecedented coordinates -> " + lowestPair
							+ " (n. occurr. " + lowestoccs + ")");
					assignCoordinates(lowestPair.longitude, lowestPair.latitude);
				} else {
					System.out.println("Event '" + title + "' has lowly observed previous coordinates -> " + lowestPair
							+ " (n. occurr. " + lowestoccs + ")");
					// else assign the latest shared one (lowest number of overlaps)
					assignCoordinates(lowestPair.longitude, lowestPair.latitude);

				}
			}

		}

		Pair bestPair = new Pair(this.longitude, this.latitude);
		return bestPair;
	}

	public void filterObjectswithIRI() throws Exception {
		LinkedHashSet<String> objectwithIRI = new LinkedHashSet<>();

		for (String obj : associatedObjects) {
			if (obj.length() > 2) {
				// get link
				WikidataExplorer we = new WikidataExplorer();
				String uri = we.queryWikidata(obj);
				// if is in wikidata
				if (uri.length() > 0) {
					// select the object
					objectwithIRI.add(obj);
					wikidatalinks.add(uri);
				}
			}
		}

		associatedObjects.clear();
		associatedObjects.addAll(objectwithIRI);

	}

	public List<Pair> inferCoordinates() throws Exception {
		List<Pair> coordinates = new ArrayList<>();
		
		for (String obj : associatedObjects) {
			if (obj.length() > 2) {
				WikidataExplorer we = new WikidataExplorer();
				Pair p = we.getCoordinates(obj);
				if (p != null)
					coordinates.add(p);
			}
		}
		if (coordinates.size()==0)
		{
			coordinates.add(new Pair(0,0));
		}
		
		addCandidateCoordinates(coordinates);
		assignCoordinates(coordinates.get(0).longitude, coordinates.get(0).latitude);
		
		
		return coordinates;

	}
	
}
