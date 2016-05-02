package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import util.IndividualGeneratorUtil;
import util.SelectionUtil;

public class Environment {

	public static List<Item> items;
	
	private final int FIRST_GENERATION = 1;
	private ArrayList<Generation> generations;
	private Integer numberOfGenerations;
	private Generation currentGeneration;
	
	private Integer populationSize;
	private Integer chromossomeSize;
	private Double mutationChance;
	private Integer knapsackWeightLimit;
	private Random random;
	
	private Integer matingPoolSize;

	public Environment(Integer populationSize, Integer numberOfGenerations, Double mutationChance, Integer knapsackWeightLimit, List<Item> items) {

		Environment.items = items;
		
		this.generations = new ArrayList<Generation>();
		this.populationSize = populationSize;
		this.numberOfGenerations = numberOfGenerations;
		this.mutationChance = mutationChance;
		this.knapsackWeightLimit = knapsackWeightLimit;
		this.random = new Random();
		this.matingPoolSize = populationSize / 2;
		this.chromossomeSize = items.size();
	}

	public void generateNextGeneration() {

		if (isFirstGeneration()) {
			generations.add(Generation.getFirstGeneration(FIRST_GENERATION, populationSize, chromossomeSize, knapsackWeightLimit, random));
		} else {

			ArrayList<Individual> nextGenerationPopulation = new ArrayList<Individual>();

			for (int mateNumber = 0; mateNumber < matingPoolSize; mateNumber++) {

				Individual[] parents = selectParents(currentGeneration.getPopulation(), mateNumber);
				Individual[] sons = mate(parents, mateNumber);

				nextGenerationPopulation.add(sons[0]);
				nextGenerationPopulation.add(sons[1]);
			}

			generations.add(new Generation(currentGeneration.getGenerationNumber() + 1, populationSize, chromossomeSize, nextGenerationPopulation));

		}

		currentGeneration = getLatestGeneration();

	}

	private Individual[] selectParents(List<Individual> population, int mateNumber) {
		// Individual[] parents = { population.get(mateNumber),
		// population.get((mateNumber + 1) % matingPoolSize) };
		// Individual[] parents = { Selection.fittest(population,
		// matingPoolSize, random), Selection.fittest(population,
		// matingPoolSize, random) };
		Individual[] parents = { SelectionUtil.realTournamentSelection(population, random), SelectionUtil.realTournamentSelection(population, random) };
		// Individual[] parents = {
		// Selection.fitnessProportionalSelection(population, random),
		// Selection.fitnessProportionalSelection(population, random) };

		return parents;
	}

	private Individual[] mate(Individual[] parents, int mateNumber) {

		IndividualGeneratorUtil individualGenerator = new IndividualGeneratorUtil(chromossomeSize, random);
		Individual[] sons = individualGenerator.chrossOver(parents[0], parents[1]);

		sons[0].mutate(mutationChance, random);
		sons[0].setNumber(mateNumber * 2 + 1);

		sons[1].mutate(mutationChance, random);
		sons[1].setNumber(mateNumber * 2 + 2);

		return sons;
	}

	public Generation getGeneration(int index) {
		return generations.get(index - 1);
	}

	public int getNumberOfGenerations() {
		return numberOfGenerations;
	}

	private Generation getLatestGeneration() {
		return generations.get(generations.size() - 1);
	}

	private boolean isFirstGeneration() {
		return currentGeneration == null;
	}
}

