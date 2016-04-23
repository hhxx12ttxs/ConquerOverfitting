public class Factorial {
    public static void main(String[] args) {
        Factorial factorial = new Factorial();
        factorial.calculate(10);
    }

    public int calculate(int number) {
        if(number > 1) {
            return number * this.calculate(number - 1);
        }
        return 1;
    }
}

