this.valuePerMilli = ((double)(endValue - startValue)) / ((double)durationMillis);
}

/**
* Constructor where you specify <i>value/ms</i> between the two pixel values.
public void setStartValue(int startValue) {
this.startValue = startValue;

if (durationMillis > 0) {
this.valuePerMilli = ((double)(endValue - startValue)) / ((double)durationMillis);

