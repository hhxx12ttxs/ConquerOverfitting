package example;

public class Salary {

    public static void main(String[] args) {
        salaryCurrent(45, 11);
    }

    static void salaryCurrent(double hours, double money) {
        double sum;
        if (hours > 0 & hours <= 40 & money >= 8) {
            sum = hours * money;
            System.out.println("salary " + sum);
        } else if (hours > 40 & hours <= 60 & money >= 8) {
            sum = hours * (money * 1.5);
            System.out.println("salary " + sum);
        }
    }
}

