<<<<<<< HEAD
/**
 * Copyright 2009 Rednaxela
 * 
 * This software is provided 'as-is', without any express or implied
 * warranty. In no event will the authors be held liable for any damages
 * arising from the use of this software.
 * 
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 * 
 *    1. The origin of this software must not be misrepresented; you must not
 *    claim that you wrote the original software. If you use this software
 *    in a product, an acknowledgment in the product documentation would be
 *    appreciated but is not required.
 * 
 *    2. This notice may not be removed or altered from any source
 *    distribution.
 */

package ags.utils.dataStructures.trees.secondGenKD;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * An efficient well-optimized kd-tree
 * 
 * @author Rednaxela
 */
public abstract class KdTree<T> {
    // Static variables
    private static final int           bucketSize = 24;

    // All types
    private final int                  dimensions;
    private final KdTree<T>            parent;

    // Root only
    private final LinkedList<double[]> locationStack;
    private final Integer              sizeLimit;

    // Leaf only
    private double[][]                 locations;
    private Object[]                   data;
    private int                        locationCount;

    // Stem only
    private KdTree<T>                  left, right;
    private int                        splitDimension;
    private double                     splitValue;

    // Bounds
    private double[]                   minLimit, maxLimit;
    private boolean                    singularity;

    // Temporary
    private Status                     status;

    /**
     * Construct a RTree with a given number of dimensions and a limit on
     * maxiumum size (after which it throws away old points)
     */
    private KdTree(int dimensions, Integer sizeLimit) {
        this.dimensions = dimensions;

        // Init as leaf
        this.locations = new double[bucketSize][];
        this.data = new Object[bucketSize];
        this.locationCount = 0;
        this.singularity = true;

        // Init as root
        this.parent = null;
        this.sizeLimit = sizeLimit;
        if (sizeLimit != null) {
            this.locationStack = new LinkedList<double[]>();
        }
        else {
            this.locationStack = null;
        }
    }

    /**
     * Constructor for child nodes. Internal use only.
     */
    private KdTree(KdTree<T> parent, boolean right) {
        this.dimensions = parent.dimensions;

        // Init as leaf
        this.locations = new double[Math.max(bucketSize, parent.locationCount)][];
        this.data = new Object[Math.max(bucketSize, parent.locationCount)];
        this.locationCount = 0;
        this.singularity = true;

        // Init as non-root
        this.parent = parent;
        this.locationStack = null;
        this.sizeLimit = null;
    }

    /**
     * Get the number of points in the tree
     */
    public int size() {
        return locationCount;
    }

    /**
     * Add a point and associated value to the tree
     */
    public void addPoint(double[] location, T value) {
        KdTree<T> cursor = this;

        while (cursor.locations == null || cursor.locationCount >= cursor.locations.length) {
            if (cursor.locations != null) {
                cursor.splitDimension = cursor.findWidestAxis();
                cursor.splitValue = (cursor.minLimit[cursor.splitDimension] + cursor.maxLimit[cursor.splitDimension]) * 0.5;

                // Never split on infinity or NaN
                if (cursor.splitValue == Double.POSITIVE_INFINITY) {
                    cursor.splitValue = Double.MAX_VALUE;
                }
                else if (cursor.splitValue == Double.NEGATIVE_INFINITY) {
                    cursor.splitValue = -Double.MAX_VALUE;
                }
                else if (Double.isNaN(cursor.splitValue)) {
                    cursor.splitValue = 0;
                }

                // Don't split node if it has no width in any axis. Double the
                // bucket size instead
                if (cursor.minLimit[cursor.splitDimension] == cursor.maxLimit[cursor.splitDimension]) {
                    double[][] newLocations = new double[cursor.locations.length * 2][];
                    System.arraycopy(cursor.locations, 0, newLocations, 0, cursor.locationCount);
                    cursor.locations = newLocations;
                    Object[] newData = new Object[newLocations.length];
                    System.arraycopy(cursor.data, 0, newData, 0, cursor.locationCount);
                    cursor.data = newData;
                    break;
                }

                // Don't let the split value be the same as the upper value as
                // can happen due to rounding errors!
                if (cursor.splitValue == cursor.maxLimit[cursor.splitDimension]) {
                    cursor.splitValue = cursor.minLimit[cursor.splitDimension];
                }

                // Create child leaves
                KdTree<T> left = new ChildNode(cursor, false);
                KdTree<T> right = new ChildNode(cursor, true);

                // Move locations into children
                for (int i = 0; i < cursor.locationCount; i++) {
                    double[] oldLocation = cursor.locations[i];
                    Object oldData = cursor.data[i];
                    if (oldLocation[cursor.splitDimension] > cursor.splitValue) {
                        // Right
                        right.locations[right.locationCount] = oldLocation;
                        right.data[right.locationCount] = oldData;
                        right.locationCount++;
                        right.extendBounds(oldLocation);
                    }
                    else {
                        // Left
                        left.locations[left.locationCount] = oldLocation;
                        left.data[left.locationCount] = oldData;
                        left.locationCount++;
                        left.extendBounds(oldLocation);
                    }
                }

                // Make into stem
                cursor.left = left;
                cursor.right = right;
                cursor.locations = null;
                cursor.data = null;
            }

            cursor.locationCount++;
            cursor.extendBounds(location);

            if (location[cursor.splitDimension] > cursor.splitValue) {
                cursor = cursor.right;
            }
            else {
                cursor = cursor.left;
            }
        }

        cursor.locations[cursor.locationCount] = location;
        cursor.data[cursor.locationCount] = value;
        cursor.locationCount++;
        cursor.extendBounds(location);

        if (this.sizeLimit != null) {
            this.locationStack.add(location);
            if (this.locationCount > this.sizeLimit) {
                this.removeOld();
            }
        }
    }

    /**
     * Extends the bounds of this node do include a new location
     */
    private final void extendBounds(double[] location) {
        if (minLimit == null) {
            minLimit = new double[dimensions];
            System.arraycopy(location, 0, minLimit, 0, dimensions);
            maxLimit = new double[dimensions];
            System.arraycopy(location, 0, maxLimit, 0, dimensions);
            return;
        }

        for (int i = 0; i < dimensions; i++) {
            if (Double.isNaN(location[i])) {
                minLimit[i] = Double.NaN;
                maxLimit[i] = Double.NaN;
                singularity = false;
            }
            else if (minLimit[i] > location[i]) {
                minLimit[i] = location[i];
                singularity = false;
            }
            else if (maxLimit[i] < location[i]) {
                maxLimit[i] = location[i];
                singularity = false;
            }
        }
    }

    /**
     * Find the widest axis of the bounds of this node
     */
    private final int findWidestAxis() {
        int widest = 0;
        double width = (maxLimit[0] - minLimit[0]) * getAxisWeightHint(0);
        if (Double.isNaN(width)) width = 0;
        for (int i = 1; i < dimensions; i++) {
            double nwidth = (maxLimit[i] - minLimit[i]) * getAxisWeightHint(i);
            if (Double.isNaN(nwidth)) nwidth = 0;
            if (nwidth > width) {
                widest = i;
                width = nwidth;
            }
        }
        return widest;
    }

    /**
     * Remove the oldest value from the tree. Note: This cannot trim the bounds
     * of nodes, nor empty nodes, and thus you can't expect it to perfectly
     * preserve the speed of the tree as you keep adding.
     */
    private void removeOld() {
        double[] location = this.locationStack.removeFirst();
        KdTree<T> cursor = this;

        // Find the node where the point is
        while (cursor.locations == null) {
            if (location[cursor.splitDimension] > cursor.splitValue) {
                cursor = cursor.right;
            }
            else {
                cursor = cursor.left;
            }
        }

        for (int i = 0; i < cursor.locationCount; i++) {
            if (cursor.locations[i] == location) {
                System.arraycopy(cursor.locations, i + 1, cursor.locations, i, cursor.locationCount - i - 1);
                cursor.locations[cursor.locationCount-1] = null;
                System.arraycopy(cursor.data, i + 1, cursor.data, i, cursor.locationCount - i - 1);
                cursor.data[cursor.locationCount-1] = null;
                do {
                    cursor.locationCount--;
                    cursor = cursor.parent;
                } while (cursor.parent != null);
                return;
            }
        }
        // If we got here... we couldn't find the value to remove. Weird...
    }

    /**
     * Enumeration representing the status of a node during the running
     */
    private static enum Status {
        NONE, LEFTVISITED, RIGHTVISITED, ALLVISITED
    }

    /**
     * Stores a distance and value to output
     */
    public static class Entry<T> {
        public final double distance;
        public final T      value;

        private Entry(double distance, T value) {
            this.distance = distance;
            this.value = value;
        }
    }

    /**
     * Calculates the nearest 'count' points to 'location'
     */
    @SuppressWarnings("unchecked")
    public List<Entry<T>> nearestNeighbor(double[] location, int count, boolean sequentialSorting) {
        KdTree<T> cursor = this;
        cursor.status = Status.NONE;
        double range = Double.POSITIVE_INFINITY;
        ResultHeap resultHeap = new ResultHeap(count);

        do {
            if (cursor.status == Status.ALLVISITED) {
                // At a fully visited part. Move up the tree
                cursor = cursor.parent;
                continue;
            }

            if (cursor.status == Status.NONE && cursor.locations != null) {
                // At a leaf. Use the data.
                if (cursor.locationCount > 0) {
                    if (cursor.singularity) {
                        double dist = pointDist(cursor.locations[0], location);
                        if (dist <= range) {
                            for (int i = 0; i < cursor.locationCount; i++) {
                                resultHeap.addValue(dist, cursor.data[i]);
                            }
                        }
                    }
                    else {
                        for (int i = 0; i < cursor.locationCount; i++) {
                            double dist = pointDist(cursor.locations[i], location);
                            resultHeap.addValue(dist, cursor.data[i]);
                        }
                    }
                    range = resultHeap.getMaxDist();
                }

                if (cursor.parent == null) {
                    break;
                }
                cursor = cursor.parent;
                continue;
            }

            // Going to descend
            KdTree<T> nextCursor = null;
            if (cursor.status == Status.NONE) {
                // At a fresh node, descend the most probably useful direction
                if (location[cursor.splitDimension] > cursor.splitValue) {
                    // Descend right
                    nextCursor = cursor.right;
                    cursor.status = Status.RIGHTVISITED;
                }
                else {
                    // Descend left;
                    nextCursor = cursor.left;
                    cursor.status = Status.LEFTVISITED;
                }
            }
            else if (cursor.status == Status.LEFTVISITED) {
                // Left node visited, descend right.
                nextCursor = cursor.right;
                cursor.status = Status.ALLVISITED;
            }
            else if (cursor.status == Status.RIGHTVISITED) {
                // Right node visited, descend left.
                nextCursor = cursor.left;
                cursor.status = Status.ALLVISITED;
            }

            // Check if it's worth descending. Assume it is if it's sibling has
            // not been visited yet.
            if (cursor.status == Status.ALLVISITED) {
                if (nextCursor.locationCount == 0
                        || (!nextCursor.singularity && pointRegionDist(location, nextCursor.minLimit,
                                nextCursor.maxLimit) > range)) {
                    continue;
                }
            }

            // Descend down the tree
            cursor = nextCursor;
            cursor.status = Status.NONE;
        } while (cursor.parent != null || cursor.status != Status.ALLVISITED);

        ArrayList<Entry<T>> results = new ArrayList<Entry<T>>(resultHeap.values);
        if (sequentialSorting) {
            while (resultHeap.values > 0) {
                resultHeap.removeLargest();
                results.add(new Entry<T>(resultHeap.removedDist, (T)resultHeap.removedData));
            }
        }
        else {
            for (int i = 0; i < resultHeap.values; i++) {
                results.add(new Entry<T>(resultHeap.distance[i], (T)resultHeap.data[i]));
            }
        }

        return results;
    }

