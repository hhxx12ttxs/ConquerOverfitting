public boolean updateValue(long val, int day, int month, int year) {
if(year!=this.year || thisYear[getIndex(day,month,year)]>val) {
return false;
thisYear = new long[getDaysInYear(year)];
}

public long getValue(int day, int month, int year) {
if(year!=this.year) {

