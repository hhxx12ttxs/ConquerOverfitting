public static Number multiply(Number n1, Number n2) {
if (n1 instanceof Double || n2 instanceof Double) {
return n1.doubleValue() * n2.doubleValue();
}
else if (n1 instanceof Float || n2 instanceof Float) {
return n1.floatValue() * n2.floatValue();

