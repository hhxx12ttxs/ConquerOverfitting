package model;

public class CompleteHours implements Comparable<CompleteHours> {
private int hours;
public int compareTo(CompleteHours o) {
if (this.hours == o.hours &amp;&amp; this.minutes == o.minutes) {

