import automenta.spacenet.var.ObjectVar;

abstract public class IfDoubleChanges extends IfChanges<Double> {

public IfDoubleChanges(DoubleVar... r) {
super(r);
}

@Override public void afterValueChange(ObjectVar r, Double previous, Double next) {

