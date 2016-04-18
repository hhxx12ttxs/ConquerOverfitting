package logic;

import workers.Worker;

public class Calc {

    private static final int HOURSFORBONUS = 20;

    public int pay(final Worker worker, final int hoursWorked) {
        int salarySum = 0;
        salarySum += calculateBasicSalary(worker, hoursWorked);
        salarySum += calculateOverworkBonus(worker, hoursWorked);
        salarySum += calculateHeroBonus(worker, hoursWorked);
        return salarySum;
    }

    protected int calculateBasicSalary(Worker worker, int hoursWorked) {
        if (hoursWorked > worker.getOverworkLimit()) {
            return worker.getHourCost() * worker.getOverworkLimit();
        }
        return worker.getHourCost() * hoursWorked;
    }

    protected int calculateOverworkBonus(Worker worker, int hoursWorked) {
        int overworkHours = hoursWorked - worker.getOverworkLimit();
        if (overworkHours > 0) {
            return worker.getHourCost() * overworkHours * worker.getOverworkMultiplier();
        }
        return 0;
    }

    protected int calculateHeroBonus(Worker worker, int hoursWorked) {
        if (hoursWorked > HOURSFORBONUS) {
            return worker.getHeroBonus();
        }
        return 0;
    }


}
