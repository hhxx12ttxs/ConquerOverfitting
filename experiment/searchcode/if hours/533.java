package zmp.parkinggaragepos;

/**
 *
 * @author Zachary Prisk
 * 
 * This hourly parking fee strategy is the default for Thrifty Fee Garage.
 */
public class DefaultHourlyThriftyFeeCalculator implements HourlyParkingFeeStrategy
{
    private double minimumFeePrice = 1.50;
    private double minimumHours = 2.00;
    private double hourlyRateAfterMinimumHours = 0.75;
    
    @Override
    public double getFeeAmount(double hours)
    {
       double totalFee = 0;
       
        if (hours <= 3)
        {
            totalFee = minimumFeePrice;
        }
        else
        {
            double extraHours = Math.ceil(hours - minimumHours);
            double extraHoursCost = extraHours * hourlyRateAfterMinimumHours;
            totalFee = extraHoursCost + minimumFeePrice;
        }
     return totalFee;
    }    
}

