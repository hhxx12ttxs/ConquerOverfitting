package affix.java8.dateandtime;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
LocalDate thisYearBirthDay = LocalDate.of(today.getYear(), birthday.getMonth(), birthday.getDayOfMonth());
LocalDate nextYearBirthDay = thisYearBirthDay.plusYears(1);

if(thisYearBirthDay.isAfter(today)){

