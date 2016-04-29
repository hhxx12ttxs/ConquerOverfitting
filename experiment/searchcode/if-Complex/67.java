public class Complex {

    private double x, y;

    public Complex(double u, double v) {
        x = u;
        y = v;
    }

    public double real() {
        return x;
    }

    public double image() {
        return y;
    }

    public double mod() {
        if (x != 0 || y != 0) {
            return Math.sqrt(x * x + y * y);
        } else {
            return 0d;
        }
    }

    public double arg() {
        return Math.atan2(y, x);
    }

    public Complex conj() {
        return new Complex(x, -y);
    }

    public Complex plus(Complex w) {
        return new Complex(x + w.real(), y + w.image());
    }

    public Complex minus(Complex w) {
        return new Complex(x - w.real(), y - w.image());
    }

    public Complex times(Complex w) {
        return new Complex(x * w.real() - y * w.image(), x * w.image() + y * w.real());
    }

    public Complex div(Complex w) {
        double den = Math.pow(w.mod(), 2);
        return new Complex((x * w.real() + y * w.image()) / den, (y * w.real() - x * w.image()) / den);
    }

    public Complex exp() {
        return new Complex(Math.exp(x) * Math.cos(y), Math.exp(x) * Math.sin(y));
    }

    public Complex log() {
        return new Complex(Math.log(this.mod()), this.arg());
    }

    public Complex sqrt() {
        double r = Math.sqrt(this.mod());
        double theta = this.arg() / 2;
        return new Complex(r * Math.cos(theta), r * Math.sin(theta));
    }

    public String toString() {
        if (x != 0 && y > 0) {
            return x + " + " + y + "i";
        }
        if (x != 0 && y < 0) {
            return x + " - " + (-y) + "i";
        }
        if (y == 0) {
            return String.valueOf(x);
        }
        if (x == 0) {
            return y + "i";
        }
        // shouldn't get here (unless Inf or NaN)
        return "";

    }
}

