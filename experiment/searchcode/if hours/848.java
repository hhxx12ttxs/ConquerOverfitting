package Payroll;

public final class TravailleurHoraire extends Employe {
    private int workedHours; //worked hours/week
    private double hourPrice; // prix 1h normal
    private double perSup;
    int hoursWeek = 8*5;  //8h multi par 5jrs
    int hoursMonth = hoursWeek*4;

    public TravailleurHoraire(String traHorFristName, String traHorLastName, int workedHours, double hourPrice, double perSup){
        super(traHorFristName, traHorLastName);
        setHourPrice(hourPrice);
        setWorkedHours(workedHours);
        setPerSup(perSup);
    }

    public void setWorkedHours(int workedHours){
        if(workedHours >= 0 && workedHours <= hoursMonth){
            this.workedHours = workedHours;
        }else{
            this.workedHours = hoursMonth;
        }
    }

    public void setHourPrice(double hourPrice){
        this.hourPrice = hourPrice;
    }

    private void setPerSup(double perSup){
        this.perSup = perSup;
    }

    @Override
    public double gains() {
        if(workedHours <= hoursMonth){
            return workedHours * hourPrice;
        }else {
            return hoursMonth * hourPrice  + (workedHours - hoursMonth) * hourPrice * perSup ;
        }
    }

    public String toString(){
        return "Travailleur Horaire: " + super.toString() + " " + "earn " + this.gains() + " per Month.";
    }
}

