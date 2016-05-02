package mailhenvendelser;

import java.awt.BasicStroke;
import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

public class MChartPanel extends ChartPanel{
	
	private static final long serialVersionUID = 1L;
	private MOracle mOracle;
	private DefaultCategoryDataset dataset;
	private JFreeChart chart;
	
	
	
	public MChartPanel(JFreeChart chart,MOracle mOracle) {
		super(chart);
		this.chart = chart;
		this.mOracle = mOracle;
		this.chart = createChart(dataset,"");
		this.setChart(this.chart);
		this.setBorder(BorderFactory.createLineBorder(Color.black));
		
	}

    public void loadGraphData(String start, String slut, boolean isSunday,String period, boolean snit) {
        
    	dataset = new DefaultCategoryDataset();

    		ResultSet rs;
    		String title;
    		if (snit){
    			title = "Antal mails i snit pr. time - " + period;
    			rs = mOracle.getAverage(start,slut,isSunday);
    		}else{
    			title = "Antal mails total pr. time - " + period;
    			rs = mOracle.getTotals(start,slut,isSunday);
    		}
    		
        	try {
				while(rs.next()) {
					dataset.addValue(rs.getInt("B9_IND"), "MS total", rs.getString("KL"));
				}
			} catch (SQLException e) {e.printStackTrace();}

        	
        	
			chart = createChart(dataset,title);
	        setChart(chart);   
	        
    }

    private JFreeChart createChart(CategoryDataset dataset,String title) {
        // create the chart...
        JFreeChart chart = ChartFactory.createLineChart(
        	title,       // chart title
            null,                    // domain axis label
            null,              // range axis label
            dataset,                   // data
            PlotOrientation.VERTICAL,  // orientation
            true,                      // include legend
            true,                      // tooltips
            false                      // urls
        );
        

        chart.setBackgroundPaint(Color.white);

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setRangeGridlinePaint(Color.black);

        // customise the range axis...
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setAutoRangeIncludesZero(true);
        
        // customise the renderer...
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        //renderer.setDrawShapes(true);

        
        renderer.setSeriesStroke(
                0,
                new BasicStroke(
	                2.0f,
	                BasicStroke.CAP_ROUND,
	                BasicStroke.JOIN_ROUND,
	                1.0f,
	        		new float[] {0.0f, 1.0f},
	                0.0f
                )
            );
        
        renderer.setSeriesStroke(
            1,
            new BasicStroke(
	        		2.0f,
	        		BasicStroke.CAP_ROUND,
	        		BasicStroke.JOIN_ROUND,
	        		1.0f,
	        		new float[] {0.0f, 1.0f},
	        		0.0f
            	)
        		);
        renderer.setSeriesStroke(
                2,
                new BasicStroke(
    	        		2.0f,
    	        		BasicStroke.CAP_ROUND,
    	        		BasicStroke.JOIN_ROUND,
    	        		1.0f,
    	        		new float[] {0.0f, 1.0f},
    	        		0.0f
                	)
            		);
        renderer.setSeriesStroke(
                3,
                new BasicStroke(
    	        		2.0f,
    	        		BasicStroke.CAP_ROUND,
    	        		BasicStroke.JOIN_ROUND,
    	        		1.0f,
    	        		new float[] {0.0f, 1.0f},
    	        		0.0f
                	)
            		);

 
        
        return chart;
    }
}

