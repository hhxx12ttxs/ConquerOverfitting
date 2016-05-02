/**************************************************************************
 * 
 *  JSUBST is a program to calculate environment-specific substitution
 *  tables from annotated protein sequence alignments. Alignments may
 *  be between at least one protein of known structure and any number
 *  of protein sequences.
 *  
 *  Copyright 2008,2009,2010,2011  Sebastian Kelm (kelm@stats.ox.ac.uk)
 *  
 *  This file is part of JSUBST.
 *  
 *  JSUBST is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 3
 *  of the License, or (at your option) any later version.
 *  
 *  JSUBST is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with JSUBST. If not, see <http://www.gnu.org/licenses/>.
 *  
/**************************************************************************/
package sk.subst.base;


import java.io.Serializable;
import java.util.*;

import sk.common.struc.BinaryCluster;
import sk.common.struc.NumMatrix;


public class JSubst implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public enum MODE {CONSTRAINED, STRUC, STRUC_TO_SEQ, ALL};
	
	
	// Options
	private double clustCutoff = 60;
	private double minPID = 0;
	private double maxPID = 100;
	private boolean rawCount = false; // disable clustering
	private MODE countingMode = MODE.STRUC_TO_SEQ;
	private BinaryCluster.MODE clusteringMode = BinaryCluster.MODE.SINGLE;
			//
			// -1: count only between pairs of structures, and only amino acid pairs sharing identical environments
			//  0: count only between pairs of structures
			//  1: count between structures and anything else
			//  2: count between all pairs of proteins, even those without structural info
			///////
	private boolean debug = false; // print debug information
	
	// Data Storage
	private int numStructures;
	private transient Protein[] prots;
	private transient Protein[][] clusters;
	private transient int[] clusterAnnotations;
	private transient double[][] pairwiseIdentity;
	private Env[] envirs;
	private Env globalEnv = new Env("(global)");
	private double[] bgProbab;
	
	// State indicators
	private boolean hasCounts, hasProbabs, hasLogOdds;
	
	// Helper class
	public class AaProb
	{
		public char aa;
		public double prob;
	}
	
	
	/**
	 * Empty constructor. Remember to setEnvirs() and setProts() before use.
	 */
	public JSubst()
	{
	}
	
	/**
	 * Sets the environments. Remember to setProts() before use.
	 * @param envirs the environments to count substitutions in
	 */
	public JSubst(Env[] envirs)
	{
		setEnvirs(envirs);
	}
	
	/**
	 * Sets the environments and proteins for counting. This object will be ready for counting.
	 * @param envirs the environments to count substitutions in
	 * @param prots the proteins to count substitutions in
	 */
	public JSubst(Env[] envirs, Protein[] prots)
	{
		setEnvirs(envirs);
		setProts(prots);
	}
	

	// ////////////////////////////////////////////////////////////////

	
	public void setDebug(boolean debug)
	{
		this.debug = debug;
	}

	public boolean isDebug()
	{
		return debug;
	}
	
	
	public void setClusteringMode(BinaryCluster.MODE clusteringMode)
	{
	  this.clusteringMode = clusteringMode;
	}
	public BinaryCluster.MODE getClusteringMode()
	{
	  return clusteringMode;
	}
	
	
	public void setClustCutoff(double clustCutoff)
	{
		if (clustCutoff < 0 || clustCutoff > 100)
			throw new IllegalArgumentException("Clustering PID cut-off must be a number from 0 to 100.");
		this.clustCutoff = clustCutoff;
	}

	public double getClustCutoff()
	{
		return clustCutoff;
	}
	
	public void setRawCount(boolean rawCount)
	{
		this.rawCount = rawCount;
		if (debug)
			System.err.println("DEBUG: Setting rawCount to "+rawCount);
	}

	public boolean isRawCount()
	{
		return rawCount;
	}

	public void setCountingMode(MODE countingMode)
	{
		this.countingMode = countingMode;
		if (debug)
			System.err.println("DEBUG: Setting countingMode to "+countingMode);
	}
	public MODE getCountingMode()
	{
		return countingMode;
	}
	
	public void setMinPID(double minPID)
	{
		this.minPID = minPID;
	}
	public double getMinPID()
	{
		return minPID;
	}
	
	public void setMaxPID(double maxPID)
	{
		this.maxPID = maxPID;
	}
	public double getMaxPID()
	{
		return maxPID;
	}
	
	
	public void setEnvirs(Env[] envirs)
	{
		this.envirs=envirs;
	}
	public void setProts(Protein[] prots)
	{
		if (prots != this.prots)
		{
			hasProbabs = false;
			hasLogOdds = false;
			
			this.prots = prots;
			
			numStructures=0;
			if (null != prots)
			{
				for (Protein p : prots)
					if (p.hasStrucInfo())
						numStructures++;
				if (numStructures<1)
					throw new IllegalArgumentException("Set of protein arguments should contain at least one structure!");
			}
			
		}
	}
	
	public Protein[] getProts()
	{
		return prots;
	}
	public Env getGlobalEnvir()
	{
		return globalEnv;
	}

	public Env[] getEnvirs()
	{
		return envirs;
	}

	
