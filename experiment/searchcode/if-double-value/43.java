/*
<<<<<<< HEAD
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Christian Schudt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.xmpp.extension.rpc;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

/**
 * The value type, which is used by XML-RPC.
 *
 * @author Christian Schudt
 */
@XmlRootElement(name = "value")
public final class Value {

    @XmlElements(value = {
            @XmlElement(name = "i4", type = Integer.class),
            @XmlElement(name = "int", type = Integer.class),
            @XmlElement(name = "string", type = String.class),
            @XmlElement(name = "double", type = Double.class),
            @XmlElement(name = "base64", type = byte[].class),
            @XmlElement(name = "boolean", type = NumericBoolean.class),
            @XmlElement(name = "dateTime.iso8601", type = Date.class),
            @XmlElement(name = "array", type = ArrayType.class),
            @XmlElement(name = "struct", type = StructType.class)
    })
    private Object value;

    private Value() {
    }

    /**
     * Creates an integer value.
     *
     * @param integer The integer value.
     */
    public Value(Integer integer) {
        this.value = integer;
    }

    /**
     * Creates a string value.
     *
     * @param string The string value.
     */
    public Value(String string) {
        this.value = string;
    }

    /**
     * Creates a double value.
     *
     * @param d The double value.
     */
    public Value(Double d) {
        this.value = d;
    }

    /**
     * Creates a binary (base64) value.
     *
     * @param bytes The binary value.
     */
    public Value(byte[] bytes) {
        this.value = bytes;
    }

    /**
     * Creates a boolean value.
     *
     * @param b The boolean value.
     */
    public Value(Boolean b) {
        this.value = new NumericBoolean(b);
    }

    /**
     * Creates a date value.
     *
     * @param date The date value.
     */
    public Value(Date date) {
        this.value = date;
    }

    /**
     * Creates an array type value.
     *
     * @param list The array type value.
     */
    public Value(List<Value> list) {
        if (list != null) {
            ArrayType arrayType = new ArrayType();
            for (Value value : list) {
                arrayType.values.add(value);
            }
            this.value = arrayType;
        }
    }

    /**
     * Creates a struct type value.
     *
     * @param map The struct type value.
     */
    public Value(Map<String, Value> map) {
        if (map != null) {
            StructType structType = new StructType();
            for (Map.Entry<String, Value> entry : map.entrySet()) {
                structType.values.add(new StructType.MemberType(entry.getKey(), entry.getValue()));
            }
            this.value = structType;
        }
    }

    /**
     * Gets the value as integer or null.
     *
     * @return The integer or null.
     */
    public Integer getAsInteger() {
        return value instanceof Integer ? (Integer) value : null;
    }

    /**
     * Gets the value as double or null.
     *
     * @return The double or null.
     */
    public Double getAsDouble() {
        return value instanceof Double ? (Double) value : null;
    }

    /**
     * Gets the value as string or null.
     *
     * @return The string or null.
     */
    public String getAsString() {
        return value instanceof String ? (String) value : null;
    }

    /**
     * Gets the value as byte array or null.
     *
     * @return The byte array or null.
     */
    public byte[] getAsByteArray() {
        return value instanceof byte[] ? (byte[]) value : null;
    }

    /**
     * Gets the value as boolean or null.
     *
     * @return The boolean or null.
     */
    public Boolean getAsBoolean() {
        return value instanceof NumericBoolean ? ((NumericBoolean) value).getAsBoolean() : null;
    }

    /**
     * Gets the value as date or null.
     *
     * @return The date or null.
     */
    public Date getAsDate() {
        return value instanceof Date ? (Date) value : null;
    }

    /**
     * Gets the value as array or null.
     *
     * @return The array or null.
     */
    public List<Value> getAsArray() {
        if (value instanceof ArrayType) {
            ArrayType arrayType = (ArrayType) value;
            List<Value> result = new ArrayList<>();
            if (arrayType.values != null) {
                for (Value value : arrayType.values) {
                    result.add(value);
                }
            }
            return result;
        }
        return null;
    }

    /**
     * Gets the value as map or null.
     *
     * @return The map or null.
     */
    public Map<String, Value> getAsMap() {
        if (value instanceof StructType) {
            StructType structType = (StructType) value;
            Map<String, Value> result = new HashMap<>();
            if (structType.values != null) {
                for (StructType.MemberType member : structType.values) {
                    result.put(member.name, member.value);
                }
            }
            return result;
        }
        return null;
    }
}
=======
 * Copyright (C) 2011 Samuel Audet
 *
 * This file is part of JavaCV.
 *
 * JavaCV is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version (subject to the "Classpath" exception
 * as provided in the LICENSE.txt file that accompanied this code).
 *
 * JavaCV is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JavaCV.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * This file is based on information found in contrib.hpp of OpenCV 2.3.1,
 * which is covered by the following copyright notice:
 *
 *                           License Agreement
 *                For Open Source Computer Vision Library
 *
 * Copyright (C) 2000-2008, Intel Corporation, all rights reserved.
 * Copyright (C) 2009, Willow Garage Inc., all rights reserved.
 * Third party copyrights are property of their respective owners.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * Redistribution's of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *   * Redistribution's in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *   * The name of the copyright holders may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * This software is provided by the copyright holders and contributors "as is" and
 * any express or implied warranties, including, but not limited to, the implied
 * warranties of merchantability and fitness for a particular purpose are disclaimed.
 * In no event shall the Intel Corporation or contributors be liable for any direct,
 * indirect, incidental, special, exemplary, or consequential damages
 * (including, but not limited to, procurement of substitute goods or services;
 * loss of use, data, or profits; or business interruption) however caused
 * and on any theory of liability, whether in contract, strict liability,
 * or tort (including negligence or otherwise) arising in any way out of
 * the use of this software, even if advised of the possibility of such damage.
 *
 */

