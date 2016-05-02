package com.undeadscythes.udslibz;

import java.util.*;

/**
 * A binary polynomial.
 * @author UndeadScythes
 */
public class Polynomial {
    private static final int MAX_DEG = 31;

    private transient int representation;
    private transient int order = -1;
    private transient int degree;
    private transient int weight;

    public Polynomial(final int representation) {
        this.representation = representation;
        degree = MAX_DEG - Integer.numberOfLeadingZeros(representation);
        weight = Integer.bitCount(representation);
    }

    private void refresh() {
        degree = MAX_DEG - Integer.numberOfLeadingZeros(representation);
        weight = Integer.bitCount(representation);
    }

    public boolean equalTo(final Polynomial poly) {
        return poly.toInt() == representation;
    }

    public Polynomial copy() {
        return new Polynomial(representation);
    }

    public int getDegree() {
        return degree;
    }

    public int getWeight() {
        return weight;
    }

    public int getOrder() {
        if(order == -1) {
            final GaloisLFSR lfsr = new GaloisLFSR(this.getDegree(), this, 1);
            lfsr.clock();
            order = 1;
            while(lfsr.getState() != 1) {
                order++;
                lfsr.clock();
            }
        }
        return order;
    }

    public int getCoeff(final int coeff) {
        return (((1 << coeff) & representation) != 0) ? 1 : 0;
    }

    public int toInt() {
        return representation;
    }

    public String toBinary() {
        return BinaryUtils.toString(representation, degree);
    }

    @Override
    public String toString() {
        return toBinary();
    }

    public String toString(final String var) {
        String binary = Integer.toBinaryString(representation);
        String temp = "";
        for(int i = 0; i < binary.length() - 2; i++) {
            if(binary.charAt(i) == '1') {
                temp = temp.concat(var.concat("^" + (degree - i) + " + "));
            }
        }
        if(binary.length() > 1 && binary.charAt(binary.length() - 2) == '1') {
            temp = temp.concat(String.valueOf(var).concat(" + "));
        }
        if(binary.length() > 0 && binary.charAt(binary.length() - 1) == '1') {
            temp = temp.concat("1 + ");
        }
        return temp.substring(0, temp.length() > 3 ? temp.length() - 3 : 0);
    }

    public boolean isMonomial() {
        return (1 & representation) == 1;
    }

    public boolean isIrreducible() {
        int maxTest = (1 << degree) - 1;
        for(int i = 1; i < maxTest; i++) {
            for(int j = i; j < maxTest; j++) {
                Polynomial a = new Polynomial(i);
                Polynomial b = new Polynomial(j);
                if(a.getDegree() + b.getDegree() != degree) {
                    continue;
                }
                if(PolynomialUtils.product(a, b).toInt() == representation) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isPrimitive() {
        return getOrder() == (1 << degree) - 1;
    }

    public void add(final Polynomial add) {
        representation ^= add.toInt();
        refresh();
    }

    public void subtract(final Polynomial sub) {
        add(sub);
        refresh();
    }

    public void multiply(final Polynomial multiply) {
        int temp = 0;
        for(int i = 0; i <= multiply.getDegree(); i++) {
            if(multiply.getCoeff(i) == 1) {
                temp ^= (representation << i);
            }
        }
        representation = temp;
        refresh();
    }

    public void multiplyMod(final Polynomial multiply, final Polynomial mod) {
        multiply(multiply);
        modulo(mod);
    }

    public void modulo(final Polynomial mod) {
        int diff = getDegree() - mod.getDegree();
        while(diff >= 0) {
            add(PolynomialUtils.product(mod, (1 << diff)));
            diff = getDegree() - mod.getDegree();
        }
    }

    public Polynomial nextPoly() {
        return new Polynomial(representation + 1);
    }

    public Polynomial getStrictPrimitiveRoot() {
        final int N = (1 << degree) - 1;
        final int q = N / order;
        Polynomial alpha = new Polynomial(1);
        while(alpha.getDegree() < degree) {
            boolean primitive = false;
            if(PolynomialUtils.toPowerMod(alpha, q, this).toInt() == 2) {
                primitive = true;
                for(int i = 1; i < N; i++) {
                    if(PolynomialUtils.toPowerMod(alpha, i, this).toInt() == 1) {
                        primitive = false;
                        break;
                    }
                }
            }
            if(primitive) {
                break;
            }
            alpha = alpha.nextPoly();
        }
        return alpha;
    }

    public Polynomial getPrimitiveRoot() {
        final int N = (1 << degree) - 1;
        final int q = N / order;
        Polynomial alpha = new Polynomial(1);
        while(alpha.getDegree() < degree) {
            boolean primitive = false;
            //if(PolynomialUtils.toPowerMod(alpha, q, this).toInt() == 2) {
                primitive = true;
                for(int i = 1; i < N; i++) {
                    if(PolynomialUtils.toPowerMod(alpha, i, this).toInt() == 1) {
                        primitive = false;
                        break;
                    }
                }
            //}
            if(primitive) {
                break;
            }
            alpha = alpha.nextPoly();
        }
        return alpha;
    }

    public Polynomial getStrictPrimitiveRoot(final Polynomial start) {
        final int N = (1 << degree) - 1;
        final int q = N / order;
        Polynomial alpha = start.nextPoly();
        boolean primitive = false;
        while(alpha.getDegree() < degree) {
            primitive = false;
            if(PolynomialUtils.toPowerMod(alpha, q, this).toInt() == 2) {
                primitive = true;
                for(int i = 1; i < N; i++) {
                    if(PolynomialUtils.toPowerMod(alpha, i, this).toInt() == 1) {
                        primitive = false;
                        break;
                    }
                }
            }
            if(primitive) {
                break;
            }
            alpha = alpha.nextPoly();
        }
        if(primitive) {
            return alpha;
        }
        return null;
    }

    public Polynomial getPrimitiveRoot(final Polynomial start) {
        final int N = (1 << degree) - 1;
        final int q = N / order;
        Polynomial alpha = start.nextPoly();
        boolean primitive = false;
        while(alpha.getDegree() < degree) {
            primitive = false;
            //if(PolynomialUtils.toPowerMod(alpha, q, this).toInt() == 2) {
                primitive = true;
                for(int i = 1; i < N; i++) {
                    if(PolynomialUtils.toPowerMod(alpha, i, this).toInt() == 1) {
                        primitive = false;
                        break;
                    }
                }
            //}
            if(primitive) {
                break;
            }
            alpha = alpha.nextPoly();
        }
        if(primitive) {
            return alpha;
        }
        return null;
    }

    public List<Polynomial> getClassReps(final Polynomial alpha) {
        final int q = ((1 << degree) - 1) / order;
        final List<Polynomial> reps = new ArrayList<Polynomial>();
        Polynomial seqRep = new Polynomial(1);
        for(int classNo = 1; classNo <= q; classNo++) {
            reps.add(seqRep.copy());
            final Polynomial nextClassRep = new Polynomial(0);
            for(int i = 0; i < degree; i++) {
                if(alpha.getCoeff(i) == 1) {
                    nextClassRep.add(seqRep);
                }
                seqRep.multiplyMod(new Polynomial(2), this);
            }
            seqRep = nextClassRep;
        }
        return reps;
    }
}

