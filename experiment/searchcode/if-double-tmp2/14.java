public double squareRoot(double a) {
double tmp1 = 1, tmp2 = 1;
for (int i = 0; i < (a + 1); i++) {
tmp2 = 0.5 * (tmp1 + (a / tmp1));
* absolute
*
* @param a
*            number
* @return absolute value of number
*/
public double absolute(double a) {
if (a < 0) {
a = -a;
}
return a;
}

}

