package Warmup_Algorithms;
import java.util.Scanner;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Nageswar
 */
public class TimeConversion 
{
    public static void main(String args[])
    {
        System.out.println("Enter Time:"); // Not allowed in Hacker Rank
        Scanner in = new Scanner(System.in);
        String time = in.nextLine();
        
        
        String hours = time.substring(0,2);
        String rest = time.substring(2,time.length()-2);
        if(time.charAt(time.length()-2) == 'P')
        {
            
            int hours_converted = 0;
            
            String hours_string;
            if(Integer.parseInt(hours) != 12)
            {
                hours_converted = 12 + Integer.parseInt(hours);
                
                hours_string = Integer.toString(hours_converted);
            }
            else
            {
                hours_string = hours;
            }
            
            System.out.println( hours_string+rest);
        }
        else
        {
           if(Integer.parseInt(hours) == 12)
                {
                    hours = "00";
                }
            System.out.println( hours+rest);
        }
    }
}

