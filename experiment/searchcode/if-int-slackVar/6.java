// I equations:
for (int i = 0; i < I; i++) {
// conjoin, if needed:
if (begun_statements) {
// add J terms:
for (int j = 0; j < J; j++) {
a = prem.getRHSCoeff(j).toString();
if (!a.matches(&quot;-*0&quot;)) {

