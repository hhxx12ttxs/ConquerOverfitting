static public <T extends Object> double length(Map<T, Double> v1) {
double r = 0;
for (Double v : v1.values()) {
r += v * v;
}
return Math.sqrt(r);
}

static public <T extends Object> double product(Map<T, Double> v1, Map<T, Double> v2) {
final Map<T, Double> small = v1.size() <= v2.size() ? v1 : v2;

