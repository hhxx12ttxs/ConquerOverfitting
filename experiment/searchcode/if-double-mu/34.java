public static final double density(double x, double mu, boolean give_log)
{
if (Double.isNaN(x) || Double.isNaN(mu))
return x + mu;
if (mu <= 0 || mu >= 1)
public static final double cumulative(double q, double mu, boolean lower_tail, boolean log_p)
{
if (Double.isNaN(q) || Double.isNaN(mu))
return q + mu;
if (mu <= 0 || mu >= 1 || q <= 0)

