package task1.company.calculator;

/**
 * @author Alexey Levchhenko
 */
public class FullDaySalaryCalculator implements SalaryCalculator{
    @Override
    public double calculateSalary(int normaHours, int actualHours, double rate) {
        if((float)actualHours/normaHours>=1){
            return rate;
        }
        return (float)actualHours/normaHours * rate;
    }
}

