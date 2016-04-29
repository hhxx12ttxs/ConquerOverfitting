/**
 * Created by Giuseppe on 4/16/2014.
 */

public class TestComplex {

    public static void main(String[] args) {

        double a = 3.5;
        double b = 5.5;
        Complex c1 = new Complex(a, b);


        double c = -3.5;
        double d = 1.0;
        Complex c2 = new Complex(c, d);

        System.out.println("(" + c1 + ")" + " + " + "(" + c2 + ")" + " = " + c1.add(c2));
        System.out.println("(" + c1 + ")" + " - " + "(" + c2 + ")" + " = " + c1.subtract(c2));
        System.out.println("(" + c1 + ")" + " * " + "(" + c2 + ")" + " = " + c1.multiply(c2));
        System.out.println("(" + c1 + ")" + " / " + "(" + c2 + ")" + " = " + c1.divide(c2));
        System.out.println("|" + c1 + "| = " + c1.abs());

    }

    static class Complex  {
        private double a = 0, b = 0;

        public Complex() {
        }

        Complex(double a, double b) {
            this.a = a;
            this.b = b;
        }

        public Complex(double a) {
            this.a = a;
        }

        public double getA() {
            return a;
        }

        public double getB() {
            return b;
        }

        public Complex add(Complex secondComplex) {
            double newA = a + secondComplex.getA();
            double newB = b + secondComplex.getB();
            return new Complex(newA, newB);
        }

        public Complex subtract(Complex secondComplex) {
            double newA = a - secondComplex.getA();
            double newB = b - secondComplex.getB();
            return new Complex(newA, newB);
        }

        public Complex multiply(Complex secondComplex) {
            double newA = a * secondComplex.getA() - b * secondComplex.getB();
            double newB = b * secondComplex.getA() + a * secondComplex.getB();
            return new Complex(newA, newB);
        }

        public Complex divide(Complex secondComplex) {
            double newA = (a * secondComplex.getA() + b * secondComplex.getB())
                    / (Math.pow(secondComplex.getA(), 2.0) + Math.pow(secondComplex.getB(),
                    2.0));
            double newB = (b * secondComplex.getA() - a * secondComplex.getB())
                    / (Math.pow(secondComplex.getA(), 2.0) + Math.pow(secondComplex.getB(),
                    2.0));
            return new Complex(newA, newB);
        }

        public double abs() {
            return Math.sqrt(a * a + b * b);
        }

        @Override
        public String toString() {
            if (b != 0)
                return a + " + " + b + "i";
            return a + "";
        }
    }
}

