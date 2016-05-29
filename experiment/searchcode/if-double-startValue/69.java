private final double start;
private final double size;

public DoubleRange(double startValue, double size) {
public boolean contains(Double item) {
if (item == null) return false;
if (size >= 0) {

