double functionValueAccuracy = getFunctionValueAccuracy();

verifySequence(min, initial, max);

double yInitial = computeObjectiveValue(initial);
if (FastMath.abs(yInitial) <= functionValueAccuracy) {
return initial;

