for (int label = 0; label < prior.length; label++) {

double logSum = 0;

for (Map.Entry<Integer, Double> pairs : ins) {
int attr = pairs.getKey();
double value = pairs.getValue();

if (attr > matrix[label].length - 1) {		// skip unseen attributes

