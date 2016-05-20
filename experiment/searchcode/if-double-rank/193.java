// --- BEGIN LICENSE BLOCK ---
/* 
 * Copyright (c) 2009, Mikio L. Braun
 * Copyright (c) 2008, Johannes Schaback
 * Copyright (c) 2009, Jan Saputra M?ller
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 * 
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 * 
 *     * Neither the name of the Technische Universit?t Berlin nor the
 *       names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior
 *       written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
// --- END LICENSE BLOCK ---
package org.jblas;

import org.jblas.exceptions.SizeException;
import org.jblas.ranges.Range;
import org.jblas.util.Random;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A general matrix class for <tt>double</tt> typed values.
 * 
 * Don't be intimidated by the large number of methods this function defines. Most
 * are overloads provided for ease of use. For example, for each arithmetic operation,
 * up to six overloaded versions exist to handle in-place computations, and
 * scalar arguments.
 * 
 * <h3>Construction</h3>
 * 
 * <p>To construct a two-dimensional matrices, you can use the following constructors
 * and static methods.</p>
 * 
 * <table class="my">
 * <tr><th>Method<th>Description
 * <tr><td>DoubleMatrix(m,n, [value1, value2, value3...])<td>Values are filled in row by row.
 * <tr><td>DoubleMatrix(new double[][] {{value1, value2, ...}, ...}<td>Inner arrays are columns.
 * <tr><td>DoubleMatrix.zeros(m,n) <td>Initial values set to 0.0.
 * <tr><td>DoubleMatrix.ones(m,n) <td>Initial values set to 1.0.
 * <tr><td>DoubleMatrix.rand(m,n) <td>Values drawn at random between 0.0 and 1.0.
 * <tr><td>DoubleMatrix.randn(m,n) <td>Values drawn from normal distribution.
 * <tr><td>DoubleMatrix.eye(n) <td>Unit matrix (values 0.0 except for 1.0 on the diagonal).
 * <tr><td>DoubleMatrix.diag(array) <td>Diagonal matrix with given diagonal elements.
 * </table>
 * 
 * <p>Alternatively, you can construct (column) vectors, if you just supply the length
 * using the following constructors and static methods.</p>
 * 
 * <table class="my">
 * <tr><th>Method<th>Description
 * <tr><td>DoubleMatrix(m)<td>Constructs a column vector.
 * <tr><td>DoubleMatrix(new double[] {value1, value2, ...})<td>Constructs a column vector.
 * <tr><td>DoubleMatrix.zeros(m) <td>Initial values set to 0.0.
 * <tr><td>DoubleMatrix.ones(m) <td>Initial values set to 1.0.
 * <tr><td>DoubleMatrix.rand(m) <td>Values drawn at random between 0.0 and 1.0.
 * <tr><td>DoubleMatrix.randn(m) <td>Values drawn from normal distribution.
 * </table>
 * 
 * <p>You can also construct new matrices by concatenating matrices either horziontally
 * or vertically:</p>
 * 
 * <table class="my">
 * <tr><th>Method<th>Description
 * <tr><td>x.concatHorizontally(y)<td>New matrix will be x next to y.
 * <tr><td>x.concatVertically(y)<td>New matrix will be x atop y.
 * </table>
 * 
 * <h3>Element Access, Copying and Duplication</h3>
 * 
 * <p>To access individual elements, or whole rows and columns, use the following
 * methods:<p>
 * 
 * <table class="my">
 * <tr><th>x.Method<th>Description
 * <tr><td>x.get(i,j)<td>Get element in row i and column j.
 * <tr><td>x.put(i, j, v)<td>Set element in row i and column j to value v
 * <tr><td>x.get(i)<td>Get the ith element of the matrix (traversing rows first).
 * <tr><td>x.put(i, v)<td>Set the ith element of the matrix (traversing rows first).
 * <tr><td>x.getColumn(i)<td>Get a copy of column i.
 * <tr><td>x.putColumn(i, c)<td>Put matrix c into column i.
 * <tr><td>x.getRow(i)<td>Get a copy of row i.
 * <tr><td>x.putRow(i, c)<td>Put matrix c into row i.
 * <tr><td>x.swapColumns(i, j)<td>Swap the contents of columns i and j.
 * <tr><td>x.swapRows(i, j)<td>Swap the contents of columns i and j.
 * </table>
 * 
 * <p>For <tt>get</tt> and <tt>put</tt>, you can also pass integer arrays,
 * DoubleMatrix objects, or Range objects, which then specify the indices used 
 * as follows:
 * 
 * <ul>
 * <li><em>integer array:</em> the elements will be used as indices.
 * <li><em>DoubleMatrix object:</em> non-zero entries specify the indices.
 * <li><em>Range object:</em> see below.
 * </ul>
 * 
 * <p>When using <tt>put</tt> with multiple indices, the assigned object must
 * have the correct size or be a scalar.</p>
 *
 * <p>There exist the following Range objects. The Class <tt>RangeUtils</tt> also
 * contains the a number of handy helper methods for constructing these ranges.</p>
 * <table class="my">
 * <tr><th>Class <th>RangeUtils method <th>Indices
 * <tr><td>AllRange <td>all() <td>All legal indices.
 * <tr><td>PointRange <td>point(i) <td> A single point.
 * <tr><td>IntervalRange <td>interval(a, b)<td> All indices from a to b (inclusive)
 * <tr><td rowspan=3>IndicesRange <td>indices(int[])<td> The specified indices.
 * <tr><td>indices(DoubleMatrix)<td>The specified indices.
 * <tr><td>find(DoubleMatrix)<td>The non-zero entries of the matrix.
 * </table>
 * 
 * <p>The following methods can be used for duplicating and copying matrices.</p>
 * 
 * <table class="my">
 * <tr><th>Method<th>Description
 * <tr><td>x.dup()<td>Get a copy of x.
 * <tr><td>x.copy(y)<td>Copy the contents of y to x (possible resizing x).
 * </table>
 *    
 * <h3>Size and Shape</h3>
 * 
 * <p>The following methods permit to acces the size of a matrix and change its size or shape.</p>
 * 
 * <table class="my">
 * <tr><th>x.Method<th>Description
 * <tr><td>x.rows<td>Number of rows.
 * <tr><td>x.columns<td>Number of columns.
 * <tr><td>x.length<td>Total number of elements.
 * <tr><td>x.isEmpty()<td>Checks whether rows == 0 and columns == 0.
 * <tr><td>x.isRowVector()<td>Checks whether rows == 1.
 * <tr><td>x.isColumnVector()<td>Checks whether columns == 1.
 * <tr><td>x.isVector()<td>Checks whether rows == 1 or columns == 1.
 * <tr><td>x.isSquare()<td>Checks whether rows == columns.
 * <tr><td>x.isScalar()<td>Checks whether length == 1.
 * <tr><td>x.resize(r, c)<td>Resize the matrix to r rows and c columns, discarding the content.
 * <tr><td>x.reshape(r, c)<td>Resize the matrix to r rows and c columns.<br> Number of elements must not change.
 * </table>
 * 
 * <p>The size is stored in the <tt>rows</tt> and <tt>columns</tt> member variables.
 * The total number of elements is stored in <tt>length</tt>. Do not change these
 * values unless you know what you're doing!</p>
 * 
 * <h3>Arithmetics</h3>
 * 
 * <p>The usual arithmetic operations are implemented. Each operation exists in a
 * in-place version, recognizable by the suffix <tt>"i"</tt>, to which you can supply
 * the result matrix (or <tt>this</tt> is used, if missing). Using in-place operations
 * can also lead to a smaller memory footprint, as the number of temporary objects
 * which are directly garbage collected again is reduced.</p>
 * 
 * <p>Whenever you specify a result vector, the result vector must already have the
 * correct dimensions.</p>
 * 
 * <p>For example, you can add two matrices using the <tt>add</tt> method. If you want
 * to store the result in of <tt>x + y</tt> in <tt>z</tt>, type
 * <span class=code>
 * x.addi(y, z)   // computes x = y + z.
 * </span>
 * Even in-place methods return the result, such that you can easily chain in-place methods,
 * for example:
 * <span class=code>
 * x.addi(y).addi(z) // computes x += y; x += z
 * </span></p> 
 *
 * <p>Methods which operate element-wise only make sure that the length of the matrices
 * is correct. Therefore, you can add a 3 * 3 matrix to a 1 * 9 matrix, for example.</p>
 * 
 * <p>Finally, there exist versions which take doubles instead of DoubleMatrix Objects
 * as arguments. These then compute the operation with the same value as the
 * right-hand-side. The same effect can be achieved by passing a DoubleMatrix with
 * exactly one element.</p>
 * 
 * <table class="my">
 * <tr><th>Operation <th>Method <th>Comment
 * <tr><td>x + y <td>x.add(y)			<td>
 * <tr><td>x - y <td>x.sub(y), y.rsub(x) <td>rsub subtracts left from right hand side
 * <tr><td rowspan=3>x * y 	<td>x.mul(y) <td>element-wise multiplication 
 * <tr>						<td>x.mmul(y)<td>matrix-matrix multiplication
 * <tr>						<td>x.dot(y) <td>scalar-product
 * <tr><td>x / y <td>x.div(y), y.rdiv(x) <td>rdiv divides right hand side by left hand side.
 * <tr><td>- x	<td>x.neg()				<td>
 * </table>
 * 
 * <p>There also exist operations which work on whole columns or rows.</p>
 * 
 * <table class="my">
 * <tr><th>Method <th>Description
 * <tr><td>x.addRowVector<td>adds a vector to each row (addiRowVector works in-place)
 * <tr><td>x.addColumnVector<td>adds a vector to each column
 * <tr><td>x.subRowVector<td>subtracts a vector from each row
 * <tr><td>x.subColumnVector<td>subtracts a vector from each column
 * <tr><td>x.mulRow<td>Multiplies a row by a scalar
 * <tr><td>x.mulColumn<td>multiplies a row by a column
 * </table>
 * 
 * <p>In principle, you could achieve the same result by first calling getColumn(), 
 * adding, and then calling putColumn, but these methods are much faster.</p>
 * 
 * <p>The following comparison operations are available</p>
 *  
 * <table class="my">
 * <tr><th>Operation <th>Method
 * <tr><td>x &lt; y		<td>x.lt(y)
 * <tr><td>x &lt;= y	<td>x.le(y)
 * <tr><td>x &gt; y		<td>x.gt(y)
 * <tr><td>x &gt;= y	<td>x.ge(y)
 * <tr><td>x == y		<td>x.eq(y)
 * <tr><td>x != y		<td>x.ne(y)
 * </table>
 *
 * <p> Logical operations are also supported. For these operations, a value different from
 * zero is treated as "true" and zero is treated as "false". All operations are carried
 * out elementwise.</p>
 * 
 * <table class="my">
 * <tr><th>Operation <th>Method
 * <tr><td>x & y 	<td>x.and(y)
 * <tr><td>x | y 	<td>x.or(y)
 * <tr><td>x ^ y	<td>x.xor(y)
 * <tr><td>! x		<td>x.not()
 * </table>
 * 
 * <p>Finally, there are a few more methods to compute various things:</p>
 * 
 * <table class="my">
 * <tr><th>Method <th>Description
 * <tr><td>x.max() <td>Return maximal element
 * <tr><td>x.argmax() <td>Return index of largest element
 * <tr><td>x.min() <td>Return minimal element
 * <tr><td>x.argmin() <td>Return index of largest element
 * <tr><td>x.columnMins() <td>Return column-wise minima
 * <tr><td>x.columnArgmins() <td>Return column-wise index of minima
 * <tr><td>x.columnMaxs() <td>Return column-wise maxima
 * <tr><td>x.columnArgmaxs() <td>Return column-wise index of maxima
 * </table>
 * 
 * @author Mikio Braun, Johannes Schaback
 */
