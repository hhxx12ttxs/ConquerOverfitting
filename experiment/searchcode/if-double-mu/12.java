public final class Logistic extends Distribution
{
/**
* Location
*/
private double mu;
/**
* Scale
*/
private double s;

public Logistic(double mu, double s)
{
this.mu = mu;
setS(s);

