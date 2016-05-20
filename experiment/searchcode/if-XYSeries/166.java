/*****************************************************************************
 *  Copyright (C) 2011 by Thomas Goossens - thomasgoossens.be				**
 *  																		**
 *  This program is free software: you can redistribute it and/or modify	**	
 *  it under the terms of the GNU General Public License as published by	**
 *  the Free Software Foundation, either version 3 of the License, or		**
 *  (at your option) any later version.										**
 *  																		**
 *  This program is distributed in the hope that it will be useful,			**
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of			**
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the			**
 *  GNU General Public License for more details.							**
 *																			**
 * You should have received a copy of the GNU General Public License		**
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>. 	**
 *******************************************************************************/

package plotting;

import java.io.File;
import java.io.IOException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.*;



public class Scatter {

	private String title;
	private  XYSeriesCollection dataset; //contains all series
	private String XAxisLabel;
	private String YAxisLabel;
	
	public Scatter(String title,String XAxisLabel, String YAxisLabel){
		this.title=title;
		this.XAxisLabel=XAxisLabel;
		this.YAxisLabel=YAxisLabel;
		dataset = new XYSeriesCollection();	
	}
	

    public void addDataSeries(String title, double[][] data){
    	XYSeries series = new XYSeries(title);
    	for(int i = 0;i<data.length;i++){
    		series.add(data[i][0],data[i][1]);
    	}
 
    	addToDataSet(series);
    }
 
    private void addToDataSet(XYSeries series){
    	this.dataset.addSeries(series);
    }
    
    
    public void createChart(String filename){
    	JFreeChart chart = ChartFactory.createXYLineChart(this.title, // Title
                this.XAxisLabel, // x-axis Label
                this.YAxisLabel, // y-axis Label
                dataset, // Dataset
                PlotOrientation.VERTICAL, // Plot Orientation
                true, // Show Legend
                true, // Use tooltips
                false // Configure chart to generate URLs?
            );
        try {
            ChartUtilities.saveChartAsPNG(new File(filename), chart, 800,
               600);
             
        } catch (IOException e) {
            System.err.println("Problem occurred creating chart.");
        }
    }
    
}
