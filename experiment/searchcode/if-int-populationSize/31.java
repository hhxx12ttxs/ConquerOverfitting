int populationSize = 1000000;
int maxValue = 100;
float shiftRate = 0.01f; //1%
int shiftFrequency = populationSize;
System.out.println(&quot;PopulationSize: &quot;+populationSize);
Zipf zipf = new Zipf(maxValue, 0.5);
for (int i = 0; i < populationSize; i++) {