package com.googlecode.javacv.cpp;

import com.googlecode.javacpp.FloatPointer;
import com.googlecode.javacpp.FunctionPointer;
import com.googlecode.javacpp.Pointer;
import com.googlecode.javacpp.annotation.Adapter;
import com.googlecode.javacpp.annotation.ByRef;
import com.googlecode.javacpp.annotation.ByVal;
import com.googlecode.javacpp.annotation.Cast;
import com.googlecode.javacpp.annotation.Index;
import com.googlecode.javacpp.annotation.MemberGetter;
import com.googlecode.javacpp.annotation.Name;
import com.googlecode.javacpp.annotation.Namespace;
import com.googlecode.javacpp.annotation.NoOffset;
import com.googlecode.javacpp.annotation.Opaque;
import com.googlecode.javacpp.annotation.Platform;
import com.googlecode.javacpp.annotation.Properties;
import com.googlecode.javacpp.annotation.ValueGetter;

import static com.googlecode.javacpp.Loader.*;
import static com.googlecode.javacv.cpp.opencv_core.*;

/**
 *
 * @author Samuel Audet
 */
@Properties({
    @Platform(includepath=genericIncludepath, linkpath=genericLinkpath,
        include={"<opencv2/contrib/contrib.hpp>", "opencv_adapters.h"},
        link={"opencv_contrib", "opencv_features2d", "opencv_flann", "opencv_calib3d", "opencv_highgui", "opencv_imgproc", "opencv_core"}),
    @Platform(value="windows", includepath=windowsIncludepath,
        link={"opencv_contrib231","opencv_features2d231", "opencv_flann231", "opencv_calib3d231", "opencv_highgui231", "opencv_imgproc231", "opencv_core231"}),
    @Platform(value="windows-x86",    linkpath=windowsx86Linkpath, preloadpath=windowsx86Preloadpath),
    @Platform(value="windows-x86_64", linkpath=windowsx64Linkpath, preloadpath=windowsx64Preloadpath),
    @Platform(value="android", includepath=androidIncludepath, linkpath=androidLinkpath) })
public class opencv_contrib {
    static { load(opencv_features2d.class); load(); }

    public static class CvAdaptiveSkinDetector extends Pointer {
        static { load(); }
        public CvAdaptiveSkinDetector() { allocate(); }
        public CvAdaptiveSkinDetector(int samplingDivider/*=1*/, int morphingMethod/*=MORPHING_METHOD_NONE*/) {
            allocate(samplingDivider, morphingMethod);
        }
        public CvAdaptiveSkinDetector(Pointer p) { super(p); }
        private native void allocate();
        private native void allocate(int samplingDivider/*=1*/, int morphingMethod/*=MORPHING_METHOD_NONE*/);

        public static final int
                MORPHING_METHOD_NONE = 0,
                MORPHING_METHOD_ERODE = 1,
                MORPHING_METHOD_ERODE_ERODE = 2,
                MORPHING_METHOD_ERODE_DILATE = 3;

        public native void process(IplImage inputBGRImage, IplImage outputHueMask);

//        protected native void initData(IplImage src, int widthDivider, int heightDivider);
//        protected native void adaptiveFilter();
    }


    @NoOffset public static class CvFuzzyPoint extends Pointer {
        static { load(); }
        public CvFuzzyPoint() { }
        public CvFuzzyPoint(double _x, double _y) { allocate(_x, _y); }
        public CvFuzzyPoint(Pointer p) { super(p); }
        private native void allocate(double _x, double _y);

        public native double x();     public native CvFuzzyPoint x(double x);
        public native double y();     public native CvFuzzyPoint y(double y);
        public native double value(); public native CvFuzzyPoint value(double value);
    }

    public static class CvFuzzyCurve extends Pointer {
        static { load(); }
        public CvFuzzyCurve() { allocate(); }
        public CvFuzzyCurve(int size) { allocateArray(size); }
        public CvFuzzyCurve(Pointer p) { super(p); }
        private native void allocate();
        private native void allocateArray(int size);

        @Override public CvFuzzyCurve position(int position) {
            return (CvFuzzyCurve)super.position(position);
        }

        public native void setCentre(double _centre);
        public native double getCentre();
        public native void clear();
        public native void addPoint(double x, double y);
        public native double calcValue(double param);
        public native double getValue();
        public native void setValue(double _value);
    }

