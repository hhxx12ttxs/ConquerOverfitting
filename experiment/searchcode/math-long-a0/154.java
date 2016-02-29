package selena.common.math;

import ni.types.Matrixf;
import ni.types.Vector2f;
import ni.types.Vector2l;
import ni.types.Vector3f;
import ni.types.Vector3l;
import ni.types.Vector4f;
import ni.types.Vector4l;

public class Vec {
    protected Vec() {
    }

    public static final float EPSILON1 = 1e-1f;
    public static final float EPSILON2 = 1e-2f;
    public static final float EPSILON3 = 1e-3f;
    public static final float EPSILON4 = 1e-4f;
    public static final float EPSILON5 = 1e-5f;
    public static final float EPSILON6 = 1e-6f;
    public static final float EPSILON7 = 1e-7f;
    public static final float EPSILON = EPSILON5;

    public static final float ROOT_TWO = (float)Math.sqrt(2);
    public static final float ROOT_THREE = (float)Math.sqrt(3);
    public static final float E = (float)Math.exp(1);
    public static final float PI = (float)Math.PI;
    public static final float TWO_PI = 2.0f * PI;
    public static final float HALF_PI = 0.5f * PI;
    public static final float QUARTER_PI = 0.25f * PI;
    public static final double LOG_2 = Math.log(2.0);

    public static float toRad(final float x) {
        return x * (PI / 180);
    }

    public static double toRad(final double x) {
        return x * (PI / 180);
    }

    public static float toDeg(final float x) {
        return x * (180 / PI);
    }

    public static double toDeg(final double x) {
        return x * (180 / PI);
    }

    public static boolean fuzzyEqual(final float A, final float B, final float epsilon) {
        return Math.abs(A - B) <= epsilon;
    }

    public static boolean fuzzyEqual(final float A, final float B) {
        return Math.abs(A - B) <= EPSILON5;
    }

    public static boolean fuzzyEqual(final double A, final double B, final double epsilon)
    {
        return Math.abs(A - B) <= epsilon;
    }

    public static boolean fuzzyEqual(final double A, final double B) {
        return Math.abs(A - B) <= EPSILON5;
    }

    public static float finvert(final float v) {
        return v == 0 ? 1 : (1 / v);
    }

    public static float fdiv(final float n, final float d) {
        return d == 0 ? 1 : (n / d);
    }

    public static float lerp(final float a, final float b, final float fac) {
        return a + ((b - a) * fac);
    }

    public static double lerp(final double a, final double b, final double fac) {
        return a + ((b - a) * fac);
    }

    /**
     * Blends new values into an accumulator to produce a smoothed time series.
     * 
     * @param accumulator is the current value of the accumulator
     * @param newValue is the new value to accumulate
     * @param smoothRate typically made proportional to "frameTime". If
     *            smoothRate is 0 the accumulator will not change, if smoothRate
     *            is 1 the accumulator is set to the new value (no smoothing),
     *            useful values are "near zero".
     * @return the new smoothed value of the accumulator
     */
    public static double blendIntoAccumulator(final double accumulator, final double newValue, final double smoothRate)
    {
        return Vec.lerp(accumulator, newValue, Vec.clamp(smoothRate, 0, 1));
    }

    public static float blendIntoAccumulator(final float accumulator, final float newValue, final float smoothRate)
    {
        return Vec.lerp(accumulator, newValue, Vec.clamp(smoothRate, 0, 1));
    }

    public static final float smoothStep(final float a, final float b, final float x) {
        if (x <= a)
            return 0;
        if (x >= b)
            return 1;
        final float t = clamp((x - a) / (b - a), 0.0f, 1.0f);
        return t * t * (3 - 2 * t);
    }

    public static final float smoothFactor(final float t) {
        return t * t * (3 - 2 * t);
    }

    public static int min(final int a, final int b) {
        return Math.min(a, b);
    }

    public static float min(final float a, final float b) {
        return Math.min(a, b);
    }

    public static double min(final double a, final double b) {
        return Math.min(a, b);
    }

    public static int min(final int a, final int b, final int c) {
        return a < b ? (a < c ? a : c) : (b < c ? b : c);
    }

    public static float min(final float a, final float b, final float c) {
        return a < b ? (a < c ? a : c) : (b < c ? b : c);
    }

    public static double min(final double a, final double b, final double c) {
        return a < b ? (a < c ? a : c) : (b < c ? b : c);
    }

    public static int max(final int a, final int b) {
        return Math.max(a, b);
    }

    public static float max(final float a, final float b) {
        return Math.max(a, b);
    }

    public static double max(final double a, final double b) {
        return Math.max(a, b);
    }

    public static int max(final int a, final int b, final int c) {
        return a > b ? (a > c ? a : c) : (b > c ? b : c);
    }

    public static float max(final float a, final float b, final float c) {
        return a > b ? (a > c ? a : c) : (b > c ? b : c);
    }

    public static double max(final double a, final double b, final double c) {
        return a > b ? (a > c ? a : c) : (b > c ? b : c);
    }

    public static int mid(final int a, final int b, final int c) {
        return Math.max(a, Math.min(b, c));
    }

    public static float mid(final float a, final float b, final float c) {
        return Math.max(a, Math.min(b, c));
    }

    public static double mid(final double a, final double b, final double c) {
        return Math.max(a, Math.min(b, c));
    }

    public static int clamp(final int v, final int m, final int M) {
        int r;
        if (v < m)
            r = m;
        else if (v > M)
            r = M;
        else
            r = v;
        return r;
    }

    public static float clamp(final float v, final float m, final float M) {
        float r;
        if (v < m)
            r = m;
        else if (v > M)
            r = M;
        else
            r = v;
        return r;
    }

    public static double clamp(final double v, final double m, final double M) {
        double r;
        if (v < m)
            r = m;
        else if (v > M)
            r = M;
        else
            r = v;
        return r;
    }

    public static float sqrt(final float a) {
        return (float)Math.sqrt(a);
    }

    public static float fastInverseSqrt(float x) {
        final float xhalf = 0.5f * x;
        int i = Float.floatToRawIntBits(x);
        i = 0x5F3759DF - (i >> 1);
        x = Float.intBitsToFloat(i);
        x = x * (1.5f - xhalf * x * x);
        return x;
    }

    public static int clampToInteger(final double value, final int min, final int max) {
        final int v = (int)value;
        if (v < min)
            return min;
        if (v > max)
            return max;
        return v;
    }

    public static int clampToInteger(final float value, final int min, final int max) {
        final int v = (int)value;
        if (v < min)
            return min;
        if (v > max)
            return max;
        return v;
    }

    public static int sign(final double a) {
        if (a == 0.0f)
            return 0;
        return (a > 0) ? 1 : -1;
    }

    public static int sign(final float a) {
        if (a == 0.0f)
            return 0;
        return (a > 0) ? 1 : -1;
    }

    public static final int sign(final int a) {
        return (a == 0) ? 0 : ((a > 0) ? 1 : -1);
    }

    public static int sign(final long a) {
        if (a == 0)
            return 0;
        return (a > 0) ? 1 : -1;
    }

    public static float fmod(final float n, final float d) {
        final float x = n / d;
        return n - floor(x) * d;
    }

    /**
     * Integer modulus function
     * 
     * @param n number
     * @param d divisor
     * @return
     */
    public static int mod(final int number, final int divisor) {
        int r = (number % divisor);
        if (r < 0) {
            r += divisor;
        }
        return r;
    }

    /**
     * Detects the number of times that boundary is passed when adding increase
     * to base
     * 
     * @param increase
     * @param boundary
     * @param base
     * @return
     */
    public static long quantize(final long increase, final long boundary, final long base)
    {
        return ((base + increase) / boundary) - (base / boundary);
    }

    public static int abs(final int a) {
        return Math.abs(a);
    }

    public static long abs(final long a) {
        return Math.abs(a);
    }

    public static float abs(final float a) {
        return Math.abs(a);
    }

    public static double abs(final double a) {
        return Math.abs(a);
    }

    public static float sigmoid(final float a) {
        final double ea = Math.exp(-a);
        final float df = (float)(1 / (1.0f + ea));
        if (Float.isNaN(df))
            return (a > 0) ? 1 : 0;
        return df;
    }

    public static double sigmoid(final double a) {
        final double ea = Math.exp(-a);
        final double df = (1 / (1.0f + ea));
        if (Double.isNaN(df))
            return (a > 0) ? 1 : 0;
        return df;
    }

    public static float tanh(final float a) {
        final double ex = Math.exp(2 * a);
        final float df = (float)((ex - 1) / (ex + 1));
        if (Float.isNaN(df))
            return (a > 0) ? 1 : -1;
        return df;
    }

    public static double tanh(final double a) {
        final double ex = Math.exp(2 * a);
        final double df = ((ex - 1) / (ex + 1));
        if (Double.isNaN(df))
            return (a > 0) ? 1 : -1;
        return df;
    }

    public static double tanhScaled(final double a) {
        return 1.7159 * tanh((2.0 / 3.0) * a);
    }

    public static double tanhScaledDerivative(final double a) {
        final double ta = tanh((2.0 / 3.0) * a);
        return (1.7159 * (2.0 / 3.0)) * (ta * (1 - ta));
    }

    public static float inverseSigmoid(final float a) {
        if (a >= 1)
            return 800;
        if (a <= 0)
            return -800;
        final double ea = a / (1.0 - a);
        return (float)Math.log(ea);
    }

    public static float sigmoidDerivative(final float a) {
        final float sa = sigmoid(a);
        return sa * (1 - sa);
    }

    public static float tanhDerivative(final float a) {
        final float sa = tanh(a);
        return 1 - sa * sa;
    }

    public static float sin(double a) {
        a = a % TWO_PI;
        return (float)Math.sin(a);
    }

    public static float cos(double a) {
        a = a % TWO_PI;
        return (float)Math.cos(a);
    }

    public static float sin(float a) {
        a = a % TWO_PI;
        return (float)Math.sin(a);
    }

    public static float cos(float a) {
        a = a % TWO_PI;
        return (float)Math.cos(a);
    }

