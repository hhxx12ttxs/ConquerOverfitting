private final Vector series=new Vector();
private int pos=0;
private DataSeries curSeries=null;

public DefaultCategoryGraph2DModel() {}
public void changeSeries(int i,float newSeries[]) {
series.setElementAt(new DataSeries(newSeries),i);

