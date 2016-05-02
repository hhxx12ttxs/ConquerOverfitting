package wolfchart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.io.Serializable;
import java.util.Map;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.renderer.AreaRendererEndType;
import org.jfree.chart.renderer.category.AreaRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.ui.RectangleInsets;

import de.laures.cewolf.ChartPostProcessor;

/**
 * Post processor which is used change the border colour of a pie chart.
 */
public class TickPostProcessor implements ChartPostProcessor, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	public void processChart(Object chart, Map params) {
		JFreeChart localChart = (JFreeChart) chart;
		Plot plot = localChart.getPlot();
		// localChart.setBorderPaint(Color.magenta);
		plot.setBackgroundPaint(Color.white);
		// plot.setOutlinePaint(Color.green);

		CategoryPlot abc = (CategoryPlot) plot;
		abc.setRangeGridlinePaint(Color.black);
		// abc.setRangeGridlineStroke(BasicStroke);

		CategoryItemRenderer rend = abc.getRenderer();

		if (rend instanceof AreaRenderer) {
			AreaRenderer area = (AreaRenderer) rend;
			area.setEndType(AreaRendererEndType.TRUNCATE);
			//area.setAutoPopulateSeriesShape(true);
		} else if (rend instanceof LineAndShapeRenderer) {
			LineAndShapeRenderer line = (LineAndShapeRenderer) rend;
			line.setSeriesShapesVisible(0, true);
			//line.setAutoPopulateSeriesShape(true);
			//rend.setSeriesShape(0, new Ellipse2D.Double(-4.0, -4.0, 12.0, 16.0));
		}
		
		
		System.out.println("hola");

		plot.setForegroundAlpha(0.5f);

		
		
		
		
//		rend
//				.setSeriesStroke(0, new BasicStroke(5.0f,
//						BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 1.0f,
//						new float[] { 1.0f, 6.0f }, 0.0f));

		abc.setAxisOffset(new RectangleInsets(0, 0, 0, 0));
		abc.getDomainAxis().setLowerMargin(0);
		abc.getDomainAxis().setUpperMargin(0);
		abc.getRangeAxis().setLowerMargin(0);
		plot.setInsets(new RectangleInsets(0, 0, 0, 0));
		localChart.setPadding(new RectangleInsets(0, 0, 0, 0));
		// categoryPlot.addA . .setRangeGridlinePaint(Color.lightGray);


//		abc.getRangeAxis().setLowerMargin(0);
//		abc.getRangeAxis().setLabelAngle(70 * Math.PI / 2.0);
		
		abc.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
//        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
//        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
//        rangeAxis.setLabelAngle(0 * Math.PI / 2.0);

		// plot.setr
	}
}

