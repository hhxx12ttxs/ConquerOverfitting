package parkingticketammended;

public class AloudLeavingTime
{

    private int timeHours, timeMinutes;

    private int counter = 0;

    LengthOfStay length;

    ArrivalTime time;

    public AloudLeavingTime(LengthOfStay length, ArrivalTime time)
    {
        this.length = length;
        this.time = time;
        this.timeHours = (time.getArrivalHours() + length.getLengthofStayHours());
        this.timeMinutes = (time.getArrivalMinutes() + length.getLengthofStayMins());
        if (timeMinutes > 59)
        {

            this.timeMinutes = (timeMinutes - 60);
            this.timeHours++;

        }

        if (timeHours > 23)
        {

            this.timeHours = (timeHours - 24);
            this.counter++;
        }
    }

    public int getAloudLeavingHours()
    {

        return timeHours;
    }

    public int getAloudLeavingMinutes()
    {

        return timeMinutes;
    }

    public int getCounter()
    {

        return counter;

    }
}

