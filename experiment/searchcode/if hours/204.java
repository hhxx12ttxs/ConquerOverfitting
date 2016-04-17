package nakov.principles;

public class Worker extends Human {
    private double wage;
    private int workedHours;

    public Worker(String firstName, String lastName, double wage, int workedHours){
        super(firstName,lastName);
        setWage(wage);
        setWorkedHours(workedHours);
    }

    public double wageForHour(){
        return wage / workedHours;
    }

    public double getWage() {
        return wage;
    }

    public void setWage(double wage) {
        if (wage > 0) {
            this.wage = wage;
        } else {
            throw new IllegalArgumentException("Invalid wage");
        }
    }

    public int getWorkedHours() {
        return workedHours;
    }

    public void setWorkedHours(int workedHours) {
        if (workedHours > 0) {
            this.workedHours = workedHours;
        } else {
            throw new IllegalArgumentException("Negative working hours!");
        }
    }
}

