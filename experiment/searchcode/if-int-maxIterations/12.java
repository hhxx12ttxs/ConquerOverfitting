public class PojoJuliaSet extends ValueGenerator {

int maxIterations = 50;

float cRe = -0.7f;
float cIm = 0.27015f;

@Override
public float at(double x, double y) {
if (name.equals(&quot;maxIterations&quot;)) {
maxIterations = (int) value;
}
if (name.equals(&quot;cRe&quot;)) {

