public Neighborhood3D(PSO3D p)
{
po = p;
}

public void updateBest(double currVal, double currX, double currY, double currZ)
{
if (currVal > bestVal)
{
bestVal = currVal;

