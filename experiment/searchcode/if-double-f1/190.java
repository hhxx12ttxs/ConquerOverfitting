// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) nonlb 

package android.gesture;

import android.graphics.RectF;
import android.util.Log;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

// Referenced classes of package android.gesture:
//            GesturePoint, OrientedBoundingBox, Gesture, GestureStroke

public final class GestureUtils {

    private GestureUtils() {
    }

    static void closeStream(Closeable closeable) {
        if(closeable == null)
            break MISSING_BLOCK_LABEL_10;
        closeable.close();
_L1:
        return;
        IOException ioexception;
        ioexception;
        Log.e("Gestures", "Could not close stream", ioexception);
          goto _L1
    }

    static float[] computeCentroid(float af[]) {
        float f = 0.0F;
        float f1 = 0.0F;
        int i = af.length;
        int k;
        for(int j = 0; j < i; j = k + 1) {
            f += af[j];
            k = j + 1;
            f1 += af[k];
        }

        float af1[] = new float[2];
        af1[0] = (2.0F * f) / (float)i;
        af1[1] = (2.0F * f1) / (float)i;
        return af1;
    }

    private static float[][] computeCoVariance(float af[]) {
        int ai[] = new int[2];
        ai[0] = 2;
        ai[1] = 2;
        float af1[][] = (float[][])Array.newInstance(Float.TYPE, ai);
        af1[0][0] = 0.0F;
        af1[0][1] = 0.0F;
        af1[1][0] = 0.0F;
        af1[1][1] = 0.0F;
        int i = af.length;
        int k;
        for(int j = 0; j < i; j = k + 1) {
            float f = af[j];
            k = j + 1;
            float f1 = af[k];
            float af6[] = af1[0];
            af6[0] = af6[0] + f * f;
            float af7[] = af1[0];
            af7[1] = af7[1] + f * f1;
            af1[1][0] = af1[0][1];
            float af8[] = af1[1];
            af8[1] = af8[1] + f1 * f1;
        }

        float af2[] = af1[0];
        af2[0] = af2[0] / (float)(i / 2);
        float af3[] = af1[0];
        af3[1] = af3[1] / (float)(i / 2);
        float af4[] = af1[1];
        af4[0] = af4[0] / (float)(i / 2);
        float af5[] = af1[1];
        af5[1] = af5[1] / (float)(i / 2);
        return af1;
    }

    private static float[] computeOrientation(float af[][]) {
        float af1[] = new float[2];
        if(af[0][1] == 0.0F || af[1][0] == 0.0F) {
            af1[0] = 1.0F;
            af1[1] = 0.0F;
        }
        float f = -af[0][0] - af[1][1];
        float f1 = af[0][0] * af[1][1] - af[0][1] * af[1][0];
        float f2 = f / 2.0F;
        float f3 = (float)Math.sqrt(Math.pow(f2, 2D) - (double)f1);
        float f4 = f3 + -f2;
        float f5 = -f2 - f3;
        if(f4 == f5) {
            af1[0] = 0.0F;
            af1[1] = 0.0F;
        } else {
            float f6;
            if(f4 > f5)
                f6 = f4;
            else
                f6 = f5;
            af1[0] = 1.0F;
            af1[1] = (f6 - af[0][0]) / af[0][1];
        }
        return af1;
    }

    public static OrientedBoundingBox computeOrientedBoundingBox(ArrayList arraylist) {
        int i = arraylist.size();
        float af[] = new float[i * 2];
        for(int j = 0; j < i; j++) {
            GesturePoint gesturepoint = (GesturePoint)arraylist.get(j);
            int k = j * 2;
            af[k] = gesturepoint.x;
            af[k + 1] = gesturepoint.y;
        }

        return computeOrientedBoundingBox(af, computeCentroid(af));
    }

    public static OrientedBoundingBox computeOrientedBoundingBox(float af[]) {
        int i = af.length;
        float af1[] = new float[i];
        for(int j = 0; j < i; j++)
            af1[j] = af[j];

        return computeOrientedBoundingBox(af1, computeCentroid(af1));
    }