    // Override in subclasses
    protected abstract double pointDist(double[] p1, double[] p2);

    protected abstract double pointRegionDist(double[] point, double[] min, double[] max);

    protected double getAxisWeightHint(int i) {
        return 1.0;
    }

    /**
     * Internal class for child nodes
     */
    private class ChildNode extends KdTree<T> {
        private ChildNode(KdTree<T> parent, boolean right) {
            super(parent, right);
        }

        // Distance measurements are always called from the root node
        protected double pointDist(double[] p1, double[] p2) {
            throw new IllegalStateException();
        }

        protected double pointRegionDist(double[] point, double[] min, double[] max) {
            throw new IllegalStateException();
        }
    }

    /**
     * Class for tree with Weighted Squared Euclidean distancing
     */
    public static class WeightedSqrEuclid<T> extends KdTree<T> {
        private double[] weights;

        public WeightedSqrEuclid(int dimensions, Integer sizeLimit) {
            super(dimensions, sizeLimit);
            this.weights = new double[dimensions];
            Arrays.fill(this.weights, 1.0);
        }

        public void setWeights(double[] weights) {
            this.weights = weights;
        }

        protected double getAxisWeightHint(int i) {
            return weights[i];
        }

        protected double pointDist(double[] p1, double[] p2) {
            double d = 0;

            for (int i = 0; i < p1.length; i++) {
                double diff = (p1[i] - p2[i]) * weights[i];
                if (!Double.isNaN(diff)) {
                    d += diff * diff;
                }
            }

            return d;
        }

        protected double pointRegionDist(double[] point, double[] min, double[] max) {
            double d = 0;

            for (int i = 0; i < point.length; i++) {
                double diff = 0;
                if (point[i] > max[i]) {
                    diff = (point[i] - max[i]) * weights[i];
                }
                else if (point[i] < min[i]) {
                    diff = (point[i] - min[i]) * weights[i];
                }

                if (!Double.isNaN(diff)) {
                    d += diff * diff;
                }
            }

            return d;
        }
    }

    /**
     * Class for tree with Unweighted Squared Euclidean distancing
     */
    public static class SqrEuclid<T> extends KdTree<T> {
        public SqrEuclid(int dimensions, Integer sizeLimit) {
            super(dimensions, sizeLimit);
        }

        protected double pointDist(double[] p1, double[] p2) {
            double d = 0;

            for (int i = 0; i < p1.length; i++) {
                double diff = (p1[i] - p2[i]);
                if (!Double.isNaN(diff)) {
                    d += diff * diff;
                }
            }

            return d;
        }

        protected double pointRegionDist(double[] point, double[] min, double[] max) {
            double d = 0;

            for (int i = 0; i < point.length; i++) {
                double diff = 0;
                if (point[i] > max[i]) {
                    diff = (point[i] - max[i]);
                }
                else if (point[i] < min[i]) {
                    diff = (point[i] - min[i]);
                }

                if (!Double.isNaN(diff)) {
                    d += diff * diff;
                }
            }

            return d;
        }
    }

    /**
     * Class for tree with Weighted Manhattan distancing
     */
    public static class WeightedManhattan<T> extends KdTree<T> {
        private double[] weights;

        public WeightedManhattan(int dimensions, Integer sizeLimit) {
            super(dimensions, sizeLimit);
            this.weights = new double[dimensions];
            Arrays.fill(this.weights, 1.0);
        }

        public void setWeights(double[] weights) {
            this.weights = weights;
        }

        protected double getAxisWeightHint(int i) {
            return weights[i];
        }

        protected double pointDist(double[] p1, double[] p2) {
            double d = 0;

            for (int i = 0; i < p1.length; i++) {
                double diff = (p1[i] - p2[i]);
                if (!Double.isNaN(diff)) {
                    d += ((diff < 0) ? -diff : diff) * weights[i];
                }
            }

            return d;
        }

        protected double pointRegionDist(double[] point, double[] min, double[] max) {
            double d = 0;

            for (int i = 0; i < point.length; i++) {
                double diff = 0;
                if (point[i] > max[i]) {
                    diff = (point[i] - max[i]);
                }
                else if (point[i] < min[i]) {
                    diff = (min[i] - point[i]);
                }

                if (!Double.isNaN(diff)) {
                    d += diff * weights[i];
                }
            }

            return d;
        }
    }

    /**
     * Class for tree with Manhattan distancing
     */
    public static class Manhattan<T> extends KdTree<T> {
        public Manhattan(int dimensions, Integer sizeLimit) {
            super(dimensions, sizeLimit);
        }

        protected double pointDist(double[] p1, double[] p2) {
            double d = 0;

            for (int i = 0; i < p1.length; i++) {
                double diff = (p1[i] - p2[i]);
                if (!Double.isNaN(diff)) {
                    d += (diff < 0) ? -diff : diff;
                }
            }

            return d;
        }

        protected double pointRegionDist(double[] point, double[] min, double[] max) {
            double d = 0;

            for (int i = 0; i < point.length; i++) {
                double diff = 0;
                if (point[i] > max[i]) {
                    diff = (point[i] - max[i]);
                }
                else if (point[i] < min[i]) {
                    diff = (min[i] - point[i]);
                }

                if (!Double.isNaN(diff)) {
                    d += diff;
                }
            }

            return d;
        }
    }

    /**
     * Class for tracking up to 'size' closest values
     */
    private static class ResultHeap {
        private final Object[] data;
        private final double[] distance;
        private final int      size;
        private int            values;
        public Object          removedData;
        public double          removedDist;

        public ResultHeap(int size) {
            this.data = new Object[size];
            this.distance = new double[size];
            this.size = size;
            this.values = 0;
        }

        public void addValue(double dist, Object value) {
            // If there is still room in the heap
            if (values < size) {
                // Insert new value at the end
                data[values] = value;
                distance[values] = dist;
                upHeapify(values);
                values++;
            }
            // If there is no room left in the heap, and the new entry is lower
            // than the max entry
            else if (dist < distance[0]) {
                // Replace the max entry with the new entry
                data[0] = value;
                distance[0] = dist;
                downHeapify(0);
            }
        }

        public void removeLargest() {
            if (values == 0) {
                throw new IllegalStateException();
            }

            removedData = data[0];
            removedDist = distance[0];
            values--;
            data[0] = data[values];
            distance[0] = distance[values];
            downHeapify(0);
        }

        private void upHeapify(int c) {
            for (int p = (c - 1) / 2; c != 0 && distance[c] > distance[p]; c = p, p = (c - 1) / 2) {
                Object pData = data[p];
                double pDist = distance[p];
                data[p] = data[c];
                distance[p] = distance[c];
                data[c] = pData;
                distance[c] = pDist;
            }
        }

        private void downHeapify(int p) {
            for (int c = p * 2 + 1; c < values; p = c, c = p * 2 + 1) {
                if (c + 1 < values && distance[c] < distance[c + 1]) {
                    c++;
                }
                if (distance[p] < distance[c]) {
                    // Swap the points
                    Object pData = data[p];
                    double pDist = distance[p];
                    data[p] = data[c];
                    distance[p] = distance[c];
                    data[c] = pData;
                    distance[c] = pDist;
                }
                else {
                    break;
                }
            }
        }

