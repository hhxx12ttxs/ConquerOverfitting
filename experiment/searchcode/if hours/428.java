public class FullTimeEmployee extends Employee{
    
    public FullTimeEmployee(){
        super();
    }
   
    public double getPay(){
        double emppay;
        if(hours>40)
            emppay = ((hours - 40)*rate) + hours * rate;
        else
            emppay = hours * rate;
        totalPay += emppay;
        return emppay;
    }    
}

