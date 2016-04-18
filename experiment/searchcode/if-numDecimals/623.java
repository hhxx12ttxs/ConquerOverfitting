package unw.ui.box.sweep;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.AbstractCategoryItemLabelGenerator;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.urls.StandardXYURLGenerator;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.AbstractDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.experimental.chart.swt.ChartComposite;

import unw.base.UnwCore;
import unw.base.db.model.ModelDB;
import unw.base.db.model.PortDB;
import unw.base.db.model.VIRTPORT_TYPE;
import unw.base.db.model.DBDesignConstants.ComputerTypes;
import unw.base.db.node.PortDBNode;
import unw.base.project.DBProject;
import unw.base.service.definition.UnwServiceRegistry;
import unw.box.engine.service.IEngineService;
import unw.box.evaluator.probe.SBoxTableItem;
import unw.box.evaluator.probe.SBoxTableItemList;
import unw.box.evaluator.tools.SBToolsEngine;
import unw.box.evaluator.tools.SBToolsSweep;
import unw.box.evaluator.tools.WaveformTools;
import unw.de.kupzog.ktableviewer.KCellFormatValidation.KCELLFORMAT_TYPE;
import unw.de.kupzog.ktableviewer.KTableViewer;
import unw.de.kupzog.ktableviewer.KTableViewer.ColumnDescriptor;
import unw.de.kupzog.ktableviewer.KTableViewer.ColumnType;
import unw.de.kupzog.ktableviewer.KTableViewerActionHandler;
import unw.de.kupzog.ktableviewer.KTableViewerModelProvider;
import unw.state.ModelStateManager;
import unw.toolbox.format.FormatNumber;
import unw.toolbox.format.FormatValidation;
import unw.toolbox.log.Logger;
import unw.toolbox.string.StringUtilities;
import unw.ui.box.probe.ProbeView;
import unw.ui.box.probe.SBoxTableContentProvider;

import com.ti.smartsight.technology.TechnologyManager;

import de.kupzog.ktable.SWTX;

/**
 * View to display sweep result
 * 
 * BEFORE opening the view, the user must 
 *  - set selectedInput    (= null for state sweep)
 *  - set selectedOutputs
 *  

 *
 */
public class SBoxWaveformView2 extends ViewPart {

	public static final String ID = "com.ti.SmartSight.SmartBox.SBoxWaveformView2";

	// SBoxTable Item to display
	// 
	private static SBoxTableItem               selectedInput;
	private static ArrayList<Object>           selectedOutputs = null;
	private static SBoxWaveInfo                sboxWaveInfo;
	

	private Composite      topForm           = null;
	private TabFolder      chartDataTabFolder = null;
	private Text           stepText           = null;
	private JFreeChart     waveformChart      = null;

	private KTableViewer   dataTableViewer    = null;

	AbstractDataset                     dataset;

	public  String                      xAxisTitle      = null;

	public  Double[]                    xDataNumbers    = null; // x data for float sweep
	private String[]                    xDataTokens     = null; // x data for string sweep

	public  HashMap<String, Double[]>   yData           = null; // y data
	private HashMap<String, String>     allYDataUnits   = null;

	private float min        = 0;
	private float max        = 1;
	private float stepCalcul = 0.1f;

	private org.eclipse.swt.widgets.Label title;
	private Text minText;
	private Text maxText;

	private TabItem chartTab;

	private SBoxTableItem xItem;

	private ChartComposite chartComposite;

	public SBoxWaveformView2() {
	}

