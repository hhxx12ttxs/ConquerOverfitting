public class InteractBC implements Interact {
public final double EPSILON;
public final double MU;

public InteractBC(double EPSILON, double MU) {
this.EPSILON = EPSILON;
this.MU = MU;
}



@Override
public double opinionDiff(Agent a, Agent b) {

