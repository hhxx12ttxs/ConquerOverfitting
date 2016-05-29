public static Complex[][] fft(Complex[][] x)
{
int nrows = x.length;
if (nrows < 1 || x[0].length < 1)
throw new IllegalArgumentException(Messages.getString(&quot;FFT2D.InvalidSize&quot;)); //$NON-NLS-1$
int ncols = x[0].length;
double[] data = new double[nrows * ncols * 2];

