* Neuron class that models a perceptron.
*
*/
public class Neuron
{
double[] weights;
double[] input;
double[] oldDelta; // for use with momentum
input = new double[inputSize + 1];
oldDelta = new double[inputSize + 1];
nextLevel = nl;
learningNumber = learningRate;

