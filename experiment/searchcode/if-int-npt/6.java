public class Krig {
final double[][] x;
final UniVarRealValueFun vgram;
int ndim, npt;
double lastval, lasterr;
v = new double[npt+1][npt+1];
y = new double[npt+1];
yvi = new double[npt+1];

int i,j;
for (i=0;i<npt;i++) {