public class DoubleMatrix implements Serializable {

    /** Number of rows. */
    public int rows;
    /** Number of columns. */
    public int columns;
    /** Total number of elements (for convenience). */
    public int length;
    /** The actual data stored by rows (that is, row 0, row 1...). */
    public double[] data = null; // rows are contiguous
    public static final DoubleMatrix EMPTY = new DoubleMatrix();
    static final long serialVersionUID = -1249281332731183060L;

    /**************************************************************************
     *
     * Constructors and factory functions
     *
     **************************************************************************/
    /** Create a new matrix with <i>newRows</i> rows, <i>newColumns</i> columns
     * using <i>newData></i> as the data. The length of the data is not checked!
     */
    public DoubleMatrix(int newRows, int newColumns, double... newData) {
        rows = newRows;
        columns = newColumns;
        length = rows * columns;

        if (newData != null && newData.length != newRows * newColumns) {
            throw new IllegalArgumentException(
                    "Passed data must match matrix dimensions.");
        }

        data = newData;
        //System.err.printf("%d * %d matrix created\n", rows, columns);
    }

    /**
     * Creates a new <i>n</i> times <i>m</i> <tt>DoubleMatrix</tt>.
     * @param newRows the number of rows (<i>n</i>) of the new matrix.
     * @param newColumns the number of columns (<i>m</i>) of the new matrix.
     */
    public DoubleMatrix(int newRows, int newColumns) {
        this(newRows, newColumns, new double[newRows * newColumns]);
    }

    /**
     * Creates a new <tt>DoubleMatrix</tt> of size 0 times 0.
     */
    public DoubleMatrix() {
        this(0, 0, (double[]) null);
    }

    /**
     * Create a Matrix of length <tt>len</tt>. By default, this creates a row vector.
     * @param len
     */
    public DoubleMatrix(int len) {
        this(len, 1, new double[len]);
    }

    public DoubleMatrix(double[] newData) {
        this(newData.length);
        data = newData;
    }

    /**
     * Creates a new matrix by reading it from a file.
     * @param filename the path and name of the file to read the matrix from
     * @throws IOException
     */
    public DoubleMatrix(String filename) throws IOException {
        load(filename);
    }

