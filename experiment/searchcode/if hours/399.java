/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author 2012
 */
import java.util.Scanner;
public class problem_2 
{
    double min;
    int num_of_hours;
    public double calculateCharges(int num_of_hours)
    {
        min=2.00;
        if(num_of_hours<=3)
        {
            return 2.00;
        }
        else if(num_of_hours>19)
        {
            return 10;
        }
        
        else 
        
            while(num_of_hours>=4)
            {
            min+=0.50;
            num_of_hours--;
           
        }
         return min;
    }

        
    
    public static void main(String ar[])
    {
        Scanner s=new Scanner(System.in);
        System.out.println("Enter number of hours: ");
        int hours=s.nextInt();
        if(hours>24)
        {
            System.out.println("No car parks for more than 24 hours");
            
        }
            
        
       problem_2 pp=new problem_2();
      System.out.print(pp.calculateCharges(hours) + "$");
       
    }
    
}