    private static OrientedBoundingBox computeOrientedBoundingBox(float af[], float af1[]) {
        translate(af, -af1[0], -af1[1]);
        float af2[] = computeOrientation(computeCoVariance(af));
        float f;
        float f1;
        float f2;
        float f3;
        float f4;
        int i;
        if(af2[0] == 0.0F && af2[1] == 0.0F) {
            f = -1.570796F;
        } else {
            f = (float)Math.atan2(af2[1], af2[0]);
            rotate(af, -f);
        }
        f1 = 3.402823E+38F;
        f2 = 3.402823E+38F;
        f3 = 1.401298E-45F;
        f4 = 1.401298E-45F;
        i = af.length;
        int k;
        for(int j = 0; j < i; j = k + 1) {
            if(af[j] < f1)
                f1 = af[j];
            if(af[j] > f3)
                f3 = af[j];
            k = j + 1;
            if(af[k] < f2)
                f2 = af[k];
            if(af[k] > f4)
                f4 = af[k];
        }

        return new OrientedBoundingBox((float)((double)(180F * f) / 3.1415926535897931D), af1[0], af1[1], f3 - f1, f4 - f2);
    }

    static float computeStraightness(float af[]) {
        float f = computeTotalLength(af);
        float f1 = af[2] - af[0];
        float f2 = af[3] - af[1];
        return (float)Math.sqrt(f1 * f1 + f2 * f2) / f;
    }

    static float computeStraightness(float af[], float f) {
        float f1 = af[2] - af[0];
        float f2 = af[3] - af[1];
        return (float)Math.sqrt(f1 * f1 + f2 * f2) / f;
    }

    static float computeTotalLength(float af[]) {
        float f = 0.0F;
        int i = -4 + af.length;
        for(int j = 0; j < i; j += 2) {
            float f1 = af[j + 2] - af[j];
            float f2 = af[j + 3] - af[j + 1];
            f = (float)((double)f + Math.sqrt(f1 * f1 + f2 * f2));
        }

        return f;
    }

    static float cosineDistance(float af[], float af1[]) {
        float f = 0.0F;
        int i = af.length;
        for(int j = 0; j < i; j++)
            f += af[j] * af1[j];

        return (float)Math.acos(f);
    }

    static float minimumCosineDistance(float af[], float af1[], int i) {
        int j = af.length;
        float f = 0.0F;
        float f1 = 0.0F;
        for(int k = 0; k < j; k += 2) {
            f += af[k] * af1[k] + af[k + 1] * af1[k + 1];
            f1 += af[k] * af1[k + 1] - af[k + 1] * af1[k];
        }

        float f2;
        if(f != 0.0F) {
            float f3 = f1 / f;
            double d = Math.atan(f3);
            if(i > 2 && Math.abs(d) >= 3.1415926535897931D / (double)i) {
                f2 = (float)Math.acos(f);
            } else {
                double d1 = Math.cos(d);
                double d2 = d1 * (double)f3;
                f2 = (float)Math.acos(d1 * (double)f + d2 * (double)f1);
            }
        } else {
            f2 = 1.570796F;
        }
        return f2;
    }

    private static void plot(float f, float f1, float af[], int i) {
        int j;
        int k;
        int l;
        int i1;
        if(f < 0.0F)
            f = 0.0F;
        if(f1 < 0.0F)
            f1 = 0.0F;
        j = (int)Math.floor(f);
        k = (int)Math.ceil(f);
        l = (int)Math.floor(f1);
        i1 = (int)Math.ceil(f1);
        if(f != (float)j || f1 != (float)l) goto _L2; else goto _L1
_L1:
        int j2 = k + i1 * i;
        if(af[j2] < 1.0F)
            af[j2] = 1.0F;
_L4:
        return;
_L2:
        double d = Math.pow((float)j - f, 2D);
        double d1 = Math.pow((float)l - f1, 2D);
        double d2 = Math.pow((float)k - f, 2D);
        double d3 = Math.pow((float)i1 - f1, 2D);
        float f2 = (float)Math.sqrt(d + d1);
        float f3 = (float)Math.sqrt(d2 + d1);
        float f4 = (float)Math.sqrt(d + d3);
        float f5 = (float)Math.sqrt(d2 + d3);
        float f6 = f5 + (f4 + (f2 + f3));
        float f7 = f2 / f6;
        int j1 = j + l * i;
        if(f7 > af[j1])
            af[j1] = f7;
        float f8 = f3 / f6;
        int k1 = k + l * i;
        if(f8 > af[k1])
            af[k1] = f8;
        float f9 = f4 / f6;
        int l1 = j + i1 * i;
        if(f9 > af[l1])
            af[l1] = f9;
        float f10 = f5 / f6;
        int i2 = k + i1 * i;
        if(f10 > af[i2])
            af[i2] = f10;
        if(true) goto _L4; else goto _L3
_L3:
    }

