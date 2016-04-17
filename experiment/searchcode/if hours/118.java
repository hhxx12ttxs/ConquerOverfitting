/**
 *
 * @author Gabriela Jurca
 */
public abstract class Employee 
{
    private final String _name;
    private final Double _hoursWorked;
    
    protected final Double fullTimeHours = 40.0;     
    
    public Employee() 
    {
        _name = null;
        _hoursWorked = null;
    }
    
    public Employee(String Name, Double HoursWorked)
    {
        _name = Name; 
        _hoursWorked = HoursWorked;
    }

    public String getName() {
        return _name;
    }

    public Double getHoursWorked() {
        return _hoursWorked;
    }  

    public Double getFullTimeHours() {
        return fullTimeHours;
    }
    
    public Double calculatePay()
    {
        double pay = 0.0;      

        if(!hasWorkedOverTime())
        {
            pay = getHoursWorked() * getHourlyPay();
        }
        else
        {
            double regularHoursPay = fullTimeHours * getHourlyPay();
            
            double overTimeHours = Math.abs(fullTimeHours - getHoursWorked());
            double overTimeHoursPay = overTimeHours * getOverTimeRatio() * getHourlyPay();
            
            pay = regularHoursPay + overTimeHoursPay;
        }       
        
        return pay;
    }   
    
    public boolean hasWorkedOverTime()
    {
        return fullTimeHours - getHoursWorked() < 0;
    }
    
    public abstract Double getHourlyPay();
    public abstract Double getOverTimeRatio();

    
}

