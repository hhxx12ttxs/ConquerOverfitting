ReflexValue b = rhs.evaluate(debugger, scope);

if (b.equals(0)) {
throwError(&quot;division by zero&quot;, lhs, rhs, a, b);
}
// number / number
if (a.isNumber() &amp;&amp; b.isNumber()) {

