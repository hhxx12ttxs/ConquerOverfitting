package Charter;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Iterator;
import java.util.Vector;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.LegendItemEntity;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;


public class MyChart extends ApplicationFrame
implements ChartMouseListener {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = -5115305355688347417L;
	
	private JFreeChart chart;
    private String name;
	private Vector<XYSeries> xySeries;
	private XYSeriesCollection xyDataset;
	
	public MyChart(String s) {
        super(s);
        name = s;
        xySeries = new Vector<XYSeries>(); 
        xyDataset = new XYSeriesCollection();
	}
	
	public void addSeries(XYSeries xy) {
		xySeries.add(xy);
	}
	
	private void seriesToCollection() {
		Iterator<XYSeries> e = xySeries.iterator();
		while (e.hasNext()) {
			xyDataset.addSeries(e.next());
		}
	}
	
	public void drawChart() {
		seriesToCollection();
		chart = ChartFactory.createXYLineChart(name, "X", "Y", xyDataset, PlotOrientation.VERTICAL, true, true, true);
        //chart.addSubtitle(new TextTitle("Click on the legend to see series highlighted..."));

        XYPlot xyplot = (XYPlot)chart.getPlot();
        /*NumberAxis numberAxis = (NumberAxis) xyplot.getDomainAxis();
        numberAxis.setTickUnit(new NumberTickUnit(0.5));*/
        XYLineAndShapeRenderer xylineandshaperenderer = (XYLineAndShapeRenderer)xyplot.getRenderer();
        xylineandshaperenderer.setBaseShapesFilled(true);
        xylineandshaperenderer.setDrawOutlines(true);
        xyplot.getRenderer().setSeriesPaint(0, Color.RED);
        xyplot.getRenderer().setSeriesPaint(1, Color.BLUE);
        xyplot.getRenderer().setSeriesPaint(2, Color.GREEN);
        xyplot.getRenderer().setSeriesPaint(3, Color.MAGENTA);
        
        ChartPanel chartpanel = new ChartPanel(chart);
        chartpanel.setPreferredSize(new Dimension(500, 270));
        chartpanel.setMouseZoomable(true);
        chartpanel.setMouseWheelEnabled(true);
        chartpanel.addChartMouseListener(this);
        //chartpanel.addKeyListener(this);
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

