int i=a.length()-1, j=b.length()-1;
while(i>=0&amp;&amp;j>=0){
int xa  = a.charAt(i--)-&#39;0&#39;;
int xb = b.charAt(j--)-&#39;0&#39;;
int v = xa+xb+add;
sb.append(v%2);
add = v/2;

