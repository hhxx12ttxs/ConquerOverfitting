package util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import model.Individual;
import model.Knapsack;

public class IndividualGeneratorUtil {

	private Integer chromossomeSize;
	private Random random;

	public IndividualGeneratorUtil(Integer chromossomeSize, Random random) {
		super();
		this.chromossomeSize = chromossomeSize;
		this.random = random;
	}

	public Individual[] chrossOver(Individual father, Individual mother) {

		Byte[] fatherX = father.getChromossome();
		Byte[] motherX = mother.getChromossome();

		Byte[] sonA = new Byte[chromossomeSize];
		Byte[] sonB = new Byte[chromossomeSize];

		Integer chrossOverCut = random.nextInt(chromossomeSize - 1) + 1;

		for (int index = 0; index < chromossomeSize; index++) {

			if (index < chrossOverCut) {

				sonA[index] = fatherX[index];
				sonB[index] = motherX[index];

			} else {

				sonA[index] = motherX[index];
				sonB[index] = fatherX[index];
			}

		}

		Individual[] sons = { new Individual(chromossomeSize, sonA), new Individual(chromossomeSize, sonB) };

		sons[0] = Knapsack.getInstance().isIndividualValid(sons[0]) ? sons[0] : father;
		sons[1] = Knapsack.getInstance().isIndividualValid(sons[1]) ? sons[1] : mother;
		

		return sons;

	}

	public List<Individual> getRandomPopulation(Integer populationSize) {
		ArrayList<Individual> population = new ArrayList<Individual>();
		for (int individualNumber = 0; individualNumber < populationSize; individualNumber++) {
			population.add(generateValidIndividual(individualNumber));
		}
		return population;
	}

	private Individual generateValidIndividual(int individualNumber) {

		Individual justBorn = null;
		while (!Knapsack.getInstance().isIndividualValid(justBorn)) {
			justBorn = generateRandomIndividual(individualNumber);
		}
		return justBorn;

	}

	private Individual generateRandomIndividual(int individualNumber) {
		Byte[] chromossome = new Byte[chromossomeSize];
		for (int i = 0; i < chromossomeSize; i++) {
			chromossome[i] = (byte) Math.round(random.nextDouble());
		}
		return new Individual(individualNumber, chromossome);
	}
}

