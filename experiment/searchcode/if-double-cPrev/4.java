for (TreePoint cq : cp.getNeighbors()) {

double rp = cp.getR();
double rq = cq.getR();

if (cq.getWork() == 2 &amp;&amp; cq.getNeighborCount() == 2
&amp;&amp; Math.abs((rq - rp) / (rq + rp)) < 0.5 * maxdr) {
vpt.add(cq);
double dl = distanceBetween(cprev, cq);
ltot += dl;
ldtot += dl * (cprev.getR() + cq.getR());

