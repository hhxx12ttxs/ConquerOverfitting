
package ex_05_05;

import java.security.InvalidParameterException;
import java.time.LocalDate;
public static long getDaysBetween(LocalDate start, LocalDate end){
if(start.isAfter(end)){
throw new InvalidParameterException(&quot;start is later than end&quot;);

