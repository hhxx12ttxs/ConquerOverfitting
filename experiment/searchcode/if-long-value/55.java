public class IncrementalAggregateSumLong implements Aggregate<Long>
{
protected long sum = 0;

@Override
public Long getValue()
{
return sum;
}

@Override
public void addValue(Long value)
{
if(value == null)
return;
sum += value;
}

}

