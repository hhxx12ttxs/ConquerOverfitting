/**
 * Exercise 1 –  Pay Day
 * Exercise 2 – Adding Machine
 * @author Zhipeng Mei
 * CCSF ID: w10479122
 * Date: Sept 19, 2015
 */

import java.util.Scanner;
public class payDay_AddMachine {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        Scanner in = new Scanner (System.in);
        
        boolean choice = false;
        while(!choice)
        {
            menu();
            if(in.hasNextInt())
            {
                int chose = in.nextInt();
                if(chose > 0 && chose < 3)
                {
                    switch(chose)
                    {
                        case 1: System.out.println("Exercise 1\n");
                                exercise1();
                                break;
                        case 2: System.out.println("Exercise 2\n");
                                exercise2();
                                break;
                        default: exercise1();
                                break;
                    }

                }//if condition

                else{
                    System.out.println("Input Error, please enter a valid choice.\n");
                }
            }//if hasNextInt statement

            else
            {
              String input = in.next();

              if(input.equals("Q"))
              {
                  System.out.println("Quitting...");
                  choice = true;
              }
              else
              {
                  System.out.println("Input Error, please enter a valid choice.\n");
              }
            }
        }//while loop, !choice condition
        
        
    }           
              
    
      public static void menu() 
      {
        System.out.println("Menu:");
        System.out.println("1. Pay Day");
        System.out.println("2. Adding Machine");
        System.out.println("Q to quit");
        System.out.println("Enter 1 or 2 to continue.");
    }
      
      public static void exercise1()
    {
        int mondayHours = 0;
        int tuesdayHours = 0;
        int wednesdayHours = 0;
        int thursdayHours = 0;
        int fridayHours = 0;
        
        mondayHours = monday(mondayHours);
        tuesdayHours = tuesday(tuesdayHours);
        wednesdayHours = wednesday(wednesdayHours);
        thursdayHours = thursday(thursdayHours);
        fridayHours = friday(fridayHours);
       
        double totalWeeklyBonus = weeklyBonus(mondayHours, 
                                    tuesdayHours,  wednesdayHours, 
                                    thursdayHours,  fridayHours);
                   
                         
        double totalWeeklyPay = mondayPay(mondayHours) + tuesdayPay(tuesdayHours) +
                            wednesdayPay(wednesdayHours) + thursdayPay(thursdayHours) +
                            fridayPay(fridayHours) + totalWeeklyBonus;
       
        
         System.out.println("The total pay for the week is: " + totalWeeklyPay + "\n");
         
         
    }
    
    //calculation of totalMondayPay
    public static double mondayPay(int mondayHours)
    {   
        double totalMondayPay = 0;
        
        double regularHour;
        int dailyOT;
        double otPay;
                        
        if (mondayHours > 8)
        {  
            regularHour = 8 * 12.50;
            dailyOT = mondayHours - 8;
            otPay = dailyOT * (12.50 * 1.5);
            totalMondayPay = regularHour + otPay;            
        }
        else {
            regularHour = mondayHours * 12.50;
            totalMondayPay = regularHour;          
        }
    
        return totalMondayPay;
    }
    
    //calculation of totalTuesdayPay
    public static double tuesdayPay(int tuesdayHours)
    {   
        double totalTuesdayPay = 0;
        
        double regularHour;
        int dailyOT;
        double otPay;
                        
        if (tuesdayHours > 8)
        {  
            regularHour = 8 * 12.50;
            dailyOT = tuesdayHours - 8;
            otPay = dailyOT * (12.50 * 1.5);
            totalTuesdayPay = regularHour + otPay;            
        }
        else {
            regularHour = tuesdayHours * 12.50;
            totalTuesdayPay = regularHour;          
        }   
        return totalTuesdayPay;
    }
    
    //calculation of totalWednesdayPay
    public static double wednesdayPay(int wednesdayHours)
    {   
        double totalWednesdayPay = 0;
        
        double regularHour;
        int dailyOT;
        double otPay;
                        
        if (wednesdayHours > 8)
        {  
            regularHour = 8 * 12.50;
            dailyOT = wednesdayHours - 8;
            otPay = dailyOT * (12.50 * 1.5);
            totalWednesdayPay = regularHour + otPay;            
        }
        else {
            regularHour = wednesdayHours * 12.50;
            totalWednesdayPay = regularHour;          
        }   
        return totalWednesdayPay;
    }
    
        //calculation of totalThursdayPay
       public static double thursdayPay(int thursdayHours)
    {   
        double totalThursdayPay = 0;
        
        double regularHour;
        int dailyOT;
        double otPay;
                        
        if (thursdayHours > 8)
        {  
            regularHour = 8 * 12.50;
            dailyOT = thursdayHours - 8;
            otPay = dailyOT * (12.50 * 1.5);
            totalThursdayPay = regularHour + otPay;            
        }
        else {
            regularHour = thursdayHours * 12.50;
            totalThursdayPay = regularHour;          
        }   
        return totalThursdayPay;
    }
       
        //calculation of totalFridayPay
        public static double fridayPay(int fridayHours)
    {   
        double totalFridayPay = 0;
        
        double regularHour;
        int dailyOT;
        double otPay;
                        
        if (fridayHours > 8)
        {  
            regularHour = 8 * 12.50;
            dailyOT = fridayHours - 8;
            otPay = dailyOT * (12.50 * 1.5);
            totalFridayPay = regularHour + otPay;            
        }
        else {
            regularHour = fridayHours * 12.50;
            totalFridayPay = regularHour;          
        }   
        return totalFridayPay;
    }
    
    //calculation of weekly bonus only if work more than 40 hours
    /**
     *
     * @param totalWeekBonus
     * @param totalWeekHours
     * @param mondayHours
     * @param tuesdayHours
     * @param wednesdayHours
     * @param thursdayHours
     * @param fridayHours
     * @return
     */
    public static int weeklyBonus(int mondayHours, 
                                   int tuesdayHours, int wednesdayHours, 
                                   int thursdayHours, int fridayHours)
    {              
        int WeekHours = mondayHours + tuesdayHours + wednesdayHours
                           + thursdayHours + fridayHours;
        
        int totalWeekBonus = 0;
        
        if(WeekHours > 40)
        {
            int totalWeekHours = WeekHours - 40;
            totalWeekBonus = totalWeekHours * 5;
            return totalWeekBonus;
        }
        
        return totalWeekBonus;
    }
    
    
    
    //importing all the work day hours from Mon-Fri
    public static int monday(int mondayHours)
{
    Scanner in = new Scanner(System.in);
        boolean done = false;
        while(!done)
        {
            System.out.println("How many hours did you work on Monday: ");
            if(in.hasNextInt())
            {
                int hours = in.nextInt();
                
                if(hours >= 0 && hours < 25)
                {
        
                   mondayHours = hours;
                   return mondayHours; 
                   
                }
                else
                {
                   System.out.println("Input Error, please enter a valid choice.\n");
                }
            }
            else
            {
              String input = in.next();

              if(input.equals("Q"))
              {
                  System.out.println("Quitting...");
                  done = true;
              }
              else
              {
                  System.out.println("Input Error, please enter a valid choice.\n");
              }
            }   
        } return mondayHours;
}
    
    //Tuesday
    public static int tuesday(int tuesdayHours)
{
    Scanner in = new Scanner(System.in);
        boolean done = false;
        while(!done)
        {
            System.out.println("How many hours did you work on Tuesday: ");
            if(in.hasNextInt())
            {
                int hours = in.nextInt();
                
                if(hours >= 0 && hours < 25)
                {
        
                   tuesdayHours = hours;
                   return tuesdayHours; 
                   
                }
                else
                {
                   System.out.println("Input Error, please enter a valid choice.\n");
                }
            }   
            else
            {
              String input = in.next();

              if(input.equals("Q"))
              {
                  System.out.println("Quitting...");
                  done = true;
              }
              else
              {
                  System.out.println("Input Error, please enter a valid choice.\n");
              }
            }   
        } return tuesdayHours;
}
 
     //Wednesday
    public static int wednesday(int wednesdayHours)
{
    Scanner in = new Scanner(System.in);
        boolean done = false;
        while(!done)
        {
            System.out.println("How many hours did you work on Wednesday: ");
            if(in.hasNextInt())
            {
                int hours = in.nextInt();
                
                if(hours >= 0 && hours < 25)
                {
        
                   wednesdayHours = hours;
                   return wednesdayHours; 
                   
                }
                else
                {
                   System.out.println("Input Error, please enter a valid choice.\n");
                }
            }
            else
            {
              String input = in.next();

              if(input.equals("Q"))
              {
                  System.out.println("Quitting...");
                  done = true;
              }
              else
              {
                  System.out.println("Input Error, please enter a valid choice.\n");
              }
            }   
        } return wednesdayHours;
}
 
    //thursday
    public static int thursday(int thursdayHours)
{
    Scanner in = new Scanner(System.in);
        boolean done = false;
        while(!done)
        {
            System.out.println("How many hours did you work on Thursday: ");
            if(in.hasNextInt())
            {
                int hours = in.nextInt();
                
                if(hours >= 0 && hours < 25)
                {
        
                   thursdayHours = hours;
                   return thursdayHours; 
                   
                }
                else
                {
                   System.out.println("Input Error, please enter a valid choice.\n");
                }
            }
            else
            {
              String input = in.next();

              if(input.equals("Q"))
              {
                  System.out.println("Quitting...");
                  done = true;
              }
              else
              {
                  System.out.println("Input Error, please enter a valid choice.\n");
              }
            }   
        } return thursdayHours;
}
 
    //Friday
    public static int friday(int fridayHours)
{
    Scanner in = new Scanner(System.in);
        boolean done = false;
        while(!done)
        {
            System.out.println("How many hours did you work on Friday: ");
            if(in.hasNextInt())
            {
                int hours = in.nextInt();
                
                if(hours >= 0 && hours < 25)
                {
        
                   fridayHours = hours;
                   return fridayHours; 
                   
                }
                else
                {
                   System.out.println("Input Error, please enter a valid choice.\n");
                }
            }
            else
            {
              String input = in.next();

              if(input.equals("Q"))
              {
                  System.out.println("Quitting...");
                  done = true;
              }
              else
              {
                  System.out.println("Input Error, please enter a valid choice.\n");
              }
            }   
        } return fridayHours;
    }
    
    public static void exercise2()
    {
     Scanner input = new Scanner(System.in);
        int subtotal = 0;
        int total = 0; 
        int numbersRead=0;
        System.out.println("Enter numbers and enter 0 to get result.");
        while(true)
        {
            while(true)
            {
                int number = input.nextInt();
                numbersRead++;
                
                if(number == 0)
                {
                    break;
                }
                subtotal = subtotal + number;
            }
            
            total = total + subtotal;
            if(subtotal != 0)
            {
                System.out.println("subtotal: " + subtotal+"\n");
                subtotal = 0;
            }else
            {
                System.out.println("total: " + total+"\n");
                break;
            }
        }   
        
    }
}