    public static int floor(final float a) {
        if (a >= 0)
            return (int)a;
        final int x = (int)a;
        return (a == x) ? x : x - 1;
    }

    public static float frac(final float a) {
        return a - floor(a);
    }

    public static double frac(final double a) {
        return a - floor(a);
    }

    public static int floor(final double a) {
        if (a >= 0)
            return (int)a;
        final int x = (int)a;
        return (a == x) ? x : x - 1;
    }

    public static int square(final byte b) {
        return b * b;
    }

    public static int square(final int a) {
        return a * a;
    }

    public static float square(final float a) {
        return a * a;
    }

    public static double square(final double a) {
        return a * a;
    }

    public static float round(final float f, final int dp) {
        final float factor = (float)Math.pow(10, -dp);
        return Math.round(f / factor) * factor;
    }

    public static int roundUp(final double d) {
        final int i = (int)d;
        return (i == d) ? i : (i + 1);
    }

    public static int roundUp(final Number d) {
        return roundUp(d.doubleValue());
    }

    public static int roundUp(final float d) {
        final int i = (int)d;
        return (i == d) ? i : (i + 1);
    }

    /**
     * Computes a fast approximation to <code>Math.pow(a, b)</code>. Adapted
     * from <url>http://www.dctsystems.co.uk/Software/power.html</url>.
     * 
     * @param a a positive number
     * @param b a number
     * @return a^b
     */
    public static final float fastPower(final float a, float b) {
        // adapted from: http://www.dctsystems.co.uk/Software/power.html
        float x = Float.floatToRawIntBits(a);
        x *= 1.0f / (1 << 23);
        x = x - 127;
        float y = x - (int)Math.floor(x);
        b *= x + (y - y * y) * 0.346607f;
        y = b - (int)Math.floor(b);
        y = (y - y * y) * 0.33971f;
        return Float.intBitsToFloat((int)((b + 127 - y) * (1 << 23)));
    }

    /** Counts number of 1 bits in i */
    public static final int bitCount(int i) {
        i = i - ((i >> 1) & 0x55555555);
        i = (i & 0x33333333) + ((i >> 2) & 0x33333333);
        i = (i + (i >> 4)) & 0x0F0F0F0F;
        i = i + (i >> 8);
        i = i + (i >> 16);
        return i & 0x0000003F;
    }

    /** Return integer factorial of n */
    public static final int factorial(final int n) {
        int prod = 1;
        for (int i = 1; i <= n; ++i) {
            prod *= i;
        }
        return prod;
    }

    /** Check if the given size is power of 2. */
    public static final boolean isPow2(final int x)
    {
        return ((x != 0) && ((x & (x - 1)) == 0));
    }

    /** Find the value nearest to n which is also a power of two. */
    public static final int round2(final int n)
    {
        int m;
        for (m = 1; m < n; m <<= 1) {
        }
        if (m - n <= n - (m >> 1))
            return m;
        else
            return m >> 1;
    }

    /** Resize a not pow2 in pow2 size. */
    public static final int getNearestPow2(final int size, final boolean lowerPow2)
    {
        if (isPow2(size))
            return size;
        int base = 1;
        final int ri = (floor(Math.log(size) / LOG_2));
        if ((!lowerPow2) && (size > 32) && (size < 256))
            base = 2;
        return base << ri;
    }

    /** Resize a not pow2 in pow2 size. */
    public static final int getNextPow2(final int size)
    {
        if (isPow2(size))
            return size;
        final int ri = (floor(Math.log(size) / LOG_2));
        return 2 << ri;
    }

    /**
     * Get the number of mip maps level for the given resolution to the
     * specified minimum resolution
     */
    public static final int getNumMipMaps(int w, int h, int minw, int minh)
    {
        if (minw == 0)
            minw = 1;
        if (minh == 0)
            minh = 1;
        if (w <= minw || h <= minh)
            return 0;
        int i;
        for (i = 0; ((w >>= 1) >= minw) && ((h >>= 1) >= minh); ++i) {
        }
        return i + 1;
    }

    /**
     * Compute the number of mip maps for a full mip map chain for the specified
     * resolution
     */
    public static final int computeNumMipMaps(final int w, final int h, final int d) {
        int s = max(w, h, d);
        if (s <= 1)
            return 0;
        int i;
        for (i = 0; (s >>= 1) >= 1; ++i)
        {
        }
        return i + 1;
    }

    /** Computes the determinant of a 2x2 matrix */
    public static final float det2x2(final float[] apIn) {
        return apIn[0] * apIn[3] - apIn[1] * apIn[2];
    }

    /** Computes the determinant of a 2x2 matrix */
    public static final float det2x2(final float a, final float b, final float c, final float d)
    {
        return a * d - b * c;
    }

    /** Computes the determinant of a 3x3 matrix */
    public static final float det3x3(final float[] apIn) {
        final float fA = apIn[4] * apIn[8] - apIn[5] * apIn[7];
        final float fB = apIn[5] * apIn[6] - apIn[3] * apIn[8];
        final float fC = apIn[3] * apIn[7] - apIn[4] * apIn[6];
        return apIn[0] * fA + apIn[1] * fB + apIn[2] * fC;
    }

    /** Computes the determinant of a 3x3 matrix */
    public static final float det3x3(final float a0, final float a1, final float a2, final float a3, final float a4, final float a5, final float a6, final float a7, final float a8)
    {
        final float fA = a4 * a8 - a5 * a7;
        final float fB = a5 * a6 - a3 * a8;
        final float fC = a3 * a7 - a4 * a6;
        return a0 * fA + a1 * fB + a2 * fC;
    }

    /** Computes the determinant of a 4x4 matrix */
    public static final float det4x4(final float[] apIn) {
        final float fA0 = apIn[0] * apIn[5] - apIn[1] * apIn[4];
        final float fA1 = apIn[0] * apIn[6] - apIn[2] * apIn[4];
        final float fA2 = apIn[0] * apIn[7] - apIn[3] * apIn[4];
        final float fA3 = apIn[1] * apIn[6] - apIn[2] * apIn[5];
        final float fA4 = apIn[1] * apIn[7] - apIn[3] * apIn[5];
        final float fA5 = apIn[2] * apIn[7] - apIn[3] * apIn[6];
        final float fB0 = apIn[8] * apIn[13] - apIn[9] * apIn[12];
        final float fB1 = apIn[8] * apIn[14] - apIn[10] * apIn[12];
        final float fB2 = apIn[8] * apIn[15] - apIn[11] * apIn[12];
        final float fB3 = apIn[9] * apIn[14] - apIn[10] * apIn[13];
        final float fB4 = apIn[9] * apIn[15] - apIn[11] * apIn[13];
        final float fB5 = apIn[10] * apIn[15] - apIn[11] * apIn[14];
        return fA0 * fB5 - fA1 * fB4 + fA2 * fB3 + fA3 * fB2 - fA4 * fB1 + fA5 * fB0;
    }

    /** Computes the determinant of a 4x4 matrix */
    public static final float det4x4(final float a0, final float a1, final float a2, final float a3,
            final float a4, final float a5, final float a6, final float a7,
            final float a8, final float a9, final float a10, final float a11,
            final float a12, final float a13, final float a14, final float a15)
    {
        final float fA0 = a0 * a5 - a1 * a4;
        final float fA1 = a0 * a6 - a2 * a4;
        final float fA2 = a0 * a7 - a3 * a4;
        final float fA3 = a1 * a6 - a2 * a5;
        final float fA4 = a1 * a7 - a3 * a5;
        final float fA5 = a2 * a7 - a3 * a6;
        final float fB0 = a8 * a13 - a9 * a12;
        final float fB1 = a8 * a14 - a10 * a12;
        final float fB2 = a8 * a15 - a11 * a12;
        final float fB3 = a9 * a14 - a10 * a13;
        final float fB4 = a9 * a15 - a11 * a13;
        final float fB5 = a10 * a15 - a11 * a14;
        return fA0 * fB5 - fA1 * fB4 + fA2 * fB3 + fA3 * fB2 - fA4 * fB1 + fA5 * fB0;
    }

    /** Converts an horizontal fov to a vertical fov */
    public static final float fovHzToVt(final float afHFov, final float afWbyHAspect) {
        return 2.0f * (float)Math.atan(Math.tan(afHFov * 0.5f) / afWbyHAspect);
    }

    /** Converts a vertical fov to an horizontal fov */
    public static final float fovVtToHz(final float afVFov, final float afWbyHAspect) {
        return 2.0f * (float)Math.atan(Math.tan(afVFov * 0.5f) * afWbyHAspect);
    }

    //===========================================================================
    //
    // Vector2f
    //
    //===========================================================================
    public static final Vector2f VEC2F_ZERO = Vec2f(0, 0);
    public static final Vector2f VEC2F_ONE = Vec2f(1, 1);
    public static final Vector2f VEC2F_MINUS_ONE = Vec2f(-1, -1);
    public static final Vector2f VEC2F_TWO = Vec2f(2, 2);
    public static final Vector2f VEC2F_MINUS_TWO = Vec2f(-2, -2);
    public static final Vector2f VEC2F_HALF = Vec2f(0.5f, 0.5f);
    public static final Vector2f VEC2F_MINUS_HALF = Vec2f(-0.5f, -0.5f);
    public static final Vector2f VEC2F_X = Vec2f(1, 0);
    public static final Vector2f VEC2F_MINUS_X = Vec2f(1, 0);
    public static final Vector2f VEC2F_Y = Vec2f(0, 1);
    public static final Vector2f VEC2F_MINUS_Y = Vec2f(0, -1);

    public static Vector2f Vec2f() {
        return new Vector2f();
    }

    public static Vector2f Vec2f(final int x, final int y) {
        return (new Vector2f()).set(x, y);
    }

    public static Vector2f Vec2f(final float x, final float y) {
        return (new Vector2f()).set(x, y);
    }

    public static Vector2f Vec2f(final Object o) {
        return (new Vector2f()).set(o);
    }

    public static Vector2f add(Vector2f r, final Vector2f a, final Vector2f b) {
        if (r == null) {
            r = new Vector2f();
        }
        r.x = a.x + b.x;
        r.y = a.y + b.y;
        return r;
    }

