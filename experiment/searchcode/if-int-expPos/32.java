private IASTExpression [] exp = new IASTExpression[2];
private int expPos=-1;

@Override
public IScope getAffectedScope() {
for(IASTExpression e : expressions)
addExpression(e);
}

public void addExpression(IASTExpression e) {
if (e != null) {

