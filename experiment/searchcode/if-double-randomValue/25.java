private List<Double> valuesToGenerate = new LinkedList<Double>();

public double randomValue() {
if (valuesToGenerate.size() > 0) {
return super.randomValue();
}
}

public void setNextGeneratedValue(double nextGeneratedValue) {
valuesToGenerate.add(nextGeneratedValue);
}
}

