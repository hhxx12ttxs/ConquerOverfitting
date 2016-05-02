package com.cloudena.vo.plot;

import java.util.ArrayList;
import java.util.List;


public class DataSeriesSet {

    private List<DataSeries> series;
    
    public void addDataToSeries(DataSeries data){
        if (this.series == null)
            this.series = new ArrayList<DataSeries>();
        this.series.add(data);
    }

    public List<DataSeries> getSeries(){
        return this.series;
    }

    public void setSeries(List<DataSeries> series) {
        this.series = series;
    }
    
    public int size(){
    	if(series == null || series.isEmpty()) return 0;
    	return this.series.size();
    }

    @Override
    public String toString() {
        return "com.cloudena.vo.plot.DataSeriesSet [" + series + "]";
    }

}

