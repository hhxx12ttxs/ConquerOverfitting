public class GeneratorThread extends Thread {

private ICallbackReceiver receiver;
private int populationSize;
private IRng rng;
StringBuffer sb = new StringBuffer();
double result;
double [] results = new double[this.populationSize];

for (int i = 0; i < populationSize; i++) {

