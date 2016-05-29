ArrayList<Double> errors = loadErrors(predictionsFilename, res.isRegression);

double testError = -1;
if (res.isRegression) {
testError = rmse(errors);
}
else {
System.out.println(res.experiment.name + &quot;,&quot; + res.seed + &quot;,&quot;
+ res.numTrajectories + &quot;,&quot; + res.numEval + &quot;,&quot; + res.totalNumEval

