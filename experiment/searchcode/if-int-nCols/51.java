private int nrows;
private int ncols;
private BigInteger[][] data;

public Matrix(BigInteger[][] dat) {
this.ncols = dat[0].length;
}

public Matrix(int nrow, int ncol) {
this.nrows = nrow;
this.ncols = ncol;

