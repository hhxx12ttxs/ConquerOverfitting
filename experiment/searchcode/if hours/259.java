public class HorseCarriage extends Drivable {
    private double hoursLeft = 0.0;
    private double kmPrHour = 10.0;
    
    public boolean drive(int distance) {
        double hoursNeeded = distance/kmPrHour;
        if (hoursNeeded <= hoursLeft) {
            hoursLeft -= hoursNeeded;
            mileage += distance;
            return true;
        }        
        else return false;
    }
    
    public void restHorses() {
        hoursLeft = 10;
    }
}
