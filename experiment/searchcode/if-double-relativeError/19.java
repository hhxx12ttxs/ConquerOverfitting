updateMetrics(sim.simulate());
}

public void runMany(int iterations, double absoluteError,
double relativeError) {
runMany(iterations, absoluteError, relativeError, null);
if (errorMet(absoluteError, relativeError))
break;
}
}

protected boolean errorMet(double absolute, double relative) {
if (fastError > 0.0

