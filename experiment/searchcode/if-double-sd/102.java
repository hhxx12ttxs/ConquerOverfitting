/**
 * This file is part of the A5 pipeline.
 * (c) 2011, 2012 Andrew Tritt and Aaron Darling
 * This software is licensed under the GPL, v3.0. Please see the file LICENSE for details
 */
package org.halophiles.assembly.qc;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import org.halophiles.assembly.ReadPair;
import org.halophiles.assembly.ReadSet;

public class EMClusterer {
	/** 
	 * A seed for randomly sampling reads 
	 */
	private static long SEED = 1000;
	/**
	 * A random number generator for sampling reads  
	 */
	private Random rand;
	
	/**
	 * The ReadPairs to cluster
	 */
	private ReadPair[] reads;
	
	/**
	 * The probability of each ReadPair being generated by each Gaussian
	 */
	private double[][] P;
	
	/**
	 * The current assignment of each ReadPair
	 */
	private int[] C;
	
	/**
	 * The ReadSets representing underlying Gaussians 
	 */
	private ReadSet[] clusters;
	
	/**
	 * The mean of each Gaussian
	 */
	private double[] MU;
	
	/**
	 * The standard deviation of each Gaussian
	 */
	private double[] SD;
	
	/**
	 * The mean of each Gaussian at the previous iteration
	 */
	private double[] MU_last;
	
	/**
	 * The standard deviation of each Gaussian at the previous iteration
	 */
	private double[] SD_last;
	
	/**
	 * The log-likelihood of the current state.
	 */
	private double L;
	
	public EMClusterer(Collection<ReadPair> reads, int k){
		rand = new Random(SEED++);
		this.reads = new ReadPair[reads.size()];
		P = new double[reads.size()][k];
		C = new int[reads.size()];
		clusters = new ReadSet[k];
		MU = new double[k];
		SD = new double[k];
		MU_last = new double[k];
		SD_last = new double[k];
		int i;
		for (i = 0; i < clusters.length; i++)
			clusters[i] = new ReadSet(i);
		
		Iterator<ReadPair> it = reads.iterator();
		i=0;
		int c = 0;
		while(it.hasNext()){
			this.reads[i] = it.next();
			c = rand.nextInt(k);
			C[i] = c;
			clusters[c].add(this.reads[i]);
			i++;
		}
		maximize();
	}
	
	/**
	 * Run up to <code>I</code> EM iterations until the average change in 
	 * cluster means is less than <code>minDelta</code>
	 * 
	 * @param I the maximum number of iterations to run this EM algorithm for
	 * @param minDelta the minimal change in average cluster means before for calling convergence
	 * @return the number of iterations until convergence 
	 */
	public int iterate(int I, double minDelta){
		double delta = 0;
		int i = 0;
		for (i = 0; i < I; i++){
			maximize();
			expect();
			for (int j = 0; j < clusters.length; j++){
				MU[j] = clusters[j].mean();
				SD[j] = clusters[j].sd();
			}
			delta = 0.0;
			for (int j = 0; j < clusters.length; j++){
				delta += Math.abs(MU[j]-MU_last[j])/MU_last[j];
			}
			delta = delta/clusters.length;
			if (delta < minDelta) 
				break;
		}
		return i;
	}
	/**
	 * 
	 * Returns the clusters resulting from the EM clustering algorithm
	 * @return clusters of reads representing individual underlying insert size distributions
	 */
	public Collection<ReadSet> getClusters(){
		Collection<ReadSet> ret = new HashSet<ReadSet>();
		for (int i = 0; i < clusters.length; i++){
			ret.add(clusters[i]);
		}
		return ret;
	}
	
	/**
	 * Randomly assign each read pair to a knew Gaussian given the current parameter values
	 */
	private void expect(){
		double U = 0;
		int next = 0;
		L = 0.0;
		for (int i = 0; i < reads.length; i++){
			U = rand.nextDouble();
			next = Arrays.binarySearch(P[i], U);
			if (next < 0)
				next = -(next + 1);
			if (C[i] != next){
				clusters[C[i]].remove(reads[i]);
				clusters[next].add(reads[i]);
				C[i] = next;
			}
			/*
			 * Tally our current log-likelihood
			 */
			if (next == 0)
				L += Math.log(P[i][0]);
			else 
				L += Math.log(P[i][next]-P[i][next-1]);
		}
	}
	
	/**
	 * Compute the parameters of our model
	 */
	private void maximize(){
		/*
		 * Compute the maximum likelihood values of each Gaussian
		 */
		for (int i = 0; i < clusters.length; i++){
			MU_last[i] = clusters[i].mean();
			SD_last[i] = clusters[i].sd();
		}
		
		/*
		 * Compute the probability of each read pair being generated by each Gaussian
		 */
		double total;
		L = 0.0;
		double p;
		for (int i = 0; i < reads.length; i++){
			total = 0.0;
			for (int j = 0; j < clusters.length; j++){
				p = p(clusters[j].mean(),clusters[j].sd(),reads[i].getInsert());
				L += Math.log(p);
				total += p;
				P[i][j] = total;	
			}
			for (int j = 0; j < clusters.length; j++)
				P[i][j] = P[i][j]/total;
			
		}
	}
	
	public double likelihood(){
		return L;
	}
	
	private static double p(double mu, double sd, double x){
		double den = 2*sd*sd;
		return Math.exp(-Math.pow(mu-x,2)/den)/Math.sqrt(Math.PI*den);
	}
	
	
}

