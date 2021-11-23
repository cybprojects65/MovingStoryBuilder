package org.gcube.moving.clustering;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.gcube.moving.utils.Pair;

public class Clusterer {

	public Clusterer() {

	}

	public List<Boolean> clusterCoordinatesStatistical(List<Pair> allCoordinates) {

		int nco = allCoordinates.size();
		double logmeanx = 0;
		double logmeany = 0;
		
		for (Pair p : allCoordinates) {
			logmeanx = logmeanx + Math.log(p.longitude);
			logmeany = logmeany + Math.log(p.latitude);
		}

		logmeanx = logmeanx/(double) nco;
		logmeany = logmeany/(double) nco;
		
		double dsmeanx = 0;
		double dsmeany = 0;
		for (Pair p : allCoordinates) {
			dsmeanx += ( (logmeanx - Math.log(p.longitude)) * (logmeanx - Math.log(p.longitude)) );
			dsmeany += ( (logmeany - Math.log(p.latitude))  * (logmeany - Math.log(p.latitude)) );
		}

		dsmeanx = Math.sqrt( (dsmeanx/(double) (nco-1)) );
		dsmeany = Math.sqrt( (dsmeany/(double) (nco-1)) );
		
		
		
		double m = 1.96;
		
		double thrx0 = logmeanx-m*dsmeanx;
		double thrx1 = logmeanx+m*dsmeanx;
		
		double thry0 = logmeany-m*dsmeany;
		double thry1 = logmeany+m*dsmeany;
		
		logmeanx = Math.exp(logmeanx);
		logmeany = Math.exp(logmeany);
		thrx0 = Math.exp(thrx0);
		thrx1 = Math.exp(thrx1);
		thry0 = Math.exp(thry0);
		thry1 = Math.exp(thry1);
		
		System.out.println("X:" + logmeanx+" ["+thrx0+","+thrx1+"]");
		System.out.println("Y:" + logmeany+" ["+thry0+","+thry1+"]");
		
		List<Boolean> goodCoordinate = new ArrayList<>();
		
		for (Pair p : allCoordinates) {

			boolean good = false;

			if (p.longitude>thrx0 && p.longitude<thrx1 && p.latitude>thry0 && p.latitude<thry1)
				good = true;
			
			if (good)
				System.out.println("GOOD->" + p);
			else
				System.out.println("BAD->" + p);

			goodCoordinate.add(good);
		}

		System.out.println("Fitness of coordinates:\n" + goodCoordinate);
		return goodCoordinate;
	}

	public List<Boolean> clusterCoordinates(List<Pair> allCoordinates) {

		double[][] features = new double[allCoordinates.size()][2];
		int i = 0;
		for (Pair p : allCoordinates) {
			features[i][0] = p.longitude;
			features[i][1] = p.latitude;

			i++;
		}

		System.out.println("Clustering N. points: " + features.length);

		File tempOutputFile = new File("clustering" + UUID.randomUUID() + ".csv");
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
			if (s > bestclustersize) {
				bestcluster = cindex;
				bestclustersize = s;
			}

		}

		System.out.println("Best cluster is " + bestcluster + " with size " + bestclustersize);
		List<Boolean> goodCoordinate = new ArrayList<>();
		List<double[]> bcluster = kmeans.clusters.get(bestcluster);

		for (Pair p : allCoordinates) {

			boolean good = false;

			for (double[] e : bcluster) {

				if (e[0] == p.longitude && e[1] == p.latitude) {
					good = true;
					break;

				}

			}

			if (good)
				System.out.println("GOOD->" + p);
			else
				System.out.println("BAD->" + p);

			goodCoordinate.add(good);
		}

		tempOutputFile.delete();

		System.out.println("Fitness of coordinates:\n" + goodCoordinate);
		return goodCoordinate;
	}

}
