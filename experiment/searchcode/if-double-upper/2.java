public void mutate(double percentage) {
if(percentage > 0) {
double max_add = upper_bound - double_value;
double mutated = double_value + change;

if(mutated < upper_bound) {
setAllele(new Double(mutated));

