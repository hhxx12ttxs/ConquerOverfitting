return year < 0 ? -year : year;
}
public int getDifference(long minuendInstant, long subtrahendInstant) {
return getWrappedField().getDifference(minuendInstant, subtrahendInstant);
}
public long getDifferenceAsLong(long minuendInstant, long subtrahendInstant) {

