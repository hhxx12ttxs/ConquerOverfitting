newResult[col][row][2] = pixels[pixel];

if (isWithinThreshold(newResult[col][row])) {
colorPercentage[(int)(numX*((double)col/width))][(int)(numY*((double)row/height))]++;
}

col+=5;
if (col == width) {

