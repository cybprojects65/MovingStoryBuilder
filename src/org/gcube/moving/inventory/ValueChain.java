package org.gcube.moving.inventory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import org.gcube.moving.clustering.Clusterer;
import org.gcube.moving.events.Event;
import org.gcube.moving.geocoding.GoogleGeocoder;
import org.gcube.moving.utils.Pair;

import com.opencsv.CSVReader;

public class ValueChain {

	LinkedHashMap<String, Event> events = new LinkedHashMap<>();
	Event[] eventArray = null;
	String[] headersArray = null;

	public ValueChain() throws Exception {

		CSVReader reader = new CSVReader(new FileReader("mappingtable.csv"));
		List<String[]> rows = reader.readAll();
		int i = 0;
		for (String[] r : rows) {
			if (i > 0) {

				String eventLabel = r[1];
				Event e = events.get(eventLabel);
				if (e == null) {
					e = new Event(eventLabel);
					events.put(eventLabel, e);
				}
			}

			i++;
		}

		reader.close();

	}

	public void parseHeader(String headers) {

		headersArray = headers.split(",");

	}

	public String[] getRow(String header) throws Exception {

		CSVReader reader = new CSVReader(new FileReader("mappingtable.csv"));
		List<String[]> rows = reader.readAll();
		reader.close();
		int i = 0;
		for (String[] r : rows) {
			if (i > 0) {

				String dbLabel = r[0];

				if (dbLabel.equals(header)) {
					return r;

				}

			}

			i++;
		}
		return null;

	}

	public String getEventLabel(String row[]) throws Exception {
		return row[1];
	}

	public String getDescription(String row[], String content) throws Exception {
		if (content.equalsIgnoreCase("Yes") || content.equalsIgnoreCase("Y"))
			content = "are";
		else if (content.equalsIgnoreCase("No") || content.equalsIgnoreCase("N"))
			content = "are not";

		return (row[2].trim() + " " + content + " " + row[3].trim()).trim();
	}

	public boolean isTitle(String row[]) throws Exception {
		return Boolean.parseBoolean(row[4]);
	}

	public boolean isGeospatial(String row[]) throws Exception {
		return Boolean.parseBoolean(row[5]);
	}

	public boolean isDescriptive(String row[]) throws Exception {
		return Boolean.parseBoolean(row[6]);
	}

	public void analyseField(String field, int columnIndex) throws Exception {

		String header = headersArray[columnIndex].trim();
		System.out.println("H:" + header);
		if (header.trim().length() == 0)
			return;

		if (field.trim().length() == 0)
			return;

		String row[] = getRow(header);
		String eventLabel = getEventLabel(row);
		if (field.length() > 10)
			System.out.println("Field " + field.substring(0, 10) + "[..]" + " (" + header + ")" + " -> " + eventLabel);
		else
			System.out.println("Field " + field + " (" + header + ")" + " -> " + eventLabel);
		Event e = events.get(eventLabel);
		String description = getDescription(row, field);
		if (isTitle(row))
			e.setTitle(description);
		else
			e.updateDescription(description);

	}

	List<Pair> allCoordinates = new ArrayList<>();
	List<Boolean> allCoordinatesFitness = new ArrayList<>();
	List<Pair> assignedCoordinates = new ArrayList<>();
	public static boolean simulatecoordinates = true;

	public String produceEvents() throws Exception {

		//extract and filter objects extracted by the nlp hub
		
		System.out.println("# ENRICHING OBJECTS WITH NE, IRI, AND COORDINATES #");
		for (String label : events.keySet()) {

			Event e = events.get(label);
			if (e.description.trim().length() > 0) {

				ObjectExtractor oe = new ObjectExtractor();
				oe.extractObjects(e.description.trim());

				System.out.println("Event objects " + e.title + "->" + oe.allobjs);
				System.out.println("Event objects with potential geo information" + e.title + "->" + oe.allobjs);

				e.updateObjects(oe.allobjs);
				e.filterObjectswithIRI();

				List<Pair> ps = e.inferCoordinates();
				if (ps.size() > 1 || ps.get(0).longitude != 0)
					allCoordinates.addAll(ps);

				System.out.println("Event coordinates " + e.title + "->" + ps);
				System.out.println("Adding event " + label);
			}

		}
		System.out.println("# END - ENRICHING OBJECTS WITH NE, IRI, AND COORDINATES #");
		
		System.out.println("# CLUSTERING COORDINATES #");
		
		// score all coordinates based on the largest cluster
		Clusterer cluster = new Clusterer();
		allCoordinatesFitness = cluster.clusterCoordinates(allCoordinates);
		
		System.out.println("# END - CLUSTERING COORDINATES #");
		
		System.out.println("# OPTIMIZE COORDINATE ASSIGNED TO GEOSPATIAL EVENTS #");
		
		for (String label : events.keySet()) {
			Event e = events.get(label);
			// Pair bestPair = e.decideBestCoordinates(allCoordinates,
			// allCoordinatesFitness, assignedCoordinates);
			Pair bestPair = e.decideBestCoordinatesWithRedistribution(allCoordinates, allCoordinatesFitness,
					assignedCoordinates);
			if (bestPair.longitude!=0 && bestPair.latitude!=0)
				assignedCoordinates.add(bestPair);
		}

		System.out.println("# END - OPTIMIZE COORDINATE ASSIGNED TO GEOSPATIAL EVENTS #");
		
		System.out.println("# REDISTRIBUTED COORDINATES TO NON-GEOSPATIAL EVENTS #");
		//redistribute residual coordinates
		for (String label : events.keySet()) {
			Event e = events.get(label);
			Pair bestPair = e.assignRedistributedCoordinates(allCoordinates, allCoordinatesFitness,
					assignedCoordinates);
			if (bestPair.longitude!=0 && bestPair.latitude!=0)
				assignedCoordinates.add(bestPair);
		}
		System.out.println("# END - REDISTRIBUTED COORDINATES TO NON-GEOSPATIAL EVENTS #");
		
		System.out.println("# TRANSFORMING EVENT INTO STRINGS #");
		//report all events
		StringBuffer sb = new StringBuffer();
		for (String label : events.keySet()) {
			Event e = events.get(label);
			sb.append(e.toString() + "\n");
		}
		System.out.println("");
		System.out.println("# END - TRANSFORMING EVENT INTO STRINGS #");
		
		return sb.toString();
	}

