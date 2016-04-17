package utils;

public class MyTime {
    
    private int hours;
    private int mins;
    static private int maxhours,minhours;
    static{
        maxhours = 20;
        minhours = 7;
    }

    public MyTime(int hours, int mins) {
        this.hours = hours;
        this.mins = mins;
    }
    
    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getMins() {
        return mins;
    }

    public void setMins(int mins) {
        this.mins = mins;
    }
    
    public MyTime(int inpminutes){
        this.hours = inpminutes/60;
        this.mins = inpminutes%60;
    }
    public MyTime(String timerep){
         String[] splits = timerep.split(":");
         hours = new Integer(splits[0]).intValue();
         mins = new Integer(splits[1]).intValue();
    }
    public String toString(){
        String repr = hours + ":" + mins;
        return repr;
    }
    public static MyTime add(MyTime t1, MyTime t2){
        
        
        MyTime t3 = new MyTime(0,0);
        t3.hours = t1.hours + t2.hours;
        
        t3.mins = t1.mins + t2.mins;
        t3.hours += t3.mins/60;
        if(t3.hours > maxhours) 
            t3.hours=maxhours;
        if(t3.hours < 0)    t3.hours*=-1;
        t3.mins = t3.mins%60;
        
        return t3;
    }
    public static MyTime subtractMinutes(final MyTime t1, int mins)
    {
        MyTime t3=new MyTime(0,0);      //t1 4 15
        MyTime temp=new MyTime(0,0);    //min 30
        temp.hours = mins/60;   //0
        temp.mins = mins%60; //30
        t3.mins = t1.mins - temp.mins;  //-15
        int borrow=0;
        if(t3.mins < 0){
            
            t3.mins +=60;       //45    
            borrow =1;       //3
        }
        t3.hours = t1.hours - temp.hours - borrow;   //3
        
            
        if(t3.hours < minhours) 
            t3.hours=minhours;
        
        return t3;
        
    }
    
    public static MyTime subtract(final MyTime t1, final MyTime t2)
    {
        MyTime t3= new MyTime(0,0);
        int borrow = 0;
        t3.mins = t1.mins - t2.mins;
        if(t3.mins < 0){
            t3.mins += 60;
            borrow = 1;
            
        }
        
        t3.hours = t1.hours - t2.hours - borrow;
        return t3;
    }
    
}
