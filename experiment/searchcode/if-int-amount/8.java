public class USProgressiveRateStrategy extends USRateStrategy {
public int calculateTime(int amount) {
int time = 0;
if (amount>=650) {
amount -=350;
time=120+amount/5;
}
else if (amount>=150) {