    public static class CvFuzzyFunction extends Pointer {
        static { load(); }
        public CvFuzzyFunction() { allocate(); }
        public CvFuzzyFunction(int size) { allocateArray(size); }
        public CvFuzzyFunction(Pointer p) { super(p); }
        private native void allocate();
        private native void allocateArray(int size);

        @Override public CvFuzzyFunction position(int position) {
            return (CvFuzzyFunction)super.position(position);
        }

        public native void addCurve(CvFuzzyCurve curve, double value/*=0*/);
        public native void resetValues();
        public native double calcValue();
        public native CvFuzzyCurve newCurve();

        @NoOffset @Adapter("VectorAdapter<CvFuzzyCurve>")
        public native CvFuzzyCurve curves(); public native CvFuzzyFunction curves(CvFuzzyCurve curves);
    }

    public static class CvFuzzyRule extends Pointer {
        static { load(); }
        public CvFuzzyRule() { allocate(); }
        public CvFuzzyRule(int size) { allocateArray(size); }
        public CvFuzzyRule(Pointer p) { super(p); }
        private native void allocate();
        private native void allocateArray(int size);

        @Override public CvFuzzyRule position(int position) {
            return (CvFuzzyRule)super.position(position);
        }

        public native void setRule(CvFuzzyCurve c1, CvFuzzyCurve c2, CvFuzzyCurve o1);
        public native double calcValue(double param1, double param2);
        public native CvFuzzyCurve getOutputCurve();
    }

    public static class CvFuzzyController extends Pointer {
        static { load(); }
        public CvFuzzyController() { allocate(); }
        public CvFuzzyController(int size) { allocateArray(size); }
        public CvFuzzyController(Pointer p) { super(p); }
        private native void allocate();
        private native void allocateArray(int size);

        @Override public CvFuzzyController position(int position) {
            return (CvFuzzyController)super.position(position);
        }

        public native void addRule(CvFuzzyCurve c1, CvFuzzyCurve c2, CvFuzzyCurve o1);
        public native double calcOutput(double param1, double param2);
    }

    @NoOffset public static class CvFuzzyMeanShiftTracker extends Pointer {
        static { load(); }
        public CvFuzzyMeanShiftTracker() { allocate(); }
        public CvFuzzyMeanShiftTracker(int size) { allocateArray(size); }
        public CvFuzzyMeanShiftTracker(Pointer p) { super(p); }
        private native void allocate();
        private native void allocateArray(int size);

        @Override public CvFuzzyMeanShiftTracker position(int position) {
            return (CvFuzzyMeanShiftTracker)super.position(position);
        }

        public static final int 
        //enum TrackingState
                tsNone          = 0,
                tsSearching     = 1,
                tsTracking      = 2,
                tsSetWindow     = 3,
                tsDisabled      = 10,

        //enum ResizeMethod {
                rmEdgeDensityLinear     = 0,
                rmEdgeDensityFuzzy      = 1,
                rmInnerDensity          = 2,

                MinKernelMass           = 1000;

        //@Cast("SearchWindow")
        @MemberGetter public native @ByRef Pointer kernel(); // public native CvFuzzyMeanShiftTracker kernel(Pointer kernel);
        public native int searchMode(); public native CvFuzzyMeanShiftTracker searchMode(int searchMode);

        public native void track(IplImage maskImage, IplImage depthMap, int resizeMethod,
                @Cast("bool") boolean resetSearch, int minKernelMass/*=MinKernelMass*/);
    }


    @Namespace("cv") public static class Octree extends Pointer {
        static { load(); }
        @NoOffset public static class Node extends Pointer {
            static { load(); }
            public Node() { allocate(); }
            public Node(int size) { allocateArray(size); }
            public Node(Pointer p) { super(p); }
            private native void allocate();
            private native void allocateArray(int size);

            @Override public Node position(int position) {
                return (Node)super.position(position);
            }

            public native int begin();                public native Node begin(int begin);
            public native int end();                  public native Node end(int end);
            public native float x_min();              public native Node x_min(float x_min);
            public native float x_max();              public native Node x_max(float x_max);
            public native float y_min();              public native Node y_min(float y_min);
            public native float y_max();              public native Node y_max(float y_max);
            public native float z_min();              public native Node z_min(float z_min);
            public native float z_max();              public native Node z_max(float z_max);
            public native int maxLevels();            public native Node maxLevels(int maxLevels);
            @Cast("bool")
            public native boolean isLeaf();           public native Node isLeaf(boolean isLeaf);
            public native int/*[8]*/ children(int i); public native Node children(int i, int children);
        }
        public Octree() { allocate(); }
        public Octree(CvPoint3D32f points, int maxLevels/*=10*/, int minPoints/*=20*/) {
            allocate(points, maxLevels, minPoints);
        }
        public Octree(Pointer p) { super(p); }
        private native void allocate();
        private native void allocate(@Adapter("VectorAdapter<CvPoint3D32f,cv::Point3f>")
                CvPoint3D32f points, int maxLevels/*=10*/, int minPoints/*=20*/);

