public Map<String, Double> chisquare(FeatureStats stats, double criticalLevel) {
Map<String, Double> selectedFeatures = new HashMap<>();
int N1dot, N0dot, N00, N01, N10, N11;
double chisquareScore;
Double previousScore;
for(Map.Entry<String, Map<String, Integer>> entry1 : stats.featureCategoryJointCount.entrySet()) {

