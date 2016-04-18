// Anthony Pizzimenti
//
/* hourly worker subclass which receives 1.5x pay after 40 hours */


public class hourlyWorker extends worker {
    
    private int hours;
    
    public hourlyWorker(String a, double b, int c) {
        super(a, b);
        hours = c;
    }
    
    public double wage() {
        double wage = 0;
        
        if (hours < 40) {
            wage = computePay(hours);
        } else {
            wage = computePay(40) + (computePay(hours - 40) * 1.5);
        }
        
        return wage;
    }
}
