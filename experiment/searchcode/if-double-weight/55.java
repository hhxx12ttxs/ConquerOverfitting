private int numOfNeurons;
private double [] weight;
private double inputWeight;
private double outputWeight;
public double updateNeuronWeights( double weight, double err, double neuronVal, double inputVal, String neuronType) {
if ( neuronType.equals( &quot;output&quot; ) ){
outputWeight = outputWeight - err * neuronVal * learningRateOutput;

