package com.or.clock;

import java.security.PublicKey;

/**
 * Created by orar on 12/9/2015.
 */
public class Clock {
    private int  hours , minutes , seconds ;

    private void trick() {
        hours++;
        minutes++;
        seconds++;

        hours += minutes / 60;
        seconds += minutes / 60;
        hours %= 60;
        seconds %= 60;
        hours %= 24;
    }

    public void Show() {
        if (hours < 10) {
            System.out.print("0");
            System.out.print(hours + ":");
        }
        else if (hours > 10) {
            System.out.print(hours + ":");
        }
        if (minutes < 10) {
            System.out.print("0");
            System.out.print(minutes + ":");
        }
        else if (minutes > 10) {
            System.out.print(minutes + ":");
        }
        if (seconds < 10) {
            System.out.print("0");
            System.out.println(seconds);
        }
        else if (seconds > 10) {
            System.out.println(seconds);
        }
        }



    public void AddOneMinutes(int m) {
        minutes = m;
    }
    public void AddOneHHours(int h){
        hours = h;
    }
    public void AddOneHSecond(int s){
        seconds = s;
    }
    public int GetSecounds(){
        return seconds;
    }
    public int GetMinutes(){
        return minutes;
    }
    public int GetHours(){
        return hours;
    }
    public void RestClock(){
        seconds = 0;
        minutes = 0;
        hours = 0;

    }



    }







