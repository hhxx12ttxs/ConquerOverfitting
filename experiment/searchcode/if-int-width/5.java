public int computeArea(int A, int B, int C, int D, int E, int F, int G, int H) {
int width = 0;
int hight = 0;

if (A < E &amp;&amp; E < C &amp;&amp; C < G) {
width = C - E;
}
else if (E < A &amp;&amp; A < G &amp;&amp; G < C) {
width = G - A;

