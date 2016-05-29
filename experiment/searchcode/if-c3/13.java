int c3 = c1;
for(int i = 0; i < 8; ++i) {
++r3;
++c3;
if(r2 == r3 &amp;&amp; c2 == c3) {
c3 = c1;
for(int i = 0; i < 8; ++i) {
--r3;
--c3;
if(r2 == r3 &amp;&amp; c2 == c3) {