        public double getMaxDist() {
            if (values < size) {
                return Double.POSITIVE_INFINITY;
            }
            return distance[0];
        }
    }
=======
/*
 * Copyright 2014 Frank Asseg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package net.objecthunter.exp4j;

import static java.lang.Math.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.objecthunter.exp4j.function.Function;
import net.objecthunter.exp4j.operator.Operator;

import org.junit.Test;

public class ExpressionBuilderTest {

    @Test
    public void testExpressionBuilder1() throws Exception {
        double result = new ExpressionBuilder("2+1")
                .build()
                .evaluate();
        assertEquals(3d, result, 0d);
    }

    @Test
    public void testExpressionBuilder2() throws Exception {
        double result = new ExpressionBuilder("cos(x)")
                .variables("x")
                .build()
                .setVariable("x", Math.PI)
                .evaluate();
        double expected = cos(Math.PI);
        assertEquals(-1d, result, 0d);
    }

    @Test
    public void testExpressionBuilder3() throws Exception {
        double x = Math.PI;
        double result = new ExpressionBuilder("sin(x)-log(3*x/4)")
                .variables("x")
                .build()
                .setVariable("x", x)
                .evaluate();

        double expected = sin(x) - log(3 * x / 4);
        assertEquals(expected, result, 0d);
    }

    @Test
    public void testExpressionBuilder4() throws Exception {
        Function log2 = new Function("log2", 1) {

            @Override
            public double apply(double... args) {
                return Math.log(args[0]) / Math.log(2);
            }
        };
        double result = new ExpressionBuilder("log2(4)")
                .function(log2)
                .build()
                .evaluate();

        double expected = 2;
        assertEquals(expected, result, 0d);
    }

    @Test
    public void testExpressionBuilder5() throws Exception {
        Function avg = new Function("avg", 4) {

            @Override
            public double apply(double... args) {
                double sum = 0;
                for (double arg : args) {
                    sum += arg;
                }
                return sum / args.length;
            }
        };
        double result = new ExpressionBuilder("avg(1,2,3,4)")
                .function(avg)
                .build()
                .evaluate();

        double expected = 2.5d;
        assertEquals(expected, result, 0d);
    }

    @Test
    public void testExpressionBuilder6() throws Exception {
        Operator factorial = new Operator("!", 1, true, Operator.PRECEDENCE_POWER + 1) {

            @Override
            public double apply(double... args) {
                final int arg = (int) args[0];
                if ((double) arg != args[0]) {
                    throw new IllegalArgumentException("Operand for factorial has to be an integer");
                }
                if (arg < 0) {
                    throw new IllegalArgumentException("The operand of the factorial can not be less than zero");
                }
                double result = 1;
                for (int i = 1; i <= arg; i++) {
                    result *= i;
                }
                return result;
            }
        };

        double result = new ExpressionBuilder("3!")
                .operator(factorial)
                .build()
                .evaluate();

        double expected = 6d;
        assertEquals(expected, result, 0d);
    }

    @Test
    public void testExpressionBuilder7() throws Exception {
        ValidationResult res = new ExpressionBuilder("x")
                .variables("x")
                .build()
                .validate();
        assertFalse(res.isValid());
        assertEquals(res.getErrors().size(), 1);
    }

    @Test
    public void testExpressionBuilder8() throws Exception {
        ValidationResult res = new ExpressionBuilder("x*y*z")
                .variables("x", "y", "z")
                .build()
                .validate();
        assertFalse(res.isValid());
        assertEquals(res.getErrors().size(), 3);
    }

    @Test
    public void testExpressionBuilder9() throws Exception {
        ValidationResult res = new ExpressionBuilder("x")
                .variables("x")
                .build()
                .setVariable("x", 1d)
                .validate();
        assertTrue(res.isValid());
    }

    @Test
    public void testValidationDocExample() throws Exception {
        Expression e = new ExpressionBuilder("x")
                .variables("x")
                .build();
        ValidationResult res = e.validate();
        assertFalse(res.isValid());
        assertEquals(1, res.getErrors().size());

        e.setVariable("x", 1d);
        res = e.validate();
        assertTrue(res.isValid());
    }

    @Test
    public void testExpressionBuilder10() throws Exception {
        double result = new ExpressionBuilder("1e1")
                .build()
                .evaluate();
        assertEquals(10d, result, 0d);
    }

    @Test
    public void testExpressionBuilder11() throws Exception {
        double result = new ExpressionBuilder("1.11e-1")
                .build()
                .evaluate();
        assertEquals(0.111d, result, 0d);
    }

    @Test
    public void testExpressionBuilder12() throws Exception {
        double result = new ExpressionBuilder("1.11e+1")
                .build()
                .evaluate();
        assertEquals(11.1d, result, 0d);
    }

    @Test
    public void testExpressionBuilder13() throws Exception {
        double result = new ExpressionBuilder("-3^2")
                .build()
                .evaluate();
        assertEquals(-9d, result, 0d);
    }

    @Test
    public void testExpressionBuilder14() throws Exception {
        double result = new ExpressionBuilder("(-3)^2")
                .build()
                .evaluate();
        assertEquals(9d, result, 0d);
    }

    @Test(expected = ArithmeticException.class)
    public void testExpressionBuilder15() throws Exception {
        double result = new ExpressionBuilder("-3/0")
                .build()
                .evaluate();
    }

    @Test
    public void testExpressionBuilder16() throws Exception {
        double result = new ExpressionBuilder("log(x) - y * (sqrt(x^cos(y)))")
                .variables("x", "y")
                .build()
                .setVariable("x", 1d)
                .setVariable("y", 2d)
                .evaluate();
    }

    @Test
    public void testExpressionBuilder17() throws Exception {
        Expression e = new ExpressionBuilder("x-y*")
                .variables("x", "y")
                .build();
        ValidationResult res = e.validate(false);
        assertFalse(res.isValid());
        assertEquals(1,res.getErrors().size());
        assertEquals("Too many operators", res.getErrors().get(0));
    }

    @Test
    public void testExpressionBuilder18() throws Exception {
        Expression e = new ExpressionBuilder("log(x) - y *")
                .variables("x", "y")
                .build();
        ValidationResult res = e.validate(false);
        assertFalse(res.isValid());
        assertEquals(1,res.getErrors().size());
        assertEquals("Too many operators", res.getErrors().get(0));
    }

    @Test
    public void testExpressionBuilder19() throws Exception {
        Expression e = new ExpressionBuilder("x - y *")
                .variables("x", "y")
                .build();
        ValidationResult res = e.validate(false);
        assertFalse(res.isValid());
        assertEquals(1,res.getErrors().size());
        assertEquals("Too many operators", res.getErrors().get(0));
    }

    /* legacy tests from earlier exp4j versions */

    @Test
    public void testFunction1() throws Exception {
        Function custom = new Function("timespi") {

            @Override
            public double apply(double... values) {
                return values[0] * Math.PI;
            }
        };
        Expression e = new ExpressionBuilder("timespi(x)")
                .function(custom)
                .variables("x")
                .build()
                .setVariable("x", 1);
        double result = e.evaluate();
        assertTrue(result == Math.PI);
    }

    @Test
    public void testFunction2() throws Exception {
        Function custom = new Function("loglog") {

            @Override
            public double apply(double... values) {
                return Math.log(Math.log(values[0]));
            }
        };
        Expression e = new ExpressionBuilder("loglog(x)")
                .variables("x")
                .function(custom)
                .build()
                .setVariable("x", 1);
        double result = e.evaluate();
        assertTrue(result == Math.log(Math.log(1)));
    }

    @Test
    public void testFunction3() throws Exception {
        Function custom1 = new Function("foo") {

            @Override
            public double apply(double... values) {
                return values[0] * Math.E;
            }
        };
        Function custom2 = new Function("bar") {

            @Override
            public double apply(double... values) {
                return values[0] * Math.PI;
            }
        };
        Expression e = new ExpressionBuilder("foo(bar(x))")
                .function(custom1)
                .function(custom2)
                .variables("x")
                .build()
                .setVariable("x", 1);
        double result = e.evaluate();
        assertTrue(result == 1 * Math.E * Math.PI);
    }

    @Test
    public void testFunction4() throws Exception {
        Function custom1 = new Function("foo") {

            @Override
            public double apply(double... values) {
                return values[0] * Math.E;
            }
        };
        double varX = 32.24979131d;
        Expression e = new ExpressionBuilder("foo(log(x))")
                .variables("x")
                .function(custom1)
                .build()
                .setVariable("x", varX);
        double result = e.evaluate();
        assertTrue(result == Math.log(varX) * Math.E);
    }

    @Test
    public void testFunction5() throws Exception {
        Function custom1 = new Function("foo") {

            @Override
            public double apply(double... values) {
                return values[0] * Math.E;
            }
        };
        Function custom2 = new Function("bar") {

            @Override
            public double apply(double... values) {
                return values[0] * Math.PI;
            }
        };
        double varX = 32.24979131d;
        Expression e = new ExpressionBuilder("bar(foo(log(x)))")
                .variables("x")
                .function(custom1)
                .function(custom2)
                .build()
                .setVariable("x", varX);
        double result = e.evaluate();
        assertTrue(result == Math.log(varX) * Math.E * Math.PI);
    }

    @Test
    public void testFunction6() throws Exception {
        Function custom1 = new Function("foo") {

            @Override
            public double apply(double... values) {
                return values[0] * Math.E;
            }
        };
        Function custom2 = new Function("bar") {

            @Override
            public double apply(double... values) {
                return values[0] * Math.PI;
            }
        };
        double varX = 32.24979131d;
        Expression e = new ExpressionBuilder("bar(foo(log(x)))")
                .variables("x")
                .functions(custom1, custom2)
                .build()
                .setVariable("x", varX);
        double result = e.evaluate();
        assertTrue(result == Math.log(varX) * Math.E * Math.PI);
    }

    @Test
    public void testFunction7() throws Exception {
        Function custom1 = new Function("half") {

            @Override
            public double apply(double... values) {
                return values[0] / 2;
            }
        };
        Expression e = new ExpressionBuilder("half(x)")
                .variables("x")
                .function(custom1)
                .build()
                .setVariable("x", 1d);
        assertTrue(0.5d == e.evaluate());
    }

    @Test
    public void testFunction10() throws Exception {
        Function custom1 = new Function("max", 2) {

            @Override
            public double apply(double... values) {
                return values[0] < values[1] ? values[1] : values[0];
            }
        };
        Expression e =
                new ExpressionBuilder("max(x,y)")
                        .variables("x", "y")
                        .function(custom1)
                        .build()
                        .setVariable("x", 1d)
                        .setVariable("y", 2d);
        assertTrue(2 == e.evaluate());
    }

    @Test
    public void testFunction11() throws Exception {
        Function custom1 = new Function("power", 2) {

            @Override
            public double apply(double... values) {
                return Math.pow(values[0], values[1]);
            }
        };
        Expression e =
                new ExpressionBuilder("power(x,y)")
                        .variables("x", "y")
                        .function(custom1)
                        .build()
                        .setVariable("x", 2d)
                        .setVariable("y",
                                4d);
        assertTrue(Math.pow(2, 4) == e.evaluate());
    }

    @Test
    public void testFunction12() throws Exception {
        Function custom1 = new Function("max", 5) {

            @Override
            public double apply(double... values) {
                double max = values[0];
                for (int i = 1; i < numArguments; i++) {
                    if (values[i] > max) {
                        max = values[i];
                    }
                }
                return max;
            }
        };
        Expression e = new ExpressionBuilder("max(1,2.43311,51.13,43,12)")
                .function(custom1)
                .build();
        assertTrue(51.13d == e.evaluate());
    }

