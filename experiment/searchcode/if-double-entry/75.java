import weka.core.Instance;

public class PossibilityCalculator {
private List<DataEntry> entries;
private Map<Double, Integer> categoryCounter;
private void registerCategory(double category) {
if (this.categoryCounter.containsKey(category)) {
int count = this.categoryCounter.get(category);

