public class Uniform implements Distribution {

double lowerBound;
double upperBound;

public Uniform(double lb, double ub){
if(lb>=ub){
throw new RuntimeException(&quot;The lower bound &quot;+lb+&quot; of the uniform distribution is not strictly smaller than the upper bound &quot;+ub);
}
lowerBound=lb;
upperBound=ub;
}

@Override
public double sample() {

