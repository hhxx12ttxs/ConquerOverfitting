public class GreaterThanOrEqualOperator implements BinaryOperator {

public double calculate(double p1, double p2) {
// dbgMsg(&quot;Calculating: &quot;+p1+&quot;>=&quot;+p2+&quot;?&quot;);
if (p1 >= p2) {
return 1;
}
else {
return 0;
}
}



}

