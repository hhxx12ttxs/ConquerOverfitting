public IntegerComparer(IntegerExpression compare_expression) {
this.compare_expression = compare_expression;
}

@Override
public int compare(Expression compare_to) {
if (compare_to instanceof IntegerExpression) {
Integer integer_compare    = new Integer(compare_expression.evaluate());

