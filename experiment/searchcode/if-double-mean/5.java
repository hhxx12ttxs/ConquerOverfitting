public class Bernoulli {
double mean;
MTRandom m = new MTRandom();

public Bernoulli(double mean) {
this.mean = mean;
}

public int nextBer() {
double ran = m.nextDouble();
if(ran < mean) return 1;
else return 0;
}

}

