public StringComparer(StringExpression compare_expression) {
this.compare_expression = compare_expression;
}

@Override
public int compare(Expression compare_to) {
if (compare_to instanceof StringExpression) {
String string_compare    = new String(compare_expression.evaluate());

