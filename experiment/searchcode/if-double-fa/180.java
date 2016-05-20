package tools.clustering.isodata;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import neuro.uu.se.utils.Utils;

import tools.clustering.isodata.workStrategies.isodata_assigning;
import tools.fasta.fastaUtils;
import tools.utils.DoubleMatrix;

public class isodata {
	
	public static void main(String[] args)throws Exception{
//		int K0=Integer.parseInt(args[3]),I0=1,P0=10,On0=5;
		double Os0=1.5,Oc0=8;
//		for(int i=0;i<20;i++){
//			isodata io;
//			if (i==0) {
//				io=new isodata(args[0],false,K0,I0,P0,On0,Os0,Oc0);
//				io.saveDatumToFile(args[1]);
//			}else{
//				io=new isodata(args[1],true,K0,I0,P0,On0,Os0,Oc0);
//			}
//			io.cluster();
//			io.printResult(args[2]+"_"+i+"_");
//		}
//		isodata id=new isodata(args[0],false,200,200,10,5,0,0);
//		isodata_stat.distanceHistogram(id.datum, 0.2, "/local/out/Linn/Allaseq7tm2_distanceHist.csv");
//		id.saveDatumToFile(args[1]);
		System.out.println("initializing...");
//		isodata id=new isodata(args[1],true);
		isodata id=new isodata(args[0],false);
		id.saveDatumToFile(args[1]);
//		System.out.println("creating histogram");
//		isodata_stat.distanceHistogram(id.datum, 0.2, args[2]+"_distanceHist.csv");
//		System.out.println("clustering");
//		for(int i=0;i<1;i++){
//			isodata id=new isodata(args[1],true);
//			id.cluster(K0,I0,P0,On0,Os0,Oc0);
//			id.printResult("results/"+args[2]+"_"+i,args[2],true);
//		}
//		System.out.println("Finding intersection...");
//		isodata_stat.consensusClusters(args[2]+"_many.csv", args[2]+"_intersection.csv",0.75*K0);
//		id.printResult("isodata_tmp");
//		isodata id=new isodata(args[0],true);
//		id.saveDatumToFile(args[1]);
//		System.out.println("Analyzing...");
//		id.analyzeClusters(args[1], args[2]);
//		createFaForClusters("/local/membrane_protein/clustering/allMembrane_protein/allMembrane_protein_largestIPI.fa", "/local/membrane_protein/clustering/allMembrane_protein/allMembrane_protein_clusters_data_extended.csv", "/local/membrane_protein/clustering/allMembrane_protein/clusters/fa/allMembrane_protein_cluster");
		
		
		System.out.println("Done!");
		System.exit(0);
	}

	/*
	 * K= desired number of clusters
	 * I= maximum number of iterations
	 * P= maximum number of pairs of clusters which can be merged
	 * On= minimum number of samples in a cluster
	 * Os= threshold for maximum value for std (split)
	 * Oc= threshold for minimum distance (merge)
	 */
	private int K,I,P,On,nextClusterNr=1;
	private double Os,Oc,avgAllDistanceToCentroid;
	private isodata_datum datum,centroids;
	private Random rand=new Random();
	private HashMap<Integer, isodata_cluster> clusters;

