for (int i = 0; i < days.length; i++) {
for (int j = 1; j <= days[i]; j++, week++) {
if (week == 8) {
week = 1;
private static int[] getDaysOfTheYear(int year) {
// 用于存放this year每个月有多少天
int[] days = new int[12];

if (isLeapYear(year)) {

