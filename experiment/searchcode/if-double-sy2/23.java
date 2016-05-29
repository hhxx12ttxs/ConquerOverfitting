import ij.measure.*;
import ij.process.*;

public class T1TIFitter extends LinearFitter {

static double[] ti;
static FitResult calculate(double t1) {
int i;
sx2 = 0;
sxy = 0;
sy2 = 0;
sx = 0;
sy = 0;
for (i = 0; i < y.length; i++) {

