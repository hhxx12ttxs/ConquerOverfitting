
/**
 * Write a description of class Prog213a here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */

import.java.util.Scanner;
import java.io.*;
public class Prog213a
{
    public static void main(String[] args) {
        double wages = 0;
        int hours;
        int weeklyHours = 0;
        try {
            inFile = new Scanner (new File("prog213a.txt"));
        } 
        catch (FileNotFoundException e) {
            System.out.println ("File not found!");
            System.exit (0);
        }
        System.out.print("Hours Worked: ");
        for (
        for (int a= 0; a <= 7; a++) {
            hours = inFile.nextInt();
            weeklyHours += hours;
            wages += (30 * hours);
            if (hours > 8) {
                wages += (25.5 * (hours - 8));
            }
            if (weeklyHours > 40) {
                wages += (15 * hours
            }
        }
        
    }
}

