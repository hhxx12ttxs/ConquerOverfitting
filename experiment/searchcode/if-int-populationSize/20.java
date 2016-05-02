/*  Author: Rob Stewart
 *  Edited: Hans-Nikolai Viessmann
 *  Date: 21.11.2012
 *  About: A port of RailSearch-ga.c
 */

package com.hans.ga;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;

public class GACycle {

	/*******************************************************/
	/*   This is a simple GA solving a TSP problem, which  */
	/*   read in as a distance matrix.                     */
	/*                                                     */
	/*   Here are the parameter settings, hopefully        */
	/*   most are self-explanatory                         */
	/*                                                     */
	/*   NB: this code has a fixed random seed and so it   */
	/*   will always give the same results for the same    */
	/*   problem;  change the random seed (below) and      */
	/*   recompile to give another independent trial.       */
	/*   Normally, you should do several (e.g. 20) runs    */
	/*   with different random seeds, and look at the mean */
	/*   and standard deviation of the results.            */
	/*******************************************************/

	private static int MAX_CITIES = 100;
	private static int MAX_POPULATION = 100;
	private static int populationSize = 100;
	private static int elitism = 1;
	private static int maxGenerations = 20;

	/*******************************************************/
	/*  Data structures for holding information about the   */
	/*  problem being solved.   In this case, the problem  */
	/*  is defined only by a distance matrix.              */
	/*                                                     */
	/*  distance[x][y]  gives the distance from x to y     */
	/*                                                     */
	/*  NB:  you may be thinking of the cities as          */
	/*       A, B, ..., L,  or as  1, 2, ..., 12           */
	/*       but this code thinks of them as               */
	/*       0, 1, 2, ..., 11                              */
	/*******************************************************/

	private static int numberOfCities = 9;
	private static String[] cityName = {
		"Edinburgh",
		"Aberdeen",
		"Ayr",
		"Fort Williams",
		"Glasgow",
		"Inverness",
		"StAndrews",
		"Stirling"
	};
	private static double[][] distance;

	/******************************************************/
	/* Data structures used by the GA;  mainly to hold the */
	/* population and to do bookkeeping                   */
	/*                                                    */
	/* The 7th city (or 'gene') in the 5th candidate      */
	/* solution (or 'chromosome') in the current          */
	/* population will be                                 */
	/*     current_population[4][6]                       */
	/*                                                    */
	/* The fitness of the 5th candidate solution will be  */ 
	/* stored in  fitness[4]                              */ 
	/******************************************************/

	private static int[][] currentPopulation;
	private static int[][] intermediatePopulation;
	private static double[] fitness;
	private static int bestSolutionSoFar;
	private static double bestFitnessSoFar;
	private static int[] referenceSolution;

	/*******************************************************/
	/* The main loop.  For simplicity and ease of reading  */
	/* this code, the program takes no arguments.          */
	/*                                                     */
	/* Parameters and similar are set up above, and can    */
	/* of course be edited, but you must then remember to  */
	/* recompile the code!                                 */
	/*******************************************************/
	public static void main(String[] s) {

		if(s.length != 1){
			System.out.println("Missing inputs, please enter in location of distances.txt");
			System.exit(1);
		}

		int generation;

		distance = new double[MAX_CITIES][MAX_CITIES];
		currentPopulation = new int[MAX_POPULATION][MAX_CITIES];
		intermediatePopulation = new int[MAX_POPULATION][MAX_CITIES];
		fitness = new double[MAX_POPULATION];
		referenceSolution = new int[MAX_CITIES];

		readTheDistanceMatrix(s[0]);

		initialiseThePopulation();

		System.out.println("Evaluating the Population:");
		System.out.println("generation " + 0);
		evaluateThePopulation();

		generation = 1;

		while (generation < maxGenerations) {
			System.out.println("generation " + generation);
			produceTheNextGeneration();
			evaluateThePopulation();
			generation++;
		}
	}

