private int sigLength;

public TimeSeriesMatrix(int rowDimension, int columnDimension){
this.sigMat = MatrixUtils.createRealMatrix(rowDimension, columnDimension);
this.numOfSig = rowDimension;
this.sigLength = columnDimension;
}

public TimeSeriesMatrix(int rowDimension, int columnDimension, double t_dt){

