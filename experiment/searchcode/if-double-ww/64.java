package org.darchitect.nn.som;

/**
 * Self Organized Map implementation
 * @author Guillermo Sandoval [darchitect@gmail.com]
 */
public class SOM {
	
	/**
	 * Number of iterations needed in the training process
	 * */
	private final int ITERATION_NUMBER;
	
	/**
	 * Sets neighborhood radius at time zero by a factor of d0tFactor of the lattice width
	 * */
	private double d0tFactor = 0.5;
	
	/**
	 * Neighborhood radius at time zero
	 * */
	private double d0t;
	
	/**
	 * Learning rate at time zero
	 * */
	private double a0t = 0.1;
	
	/**
	 * A.K.A. Kohonen Layer
	 * */
	private Neuron lattice[][];
	
	/**
	 * Denotes a constant dependent on the number of iterations (time constant)
	 * */
	private double lambda;
	
	private boolean trained;
	
	public SOM(int kohonenLayerWidth, int inputVectorSize) {
		this(kohonenLayerWidth, inputVectorSize, null);
	}
	
	public SOM(int kohonenLayerWidth, int inputVectorSize, double weights[][][]) {
		this(kohonenLayerWidth, inputVectorSize, .5, .1, 100, weights);
	}
	
	public SOM(int kohonenLayerWidth, int inputVectorSize, double d0tFactor, double a0t, int iterations, double weights[][][]) {
		this.d0tFactor = d0tFactor;
		this.a0t = a0t;
		lattice = new Neuron[kohonenLayerWidth][kohonenLayerWidth];
		if (weights == null)
			initLattice(generateRandomWeights(kohonenLayerWidth, kohonenLayerWidth, inputVectorSize));
		else
			initLattice(weights);
		
		ITERATION_NUMBER = iterations;
		d0t = kohonenLayerWidth * this.d0tFactor;
		lambda = ITERATION_NUMBER / Math.log(d0t);
		
		trained = true;
	}
	
	/**
	 * Initializes the Kohonen layer by setting the node weights to w
	 * @param w Neuron weights net
	 */
	private void initLattice(double w[][][]) {
		for (int i = 0; i < lattice.length; i++)
			for (int j = 0; j < lattice.length; j++)
				lattice[i][j] = new Neuron(w[i][j]);
	}
	
	/**
	 * Generates a vector weight matrix initialized with random values 
	 * @param w Layer width
	 * @param h Layer height
	 * @param s Weight vector length
	 * @return
	 */
	private double[][][] generateRandomWeights(int w, int h, int s) {
		double ww[][][] = new double[w][h][s];
		for (int i = 0; i < w; i++) 
			for (int j = 0; j < h; j++)
				for (int k = 0; k < s; k++)
					ww[i][j][k] = Math.random() - .5;
		return ww;
	}
	
	/**
	 * Retrieves the Kohonen layer weight vectors
	 * @return
	 */
	public double[][][] getWeights() {
		int vectorSize = lattice[0][0].v.length;
		double w[][][] = new double[lattice.length][lattice.length][vectorSize];
		for (int i = 0; i < w.length; i++)
			for (int j = 0; j < w.length; j++)
				for (int k = 0; k < vectorSize; k++)
					w[i][j] = lattice[i][j].v;
		return w;
	}
	
	/**
	 * Trains the neural network by selecting weight vectors from the training 
	 * data passed as a parameter
	 * @param vvs Training data. An array of training weight vectors. 
	 * The first dimension represents each of the vectors, the second the vectors themselves
	 */
	public void train(double vvs[][]) {
		int t = 0;
		int N = lattice.length;
		int bmu, i, j, ran;
		Neuron nn;
		double neighborhood, learningRate, neighborhoodRadius, dist;
		while (t++ < ITERATION_NUMBER) {
			ran = random(vvs.length);
			bmu = findBMU(vvs[ran]);
			neighborhoodRadius = neighborhoodRadius(t);
			learningRate = learningRate(t);
			for (i = 0; i < N; i++) {
				for (j = 0; j < N; j++) {
					nn = lattice[i][j];
					dist = distance(bmu / N, bmu % N, i, j);
					if (dist < neighborhoodRadius) {
						neighborhood = neighborhoodFunction(dist, neighborhoodRadius);
						nn.adjustWeights(vvs[ran], learningRate, neighborhood);
					}
				}
			}
		}
		trained = true;
	}
	
	public boolean hasBeenTrained() {
		return trained;
	}
	
	/**
	 * Calculates the value of the neighborhood function
	 * @param dist Distance from the node to the BMU
	 * @param r Neighborhood radius
	 * @return
	 */
	private double neighborhoodFunction(double dist, double r) {
		return Math.exp(- (dist * dist) / (2 * r));
	}
	
