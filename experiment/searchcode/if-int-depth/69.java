System.out.println(bracketDepth(&quot;(1+2+(9-8)-*(7-8 + (1-2))) + (6+1)&quot;));
}

static int bracketDepth(String s) {
int currentDepth = 0;
int maxDepth = 0;
for(int i=0; i<s.length(); i++) {
if(s.charAt(i) == &#39;(&#39;) {

