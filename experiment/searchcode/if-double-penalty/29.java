* @param probability
* @param penalty
*/
public PenaltyNode(Random random, double probability, Penalty penalty) {
public boolean evaluate(final Collection<Penalty> results) {
if (probability < 1.0 &amp;&amp; random.nextDouble() > probability) {

