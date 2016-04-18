/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author mvp5542
 */
public class Garage {
    
    private double hours;
    private double charges=0;
    private double total;
    
    public void setHours(double hours)
    {
        while(hours > 0 && hours < 24)
            this.hours = hours;
    }
    public double getHours(double hours)
    {
        return this.hours;
    }
   
    public double getTotal()
    {
        return this.total;
    }
    
    public double calculateCharges(double h)
    {
        charges=0;
        if(h > 0.00 && h <= 3.00)
        {
            charges = 2.00;
        }
        else if (h > 3)
        { 
             charges = (h - 3)*0.5 + 2.00;
             if(charges > 10)
             {
                 charges = 10;
             }
        }
        if(charges < 10)
        total += charges;
        else
        total += 10.00;
        
        return charges;
    }
    
    
             
    
}

