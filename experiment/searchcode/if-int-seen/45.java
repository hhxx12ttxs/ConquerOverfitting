/* score with flexible number of steps */
public double score(Object hidden, Object seen, int steps) {
double total = 0;
/* returns probability of hidden -> seen with <code>steps</code>
* random walk steps */
public double step(Object hidden, Object seen, int steps) {
if (steps < 1) {

