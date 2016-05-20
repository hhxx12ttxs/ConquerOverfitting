package ru.ifmo.ctddev.pyankova.task2;

import java.io.*;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Created with IntelliJ IDEA.
 * User: yulya3102
 * Date: 20.02.13
 * Time: 17:17
 * To change this template use File | Settings | File Templates.
 */
public class Matrix {
    private int matrix[][] = null;
    public final int rows;
    public final int columns;

    public Matrix(int rows, int columns) {
        if (rows > 0 && columns > 0) {
            this.rows = rows;
            this.columns = columns;
            matrix = new int[rows][columns];
        } else {
            throw new InvalidMatrixSizeException(rows, columns);
        }
    }

    public Matrix(int[][] matrix) {
        if (matrix != null) {
            rows = matrix.length;
            if (rows > 0) {
                columns = matrix[0].length;
                if (columns > 0) {
                    this.matrix = new int[rows][columns];
                    for (int i = 0; i < rows; i++) {
                        for (int j = 0; j < columns; j++) {
                            this.matrix[i][j] = matrix[i][j];
                        }
                    }
                } else {
                    throw new InvalidMatrixSizeException(rows, columns);
                }
            } else {
                throw new InvalidMatrixSizeException(rows);
            }
        } else {
            throw new NullPointerException("Argument must not be null");
        }
    }

    public Matrix(File file) throws IOException {
        try (Scanner in = new Scanner(file)) {
            rows = in.nextInt();
            columns = in.nextInt();
            matrix = new int[rows][columns];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    set(in.nextInt(), i, j);
                }
            }
        } catch (NoSuchElementException exception) {
            throw new MatrixIncorrectInputException(exception);
        }
    }

    public Matrix(Scanner scanner) {
        try {
            rows = scanner.nextInt();
            columns = scanner.nextInt();
            if (rows > 0 && columns > 0) {
                matrix = new int[rows][columns];
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < columns; j++) {
                        matrix[i][j] = scanner.nextInt();
                    }
                }
            } else {
                throw new InvalidMatrixSizeException(rows, columns);
            }
        } catch (NoSuchElementException exception) {
            throw new MatrixIncorrectInputException(exception);
        }
    }

    public Matrix add(Matrix other) {
        if (other.rows == rows && other.columns == columns) {
            Matrix result = new Matrix(rows, columns);
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    result.set(get(i, j) + other.get(i, j), i, j);
                }
            }
            return result;
        } else {
            throw new MismatchingMatrixSizesException(this, other);
        }
    }

    public Matrix subtract(Matrix other) {
        if (other.rows == rows && other.columns == columns) {
            Matrix result = new Matrix(rows, columns);
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    result.set(get(i, j) - other.get(i, j), i, j);
                }
            }
            return result;
        } else {
            throw new MismatchingMatrixSizesException(this, other);
        }
    }

    public Matrix multiply(Matrix other) {
        if (columns == other.rows) {
            Matrix result = new Matrix(rows, other.columns);
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < other.columns; j++) {
                    int accumulator = 0;
                    for (int k = 0; k < columns; k++) {
                        accumulator += get(i, k) * other.get(k, j);
                    }
                    result.set(accumulator, i, j);
                }
            }
            return result;
        } else {
            throw new MismatchingMatrixSizesException(this, other);
        }
    }

    public Matrix scale(int ratio) {
        Matrix result = new Matrix(rows, columns);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result.set(matrix[i][j] * ratio, i, j);
            }
        }
        return result;
    }

    public int get(int row, int column) {
        if (0 <= row && row < rows && 0 <= column && column < columns) {
            return matrix[row][column];
        } else {
            throw new MatrixIndexOutOfBoundsException(this, row, column);
        }
    }

    public void set(int integer, int row, int column) {
        if (0 <= row && row < rows && 0 <= column && column < columns) {
            matrix[row][column] = integer;
        } else {
            throw new MatrixIndexOutOfBoundsException(this, row, column);
        }
    }

    public Matrix transpose() {
        Matrix result = new Matrix(columns, rows);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result.set(get(i, j), j, i);
            }
        }
        return result;
    }

    public void write(File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(rows + " " + columns);
            for (int i = 0; i < rows; i++) {
                writer.write("\n" + get(i, 0) + " ");
                for (int j = 1; j < columns; j++) {
                    writer.write(get(i, j) + " ");
                }
            }
        }
    }
}

