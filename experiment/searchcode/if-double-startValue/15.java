protected double startValue;


public ARWStatDistribution(double startValue )
{
this.startValue = startValue;
}


@Override
public Double GetNumber() {

double newValue = startValue +  GetMean() + rnd.nextGaussian() * GetStandardDeviation();

