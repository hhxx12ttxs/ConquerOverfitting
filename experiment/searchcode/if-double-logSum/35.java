public class LogProductAggregator extends NumericalAggregator {

private double logSum = 0;

public LogProductAggregator(AggregationFunction function) {
public void count(double value) {
logSum += Math.log(value);
}

@Override
protected void count(double value, double weight) {

