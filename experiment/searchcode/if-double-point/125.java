<<<<<<< HEAD
/*
 * Copyright (C) 2012 CyberAgent
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.co.cyberagent.android.gpuimage;

import android.graphics.Point;
import android.graphics.PointF;
import android.opengl.GLES20;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class GPUImageToneCurveFilter extends GPUImageFilter {
    public static final String TONE_CURVE_FRAGMENT_SHADER = "" +
            " varying highp vec2 textureCoordinate;\n" +
            " uniform sampler2D inputImageTexture;\n" +
            " uniform sampler2D toneCurveTexture;\n" +
            "\n" +
            " void main()\n" +
            " {\n" +
            "     lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "     lowp float redCurveValue = texture2D(toneCurveTexture, vec2(textureColor.r, 0.0)).r;\n" +
            "     lowp float greenCurveValue = texture2D(toneCurveTexture, vec2(textureColor.g, 0.0)).g;\n" +
            "     lowp float blueCurveValue = texture2D(toneCurveTexture, vec2(textureColor.b, 0.0)).b;\n" +
            "\n" +
            "     gl_FragColor = vec4(redCurveValue, greenCurveValue, blueCurveValue, textureColor.a);\n" +
            " }";

    private int[] mToneCurveTexture = new int[]{OpenGlUtils.NO_TEXTURE};
    private int mToneCurveTextureUniformLocation;

    private PointF[] mRgbCompositeControlPoints;
    private PointF[] mRedControlPoints;
    private PointF[] mGreenControlPoints;
    private PointF[] mBlueControlPoints;

    private ArrayList<Float> mRgbCompositeCurve;
    private ArrayList<Float> mRedCurve;
    private ArrayList<Float> mGreenCurve;
    private ArrayList<Float> mBlueCurve;


    public GPUImageToneCurveFilter() {
        super(NO_FILTER_VERTEX_SHADER, TONE_CURVE_FRAGMENT_SHADER);

        PointF[] defaultCurvePoints = new PointF[]{new PointF(0.0f, 0.0f), new PointF(0.5f, 0.5f), new PointF(1.0f, 1.0f)};
        mRgbCompositeControlPoints = defaultCurvePoints;
        mRedControlPoints = defaultCurvePoints;
        mGreenControlPoints = defaultCurvePoints;
        mBlueControlPoints = defaultCurvePoints;
    }

    @Override
    public void onInit() {
        super.onInit();
        mToneCurveTextureUniformLocation = GLES20.glGetUniformLocation(getProgram(), "toneCurveTexture");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
        GLES20.glGenTextures(1, mToneCurveTexture, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mToneCurveTexture[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    }

    @Override
    public void onInitialized() {
        super.onInitialized();
        setRgbCompositeControlPoints(mRgbCompositeControlPoints);
        setRedControlPoints(mRedControlPoints);
        setGreenControlPoints(mGreenControlPoints);
        setBlueControlPoints(mBlueControlPoints);
    }

    @Override
    protected void onDrawArraysPre() {
        if (mToneCurveTexture[0] != OpenGlUtils.NO_TEXTURE) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mToneCurveTexture[0]);
            GLES20.glUniform1i(mToneCurveTextureUniformLocation, 3);
        }
    }

    public void setFromCurveFileInputStream(InputStream input) {
        try {
            int version = readShort(input);
            int totalCurves = readShort(input);

            ArrayList<PointF[]> curves = new ArrayList<PointF[]>(totalCurves);
            float pointRate = 1.0f / 255;

            for (int i = 0; i < totalCurves; i++) {
                // 2 bytes, Count of points in the curve (short integer from 2...19)
                short pointCount = readShort(input);

                PointF[] points = new PointF[pointCount];

                // point count * 4
                // Curve points. Each curve point is a pair of short integers where
                // the first number is the output value (vertical coordinate on the
                // Curves dialog graph) and the second is the input value. All coordinates have range 0 to 255.
                for (int j = 0; j < pointCount; j++) {
                    short y = readShort(input);
                    short x = readShort(input);

                    points[j] = new PointF(x * pointRate, y * pointRate);
                }

                curves.add(points);
            }
            input.close();

            mRgbCompositeControlPoints = curves.get(0);
            mRedControlPoints = curves.get(1);
            mGreenControlPoints = curves.get(2);
            mBlueControlPoints = curves.get(3);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private short readShort(InputStream input) throws IOException {
        return (short) (input.read() << 8 | input.read());
    }

    public void setRgbCompositeControlPoints(PointF[] points) {
        mRgbCompositeControlPoints = points;
        mRgbCompositeCurve = createSplineCurve(mRgbCompositeControlPoints);
        updateToneCurveTexture();
    }

    public void setRedControlPoints(PointF[] points) {
        mRedControlPoints = points;
        mRedCurve = createSplineCurve(mRedControlPoints);
        updateToneCurveTexture();
    }

    public void setGreenControlPoints(PointF[] points) {
        mGreenControlPoints = points;
        mGreenCurve = createSplineCurve(mGreenControlPoints);
        updateToneCurveTexture();
    }

    public void setBlueControlPoints(PointF[] points) {
        mBlueControlPoints = points;
        mBlueCurve = createSplineCurve(mBlueControlPoints);
        updateToneCurveTexture();
    }

    private void updateToneCurveTexture() {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mToneCurveTexture[0]);

                if ((mRedCurve.size() >= 256) && (mGreenCurve.size() >= 256) && (mBlueCurve.size() >= 256) && (mRgbCompositeCurve.size() >= 256)) {
                    byte[] toneCurveByteArray = new byte[256 * 4];
                    for (int currentCurveIndex = 0; currentCurveIndex < 256; currentCurveIndex++) {
                        // BGRA for upload to texture
                        toneCurveByteArray[currentCurveIndex * 4 + 2] = (byte) ((int) Math.min(Math.max(currentCurveIndex + mBlueCurve.get(currentCurveIndex) + mRgbCompositeCurve.get(currentCurveIndex), 0), 255) & 0xff);
                        toneCurveByteArray[currentCurveIndex * 4 + 1] = (byte) ((int) Math.min(Math.max(currentCurveIndex + mGreenCurve.get(currentCurveIndex) + mRgbCompositeCurve.get(currentCurveIndex), 0), 255) & 0xff);
                        toneCurveByteArray[currentCurveIndex * 4] = (byte) ((int) Math.min(Math.max(currentCurveIndex + mRedCurve.get(currentCurveIndex) + mRgbCompositeCurve.get(currentCurveIndex), 0), 255) & 0xff);
                        toneCurveByteArray[currentCurveIndex * 4 + 3] = (byte) (255 & 0xff);
                    }

                    GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 256 /*width*/, 1 /*height*/, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, ByteBuffer.wrap(toneCurveByteArray));
                }
