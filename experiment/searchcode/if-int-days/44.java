public class Month {

private int days;
private int month;
public Year year;

public Month(int m, Year y) {
year = new Year(y);

}

public int findDays() {

switch (month) {

case 1:
days = 31;
break;

case 2:

