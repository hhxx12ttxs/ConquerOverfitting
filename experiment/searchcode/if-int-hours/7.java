System.out.println(payment(41));
}
public static double payment(int hours) {
double payment = 0.0;
double basePay = 8;
int normalHours = 0;
int extraHours = 0;
if (hours > 60) {
normalHours = 40;

