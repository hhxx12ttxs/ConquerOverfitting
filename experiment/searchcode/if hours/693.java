public class HourlyEmployee extends Employee {
    private double wage; 
    private double hours; //hours worked for the week
    
    //constructor
    public HourlyEmployee(String firstName, String lastName, 
            String socialSecurityNumber, double wage, double hours){
        super(firstName, lastName, socialSecurityNumber); 
        
        if(wage<0.0)
            throw new IllegalArgumentException("hourly wage must be >= 0.0"); 
        
        if((hours<0.0)||(hours>168.0))
            throw new IllegalArgumentException("hours worked must be >= 0.0 and <= 168.0"); 
        
        this.wage=wage;
        this.hours=hours;
    }
    
    //setwage
    public void setWage(double wage){
        if(wage<0.0)
            throw new IllegalArgumentException("hourly wage must be >= 0.0"); 
        
        this.wage=wage;    
    }
    //return wage
    public double getWage(){
        return wage;
    }
    
    //setwage
    public void setHours(double hours){
       if((hours<0.0)||(hours>168.0))
            throw new IllegalArgumentException("hours worked must be >= 0.0 and <= 168.0"); 
        
        this.hours=hours;
    }
    
    //return wage
    public double getHours(){
        return hours;
    }
    
    //calculate earnings; override abstract method earning in employee
    @Override
    public double earnings(){
        if(getHours() <=40)//no overtime
            return getWage()*getHours();
        else
            return 40*getWage()+(getHours()-40)*getWage()*1.5;
    }
    
    
    //return String representation of HourlyEmployee object
    @Override 
    public String toString(){
        return String.format("hourly employee: %s%n%s: $%,.2f; %s: %,.2f",  
               super.toString(), "hourly wage", getWage(), "hours worked", getHours());
    }
}

