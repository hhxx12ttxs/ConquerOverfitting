package Homework_week_3;


public class Subject {

    private String name;
    private int hoursOfSubject;
    private int hoursWorked;
    private int mark;

    public Subject() {
    }

    public Subject(String name, int hoursOfSubject, int hoursWorked, int mark) {
        this.name = name;
        this.hoursOfSubject = hoursOfSubject;
        this.hoursWorked = hoursWorked;
        this.mark = mark;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHoursOfSubject() {
        return hoursOfSubject;
    }

    public void setHoursOfSubject(int hoursOfSubject) {
        this.hoursOfSubject = hoursOfSubject;
    }

    public int getHoursWorked() {
        return hoursWorked;
    }

    public void setHoursWorked(int hoursWorked) {
        this.hoursWorked = hoursWorked;
    }

    public int getMark() {
        return mark;
    }

    public void setMark(int mark) {
        this.mark = mark;
    }

    public void examPass(){

        if (hoursWorked > hoursOfSubject * 0.8){
            mark = 5;
        }else {
            if (hoursWorked > hoursOfSubject * 0.6){
                mark = 4;
            }else {
                mark = 3;
            }
        }

    }

    @Override
    public String toString() {
        return "Subject{" +
                "name='" + name + '\'' +
                ", hoursOfSubject=" + hoursOfSubject +
                ", hoursWorked=" + hoursWorked +
                ", mark=" + mark +
                '}';
    }
}

