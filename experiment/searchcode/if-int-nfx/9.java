private double reff, refx, refy, refz;
private double nfx, nfy, nfz;
private int r, g, b, pb, pr, pg;
private final ShadingInfo shadingInfo;
nfy = vaz * vbx - vax * vbz;
nfz = vax * vby - vay * vbx;
double rr = Math.sqrt(nfx * nfx + nfy * nfy + nfz * nfz);
if (Math.abs(rr) < MathLib.EPSILON) {