    static float[] rotate(float af[], float f) {
        float f1 = (float)Math.cos(f);
        float f2 = (float)Math.sin(f);
        int i = af.length;
        for(int j = 0; j < i; j += 2) {
            float f3 = f1 * af[j] - f2 * af[j + 1];
            float f4 = f2 * af[j] + f1 * af[j + 1];
            af[j] = f3;
            af[j + 1] = f4;
        }

        return af;
    }

    static float[] scale(float af[], float f, float f1) {
        int i = af.length;
        for(int j = 0; j < i; j += 2) {
            af[j] = f * af[j];
            int k = j + 1;
            af[k] = f1 * af[k];
        }

        return af;
    }

    public static float[] spatialSampling(Gesture gesture, int i) {
        return spatialSampling(gesture, i, false);
    }

    public static float[] spatialSampling(Gesture gesture, int i, boolean flag) {
        float f;
        float af[];
        RectF rectf;
        float f1;
        float f2;
        float f3;
        float f4;
        f = i - 1;
        af = new float[i * i];
        Arrays.fill(af, 0.0F);
        rectf = gesture.getBoundingBox();
        f1 = rectf.width();
        f2 = rectf.height();
        f3 = f / f1;
        f4 = f / f2;
        if(!flag) goto _L2; else goto _L1
_L1:
        int k;
        float f7;
        float f8;
        float f9;
        float f10;
        ArrayList arraylist;
        int j;
        float af1[];
        int i1;
        float f27;
        if(f3 < f4)
            f27 = f3;
        else
            f27 = f4;
        f3 = f27;
        f4 = f27;
_L6:
        f7 = -rectf.centerX();
        f8 = -rectf.centerY();
        f9 = f / 2.0F;
        f10 = f / 2.0F;
        arraylist = gesture.getStrokes();
        j = arraylist.size();
        k = 0;
_L4:
        if(k >= j)
            break; /* Loop/switch isn't completed */
        af1 = ((GestureStroke)arraylist.get(k)).points;
        int l = af1.length;
        float af2[] = new float[l];
        float f5;
        float f6;
        float f25;
        float f26;
        for(i1 = 0; i1 < l; i1 += 2) {
            af2[i1] = f9 + f3 * (f7 + af1[i1]);
            af2[i1 + 1] = f10 + f4 * (f8 + af1[i1 + 1]);
        }

        float f11 = -1F;
        float f12 = -1F;
        for(int j1 = 0; j1 < l; j1 += 2) {
            float f13;
            float f14;
            if(af2[j1] < 0.0F)
                f13 = 0.0F;
            else
                f13 = af2[j1];
            if(af2[j1 + 1] < 0.0F)
                f14 = 0.0F;
            else
                f14 = af2[j1 + 1];
            if(f13 > f)
                f13 = f;
            if(f14 > f)
                f14 = f;
            plot(f13, f14, af, i);
            if(f11 != -1F) {
                if(f11 > f13) {
                    float f22 = (float)Math.ceil(f13);
                    float f23 = (f12 - f14) / (f11 - f13);
                    for(; f22 < f11; f22++) {
                        float f24 = f14 + f23 * (f22 - f13);
                        plot(f22, f24, af, i);
                    }

                } else
                if(f11 < f13) {
                    float f19 = (float)Math.ceil(f11);
                    float f20 = (f12 - f14) / (f11 - f13);
                    for(; f19 < f13; f19++) {
                        float f21 = f14 + f20 * (f19 - f13);
                        plot(f19, f21, af, i);
                    }

                }
                if(f12 > f14) {
                    float f17 = (float)Math.ceil(f14);
                    float f18 = (f11 - f13) / (f12 - f14);
                    for(; f17 < f12; f17++)
                        plot(f13 + f18 * (f17 - f14), f17, af, i);

                } else
                if(f12 < f14) {
                    float f15 = (float)Math.ceil(f12);
                    float f16 = (f11 - f13) / (f12 - f14);
                    for(; f15 < f14; f15++)
                        plot(f13 + f16 * (f15 - f14), f15, af, i);

                }
            }
            f11 = f13;
            f12 = f14;
        }

        k++;
        continue; /* Loop/switch isn't completed */
_L2:
        f5 = f1 / f2;
        if(f5 > 1.0F)
            f5 = 1.0F / f5;
        if(f5 < 0.26F) {
            if(f3 < f4)
                f26 = f3;
            else
                f26 = f4;
            f3 = f26;
            f4 = f26;
        } else
        if(f3 > f4) {
            f25 = f4 * NONUNIFORM_SCALE;
            if(f25 < f3)
                f3 = f25;
        } else {
            f6 = f3 * NONUNIFORM_SCALE;
            if(f6 < f4)
                f4 = f6;
        }
        continue; /* Loop/switch isn't completed */
        if(true) goto _L4; else goto _L3
_L3:
        return af;
        if(true) goto _L6; else goto _L5
_L5:
    }

