private double[] recall;
private double[] f1;

private double MicroF1;
private double MacroF1;

public Metrics(int n) {
recall = new double[N + 1];
f1 = new double[N + 1];
MicroF1 = 0.0;
MacroF1 = 0.0;
}

public double getMacroF1() {

