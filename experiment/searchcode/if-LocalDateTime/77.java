package net.vegard.java8.time;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Java8DateTimeAPIExample {
private static void isBeforeOrAfterToday(final LocalDateTime aDate) {
if (aDate.isBefore(LocalDateTime.now())) {
System.out.println(&quot;The date is before today.&quot;);