        public native void buildTree(@Adapter("VectorAdapter<CvPoint3D32f,cv::Point3f>")
                CvPoint3D32f points, int maxLevels/*=10*/, int minPoints/*=20*/);
        public native void getPointsWithinSphere(@ByVal CvPoint3D32f center, float radius,
                @Adapter(value="VectorAdapter<CvPoint3D32f,cv::Point3f>", out=true) CvPoint3D32f points);
        public native @Adapter("VectorAdapter<cv::Octree::Node>") Node getNodes();
    }

    @NoOffset @Namespace("cv") public static class Mesh3D extends Pointer {
        static { load(); }
        public Mesh3D() { allocate(); }
        public Mesh3D(@ByVal CvPoint3D32f vtx) { allocate(vtx); }
        public Mesh3D(Pointer p) { super(p); }
        private native void allocate();
        private native void allocate(@Adapter("VectorAdapter<CvPoint3D32f,cv::Point3f>") CvPoint3D32f vtx);

        @Opaque public static class EmptyMeshException extends Pointer {
            static { load(); }
            public EmptyMeshException() { }
            public EmptyMeshException(Pointer p) { super(p); }
        }

        public native void buildOctree();
        public native void clearOctree();
        public native float estimateResolution(float tryRatio/*=0.1f*/);
        public native void computeNormals(float normalRadius, int minNeighbors/*=20*/);
        public native void computeNormals(@Adapter("VectorAdapter<int>") int[] subset, float normalRadius, int minNeighbors/*=20*/);

        public native void writeAsVrml(String file, @Adapter("VectorAdapter<CvScalar, cv::Scalar>") CvScalar colors/*=null*/);

        @Adapter("VectorAdapter<CvPoint3D32f,cv::Point3f>")
        public native CvPoint3D32f vtx();     public native Mesh3D vtx(CvPoint3D32f vtx);
        @Adapter("VectorAdapter<CvPoint3D32f,cv::Point3f>")
        public native CvPoint3D32f normals(); public native Mesh3D normals(CvPoint3D32f normals);
        public native float resolution();     public native Mesh3D resolution(float resolution);
        public native @ByRef Octree octree(); public native Mesh3D octree(Octree octree);

//        @MemberGetter public static native @ByVal CvPoint3D32f allzero();
    }

    @Name("std::vector<std::vector<cv::Vec2i> >")
    public static class Vec2iVectorVector extends Pointer {
        static { load(); }
        public Vec2iVectorVector()       { allocate();  }
        public Vec2iVectorVector(long n) { allocate(n); }
        public Vec2iVectorVector(Pointer p) { super(p); }
        private native void allocate();
        private native void allocate(@Cast("size_t") long n);

        public native long size();
        public native void resize(@Cast("size_t") long n);
        public native @Index(1) long size(@Cast("size_t") long i);
        public native @Index(1) void resize(@Cast("size_t") long i, @Cast("size_t") long n);

        @Index @ValueGetter @ByVal public native CvScalar get(@Cast("size_t") long i, @Cast("size_t") long j);
        //public native Vec2iVectorVector put(@Cast("size_t") long i, @Cast("size_t") long j, CvScalar value);
    }

    @NoOffset @Namespace("cv") public static class SpinImageModel extends Pointer {
        static { load(); }
        public SpinImageModel() { allocate(); }
        public SpinImageModel(Mesh3D mesh) { allocate(mesh); }
        public SpinImageModel(Pointer p) { super(p); }
        private native void allocate();
        private native void allocate(@ByRef Mesh3D mesh);

        public native float normalRadius();             public native SpinImageModel normalRadius(float normalRadius);
        public native int minNeighbors();               public native SpinImageModel minNeighbors(int minNeighbors);

        public native float binSize();                  public native SpinImageModel binSize(float binSize);
        public native int imageWidth();                 public native SpinImageModel imageWidth(int imageWidth);

        public native float lambda();                   public native SpinImageModel lambda(float lambda);
        public native float gamma();                    public native SpinImageModel gamma(float gamma);

        public native float T_GeometriccConsistency();  public native SpinImageModel T_GeometriccConsistency(float T_GeometriccConsistency);
        public native float T_GroupingCorespondances(); public native SpinImageModel T_GroupingCorespondances(float T_GroupingCorespondances);

        public native void setLogger(@Cast("std::ostream*") Pointer log);
        public native void selectRandomSubset(float ratio);
        public native void setSubset(@Adapter("VectorAdapter<int>") int[] subset);
        public native void compute();

        public native void match(@ByRef SpinImageModel scene, @ByRef Vec2iVectorVector result);

        public native @Adapter("MatAdapter") IplImage packRandomScaledSpins(@Cast("bool") boolean separateScale/*=false*/, @Cast("size_t") long xCount/*=10*/, @Cast("size_t") long yCount/*=10*/);

        public native long getSpinCount();
        public native @Adapter("MatAdapter") IplImage getSpinImage(@Cast("size_t") long index);
        public native @ByVal CvPoint3D32f getSpinVertex(@Cast("size_t") long index);
        public native @ByVal CvPoint3D32f getSpinNormal(@Cast("size_t") long index);

