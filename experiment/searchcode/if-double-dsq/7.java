curr = arr[i];
dsq = L2.getSquared(s1.values, curr.values, excDist);
if (dsq >= 0) {
candidatesCount++;
if ( dsq < distsq1 &amp;&amp; dsq <= maxFDsq) {
distsq2 = distsq1;
distsq1 = dsq;

