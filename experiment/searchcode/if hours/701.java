import hsa.*;
import javax.swing.JOptionPane;

public class EmployeePay {

    public static void main(String[] args) {
        Console c = new Console();
        double[] hours;
        hours= new double[5];
        double[] sal;
        sal= new double[5];
       
        for (int x=1; x<=5; x++){
            while(true){
                c.print("Enter the hours worked of employee bewtween 1 - 70 "+x+": ");
                hours[x-1]= c.readDouble();
                if(hours[x-1] >0 & hours[x-1] <= 70){
                    break;
                }
                else{
                c.print("Invalid.\n");
            }
            }       
        }
        for (int y=0; y<=5; y++){
            if(hours[y]<=40){
                sal[y]=hours[y]*15;
                c.println("The salary of employee " +(y+1)+ " is: $" + sal[y]);
            }
            else{
                hours[y]-= 40;
                sal[y]=hours[y]*22.5 +600;
                c.println("The salary of employee " +(y+1)+ " is: $" + sal[y]);
            }
        }    
    }
    
}

