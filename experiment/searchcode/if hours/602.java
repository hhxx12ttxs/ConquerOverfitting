/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.domain.assignment3.config.DIP.violation;

/**
 *
 * @author Tristan
 */
public class CalcHours { 
    public int Hours(String title){ 
        int hours;
        if( "Manager".equals(title)) {
            hours = 3; 
        }
        else
            hours = 2; 
        return hours;
    }
    
}

