package org.gcube.moving.utils;

import java.io.Serializable;
import java.util.List;

public class Pair implements Serializable{


	private static final long serialVersionUID = 1L;
	public double longitude;
	public double latitude;
	
	public Pair(double longi, double lati) {
		longitude = longi;
		latitude = lati;
	}
	
	public String toString() {
		return longitude+";"+latitude;
	}

	
	public static int contains(List<Pair> list,Pair p) {
		int counter = 0;
		
		for (Pair pp:list) {
			
			if (pp.longitude == p.longitude && pp.latitude == p.latitude)
					counter++;
		}
		
		return counter;
		
	}
	
	public static int getIndex(List<Pair> list,Pair p) {
		int index = 0;
		
		for (Pair pp:list) {
			
			if (pp.longitude == p.longitude && pp.latitude == p.latitude) {
					return index;
			}
			index++;
		}
		
		return -1;
		
	}
	
}