        public native @ByRef Mesh3D getMesh();

        public native static boolean spinCorrelation(IplImage spin1, IplImage spin2, float lambda, @ByRef float[] result);

//        public native static @ByVal CvPoint2D32f calcSpinMapCoo(@ByVal CvPoint3D32f point, @ByVal CvPoint3D32f vertex, @ByVal CvPoint3D32f normal);
//
//        public native static float geometricConsistency(@ByVal CvPoint3D32f pointScene1, @ByVal CvPoint3D32f normalScene1,
//                @ByVal CvPoint3D32f pointModel1, @ByVal CvPoint3D32f normalModel1,
//                @ByVal CvPoint3D32f pointScene2, @ByVal CvPoint3D32f normalScene2,
//                @ByVal CvPoint3D32f pointModel2, @ByVal CvPoint3D32f normalModel2);
//
//        public native static float groupingCreteria(@ByVal CvPoint3D32f pointScene1, @ByVal CvPoint3D32f normalScene1,
//                @ByVal CvPoint3D32f pointModel1, @ByVal CvPoint3D32f normalModel1,
//                @ByVal CvPoint3D32f pointScene2, @ByVal CvPoint3D32f normalScene2,
//                @ByVal CvPoint3D32f pointModel2, @ByVal CvPoint3D32f normalModel2, float gamma);
//
//        protected native void defaultParams();
//
//        protected native void matchSpinToModel(IplImage spin,
//                @Adapter(value="VectorAdapter<int>",out=true) IntPointer indeces,
//                @Adapter(value="VectorAdapter<float>",out=true) FloatPointer corrCoeffs,
//                @Cast("bool") boolean useExtremeOutliers/*=true*/);
//
//        protected native void repackSpinImages(@Adapter(value="VectorAdapter<uchar>",out=true)
//                @Cast("uchar*") BytePointer mask, IplImage spinImages, @Cast("bool") boolean reAlloc/*=true*/);
//
//        protected native vector<int> subset;
//        protected native Mesh3D mesh;
//        protected native Mat spinImages;
//        protected native @Cast("std::ostream*") Pointer out;
    }

    @Namespace("cv") public static class TickMeter extends Pointer {
        static { load(); }
        public TickMeter() { allocate(); }
        public TickMeter(Pointer p) { super(p); }
        private native void allocate();

        public native void start();
        public native void stop();

        public native long getTimeTicks();
        public native double getTimeMicro();
        public native double getTimeMilli();
        public native double getTimeSec();
        public native long getCounter();

        public native void reset();
    }

//    public static native @ByRef std::ostream operator<<(@ByRef std::ostream out, @ByRef TickMeter tm);


    @NoOffset @Namespace("cv") public static class SelfSimDescriptor extends Pointer {
        static { load(); }
        public SelfSimDescriptor() { allocate(); }
        public SelfSimDescriptor(int _ssize, int _lsize,
                int _startDistanceBucket/*=DEFAULT_START_DISTANCE_BUCKET*/,
                int _numberOfDistanceBuckets/*=DEFAULT_NUM_DISTANCE_BUCKETS*/,
                int _nangles/*=DEFAULT_NUM_ANGLES*/) {
            allocate(_ssize, _lsize, _startDistanceBucket, _numberOfDistanceBuckets, _nangles);
        }
        public SelfSimDescriptor(@ByRef SelfSimDescriptor ss) { allocate(ss); }
        public SelfSimDescriptor(Pointer p) { super(p); }
        private native void allocate();
        private native void allocate(int _ssize, int _lsize,
                int _startDistanceBucket/*=DEFAULT_START_DISTANCE_BUCKET*/,
                int _numberOfDistanceBuckets/*=DEFAULT_NUM_DISTANCE_BUCKETS*/,
                int _nangles/*=DEFAULT_NUM_ANGLES*/);
        private native void allocate(@ByRef SelfSimDescriptor ss);

        public native @Name("operator=") @ByRef SelfSimDescriptor copy(@ByRef SelfSimDescriptor ss);

        public native long getDescriptorSize();
        public native @ByVal CvSize getGridSize(@ByVal CvSize imgsize, @ByVal CvSize winStride);

        public native void compute(@Adapter("MatAdapter") CvArr img, @Adapter(value="VectorAdapter<float>", out=true) FloatPointer descriptors,
                @ByVal CvSize winStride/*=Size()*/, @Adapter("VectorAdapter<CvPoint,cv::Point>") CvPoint locations/*=null*/);
        public native void computeLogPolarMapping(@Adapter("MatAdapter") CvArr mappingMask);
        public native void SSD(@Adapter("MatAdapter") CvArr img, @ByVal CvPoint pt, @Adapter("MatAdapter") CvArr ssd);