	@Override
	public void createPartControl(Composite parent) {
		PortDB port = null;

		//------------------------------------------------------
		// Get sweep parameters
		//

		if( selectedOutputs == null || selectedOutputs.size() == 0) {

			org.eclipse.swt.widgets.Label warningLabel = new org.eclipse.swt.widgets.Label(parent, SWT.NONE);
			warningLabel.setText("There aren't "+
					ProbeView.FORCE_SECTION_NAME + " or " +
					ProbeView.OBSERVE_SECTION_NAME + " selected.");
			return;
		}

		xItem = selectedInput;
		DBProject project = UnwCore.getDBProject();
		ModelDB topModel = project.model();

		if (xItem != null) {
			xAxisTitle = SBoxTableContentProvider.getTableEntryName(xItem);
			port = xItem.getPortDBNode().getPort();
		}
		else {
			xAxisTitle = "state of " + topModel.getModelName();
		}



		//----------------------------------------------------
		// Build GUI
		//
		topForm = new Composite(parent, SWT.NONE);
		topForm.setLayout(new GridLayout(1, false));

		chartDataTabFolder = new TabFolder(topForm, SWT.TOP);
		chartTab = new TabItem(chartDataTabFolder, SWT.NONE, 0);
		chartTab.setText("Chart Graphic");
		TabItem dataTab = new TabItem(chartDataTabFolder, SWT.NONE, 1);
		dataTab.setText("Data Table");

		GridData gridData = new GridData(GridData.FILL_BOTH);
		chartDataTabFolder.setLayoutData(gridData);

		Composite paramsCompo = new Composite(topForm, SWT.NONE);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		paramsCompo.setLayoutData(gridData);
		paramsCompo.setLayout(new GridLayout(7, false));

		if (port != null && port.getTypedef().getTokens(true).length == 0)
			addMinMaxStepBar(paramsCompo, port);

		//--------------------------------------------------------
		// Build data set to store it in global variables
		//

		if (xItem != null) {

			if( port.getComputerType() == ComputerTypes.FLOAT || 
					port.getComputerType() == ComputerTypes.INTEGER) {
				
				if (sboxWaveInfo == null) {
					min          = port.getTypedef().getMin(true);
					max          = port.getTypedef().getMax(true);
					stepCalcul   = port.getTypedef().getStep(true);
				} else {
					min = sboxWaveInfo.min;
					max = sboxWaveInfo.max;
					stepCalcul = (max - min) / 50;
				}
				
				minText.setText(Float.toString(min));
				maxText.setText(Float.toString(max));
				stepText.setText(Float.toString(stepCalcul));

				xDataNumbers = (Double[])getXData(xItem);
				yData        = computeYDataPin(xItem, xDataNumbers, selectedOutputs);

			}
			else if( port.getComputerType() == ComputerTypes.STRING ){
				xDataTokens = (String[])getXData(xItem);
				yData       = computeYDataPin(xItem, xDataTokens, selectedOutputs);
			} else {
				Logger.error("type of swept port " + xItem.getPortDBNode().getPinName() + " not supported " + 
						port.getComputerType().getText()); 
				return;
			}
		} else {
			xDataTokens = ModelStateManager.instance.modelStateNames(topModel).toArray(new String[]{});
			SBoxTableItem yItem;
			String        pinName;
			ArrayList<SBoxTableItem>  outputs = new ArrayList<SBoxTableItem>();
			ComputerTypes ctype;

			allYDataUnits = new HashMap<String, String>(outputs.size());

			// compute ydata for selected output of type float and integer. String output are ignored.
			for (int i = 0; i < selectedOutputs.size(); i ++) {
				yItem = (SBoxTableItem)selectedOutputs.get(i);
				ctype = null;
				if (yItem.getPortDBNode() != null)
					ctype = yItem.getPortDBNode().getPort().getComputerType();

				if (ctype == null || 
						ctype == ComputerTypes.FLOAT || ctype == ComputerTypes.INTEGER) {
					pinName = yItem.getText();
					outputs.add(yItem);
					allYDataUnits.put(pinName, SBToolsEngine.getUnitMesure(yItem));
				}
			}
			yData = SBToolsSweep.sweepState(xDataTokens, outputs.toArray(new SBoxTableItem[outputs.size()]));
		}


		//---------------------------------------
		// Title, min, max step widget
		//
		title = new org.eclipse.swt.widgets.Label(paramsCompo, SWT.LEFT);
		title.setText("x Axis: " + xAxisTitle + " ");


		//--------------------------------------------------------
		// Create chart canvas

		waveformChart = createChart();

		if(waveformChart != null) {
			chartComposite = new ChartComposite(chartDataTabFolder, SWT.NONE, waveformChart, true);
			chartTab.setControl(chartComposite);

			createDataTable(chartDataTabFolder, dataTab);
		}
		else {
			org.eclipse.swt.widgets.Label warningLabel = new org.eclipse.swt.widgets.Label(topForm, SWT.NONE);
			warningLabel.setText("Cannot generate the chart. See the error console.");
		}
	}

