RangePolicy(int maxRange)
{
this.maxRange = maxRange;
}
public void recordEliminationSuccess()
{
if(currentRange < maxRange)
currentRange++;
}
public void recordEliminationTimeout()
{
if(currentRange > 1)

