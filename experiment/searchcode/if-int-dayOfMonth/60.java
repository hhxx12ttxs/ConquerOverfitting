public class Date {

private String date;

public Date(int dayOfMonth, int month, int year) {
if (dayOfMonth < 10) {
this.date = &quot;0&quot; + dayOfMonth;
} else {
this.date = &quot;&quot; + dayOfMonth;
}

if (month < 10) {

