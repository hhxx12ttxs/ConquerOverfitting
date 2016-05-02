package edu.ufl.qure;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;

import jaligner.Alignment;
import jaligner.Sequence;
import jaligner.SmithWatermanGotoh;
import jaligner.matrix.*;
import jaligner.util.Commons;

/**
 * Copyright (C) 2012, Mattia C.F. Prosperi <m.prosperi@epi.ufl.edu>, 
 * Marco Salemi <salemi@pathology.ufl.edu>, and Tyler Strickland <tyler@tylers.org>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 */
public class ReadSet
{
	public ArrayList<Read> population;
	public String referenceGenomeName;
	public String referenceGenome;
	public String consensusGenomeNoIndels;
	public Hashtable genomeDictionary;
	public double avgReadLength;
	public double stdReadLength;
	public double rndScoreAvg;
	public double rndScoreStd;
	public Matrix matrix;
	public HashMap<Double,Base> baseSet;
	public AmpliconSet ampliconSet;
	public ArrayList<AmpliconSet> goodAmpliconSets;
	public double alignStart;
	public double alignStop;	
	
	public ReadSet()
	throws Exception
	{
		population = new ArrayList<Read>();
		baseSet = new HashMap<Double,Base>();
		Logger l = Logger.getLogger(MatrixLoader.class.getName());
		l.setLevel(Level.OFF);
		Logger z = Logger.getLogger(Commons.class.getName());
		z.setLevel(Level.OFF);
		matrix = MatrixLoader.load("EDNAFULL");
	}
	
	public ReadSet(int capacity)
	throws Exception
	{
		population = new ArrayList<Read>(capacity);
		Logger l = Logger.getLogger(MatrixLoader.class.getName());
		l.setLevel(Level.OFF);
		Logger z = Logger.getLogger(Commons.class.getName());
		z.setLevel(Level.OFF);
		matrix = MatrixLoader.load("EDNAFULL");
	}
					
