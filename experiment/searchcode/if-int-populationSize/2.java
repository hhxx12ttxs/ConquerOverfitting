public class Population {
private List<Chromosome> population;
private int populationSize;

public Population(int populationSize){
public Chromosome get(int indexOfChromosome){
if(indexOfChromosome < populationSize){
return population.get(indexOfChromosome);
}
return null;
}


}

