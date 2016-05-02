public class FloatChromo extends Chromo {
	// TODO: Create representations and assoc functions
	//double genes will be between -1 and 1, multiply by a max value when returning values
	double [] genes;
	
	public FloatChromo()
	{
		genes = new double[Parameters.numGenes];
		
		for(int i = 0; i < Parameters.numGenes; i++)
		{
			genes[i] = 0;//  (2 * Search.r.nextDouble() - 1)/10000000; //between  -1 and 1
			//genes[i] =  Search.r.nextGaussian(); //between  -1 and 1
		}
		
	}
	
	public double getGeneValue(int geneID) 
	{
		return Parameters.maxDoubleVal * genes[geneID];
	}
	public String getGeneAlpha(int geneID)
	{
		return genes[geneID]+"";
	}
	public int getIntGeneValue(int geneID)
	{
		return (int)genes[geneID];
	}
	public int getPosIntGeneValue(int geneID)
	{
		return (int)genes[geneID];
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
					//next gaussian has no upper/lower bound, so must divide by a number to keep most answers under 1
					//rnum = Search.r.nextGaussian()/(Parameters.maxDoubleVal/10); 
					rnum = (Search.r.nextDouble()*2 - 1)/((Parameters.maxDoubleVal)/(Parameters.maxFloatPerturb)) ; 
					//rnum = Search.r.nextDouble()*2-1;
					genes[j] += rnum;
					if(genes[j] > 1)
					{
						genes[j] = 1;
					}
					if(genes[j] < -1)
					{
						genes[j] = -1;
					}
				}
				rnum = Search.r.nextDouble();
				if(rnum < Parameters.mutateNegateFloatRate)
				{
					genes[j] = -genes[j];
				}
			}
			break;

		default:
			System.out.println("ERROR - No mutation method selected");
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
					((FloatChromo)child1).genes[i] = ((FloatChromo)parent1).genes[i];
					((FloatChromo)child2).genes[i] = ((FloatChromo)parent2).genes[i];
				}
				else
				{
					((FloatChromo)child1).genes[i] = ((FloatChromo)parent2).genes[i];
					((FloatChromo)child2).genes[i] = ((FloatChromo)parent1).genes[i];
				}
				
			}
			
			
			break;

		case 2:     //  Averaging 
			for(int i = 0; i < Parameters.numGenes; i++)
			{
				((FloatChromo)child1).genes[i] = (((FloatChromo)parent1).genes[i] + ((FloatChromo)parent2).genes[i])/2.;
				((FloatChromo)child2).genes[i] = (((FloatChromo)parent1).genes[i] + ((FloatChromo)parent2).genes[i])/2.;
			}
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

	
	//  Produce a new child from a single parent  ******************************

	public void mateParents(int pnum, Chromo parent, Chromo child)
	{
		
		((FloatChromo)child).genes = (double[])((FloatChromo)parent).genes.clone();
		child.rawFitness = -1;   //  Fitness not yet evaluated
		child.sclFitness = -1;   //  Fitness not yet scaled
		child.proFitness = -1;   //  Fitness not yet proportionalized
	}

	//  Copy one chromosome to another  ***************************************

	public void copyB2A (Chromo targetA, Chromo sourceB)
	{
		targetA.chromo = sourceB.chromo;
		((FloatChromo)targetA).genes = (double[])((FloatChromo)sourceB).genes.clone();
		targetA.rawFitness = sourceB.rawFitness;
		targetA.sclFitness = sourceB.sclFitness;
		targetA.proFitness = sourceB.proFitness;
		return;
	}
	
}

