if(targetx == 0 &amp;&amp; targety == 0) {
if(rand.nextDouble() > 0.95)
facing = rand.nextInt(4);
if(facing == 3 &amp;&amp; x > 1) { // west
x-=1;
}

}
else {
if(facing==0 || facing==2) {
if(targety < y &amp;&amp; y > 1)

