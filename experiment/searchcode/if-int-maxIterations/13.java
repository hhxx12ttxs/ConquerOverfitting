public class PojoNewtonFractal extends ValueGenerator {

int maxIterations = 50;

static final Complex a = new Complex(-0.7, 0.3);

@Override
private int newtonIterations(Complex z) {

for (int t = 0; t < maxIterations; t++) {
if (z.abs() > 2.0) {

