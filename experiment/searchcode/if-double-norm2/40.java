s.assign(b, DoubleFunctions.plusMultFirst(-1));
r = A.zMult(s, null, 1, 0, true);
rnrm = alg.norm2(r);
if (!(M instanceof DoubleIdentity)) {
r = A.zMult(s, null, 1, 0, true);
rnrm = alg.norm2(r);
if (!(M instanceof DoubleIdentity)) {

