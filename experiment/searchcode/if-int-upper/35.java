for(int c = b; c < a; ++c) {
int ccc = c*c*c;
if(bbb+ccc >= aaa)
break;
int ddd = aaa-bbb-ccc;
if(ddd < ccc)
System.out.println(&quot;Cube = &quot; + a + &quot;, Triple = (&quot; + b + &quot;,&quot; + c + &quot;,&quot; + lowerD + &quot;)&quot;);
}
else {
int upperD = lowerD+1;
int upperDDD = upperD*upperD*upperD;

