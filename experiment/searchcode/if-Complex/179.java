        double r = real * d;
public class Complex {
    public Complex(double r, double i) {
        real = r;
    public void display() {
        if (real != 0) {
            System.out.print(real);
            if (imaginary != 0) {
                System.out.println(\"+\" + imaginary + \"i\");
            }
        } else if (imaginary != 0) {
            System.out.println(imaginary + \"i\");
    public Complex multbyScalar(double d) {

