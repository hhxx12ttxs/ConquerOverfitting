this.seriesList = new java.util.ArrayList();

if (series != null) {
this.seriesList.add(series);
series.addChangeListener(this);
}
}


public int getItemCount(int seriesIndex) {
return getSeries(seriesIndex).getItemCount();