        public native int smallSize();               public native SelfSimDescriptor smallSize(int smallSize);
        public native int largeSize();               public native SelfSimDescriptor largeSize(int largeSize);
        public native int startDistanceBucket();     public native SelfSimDescriptor startDistanceBucket(int startDistanceBucket);
        public native int numberOfDistanceBuckets(); public native SelfSimDescriptor numberOfDistanceBuckets(int numberOfDistanceBuckets);
        public native int numberOfAngles();          public native SelfSimDescriptor numberOfAngles(int numberOfAngles);

        public static final int DEFAULT_SMALL_SIZE = 5, DEFAULT_LARGE_SIZE = 41,
                DEFAULT_NUM_ANGLES = 20, DEFAULT_START_DISTANCE_BUCKET = 3,
                DEFAULT_NUM_DISTANCE_BUCKETS = 7;
    }


//    public static class Fjac extends FunctionPointer {
//        static { load(); }
//        public    Fjac(Pointer p) { super(p); }
//        protected Fjac() { allocate(); }
//        protected final native void allocate();
//        public native void call(int i, int j, @Cast("cv::Mat&") @Adapter("MatAdapter") CvMat point_params,
//                @Cast("cv::Mat&") @Adapter("MatAdapter") CvMat cam_params,
//                @Cast("cv::Mat&") @Adapter("MatAdapter") CvMat A,
//                @Cast("cv::Mat&") @Adapter("MatAdapter") CvMat B, Pointer data);
//    }
//
//    public static class Func extends FunctionPointer {
//        static { load(); }
//        public    Func(Pointer p) { super(p); }
//        protected Func() { allocate(); }
//        protected final native void allocate();
//        public native void call(int i, int j, @Cast("cv::Mat&") @Adapter("MatAdapter") CvMat point_params,
//                @Cast("cv::Mat&") @Adapter("MatAdapter") CvMat cam_params,
//                @Cast("cv::Mat&") @Adapter("MatAdapter") CvMat estim, Pointer data);
//    }
//
//    public static class BundleAdjustCallback extends FunctionPointer {
//        static { load(); }
//        public    BundleAdjustCallback(Pointer p) { super(p); }
//        protected BundleAdjustCallback() { allocate(); }
//        protected final native void allocate();
//        public native @Cast("bool") boolean call(int iteration, double norm_error, Pointer user_data);
//    }
//
//    @NoOffset @Namespace("cv") public static class LevMarqSparse extends Pointer {
//        static { load(); }
//        public LevMarqSparse() { allocate(); }
//        public LevMarqSparse(int npoints, int ncameras, int nPointParams, int nCameraParams, int nErrParams,
//                @Adapter("MatAdapter") CvMat visibility, @Adapter("MatAdapter") CvMat P0,
//                @Adapter("MatAdapter") CvMat X, @ByVal CvTermCriteria criteria, Fjac fjac, Func func, Pointer data,
//                BundleAdjustCallback cb, Pointer user_data) {
//            allocate(npoints, ncameras, nPointParams, nCameraParams, nErrParams, visibility, P0, X, criteria, fjac, func, data, cb, user_data);
//        }
//        public LevMarqSparse(Pointer p) { super(p); }
//        private native void allocate();
//        private native void allocate(int npoints, int ncameras, int nPointParams, int nCameraParams, int nErrParams,
//                @Adapter("MatAdapter") CvMat visibility, @Adapter("MatAdapter") CvMat P0,
//                @Adapter("MatAdapter") CvMat X, @ByVal CvTermCriteria criteria, Fjac fjac, Func func, Pointer data,
//                BundleAdjustCallback cb, Pointer user_data);
//
//        public native void run(int npoints, int ncameras, int nPointParams, int nCameraParams, int nErrParams,
//                @Adapter("MatAdapter") CvMat visibility, @Adapter("MatAdapter") CvMat P0,
//                @Adapter("MatAdapter") CvMat X, @ByVal CvTermCriteria criteria, Fjac fjac, Func func, Pointer data);
//
//        public native void clear();
//
//        // useful function to do simple bundle adjastment tasks
//        public static native void bundleAdjust(@Adapter("VectorAdapter<CvPoint3D32f,cv::Point3d>") CvPoint3D32f points,
//                @ByRef Point2dVectorVector imagePoints, @ByRef IntVectorVector visibility,
//                @Adapter("VectorAdapter<CvMat*,cv::Mat>") CvMatArray cameraMatrix,
//                @Adapter("VectorAdapter<CvMat*,cv::Mat>") CvMatArray R,
//                @Adapter("VectorAdapter<CvMat*,cv::Mat>") CvMatArray T,
//                @Adapter("VectorAdapter<CvMat*,cv::Mat>") CvMatArray distCoeffs,
//                @ByVal CvTermCriteria criteria/*=TermCriteria(TermCriteria::COUNT+TermCriteria::EPS, 30, DBL_EPSILON)*/,
//                BundleAdjustCallback cb/*=null*/, Pointer user_data/*=null*/);
//
//        public native void optimize(@ByRef CvMat _vis);
//
//        public native void ask_for_proj(@ByRef CvMat _vis, @Cast("bool") boolean once/*=false*/);
//        public native void ask_for_projac(@ByRef CvMat _vis);
//
//        public native CvMat err();               public native LevMarqSparse err(CvMat err);
//        public native double prevErrNorm();      public native LevMarqSparse prevErrNorm(double prevErrNorm);
//        public native double errNorm();          public native LevMarqSparse errNorm(double errNorm);
//        public native double lambda();           public native LevMarqSparse lambda(double lambda);
//        @ByRef
//        public native CvTermCriteria criteria(); public native LevMarqSparse criteria(CvTermCriteria criteria);
//        public native int iters();               public native LevMarqSparse iters(int iters);
//
//        public native CvMatArray U();            public native LevMarqSparse U(CvMatArray U);
//        public native CvMatArray V();            public native LevMarqSparse V(CvMatArray V);
//        public native CvMatArray inv_V_star();   public native LevMarqSparse inv_V_star(CvMatArray inv_V_star);
//
//        public native CvMatArray A();            public native LevMarqSparse A(CvMatArray A);
//        public native CvMatArray B();            public native LevMarqSparse B(CvMatArray B);
//        public native CvMatArray W();            public native LevMarqSparse W(CvMatArray W);
//
//        public native CvMat X();                 public native LevMarqSparse X(CvMat X);
//        public native CvMat hX();                public native LevMarqSparse hX(CvMat hX);
//
//        public native CvMat prevP();             public native LevMarqSparse prevP(CvMat prevP);
//        public native CvMat P();                 public native LevMarqSparse P(CvMat P);
//
//        public native CvMat deltaP();            public native LevMarqSparse deltaP(CvMat deltaP);
//
//        public native CvMatArray ea();           public native LevMarqSparse ea(CvMatArray ea);
//        public native CvMatArray eb();           public native LevMarqSparse eb(CvMatArray eb);
//
//        public native CvMatArray Yj();           public native LevMarqSparse Yj(CvMatArray Yj);
//        public native CvMat S();                 public native LevMarqSparse S(CvMat S);
//        public native CvMat JtJ_diag();          public native LevMarqSparse JtJ_diag(CvMat JtJ_diag);
//        public native CvMat Vis_index();         public native LevMarqSparse Vis_index(CvMat Vis_index);
//
//        public native int num_cams();            public native LevMarqSparse num_cams(int num_cams);
//        public native int num_points();          public native LevMarqSparse num_points(int num_points);
//        public native int num_err_param();       public native LevMarqSparse num_err_param(int num_err_param);
//        public native int num_cam_param();       public native LevMarqSparse num_cam_param(int num_cam_param);
//        public native int num_point_param();     public native LevMarqSparse num_point_param(int cnum_point_paramb);
//
//        public native Fjac fjac();               public native LevMarqSparse fjac(Fjac fjac);
//        public native Func func();               public native LevMarqSparse func(Func func);
//
//        public native Pointer data();            public native LevMarqSparse data(Pointer data);
//
//        public native BundleAdjustCallback cb(); public native LevMarqSparse cb(BundleAdjustCallback cb);
//        public native Pointer user_data();       public native LevMarqSparse user_data(Pointer user_data);
//    }

