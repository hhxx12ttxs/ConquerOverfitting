/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Matthew
 */
public class Time {
    private int hours;
    private int minutes;
    
    public Time(int hours, int minutes)
    {
        setHours(hours);
        setMinutes(minutes);
    }

    public void setHours(int hours) {
        if(hours>=0 && hours<24)
            this.hours = hours;
        else
            this.hours=0;
    }

    public void setMinutes(int minutes) {
        if(minutes>=0 && minutes<60)
            this.minutes = minutes;
        else
            this.minutes=0;
    }
    
    /*public void addMinutes(int minutes)
    {
        int totalMinutes = minutes + this.minutes;
        if(totalMinutes/60!=0)
        {
            setMinutes(totalMinutes%60);
            int totalHours = totalMinutes/60 + this.hours;
            setHours(totalHours%24);
        }
        else
            setMinutes(totalMinutes);
    }*/
    
    public boolean lessThan(Time object)
    {
        if(this.getHours()<object.getHours())
            return true;
        if(this.getHours()==object.getHours())
        {
            if(this.getMinutes()<object.getMinutes())
                return true;
        }
        return false;
    }
    
    public boolean equal(Time object)
    {
        if(this.getHours()==object.getHours()&&
                this.getMinutes()==object.getMinutes())
            return true;
        return false;
    }
    
    public boolean lessThanOrEqual(Time object)
    {
        if(this.lessThan(object) || this.equal(object))
            return true;
        return false;
    }
    
    public int minutesTo(Time object)
    {
        if(object.getHours()==0 && object.getMinutes()==0)
            return (24-getHours()-1)*60 + (60-getMinutes());
        if(object.lessThan(this))
            return -1;
        if(this.equal(object))
            return 0;
        int minutes1 = this.getHours()*60+this.getMinutes();
        int minutes2 = object.getHours()*60+object.getMinutes();
        return minutes2-minutes1;
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }
    
    @Override
    public String toString()
    {
        if(getHours()/10==0 && getMinutes()/10==0)
            return "0"+getHours()+":0"+getMinutes();
        if(getHours()/10==0 && getMinutes()/10!=0)
            return "0"+getHours()+":"+getMinutes();
        if(getHours()/10!=0 && getMinutes()/10==0)
            return getHours()+":0"+getMinutes();
        return getHours()+":"+getMinutes();
    }
}

