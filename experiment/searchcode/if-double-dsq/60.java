protected <region R1>void walksub(Node p, double dsq, double tolsq, HGStruct<R1> hg,
int level) reads MP writes R, R1 {
/* should p be opened?    */
if (p.subdivp(p, dsq, tolsq, hg)) {
/* loop over the subcells */