    public static Vector2f add(Vector2f r, final Vector2f a, final Vector2f b, final Vector2f... o)
    {
        if (r == null) {
            r = new Vector2f();
        }
        r.x = a.x + b.x;
        r.y = a.y + b.y;
        for (final Vector2f c : o) {
            r.x += c.x;
            r.y += c.y;
        }
        return r;
    }

    public static Vector2f add(Vector2f r, final Vector2f a, final float b) {
        if (r == null) {
            r = new Vector2f();
        }
        r.x = a.x + b;
        r.y = a.y + b;
        return r;
    }

    public static Vector2f sub(Vector2f r, final Vector2f a, final Vector2f b) {
        if (r == null) {
            r = new Vector2f();
        }
        r.x = a.x - b.x;
        r.y = a.y - b.y;
        return r;
    }

    public static Vector2f sub(Vector2f r, final Vector2f a, final float b) {
        if (r == null) {
            r = new Vector2f();
        }
        r.x = a.x - b;
        r.y = a.y - b;
        return r;
    }

    public static Vector2f mul(Vector2f r, final Vector2f a, final Vector2f b) {
        if (r == null) {
            r = new Vector2f();
        }
        r.x = a.x * b.x;
        r.y = a.y * b.y;
        return r;
    }

    public static Vector2f mul(Vector2f r, final Vector2f a, final float b) {
        if (r == null) {
            r = new Vector2f();
        }
        r.x = a.x * b;
        r.y = a.y * b;
        return r;
    }

    public static Vector2f div(Vector2f r, final Vector2f a, final Vector2f b) {
        if (r == null) {
            r = new Vector2f();
        }
        r.x = a.x / b.x;
        r.y = a.y / b.y;
        return r;
    }

    public static Vector2f div(Vector2f r, final Vector2f a, final float b) {
        if (r == null) {
            r = new Vector2f();
        }
        r.x = a.x / b;
        r.y = a.y / b;
        return r;
    }

    public static Vector2f mad(Vector2f r, final Vector2f a, final Vector2f b, final Vector2f c)
    {
        if (r == null) {
            r = new Vector2f();
        }
        r.x = a.x * b.x + c.x;
        r.y = a.y * b.y + c.y;
        return c;
    }

    public static Vector2f mad(Vector2f r, final Vector2f a, final float b, final Vector2f c)
    {
        if (r == null) {
            r = new Vector2f();
        }
        r.x = a.x * b + c.x;
        r.y = a.y * b + c.y;
        return c;
    }

    public static Vector2f min(Vector2f r, final Vector2f a, final Vector2f b) {
        if (r == null) {
            r = new Vector2f();
        }
        r.x = min(a.x, b.x);
        r.y = min(a.y, b.y);
        return r;
    }

    public static Vector2f max(Vector2f r, final Vector2f a, final Vector2f b) {
        if (r == null) {
            r = new Vector2f();
        }
        r.x = max(a.x, b.x);
        r.y = max(a.y, b.y);
        return r;
    }

    public static Vector2f mid(Vector2f r, final Vector2f a, final Vector2f b, final Vector2f c)
    {
        if (r == null) {
            r = new Vector2f();
        }
        r.x = mid(a.x, b.x, c.x);
        r.y = mid(a.y, b.y, c.y);
        return r;
    }

    public static Vector2f clamp(Vector2f r, final Vector2f v, final Vector2f m, final Vector2f M)
    {
        if (r == null) {
            r = new Vector2f();
        }
        r.x = clamp(v.x, m.x, M.x);
        r.y = clamp(v.y, m.y, M.y);
        return r;
    }

    public static Vector2f abs(Vector2f r, final Vector2f a) {
        if (r == null) {
            r = new Vector2f();
        }
        r.x = Math.abs(a.x);
        r.y = Math.abs(a.y);
        return r;
    }

    public static Vector2f lerp(Vector2f r, final Vector2f a, final Vector2f b, final float fac)
    {
        if (r == null) {
            r = new Vector2f();
        }
        r.x = lerp(a.x, b.x, fac);
        r.y = lerp(a.y, b.y, fac);
        return r;
    }

    public static Vector2f blendIntoAccumulator(final Vector2f r, final Vector2f accumulator, final Vector2f newValue, final float smoothRate)
    {
        return Vec.lerp(r, accumulator, newValue, Vec.clamp(smoothRate, 0, 1));
    }

    public static float dot(final Vector2f a, final Vector2f b) {
        return a.x * b.x + a.y * b.y;
    }

    public static float angle(final Vector2f a, final Vector2f b) {
        float dls = dot(a, b) / (length(a) * length(b));
        if (dls < -1f) {
            dls = -1f;
        }
        else if (dls > 1.0f) {
            dls = 1.0f;
        }
        return (float)Math.acos(dls);
    }

    public static float cross(final Vector2f V1, final Vector2f V2) {
        return V1.x * V2.y - V1.y * V2.x;
    }

    public static float ccw(final Vector2f V1, final Vector2f V2) {
        return cross(V1, V2);
    }

    public static Vector2f setLength(Vector2f Out, final Vector2f V, final float l) {
        if (Out == null) {
            Out = new Vector2f();
        }
        final float fac = l / (float)Math.sqrt(V.x * V.x + V.y * V.y);
        Out.x = V.x * fac;
        Out.y = V.y * fac;
        return Out;
    }

    public static boolean isNormal(final Vector2f V, final float epsilon) {
        return fuzzyEqual(lengthSq(V), 1, epsilon);
    }

    public static boolean isNormal(final Vector2f V) {
        return fuzzyEqual(lengthSq(V), 1);
    }

    public static Vector2f normalize(Vector2f Out, final Vector2f V) {
        if (Out == null) {
            Out = new Vector2f();
        }
        float denom = (float)Math.sqrt(V.x * V.x + V.y * V.y);
        denom = 1.0f / denom;
        Out.x = V.x * denom;
        Out.y = V.y * denom;
        return Out;
    }

    public static Vector2f normalize(final Vector2f V) {
        return normalize(V, V);
    }

    public static Vector2f negate(Vector2f Out, final Vector2f V) {
        if (Out == null) {
            Out = new Vector2f();
        }
        Out.x = -V.x;
        Out.y = -V.y;
        return Out;
    }

    public static Vector2f negate(final Vector2f V) {
        return negate(V, V);
    }

    public static Vector2f invert(Vector2f Out, final Vector2f V) {
        if (Out == null) {
            Out = new Vector2f();
        }
        Out.x = 1.0f / V.x;
        Out.y = 1.0f / V.y;
        return Out;
    }

    public static Vector2f invert(final Vector2f V) {
        return invert(V, V);
    }

    public static float lengthSq(final Vector2f v) {
        return dot(v, v);
    }

    public static float length(final Vector2f v) {
        return (float)Math.sqrt(dot(v, v));
    }

    public static float distanceSq(final Vector2f a, final Vector2f b) {
        final float Vx = b.x - a.x;
        final float Vy = b.y - a.y;
        return Vx * Vx + Vy * Vy;
    }

    public static float distance(final Vector2f a, final Vector2f b) {
        return (float)Math.sqrt(distanceSq(a, b));
    }

    public static boolean fuzzyEqual(final Vector2f A, final Vector2f B, final float epsilon)
    {
        return (Math.abs(A.x - B.x) <= epsilon) &&
               (Math.abs(A.y - B.y) <= epsilon);
    }

    public static boolean fuzzyEqual(final Vector2f A, final Vector2f B) {
        return fuzzyEqual(A, B, EPSILON5);
    }

    public static int compare(final Vector2f A, final Vector2f B) {
        if (A.x < B.x)
            return -1;
        if (A.x > B.x)
            return 1;
        if (A.y < B.y)
            return -1;
        if (A.y > B.y)
            return 1;
        return 0;
    }

    public static Vector4f transform(Vector4f Out, final Vector2f V, final Matrixf M) {
        if (Out == null) {
            Out = new Vector4f();
        }
        final float x = V.x, y = V.y;
        Out.x = x * M._11 + y * M._21 + M._41;
        Out.y = x * M._12 + y * M._22 + M._42;
        Out.z = x * M._13 + y * M._23 + M._43;
        Out.w = x * M._14 + y * M._24 + M._44;
        return Out;
    }

    public static Vector2f transformCoord(Vector2f Out, final Vector2f V, final Matrixf M)
    {
        if (Out == null) {
            Out = new Vector2f();
        }
        float rhw, x, y;
        x = V.x;
        y = V.y;
        rhw = (x * M._14 + y * M._24 + M._44);
        if (rhw == 0) {
            Out.x = Out.y = 0;
            return Out;
        }
        rhw = 1.0f / rhw;
        Out.x = (x * M._11 + y * M._21 + M._41) * rhw;
        Out.y = (x * M._12 + y * M._22 + M._42) * rhw;
        return Out;
    }

    public static Vector2f transformNormal(Vector2f Out, final Vector2f V, final Matrixf M)
    {
        if (Out == null) {
            Out = new Vector2f();
        }
        final float x = V.x, y = V.y;
        Out.x = x * M._11 + y * M._21;
        Out.y = x * M._12 + y * M._22;
        return Out;
    }

    public static Vector2f reflect(Vector2f Out, final Vector2f Dir, final Vector2f Normal)
    {
        if (Out == null) {
            Out = new Vector2f();
        }
        sub(Out, Dir, mul(Out, Normal, dot(Normal, Dir) * 2.0f));
        return Out;
    }

    //===========================================================================
    //
    // Vector2l
    //
    //===========================================================================
    public static final Vector2l VEC2L_ZERO = Vec2l(0, 0);
    public static final Vector2l VEC2L_ONE = Vec2l(1, 1);
    public static final Vector2l VEC2L_MINUS_ONE = Vec2l(-1, -1);
    public static final Vector2l VEC2L_TWO = Vec2l(2, 2);
    public static final Vector2l VEC2L_MINUS_TWO = Vec2l(-2, -2);
    public static final Vector2l VEC2L_X = Vec2l(1, 0);
    public static final Vector2l VEC2L_MINUS_X = Vec2l(1, 0);
    public static final Vector2l VEC2L_Y = Vec2l(0, 1);
    public static final Vector2l VEC2L_MINUS_Y = Vec2l(0, -1);

