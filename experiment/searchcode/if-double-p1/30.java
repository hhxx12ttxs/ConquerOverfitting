public class SalaryCalculator {
public double calcHours(int P1, int P2, int salary) {
if (P1 * 200 >= salary)
return salary / (double) P1;
else
return (salary - P1 * 200) / (double) P2 + 200;
}
}