    static float squaredEuclideanDistance(float af[], float af1[]) {
        float f = 0.0F;
        int i = af.length;
        for(int j = 0; j < i; j++) {
            float f1 = af[j] - af1[j];
            f += f1 * f1;
        }

        return f / (float)i;
    }

    public static float[] temporalSampling(GestureStroke gesturestroke, int i) {
        float f = gesturestroke.length / (float)(i - 1);
        int j = i * 2;
        float af[] = new float[j];
        float f1 = 0.0F;
        float af1[] = gesturestroke.points;
        float f2 = af1[0];
        float f3 = af1[1];
        float f4 = 1.401298E-45F;
        float f5 = 1.401298E-45F;
        af[0] = f2;
        int k = 0 + 1;
        af[k] = f3;
        int l = k + 1;
        int i1 = 0;
        int j1 = af1.length / 2;
label0:
        do {
label1:
            {
label2:
                {
                    if(i1 < j1) {
                        if(f4 != 1.401298E-45F)
                            break label1;
                        if(++i1 < j1)
                            break label2;
                    }
                    for(int k1 = l; k1 < j; k1 += 2) {
                        af[k1] = f2;
                        af[k1 + 1] = f3;
                    }

                    break label0;
                }
                f4 = af1[i1 * 2];
                f5 = af1[1 + i1 * 2];
            }
            float f6 = f4 - f2;
            float f7 = f5 - f3;
            float f8 = (float)Math.sqrt(f6 * f6 + f7 * f7);
            if(f1 + f8 >= f) {
                float f9 = (f - f1) / f8;
                float f10 = f2 + f9 * f6;
                float f11 = f3 + f9 * f7;
                af[l] = f10;
                int l1 = l + 1;
                af[l1] = f11;
                l = l1 + 1;
                f2 = f10;
                f3 = f11;
                f1 = 0.0F;
            } else {
                f2 = f4;
                f3 = f5;
                f4 = 1.401298E-45F;
                f5 = 1.401298E-45F;
                f1 += f8;
            }
        } while(true);
        return af;
    }

    static float[] translate(float af[], float f, float f1) {
        int i = af.length;
        for(int j = 0; j < i; j += 2) {
            af[j] = f + af[j];
            int k = j + 1;
            af[k] = f1 + af[k];
        }

        return af;
    }

    private static final float NONUNIFORM_SCALE = 0F;
    private static final float SCALING_THRESHOLD = 0.26F;

    static  {
        NONUNIFORM_SCALE = (float)Math.sqrt(2D);
    }
}