	private void createDataSet() {

		if (xDataNumbers != null)
			createXYDataSet();
		else
			createBarDataSet();
	}

	private void createXYDataSet() {
		XYSeries series;

		dataset = new XYSeriesCollection();

		for (String serieName : yData.keySet()) {
			series = new XYSeries(serieName);

			Double[] ys = yData.get(serieName);

			for (int i = 0; i < xDataNumbers.length; i++)
				series.add(xDataNumbers[i], ys[i]);

			((XYSeriesCollection)dataset).addSeries(series);
		}
	}

	private void createBarDataSet() {
		dataset = new DefaultCategoryDataset();

		for (String serieName : yData.keySet()) {
			Double[] ys = yData.get(serieName);

			for (int i = 0; i < xDataTokens.length; i++)
				((DefaultCategoryDataset)dataset).addValue(ys[i], serieName, xDataTokens[i]);
		}
	}

	private void addMinMaxStepBar(Composite paramsCompo, PortDB xPort) {
		org.eclipse.swt.widgets.Label minTitle = new org.eclipse.swt.widgets.Label(paramsCompo, SWT.LEFT);
		minTitle.setText(" min =");
		minText = new Text(paramsCompo, SWT.BORDER | SWT.SINGLE | SWT.RIGHT);
		GridData td = new GridData(SWT.FILL, SWT.BEGINNING, true, true);
		minText.setLayoutData(td);

		org.eclipse.swt.widgets.Label stepTitle = new org.eclipse.swt.widgets.Label(paramsCompo, SWT.LEFT);
		stepTitle.setText(" step =");
		stepText = new Text(paramsCompo, SWT.BORDER | SWT.SINGLE | SWT.RIGHT);

		td = new GridData(SWT.FILL, SWT.BEGINNING, true, true);
		stepText.setLayoutData(td);

		org.eclipse.swt.widgets.Label maxTitle = new org.eclipse.swt.widgets.Label(paramsCompo, SWT.LEFT);
		maxTitle.setText(" max =");
		maxText = new Text(paramsCompo, SWT.BORDER | SWT.SINGLE | SWT.RIGHT);
		td = new GridData(SWT.FILL, SWT.BEGINNING, true, true);
		maxText.setLayoutData(td);

		minText.setText(Float.toString(min));
		maxText.setText(Float.toString(max));
		stepText.setText(Float.toString(stepCalcul));

		// text listener
		SelectionAdapter txtSelectionListener = getFloatAxisSettingListener();

		minText.addSelectionListener(txtSelectionListener);
		maxText.addSelectionListener(txtSelectionListener);
		stepText.addSelectionListener(txtSelectionListener);
	}

