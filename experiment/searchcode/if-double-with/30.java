static double N18251644(String []i) {
double p = Double.NaN;
if (i[3] == null) {
p = 1;
} else if (Double.parseDouble( i[3]) <= 0.366505) {
p = WekaClassifier.Nada0e95(i);
} else if (Double.parseDouble( i[3]) > 0.366505) {

