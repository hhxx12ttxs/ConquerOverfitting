throw new IllegalArgumentException(&quot;Divide by zero&quot;);
}
if (n1.isZero() || n2.isA(&quot;1&quot;)) {
return new Factory().createCopy(n1);
}
if (n2.isA(&quot;-1&quot;)) {
int sign = n1.getSign() * n2.getSign();

if (0 > compareAbs) {
setRest(new Factory().createCopy(n1));
return new Factory().createZero();

