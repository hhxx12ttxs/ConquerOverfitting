/**
 * Created by wwadge on 01/01/15.
 */
public class Clock {

    private int hours;
    private int mins;


    public int getMins() {
        return mins;
    }

    public void setMins(int mins) {
        this.mins = mins;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        if (hours >=0 && hours < 24) {
            this.hours = hours;
        } else {
            System.out.println("fuck off");
        }
    }

    public Clock(){
        // default constructor - always there
    }
}

