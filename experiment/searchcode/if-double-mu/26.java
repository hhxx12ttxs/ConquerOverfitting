public final class Laplace extends Distribution
{
/**
* location
*/
private double mu;
/*
* Scale
*/
private double b;
public double getMu()
{
return mu;
}

public void setB(double b)
{
if (b <= 0)

