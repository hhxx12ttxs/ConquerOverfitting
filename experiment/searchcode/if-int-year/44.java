public class LeapAge {
public int getAge(int year, int born) {
int leapNum = 0;
for (int i = born + 1; i <= year; i++) {
if (isLeapYear(i)) {
leapNum++;
}
}
return leapNum;

