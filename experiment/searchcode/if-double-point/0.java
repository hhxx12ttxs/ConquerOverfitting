public class DoublePoint implements Metrical<Double> {
private double x;
private double y;
public Double distanceTo(Metrical<Double> o) {
if (!(o instanceof DoublePoint)) {
throw new IllegalArgumentException(&quot;Distance can be calculated between two double points only&quot;);

