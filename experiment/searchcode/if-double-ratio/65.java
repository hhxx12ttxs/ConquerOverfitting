protected double entryDistance(int a, int b) {
double ratio;
if (a > b) {
ratio = (a + epsilone) / (b + epsilone);
protected double entryDistance(double a, double b) {

double ratio;
if (a < b) {
ratio = (a + epsilone) / (b + epsilone);