	private HashMap<Integer,Integer> OldConfiguration=new HashMap<Integer, Integer>(); //<id,clusternr>
	
//	public isodata(String inFile,boolean loadOld,int K,int I,int P, int On,double Os,double Oc)throws Exception{
	public isodata(String inFile,boolean loadOld)throws Exception{
		if (loadOld) {
			loadDatumFromFile(inFile);
		}else{
			datum=new isodata_datum(inFile);
		}
		Integer tInt=new Integer(0);
		clusters=new HashMap<Integer, isodata_cluster>();
		clusters.put(new Integer(0),new isodata_cluster());
		((isodata_cluster)clusters.get(tInt)).setAvgDistanceToCentroid(0);
		for (Iterator iter = datum.getDatum().iterator(); iter.hasNext();) {
			isodata_data i = (isodata_data) iter.next();
			((isodata_cluster)clusters.get(tInt)).add(i);
		}
	}
	public isodata(String inFile,String nameIndexFile,String subsetFile,boolean double_reduce)throws Exception{
		if (double_reduce) {
			datum=new isodata_datum(inFile,nameIndexFile,subsetFile,true);
		}else{
			datum=new isodata_datum(inFile,nameIndexFile,subsetFile);
		}
		Integer tInt=new Integer(0);
		clusters=new HashMap<Integer, isodata_cluster>();
		clusters.put(new Integer(0),new isodata_cluster());
		((isodata_cluster)clusters.get(tInt)).setAvgDistanceToCentroid(0);
		for (Iterator iter = datum.getDatum().iterator(); iter.hasNext();) {
			isodata_data i = (isodata_data) iter.next();
			((isodata_cluster)clusters.get(tInt)).add(i);
		}
	}
	public void cluster(int K,int I,int P,int On,double Os,double Oc)throws Exception{
		this.K=K;
		this.I=I;
		this.P=P;
		this.On=On;
		this.Os=Os;
		this.Oc=Oc;
		boolean updated;
		ArrayList toUpdate,updatedClusters;
		Integer tInt;
		int k=0;
		double tDouble;
		double[] tDoubleA;
		Object[] keyset;
		for (Integer i : datum.getAllIndices()) {
			OldConfiguration.put(i,new Integer(datum.getCluster(i).intValue()));
		}
		//choose initial centroids
		int[] exists=new int[K];
		ArrayList<isodata_data> cTmp=new ArrayList<isodata_data>();
		for(int i=0;i<K;i++){
			exists[i]=datum.getAllIndices().get(rand.nextInt(datum.size()));
			boolean tmp=false;
			//check if it is already taken
			for(int j=0;j<i&&!tmp;j++){
				tmp=(exists[i]==exists[j]);
			}
			//If not taken, add it to the arraylist
			if(!tmp){
				tInt=new Integer(nextClusterNr);
				cTmp.add( new isodata_data(new DoubleMatrix((DoubleMatrix)datum.getData(new Integer(exists[i]))),tInt) );
				clusters.put(tInt,new isodata_cluster());
				nextClusterNr++;
			}
		}
		centroids=new isodata_datum(cTmp);
		datum.setAllCentroids(centroids);
		centroids.calculateDistances(true);
		while(k<I){
			//assign datum to clusters
			System.out.println("Itteration: "+k);
			System.out.println("Nr of clusters: "+clusters.size());
			Utils.forEachCollectionObject(new isodata_assigning(),datum.getDatum());
			this.updateClusters();
			//drop small clusters
			updated=false;
			toUpdate=new ArrayList();
			keyset=clusters.keySet().toArray();
			for (int i0=0;i0<keyset.length;i0++) {
				Integer i = (Integer) keyset[i0];
				ArrayList curClust=(ArrayList)clusters.get(i);
				//if cluster size smaller than On, drop it
				if(curClust.size()<On){
					updated=true;
					for (Iterator iterator = curClust.iterator(); iterator.hasNext();) {
						isodata_data j = (isodata_data) iterator.next();
						j.setCluster(new Integer(0));
						toUpdate.add(j);
					}
					clusters.remove(i);
					centroids.remove(i);
				}
			}
			//reinsert dropped data into clusters
			if(updated){
				//update only those that has been dropped
				centroids.calculateDistances();
				Utils.forEachListObject(new isodata_assigning(),toUpdate);
				this.updateClusters(toUpdate);
			}
			//calculate new centroids
			for (Iterator iter = clusters.keySet().iterator(); iter.hasNext();) {
				Integer i = (Integer) iter.next();
				if(i.intValue()!=0){
					this.updateCentroid(i);
				}
			}
			centroids.calculateDistances(true);
			//calculate avgDistances
			avgAllDistanceToCentroid=0;
			for (Iterator iter = clusters.values().iterator(); iter.hasNext();) {
				isodata_cluster curClust=(isodata_cluster) iter.next();
				tDouble=0;
				for (Iterator iterator = curClust.iterator(); iterator.hasNext();) {
					tDouble+= ((isodata_data) iterator.next()).getDistanceToCentroid();
				}
				avgAllDistanceToCentroid+=tDouble;
//				System.out.println("clustAVG\t"+tDouble/curClust.size());
				curClust.setAvgDistanceToCentroid(tDouble/curClust.size());
			}
			avgAllDistanceToCentroid/=datum.size();
			
			System.out.println("totAVG\t"+avgAllDistanceToCentroid);
			//split, if not too many clusters
			if(clusters.size()<=2*K){
				updated=false;
				toUpdate=new ArrayList();
				updatedClusters=new ArrayList();
				keyset=clusters.keySet().toArray();
				for (int i0=0; i0<keyset.length;i0++) {
					Integer i = (Integer) keyset[i0];
					isodata_cluster curClust=(isodata_cluster)clusters.get(i);
					if(curClust.getAvgDistanceToCentroid()>avgAllDistanceToCentroid && curClust.size()>2*On){
						tDoubleA=calculateMaxSTD(i);
						if(tDoubleA[0]>Os){
							updated=true;
							toUpdate.addAll(curClust);
							tInt=new Integer(0);
							for (Iterator iter = curClust.iterator(); iter.hasNext();) {
								((isodata_data) iter.next()).setCluster(tInt);
								
							}
							//update centroid and add new centroid
							((DoubleMatrix)centroids.getData(i)).add(0, (int)tDoubleA[1], tDoubleA[0]);
							tInt=new Integer(nextClusterNr);
							DoubleMatrix d=new DoubleMatrix((DoubleMatrix)centroids.getData(i));
							d.add(0, (int)tDoubleA[1], -2*tDoubleA[0]);
							centroids.putData(tInt, new isodata_data(d,tInt));
							clusters.put(tInt,new isodata_cluster());
							updatedClusters.add(i);
							updatedClusters.add(tInt);
							nextClusterNr++;
						}
					}
				}
				if(updated){
					centroids.calculateDistances(true);
					datum.setAllDataDistancesAndOrders(centroids);
					Utils.forEachListObject(new isodata_assigning(), toUpdate);
					this.updateClusters(toUpdate);
					for (Iterator iter = updatedClusters.iterator(); iter.hasNext();) {
						this.updateCentroid((Integer) iter.next());
					}
					centroids.calculateDistances();
				}
			}
			//Merge, if not too few clusters
			if(clusters.size()>=K/2){
				for (Iterator iter = centroids.getNClosestClusters(P, Oc).iterator(); iter.hasNext();) {
					int[] pair = (int[]) iter.next();
					//Merge cluster row and column by inserting column into row
					tInt=new Integer(pair[0]);
					ArrayList curClust=(ArrayList)clusters.get(new Integer(pair[1]));
					for (Iterator iterator = curClust.iterator(); iterator.hasNext();) {
						isodata_data i = (isodata_data) iterator.next();
						i.setCluster(tInt);
					}
					DoubleMatrix tmp=((isodata_data)centroids.getData(tInt)).getData();
					tmp.multiplyThis(((ArrayList)clusters.get(tInt)).size());
					tmp.addToThis(((isodata_data)centroids.getData(new Integer(pair[1]))).getData().multiplyThis(curClust.size()));
					((ArrayList)clusters.get(tInt)).addAll(curClust);
					centroids.setData(tInt, tmp.divideThis(((ArrayList)clusters.get(tInt)).size()));
					centroids.remove(new Integer(pair[1]));
					clusters.remove(new Integer(pair[1]));
				}
			}
			updated=false;
			for (Integer i : datum.getAllIndices()) {
				Integer i1=OldConfiguration.get(i);
				Integer i2=datum.getCluster(i);
				if(i2.intValue()!=i1.intValue()){
					updated=true;
					OldConfiguration.put(i, new Integer(i2.intValue()));
				}
			}
			if(!updated){
				//no points moved
				System.out.println("Finished!");
				break;
			}
			centroids.calculateDistances(true);
			datum.setAllDataDistancesAndOrders(centroids);
			k++;
		}
	}