    public static Vector2l Vec2l() {
        return new Vector2l();
    }

    public static Vector2l Vec2l(final int x, final int y) {
        return (new Vector2l()).set(x, y);
    }

    public static Vector2l Vec2l(final float x, final float y) {
        return (new Vector2l()).set(x, y);
    }

    public static Vector2l Vec2l(final Object o) {
        return (new Vector2l()).set(o);
    }

    public static Vector2l add(Vector2l r, final Vector2l a, final Vector2l b) {
        if (r == null) {
            r = new Vector2l();
        }
        r.x = a.x + b.x;
        r.y = a.y + b.y;
        return r;
    }

    public static Vector2l add(Vector2l r, final Vector2l a, final int b) {
        if (r == null) {
            r = new Vector2l();
        }
        r.x = a.x + b;
        r.y = a.y + b;
        return r;
    }

    public static Vector2l sub(Vector2l r, final Vector2l a, final Vector2l b) {
        if (r == null) {
            r = new Vector2l();
        }
        r.x = a.x - b.x;
        r.y = a.y - b.y;
        return r;
    }

    public static Vector2l sub(Vector2l r, final Vector2l a, final int b) {
        if (r == null) {
            r = new Vector2l();
        }
        r.x = a.x - b;
        r.y = a.y - b;
        return r;
    }

    public static Vector2l mul(Vector2l r, final Vector2l a, final Vector2l b) {
        if (r == null) {
            r = new Vector2l();
        }
        r.x = a.x * b.x;
        r.y = a.y * b.y;
        return r;
    }

    public static Vector2l mul(Vector2l r, final Vector2l a, final int b) {
        if (r == null) {
            r = new Vector2l();
        }
        r.x = a.x * b;
        r.y = a.y * b;
        return r;
    }

    public static Vector2l div(Vector2l r, final Vector2l a, final Vector2l b) {
        if (r == null) {
            r = new Vector2l();
        }
        r.x = a.x / b.x;
        r.y = a.y / b.y;
        return r;
    }

    public static Vector2l div(Vector2l r, final Vector2l a, final int b) {
        if (r == null) {
            r = new Vector2l();
        }
        r.x = a.x / b;
        r.y = a.y / b;
        return r;
    }

    public static Vector2l mad(Vector2l r, final Vector2l a, final Vector2l b, final Vector2l c)
    {
        if (r == null) {
            r = new Vector2l();
        }
        r.x = a.x * b.x + c.x;
        r.y = a.y * b.y + c.y;
        return c;
    }

    public static Vector2l mad(Vector2l r, final Vector2l a, final int b, final Vector2l c)
    {
        if (r == null) {
            r = new Vector2l();
        }
        r.x = a.x * b + c.x;
        r.y = a.y * b + c.y;
        return c;
    }

    public static Vector2l min(Vector2l r, final Vector2l a, final Vector2l b) {
        if (r == null) {
            r = new Vector2l();
        }
        r.x = Math.min(a.x, b.x);
        r.y = Math.min(a.y, b.y);
        return r;
    }

    public static Vector2l max(Vector2l r, final Vector2l a, final Vector2l b) {
        if (r == null) {
            r = new Vector2l();
        }
        r.x = Math.max(a.x, b.x);
        r.y = Math.max(a.y, b.y);
        return r;
    }

    public static Vector2l mid(Vector2l r, final Vector2l a, final Vector2l b, final Vector2l c)
    {
        if (r == null) {
            r = new Vector2l();
        }
        r.x = mid(a.x, b.x, c.x);
        r.y = mid(a.y, b.y, c.y);
        return r;
    }

    public static Vector2l clamp(Vector2l r, final Vector2l v, final Vector2l m, final Vector2l M)
    {
        if (r == null) {
            r = new Vector2l();
        }
        r.x = clamp(v.x, m.x, M.x);
        r.y = clamp(v.y, m.y, M.y);
        return r;
    }

    public static Vector2l abs(Vector2l r, final Vector2l a) {
        if (r == null) {
            r = new Vector2l();
        }
        r.x = Math.abs(a.x);
        r.y = Math.abs(a.y);
        return r;
    }

    public static int dot(final Vector2l a, final Vector2l b) {
        return a.x * b.x + a.y * b.y;
    }

    public static Vector2l negate(Vector2l Out, final Vector2l V) {
        if (Out == null) {
            Out = new Vector2l();
        }
        Out.x = -V.x;
        Out.y = -V.y;
        return Out;
    }

    public static Vector2l negate(final Vector2l V) {
        return negate(V, V);
    }

    public static int lengthSq(final Vector2l v) {
        return dot(v, v);
    }

    public static int length(final Vector2l v) {
        return (int)Math.sqrt(dot(v, v));
    }

    public static int distanceSq(final Vector2l a, final Vector2l b) {
        final int Vx = b.x - a.x;
        final int Vy = b.y - a.y;
        return Vx * Vx + Vy * Vy;
    }

    public static int distance(final Vector2l a, final Vector2l b) {
        return (int)Math.sqrt(distanceSq(a, b));
    }

    public static int compare(final Vector2l A, final Vector2l B) {
        if (A.x < B.x)
            return -1;
        if (A.x > B.x)
            return 1;
        if (A.y < B.y)
            return -1;
        if (A.y > B.y)
            return 1;
        return 0;
    }

    //===========================================================================
    //
    // Vector3f
    //
    //===========================================================================
    public static final Vector3f VEC3F_ZERO = Vec3f(0, 0, 0);
    public static final Vector3f VEC3F_ONE = Vec3f(1, 1, 1);
    public static final Vector3f VEC3F_MINUS_ONE = Vec3f(-1, -1, -1);
    public static final Vector3f VEC3F_TWO = Vec3f(2, 2, 2);
    public static final Vector3f VEC3F_MINUS_TWO = Vec3f(-2, -2, -2);
    public static final Vector3f VEC3F_HALF = Vec3f(0.5f, 0.5f, 0.5f);
    public static final Vector3f VEC3F_MINUS_HALF = Vec3f(-0.5f, -0.5f, -0.5f);
    public static final Vector3f VEC3F_X = Vec3f(1, 0, 0);
    public static final Vector3f VEC3F_MINUS_X = Vec3f(1, 0, 0);
    public static final Vector3f VEC3F_Y = Vec3f(0, 1, 0);
    public static final Vector3f VEC3F_MINUS_Y = Vec3f(0, -1, 0);
    public static final Vector3f VEC3F_Z = Vec3f(0, 0, 1);
    public static final Vector3f VEC3F_MINUS_Z = Vec3f(0, 0, -1);

    public static Vector3f Vec3f() {
        return new Vector3f();
    }

    public static Vector3f Vec3f(final int x, final int y, final int z) {
        return (new Vector3f()).set(x, y, z);
    }

    public static Vector3f Vec3f(final float x, final float y, final float z) {
        return (new Vector3f()).set(x, y, z);
    }

    public static Vector3f Vec3f(final Object o) {
        return (new Vector3f()).set(o);
    }

    public static Vector3f add(Vector3f r, final Vector3f a, final Vector3f b) {
        if (r == null) {
            r = new Vector3f();
        }
        r.x = a.x + b.x;
        r.y = a.y + b.y;
        r.z = a.z + b.z;
        return r;
    }

    public static Vector3f add(Vector3f r, final Vector3f a, final Vector3f b, final Vector3f... o)
    {
        if (r == null) {
            r = new Vector3f();
        }
        r.x = a.x + b.x;
        r.y = a.y + b.y;
        r.z = a.z + b.z;
        for (final Vector3f c : o) {
            r.x += c.x;
            r.y += c.y;
            r.z += c.z;
        }
        return r;
    }

    public static Vector3f add(Vector3f r, final Vector3f a, final float b) {
        if (r == null) {
            r = new Vector3f();
        }
        r.x = a.x + b;
        r.y = a.y + b;
        r.z = a.z + b;
        return r;
    }

    public static Vector3f sub(Vector3f r, final Vector3f a, final Vector3f b) {
        if (r == null) {
            r = new Vector3f();
        }
        r.x = a.x - b.x;
        r.y = a.y - b.y;
        r.z = a.z - b.z;
        return r;
    }

    public static Vector3f sub(Vector3f r, final Vector3f a, final float b) {
        if (r == null) {
            r = new Vector3f();
        }
        r.x = a.x - b;
        r.y = a.y - b;
        r.z = a.z - b;
        return r;
    }

    public static Vector3f mul(Vector3f r, final Vector3f a, final Vector3f b) {
        if (r == null) {
            r = new Vector3f();
        }
        r.x = a.x * b.x;
        r.y = a.y * b.y;
        r.z = a.z * b.z;
        return r;
    }

    public static Vector3f mul(Vector3f r, final Vector3f a, final float b) {
        if (r == null) {
            r = new Vector3f();
        }
        r.x = a.x * b;
        r.y = a.y * b;
        r.z = a.z * b;
        return r;
    }

    public static Vector3f div(Vector3f r, final Vector3f a, final Vector3f b) {
        if (r == null) {
            r = new Vector3f();
        }
        r.x = a.x / b.x;
        r.y = a.y / b.y;
        r.z = a.z / b.z;
        return r;
    }

    public static Vector3f div(Vector3f r, final Vector3f a, final float b) {
        if (r == null) {
            r = new Vector3f();
        }
        r.x = a.x / b;
        r.y = a.y / b;
        r.z = a.z / b;
        return r;
    }

    public static Vector3f mad(Vector3f r, final Vector3f a, final Vector3f b, final Vector3f c)
    {
        if (r == null) {
            r = new Vector3f();
        }
        r.x = a.x * b.x + c.x;
        r.y = a.y * b.y + c.y;
        r.z = a.z * b.z + c.z;
        return c;
    }

    public static Vector3f mad(Vector3f r, final Vector3f a, final float b, final Vector3f c)
    {
        if (r == null) {
            r = new Vector3f();
        }
        r.x = a.x * b + c.x;
        r.y = a.y * b + c.y;
        r.z = a.z * b + c.z;
        return c;
    }

