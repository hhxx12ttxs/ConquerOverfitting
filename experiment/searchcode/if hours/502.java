package ua.di1.university;

import java.text.Format;
import java.util.Formatter;

/**
 * Created by I on 24.01.2016.
 */
public class Subject {
    private int markSystem = 12;
    private String name;
    private int hours;
    private int hoursStudied;
    private int mark;
    private boolean examPassed;
    private float passThreshold = 50;

    public Subject(String name){
        this.name = name;
    }
    public Subject(String name, int hours){
        this.name = name;
        this.hours = hours;
    }

    public boolean passExam(){
        float progress = (float) hoursStudied / hours * 100;
        examPassed = (progress > passThreshold);
        if (!examPassed) return false;
        mark =  Math.round(progress * markSystem / 100);
        return true;
    }

    public String getName() {
        return name;
    }

    public String asString(){
        return String.format("Subject: %s (%d hours course). %d hours studied.",
                name, hours, hoursStudied );
    }

    public int getMark() {
        if (!examPassed) return 0;

        return mark;
    }

    public int study(int hoursStudied) {
        return this.hoursStudied += hoursStudied;
    }

    public int getHoursStudied() {
        return hoursStudied;
    }
}

