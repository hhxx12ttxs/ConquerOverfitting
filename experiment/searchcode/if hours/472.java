import hsa.*;
public class EmployeePay {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Console c=new Console();
        double e1,e2,e3,e4,e5;
        double hours;
        double s1,s2,s3,s4,s5;
       
       
        for(int x=1; x<=5;x++)
        { 
           
           c.print("Enter the amount of hours for employee "+x+" (between 1 and 70): ");
           hours=c.readDouble();
        
        
            
           if(x==1)
           {
               e1=hours;
           }
           else if(x==2)
           {
               e2=hours;
           }
           else if(x==3)
           {
               e3=hours;
           }
           else if(x==4)
           {
               e4=hours;
           }
           else
               e5=hours;
           
       }
           
       
       
    }
    
}

