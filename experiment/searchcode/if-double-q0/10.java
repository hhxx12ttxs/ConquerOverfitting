public GreedyPredictor() {
this(0, new WeightedAverage(0), 0.0);
}

public GreedyPredictor(double q0, double alpha, double epsilon) {
this(q0, new WeightedAverage(alpha), epsilon);
}

public GreedyPredictor(double q0, Stepsize stepsize) {

