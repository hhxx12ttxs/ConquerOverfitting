package zm.hashcode.design.softwaredesignprinciples.ocp.violation;

/**
 * Created by student on 2015/02/27.
 */
public class Salary
{
    private String time;
    private int nHours;
    private int oHours;

    public double calcSalary(){
        double salary  = 0;


        if (time == "overtime")
        {
            salary = nHours * oHours *0.2;
        }
        else if (time == "normaltime"){
            salary = nHours * 0.2;
        }
        return salary;
    }

}

