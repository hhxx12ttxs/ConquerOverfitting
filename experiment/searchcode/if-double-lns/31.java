public CountriesData(String filename)
{
parseFile(filename);
this.numberOfYears = 54;

}

public double calculateHighestMeanPopulationOverall()
meanPop[i] = data[i].calculateMeanPopulation();

double lg =meanPop[0];
for(int i = 0; i < meanPop.length; i++)
if(meanPop[i] > lg)
lg = meanPop[i];

