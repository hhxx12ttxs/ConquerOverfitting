private static Point simulatedAnnealing(Point startingPoint, double d) {
Point currentPoint = startingPoint;
Point child;
child = randomChild(currentPoint, d);

double diff = evaluate(child) - evaluate(currentPoint);

if(diff >= 0) currentPoint = child;

