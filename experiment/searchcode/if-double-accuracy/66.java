Log10 log10 = new Log10();

public Formula(double accuracy, boolean fromTable) {
super(accuracy);
log10 = new Log10(accuracy, fromTable);
}

public Formula(double accuracy) {
super(accuracy);
}

public Formula() {

