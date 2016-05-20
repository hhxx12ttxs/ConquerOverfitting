package eu.future.earth.gwt.client.hordate;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.ResizeComposite;

import eu.future.earth.gwt.charts.client.BlokRenderer;
import eu.future.earth.gwt.charts.client.DefaultPointItemWithData;
import eu.future.earth.gwt.charts.client.DefaultPointTranslatorWithData;
import eu.future.earth.gwt.charts.client.GraphRaster;
import eu.future.earth.gwt.charts.client.RasterLine;
import eu.future.earth.gwt.charts.client.event.mouseclick.MouseClickOnPointEvent;
import eu.future.earth.gwt.charts.client.event.mouseclick.MouseClickOnPointHandler;
import eu.future.earth.gwt.charts.client.event.mouseover.MouseOverPointEvent;
import eu.future.earth.gwt.charts.client.event.mouseover.MouseOverPointHandler;
import eu.future.earth.gwt.client.OverviewPanelDemo;
import eu.future.earth.gwt.client.RatioResult;
import eu.future.earth.gwt.client.date.DateEvent;
import eu.future.earth.gwt.client.date.DateEventListener;
import eu.future.earth.gwt.client.date.DefaultEventData;
import eu.future.earth.gwt.client.date.horizontal.HorizontalViewPanelNoDays;
import eu.future.earth.gwt.client.utils.General;

public class HorizontalDemo extends ResizeComposite implements ProvidesResize, DateEventListener<DefaultEventData>, ClickHandler {

	private HorizontalDateRendererDemo renderer = new HorizontalDateRendererDemo();

	private final static int LEFT_LABEL_WIDTH = 160;
	
	private HorizontalViewPanelNoDays<DefaultEventData, UserDemo> demo = new HorizontalViewPanelNoDays<DefaultEventData, UserDemo>(renderer, LEFT_LABEL_WIDTH);

	private Button reload = new Button("reload", this);

	private GraphRaster canvas = new GraphRaster();

	private Label label = new Label("Feedback");

	private CheckBox yLabel = new CheckBox("Show y labels");
	private CheckBox xLabel = new CheckBox("Show x labels");
	
	public HorizontalDemo() {
		super();
		DockLayoutPanel panel = new DockLayoutPanel(Unit.PX);
		initWidget(panel);
		HorizontalPanel top = new HorizontalPanel();
		top.add(reload);
		panel.addNorth(top, 30);
		
		top.add(yLabel);
		yLabel.setValue(true);
		xLabel.setValue(true);
		top.add(xLabel);
		
		panel.add(demo);
		canvas.setShowXItems(false);
		canvas.setShowYItems(false);
		canvas.setLeftOffSet(LEFT_LABEL_WIDTH);
		demo.addWidgetToScroll(canvas, canvas.getyLabel());
		
		final int yHeight = 15;
		
		xLabel.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				canvas.setShowXItems(xLabel.getValue());
				canvas.draw();
			}
		});

		yLabel.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				canvas.setShowYItems(yLabel.getValue());
				canvas.draw();
			}
		});
		
		for (int i = 1; i < 72; i++) {
			RasterLine line = new RasterLine(i * renderer.getIntervalWidth(), "" + i, General.even(i));
			if (General.even(i)) {
				line.setBold(true);
			}
			canvas.addXLine(line);
		}
		for (int i = 1; i < 4; i++) {
			RasterLine line = new RasterLine(i * yHeight, "" + i, true);
			if (General.even(i)) {
				line.setBold(true);
			}
			canvas.addYLine(line);
		}
		{
			List<DefaultEventData> samples = OverviewPanelDemo.createPlanningSamples(renderer);
			demo.setEvents(samples);
		}
		{

			BlokRenderer<DefaultPointItemWithData<RatioResult>> employeesNeededPresent = new BlokRenderer<DefaultPointItemWithData<RatioResult>>(new DefaultPointTranslatorWithData<RatioResult>());
			employeesNeededPresent.addMouseClickOnPointHandler(new MouseClickOnPointHandler<DefaultPointItemWithData<RatioResult>>() {

				@Override
				public void onMouseOverPointEvent(MouseClickOnPointEvent<DefaultPointItemWithData<RatioResult>> newDataEvent) {
					RatioResult re = newDataEvent.getData().getData();
					label.setText("Over " + re.getHourxValue() + "," + re.getyValue());

				}
			});

			employeesNeededPresent.addMouseOverPointHandler(new MouseOverPointHandler<DefaultPointItemWithData<RatioResult>>() {

				@Override
				public void onMouseOverPointEvent(MouseOverPointEvent<DefaultPointItemWithData<RatioResult>> newDataEvent) {
					RatioResult re = newDataEvent.getData().getData();
					label.setText("Click " + re.getHourxValue() + "," + re.getyValue());
				}
			});

			
//			final int xWidth = renderer.getIntervalWidth();

			employeesNeededPresent.setColor("#000000");
			double last = 0;
			List<RatioResult> samples = createSamples();
			for (RatioResult timeData : samples) {
				if (last != timeData.getyValue()) {
					int newValue = timeData.getyValue();
					yCount = Math.max(yCount, newValue);
					int yPoint = demo.getTimePos(timeData.getHourxValue(), 0);
					employeesNeededPresent.addItem(new DefaultPointItemWithData<RatioResult>(yPoint, last * yHeight));
					employeesNeededPresent.addItem(new DefaultPointItemWithData<RatioResult>(yPoint, newValue * yHeight, timeData.getHourxValue() + "," + timeData.getyValue(), timeData));
					last = newValue;
				}
			}

			canvas.addLine(employeesNeededPresent);

			canvas.draw();
		}

		// demo.removeEmptyRows();
		demo.addDateEventHandler(this);
		demo.redrawPanel();
	}

	public void handleDateEvent(DateEvent<DefaultEventData> newEvent) {
		GWT.log("" + newEvent, null);

	}

	private List<RatioResult> createSamples() {
		List<RatioResult> result = new ArrayList<RatioResult>();
		for (int i = 0; i < values.length; i++) {
			result.add(new RatioResult(i, values[i]));
		}
		return result;
	}

	private static int[] values = {
			0, 2, 3, 2, 3, 1, 3, 1, 1, 3, 2, 2, 3, 0
	};

	private double yCount = 0;

	@Override
	public void onClick(ClickEvent event) {

	}

}

