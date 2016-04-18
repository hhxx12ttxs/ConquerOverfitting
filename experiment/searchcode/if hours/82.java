package domain.work;

import domain.Performance;

public class WorkUnit {
	
    private double hours;
    private Performance performance;

    public WorkUnit(double hours, Performance performance) {
        validateHours(hours);
        this.hours = hours;
        this.performance = performance;
    }

    private void validateHours(double hours) {
        if(hours<=0 || hours > 24){
            throw new IllegalArgumentException("Hours should be between 0 and 24.");
        }
        double fraction = hours - Math.floor(hours);
        if(fraction != 0 && fraction != .5){
            throw new IllegalArgumentException("Fractions different than 0.5 are not accepted.");
        }
    }

    public double getHours() {
        return hours;
    }

    public Performance getPerformance() {
        return performance;
    }

    @Override
    public String toString() {
        return performance.getCode() + "->" + hours + "h";
    }
}