	private SelectionAdapter getFloatAxisSettingListener(	) {
		SelectionAdapter txtSelectionListener = new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				Float minFloat = checkFloat(minText.getText());
				if(minFloat.isNaN()) { // not a valid number?
					MessageDialog.openWarning(getSite().getShell(),
							"Invalid input", 
					"Invalid min value.\nPlease verify it.");
					return;
				}

				Float maxFloat = checkFloat(maxText.getText());
				if(maxFloat.isNaN()) { // not a valid number?
					MessageDialog.openWarning(getSite().getShell(),
							"Invalid input", 
					"Invalid max value.\nPlease verify it.");
					return;
				}

				if(maxFloat < minFloat) {
					MessageDialog.openWarning(getSite().getShell(),
							"Invalid input", 
					"The min value can't be greater than the max value.\nPlease verify it.");
					return;
				}

				Float stepFloat = checkFloat(stepText.getText());
				if(stepFloat.isNaN()) { // not a valid number?
					MessageDialog.openWarning(getSite().getShell(),
							"Invalid input", 
					"Invalid step value.\nPlease verify it.");
					return;
				}

				min = minFloat;
				stepCalcul = stepFloat;
				max = maxFloat;

				updateDataPoints(xItem);

				if(chartComposite != null) {
					chartComposite.dispose();
				}

				waveformChart = createChart();
				if(waveformChart != null) {

					chartComposite = new ChartComposite(chartDataTabFolder, SWT.NONE, waveformChart, true);
					chartTab.setControl(chartComposite);
				}
				else {
					org.eclipse.swt.widgets.Label warningLabel = new org.eclipse.swt.widgets.Label(topForm, SWT.NONE);
					warningLabel.setText("There are some problems generating the chart. See the error log for more details.");
				}
			}

