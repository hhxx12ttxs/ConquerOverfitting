package ann;

public class ValueThreshold implements Threshold {
protected double thresholdValue;

public ValueThreshold(double f) {
thresholdValue = f;
}

@Override
public double fire(double v) {
if(thresholdValue>=v) {

