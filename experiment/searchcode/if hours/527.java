package com.kblyumkin.lecture1.homeWork;

public class ClockAngel {
    public static void main(String[] args) {
        int hours = 27;
        int mins = 430;
        int result = 0;
        hours = hours % 12;
        mins = mins % 60;
        float degreesForHours = 360/12;
        float degreesForMins = 360/60;
        float hoursAngel = hours * degreesForHours;
        float minsAngel = mins * degreesForMins;
        if(hoursAngel>minsAngel){
            result = (int)hoursAngel - (int)minsAngel;
        }
        else{
            result = (int)minsAngel - (int)hoursAngel;
        }
        if(result >= 180){
            result = 360 - result;
        }
        System.out.println("Anglel between hours and minute narrows is " + result);
    }
}

