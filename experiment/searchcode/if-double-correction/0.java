* Represents the synaptic strength of a &#39;Connection&#39; with a &#39;value&#39;.
*
*/

public class Weight {

private double value,
correction,  // from the now epoch.
public Weight(double min, double max, Random generator) {
if (generator != null) {
this.value = this.randomWithinRange(min, max, generator);

