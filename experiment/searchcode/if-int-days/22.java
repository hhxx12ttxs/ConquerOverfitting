int days = getTotalDaysSince1980();
int year = 1980;
while (days > 365) {  // subtract out years
    if (isLeapYear(year)) {
        if (days > 366) {
            days -= 366;
            year += 1;