    public static Vector3f min(Vector3f r, final Vector3f a, final Vector3f b) {
        if (r == null) {
            r = new Vector3f();
        }
        r.x = min(a.x, b.x);
        r.y = min(a.y, b.y);
        r.z = min(a.z, b.z);
        return r;
    }

    public static Vector3f max(Vector3f r, final Vector3f a, final Vector3f b) {
        if (r == null) {
            r = new Vector3f();
        }
        r.x = max(a.x, b.x);
        r.y = max(a.y, b.y);
        r.z = max(a.z, b.z);
        return r;
    }

    public static Vector3f mid(Vector3f r, final Vector3f a, final Vector3f b, final Vector3f c)
    {
        if (r == null) {
            r = new Vector3f();
        }
        r.x = mid(a.x, b.x, c.x);
        r.y = mid(a.y, b.y, c.y);
        r.z = mid(a.z, b.z, c.z);
        return r;
    }

    public static Vector3f clamp(Vector3f r, final Vector3f v, final Vector3f m, final Vector3f M)
    {
        if (r == null) {
            r = new Vector3f();
        }
        r.x = clamp(v.x, m.x, M.x);
        r.y = clamp(v.y, m.y, M.y);
        r.z = clamp(v.z, m.z, M.z);
        return r;
    }

    public static Vector3f abs(Vector3f r, final Vector3f a) {
        if (r == null) {
            r = new Vector3f();
        }
        r.x = Math.abs(a.x);
        r.y = Math.abs(a.y);
        r.z = Math.abs(a.z);
        return r;
    }

    public static Vector3f lerp(Vector3f r, final Vector3f a, final Vector3f b, final float fac)
    {
        if (r == null) {
            r = new Vector3f();
        }
        r.x = lerp(a.x, b.x, fac);
        r.y = lerp(a.y, b.y, fac);
        r.z = lerp(a.z, b.z, fac);
        return r;
    }

    public static Vector3f blendIntoAccumulator(final Vector3f r, final Vector3f accumulator, final Vector3f newValue, final float smoothRate)
    {
        return Vec.lerp(r, accumulator, newValue, Vec.clamp(smoothRate, 0, 1));
    }

    public static float dot(final Vector3f a, final Vector3f b) {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }

    public static float angle(final Vector3f a, final Vector3f b) {
        float dls = dot(a, b) / (length(a) * length(b));
        if (dls < -1f) {
            dls = -1f;
        }
        else if (dls > 1.0f) {
            dls = 1.0f;
        }
        return (float)Math.acos(dls);
    }

    public static Vector3f cross(Vector3f Out, final Vector3f V1, final Vector3f V2) {
        if (Out == null) {
            Out = new Vector3f();
        }
        final float x = V1.y * V2.z - V1.z * V2.y;
        final float y = V1.z * V2.x - V1.x * V2.z;
        final float z = V1.x * V2.y - V1.y * V2.x;
        Out.x = x;
        Out.y = y;
        Out.z = z;
        return Out;
    }

    public static Vector3f setLength(Vector3f Out, final Vector3f V, final float l) {
        if (Out == null) {
            Out = new Vector3f();
        }
        final float fac = l / (float)Math.sqrt(V.x * V.x + V.y * V.y + V.z * V.z);
        Out.x = V.x * fac;
        Out.y = V.y * fac;
        Out.z = V.z * fac;
        return Out;
    }

    public static boolean isNormal(final Vector3f V, final float epsilon) {
        return fuzzyEqual(lengthSq(V), 1, epsilon);
    }

    public static boolean isNormal(final Vector3f V) {
        return fuzzyEqual(lengthSq(V), 1);
    }

    public static Vector3f normalize(Vector3f Out, final Vector3f V) {
        if (Out == null) {
            Out = new Vector3f();
        }
        float denom = (float)Math.sqrt(V.x * V.x + V.y * V.y + V.z * V.z);
        denom = 1.0f / denom;
        Out.x = V.x * denom;
        Out.y = V.y * denom;
        Out.z = V.z * denom;
        return Out;
    }

    public static Vector3f normalize(final Vector3f V) {
        return normalize(V, V);
    }

    public static Vector3f negate(Vector3f Out, final Vector3f V) {
        if (Out == null) {
            Out = new Vector3f();
        }
        Out.x = -V.x;
        Out.y = -V.y;
        Out.z = -V.z;
        return Out;
    }

    public static Vector3f negate(final Vector3f V) {
        return negate(V, V);
    }

    public static Vector3f invert(Vector3f Out, final Vector3f V) {
        if (Out == null) {
            Out = new Vector3f();
        }
        Out.x = 1.0f / V.x;
        Out.y = 1.0f / V.y;
        Out.z = 1.0f / V.z;
        return Out;
    }

    public static Vector3f invert(final Vector3f V) {
        return invert(V, V);
    }

    public static float lengthSq(final Vector3f v) {
        return dot(v, v);
    }

    public static float length(final Vector3f v) {
        return (float)Math.sqrt(dot(v, v));
    }

    public static float distanceSq(final Vector3f a, final Vector3f b) {
        final float Vx = b.x - a.x;
        final float Vy = b.y - a.y;
        final float Vz = b.z - a.z;
        return Vx * Vx + Vy * Vy + Vz * Vz;
    }

    public static float distance(final Vector3f a, final Vector3f b) {
        return (float)Math.sqrt(distanceSq(a, b));
    }

    public static boolean fuzzyEqual(final Vector3f A, final Vector3f B, final float epsilon)
    {
        return (Math.abs(A.x - B.x) <= epsilon) &&
               (Math.abs(A.y - B.y) <= epsilon) &&
               (Math.abs(A.z - B.z) <= epsilon);
    }

    public static boolean fuzzyEqual(final Vector3f A, final Vector3f B) {
        return fuzzyEqual(A, B, EPSILON5);
    }

    public static int compare(final Vector3f A, final Vector3f B) {
        if (A.x < B.x)
            return -1;
        if (A.x > B.x)
            return 1;
        if (A.y < B.y)
            return -1;
        if (A.y > B.y)
            return 1;
        if (A.z < B.z)
            return -1;
        if (A.z > B.z)
            return 1;
        return 0;
    }

    public static Vector4f transform(Vector4f Out, final Vector3f V, final Matrixf M) {
        if (Out == null) {
            Out = new Vector4f();
        }
        float x, y, z;
        x = V.x;
        y = V.y;
        z = V.z;
        Out.x = x * M._11 + y * M._21 + z * M._31 + M._41;
        Out.y = x * M._12 + y * M._22 + z * M._32 + M._42;
        Out.z = x * M._13 + y * M._23 + z * M._33 + M._43;
        Out.w = x * M._14 + y * M._24 + z * M._34 + M._44;
        return Out;
    }

    public static Vector3f transformCoord(Vector3f Out, final Vector3f V, final Matrixf M)
    {
        if (Out == null) {
            Out = new Vector3f();
        }
        float rhw, x, y, z;
        x = V.x;
        y = V.y;
        z = V.z;
        rhw = (x * M._14 + y * M._24 + z * M._34 + M._44);
        if (rhw == 0) {
            Out.x = Out.y = Out.z = 0;
            return Out;
        }
        rhw = 1.0f / rhw;
        Out.x = (x * M._11 + y * M._21 + z * M._31 + M._41) * rhw;
        Out.y = (x * M._12 + y * M._22 + z * M._32 + M._42) * rhw;
        Out.z = (x * M._13 + y * M._23 + z * M._33 + M._43) * rhw;
        return Out;
    }

    public static Vector3f transformNormal(Vector3f Out, final Vector3f V, final Matrixf M)
    {
        if (Out == null) {
            Out = new Vector3f();
        }
        float x, y, z;
        x = V.x;
        y = V.y;
        z = V.z;
        Out.x = x * M._11 + y * M._21 + z * M._31;
        Out.y = x * M._12 + y * M._22 + z * M._32;
        Out.z = x * M._13 + y * M._23 + z * M._33;
        return Out;
    }

    public static Vector3f toViewport(Vector3f Out, final Vector3f avProjected, final Vector4f aRect)
    {
        if (Out == null) {
            Out = new Vector3f();
        }
        final float fVPW2 = aRect.getWidth() * 0.5f;
        final float fVPH2 = aRect.getHeight() * 0.5f;
        final float fVPLeft = aRect.getLeft();
        final float fVPBot = aRect.getBottom();
        final float fVPTop = aRect.getTop();
        Out.x = (avProjected.x) * fVPW2 + (fVPLeft + fVPW2);
        Out.y = fVPBot - ((avProjected.y) * fVPH2 + (fVPTop + fVPH2));
        Out.z = avProjected.z;
        return Out;
    }

    public static Vector3f project(Vector3f Out, final Vector3f avPos, final Matrixf amtxWVP, final Vector4f aRect)
    {
        if (Out == null) {
            Out = new Vector3f();
        }
        final Vector3f projected = transformCoord(null, avPos, amtxWVP);
        final float fVPW2 = aRect.getWidth() * 0.5f;
        final float fVPH2 = aRect.getHeight() * 0.5f;
        final float fVPLeft = aRect.getLeft();
        final float fVPBot = aRect.getBottom();
        final float fVPTop = aRect.getTop();
        Out.x = (projected.x) * fVPW2 + (fVPLeft + fVPW2);
        Out.y = fVPBot - ((projected.y) * fVPH2 + (fVPTop + fVPH2));
        Out.z = projected.z;
        return Out;
    }

    public static Vector4f projectRHW(Vector4f Out, final Vector3f avPos, final Matrixf mtxWVP, final Vector4f aRect)
    {
        if (Out == null) {
            Out = new Vector4f();
        }
        final Vector4f v1 = transform(null, avPos, mtxWVP);
        final float fVPW2 = aRect.getWidth() * 0.5f;
        final float fVPH2 = aRect.getHeight() * 0.5f;
        final float fVPLeft = aRect.getLeft();
        final float fVPBot = aRect.getBottom();
        final float fVPTop = aRect.getTop();
        final float RHW = finvert(v1.w);
        Out.x = (v1.x * RHW) * fVPW2 + (fVPLeft + fVPW2);
        Out.y = fVPBot - ((v1.y * RHW) * fVPH2 + (fVPTop + fVPH2));
        Out.z *= RHW;
        Out.w = RHW;
        return Out;
    }