//	public void calculateCountsRaw()
//	{
//		int numClusters = cluster();
//		
//		if (1 == numClusters)
//		{
//			// All proteins are in a single cluster.
//			// This means we should not be doing any comparisons at all...
//			//
//			System.err.println("NOTICE: One single cluster (" + prots.length
//			+ " total sequences) at clustering cut-off "+clustCutoff+". Not counting substitutions.");
//		}
//		else if (prots.length == numClusters)
//		{
//			// The number of clusters is equal to the number of proteins. Clusters are useless, so don't use them.
//			//
//			countSubst(prots, prots, 1.0);
//		}
//		else
//		{
//			// We have more than one cluster, and the number of clusters is not equal to the number of proteins.
//			//
//			// Protein clustering has meaning, so we compare each cluster to
//			// every other cluster
//			//
//			for (int c1 = 1; c1 < clusters.length; c1++)
//			{
//				for (int c2 = 0; c2 < c1; c2++)
//				{
//					if (0==clusterAnnotations[c1] && 0==clusterAnnotations[c2])
//						continue;
//					
//					if (clusterAnnotations[c1] > 0)
//					{
//						countSubst(clusters[c1], clusters[c2], 1.0);
//					}
//					
//					if (clusterAnnotations[c2] > 0)
//					{
//						countSubst(clusters[c2], clusters[c1], 1.0);
//					}
//				}
//			}
//		}
//		
//		
//		calculateBgProbab();
//		
//		
//		hasCounts = true;
//		hasProbabs = false;
//		hasLogOdds = false;
//	}
	
	/**
	 * Calculate substitution counts, according to the set options.
	 * Methods influencing the behaviour of this function are
	 * setCountSeq2Seq(), setRawCount()
	 */
	public int calculateCounts()
	{
		// Calculate protein similarity matrix
		//
		pairwiseIdentity = new double[prots.length][prots.length];
		pairwiseIdentity[0][0] = 100.0;
		for (int i = 1; i < prots.length; i++)
		{
			for (int j = 0; j < i; j++)
			{
				pairwiseIdentity[j][i] = pairwiseIdentity[i][j] = prots[i].getPID(prots[j]);
			}
			pairwiseIdentity[i][i] = 100.0;
		}
		
		// Cluster proteins, unless we are doing "raw" counts only.
		//
		int numClusters = rawCount ? prots.length : cluster();
		
		int grandTotal = 0; // Total unweighted counts
		
		if (1 == numClusters)
		{
			// All proteins are in a single cluster.
			// This means we should not be doing any comparisons at all...
			//
			if (debug)
				System.err.println("NOTICE: One single cluster (" + prots.length
						+ " total sequences) at clustering cut-off "+clustCutoff+". Not counting substitutions.");
			
			// DEBUG: In the original SUBST, weight = 1 / (number of comparisons made).
		}
		else if (prots.length == numClusters)
		{
			// The number of clusters is equal to the number of proteins. Clusters are useless, so don't use them.
			//
			// NOTE: This is also true when we count "raw" counts only
			//
			double weight = 1.0 / numStructures;

			countComposition(prots, 1.0 / numStructures);
			
			switch (countingMode)
			{
				case STRUC_TO_SEQ:
					grandTotal += countSubst1DStrucCluster2All(prots, prots, weight);
					break;
				case STRUC:
				case CONSTRAINED:
					grandTotal += countSubst2DStruc2Struc(prots, prots, weight);
					break;
				case ALL:
					grandTotal += countSubst2DAll2All(prots, prots, weight);
					break;
				default:
					throw new RuntimeException("Unhandled enum value: "+countingMode);
			}
		}
		else
		{
			for (int c = 0; c < clusters.length; c++)
			{
				countComposition(clusters[c], 1.0 / (numStructures * clusters[c].length));
			}
			
			// We have more than one cluster, and the number of clusters is not equal to the number of proteins.
			//
			// Protein clustering has meaning, so we compare each cluster to
			// every other cluster and use a weighting scheme
			//
			for (int c1 = 1; c1 < clusters.length; c1++)
			{
				for (int c2 = 0; c2 < c1; c2++)
				{
					switch (countingMode)
					{
						case ALL:
						{
							// We should count all substitutions, even between sequences without structural information.
							// These counts are then added to the structural environments of any proteins in 'prots',
							// which have associated structural information.
							//
							
							double weight = 1.0 / (numStructures * clusters[c1].length * clusters[c2].length);
							
							int tot = countSubst2DAll2All(clusters[c1], clusters[c2], weight);
							grandTotal += tot;
								
							if (debug)
								System.err.println("DEBUG: Bi-directionally comparing clusters "+c1+"("+clusters[c1].length+" seqs) and "+c2+"("+clusters[c2].length+" seqs) with weight "+weight+" = "+tot+" * weight = "+(tot*weight));
							
							break;
						}
						case STRUC_TO_SEQ:
						{
							// We should count only substitutions between structures and anything else,
							// not between sequences without structural information. Counts are added to
							// the structural environments of the structure in question.
							//
							
							if (clusterAnnotations[c1] > 0)
							{
								// The weight for the counts is the number of comparisons made between the two clusters in this direction.
								//
								double weight = 1.0 / (clusterAnnotations[c1] * clusters[c1].length * clusters[c2].length);
								
								int tot = countSubst1DStrucCluster2All(clusters[c1], clusters[c2], weight);
								grandTotal += tot;
								
								if (debug)
									System.err.println("DEBUG: Comparing cluster "+c1+"("+clusters[c1].length+" seqs) to "+c2+"("+clusters[c2].length+" seqs) with weight 1/"+weight+" = "+tot+" * weight = "+(tot*weight));
							}
							
							if (clusterAnnotations[c2] > 0)
							{
								// The weight for the counts is the number of comparisons made between the two clusters in this direction.
								//
								double weight = 1.0 / (clusterAnnotations[c2] * clusters[c1].length * clusters[c2].length);
								
								int tot = countSubst1DStrucCluster2All(clusters[c2], clusters[c1], weight);
								grandTotal += tot;
								
								if (debug)
									System.err.println("DEBUG: Comparing cluster "+c2+"("+clusters[c2].length+" seqs) to "+c1+"("+clusters[c1].length+" seqs) with weight 1/"+weight+" = "+tot+" * weight = "+(tot*weight));
							}
							
							break;
						}
						case STRUC:
						case CONSTRAINED:
						{
							// We should count substitutions between pairs of structures, ignoring any unannotated sequences
							double weight = 1.0 / (clusterAnnotations[c1] * clusterAnnotations[c2]);
							
							int tot = countSubst2DStruc2Struc(clusters[c1], clusters[c2], weight);
							grandTotal += tot;
								
							if (debug)
								System.err.println("DEBUG: Bi-directionally comparing structure clusters "+c1+"("+clusters[c1].length+" seqs) and "+c2+"("+clusters[c2].length+" seqs) with weight 1/"+weight+" = "+tot+" * weight = "+(tot*weight));
							
							break;
						}
						default:
						{
							throw new RuntimeException("Unhandled enum value: "+countingMode);
						}
					}
				}
			}
		}
		
		
		hasCounts = true;
		hasProbabs = false;
		hasLogOdds = false;
		
		return grandTotal;
	}
	
	/**
	 * Manually add a value to all counts. Each structural environments
	 * receives a bonus of value / (number of environments), such that
	 * the total bonus to each count in the global environment is equal
	 * to 'value'.
	 * 
	 * @param value Value to be added to each count in the global environment
	 */
	public void addToCounts(double value)
	{
		double fraction = value/envirs.length;
		for(Env env : envirs)
		{
			env.getCounts().incrementAll(fraction);
		}
		globalEnv.getCounts().incrementAll(value);
	}
	
	public void makeSymmetric()
	{
		if (hasProbabs)
			throw new RuntimeException("Probabilities already calculated. Symmetrising matrix at this point may not have expected effect.");
		for(Env env : envirs)
		{
			((SubstMatrix)env.getCounts()).makeSymmetric();
		}
		((SubstMatrix)globalEnv.getCounts()).makeSymmetric();
	}
	
	/**
	 * Calculate probability matrices.
	 */
	public void calculateProbab()
	{
		if (hasProbabs)
			return;
		if (!hasCounts)
			calculateCounts();

		for (Env env : envirs)
		{
			env.calculateProbab();
		}
		globalEnv.calculateProbab();

		hasProbabs = true;
		hasLogOdds = false;
	}

	/**
	 * Calculate log-odds matrices.
	 */
	public void calculateLogOdds()
	{
		if (hasLogOdds)
			return;
		if (!hasProbabs)
			calculateProbab();
		if (null == bgProbab)
			calculateBgProbab();
		
		for (Env env : envirs)
		{
			env.calculateLogOdd(bgProbab);
		}
		globalEnv.calculateLogOdd(bgProbab);

		hasLogOdds = true;
	}


	// ////////////////////////////////////////////////////////////////
	
