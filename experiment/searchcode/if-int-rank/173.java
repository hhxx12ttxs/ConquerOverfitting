package matrix;

import org.junit.Test;
import junit.framework.*;

public class Matrix {
    private double[][] cell;
    private int width, height;
  
    public Matrix() {
        width = 0;
        height = 0;
    }
    
    public Matrix(int _height, int _width) {
        width = _width;
        height = _height;
        cell = new double[height][width];
    }
    
    public Matrix(int _height, int _width, int anyValue) {
        width = _width;
        height = _height;
        cell = new double[height][width];
        
        for(int i = 0; i < height; i++)
            for(int j = 0; j < width; j++)
                cell[i][j] = anyValue;
    }
    
    public Matrix(double[][] matrix) {
        width = matrix.length;
        height = matrix.length;
        cell = (double[][])matrix.clone();
    }
    
    public void SetCell(int _height, int _width, int value) {
        cell[_height][_width] = value;
    }
    
    public void Assign(Matrix matrix) {
        if(height != matrix.height || width != matrix.width) return;
        
        for(int i = 0; i < height; i++) 
            for(int j = 0; j < width; j++) 
                cell[i][j] = matrix.cell[i][j];
    }
    
    public void Assign(double[][] matrix) {
        if(height != matrix.length || width != matrix.length) return;
        
        for(int i = 0; i < height; i++) 
            for(int j = 0; j < width; j++) 
                cell[i][j] = matrix[i][j];
    }
    
    public boolean Equal(Matrix matrix) {
        if(height != matrix.height || width != matrix.width) return false;
        
        for(int i = 0; i < height; i++) 
            for(int j = 0; j < width; j++) 
                if(cell[i][j] != matrix.cell[i][j])
                    return false;
                
        return true;
    }
    
    public boolean Equal(double[][] matrix) {
        if(height != matrix.length || width != matrix.length) return false;
        
        double EPS = 0.1;
        
        for(int i = 0; i < height; i++) 
            for(int j = 0; j < width; j++) 
                if(Math.abs(cell[i][j] - matrix[i][j]) > EPS)
                    return false;
                
        return true;
    }
            
    public void Print() {
        for(int i = 0; i < height; i++) {
            for(int j = 0; j < width; j++)
                System.out.print(cell[i][j] + " ");
            System.out.println();
        }
    }
    
    public Matrix Add(Matrix matrix) {
        if(height != matrix.height || width != matrix.width) return this;
        
        Matrix anyMatrix = new Matrix(height, width);
        anyMatrix.Assign(this);
        
        for(int i = 0; i < height; i++)
            for(int j = 0; j < width; j++)
                anyMatrix.cell[i][j] += matrix.cell[i][j];
        
        return anyMatrix;
    }
    
    public Matrix Subtract(Matrix matrix) {
        if(this.height != matrix.height || width != matrix.width) return this;
        
        Matrix anyMatrix = new Matrix(height, width);
        anyMatrix.Assign(this);
        
        for(int i = 0; i < height; i++)
            for(int j = 0; j < width; j++)
                anyMatrix.cell[i][j] -= matrix.cell[i][j];
        
        return anyMatrix;
    }        
            
    public Matrix Multiply(Matrix matrix) {
        if(width != matrix.height) return this;
        int anyCell;
        
        Matrix anyMatrix = new Matrix(height, width);
        anyMatrix.Assign(this);
        
        for(int i = 0; i < height; i++)
            for(int j = 0; j < width; j++) {
                anyCell = 0;
                for(int k = 0; k < width; k++)
                    anyCell += cell[i][k] * matrix.cell[k][j];
                
                anyMatrix.cell[i][j] = anyCell;
            }
        return anyMatrix;
    }
    
    public Matrix Multiply(double number) {
        Matrix anyMatrix = new Matrix(height, width);
        anyMatrix.Assign(this);
        
        for(int i = 0; i < height; i++)
            for(int j = 0; j < width; j++) 
                anyMatrix.cell[i][j] *= number;
        
        return anyMatrix;
    }
    
    public Matrix Multiply(double [] column) {
        if(width != column.length) return this;
        int anyCell;
        
        Matrix anyMatrix = new Matrix(height, width);
        anyMatrix.Assign(this);
        
        for(int i = 0; i < height; i++)
            for(int j = 0; j < width; j++) {
                anyCell = 0;
                for(int k = 0; k < width; k++)
                    anyCell += cell[i][k] * column[k];
                            
                anyMatrix.cell[i][j] = anyCell;
            }
        return anyMatrix;
    }
    
