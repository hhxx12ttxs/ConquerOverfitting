package neuralSystems;

/**
 * This class is based off the Monarch Link class. It represents the weight
 * matrix between two adjacent layers of neurons, as well as methods for the
 * interaction between layers. The outputs of the source layer are inputs to
 * the destination layer during propagation.
 */
public abstract class Link {
	/** In feed-forward propagation, outputs of the sourceLayer become inputs
	 * to the destinationLayer.*/
	protected Layer destinationLayer;
	/** In feed-forward propagation, outputs of the sourceLayer become inputs
	 * to the destinationLayer.*/
	protected Layer sourceLayer;
	/** The weight matrix between layers of neurons. 0th index is the from index
	 * (from source layer) and the 1st index is the to index (to destination layer)*/
	protected double[][] weights;
	/** A store of the change in each weight. deltaWeights[i][j] is the a store
	 * of the change in the weight weights[i][j]*/
	protected double[][] deltaWeights;
	
	/**
	 * @return the destination layer of the link. The outputs of the source layer
	 * get propagated to the destination layer
	 */
	public Layer getDestinationLayer()
	{
		return destinationLayer;
	}
	
	/**
	 * @return the source layer of the link. The outputs of the source layer
	 * get propagated to the destination layer
	 */
	public Layer getSourceLayer()
	{
		return sourceLayer;
	}
	
	/**
	 * Randomizes each weight in the internal weight matrix between lowerBound
	 * and upperBound.
	 * @param lowerBound The lowest number a weight can have.
	 * @param upperBound Just above the highest number a weight can have.
	 */
	public void randomizeWeights(double lowerBound, double upperBound)
	{
		if (upperBound < lowerBound)
			throw new IllegalArgumentException("The lowerBound must not exceed the " +
					"upperBound of the random weights. The numbers were " +
					lowerBound + " and " + upperBound + ", respectively.");
		// seed the random number generator with the current time.
		java.util.Random generator = new java.util.Random();
		
		for (int fromIndex = 0; fromIndex < weights.length; fromIndex++) {
			for (int toIndex = 0; toIndex < weights[fromIndex].length; toIndex++) {
				weights[fromIndex][toIndex] 
				                   = generator.nextDouble()*(upperBound-lowerBound) + 
				                   		lowerBound;
			}
		}
	}
	
	/**
	 * Calculates the outputs of the destination layer from the outputs of 
	 * the source layer
	 */
	public void propagate()
	{
		double netTerm;
		
		for (int toIndex = 1; toIndex <= destinationLayer.numberOfUnits(); toIndex++) {
			netTerm = 0;
			for (int fromIndex = 0; fromIndex <= sourceLayer.numberOfUnits(); fromIndex++) {
				netTerm += weights[fromIndex][toIndex]*sourceLayer.outputs[fromIndex];
			}
			destinationLayer.outputs[toIndex] = destinationLayer.transferFunction(netTerm);
		}
//		{
//			real sum;
//
//
//			for (integer i = 1; i <= upper->numberUnits(); i++) 
//				{
//				sum = 0.0;
//				for (integer j = 0; j <= lower->numberUnits(); j++)
//					{
//					sum += ((*weights)[i][j]) * (*(lower->output))[j] ;
//					}
//				upper->output->addAt(i, lower->f(sum)); //? shouldn't this be upper->f(sum)
//				}
//			}
	}
	/**
	 * Propagates errors backward through the link (this should be called after
	 * computeError())
	 * @see computeError()
	 */
	public abstract void backpropagate();
	/**
	 * Adjusts the weight matrices. This should often be called after backpropagate()
	 * and computeError()
	 * @see backpropagate()
	 */
	public abstract void adjust();
	/**
	 * Computes the internal error of the destination layer neuron outputs
	 * based on the given target outputs
	 * @param target The outputs desired of the destination layer's outputs
	 * @return an error value as a function of the neurons' errors
	 */
	public abstract double computeError(double[] target);
	/**
	 * Resets the link for a new trial.
	 */
	public abstract void newTrial();
	/**
	 * Constructs a new link between two adjacent layers of neurons, where during
	 * feed-forward propagation the outputs of the source layer become the inputs
	 * of the destination layer.
	 * @param sourceLayer The outputs of the neurons in this layer are connected
	 * to the inputs of the destination layer.
	 * @param destinationLayer The inputs of the neurons in this layer are connected
	 * to the outputs of the source layer.
	 */
	public Link(Layer sourceLayer, Layer destinationLayer)
	{
		this.sourceLayer = sourceLayer;
		this.destinationLayer = destinationLayer;
		// The +1 in the length of the dimensions of the weights account for the
		// bias element in the layers
		this.weights = new double[sourceLayer.numberOfUnits()+1][destinationLayer.numberOfUnits()+1];
		this.deltaWeights = new double[this.weights.length][this.weights[0].length];
		randomizeWeights(-0.5, 0.5);
	}
}

