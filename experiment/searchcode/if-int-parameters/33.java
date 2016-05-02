import java.util.ArrayList;

/**
 * 
 */

/**
 * @author david
 *
 */
public class DynamicChromo extends Chromo 
{
	ArrayList< ArrayList<Double> > genes;
	
	public DynamicChromo()
	{
		genes = new ArrayList< ArrayList<Double> >();
		
		for(int i = 0; i < Parameters.numGenes; i++)
		{
			genes.add(new ArrayList<Double>());
			genes.get(i).add(new Double(0));
			//genes.get(i).add(Search.r.nextDouble());
			
		}
		
	}

	protected double averageGene(int geneID)
	{
		ArrayList<Double> a = genes.get(geneID);
		if(a.size() == 0) return 0;
		double result = 0;
		for(int i = 0; i < a.size(); i++)
		{
			result += a.get(i);
		}
		return result / (double)a.size();
	}
	public double getGeneValue(int geneID) 
	{
		return Parameters.maxDoubleVal * averageGene(geneID);
	}
	public String getGeneAlpha(int geneID)
	{
		return getGeneValue(geneID) +"";
	}
	public int getIntGeneValue(int geneID)
	{
		return (int) getGeneValue(geneID);
	}
	public int getPosIntGeneValue(int geneID)
	{
		return (int)getGeneValue(geneID);
	}
	//  Mutate a Chromosome Based on Mutation Type *****************************

	public void doMutation()
	{
		double rnum;

		switch (Parameters.mutationType)
		{
		case 1:     // perturbs existing double value by a normal random in the range of +-maxdouble

			for (int j=0; j< Parameters.numGenes; j++)
			{
				
				rnum = Search.r.nextDouble();
				if (rnum < Parameters.mutationRate)
				{
					mutateGene(j,(int)(Search.r.nextDouble()*(double)genes.get(j).size()));
				}
				if (rnum < Parameters.mutateNegateFloatRate)
				{
					negateBlock(j,(int)(Search.r.nextDouble()*(double)genes.get(j).size()));
				}
				rnum = Search.r.nextDouble();
				if (rnum < Parameters.mutateDuplicateRate)
				{
					if(Search.r.nextDouble() < .5)
					{
						addBlock(j,(int)(Search.r.nextDouble()*(double)genes.get(j).size()));
					}
					else
					{
						deleteBlock(j,(int)(Search.r.nextDouble()*(double)genes.get(j).size()));
					}
				}
			}
			break;

		default:
			System.out.println("ERROR - No mutation method selected");
		}
	}
	public void addBlock(int geneIndex, int blockIndex)
	{
		if(genes.size() > geneIndex && genes.get(geneIndex).size() > blockIndex)
		{
			genes.get(geneIndex).add(genes.get(geneIndex).get(blockIndex));
		}
		else
		{
			System.out.println("Dynamic Chrome Error: AddBlock Out of Bounds");
		}
	}
	public void deleteBlock(int geneIndex, int blockIndex)
	{
		if(genes.size() > geneIndex && genes.get(geneIndex).size() > 1 && genes.get(geneIndex).size() > blockIndex)
		{
			genes.get(geneIndex).remove(blockIndex);
		}
		else
		{
			//System.out.println("Dynamic Chrome Error: DeleteBlock Out of Bounds");
		}
	}
	public void mutateGene(int geneIndex, int blockIndex)
	{
		double rnum;
		if(genes.size() > geneIndex && genes.get(geneIndex).size() > blockIndex)
		{
			rnum = (Search.r.nextDouble()*2 - 1)/((Parameters.maxDoubleVal)/(Parameters.maxFloatPerturb)) ; 
			//rnum = Search.r.nextGaussian()/10;
			Double d = genes.get(geneIndex).get(blockIndex).doubleValue() + rnum;
			genes.get(geneIndex).set(blockIndex, d);
		}
		else
		{
			System.out.println("Dynamic Chrome Error: MutateGene Out of Bounds");
		}
	}
	public void negateBlock(int geneIndex, int blockIndex)
	{
		if(genes.size() > geneIndex && genes.get(geneIndex).size() > blockIndex)
		{
			Double d = -genes.get(geneIndex).get(blockIndex).doubleValue();
			genes.get(geneIndex).set(blockIndex, d);
		}
		else
		{
			System.out.println("Dynamic Chrome Error: MutateGene Out of Bounds");
		}
	}
	//  Produce a new child from two parents  **********************************
	
