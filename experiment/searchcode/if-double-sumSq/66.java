protected double sum;
protected double sumSq;
protected long numDataPoints;

public void clear()
public void enter(Object object)
{
if (object == null)
{
return;
}

double value = ((Number) object).doubleValue();