    /**
     * Creates a new <i>n</i> times <i>m</i> <tt>DoubleMatrix</tt> from
     * the given <i>n</i> times <i>m</i> 2D data array. The first dimension of the array makes the
     * rows (<i>n</i>) and the second dimension the columns (<i>m</i>). For example, the
     * given code <br/><br/>
     * <code>new DoubleMatrix(new double[][]{{1d, 2d, 3d}, {4d, 5d, 6d}, {7d, 8d, 9d}}).print();</code><br/><br/>
     * will constructs the following matrix:
     * <pre>
     * 1.0	2.0	3.0
     * 4.0	5.0	6.0
     * 7.0	8.0	9.0
     * </pre>.
     * @param data <i>n</i> times <i>m</i> data array
     */
    public DoubleMatrix(double[][] data) {
        this(data.length, data[0].length);

        for (int r = 0; r < rows; r++) {
            assert (data[r].length == columns);
        }

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                put(r, c, data[r][c]);
            }
        }
    }

    public DoubleMatrix(List<Double> data) {
        this(data.size());

        int c = 0;
        for (java.lang.Double d : data) {
            put(c++, d);
        }
    }

    /**
     * Construct DoubleMatrix from ASCII representation.
     *
     * This is not very fast, but can be quiet useful when
     * you want to "just" construct a matrix, for example
     * when testing.
     *
     * The format is semicolon separated rows of space separated values,
     * for example "1 2 3; 4 5 6; 7 8 9".
     */
    public static DoubleMatrix valueOf(String text) {
        String[] rowValues = text.split(";");

        // process first line
        String[] columnValues = rowValues[0].trim().split("\\s+");

        DoubleMatrix result = null;

        // process rest
        for (int r = 0; r < rowValues.length; r++) {
            columnValues = rowValues[r].trim().split("\\s+");

            if (r == 0) {
                result = new DoubleMatrix(rowValues.length, columnValues.length);
            }

            for (int c = 0; c < columnValues.length; c++) {
                result.put(r, c, Double.valueOf(columnValues[c]));
            }
        }

        return result;
    }

    /**
     * Serialization
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
    }

    /** Create matrix with random values uniformly in 0..1. */
    public static DoubleMatrix rand(int rows, int columns) {
        DoubleMatrix m = new DoubleMatrix(rows, columns);

        for (int i = 0; i < rows * columns; i++) {
            m.data[i] = (double) Random.nextDouble();
        }

        return m;
    }

    /** Creates a row vector with random values uniformly in 0..1. */
    public static DoubleMatrix rand(int len) {
        return rand(len, 1);
    }

    /** Create matrix with normally distributed random values. */
    public static DoubleMatrix randn(int rows, int columns) {
        DoubleMatrix m = new DoubleMatrix(rows, columns);

        for (int i = 0; i < rows * columns; i++) {
            m.data[i] = (double) Random.nextGaussian();
        }

        return m;
    }

    /** Create row vector with normally distributed random values. */
    public static DoubleMatrix randn(int len) {
        return randn(len, 1);
    }

    /** Creates a new matrix in which all values are equal 0. */
    public static DoubleMatrix zeros(int rows, int columns) {
        return new DoubleMatrix(rows, columns);
    }

    /** Creates a row vector of given length. */
    public static DoubleMatrix zeros(int length) {
        return zeros(length, 1);
    }

    /** Creates a new matrix in which all values are equal 1. */
    public static DoubleMatrix ones(int rows, int columns) {
        DoubleMatrix m = new DoubleMatrix(rows, columns);

        for (int i = 0; i < rows * columns; i++) {
            m.put(i, 1.0);
        }

        return m;
    }

    /** Creates a row vector with all elements equal to 1. */
    public static DoubleMatrix ones(int length) {
        return ones(length, 1);
    }

    /** Construct a new n-by-n identity matrix. */
    public static DoubleMatrix eye(int n) {
        DoubleMatrix m = new DoubleMatrix(n, n);

        for (int i = 0; i < n; i++) {
            m.put(i, i, 1.0);
        }

        return m;
    }

    /**
     * Creates a new matrix where the values of the given vector are the diagonal values of
     * the matrix.
     */
    public static DoubleMatrix diag(DoubleMatrix x) {
        DoubleMatrix m = new DoubleMatrix(x.length, x.length);

        for (int i = 0; i < x.length; i++) {
            m.put(i, i, x.get(i));
        }

        return m;
    }

    /**
     * Create a 1-by-1 matrix. For many operations, this matrix functions like a
     * normal double.
     */
    public static DoubleMatrix scalar(double s) {
        DoubleMatrix m = new DoubleMatrix(1, 1);
        m.put(0, 0, s);
        return m;
    }

    /** Test whether a matrix is scalar. */
    public boolean isScalar() {
        return length == 1;
    }

    /** Return the first element of the matrix. */
    public double scalar() {
        return get(0);
    }

    public static DoubleMatrix logspace(double lower, double upper, int size) {
        DoubleMatrix result = new DoubleMatrix(size);
        for (int i = 0; i < size; i++) {
            double t = (double) i / (size - 1);
            double e = lower * (1 - t) + t * upper;
            result.put(i, (double) Math.pow(10.0, e));
        }
        return result;
    }

    public static DoubleMatrix linspace(int lower, int upper, int size) {
        DoubleMatrix result = new DoubleMatrix(size);
        for (int i = 0; i < size; i++) {
            double t = (double) i / (size - 1);
            result.put(i, lower * (1 - t) + t * upper);
        }
        return result;
    }

    /**
     * Concatenates two matrices horizontally. Matrices must have identical
     * numbers of rows.
     */
    public static DoubleMatrix concatHorizontally(DoubleMatrix A, DoubleMatrix B) {
        if (A.rows != B.rows) {
            throw new SizeException("Matrices don't have same number of rows.");
        }

        DoubleMatrix result = new DoubleMatrix(A.rows, A.columns + B.columns);
        SimpleBlas.copy(A, result);
        JavaBlas.rcopy(B.length, B.data, 0, 1, result.data, A.length, 1);
        return result;
    }

    /**
     * Concatenates two matrices vertically. Matrices must have identical
     * numbers of columns.
     */
    public static DoubleMatrix concatVertically(DoubleMatrix A, DoubleMatrix B) {
        if (A.columns != B.columns) {
            throw new SizeException("Matrices don't have same number of columns (" + A.columns + " != " + B.columns + ".");
        }

        DoubleMatrix result = new DoubleMatrix(A.rows + B.rows, A.columns);

        for (int i = 0; i < A.columns; i++) {
            JavaBlas.rcopy(A.rows, A.data, A.index(0, i), 1, result.data, result.index(0, i), 1);
            JavaBlas.rcopy(B.rows, B.data, B.index(0, i), 1, result.data, result.index(A.rows, i), 1);
        }

        return result;
    }

    /**************************************************************************
     * Working with slices (Man! 30+ methods just to make this a bit flexible...)
     */
    /** Get all elements specified by the linear indices. */
    public DoubleMatrix get(int[] indices) {
        DoubleMatrix result = new DoubleMatrix(indices.length);

        for (int i = 0; i < indices.length; i++) {
            result.put(i, get(indices[i]));
        }

        return result;
    }

    /** Get all elements for a given row and the specified columns. */
    public DoubleMatrix get(int r, int[] indices) {
        DoubleMatrix result = new DoubleMatrix(1, indices.length);

        for (int i = 0; i < indices.length; i++) {
            result.put(i, get(r, indices[i]));
        }

        return result;
    }

    /** Get all elements for a given column and the specified rows. */
    public DoubleMatrix get(int[] indices, int c) {
        DoubleMatrix result = new DoubleMatrix(indices.length, c);

        for (int i = 0; i < indices.length; i++) {
            result.put(i, get(indices[i], c));
        }

        return result;
    }

    /** Get all elements from the specified rows and columns. */
    public DoubleMatrix get(int[] rindices, int[] cindices) {
        DoubleMatrix result = new DoubleMatrix(rindices.length, cindices.length);

        for (int i = 0; i < rindices.length; i++) {
            for (int j = 0; j < cindices.length; j++) {
                result.put(i, j, get(rindices[i], cindices[j]));
            }
        }

        return result;
    }

    /** Get elements from specified rows and columns. */
    public DoubleMatrix get(Range rs, Range cs) {
        rs.init(0, rows);
        cs.init(0, columns);
        DoubleMatrix result = new DoubleMatrix(rs.length(), cs.length());

        for (; rs.hasMore(); rs.next()) {
            cs.init(0, columns);
            for (; cs.hasMore(); cs.next()) {
                result.put(rs.index(), cs.index(), get(rs.value(), cs.value()));
            }
        }

        return result;
    }

    public DoubleMatrix get(Range rs, int c) {
        rs.init(0, rows);
        DoubleMatrix result = new DoubleMatrix(rs.length(), 1);

        for (; rs.hasMore(); rs.next()) {
            result.put(rs.index(), 0, get(rs.value(), c));
        }

        return result;
    }

    public DoubleMatrix get(int r, Range cs) {
        cs.init(0, columns);
        DoubleMatrix result = new DoubleMatrix(1, cs.length());

        for (; cs.hasMore(); cs.next()) {
            result.put(0, cs.index(), get(r, cs.value()));
        }

        return result;

    }

    /** Get elements specified by the non-zero entries of the passed matrix. */
    public DoubleMatrix get(DoubleMatrix indices) {
        return get(indices.findIndices());
    }

    /**
     * Get elements from a row and columns as specified by the non-zero entries of
     * a matrix.
     */
    public DoubleMatrix get(int r, DoubleMatrix indices) {
        return get(r, indices.findIndices());
    }

    /**
     * Get elements from a column and rows as specified by the non-zero entries of
     * a matrix.
     */
    public DoubleMatrix get(DoubleMatrix indices, int c) {
        return get(indices.findIndices(), c);
    }

    /**
     * Get elements from columns and rows as specified by the non-zero entries of
     * the passed matrices.
     */
    public DoubleMatrix get(DoubleMatrix rindices, DoubleMatrix cindices) {
        return get(rindices.findIndices(), cindices.findIndices());
    }

    /** Return all elements with linear index a, a + 1, ..., b - 1.*/
    public DoubleMatrix getRange(int a, int b) {
        DoubleMatrix result = new DoubleMatrix(b - a);

        for (int k = 0; k < b - a; k++) {
            result.put(k, get(a + k));
        }

        return result;
    }

    /** Get elements from a row and columns <tt>a</tt> to <tt>b</tt>. */
    public DoubleMatrix getColumnRange(int r, int a, int b) {
        DoubleMatrix result = new DoubleMatrix(1, b - a);

        for (int k = 0; k < b - a; k++) {
            result.put(k, get(r, a + k));
        }

        return result;
    }

    /** Get elements from a column and rows <tt>a/tt> to <tt>b</tt>. */
    public DoubleMatrix getRowRange(int a, int b, int c) {
        DoubleMatrix result = new DoubleMatrix(b - a);

        for (int k = 0; k < b - a; k++) {
            result.put(k, get(a + k, c));
        }

        return result;
    }

    /**
     * Get elements from rows <tt>ra</tt> to <tt>rb</tt> and
     * columns <tt>ca</tt> to <tt>cb</tt>.
     */
    public DoubleMatrix getRange(int ra, int rb, int ca, int cb) {
        DoubleMatrix result = new DoubleMatrix(rb - ra, cb - ca);

        for (int i = 0; i < rb - ra; i++) {
            for (int j = 0; j < cb - ca; j++) {
                result.put(i, j, get(ra + i, ca + j));
            }
        }

        return result;
    }

    /** Get whole rows from the passed indices. */
    public DoubleMatrix getRows(int[] rindices) {
        DoubleMatrix result = new DoubleMatrix(rindices.length, columns);
        for (int i = 0; i < rindices.length; i++) {
            JavaBlas.rcopy(columns, data, index(rindices[i], 0), rows, result.data, result.index(i, 0), result.rows);
        }
        return result;
    }

    /** Get whole rows as specified by the non-zero entries of a matrix. */
    public DoubleMatrix getRows(DoubleMatrix rindices) {
        return getRows(rindices.findIndices());
    }

    public DoubleMatrix getRows(Range indices, DoubleMatrix result) {
        indices.init(0, rows);
        if (result.rows < indices.length()) {
            throw new SizeException("Result matrix does not have enough rows (" + result.rows + " < " + indices.length() + ")");
        }
        result.checkColumns(columns);

        for (int c = 0; c < columns; c++) {
            indices.init(0, rows);
            for (int r = 0; indices.hasMore(); indices.next(), r++) {
                result.put(r, c, get(indices.index(), c));
            }
        }
        return result;
    }

    public DoubleMatrix getRows(Range indices) {
        indices.init(0, rows);
        DoubleMatrix result = new DoubleMatrix(indices.length(), columns);
        return getRows(indices, result);
    }

    /** Get whole columns from the passed indices. */
    public DoubleMatrix getColumns(int[] cindices) {
        DoubleMatrix result = new DoubleMatrix(rows, cindices.length);
        for (int i = 0; i < cindices.length; i++) {
            JavaBlas.rcopy(rows, data, index(0, cindices[i]), 1, result.data, result.index(0, i), 1);
        }
        return result;
    }

    /** Get whole columns as specified by the non-zero entries of a matrix. */
    public DoubleMatrix getColumns(DoubleMatrix cindices) {
        return getColumns(cindices.findIndices());
    }

    /**
     * Assert that the matrix has a certain length.
     * @throws SizeException
     */
    public void checkLength(int l) {
        if (length != l) {
            throw new SizeException("Matrix does not have the necessary length (" + length + " != " + l + ").");
        }
    }

    /**
     * Asserts that the matrix has a certain number of rows.
     * @throws SizeException
     */
    public void checkRows(int r) {
        if (rows != r) {
            throw new SizeException("Matrix does not have the necessary number of rows (" + rows + " != " + r + ").");
        }
    }

    /**
     * Asserts that the amtrix has a certain number of columns.
     * @throws SizeException
     */
    public void checkColumns(int c) {
        if (columns != c) {
            throw new SizeException("Matrix does not have the necessary number of columns (" + columns + " != " + c + ").");
        }
    }


    /** Set elements in linear ordering in the specified indices. */
    public DoubleMatrix put(int[] indices, DoubleMatrix x) {
        if (x.isScalar()) {
            return put(indices, x.scalar());
        }
        x.checkLength(indices.length);

        for (int i = 0; i < indices.length; i++) {
            put(indices[i], x.get(i));
        }

        return this;
    }

    /** Set multiple elements in a row. */
    public DoubleMatrix put(int r, int[] indices, DoubleMatrix x) {
        if (x.isScalar()) {
            return put(r, indices, x.scalar());
        }
        x.checkColumns(indices.length);

        for (int i = 0; i < indices.length; i++) {
            put(r, indices[i], x.get(i));
        }

        return this;
    }

    /** Set multiple elements in a row. */
    public DoubleMatrix put(int[] indices, int c, DoubleMatrix x) {
        if (x.isScalar()) {
            return put(indices, c, x.scalar());
        }
        x.checkRows(indices.length);

        for (int i = 0; i < indices.length; i++) {
            put(indices[i], c, x.get(i));
        }

        return this;
    }

    /** Put a sub-matrix as specified by the indices. */
    public DoubleMatrix put(int[] rindices, int[] cindices, DoubleMatrix x) {
        if (x.isScalar()) {
            return put(rindices, cindices, x.scalar());
        }
        x.checkRows(rindices.length);
        x.checkColumns(cindices.length);

        for (int i = 0; i < rindices.length; i++) {
            for (int j = 0; j < cindices.length; j++) {
                put(rindices[i], cindices[j], x.get(i, j));
            }
        }

        return this;
    }

    /** Put a matrix into specified indices. */
    public DoubleMatrix put(Range rs, Range cs, DoubleMatrix x) {
        rs.init(0, rows);
        cs.init(0, columns);

        x.checkRows(rs.length());
        x.checkColumns(cs.length());

        for (; rs.hasMore(); rs.next()) {
            cs.init(0, columns);
            for (; cs.hasMore(); cs.next()) {
                put(rs.value(), cs.value(), x.get(rs.index(), cs.index()));
            }
        }

        return this;
    }

    /** Put a single value into the specified indices (linear adressing). */
    public DoubleMatrix put(int[] indices, double v) {
        for (int i = 0; i < indices.length; i++) {
            put(indices[i], v);
        }

        return this;
    }

    /** Put a single value into a row and the specified columns. */
    public DoubleMatrix put(int r, int[] indices, double v) {
        for (int i = 0; i < indices.length; i++) {
            put(r, indices[i], v);
        }

        return this;
    }

    /** Put a single value into the specified rows of a column. */
    public DoubleMatrix put(int[] indices, int c, double v) {
        for (int i = 0; i < indices.length; i++) {
            put(indices[i], c, v);
        }

        return this;
    }

    /** Put a single value into the specified rows and columns. */
    public DoubleMatrix put(int[] rindices, int[] cindices, double v) {
        for (int i = 0; i < rindices.length; i++) {
            for (int j = 0; j < cindices.length; j++) {
                put(rindices[i], cindices[j], v);
            }
        }

        return this;
    }

    /**
     * Put a sub-matrix into the indices specified by the non-zero entries
     * of <tt>indices</tt> (linear adressing).
     */
    public DoubleMatrix put(DoubleMatrix indices, DoubleMatrix v) {
        return put(indices.findIndices(), v);
    }

    /** Put a sub-vector into the specified columns (non-zero entries of <tt>indices</tt>) of a row. */
    public DoubleMatrix put(int r, DoubleMatrix indices, DoubleMatrix v) {
        return put(r, indices.findIndices(), v);
    }

    /** Put a sub-vector into the specified rows (non-zero entries of <tt>indices</tt>) of a column. */
    public DoubleMatrix put(DoubleMatrix indices, int c, DoubleMatrix v) {
        return put(indices.findIndices(), c, v);
    }

    /**
     * Put a sub-matrix into the specified rows and columns (non-zero entries of
     * <tt>rindices</tt> and <tt>cindices</tt>.
     */
    public DoubleMatrix put(DoubleMatrix rindices, DoubleMatrix cindices, DoubleMatrix v) {
        return put(rindices.findIndices(), cindices.findIndices(), v);
    }

    /**
     * Put a single value into the elements specified by the non-zero
     * entries of <tt>indices</tt> (linear adressing).
     */
    public DoubleMatrix put(DoubleMatrix indices, double v) {
        return put(indices.findIndices(), v);
    }

    /**
     * Put a single value into the specified columns (non-zero entries of
     * <tt>indices</tt>) of a row.
     */
    public DoubleMatrix put(int r, DoubleMatrix indices, double v) {
        return put(r, indices.findIndices(), v);
    }

    /**
     * Put a single value into the specified rows (non-zero entries of
     * <tt>indices</tt>) of a column.
     */
    public DoubleMatrix put(DoubleMatrix indices, int c, double v) {
        return put(indices.findIndices(), c, v);
    }

    /**
     * Put a single value in the specified rows and columns (non-zero entries
     * of <tt>rindices</tt> and <tt>cindices</tt>.
     */
    public DoubleMatrix put(DoubleMatrix rindices, DoubleMatrix cindices, double v) {
        return put(rindices.findIndices(), cindices.findIndices(), v);
    }

    /** Find the linear indices of all non-zero elements. */
    public int[] findIndices() {
        int len = 0;
        for (int i = 0; i < length; i++) {
            if (get(i) != 0.0) {
                len++;
            }
        }

        int[] indices = new int[len];
        int c = 0;

        for (int i = 0; i < length; i++) {
            if (get(i) != 0.0) {
                indices[c++] = i;
            }
        }

        return indices;
    }

    /**************************************************************************
     * Basic operations (copying, resizing, element access)
     */
    /** Return transposed copy of this matrix. */
    public DoubleMatrix transpose() {
        DoubleMatrix result = new DoubleMatrix(columns, rows);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result.put(j, i, get(i, j));
            }
        }

        return result;
    }

    /**
     * Compare two matrices. Returns true if and only if other is also a
     * DoubleMatrix which has the same size and the maximal absolute
     * difference in matrix elements is smaller thatn 1e-6.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DoubleMatrix)) {
            return false;
        }

        DoubleMatrix other = (DoubleMatrix) o;

        if (!sameSize(other)) {
            return false;
        }

        DoubleMatrix diff = MatrixFunctions.absi(sub(other));

        return diff.max() / (rows * columns) < 1e-6;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + this.rows;
        hash = 83 * hash + this.columns;
        hash = 83 * hash + Arrays.hashCode(this.data);
        return hash;
    }
    
    /** Resize the matrix. All elements will be set to zero. */
    public void resize(int newRows, int newColumns) {
        rows = newRows;
        columns = newColumns;
        length = newRows * newColumns;
        data = new double[rows * columns];
    }

    /** Reshape the matrix. Number of elements must not change. */
    public DoubleMatrix reshape(int newRows, int newColumns) {
        if (length != newRows * newColumns) {
            throw new IllegalArgumentException(
                    "Number of elements must not change.");
        }

        rows = newRows;
        columns = newColumns;

        return this;
    }

    /** Generate a new matrix which has the given number of replications of this. */
    public DoubleMatrix repmat(int rowMult, int columnMult) {
        DoubleMatrix result = new DoubleMatrix(rows * rowMult, columns * columnMult);

        for (int c = 0; c < columnMult; c++) {
            for (int r = 0; r < rowMult; r++) {
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < columns; j++) {
                        result.put(r * rows + i, c * columns + j, get(i, j));
                    }
                }
            }
        }
        return result;
    }

    /** Checks whether two matrices have the same size. */
    public boolean sameSize(DoubleMatrix a) {
        return rows == a.rows && columns == a.columns;
    }

    /** Throws SizeException unless two matrices have the same size. */
    public void assertSameSize(DoubleMatrix a) {
        if (!sameSize(a)) {
            throw new SizeException("Matrices must have the same size.");
        }
    }

    /** Checks whether two matrices can be multiplied (that is, number of columns of
     * this must equal number of rows of a. */
    public boolean multipliesWith(DoubleMatrix a) {
        return columns == a.rows;
    }

    /** Throws SizeException unless matrices can be multiplied with one another. */
    public void assertMultipliesWith(DoubleMatrix a) {
        if (!multipliesWith(a)) {
            throw new SizeException("Number of columns of left matrix must be equal to number of rows of right matrix.");
        }
    }

    /** Checks whether two matrices have the same length. */
    public boolean sameLength(DoubleMatrix a) {
        return length == a.length;
    }

    /** Throws SizeException unless matrices have the same length. */
    public void assertSameLength(DoubleMatrix a) {
        if (!sameLength(a)) {
            throw new SizeException("Matrices must have same length (is: " + length + " and " + a.length + ")");
        }
    }

    /** Copy DoubleMatrix a to this. this a is resized if necessary. */
    public DoubleMatrix copy(DoubleMatrix a) {
        if (!sameSize(a)) {
            resize(a.rows, a.columns);
        }

        System.arraycopy(a.data, 0, data, 0, length);
        return a;
    }

    /**
     * Returns a duplicate of this matrix. Geometry is the same (including offsets, transpose, etc.),
     * but the buffer is not shared.
     */
    public DoubleMatrix dup() {
        DoubleMatrix out = new DoubleMatrix(rows, columns);

        JavaBlas.rcopy(length, data, 0, 1, out.data, 0, 1);

        return out;
    }

    /** Swap two columns of a matrix. */
    public DoubleMatrix swapColumns(int i, int j) {
        NativeBlas.dswap(rows, data, index(0, i), 1, data, index(0, j), 1);
        return this;
    }

    /** Swap two rows of a matrix. */
    public DoubleMatrix swapRows(int i, int j) {
        NativeBlas.dswap(columns, data, index(i, 0), rows, data, index(j, 0), rows);
        return this;
    }

    /** Set matrix element */
    public DoubleMatrix put(int rowIndex, int columnIndex, double value) {
        data[index(rowIndex, columnIndex)] = value;
        return this;
    }

    /** Retrieve matrix element */
    public double get(int rowIndex, int columnIndex) {
        return data[index(rowIndex, columnIndex)];
    }

    /** Get index of an element */
    public int index(int rowIndex, int columnIndex) {
        return rowIndex + rows * columnIndex;
    }

    /** Compute the row index of a linear index. */
    public int indexRows(int i) {
        return i / rows;
    }

    /** Compute the column index of a linear index. */
    public int indexColumns(int i) {
        return i - indexRows(i) * rows;
    }

    /** Get a matrix element (linear indexing). */
    public double get(int i) {
        return data[i];
    }

    /** Set a matrix element (linear indexing). */
    public DoubleMatrix put(int i, double v) {
        data[i] = v;
        return this;
    }

    /** Set all elements to a value. */
    public DoubleMatrix fill(double value) {
        for (int i = 0; i < length; i++) {
            put(i, value);
        }
        return this;
    }

    /** Get number of rows. */
    public int getRows() {
        return rows;
    }

    /** Get number of columns. */
    public int getColumns() {
        return columns;
    }

    /** Get total number of elements. */
    public int getLength() {
        return length;
    }

    /** Checks whether the matrix is empty. */
    public boolean isEmpty() {
        return columns == 0 || rows == 0;
    }

    /** Checks whether the matrix is square. */
    public boolean isSquare() {
        return columns == rows;
    }

    /** Throw SizeException unless matrix is square. */
    public void assertSquare() {
        if (!isSquare()) {
            throw new SizeException("Matrix must be square!");
        }
    }

    /** Checks whether the matrix is a vector. */
    public boolean isVector() {
        return columns == 1 || rows == 1;
    }

    /** Checks whether the matrix is a row vector. */
    public boolean isRowVector() {
        return rows == 1;
    }

    /** Checks whether the matrix is a column vector. */
    public boolean isColumnVector() {
        return columns == 1;
    }

    /** Returns the diagonal of the matrix. */
    public DoubleMatrix diag() {
        assertSquare();
        DoubleMatrix d = new DoubleMatrix(rows);
        JavaBlas.rcopy(rows, data, 0, rows + 1, d.data, 0, 1);
        return d;
    }

    /** Pretty-print this matrix to <tt>System.out</tt>. */
    public void print() {
        System.out.println(toString());
    }

    /** Generate string representation of the matrix. */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();

        s.append("[");

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                s.append(get(i, j));
                if (j < columns - 1) {
                    s.append(", ");
                }
            }
            if (i < rows - 1) {
                s.append("; ");
            }
        }

        s.append("]");

        return s.toString();
    }

    /**
     * Generate string representation of the matrix, with specified
     * format for the entries. For example, <code>x.toString("%.1f")</code>
     * generates a string representations having only one position after the
     * decimal point.
     */
    public String toString(String fmt) {
        StringWriter s = new StringWriter();
        PrintWriter p = new PrintWriter(s);

        p.print("[");

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                p.printf(fmt, get(r, c));
                if (c < columns - 1) {
                    p.print(", ");
                }
            }
            if (r < rows - 1) {
                p.print("; ");
            }
        }

        p.print("]");

        return s.toString();
    }

    /** Converts the matrix to a one-dimensional array of doubles. */
    public double[] toArray() {
        double[] array = new double[length];

        System.arraycopy(data, 0, array, 0, length);

        return array;
    }

    /** Converts the matrix to a two-dimensional array of doubles. */
    public double[][] toArray2() {
        double[][] array = new double[rows][columns];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                array[r][c] = get(r, c);
            }
        }

        return array;
    }

    /** Converts the matrix to a one-dimensional array of integers. */
    public int[] toIntArray() {
        int[] array = new int[length];

        for (int i = 0; i < length; i++) {
            array[i] = (int) Math.rint(get(i));
        }

        return array;
    }

    /** Convert the matrix to a two-dimensional array of integers. */
    public int[][] toIntArray2() {
        int[][] array = new int[rows][columns];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                array[r][c] = (int) Math.rint(get(r, c));
            }
        }

        return array;
    }

    /** Convert the matrix to a one-dimensional array of boolean values. */
    public boolean[] toBooleanArray() {
        boolean[] array = new boolean[length];

        for (int i = 0; i < length; i++) {
            array[i] = get(i) != 0.0 ? true : false;
        }

        return array;
    }

    /** Convert the matrix to a two-dimensional array of boolean values. */
    public boolean[][] toBooleanArray2() {
        boolean[][] array = new boolean[rows][columns];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                array[r][c] = get(r, c) != 0.0 ? true : false;
            }
        }

        return array;
    }

    public FloatMatrix toFloat() {
         FloatMatrix result = new FloatMatrix(rows, columns);
         for (int i = 0; i < length; i++) {
            result.put(i, (float) get(i));
         }
         return result;
    }

    /**
     * A wrapper which allows to view a matrix as a List of Doubles (read-only!).
     * Also implements the {@link ConvertsToDoubleMatrix} interface.
     */
    public class ElementsAsListView extends AbstractList<Double> implements ConvertsToDoubleMatrix {

        private DoubleMatrix me;

        public ElementsAsListView(DoubleMatrix me) {
            this.me = me;
        }

        @Override
        public Double get(int index) {
            return me.get(index);
        }

        @Override
        public int size() {
            return me.length;
        }

        public DoubleMatrix convertToDoubleMatrix() {
            return me;
        }
    }

    public class RowsAsListView extends AbstractList<DoubleMatrix> implements ConvertsToDoubleMatrix {

        private DoubleMatrix me;

        public RowsAsListView(DoubleMatrix me) {
            this.me = me;
        }

        @Override
        public DoubleMatrix get(int index) {
            return getRow(index);
        }

        @Override
        public int size() {
            return rows;
        }

        public DoubleMatrix convertToDoubleMatrix() {
            return me;
        }
    }

    public class ColumnsAsListView extends AbstractList<DoubleMatrix> implements ConvertsToDoubleMatrix {

        private DoubleMatrix me;

        public ColumnsAsListView(DoubleMatrix me) {
            this.me = me;
        }

        @Override
        public DoubleMatrix get(int index) {
            return getColumn(index);
        }

        @Override
        public int size() {
            return columns;
        }

        public DoubleMatrix convertToDoubleMatrix() {
            return me;
        }
    }

    public List<Double> elementsAsList() {
        return new ElementsAsListView(this);
    }

    public List<DoubleMatrix> rowsAsList() {
        return new RowsAsListView(this);
    }

    public List<DoubleMatrix> columnsAsList() {
        return new ColumnsAsListView(this);
    }

    /**************************************************************************
     * Arithmetic Operations
     */
    /**
     * Ensures that the result vector has the same length as this. If not,
     * resizing result is tried, which fails if result == this or result == other.
     */
    private void ensureResultLength(DoubleMatrix other, DoubleMatrix result) {
        if (!sameLength(result)) {
            if (result == this || result == other) {
                throw new SizeException("Cannot resize result matrix because it is used in-place.");
            }
            result.resize(rows, columns);
        }
    }

    /** Add two matrices (in-place). */
    public DoubleMatrix addi(DoubleMatrix other, DoubleMatrix result) {
        if (other.isScalar()) {
            return addi(other.scalar(), result);
        }
        if (isScalar()) {
            return other.addi(scalar(), result);
        }

        assertSameLength(other);
        ensureResultLength(other, result);

        if (result == this) {
            SimpleBlas.axpy(1.0, other, result);
        } else if (result == other) {
            SimpleBlas.axpy(1.0, this, result);
        } else {
            /*SimpleBlas.copy(this, result);
            SimpleBlas.axpy(1.0, other, result);*/
            JavaBlas.rzgxpy(length, result.data, data, other.data);
        }

        return result;
    }

    /** Add a scalar to a matrix (in-place). */
    public DoubleMatrix addi(double v, DoubleMatrix result) {
        ensureResultLength(null, result);

        for (int i = 0; i < length; i++) {
            result.put(i, get(i) + v);
        }
        return result;
    }

    /** Subtract two matrices (in-place). */
    public DoubleMatrix subi(DoubleMatrix other, DoubleMatrix result) {
        if (other.isScalar()) {
            return subi(other.scalar(), result);
        }
        if (isScalar()) {
            return other.rsubi(scalar(), result);
        }

        assertSameLength(other);
        ensureResultLength(other, result);

        if (result == this) {
            SimpleBlas.axpy(-1.0, other, result);
        } else if (result == other) {
            SimpleBlas.scal(-1.0, result);
            SimpleBlas.axpy(1.0, this, result);
        } else {
            SimpleBlas.copy(this, result);
            SimpleBlas.axpy(-1.0, other, result);
        }
        return result;
    }

    /** Subtract a scalar from a matrix (in-place). */
    public DoubleMatrix subi(double v, DoubleMatrix result) {
        ensureResultLength(null, result);

        for (int i = 0; i < length; i++) {
            result.put(i, get(i) - v);
        }
        return result;
    }

    /**
     * Subtract two matrices, but subtract first from second matrix, that is,
     * compute <em>result = other - this</em> (in-place).
     * */
    public DoubleMatrix rsubi(DoubleMatrix other, DoubleMatrix result) {
        return other.subi(this, result);
    }

    /** Subtract a matrix from a scalar (in-place). */
    public DoubleMatrix rsubi(double a, DoubleMatrix result) {
        ensureResultLength(null, result);

        for (int i = 0; i < length; i++) {
            result.put(i, a - get(i));
        }
        return result;
    }

    /** Elementwise multiplication (in-place). */
    public DoubleMatrix muli(DoubleMatrix other, DoubleMatrix result) {
        if (other.isScalar()) {
            return muli(other.scalar(), result);
        }
        if (isScalar()) {
            return other.muli(scalar(), result);
        }

        assertSameLength(other);
        ensureResultLength(other, result);

        for (int i = 0; i < length; i++) {
            result.put(i, get(i) * other.get(i));
        }
        return result;
    }

    /** Elementwise multiplication with a scalar (in-place). */
    public DoubleMatrix muli(double v, DoubleMatrix result) {
        ensureResultLength(null, result);

        for (int i = 0; i < length; i++) {
            result.put(i, get(i) * v);
        }
        return result;
    }

    /** Matrix-matrix multiplication (in-place). */
    public DoubleMatrix mmuli(DoubleMatrix other, DoubleMatrix result) {
        if (other.isScalar()) {
            return muli(other.scalar(), result);
        }
        if (isScalar()) {
            return other.muli(scalar(), result);
        }

        /* check sizes and resize if necessary */
        assertMultipliesWith(other);
        if (result.rows != rows || result.columns != other.columns) {
            if (result != this && result != other) {
                result.resize(rows, other.columns);
            } else {
                throw new SizeException("Cannot resize result matrix because it is used in-place.");
            }
        }

        if (result == this || result == other) {
            /* actually, blas cannot do multiplications in-place. Therefore, we will fake by
             * allocating a temporary object on the side and copy the result later.
             */
            DoubleMatrix temp = new DoubleMatrix(result.rows, result.columns);
            if (other.columns == 1) {
                SimpleBlas.gemv(1.0, this, other, 0.0, temp);
            } else {
                SimpleBlas.gemm(1.0, this, other, 0.0, temp);
            }
            SimpleBlas.copy(temp, result);
        } else {
            if (other.columns == 1) {
                SimpleBlas.gemv(1.0, this, other, 0.0, result);
            } else {
                SimpleBlas.gemm(1.0, this, other, 0.0, result);
            }
        }
        return result;
    }

    /** Matrix-matrix multiplication with a scalar (for symmetry, does the
     * same as <code>muli(scalar)</code> (in-place).
     */
    public DoubleMatrix mmuli(double v, DoubleMatrix result) {
        return muli(v, result);
    }

    /** Elementwise division (in-place). */
    public DoubleMatrix divi(DoubleMatrix other, DoubleMatrix result) {
        if (other.isScalar()) {
            return divi(other.scalar(), result);
        }
        if (isScalar()) {
            return other.rdivi(scalar(), result);
        }

        assertSameLength(other);
        ensureResultLength(other, result);

        for (int i = 0; i < length; i++) {
            result.put(i, get(i) / other.get(i));
        }
        return result;
    }

    /** Elementwise division with a scalar (in-place). */
    public DoubleMatrix divi(double a, DoubleMatrix result) {
        ensureResultLength(null, result);

        for (int i = 0; i < length; i++) {
            result.put(i, get(i) / a);
        }
        return result;
    }

    /**
     * Elementwise division, with operands switched. Computes
     * <code>result = other / this</code> (in-place). */
    public DoubleMatrix rdivi(DoubleMatrix other, DoubleMatrix result) {
        return other.divi(this, result);
    }

    /** (Elementwise) division with a scalar, with operands switched. Computes
     * <code>result = a / this</code> (in-place). */
    public DoubleMatrix rdivi(double a, DoubleMatrix result) {
        ensureResultLength(null, result);

        for (int i = 0; i < length; i++) {
            result.put(i, a / get(i));
        }
        return result;
    }

    /** Negate each element (in-place). */
    public DoubleMatrix negi() {
        for (int i = 0; i < length; i++) {
            put(i, -get(i));
        }
        return this;
    }

    /** Negate each element. */
    public DoubleMatrix neg() {
        return dup().negi();
    }

    /** Maps zero to 1.0 and all non-zero values to 0.0 (in-place). */
    public DoubleMatrix noti() {
        for (int i = 0; i < length; i++) {
            put(i, get(i) == 0.0 ? 1.0 : 0.0);
        }
        return this;
    }

    /** Maps zero to 1.0 and all non-zero values to 0.0. */
    public DoubleMatrix not() {
        return dup().noti();
    }

    /** Maps zero to 0.0 and all non-zero values to 1.0 (in-place). */
    public DoubleMatrix truthi() {
        for (int i = 0; i < length; i++) {
            put(i, get(i) == 0.0 ? 0.0 : 1.0);
        }
        return this;
    }

    /** Maps zero to 0.0 and all non-zero values to 1.0. */
    public DoubleMatrix truth() {
        return dup().truthi();
    }

    public DoubleMatrix isNaNi() {
        for (int i = 0; i < length; i++) {
            put(i, Double.isNaN(get(i)) ? 1.0 : 0.0);
        }
        return this;
    }

    public DoubleMatrix isNaN() {
        return dup().isNaNi();
    }

    public DoubleMatrix isInfinitei() {
        for (int i = 0; i < length; i++) {
            put(i, Double.isInfinite(get(i)) ? 1.0 : 0.0);
        }
        return this;
    }

    public DoubleMatrix isInfinite() {
        return dup().isInfinitei();
    }

    public DoubleMatrix selecti(DoubleMatrix where) {
        checkLength(where.length);
        for (int i = 0; i < length; i++) {
            if (where.get(i) == 0.0) {
                put(i, 0.0);
            }
        }
        return this;
    }

    public DoubleMatrix select(DoubleMatrix where) {
        return dup().selecti(where);
    }

    /****************************************************************
     * Rank one-updates
     */
    /** Computes a rank-1-update A = A + alpha * x * y'. */
    public DoubleMatrix rankOneUpdate(double alpha, DoubleMatrix x, DoubleMatrix y) {
        if (rows != x.length) {
            throw new SizeException("Vector x has wrong length (" + x.length + " != " + rows + ").");
        }
        if (columns != y.length) {
            throw new SizeException("Vector y has wrong length (" + x.length + " != " + columns + ").");
        }

        SimpleBlas.ger(alpha, x, y, this);
        return this;
    }

    /** Computes a rank-1-update A = A + alpha * x * x'. */
    public DoubleMatrix rankOneUpdate(double alpha, DoubleMatrix x) {
        return rankOneUpdate(alpha, x, x);
    }

    /** Computes a rank-1-update A = A + x * x'. */
    public DoubleMatrix rankOneUpdate(DoubleMatrix x) {
        return rankOneUpdate(1.0, x, x);
    }

    /** Computes a rank-1-update A = A + x * y'. */
    public DoubleMatrix rankOneUpdate(DoubleMatrix x, DoubleMatrix y) {
        return rankOneUpdate(1.0, x, y);
    }

    /****************************************************************
     * Logical operations
     */
    /** Returns the minimal element of the matrix. */
    public double min() {
        if (isEmpty()) {
            return Double.POSITIVE_INFINITY;
        }
        double v = Double.POSITIVE_INFINITY;
        for (int i = 0; i < length; i++) {
            if (!Double.isNaN(get(i)) && get(i) < v) {
                v = get(i);
            }
        }

        return v;
    }

    /**
     * Returns the linear index of the minimal element. If there are
     * more than one elements with this value, the first one is returned.
     */
    public int argmin() {
        if (isEmpty()) {
            return -1;
        }
        double v = Double.POSITIVE_INFINITY;
        int a = -1;
        for (int i = 0; i < length; i++) {
            if (!Double.isNaN(get(i)) && get(i) < v) {
                v = get(i);
                a = i;
            }
        }

        return a;
    }

    /**
     * Computes the minimum between two matrices. Returns the smaller of the
     * corresponding elements in the matrix (in-place).
     */
    public DoubleMatrix mini(DoubleMatrix other, DoubleMatrix result) {
        if (result == this) {
            for (int i = 0; i < length; i++) {
                if (get(i) > other.get(i)) {
                    put(i, other.get(i));
                }
            }
        } else {
            for (int i = 0; i < length; i++) {
                if (get(i) > other.get(i)) {
                    result.put(i, other.get(i));
                } else {
                    result.put(i, get(i));
                }
            }
        }
        return this;
    }

    /**
     * Computes the minimum between two matrices. Returns the smaller of the
     * corresponding elements in the matrix (in-place on this).
     */
    public DoubleMatrix mini(DoubleMatrix other) {
        return mini(other, this);
    }

    /**
     * Computes the minimum between two matrices. Returns the smaller of the
     * corresponding elements in the matrix (in-place on this).
     */
    public DoubleMatrix min(DoubleMatrix other) {
        return mini(other, new DoubleMatrix(rows, columns));
    }

    public DoubleMatrix mini(double v, DoubleMatrix result) {
        if (result == this) {
            for (int i = 0; i < length; i++) {
                if (get(i) > v) {
                    result.put(i, v);
                }
            }
        } else {
            for (int i = 0; i < length; i++) {
                if (get(i) > v) {
                    result.put(i, v);
                } else {
                    result.put(i, get(i));
                }
            }

        }
        return this;
    }

    public DoubleMatrix mini(double v) {
        return mini(v, this);
    }

    public DoubleMatrix min(double v) {
        return mini(v, new DoubleMatrix(rows, columns));
    }

    /** Returns the maximal element of the matrix. */
    public double max() {
        if (isEmpty()) {
            return Double.NEGATIVE_INFINITY;
        }
        double v = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < length; i++) {
            if (!Double.isNaN(get(i)) && get(i) > v) {
                v = get(i);
            }
        }
        return v;
    }

    /**
     * Returns the linear index of the maximal element of the matrix. If
     * there are more than one elements with this value, the first one
     * is returned.
     */
    public int argmax() {
        if (isEmpty()) {
            return -1;
        }
        double v = Double.NEGATIVE_INFINITY;
        int a = -1;
        for (int i = 0; i < length; i++) {
            if (!Double.isNaN(get(i)) && get(i) > v) {
                v = get(i);
                a = i;
            }
        }

        return a;
    }

    /**
     * Computes the maximum between two matrices. Returns the larger of the
     * corresponding elements in the matrix (in-place).
     */
    public DoubleMatrix maxi(DoubleMatrix other, DoubleMatrix result) {
        if (result == this) {
            for (int i = 0; i < length; i++) {
                if (get(i) < other.get(i)) {
                    put(i, other.get(i));
                }
            }
        } else {
            for (int i = 0; i < length; i++) {
                if (get(i) < other.get(i)) {
                    result.put(i, other.get(i));
                } else {
                    result.put(i, get(i));
                }
            }
        }
        return this;
    }

    /**
     * Computes the maximum between two matrices. Returns the smaller of the
     * corresponding elements in the matrix (in-place on this).
     */
    public DoubleMatrix maxi(DoubleMatrix other) {
        return maxi(other, this);
    }

    /**
     * Computes the maximum between two matrices. Returns the larger of the
     * corresponding elements in the matrix (in-place on this).
     */
    public DoubleMatrix max(DoubleMatrix other) {
        return maxi(other, new DoubleMatrix(rows, columns));
    }

    public DoubleMatrix maxi(double v, DoubleMatrix result) {
        if (result == this) {
            for (int i = 0; i < length; i++) {
                if (get(i) < v) {
                    result.put(i, v);
                }
            }
        } else {
            for (int i = 0; i < length; i++) {
                if (get(i) < v) {
                    result.put(i, v);
                } else {
                    result.put(i, get(i));
                }
            }

        }
        return this;
    }

    public DoubleMatrix maxi(double v) {
        return maxi(v, this);
    }

    public DoubleMatrix max(double v) {
        return maxi(v, new DoubleMatrix(rows, columns));
    }

    /** Computes the sum of all elements of the matrix. */
    public double sum() {
        double s = 0.0;
        for (int i = 0; i < length; i++) {
            s += get(i);
        }
        return s;
    }

    /** Computes the product of all elements of the matrix */
    public double prod() {
        double p = 1.0;
        for (int i = 0; i < length; i++) {
            p *= get(i);
        }
        return p;
    }

    /**
     * Computes the mean value of all elements in the matrix,
     * that is, <code>x.sum() / x.length</code>.
     */
    public double mean() {
        return sum() / length;
    }

    /**
     * Computes the cumulative sum, that is, the sum of all elements
     * of the matrix up to a given index in linear addressing (in-place).
     */
    public DoubleMatrix cumulativeSumi() {
        double s = 0.0;
        for (int i = 0; i < length; i++) {
            s += get(i);
            put(i, s);
        }
        return this;
    }

    /**
     * Computes the cumulative sum, that is, the sum of all elements
     * of the matrix up to a given index in linear addressing.
     */
    public DoubleMatr
