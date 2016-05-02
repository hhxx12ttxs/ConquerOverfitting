import java.awt.Color;

import javax.swing.JFrame;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.block.BlockContainer;
import org.jfree.chart.block.BorderArrangement;
import org.jfree.chart.block.EmptyBlock;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.title.CompositeTitle;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.RefineryUtilities;

public class Graph2D {
	/*@ specification Graph2D {

	///////////////////////////////////////////////////////////////////////////////
	// goal of this class: 														 //
	// * Allow the representation of the results in a 2D graph 					 //
	// * Allow the representation of sums of variables 							 //
	///////////////////////////////////////////////////////////////////////////////
	
	// ********************************* VARIABLES ********************************
	
	boolean bUseShapes, bUseSpline; int iYear;
	
	// control variables
	void drawingInitialized, done;
	
	// x-y variables: (String title, double[][] values)
	String sX;
	double[][] mdX;
	alias (Object) xSummed;
	alias (Object) ySingle1;
	alias (Object) ySummed1;
	alias (Object) ySingle2;
	alias (Object) ySummed2;
	
	// customization variables
	String sTitle, sXLabel, sY1Label, sY2Label;
	
	// ******************************* DEPENDENCIES *******************************
	sTitle, sXLabel, sY1Label, sY2Label, xSummed, ySingle1, ySummed1, ySingle2, ySummed2, bUseShapes, bUseSpline, iYear -> done {draw};
	
	// *********************************** GOALS **********************************
	-> done;
	
	}@*/

	private static final String _sTitle = "Graph2D";
	private static String[] _vsSeriesNames;
	private static String _sSeriesSummedName = "Sum-Y";

	public void draw(String sTitle, String sXLabel, String sY1Label, String sY2Label, Object[] voXSummed,
			Object[] voYSingle1, Object[] voYSummed1, Object[] voYSingle2, Object[] voYSummed2, 
			boolean bUseShapes, boolean bUseSpline, int iYear) {
		// create collections of datasets
		XYSeriesCollection collection1 = createDataset(voXSummed, voYSingle1, voYSummed1, iYear);
		XYSeriesCollection collection2 = createDataset(voXSummed, voYSingle2, voYSummed2, iYear);
		
		// create chart
		JFreeChart chart = createChart(collection1, collection2,
				sTitle, sXLabel, sY1Label, sY2Label,
				bUseShapes, bUseSpline);
		ChartPanel chartPanel = new ChartPanel(chart, true, true, true, true, true);
		JFrame frame = new JFrame(_sTitle);
		frame.setContentPane(chartPanel);
		// display the chart
		frame.pack();
		RefineryUtilities.centerFrameOnScreen(frame);
		frame.setVisible(true);
	}

	private XYSeriesCollection createDataset(Object[] voXSummed, Object[] voYSingle,
			Object[] voYSummed, int iYear) {
		// allows dataset/series collection
		XYSeriesCollection collection = new XYSeriesCollection();
		// series names ...
		_vsSeriesNames = new String[voYSingle.length];
		for (int i = 0; i < voYSingle.length; i++) {
			_vsSeriesNames[i] = (String) (((Object[]) (voYSingle[i]))[0]);
		}
		// Domain values are the same for all graphs ...
		double[] vdX = new double[((double[][]) (((Object[])(voXSummed[0]))[1])).length];
		for (int j = 0; j < vdX.length; j++){
			for (int i=0;i<voXSummed.length;i++){
				vdX[j] += ((double[][]) (((Object[]) (voXSummed[i]))[1]))[j][iYear-1];
			}
		}
		// Range values are different for each series ...
		for (int i = 0; i < _vsSeriesNames.length; i++) {
			XYSeries series = new XYSeries(_vsSeriesNames[i]);
			double[] vdY = new double[vdX.length];
			for (int j = 0; j < vdX.length; j++) {
				vdY[j] = ((double[][]) (((Object[]) (voYSingle[i]))[1]))[j][iYear-1];
			}
			for (int j = 0; j < vdX.length; j++) {
				series.add(vdX[j], vdY[j]);
			}
			collection.addSeries(series);
		}
		if (voYSummed.length != 0){
			XYSeries series = new XYSeries(_sSeriesSummedName);
			double[] vdY = new double[vdX.length];
			for (int j = 0; j < vdX.length; j++) {
				for (int i = 0; i < voYSummed.length; i++) {
					vdY[j] += ((double[][]) (((Object[]) (voYSummed[i]))[1]))[j][iYear-1];
				}
				series.add(vdX[j], vdY[j]);
			}
			collection.addSeries(series);
		}
		return collection;
	}