    @Namespace("cv") public static native int chamerMatching(@Adapter("MatAdapter") CvArr img,
            @Adapter("MatAdapter") CvArr templ, @ByRef PointVectorVector results,
            @Adapter(value="VectorAdapter<float>", out=true) FloatPointer cost,
            double templScale/*=1*/, int maxMatches/*=20*/, double minMatchDistance/*=1.0*/, int padX/*=3*/,
            int padY/*=3*/, int scales/*=5*/, double minScale/*=0.6*/, double maxScale/*=1.6*/,
            double orientationWeight/*=0.5*/, double truncate/*=20*/);


    @NoOffset @Namespace("cv") public static class StereoVar extends Pointer {
        static { load(); }
        public StereoVar() { allocate(); }
        public StereoVar(int levels, double pyrScale, int nIt, int minDisp, int maxDisp, int poly_n,
                double poly_sigma, float fi, float lambda, int penalization, int cycle, int flags) {
            allocate(levels, pyrScale, nIt, minDisp, maxDisp, poly_n, poly_sigma, fi, lambda, penalization, cycle, flags);
        }
        public StereoVar(Pointer p) { super(p); }
        private native void allocate();
        private native void allocate(int levels, double pyrScale, int nIt, int minDisp, int maxDisp, int poly_n,
                double poly_sigma, float fi, float lambda, int penalization, int cycle, int flags);

        public static final int
                USE_INITIAL_DISPARITY = 1, USE_EQUALIZE_HIST = 2, USE_SMART_ID = 4, USE_AUTO_PARAMS = 8, USE_MEDIAN_FILTERING = 16,
                CYCLE_O = 0, CYCLE_V = 1,
                PENALIZATION_TICHONOV = 0, PENALIZATION_CHARBONNIER = 1, PENALIZATION_PERONA_MALIK = 2;

        public native @Name("operator()") void compute(@Adapter("MatAdapter") CvArr left,
                @Adapter("MatAdapter") CvArr right, @Adapter("MatAdapter") CvArr disp);

