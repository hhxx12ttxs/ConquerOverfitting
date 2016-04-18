package main;

public class WeekdayPrices extends PreBookedTicket
{
    static double price;

    public static void weekday()
    {

        if ((hoursBooked == 1) || (ExitBarrier.hoursToCharge == 1 || ExitBarrier.differenceHours == 1))
        {
            price = 4.70;
        }
        else if ((hoursBooked == 2) || (ExitBarrier.hoursToCharge == 2) || (ExitBarrier.differenceHours == 2))
        {
            price = 7.40;
        }
        else if ((hoursBooked == 3) || (ExitBarrier.hoursToCharge == 3) || (ExitBarrier.differenceHours == 3))
        {
            price = 10.30;
        }
        else if ((hoursBooked == 4) || (ExitBarrier.hoursToCharge == 4) || (ExitBarrier.differenceHours == 4))
        {
            price = 10.30;
        }
        else if ((hoursBooked == 5) || (ExitBarrier.hoursToCharge == 5) || (ExitBarrier.differenceHours == 5))
        {
            price = 14.80;
        }
        else if ((hoursBooked == 6) || (ExitBarrier.hoursToCharge == 6) || (ExitBarrier.differenceHours == 6))
        {
            price = 14.80;
        }
        else if ((hoursBooked == 7) || (ExitBarrier.hoursToCharge == 7) || (ExitBarrier.differenceHours == 7))
        {
            price = 17.80;
        }
        else if ((hoursBooked == 8) || (ExitBarrier.hoursToCharge == 8) || (ExitBarrier.differenceHours == 8))
        {
            price = 17.80;
        }
        else if ((hoursBooked == 9) || (ExitBarrier.hoursToCharge == 9) || (ExitBarrier.differenceHours == 9))
        {
            price = 17.80;
        }
        else if ((hoursBooked == 10) || (ExitBarrier.hoursToCharge == 10) || (ExitBarrier.differenceHours == 10))
        {
            price = 20.20;
        }
        else if ((hoursBooked == 11) || (ExitBarrier.hoursToCharge == 11) || (ExitBarrier.differenceHours == 11))
        {
            price = 20.20;
        }
        else if ((hoursBooked == 12) || (ExitBarrier.hoursToCharge == 12) || (ExitBarrier.differenceHours == 12))
        {
            price = 20.20;
        }
        else if ((hoursBooked <= 24) || (ExitBarrier.hoursToCharge <= 24) || (ExitBarrier.differenceHours <= 24))
        {
            price = 23.70;
        }

    }

}

