/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tpc;
import java.io.Serializable;
/**
 *
 * @author ngsm
 */
public class Task implements Serializable {
    
    private String taskNum;
    private String description;
    private int estHours;
    private int actualHours;
    private String status;
    
    /** 
     * Constructor, initialize taskNum, description, estimated hours
     * but set the status to "outstanding" and "actual hours" to 0
     * @param taskNum is based on the project
     * @param description 
     * @param estHours 
     */
    public Task(String taskNum, String description, int estHours)
    {
        this.taskNum = taskNum;
        this.description = description;
        this.estHours = estHours;
        this.actualHours = 0;
        this.status = "outstanding";
    }

    public String getTaskNum() {
        return taskNum;
    }

    public void setTaskNum(String taskNum) {
        this.taskNum = taskNum;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getActualHours() {
        return actualHours;
    }

    public void setActualHours(int actualHours) {
        if (actualHours >= 0)
            this.actualHours = actualHours;
    }

    public int getEstHours() {
        return estHours;
    }

    
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setEstHours(int estHours) {
       if (estHours >=0)
        this.estHours = estHours;
    }

    @Override
    public String toString() {
        return "taskNum=" + taskNum + ", description=" + description + ", estHours=" + estHours + ", actualHours=" + actualHours + ", status=" + status;
    }

       
}

