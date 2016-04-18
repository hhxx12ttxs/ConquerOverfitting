public class Salary 
{
    private double hourlyWage;
    private int hoursWorked;
    
    double getHours()
    {
        return hoursWorked;
    }
    
    double getHourlyWage()
    {
        return hourlyWage;
    }
    
    void setHours(int newHours)
    {
        hoursWorked = newHours;
    }
    
    void setHourlyWage(double newHourlyWage)
    {
        hourlyWage = newHourlyWage;
    }
    
    double calculatePay()
    {
        double totalPay;
        if(hoursWorked >= 30)
        {
            if(hoursWorked >=40)
            {
                totalPay = ((40 * hourlyWage) + ((hoursWorked - 40) * hourlyWage * (1.5)));
                totalPay = totalPay * .9;
            }
            else
            {
                totalPay = (hoursWorked * hourlyWage);
                totalPay = totalPay * 0.9;
            }
        }
        else
        {
            totalPay = hoursWorked * hourlyWage;
        }
        return totalPay;
    }  
}

