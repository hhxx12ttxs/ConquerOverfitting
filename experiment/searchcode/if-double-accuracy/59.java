private String _unit = &quot;&quot;;
private AccuracyDouble _accuracyDouble = AccuracyDouble.DOUBLE;

public void setUnit(String unit) {
if (AccuracyDouble.DOUBLE.getValue().equals(accuracy)) {
_accuracyDouble = AccuracyDouble.DOUBLE;
} else if (AccuracyDouble.FLOAT.getValue().equals(accuracy)) {

