public class PowerSource extends VoltageSource implements IRootSystemPreStepProcess, INBTTReady {

String name;

double P, Umax, Imax;

public PowerSource(String name,State aPin) {
double U = (Math.sqrt(t.U * t.U + 4 * P * t.R) + t.U) / 2;
U =  Math.min(Math.min(U, Umax), t.U + t.R * Imax);
if (Double.isNaN(U)) U = 0;
if (U < t.U) U = t.U;

