public BooleanComparer(BooleanExpression compare_expression) {
this.compare_expression = compare_expression;
}

@Override
public int compare(Expression compare_to) {
if (compare_to instanceof BooleanExpression) {
Boolean boolean_compare    = new Boolean(compare_expression.evaluate());

