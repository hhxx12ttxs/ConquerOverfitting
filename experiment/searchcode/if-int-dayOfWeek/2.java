Sunday(7);

private int value;

private DayOfWeek(int value) {
this.value = value;
}

public int getValue() {
return value;
}

public static DayOfWeek getDayOfWeek(int dayOfWeek) throws IllegalArgumentException {

