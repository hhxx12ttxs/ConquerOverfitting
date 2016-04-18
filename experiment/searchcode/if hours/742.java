package com.cherepushko.officesim;

/**
 *
 * @author Panas Cherepushko
 */
public class Position implements Comparable{
    private String position;
    private int salary;
    private int hoursToWork = 0;
    private int workedHours = 0;
    
    public String getPosition(){ return this.position;};
    public int getSalary(){ return this.salary;};

    public Position(String p, int s){
        this.position = p;
        this.salary = s;
    }

    @Override
    public int compareTo(Object o) {
        Position x = (Position) o; 
        if(x.getPosition() != this.position)
            return 1;
        return -1;
    }
    
    public void iWorkedOneHour(){
        if(this.hoursToWork > 0){
            this.hoursToWork--;
            this.workedHours++;
        }
    };
    
    public int getHoursToWork(){ return this.hoursToWork; };
    public int getWorkedHours(){ return this.workedHours; };
    
    public int newTask(int h){ 
        return this.hoursToWork = h >= 1 && h <= 2 ? h : 1;
    };
    
}

