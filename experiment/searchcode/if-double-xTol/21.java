public class LbfgsMinimizer implements IFunctionMinimizer {

private static final int MAX_FAILED = 100;

private double m_xtol = 1e-9, m_gtol = 1e-5;
return m_fcur == null ? Double.NaN : m_fcur.getValue();
}

// / <param name=&quot;xtol&quot;>An estimate of the machine precision (e.g. 10e-16 on