    public static Vector3f unproject(Vector3f Out, final Vector3f avIn,
            final float afVPX, final float afVPY, final float afVPW, final float afVPH,
            final Matrixf amtxInvViewProj)
    {
        if (Out == null) {
            Out = new Vector3f();
        }

        // Compute normalized coordinates between -1 and 1.
        final Vector4f vNormalized = Vec4f(
                ((avIn.x - afVPX) * 2.0f / afVPW) - 1.0f,
                -(((avIn.y - afVPY) * 2.0f / afVPH) - 1.0f),
                2.0f * avIn.z - 1.0f,
                1.0f);

        // Object coordinates.
        transform(vNormalized, vNormalized, amtxInvViewProj);
        if (vNormalized.w == 0.0f) {
            Out.x = Out.y = Out.z = 0.0f;
            return Out;
        }
        Out.x = vNormalized.x / vNormalized.w;
        Out.y = vNormalized.y / vNormalized.w;
        Out.z = vNormalized.z / vNormalized.w;
        return Out;
    }

    public static Vector3f reflect(Vector3f Out, final Vector3f Dir, final Vector3f Normal)
    {
        if (Out == null) {
            Out = new Vector3f();
        }
        return sub(Out, Dir, mul(Out, Normal, dot(Normal, Dir) * 2.0f));
    }

    //===========================================================================
    //
    // Vector3l
    //
    //===========================================================================
    public static final Vector3l VEC3L_ZERO = Vec3l(0, 0, 0);
    public static final Vector3l VEC3L_ONE = Vec3l(1, 1, 1);
    public static final Vector3l VEC3L_MINUS_ONE = Vec3l(-1, -1, -1);
    public static final Vector3l VEC3L_TWO = Vec3l(2, 2, 2);
    public static final Vector3l VEC3L_MINUS_TWO = Vec3l(-2, -2, -2);
    public static final Vector3l VEC3L_X = Vec3l(1, 0, 0);
    public static final Vector3l VEC3L_MINUS_X = Vec3l(1, 0, 0);
    public static final Vector3l VEC3L_Y = Vec3l(0, 1, 0);
    public static final Vector3l VEC3L_MINUS_Y = Vec3l(0, -1, 0);
    public static final Vector3l VEC3L_Z = Vec3l(0, 0, 1);
    public static final Vector3l VEC3L_MINUS_Z = Vec3l(0, 0, -1);

    public static Vector3l Vec3l() {
        return new Vector3l();
    }

    public static Vector3l Vec3l(final int x, final int y, final int z) {
        return (new Vector3l()).set(x, y, z);
    }

    public static Vector3l Vec3l(final float x, final float y, final float z) {
        return (new Vector3l()).set(x, y, z);
    }

    public static Vector3l Vec3l(final Object o) {
        return (new Vector3l()).set(o);
    }

    public static Vector3l add(Vector3l r, final Vector3l a, final Vector3l b) {
        if (r == null) {
            r = new Vector3l();
        }
        r.x = a.x + b.x;
        r.y = a.y + b.y;
        r.z = a.z + b.z;
        return r;
    }

    public static Vector3l add(Vector3l r, final Vector3l a, final int b) {
        if (r == null) {
            r = new Vector3l();
        }
        r.x = a.x + b;
        r.y = a.y + b;
        r.z = a.z + b;
        return r;
    }

    public static Vector3l sub(Vector3l r, final Vector3l a, final Vector3l b) {
        if (r == null) {
            r = new Vector3l();
        }
        r.x = a.x - b.x;
        r.y = a.y - b.y;
        r.z = a.z - b.z;
        return r;
    }

    public static Vector3l sub(Vector3l r, final Vector3l a, final int b) {
        if (r == null) {
            r = new Vector3l();
        }
        r.x = a.x - b;
        r.y = a.y - b;
        r.z = a.z - b;
        return r;
    }

    public static Vector3l mul(Vector3l r, final Vector3l a, final Vector3l b) {
        if (r == null) {
            r = new Vector3l();
        }
        r.x = a.x * b.x;
        r.y = a.y * b.y;
        r.z = a.z * b.z;
        return r;
    }

    public static Vector3l mul(Vector3l r, final Vector3l a, final int b) {
        if (r == null) {
            r = new Vector3l();
        }
        r.x = a.x * b;
        r.y = a.y * b;
        r.z = a.z * b;
        return r;
    }

    public static Vector3l div(Vector3l r, final Vector3l a, final Vector3l b) {
        if (r == null) {
            r = new Vector3l();
        }
        r.x = a.x / b.x;
        r.y = a.y / b.y;
        r.z = a.z / b.z;
        return r;
    }

    public static Vector3l div(Vector3l r, final Vector3l a, final int b) {
        if (r == null) {
            r = new Vector3l();
        }
        r.x = a.x / b;
        r.y = a.y / b;
        r.z = a.z / b;
        return r;
    }

    public static Vector3l mad(Vector3l r, final Vector3l a, final Vector3l b, final Vector3l c)
    {
        if (r == null) {
            r = new Vector3l();
        }
        r.x = a.x * b.x + c.x;
        r.y = a.y * b.y + c.y;
        r.z = a.z * b.z + c.z;
        return c;
    }

    public static Vector3l mad(Vector3l r, final Vector3l a, final int b, final Vector3l c)
    {
        if (r == null) {
            r = new Vector3l();
        }
        r.x = a.x * b + c.x;
        r.y = a.y * b + c.y;
        r.z = a.z * b + c.z;
        return c;
    }

    public static Vector3l min(Vector3l r, final Vector3l a, final Vector3l b) {
        if (r == null) {
            r = new Vector3l();
        }
        r.x = Math.min(a.x, b.x);
        r.y = Math.min(a.y, b.y);
        r.z = Math.min(a.z, b.z);
        return r;
    }

    public static Vector3l max(Vector3l r, final Vector3l a, final Vector3l b) {
        if (r == null) {
            r = new Vector3l();
        }
        r.x = Math.max(a.x, b.x);
        r.y = Math.max(a.y, b.y);
        r.z = Math.max(a.z, b.z);
        return r;
    }

    public static Vector3l mid(Vector3l r, final Vector3l a, final Vector3l b, final Vector3l c)
    {
        if (r == null) {
            r = new Vector3l();
        }
        r.x = mid(a.x, b.x, c.x);
        r.y = mid(a.y, b.y, c.y);
        r.z = mid(a.z, b.z, c.z);
        return r;
    }

    public static Vector3l clamp(Vector3l r, final Vector3l v, final Vector3l m, final Vector3l M)
    {
        if (r == null) {
            r = new Vector3l();
        }
        r.x = clamp(v.x, m.x, M.x);
        r.y = clamp(v.y, m.y, M.y);
        r.z = clamp(v.z, m.z, M.z);
        return r;
    }

    public static Vector3l abs(Vector3l r, final Vector3l a) {
        if (r == null) {
            r = new Vector3l();
        }
        r.x = Math.abs(a.x);
        r.y = Math.abs(a.y);
        r.z = Math.abs(a.z);
        return r;
    }

    public static int dot(final Vector3l a, final Vector3l b) {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }

    public static Vector3l negate(Vector3l Out, final Vector3l V) {
        if (Out == null) {
            Out = new Vector3l();
        }
        Out.x = -V.x;
        Out.y = -V.y;
        Out.z = -V.z;
        return Out;
    }

    public static Vector3l negate(final Vector3l V) {
        return negate(V, V);
    }

    public static int lengthSq(final Vector3l v) {
        return dot(v, v);
    }

    public static int length(final Vector3l v) {
        return (int)Math.sqrt(dot(v, v));
    }

    public static int distanceSq(final Vector3l a, final Vector3l b) {
        final int Vx = b.x - a.x;
        final int Vy = b.y - a.y;
        final int Vz = b.z - a.z;
        return Vx * Vx + Vy * Vy + Vz * Vz;
    }

    public static int distance(final Vector3l a, final Vector3l b) {
        return (int)Math.sqrt(distanceSq(a, b));
    }

    public static int compare(final Vector3l A, final Vector3l B) {
        if (A.x < B.x)
            return -1;
        if (A.x > B.x)
            return 1;
        if (A.y < B.y)
            return -1;
        if (A.y > B.y)
            return 1;
        if (A.z < B.z)
            return -1;
        if (A.z > B.z)
            return 1;
        return 0;
    }

    //===========================================================================
    //
    // Vector4f
    //
    //===========================================================================
    public static final Vector4f VEC4F_ZERO = Vec4f(0, 0, 0, 0);
    public static final Vector4f VEC4F_ONE = Vec4f(1, 1, 1, 1);
    public static final Vector4f VEC4F_MINUS_ONE = Vec4f(-1, -1, -1, -1);
    public static final Vector4f VEC4F_TWO = Vec4f(2, 2, 2, 2);
    public static final Vector4f VEC4F_MINUS_TWO = Vec4f(-2, -2, -2, -2);
    public static final Vector4f VEC4F_HALF = Vec4f(0.5f, 0.5f, 0.5f, 0.5f);
    public static final Vector4f VEC4F_MINUS_HALF = Vec4f(-0.5f, -0.5f, -0.5f, -0.5f);
    public static final Vector4f VEC4F_X = Vec4f(1, 0, 0, 0);
    public static final Vector4f VEC4F_MINUS_X = Vec4f(1, 0, 0, 0);
    public static final Vector4f VEC4F_Y = Vec4f(0, 1, 0, 0);
    public static final Vector4f VEC4F_MINUS_Y = Vec4f(0, -1, 0, 0);
    public static final Vector4f VEC4F_Z = Vec4f(0, 0, 1, 0);
    public static final Vector4f VEC4F_MINUS_Z = Vec4f(0, 0, -1, 0);
    public static final Vector4f VEC4F_W = Vec4f(0, 0, 0, 1);
    public static final Vector4f VEC4F_MINUS_W = Vec4f(0, 0, 0, -1);

