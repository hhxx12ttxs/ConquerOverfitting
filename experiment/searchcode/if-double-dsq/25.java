private double avg;			// = 1/n*sum^n{x_i}
private double dsqSum;		// = sum^n{(x_i-avg)^2}

private double min = Double.MAX_VALUE;
public void addData(double value) {
double d = value-avg;
n++;
avg += d/n;
dsqSum += (n-1)*d*d/n;
if(value < min) {

