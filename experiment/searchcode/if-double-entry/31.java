public abstract class Polynomial implements Expression, Expression3 {
protected HashMap<Integer, Double> factors = new HashMap<Integer, Double>();
public double evaluate(double varValue) {
double ret = 0;
for (Map.Entry<Integer, Double> entry : factors.entrySet()) {
ret += Math.pow(varValue, entry.getKey()) * entry.getValue();