	public void updatePopulationStats() throws DocumentOperationException
	{
		int u=0;
		while(u<population.size())
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			if (population.get(u).stop<alignStart || population.get(u).start>alignStop)
				population.remove(u);
			else
				u++;
		}
		population.trimToSize();
		if (population.size()<=0)
		{
			System.out.println("zero reads have been mapped to the reference genome.");
			System.out.println("exiting program.");
			System.exit(0);
		}
		double [] readLengths = new double [population.size()];
		//double stdReadLengthNew = -1;
		for (int i=0; i<population.size(); i++)
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			Read r = this.population.get(i);
			r.mappingPosition=(int)(r.start+(r.stop-r.start)/2);
			readLengths[i]=r.insertions+r.stop-r.start;
		}
		avgReadLength=Functions.average(readLengths);
		stdReadLength=Functions.stdev(readLengths);
		System.out.println("\tpost-alignment (st.dev.) read length is "+Math.round(avgReadLength)+" ("+Math.round(stdReadLength)+")");
		System.out.println("\treference genome is covered from position "+Math.round(alignStart)+" to "+Math.round(alignStop));
	}
	
	public void removeBadReads(double alpha) throws DocumentOperationException
	{
		int i=0;
		int removed=0;
		while(i<population.size())
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			if (population.get(i).adjustedPvalue>alpha)
			{
				population.remove(i);
				removed++;
			}
			else
				i++;
		}
		population.trimToSize();
		if (population.size()<=0)
		{
			System.out.println("zero reads have been mapped to the reference genome.");
			System.out.println("exiting program.");
			System.exit(0);
		}
		System.out.println("\tremoved "+removed+" reads with alignment score p>"+alpha);
		
	}
	
	public void estimateBaseSet() throws DocumentOperationException
	{
		if(QuRe.cancel)
		    throw new DocumentOperationException("Cancelled!");
		System.out.print("reconstructing consensus genome and variations ");
		int initialCapacity = referenceGenome.length()+(int)(Math.log(referenceGenome.length()));
		HashMap<Double,Base> bs = new HashMap<Double,Base>(initialCapacity,0.99f);
		for (int i=0; i<referenceGenome.length(); i++)
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			Base b =  new Base();
			b.reference=referenceGenome.charAt(i);
			b.position=(double)(i+1);
			bs.put(b.position,b);
		}
		for (int i=0; i<population.size(); i++)
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			String perc = (Math.round(100f*((float)(i))/((float)(population.size()))))+"% ";
			System.out.print(perc);
			Read r = this.population.get(i);
			for (int j=0; j<r.SNP_list.size(); j++)
			{
				if(QuRe.cancel)
				    throw new DocumentOperationException("Cancelled!");
				SNP snp = r.SNP_list.get(j);
				if (bs.get(snp.position)!=null)
				{
					Base b = bs.get(snp.position);
					switch (snp.base)
					{
						case 'A': b.A++; break;
						case 'C': b.C++; break;
						case 'G': b.G++; break;
						case 'T': b.T++; break;
						case '-': b.del++; break;
						case 'R': b.A+=0.5d; b.G+=0.5d; break;
						case 'Y': b.C+=0.5d; b.T+=0.5d; break;
						case 'K': b.G+=0.5d; b.T+=0.5d; break;
						case 'M': b.A+=0.5d; b.C+=0.5d; break;
						case 'S': b.C+=0.5d; b.G+=0.5d; break;
						case 'W': b.A+=0.5d; b.T+=0.5d; break;
						case 'B': b.C+=0.3333d; b.G+=0.3333d; b.T+=0.3333d; break;
						case 'D': b.A+=0.3333d; b.G+=0.3333d; b.T+=0.3333d; break;
						case 'H': b.A+=0.3333d; b.C+=0.3333d; b.T+=0.3333d; break;
						case 'V': b.A+=0.3333d; b.C+=0.3333d; b.G+=0.3333d; break;
						default : b.A+=0.2d; b.C+=0.2d; b.G+=0.2d; b.T+=0.2d; b.del+=0.2d; break;
					}
				}
				else
				{
					Base b = new Base();
					b.position = snp.position;
					b.reference = '-';
					switch (snp.base)
					{
						case 'A': b.A++; break;
						case 'C': b.C++; break;
						case 'G': b.G++; break;
						case 'T': b.T++; break;
						case '-': b.del++; break;
						case 'R': b.A+=0.5d; b.G+=0.5d; break;
						case 'Y': b.C+=0.5d; b.T+=0.5d; break;
						case 'K': b.G+=0.5d; b.T+=0.5d; break;
						case 'M': b.A+=0.5d; b.C+=0.5d; break;
						case 'S': b.C+=0.5d; b.G+=0.5d; break;
						case 'W': b.A+=0.5d; b.T+=0.5d; break;
						case 'B': b.C+=0.3333d; b.G+=0.3333d; b.T+=0.3333d; break;
						case 'D': b.A+=0.3333d; b.G+=0.3333d; b.T+=0.3333d; break;
						case 'H': b.A+=0.3333d; b.C+=0.3333d; b.T+=0.3333d; break;
						case 'V': b.A+=0.3333d; b.C+=0.3333d; b.G+=0.3333d; break;
						default : b.A+=0.2d; b.C+=0.2d; b.G+=0.2d; b.T+=0.2d; b.del+=0.2d; break;
					}
					bs.put(b.position,b);
				}
			}
			for (int o=0; o<perc.length(); o++)
				System.out.print("\b");
		}
		System.out.print("100%");
		Object [] key = bs.keySet().toArray();
		for   (int i=0; i<key.length; i++)
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			Base b = bs.get(key[i]);
			for (int j=0; j<population.size(); j++)
			{
				if(QuRe.cancel)
				    throw new DocumentOperationException("Cancelled!");
				Read r = population.get(j);
				if (b.position>=r.start && b.position<=r.stop)
					b.coverage++;
			}
		}
		LinkedList<Double> bc = new LinkedList<Double>();
		for   (int i=0; i<key.length; i++)
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			if (bs.get(key[i]).coverage>0)
				bc.add(bs.get(key[i]).coverage);
		}
		Double [] base_coverage = bc.toArray(new Double [bc.size()]);
		double avgBasecoverage=Functions.average(base_coverage);
		double stdBasecoverage=Functions.stdev(base_coverage);
		System.out.println();
		System.out.println("\taverage (st.dev.) coverage of each mapped base is "+Math.round(avgBasecoverage)+" ("+Math.round(stdBasecoverage)+")");
		for (int i=0; i<key.length; i++)
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			Base b = bs.get(key[i]);
			b.setConsensus(avgBasecoverage,stdBasecoverage,0.05d);
			b.calculateEntropy();
		}
		for (int i=0; i<key.length; i++)
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			Base b = bs.get(key[i]);
			if (b.removed)
				bs.remove(key[i]);
		}
		key = bs.keySet().toArray();
		Arrays.sort(key);
		double maxAvgCoverage = 0;
		int maxIndMaxAvgCoverage = 0;
		double [] avgCoverage = new double [key.length];
		for (int i=0; i<key.length; i++)
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			double k = 0;
			for (int j=Math.max(0,i-10); j<Math.min(key.length,i+10); j++)
			{
				if(QuRe.cancel)
				    throw new DocumentOperationException("Cancelled!");
				Base b = bs.get(key[j]);
				avgCoverage[i]+=b.coverage;
				k++;
			}
			avgCoverage[i]=avgCoverage[i]/k;
			if (avgCoverage[i]>maxAvgCoverage)
			{
				maxAvgCoverage = avgCoverage[i];
				maxIndMaxAvgCoverage = i;
			}
		}
		double maxCoverageStart=bs.get(key[maxIndMaxAvgCoverage]).position;
		double maxCoverageStop=bs.get(key[maxIndMaxAvgCoverage]).position;
		for (int i=maxIndMaxAvgCoverage; i<key.length; i++)
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			Base b = bs.get(key[i]);
			if ((b.position-maxCoverageStop)<1.1d)
				maxCoverageStop=b.position;
			else
				break;
		}
		for (int i=maxIndMaxAvgCoverage; i>0; i--)
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			Base b = bs.get(key[i]);
			if ((maxCoverageStart-b.position)<1.1d)
				maxCoverageStart=b.position;
			else
				break;
		}
		/*for (int i=0; i<key.length; i++)
		{
			while(i<maxCoverageStop)
				i++;
			Base b1 = bs.get(key[i]);
			double maxConsecutiveIndex=b1.position;
			for (int j=i+1; j<key.length; j++)
			{
				Base b2 = bs.get(key[j]);
				if (b2.position-maxConsecutiveIndex<=1.1d)
					maxConsecutiveIndex=b2.position;
				else
					break;
			}
			if ((maxCoverageStop-maxCoverageStart)<(maxConsecutiveIndex-b1.position))
			{
				maxCoverageStart=b1.position;
				maxCoverageStop=maxConsecutiveIndex;
			}
		}*/
		for (int i=0; i<key.length; i++)
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			Base b = bs.get(key[i]);
			if (b.position<maxCoverageStart || b.position>maxCoverageStop)
				bs.remove(key[i]);
		}
		double minStart=alignStop;
		double maxStop=alignStart;
		key = bs.keySet().toArray();
		for (int i=0; i<key.length; i++)
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			Base b = bs.get(key[i]);
			if (b.removed)
				bs.remove(key[i]);
			else
			{
				if (b.position<minStart)
					minStart=b.position;
				if (b.position>maxStop)
					maxStop=b.position;
			}
		}
		alignStart=minStart;
		alignStop=maxStop;
		baseSet=bs;
	}
	
	public class correctReadsThread implements Runnable 
	{
		Read read;
		HashMap<Double,Base> baseSet;
		
		public correctReadsThread(Read r, HashMap<Double,Base> b)
		{
			read=r;
			baseSet=b;
		}
		public void run()
		{
			read.correct(baseSet);
		}
	}
	
	public void correctReadsParallel(double tolerance, double erNoHomopol, double erHomopol, int n_proc) throws DocumentOperationException
	{
		if(QuRe.cancel)
		    throw new DocumentOperationException("Cancelled!");
		System.out.print("\tcorrecting mapped reads, SNP and indel list ");
		consensusGenomeNoIndels="";
		for (int i=0; i<referenceGenome.length(); i++)
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			if (baseSet.get((i+1d))==null)
				consensusGenomeNoIndels+=referenceGenome.charAt(i);
			else
			{
				consensusGenomeNoIndels+=baseSet.get((i+1d)).consensus;
			}
		}
		//System.out.println(referenceGenome);
		//System.out.println(consensusGenomeNoIndels);
		Object [] key = baseSet.keySet().toArray();
		for   (int i=0; i<key.length; i++)
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			Base b = baseSet.get(key[i]);
			b.correct(tolerance,erNoHomopol,erHomopol, consensusGenomeNoIndels, (double)(key.length*5));
		}
		double minStart=alignStop;
		double maxStop=alignStart;
		for (int i=0; i<key.length; i++)
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			Base b = baseSet.get(key[i]);
			if (b.removed)
				baseSet.remove(key[i]);
			else
			{
				if (b.position<minStart)
					minStart=b.position;
				if (b.position>maxStop)
					maxStop=b.position;
			}
		}
		alignStart=minStart;
		alignStop=maxStop;
		key = baseSet.keySet().toArray();
		int i=0;
		Thread [] thread_list = new Thread [n_proc];
		while(i<population.size())
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			String perc = (Math.round(100f*((float)(i))/((float)(population.size()))))+"% ";
			System.out.print(perc);
			if (i==0)
				for (int j=0; j<n_proc; j++)
				{
					Read r = population.get(i);
					Runnable runnable = new correctReadsThread(r, baseSet);
					thread_list[j] = new Thread(runnable);
					thread_list[j].start();
					i++;
				}
			else
			{
				int j=0;
				while(thread_list[j].isAlive())
				{
					j++;
					j=j%n_proc;
				}
				Read r = population.get(i);
				Runnable runnable = new correctReadsThread(r, baseSet);
				thread_list[j] = new Thread(runnable);
				thread_list[j].start();
				i++;
			}
			for (int o=0; o<perc.length(); o++)
				System.out.print("\b");
		}
		int j=0;
		while(j<n_proc)
		{
			if (!thread_list[j].isAlive())
				j++;
		}
		System.out.print("100%");
		System.out.println();
		key = baseSet.keySet().toArray();
		for (int k=0; k<key.length; k++)
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			Base b = baseSet.get(key[k]);
			if (b.removed)
				baseSet.remove(key[k]);
			else
				b.calculateEntropy();
		}
		minStart=alignStop;
		maxStop=alignStart;
		key = baseSet.keySet().toArray();
		for (int k=0; k<key.length; k++)
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			Base b = baseSet.get(key[k]);
			if (b.position<minStart)
				minStart=b.position;
			if (b.position>maxStop)
				maxStop=b.position;
		}
		alignStart=minStart;
		alignStop=maxStop;
	}
	
	public void setAllPvalues(String s) throws DocumentOperationException
	{
		for (int i=0; i<population.size(); i++)
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			Read r = this.population.get(i);
			r.setPvalue(rndScoreAvg,rndScoreStd);
		}
		Comparator<Read> comparatorPvalues = new Comparator<Read>()
		{
			public int compare(Read r1, Read r2)
			{
				if (r1.pvalue<r2.pvalue)
					return -1;
				if (r1.pvalue>r2.pvalue)
					return 1;
				return 0;
			}
		};
		if (s.equals("BH"))
		{
			Collections.sort(population,comparatorPvalues);
			for (int i=0; i<population.size(); i++)
			{
				Read r = this.population.get(i);
				r.adjustedPvalue=r.pvalue*((double)(population.size()+1))/((double)(i+1));
			}
		}
		else
		{
			for (int i=0; i<population.size(); i++)
			{
				Read r = this.population.get(i);
				r.adjustedPvalue=Math.min(1,r.pvalue*((double)population.size()));
			}
		}
	}
	
	public class setRandomScoresThread implements Runnable 
	{
		ArrayList<Read> population;
		double [] rndScores;
		String referenceGenome;
		double avgReadLength;
		double stdReadLength;
		float gop;
		float gep;
		Matrix matrix;
		int index;
		
		public setRandomScoresThread(ArrayList<Read> p, double [] r , String ref, double a, double s, float go, float ge, Matrix m, int i)
		{
			population=p;
			rndScores=r;
			referenceGenome=ref;
			avgReadLength=a;
			stdReadLength=s;
			gop=go;
			gep=ge;
			matrix=m;
			index=i;
		}
		public void run()
		{
			Logger l = Logger.getLogger(SmithWatermanGotoh.class.getName());
			l.setLevel(Level.OFF);
			int rnd = (int)(Math.random()*(this.population.size()-1));
			Read r = this.population.get(rnd);
			String rndQuery = Functions.shuffle(r.sequence);
			int rndMid = (int)(Math.random()*(referenceGenome.length()-1));
			int rndStart = Math.max(rndMid-(int)(avgReadLength+2*stdReadLength),0);
			int rndStop = Math.min(rndMid+(int)(avgReadLength+2*stdReadLength),referenceGenome.length());
			String rndRefer = referenceGenome.substring(rndStart,rndStop);
			Sequence rndQuerySeq = new Sequence(rndQuery,"","",0);
			Sequence rndReferSeq = new Sequence(rndRefer,"","",0);
			Alignment rndAlignment = new Alignment();
			rndAlignment = SmithWatermanGotoh.align(rndQuerySeq, rndReferSeq, matrix, gop, gep);
			rndScores[index]=(double)rndAlignment.getScore();
		}
	}
	
	public void setRandomScoresParallel(int sampleSize, float gop, float gep, int n_proc) throws DocumentOperationException
	{
		if(QuRe.cancel)
		    throw new DocumentOperationException("Cancelled!");
		Logger l = Logger.getLogger(SmithWatermanGotoh.class.getName());
		l.setLevel(Level.OFF);
		double [] rndScores = new double[sampleSize];
		System.out.print("\tcalculating quasi-random alignment score distribution ");
		int i=0;
		Thread [] thread_list = new Thread [n_proc];
		while(i<sampleSize)
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			String perc = (Math.round(100f*((float)(i))/((float)(sampleSize))))+"% ";
			System.out.print(perc);
			if (i==0)
				for (int j=0; j<n_proc; j++)
				{
					Runnable runnable = new setRandomScoresThread(population, rndScores, referenceGenome, avgReadLength, stdReadLength, gop, gep, matrix, i);
					thread_list[j] = new Thread(runnable);
					thread_list[j].start();
					i++;
				}
			else
			{
				int j=0;
				while(thread_list[j].isAlive())
				{
					j++;
					if (Math.random()<0.001d)
						j=(int)(Math.random()*n_proc);
					j=j%n_proc;
				}
				Runnable runnable = new setRandomScoresThread(population, rndScores, referenceGenome, avgReadLength, stdReadLength, gop, gep, matrix, i);
				thread_list[j] = new Thread(runnable);
				thread_list[j].start();
				i++;
			}
			for (int o=0; o<perc.length(); o++)
				System.out.print("\b");
		}
		int j=0;
		while(j<n_proc)
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			if (!thread_list[j].isAlive())
				j++;
		}
		System.out.print("100%");
		rndScoreAvg=Functions.average(rndScores);
		rndScoreStd=Functions.stdev(rndScores);
		System.out.println("\r\n\taverage (st.dev) quasi-random score is "+Math.round(rndScoreAvg)+" ("+Math.round(rndScoreStd)+")");
	}
	
	public void setRandomScores(int sampleSize, float gop, float gep) throws DocumentOperationException
	{
		Logger l = Logger.getLogger(SmithWatermanGotoh.class.getName());
		l.setLevel(Level.OFF);
		double [] rndScores = new double [sampleSize];
		System.out.print("\tcalculating quasi-random alignment score distribution ");
		for (int i=0; i<sampleSize; i++)
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			String perc = (Math.round(100f*((float)(i))/((float)(sampleSize))))+"% ";
			System.out.print(perc);
			int rnd = (int)(Math.random()*(this.population.size()-1));
			Read r = this.population.get(rnd);
			String rndQuery = Functions.shuffle(r.sequence);
			int rndMid = (int)(Math.random()*(referenceGenome.length()-1));
			int rndStart = Math.max(rndMid-(int)(avgReadLength+2*stdReadLength),0);
			int rndStop = Math.min(rndMid+(int)(avgReadLength+2*stdReadLength),referenceGenome.length());
			String rndRefer = referenceGenome.substring(rndStart,rndStop);
			Sequence rndQuerySeq = new Sequence(rndQuery,"","",0);
			Sequence rndReferSeq = new Sequence(rndRefer,"","",0);
			Alignment rndAlignment = new Alignment();
			rndAlignment = SmithWatermanGotoh.align(rndQuerySeq, rndReferSeq, matrix, gop, gep);
			rndScores[i]=rndAlignment.getScore();
			for (int o=0; o<perc.length(); o++)
				System.out.print("\b");
		}
		System.out.print("100%");
		rndScoreAvg=Functions.average(rndScores);
		rndScoreStd=Functions.stdev(rndScores);
		System.out.println("\r\n\taverage (st.dev) quasi-random score is "+Math.round(rndScoreAvg)+" ("+Math.round(rndScoreStd)+")");
	}
	
	public void readFastaFromList(List <NucleotideSequenceDocument>seqList) throws DocumentOperationException
	{
		population = new ArrayList<Read>();
		System.out.print("parsing sequences");
		for (NucleotideSequenceDocument seq: seqList)
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			Read r = new Read();
			r.name = seq.getName();
			r.sequence = seq.getSequenceString();
			population.add(r);
		}

		population.trimToSize();
		double [] rl = new double [population.size()];
		for (int i=0; i<population.size(); i++)
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			population.get(i).sequence = population.get(i).sequence.toUpperCase();
			population.get(i).sequence = population.get(i).sequence.replaceAll("U","T");
			population.get(i).sequence = population.get(i).sequence.replaceAll("[^ACGTRYKMSWBDHVN]+","");
			rl[i]=population.get(i).sequence.length();
		}
		double avg = Functions.average(rl);
		double std = Functions.stdev(rl);
		avgReadLength=avg;
		stdReadLength=std;
		System.out.println("\taverage (st.dev.) read length is "+Math.round(avg)+" ("+Math.round(std)+")");
	}
	
	public void readFasta(String fileName)
	throws Exception
	{
		if(QuRe.cancel)
		    throw new DocumentOperationException("Cancelled!");
		population = new ArrayList<Read>();
		System.out.print("parsing \""+fileName+"\" read file ");
		FileReader fr = new FileReader(fileName);
		File f = new File(fileName);
		long scanSize=0;
		BufferedReader br = new BufferedReader(fr);
		String s1="";
		Read r = new Read();
		int count=0;
		String s = "";
		while(true)
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			s = br.readLine();
			if (s==null)
			{
				r.sequence=s1;
				population.add(r);
				System.out.print("100%");
				break;
			}
			byte [] b = s.getBytes();
			scanSize+=b.length;
			String perc = (Math.round(100f*((float)(scanSize))/((float)(f.length()))))+"% ";
			System.out.print(perc);
			if (s.startsWith(">"))
			{
				if (count>0)
				{
					r.sequence=s1;
					population.add(r);
					s1="";
				}
				r = new Read();
				r.name=s.substring(1);
				r.idx=count;
				count++;
			}
			else
				s1+=s;
			for (int o=0; o<perc.length(); o++)
				System.out.print("\b");
		}
		fr.close();
		population.trimToSize();
		double [] rl = new double [population.size()];
		for (int i=0; i<population.size(); i++)
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			population.get(i).sequence = population.get(i).sequence.toUpperCase();
			population.get(i).sequence = population.get(i).sequence.replaceAll("U","T");
			population.get(i).sequence = population.get(i).sequence.replaceAll("[^ACGTRYKMSWBDHVN]+","");
			rl[i]=population.get(i).sequence.length();
		}
		double avg = Functions.average(rl);
		double std = Functions.stdev(rl);
		avgReadLength=avg;
		stdReadLength=std;
		System.out.println("\r\n\t"+count+" reads");
		System.out.println("\taverage (st.dev.) read length is "+Math.round(avg)+" ("+Math.round(std)+")");
	}
	
	public void readReferenceGenome(String file, int kappa)
	throws Exception
	{
		FileReader fr = new FileReader(file);
		File f = new File(file);
		BufferedReader read = new BufferedReader(fr);
		System.out.print("parsing \""+file+"\" reference genome file ");
		String rgn = "";
		String rg = "";
		long scanSize=0;
		while(true)
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			String s = read.readLine();
			if (s==null)
			{
				System.out.print("100%");
				break;
			}
			String perc = (Math.round(100f*((float)(scanSize))/((float)(f.length()))))+"% ";
			System.out.print(perc);
			byte [] b = s.getBytes();
			scanSize+=b.length;
			if (!s.startsWith(">"))
				rg+=s;
			else
				rgn=s;
			for (int o=0; o<perc.length(); o++)
				System.out.print("\b");
		}
		referenceGenomeName=rgn;
		referenceGenome=rg;
		referenceGenome = referenceGenome.toUpperCase();
		referenceGenome = referenceGenome.replaceAll("U","T");
		//referenceGenome = referenceGenome.replaceAll("[^ACGTRYKMSWBDHVN]+","");
		referenceGenome = referenceGenome.replaceAll("[^ACGT]+","");
		System.out.println("\r\n\t"+rgn+" read ("+referenceGenome.length()+" bases)");
		this.buildDictionary(kappa);
	}
	
	public void buildDictionary (int k) throws DocumentOperationException
	{
		Hashtable h = new Hashtable();
		int index = 0;
		System.out.print("\tbuilding dictionary ");
		while ((index+k)<referenceGenome.length())
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			String perc = (Math.round(100f*((float)(index))/((float)(referenceGenome.length()))))+"% ";
			System.out.print(perc);
			String kmer = referenceGenome.substring(index,index+k);
			if (h.get(kmer)==null)
			{
				LinkedList<Integer> list = new LinkedList<Integer>();
				list.add(index);
				h.put(kmer,list);
			}
			else
			{
				LinkedList<Integer> list = (LinkedList)(h.get(kmer));
				list.add(index);
				h.put(kmer,list);
			}
			index++;	
			for (int o=0; o<perc.length(); o++)
				System.out.print("\b");
		}
		System.out.print("100%");
		genomeDictionary=h;
		//String dictionary = h.toString();
		System.out.println();
	}
	
	public class alignThread implements Runnable 
	{
		Read read;
		Hashtable genomeDictionary;
		String refGenome;
		double avgReadLength;
		double stdReadLength;
		Matrix matrix;
		float gop;
		float gep;
		int kmer;
		public alignThread (Read r, Hashtable gd, String rg, double al, double sl, Matrix m, float o, float e, int kappa)
		{
			read = r;
			genomeDictionary = gd;
			refGenome = rg;
			avgReadLength = al;
			stdReadLength = sl;
			matrix = m;
			gop = o;
			gep = e;
			kmer=kappa;
		}
		public void run()
		{
			read.align(genomeDictionary,refGenome,avgReadLength,stdReadLength,matrix,gop,gep,kmer);
		}
	}
	
	public void alignParallel(int n_proc,float gop, float gep, int kmer)
	throws Exception
	{
		Date d1 = new Date();
		long starttime=d1.getTime();
		System.out.print("aligning reads to reference genome ");
		int i=0;
		Thread [] thread_list = new Thread [n_proc];
		while(i<population.size())
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			String perc = (Math.round(100f*((float)(i))/((float)(population.size()))))+"% ";
			System.out.print(perc);
			if (i==0)
				for (int j=0; j<n_proc; j++)
				{
					Read r = population.get(i);
					Runnable runnable = new alignThread(r, genomeDictionary, referenceGenome, avgReadLength, stdReadLength, matrix, gop, gep, kmer);
					thread_list[j] = new Thread(runnable);
					thread_list[j].start();
					i++;
				}
			else
			{
				int j=0;
				while(thread_list[j].isAlive())
				{
					j++;
					if (Math.random()<0.001d)
						j=(int)(Math.random()*n_proc);
					j=j%n_proc;
				}
				Read r = population.get(i);
				Runnable runnable = new alignThread(r, genomeDictionary, referenceGenome, avgReadLength, stdReadLength, matrix, gop, gep, kmer);
				thread_list[j] = new Thread(runnable);
				thread_list[j].start();
				i++;
			}
			for (int o=0; o<perc.length(); o++)
				System.out.print("\b");
		}
		int j=0;
		while(j<n_proc)
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			if (!thread_list[j].isAlive())
				j++;
		}
		System.out.print("100%\r\n");
		
		double gSta=-1;
		double gSto=-1;
		for (i=0; i<population.size(); i++)
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			double sta = (double)(this.population.get(i).start);
			double sto = (double)(this.population.get(i).stop);
			if (i==0)
			{
				gSta=sta;
				gSto=sto;
			}
			if (sta<gSta)
				gSta=sta;
			if (sto>gSto)
				gSto=sto;
		}
		alignStart=gSta;
		alignStop=gSto;
		
		Date d2 = new Date();
		long stoptime=d2.getTime();
		long timePassed=stoptime-starttime;
		System.out.println("\ttime employed = "+timePassed+" ms");
	}
	
	public class randomAmpliconThread implements Runnable 
	{
		double avgReadLength;
		double stdReadLength;
		double alignStart;
		double alignStop;
		ArrayList<Read> pop;
		AmpliconSet amplicon;
		public randomAmpliconThread (double al, double sl, double asta, double asto, AmpliconSet amp, ArrayList<Read> p)
		{
			avgReadLength = al;
			stdReadLength = sl;
			alignStart = asta;
			alignStop = asto;
			amplicon = amp;
			pop = p;
		}
		public void run()
		{
			amplicon.initialiseRandomAmpliconSet(avgReadLength, stdReadLength, alignStart, alignStop, pop);
		}
	}
	
	public void estimateAmpliconsParallel(int runs, int n_proc) throws DocumentOperationException
	{
		ArrayList<Read> pop = new ArrayList<Read>();
		int subSetSize=(int)((double)(population.size())/2d);
		if (population.size()>10000)
			subSetSize=5000;
		if (population.size()>100000)
			subSetSize=10000;
		if (population.size()>500000)
			subSetSize=50000;
		int f=(int)((double)(population.size())/(double)(subSetSize));
		for (int i=0; i<population.size(); i++)
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			if (i%f==0)
				pop.add(population.get(i));
		}
		pop=population;
		//System.out.println("optimising amplicon set");
		//if (population.size()<5000)
		//	pop=population;
		//else
		//	System.out.println("\tsubsampling enabled from "+population.size()+" reads to "+pop.size());
		System.out.print("\tphase 0: fixed-size sliding window overlaps ");
		LinkedList<AmpliconSet> ampliconList = new LinkedList<AmpliconSet>();
		double [] steps = {1d/20d,2d/20d,3d/20d,4d/20d,5d/20d,6d/20d,7d/20d,8d/20d,9d/20d,10d/20d,11d/20d,12d/20d,13d/20d,14d/20d,15d/20d,16d/20d,17d/20d,18d/20d,19d/20d};
		double [] windowSizes = {(avgReadLength+stdReadLength),avgReadLength,(avgReadLength-stdReadLength),(avgReadLength-2*stdReadLength),(avgReadLength-3*stdReadLength)};
		for (int count=0; count<windowSizes.length; count++)
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			if (windowSizes[count]<60)
				windowSizes[count]=Math.max(60,Math.random()*avgReadLength);
		}
		//double bestSTEP=0;
		//double bestWS=0;
		for (int count=0; count<steps.length; count++)
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			String perc = (Math.round(100f*((float)(count*windowSizes.length))/((float)(steps.length*windowSizes.length))))+"% ";
			System.out.print(perc);
			for (int count1=0; count1<windowSizes.length; count1++)
			{
				if(QuRe.cancel)
				    throw new DocumentOperationException("Cancelled!");
				double ws = windowSizes[count1];
				double step = steps[count]*ws;
				int windows = (int)((alignStop-alignStart)/step);
				//System.out.println("no. of windows "+windows+"; windowSizes "+ws+"; step "+step+"; alignStart "+alignStart+"; alignStop "+alignStop);
				if (windows>1)
				{
					double [] startsSW = new double[windows];
					double [] stopsSW = new double[windows];
					startsSW[0]=alignStart;
					stopsSW[0]=startsSW[0]+ws;
					for (int i=1; i<windows; i++)
					{
						startsSW[i]=startsSW[i-1]+step;
						stopsSW[i]=startsSW[i]+ws;
					}
					stopsSW[windows-1]=alignStop;
					AmpliconSet amplicon = new AmpliconSet(startsSW,stopsSW,pop,alignStart,alignStop);
					if (amplicon.checkConsistency() && amplicon.minReadCoverage>1)
						ampliconList.add(amplicon);
				}
				else
				{
					double [] startsSW = new double[2];
					double [] stopsSW = new double[2];
					int st = (int)Math.max(2,(Math.random()*(alignStop-alignStart)));
					startsSW[0]=alignStart;
					stopsSW[0]=alignStart+st;
					startsSW[1]=alignStop-st;
					stopsSW[1]=alignStop;
					AmpliconSet amplicon = new AmpliconSet(startsSW,stopsSW,pop,alignStart,alignStop);
					if (amplicon.checkConsistency() && amplicon.minReadCoverage>1)
						ampliconList.add(amplicon);
				}
			}
			for (int o=0; o<perc.length(); o++)
				System.out.print("\b");
		}
		System.out.print("100%");
		System.out.println();
		System.out.print("\tphase 1: random overlaps ");
		int t=0;
		Thread [] thread_list = new Thread [n_proc];
		while(t<runs)
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			String perc = (Math.round(100f*((float)(t))/((float)(runs))))+"% ";
			System.out.print(perc);
			if (t==0)
				for (int j=0; j<n_proc; j++)
				{
					AmpliconSet a = new AmpliconSet();
					ampliconList.add(a);
					Runnable runnable = new randomAmpliconThread(avgReadLength, stdReadLength, alignStart, alignStop, a, pop);
					thread_list[j] = new Thread(runnable);
					thread_list[j].start();
					t++;
				}
			else
			{
				int j=0;
				while(thread_list[j].isAlive())
				{
					j++;
					j=j%n_proc;
				}
				AmpliconSet a = new AmpliconSet();
				ampliconList.add(a);
				Runnable runnable = new randomAmpliconThread(avgReadLength, stdReadLength, alignStart, alignStop, a, pop);
				thread_list[j] = new Thread(runnable);
				thread_list[j].start();
				t++;
			}
			for (int o=0; o<perc.length(); o++)
				System.out.print("\b");
		}
		int j=0;
		while(j<n_proc)
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			if (!thread_list[j].isAlive())
				j++;
		}
		System.out.print("100%");
		System.out.println();
		System.out.println("\tphase 3: assessing best a-posteriori overlaps set");
		int o=0;
		while(o<ampliconList.size())
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			//System.out.println(ampliconList.get(o).checkConsistency()+" "+ampliconList.get(o).minReadCoverage+" "+ampliconList.get(o).minOverlapDiversity+" "+ampliconList.get(o).starts[0]+" "+ampliconList.get(o).stops[ampliconList.get(o).stops.length-1]+" "+ampliconList.get(o).stops.length);
			if (ampliconList.get(o).checkConsistency() && ampliconList.get(o).minReadCoverage>1)
				o++;
			else
				ampliconList.remove(o);
		}	
		System.out.println("\t\t overlaps space n="+ampliconList.size());
		double [] minReadCoverages=new double[ampliconList.size()];
		double [] minOverlapDiversities=new double[ampliconList.size()];
		for (int count=0; count<ampliconList.size(); count++)
		{
			if(QuRe.cancel)
			    throw new DocumentOperationException("Cancelled!");
			minReadCoverages[count]=ampliconList.get(count).minReadCoverage;
			minOverlapDiversities[count]=ampliconList.get(count).minOverlapDiversity;
		}
		double avgMRC=Functions.average(minReadCoverages);
		double stdMRC=Functions.stdev(minReadCoverages);
		double avgMOD=Functions.average(minOverlapDiversities);
		double stdMOD=Functions.stdev(minOverlapDiversities);
		System.out.println("\t\t average (st.dev) min. interval coverage "+Math.round(avgMRC)+" ("+Math.round(stdMRC)+")");
		System.out.println("\t\t average (st.dev) min. overlaps diversity "+Functions.roundToDecimals(avgMOD,1)+" ("+Functions.roundToDecimals(stdMOD,1)+")");
		for (int oo=0; oo<ampliconList.size(); oo++)
			ampliconList.get(oo).setStats(avgMRC, stdMRC, avgMOD, stdMOD);
		Comparator<AmpliconSet> comparatorAmplicon = new Comparator<AmpliconSet>()
		{
		    
			public int compare(AmpliconSet r1, AmpliconSet r2)
			{
				if (r1.isBetter(r2))
					return -1;
				if (!r1.isBetter(r2))
					return 1;
				return 0;
			}
		};
		Collections.sort(ampliconList,comparatorAmplicon);
		AmpliconSet bestAmplicon=new AmpliconSet();
		bestAmplicon=ampliconList.get(0);
		goodAmpliconSets=new ArrayList<AmpliconSet>();
		for (int oo=0; oo<((double)ampliconList.size()*0.25d); oo++)
			goodAmpliconSets.add(ampliconList.get(oo));
		System.out.println("\t\t max. a-posteriori overlaps set:");
		System.out.println("\t\t\tmin. interval coverage "+Math.round(bestAmplicon.minReadCoverage)+" (post.prob. = "+Functions.roundToDecimals(bestAmplicon.minReadCoverageProbability,2)+")");
		System.out.println("\t\t\tmin. overlap diversity "+Functions.roundToDecimals(bestAmplicon.minOverlapDiversity,1)+" (post.prob. = "+Functions.roundToDecimals(bestAmplicon.minOverlapDiversityProbability,2)+")");
		System.out.println("\t\t\tnumber of intervals = "+bestAmplicon.starts.length);
		for (int oo=0; oo<bestAmplicon.starts.length; oo++)
		{
			System.out.print("\t\t\t"+Math.round(bestAmplicon.starts[oo])+"-"+Math.round(bestAmplicon.stops[oo])+"\r\n");
		}
		ampliconSet=bestAmplicon;
	}
}

