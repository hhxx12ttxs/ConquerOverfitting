private int o[];
private double threshold=0;

public double getThreshold() {
return threshold;
double[] result = null;
double[] newResult = null;
double difference;

result = detect(hmm.readFile());

