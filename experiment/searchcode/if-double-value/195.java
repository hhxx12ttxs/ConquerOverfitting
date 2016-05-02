<<<<<<< HEAD
/*
Copyright 1999 CERN - European Organization for Nuclear Research.
Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose 
is hereby granted without fee, provided that the above copyright notice appear in all copies and 
that both that copyright notice and this permission notice appear in supporting documentation. 
CERN makes no representations about the suitability of this software for any purpose. 
It is provided "as is" without expressed or implied warranty.
*/
package org.apache.mahout.math.matrix.linalg;

import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.function.Functions;
import org.apache.mahout.math.matrix.DoubleMatrix1D;
import org.apache.mahout.math.matrix.DoubleMatrix2D;
import org.apache.mahout.math.matrix.impl.AbstractMatrix2D;
import org.apache.mahout.math.matrix.impl.DenseDoubleMatrix2D;

/** @deprecated until unit tests are in place.  Until this time, this class/interface is unsupported. */
@Deprecated
public final class Property {

  /** The default Property object; currently has <tt>tolerance()==1.0E-9</tt>. */
  public static final Property DEFAULT = new Property(1.0E-9);

  /** A Property object with <tt>tolerance()==0.0</tt>. */
  public static final Property ZERO = new Property(0.0);

  private final double tolerance;

  /** Constructs an instance with a tolerance of <tt>Math.abs(newTolerance)</tt>. */
  public Property(double newTolerance) {
    tolerance = Math.abs(newTolerance);
  }

  /**
   * Checks whether the given matrix <tt>A</tt> is <i>rectangular</i>.
   *
   * @throws IllegalArgumentException if <tt>A.rows() < A.columns()</tt>.
   */
  public static void checkRectangular(AbstractMatrix2D a) {
    if (a.rows() < a.columns()) {
      throw new IllegalArgumentException("Matrix must be rectangular");
    }
  }

  /**
   * Checks whether the given matrix <tt>A</tt> is <i>square</i>.
   *
   * @throws IllegalArgumentException if <tt>A.rows() != A.columns()</tt>.
   */
  public static void checkSquare(AbstractMatrix2D a) {
    if (a.rows() != a.columns()) {
      throw new IllegalArgumentException("Matrix must be square");
    }
  }

  public static void checkSquare(Matrix matrix) {
    if(matrix.numRows() != matrix.numCols()) {
      throw new IllegalArgumentException("Matrix must be square");      
    }
  }

  /** Returns the matrix's fraction of non-zero cells; <tt>A.cardinality() / A.size()</tt>. */
  public static double density(DoubleMatrix2D a) {
    return a.cardinality() / (double) a.size();
  }

