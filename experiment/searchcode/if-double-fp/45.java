public void addResult(double predict, double actual) {
counter++;
double loss = predict - actual;
sqLoss += loss * loss;
if(predict > 0.0) {
double rmse = Math.sqrt(sqLoss) / counter;
double precision = (double)tp / (tp + fp);
double recall = (double)tp / (tp + fn);
if(counter > 0) {

