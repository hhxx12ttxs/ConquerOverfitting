public static double getValue(ExpressionNode nod, double t) {
if (nod == null) {
return 0;
}
switch (nod.mType) {
case PLUS:
return Math.pow(getValue(nod.mChildNode, t), getValue(nod.mLeftNode, t));
case FUNC:
if (nod.mFuncName == &quot;SIN&quot;)
return Math.sin(getValue(nod.mChildNode, t));

