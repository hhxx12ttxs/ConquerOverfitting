package com.sandbox.algorithms;
public class Matrix {
 
    int input[];
    int ROWS, COLS;
     
    public static void main(String args[]) {
         
        int M = 4;
        int N = 3;
        Matrix matrix = new Matrix(M, N);
        System.out.println("Original Matrix -->");
        matrix.printMatrix(M, N);
        matrix.rotateBy90();
        System.out.println("After rotating by 90 -->");
        matrix.printMatrix(N, M);
         
    }
     
    public Matrix(int ROWS, int COLS) {
        this.ROWS = ROWS;
        this.COLS = COLS;
        input = new int[ROWS * COLS];
         
        fillMatrix();
    }
     
    public void fillMatrix() {
         
        int data = 1;
        for(int i = 0; i < ROWS * COLS; i++) {
            input[i] = data++;
        }
    }
     
    public void printMatrix(int ROWS, int COLS) {
         
        for(int i = 0; i < ROWS; i++) {
            for(int j = 0; j < COLS; j++) {
                System.out.print(input[i * COLS + j] + " ");
            }
            System.out.println();
        }
    }
     
    public void transpose() {
         
        int size = ROWS * COLS;
        int last = size - 1;
        int cols = ROWS;
         
        boolean[] visited = new boolean[size];
 
        int cycle = 0;
        while(++cycle != size) {
             
            if(visited[cycle]) {
                continue;
            }
            int a = cycle;
            do {
                a = (a == last) ? last : (cols * a) % last;
                // swap the element at a & cycle
                swap(a, cycle);
                visited[a] = true;
                 
            } while (a != cycle);
        }
    }
     
    public void swap(int a, int b) {
         
        int temp = input[a];
        input[a] = input[b];
        input[b] = temp;
    }
     
    public void rotateBy90() {
         
        //first transpose the matrix
        transpose();
         
        // swap the columns in the transposed matrix
        int cols = ROWS;
        int rows = COLS;
         
        for(int r = 0; r < rows; r++) {
            for(int i = 0, j = cols - 1;  i < j; i++, j--) {
                swap(r * cols + i, r * cols + j);
            }
        }
    }
}
