package jp.co.geo.logviewer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.experimental.chart.swt.ChartComposite;
 
public class JFreeChartComposite extends Shell {
 
	private DefaultPieDataset dataset;
	private JFreeChart piechart;
	private ChartComposite composite;
 
	public JFreeChartComposite(Display display) {
		super(display, SWT.SHELL_TRIM);
		setLayout(new FillLayout(SWT.HORIZONTAL));
		composite = new ChartComposite(this, SWT.NONE);
		composite.setLayout(new FillLayout(SWT.HORIZONTAL));
 
		dataset = new DefaultPieDataset();
		createContents();
	}
 
	protected void createContents() {
		setSize(640, 430);
		dataset.setValue("Java", new Double(20));
		dataset.setValue("C/C++", new Double(15.6));
		dataset.setValue("CSharp", new Double(24.4));
		dataset.setValue("Delphi", new Double(30.0));
		dataset.setValue("VB", new Double(20.0));
 
		piechart = ChartFactory.createPieChart("Programmer Population", dataset, true, true, false);
		PiePlot plot = (PiePlot) piechart.getPlot();
		plot.setCircular(true);
 
		composite.setChart(piechart);
	}
 
	public static void main(String args[]) {
		try {
			Display display = Display.getDefault();
			JFreeChartComposite shell = new JFreeChartComposite(display);
			shell.open();
			shell.layout();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
 
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
 
}