//	/**
//	 * Internal optimisation function. Returns only the proteins with
//	 * associated structural information.
//	 * 
//	 * @return
//	 */
//	private Protein[] getStructuresOnly()
//	{
//		// OPTIMISATION:
//		// Crop protsEnv to all proteins, which have structural info
//		List<Protein> structures = new ArrayList<Protein>();
//		for (Protein p : prots)
//		{
//			if (p.hasStrucInfo())
//				structures.add(p);
//		}
//		return structures.toArray(new Protein[structures.size()]);
//	}
	
	
	/**
	 * Internal utility function. Retrieves a structural environment based on
	 * its corresponding character array.
	 * 
	 * @param value
	 * @return
	 */
	private Env getEnv(char[] value)
	{
		for (int i = 0; i < envirs.length; i++)
		{
			if (envirs[i].hasValue(value))
				return envirs[i];
		}
		return null;
	}
	
	
	/**
	 * Count of amino acid composition in each environment. Counts are added to
	 * all structural environments available in protsFrom.
	 * 
	 * @param prots protein cluster
	 * @param weight the weight by which counts should be multiplied
	 * @return the raw total number of substitutions counted
	 */
	private int countComposition(Protein[] prots, double weight)
	{
		int total=0;
		int envLength = envirs[0].length();
		int aliLength = prots[0].getSequence().length();
		char[] envValBuffer = new char[envLength];
		
		
		for (int ip=0; ip<prots.length; ip++)
		{
			Protein p = prots[ip];
			
			for (int aaPos = 0; aaPos < aliLength; aaPos++)
			{
				// Amino acid aa1 is substituted with amino acid aa2 in
				// environment env
				//
				
				// Get amino acid in protein p
				int aaP = p.getSequenceIntAt(aaPos);
				// Skip gaps
				if (0 > aaP)
					continue;
				
				// Skip masked position
				if (p.isMaskedAt(aaPos))
					continue;
				
				for (Protein envProt : this.prots)
				{
					if (!envProt.hasStrucInfo() || !envProt.getEnvCharsAt(aaPos, envValBuffer))
						continue;
					
					// Get structural environment
					Env env = getEnv(envValBuffer);
					
					env.incrementCompositionCount(aaP, weight);
					globalEnv.incrementCompositionCount(aaP, weight);
					total++;
				}
			}
		}
		return total;
	}
	
	
	/**
	 * Bidirectional count of substitutions in all structures protsP vs protsQ.
	 * Counts are added to the structural environments of the respective proteins.
	 * Any proteins without structural information are ignored.
	 * 
	 * This method handles the case where protsP==protsQ. Even in this case, every
	 * protein is compared to every other protein only once.
	 * 
	 * @param protsP protein cluster 1
	 * @param protsQ protein cluster 2
	 * @param weight weight by which substitution counts should be multiplied
	 * @return the raw total number of substitutions counted
	 */
	private int countSubst2DStruc2Struc(Protein[] protsP, Protein[] protsQ,
			double weight)
	{
		boolean constrained = (countingMode == MODE.CONSTRAINED);
		
		int total=0;
		int envLength = envirs[0].length();
		int aliLength = prots[0].getSequence().length();
		char[] envValBufferP = new char[envLength];
		char[] envValBufferQ = new char[envLength];
		
		for (int ip=0; ip<protsP.length; ip++)
		{
			Protein p = protsP[ip];
			
			// Skip unannotated sequences
			if (!p.hasStrucInfo())
				continue;
		
			for (int aaPos = 0; aaPos < aliLength; aaPos++)
			{
				// Amino acid aa1 is substituted with amino acid aa2 in
				// environment env
				//
				
				// Get amino acid in protein p
				int aaP = p.getSequenceIntAt(aaPos);
				
				// Skip gaps
				// Get environment string and skip amino acids with missing environment
				if (0 > aaP || !p.getEnvCharsAt(aaPos, envValBufferP))
					continue;
				
				// Skip masked position
				if (p.isMaskedAt(aaPos))
					continue;
				
				// Get environment object
				Env envP = getEnv(envValBufferP);
				
				
				int iq=0;
				// If we are comparing protsP to itself, skip comparisons already made.
				if (protsP==protsQ)
					iq=ip+1;
				
				for (; iq<protsQ.length; iq++)
				{
					Protein q = protsQ[iq];

					// Skip masked position
					if (q.isMaskedAt(aaPos))
						continue;
					
					// Skip comparisons of proteins to themselves
					// Skip unannotated sequences
					if (p == q || !q.hasStrucInfo())
						continue;
					
					// Skip pairs of proteins not within the specified range of sequence identity
					if ((pairwiseIdentity[ip][iq] < minPID) || (pairwiseIdentity[ip][iq] > maxPID))
						continue;
					
					// Get amino acid in protein q
					int aaQ = q.getSequenceIntAt(aaPos);

					// Skip gaps
					// Get environment string and skip amino acids with missing environment
					if (0 > aaQ || !q.getEnvCharsAt(aaPos, envValBufferQ))
						continue;
					
					if (!constrained || Arrays.equals(envValBufferP, envValBufferQ))
					{
						// Get environment object
						Env envQ = getEnv(envValBufferQ);
						
						envP.incrementCount(aaP, aaQ, weight);
						envQ.incrementCount(aaQ, aaP, weight);
						globalEnv.incrementCount(aaP, aaQ, weight);
						globalEnv.incrementCount(aaQ, aaP, weight);
						total+=2;
					}
				}
			}
		}
		return total;
	}
	
	
	/**
	 * Unidirectional count of substitutions from all proteins in protsFrom,
	 * to all proteins in protsTo. Counts are added to all structural environments
	 * available in protsFrom.
	 * 
	 * @param protsFrom protein cluster 1, containing at least one structure
	 * @param protsTo protein cluster 2
	 * @param weight the weight by which substitution counts should be multiplied
	 * @return the raw total number of substitutions counted
	 */
	private int countSubst1DStrucCluster2All(Protein[] protsFrom, Protein[] protsTo,
			double weight)
	{
		int total=0;
		int envLength = envirs[0].length();
		int aliLength = prots[0].getSequence().length();
		char[] envValBuffer = new char[envLength];
		
		
		for (int ip=0; ip<protsFrom.length; ip++)
		{
			Protein p = protsFrom[ip];
			
			for (int aaPos = 0; aaPos < aliLength; aaPos++)
			{
				// Amino acid aa1 is substituted with amino acid aa2 in
				// environment env
				//
				
				// Get amino acid in protein p
				int aaP = p.getSequenceIntAt(aaPos);
				// Skip gaps
				if (0 > aaP)
					continue;
				
				// Skip masked position
				if (p.isMaskedAt(aaPos))
					continue;
				
				for (Protein envProt : protsFrom)
				{
					if (!envProt.hasStrucInfo() || !envProt.getEnvCharsAt(aaPos, envValBuffer))
						continue;
					
					// Get structural environment
					Env env = getEnv(envValBuffer);
					
					int iq=0;
					// If we are comparing protsFrom to itself, skip comparisons already made.
					if (protsFrom==protsTo)
						iq=ip+1;
					
					for (; iq<protsTo.length; iq++)
					{
						Protein q = protsTo[iq];
						
						// Skip masked position
						if (q.isMaskedAt(aaPos))
							continue;
						
						// Skip comparisons of proteins to themselves
						if (p == q)
							continue;
						
						// Skip pairs of proteins not within the specified range of sequence identity
						if ((pairwiseIdentity[ip][iq] < minPID) || (pairwiseIdentity[ip][iq] > maxPID))
							continue;
						
						// Get amino acid in protein q
						int aaQ = q.getSequenceIntAt(aaPos);
						// Skip gaps
						if (0 > aaQ)
							continue;
						
						env.incrementCount(aaP, aaQ, weight);
						globalEnv.incrementCount(aaP, aaQ, weight);
						total++;
					}
				}
			}
		}
		return total;
	}
	
	
	
	
	
	/**
	 * Bidirectional count of substitutions in all sequences protsP vs protsQ.
	 * Counts are added to the structural environments of all proteins from
	 * the global protein list, which have structural information.
	 * 
	 * This method handles the case where protsP==protsQ. Even in this case, every
	 * protein is compared to every other protein only once.
	 * 
	 * @param protsP protein cluster 1
	 * @param protsQ protein cluster 2
	 * @param weight weight by which substitution counts should be multiplied
	 * @return the raw total number of substitutions counted
	 */
	private int countSubst2DAll2All(Protein[] protsP, Protein[] protsQ,
			double weight)
	{
		// Divide weight by the number of structures, as we otherwise count the same substitution multiple times.
		if (numStructures > 1)
			weight /= numStructures;
		
		int total=0;
		int envLength = envirs[0].length();
		int aliLength = prots[0].getSequence().length();
		char[] envValBuffer = new char[envLength];
		
		for (int ip=0; ip<protsP.length; ip++)
		{
			Protein p = protsP[ip];
			
			for (int aaPos = 0; aaPos < aliLength; aaPos++)
			{
				// Amino acid aa1 is substituted with amino acid aa2 in
				// environment env
				//
				
				// Get amino acid in protein p
				int aaP = p.getSequenceIntAt(aaPos);
				// Skip gaps
				if (0 > aaP)
					continue;
				
				// Skip masked position
				if (p.isMaskedAt(aaPos))
					continue;
				
				for (Protein envProt : prots)
				{
					if (!envProt.hasStrucInfo() || !envProt.getEnvCharsAt(aaPos, envValBuffer))
						continue;
					
					// Get structural environment
					Env env = getEnv(envValBuffer);
					
					int iq=0;
					// If we are comparing protsP to itself, skip comparisons already made.
					if (protsP==protsQ)
						iq=ip+1;
					
					for (; iq<protsQ.length; iq++)
					{
						Protein q = protsQ[iq];
						
						// Skip masked position
						if (q.isMaskedAt(aaPos))
							continue;
						
						// Skip comparisons of proteins to themselves
						if (p == q)
							continue;
						
						// Skip pairs of proteins not within the specified range of sequence identity
						if ((pairwiseIdentity[ip][iq] < minPID) || (pairwiseIdentity[ip][iq] > maxPID))
							continue;
						
						// Get amino acid in protein q
						int aaQ = q.getSequenceIntAt(aaPos);
						// Skip gaps
						if (0 > aaQ)
							continue;
						
						env.incrementCount(aaP, aaQ, weight);
						env.incrementCount(aaQ, aaP, weight);
						globalEnv.incrementCount(aaP, aaQ, weight);
						globalEnv.incrementCount(aaQ, aaP, weight);
						total+=2;
					}
				}
			}
		}
		return total;
	}
	

	/**
	 * Performs clustering on the proteins. Results will be saved in
	 * this.clusters, if the number of clusters is not 1 or equal to the number
	 * of proteins.
	 * 
	 * @return true if clusters were saved (if it was meaningful to save them),
	 *         false otherwise
	 */
	private int cluster()
	{
		// Create a similarity matrix for all Proteins
		//
		double[][] similMatrix = new double[prots.length][prots.length];
		for (int i = 0; i < prots.length; i++)
		{
			for (int j = 0; j < prots.length; j++)
			{
				similMatrix[i][j] = pairwiseIdentity[i][j];
			}
		}
		
		// Perform clustering
		//
		List<BinaryCluster<Protein>> clusterList = BinaryCluster
				.doHierarchicalClustering(similMatrix, prots, clustCutoff, clusteringMode);

		System.err.println("#NOTICE: Found " + clusterList.size()
				+ " clusters (in " + prots.length
				+ " total sequences), given similarity cut-off " + clustCutoff);
		
		// Check if it is meaningful to use these clusters, rather than the flat
		// list of proteins
		//
		if (clusterList.size() == 1 || clusterList.size() == prots.length)
		{
//			System.err.println("#NOTICE: Clustering useless (" + prots.length
//					+ " total sequences, " + clusterList.size()
//					+ " clusters), given similarity cut-off " + clustCutoff);
			clusters = null;
			clusterAnnotations = null;
			return clusterList.size();
		}
		
		// Save clusters as a 2D array (rows may not all happen to have the same
		// length!)
		//
		List<Protein> protBuff = new ArrayList<Protein>(Math.min(10,
				prots.length / clusterList.size()));
		clusters = new Protein[clusterList.size()][];
		clusterAnnotations = new int[clusters.length];
		for (int i = 0; i < clusters.length; i++)
		{
			protBuff.clear();
			clusterList.get(i).getData(protBuff);
			clusters[i] = protBuff.toArray(new Protein[protBuff.size()]);
			int numAnnot=0;
			for(Protein p : clusters[i])
				if (p.hasStrucInfo())
					numAnnot++;
			clusterAnnotations[i] = numAnnot;
		}

//		System.err.println("PIDs within clusters:");
//		for (int i=0; i<clusters.length; i++)
//		{
//			System.err.printf("Cluster %d (%d)\n", i, clusters[i].length);
//			for (int j=0; j<clusters[i].length-1; j++)
//			{
//				for (int k=j+1; k<clusters[i].length; k++)
//				{
//					double p = clusters[i][j].getPID(clusters[i][k]);
//					int c = 0;
//					if (p<this.getClustCutoff())
//						c=-1;
//					else if (p>this.getClustCutoff())
//						c=1;
////					System.err.println(clusters[i][j].getSequence());
////					System.err.println(clusters[i][k].getSequence());
//					System.err.printf("  %d ~ %d = %f  (%d)\n\n", j, k, p, c);
//				}
//			}
//		}
		
		return clusters.length;
	}

	private void calculateBgProbab()
	{
		int size = Env.getAAcount();
		bgProbab = new double[size];

		for (int b = 0; b < size; b++)
		{
			// Let sumAB be the sum of counts of all amino acids A to a specific
			// amino acid B in all environments
			//
			double sumAB = 0;
			// for(Env env : envirs)
			// {
			// NumMatrix counts = env.getCounts();
			// for(int a=0; a<size; a++)
			// {
			// sumAB += counts.getDouble(a, b);
			// }
			// }
			NumMatrix counts = globalEnv.getCounts();
			for (int a = 0; a < size; a++)
				sumAB += counts.getDouble(a, b);
			bgProbab[b] = sumAB;
		}
		
		double sumABE = 0;
		for (int a = 0; a < size; a++)
			sumABE += bgProbab[a];

		if (sumABE > 0)
			for (int a = 0; a < size; a++)
				bgProbab[a] /= sumABE;
	}

	public AaProb[] getSortedBgProbabs()
	{
		if (null == bgProbab)
			calculateBgProbab();
		
		char[] aas = Env.getAAs(); // this makes a copy
		AaProb[] sorted = new AaProb[aas.length];
		for (int rank = 0; rank < aas.length; rank++)
		{
			double val = -1;
			int bestAa = -1;
			for (int aa = 0; aa < aas.length; aa++)
			{
				if (bgProbab[aa] > val)
				{
					val = bgProbab[aa];
					bestAa = aa;
				}
			}
			bgProbab[bestAa] = -1;
			sorted[rank] = new AaProb();
			sorted[rank].aa = aas[bestAa];
			sorted[rank].prob = val;
		}
		return sorted;
	}
}

