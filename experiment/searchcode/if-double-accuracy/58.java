_unit = unit;
}

public void setAccuracy(String accuracy) {
if (AccuracyDouble.DOUBLE.getValue().equals(accuracy)) {
_accuracyDouble = AccuracyDouble.DOUBLE;
} else if (AccuracyDouble.FLOAT.getValue().equals(accuracy)) {

