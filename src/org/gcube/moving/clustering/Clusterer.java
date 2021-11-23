package org.gcube.moving.clustering;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.gcube.moving.utils.Pair;

public class Clusterer {

	
	public Clusterer() {
		
		
	}
	
	public List<Boolean> clusterCoordinates(List<Pair> allCoordinates) {
		
		double[][]features = new double[allCoordinates.size()][2];
		int i = 0;
		for (Pair p:allCoordinates) {
			features[i][0] = p.longitude;
			features[i][1] = p.latitude;
			
			i++;
		}
		
		File tempOutputFile = new File("clustering"+UUID.randomUUID()+".csv");
		int minclusters = 1;
		int maxClusters = 3;
		int minElementsinCluster = 10;
		MultiKMeans kmeans = new MultiKMeans();
		
		try {
			kmeans.cluster(features, minElementsinCluster, minclusters, maxClusters, tempOutputFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		int bestcluster = -1;
		int bestclustersize = -1;
		for (Integer cindex : kmeans.clusters.keySet()) {
			List<double[]> cluster = kmeans.clusters.get(cindex);
			int s = cluster.size();
			if (s>bestclustersize) {
				bestcluster = cindex;
				bestclustersize = s;
			}
				
		}
		
		System.out.println("Best cluster is "+bestcluster+" with size "+bestclustersize);
		List<Boolean> goodCoordinate = new ArrayList<>();
		List<double[]> bcluster = kmeans.clusters.get(bestcluster);
		
		for (Pair p:allCoordinates) {
			
			boolean good = false;
			
			for (double[] e:bcluster) {
				
				if (e[0] == p.longitude && e[1] == p.latitude) {
					good = true;
					break;
					
				}
				
			}
			
			goodCoordinate.add(good);
		}
		
		
		tempOutputFile.delete();
		
		System.out.println("Fitness of coordinates:\n"+goodCoordinate);
		return goodCoordinate;
	}
	
}
