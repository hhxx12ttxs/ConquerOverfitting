/**
 * representation of an RGBA color for a single pixel, using the
 * linearized sRGB color space
 */
public class Pixel {

    public static final double GAMMA = 2.2;
    public static final Pixel BLACK = new Pixel(0., 0., 0.);
    public static final Pixel WHITE = new Pixel(1., 1., 1.);
    
    double r, g, b, a;
    
    public Pixel(double r, double g, double b) {
        init(r, g, b, 1.);
    }

    public Pixel(double r, double g, double b, double a) {
        init(r, g, b, a);
    }
    
    public Pixel(double[] k) {
        if (k.length == 4) {
            init(k[0], k[1], k[2], k[3]);
        } else {
            init(k[0], k[1], k[2], 1.);
        }
    }
    
    public Pixel(double[] k, double alpha) {
        init(k[0], k[1], k[2], alpha);        
    }

    protected void init(double r, double g, double b, double a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        validateRanges();
    }
    
    protected void validateRanges() {
        for (double k : toArray()) {
            if (k < -Util.EPSILON || k > 1. + Util.EPSILON) {
                throw new IllegalArgumentException(String.format("[%f %f %f %f] out of gamut", r, g, b, a));
            }
        }
    }
    
    public double[] toArray() {
        return new double[] {r, g, b, a};
    }

    public byte[] toRGB() {
        //todo: optimize gamma conversion
        return new byte[] {
            byteVal(Math.pow(r, 1./GAMMA)),
            byteVal(Math.pow(g, 1./GAMMA)),
            byteVal(Math.pow(b, 1./GAMMA))
        };
    }

    public byte[] toRGBA() {
        byte[] rgb = toRGB();
        return new byte[] {rgb[0], rgb[1], rgb[2], byteVal(a)};
    }
    
    public Pixel blend(Pixel other, double k) {
        if (Math.abs(a - other.a) < Util.EPSILON) {
            return new Pixel(Util.linearInterpolate(toArray(), other.toArray(), k));
        } else {
            //if alphas differ, need to interpolate based on pre-multiplied color values
            double[] px1 = toArray();
            double[] px2 = other.toArray();
            for (int i = 0; i < 3; i++) {
                px1[i] *= px1[4];
                px2[i] *= px2[4];
            }
            double[] px3 = Util.linearInterpolate(px1, px2, k);
            for (int i = 0; i < 3; i++) {
                px3[i] /= px3[4];
            }
            return new Pixel(px3);
        }
    }
    
    
    
    public static int intVal(byte b) {
        return (b >= 0 ? b : b + 256);
    }

    public static double floatVal(byte b) {
        return (intVal(b) + .5) / 256.;
    }
    
    public static byte byteVal(double d) {
        int k = (int)(256. * d);
        if (k < 0) {
            k = 0;
        } else if (k > 255) {
            k = 255;
        }
        return (byte)k;
    }
    
    

    
    public static double[] sRGB(double r_g, double g_g, double b_g) {
        return new double[] {
            Math.pow(r_g, GAMMA),
            Math.pow(g_g, GAMMA),
            Math.pow(b_g, GAMMA)
        };
    }
    
    public static double[] sRGB(double[] k) {
        return sRGB(k[0], k[1], k[2]);
    }
    
    public static Pixel fromRGB(byte r, byte g, byte b) {
        return new Pixel(sRGB(floatVal(r), floatVal(g), floatVal(b)));
    }
    
}