    public static Vector4f Vec4f() {
        return new Vector4f();
    }

    public static Vector4f Vec4f(final int x, final int y, final int z, final int w) {
        return (new Vector4f()).set(x, y, z, w);
    }

    public static Vector4f Vec4f(final float x, final float y, final float z, final float w)
    {
        return (new Vector4f()).set(x, y, z, w);
    }

    public static Vector4f Vec4f(final Object o) {
        return (new Vector4f()).set(o);
    }

    public static Vector4f add(Vector4f r, final Vector4f a, final Vector4f b) {
        if (r == null) {
            r = new Vector4f();
        }
        r.x = a.x + b.x;
        r.y = a.y + b.y;
        r.z = a.z + b.z;
        r.w = a.w + b.w;
        return r;
    }

    public static Vector4f add(Vector4f r, final Vector4f a, final Vector4f b, final Vector4f... o)
    {
        if (r == null) {
            r = new Vector4f();
        }
        r.x = a.x + b.x;
        r.y = a.y + b.y;
        r.z = a.z + b.z;
        r.w = a.w + b.w;
        for (final Vector4f c : o) {
            r.x += c.x;
            r.y += c.y;
            r.z += c.z;
            r.w += c.w;
        }
        return r;
    }

    public static Vector4f add(Vector4f r, final Vector4f a, final float b) {
        if (r == null) {
            r = new Vector4f();
        }
        r.x = a.x + b;
        r.y = a.y + b;
        r.z = a.z + b;
        r.w = a.w + b;
        return r;
    }

    public static Vector4f sub(Vector4f r, final Vector4f a, final Vector4f b) {
        if (r == null) {
            r = new Vector4f();
        }
        r.x = a.x - b.x;
        r.y = a.y - b.y;
        r.z = a.z - b.z;
        r.w = a.w - b.w;
        return r;
    }

    public static Vector4f sub(Vector4f r, final Vector4f a, final float b) {
        if (r == null) {
            r = new Vector4f();
        }
        r.x = a.x - b;
        r.y = a.y - b;
        r.z = a.z - b;
        r.w = a.w - b;
        return r;
    }

    public static Vector4f mul(Vector4f r, final Vector4f a, final Vector4f b) {
        if (r == null) {
            r = new Vector4f();
        }
        r.x = a.x * b.x;
        r.y = a.y * b.y;
        r.z = a.z * b.z;
        r.w = a.w * b.w;
        return r;
    }

    public static Vector4f mul(Vector4f r, final Vector4f a, final float b) {
        if (r == null) {
            r = new Vector4f();
        }
        r.x = a.x * b;
        r.y = a.y * b;
        r.z = a.z * b;
        r.w = a.w * b;
        return r;
    }

    public static Vector4f div(Vector4f r, final Vector4f a, final Vector4f b) {
        if (r == null) {
            r = new Vector4f();
        }
        r.x = a.x / b.x;
        r.y = a.y / b.y;
        r.z = a.z / b.z;
        r.w = a.w / b.w;
        return r;
    }

    public static Vector4f div(Vector4f r, final Vector4f a, final float b) {
        if (r == null) {
            r = new Vector4f();
        }
        r.x = a.x / b;
        r.y = a.y / b;
        r.z = a.z / b;
        r.w = a.w / b;
        return r;
    }

    public static Vector4f mad(Vector4f r, final Vector4f a, final Vector4f b, final Vector4f c)
    {
        if (r == null) {
            r = new Vector4f();
        }
        r.x = a.x * b.x + c.x;
        r.y = a.y * b.y + c.y;
        r.z = a.z * b.z + c.z;
        r.w = a.w * b.w + c.w;
        return c;
    }

    public static Vector4f mad(Vector4f r, final Vector4f a, final float b, final Vector4f c)
    {
        if (r == null) {
            r = new Vector4f();
        }
        r.x = a.x * b + c.x;
        r.y = a.y * b + c.y;
        r.z = a.z * b + c.z;
        r.w = a.w * b + c.w;
        return c;
    }

    public static Vector4f min(Vector4f r, final Vector4f a, final Vector4f b) {
        if (r == null) {
            r = new Vector4f();
        }
        r.x = Math.min(a.x, b.x);
        r.y = Math.min(a.y, b.y);
        r.z = Math.min(a.z, b.z);
        r.w = Math.min(a.w, b.w);
        return r;
    }

    public static Vector4f max(Vector4f r, final Vector4f a, final Vector4f b) {
        if (r == null) {
            r = new Vector4f();
        }
        r.x = Math.max(a.x, b.x);
        r.y = Math.max(a.y, b.y);
        r.z = Math.max(a.z, b.z);
        r.w = Math.max(a.w, b.w);
        return r;
    }

    public static Vector4f mid(Vector4f r, final Vector4f a, final Vector4f b, final Vector4f c)
    {
        if (r == null) {
            r = new Vector4f();
        }
        r.x = mid(a.x, b.x, c.x);
        r.y = mid(a.y, b.y, c.y);
        r.z = mid(a.z, b.z, c.z);
        r.w = mid(a.w, b.w, c.w);
        return r;
    }

    public static Vector4f clamp(Vector4f r, final Vector4f v, final Vector4f m, final Vector4f M)
    {
        if (r == null) {
            r = new Vector4f();
        }
        r.x = clamp(v.x, m.x, M.x);
        r.y = clamp(v.y, m.y, M.y);
        r.z = clamp(v.z, m.z, M.z);
        r.w = clamp(v.w, m.w, M.w);
        return r;
    }

    public static Vector4f abs(Vector4f r, final Vector4f a) {
        if (r == null) {
            r = new Vector4f();
        }
        r.x = Math.abs(a.x);
        r.y = Math.abs(a.y);
        r.z = Math.abs(a.z);
        r.w = Math.abs(a.w);
        return r;
    }

    public static Vector4f lerp(Vector4f r, final Vector4f a, final Vector4f b, final float fac)
    {
        if (r == null) {
            r = new Vector4f();
        }
        r.x = lerp(a.x, b.x, fac);
        r.y = lerp(a.y, b.y, fac);
        r.z = lerp(a.z, b.z, fac);
        r.w = lerp(a.w, b.w, fac);
        return r;
    }

    public static Vector4f blendIntoAccumulator(final Vector4f r, final Vector4f accumulator, final Vector4f newValue, final float smoothRate)
    {
        return Vec.lerp(r, accumulator, newValue, Vec.clamp(smoothRate, 0, 1));
    }

    public static float dot(final Vector4f a, final Vector4f b) {
        return a.x * b.x + a.y * b.y + a.z * b.z + a.w * b.w;
    }

    public static float angle(final Vector4f a, final Vector4f b) {
        float dls = dot(a, b) / (length(a) * length(b));
        if (dls < -1f) {
            dls = -1f;
        }
        else if (dls > 1.0f) {
            dls = 1.0f;
        }
        return (float)Math.acos(dls);
    }

    public static Vector4f cross(Vector4f Out, final Vector4f V1, final Vector4f V2, final Vector4f V3)
    {
        if (Out == null) {
            Out = new Vector4f();
        }
        float a, b, c, d, e, f;
        a = V2.x * V3.y - V2.y * V3.x;
        b = V2.x * V3.z - V2.z * V3.x;
        c = V2.x * V3.w - V2.w * V3.x;
        d = V2.y * V3.z - V2.z * V3.y;
        e = V2.y * V3.w - V2.w * V3.y;
        f = V2.z * V3.w - V2.w * V3.z;
        final float x = f * V1.y - e * V1.z + d * V1.w;
        final float y = -f * V1.x - c * V1.z + b * V1.w;
        final float z = e * V1.x - c * V1.y + a * V1.w;
        final float w = -d * V1.x - b * V1.y + a * V1.z;
        Out.x = x;
        Out.y = y;
        Out.z = z;
        Out.w = w;
        return Out;
    }

    public static Vector4f setLength(Vector4f Out, final Vector4f V, final float l) {
        if (Out == null) {
            Out = new Vector4f();
        }
        final float fac = l / (float)Math.sqrt(V.x * V.x + V.y * V.y + V.z * V.z + V.w * V.w);
        Out.x = V.x * fac;
        Out.y = V.y * fac;
        Out.z = V.z * fac;
        Out.w = V.w * fac;
        return Out;
    }

    public static boolean isNormal(final Vector4f V, final float epsilon) {
        return fuzzyEqual(lengthSq(V), 1, epsilon);
    }

    public static boolean isNormal(final Vector4f V) {
        return fuzzyEqual(lengthSq(V), 1);
    }

    public static Vector4f normalize(Vector4f Out, final Vector4f V) {
        if (Out == null) {
            Out = new Vector4f();
        }
        float denom = (float)Math.sqrt(V.x * V.x + V.y * V.y + V.z * V.z + V.w * V.w);
        denom = 1.0f / denom;
        Out.x = V.x * denom;
        Out.y = V.y * denom;
        Out.z = V.z * denom;
        Out.w = V.w * denom;
        return Out;
    }

    public static Vector4f normalize(final Vector4f V) {
        return normalize(V, V);
    }

    public static Vector4f negate(Vector4f Out, final Vector4f V) {
        if (Out == null) {
            Out = new Vector4f();
        }
        Out.x = -V.x;
        Out.y = -V.y;
        Out.z = -V.z;
        Out.w = -V.w;
        return Out;
    }

    public static Vector4f negate(final Vector4f V) {
        return negate(V, V);
    }

    public static Vector4f invert(Vector4f Out, final Vector4f V) {
        if (Out == null) {
            Out = new Vector4f();
        }
        Out.x = 1.0f / V.x;
        Out.y = 1.0f / V.y;
        Out.z = 1.0f / V.z;
        Out.w = 1.0f / V.w;
        return Out;
    }

    public static Vector4f invert(final Vector4f V) {
        return invert(V, V);
    }

    public static float lengthSq(final Vector4f v) {
        return dot(v, v);
    }

    public static float length(final Vector4f v) {
        return (float)Math.sqrt(dot(v, v));
    }

