public class RBFNeuron extends Point {
private double sigma;
public RBFNeuron(double... args) {
public void setSigma(double sigma) {
if (0. != sigma)
this.sigma = sigma;
else
this.sigma = 1;

}

}

