package org.pml.gnd.gwt.client.ui.document;

import org.pml.gnd.gwt.client.handlers.GraphHandlers;
import org.pml.gnd.gwt.client.util.GWTUtils;
import org.pml.gnd.gwt.client.util.date.DateFormats;

import ca.nanometrics.gflot.client.DataPoint;
import ca.nanometrics.gflot.client.PlotModel;
import ca.nanometrics.gflot.client.Series;
import ca.nanometrics.gflot.client.SeriesHandler;
import ca.nanometrics.gflot.client.SimplePlot;
import ca.nanometrics.gflot.client.options.AbstractAxisOptions.AxisPosition;
import ca.nanometrics.gflot.client.options.AxisOptions;
import ca.nanometrics.gflot.client.options.GlobalSeriesOptions;
import ca.nanometrics.gflot.client.options.GridOptions;
import ca.nanometrics.gflot.client.options.LegendOptions;
import ca.nanometrics.gflot.client.options.LegendOptions.LegendPosition;
import ca.nanometrics.gflot.client.options.LineSeriesOptions;
import ca.nanometrics.gflot.client.options.PanOptions;
import ca.nanometrics.gflot.client.options.PlotOptions;
import ca.nanometrics.gflot.client.options.PointsSeriesOptions;
import ca.nanometrics.gflot.client.options.TimeSeriesAxisOptions;
import ca.nanometrics.gflot.client.options.ZoomOptions;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class GraphView extends ViewWithUiHandlers<GraphHandlers> implements
		GraphPresenter.MyView
{

	public interface Binder extends UiBinder<Widget, GraphView>
	{
	}

	private final Widget widget;

	@Override
	public Widget asWidget()
	{
		return widget;
	}

	// TODO Try to remove duplication of constants with ui.xml file
	public static final String LEFT_AXIS_COLOR = "#CC0000";
	public static final String RIGHT_AXIS_COLOR = "#0000CC";

	private SeriesHandler leftSeries;
	private SeriesHandler rightSeries;

	SimplePlot plot = new SimplePlot(new PlotModel(), new PlotOptions());

	@UiField
	Panel plotSlot;
	@UiField
	Panel selectionBarPanel;
	@UiField
	ListBox leftAxisBox;
	@UiField
	ListBox rightAxisBox;
	@UiField
	Anchor permalink;

	@Inject
	public GraphView(final Binder binder)
	{
		widget = binder.createAndBindUi(this);

		// format the plot
		PlotOptions plotOptions = plot.getPlotOptions();

		// add point series option to show dot (small circle) on point
		plotOptions.setGlobalSeriesOptions(new GlobalSeriesOptions()
				.setLineSeriesOptions(
						new LineSeriesOptions().setLineWidth(1).setShow(true))
				.setPointsOptions(new PointsSeriesOptions().setRadius(2).setShow(true))
				.setShadowSize(2d));

		plotOptions.setGridOptions(new GridOptions().setHoverable(true));
		plotOptions.setLegendOptions(new LegendOptions().setPosition(
				LegendPosition.NORTH_EAST).setNumOfColumns(5));
		plotOptions.getLegendOptions().setShow(false);

		// plotOptions.getGridOptions().clearMinBorderMargin();
		plotOptions.getGridOptions().setBorderWidth(1);
		plotOptions.getGridOptions().setMinBorderMargin(5);

		// support for zoom
		plotOptions.setZoomOptions(new ZoomOptions().setInteractive(true))
				.setPanOptions(new PanOptions().setInteractive(true));

		// format xAxis date display
		TimeSeriesAxisOptions timeSeriesOptions = new TimeSeriesAxisOptions();
		timeSeriesOptions.setTimeFormat("%y/%0m/%0d %H:%M:%S");
		plotOptions.addXAxisOptions(timeSeriesOptions);
		plotOptions.addYAxisOptions(new AxisOptions().setColor(LEFT_AXIS_COLOR));
		plotOptions.addYAxisOptions(new AxisOptions().setAlignTicksWithAxis(1)
				.setPosition(AxisPosition.RIGHT).setColor(RIGHT_AXIS_COLOR));

		plotSlot.add(new ResizeAdapter(plot));
	}

	@Override
	public void clear()
	{
		plot.getModel().removeAllSeries();
		leftAxisBox.clear();
		rightAxisBox.clear();
		redraw();
	}

	public void redraw()
	{
		if (plot.isPlotLoaded())
			plot.redraw();
	}

	@Override
	public void onDisplay()
	{
		redraw();
	}

	@Override
	public void showThisMeasurementAsLeftYAxis(JSONArray timeArray,
			JSONArray measurementArray, String thisMeasurementName)
	{
		clearLeftData();
		leftSeries = plot.getModel().addSeries(
				new Series(thisMeasurementName).setColor(LEFT_AXIS_COLOR));
		addPointsToSeries(timeArray, measurementArray, leftSeries);
		redraw();
	}

	@Override
	public void showThisMeasurementAsRightYAxis(JSONArray timeArray,
			JSONArray measurementArray, String thisMeasurementName)
	{
		clearRightData();
		rightSeries = plot.getModel().addSeries(
				new Series(thisMeasurementName).setYAxis(2).setColor(RIGHT_AXIS_COLOR));
		addPointsToSeries(timeArray, measurementArray, rightSeries);
		redraw();
	}

	private void addPointsToSeries(JSONArray timeArray,
			JSONArray measurementArray, SeriesHandler series)
	{
		int len = measurementArray.size();
		for (int i = 0; i < len; i++)
		{
			JSONString dateStr = (JSONString) timeArray.get(i);
			// convert to milliseconds as Flot only accept double values on both
			// axis. So time can be set as in milliseconds
			long time = DateFormats.CUSTOM.parse(dateStr.stringValue()).getTime();
			DataPoint newPoint = new DataPoint(time, Double.valueOf(measurementArray
					.get(i).toString()));
			series.add(newPoint);
		}
	}

	@Override
	public void addItemToLeftAxisBox(String item)
	{
		leftAxisBox.addItem(item);
	}

	@Override
	public void addItemToRightAxisBox(String item)
	{
		rightAxisBox.addItem(item);
	}

	@Override
	public void clearLeftData()
	{
		if (leftSeries != null)
			plot.getModel().removeSeries(leftSeries);
		leftSeries = null;
		redraw();
	}

	@Override
	public void clearRightData()
	{
		if (rightSeries != null)
			plot.getModel().removeSeries(rightSeries);
		rightSeries = null;
		redraw();
	}

	@UiHandler("leftAxisBox")
	void handleLeftAxisChange(ChangeEvent e)
	{
		if (getUiHandlers() != null)
			getUiHandlers().onLeftAxisBoxChange(
					leftAxisBox.getValue(leftAxisBox.getSelectedIndex()));
	}

	@UiHandler("rightAxisBox")
	void handleRightAxisChange(ChangeEvent e)
	{
		if (getUiHandlers() != null)
			getUiHandlers().onRightAxisBoxChange(
					rightAxisBox.getValue(rightAxisBox.getSelectedIndex()));
	}

	private class ResizeAdapter extends Composite implements RequiresResize
	{

		final SimplePlot plot;

		public ResizeAdapter(SimplePlot plot)
		{
			this.plot = plot;
			initWidget(plot);
		}

		@Override
		public void onResize()
		{
			plot.setWidth(Window.getClientWidth() - Window.getClientWidth() / 50); // 1%-width
																																							// margins
																																							// (left
																																							// and
																																							// right)
			plot.setHeight(Window.getClientHeight() - 40
					- DocumentMainView.getUsedHeight());
		}
	}

	@UiHandler("permalink")
	void onPermalinkClick(ClickEvent e) {
		if (getUiHandlers() != null)
			getUiHandlers().onPermalinkClick();
		
		e.preventDefault();
		e.stopPropagation();
	}


	@Override
	public void setLeftAxisBox(String left)
	{
		GWTUtils.setSelectedText(leftAxisBox, left, true);
	}

	@Override
	public void setRightAxisBox(String right)
	{
		GWTUtils.setSelectedText(rightAxisBox, right, true);
	}
}

