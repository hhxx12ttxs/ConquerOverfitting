private int[] computeFibonacciSeriesUptoControl(int control, int [] series){

if (series.length == control) {
return series;
}

int [] newSeries = Arrays.copyOf(series, series.length + 1);
if(series.length < 2){

