private double mu, lambda;

public My_IverseGaussian(double mu, double lambda){
this.lambda = lambda;
this.mu = mu;
}

public double sample() {
Random rand = new Random();
double v = rand.nextGaussian();   // sample from a normal distribution with a mean of 0 and 1 standard deviation

