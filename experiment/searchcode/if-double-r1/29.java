String output;
double[][] r1 = new double[2][2];		// r1[0][0] = //r1[x][leftmost or downmost]
double[][] r2 = new double[2][2];		// r2[y][rightmost or upmost]
r2height = input.nextDouble();
input.close();

// processing
r1[0][0] = r1x - r1width;
r1[1][0] = r1y - r1height;

