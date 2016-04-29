/**
 * Created by igor on 06.06.2014.
 */
public class ComplexConverter {

    public static Complex[] shortArrayToComplex(short[] array) {
        int n = array.length;
        Complex[] complexArray = new Complex[n];
        for (int i = 0; i < n; i++) {
            complexArray[i] = new Complex(array[i], 0);
        }

        return complexArray;
    }

    public static short[] ComplexArrayToShort(Complex[] complexArray) {
        int n = complexArray.length;
        short[] array = new short[n];

        for (int i = 0; i < n; i++) {
            if (complexArray[i].re() < Short.MIN_VALUE) {
                array[i] = Short.MIN_VALUE;
            }
            else if (complexArray[i].re() > Short.MAX_VALUE) {
                array[i] = Short.MAX_VALUE;
            }
            else {
                array[i] = (short)complexArray[i].re();
            }
        }
        return array;
    }
}