	public String produceEventsLegacy() throws Exception {

		for (String label : events.keySet()) {

			Event e = events.get(label);
			if (e.description.trim().length() > 0) {

				ObjectExtractor oe = new ObjectExtractor();
				oe.extractObjects(e.description.trim());

				System.out.println("Event objects " + e.title + "->" + oe.allobjs);
				System.out.println("Event objects with potential geo information" + e.title + "->" + oe.allobjs);

				e.updateObjects(oe.allobjs);

				List<Pair> ps = inferCoordinates(oe.potentialgeoobjs);

				e.addCandidateCoordinates(ps);

				System.out.println("Event coordinates " + e.title + "->" + ps);

				e.assignCoordinates(ps.get(0).longitude, ps.get(0).latitude);

				System.out.println("Adding event " + label);

			}

		}

		StringBuffer sb = new StringBuffer();

		if (!simulatecoordinates) {
			// score all coordinates based on the largest cluster
			Clusterer cluster = new Clusterer();
			allCoordinatesFitness = cluster.clusterCoordinates(allCoordinates);

			for (String label : events.keySet()) {
				Event e = events.get(label);
				Pair bestPair = e.decideBestCoordinates(allCoordinates, allCoordinatesFitness, assignedCoordinates);
				assignedCoordinates.add(bestPair);
				e.filterObjectswithIRI();
				sb.append(e.toString() + "\n");
			}

		} else {// score all coordinates based on the largest cluster

			for (String label : events.keySet()) {
				Event e = events.get(label);
				sb.append(e.toString() + "\n");
			}
		}

		System.out.println("");
		return sb.toString();
	}

	public List<Pair> inferCoordinates(LinkedHashSet<String> objects) {

		if (simulatecoordinates) {

			Pair p = new Pair(1, 1);
			List<Pair> ps = new ArrayList<>();
			ps.add(p);

			File tempFile = new File("listOfObjectsToGeospatialise.txt");
			HashSet<String> hashSet = new HashSet<>();
			try {
				if (tempFile.exists())
					hashSet.addAll(Files.readAllLines(tempFile.toPath()));
				hashSet.addAll(objects);
				Writer fstream = new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8);

				for (String hs : hashSet) {
					fstream.write(hs + "\n");
				}

				fstream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return ps;
		}
		if (objects.size() == 0) {

			Pair p = new Pair(0, 0);
			List<Pair> ps = new ArrayList<>();
			ps.add(p);
			return ps;

		} else {

			LinkedHashSet<String> geospatialOnes = new LinkedHashSet<>();
			List<Pair> ps = new ArrayList<>();

			for (String o : objects) {
				GoogleGeocoder gg = new GoogleGeocoder();
				try {
					gg.geocode(o);
					if (gg.address != null && gg.address.length() > 0 && !gg.isApproximate) {
						System.out.println(o + "->" + gg.longitude + ";" + gg.latitude);
						Pair p = new Pair(gg.longitude, gg.latitude);

						geospatialOnes.add(o);
						ps.add(p);
						allCoordinates.add(p);
					} else if (gg.address != null && gg.address.length() > 0 && gg.isApproximate) {
						System.out.println(
								o + "-> approximated by " + gg.address + "[" + gg.longitude + ";" + gg.latitude + "]");
					} else
						System.out.println(o + "-> no code");
				} catch (Exception e) {

				}

			}

			if (geospatialOnes.size() == 0) {
				Pair p = new Pair(0, 0);
				ps = new ArrayList<>();
				ps.add(p);
				return ps;
			} else
				return ps;

		}

	}
}
