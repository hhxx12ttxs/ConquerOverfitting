this.weightAttribute = weightAttribute;
}

public double getWeight(Map<String, Object> attributes) {
Double weight = (Double) attributes.get(weightAttribute);
if(weight == null) {
return DEFAULT_WEIGHT;
} else if(weight <= 0) {

