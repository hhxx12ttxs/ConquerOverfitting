public class ModifiedClopperPearsonInterval implements BinomialConfidenceInterval {

@Override
public ConfidenceInterval createInterval(int numberOfTrials, int numberOfSuccesses, double confidenceLevel) throws NotStrictlyPositiveException, NotPositiveException, NumberIsTooLargeException, OutOfRangeException {
final double alpha = (1.0 - confidenceLevel) / 2.0;

if (numberOfSuccesses > 0) {
final FDistribution distributionLowerBound = new FDistribution(2 * (numberOfTrials - numberOfSuccesses + 1),