    public static float distanceSq(final Vector4f a, final Vector4f b) {
        final float Vx = b.x - a.x;
        final float Vy = b.y - a.y;
        final float Vz = b.z - a.z;
        final float Vw = b.w - a.w;
        return Vx * Vx + Vy * Vy + Vz * Vz + Vw * Vw;
    }

    public static float distance(final Vector4f a, final Vector4f b) {
        return (float)Math.sqrt(distanceSq(a, b));
    }

    public static boolean fuzzyEqual(final Vector4f A, final Vector4f B, final float epsilon)
    {
        return (Math.abs(A.x - B.x) <= epsilon) &&
               (Math.abs(A.y - B.y) <= epsilon) &&
               (Math.abs(A.z - B.z) <= epsilon) &&
               (Math.abs(A.w - B.w) <= epsilon);
    }

    public static boolean fuzzyEqual(final Vector4f A, final Vector4f B) {
        return fuzzyEqual(A, B, EPSILON5);
    }

    public static int compare(final Vector4f A, final Vector4f B) {
        if (A.x < B.x)
            return -1;
        if (A.x > B.x)
            return 1;
        if (A.y < B.y)
            return -1;
        if (A.y > B.y)
            return 1;
        if (A.z < B.z)
            return -1;
        if (A.z > B.z)
            return 1;
        if (A.w < B.w)
            return -1;
        if (A.w > B.w)
            return 1;
        return 0;
    }

    public static Vector4f transform(Vector4f Out, final Vector4f V, final Matrixf M) {
        if (Out == null) {
            Out = new Vector4f();
        }
        float x, y, z, w;
        x = V.x;
        y = V.y;
        z = V.z;
        w = V.w;
        Out.x = x * M._11 + y * M._21 + z * M._31 + w * M._41;
        Out.y = x * M._12 + y * M._22 + z * M._32 + w * M._42;
        Out.z = x * M._13 + y * M._23 + z * M._33 + w * M._43;
        Out.w = x * M._14 + y * M._24 + z * M._34 + w * M._44;
        return Out;
    }

    //===========================================================================
    //
    // Vector4l
    //
    //===========================================================================
    public static final Vector4l VEC4L_ZERO = Vec4l(0, 0, 0, 0);
    public static final Vector4l VEC4L_ONE = Vec4l(1, 1, 1, 1);
    public static final Vector4l VEC4L_MINUS_ONE = Vec4l(-1, -1, -1, -1);
    public static final Vector4l VEC4L_TWO = Vec4l(2, 2, 2, 2);
    public static final Vector4l VEC4L_MINUS_TWO = Vec4l(-2, -2, -2, -2);
    public static final Vector4l VEC4L_X = Vec4l(1, 0, 0, 0);
    public static final Vector4l VEC4L_MINUS_X = Vec4l(1, 0, 0, 0);
    public static final Vector4l VEC4L_Y = Vec4l(0, 1, 0, 0);
    public static final Vector4l VEC4L_MINUS_Y = Vec4l(0, -1, 0, 0);
    public static final Vector4l VEC4L_Z = Vec4l(0, 0, 1, 0);
    public static final Vector4l VEC4L_MINUS_Z = Vec4l(0, 0, -1, 0);
    public static final Vector4l VEC4L_W = Vec4l(0, 0, 0, 1);
    public static final Vector4l VEC4L_MINUS_W = Vec4l(0, 0, 0, -1);

    public static Vector4l Vec4l() {
        return new Vector4l();
    }

    public static Vector4l Vec4l(final int x, final int y, final int z, final int w) {
        return (new Vector4l()).set(x, y, z, w);
    }

    public static Vector4l Vec4l(final float x, final float y, final float z, final float w)
    {
        return (new Vector4l()).set(x, y, z, w);
    }

    public static Vector4l Vec4l(final Object o) {
        return (new Vector4l()).set(o);
    }

    public static Vector4l add(Vector4l r, final Vector4l a, final Vector4l b) {
        if (r == null) {
            r = new Vector4l();
        }
        r.x = a.x + b.x;
        r.y = a.y + b.y;
        r.z = a.z + b.z;
        r.w = a.w + b.w;
        return r;
    }

    public static Vector4l add(Vector4l r, final Vector4l a, final int b) {
        if (r == null) {
            r = new Vector4l();
        }
        r.x = a.x + b;
        r.y = a.y + b;
        r.z = a.z + b;
        r.w = a.w + b;
        return r;
    }

    public static Vector4l sub(Vector4l r, final Vector4l a, final Vector4l b) {
        if (r == null) {
            r = new Vector4l();
        }
        r.x = a.x - b.x;
        r.y = a.y - b.y;
        r.z = a.z - b.z;
        r.w = a.w - b.w;
        return r;
    }

    public static Vector4l sub(Vector4l r, final Vector4l a, final int b) {
        if (r == null) {
            r = new Vector4l();
        }
        r.x = a.x - b;
        r.y = a.y - b;
        r.z = a.z - b;
        r.w = a.w - b;
        return r;
    }

    public static Vector4l mul(Vector4l r, final Vector4l a, final Vector4l b) {
        if (r == null) {
            r = new Vector4l();
        }
        r.x = a.x * b.x;
        r.y = a.y * b.y;
        r.z = a.z * b.z;
        r.w = a.w * b.w;
        return r;
    }

    public static Vector4l mul(Vector4l r, final Vector4l a, final int b) {
        if (r == null) {
            r = new Vector4l();
        }
        r.x = a.x * b;
        r.y = a.y * b;
        r.z = a.z * b;
        r.w = a.w * b;
        return r;
    }

    public static Vector4l div(Vector4l r, final Vector4l a, final Vector4l b) {
        if (r == null) {
            r = new Vector4l();
        }
        r.x = a.x / b.x;
        r.y = a.y / b.y;
        r.z = a.z / b.z;
        r.w = a.w / b.w;
        return r;
    }

    public static Vector4l div(Vector4l r, final Vector4l a, final int b) {
        if (r == null) {
            r = new Vector4l();
        }
        r.x = a.x / b;
        r.y = a.y / b;
        r.z = a.z / b;
        r.w = a.w / b;
        return r;
    }

    public static Vector4l mad(Vector4l r, final Vector4l a, final Vector4l b, final Vector4l c)
    {
        if (r == null) {
            r = new Vector4l();
        }
        r.x = a.x * b.x + c.x;
        r.y = a.y * b.y + c.y;
        r.z = a.z * b.z + c.z;
        r.w = a.w * b.w + c.w;
        return c;
    }

    public static Vector4l mad(Vector4l r, final Vector4l a, final int b, final Vector4l c)
    {
        if (r == null) {
            r = new Vector4l();
        }
        r.x = a.x * b + c.x;
        r.y = a.y * b + c.y;
        r.z = a.z * b + c.z;
        r.w = a.w * b + c.w;
        return c;
    }

    public static Vector4l min(Vector4l r, final Vector4l a, final Vector4l b) {
        if (r == null) {
            r = new Vector4l();
        }
        r.x = Math.min(a.x, b.x);
        r.y = Math.min(a.y, b.y);
        r.z = Math.min(a.z, b.z);
        r.w = Math.min(a.w, b.w);
        return r;
    }

    public static Vector4l max(Vector4l r, final Vector4l a, final Vector4l b) {
        if (r == null) {
            r = new Vector4l();
        }
        r.x = Math.max(a.x, b.x);
        r.y = Math.max(a.y, b.y);
        r.z = Math.max(a.z, b.z);
        r.w = Math.max(a.w, b.w);
        return r;
    }

    public static Vector4l mid(Vector4l r, final Vector4l a, final Vector4l b, final Vector4l c)
    {
        if (r == null) {
            r = new Vector4l();
        }
        r.x = mid(a.x, b.x, c.x);
        r.y = mid(a.y, b.y, c.y);
        r.z = mid(a.z, b.z, c.z);
        r.w = mid(a.w, b.w, c.w);
        return r;
    }

    public static Vector4l clamp(Vector4l r, final Vector4l v, final Vector4l m, final Vector4l M)
    {
        if (r == null) {
            r = new Vector4l();
        }
        r.x = clamp(v.x, m.x, M.x);
        r.y = clamp(v.y, m.y, M.y);
        r.z = clamp(v.z, m.z, M.z);
        r.w = clamp(v.w, m.w, M.w);
        return r;
    }

    public static Vector4l abs(Vector4l r, final Vector4l a) {
        if (r == null) {
            r = new Vector4l();
        }
        r.x = Math.abs(a.x);
        r.y = Math.abs(a.y);
        r.z = Math.abs(a.z);
        r.w = Math.abs(a.w);
        return r;
    }

    public static int dot(final Vector4l a, final Vector4l b) {
        return a.x * b.x + a.y * b.y + a.z * b.z + a.w * b.w;
    }

    public static Vector4l negate(Vector4l Out, final Vector4l V) {
        if (Out == null) {
            Out = new Vector4l();
        }
        Out.x = -V.x;
        Out.y = -V.y;
        Out.z = -V.z;
        Out.w = -V.w;
        return Out;
    }

    public static Vector4l negate(final Vector4l V) {
        return negate(V, V);
    }

    public static int lengthSq(final Vector4l v) {
        return dot(v, v);
    }

    public static int length(final Vector4l v) {
        return (int)Math.sqrt(dot(v, v));
    }

    public static int distanceSq(final Vector4l a, final Vector4l b) {
        final int Vx = b.x - a.x;
        final int Vy = b.y - a.y;
        final int Vz = b.z - a.z;
        final int Vw = b.w - a.w;
        return Vx * Vx + Vy * Vy + Vz * Vz + Vw * Vw;
    }

    public static int distance(final Vector4l a, final Vector4l b) {
        return (int)Math.sqrt(distanceSq(a, b));
    }

    public static int compare(final Vector4l A, final Vector4l B) {
        if (A.x < B.x)
            return -1;
        if (A.x > B.x)
            return 1;
        if (A.y < B.y)
            return -1;
        if (A.y > B.y)
            return 1;
        if (A.z < B.z)
            return -1;
        if (A.z > B.z)
            return 1;
        if (A.w < B.w)
            return -1;
        if (A.w > B.w)
            return 1;
        return 0;
    }
}

