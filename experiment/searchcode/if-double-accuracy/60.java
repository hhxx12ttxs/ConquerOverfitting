final int clipRank0 = clipRank1 - 1;
final double oldAccuracy = maxAccuracyAcrossC.get(qid,clipRank0);
if (newAccuracy > oldAccuracy) {
for (int qid = 0; qid < maxAccuracyAcrossC.rows(); qid++){
if(maxAccuracyAcrossC.get(qid, clipRank) != (-Double.MAX_VALUE)){
colSumsMaxAcc[clipRank] += maxAccuracyAcrossC.get(qid, clipRank);

