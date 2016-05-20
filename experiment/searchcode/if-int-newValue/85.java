package eu.future.earth.gwt.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

import eu.future.earth.gwt.charts.client.BlokRenderer;
import eu.future.earth.gwt.charts.client.DefaultPointItemWithData;
import eu.future.earth.gwt.charts.client.DefaultPointTranslatorWithData;
import eu.future.earth.gwt.charts.client.GraphRaster;
import eu.future.earth.gwt.charts.client.RasterLine;
import eu.future.earth.gwt.charts.client.event.mouseclick.MouseClickOnPointEvent;
import eu.future.earth.gwt.charts.client.event.mouseclick.MouseClickOnPointHandler;
import eu.future.earth.gwt.charts.client.event.mouseover.MouseOverPointEvent;
import eu.future.earth.gwt.charts.client.event.mouseover.MouseOverPointHandler;
import eu.future.earth.gwt.client.utils.General;

public class ChartDemo extends Composite {

	private Label label = new Label("Feedback");

	private GraphRaster canvas = new GraphRaster();

	// private Button editable = new Button("Toggle Editable");

	

	private TextBox yCountNumber = new TextBox();

	private CheckBox yLabel = new CheckBox("Show y labels");
	private CheckBox xLabel = new CheckBox("Show x labels");

	private TextBox xCount = new TextBox();

	public ChartDemo() {
		super();
		DockLayoutPanel main = new DockLayoutPanel(Unit.PX);
		initWidget(main);
		HorizontalPanel top = new HorizontalPanel();
		top.add(label);

		top.add(yLabel);
		top.add(yCountNumber);
		yCountNumber.setValue("10");
		xCount.setValue("12");
		yLabel.setValue(true);
		xLabel.setValue(true);

		top.add(xLabel);
		top.add(xCount);
		main.addNorth(top, 30);
		main.add(canvas);

		// buttons.add(editable);
		reset();
		canvas.draw();

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

		yCountNumber.addKeyUpHandler(new KeyUpHandler() {

			@Override
			public void onKeyUp(KeyUpEvent event) {
				reset();
			}
		});
		xCount.addKeyUpHandler(new KeyUpHandler() {

			@Override
			public void onKeyUp(KeyUpEvent event) {
				reset();
			}
		});
		
		
		
	}

	private final int yHeight = 15;

	private final int xWidth = 15;

	private void reset() {
		yCount = 0;
		canvas.clearData();
		canvas.setShowXItems(xLabel.getValue());
		canvas.setShowYItems(yLabel.getValue());
		int xVal = Integer.valueOf(xCount.getValue());
		for (int i = 0; i < xVal; i++) {
			RasterLine line = new RasterLine(i * xWidth, "" + i, General.even(i));
			if (General.even(i)) {
				line.setBold(true);
			}
			canvas.addXLine(line);
		}
		int yVal = Integer.valueOf(yCountNumber.getValue());
		for (int i = 0; i < yVal; i++) {
			RasterLine line = new RasterLine(i * yHeight, "" + i, General.even(i));
			if (General.even(i)) {
				line.setBold(true);
			}
			canvas.addYLine(line);
		}

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
		
		employeesNeededPresent.setColor("#000000");
		double last = 0;

		List<RatioResult> samples = createSamples(xVal, yVal);
		for (RatioResult timeData : samples) {
			if (last != timeData.getyValue()) {
				int newValue = timeData.getyValue();
				yCount = Math.max(yCount, newValue);
				int yPoint = timeData.getHourxValue() * xWidth;
				employeesNeededPresent.addItem(new DefaultPointItemWithData<RatioResult>(yPoint, last * yHeight));
				employeesNeededPresent.addItem(new DefaultPointItemWithData<RatioResult>(yPoint, newValue * yHeight, timeData.getHourxValue() + "," + timeData.getyValue(), timeData));
				last = newValue;
			}
		}

		canvas.addLine(employeesNeededPresent);

		canvas.draw();
	}

	private List<RatioResult> createSamples(int xVal, int yVal) {
		List<RatioResult> result = new ArrayList<RatioResult>();
		xVal = Math.max(xVal, values.length);
		for (int i = 0; i < xVal; i++) {
			result.add(new RatioResult(i, values[i]));
		}
		return result;
	}

	private static int[] values = {
			0, 2, 5, 2, 3, 7, 3, 5, 5, 6, 8, 12, 3, 0
	};

	private double yCount = 0;



}

