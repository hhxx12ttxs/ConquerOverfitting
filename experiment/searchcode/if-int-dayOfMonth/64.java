private final int dayOfMonth;
private final int hashCode;


public Day(int year, int month, int dayOfMonth) {
hashCode = Integer.valueOf(this.year).hashCode()*Integer.valueOf(this.month).hashCode()*Integer.valueOf(this.dayOfMonth).hashCode();
}

public int getYear() {
return year;

