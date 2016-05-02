package Mcube;

import Mcube.Number;
import java.math.*;


import cn.ict.cn.galios.Rational;

public class Matrix<T extends Number<T>> {

	public static class MatrixException extends Exception {
		private static final long serialVersionUID = 1L;
	}
	public static class MatrixMulUnmatchException extends Exception {
		private static final long serialVersionUID = 1L;
	}
	public static class MatrixIsNotInversebleException extends Exception {
		private static final long serialVersionUID = 1L;
	}
	private T[][] innerM;
	private T one;
	private T zero;
	
	private int cols, rows;
	
	public int getCols() {
		return cols;
	}
	
	public int getRows() {
		return rows;
	}
	
	public Matrix(int rows, int cols, T one, T zero) throws Exception {
		innerM = zero.createMatrix(rows, cols);
		this.cols = cols;
		this.rows = rows;
		this.one = one;
		this.zero = zero;
	}
	
	public void set(int x, int y, T n) {
		innerM[x][y] = n;
	}
	
	public T get(int x, int y) {
		return innerM[x][y];
	}
	
	// Matrix multiplication function 
	public Matrix<T> mul(Matrix<T> b) throws Exception {
		Matrix<T> a = this;
		if (a.cols != b.rows) {
			throw new MatrixMulUnmatchException();
		}
		
		Matrix<T> result = new Matrix<T>(a.rows, b.cols, one, zero);
		
		for (int x = 0; x < a.rows; x++) {
			for (int y = 0; y < b.cols; y++) {
				T val = zero;
				for (int i = 0; i < a.cols; i++) {
					val = val.add(a.get(x, i).mul(b.get(i, y)));
				}
				result.set(x, y, val);
			}
		}
		return result;
	}
	
	// Matrix inverse function
	public Matrix<T> getVer () throws Exception {
		if (this.cols != this.rows) {
			throw new MatrixIsNotInversebleException();
		}
		Matrix<T> ex = this.getEx();
		
		// set the diagonal line elements of the Matrix as "1" 
		for (int r = 0; r < rows; r++) {
			T c = ex.get(r, r);
			if (!c.isOne()) {
				ex.set(r, r, this.one);
				for (int j = r + 1; j < ex.cols; j++) {
					ex.set(r, j, ex.get(r, j).div(c));
				}
			}
			
			// set the lower triangular matrix elements as "0"
			for (int r2 = r + 1; r2 < rows; r2++) {
				T key = ex.get(r2, r);
				for (int j = r; j < ex.cols; j++) {
					T a = ex.get(r, j);
					T b = ex.get(r2, j);
					T out = b.minus(a.mul(key));
					ex.set(r2, j, out);
				}
			}
		}
		
		// set the uper triangular matrix elements as "0"
		for (int r = rows - 1; r >= 0; r--) {
			for (int r2 = 0; r2 < r; r2 ++)  {
				T key = ex.get(r2, r);
				for (int c = r; c < ex.cols; c++) {
					T a = ex.get(r2, c);
					T b = ex.get(r, c);
					T x = key.mul(b);
					ex.set(r2, c, a.minus(x));
				}
			}
		}
		return ex.getVMatrix();
	}
	
	/*this function is for n*n Matrix extend to n*(2*n) martix, 
	 * and the right hand of this extended matrix is a basic Matrix E
	 */
	public Matrix<T> getEx() throws Exception {
		if (this.cols != this.rows) {
			throw new MatrixIsNotInversebleException();
		}
		
		int n = this.cols;
		
		Matrix<T> Ex = new Matrix<T>(n, 2 * n, one, zero);
		
		for (int i=0; i<n; i++) {
			for (int j=0; j<n; j++) {
				Ex.set(i, j, this.get(i, j));
			}
			
			for (int j=n; j<2 * n; j++) {
				if ((j-n) == i)
					Ex.set(i, j, one);
				else 
					Ex.set(i, j, zero);
			}
		}
		
		return Ex;
	}
	
	// get the inverse matrix from the transformed extended matrix 
	public Matrix<T> getVMatrix() throws Exception {
		if (this.cols != 2 * this.rows) {
			throw new MatrixIsNotInversebleException();
		}

		int n = this.rows;
		Matrix<T> v = new Matrix<T>(n, n, one, zero);
		
		for(int i=0; i<n; i++) {
			for(int j=0; j<n; j++) {
				v.set(i, j, this.get(i, (j+n)));
			}
		}
		return v;
	}
	
	public String toString() {
		String r = "";
		for (int x = 0; x < rows; x++) {
			for (int y = 0; y < cols; y++) {
				r += String.format("%s ", get(x, y).toString());
			}
			r += "\n";
		}
		return r;
	}
	
		
}

