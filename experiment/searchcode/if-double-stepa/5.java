protected boolean stepA = true;

protected double travelTime;

public Driver(int id, String origin, String destination, Graph graph) {
@Override
public Driver call() throws Exception {
if (stepA) {
this.beforeStep();

