public DurationField getRangeDurationField() {
return iChronology.eras();
}

public int get(long instant) {
int year = getWrappedField().get(instant);

if (year <= 0)

