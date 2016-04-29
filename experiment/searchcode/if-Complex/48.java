package Task003;

/**
 * Created by Kamil on 25.02.16.
 */
public class ComplexNumber {

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    private double x;
    private double y;

    public ComplexNumber add(ComplexNumber complexNumber) {
        ComplexNumber add = new ComplexNumber();
        add.x = this.x + complexNumber.getX();
        add.y = this.y + complexNumber.getY();
        return add;
    }

    public void add2(ComplexNumber complexNumber) {
        this.x += complexNumber.getX();
        this.y += complexNumber.getY();
    }

    public ComplexNumber sub(ComplexNumber complexNumber) {
        ComplexNumber sub = new ComplexNumber();
        sub.x = this.x - complexNumber.getX();
        sub.y = this.y - complexNumber.getY();
        return sub;
    }

    public void sub2(ComplexNumber complexNumber) {
        this.x -= complexNumber.getX();
        this.y -= complexNumber.getY();
    }

    public ComplexNumber multNumber(double i) {
        ComplexNumber multNumber = new ComplexNumber(this.x * i, this.y * i);
        return multNumber;
    }

    public void multNumber2(double i) {
        this.x *= i;
        this.y *= i;
    }

    public ComplexNumber mult(ComplexNumber complexNumber) {
        ComplexNumber mult = new ComplexNumber();
        mult.x = this.x * complexNumber.getX() - this.y * complexNumber.getY();
        mult.y = this.x * complexNumber.getY() + this.y * complexNumber.getX();
        return mult;
    }

    public void mult2(ComplexNumber complexNumber) {
        this.x = this.x * complexNumber.getX() - this.y * complexNumber.getY();
        this.y = this.x * complexNumber.getY() + this.y * complexNumber.getX();
    }

    public ComplexNumber() {
        this(0, 0);
    }

    public ComplexNumber div(ComplexNumber complexNumber) {
        double a = complexNumber.getX() * complexNumber.getX() + complexNumber.getY() * complexNumber.getY();
        ComplexNumber div = new ComplexNumber();
        div.x = 1.0 * (this.x * complexNumber.getX() + this.y * complexNumber.getY()) / a;
        div.y = 1.0 * (-this.x * complexNumber.getY() + this.y * complexNumber.getX()) / a;
        return div;

    }

    public void div2(ComplexNumber complexNumber) {
        double a = complexNumber.getX() * complexNumber.getX() + complexNumber.getY() * complexNumber.getY();
        this.x = 1.0 * (this.x * complexNumber.getX() + this.y * complexNumber.getY()) / a;
        this.y = 1.0 * (-this.x * complexNumber.getY() + this.y * complexNumber.getX()) / a;

    }

    public double lenght() {
        double d = Math.sqrt(this.x * this.x + this.y * this.y);
        return d;
    }

    public String toString() {
        if (this.y >= 0) {
            return (this.x + " + " + this.y + "i");
        } else {
            return (this.x + " - " + (-1) * this.y + "i");
        }
    }

    public double arg() {
        double arg = Math.atan(1.0 * this.y / this.x);
        return arg;
    }

    public ComplexNumber pow(double pow) {
        ComplexNumber poow = new ComplexNumber();
        poow.x = Math.pow(this.x, pow) * Math.cos(pow * this.arg());
        poow.y = Math.pow(this.x, pow) * Math.sin(pow * this.arg());
        return poow;
    }

    public boolean equals(ComplexNumber complexNumber) {
        return (this.x == complexNumber.getX() && this.y == complexNumber.getY());
    }

    public ComplexNumber(double a, double b) {
        this.x = a;
        this.y = b;
    }

}

