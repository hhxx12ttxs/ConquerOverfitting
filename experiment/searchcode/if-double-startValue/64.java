this.valuePerMilli = ((double) (endValue - startValue)) / ((double) durationMillis);
}

/**
* Constructor where you specify <i>value/ms</i> between the two pixel values.
* @param durationMillis
*/
public Animation(int startValue, int endValue, double valuePerMilli)
{

this.startValue = startValue;

