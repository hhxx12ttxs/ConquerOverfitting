for(degreesOfFreedom = 0; degreesOfFreedom < this.N; ++degreesOfFreedom) {
double fit = this.beta * (double)x[degreesOfFreedom] + this.alpha;
var27 += (fit - (double)y[degreesOfFreedom]) * (fit - (double)y[degreesOfFreedom]);

