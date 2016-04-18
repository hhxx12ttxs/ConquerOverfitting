/*Chapter 3 Challenge 13*/

import javax.swing.JOptionPane;

public class PackageHoursCost
{
		public static void main(String[] args)
		{
         //Declare Variables
         String packageOne, hoursOne, hoursTwo, hoursThree;
         double hoursA, hoursB, hoursC, totalA, totalB, cost, sum = 0;
         char packageLetter;
         int ten = 10;
         
			// print prompts and get input
			packageOne=JOptionPane.showInputDialog(null,"Choose which package you have: ");
				packageLetter = packageOne.charAt(0);      
            
         //Find Package
         switch(packageLetter)
         {
            case 'a':
            case 'A':
            			hoursOne=JOptionPane.showInputDialog(null,"Enter the amount of hours used in Package A: ");
				hoursA = Integer.parseInt(hoursOne);
            
                     if (hoursA<11){
                        totalA = 9.95;
                        }
                     else {
                        sum = 9.95;
                        hoursA = hoursA-10; 
                        cost = hoursA*2;
                        totalA = sum + cost;
                        }
            
               System.out.println("Your total charge is: $" + totalA +".");
               break;
            
            case 'b':
            case 'B':
            
            			hoursTwo=JOptionPane.showInputDialog(null,"Enter the amount of hours used in Package B: ");
				hoursB = Integer.parseInt(hoursTwo);
            
                     if (hoursB<21){
                        totalB = 13.95;
                        }
                     else {
                        sum = 13.95;
                        hoursB = hoursB-20; 
                        cost = hoursB*1;
                        totalB = sum + cost;
                        }
            
               System.out.println("Your total charge is: $" + totalB +".");
               break;      
              
            case 'c':
            case 'C':
            
               System.out.println("Your total charge is: $19.95.");
               break;   
               
            default:
               System.out.println("That is not an available package.");   
         }   
      }
}      
