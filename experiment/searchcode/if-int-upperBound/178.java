package abc;

import neuralnetwork.NeuralNetwork;
import neuralnetwork.TrainingSet;



public class ABC{
	private int MAX_CYCLE;
	private int COLONY_SIZE;
	private int FOOD_SIZE;
	private int PARAMETERS;
	private int LIMIT;
	private int LOWERBOUND;
	private int UPPERBOUND;

	private double[][] food;

	private double[] functionValue;
	private double[] fitnessValue;
	private double[] probability;
	private double[] bestFood;
	private int[] trial;

	private double bestFunctionValue;
	private double delta;
	private double bestFitValue;

	private NeuralNetwork network;
	private TrainingSet trainingSet;
	public boolean end;

	public ABC(NeuralNetwork network,TrainingSet trainingSet, int maxCycle, int colonySize, int lowerBound,int upperBound) {
		this.network = network;
		this.trainingSet = trainingSet;
		MAX_CYCLE = maxCycle;
		COLONY_SIZE = colonySize;
		LOWERBOUND = lowerBound;
		UPPERBOUND = upperBound;
		PARAMETERS = network.getParameterSize();
		init();
	}

	private void init() {
		FOOD_SIZE = COLONY_SIZE / 2;
		LIMIT = (FOOD_SIZE * PARAMETERS);
		food = new double[FOOD_SIZE][PARAMETERS];
		functionValue = new double[FOOD_SIZE];
		fitnessValue = new double[FOOD_SIZE];
		probability = new double[FOOD_SIZE];
		trial = new int[FOOD_SIZE];
		bestFood = new double[PARAMETERS];
	}

	public void initializeABC() {
		for (int i = 0; i < FOOD_SIZE; i++) {
			generateFoodSource(i);
		}
		
		bestFunctionValue = functionValue[0];
		//bestFood = Arrays.copyOf(food[0], food[0].length);
		/*for(int i=0;i<food[0].length;i++)
			bestFood[i] = food[0][i];*/
		bestFood = food[0].clone();
	}

	private void generateFoodSource(int i) {
		for (int j = 0; j < PARAMETERS; j++) {
			food[i][j] = LOWERBOUND + (random() * (UPPERBOUND - LOWERBOUND));
		}
		
		functionValue[i] = calculateFunctionValue(food[i]);
		fitnessValue[i] = calculateFitnessValue(functionValue[i]);
		trial[i] = 0;
	}

	private double calculateFunctionValue(double[] temp) {
		network.setWeights(temp);
		double err = network.rmsError(trainingSet);
		
		return err;
	}

	private double calculateFitnessValue(double functionValue) {
		if (functionValue > 0)
			return 1 / (1 + Math.abs(functionValue));
		else
			return 1 + Math.abs(functionValue);
	}

	public void sendEmployedBees() {
		for (int i = 0; i < FOOD_SIZE; i++) {
			greedyAlgorithm(i, false);
		}
	}

	private void greedyAlgorithm(int i, boolean strict) {
		int newParam;
		int neighbour;
		double[] currentFood = new double[food[0].length];
		double curFunctionValue;
		double curFitValue;
		
		newParam = (int) Math.round(random() * (PARAMETERS-1));
		neighbour = (int) Math.round(random() * (FOOD_SIZE-1));
		while (neighbour == i)
			neighbour = (int) Math.round(random() * (FOOD_SIZE-1));
		delta = -1 +(random() *2);
				
		//currentFood = Arrays.copyOf(food[i], food[0].length);
		/*for(int x=0;x<food[0].length;x++)
			currentFood[x] = food[i][x];*/
		currentFood = food[i].clone();

		currentFood[newParam] = food[i][newParam] + delta
				* (food[i][newParam] - food[neighbour][newParam]);

		if (currentFood[newParam] < LOWERBOUND)
			currentFood[newParam] = LOWERBOUND;
		else if (currentFood[newParam] > UPPERBOUND)
			currentFood[newParam] = UPPERBOUND;
		
		curFunctionValue = calculateFunctionValue(currentFood);
		curFitValue = calculateFitnessValue(curFunctionValue);

		if (curFitValue >= fitnessValue[i]) {
			trial[i] = 0;
			//food[i] = Arrays.copyOf(currentFood, currentFood.length);
			/*for(int x=0;x<food[0].length;x++)
				food[i][x] = currentFood[x];*/
			food[i] = currentFood.clone();
			functionValue[i] = curFunctionValue;
			fitnessValue[i] = curFitValue;
		} else
			trial[i]++;
	}

	public void calculateProbabilities() {
		bestFitValue = 0;
		for (int i = 0; i < FOOD_SIZE; i++)
				bestFitValue += fitnessValue[i];

		for (int i = 0; i < FOOD_SIZE; i++)
			probability[i] = fitnessValue[i] / bestFitValue;
	}

	public void sendOnlookerBees() {
		double newProbability;
		for (int i = 0, ctr = 0; i < FOOD_SIZE;) {
			newProbability = random();
			if (newProbability < probability[ctr]) {
				i++;
				greedyAlgorithm(ctr, true);
			}
			ctr++;
			if (ctr == FOOD_SIZE - 1)
				ctr = 0;
		}
	}

	public void memorizeBestSource() {
		for (int i = 0; i < FOOD_SIZE; i++) {
			if (functionValue[i] < bestFunctionValue) {
				bestFunctionValue = functionValue[i];
				/*for(int x=0;x<food[0].length;x++)
					bestFood[x] = food[i][x];*/
				bestFood = food[i].clone();
				if(bestFunctionValue==0)
					end = true;
			}
		}
	}

	public void sendScoutBees() {
		int maxTrial = 0;
		for (int i = 0; i < FOOD_SIZE; i++)
			if (trial[i] > maxTrial)
				maxTrial = i;

		if (trial[maxTrial] >= LIMIT) {
			generateFoodSource(maxTrial);
		}
	}

	public double[] getBestFood() {
		return bestFood;
	}

	public double random() {
		return (Math.random() * 32767 / ((double) (32767) + (double) (1)));
		//return Math.random();
	}

	public void start() {
		initializeABC();
		memorizeBestSource();
		int  cycles;
		for (cycles= 0; cycles < MAX_CYCLE&&!end; cycles++) {
			sendEmployedBees();
			calculateProbabilities();
			sendOnlookerBees();
			memorizeBestSource();
			sendScoutBees();
		}
		
		System.out.println("input =" +network.getInputCount() +"   hidden = " +network.getHiddenCount());
		System.out.println("ABC results: minError = "+bestFunctionValue);
		System.out.println(cycles +" "+MAX_CYCLE);
	} 
	
}

