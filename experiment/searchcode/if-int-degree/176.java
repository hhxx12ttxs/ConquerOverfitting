package com.undeadscythes.udslibz;

/**
 *
 * @author UndeadScythes
 */
public class PolynomialUtils {
    public static Polynomial sum(final Polynomial polyA, final Polynomial polyB) {
        final Polynomial temp = polyA.copy();
        temp.add(polyB);
        return temp;
    }

    public static Polynomial product(final Polynomial polyA, final Polynomial polyB) {
        int temp = 0;
        for(int i = 0; i <= polyA.getDegree(); i++) {
            if(polyA.getCoeff(i) == 1) {
                temp ^= (polyB.toInt() << i);
            }
        }
        return new Polynomial(temp);
    }

    public static Polynomial productMod(final Polynomial polyA, final Polynomial polyB, final Polynomial mod) {
        int temp1 = 0;
        for(int i = 0; i <= polyA.getDegree(); i++) {
            if(polyA.getCoeff(i) == 1) {
                temp1 ^= (polyB.toInt() << i);
            }
        }
        final Polynomial temp2 = new Polynomial(temp1);
        temp2.modulo(mod);
        return temp2;
    }

    public static Polynomial product(final Polynomial polyA, final int polyB) {
        int temp = 0;
        for(int i = 0; i <= polyA.getDegree(); i++) {
            if(polyA.getCoeff(i) == 1) {
                temp ^= (polyB << i);
            }
        }
        return new Polynomial(temp);
    }

    public static Polynomial toPower(final Polynomial poly, final int e) {
        if(e == 0) return new Polynomial(1);
        if(e == 1) return new Polynomial(poly.toInt());
        return PolynomialUtils.product(poly, PolynomialUtils.toPower(poly, e - 1));
    }

    public static Polynomial toPowerMod(final Polynomial poly, final int e, final Polynomial mod) {
        if(e == 0) return new Polynomial(1);
        final Polynomial temp = new Polynomial(poly.toInt());
        for(int i = 1; i < e; i++) {
            temp.multiplyMod(poly, mod);
        }
        return temp;
    }

    public static Polynomial getPrimitive(final int degree, final int start) {
        final int requiredOrder = (1 << degree) - 1;
        int test = start;
        Polynomial polynomial = new Polynomial(test);
        while(polynomial.getWeight() % 2 == 0 || !polynomial.isMonomial() || polynomial.getDegree() < degree || polynomial.getOrder() != requiredOrder) {
            test++;
            polynomial = new Polynomial(test);
            if(polynomial.getDegree() > degree) {
                return null;
            }
        }
        return polynomial;
    }

    public static Polynomial getIrreducible(final int degree, final int start) {
        int test = start;
        Polynomial polynomial = new Polynomial(test);
        while(polynomial.getWeight() % 2 == 0 || !polynomial.isMonomial() || polynomial.getDegree() < degree || !polynomial.isIrreducible()) {
            test++;
            polynomial = new Polynomial(test);
            if(polynomial.getDegree() > degree) {
                return null;
            }
        }
        return polynomial;
    }

    public static Polynomial getStrictIrreducible(final int degree, final int start) {
        int test = start;
        Polynomial polynomial = new Polynomial(test);
        while(polynomial.getWeight() % 2 == 0 || !polynomial.isMonomial() || polynomial.getDegree() < degree || !polynomial.isIrreducible() || polynomial.isPrimitive()) {
            test++;
            polynomial = new Polynomial(test);
            if(polynomial.getDegree() > degree) {
                return null;
            }
        }
        return polynomial;
    }

    public static Polynomial getStrictIrreducible(final int degree, final int order, final int start) {
        int test = start;
        Polynomial polynomial = new Polynomial(test);
        while(polynomial.getWeight() % 2 == 0 || !polynomial.isMonomial() || polynomial.getDegree() < degree || !polynomial.isIrreducible() || polynomial.isPrimitive() || polynomial.getOrder() != order) {
            test++;
            polynomial = new Polynomial(test);
            if(polynomial.getDegree() > degree) {
                return null;
            }
        }
        return polynomial;
    }

    public static Polynomial berlekampMassey(final Sequence seq) {
        final int n = seq.getLength();
        Polynomial C = new Polynomial(1);
        Polynomial B = new Polynomial(1);
        int x = 1;
        int L = 0;
        int b = 1;
        int N = 0;
        while(N != n) {
            int temp = 0;
            for(int i = 1; i <= L; i++) {
                temp += C.getCoeff(i) * seq.getElement(N - i);
                temp = temp % 2;
            }
            final int d = (seq.getElement(N) + temp) % 2;
            if(d == 0) {
                x += 1;
            } else if(2 * L > N) {
                if(d * b == 1) {
                    C = PolynomialUtils.sum(C, PolynomialUtils.product(B, new Polynomial(1 << x)));
                }
                //C=C-db^-1X^xB
                x += 1;
            } else {
                final Polynomial T = C;
                if(d * b == 1) {
                    C = PolynomialUtils.sum(C, PolynomialUtils.product(B, new Polynomial(1 << x)));
                }
                //C=C-db^-1X^xB
                L = N + 1 - L;
                B = T;
                b = d;
                x = 1;
            }
            N++;
        }
        return new Polynomial(BinaryUtils.reverse(C.toInt(), C.getDegree() + 1));
    }
}

