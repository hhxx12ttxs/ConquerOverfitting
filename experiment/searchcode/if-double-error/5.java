double kp,ki,kd;
double totalError = 0;
double lastError = 0;
double alpha = .8;
double target;
public TrisonicsPID(double kp,double ki,double kd) {
this.ki = ki;
}
public double getCorrection(double current) {
double error = target-current;
if (error*lastError<=0) totalError = 0;