  /**
   * Returns whether all cells of the given matrix <tt>A</tt> are equal to the given value. The result is <tt>true</tt>
   * if and only if <tt>A != null</tt> and <tt>! (Math.abs(value - A[i]) > tolerance())</tt> holds for all coordinates.
   *
   * @param a     the first matrix to compare.
   * @param value the value to compare against.
   * @return <tt>true</tt> if the matrix is equal to the value; <tt>false</tt> otherwise.
   */
  public boolean equals(DoubleMatrix1D a, double value) {
    if (a == null) {
      return false;
    }
    double epsilon = tolerance();
    for (int i = a.size(); --i >= 0;) {
      //if (!(A.getQuick(i) == value)) return false;
      //if (Math.abs(value - A.getQuick(i)) > epsilon) return false;
      double x = a.getQuick(i);
      double diff = Math.abs(value - x);
      if (Double.isNaN(diff) && (Double.isNaN(value) && Double.isNaN(x) || value == x)) {
        diff = 0.0;
      }
      if (diff > epsilon) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns whether both given matrices <tt>A</tt> and <tt>B</tt> are equal. The result is <tt>true</tt> if
   * <tt>A==B</tt>. Otherwise, the result is <tt>true</tt> if and only if both arguments are <tt>!= null</tt>, have the
   * same size and <tt>! (Math.abs(A[i] - B[i]) > tolerance())</tt> holds for all indexes.
   *
   * @param a the first matrix to compare.
   * @param b the second matrix to compare.
   * @return <tt>true</tt> if both matrices are equal; <tt>false</tt> otherwise.
   */
  public boolean equals(DoubleMatrix1D a, DoubleMatrix1D b) {
    if (a == b) {
      return true;
    }
    if (!(a != null && b != null)) {
      return false;
    }
    int size = a.size();
    if (size != b.size()) {
      return false;
    }

    double epsilon = tolerance();
    for (int i = size; --i >= 0;) {
      //if (!(getQuick(i) == B.getQuick(i))) return false;
      //if (Math.abs(A.getQuick(i) - B.getQuick(i)) > epsilon) return false;
      double x = a.getQuick(i);
      double value = b.getQuick(i);
      double diff = Math.abs(value - x);
      if (Double.isNaN(diff) && (Double.isNaN(value) && Double.isNaN(x) || value == x)) {
        diff = 0.0;
      }
      if (diff > epsilon) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns whether all cells of the given matrix <tt>A</tt> are equal to the given value. The result is <tt>true</tt>
   * if and only if <tt>A != null</tt> and <tt>! (Math.abs(value - A[row,col]) > tolerance())</tt> holds for all
   * coordinates.
   *
   * @param a     the first matrix to compare.
   * @param value the value to compare against.
   * @return <tt>true</tt> if the matrix is equal to the value; <tt>false</tt> otherwise.
   */
  public boolean equals(DoubleMatrix2D a, double value) {
    if (a == null) {
      return false;
    }
    int rows = a.rows();
    int columns = a.columns();

    double epsilon = tolerance();
    for (int row = rows; --row >= 0;) {
      for (int column = columns; --column >= 0;) {
        //if (!(A.getQuick(row,column) == value)) return false;
        //if (Math.abs(value - A.getQuick(row,column)) > epsilon) return false;
        double x = a.getQuick(row, column);
        double diff = Math.abs(value - x);
        if (Double.isNaN(diff) && (Double.isNaN(value) && Double.isNaN(x) || value == x)) {
          diff = 0.0;
        }
        if (diff > epsilon) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Returns whether both given matrices <tt>A</tt> and <tt>B</tt> are equal. The result is <tt>true</tt> if
   * <tt>A==B</tt>. Otherwise, the result is <tt>true</tt> if and only if both arguments are <tt>!= null</tt>, have the
   * same number of columns and rows and <tt>! (Math.abs(A[row,col] - B[row,col]) > tolerance())</tt> holds for all
   * coordinates.
   *
   * @param a the first matrix to compare.
   * @param b the second matrix to compare.
   * @return <tt>true</tt> if both matrices are equal; <tt>false</tt> otherwise.
   */
  public boolean equals(DoubleMatrix2D a, DoubleMatrix2D b) {
    if (a == b) {
      return true;
    }
    if (!(a != null && b != null)) {
      return false;
    }
    int rows = a.rows();
    int columns = a.columns();
    if (columns != b.columns() || rows != b.rows()) {
      return false;
    }

    double epsilon = tolerance();
    for (int row = rows; --row >= 0;) {
      for (int column = columns; --column >= 0;) {
        //if (!(A.getQuick(row,column) == B.getQuick(row,column))) return false;
        //if (Math.abs((A.getQuick(row,column) - B.getQuick(row,column)) > epsilon) return false;
        double x = a.getQuick(row, column);
        double value = b.getQuick(row, column);
        double diff = Math.abs(value - x);
        if (Double.isNaN(diff) && (Double.isNaN(value) && Double.isNaN(x) || value == x)) {
          diff = 0.0;
        }
        if (diff > epsilon) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * A matrix <tt>A</tt> is <i>diagonal</i> if <tt>A[i,j] == 0</tt> whenever <tt>i != j</tt>. Matrix may but need not be
   * square.
   */
  public boolean isDiagonal(DoubleMatrix2D a) {
    double epsilon = tolerance();
    int rows = a.rows();
    int columns = a.columns();
    for (int row = rows; --row >= 0;) {
      for (int column = columns; --column >= 0;) {
        //if (row!=column && A.getQuick(row,column) != 0) return false;
        if (row != column && Math.abs(a.getQuick(row, column)) > epsilon) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * A matrix <tt>A</tt> is <i>diagonally dominant by column</i> if the absolute value of each diagonal element is
   * larger than the sum of the absolute values of the off-diagonal elements in the corresponding column. <tt>returns
   * true if for all i: abs(A[i,i]) &gt; Sum(abs(A[j,i])); j != i.</tt> Matrix may but need not be square. <p> Note:
   * Ignores tolerance.
   */
  public static boolean isDiagonallyDominantByColumn(DoubleMatrix2D a) {
    //double epsilon = tolerance();
    int min = Math.min(a.rows(), a.columns());
    for (int i = min; --i >= 0;) {
      double diag = Math.abs(a.getQuick(i, i));
      diag += diag;
      if (diag <= a.viewColumn(i).aggregate(Functions.PLUS, Functions.ABS)) {
        return false;
      }
    }
    return true;
  }

  /**
   * A matrix <tt>A</tt> is <i>diagonally dominant by row</i> if the absolute value of each diagonal element is larger
   * than the sum of the absolute values of the off-diagonal elements in the corresponding row. <tt>returns true if for
   * all i: abs(A[i,i]) &gt; Sum(abs(A[i,j])); j != i.</tt> Matrix may but need not be square. <p> Note: Ignores
   * tolerance.
   */
  public static boolean isDiagonallyDominantByRow(DoubleMatrix2D a) {
    //double epsilon = tolerance();
    int min = Math.min(a.rows(), a.columns());
    for (int i = min; --i >= 0;) {
      double diag = Math.abs(a.getQuick(i, i));
      diag += diag;
      if (diag <= a.viewRow(i).aggregate(Functions.PLUS, Functions.ABS)) {
        return false;
      }
    }
    return true;
  }

  /**
   * A matrix <tt>A</tt> is an <i>identity</i> matrix if <tt>A[i,i] == 1</tt> and all other cells are zero. Matrix may
   * but need not be square.
   */
  public boolean isIdentity(DoubleMatrix2D a) {
    double epsilon = tolerance();
    int rows = a.rows();
    int columns = a.columns();
    for (int row = rows; --row >= 0;) {
      for (int column = columns; --column >= 0;) {
        double v = a.getQuick(row, column);
        if (row == column) {
          if (Math.abs(1 - v) > epsilon) {
            return false;
          }
        } else if (Math.abs(v) > epsilon) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * A matrix <tt>A</tt> is <i>lower bidiagonal</i> if <tt>A[i,j]==0</tt> unless <tt>i==j || i==j+1</tt>. Matrix may but
   * need not be square.
   */
  public boolean isLowerBidiagonal(DoubleMatrix2D a) {
    double epsilon = tolerance();
    int rows = a.rows();
    int columns = a.columns();
    for (int row = rows; --row >= 0;) {
      for (int column = columns; --column >= 0;) {
        if (!(row == column || row == column + 1) && Math.abs(a.getQuick(row, column)) > epsilon) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * A matrix <tt>A</tt> is <i>lower triangular</i> if <tt>A[i,j]==0</tt> whenever <tt>i &lt; j</tt>. Matrix may but
   * need not be square.
   */
  public boolean isLowerTriangular(DoubleMatrix2D a) {
    double epsilon = tolerance();
    int rows = a.rows();
    int columns = a.columns();
    for (int column = columns; --column >= 0;) {
      for (int row = Math.min(column, rows); --row >= 0;) {
        //if (A.getQuick(row,column) != 0) return false;
        if (Math.abs(a.getQuick(row, column)) > epsilon) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * A matrix <tt>A</tt> is <i>non-negative</i> if <tt>A[i,j] &gt;= 0</tt> holds for all cells. <p> Note: Ignores
   * tolerance.
   */
  public static boolean isNonNegative(DoubleMatrix2D a) {
    int rows = a.rows();
    int columns = a.columns();
    for (int row = rows; --row >= 0;) {
      for (int column = columns; --column >= 0;) {
        if (a.getQuick(row, column) < 0) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * A square matrix <tt>A</tt> is <i>orthogonal</i> if <tt>A*transpose(A) = I</tt>.
   *
   * @throws IllegalArgumentException if <tt>!isSquare(A)</tt>.
   */
  public boolean isOrthogonal(DoubleMatrix2D a) {
    checkSquare(a);
    return equals(a.zMult(a, null, 1, 0, false, true),
                  DenseDoubleMatrix2D.identity(a.rows()));
  }

  /** A matrix <tt>A</tt> is <i>positive</i> if <tt>A[i,j] &gt; 0</tt> holds for all cells.
   * <p> Note: Ignores tolerance.
   */
  public static boolean isPositive(DoubleMatrix2D a) {
    int rows = a.rows();
    int columns = a.columns();
    for (int row = rows; --row >= 0;) {
      for (int column = columns; --column >= 0;) {
        if (a.getQuick(row, column) <= 0) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * A square matrix <tt>A</tt> is <i>skew-symmetric</i> if <tt>A = -transpose(A)</tt>, that is <tt>A[i,j] ==
   * -A[j,i]</tt>.
   *
   * @throws IllegalArgumentException if <tt>!isSquare(A)</tt>.
   */
  public boolean isSkewSymmetric(DoubleMatrix2D a) {
    checkSquare(a);
    double epsilon = tolerance();
    int rows = a.rows();
    //int columns = A.columns();
    for (int row = rows; --row >= 0;) {
      for (int column = rows; --column >= 0;) {
        //if (A.getQuick(row,column) != -A.getQuick(column,row)) return false;
        if (Math.abs(a.getQuick(row, column) + a.getQuick(column, row)) > epsilon) {
          return false;
        }
      }
    }
    return true;
  }

  /** A matrix <tt>A</tt> is <i>square</i> if it has the same number of rows and columns. */
  public static boolean isSquare(AbstractMatrix2D a) {
    return a.rows() == a.columns();
  }

  /**
   * A matrix <tt>A</tt> is <i>strictly lower triangular</i> if <tt>A[i,j]==0</tt> whenever <tt>i &lt;= j</tt>. Matrix
   * may but need not be square.
   */
  public boolean isStrictlyLowerTriangular(DoubleMatrix2D a) {
    double epsilon = tolerance();
    int rows = a.rows();
    int columns = a.columns();
    for (int column = columns; --column >= 0;) {
      for (int row = Math.min(rows, column + 1); --row >= 0;) {
        //if (A.getQuick(row,column) != 0) return false;
        if (Math.abs(a.getQuick(row, column)) > epsilon) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * A matrix <tt>A</tt> is <i>strictly triangular</i> if it is triangular and its diagonal elements all equal 0. Matrix
   * may but need not be square.
   */
  public boolean isStrictlyTriangular(DoubleMatrix2D a) {
    if (isTriangular(a)) {
      double epsilon = tolerance();
      for (int i = Math.min(a.rows(), a.columns()); --i >= 0;) {
        //if (A.getQuick(i,i) != 0) return false;
        if (Math.abs(a.getQuick(i, i)) > epsilon) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  /**
   * A matrix <tt>A</tt> is <i>strictly upper triangular</i> if <tt>A[i,j]==0</tt> whenever <tt>i &gt;= j</tt>. Matrix
   * may but need not be square.
   */
  public boolean isStrictlyUpperTriangular(DoubleMatrix2D a) {
    double epsilon = tolerance();
    int rows = a.rows();
    int columns = a.columns();
    for (int column = columns; --column >= 0;) {
      for (int row = rows; --row >= column;) {
        //if (A.getQuick(row,column) != 0) return false;
        if (Math.abs(a.getQuick(row, column)) > epsilon) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * A matrix <tt>A</tt> is <i>symmetric</i> if <tt>A = tranpose(A)</tt>, that is <tt>A[i,j] == A[j,i]</tt>.
   *
   * @throws IllegalArgumentException if <tt>!isSquare(A)</tt>.
   */
  public boolean isSymmetric(DoubleMatrix2D a) {
    checkSquare(a);
    return equals(a, a.viewDice());
  }

  /**
   * A matrix <tt>A</tt> is <i>triangular</i> iff it is either upper or lower triangular. Matrix may but need not be
   * square.
   */
  public boolean isTriangular(DoubleMatrix2D a) {
    return isLowerTriangular(a) || isUpperTriangular(a);
  }

  /**
   * A matrix <tt>A</tt> is <i>tridiagonal</i> if <tt>A[i,j]==0</tt> whenever <tt>Math.abs(i-j) > 1</tt>. Matrix may but
   * need not be square.
   */
  public boolean isTridiagonal(DoubleMatrix2D a) {
    double epsilon = tolerance();
    int rows = a.rows();
    int columns = a.columns();
    for (int row = rows; --row >= 0;) {
      for (int column = columns; --column >= 0;) {
        if (Math.abs(row - column) > 1 && Math.abs(a.getQuick(row, column)) > epsilon) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * A matrix <tt>A</tt> is <i>unit triangular</i> if it is triangular and its diagonal elements all equal 1. Matrix may
   * but need not be square.
   */
  public boolean isUnitTriangular(DoubleMatrix2D a) {
    if (isTriangular(a)) {
      double epsilon = tolerance();
      for (int i = Math.min(a.rows(), a.columns()); --i >= 0;) {
        //if (A.getQuick(i,i) != 1) return false;
        if (Math.abs(1 - a.getQuick(i, i)) > epsilon) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  /**
   * A matrix <tt>A</tt> is <i>upper bidiagonal</i> if <tt>A[i,j]==0</tt> unless <tt>i==j || i==j-1</tt>. Matrix may but
   * need not be square.
   */
  public boolean isUpperBidiagonal(DoubleMatrix2D a) {
    double epsilon = tolerance();
    int rows = a.rows();
    int columns = a.columns();
    for (int row = rows; --row >= 0;) {
      for (int column = columns; --column >= 0;) {
        if (!(row == column || row == column - 1) && Math.abs(a.getQuick(row, column)) > epsilon) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * A matrix <tt>A</tt> is <i>upper triangular</i> if <tt>A[i,j]==0</tt> whenever <tt>i &gt; j</tt>. Matrix may but
   * need not be square.
   */
  public boolean isUpperTriangular(DoubleMatrix2D a) {
    double epsilon = tolerance();
    int rows = a.rows();
    int columns = a.columns();
    for (int column = columns; --column >= 0;) {
      for (int row = rows; --row > column;) {
        //if (A.getQuick(row,column) != 0) return false;
        if (Math.abs(a.getQuick(row, column)) > epsilon) {
          return false;
        }
      }
    }
    return true;
  }

  /** A matrix <tt>A</tt> is <i>zero</i> if all its cells are zero. */
  public boolean isZero(DoubleMatrix2D a) {
    return equals(a, 0);
  }

  /**
   * The <i>lower bandwidth</i> of a square matrix <tt>A</tt> is the maximum <tt>i-j</tt> for which <tt>A[i,j]</tt> is
   * nonzero and <tt>i &gt; j</tt>. A <i>banded</i> matrix has a "band" about the diagonal. Diagonal, tridiagonal and
   * triangular matrices are special cases.
   *
   * @param a the square matrix to analyze.
   * @return the lower bandwith.
   * @throws IllegalArgumentException if <tt>!isSquare(A)</tt>.
   * @see #semiBandwidth(DoubleMatrix2D)
   * @see #upperBandwidth(DoubleMatrix2D)
   */
  public int lowerBandwidth(DoubleMatrix2D a) {
    checkSquare(a);
    double epsilon = tolerance();
    int rows = a.rows();

    for (int k = rows; --k >= 0;) {
      for (int i = rows - k; --i >= 0;) {
        int j = i + k;
        //if (A.getQuick(j,i) != 0) return k;
        if (Math.abs(a.getQuick(j, i)) > epsilon) {
          return k;
        }
      }
    }
    return 0;
  }

  /**
   * Returns the <i>semi-bandwidth</i> of the given square matrix <tt>A</tt>. A <i>banded</i> matrix has a "band" about
   * the diagonal. It is a matrix with all cells equal to zero, with the possible exception of the cells along the
   * diagonal line, the <tt>k</tt> diagonal lines above the diagonal, and the <tt>k</tt> diagonal lines below the
   * diagonal. The <i>semi-bandwith l</i> is the number <tt>k+1</tt>. The <i>bandwidth p</i> is the number <tt>2*k +
   * 1</tt>. For example, a tridiagonal matrix corresponds to <tt>k=1, l=2, p=3</tt>, a diagonal or zero matrix
   * corresponds to <tt>k=0, l=1, p=1</tt>, <p> The <i>upper bandwidth</i> is the maximum <tt>j-i</tt> for which
   * <tt>A[i,j]</tt> is nonzero and <tt>j &gt; i</tt>. The <i>lower bandwidth</i> is the maximum <tt>i-j</tt> for which
   * <tt>A[i,j]</tt> is nonzero and <tt>i &gt; j</tt>. Diagonal, tridiagonal and triangular matrices are special cases.
   * <p> Examples: <table border="1" cellspacing="0"> <tr align="left" valign="top"> <td valign="middle"
   * align="left"><tt>matrix</tt></td> <td> <tt>4&nbsp;x&nbsp;4&nbsp;<br> 0&nbsp;0&nbsp;0&nbsp;0<br>
   * 0&nbsp;0&nbsp;0&nbsp;0<br> 0&nbsp;0&nbsp;0&nbsp;0<br> 0&nbsp;0&nbsp;0&nbsp;0 </tt></td> <td><tt>4&nbsp;x&nbsp;4<br>
   * 1&nbsp;0&nbsp;0&nbsp;0<br> 0&nbsp;0&nbsp;0&nbsp;0<br> 0&nbsp;0&nbsp;0&nbsp;0<br> 0&nbsp;0&nbsp;0&nbsp;1 </tt></td>
   * <td><tt>4&nbsp;x&nbsp;4<br> 1&nbsp;1&nbsp;0&nbsp;0<br> 1&nbsp;1&nbsp;1&nbsp;0<br> 0&nbsp;1&nbsp;1&nbsp;1<br>
   * 0&nbsp;0&nbsp;1&nbsp;1 </tt></td> <td><tt> 4&nbsp;x&nbsp;4<br> 0&nbsp;1&nbsp;1&nbsp;1<br>
   * 0&nbsp;1&nbsp;1&nbsp;1<br> 0&nbsp;0&nbsp;0&nbsp;1<br> 0&nbsp;0&nbsp;0&nbsp;1 </tt></td> <td><tt>
   * 4&nbsp;x&nbsp;4<br> 0&nbsp;0&nbsp;0&nbsp;0<br> 1&nbsp;1&nbsp;0&nbsp;0<br> 1&nbsp;1&nbsp;0&nbsp;0<br>
   * 1&nbsp;1&nbsp;1&nbsp;1 </tt></td> <td><tt>4&nbsp;x&nbsp;4<br> 1&nbsp;1&nbsp;0&nbsp;0<br> 0&nbsp;1&nbsp;1&nbsp;0<br>
   * 0&nbsp;1&nbsp;0&nbsp;1<br> 1&nbsp;0&nbsp;1&nbsp;1 </tt><tt> </tt> </td> <td><tt>4&nbsp;x&nbsp;4<br>
   * 1&nbsp;1&nbsp;1&nbsp;0<br> 0&nbsp;1&nbsp;0&nbsp;0<br> 1&nbsp;1&nbsp;0&nbsp;1<br> 0&nbsp;0&nbsp;1&nbsp;1 </tt> </td>
   * </tr> <tr align="center" valign="middle"> <td><tt>upperBandwidth</tt></td> <td> <div
   * align="center"><tt>0</tt></div> </td> <td> <div align="center"><tt>0</tt></div> </td> <td> <div
   * align="center"><tt>1</tt></div> </td> <td><tt>3</tt></td> <td align="center" valign="middle"><tt>0</tt></td> <td
   * align="center" valign="middle"> <div align="center"><tt>1</tt></div> </td> <td align="center" valign="middle"> <div
   * align="center"><tt>2</tt></div> </td> </tr> <tr align="center" valign="middle"> <td><tt>lowerBandwidth</tt></td>
   * <td> <div align="center"><tt>0</tt></div> </td> <td> <div align="center"><tt>0</tt></div> </td> <td> <div
   * align="center"><tt>1</tt></div> </td> <td><tt>0</tt></td> <td align="center" valign="middle"><tt>3</tt></td> <td
   * align="center" valign="middle"> <div align="center"><tt>3</tt></div> </td> <td align="center" valign="middle"> <div
   * align="center"><tt>2</tt></div> </td> </tr> <tr align="center" valign="middle"> <td><tt>semiBandwidth</tt></td>
   * <td> <div align="center"><tt>1</tt></div> </td> <td> <div align="center"><tt>1</tt></div> </td> <td> <div
   * align="center"><tt>2</tt></div> </td> <td><tt>4</tt></td> <td align="center" valign="middle"><tt>4</tt></td> <td
   * align="center" valign="middle"> <div align="center"><tt>4</tt></div> </td> <td align="center" valign="middle"> <div
   * align="center"><tt>3</tt></div> </td> </tr> <tr align="center" valign="middle"> <td><tt>description</tt></td> <td>
   * <div align="center"><tt>zero</tt></div> </td> <td> <div align="center"><tt>diagonal</tt></div> </td> <td> <div
   * align="center"><tt>tridiagonal</tt></div> </td> <td><tt>upper triangular</tt></td> <td align="center"
   * valign="middle"><tt>lower triangular</tt></td> <td align="center" valign="middle"> <div
   * align="center"><tt>unstructured</tt></div> </td> <td align="center" valign="middle"> <div
   * align="center"><tt>unstructured</tt></div> </td> </tr> </table>
   *
   * @param a the square matrix to analyze.
   * @return the semi-bandwith <tt>l</tt>.
   * @throws IllegalArgumentException if <tt>!isSquare(A)</tt>.
   * @see #lowerBandwidth(DoubleMatrix2D)
   * @see #upperBandwidth(DoubleMatrix2D)
   */
  public int semiBandwidth(DoubleMatrix2D a) {
    checkSquare(a);
    double epsilon = tolerance();
    int rows = a.rows();

    for (int k = rows; --k >= 0;) {
      for (int i = rows - k; --i >= 0;) {
        int j = i + k;
        //if (A.getQuick(j,i) != 0) return k+1;
        //if (A.getQuick(i,j) != 0) return k+1;
        if (!(Math.abs(a.getQuick(j, i)) <= epsilon)) {
          return k + 1;
        }
        if (Math.abs(a.getQuick(i, j)) > epsilon) {
          return k + 1;
        }
      }
    }
    return 1;
  }

  /** Returns the current tolerance. */
  public double tolerance() {
    return tolerance;
  }

  /**
   * The <i>upper bandwidth</i> of a square matrix <tt>A</tt> is the maximum <tt>j-i</tt> for which <tt>A[i,j]</tt> is
   * nonzero and <tt>j &gt; i</tt>. A <i>banded</i> matrix has a "band" about the diagonal. Diagonal, tridiagonal and
   * triangular matrices are special cases.
   *
   * @param a the square matrix to analyze.
   * @return the upper bandwith.
   * @throws IllegalArgumentException if <tt>!isSquare(A)</tt>.
   * @see #semiBandwidth(DoubleMatrix2D)
   * @see #lowerBandwidth(DoubleMatrix2D)
   */
  public int upperBandwidth(DoubleMatrix2D a) {
    checkSquare(a);
    double epsilon = tolerance();
    int rows = a.rows();

    for (int k = rows; --k >= 0;) {
      for (int i = rows - k; --i >= 0;) {
        int j = i + k;
        //if (A.getQuick(i,j) != 0) return k;
        if (!(Math.abs(a.getQuick(i, j)) <= epsilon)) {
          return k;
        }
      }
    }
    return 0;
  }
=======
package com.jjoe64.graphview;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.jjoe64.graphview.compatible.ScaleGestureDetector;

/**
 * GraphView is a Android View for creating zoomable and scrollable graphs.
 * This is the abstract base class for all graphs. Extend this class and implement {@link #drawSeries(Canvas, GraphViewData[], float, float, float, double, double, double, double, float)} to display a custom graph.
 * Use {@link LineGraphView} for creating a line chart.
 *
 * @author jjoe64 - jonas gehring - http://www.jjoe64.com
 *
 * Copyright (C) 2011 Jonas Gehring
 * Licensed under the GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 */
abstract public class GraphView extends LinearLayout {
	static final private class GraphViewConfig {
		static final float BORDER = 20;
		static final float VERTICAL_LABEL_WIDTH = 100;
		static final float HORIZONTAL_LABEL_HEIGHT = 80;
	}

	private class GraphViewContentView extends View {
		private float lastTouchEventX;
		private float graphwidth;

		/**
		 * @param context
		 */
		public GraphViewContentView(Context context) {
			super(context);
			setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		}

		/**
		 * @param canvas
		 */
		@Override
		protected void onDraw(Canvas canvas) {

            paint.setAntiAlias(true);

			// normal
			paint.setStrokeWidth(0);

			float border = GraphViewConfig.BORDER;
			float horstart = 0;
			float height = getHeight();
			float width = getWidth() - 1;
			double maxY = getMaxY();
			double minY = getMinY();
			double diffY = maxY - minY;
			double maxX = getMaxX(false);
			double minX = getMinX(false);
			double diffX = maxX - minX;
			float graphheight = height - (2 * border);
			graphwidth = width;

			if (horlabels == null) {
				horlabels = generateHorlabels(graphwidth);
			}
			if (verlabels == null) {
				verlabels = generateVerlabels(graphheight);
			}

			// vertical lines
			paint.setTextAlign(Align.LEFT);
			int vers = verlabels.length - 1;
			for (int i = 0; i < verlabels.length; i++) {
				paint.setColor(Color.DKGRAY);
				float y = ((graphheight / vers) * i) + border;
				canvas.drawLine(horstart, y, width, y, paint);
			}

			// horizontal labels + lines
			int hors = horlabels.length - 1;
			for (int i = 0; i < horlabels.length; i++) {
				paint.setColor(Color.DKGRAY);
				float x = ((graphwidth / hors) * i) + horstart;
				canvas.drawLine(x, height - border, x, border, paint);
				paint.setTextAlign(Align.CENTER);
				if (i==horlabels.length-1)
					paint.setTextAlign(Align.RIGHT);
				if (i==0)
					paint.setTextAlign(Align.LEFT);
				paint.setColor(Color.WHITE);
				canvas.drawText(horlabels[i], x, height - 4, paint);
			}

			paint.setTextAlign(Align.CENTER);
			canvas.drawText(title, (graphwidth / 2) + horstart, border - 4, paint);

			if (maxY != minY) {
				paint.setStrokeCap(Paint.Cap.ROUND);

				for (int i=0; i<graphSeries.size(); i++) {
					paint.setStrokeWidth(graphSeries.get(i).style.thickness);
					paint.setColor(graphSeries.get(i).style.color);
					drawSeries(canvas, _values(i), graphwidth, graphheight, border, minX, minY, diffX, diffY, horstart);
				}

				if (showLegend) drawLegend(canvas, height, width);
			}
		}

		private void onMoveGesture(float f) {
			// view port update
			if (viewportSize != 0) {
				viewportStart -= f*viewportSize/graphwidth;

				// minimal and maximal view limit
				double minX = getMinX(true);
				double maxX = getMaxX(true);
				if (viewportStart < minX) {
					viewportStart = minX;
				} else if (viewportStart+viewportSize > maxX) {
					viewportStart = maxX - viewportSize;
				}

				// labels have to be regenerated
				horlabels = null;
				verlabels = null;
				viewVerLabels.invalidate();
			}
			invalidate();
		}

		/**
		 * @param event
		 */
		@Override
		public boolean onTouchEvent(MotionEvent event) {
			if (!isScrollable()) {
				return super.onTouchEvent(event);
			}

			boolean handled = false;
			// first scale
			if (scalable && scaleDetector != null) {
				scaleDetector.onTouchEvent(event);
				handled = scaleDetector.isInProgress();
			}
			if (!handled) {
				// if not scaled, scroll
				if ((event.getAction() & MotionEvent.ACTION_DOWN) == MotionEvent.ACTION_DOWN) {
					handled = true;
				}
				if ((event.getAction() & MotionEvent.ACTION_UP) == MotionEvent.ACTION_UP) {
					lastTouchEventX = 0;
					handled = true;
				}
				if ((event.getAction() & MotionEvent.ACTION_MOVE) == MotionEvent.ACTION_MOVE) {
					if (lastTouchEventX != 0) {
						onMoveGesture(event.getX() - lastTouchEventX);
					}
					lastTouchEventX = event.getX();
					handled = true;
				}
				if (handled)
					invalidate();
			}
			return handled;
		}
	}

	/**
	 * one data set for a graph series
	 */
	static public class GraphViewData {
		public final double valueX;
		public final double valueY;
		public GraphViewData(double valueX, double valueY) {
			super();
			this.valueX = valueX;
			this.valueY = valueY;
		}
	}

	public enum LegendAlign {
		TOP, MIDDLE, BOTTOM
	}

	private class VerLabelsView extends View {
		/**
		 * @param context
		 */
		public VerLabelsView(Context context) {
			super(context);
			setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 10));
		}

		/**
		 * @param canvas
		 */
		@Override
		protected void onDraw(Canvas canvas) {
			// normal
			paint.setStrokeWidth(0);

			float border = GraphViewConfig.BORDER;
			float height = getHeight();
			float graphheight = height - (2 * border);

			if (verlabels == null) {
				verlabels = generateVerlabels(graphheight);
			}

			// vertical labels
			paint.setTextAlign(Align.LEFT);
			int vers = verlabels.length - 1;
			for (int i = 0; i < verlabels.length; i++) {
				float y = ((graphheight / vers) * i) + border;
				paint.setColor(Color.WHITE);
				canvas.drawText(verlabels[i], 0, y, paint);
			}
		}
	}

	protected final Paint paint;
	private String[] horlabels;
	private String[] verlabels;
	private String title;
	private boolean scrollable;
	private double viewportStart;
	private double viewportSize;
	private final View viewVerLabels;
	private ScaleGestureDetector scaleDetector;
	private boolean scalable;
	private NumberFormat numberformatter;
	private final List<GraphViewSeries> graphSeries;
	private boolean showLegend = false;
	private float legendWidth = 120;
	private LegendAlign legendAlign = LegendAlign.MIDDLE;
	private boolean manualYAxis;
	private double manualMaxYValue;
	private double manualMinYValue;

	/**
	 *
	 * @param context
	 * @param title [optional]
	 */
	public GraphView(Context context, String title) {
		super(context);
		setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		if (title == null)
			title = "";
		else
			this.title = title;

		paint = new Paint();
		graphSeries = new ArrayList<GraphViewSeries>();

		viewVerLabels = new VerLabelsView(context);
		addView(viewVerLabels);
		addView(new GraphViewContentView(context), new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1));
	}

	private GraphViewData[] _values(int idxSeries) {
		GraphViewData[] values = graphSeries.get(idxSeries).values;
		if (viewportStart == 0 && viewportSize == 0) {
			// all data
			return values;
		} else {
			// viewport
			List<GraphViewData> listData = new ArrayList<GraphViewData>();
			for (int i=0; i<values.length; i++) {
				if (values[i].valueX >= viewportStart) {
					if (values[i].valueX > viewportStart+viewportSize) {
						listData.add(values[i]); // one more for nice scrolling
						break;
					} else {
						listData.add(values[i]);
					}
				} else {
					if (listData.isEmpty()) {
						listData.add(values[i]);
					}
					listData.set(0, values[i]); // one before, for nice scrolling
				}
			}
			return listData.toArray(new GraphViewData[listData.size()]);
		}
	}

	public void addSeries(GraphViewSeries series) {
		series.addGraphView(this);
		graphSeries.add(series);
	}

	protected void drawLegend(Canvas canvas, float height, float width) {
		int shapeSize = 15;

		// rect
		paint.setARGB(180, 100, 100, 100);
		float legendHeight = (shapeSize+5)*graphSeries.size() +5;
		float lLeft = width-legendWidth - 10;
		float lTop;
		switch (legendAlign) {
		case TOP:
			lTop = 10;
			break;
		case MIDDLE:
			lTop = height/2 - legendHeight/2;
			break;
		default:
			lTop = height - GraphViewConfig.BORDER - legendHeight -10;
		}
		float lRight = lLeft+legendWidth;
		float lBottom = lTop+legendHeight;
		canvas.drawRoundRect(new RectF(lLeft, lTop, lRight, lBottom), 8, 8, paint);

		for (int i=0; i<graphSeries.size(); i++) {
			paint.setColor(graphSeries.get(i).style.color);
			canvas.drawRect(new RectF(lLeft+5, lTop+5+(i*(shapeSize+5)), lLeft+5+shapeSize, lTop+((i+1)*(shapeSize+5))), paint);
			if (graphSeries.get(i).description != null) {
				paint.setColor(Color.WHITE);
				paint.setTextAlign(Align.LEFT);
				canvas.drawText(graphSeries.get(i).description, lLeft+5+shapeSize+5, lTop+shapeSize+(i*(shapeSize+5)), paint);
			}
		}
	}

	abstract public void drawSeries(Canvas canvas, GraphViewData[] values, float graphwidth, float graphheight, float border, double minX, double minY, double diffX, double diffY, float horstart);

	/**
	 * formats the label
	 * can be overwritten
	 * @param value x and y values
	 * @param isValueX if false, value y wants to be formatted
	 * @return value to display
	 */
	protected String formatLabel(double value, boolean isValueX) {
		if (numberformatter == null) {
			numberformatter = NumberFormat.getNumberInstance();
			double highestvalue = getMaxY();
			double lowestvalue = getMinY();
			if (highestvalue - lowestvalue < 0.1) {
				numberformatter.setMaximumFractionDigits(6);
			} else if (highestvalue - lowestvalue < 1) {
				numberformatter.setMaximumFractionDigits(4);
			} else if (highestvalue - lowestvalue < 20) {
				numberformatter.setMaximumFractionDigits(3);
			} else if (highestvalue - lowestvalue < 100) {
				numberformatter.setMaximumFractionDigits(1);
			} else {
				numberformatter.setMaximumFractionDigits(0);
			}
		}
		return numberformatter.format(value);
	}

	private String[] generateHorlabels(float graphwidth) {
		int numLabels = (int) (graphwidth/GraphViewConfig.VERTICAL_LABEL_WIDTH);
		String[] labels = new String[numLabels+1];
		double min = getMinX(false);
		double max = getMaxX(false);
		for (int i=0; i<=numLabels; i++) {
			labels[i] = formatLabel(min + ((max-min)*i/numLabels), true);
		}
		return labels;
	}

	synchronized private String[] generateVerlabels(float graphheight) {
		int numLabels = (int) (graphheight/GraphViewConfig.HORIZONTAL_LABEL_HEIGHT);
		String[] labels = new String[numLabels+1];
		double min = getMinY();
		double max = getMaxY();
		for (int i=0; i<=numLabels; i++) {
			labels[numLabels-i] = formatLabel(min + ((max-min)*i/numLabels), false);
		}
		return labels;
	}

	public LegendAlign getLegendAlign() {
		return legendAlign;
	}

	public float getLegendWidth() {
		return legendWidth;
	}

	/**
	 * returns the maximal X value of the current viewport (if viewport is set)
	 * otherwise maximal X value of all data.
	 * @param ignoreViewport
	 *
	 * warning: only override this, if you really know want you're doing!
	 */
	protected double getMaxX(boolean ignoreViewport) {
		// if viewport is set, use this
		if (!ignoreViewport && viewportSize != 0) {
			return viewportStart+viewportSize;
		} else {
			// otherwise use the max x value
			// values must be sorted by x, so the last value has the largest X value
			double highest = 0;
			if (graphSeries.size() > 0)
			{
				GraphViewData[] values = graphSeries.get(0).values;
				highest = values[values.length-1].valueX;
				for (int i=1; i<graphSeries.size(); i++) {
					values = graphSeries.get(i).values;
					highest = Math.max(highest, values[values.length-1].valueX);
				}
			}
			return highest;
		}
	}

	/**
	 * returns the maximal Y value of all data.
	 *
	 * warning: only override this, if you really know want you're doing!
	 */
	protected double getMaxY() {
		double largest;
		if (manualYAxis) {
			largest = manualMaxYValue;
		} else {
			largest = Integer.MIN_VALUE;
			for (int i=0; i<graphSeries.size(); i++) {
				GraphViewData[] values = _values(i);
				for (int ii=0; ii<values.length; ii++)
					if (values[ii].valueY > largest)
						largest = values[ii].valueY;
			}
		}
		return largest;
	}

	/**
	 * returns the minimal X value of the current viewport (if viewport is set)
	 * otherwise minimal X value of all data.
	 * @param ignoreViewport
	 *
	 * warning: only override this, if you really know want you're doing!
	 */
	protected double getMinX(boolean ignoreViewport) {
		// if viewport is set, use this
		if (!ignoreViewport && viewportSize != 0) {
			return viewportStart;
		} else {
			// otherwise use the min x value
			// values must be sorted by x, so the first value has the smallest X value
			double lowest = 0;
			if (graphSeries.size() > 0)
			{
				GraphViewData[] values = graphSeries.get(0).values;
				lowest = values[0].valueX;
				for (int i=1; i<graphSeries.size(); i++) {
					values = graphSeries.get(i).values;
					lowest = Math.min(lowest, values[0].valueX);
				}
			}
			return lowest;
		}
	}

	/**
	 * returns the minimal Y value of all data.
	 *
	 * warning: only override this, if you really know want you're doing!
	 */
	protected double getMinY() {
		double smallest;
		if (manualYAxis) {
			smallest = manualMinYValue;
		} else {
			smallest = Integer.MAX_VALUE;
			for (int i=0; i<graphSeries.size(); i++) {
				GraphViewData[] values = _values(i);
				for (int ii=0; ii<values.length; ii++)
					if (values[ii].valueY < smallest)
						smallest = values[ii].valueY;
			}
		}
		return smallest;
	}

	public boolean isScrollable() {
		return scrollable;
	}

	public boolean isShowLegend() {
		return showLegend;
	}

	public void redrawAll() {
		verlabels = null;
		horlabels = null;
		numberformatter = null;
		invalidate();
		viewVerLabels.invalidate();
	}

	public void removeSeries(GraphViewSeries series)
	{
		graphSeries.remove(series);
	}

	public void removeSeries(int index)
	{
		if (index < 0 || index >= graphSeries.size())
		{
			throw new IndexOutOfBoundsException("No series at index " + index);
		}

		graphSeries.remove(index);
	}

	public void scrollToEnd() {
		if (!scrollable) throw new IllegalStateException("This GraphView is not scrollable.");
		double max = getMaxX(true);
		viewportStart = max-viewportSize;
		redrawAll();
	}

	/**
	 * set's static horizontal labels (from left to right)
	 * @param horlabels if null, labels were generated automatically
	 */
	public void setHorizontalLabels(String[] horlabels) {
		this.horlabels = horlabels;
	}

	public void setLegendAlign(LegendAlign legendAlign) {
		this.legendAlign = legendAlign;
	}

	public void setLegendWidth(float legendWidth) {
		this.legendWidth = legendWidth;
	}

	/**
	 * you have to set the bounds {@link #setManualYAxisBounds(double, double)}. That automatically enables manualYAxis-flag.
	 * if you want to disable the menual y axis, call this method with false.
	 * @param manualYAxis
	 */
	public void setManualYAxis(boolean manualYAxis) {
		this.manualYAxis = manualYAxis;
	}

	/**
	 * set manual Y axis limit
	 * @param max
	 * @param min
	 */
	public void setManualYAxisBounds(double max, double min) {
		manualMaxYValue = max;
		manualMinYValue = min;
		manualYAxis = true;
	}

	/**
	 * this forces scrollable = true
	 * @param scalable
	 */
	synchronized public void setScalable(boolean scalable) {
		this.scalable = scalable;
		if (scalable == true && scaleDetector == null) {
			scrollable = true; // automatically forces this
			scaleDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
				@Override
				public boolean onScale(ScaleGestureDetector detector) {
					double center = viewportStart + viewportSize / 2;
					viewportSize /= detector.getScaleFactor();
					viewportStart = center - viewportSize / 2;

					// viewportStart must not be < minX
					double minX = getMinX(true);
					if (viewportStart < minX) {
						viewportStart = minX;
					}

					// viewportStart + viewportSize must not be > maxX
					double maxX = getMaxX(true);
					double overlap = viewportStart + viewportSize - maxX;
					if (overlap > 0) {
						// scroll left
						if (viewportStart-overlap > minX) {
							viewportStart -= overlap;
						} else {
							// maximal scale
							viewportStart = minX;
							viewportSize = maxX - viewportStart;
						}
					}
					redrawAll();
					return true;
				}
			});
		}
	}

	/**
	 * the user can scroll (horizontal) the graph. This is only useful if you use a viewport {@link #setViewPort(double, double)} which doesn't displays all data.
	 * @param scrollable
	 */
	public void setScrollable(boolean scrollable) {
		this.scrollable = scrollable;
	}

	public void setShowLegend(boolean showLegend) {
		this.showLegend = showLegend;
	}

	/**
	 * set's static vertical labels (from top to bottom)
	 * @param verlabels if null, labels were generated automatically
	 */
	public void setVerticalLabels(String[] verlabels) {
		this.verlabels = verlabels;
	}

	/**
	 * set's the viewport for the graph.
	 * @param start x-value
	 * @param size
	 */
	public void setViewPort(double start, double size) {
		viewportStart = start;
		viewportSize = size;
	}
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

