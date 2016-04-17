/**
 * Created by Farooq on 2/14/2016.
 */
public final class HourlyEmployee extends Employee {

    private final static double _maxHours = 168.0;
    private final static double _minHours = 0.0;

    private double _wage;
    private double _hours;

    public HourlyEmployee(
            String firstName,
            String lastName,
            String ssn,
            double wage,
            double hours) {
        super(firstName, lastName, ssn);

        validateAndSetWage(wage);
        validateAndSetHours(hours);
    }

    public static double getMaxHours() {
        return _maxHours;
    }

    public static double getMinHours() {
        return _minHours;
    }

    @Override
    public double getPaymentAmount() {

        if (getHours() <= 40.0) {
            return getWage() * getHours();
        }

        return 40.0 * getWage() + (getHours() - 40.0) * getWage() * 1.5;
    }

    public double getWage() {
        return _wage;
    }

    public void setWage(double wage) {

        validateAndSetWage(wage);
    }

    public double getHours() {
        return _hours;
    }

    public void setHours(double hours) {

        validateAndSetHours(hours);
    }

    @Override
    public String toString() {
        return String.format(
                "hourly employee: %s%nhourly wage: $%,.2f; hours worked: %,.2f",
                super.toString(),
                getWage(),
                getHours()
        );
    }

    private void validateAndSetHours(double hours) {
        if (hours < getMinHours() || hours > getMaxHours()) {
            throw new IllegalArgumentException(
                    String.format("Hours must be between %,.2f and %,.2f hours.",
                            getMinHours(),
                            getMaxHours())
            );
        }

        this._hours = hours;
    }

    private void validateAndSetWage(double wage) {
        if (wage <= 0.0) {
            throw new IllegalArgumentException("Wage must be greater than zero.");
        }

        this._wage = wage;
    }

}