	private static void initialiseThePopulation() {

		int chromosome, i, thisCity, refLength;

		// this is a good place to initialise the "best so far" fitness,
		// set to 0, so that the first evaluated fitness is
		// sure to become best so far to begin with.
		bestFitnessSoFar = 0.0;

		for (chromosome = 0; chromosome < populationSize; chromosome++) {
			// we will now efficiently generate a random permutation of 
			// the integers  0, 1, ..., N-1  where N is number_of_cities

			// this starts with a 'reference' permutation that is simply
			// 0, 1, 2, ..., N-1

			for (i = 0; i < numberOfCities; i++) {
				referenceSolution[i] = i;
			}

			// ref_length gives the current length of the ref solution; it 
			// will gradually reduce
			refLength = numberOfCities - 1;

			// now repeatedly take random genes from the reference population
			// and build the current chromosome
			for (i = 0; i < numberOfCities; i++) {
				// this_city = rand() % ref_length;
				if (i == 0 || i == numberOfCities-1 ) {
					// Edinburgh is always the first or last position in the array
					currentPopulation[chromosome][i] = 0;
				}
				else{
					thisCity = getRandomNumberBetween(0, refLength);
					currentPopulation[chromosome][i] = referenceSolution[thisCity];
					referenceSolution[thisCity] = referenceSolution[refLength - 1];
					refLength--;
				}
			}
		}
	}

	/****************************************************/
	/* the code assumes that there is a distance matrix */
	/* in a file called "distances.txt"                 */
	/****************************************************/
	private static void readTheDistanceMatrix(String filePath) {

		int city1, city2;
		BufferedReader readbuffer = null;
		String strRead;
		String splitarray[];
		double inputNumberDouble;

		try {
			System.out.println("Reading Cities from "+filePath);
			readbuffer = new BufferedReader(new FileReader(filePath));
			for (city1 = 0; city1 < numberOfCities - 1; city1++) {
				strRead = readbuffer.readLine();
				splitarray = strRead.split("\t");
				for (city2 = 0; city2 < numberOfCities - 1; city2++) {
					inputNumberDouble = Double.parseDouble(splitarray[city2]);
					distance[city1][city2] = inputNumberDouble;
				}
				//System.out.println("Read in city " + city1 + ": " + cityName[city1]);
			}
		} catch (Exception e) {
			System.out.println(e);
			System.exit(0);
		}
	}

	private static void evaluateThePopulation() {
		int chromosome, city, city1, city2;
		double total_distance;

		for (chromosome = 0; chromosome < populationSize; chromosome++) {
			// evaluate this chromosome;

			total_distance = 0;

			for (city = 0; city < numberOfCities; city++) {
				city1 = currentPopulation[chromosome][city];
				city2 = currentPopulation[chromosome][(city + 1) % numberOfCities];
				total_distance += distance[city1][city2];
			}
			if (total_distance == 0) {
				System.out.println("Something funny is going on - no solution should have a tour distance of 0");
				System.exit(0);
			}

			// note that we are using Roulette Wheel selection in this GA. So, although we
			// are trying to minimise the TSP tour distance, fitness needs to be something
			// that we are trying to *maximise*. So, we take 1/distance to be fitness:
			fitness[chromosome] = 1.0 / total_distance;

			// now some book-keeping to keep track of best so far
			if (fitness[chromosome] >= bestFitnessSoFar) {
				bestFitnessSoFar = fitness[chromosome];
				bestSolutionSoFar = chromosome;
			}
		}

		// print the population and fitnesses
		for (chromosome = 0; chromosome < populationSize; chromosome++) {
			if (chromosome == bestSolutionSoFar) {
				System.out.println("best so far: ");
				/*for (city1 = 0; city1 < numberOfCities; city1++) {
					//System.out.print(currentPopulation[chromosome][city1] + " ");
					System.out.printf("\t%s, ", cityName[currentPopulation[chromosome][city1]]);
				}
				System.out.println("- " + fitness[chromosome] + " (distance: " + Math.round(1.0 / fitness[chromosome]) + ")");
				*/
			}

			for (city1 = 0; city1 < numberOfCities; city1++) {
				//System.out.print(currentPopulation[chromosome][city1] + " ");
				System.out.printf("%14s, ", cityName[currentPopulation[chromosome][city1]]);
			}

			System.out.println("- " + fitness[chromosome] + " (distance: " + Math.round(1.0 / fitness[chromosome]) + ")");

		}
		System.out.println();
	}

