double defaultDelta;

HashMap<String, Double> deltaHM;


public Resolution(double d, HashMap<String, Double> resHM) {
if (id != null &amp;&amp; deltaHM != null &amp;&amp; deltaHM.containsKey(id)) {
localDelta = deltaHM.get(id).doubleValue();

} else if (region != null &amp;&amp; deltaHM != null &amp;&amp; deltaHM.containsKey(region)) {

