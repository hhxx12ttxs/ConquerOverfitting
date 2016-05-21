/* ComplexRGBTexture.java
 * Created on July 1, 2007, 10:00 PM
import java.awt.image.BufferedImage;
import math.complex;
public class ComplexRGBTexture {
    
    public complex [] data;
    int min = 0, max = 1;
    public ComplexRGBTexture(int size) {
        this.size = size;
    public ComplexRGBTexture threshold( int cut ) {
        for (complex z : data) if (complex.mod(z) < cut) z.setComplex(0,0);
        return this;

