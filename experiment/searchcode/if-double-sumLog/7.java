double sumOfProductsL = 0.0;
double sumlogLoss;
for (int labelIndex = 0; labelIndex < groundTruth.length; labelIndex++) {
double prediction = confidences[labelIndex];
if (prediction > (1 - epsilon)) {

