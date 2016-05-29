this.lower = lower;
this.upper = upper;
}

public Interval getLowerHalf()
{
if(upper==lower){
return new Interval(lower,upper);
return (new Interval(lower,(int)Math.ceil((double)(upper - lower)/2) - 1 + lower));
}

public Interval getUpperHalf()
{
if(upper==lower){
return new Interval(lower,upper);

