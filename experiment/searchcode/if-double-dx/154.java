package net.my.math;

public class Integral {

    private void integrate(int a, int b, long N) {
        double dx = ((double) (b)-a)/N;
        double tmp;
        double k1 = 0.0;
        double k2 = 0.0;
        double k3 = 0.0;
        double k4 = 0.0;

        for (int i=0;i<N;i++) {
            tmp = a + i * dx;
            k1 += this.dctlikefunc(tmp);
            k2 += this.logfunc(tmp);
            k3 += this.osomefunc(tmp);
            k4 += this.sincfunc(tmp);
        }

        System.out.print(Double.toString(k1*dx) + "\n" + Double.toString(k2*dx) + "\n" + Double.toString(k3*dx) + "\n" + Double.toString(k4*dx) + "\n");
    }

    private void dataToCsv() {

    }

    private double sincfunc(double x)  {
        if(x==0)
            x+=0.00000001;
        return Math.sin(x) / x;
    }

    private double dctlikefunc(double x) {
        double pi = 3.14159265;
        if (x==1)
            x+=0.00000001;
        return x * Math.cos(pi / (x-1) * x);
    }

    private double logfunc(double x) {
        if (x==0)
            x+=0.00000001;
        return Math.log(x);
    }

    private double osomefunc(double x) {
        return Math.pow(Math.abs(Math.sin(x * Math.exp(1))), Math.exp(1));
    }

    public static void main(String[] args) {
        Integral i;
        i = new Integral();
        double t0 = System.currentTimeMillis();
        i.integrate(10, 1000, 10000000);
        double t1 = System.currentTimeMillis();
        double  dif = (t1-t0)/1000;

        System.out.print("function took: " + Double.toString(dif) + "s");
    }
}

