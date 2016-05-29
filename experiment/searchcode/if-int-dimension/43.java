diagonals += getOutsideNumber(i);
}

return diagonals;
}

private static int getOutsideNumber(int dimension) {

if (dimension == 1) {
return 1;
} else if (dimension % 2 == 1) {// only works for uneven dimension
int cornersSum = 4 * dimension * dimension;

