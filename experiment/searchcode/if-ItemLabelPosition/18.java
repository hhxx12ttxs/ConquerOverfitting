package ubc.cpsc544;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

/**
 * A simple demonstration application showing how to create a horizontal bar
 * chart.
 * 
 */
public class BarChart /* extends ApplicationFrame */{

	private ChartPanel chartPanel;
	final double[][] data = new double[][] {
			{ 10000.0, 43000.0, 35000.0, 68000.0, 54000.0 },
			{ 41000.0, 33000.0, 22000.0, 34000.0, 62000.0 } };

	private static final int GREEN_THRESHOLD = 40000;
	private static final int YELLOW_THRESHOLD = 60000;
	
	/**
	 * A custom renderer that returns a different color for each item in a
	 * single series.
	 */
	class CustomRenderer extends BarRenderer {	

		/**
		 * Returns the paint for an item. Overrides the default behaviour
		 * inherited from AbstractSeriesRenderer.
		 * 
		 * @param row
		 *            the series.
		 * @param column
		 *            the category.
		 * 
		 * @return The item color.
		 */
		public Paint getItemPaint(final int row, final int column) {
			double val = data[row][column];
			if (val < GREEN_THRESHOLD)
				return Color.green;
			else if (val < YELLOW_THRESHOLD)
				return Color.yellow;
			else
				return Color.red;

		}
	}

	/**
	 * Creates a new demo instance.
	 * 
	 * @param title
	 *            the frame title.
	 */
	public BarChart() {
		final CategoryDataset dataset = createDataset();
		final JFreeChart chart = createChart(dataset);

		this.chartPanel = new ChartPanel(chart);
		getChartPanel().setPreferredSize(new java.awt.Dimension(500, 270));

	}

	/**
	 * Creates a sample dataset.
	 * 
	 * @return A dataset.
	 */
	private CategoryDataset createDataset() {
		// final double[][] data = new double[][] {
		// {1.0, 43.0, 35.0, 58.0, 54.0},
		// {54.0, 75.0, 63.0, 83.0, 43.0},
		// {41.0, 33.0, 22.0, 34.0, 62.0}
		// };
		// return DatasetUtilities.createCategoryDataset("Series ", "Floor ",
		// data);

		// row keys...
		final String series1 = "Current Month";
		final String series2 = "Last Month";

		// column keys...
		final String category1 = "Floor 5";
		final String category2 = "Floor 4";
		final String category3 = "Floor 3";
		final String category4 = "Floor 2";
		final String category5 = "Floor 1";

		// create the dataset...
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		dataset.addValue(data[0][0], series1, category1);
		dataset.addValue(data[0][1], series1, category2);
		dataset.addValue(data[0][2], series1, category3);
		dataset.addValue(data[0][3], series1, category4);
		dataset.addValue(data[0][4], series1, category5);

		dataset.addValue(data[1][0], series2, category1);
		dataset.addValue(data[1][1], series2, category2);
		dataset.addValue(data[1][2], series2, category3);
		dataset.addValue(data[1][3], series2, category4);
		dataset.addValue(data[1][4], series2, category5);

		return dataset;
	}

	/**
	 * Creates a chart.
	 * 
	 * @param dataset
	 *            the dataset.
	 * 
	 * @return A chart.
	 */
	private JFreeChart createChart(final CategoryDataset dataset) {

		final JFreeChart chart = ChartFactory.createBarChart("", // chart title
				"", // domain axis label
				"Energy Consumption (kWH)", // range axis label
				dataset, // data
				PlotOrientation.HORIZONTAL, // orientation
				true, // include legend
				true, false);

		// NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...

		// set the background color for the chart...
		chart.setBackgroundPaint(Color.lightGray);

		// get a reference to the plot for further customisation...
		final CategoryPlot plot = chart.getCategoryPlot();
		plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);

		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);

		final IntervalMarker target = new IntervalMarker(0,GREEN_THRESHOLD);
		target.setLabel("");
		target.setLabelFont(new Font("SansSerif", Font.ITALIC, 11));
		target.setLabelAnchor(RectangleAnchor.LEFT);
		target.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
		target.setPaint(new Color(222, 222, 255, 128));
//		target.setPaint(new Color(10, 222, 2, 128));
		plot.addRangeMarker(target, Layer.BACKGROUND);

		// disable bar outlines...
		final BarRenderer renderer1 = (BarRenderer) plot.getRenderer();
		renderer1.setDrawBarOutline(false);
		renderer1.setItemMargin(0);

		final CategoryItemRenderer renderer = new CustomRenderer();
		// renderer.setLabelGenerator(new StandardCategoryLabelGenerator());
		// renderer.setItemLabelsVisible(true);
		// final ItemLabelPosition p = new ItemLabelPosition(
		// ItemLabelAnchor.CENTER, TextAnchor.CENTER, TextAnchor.CENTER,
		// 45.0);
		// renderer.setPositiveItemLabelPosition(p);
		// plot.setRenderer(renderer); //TODO: Maybe use the renderer
		// depending upon bar value

		// change the auto tick unit selection to integer units only...
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setRange(0.0, 100000.0);
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		// OPTIONAL CUSTOMISATION COMPLETED.

		return chart;

	}

	public ChartPanel getChartPanel() {
		return chartPanel;
	}

}

