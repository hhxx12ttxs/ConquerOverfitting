double[][] retval = new double[N][]; // default to null
for(LogicRule lr : rules)
{
if(lr instanceof IndependentRule)
RelaxedSample relax = new RelaxedSample(c, p, s);

// Do LogicLDA MAP inference via Stochastic Gradient Descent
double stepa = Math.sqrt(numinner);

