}	else {
//operators
if (Character.toLowerCase(lastChar) == &#39;+&#39;) {
//addition
currentToken = new ArithmeticOperatorToken(&quot;addition&quot;, &quot;+&quot;);
currentToken = new ArithmeticOperatorToken(&quot;subtraction&quot;, &quot;-&quot;);
getChar();
} else if (Character.toLowerCase(lastChar) == &#39;*&#39;) {
//multiplication

