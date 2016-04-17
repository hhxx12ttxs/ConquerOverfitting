public class EmployeeRecord{
    private String name;
    private int[] hours;
    private double hourlyWage;
    private double averageHours;
    private double averagePay;
    private int totalHours;
    private double WeeklyPay;
    private int overtimeHours;
    private int normalHours;
    private double SpecialPay;
    private double DailyOvertimePay;
    private double SpecialOvertimeHours;
    
    public EmployeeRecord(String initName){
        name = initName;
        hours = new int[7];
        hourlyWage = 0;
    }

    public String getName(){
        return name;
    }

    public double getHourlyWage(){
        return hourlyWage;
    }
    
    public void setHourlyWage(double w){
        hourlyWage = w;
    }

    public void setHours(int day, int h){
        hours[day] = h;
    }

    public double computeWeeklyPay(){
    //Any hours over 40 in a week are overtime
    //Any hours over 8 in one day are overtime
    //Overtime is time and a half
    //You write this method
    WeeklyPay = 0;
    overtimeHours = 0;
    normalHours = 0;
    DailyOvertimePay = 0;
    for (int i=0; i < hours.length; i++)
        {
         if (hours[i] > 8)
            {
                overtimeHours = overtimeHours + (hours[i] - 8);
                normalHours = normalHours + (hours[i] - overtimeHours);
            }
         else
             {
                normalHours = normalHours + hours[i];
             }
        }
    totalHours = overtimeHours + normalHours;
    DailyOvertimePay = overtimeHours*(hourlyWage*1.5);
    SpecialPay = 0;
    SpecialOvertimeHours = 0;
    if (totalHours > 40)
       {
            SpecialOvertimeHours = (totalHours-40)- overtimeHours;
            normalHours = 40;
            SpecialPay = (SpecialOvertimeHours*(hourlyWage*1.5));
       }
    WeeklyPay = DailyOvertimePay + SpecialPay + (normalHours*hourlyWage);
    return WeeklyPay;

    }
    
    public double getHours(){
    //return the total number of hours worked for the week
    //for this employee
    totalHours = 0;
    for (int i=0; i < hours.length; i++)
        {
        totalHours = totalHours + hours[i];
        }
    return totalHours;

    }
    public double averageHoursPerDay(){
    //return the average number of hours worked per day
    //for this employee
    averageHours = 0;
    for (int i=0; i < hours.length; i++)
        {
            averageHours = hours[i] + averageHours; 
        }
    averageHours = averageHours/(hours.length);
    return averageHours;
    }
    
    public double averagePayPerDay() 
    {
    //return the average pay per day
    //for this employee
    averagePay = WeeklyPay/hours.length;
    return averagePay;
    }




}


