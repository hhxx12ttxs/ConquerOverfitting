package chapter_5.ex_05_07;


import java.time.LocalDateTime;

public class TimeInterval {
public TimeInterval(LocalDateTime start, LocalDateTime end) {
if(start.isAfter(end))
throw new IllegalArgumentException(&quot;interval time is inverse &quot;);

