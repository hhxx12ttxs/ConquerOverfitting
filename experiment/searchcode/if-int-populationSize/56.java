public class Population
{
// Class varaiables
private int populationSize;
private int rows;
private int cols;
public void initPop()
{
for (int i = 0; i < populationSize; i++)
{
Chromosome c1 = new Chromosome(rows,cols,numInputs,numOutputs);

