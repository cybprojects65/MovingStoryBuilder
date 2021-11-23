package org.gcube.moving.events;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

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

		String row = "\"" + title + "\"" + sep + "\"" + description + "\"" + sep + "\"" + objs + "\"" + sep + "\""
				+ links + "\"" + sep + "\"" + longitude + "\"" + sep + "\"" + latitude + "\"";
		return row;
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
