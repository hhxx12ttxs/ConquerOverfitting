size), and r (number of type 1 objects)*/
public class HypergeometricDistribution extends Distribution{
private int populationSize, sampleSize, type1Size;
if (n < 0) n = 0; else if (n > m) n = m;
//Assign parameter values
populationSize = m;
type1Size = r;

