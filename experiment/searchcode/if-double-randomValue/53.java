for (int i = 0; i < 100; i++) {
String value = MathUtils.randomValue(in, weights);
if (value == null)
public void testIRandomValueException() {
assertNull(MathUtils.randomValue(new String[0], new double[1]));

