/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package homework2;

/**
 *
 * @author Cvety
 */
public class Task {

    private String name;
    private int workingHours;

    public Task(String name, int workingHours) {
        if(name != null && name.matches( "[A-Z][a-zA-Z]*" )){
            this.name = name;
        }else{
            this.name = "Task";
        }
        if(workingHours > 0 && workingHours <= 100){
            this.workingHours = workingHours;
        }else{
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
        if(workingHours >= 0 && workingHours <= 100){
            this.workingHours = workingHours;
        }else{
            this.workingHours = 10;
        }
    }

    @Override
    public String toString() {
        return "Task{" + "name = " + name + " workingHours = " + workingHours + '}';
    }



}

