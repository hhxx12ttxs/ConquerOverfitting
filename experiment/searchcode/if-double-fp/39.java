* @author akishore
*
*/
public class EvalMetrics {

double fp;
double fn;
double tp;
double tn;
double f1;
double err;
public void incrTN() {
tn++;
}

public void incrFP(double count) {
fp += count;
}

public void incrFN(double count) {

