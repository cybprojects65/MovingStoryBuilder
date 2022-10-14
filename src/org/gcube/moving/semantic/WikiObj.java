package org.gcube.moving.semantic;

import java.io.Serializable;

import org.gcube.moving.utils.Pair;

public class WikiObj implements Serializable{

	
		/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
		public String uri;
		public Pair coordinates;
		public WikiObj(String uri,Pair coordinates) {
			this.uri = uri;
			this.coordinates = coordinates;
		}
}
