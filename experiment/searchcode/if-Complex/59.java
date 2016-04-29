package org.cakedev.view.crap;

import org.cakedev.view.nextgen.CollectionViewX;
import org.cakeframework.util.functions.BinaryMapper;
import org.cakeframework.util.functions.Generator;
import org.cakeframework.util.functions.Procedure;
import org.cakeframework.util.functions.Reducer;
import org.cakeframework.view.CollectionView;

public class GatherVersionReduce2 {

    public static void main2(String[] args) {
        CollectionViewX<Complex> view = null;
        CollectionView<ComplexAdder2> tmp = view.gather(new ComplexAdder2());

        tmp.reduce(new Reducer<ComplexAdder2>() {
            public ComplexAdder2 reduce(ComplexAdder2 a, ComplexAdder2 b) {
                a.real += b.real;
                a.imaginary += b.imaginary;
                return a;
            }
        });
    }

    static class ComplexAdder2 implements BinaryMapper<Complex, ComplexAdder2, ComplexAdder2>, Reducer<ComplexAdder2> {
        double real, imaginary;

        @Override
        public ComplexAdder2 reduce(ComplexAdder2 a, ComplexAdder2 b) {
            a.real += b.real;
            a.imaginary += b.imaginary;
            return a;
        }

        @Override
        public ComplexAdder2 map(Complex a, ComplexAdder2 b) {
            if (b == null) {
                b = new ComplexAdder2();
            }
            b.real += a.real;
            b.imaginary += a.real;
            return b;
        }

        Complex toComplex() {
            return new Complex(real, imaginary);
        }
    }
    
    
    public static void main(String[] args) {
        CollectionViewX<Complex> view = null;
        CollectionView<ComplexAdder> tmp = view.gather(new Generator<ComplexAdder>() {
            public ComplexAdder next() {
                return new ComplexAdder();
            }
        });

        tmp.reduce(new Reducer<ComplexAdder>() {
            public ComplexAdder reduce(ComplexAdder a, ComplexAdder b) {
                a.real += b.real;
                a.imaginary += b.imaginary;
                return a;
            }
        });
    }

    static class ComplexReducer implements Reducer<Complex> {
        public Complex reduce(Complex a, Complex b) {
            return new Complex(a.real + b.real, a.imaginary + b.imaginary);
        }
    }

    static class ComplexAdder implements Procedure<Complex> {
        double real, imaginary;

        public void apply(Complex c) {
            real += c.real;
            imaginary += c.imaginary;
        }
    }

    static class ComplexReducer2 implements Reducer<ComplexAdder>, Generator<ComplexAdder> {

        @Override
        public ComplexAdder reduce(ComplexAdder a, ComplexAdder b) {
            a.real += b.real;
            a.imaginary += b.imaginary;
            return a;
        }

        @Override
        public ComplexAdder next() {
            return new ComplexAdder();
        }

    }


    static class Complex {
        public Complex(double d, double e) {
            // TODO Auto-generated constructor stub
        }

        double real, imaginary;
    }
}

