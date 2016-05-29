public class RopeConstraint<B extends Body<V>, V extends Vector<V>> extends DistanceConstraint<B,V> {

public RopeConstraint(B b1, B b2) {
super(b1, b2);
}

public RopeConstraint(B b1, B b2, double length) {
super(b1, b2, length);