    public Matrix Transpose() {
        Matrix anyMatrix = new Matrix(height, width);
        anyMatrix.Assign(this);
        
        for(int i = 0; i < height; i++)
            for(int j = 0; j < width; j++)
                anyMatrix.cell[i][j] = cell[j][i];
        
        return anyMatrix;
    }
    
    public static double CalculateDeterminant(double[][] matrix){
        double calcResult = 0;
        if (matrix.length == 2) {
            calcResult = matrix[0][0] * matrix[1][1] - matrix[1][0] * matrix[0][1];
        }
        else {
            int K;
            for(int i = 0; i < matrix.length; i++) {
                if(i % 2 == 1) 
                    K = -1; 
                else 
                    K = 1;             
                calcResult += K * matrix[0][i] * CalculateDeterminant(GetMinor(matrix, 0, i)); 
            }
        }
        return calcResult;
    }

    private static double[][] GetMinor(double[][] matrix, int row, int column){
        int minorLength = matrix.length - 1;
        double[][] minor = new double[minorLength][minorLength];
        int dI = 0;
        int dJ;
        
        for(int i = 0; i <= minorLength; i++) {
            dJ = 0;
            for(int j = 0; j <= minorLength; j++) {
                if(i == row)
                    dI = 1;
                else
                    if(j == column)
                        dJ = 1;
                    else
                        minor[i - dI][j - dJ] = matrix[i][j];
            }
        }
        return minor;
    }
    
    public double Determinant() {
        return CalculateDeterminant(cell.clone());
    }
    
    public Matrix Inverse(){
        int i, j, k;
        int size = width;
        Matrix E = new Matrix(size, size);
        Matrix A = new Matrix(height, width);
        A.Assign(this);
        
        for (i = 0; i < size; i++){
            for (j = 0; j < size; j++){    
                if (i == j) 
                    E.cell[i][j] = 1;
                else 
                    E.cell[i][j] = 0;
            }
        }
        
        for (k = 0; k < size; k++) {    
            for (j = k + 1; j < size; j++) {
                A.cell[k][j] = A.cell[k][j] / A.cell[k][k];
            }
            
            for (j = 0; j < size; j++) {
                E.cell[k][j] = E.cell[k][j] / A.cell[k][k];
            }
            
            A.cell[k][k] = A.cell[k][k] / A.cell[k][k];
            
            if (k > 0) {
                for (i = 0; i < k; i++) {
                    for (j = 0; j < size; j++) {
                        E.cell[i][j] = E.cell[i][j] - E.cell[k][j] * A.cell[i][k];
                    }
                    for (j = size - 1; j >= k; j--){
                        A.cell[i][j] = A.cell[i][j] - A.cell[k][j] * A.cell[i][k];
                    }
                }    
            }

            for (i = k + 1; i < size; i++) {
                for (j = 0; j < size; j++) 
                    E.cell[i][j] = E.cell[i][j] - E.cell[k][j] * A.cell[i][k];
                
                for (j = size - 1; j >= k; j--)
                    A.cell[i][j] = A.cell[i][j] - A.cell[k][j] * A.cell[i][k];
            }
        }        
        return E;
    }    
    
    public static int max(int a, int b) { return a > b ? a : b; }
    
    public int Rank() {
        int n = width, m = height, rank = max(n, m);
        boolean[] lineUsed = new boolean[n];
        double[][] A = cell.clone();
        
        for(int i = 0; i < m; i++) {
            int j;
            
            for(j = 0; j < n; j++)
                if(!lineUsed[j] && Math.abs(A[j][i]) > 0)
                    break;
            
            if (j == n)
                --rank;
            else {
                lineUsed[j] = true;
                   
                for(int p = i + 1; p < m; p++)
                    A[j][p] /= A[j][i];
                   
                for(int k = 0; k < n; k++)
                    if(k != j && Math.abs(A[k][i]) > 0)
                        for(int p = i + 1; p < m; p++)
                            A[k][p] -= A[j][p] * A[k][i];
            }
        }
        return rank;
    }
    
    public Matrix Square() {
        Matrix anyMatrix = new Matrix(height, width);
        anyMatrix.Assign(this);
        
        return anyMatrix.Multiply(anyMatrix);
    }
    
    public int GetWidth()   { return width; }
    public int GetHeight()  { return height; }
    
    public static void test(boolean expr) {
        if(expr) System.out.println("Test successful");
        else System.out.println("Test error");
    }
    