//        Buffer pixels!
//        GLES20.glTexImage2D(int target,
//            int level,
//            int internalformat,
//            int width,
//            int height,
//            int border,
//            int format,
//            int type,
//            java.nio.Buffer pixels);
            }
        });
    }

    private ArrayList<Float> createSplineCurve(PointF[] points) {
        if (points == null || points.length <= 0) {
            return null;
        }

        // Sort the array
        PointF[] pointsSorted = points.clone();
        Arrays.sort(pointsSorted, new Comparator<PointF>() {
            @Override
            public int compare(PointF point1, PointF point2) {
                if (point1.x < point2.x) {
                    return -1;
                } else if (point1.x > point2.x) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        // Convert from (0, 1) to (0, 255).
        Point[] convertedPoints = new Point[pointsSorted.length];
        for (int i = 0; i < points.length; i++) {
            PointF point = pointsSorted[i];
            convertedPoints[i] = new Point((int) (point.x * 255), (int) (point.y * 255));
        }

        ArrayList<Point> splinePoints = createSplineCurve2(convertedPoints);

        // If we have a first point like (0.3, 0) we'll be missing some points at the beginning
        // that should be 0.
        Point firstSplinePoint = splinePoints.get(0);
        if (firstSplinePoint.x > 0) {
            for (int i = firstSplinePoint.x; i >= 0; i--) {
                splinePoints.add(0, new Point(i, 0));
            }
        }

        // Insert points similarly at the end, if necessary.
        Point lastSplinePoint = splinePoints.get(splinePoints.size() - 1);
        if (lastSplinePoint.x < 255) {
            for (int i = lastSplinePoint.x + 1; i <= 255; i++) {
                splinePoints.add(new Point(i, 255));
            }
        }

        // Prepare the spline points.
        ArrayList<Float> preparedSplinePoints = new ArrayList<Float>(splinePoints.size());
        for (Point newPoint : splinePoints) {
            Point origPoint = new Point(newPoint.x, newPoint.x);

            float distance = (float) Math.sqrt(Math.pow((origPoint.x - newPoint.x), 2.0) + Math.pow((origPoint.y - newPoint.y), 2.0));

            if (origPoint.y > newPoint.y) {
                distance = -distance;
            }

            preparedSplinePoints.add(distance);
        }

        return preparedSplinePoints;
    }

    private ArrayList<Point> createSplineCurve2(Point[] points) {
        ArrayList<Double> sdA = createSecondDerivative(points);

        // Is [points count] equal to [sdA count]?
//    int n = [points count];
        int n = sdA.size();
        if (n < 1) {
            return null;
        }
        double sd[] = new double[n];

        // From NSMutableArray to sd[n];
        for (int i = 0; i < n; i++) {
            sd[i] = sdA.get(i);
        }


        ArrayList<Point> output = new ArrayList<Point>(n + 1);

        for (int i = 0; i < n - 1; i++) {
            Point cur = points[i];
            Point next = points[i + 1];

            for (int x = cur.x; x < next.x; x++) {
                double t = (double) (x - cur.x) / (next.x - cur.x);

                double a = 1 - t;
                double b = t;
                double h = next.x - cur.x;

                double y = a * cur.y + b * next.y + (h * h / 6) * ((a * a * a - a) * sd[i] + (b * b * b - b) * sd[i + 1]);

                if (y > 255.0) {
                    y = 255.0;
                } else if (y < 0.0) {
                    y = 0.0;
                }

                output.add(new Point(x, (int) Math.round(y)));
            }
        }

        // If the last point is (255, 255) it doesn't get added.
        if (output.size() == 255) {
            output.add(points[points.length - 1]);
        }
        return output;
    }

    private ArrayList<Double> createSecondDerivative(Point[] points) {
        int n = points.length;
        if (n <= 1) {
            return null;
        }

        double matrix[][] = new double[n][3];
        double result[] = new double[n];
        matrix[0][1] = 1;
        // What about matrix[0][1] and matrix[0][0]? Assuming 0 for now (Brad L.)
        matrix[0][0] = 0;
        matrix[0][2] = 0;

        for (int i = 1; i < n - 1; i++) {
            Point P1 = points[i - 1];
            Point P2 = points[i];
            Point P3 = points[i + 1];

            matrix[i][0] = (double) (P2.x - P1.x) / 6;
            matrix[i][1] = (double) (P3.x - P1.x) / 3;
            matrix[i][2] = (double) (P3.x - P2.x) / 6;
            result[i] = (double) (P3.y - P2.y) / (P3.x - P2.x) - (double) (P2.y - P1.y) / (P2.x - P1.x);
        }

        // What about result[0] and result[n-1]? Assuming 0 for now (Brad L.)
        result[0] = 0;
        result[n - 1] = 0;

        matrix[n - 1][1] = 1;
        // What about matrix[n-1][0] and matrix[n-1][2]? For now, assuming they are 0 (Brad L.)
        matrix[n - 1][0] = 0;
        matrix[n - 1][2] = 0;

        // solving pass1 (up->down)
        for (int i = 1; i < n; i++) {
            double k = matrix[i][0] / matrix[i - 1][1];
            matrix[i][1] -= k * matrix[i - 1][2];
            matrix[i][0] = 0;
            result[i] -= k * result[i - 1];
        }
        // solving pass2 (down->up)
        for (int i = n - 2; i >= 0; i--) {
            double k = matrix[i][2] / matrix[i + 1][1];
            matrix[i][1] -= k * matrix[i + 1][0];
            matrix[i][2] = 0;
            result[i] -= k * result[i + 1];
        }

        ArrayList<Double> output = new ArrayList<Double>(n);
        for (int i = 0; i < n; i++) output.add(result[i] / matrix[i][1]);

        return output;
    }
=======
package cgeo.geocaching.geopoint;

import cgeo.geocaching.ICoordinates;

import java.util.Locale;
import java.util.Set;



public class Viewport {

    public final Geopoint center;
    public final Geopoint bottomLeft;
    public final Geopoint topRight;

    public Viewport(final ICoordinates point1, final ICoordinates point2) {
        final Geopoint gp1 = point1.getCoords();
        final Geopoint gp2 = point2.getCoords();
        this.bottomLeft = new Geopoint(Math.min(gp1.getLatitude(), gp2.getLatitude()),
                Math.min(gp1.getLongitude(), gp2.getLongitude()));
        this.topRight = new Geopoint(Math.max(gp1.getLatitude(), gp2.getLatitude()),
                Math.max(gp1.getLongitude(), gp2.getLongitude()));
        this.center = new Geopoint((gp1.getLatitude() + gp2.getLatitude()) / 2,
                (gp1.getLongitude() + gp2.getLongitude()) / 2);
    }

    public Viewport(final ICoordinates center, final double latSpan, final double lonSpan) {
        this.center = center.getCoords();
        final double centerLat = this.center.getLatitude();
        final double centerLon = this.center.getLongitude();
        final double latHalfSpan = Math.abs(latSpan) / 2;
        final double lonHalfSpan = Math.abs(lonSpan) / 2;
        bottomLeft = new Geopoint(centerLat - latHalfSpan, centerLon - lonHalfSpan);
        topRight = new Geopoint(centerLat + latHalfSpan, centerLon + lonHalfSpan);
    }

    public double getLatitudeMin() {
        return bottomLeft.getLatitude();
    }

    public double getLatitudeMax() {
        return topRight.getLatitude();
    }

    public double getLongitudeMin() {
        return bottomLeft.getLongitude();
    }

    public double getLongitudeMax() {
        return topRight.getLongitude();
    }

    public Geopoint getCenter() {
        return center;
    }

    public double getLatitudeSpan() {
        return getLatitudeMax() - getLatitudeMin();
    }

    public double getLongitudeSpan() {
        return getLongitudeMax() - getLongitudeMin();
    }

    /**
     * Check whether a point is contained in this viewport.
     *
     * @param point
     *            the coordinates to check
     * @return true if the point is contained in this viewport, false otherwise or if the point contains no coordinates
     */
    public boolean contains(final ICoordinates point) {
        final Geopoint coords = point.getCoords();
        return coords != null
                && coords.getLongitudeE6() >= bottomLeft.getLongitudeE6()
                && coords.getLongitudeE6() <= topRight.getLongitudeE6()
                && coords.getLatitudeE6() >= bottomLeft.getLatitudeE6()
                && coords.getLatitudeE6() <= topRight.getLatitudeE6();
    }

    @Override
    public String toString() {
        return "(" + bottomLeft.toString() + "," + topRight.toString() + ")";
    }

    /**
     * Check whether another viewport is fully included into the current one.
     *
     * @param vp
     *            the other viewport
     * @return true if the vp is fully included into this one, false otherwise
     */
    public boolean includes(final Viewport vp) {
        return contains(vp.bottomLeft) && contains(vp.topRight);
    }

    /**
     * Return the "where" part of the string appropriate for a SQL query.
     *
     * @param dbTable
     *            the database table to use as prefix, or null if no prefix is required
     * @return the string without the "where" keyword
     */
    public String sqlWhere(final String dbTable) {
        final String prefix = dbTable == null ? "" : (dbTable + ".");
        return String.format((Locale) null,
                "%slatitude >= %s and %slatitude <= %s and %slongitude >= %s and %slongitude <= %s",
                prefix, getLatitudeMin(), prefix, getLatitudeMax(), prefix, getLongitudeMin(), prefix, getLongitudeMax());
    }

    /**
     * Return a widened or shrunk viewport.
     *
     * @param factor
     *            multiplicative factor for the latitude and longitude span (> 1 to widen, < 1 to shrink)
     * @return a widened or shrunk viewport
     */
    public Viewport resize(final double factor) {
        return new Viewport(getCenter(), getLatitudeSpan() * factor, getLongitudeSpan() * factor);
    }

    /**
     * Return a viewport that contains the current viewport as well as another point.
     *
     * @param point
     *            the point we want in the viewport
     * @return either the same or an expanded viewport
     */
    public Viewport expand(final ICoordinates point) {
        if (contains(point)) {
            return this;
        }

        final Geopoint coords = point.getCoords();
        final double latitude = coords.getLatitude();
        final double longitude = coords.getLongitude();
        final double latMin = Math.min(getLatitudeMin(), latitude);
        final double latMax = Math.max(getLatitudeMax(), latitude);
        final double lonMin = Math.min(getLongitudeMin(), longitude);
        final double lonMax = Math.max(getLongitudeMax(), longitude);
        return new Viewport(new Geopoint(latMin, lonMin), new Geopoint(latMax, lonMax));
    }

    /**
     * Return the smallest viewport containing all the given points.
     *
     * @param points
     *            a set of points. Point with null coordinates (or null themselves) will be ignored
     * @return the smallest viewport containing the non-null coordinates, or null if no coordinates are non-null
     */
    static public Viewport containing(final Set<? extends ICoordinates> points) {
        Viewport viewport = null;
        for (final ICoordinates point : points) {
            if (point != null && point.getCoords() != null) {
                if (viewport == null) {
                    viewport = new Viewport(point, point);
                } else {
                    viewport = viewport.expand(point);
                }
            }
        }
        return viewport;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Viewport)) {
            return false;
        }
        final Viewport vp = (Viewport) other;
        return bottomLeft.equals(vp.bottomLeft) && topRight.equals(vp.topRight);
    }

    @Override
    public int hashCode() {
        return bottomLeft.hashCode() ^ topRight.hashCode();
    }

>>>>>>> 76aa07461566a5976980e6696204781271955163
}

