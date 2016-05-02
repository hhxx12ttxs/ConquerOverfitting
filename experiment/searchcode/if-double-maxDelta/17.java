package geneticAlgorithm;

import java.util.Random;

public class Chromosome {
	private Random random;
	
	/*
	 * ?????
	 */
	public double[] sequence;
	
	/*
	 * ?????
	 */
	private int length;
	
	/*
	 * ????????
	 */
	private double fitness;
	
	/*
	 * ????????
	 */
	private double maxDelta = 0.1;
	
	/*
	 * ???
	 */
	private double mutationRat = 0.1;
	
	/*
	 * ????
	 */
	public Chromosome(double[] initSequence, int initLength) {
		sequence = initSequence;
		length = initLength;
		random = new Random();
	}
	
	/*
	 * ?????????
	 */
	public void setMaxDelta(double newMaxDelta) {
		maxDelta = newMaxDelta;
	}
	
	/*
	 * ?????
	 */
	public void setMutationRat(double newMutationRat) {
		mutationRat = newMutationRat;
	}
	
	/*
	 * ??
	 */
	public void mutation() {
		for (int i = 0; i < length; i ++) {
			if (randomRat() < mutationRat) {
				sequence[i] += getRandomValue() * maxDelta;
			}
		}
	}
	
	/*
	 * ??????????
	 */
	public void setFitness(double newFitness) {
		fitness = newFitness;
	}
	
	/*
	 * ??????????
	 */
	public double getFitness() {
		return fitness;
	}
	
	/*
	 * ???????
	 */
	public int getLength() {
		return length;
	}

	/*
	 * ????[-1, 1]???
	 */
	private double getRandomValue() {
		return random.nextDouble() * 2 - 1;
	}
	
	/*
	 * ????[0, 1]???
	 */
	private double randomRat() {
		return random.nextDouble();
	}
}

