public class SparseLinearInequality implements LinearInequality {

Map<String, Double> coeffMap;
double rhs;
for (String s : coeffMap.keySet()) {
double coeff = coeffMap.get(s);
if (coeff != 0) {
if (first) {

