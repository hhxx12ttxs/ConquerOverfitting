public double tp;
public double fp;
public double fn;

public Classes() {
tp = 0.0;
public void update(double t, double f, double n) {
tp += t;
fp += f;
fn += n;
}

public double cal_precision() {
if ((tp + fp) != 0.0) {

