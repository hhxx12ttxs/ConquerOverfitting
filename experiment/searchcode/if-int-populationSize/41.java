package sdsu.cs657.geneticAlgorithm_TravelingSalesman;

import java.util.ArrayList;

public class Generation {

	private ArrayList<Chromosome> presentGeneration;
	private ArrayList<Chromosome> nextGeneration;
	private double crossoverProbability = 0;
	private double mutationProbability = 0;
	
	public Generation(int populationSize, int chromosomeSize,  double operatorProbability){
		presentGeneration = new ArrayList<Chromosome>(populationSize);
		for(int i=0; i<populationSize; i++)
			presentGeneration.add(new Chromosome(chromosomeSize));
	//	previousGeneration = new ArrayList<Chromosome>(populationSize);
		setCrossoverProbability(operatorProbability);	
	}
	
	public void setCrossoverProbability(double value){
		if(value > 1)
			this.crossoverProbability = 1;
		else if(value < 0)
			this.crossoverProbability = 0;
		else
			this.crossoverProbability = value;
		
		setMutationProbability();
	}
	
	public void generateInitial(){
		for(Chromosome each: presentGeneration){
			each.generateRandom();
			while(checkValidity(each)){
				each.generateRandom();
			}
		}
	}
	
	public void evaluateFitness(){
		for(Chromosome each: presentGeneration)
			each.evaluateFitness();
	}
	
	private void setMutationProbability(){
		this.mutationProbability = 1 - this.crossoverProbability;
	}
	
	private boolean checkValidity(Chromosome t){
		for(Chromosome each: presentGeneration){
			if(t == each)
				continue;
			if(t.equals(each)){
				return true;
			}
		}
		return false;
	}
	
	//for checking only!
	public boolean checktest(){
		for(Chromosome tester: presentGeneration){
			for(Chromosome others: presentGeneration){
				if(tester == others)
					continue;
				if(tester.equals(others)){
					System.out.println("\n"+"Tester__________"+tester.toString());
					System.out.println("\n"+"others__________"+others.toString());
					return true;
				}
			}
		}
		return false;
	}
	
	
	public void generateNextGeneration(){
		int crossoverCount = (int) (crossoverProbability * presentGeneration.size());
		int mutationCount = presentGeneration.size() - crossoverCount;
		for(int i=0; i<crossoverCount; i++){
			
		}
		for(int i = 0; i<mutationCount; i++){
			
		}
	}
	
	public double avergaeFitness(){
		double sum =0;
		for(Chromosome each: presentGeneration){
			sum+=each.getFitness();
		}
		return sum/presentGeneration.size();
	}
	
	public static void main(String args[]){
//		int count = 0;
//		for (int i=0 ;i<100; i++){
//			System.out.println(i);
		Generation test = new Generation(5,10, 0.1);
		test.generateInitial();
		test.evaluateFitness();
//		if(test.checktest())
//			count++;
//		}
//		System.out.println(count);
	}
}

