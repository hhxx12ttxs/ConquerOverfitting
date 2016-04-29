/* ComplexRGBTexture.java
 * Created on July 1, 2007, 10:00 PM
 */

package util;

/**
 * @author Michael Everett Rule
 */
import java.awt.Color;
import java.awt.image.BufferedImage;
import math.complex;

public class ComplexRGBTexture {
    
    public complex [] data;
    int min = 0, max = 1;
    float normal = 0,scale = 1;
    int size, length;

    public ComplexRGBTexture(int size) {
        this.size = size;
        length = size * size;
        data = new complex[length];
    }
    
    /** Creates a new instance of intRGBImage */
    public ComplexRGBTexture( complex[] source, int size ) {
        this.size = size;
        length = size * size;
        data = source;
    }
    
    /** Creates a new instance of intRGBImage */
    public ComplexRGBTexture( complex[][] source ) {
        size = source.length;
        length = size * size;
        data = new complex[ length ];
        int i , j;
        for ( i = 0; i < size; i++ )
            for ( j = 0; j < size; j++ )
                data[i + j * size ] = source[i][j];
    }
    
    public ComplexRGBTexture threshold( int cut ) {
        for (complex z : data) if (complex.mod(z) < cut) z.setComplex(0,0);
        return this;
    }
    
    void normalise() {
        //System.out.println("normalising...");
        min = max =0;
        scale = normal = 0;
        for (int i = 0; i < length; i++) {
            double mag = complex.mod( data[i] );
            normal += mag;
            max = (int)StrictMath.max(max, mag);
            min = (int)StrictMath.min(min, mag);
        }
        scale = ( max == min ) ? 1 : 256 / (max - min);
        normal /= 3 * length ;
    }
    
    public float normalise( int component ) {
        return (component - min) * scale;
    }
    
    public int getRGB( int index ) {
        return Color.HSBtoRGB( (float)complex.arg(data[index]) , 0.f, .999f*(float)complex.mod(data[index]) );
    }

    public int getRGB( int i , int j ) {
        return getRGB( i*size + j );
    }

    public complex getValue( int i ) {
        return data[i];
    }
    
    public BufferedImage toBufferedImage() {
        return toBufferedImage( new BufferedImage( size, size, BufferedImage.TYPE_INT_RGB ) );
    }
    
    public BufferedImage toBufferedImage( BufferedImage temp ) {
        normalise();
        
        int i,j,color,index = 0;
        for (i = 0; i < size; i++)
            for (j = 0; j < size; j++)
                temp.setRGB( i, j, getRGB( i + ( j ) * size ));
        
        return temp;
    }
    
}

