package unimelb.daniel.finances.domain;

public class Year {

private int year;

public Year(int year) {
this.year = year;
}

public int toInt() {
return this.year;
}

public Year nextYear() {

