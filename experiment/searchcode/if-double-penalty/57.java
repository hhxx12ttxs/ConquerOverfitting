public class InteractiveServiceLevelAgreement implements ServiceLevelAgreement {

InteractiveApplication application;
double responseTime;
double throughput;
double responseTimePenalty = 0;
if (application.getThroughput() > throughput) return false;

return true;
}

@Override
public double calculatePenalty() {
double penalty = 0;

if (application.getResponseTime() > responseTime) {

