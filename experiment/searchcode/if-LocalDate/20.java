package lab5_1;

import java.time.LocalDate;

public final class DateRange {

LocalDate startDate;
public boolean isinRange(LocalDate calendar){
LocalDate c=startDate;
LocalDate checkdate=calendar;

if(calendar.isAfter(startDate) &amp;&amp; calendar.isBefore(endDate)){

