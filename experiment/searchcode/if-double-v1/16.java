
public class Roberts {

static double measure (double[] v1, double[] v2) {

double acum1 = 0;
double acum2 = 0;

for (int i = 0; i < v1.length; i++) {

double v = 0;
if (Math.max(v1[i], v2[i]) == 0)

