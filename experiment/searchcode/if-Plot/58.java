for (int i = 0; i < 16 &amp;&amp; result == null; i++) {
if (allPlot.get(i).name.compareTo(lastMove) == 0) {
result = allPlot.get(i);
public String addTorus(String id, String lastMove) { // return the high of
// the torus
Plot plot = retrievePlot(lastMove);
if (plot != null) {

