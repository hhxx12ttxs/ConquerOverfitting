private double error;
private double previousError;
private double previousTime;
private double output;
public PID(double p, double i, double d) {
kP = p;
kI = i;
kD = d;
previousTime = 0;
output = 0;
}

public void update(double current, double want) {

