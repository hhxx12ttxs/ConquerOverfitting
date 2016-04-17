package parkingticketammended;

public class TimePaidFor
{
    private int hours, mins;

    public TimePaidFor()
    {

        System.out.print("Please enter how long you paid for in hours: ");
        int hours = Utils.scanner.nextInt();

        System.out.print("And minutes: ");
        int mins = Utils.scanner.nextInt();

        if (mins > 0)
        {
            hours++;
        }
        this.hours = hours;
        this.mins = mins;
    }

    public int getLengthPaidForHours()
    {

        return hours;
    }

    public int getLengthofStayMins()
    {
        return mins;
    }
}

