/*
 * The MIT License
 *
 * Copyright 2014 Christos Petropoulos, Paula Subías.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package dsmv_simulation;

import java.util.stream.IntStream;

/**
 *
 * @author Christos Petropoulos, Paula Subías
 */
public class Activities {
    
    private int activity_hours[];
    
    public Activities(int activity_hours[]){
        if(IntStream.of(activity_hours).sum()!=24)
            throw new IllegalArgumentException("The sum of the hours has to be 24.");
        this.activity_hours = activity_hours.clone();
    }
    
    public void setWorkingHours(int hours){
        this.activity_hours[0] = hours;
    }

    public void setLeisuringHours(int hours){
        this.activity_hours[1]  = hours;
    }
    
    public void setRestingHours(int hours){
        this.activity_hours[2]  = hours;
    }
     
    public void setSleepingHours(int hours){
        this.activity_hours[3]  = hours;
    }   
    
    public void decreaseWorkingHours(){
        this.activity_hours[0]--;
    }

    public void decreaseLeisuringHours(){
        this.activity_hours[1]--;
    }
    
    public void decreaseRestingHours(){
        this.activity_hours[2]--;
    }
    
    public void decreaseSleepingHours(){
        this.activity_hours[3]--;
    }
    
    public void set(int i, int value){
        this.activity_hours[i] = value;
    }
    
    public void decrease(int i){
        this.activity_hours[i]--;
    }    
    
    public void increase(int i){
        this.activity_hours[i]++;
    }
    
    public int getWorkingHours(){
        return this.activity_hours[0];
    }

    public int getLeisuringHours(){
        return this.activity_hours[1];
    }
    
    public int getRestingHours(){
        return this.activity_hours[2];
    }
    
    public int getSleepingHours(){
        return this.activity_hours[3];
    }
    
    public int[] getActivityHours(){
        return this.activity_hours.clone();
    }
    public int get(int i){
        if(i>=this.activity_hours.length)
            throw new IllegalArgumentException("Invalid index for activity.");
        return this.activity_hours[i];
    }   
    
    public boolean hoursLeft(){
        for(int i=0;i<4;i++){
            if(this.activity_hours[i]>1)
                return true;
        }
        return false;
    }    
    
    public void print(){
        System.out.println("sleep:"+this.getSleepingHours()+" work:"+this.getWorkingHours()
                          +" leisure:"+this.getLeisuringHours()+" rest:"+this.getRestingHours());
    }
}
