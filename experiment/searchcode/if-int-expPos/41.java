public class CPPASTAmbiguousExpression extends ASTAmbiguousNode implements
IASTAmbiguousExpression {

private IASTExpression [] exp = new IASTExpression[2];
private int expPos=-1;

public CPPASTAmbiguousExpression(IASTExpression... expressions) {
for(IASTExpression e : expressions)

