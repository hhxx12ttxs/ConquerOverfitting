Bracket b = new Bracket();
b.print(4);
}

public void print(int n) {
if (n > 0) {
printBracket(n, 0, 0,&quot;&quot;);
}
}

public void printBracket(int n, int open, int close, String s) {
if (open == n &amp;&amp; close == n) {

