private AnalogSink analogRef;

private double uMin = -10.0;
private double uMax = 10.0;


public BeamRegul(ReferenceGenerator refgen, Beam beam, int priority) {
private double limit(double u, double umin, double umax){
if(u<umin){
return umin;
}else if(u>umax){
return umax;
}else{

