public class LbfgsMinimizer extends ProcessingHookProvider<LbfgsMinimizer, IFunctionInstance> implements IFunctionMinimizer {

private static final int MAX_FAILED = 100;

private double m_xtol = 1e-9, m_gtol = 1e-5;
public double getXTol() {
return m_xtol;
}

/**
*
* @param function

