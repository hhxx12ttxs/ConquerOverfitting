public void Right_Left(double Right, double Left) {//計算R-L屬於哪個區間
double dist = Right - Left;
if (dist <= -10) {
if (dist <= -130) {
alpha_d1 = 1;
} else {
double[] alpha = { 0, 0, 0, 0, 0, 0, 0, 0 };
double maxalpha = 0;
if (d1[2] == 1 &amp;&amp; d2[0] == 1) {
alpha[1] = Math.min(alpha_d1, alpha_d2);

