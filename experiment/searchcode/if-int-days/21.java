int days;

public Days() {
this.days = 0;
}

public Days(int days) {
this();
setDays(days);
}

public void setDays(int days) {
if (days >= 0) {
this.days = days;
}
}

public int getDays() {

