private static final long serialVersionUID = 1;

public double bestVal = 0;
MutableDouble3D bestPosition = new MutableDouble3D();

public PSO3D po;
public void updateBest(double currVal, double currX, double currY, double currZ) {
if (currVal > bestVal) {