	public void mateParents(int pnum1, int pnum2, Chromo parent1, Chromo parent2, Chromo child1, Chromo child2)
	{
		int xoverPoint1;
		int xoverPoint2;

		switch (Parameters.xoverType){

		case 1:     //  Single Point Crossover, no averaging across genes

			//  Select crossover point
			xoverPoint1 = 1+(int)(Search.r.nextDouble() * ((double)Parameters.numGenes));

			//  Create child chromosome from parental material
			for(int i = 0; i < Parameters.numGenes; i++)
			{
				if(i < xoverPoint1)
				{
					copyGene(((DynamicChromo)parent1).genes.get(i),((DynamicChromo)child1).genes.get(i));
					copyGene(((DynamicChromo)parent2).genes.get(i),((DynamicChromo)child2).genes.get(i));
				}
				else
				{
					copyGene(((DynamicChromo)parent2).genes.get(i),((DynamicChromo)child1).genes.get(i));
					copyGene(((DynamicChromo)parent1).genes.get(i),((DynamicChromo)child2).genes.get(i));
				}
				
			}
			
			
			break;

		case 2:     //  Averaging - cant do it 
			//for(int i = 0; i < Parameters.numGenes; i++)
			//{
				//((DynamicChromo)child1).genes[i] = (((DynamicChromo)parent1).genes[i] + ((FloatChromo)parent2).genes[i])/2.;
				//((DynamicChromo)child2).genes[i] = (((DynamicChromo)parent1).genes[i] + ((FloatChromo)parent2).genes[i])/2.;
			//}
			break;
		case 3:     //  Uniform Crossover

		default:
			System.out.println("ERROR - Bad crossover method selected");
		}

		//  Set fitness values back to zero
		child1.rawFitness = -1;   //  Fitness not yet evaluated
		child1.sclFitness = -1;   //  Fitness not yet scaled
		child1.proFitness = -1;   //  Fitness not yet proportionalized
		child2.rawFitness = -1;   //  Fitness not yet evaluated
		child2.sclFitness = -1;   //  Fitness not yet scaled
		child2.proFitness = -1;   //  Fitness not yet proportionalized
	}
	protected void copyGene(ArrayList<Double> source, ArrayList<Double> dest)
	{
		dest.clear();
		for(int i = 0; i< source.size(); i++)
		{
			dest.add(source.get(i));
		}
	}
//	protected ArrayList<Double> averageGenes(ArrayList<Double> a, ArrayList<Double> b)
//	{
//
//		ArrayList<Double> result = new ArrayList<Double>();
//		for(int i = 0; i< source.size(); i++)
//		{
//			result.add((a.get(i).doubleValue() + b.get(i).doubleValue())/2.0);
//		}
//	}
	protected void copyGenes(ArrayList< ArrayList<Double> > source, ArrayList< ArrayList<Double> > dest)
	{
		dest.clear();
		ArrayList<Double> temp = new ArrayList<Double>();
		for(int i = 0; i < source.size(); i++)
		{
			temp.clear();
			for(int j = 0; j< source.get(i).size(); j++)
			{
				temp.add(source.get(i).get(j));
			}
			dest.add((ArrayList<Double>)temp.clone());			
		}
	}
	//  Produce a new child from a single parent  ******************************

	public void mateParents(int pnum, Chromo parent, Chromo child)
	{
		
		copyGenes(((DynamicChromo)parent).genes,((DynamicChromo)child).genes);
		child.rawFitness = -1;   //  Fitness not yet evaluated
		child.sclFitness = -1;   //  Fitness not yet scaled
		child.proFitness = -1;   //  Fitness not yet proportionalized
	}

	//  Copy one chromosome to another  ***************************************

	public void copyB2A (Chromo targetA, Chromo sourceB)
	{
		copyGenes(((DynamicChromo)sourceB).genes,((DynamicChromo)targetA).genes);
		targetA.rawFitness = sourceB.rawFitness;
		targetA.sclFitness = sourceB.sclFitness;
		targetA.proFitness = sourceB.proFitness;
		return;
	}
	
	public void print()
	{
		System.out.print("gene length\t");
		double ave = 0;
		for(int i = 0; i < genes.size(); i++)
		{
			System.out.print(genes.get(i).size() + "\t");
			ave += genes.get(i).size();
		}
		System.out.println((ave/(double)genes.size()));
	
	}
}

