package com.hml.weka;

import java.util.Comparator;

public class Cluster implements Comparable{
	
	private String id;
	private int cluster;
	private String instanceValue;

	public Cluster(String id, int cluster, String attributeValue){
		this.id = id;
		this.cluster = cluster;
		this.instanceValue = attributeValue;
	}

	@Override
	public int compareTo(Object o) {
		
		Cluster p2 = (Cluster)o;
		
		if (cluster == p2.cluster)
			return 0;
		else if (cluster > p2.cluster)
			return 1;
		else
			return -1;
	}


	public String getId() {
		return id;
	}

	public int getCluster() {
		return cluster;
	}
	
	public String getInstanceValue() {
		return instanceValue;
	}
	
	@Override
	public String toString(){
		return "ID : "+this.id+" | Cluster Number "+this.cluster+" | Attribute "+this.instanceValue;
	}
	
	
}

