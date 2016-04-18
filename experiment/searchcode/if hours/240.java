



public class Payroll 
{

    
    private String name;
    private double hoursWorked, payRate;
    int idNum;
    
   public Payroll( String ename, int id)
   {
       name = ename;
       hoursWorked = 0;
       payRate = 0;
       idNum = id;
       
   }
   
   public double setPayRate( double rate)
   {
     payRate = rate;  
     return payRate;
     
   }
   
   public double setHoursWorked( double setHours)
   {
       hoursWorked = setHours;
       return hoursWorked;
   }
   public double grossPay ()
   {
    	double pay = 0;	

        if (hoursWorked< 40) 
            {pay = hoursWorked*payRate;
 	
			 } 
	else if (hoursWorked<=40) {
            pay = hoursWorked*payRate;
       		}  
	else if (hoursWorked>40.1) {
            pay = 40*payRate+(hoursWorked-40)*(1.5*payRate);
                }
 	return pay;
     }
}

