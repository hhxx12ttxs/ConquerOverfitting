public class ClopperPearsonInterval implements BinomialConfidenceInterval {

/** {@inheritDoc} */
public ConfidenceInterval createInterval(int numberOfTrials, int numberOfSuccesses,
final double fValueLowerBound = distributionLowerBound.inverseCumulativeProbability(1 - alpha);
if (numberOfSuccesses > 0) {
lowerBound = numberOfSuccesses /

