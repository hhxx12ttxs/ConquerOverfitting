for (Ratio<T, Double> ratio : ratios) {
if (ratio.getKey() == key) {
return ratio;
}
}
return null;
}

public double getTotalRatio() {
List<Ratio<T, Double>> tempRatios = new ArrayList<>();

for (Ratio<T, Double> ratio : ratios) {
double value = 0.0;
if (index == -1) {

