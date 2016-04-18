package Opgave1;

public class HorseCarriage extends Vehicle {
    private double hoursLeft = 0.0;
    private double kmPrHour = 10.0;
    
    public boolean drive(int distance) {
        double hoursNeeded = distance/kmPrHour;
        if (hoursNeeded <= hoursLeft) {
            hoursLeft -= hoursNeeded;
            super.drive(distance);
            return true;
        }        
        else return false;
    }

    public HorseCarriage() {
        super();
    }

    public void restHorses() {
        hoursLeft = 10;
    }
}
