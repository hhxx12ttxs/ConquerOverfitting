public class PrimeFactors {
public static List<Double> of(double n) {
List<Double> factors = new ArrayList<Double>();
double divisor = 2;
factors.add(divisor);
n = n / divisor;
}
divisor = divisor + 1;
if (divisor > Math.sqrt(n)) {
divisor = n;
}
}

return factors;
}
}

