import org.auvua.reactive.core.RxVar;

public class PidController extends RxVar<Double> {

private double feedForward = 0;
private double lastIntegral = 0;
public final RxVar<Double> outputMin = R.var(-Double.MAX_VALUE);
public final RxVar<Double> outputMax = R.var(Double.MAX_VALUE);