	public void analyzeClusters(String clusterFile,String outPrefix)throws Exception{
		final int restClusterID=616;
		final double histBin=0.1;
		HashMap<Integer, Integer>histAll,histAvg,histTotAvg;
		HashMap<Integer, Double> totAvg,allAvg;
		BufferedReader in=new BufferedReader(new FileReader(clusterFile));
		String[] l;
		Integer id,clust;
		ArrayList tmp=new ArrayList();
		boolean done;
		double tD,avg,clustAvg;
		int bin,maxBin,maxTotBin;
		//Load
		for(String s=in.readLine();s!=null;s=in.readLine()){
			if(s.length()>0){
				l=s.split("\t");
				id=new Integer(l[0]);
				clust=new Integer(l[1]);
				clust=new Integer(clust.intValue()+1);//move it away from the zero
				datum.setCluster(id, clust);
				if(!clusters.containsKey(clust)){
					clusters.put(clust, new isodata_cluster());
					tmp.add(new isodata_data((DoubleMatrix)datum.getData(id),clust));
				}
			}
		}
		//Prepare
		centroids=new isodata_datum(tmp);
		done=false;
		for (Integer i : datum.keySet()) {
			if (datum.getCluster(i).intValue()==0) {
				if(!done){
					centroids.putData(restClusterID, new isodata_data((DoubleMatrix)datum.getData(i),restClusterID));
					done=true;
				}
				datum.setCluster(i, restClusterID);
			}
		}
		clusters.put(new Integer(restClusterID), new isodata_cluster());
		datum.setAllCentroids(centroids);
		this.updateClusters();
		//Print for each cluster
		BufferedWriter out,out2;
		String Wilks;
		histTotAvg=new HashMap<Integer, Integer>();
		totAvg=new HashMap<Integer, Double>();
		allAvg=new HashMap<Integer, Double>();
		maxTotBin=0;
		for (Integer i : centroids.keySet()) {
			clustAvg=0;
			this.updateCentroid(i);
			tmp=clusters.get(i);
			out=new BufferedWriter(new FileWriter(outPrefix+"_cluster_"+(i.intValue()-1)+".csv"));
			out2=new BufferedWriter(new FileWriter(outPrefix+"_cluster_"+(i.intValue()-1)+".Wilksdata"));
			Wilks="shapiro.test(c(";
			out.write("Max sd: "+calculateMaxSTD(i)[0]+"\nID's:");
			histAll=new HashMap<Integer, Integer>();
			histAvg=new HashMap<Integer, Integer>();
			maxBin=0;
			for (Iterator iter = tmp.iterator(); iter.hasNext();) {
				isodata_data j = (isodata_data) iter.next();
				out.write("\t"+j.getId());
			}
			out.write("\tAvg to all other\nEuclidian distance to centroid:");
			for (Iterator iter = tmp.iterator(); iter.hasNext();) {
				isodata_data j = (isodata_data) iter.next();
				out.write("\t"+j.distanceTo((DoubleMatrix)centroids.getData(i)));
			}
			out.write("\nLength normalized global distance to:\n");
			clustAvg=0;
			for (Iterator iter = tmp.iterator(); iter.hasNext();) {
				isodata_data j = (isodata_data) iter.next();
				out.write(j.getId()+"");
				avg=0;
				for (Iterator iterator = tmp.iterator(); iterator.hasNext();) {
					isodata_data k = (isodata_data) iterator.next();
					tD=j.getData().get(0, k.getId());//the distance between j and k
					if(j.getId().intValue()!=k.getId().intValue()){
						avg+=tD;
					}
					bin=(int)Math.floor(tD/histBin);
					maxBin=bin>maxBin?bin:maxBin;
					if(histAll.containsKey(bin)){
						histAll.put(bin, histAll.get(bin).intValue()+1);
					}else{
						histAll.put(bin, 1);
					}
					out.write("\t"+tD);
				}
				avg/=(tmp.size()-1);
				allAvg.put(j.getId(), avg);
				clustAvg+=avg;
				bin=(int)Math.floor(avg/histBin);
				if(histAvg.containsKey(bin)){
					histAvg.put(bin, histAvg.get(bin).intValue()+1);
				}else{
					histAvg.put(bin, 1);
				}
				Wilks+=avg+",";
				out.write("\t"+avg+"\n");
			}
			clustAvg/=tmp.size();
			totAvg.put(i, clustAvg);
			bin=(int)Math.floor(clustAvg/histBin);
			maxTotBin=bin>maxTotBin?bin:maxTotBin;
			if(histTotAvg.containsKey(bin)){
				histTotAvg.put(bin, histTotAvg.get(bin).intValue()+1);
			}else{
				histTotAvg.put(bin, 1);
			}
			out.write("\nHistogram data, binSize:\t"+histBin+"\nBin\tAll\tAvg\n");
			for(int j=0;j<=maxBin;j++){
				out.write(j+"");
				if(histAll.containsKey(j)){
					out.write("\t"+histAll.get(j));
				}else{
					out.write("\t0");
				}
				if(histAvg.containsKey(j)){
					out.write("\t"+histAvg.get(j));
				}else{
					out.write("\t0");
				}
				out.write("\n");
			}
			Wilks=Wilks.substring(0,Wilks.length()-1)+"))\n";
			out2.write(Wilks);
			out.close();
			out2.close();
		}
		centroids.calculateDistances(true);
		//Print allAvg
		out=new BufferedWriter(new FileWriter(outPrefix+"_AVGdistanceToOther.csv"));
		out.write("Id\tAVG distance\n");
		for (Integer i : allAvg.keySet()) {
			out.write((i.intValue())+"\t"+allAvg.get(i)+"\n");
		}
		out.close();
		//Print TotAvgHist
		out=new BufferedWriter(new FileWriter(outPrefix+"_AVGdistanceToCentroid.csv"));
		out.write("Cluster\tAVG distance\n");
		for (Integer i : totAvg.keySet()) {
			out.write((i.intValue()-1)+"\t"+totAvg.get(i)+"\n");
		}
		out.write("\nHistogram binSize:\t"+histBin+"\nBin\tAvg\n");
		for(int i=0;i<=maxTotBin;i++){
			out.write(i+"");
			if(histTotAvg.containsKey(i)){
				out.write("\t"+histTotAvg.get(i));
			}else{
				out.write("\t0");
			}
			out.write("\n");
		}
		out.close();
		//Print for centroids
		out=new BufferedWriter(new FileWriter(outPrefix+"_centroidsDistance.csv"));
		Object[] keySet=centroids.keySet().toArray();
		out.write("id");
		for (int i0 = 0; i0 < keySet.length; i0++) {
			out.write("\t"+((Integer)keySet[i0]).intValue());
		}
		out.write("\n");
		for (int i0 = 0; i0 < keySet.length; i0++) {
			Integer i=(Integer)keySet[i0];
			out.write(i+"");
			for (int j = 0; j < keySet.length; j++) {
//				try{
//				System.out.println(centroids.getDistance(i, (Integer)keySet[j]) );
				out.write("\t"+centroids.getDistance(i, (Integer)keySet[j]));
//				}catch (Exception e) {
//					System.out.println(i+"\t"+(Integer)keySet[j]);
//				}
			}
			out.write("\n");
		}
		out.close();
	}
	public static void createFaForClusters(String faFile,String clusterFile,String outPrefix)throws Exception{
		BufferedReader in =new BufferedReader(new FileReader(clusterFile));
		HashMap<String, ArrayList<String>> clusts=new HashMap<String, ArrayList<String>>();
		ArrayList<String> all=new ArrayList<String>();
		String[] l;
		for(String s=in.readLine();s!=null;s=in.readLine()){
			if(s.length()>0){
				l=s.split("\t");
				if(!clusts.containsKey(l[2])){
					clusts.put(l[2], new ArrayList<String>());
				}
				clusts.get(l[2]).add(l[1]);
				all.add(l[1]);
			}
		}
		for (String s : clusts.keySet()) {
			fastaUtils.subSet(faFile, clusts.get(s), true, outPrefix+"_"+s+".fa");
		}
		fastaUtils.subSet(faFile, all, false, outPrefix+"_rest.fa");
	}
	public void printResult(String outPrefix,boolean Short)throws Exception{
		this.printResult(outPrefix, outPrefix,Short);
	}
	public void printResult(String outPrefix,String many_outPrefix,boolean Short)throws Exception{
		BufferedWriter out=new BufferedWriter(new FileWriter(outPrefix+"_clusters.csv"));
		for (Integer i : datum.getAllIndices()) {
			out.write(i.intValue()+","+datum.getDistanceToCentroid(i)+","+OldConfiguration.get(i).intValue()+"\n");
		}
		out.close();
		//Append clusterNr to _many.csv
		BufferedReader in;
		try{
			in=new BufferedReader(new FileReader(many_outPrefix+"_many.csv"));
		}catch (Exception e) {
			in=new BufferedReader(new FileReader(many_outPrefix+"_names.csv"));
		}
		ArrayList<String> many=new ArrayList<String>();
		for(String s=in.readLine();s!=null;s=in.readLine()){
			if (s.length()>0) {
				if(s.endsWith("\t")){
					many.add(s.substring(0, s.length()-1));
				}else{
					many.add(s);
				}
			}
		}
		in.close();
		out=new BufferedWriter(new FileWriter(many_outPrefix+"_many.csv"));
		for (String string : many) {
			out.write(string+"\t"+OldConfiguration.get(new Integer(string.split("\t")[0]))+"\n");
		}
		out.close();
		out=new BufferedWriter(new FileWriter(outPrefix+"_info.csv"));
		out.write("Parameters:\n" +
				"Desired number of clusters: "+K+
				"\nMax iterations: "+I+
				"\nMaximum clusters to merge: "+P+
				"\nMin transcripts in a cluster: "+On+
				"\nMax standard deviation: "+Os+
				"\nMin centroid distanc: "+Oc+
				"\n\nResults:\nNumber of clusters: "+(clusters.size()-1)+
				"\nTotal average distance to centroid: "+avgAllDistanceToCentroid+
				"\n\nCluster\tSize\tAvg(distance to centroid)\tMaximum standard deviation\n");
		for (Integer i : clusters.keySet()) {
			if(i.intValue()!=0){
				out.write(i.intValue()+"\t"+clusters.get(i).size()+"\t"+clusters.get(i).getAvgDistanceToCentroid()+"\t"+calculateMaxSTD(i)[0]+"\n");
			}
		}
		if(!Short){
			out.write("\nCentroid distances:\ncluster");
			Object[] keyset=centroids.keySet().toArray();
			for (int i = 0; i < keyset.length; i++) {
				out.write("\t"+((Integer)keyset[i]).intValue());
			}
			for (int i = 0; i < keyset.length; i++) {
				out.write("\n"+((Integer)keyset[i]).intValue());
				for (int j = 0; j < keyset.length; j++) {
					out.write("\t"+centroids.getDistance((Integer)keyset[i], (Integer)keyset[j]));
				}
			}
			out.write("\n\n\nCentroids:\ncluster\tcentroid==>\n");
			for (int i = 0; i < keyset.length; i++) {
				out.write(((Integer)keyset[i]).intValue()+"\t"+centroids.getData((Integer)keyset[i]).toString());
			}
		}
		out.close();
	}
	public void saveDatumToFile(String outFile)throws Exception{
		ObjectOutputStream out=new ObjectOutputStream(new FileOutputStream(outFile));
		out.writeObject(datum);
		out.close();
	}
	public void loadDatumFromFile(String inFile)throws Exception{
		ObjectInputStream in=new ObjectInputStream(new FileInputStream(inFile));
		datum=(isodata_datum)in.readObject();
		in.close();
//		Integer zero=new Integer(0);
//		for (isodata_data i : datum.getDatum()) {
//			i.setCluster(zero);
//		}
	}
	private double[] calculateMaxSTD(Integer i)throws Exception{
		double[] max=new double[]{0,0};
		ArrayList curClust=(ArrayList)clusters.get(i);
		DoubleMatrix tmp=new DoubleMatrix(1,((isodata_data)curClust.get(0)).getData().getWidth());
		for (Iterator iter = curClust.iterator(); iter.hasNext();) {
			isodata_data b = (isodata_data) iter.next();
			tmp.addToThis( b.getData().subtract( (DoubleMatrix)centroids.getData(b.getCluster()) ).dotMultiplySelf() );
		}
		max=tmp.max();
		max[0]=Math.sqrt(max[0]/curClust.size());
//		System.out.println(max[0]);
		return max;
	}
	
