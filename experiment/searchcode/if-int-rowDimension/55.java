* @serial row dimension.
*
*/
private int rowDimension;

/**
* Column dimension
* @serial column dimension.
columnDimension = A[0].length;
for (int i = 0; i < rowDimension; i++) {
if (A[i].length != columnDimension) {
throw new IllegalArgumentException(

