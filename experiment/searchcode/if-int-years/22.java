public Prosecutor(String name, int yearsInBusiness, int numberOfCases) {
super(name, yearsInBusiness, numberOfCases);
}

public void setYearsInBusiness(int yearsInBusiness) {
if (yearsInBusiness < 10) {
throw new IllegalArgumentException(

