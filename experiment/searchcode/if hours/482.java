package HomeWork1;

/**
 * Created by ��������� on 17.10.2015.
 */
public class ClockAngel {
    public static void main(String[] args) {
    int hours = 27;
    int mins = 430;
    int result = 0;
        hours = hours % 12;
        mins = mins % 60;
        int degreesForHours = 360/12;
        int degreesForMins = 360/60;
        int hoursAngel = hours * degreesForHours;
        int minsAngel = mins * degreesForMins;
        if (hoursAngel > minsAngel) {
            result = hoursAngel - minsAngel;
        } else {
            result = minsAngel - hoursAngel;
        }
        if(result >= 180){
            result = 360 - result;
        }
    System.out.println("Angel between hours and minute narrows is " + result);
    }
}

