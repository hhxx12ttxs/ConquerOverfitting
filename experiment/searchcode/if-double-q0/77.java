public abstract class ReinforcementPredictor extends AbstractPredictor {

boolean log = false;
final double q0;
final Stepsize stepsize;

Map<Pair<State, Integer>, QValue> q = new HashMap<Pair<State, Integer>, QValue>();

public ReinforcementPredictor(double q0, Stepsize stepsize) {

