private double targetX;
private double targetY;
private int trackMargin = 40;

public TrackingCommand() {
protected void execute() {
double angle = 0;
double impulse = 0;

if (RoboRealm.server != null) {

