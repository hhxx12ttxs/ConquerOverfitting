this.builder = builder;
this.samples = samples;
}

public ClassifierAccuracy getAverageAccuracy() {
double min = Double.MAX_VALUE;
//					+ &quot; classifiers.&quot;);
SentimentClassifier c = builder.buildClassifier();
double accuracy = c.getAccuracy();

