package jp.monochrome.Utility;

public class MyMatrix {
int nrow;
int ncol;
double[][] data;

public MyMatrix(int nr, int nc) {
nrow = nr;
ncol = nc;
data = new double[nrow][ncol];

