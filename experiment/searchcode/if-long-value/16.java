public CumulativeCounter() {

}

public CumulativeCounter(long initialValue) {
currentValue = lastRestartValue = lastValue = initialValue;
}

public long accumulateValue(long value) {
if(value < lastValue) {

