/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

/**
 *
 * @author Cvety
 */
public class Task {

    private final String name;
    private int workingHours;

    public Task(final String name, int workingHours) {
        if (name != null && name.matches("[A-Z][a-zA-Z]*")) {
            this.name = name;
        } else {
            this.name = "Task";
        }
        if (workingHours > 0 && workingHours <= 100) {
            this.workingHours = workingHours;
        } else {
            this.workingHours = 50;
        }
    }

    //getyri
    public String getName() {
        return name;
    }

    public int getWorkingHours() {
        return workingHours;
    }

    //setyr
    public void setWorkingHours(int workingHours) {
        if (workingHours >= 0 && workingHours <= 100) {
            this.workingHours = workingHours;
        } else {
            this.workingHours = 0;
        }
    }

    @Override
    public String toString() {
        return "Task{" + "name = " + name + " workingHours = " + getWorkingHours() + '}';
    }
}

