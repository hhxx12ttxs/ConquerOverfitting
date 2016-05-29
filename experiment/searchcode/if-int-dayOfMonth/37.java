else
dayOfMonth -= 365;
year ++;
}
//day count in each month
int[] dayCounter;
if(isLeapYear(year)){
dayCounter = new int[]{31,28,31,30,31,30,31,31,30,31,30,31};
}
int i = 0;
for(;;){
if(dayOfMonth - dayCounter[i] < 0)