	@SuppressWarnings("deprecation")
	private JFreeChart createChart(XYSeriesCollection collection1, XYSeriesCollection collection2, 
			String sTitle, String sXLabel, String sY1Label, String sY2Label,
			boolean bUseShapes, boolean bUseSpline) {

		boolean bHas2Sets = true;
		
		XYSplineRenderer splineRenderer1 = new XYSplineRenderer();
		if (!bUseShapes) splineRenderer1.setBaseShapesVisible(false);
		splineRenderer1.setToolTipGenerator(new StandardXYToolTipGenerator());
		
		XYLineAndShapeRenderer lineRenderer1 = new XYLineAndShapeRenderer();
		if (!bUseShapes) lineRenderer1.setBaseShapesVisible(false);
		lineRenderer1.setToolTipGenerator(new StandardXYToolTipGenerator());
		
		XYSplineRenderer splineRenderer2 = new XYSplineRenderer();
		if (!bUseShapes) splineRenderer2.setBaseShapesVisible(false);
		splineRenderer2.setToolTipGenerator(new StandardXYToolTipGenerator());
		
		XYLineAndShapeRenderer lineRenderer2 = new XYLineAndShapeRenderer();
		if (!bUseShapes) lineRenderer2.setBaseShapesVisible(false);
		lineRenderer2.setToolTipGenerator(new StandardXYToolTipGenerator());
		
		NumberAxis xAxis  = new NumberAxis(sXLabel);
		xAxis.setAutoRangeIncludesZero(false);
		NumberAxis y1Axis = new NumberAxis(sY1Label);
		y1Axis.setAutoRangeIncludesZero(false);
		NumberAxis y2Axis = new NumberAxis(sY2Label);
		y2Axis.setAutoRangeIncludesZero(false);
		
		// swap datasets if only Y2 info available
		if (collection1.equals(null) != collection2.equals(null) ){
			bHas2Sets = false;
			if (collection1.equals(null)){ collection1 = collection2; }
		}
		XYPlot plot = (bUseSpline) ? new XYPlot(collection1, xAxis, y1Axis, splineRenderer1) 
								   : new XYPlot(collection1, xAxis, y1Axis,   lineRenderer1);
		
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		plot.setOrientation(PlotOrientation.VERTICAL);
		
		JFreeChart chart = new JFreeChart(sTitle, plot);
		
		if (bHas2Sets){
			// add second dataset
			plot.setDataset(1, collection2);
			plot.mapDatasetToRangeAxis(1, 1);
			plot.setRangeAxis(1, y2Axis);
			if (bUseSpline) plot.setRenderer(1, splineRenderer2);
			else            plot.setRenderer(1,   lineRenderer2);
			plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
			
			// modify legend
			chart.removeLegend();
			
			LegendTitle legend1 = new LegendTitle(plot.getRenderer(0));
	        legend1.setMargin(new RectangleInsets(2, 2, 2, 2));
	        legend1.setFrame(new BlockBorder());

	        LegendTitle legend2 = new LegendTitle(plot.getRenderer(1));
	        legend2.setMargin(new RectangleInsets(2, 2, 2, 2));
	        legend2.setFrame(new BlockBorder());

	        BlockContainer container = new BlockContainer(new BorderArrangement());
	        container.add(legend1, RectangleEdge.LEFT);
	        container.add(legend2, RectangleEdge.RIGHT);
	        container.add(new EmptyBlock(2000, 0));
	        CompositeTitle legends = new CompositeTitle(container);
	        legends.setPosition(RectangleEdge.BOTTOM);
	        chart.addSubtitle(legends);
		}
		return chart;
	}

}
