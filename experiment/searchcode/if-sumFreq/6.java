package edu.ufl.qure;

import java.util.*;

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
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;

public class LocalVariantSet
{
	public Hashtable<String,LocalVariant> lvsH;
	public ArrayList<LocalVariant> lvsA;
	
	public LocalVariantSet()
	{
		lvsH=null;
		lvsA=null;
	}
	public LocalVariantSet(ArrayList<Read> p, double start, double middle1, double middle2, double stop, String genome, double hErr, double nonhErr, int n_proc)
	{
		Comparator<LocalVariant> comparatorFrequencies = new Comparator<LocalVariant>()
		{
			public int compare(LocalVariant lv1, LocalVariant lv2)
			{
				if (lv1.frequency>lv2.frequency)
					return -1;
				if (lv1.frequency<lv2.frequency)
					return 1;
				return 0;
			}
		};
		lvsH=new Hashtable<String,LocalVariant>();
		for (int i=0; i<p.size(); i++)
		{
			Read r = p.get(i);
			if (r.spans(start,stop))
			{
				String snpA=r.getSNPString(middle1+1, middle2-1);
				String snpO1=r.getSNPString(start, middle1);
				String snpO2=r.getSNPString(middle2, stop);
				String snp=r.getSNPString(start, stop);
				if (lvsH.get(snp)==null)
				{
					LocalVariant lv = new LocalVariant(snpA,snpO1,snpO2,snp,1);
					lvsH.put(snp,lv);
				}
				else
				{
					LocalVariant lv = lvsH.get(snp);
					lv.frequency=lv.frequency+1;
					lvsH.put(snp,lv);
				}
			}
		}
		Object [] keys = lvsH.keySet().toArray();
		
		/*
		for (int i=0; i<keys.length; i++)
		{
			LocalVariant lv = lvsH.get(keys[i]);
			if (lv.frequency<2)
				lvsH.remove(keys[i]);
		}
		*/
		
		/*
		keys = lvsH.keySet().toArray();
		lvsA=new ArrayList();
		for (int i=0; i<keys.length; i++)
		{
			LocalVariant lv = lvsH.get(keys[i]);
			lvsA.add(lv);
		}
		Collections.sort(lvsA,comparatorFrequencies);
		this.correctLocalVariantSetParallel(start, stop, genome, hErr, nonhErr, 250, n_proc);
		*/
		
		keys = lvsH.keySet().toArray();
		double tot=0;
		for (int i=0; i<keys.length; i++)
		{
			LocalVariant lv = lvsH.get(keys[i]);
			tot+=lv.frequency;
		}
		lvsA=new ArrayList<LocalVariant>();
		for (int i=0; i<keys.length; i++)
		{
			LocalVariant lv = lvsH.get(keys[i]);
			lv.frequency = 100*lv.frequency/tot;
			lvsH.put(lv.SNP,lv);
			lvsA.add(lv);
		}
		Collections.sort(lvsA,comparatorFrequencies);
	}
	
	public class correctLocalVariantSetThread implements Runnable  
	{
		ClusterSet clusterSet;
		public correctLocalVariantSetThread(ClusterSet cs)
		{
			clusterSet=cs;
		}
		public void run()
		{
			clusterSet.setPosterior();
		}
	}
	