	/************************************************************/
	/*  There are various ways to do crossover of permutations. */
	/*  They tend to be fiddly - it's easy to get your i's and  */
	/*  j's mixed up.  This code implements 'position-based'    */
	/*  crossover.   First, select some random gene positions   */
	/*  from parent1.                                           */
	/*                                                          */
	/*  e.g. parent1 might be  "3 7 1 2 4 6 5", and the rand    */
	/*  positions might be the 2nd, 3rd and 5th.                */
	/*                                                          */
	/*  Then, start building the child by making it the same    */
	/*  as parent1 in those positions.  So, the child starts    */
	/*  out in this case as:  "x 7 1 x 4 x x", where "x" means  */
	/*  we haven't decided what goes there yet.                 */
	/*                                                          */
	/*  Now, suppose parent2 is "3 4 5 1 2 6 7" -- we complete  */
	/*  the child by filling in the remaining cities            */
	/*  (in this case 2, 3, 5 and 6) in the same order that they*/
	/*  are in parent2.  So the child becomes: "3 7 1 5 4 2 6"  */
	/***********************************************************/
	private static int[] crossover(int parent1, int parent2, int[] child) {
		int[] genes_present, chosen;
		genes_present = new int[MAX_CITIES];
		chosen = new int[MAX_CITIES];
		int i, thisgene, place;

		int rnd = getRandomNumberBetween(0, 100);
		for (i = 0; i < numberOfCities; i++) {
			if (rnd <= 50) {
				chosen[i] = 1;
			} else {
				chosen[i] = 0;
			}
		}

		// set child to be same as parent1 in these positions
		for (i = 0; i < numberOfCities; i++) {
			if (chosen[i] == 1) {
				child[i] = currentPopulation[parent1][i];
			}
		}

		// now record the genes that the child already has
		// (first need to initialise this array)
		for (i = 0; i < numberOfCities; i++) {
			genes_present[i] = 0;
		}
		for (i = 0; i < numberOfCities; i++) {
			if (chosen[i] == 1) {
				genes_present[child[i]] = 1;
			}
		}

		// now collect the remaining genes from parent2
		for (i = 0; i < numberOfCities; i++) {
			thisgene = currentPopulation[parent2][i];
			if (genes_present[thisgene] == 0) { //place it in the next unchosen position of the child
				place = 0;
				while (chosen[place] == 1) {
					place++;
				}
				child[place] = thisgene;
				genes_present[thisgene] = 1;
				chosen[place] = 1;
			}
		}
		return child;
	}

	private static void produceTheNextGeneration() {

		int newCandidate, parent1, parent2, gene;
		int child[] = new int[MAX_CITIES];

		if (elitism == 1) {
			for (gene = 0; gene < numberOfCities; gene++) {
				intermediatePopulation[0][gene] = currentPopulation[bestSolutionSoFar][gene];
			}
		}

		for (newCandidate = elitism; newCandidate < populationSize; newCandidate++) {
			parent1 = rouletteWheelSelect();
			parent2 = rouletteWheelSelect();
			child = crossover(parent1, parent2, child);
			child = mutate(child);

			for (gene = 0; gene < numberOfCities; gene++) {
				intermediatePopulation[newCandidate][gene] = child[gene];
			}
		}

		for (newCandidate = 0; newCandidate < populationSize; newCandidate++) {
			for (gene = 0; gene < numberOfCities; gene++) {
				currentPopulation[newCandidate][gene] = intermediatePopulation[newCandidate][gene];
			}
		}
	}

	/**************************************************************/
	/*  There are several possible mutation methods that make     */
	/*  sure you still get a permutation.    This is one of the   */
	/*  the simplest.  Take any two adjacent genes, and swap them */
	/**************************************************************/
	private static int[] mutate(int[] child) {
		int g = getRandomNumberBetween(1, numberOfCities - 2);
		int temp = child[g];
		child[g] = child[g + 1];
		child[g + 1] = temp;
		return child;
	}

	private static int rouletteWheelSelect() {
		double fitTotal, pointer, accumulatingFitness, randReal;
		int chromosome, randint, selected = 0;

		fitTotal = 0.0;
		for (chromosome = 0; chromosome < populationSize; chromosome++) {
			fitTotal += fitness[chromosome];
		}

		randint = getRandomNumberFrom(0, 1000000);
		randReal = randint / 1000000.0;
		pointer = fitTotal * randReal;
		accumulatingFitness = 0.0;

		while (selected < populationSize) {
			accumulatingFitness += fitness[selected];
			if (pointer < accumulatingFitness) {
				break;
			}

			if (selected != populationSize - 1) {
				selected++;
			}
		}
		return selected;
	}

	private static int getRandomNumberBetween(int min, int max) {
		Random foo = new Random();
		int randomNumber = foo.nextInt(max - min) + min;
		if (randomNumber == min) {
			// Since the random number is between the min and max values, simply add 1
			return min + 1;
		} else {
			return randomNumber;
		}
	}

	public static int getRandomNumberFrom(int min, int max) {
		Random foo = new Random();
		int randomNumber = foo.nextInt((max + 1) - min) + min;
		return randomNumber;
	}
}
