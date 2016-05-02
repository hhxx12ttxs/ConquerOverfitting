package model;

import java.util.List;


public class GeneticAlgorithm {

	private Environment environMent;

	public GeneticAlgorithm(Integer numberOfGenerations, Integer populationSize, Double mutationChance, Integer weightLimit, List<Item> items) {
		Knapsack.initializeKnapsack(items, weightLimit);
		environMent = new Environment(populationSize, numberOfGenerations, mutationChance, weightLimit, items);
	}

	public void start() {
		System.out.println("Generation\tBest\tGenotype\tValue ($)\tWeight (kg)");

		for (int generation = 1; generation <= environMent.getNumberOfGenerations(); generation++) {
			environMent.generateNextGeneration();

			//if (generation % 5 == 0 || generation == 1) {
				System.out.println(environMent.getGeneration(generation).toString());
			//}
		}
	}
}

