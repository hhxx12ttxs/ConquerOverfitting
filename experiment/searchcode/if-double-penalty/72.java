protected abstract int getPenaltyAfterDays();
protected abstract double getPenaltyRate();

public double calculateCharge(int rentedDays) {
double amount = getPerRentalCharge();
if (rentedDays > getPenaltyAfterDays()) {