    @Test
    public void testFunction13() throws Exception {
        Function custom1 = new Function("max", 3) {

            @Override
            public double apply(double... values) {
                double max = values[0];
                for (int i = 1; i < numArguments; i++) {
                    if (values[i] > max) {
                        max = values[i];
                    }
                }
                return max;
            }
        };
        double varX = Math.E;
        Expression e = new ExpressionBuilder("max(log(x),sin(x),x)")
                .variables("x")
                .function(custom1)
                .build()
                .setVariable("x", varX);
        assertTrue(varX == e.evaluate());
    }

    @Test
    public void testFunction14() throws Exception {
        Function custom1 = new Function("multiply", 2) {

            @Override
            public double apply(double... values) {
                return values[0] * values[1];
            }
        };
        double varX = 1;
        Expression e = new ExpressionBuilder("multiply(sin(x),x+1)")
                .variables("x")
                .function(custom1)
                .build()
                .setVariable("x", varX);
        double expected = Math.sin(varX) * (varX + 1);
        double actual = e.evaluate();
        assertTrue(expected == actual);
    }

    @Test
    public void testFunction15() throws Exception {
        Function custom1 = new Function("timesPi") {

            @Override
            public double apply(double... values) {
                return values[0] * Math.PI;
            }
        };
        double varX = 1;
        Expression e = new ExpressionBuilder("timesPi(x^2)")
                .variables("x")
                .function(custom1)
                .build()
                .setVariable("x", varX);
        double expected = varX * Math.PI;
        double actual = e.evaluate();
        assertTrue(expected == actual);
    }

