package org.gcube.moving.geocoding;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.gcube.moving.utils.Pair;

public class CoordinateDistributor {

	public Pair decideBestCoordinates(List<Pair> allCoordinates, List<Boolean> allCoordinatesFitness,
			List<Pair> assignedCoordinates, List<Pair> eventCoordinates, String title) {

		// if empty, assign the most common coordinate with a high score
		if (eventCoordinates.size() == 0 || (eventCoordinates.size() == 1 && eventCoordinates.get(0).longitude == 0
				&& eventCoordinates.get(0).latitude == 0)) {
			return new Pair(0, 0);
		} else {
			// the event has coordinates
			// select only good coordinates
			List<Pair> goodCandidates = new ArrayList();
			for (Pair candidate : eventCoordinates) {
				int idx = Pair.getIndex(allCoordinates, candidate);
				//System.out.println("searching "+candidate+" in "+allCoordinates);
				boolean b = false;
				if (idx>-1)
					b = allCoordinatesFitness.get(idx);
				if (b)
					goodCandidates.add(candidate);
			}

			
			if (goodCandidates.size() == 0)
					return new Pair(0, 0);
				
			int lowestoccs = allCoordinates.size();
			Pair lowestPair = null;

			List<Integer> goodCandidatesNocc = new ArrayList();
			//take the most characteristic place
			for (Pair candidate : goodCandidates) {

				int nocc = Pair.contains(allCoordinates, candidate);
				goodCandidatesNocc.add(nocc);
				if (nocc <= lowestoccs) {
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
				return new Pair(lowestPair.longitude, lowestPair.latitude);

			} else {
				// assign the one with the lowest occurrences that has not been seen before
				// sort candidates
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

				// get the first unseen or lowly seen before
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
				} else {
					System.out.println("Event '" + title + "' has lowly observed previous coordinates -> " + lowestPair
							+ " (n. occurr. " + lowestoccs + ")");
				}

				return new Pair(lowestPair.longitude, lowestPair.latitude);
			}

		}
	}

	public Pair redistributeCoordinates(List<Pair> allCoordinates, List<Boolean> allCoordinatesFitness,
			List<Pair> assignedCoordinates, String title, Pair currentPair) {

		// if empty, assign the most common coordinate with a high score
		if (currentPair.longitude == 0 && currentPair.latitude == 0) {

			// get the first unseen or lowly seen before
			int counter = 0;
			for (Pair candidate : allCoordinates) {
				
				if (allCoordinatesFitness.get(counter)) {
					int nocc = Pair.contains(assignedCoordinates, candidate);
					if (nocc ==0 ) {
						System.out.println("Event '" + title + "' is being assigned unprecedented coordinates -> " + new Pair(candidate.longitude,candidate.latitude));
						return new Pair(candidate.longitude, candidate.latitude);
					}
				}
				
				counter++;
			}
			
			//if we did not find any unseen coordinate, assign the most common one
			int highestoccs = 0;
			Pair highestPair = null;
			counter = 0;
			for (Pair candidate : allCoordinates) {
				if (allCoordinatesFitness.get(counter)) {
					int nocc = Pair.contains(allCoordinates, candidate);
					if (nocc > highestoccs) {
						highestoccs = nocc;
						highestPair = new Pair(candidate.longitude, candidate.latitude);
					}
				}
				counter++;
			}

			double randomjitterx = -0.1 + (0.1 - (-0.1)) * new Random().nextDouble();
			
			double randomjittery = -0.1 + (0.1 - (-0.1)) * new Random().nextDouble();
			
			highestPair.longitude =highestPair.longitude+randomjitterx; 
			highestPair.latitude =highestPair.latitude+randomjittery;
			
			System.out.println(
					"Event '" + title + "' is being assigned the most common coordinates -> " + highestPair + " (n. occurr. " + highestoccs + ")");
			
			return new Pair(highestPair.longitude, highestPair.latitude);

		} else {
			return currentPair;
		}
	}

}
