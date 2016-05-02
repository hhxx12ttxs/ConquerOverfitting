package gdmachado.Inceptron;

import java.util.ArrayList;
import java.util.Stack;

public class MultilayerPerceptron
{
	//
	// Constructor
	//
	public MultilayerPerceptron(int[] topology, // {input, hidden, output}
			double learningRate,
			double bias,
			double value)
	{
		this.outputStack = new Stack<double[]>();
		this.layers = new ArrayList<Layer>();
		
		// Initialize input layer
		layers.add(new Layer(topology[0], 
				topology[1], 
				bias, 
				learningRate,
				value));

		// Initialize hidden layers
		if (topology.length > 2)
		{
			for(int i = 1; i < topology.length - 1; i++)
				layers.add(new Layer(topology[i], 
						topology[i+1], 
						bias, 
						learningRate,
						value));

		}

		// Initialize output layer
		layers.add(new Layer(topology[topology.length - 1], 
				topology[topology.length - 1], 
				bias, 
				learningRate,
				value));
		
		this.LAMBDA = this.layers.get(0).LAMBDA;
	}

	//
	// Constructor
	//
	public MultilayerPerceptron(int[] topology, // {input, hidden, ..., hidden, output}
			double learningRate,
			double bias,
			double lower,
			double upper)
	{
		this.outputStack = new Stack<double[]>();
		this.layers = new ArrayList<Layer>();
		
		// Initialize input layer
		layers.add(new Layer(topology[0], 
				topology[0], 
				bias, 
				learningRate,
				lower,
				upper));

		// Initialize hidden layers
		if (topology.length > 2)
		{
			for(int i = 0; i < topology.length - 2; i++)
				layers.add(new Layer(topology[i], 
						topology[i+1], 
						bias, 
						learningRate,
						lower,
						upper));

		}

		// Initialize output layer
		layers.add(new Layer(topology[topology.length - 2], 
				topology[topology.length - 1], 
				bias, 
				learningRate,
				lower,
				upper));
		
		this.LAMBDA = this.layers.get(0).LAMBDA;
	}
	
	//
	// think
	// Uses a stack to feed input forward, to get the network output
	//
	public double[] think(double[] data)
	{
		double[] result = null;
		
		outputStack.push(layers.get(0).think(data));
		result = outputStack.peek();
		for(int i = 1; i < layers.size(); i++)
		{
			outputStack.push(layers.get(i).think(result));
			result = outputStack.peek();
		}
		return result;
	}
	
	//
	// updateWeights
	// This method executes a batch of the backpropagation algorithm, given output layer's delta and output
	// perhaps it should be implemented inside the Teacher class instead...
	//
	protected void updateWeights(double[] delta, double[] data) {
		// remove network output from stack (we're only interested in the hidden layers' output)
		outputStack.pop();
		
		// update weights of output layer (using the first hidden layer's output)
		layers.get(layers.size()-1).updateWeights(delta, outputStack.pop());
		
		// update weights of hidden layers
		for(int i = layers.size()-2; i > -1; i--)
		{
			// get result for current layer
			double[] currentResult = data;
			
			// get weights for current layer
			double[] currentWeights = layers.get(i).getWeights()[i];
			
			// calculate deltas for current layer
			double[] currentDeltas = new double[currentResult.length];
			for(int j = 0; j < currentResult.length; j++)
				currentDeltas[j] = MathUtils.getHiddenDelta(currentResult[j], currentWeights, delta, LAMBDA);
			
			// update weights for current layer
			layers.get(i).updateWeights(currentDeltas, currentResult);
		}
		
		/*// update weights of input layer
		double[] currentResult = outputStack.pop();
		double[][] currentWeights = layers.get(1).getWeights();
		double[] currentDeltas = new double[currentResult.length];
		for(int j = 0; j < currentResult.length; j++)
			currentDeltas[j] = MathUtils.getHiddenDelta(currentResult[j], new double[]{currentWeights[0][j],currentWeights[1][j]}, delta, LAMBDA);
		layers.get(0).updateWeights(currentDeltas, data);*/
	}
	
	//
	// Returns an array containing all weights and bias
	//
	public double[] getIndividual()
	{
		int totalBias = 0;
		int totalWeights = (int)Math.pow(layers.get(0).neurons.size(), 2);
		for(int i = 0; i < layers.size()-1; i++) totalWeights += layers.get(i).neurons.size() * layers.get(i+1).neurons.size();
		for(int j = 0; j < layers.size(); j++) totalBias += layers.get(j).neurons.size();
		//int totalBias = layers.get(0).neurons.size() + layers.get(1).neurons.size(); 
		int l = 0;
		double[] array = new double[totalWeights + totalBias];
		for(int i = 0; i < layers.size(); i++)
		{
			for(int j = 0; j < layers.get(i).neurons.size(); j++)
			{
				for(int k = 0; k < layers.get(i).neurons.get(j).Weights.length; k++)
				{
					array[l++] = layers.get(i).neurons.get(j).Weights[k];
				}
			}
		}
		for(int m = 0; m < layers.size(); m++)
		{
			for(int n = 0; n < layers.get(m).neurons.size(); n++)
			{
				array[l++] = layers.get(m).neurons.get(n).Bias;
			}
		}
		return array;
	}
	
	public void setIndividual(double[] individual)
	{
		// Weights
		int l = 0;
		for(int i = 0; i < layers.size(); i++)
		{
			for(int j = 0; j < layers.get(i).neurons.size(); j++)
			{
				for(int k = 0; k < layers.get(i).neurons.get(j).Weights.length; k++)
				{
					layers.get(i).neurons.get(j).Weights[k] = individual[l++];
				}
			}
		}
		
		// Bias
		for(int m = 0; m < layers.size(); m++)
		{
			for(int n = 0; n < layers.get(m).neurons.size(); n++)
			{
				layers.get(m).neurons.get(n).Bias = individual[l++];
			}
		}
	}
	
	

	//
	// Attributes
	//
	ArrayList<Layer> layers;
	Stack<double[]> outputStack;
	public double LAMBDA;
}

