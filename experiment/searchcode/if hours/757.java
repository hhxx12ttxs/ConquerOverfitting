package com.elbudii.homework1;

public class ClockAngel {
    public static void main(String[] args) {
        int hours = 27;
        int mins = 430;

        if (hours >= 0 && hours <= 24 && mins >= 0 && mins <= 60) {
            hours = 30 * hours;
            mins = 6 * mins;
            int result = Math.abs(hours - mins);

            if (result >=0 && result <= 180) {
            } else result = 360 - result; {
                System.out.println("Angel between hours and minute narrows is " + result);
            }
        } else {
            System.out.println("Assign the wrong value");
        }
    }
}