	public void correctLocalVariantSetParallel(double start, double stop, String genome, double hErr, double nonhErr, int runs, int n_proc) throws DocumentOperationException
	{
		ArrayList<Integer> bestClusterSet = new ArrayList<Integer>();
		//LinkedList<ArrayList<Integer>> allClusters = new LinkedList<ArrayList<Integer>>();
		for (int i=0; i<Math.floor(lvsA.size()*0.15); i++)
			bestClusterSet.add(i);
		double bestPosteriorLikelihood = Functions.posteriorClusteringLikelihood(bestClusterSet, lvsA, (stop-start), genome, hErr, nonhErr);
		
		/*
		for (int i=0; i<30; i++)
		{
			ArrayList<Integer> clusterIndices = Functions.proposeClusterSet(bestClusterSet,lvsA.size());
			ClusterSet cs = new ClusterSet(clusterIndices, lvsA, (stop-start), genome, hErr, nonhErr);
			cs.setPosterior();
			if (cs.posteriorLikelihood<bestPosteriorLikelihood)
			{
				System.out.println(cs.clusterSet.size());
				bestClusterSet=cs.clusterSet;
				bestPosteriorLikelihood=cs.posteriorLikelihood;
			}
		}		
		*/
		
		int i=0;
		Thread [] thread_list = new Thread [n_proc];
		ClusterSet [] csa = new ClusterSet [n_proc];
		while(i<runs)
		{
			String perc = (Math.round(100f*((float)(i))/((float)(runs))))+"% ";
			System.out.print(perc);
			if (i==0)
				for (int j=0; j<n_proc; j++)
				{
					ArrayList<Integer> clusterIndices = Functions.proposeClusterSet(bestClusterSet,lvsA.size());
					ClusterSet cs = new ClusterSet(clusterIndices, lvsA, (stop-start), genome, hErr, nonhErr);
					csa[j] = cs;
					Runnable runnable = new correctLocalVariantSetThread(csa[j]);
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
				if (csa[j].posteriorLikelihood<bestPosteriorLikelihood)
				{
					bestClusterSet=csa[j].clusterSet;
					bestPosteriorLikelihood=csa[j].posteriorLikelihood;
				}
				ArrayList<Integer> clusterIndices = Functions.proposeClusterSet(bestClusterSet,lvsA.size());
				ClusterSet cs = new ClusterSet(clusterIndices, lvsA, (stop-start), genome, hErr, nonhErr);
				csa[j] = cs;
				Runnable runnable = new correctLocalVariantSetThread(csa[j]);
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
		j=0;
		while(j<n_proc)
		{
			if (csa[j].posteriorLikelihood<bestPosteriorLikelihood)
			{
				bestClusterSet=csa[j].clusterSet;
				bestPosteriorLikelihood=csa[j].posteriorLikelihood;
			}	
			j++;
		}
		System.out.println("100%");
		
		System.out.println("\t\tbest BIC = "+Functions.roundToDecimals(bestPosteriorLikelihood,2));
		
		ArrayList<LocalVariant> lvsA_new = new ArrayList<LocalVariant>();
		for (int l=0; l<bestClusterSet.size(); l++)
			lvsA_new.add(lvsA.get(bestClusterSet.get(l)));
		
		/*
		lvsA=lvsA_new;
		lvsH=new Hashtable();
		for (int l=0; l<lvsA.size(); l++)
		{
			LocalVariant lv = lvsA.get(l);
			lvsH.put(lv.SNP,lv);
		}
		*/
		
		ArrayList<LocalVariant> lvsA_new_new = new ArrayList<LocalVariant>();
		for (int l1=0; l1<bestClusterSet.size(); l1++)
		{
			Hashtable<String,Double> cons = new Hashtable<String,Double>();
			double tot = 0;
			double sumFreq = 0;
			for (int l2=0; l2<lvsA.size(); l2++)
			{
				if (lvsA.get(l2).classify(lvsA_new)==l1)
				{
					tot++;
					sumFreq+=lvsA.get(l2).frequency;
					String all_snp_string = lvsA.get(l2).SNP;
					if (all_snp_string.startsWith(","))
						all_snp_string=all_snp_string.substring(1);
					String [] all_snp_array = all_snp_string.split(",");
					for (int l3=0; l3<all_snp_array.length; l3++)
					{
						if (cons.get(all_snp_array[l3])==null)
							cons.put(all_snp_array[l3],1d);
						else
						{
							double f = cons.get(all_snp_array[l3])+1d;
							cons.put(all_snp_array[l3],f);
						}
					}
				}				
			}
			String [] keys = cons.keySet().toArray(new String[0]);
			for (int l2=0; l2<keys.length; l2++)
			{
				double nf = cons.get(keys[l2])/tot;
				if (nf>0.5)
					cons.put(keys[l2],nf);
				else
					cons.remove(keys[l2]);
			}
			String SNP_new_new = "";
			keys = cons.keySet().toArray(new String[0]);
			for (int l2=0; l2<keys.length; l2++)
				SNP_new_new+=keys[l2]+",";
			//System.out.println(SNP_new_new);
			LocalVariant lv_new_new = new LocalVariant("", "", "", SNP_new_new, sumFreq);
			lvsA_new_new.add(lv_new_new);
		}
		
		lvsA=lvsA_new_new;
		lvsH=new Hashtable<String,LocalVariant>();
		for (int l=0; l<lvsA.size(); l++)
		{
			LocalVariant lv = lvsA.get(l);
			lvsH.put(lv.SNP,lv);
		}
		
		
		//System.out.println("\t\tnumber of variants = "+lvsA.size());
	}
	
	public void sortMultinomialFrequencies()
	{
		Comparator<LocalVariant> comparatorFrequencies = new Comparator<LocalVariant>()
		{
			public int compare(LocalVariant lv1, LocalVariant lv2)
			{
				if (lv1.frequency>lv2.frequency)
					return -1;
				if (lv1.frequency<lv2.frequency)
					return 1;
				return 0;
			}
		};		
		Collections.sort(this.lvsA,comparatorFrequencies);
	}
}

