public class Matf {
private float[] data;
private int nCol; // number of columns
private int nRow; // number of columns

public Matf(int nRow, int nCol) {
data = new float[nRow * nCol];
this.nCol = nCol;

