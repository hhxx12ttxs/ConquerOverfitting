private double accuracy;
private final double expectedAccuracy;
private List<Double> attributeWeights;
public AttributeWeighting(double accuracy, double expectedAccuracy, List<Double> attributeWeights) {
setAccuracy(accuracy);
this.expectedAccuracy = expectedAccuracy;

