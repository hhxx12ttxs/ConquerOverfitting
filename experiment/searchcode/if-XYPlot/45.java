package charter;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.LegendItemEntity;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.util.ShapeUtilities;


public class DotChart extends ApplicationFrame
implements ChartMouseListener {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = -5115305355688347417L;
	
	private JFreeChart chart;
    private String name;
    private ArrayList<XYSeries> xySeries;
	private XYSeriesCollection xyDataset;
	
	public DotChart(String s) {
        super(s);
        name = s;
        xySeries = new ArrayList<XYSeries>();
        xyDataset = new XYSeriesCollection();
	}
	
	public void addSeries(XYSeries xy) {
		xySeries.add(xy);
	}
	
	private void seriesToCollection() {
		for (XYSeries series : xySeries)
			xyDataset.addSeries(series);
	}
	
	public void drawChart() {
		seriesToCollection();
		//ChartFactory.createXYLineChart(name, "X", "Y", xyDataset, PlotOrientation.VERTICAL, true, true, true);
        //chart.addSubtitle(new TextTitle("Click on the legend to see series highlighted..."));
		chart = ChartFactory.createScatterPlot(name, "X", "Y", xyDataset, PlotOrientation.VERTICAL, true, true, true);
        XYPlot xyplot = (XYPlot)chart.getPlot();
        /*NumberAxis numberAxis = (NumberAxis) xyplot.getDomainAxis();
        numberAxis.setTickUnit(new NumberTickUnit(0.5));*/
        AbstractRenderer xydotrenderer = (AbstractRenderer)xyplot.getRenderer();
        xydotrenderer.setSeriesShape(0, ShapeUtilities.createDiamond(1), true);
        xydotrenderer.setSeriesShape(1, ShapeUtilities.createDiamond(1), true);
        
        
        xyplot.getRenderer().setSeriesPaint(0, Color.RED);
        xyplot.getRenderer().setSeriesPaint(1, Color.BLUE);
        xyplot.getRenderer().setSeriesPaint(2, Color.GREEN);
        xyplot.getRenderer().setSeriesPaint(3, Color.MAGENTA);
               
        
        ChartPanel chartpanel = new ChartPanel(chart);
        chartpanel.setPreferredSize(new Dimension(500, 500));
        chartpanel.setMouseZoomable(true);
        chartpanel.setMouseWheelEnabled(true);
        chartpanel.addChartMouseListener(this);
        
        
        setContentPane(chartpanel);
	}

	
	public void chartMouseClicked(ChartMouseEvent chartmouseevent)
    {
        org.jfree.chart.entity.ChartEntity chartentity = chartmouseevent.getEntity();
        if(chartentity != null && (chartentity instanceof LegendItemEntity))
        {
            LegendItemEntity legenditementity = (LegendItemEntity)chartentity;
            @SuppressWarnings("rawtypes")
			Comparable comparable = legenditementity.getSeriesKey();
            XYPlot xyplot = (XYPlot)chart.getPlot();
            XYDataset xydataset = xyplot.getDataset();
            XYItemRenderer xyitemrenderer = xyplot.getRenderer();
            for(int i = 0; i < xydataset.getSeriesCount(); i++)
            {
                xyitemrenderer.setSeriesStroke(i, new BasicStroke(1.0F));
                if(xydataset.getSeriesKey(i).equals(comparable))
                    xyitemrenderer.setSeriesStroke(i, new BasicStroke(2.0F));
            }
        }
    }


	@Override
	public void chartMouseMoved(ChartMouseEvent arg0) {
		
		
	}

	
}

