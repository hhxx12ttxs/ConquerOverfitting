private HashSet<Datapoint> neighborhood;
private WeightI weight;
private short n;
private double Z;
private double S;
double zj = j.getValue(featureIdx);
double lij = weight.compute(x, y, dpX, dpY);

num += (lij * zj);
sumDen += (Math.pow(lij, 2));

