package gankster.oop.lesson6.dz.ex1;

public class FactorialThread implements Runnable {

    private int factorial;

    public FactorialThread(int factorial) {
        this.factorial = factorial;
    }

    @Override
    public void run() {

        double fact = 1;

        if (factorial == 1){
            System.out.println(Thread.currentThread().getName() + " - " + fact);
        }
        else {
            for (int i = 1; i <= factorial; i++) {
                fact *= i;
            }
            System.out.println(Thread.currentThread().getName() + " - " + fact);
        }

    }


}

