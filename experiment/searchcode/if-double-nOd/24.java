for (Muchie e : u.getAdjacencies())
{
Nod v = e.getTarget();
double weight = e.getWeight();
double distanceThroughU = u.getMinDistance() + weight;
if (distanceThroughU < v.getMinDistance()) {

