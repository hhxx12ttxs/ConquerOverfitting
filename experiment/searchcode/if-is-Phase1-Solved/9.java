} while (busy);

// +++++++++++++ compute new coordinates and new minDistPhase1 ++++++++++
// if minDistPhase1 =0, the H subgroup is reached
if (minDistPhase1[n + 1] == 0 &amp;&amp; n >= depthPhase1 - 5) {
minDistPhase1[n + 1] = 10;// instead of 10 any value >5 is possible
if (n == depthPhase1 - 1 &amp;&amp; (s = totalDepth(depthPhase1, maxDepth)) >= 0) {

