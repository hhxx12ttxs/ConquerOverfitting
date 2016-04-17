/**
 * Represents an employee that gets paid by the hour
 *
 * @author Steven Kemp, Period 3, Table 5
 * @version March 15, 2016
 */
public class Hourly extends Employee
{
    private int hoursWorked;

    /**
     * Sets up this hourly employee using the specified information
     */
    public Hourly (String eName, String eAddress, String ePhone, String socSecNumber, double rate)
    {
        super (eName, eAddress, ePhone, socSecNumber, rate);

        hoursWorked = 0;
    }

    /**
     * Adds the specified number of hours to this employee's accumulated hours
     */
    public void addHours(int moreHours)
    {
        hoursWorked += moreHours;
    }

    /**
     * Computes and returns the pay for this hourly employee
     */
    public double pay()
    {
        double payment = 0;

        if (hoursWorked > 40) {
            payment += (getPayRate() * 1.5) * (hoursWorked - 40);
            hoursWorked -= 40;
        }

        payment += getPayRate() * hoursWorked;

        hoursWorked = 0;

        return payment;
    }

    /**
     * Returns information about this hourly employee as a string
     */
    public String toString()
    {
        String result = super.toString();

        result += "\nCurrent hours: " + hoursWorked;

        return result;
    }
}