			private Float checkFloat(String floatInString) {
				if((floatInString.length() == 0) || // no text? 
						(!FormatValidation.isFloat(floatInString, false))){ // not a float? 
					return Float.NaN;
				}

				return StringUtilities.convertStringToFloat(floatInString);
			}
		};
		return txtSelectionListener;
	}

	private void updateDataPoints(SBoxTableItem xItem) {

		PortDB port = xItem.getPortDBNode().getPort();

		if( port.getComputerType() == ComputerTypes.FLOAT || 
				port.getComputerType() == ComputerTypes.INTEGER) {

			xDataNumbers = (Double[])getXData(xItem);
			yData        = computeYDataPin(xItem, xDataNumbers, selectedOutputs);

		}
		else if( port.getComputerType() == ComputerTypes.STRING ){
			xDataTokens = (String[])getXData(xItem);
			yData       = computeYDataPin(xItem, xDataTokens, selectedOutputs);
		}
	}

	private JFreeChart createChart() {
		JFreeChart chart;

		createDataSet();

		if (xDataNumbers != null)
			chart = createXYStepChart(
					"sweep versus " + xItem.getPortDBNode().getPortName() + " (" + xItem.getPortDBNode().getPort().getTypeName() + ") - process " +
					                           TechnologyManager.getProcess(), 
					xAxisTitle,
					"signal", 
					(XYDataset)dataset, 
					PlotOrientation.VERTICAL, 
					true, 
					false,
					false); 
		else {
			chart = ChartFactory.createBarChart(
					"sweep versus states of " + UnwCore.project.model().getModelName(),
					xAxisTitle,               // domain axis label
					"Value",                  // range axis label
					(CategoryDataset)dataset,                  // data
					PlotOrientation.VERTICAL, // orientation
					true,                     // include legend
					true,                     // tooltips?
					false                     // URLs?
			);


			CategoryItemRenderer renderer = chart.getCategoryPlot().getRenderer();

			renderer.setBaseItemLabelGenerator(new LabelGenerator());
			renderer.setBaseItemLabelsVisible(true);

		}
		return chart;
	}

	private JFreeChart createXYStepChart(String title, String xAxisLabel,
			String yAxisLabel, XYDataset dataset, 
			PlotOrientation orientation,
			boolean legend, 
			boolean tooltips, 
			boolean urls) {

		if (orientation == null) {
			throw new IllegalArgumentException("Null 'orientation' argument.");
		}
		NumberAxis xAxis = new NumberAxis(xAxisLabel);
		xAxis.setAutoRangeIncludesZero(false);

		NumberAxis yAxis = new NumberAxis(yAxisLabel);
		//		yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		XYToolTipGenerator toolTipGenerator = null;
		if (tooltips) {
			toolTipGenerator = new StandardXYToolTipGenerator(
					StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
					NumberFormat.getInstance(), 
					NumberFormat.getInstance());
		}

		XYURLGenerator urlGenerator = null;
		if (urls) {
			urlGenerator = new StandardXYURLGenerator();
		}
		//		XYStepRenderer renderer = new XYStepRenderer(toolTipGenerator,
		//				urlGenerator);
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, 
				false);
		renderer.setBaseToolTipGenerator(toolTipGenerator);
		renderer.setURLGenerator(urlGenerator);

		XYItemLabelGenerator gen = new StandardXYItemLabelGenerator("@ {1} = {2}",
				new DecimalFormat("00.00#E0"),
				new DecimalFormat("00.00#E0"));
		renderer.setBaseItemLabelGenerator(gen);
		//		renderer.setBaseItemLabelsVisible(true);

		XYPlot plot = new XYPlot(dataset, xAxis, yAxis, null);
		plot.setRenderer(renderer);
		plot.setOrientation(orientation);
		plot.setDomainCrosshairVisible(true);
		plot.setRangeCrosshairVisible(true);
		JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
				plot, legend);
		return chart;

	}

	private Object[] getXData(SBoxTableItem xItem) {
		PortDB xPort = xItem.getPortDBNode().getPort();

		String[] tokens = xPort.getTypedef().getTokens(true);

		if (tokens.length != 0)
			return tokens;

		int maxPoints = 50;
		double totalPoints = (max-min)/stepCalcul;
		
		if (stepCalcul ==0)	totalPoints = 51;
		
		if (totalPoints > maxPoints) {
			stepCalcul = (max-min)/maxPoints;
			stepText.setText(Float.toString(stepCalcul));
		}

		return WaveformTools.getXData(min, max, stepCalcul);
	}

	private HashMap<String, Double[]> computeYDataPin(SBoxTableItem     xItem,
			Object[]          xValues, 
			ArrayList<Object> outputs) {

		HashMap<String, Double[]>           allYData      = new HashMap<String, Double[]>(outputs.size());

		allYDataUnits = new HashMap<String, String>(outputs.size());

		PortDBNode    xportDBNode   = xItem.getPortDBNode();
		VIRTPORT_TYPE xportVirtType = xItem.getVirtType();
		String        pinName       = null;

		for(Object obj: outputs) {
			SBoxTableItem yItem = (SBoxTableItem)obj;
			pinName = yItem.getText();
			allYData.put(pinName, new Double[xValues.length]);
			allYDataUnits.put(pinName, SBToolsEngine.getUnitMesure(yItem));
		}

		IEngineService engine  = (IEngineService) UnwServiceRegistry.instance.service(IEngineService.ID);
		
		Object curVal = null;
		
		switch (xportDBNode.getPort().getComputerType()) {
		case FLOAT:
			curVal = engine.get_f(xportDBNode, xportVirtType);			
			break;
		case INTEGER:
			curVal = engine.get_i(xportDBNode);			
			break;
		case STRING:
			curVal = engine.get_s(xportDBNode);			
			break;

		default:
			break;
		}
		boolean wasForced = engine.isForced(xportDBNode, xportVirtType);

		long time = new Date().getTime();
		int index = 0;
		for(Object xVal: xValues) {
			
			if (xVal instanceof Float)
				engine.force(xportDBNode, (Float)xVal, xportVirtType, true);
			else if (xVal instanceof Double)
				engine.force(xportDBNode, ((Double)xVal).floatValue(), xportVirtType, true);
			else if (xVal instanceof Integer)
				engine.force(xportDBNode, (Integer)xVal, xportVirtType, true);
			else if (xVal instanceof String)
				engine.force(xportDBNode, (String)xVal, xportVirtType, true);

			for(Object obj: outputs) {
				SBoxTableItem yItem = (SBoxTableItem)obj;
				Float yVal;
				
				if (obj instanceof SBoxTableItemList)
					yVal = engine.get_f(((SBoxTableItemList)yItem).getModule(), yItem.getVirtType());
				else
					yVal = engine.get_f(yItem.getPortDBNode(), yItem.getVirtType());
				
				allYData.get(yItem.getText())[index] = yVal.doubleValue();
			}
			index++;
		}

		System.out.println("!! execution time = " + (new Date().getTime() - time) + " ms");
		
		if (wasForced) {
			if (curVal instanceof Float)
				engine.force(xportDBNode, (Float)curVal, xportVirtType, true);
			else if (curVal instanceof Double)
				engine.force(xportDBNode, ((Double)curVal).floatValue(), xportVirtType, true);
			else if (curVal instanceof Integer)
				engine.force(xportDBNode, (Integer)curVal, xportVirtType, true);
			else if (curVal instanceof String)
				engine.force(xportDBNode, (String)curVal, xportVirtType, true);
		} else {
			engine.releaseForce((PortDBNode)xportDBNode, xportVirtType, true);			
		}

		return allYData;
	}

	private void createDataTable(Composite parent, TabItem tabItem) {
		Composite tableCompo = new Composite(parent, SWT.NONE);
		tableCompo.setLayout(new GridLayout(1, false));
		tableCompo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		ChartDataTable dataTable = new ChartDataTable(tableCompo);
		dataTableViewer = dataTable.getTableViewer();

		tabItem.setControl(tableCompo);

		final TabItem dataTableTabItem = tabItem;
		chartDataTabFolder.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				if(arg0.item == dataTableTabItem) {
					if(xDataNumbers != null)
						dataTableViewer.setInput(xDataNumbers);
					else if(xDataTokens != null)
						dataTableViewer.setInput(xDataTokens);
				}
			}
		});
	}

	@Override
	public void setFocus() {
	}

	public class ChartDataTable {
		private KTableViewer tableViewer;

		ChartDataTable(Composite parent){
			int tableStyle = SWTX.AUTO_SCROLL | SWTX.FILL_WITH_LASTCOL | 
			SWT.FLAT | SWT.BORDER;
			tableViewer = new KTableViewer(parent, null, tableStyle);

			tableViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			ColumnDescriptor[] colDesc = new ColumnDescriptor[(yData.size() + 1)];
			colDesc[0] = new ColumnDescriptor(xAxisTitle, ColumnType.TEXT, null);

			int idx = 1;
			for (String keyName: yData.keySet()) {
				colDesc[idx++] = new ColumnDescriptor(keyName, ColumnType.TEXT, null);
			}

			tableViewer.setColumnProperties(colDesc);

			tableViewer.getColumnDescriptor(0).setInitialWidth(150);
			tableViewer.getColumnDescriptor(0).setFormatType(KCELLFORMAT_TYPE.FLOAT);
			tableViewer.getColumnDescriptor(1).setFormatType(KCELLFORMAT_TYPE.FLOAT);

			tableViewer.setModelProvider(new ChartDataTableContentProvider());

			KTableViewerActionHandler actionHandler = new KTableViewerActionHandler(tableViewer);
			actionHandler.registerGlobalActions(getViewSite().getActionBars());
		}

		KTableViewer getTableViewer(){
			return tableViewer;
		}
	}


	public class ChartDataTableContentProvider extends KTableViewerModelProvider {
		@Override
		public Object[] getElements(Object input) {

			Object[] dataElems = null;
			if(xDataNumbers != null)
				dataElems = xDataNumbers;
			else if(xDataTokens != null)
				dataElems = xDataTokens;
			else return null;

			Integer[] indexs = new Integer[dataElems.length];
			for(int i=0; i < dataElems.length; i++)
				indexs[i] = i;

			return indexs;
		}

		public Object getDisplayValue(Object element, String property) {
			if(property.equals(xAxisTitle))
				if(xDataNumbers != null)
					return FormatNumber.scientific(xDataNumbers[(Integer)element],1, 2);
				else if(xDataTokens != null)
					return xDataTokens[(Integer)element];

			return FormatNumber.units(yData.get(property)[((Integer)element)],2, null);
		}
	}

	public static int getNumberOfDecimals(float fNumber) {
		if (fNumber == 0 || Float.isNaN(fNumber)) {
			return 0;
		}
		/* this code causes errors because it takes garbage from the number variable
		 * BigDecimal bigD = new BigDecimal(StrictMath.abs(fNumber));
			return bigD.scale();*/

		/*int counter = 0;
	  	double dN = StrictMath.abs(dNumber); // avoid errors because of the sign (-)*/

		// because it is not possible to get an "exact" result of the decimal part
		// by doing the simple substraction:
		// int intPart = (int)dNumber; // get the integer part of the double number
		// double decPart = dNumber - intPart; // get the decimal part

		// then we will have to do it some other way:
		String numberAsString = String.valueOf(fNumber);

		// get ride of negative sign (if exists)
		int signPos = numberAsString.indexOf("-");
		if (signPos >= 0) { // sign found?
			numberAsString = numberAsString.substring(signPos+1);
		}

		// get rid of the integer part including the point
		int pointPos = numberAsString.indexOf(".");
		numberAsString = numberAsString.substring(pointPos+1);

		// get rid of right-side zeros
		int lastZeroPos = numberAsString.length()-1;
		while (numberAsString.endsWith("0")) {
			numberAsString = numberAsString.substring(0, lastZeroPos);
			lastZeroPos = numberAsString.length()-1;
		}
		return numberAsString.length();
	}

	public static int getMultiplier(Double doubleObj, int power) {
		if(power <= 0) // avoid infinite loop
			power = 1;

		// validate number is ok
		if (doubleObj.isInfinite() || doubleObj.isNaN()) {
			return Integer.MAX_VALUE;
		}

		int multiplier = 0;
		double factor = 0;
		int entiere = 0;
		int numDecimals = 0;

		factor = StrictMath.pow(10, power);
		BigDecimal factorBD = new BigDecimal(factor);
		BigDecimal bigD = new BigDecimal(doubleObj.doubleValue());
		entiere = bigD.intValue();
		numDecimals = bigD.scale();
		if (entiere > 0) {
			if(bigD.doubleValue() < factor)
				multiplier = 1;

			while (bigD.doubleValue() >= factor) {
				multiplier += power;
				bigD = bigD.divide(factorBD);
			}
		}
		else {
			while ((entiere == 0) && (numDecimals > 0)) {
				multiplier -= power;
				bigD = bigD.multiply(factorBD);
				entiere = bigD.intValue();
				numDecimals = bigD.scale();
			}
		}
		return (multiplier * -1);
	}

	public static void setSelectedOutputs(ArrayList<Object> selectedOutputs) {
		SBoxWaveformView2.selectedOutputs = selectedOutputs;
	}

	public static void setSelectedInput(SBoxTableItem selectedInput) {
		SBoxWaveformView2.selectedInput = selectedInput;
	}

	static class LabelGenerator extends AbstractCategoryItemLabelGenerator implements CategoryItemLabelGenerator {
		private static final long serialVersionUID = 5417742257872444876L;
		/** The threshold. */
		/**
		 * Creates a new generator that only displays labels that are greater
		 * than or equal to the threshold value.
		 *
		 * @param threshold the threshold value.
		 */
		public LabelGenerator() {
			super("", NumberFormat.getInstance());
		}
		/**
		 * Generates a label for the specified item. The label is typically a
		 * formatted version of the data value, but any text can be used.
		 *
		 * @param dataset the dataset (<code>null</code> not permitted).
		 * @param series the series index (zero-based).
		 * @param category the category index (zero-based).
		 *
		 * @return the label (possibly <code>null</code>).
		 */
		public String generateLabel(CategoryDataset dataset,
				int series,
				int category) {
			Number value = dataset.getValue(series, category);

			return FormatNumber.units(value.floatValue(), 2, "");
		}
	}
	
	public static class  SBoxWaveInfo {
		public float min;
		public float max;
		
		public SBoxWaveInfo(float min, float max) {
			super();
			this.max = max;
			this.min = min;
		}
	}
	
	public static void setSboxWaveIngo(SBoxWaveInfo sboxWaveIngo) {
		SBoxWaveformView2.sboxWaveInfo = sboxWaveIngo;
	}

}

