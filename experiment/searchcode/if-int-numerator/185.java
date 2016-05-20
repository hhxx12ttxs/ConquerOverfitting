/*
 * @(#)SimplexTabular.java 1.0 12/10/2009
 * @charset "utf-8";
 * Copyright (c) pendiente.
 * 
 */
package mx.uacam.fdi.io.simplex.resolvedor;

import java.util.StringTokenizer;
import org.neocs.mate.fracciones.Fraccion;
import mx.uacam.fdi.io.simplex.resolvedor.mate.Ecuacion;
import mx.uacam.fdi.io.simplex.resolvedor.mate.Monomio;
import mx.uacam.fdi.io.simplex.resolvedor.mate.RationalNumber;

/**
 *
 * @author Neo Cs
 * @version 1.0, 12/10/2009
 */
public class SimplexTabular implements Simplex {

    /**indica en que paso estamos del m?todo */
    private int pasos;
    /**indica la cantidad de veces que se ha iterado*/
    private int ciclo;
    /**define cantidad de restricciones*/
    private int m;
    /**define numero de variables basicas*/
    private int n;
    private int r;
    private int s;
    private RationalNumber[][] a = new RationalNumber[10][20];
    int[] base = new int[10];

    @Override
    public double[] maximizar(Ecuacion fo, Ecuacion[] restricciones) {
        for (int i = 0; i < fo.getMonomios().length; i++) {
            fo.getMonomio(i).setCoeficiente(-1 * fo.getMonomio(i).getCoeficiente());
        }
        
        return minimizar(fo,restricciones);
    }

    @Override
    public double[] minimizar(Ecuacion fo, Ecuacion[] restricciones) {
        m = restricciones.length;
        n = fo.getMonomios().length - 1;
        String sdat = "";

        for (int i = 0; i < m; i++) {
            Monomio[] monomios = restricciones[i].getMonomios();
            for (int j = 0; j < n; j++) {
                double d = monomios[j].getCoeficiente();
                String cadena = Fraccion.valueOf(d).toString();
                sdat += cadena + ',';
            }
            sdat += restricciones[i].getResultado() + ",";
        }

        Monomio[] monomios = fo.getMonomios();
        for (int j = 0; j < n; j++) {
            double d = Double.valueOf(monomios[j].getCoeficiente());
            String cadena = Fraccion.valueOf(d).toString();
            sdat += cadena + ',';
        }

        sdat += fo.getResultado();

        StringTokenizer st = new StringTokenizer(sdat, ",");
        for (int i = 0; i <= m; i++) {
            for (int j = 0; j < n; j++) {
                a[i][j] = new RationalNumber(st.nextToken());
            }
            base[i] = n + i;
            for (int j = n; j < n + m; j++) {
                RationalNumber rn = new RationalNumber(0);
                if (j == i + n) {
                    rn.set(1);
                }
                a[i][j] = rn;
            }
            a[i][n + m] = new RationalNumber(st.nextToken());
        }
        n += m;

        boolean esOptimal = false;
        boolean esResolvible = false;
        do {
            switch (pasos) {
                case 0:
                    esOptimal = pasos1();
                    if(!esOptimal){
                        System.out.println("varible salidad x" + (s + 1));
                    }
                    break;
                case 1:
                    esResolvible = paso2();
                    if(!esResolvible){
                        System.out.println("variable entrante x" + (base[r] + 1));
                    }
                    break;
                case 2:
                    paso3();
                    break;
                case 3:
                    paso4();
                    ciclo++;
                    break;
                default:
                    break;
            }
            pasos++;
            pasos %= 4;
        } while (!esOptimal && !esResolvible);

        double[] d = new double[m + 1];

        for (int i = 0; i <= m; i++) {
            d[i] = (double) a[i][n].numerator / (double) a[i][n].denominator;
        }

        return d;
    }

    /** search pivot s of (r, s) */
    private boolean pasos1() {
        RationalNumber c = new RationalNumber();

        s = 0;
        r = -1;
        c.set(a[m][s]);
        for (int j = 1; j < n; j++) {
            if (c.gt(a[m][j])) {
                s = j;
                c.set(a[m][s]);
            }
        }
        if (c.numerator >= 0) {
            s = -1;
            return true;
        } else {
            return false;
        }
    }

    /* search pivot r of (r, s) */
    private boolean paso2() {
        RationalNumber t = new RationalNumber();
        RationalNumber c = new RationalNumber();

        for (int i = 0; i < m; i++) {
            if (a[i][s].numerator <= 0) {
                continue;
            }
            t.set(a[i][n]);
            t.div(a[i][s]);
            if (r < 0 || t.lt(c)) {
                r = i;
                c.set(t);
            }
        }
        if (r < 0) {
            return true;
        } else {
            return false;
        }
    }

    /* pivote */
    private void paso3() {
        RationalNumber c = new RationalNumber();

        base[r] = s;
        c.set(a[r][s]);
        for (int j = 0; j <= n; j++) {
            a[r][j].div(c);
        }
        
        for (int i = 0; i < a.length; i++) {
            RationalNumber[] rationalNumbers = a[i];
            for (RationalNumber rationalNumber : rationalNumbers) {
                System.out.print(rationalNumber + "\t");
            }
            System.out.print('\n');
        }
    }

    private void paso4() {
        RationalNumber c = new RationalNumber();
        RationalNumber t = new RationalNumber();

        for (int i = 0; i <= m; i++) {
            if (i == r) {
                continue;
            }
            c.set(a[i][s]);
            for (int j = 0; j <= n; j++) {
                t.set(c);
                t.mul(a[r][j]);
                a[i][j].minus(t);
                System.out.print(a[i][j] + "\t");
            }
            System.out.print("\n");
        }
        r = s = -1;
    }
}
