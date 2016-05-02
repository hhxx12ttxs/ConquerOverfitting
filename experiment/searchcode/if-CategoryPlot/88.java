package Builders;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Formatter;
import java.util.ListIterator;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import BuilderPackage.JFreeChartBuilder;
public class JfreeLineChartBuilder extends JFreeChartBuilder {
	private String title;
	private DefaultTableModel tableModel = new DefaultTableModel();
	private DefaultCategoryDataset dataset;
	private Formatter bf = new Formatter();
	public JfreeLineChartBuilder(){

	}
	
	public void initiate(String title){
		this.title = title;
		createSeries();
		createLineChart(title);
		
		this.dimensionChart = new Dimension(540,140);
		this.dimensionTable = new Dimension(555,155);
	}
	
	private void createSeries(){
		dataset = new DefaultCategoryDataset();
        for (int i = 0; i < objects.size(); i++) {
    

        	for (String x : selection.getTimeIntervals()) {
        		dataset.addValue(objects.get(i).getData(x), objects.get(i).getType(), x);
			}
        }
	}
	private void createLineChart(String title) {  	
		chart = ChartFactory.createLineChart(title, null, null, dataset, PlotOrientation.VERTICAL, false, false, false);
		chart.setBackgroundPaint(Color.white);
		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.white);
		plot.setRangeGridlinePaint(Color.black);
		

		///LineAndShapeRenderer

		LineAndShapeRenderer r = (LineAndShapeRenderer) chart.getCategoryPlot().getRenderer();

		r.setSeriesPaint(0, new Color(64,105,156));  
		r.setSeriesPaint(1, new Color(158,65,62)); 
		r.setSeriesPaint(2, new Color(127,154,72)); 
		r.setSeriesPaint(3, new Color(105,81,133)); 
		r.setSeriesPaint(4, new Color(60,141,163)); 
		r.setSeriesPaint(5, new Color(204,123,56)); 
		r.setSeriesPaint(6, new Color(79,129,189)); 
		r.setSeriesPaint(7, new Color(198,214,172)); 
		r.setSeriesPaint(8, new Color(217,170,169));

		r.setDrawOutlines(true);      
	
		BasicStroke stroke = new BasicStroke(2);

		r.setSeriesStroke(0,stroke);
		r.setSeriesStroke(1,stroke);
		r.setSeriesStroke(2,stroke);
		r.setSeriesStroke(3,stroke);
		r.setSeriesStroke(4,stroke);
		r.setSeriesStroke(5,stroke);
		r.setSeriesStroke(6,stroke);
		r.setSeriesStroke(7,stroke);
		r.setSeriesStroke(8,stroke);

		CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
		domainAxis.setLowerMargin(0.0);
		domainAxis.setUpperMargin(0.0);
		domainAxis.setVisible(false);
		
		/*
		 * Sets the Y size and sets the interval to 10
		 */
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setRange(0.0, 100.0);
		rangeAxis.setTickUnit(new NumberTickUnit(10));
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		rangeAxis.setAutoRangeIncludesZero(true);
		
	}
	public ChartPanel getChart(){
		chartPanel = new ChartPanel(chart,false,false,false,false,false);
		chartPanel.setPreferredSize(dimensionChart);
		chartPanel.setMinimumSize(dimensionChart);
		setTableModel(true,false);

		return chartPanel;
	}
	private void setTableModel(boolean b, boolean c) {
		tableModel = new DefaultTableModel();
		ListIterator<String> columnKeys = dataset.getColumnKeys().listIterator();
		if(b)tableModel.addColumn("");
		tableModel.addColumn("");
		while(columnKeys.hasNext()){
			tableModel.addColumn(columnKeys.next());
			
		}		
		ListIterator<String> rowKeys = dataset.getRowKeys().listIterator();
		while(rowKeys.hasNext()){
			String tempRowElement = rowKeys.next();
			Vector list = new Vector<String>();
			if(b) list.add("");
			list.add(tempRowElement);
			columnKeys = dataset.getColumnKeys().listIterator();
			while(columnKeys.hasNext()){
				String temp = "";
				try{temp = dataset.getValue(tempRowElement, columnKeys.next()).toString();}catch(Exception e){temp ="";}
				if (temp.isEmpty()) {
					temp ="0";
				}

			}
			tableModel.addRow(list);
		}
		table.setModel(tableModel);


		table.getTableHeader().setPreferredSize(new Dimension(table.getColumnModel().getTotalColumnWidth(),50));

		table.getTableHeader().setFont(new Font("Arial",Font.PLAIN,9));
		if(c){
			table.getColumnModel().getColumn(0).setPreferredWidth(5);
			table.getColumnModel().getColumn(0).setMaxWidth(5);
			table.getColumnModel().getColumn(1).setPreferredWidth(115);
			table.getColumnModel().getColumn(1).setMinWidth(115);
			table.getColumnModel().getColumn(1).setMaxWidth(115);
		}else{
			table.getColumnModel().getColumn(0).setPreferredWidth(113);	
			table.getColumnModel().getColumn(0).setMinWidth(113);
			table.getColumnModel().getColumn(0).setMaxWidth(113);
		}
		
	}
	private JTable table = new JTable(){
		private static final long serialVersionUID = 1L;
		public boolean isCellEditable(int rowIndex, int colIndex) {
			return false;
		}
	};

}

