public Judge(String name, int yearsInBusiness, int numberOfCases) {
super(name, yearsInBusiness, numberOfCases);
}

public void setYearsInBusiness(int yearsInBusiness) {
if (yearsInBusiness < 5) {
throw new IllegalArgumentException(

