public class LinearActivationFunction implements IActivationFunction {

protected double coeff = 1;

public LinearActivationFunction() {
// TODO Auto-generated constructor stub
@Override
public Double getDerivativeValueByS(int level, Double input) {
double res = coeff;
if (level > 1) {

