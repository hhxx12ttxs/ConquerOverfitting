double mxlimit, pxlimit; // plus minus xlimits
double xtol = 5.0e-1;
double[] root; // three roots
boolean negative = false;
//b = Math.abs(control.getDouble(&quot;B&quot;));
b = control.getDouble(&quot;H&quot;);
pxlimit = 5.0;
mxlimit = -5.0 - xtol / 2;

