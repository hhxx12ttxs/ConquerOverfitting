Optimizable.ByGradientValue optimizable;
LineOptimizer.ByGradient lineMaximizer;

double initialStepSize = 1;
double tolerance = 0.0001;
public ConjugateGradient (Optimizable.ByGradientValue function, double initialStepSize)
{
this.initialStepSize = initialStepSize;
this.optimizable = function;