	private void updateCentroid(Integer i)throws Exception{
		ArrayList curClust=(ArrayList)clusters.get(i);
		if(curClust.size()>0){
			DoubleMatrix newCentroid=new DoubleMatrix(1,((isodata_data)curClust.get(0)).getData().getWidth());
			for (Iterator iter = curClust.iterator(); iter.hasNext();) {
				newCentroid.addToThis(((isodata_data) iter.next()).getData());
			}
			newCentroid.divideThis(curClust.size());
			centroids.setData(i,newCentroid);
		}
	}
	private void updateClusters(){
		ArrayList toRemove;
		for (Iterator iter = clusters.keySet().iterator(); iter.hasNext();) {
			Integer i=(Integer) iter.next();
			ArrayList curClust = (ArrayList)clusters.get(i);
			toRemove=new ArrayList();
			for (Iterator iterator = curClust.iterator(); iterator.hasNext();) {
				isodata_data j = (isodata_data) iterator.next();
				if(i.intValue()!=j.getCluster().intValue()){
					((ArrayList)clusters.get( j.getCluster() )).add(j);
					toRemove.add(j);
				}
			}
			curClust.removeAll(toRemove);
		}
	}
	private void updateClusters(ArrayList toUpdate){
		for (Iterator iter = clusters.values().iterator(); iter.hasNext();) {
			ArrayList i = (ArrayList) iter.next();
			i.removeAll(toUpdate);
		}
		for (Iterator iter = toUpdate.iterator(); iter.hasNext();) {
			isodata_data i = (isodata_data) iter.next();
			((ArrayList)clusters.get(i.getCluster())).add(i);
		}
	}
}

