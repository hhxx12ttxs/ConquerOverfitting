for (int pixel : image.getPixelsArray())
data[pixel]++;
int threshold = -1;
int ih, it;
int first_bin;
double[] P2 = new double[256];
int total = 0;
for (ih = 0; ih < 256; ih++)
total += data[ih];

