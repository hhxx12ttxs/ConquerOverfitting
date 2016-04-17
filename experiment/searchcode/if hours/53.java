package com.kblyumkin.lecture1.homeWork;

public class ClockAngel {
    public static void main(String[] args) {
        int hours = 27;
        int mins = 430;
        int result = 0;

        hours = hours % 12;
        mins = mins % 60;
        mins = (mins / 5) * 30;
        hours = hours * 30;
        if (hours > mins) result = hours - mins;
        else result = mins - hours;

        if (result > 180) result = 360 - result;

        System.out.println("Anglel between hours and minute narrows is " + result);

    }
}