	/**
	 * Calculates the neighborhood radius
	 * @param t Current iteration number
	 * @return
	 */
	private double neighborhoodRadius(int t) {
		return d0t * Math.exp(-t / lambda);
	}
	
	/**
	 * Calculates the learning rate
	 * @param t Current iteration number
	 * @return
	 */
	private double learningRate(int t) {
		return a0t * Math.exp(-t / lambda);
	}
	
	/**
	 * Calculates hex distance between two nodes on a rectangular grid. 
	 * Converting to the simpler coordinate system and calculate distance
	 * in that. You convert by subtracting half the y coordinate (rounded
	 * down) from the x coordinate.
	 * */
	private double distance(int x1, int y1, int x2, int y2) {
		return hexCoordinatesDistance(x1 - y1 / 2, y1, x2 - y2 / 2, y2);
	}
	
	/**
	 * Calculates distance between two nodes on an hex grid where both x and y 
	 * correspond to straight lines of hexes, i.e., letting x increase 
	 * (with constant y) by going right and y increase (with constant x) by 
	 * going 60 degrees down from right (assuming (0,0) is top left corner).
	 * This way, if you move in one of the three "natural" directions, you 
	 * either have constant x, constant y or constant (x+y).
	 * */
	private double hexCoordinatesDistance(int x1, int y1, int x2, int y2) {
		if (x1 > x2) 
			return hexCoordinatesDistance(x2, y2, x1, y1);
		else if (y2 >= y1)
			return x2 - x1 + y2 - y1;
		else
			return Math.max(x2 - x1, y1 - y2);
	}
	
	/**
	 * Completes the input vector with the more similar node found in the Kohonen Layer
	 * @param inputVector Elements of the array to be completed must be equal to Double.MAX_VALUE  
	 * @return
	 */
	public double[] query(double inputVector[]) {
		int N = lattice.length;
		int bb = findBMU(inputVector);
		return lattice[bb / N][bb % N].v;
	}
	
	/**
	 * Retrieves BMU position in the Kohonen layer
	 * @param inputVector Input signal
	 * @return
	 */
	private int findBMU(double inputVector[]) {
		int bmu = 0;
		double minDist = 1 << 28;
		double dist;
		for (int i = 0; i < lattice.length; i++) {
			for (int j = 0; j < lattice[i].length; j++) {
				dist = lattice[i][j].dist(inputVector);
				if (minDist > dist) {
					minDist = dist;
					bmu = i * lattice.length + j;
				}
			}
		}
		return bmu;
	}
	
	/**
	 * Generates a random number between 0 and n-1
	 * @param n
	 * @return
	 */
	private int random(int n) {
		return (int)(Math.random() * n);
	}
	
	/**
	 * A SOM's neuron representation
	 * @author Guillermo Sandoval
	 */
	static class Neuron {
		
		/**
		 * Neuron's weight vector
		 */
		private double v[];
		/**
		 * Neuron's label (for showing off purposes, nothing else)
		 */
		private String label;
		
		private Neuron(double v[], String label) {
			this.v = v;
			this.label = label;
		}
		
		private Neuron(double vv[]) {
			this(vv, null);
		}
		
		public String getLabel() {
			return label == null ? "" : label;
		}
		
		/**
		 * Calculates the euclidian distance between the weight vector of this neuron 
		 * and the weight vector received as parameter
		 * @param vv weight vector
		 * @return
		 */
		private double dist(double vv[]) {
			double dist = 0;
			for (int k = 0; k < vv.length; k++) {
				if (vv[k] != Double.MAX_VALUE)
					dist += (vv[k] - v[k]) * (vv[k] - v[k]);
			}
			return Math.sqrt(dist);
		}
		
		/**
		 * Adjusts the weight vector according to an input signal
		 * @param vv Input signal
		 * @param learningRate Learning rate
		 * @param influence Neighborhood function value
		 */
		private void adjustWeights(double vv[], double learningRate, double influence) {
			v = sumVector(v, scaleVector(influence * learningRate, substractVector(vv, v)));
		}
		
		private double[] sumVector(double x[], double y[]) {
			double z[] = new double[x.length];
			for (int i = 0; i < x.length; i++)
				z[i] = x[i] + y[i];
			return z;
		}
		
		private double[] scaleVector(double c, double x[]) {
			double y[] = new double[x.length];
			for (int i = 0; i < x.length; i++)
				y[i] = x[i] * c;
			return y;
		}
		
		private double[] substractVector(double x[], double y[]) {
			double z[] = new double[x.length];
			for (int i = 0; i < x.length; i++) 
				z[i] = x[i] - y[i];
			return z;
		}
	
	}
	
}

