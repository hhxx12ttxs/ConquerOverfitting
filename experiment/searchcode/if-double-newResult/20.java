public class RankingRecallEstimator {

private double w;
private double fraction;
private ParameterEstimator est;
double den = k1*Math.pow(coefExp, w-1)+k2;
double newResult = result - num/den;
if(result==newResult){
//If the result did not change it means that we will not make more progress.

