int     i, j, nrows, ncols, img[][];
double  Gx[][], Gy[][], G[][];

if (args.length != 6) {
System.exit(0);
}
nrows = Integer.parseInt(args[0]);
ncols = Integer.parseInt(args[1]);
img = new int[nrows][ncols];