    public static void main(String[] args) {
        double[][] matr1 = {{0, 3, 4, 6, 2}, {5, 2, 6, 4, 0}, {9, 5, 7, 2, 4}, {0, 4, 7, 1, 2}, {4, 5, 1, 2, 8}};
        double[][] matr2 = {{3, 6, 1, 4, 7}, {4, 0, 2, 1, 5}, {4, 7, 2, 1, 8}, {7, 3, 2, 7, 4}, {6, 0, 2, 5, 1}};
        double[][] matr3 = {{1, 2, 3, 4, 5}, {2, 4, 6, 8, 10}, {3, 6, 9, 12, 15}, {45, 68, 167, 352, 40}, {31, 23, 23, 44, 13}};
        
        Matrix matrix1 = new Matrix(matr1);
        Matrix matrix2 = new Matrix(matr2);
        
        double[][] resultAddMatrix = {{3, 9, 5, 10, 9}, {9, 2, 8, 5, 5}, {13, 12, 9, 3, 12}, {7, 7, 9, 8, 6}, {10, 5, 3, 7, 9}};
        double[][] resultSubtractMatrix = {{3, 3, -3, -2, 5}, {-1, -2, -4, -3, 5}, {-5, 2, -5, -1, 4}, {7, -1, -5, 6, 2}, {2, -5, 1, 3, -7}};
        double[][] resultMyltiplyMatrix = {{82, 46, 30, 59, 73}, {75, 84, 29, 56, 109}, {113, 109, 45, 82, 156}, {63, 52, 28, 28, 82}, {98, 37, 36, 76, 77}};
        
        double[][] resultTransposeMatrix = {{3, 4, 4, 7, 6}, {6, 0, 7, 3, 0}, {1, 2, 2, 2, 2}, {4, 1, 1, 7, 5}, {7, 5, 8, 4, 1}};
        double[][] resultInverseMatrix1 = {{0.02, -0.05, 0.16, -0.12, -0.05}, {1.3, -2.15, 1.67, -0.41, -1.06}, {-0.54, 0.86, -0.65, 0.31, 0.38}, {0.14, 0.09, -0.06, -0.1, 0.02}, {-0.79, 1.24, -1.03, 0.31, 0.76}};
        double[][] resultInverseMatrix2 = {{-1.33, 0.17, 0.31, 1.95, -1.76}, {0, -0.25, 0.19, -0.12, 0.17}, {2.33, -0.42, -0.19, -4.22, 4.16}, {0.67, -0.08, -0.27, -0.71, 0.72}, {0, 0.25, -0.12, 0.27, -0.4}};
        double[][] resultSquareMatrix = {{59, 60, 90, 30, 44}, {64, 65, 102, 54, 42}, {104, 100, 133, 98, 82}, {91, 57, 82, 35, 46}, {66, 75, 75, 64, 80}};
        
        //Тест на сравнение матриц
        test(matrix1.Equal(matr1));
        test(matrix2.Equal(matr2));
        
        //Тест на сложение матриц
        test(matrix1.Add(matrix2).Equal(resultAddMatrix));
        test(matrix1.Equal(matr1));
        
        //Тест на разность матриц
        test(matrix2.Subtract(matrix1).Equal(resultSubtractMatrix));
        test(matrix2.Equal(matr2));
        
        //Тест на произведение матриц
        test(matrix1.Multiply(matrix2).Equal(resultMyltiplyMatrix));
        test(matrix1.Equal(matr1));
        
        //Тест на транспонирование матрицы
        test(matrix2.Transpose().Equal(resultTransposeMatrix));
        test(matrix2.Equal(matr2));
        
        //Тест на нахождение определителя
        test(matrix1.Determinant() == -1048);  
        test(matrix2.Determinant() == -156);  
        test(matrix1.Equal(matr1));
        test(matrix2.Equal(matr2));
        
        //Тест на нахождение обратной матрицы
        test(matrix1.Inverse().Equal(resultInverseMatrix1));
        test(matrix2.Inverse().Equal(resultInverseMatrix2));
        test(matrix1.Equal(matr1));
        test(matrix2.Equal(matr2));
        
        //Тест на ранг матрицы
        Matrix matrix3 = new Matrix(matr3);
        test(matrix3.Equal(matr3));
        test(matrix3.Rank() == 3);
        test(matrix3.Equal(matr3));
        
        //Тест на степень матрицы
        test(matrix1.Square().Equal(resultSquareMatrix));
        test(matrix1.Equal(matr1));
    }
}
