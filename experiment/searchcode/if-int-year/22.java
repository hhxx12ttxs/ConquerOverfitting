package domain;

public class Year {
private int year;

public Year(int year) {
return new Year(year + 1);
}

public int year() {
return year;
}

public int numberOfYearsInclusive(Year endingYear) {

