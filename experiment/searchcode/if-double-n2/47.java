public class MathsCommand {
public Number add(Number n1, Number n2) {
if (n1 instanceof Double || n1 instanceof Float || n2 instanceof Double
return new Long(n1.longValue() + n2.longValue());
}
}
public Number subtract(Number n1, Number n2) {
if (n1 instanceof Double || n1 instanceof Float || n2 instanceof Double

