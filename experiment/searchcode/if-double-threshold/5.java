public class ActivationThreshold implements ActivationFunction{
double thresholdValue;

public ActivationThreshold(double d) {
this.thresholdValue = d;
}

@Override
public double activationFunction(double d) {
if (d > thresholdValue)

