public class StddevAggregator implements AggregationMethod
{
private double sum;
private double sumSq;
private long numDataPoints;
sum -= value;
sumSq -= value * value;
}

public Object getValue()
{
if (numDataPoints < 2)

