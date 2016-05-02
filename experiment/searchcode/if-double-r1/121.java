package edu.ufl.qure;

//import java.io.*;
//import java.lang.*;
import java.util.*;
//import java.math.*;

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
public class AmpliconSet
{
	double [] starts;
	double [] stops;
	double genomeSpan;
	double minReadCoverage;
	double minOverlapDiversity;
	double minNonZeroOverlapDiversity;
	double minReadCoverageProbability;
	double minOverlapDiversityProbability;
	double minNonZeroOverlapDiversityProbability;
	double score;
	
	public AmpliconSet()
	{
		starts=null;
		stops=null;
		genomeSpan=0;
		minReadCoverage=0;
		minOverlapDiversity=0;
		minNonZeroOverlapDiversity=0;
		minReadCoverageProbability=0;
		minOverlapDiversityProbability=0;
		minNonZeroOverlapDiversityProbability=0;
		score=0;
	}
	
	public AmpliconSet(double [] sta, double [] sto, ArrayList<Read> population, double alignStart, double alignStop)
	{
		score=0;
		starts=sta;
		stops=sto;
		genomeSpan = (stops[starts.length-1]-starts[0])/(alignStop-alignStart);
		ArrayList<Read> [] covering = new ArrayList[starts.length];
		double mrc = 0;
		for (int j=0; j<starts.length; j++)
		{
			covering[j]=new ArrayList<Read>();
			double rc=0;
			for (int i=0; i<population.size(); i++)
			{
				Read r = population.get(i);
				if (r.spans(starts[j],stops[j]))
				{
					rc++;
					covering[j].add(r);
				}
			}
			if (j==0)
				mrc=rc;
			else
				if (rc<mrc)
					mrc=rc;
		}
		minReadCoverage=mrc;
		
		double modp = 0;
		double [] modp2 = new double[starts.length];
		for (int j=1; j<starts.length; j++)
		{
			/*
			Hashtable hh = new Hashtable();
			double cover=1;
			for (int i=0; i<population.size(); i++)
			{
				Read r = population.get(i);
				if (r.spans(starts[j],stops[j-1]))
				{
					cover++;
					String s = r.getSNPString(starts[j],stops[j-1]);
					if (hh.get(s)==null)
						hh.put(s,1);
				}
			}
			double odp=(double)(hh.keySet().toArray().length+1)/cover;
			*/
			ArrayList<Read> allOverlappingReads = new ArrayList<Read>();
			for (int i=0; i<covering[j-1].size(); i++)
				allOverlappingReads.add(covering[j-1].get(i));
			for (int i=0; i<covering[j].size(); i++)
				allOverlappingReads.add(covering[j].get(i));
			int subSample = 3000;
			double odp=0;
			if (minReadCoverage>0 && allOverlappingReads.size()>0)
			{
				if ((double)allOverlappingReads.size()*((double)allOverlappingReads.size()-1d)/2d<subSample)
				{
					for (int i=0; i<allOverlappingReads.size(); i++)
						for (int k=i+1; k<allOverlappingReads.size(); k++)
						{
							Read r1 = allOverlappingReads.get(i);
							Read r2 = allOverlappingReads.get(k);
							double dista = r1.distance(r2,starts[j],stops[j-1]);
							odp+=dista;
						}
					odp=odp/((double)allOverlappingReads.size()*((double)allOverlappingReads.size()-1d)/2d);
					//System.out.println("full "+odp);
				}
				else
				{
					for (int i=0; i<subSample; i++)
					{
						Read r1 = allOverlappingReads.get((int)(Math.random()*(allOverlappingReads.size()-1)));
						Read r2 = allOverlappingReads.get((int)(Math.random()*(allOverlappingReads.size()-1)));
						double dista = r1.distance(r2,starts[j],stops[j-1]);
						odp+=dista;
					}
					odp=odp/(double)subSample;
					//System.out.println("subSample "+odp);
				}
			}
			if (j==1)
			{
				modp=odp;
			}
			else
			{
				if (odp<modp)
					modp=odp;
			}
			modp2[j]=odp;
		}
		minOverlapDiversity=modp;
		minNonZeroOverlapDiversity=Functions.average(modp2);
	}
	