    @Test
    public void testFunction16() throws Exception {
        Function custom1 = new Function("multiply", 3) {

            @Override
            public double apply(double... values) {
                return values[0] * values[1] * values[2];
            }
        };
        double varX = 1;
        Expression e = new ExpressionBuilder("multiply(sin(x),x+1^(-2),log(x))")
                .variables("x")
                .function(custom1)
                .build()
                .setVariable("x", varX);
        double expected = Math.sin(varX) * Math.pow((varX + 1), -2) * Math.log(varX);
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testFunction17() throws Exception {
        Function custom1 = new Function("timesPi") {

            @Override
            public double apply(double... values) {
                return values[0] * Math.PI;
            }
        };
        double varX = Math.E;
        Expression e = new ExpressionBuilder("timesPi(log(x^(2+1)))")
                .variables("x")
                .function(custom1)
                .build()
                .setVariable("x", varX);
        double expected = Math.log(Math.pow(varX, 3)) * Math.PI;
        assertTrue(expected == e.evaluate());
    }

    // thanks to Marcin Domanski who issued
    // http://jira.congrace.de/jira/browse/EXP-11
    // i have this test, which fails in 0.2.9
    @Test
    public void testFunction18() throws Exception {
        Function minFunction = new Function("min", 2) {

            @Override
            public double apply(double[] values) {
                double currentMin = Double.POSITIVE_INFINITY;
                for (double value : values) {
                    currentMin = Math.min(currentMin, value);
                }
                return currentMin;
            }
        };
        ExpressionBuilder b = new ExpressionBuilder("-min(5, 0) + 10")
                .function(minFunction);
        double calculated = b.build().evaluate();
        assertTrue(calculated == 10);
    }

    // thanks to Sylvain Machefert who issued
    // http://jira.congrace.de/jira/browse/EXP-11
    // i have this test, which fails in 0.3.2
    @Test
    public void testFunction19() throws Exception {
        Function minFunction = new Function("power", 2) {

            @Override
            public double apply(double[] values) {
                return Math.pow(values[0], values[1]);
            }
        };
        ExpressionBuilder b = new ExpressionBuilder("power(2,3)")
                .function(minFunction);
        double calculated = b.build().evaluate();
        assertEquals(Math.pow(2, 3), calculated, 0d);
    }

    // thanks to Narendra Harmwal who noticed that getArgumentCount was not
    // implemented
    // this test has been added in 0.3.5
    @Test
    public void testFunction20() throws Exception {
        Function maxFunction = new Function("max", 3) {

            @Override
            public double apply(double... values) {
                double max = values[0];
                for (int i = 1; i < numArguments; i++) {
                    if (values[i] > max) {
                        max = values[i];
                    }
                }
                return max;
            }
        };
        ExpressionBuilder b = new ExpressionBuilder("max(1,2,3)")
                .function(maxFunction);
        double calculated = b.build().evaluate();
        assertTrue(maxFunction.getNumArguments() == 3);
        assertTrue(calculated == 3);
    }

    @Test
    public void testOperators1() throws Exception {
        Operator factorial = new Operator("!", 1, true, Operator.PRECEDENCE_POWER + 1) {

            @Override
            public double apply(double... args) {
                final int arg = (int) args[0];
                if ((double) arg != args[0]) {
                    throw new IllegalArgumentException("Operand for factorial has to be an integer");
                }
                if (arg < 0) {
                    throw new IllegalArgumentException("The operand of the factorial can not be less than zero");
                }
                double result = 1;
                for (int i = 1; i <= arg; i++) {
                    result *= i;
                }
                return result;
            }
        };

        Expression e = new ExpressionBuilder("1!").operator(factorial)
                .build();
        assertTrue(1d == e.evaluate());
        e = new ExpressionBuilder("2!").operator(factorial)
                .build();
        assertTrue(2d == e.evaluate());
        e = new ExpressionBuilder("3!").operator(factorial)
                .build();
        assertTrue(6d == e.evaluate());
        e = new ExpressionBuilder("4!").operator(factorial)
                .build();
        assertTrue(24d == e.evaluate());
        e = new ExpressionBuilder("5!").operator(factorial)
                .build();
        assertTrue(120d == e.evaluate());
        e = new ExpressionBuilder("11!").operator(factorial)
                .build();
        assertTrue(39916800d == e.evaluate());
    }

    @Test
    public void testOperators2() throws Exception {
        Operator factorial = new Operator("!", 1, true, Operator.PRECEDENCE_POWER + 1) {

            @Override
            public double apply(double... args) {
                final int arg = (int) args[0];
                if ((double) arg != args[0]) {
                    throw new IllegalArgumentException("Operand for factorial has to be an integer");
                }
                if (arg < 0) {
                    throw new IllegalArgumentException("The operand of the factorial can not be less than zero");
                }
                double result = 1;
                for (int i = 1; i <= arg; i++) {
                    result *= i;
                }
                return result;
            }
        };
        Expression e = new ExpressionBuilder("2^3!").operator(factorial)
                .build();
        assertEquals(64d, e.evaluate(), 0d);
        e = new ExpressionBuilder("3!^2").operator(factorial)
                .build();
        assertTrue(36d == e.evaluate());
        e = new ExpressionBuilder("-(3!)^-1").operator(factorial)
                .build();
        double actual = e.evaluate();
        assertEquals(Math.pow(-6d, -1), actual, 0d);
    }

    @Test
    public void testOperators3() throws Exception {
        Operator gteq = new Operator(">=", 2, true, Operator.PRECEDENCE_ADDITION - 1) {

            @Override
            public double apply(double[] values) {
                if (values[0] >= values[1]) {
                    return 1d;
                } else {
                    return 0d;
                }
            }
        };
        Expression e = new ExpressionBuilder("1>=2").operator(gteq)
                .build();
        assertTrue(0d == e.evaluate());
        e = new ExpressionBuilder("2>=1").operator(gteq)
                .build();
        assertTrue(1d == e.evaluate());
        e = new ExpressionBuilder("-2>=1").operator(gteq)
                .build();
        assertTrue(0d == e.evaluate());
        e = new ExpressionBuilder("-2>=-1").operator(gteq)
                .build();
        assertTrue(0d == e.evaluate());
    }

    @Test
    public void testModulo1() throws Exception {
        double result = new ExpressionBuilder("33%(20/2)%2")
                .build().evaluate();
        assertTrue(result == 1d);
    }

    @Test
    public void testOperators4() throws Exception {
        Operator greaterEq = new Operator(">=", 2, true, 4) {

            @Override
            public double apply(double[] values) {
                if (values[0] >= values[1]) {
                    return 1d;
                } else {
                    return 0d;
                }
            }
        };
        Operator greater = new Operator(">", 2, true, 4) {

            @Override
            public double apply(double[] values) {
                if (values[0] > values[1]) {
                    return 1d;
                } else {
                    return 0d;
                }
            }
        };
        Operator newPlus = new Operator(">=>", 2, true, 4) {

            @Override
            public double apply(double[] values) {
                return values[0] + values[1];
            }
        };
        Expression e = new ExpressionBuilder("1>2").operator(greater)
                .build();
        assertTrue(0d == e.evaluate());
        e = new ExpressionBuilder("2>=2").operator(greaterEq)
                .build();
        assertTrue(1d == e.evaluate());
        e = new ExpressionBuilder("1>=>2").operator(newPlus)
                .build();
        assertTrue(3d == e.evaluate());
        e = new ExpressionBuilder("1>=>2>2").operator(greater).operator(newPlus)
                .build();
        assertTrue(1d == e.evaluate());
        e = new ExpressionBuilder("1>=>2>2>=1").operator(greater).operator(newPlus)
                .operator(greaterEq)
                .build();
        assertTrue(1d == e.evaluate());
        e = new ExpressionBuilder("1 >=> 2 > 2 >= 1").operator(greater).operator(newPlus)
                .operator(greaterEq)
                .build();
        assertTrue(1d == e.evaluate());
        e = new ExpressionBuilder("1 >=> 2 >= 2 > 1").operator(greater).operator(newPlus)
                .operator(greaterEq)
                .build();
        assertTrue(0d == e.evaluate());
        e = new ExpressionBuilder("1 >=> 2 >= 2 > 0").operator(greater).operator(newPlus)
                .operator(greaterEq)
                .build();
        assertTrue(1d == e.evaluate());
        e = new ExpressionBuilder("1 >=> 2 >= 2 >= 1").operator(greater).operator(newPlus)
                .operator(greaterEq)
                .build();
        assertTrue(1d == e.evaluate());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidOperator1() throws Exception {
        Operator fail = new Operator("2", 2, true, 1) {

            @Override
            public double apply(double[] values) {
                return 0;
            }
        };
        new ExpressionBuilder("1").operator(fail)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFunction1() throws Exception {
        Function func = new Function("1gd") {

            @Override
            public double apply(double... args) {
                return 0;
            }
        };
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFunction2() throws Exception {
        Function func = new Function("+1gd") {

            @Override
            public double apply(double... args) {
                return 0;
            }
        };
    }

    @Test
    public void testExpressionBuilder01() throws Exception {
        Expression e = new ExpressionBuilder("7*x + 3*y")
                .variables("x", "y")
                .build()
                .setVariable("x", 1)
                .setVariable("y", 2);
        double result = e.evaluate();
        assertTrue(result == 13d);
    }

    @Test
    public void testExpressionBuilder02() throws Exception {
        Expression e = new ExpressionBuilder("7*x + 3*y")
                .variables("x", "y")
                .build()
                .setVariable("x", 1)
                .setVariable("y", 2);
        double result = e.evaluate();
        assertTrue(result == 13d);
    }

    @Test
    public void testExpressionBuilder03() throws Exception {
        double varX = 1.3d;
        double varY = 4.22d;
        Expression e = new ExpressionBuilder("7*x + 3*y - log(y/x*12)^y")
                .variables("x", "y")
                .build()
                .setVariable("x", varX)
                .setVariable("y",
                        varY);
        double result = e.evaluate();
        assertTrue(result == 7 * varX + 3 * varY - Math.pow(Math.log(varY / varX * 12), varY));
    }

    @Test
    public void testExpressionBuilder04() throws Exception {
        double varX = 1.3d;
        double varY = 4.22d;
        Expression e =
                new ExpressionBuilder("7*x + 3*y - log(y/x*12)^y")
                        .variables("x", "y")
                        .build()
                        .setVariable("x", varX)
                        .setVariable("y", varY);
        double result = e.evaluate();
        assertTrue(result == 7 * varX + 3 * varY - Math.pow(Math.log(varY / varX * 12), varY));
        varX = 1.79854d;
        varY = 9281.123d;
        e.setVariable("x", varX);
        e.setVariable("y", varY);
        result = e.evaluate();
        assertTrue(result == 7 * varX + 3 * varY - Math.pow(Math.log(varY / varX * 12), varY));
    }

    @Test
    public void testExpressionBuilder05() throws Exception {
        double varX = 1.3d;
        double varY = 4.22d;
        Expression e = new ExpressionBuilder("3*y")
                .variables("y")
                .build()
                .setVariable("x", varX)
                .setVariable("y", varY);
        double result = e.evaluate();
        assertTrue(result == 3 * varY);
    }

    @Test
    public void testExpressionBuilder06() throws Exception {
        double varX = 1.3d;
        double varY = 4.22d;
        double varZ = 4.22d;
        Expression e = new ExpressionBuilder("x * y * z")
                .variables("x", "y", "z")
                .build();
        e.setVariable("x", varX);
        e.setVariable("y", varY);
        e.setVariable("z", varZ);
        double result = e.evaluate();
        assertTrue(result == varX * varY * varZ);
    }

    @Test
    public void testExpressionBuilder07() throws Exception {
        double varX = 1.3d;
        Expression e = new ExpressionBuilder("log(sin(x))")
                .variables("x")
                .build()
                .setVariable("x", varX);
        double result = e.evaluate();
        assertTrue(result == Math.log(Math.sin(varX)));
    }

    @Test
    public void testExpressionBuilder08() throws Exception {
        double varX = 1.3d;
        Expression e = new ExpressionBuilder("log(sin(x))")
                .variables("x")
                .build()
                .setVariable("x", varX);
        double result = e.evaluate();
        assertTrue(result == Math.log(Math.sin(varX)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSameName() throws Exception {
        Function custom = new Function("bar") {

            @Override
            public double apply(double... values) {
                return values[0] / 2;
            }
        };
        double varBar = 1.3d;
        Expression e = new ExpressionBuilder("bar(bar)")
                .variables("bar")
                .function(custom)
                .build()
                .setVariable("bar", varBar);
        ValidationResult res = e.validate();
        assertFalse(res.isValid());
        assertEquals(1, res.getErrors().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFunction() throws Exception {
        double varY = 4.22d;
        Expression e = new ExpressionBuilder("3*invalid_function(y)")
                .variables("<")
                .build()
                .setVariable("y", varY);
        e.evaluate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingVar() throws Exception {
        double varY = 4.22d;
        Expression e = new ExpressionBuilder("3*y*z")
                .variables("y", "z")
                .build()
                .setVariable("y", varY);
        e.evaluate();
    }

    @Test
    public void testUnaryMinusPowerPrecedence() throws Exception {
        Expression e = new ExpressionBuilder("-1^2")
                .build();
        assertEquals(-1d, e.evaluate(), 0d);
    }

    @Test
    public void testUnaryMinus() throws Exception {
        Expression e = new ExpressionBuilder("-1")
                .build();
        assertEquals(-1d, e.evaluate(), 0d);
    }

    @Test
    public void testExpression1() throws Exception {
        String expr;
        double expected;
        expr = "2 + 4";
        expected = 6d;
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testExpression10() throws Exception {
        String expr;
        double expected;
        expr = "1 * 1.5 + 1";
        expected = 1 * 1.5 + 1;
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testExpression11() throws Exception {
        double x = 1d;
        double y = 2d;
        String expr = "log(x) ^ sin(y)";
        double expected = Math.pow(Math.log(x), Math.sin(y));
        Expression e = new ExpressionBuilder(expr)
                .variables("x", "y")
                .build()
                .setVariable("x", x)
                .setVariable("y", y);
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testExpression12() throws Exception {
        String expr = "log(2.5333333333)^(0-1)";
        double expected = Math.pow(Math.log(2.5333333333d), -1);
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testExpression13() throws Exception {
        String expr = "2.5333333333^(0-1)";
        double expected = Math.pow(2.5333333333d, -1);
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testExpression14() throws Exception {
        String expr = "2 * 17.41 + (12*2)^(0-1)";
        double expected = 2 * 17.41d + Math.pow((12 * 2), -1);
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testExpression15() throws Exception {
        String expr = "2.5333333333 * 17.41 + (12*2)^log(2.764)";
        double expected = 2.5333333333d * 17.41d + Math.pow((12 * 2), Math.log(2.764d));
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testExpression16() throws Exception {
        String expr = "2.5333333333/2 * 17.41 + (12*2)^(log(2.764) - sin(5.6664))";
        double expected = 2.5333333333d / 2 * 17.41d + Math.pow((12 * 2), Math.log(2.764d) - Math.sin(5.6664d));
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testExpression17() throws Exception {
        String expr = "x^2 - 2 * y";
        double x = Math.E;
        double y = Math.PI;
        double expected = x * x - 2 * y;
        Expression e = new ExpressionBuilder(expr)
                .variables("x", "y")
                .build()
                .setVariable("x", x)
                .setVariable("y", y);
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testExpression18() throws Exception {
        String expr = "-3";
        double expected = -3;
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testExpression19() throws Exception {
        String expr = "-3 * -24.23";
        double expected = -3 * -24.23d;
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testExpression2() throws Exception {
        String expr;
        double expected;
        expr = "2+3*4-12";
        expected = 2 + 3 * 4 - 12;
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testExpression20() throws Exception {
        String expr = "-2 * 24/log(2) -2";
        double expected = -2 * 24 / Math.log(2) - 2;
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testExpression21() throws Exception {
        String expr = "-2 *33.34/log(x)^-2 + 14 *6";
        double x = 1.334d;
        double expected = -2 * 33.34 / Math.pow(Math.log(x), -2) + 14 * 6;
        Expression e = new ExpressionBuilder(expr)
                .variables("x")
                .build()
                .setVariable("x", x);
        assertEquals(expected, e.evaluate(), 0d);
    }

    @Test
    public void testExpressionPower() throws Exception {
        String expr = "2^-2";
        double expected = Math.pow(2, -2);
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(), 0d);
    }

    @Test
    public void testExpressionMultiplication() throws Exception {
        String expr = "2*-2";
        double expected = -4d;
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(), 0d);
    }

    @Test
    public void testExpression22() throws Exception {
        String expr = "-2 *33.34/log(x)^-2 + 14 *6";
        double x = 1.334d;
        double expected = -2 * 33.34 / Math.pow(Math.log(x), -2) + 14 * 6;
        Expression e = new ExpressionBuilder(expr)
                .variables("x")
                .build()
                .setVariable("x", x);
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testExpression23() throws Exception {
        String expr = "-2 *33.34/(log(foo)^-2 + 14 *6) - sin(foo)";
        double x = 1.334d;
        double expected = -2 * 33.34 / (Math.pow(Math.log(x), -2) + 14 * 6) - Math.sin(x);
        Expression e = new ExpressionBuilder(expr)
                .variables("foo")
                .build()
                .setVariable("foo", x);
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testExpression24() throws Exception {
        String expr = "3+4-log(23.2)^(2-1) * -1";
        double expected = 3 + 4 - Math.pow(Math.log(23.2), (2 - 1)) * -1;
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testExpression25() throws Exception {
        String expr = "+3+4-+log(23.2)^(2-1) * + 1";
        double expected = 3 + 4 - Math.log(23.2d);
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testExpression26() throws Exception {
        String expr = "14 + -(1 / 2.22^3)";
        double expected = 14 + -(1d / Math.pow(2.22d, 3d));
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testExpression27() throws Exception {
        String expr = "12^-+-+-+-+-+-+---2";
        double expected = Math.pow(12, -2);
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testExpression28() throws Exception {
        String expr = "12^-+-+-+-+-+-+---2 * (-14) / 2 ^ -log(2.22323) ";
        double expected = Math.pow(12, -2) * -14 / Math.pow(2, -Math.log(2.22323));
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testExpression29() throws Exception {
        String expr = "24.3343 % 3";
        double expected = 24.3343 % 3;
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(expected == e.evaluate());
    }

    @Test(expected = Exception.class)
    public void testVarname1() throws Exception {
        String expr = "12.23 * foo.bar";
        Expression e = new ExpressionBuilder(expr)
                .variables("foo.bar")
                .build()
                .setVariable("foo.bar", 1d);
        assertTrue(12.23 == e.evaluate());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMisplacedSeparator() throws Exception {
        String expr = "12.23 * ,foo";
        Expression e = new ExpressionBuilder(expr)
                .build()
                .setVariable(",foo", 1d);
        assertTrue(12.23 == e.evaluate());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidVarname() throws Exception {
        String expr = "12.23 * @foo";
        Expression e = new ExpressionBuilder(expr)
                .build()
                .setVariable("@foo", 1d);
        assertTrue(12.23 == e.evaluate());
    }

    @Test
    public void testVarMap() throws Exception {
        String expr = "12.23 * foo - bar";
        Map<String, Double> variables = new HashMap<>();
        variables.put("foo", 2d);
        variables.put("bar", 3.3d);
        Expression e = new ExpressionBuilder(expr)
                .variables(variables.keySet())
                .build()
                .setVariables(variables);
        assertTrue(12.23d * 2d - 3.3d == e.evaluate());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidNumberofArguments1() throws Exception {
        String expr = "log(2,2)";
        Expression e = new ExpressionBuilder(expr)
                .build();
        e.evaluate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidNumberofArguments2() throws Exception {
        Function avg = new Function("avg", 4) {

            @Override
            public double apply(double... args) {
                double sum = 0;
                for (double arg : args) {
                    sum += arg;
                }
                return sum / args.length;
            }
        };
        String expr = "avg(2,2)";
        Expression e = new ExpressionBuilder(expr)
                .build();
        e.evaluate();
    }

    @Test
    public void testExpression3() throws Exception {
        String expr;
        double expected;
        expr = "2+4*5";
        expected = 2 + 4 * 5;
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testExpression30() throws Exception {
        String expr = "24.3343 % 3 * 20 ^ -(2.334 % log(2 / 14))";
        double expected = 24.3343d % 3 * Math.pow(20, -(2.334 % Math.log(2d / 14d)));
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testExpression31() throws Exception {
        String expr = "-2 *33.34/log(y_x)^-2 + 14 *6";
        double x = 1.334d;
        double expected = -2 * 33.34 / Math.pow(Math.log(x), -2) + 14 * 6;
        Expression e = new ExpressionBuilder(expr)
                .variables("y_x")
                .build()
                .setVariable("y_x", x);
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testExpression32() throws Exception {
        String expr = "-2 *33.34/log(y_2x)^-2 + 14 *6";
        double x = 1.334d;
        double expected = -2 * 33.34 / Math.pow(Math.log(x), -2) + 14 * 6;
        Expression e = new ExpressionBuilder(expr)
                .variables("y_2x")
                .build()
                .setVariable("y_2x", x);
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testExpression33() throws Exception {
        String expr = "-2 *33.34/log(_y)^-2 + 14 *6";
        double x = 1.334d;
        double expected = -2 * 33.34 / Math.pow(Math.log(x), -2) + 14 * 6;
        Expression e = new ExpressionBuilder(expr)
                .variables("_y")
                .build()
                .setVariable("_y", x);
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testExpression34() throws Exception {
        String expr = "-2 + + (+4) +(4)";
        double expected = -2 + 4 + 4;
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testExpression40() throws Exception {
        String expr = "1e1";
        double expected = 10d;
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testExpression41() throws Exception {
        String expr = "1e-1";
        double expected = 0.1d;
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(expected == e.evaluate());
    }

    /*
     * Added tests for expressions with scientific notation see http://jira.congrace.de/jira/browse/EXP-17
     */
    @Test
    public void testExpression42() throws Exception {
        String expr = "7.2973525698e-3";
        double expected = 7.2973525698e-3d;
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testExpression43() throws Exception {
        String expr = "6.02214E23";
        double expected = 6.02214e23d;
        Expression e = new ExpressionBuilder(expr)
                .build();
        double result = e.evaluate();
        assertTrue(expected == result);
    }

    @Test
    public void testExpression44() throws Exception {
        String expr = "6.02214E23";
        double expected = 6.02214e23d;
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(expected == e.evaluate());
    }

    @Test(expected = NumberFormatException.class)
    public void testExpression45() throws Exception {
        String expr = "6.02214E2E3";
        new ExpressionBuilder(expr)
                .build();
    }

    @Test(expected = NumberFormatException.class)
    public void testExpression46() throws Exception {
        String expr = "6.02214e2E3";
        new ExpressionBuilder(expr)
                .build();
    }

    // tests for EXP-20: No exception is thrown for unmatched parenthesis in
    // build
    // Thanks go out to maheshkurmi for reporting
    @Test(expected = IllegalArgumentException.class)
    public void testExpression48() throws Exception {
        String expr = "(1*2";
        Expression e = new ExpressionBuilder(expr)
                .build();
        double result = e.evaluate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExpression49() throws Exception {
        String expr = "{1*2";
        Expression e = new ExpressionBuilder(expr)
                .build();
        double result = e.evaluate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExpression50() throws Exception {
        String expr = "[1*2";
        Expression e = new ExpressionBuilder(expr)
                .build();
        double result = e.evaluate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExpression51() throws Exception {
        String expr = "(1*{2+[3}";
        Expression e = new ExpressionBuilder(expr)
                .build();
        double result = e.evaluate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExpression52() throws Exception {
        String expr = "(1*(2+(3";
        Expression e = new ExpressionBuilder(expr)
                .build();
        double result = e.evaluate();
    }

    @Test
    public void testExpression53() throws Exception {
        String expr = "14 * 2x";
        Expression exp = new ExpressionBuilder(expr)
                .variables("x")
                .build();
        exp.setVariable("x", 1.5d);
        assertTrue(exp.validate().isValid());
        assertEquals(14d * 2d * 1.5d, exp.evaluate(), 0d);
    }

    @Test
    public void testExpression54() throws Exception {
        String expr = "2 ((-(x)))";
        Expression e = new ExpressionBuilder(expr)
                .variables("x")
                .build();
        e.setVariable("x", 1.5d);
        assertEquals(-3d, e.evaluate(), 0d);
    }

    @Test
    public void testExpression55() throws Exception {
        String expr = "2 sin(x)";
        Expression e = new ExpressionBuilder(expr)
                .variables("x")
                .build();
        e.setVariable("x", 2d);
        assertTrue(Math.sin(2d) * 2 == e.evaluate());
    }

    @Test
    public void testExpression56() throws Exception {
        String expr = "2 sin(3x)";
        Expression e = new ExpressionBuilder(expr)
                .variables("x")
                .build();
        e.setVariable("x", 2d);
        assertTrue(Math.sin(6d) * 2d == e.evaluate());
    }

    @Test
    public void testDocumentationExample1() throws Exception {
        Expression e = new ExpressionBuilder("3 * sin(y) - 2 / (x - 2)")
                .variables("x", "y")
                .build()
                .setVariable("x", 2.3)
                .setVariable("y", 3.14);
        double result = e.evaluate();
        double expected = 3 * Math.sin(3.14d) - 2d / (2.3d - 2d);
        assertEquals(expected, result, 0d);
    }

    @Test
    public void testDocumentationExample3() throws Exception {
        String expr = "7.2973525698e-3";
        double expected = Double.parseDouble(expr);
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertEquals(expected, e.evaluate(), 0d);
    }

    @Test(expected = ArithmeticException.class)
    public void testDocumentationExample4() throws Exception {
        Operator reciprocal = new Operator("$", 1, true, Operator.PRECEDENCE_DIVISION) {

            @Override
            public double apply(final double... args) {
                if (args[0] == 0d) {
                    throw new ArithmeticException("Division by zero!");
                }
                return 1d / args[0];
            }
        };
        new ExpressionBuilder("0$").operator(reciprocal)
                .build().evaluate();
    }

    @Test
    public void testDocumentationExample5() throws Exception {
        Function logb = new Function("logb", 2) {
            @Override
            public double apply(double... args) {
                return Math.log(args[0]) / Math.log(args[1]);
            }
        };
        double result = new ExpressionBuilder("logb(8, 2)")
                .function(logb)
                .build()
                .evaluate();
        double expected = 3;
        assertEquals(expected, result, 0d);
    }

    @Test
    public void testDocumentationExample6() throws Exception {
        double result = new ExpressionBuilder("2cos(xy)")
                .variables("x","y")
                .build()
                .setVariable("x", 0.5d)
                .setVariable("y", 0.25d)
                .evaluate();
        assertEquals(2d * cos(0.5d * 0.25d), result, 0d);
    }

    @Test
    public void testDocumentationExample7() throws Exception {
        Expression e = new ExpressionBuilder("x")
                .variable("x")
                .build();

        ValidationResult res = e.validate();
        assertFalse(res.isValid());
        assertEquals(1, res.getErrors().size());

        e.setVariable("x",1d);
        res = e.validate();
        assertTrue(res.isValid());
    }

    @Test
    public void testDocumentationExample8() throws Exception {
        Expression e = new ExpressionBuilder("x")
                .variable("x")
                .build();

        ValidationResult res = e.validate(false);
        assertTrue(res.isValid());
        assertNull(res.getErrors());

    }

    @Test
    public void testDocumentationExample2() throws Exception {
        ExecutorService exec = Executors.newFixedThreadPool(1);
        Expression e = new ExpressionBuilder("3log(y)/(x+1)")
                .variables("x", "y")
                .build()
                .setVariable("x", 2.3)
                .setVariable("y", 3.14);
        Future<Double> result = e.evaluateAsync(exec);
        double expected = 3 * Math.log(3.14d)/(3.3);
        assertEquals(expected, result.get(), 0d);
    }


    // Thanks go out to Johan Björk for reporting the division by zero problem EXP-22
    // https://www.objecthunter.net/jira/browse/EXP-22
    @Test(expected = ArithmeticException.class)
    public void testExpression57() throws Exception {
        String expr = "1 / 0";
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(Double.POSITIVE_INFINITY == e.evaluate());
    }

    @Test
    public void testExpression58() throws Exception {
        String expr = "17 * sqrt(-1) * 12";
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(Double.isNaN(e.evaluate()));
    }

    // Thanks go out to Alex Dolinsky for reporting the missing exception when an empty
    // expression is passed as in new ExpressionBuilder("")
    @Test(expected = IllegalArgumentException.class)
    public void testExpression59() throws Exception {
        Expression e = new ExpressionBuilder("")
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExpression60() throws Exception {
        Expression e = new ExpressionBuilder("   ")
                .build();
        e.evaluate();
    }

    @Test(expected = ArithmeticException.class)
    public void testExpression61() throws Exception {
        Expression e = new ExpressionBuilder("14 % 0")
                .build();
        e.evaluate();
    }

    // https://www.objecthunter.net/jira/browse/EXP-24
    // thanks go out to Rémi for the issue report
    @Test
    public void testExpression62() throws Exception {
        Expression e = new ExpressionBuilder("x*1.0e5+5")
                .variables("x")
                .build()
                .setVariable("x", Math.E);
        assertTrue(Math.E * 1.0 * Math.pow(10, 5) + 5 == e.evaluate());
    }

    @Test
    public void testExpression63() throws Exception {
        Expression e = new ExpressionBuilder("log10(5)")
                .build();
        assertEquals(Math.log10(5), e.evaluate(), 0d);
    }

    @Test
    public void testExpression64() throws Exception {
        Expression e = new ExpressionBuilder("log2(5)")
                .build();
        assertEquals(Math.log(5) / Math.log(2), e.evaluate(), 0d);
    }

    @Test
    public void testExpression65() throws Exception {
        Expression e = new ExpressionBuilder("2log(e)")
                .variables("e")
                .build()
                .setVariable("e", Math.E);

        assertEquals(2d, e.evaluate(), 0d);
    }

    @Test
    public void testExpression66() throws Exception {
        Expression e = new ExpressionBuilder("log(e)2")
                .variables("e")
                .build()
                .setVariable("e", Math.E);

        assertEquals(2d, e.evaluate(), 0d);
    }

    @Test
    public void testExpression67() throws Exception {
        Expression e = new ExpressionBuilder("2esin(pi/2)")
                .variables("e", "pi")
                .build()
                .setVariable("e", Math.E)
                .setVariable("pi", Math.PI);

        assertEquals(2 * Math.E * Math.sin(Math.PI / 2d), e.evaluate(), 0d);
    }

    @Test
    public void testExpression68() throws Exception {
        Expression e = new ExpressionBuilder("2x")
                .variables("x")
                .build()
                .setVariable("x", Math.E);
        assertEquals(2 * Math.E, e.evaluate(), 0d);
    }

    @Test
    public void testExpression69() throws Exception {
        Expression e = new ExpressionBuilder("2x2")
                .variables("x")
                .build()
                .setVariable("x", Math.E);
        assertEquals(4 * Math.E, e.evaluate(), 0d);
    }

    @Test
    public void testExpression70() throws Exception {
        Expression e = new ExpressionBuilder("2xx")
                .variables("x")
                .build()
                .setVariable("x", Math.E);
        assertEquals(2 * Math.E * Math.E, e.evaluate(), 0d);
    }

    @Test
    public void testExpression71() throws Exception {
        Expression e = new ExpressionBuilder("x2x")
                .variables("x")
                .build()
                .setVariable("x", Math.E);
        assertEquals(2 * Math.E * Math.E, e.evaluate(), 0d);
    }

    @Test
    public void testExpression72() throws Exception {
        Expression e = new ExpressionBuilder("2cos(x)")
                .variables("x")
                .build()
                .setVariable("x", Math.E);
        assertEquals(2 * Math.cos(Math.E), e.evaluate(), 0d);
    }

    @Test
    public void testExpression73() throws Exception {
        Expression e = new ExpressionBuilder("cos(x)2")
                .variables("x")
                .build()
                .setVariable("x", Math.E);
        assertEquals(2 * Math.cos(Math.E), e.evaluate(), 0d);
    }

    @Test
    public void testExpression74() throws Exception {
        Expression e = new ExpressionBuilder("cos(x)(-2)")
                .variables("x")
                .build()
                .setVariable("x", Math.E);
        assertEquals(-2d * Math.cos(Math.E), e.evaluate(), 0d);
    }

    @Test
    public void testExpression75() throws Exception {
        Expression e = new ExpressionBuilder("(-2)cos(x)")
                .variables("x")
                .build()
                .setVariable("x", Math.E);
        assertEquals(-2d * Math.cos(Math.E), e.evaluate(), 0d);
    }

    @Test
    public void testExpression76() throws Exception {
        Expression e = new ExpressionBuilder("(-x)cos(x)")
                .variables("x")
                .build()
                .setVariable("x", Math.E);
        assertEquals(-E * Math.cos(Math.E), e.evaluate(), 0d);
    }

    @Test
    public void testExpression77() throws Exception {
        Expression e = new ExpressionBuilder("(-xx)cos(x)")
                .variables("x")
                .build()
                .setVariable("x", Math.E);
        assertEquals(-E * E * Math.cos(Math.E), e.evaluate(), 0d);
    }

    @Test
    public void testExpression78() throws Exception {
        Expression e = new ExpressionBuilder("(xx)cos(x)")
                .variables("x")
                .build()
                .setVariable("x", Math.E);
        assertEquals(E * E * Math.cos(Math.E), e.evaluate(), 0d);
    }

    @Test
    public void testExpression79() throws Exception {
        Expression e = new ExpressionBuilder("cos(x)(xx)")
                .variables("x")
                .build()
                .setVariable("x", Math.E);
        assertEquals(E * E * Math.cos(Math.E), e.evaluate(), 0d);
    }

    @Test
    public void testExpression80() throws Exception {
        Expression e = new ExpressionBuilder("cos(x)(xy)")
                .variables("x", "y")
                .build()
                .setVariable("x", Math.E)
                .setVariable("y", Math.sqrt(2));
        assertEquals(sqrt(2) * E * Math.cos(Math.E), e.evaluate(), 0d);
    }

    @Test
    public void testExpression81() throws Exception {
        Expression e = new ExpressionBuilder("cos(xy)")
                .variables("x", "y")
                .build()
                .setVariable("x", Math.E)
                .setVariable("y", Math.sqrt(2));
        assertEquals(cos(sqrt(2) * E), e.evaluate(), 0d);
    }

    @Test
    public void testExpression82() throws Exception {
        Expression e = new ExpressionBuilder("cos(2x)")
                .variables("x")
                .build()
                .setVariable("x", Math.E);
        assertEquals(cos(2 * E), e.evaluate(), 0d);
    }

    @Test
    public void testExpression83() throws Exception {
        Expression e = new ExpressionBuilder("cos(xlog(xy))")
                .variables("x", "y")
                .build()
                .setVariable("x", Math.E)
                .setVariable("y", Math.sqrt(2));
        assertEquals(cos(E * log(E * sqrt(2))), e.evaluate(), 0d);
    }

    @Test
    public void testExpression84() throws Exception {
        Expression e = new ExpressionBuilder("3x_1")
                .variables("x_1")
                .build()
                .setVariable("x_1", Math.E);
        assertEquals(3d * E, e.evaluate(), 0d);
    }

    @Test
    public void testExpression85() throws Exception {
        Expression e = new ExpressionBuilder("1/2x")
                .variables("x")
                .build()
                .setVariable("x", 6);
        assertEquals(3d, e.evaluate(), 0d);
    }

    // thanks go out to Janny for providing the tests and the bug report
    @Test
    public void testUnaryMinusInParenthesisSpace() throws Exception {
        ExpressionBuilder b = new ExpressionBuilder("( -1)^2");
        double calculated = b.build().evaluate();
        assertTrue(calculated == 1d);
    }

    @Test
    public void testUnaryMinusSpace() throws Exception {
        ExpressionBuilder b = new ExpressionBuilder(" -1 + 2");
        double calculated = b.build().evaluate();
        assertTrue(calculated == 1d);
    }

    @Test
    public void testUnaryMinusSpaces() throws Exception {
        ExpressionBuilder b = new ExpressionBuilder(" -1 + + 2 +   -   1");
        double calculated = b.build().evaluate();
        assertTrue(calculated == 0d);
    }

    @Test
    public void testUnaryMinusSpace1() throws Exception {
        ExpressionBuilder b = new ExpressionBuilder("-1");
        double calculated = b.build().evaluate();
        assertTrue(calculated == -1d);
    }

    @Test
    public void testExpression4() throws Exception {
        String expr;
        double expected;
        expr = "2+4 * 5";
        expected = 2 + 4 * 5;
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testExpression5() throws Exception {
        String expr;
        double expected;
        expr = "(2+4)*5";
        expected = (2 + 4) * 5;
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testExpression6() throws Exception {
        String expr;
        double expected;
        expr = "(2+4)*5 + 2.5*2";
        expected = (2 + 4) * 5 + 2.5 * 2;
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testExpression7() throws Exception {
        String expr;
        double expected;
        expr = "(2+4)*5 + 10/2";
        expected = (2 + 4) * 5 + 10 / 2;
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testExpression8() throws Exception {
        String expr;
        double expected;
        expr = "(2 * 3 +4)*5 + 10/2";
        expected = (2 * 3 + 4) * 5 + 10 / 2;
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testExpression9() throws Exception {
        String expr;
        double expected;
        expr = "(2 * 3 +4)*5 +4 + 10/2";
        expected = (2 * 3 + 4) * 5 + 4 + 10 / 2;
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(expected == e.evaluate());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFailUnknownFunction1() throws Exception {
        String expr;
        expr = "lig(1)";
        Expression e = new ExpressionBuilder(expr)
                .build();
        e.evaluate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFailUnknownFunction2() throws Exception {
        String expr;
        expr = "galength(1)";
        new ExpressionBuilder(expr)
                .build().evaluate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFailUnknownFunction3() throws Exception {
        String expr;
        expr = "tcos(1)";
        Expression exp = new ExpressionBuilder(expr)
                .build();
        double result = exp.evaluate();
        System.out.println(result);
    }

    @Test
    public void testFunction22() throws Exception {
        String expr;
        expr = "cos(cos_1)";
        Expression e = new ExpressionBuilder(expr)
                .variables("cos_1")
                .build()
                .setVariable("cos_1", 1d);
        assertTrue(e.evaluate() == Math.cos(1d));
    }

    @Test
    public void testFunction23() throws Exception {
        String expr;
        expr = "log1p(1)";
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertEquals(log1p(1d), e.evaluate(), 0d);
    }

    @Test
    public void testFunction24() throws Exception {
        String expr;
        expr = "pow(3,3)";
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertEquals(27d, e.evaluate(), 0d);
    }

    @Test
    public void testPostfix1() throws Exception {
        String expr;
        double expected;
        expr = "2.2232^0.1";
        expected = Math.pow(2.2232d, 0.1d);
        double actual = new ExpressionBuilder(expr)
                .build().evaluate();
        assertTrue(expected == actual);
    }

    @Test
    public void testPostfixEverything() throws Exception {
        String expr;
        double expected;
        expr = "(sin(12) + log(34)) * 3.42 - cos(2.234-log(2))";
        expected = (Math.sin(12) + Math.log(34)) * 3.42 - Math.cos(2.234 - Math.log(2));
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testPostfixExponentation1() throws Exception {
        String expr;
        double expected;
        expr = "2^3";
        expected = Math.pow(2, 3);
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testPostfixExponentation2() throws Exception {
        String expr;
        double expected;
        expr = "24 + 4 * 2^3";
        expected = 24 + 4 * Math.pow(2, 3);
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testPostfixExponentation3() throws Exception {
        String expr;
        double expected;
        double x = 4.334d;
        expr = "24 + 4 * 2^x";
        expected = 24 + 4 * Math.pow(2, x);
        Expression e = new ExpressionBuilder(expr)
                .variables("x")
                .build()
                .setVariable("x", x);
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testPostfixExponentation4() throws Exception {
        String expr;
        double expected;
        double x = 4.334d;
        expr = "(24 + 4) * 2^log(x)";
        expected = (24 + 4) * Math.pow(2, Math.log(x));
        Expression e = new ExpressionBuilder(expr)
                .variables("x")
                .build()
                .setVariable("x", x);
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testPostfixFunction1() throws Exception {
        String expr;
        double expected;
        expr = "log(1) * sin(0)";
        expected = Math.log(1) * Math.sin(0);
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testPostfixFunction10() throws Exception {
        String expr;
        double expected;
        expr = "cbrt(x)";
        Expression e = new ExpressionBuilder(expr)
                .variables("x")
                .build();
        for (double x = -10; x < 10; x = x + 0.5d) {
            expected = Math.cbrt(x);
            assertTrue(expected == e.setVariable("x", x).evaluate());
        }
    }

    @Test
    public void testPostfixFunction11() throws Exception {
        String expr;
        double expected;
        expr = "cos(x) - (1/cbrt(x))";
        Expression e = new ExpressionBuilder(expr)
                .variables("x")
                .build();
        for (double x = -10; x < 10; x = x + 0.5d) {
            if (x == 0d) continue;
            expected = Math.cos(x) - (1 / Math.cbrt(x));
            assertTrue(expected == e.setVariable("x", x).evaluate());
        }
    }

    @Test
    public void testPostfixFunction12() throws Exception {
        String expr;
        double expected;
        expr = "acos(x) * expm1(asin(x)) - exp(atan(x)) + floor(x) + cosh(x) - sinh(cbrt(x))";
        Expression e = new ExpressionBuilder(expr)
                .variables("x")
                .build();
        for (double x = -10; x < 10; x = x + 0.5d) {
            expected =
                    Math.acos(x) * Math.expm1(Math.asin(x)) - Math.exp(Math.atan(x)) + Math.floor(x) + Math.cosh(x)
                            - Math.sinh(Math.cbrt(x));
            if (Double.isNaN(expected)) {
                assertTrue(Double.isNaN(e.setVariable("x", x).evaluate()));
            } else {
                assertTrue(expected == e.setVariable("x", x).evaluate());
            }
        }
    }

    @Test
    public void testPostfixFunction13() throws Exception {
        String expr;
        double expected;
        expr = "acos(x)";
        Expression e = new ExpressionBuilder(expr)
                .variables("x")
                .build();
        for (double x = -10; x < 10; x = x + 0.5d) {
            expected = Math.acos(x);
            if (Double.isNaN(expected)) {
                assertTrue(Double.isNaN(e.setVariable("x", x).evaluate()));
            } else {
                assertTrue(expected == e.setVariable("x", x).evaluate());
            }
        }
    }

    @Test
    public void testPostfixFunction14() throws Exception {
        String expr;
        double expected;
        expr = " expm1(x)";
        Expression e = new ExpressionBuilder(expr)
                .variables("x")
                .build();
        for (double x = -10; x < 10; x = x + 0.5d) {
            expected = Math.expm1(x);
            if (Double.isNaN(expected)) {
                assertTrue(Double.isNaN(e.setVariable("x", x).evaluate()));
            } else {
                assertTrue(expected == e.setVariable("x", x).evaluate());
            }
        }
    }

    @Test
    public void testPostfixFunction15() throws Exception {
        String expr;
        double expected;
        expr = "asin(x)";
        Expression e = new ExpressionBuilder(expr)
                .variables("x")
                .build();
        for (double x = -10; x < 10; x = x + 0.5d) {
            expected = Math.asin(x);
            if (Double.isNaN(expected)) {
                assertTrue(Double.isNaN(e.setVariable("x", x).evaluate()));
            } else {
                assertTrue(expected == e.setVariable("x", x).evaluate());
            }
        }
    }

    @Test
    public void testPostfixFunction16() throws Exception {
        String expr;
        double expected;
        expr = " exp(x)";
        Expression e = new ExpressionBuilder(expr)
                .variables("x")
                .build();
        for (double x = -10; x < 10; x = x + 0.5d) {
            expected = Math.exp(x);
            assertTrue(expected == e.setVariable("x", x).evaluate());
        }
    }

    @Test
    public void testPostfixFunction17() throws Exception {
        String expr;
        double expected;
        expr = "floor(x)";
        Expression e = new ExpressionBuilder(expr)
                .variables("x")
                .build();
        for (double x = -10; x < 10; x = x + 0.5d) {
            expected = Math.floor(x);
            assertTrue(expected == e.setVariable("x", x).evaluate());
        }
    }

    @Test
    public void testPostfixFunction18() throws Exception {
        String expr;
        double expected;
        expr = " cosh(x)";
        Expression e = new ExpressionBuilder(expr)
                .variables("x")
                .build();
        for (double x = -10; x < 10; x = x + 0.5d) {
            expected = Math.cosh(x);
            assertTrue(expected == e.setVariable("x", x).evaluate());
        }
    }

    @Test
    public void testPostfixFunction19() throws Exception {
        String expr;
        double expected;
        expr = "sinh(x)";
        Expression e = new ExpressionBuilder(expr)
                .variables("x")
                .build();
        for (double x = -10; x < 10; x = x + 0.5d) {
            expected = Math.sinh(x);
            assertTrue(expected == e.setVariable("x", x).evaluate());
        }
    }

    @Test
    public void testPostfixFunction20() throws Exception {
        String expr;
        double expected;
        expr = "cbrt(x)";
        Expression e = new ExpressionBuilder(expr)
                .variables("x")
                .build();
        for (double x = -10; x < 10; x = x + 0.5d) {
            expected = Math.cbrt(x);
            assertTrue(expected == e.setVariable("x", x).evaluate());
        }
    }

    @Test
    public void testPostfixFunction21() throws Exception {
        String expr;
        double expected;
        expr = "tanh(x)";
        Expression e = new ExpressionBuilder(expr)
                .variables("x")
                .build();
        for (double x = -10; x < 10; x = x + 0.5d) {
            expected = Math.tanh(x);
            assertTrue(expected == e.setVariable("x", x).evaluate());
        }
    }

    @Test
    public void testPostfixFunction2() throws Exception {
        String expr;
        double expected;
        expr = "log(1)";
        expected = 0d;
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testPostfixFunction3() throws Exception {
        String expr;
        double expected;
        expr = "sin(0)";
        expected = 0d;
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testPostfixFunction5() throws Exception {
        String expr;
        double expected;
        expr = "ceil(2.3) +1";
        expected = Math.ceil(2.3) + 1;
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testPostfixFunction6() throws Exception {
        String expr;
        double expected;
        double x = 1.565d;
        double y = 2.1323d;
        expr = "ceil(x) + 1 / y * abs(1.4)";
        expected = Math.ceil(x) + 1 / y * Math.abs(1.4);
        Expression e = new ExpressionBuilder(expr)
                .variables("x", "y")
                .build();
        assertTrue(expected == e.setVariable("x", x)
                .setVariable("y", y).evaluate());
    }

    @Test
    public void testPostfixFunction7() throws Exception {
        String expr;
        double expected;
        double x = Math.E;
        expr = "tan(x)";
        expected = Math.tan(x);
        Expression e = new ExpressionBuilder(expr)
                .variables("x")
                .build();
        assertTrue(expected == e.setVariable("x", x).evaluate());
    }

    @Test
    public void testPostfixFunction8() throws Exception {
        String expr;
        double expected;
        double varE = Math.E;
        expr = "2^3.4223232 + tan(e)";
        expected = Math.pow(2, 3.4223232d) + Math.tan(Math.E);
        Expression e = new ExpressionBuilder(expr)
                .variables("e")
                .build();
        assertTrue(expected == e.setVariable("e", varE).evaluate());
    }

    @Test
    public void testPostfixFunction9() throws Exception {
        String expr;
        double expected;
        double x = Math.E;
        expr = "cbrt(x)";
        expected = Math.cbrt(x);
        Expression e = new ExpressionBuilder(expr)
                .variables("x")
                .build();
        assertTrue(expected == e.setVariable("x", x).evaluate());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPostfixInvalidVariableName() throws Exception {
        String expr;
        double expected;
        double x = 4.5334332d;
        double log = Math.PI;
        expr = "x * pi";
        expected = x * log;
        Expression e = new ExpressionBuilder(expr)
                .variables("x", "pi")
                .build();
        assertTrue(expected == e.setVariable("x", x)
                .setVariable("log", log).evaluate());
    }

    @Test
    public void testPostfixParanthesis() throws Exception {
        String expr;
        double expected;
        expr = "(3 + 3 * 14) * (2 * (24-17) - 14)/((34) -2)";
        expected = (3 + 3 * 14) * (2 * (24 - 17) - 14) / ((34) - 2);
        Expression e = new ExpressionBuilder(expr)
                .build();
        assertTrue(expected == e.evaluate());
    }

    @Test
    public void testPostfixVariables() throws Exception {
        String expr;
        double expected;
        double x = 4.5334332d;
        double pi = Math.PI;
        expr = "x * pi";
        expected = x * pi;
        Expression e = new ExpressionBuilder(expr)
                .variables("x", "pi")
                .build();
        assertTrue(expected == e.setVariable("x", x)
                .setVariable("pi", pi).evaluate());
    }
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

