public class IterationsStopCondition extends StopCondition {

private int maxIterations;
private boolean stop;
public IterationsStopCondition(int iterations) {
public boolean stop() {
if(super.iterations+1>this.maxIterations) {
stop = true;
}
return stop;
}

}