	public void setStats(double avgReadCover, double stdReadCover, double avgOverlapDiversity, double stdOverlapDiversity)
	{
		minReadCoverageProbability=Functions.zetaStandardProbability((minReadCoverage - avgReadCover)/stdReadCover);
		minOverlapDiversityProbability=Functions.zetaStandardProbability((minOverlapDiversity - avgOverlapDiversity)/stdOverlapDiversity);
		minNonZeroOverlapDiversityProbability=Functions.zetaStandardProbability((minNonZeroOverlapDiversity - avgOverlapDiversity)/stdOverlapDiversity);
		score=-Math.log(genomeSpan);
		score=score-Math.log(minReadCoverageProbability);
		score=score-Math.log(minOverlapDiversityProbability);
		score=score-Math.log(minNonZeroOverlapDiversityProbability);
		score=-score;
		//score=genomeSpan*minReadCoverageProbability*minOverlapDiversityProbability*minNonZeroOverlapDiversityProbability;
	}
	
	public boolean isBetter(AmpliconSet b)
	{
		return (this.score>b.score);
	}
	
	public boolean checkConsistency()
	{
		if (starts.length==1)
			return true;
		for (int i=0; i<(starts.length-1); i++)
		{
			double start1=starts[i];
			double start2=starts[i+1];
			double stop1=stops[i];
			double stop2=stops[i+1];
			if (!(start1<start2 && stop1<stop2 && start1<stop1 && start2<stop2 && start2<stop1))
				return false;
		}
		return true;
	}
	
	public boolean checkNonMutualOverlapsConsistency()
	{
		if (starts.length==1)
			return true;
		if (starts.length==2)
		{
			double start1=starts[0];
			double start2=starts[1];
			double stop1=stops[0];
			double stop2=stops[1];
			return ( (start2<stop1) && (start1<start2) && (stop1<stop2) );
		}
		for (int i=0; i<(starts.length-2); i++)
		{
			double start1=starts[i];
			double start2=starts[i+1];
			double start3=starts[i+2];
			double stop1=stops[i];
			double stop2=stops[i+1];
			double stop3=stops[i+2];
			if 
			(!(
				(stop1<start3) &&
				(start1<start2) &&
				(start2<start3) &&
				(stop1<stop2) &&
				(stop2<stop3) &&
				(start2<stop1) &&
				(start3<stop2)
			))
			return false;
		}
		return true;
	}
	
	public void initialiseRandomAmpliconSet(double avgReadLength, double stdReadLength, double alignStart, double alignStop, ArrayList<Read> pop)
	{
		LinkedList<Double> startL = new LinkedList<Double>();
		LinkedList<Double> stopL = new LinkedList<Double>();
		//double stdNew = stdReadLength;
		double prevStart=alignStart;
		Random rndA = new Random();
		double rngA = rndA.nextGaussian()*stdReadLength+(avgReadLength-3*stdReadLength);
		if (rngA<60)
			rngA = Math.max(60,Math.random()*avgReadLength);
		double prevStop=Math.min(prevStart+rngA,alignStop);
		double rndO = Math.random();
		startL.add(prevStart);
		stopL.add(prevStop);	
		while (true)
		{
			if (prevStop==alignStop)
				break;
			double sta = prevStart+rngA*rndO;
			rngA = rndA.nextGaussian()*stdReadLength+(avgReadLength-3*stdReadLength);
			if (rngA<60)
				rngA = Math.max(60,Math.random()*avgReadLength);
			double sto = Math.max(prevStop+stdReadLength,sta+rngA);
			sto = Math.min(sto,alignStop);
			startL.add(sta);
			stopL.add(sto);
			prevStart = sta;
			prevStop = sto;
			rndO = Math.random();
		}
		double[] starts1 = new double[startL.size()];
		double[] stops1 = new double[startL.size()];
		for (int l=0; l<starts1.length; l++)
		{
			starts1[l]=startL.get(l);
			stops1[l]=stopL.get(l);
		}
		AmpliconSet amplicon = new AmpliconSet(starts1,stops1,pop,alignStart,alignStop);
		starts=amplicon.starts;
		stops=amplicon.stops;
		genomeSpan=amplicon.genomeSpan;
		minReadCoverage=amplicon.minReadCoverage;
		minOverlapDiversity=amplicon.minOverlapDiversity;
		minReadCoverageProbability=amplicon.minReadCoverageProbability;
		minOverlapDiversityProbability=amplicon.minOverlapDiversityProbability;
		score=amplicon.score;
	}
}
	
