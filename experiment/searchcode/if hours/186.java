/**
 * Created by Eric on 2/18/2016, 9:47 AM.
 * Description:
 */
public class TeamLeader extends ProductionWorker
{
    private String name;
    private final double MONTH_BONUS = 80.00;
    private final double REQ_TRAIN_HOURS = 36.0;
    private double attendedTrainHours;
    private double hoursWorked;

    /*
    *   Constructors
    * */

    // with super class's hourly rate
    public TeamLeader(double hourlyRate, double attendedTrainHours, double hoursWorked)
    {
        super(hourlyRate);
        this.attendedTrainHours = attendedTrainHours;
        this.hoursWorked = hoursWorked;
    }

    // no arg constructor
    public TeamLeader()
    {
    }

    /*
    *   getters & setters
    * */

    // get name
    public String getName()
    {
        return name;
    }

    // set name
    public void setName(String name)
    {
        this.name = name;
    }

    // set hours work each week
    public void setHoursWorked(double hoursWorked)
    {
        this.hoursWorked = hoursWorked;
    }

    // set attended training hours
    public void setAttendedTrainHours(double attendedTrainHours)
    {
        this.attendedTrainHours = attendedTrainHours;
    }

    public double getMONTH_BONUS()
    {
        return MONTH_BONUS;
    }

    public double getREQ_TRAIN_HOURS()
    {
        return REQ_TRAIN_HOURS;
    }

    public boolean ifReachedHours()
    {
        boolean reached = false;

        if (attendedTrainHours >= REQ_TRAIN_HOURS)
        {
            reached = true;
        }
        else
        {
            reached = false;
        }

        return reached;
    }

    public String reached()
    {
        String str;
        if (ifReachedHours())
        {
            str= " reached his hours.";
        }
        else
        {
            str = " did not reach his hours.";
        }
        return str;
    }

    public double calPay()
    {
        double anualPay;
        double anualBonus = 0;
        double anualPayWithBonus;

        if (ifReachedHours())
        {
            anualBonus = MONTH_BONUS * 12;
        }

        anualPay = getHourlyRate() * (hoursWorked * 251);

        anualPayWithBonus = anualBonus + anualPay;

        return anualPayWithBonus;
    }
}