        public native int levels();        public native StereoVar levels(int levels);
        public native double pyrScale();   public native StereoVar pyrScale(double pyrScale);
        public native int nIt();           public native StereoVar nIt(int nIt);
        public native int minDisp();       public native StereoVar minDisp(int minDisp);
        public native int maxDisp();       public native StereoVar maxDisp(int maxDisp);
        public native int poly_n();        public native StereoVar poly_n(int poly_n);
        public native double poly_sigma(); public native StereoVar poly_sigma(double poly_sigma);
        public native float fi();          public native StereoVar fi(float fi);
        public native float lambda();      public native StereoVar lambda(float lambda);
        public native int penalization();  public native StereoVar penalization(int penalization);
        public native int cycle();         public native StereoVar cycle(int cycle);
        public native int flags();         public native StereoVar flags(int flags);
    }

    @Namespace("cv") public static native void polyfit(CvMat srcx, CvMat srcy, @Adapter("MatAdapter") CvMat dst, int order);


    // enum RETINA_COLORSAMPLINGMETHOD
    public static final int
        RETINA_COLOR_RANDOM = 0,
        RETINA_COLOR_DIAGONAL = 1,
        RETINA_COLOR_BAYER = 2;

    @Namespace("cv") public static class Retina extends Pointer {
        static { load(); }
        public Retina(String parametersSaveFile, @ByVal CvSize inputSize) {
            allocate(parametersSaveFile, inputSize);
        }
        public Retina(String parametersSaveFile, @ByVal CvSize inputSize,
                boolean colorMode, @Cast("cv::RETINA_COLORSAMPLINGMETHOD") int colorSamplingMethod/*=RETINA_COLOR_BAYER*/,
                boolean useRetinaLogSampling/*=false*/, double reductionFactor/*=1.0*/, double samplingStrenght/*=10.0*/) {
            allocate(parametersSaveFile, inputSize, colorMode, colorSamplingMethod,
                    useRetinaLogSampling, reductionFactor, samplingStrenght);
        }
        public Retina(Pointer p) { super(p); }
        private native void allocate(String parametersSaveFile, @ByVal CvSize inputSize);
        private native void allocate(String parametersSaveFile, @ByVal CvSize inputSize,
                @Cast("bool") boolean colorMode, @Cast("cv::RETINA_COLORSAMPLINGMETHOD") int colorSamplingMethod/*=RETINA_COLOR_BAYER*/,
                @Cast("bool") boolean useRetinaLogSampling/*=false*/, double reductionFactor/*=1.0*/, double samplingStrenght/*=10.0*/);

        public native void setup(String retinaParameterFile/*=""*/, @Cast("bool") boolean applyDefaultSetupOnFailure/*=true*/);
        public native @ByRef String printSetup();
        public native void setupOPLandIPLParvoChannel(@Cast("bool") boolean colorMode/*=true*/, @Cast("bool") boolean normaliseOutput/*=true*/,
                double photoreceptorsLocalAdaptationSensitivity/*=0.7*/, double photoreceptorsTemporalConstant/*=0.5*/,
                double photoreceptorsSpatialConstant/*=0.53*/, double horizontalCellsGain/*=0*/,
                double HcellsTemporalConstant/*=1*/, double HcellsSpatialConstant/*=7*/, double ganglionCellsSensitivity/*=0.7*/);
        public native void setupIPLMagnoChannel(@Cast("bool") boolean normaliseOutput/*=true*/, double parasolCells_beta/*=0*/,
                double parasolCells_tau/*=0*/, double parasolCells_k/*=7*/, double amacrinCellsTemporalCutFrequency/*=1.2*/,
                double V0CompressionParameter/*=0.95*/, double localAdaptintegration_tau/*=0*/, double localAdaptintegration_k/*=7*/);
        public native void run(@Adapter("MatAdapter") CvArr inputImage);
        public native void getParvo(@Adapter("MatAdapter") CvArr retinaOutput_parvo);
        public native void getMagno(@Adapter("MatAdapter") CvArr retinaOutput_magno);
        public native void clearBuffers();

//        protected native @Adapter("FileStorageAdapter") CvFileStorage _parametersSaveFile();
//        protected native @ByRef String _parametersSaveFileName();
//        protected native std::valarray<double> _inputBuffer();
//        protected native RetinaFilter _retinaFilter();
//        protected native void _convertValarrayGrayBuffer2cvMat(@ByRef std::valarray<double> grayMatrixToConvert,
//                int nbRows, int nbColumns, @Cast("bool") boolean colorMode, @Adapter("MatAdapter") CvArr outBuffer);
//        protected native void _init(String parametersSaveFile, @ByVal CvSize inputSize, @Cast("bool") boolean colorMode,
//                @Cast("cv::RETINA_COLORSAMPLINGMETHOD") int colorSamplingMethod/*=RETINA_COLOR_BAYER*/,
//                @Cast("bool") boolean useRetinaLogSampling/*=false*/, double reductionFactor/*=1.0*/, double samplingStrenght/*=10.0*/);
    }
}

>>>>>>> 76aa07461566a5976980e6696204781271955163
