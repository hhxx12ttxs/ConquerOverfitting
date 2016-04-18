package Lesson2;

public class FooCorporation {
    private static double salary;
    private static double hours;
    public static double basePay;

    public static void main(String[] args) {
        EmployeeSalary(7.5, 35);
        EmployeeSalary(8.2, 47);
        EmployeeSalary(10.0, 73);
    }

    public static void EmployeeSalary(double basePay, int hours) {
        if (basePay < 8) {
            System.out.println("Base pay must be bigger than 8.00$");
        } else if (hours > 60) {
            System.out.println("Work hours must be lower than 60");
        } else {
            int overtimeHours = 0;
            if (hours > 40) {
                overtimeHours = hours - 40;
                hours = 40;
            }
            double pay = basePay * hours;
            pay += overtimeHours * basePay * 1.5;
            System.out.println("Pay this employee $" + pay);

        }
    }
}

