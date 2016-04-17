package parkingticketammended;

public class OverstayedPrice
{
    private double cost = 0;

    private int hours = 0;

    public OverstayedPrice(OverstayedTime hours)
    {
        this.hours = hours.getOverstayedHours();

        if (this.hours == 1)
        {

            this.cost = 4.32;
        }
        else if (this.hours <= 2)
        {
            this.cost = 6.66;
        }
        else if (this.hours <= 4)
        {
            this.cost = 9.27;
        }
        else if (this.hours <= 6)
        {
            this.cost = 13.32;
        }
        else if (this.hours <= 9)
        {
            this.cost = 16.02;
        }
        else if (this.hours <= 12)
        {
            this.cost = 18.18;
        }
        else if (this.hours <= 24)
        {
            this.cost = 21.33;
        }

    }

    public double getExtraCost()
    {

        return cost;

    }
}

