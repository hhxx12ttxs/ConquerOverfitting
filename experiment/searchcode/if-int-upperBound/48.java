public class BoundaryChecker {

private int lowerBound;
private int upperBound;

public BoundaryChecker(int lowerBound, int upperBound) {
if (lowerBound >= upperBound) {
throw new IllegalArgumentException();
}

this.lowerBound = lowerBound;

