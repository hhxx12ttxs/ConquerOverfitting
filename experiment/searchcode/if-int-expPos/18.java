private IASTExpression [] exp = new IASTExpression[2];
private int expPos=-1;

public void addExpression(IASTExpression e) {
if (e != null) {
exp = (IASTExpression[]) ArrayUtil.append( IASTExpression.class, exp, ++expPos, e );

