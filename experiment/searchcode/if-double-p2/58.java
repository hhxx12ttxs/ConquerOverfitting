public int compare(Pair <Number> p1, Pair <Number> p2)
{
int comp = 0;
if(p1.getFirst().doubleValue() + p1.getSecond().doubleValue() < p2.getSecond().doubleValue() + p2.getFirst().doubleValue())
comp = -1;
}
else if(p1.getFirst().doubleValue() + p1.getSecond().doubleValue() > p2.getFirst().doubleValue() + p2.getSecond().doubleValue())

